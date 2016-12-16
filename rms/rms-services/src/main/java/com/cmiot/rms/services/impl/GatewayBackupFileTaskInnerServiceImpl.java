package com.cmiot.rms.services.impl;


import com.cmiot.fileserver.service.RmsBackupService;
import com.cmiot.rms.common.enums.UpgradeTaskEventEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.GatewayBackupFileInfoMapper;
import com.cmiot.rms.dao.mapper.GatewayBackupFileTaskDetailMapper;
import com.cmiot.rms.dao.mapper.GatewayBackupFileTaskMapper;
import com.cmiot.rms.dao.model.GatewayBackupFileInfo;
import com.cmiot.rms.dao.model.GatewayBackupFileTask;
import com.cmiot.rms.dao.model.GatewayBackupFileTaskDetail;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.GatewayBackupFileTaskInnerService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.instruction.InvokeInsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/6/8.
 */
@Service("gatewayBackupFileTaskInnerService")
public class GatewayBackupFileTaskInnerServiceImpl implements GatewayBackupFileTaskInnerService {

    private static Logger logger = LoggerFactory.getLogger(FirmwareUpgradeTaskInnerServiceImpl.class);

    @Autowired
    GatewayBackupFileTaskMapper gatewayBackupFileTaskMapper;

    @Autowired
    GatewayBackupFileInfoMapper gatewayBackupFileInfoMapper;

    @Autowired
    GatewayInfoService gatewayInfoService;

    @Autowired
    InvokeInsService invokeInsService;

    @Autowired
    RmsBackupService rmsBackupService;

    @Autowired
    GatewayBackupFileTaskDetailMapper gatewayBackupFileTaskDetailMapper;

    @Value("${gateway.backup.file.default.maxNumber}")
    int defaultMaxNumber;

    /**
     * 网关上报Inform信息时，判断是否配置相关事件的升级任务，如有，执行升级任务
     * 目前存在4种事件可以配置升级任务：1:初始安装第一次启动时 2：周期心跳上报时 3：设备重新启动时 4：参数改变
     *
     * @param sn
     * @param oui
     * @param event
     */
    @Override
    public Boolean executeUpgradeTask(String sn, String oui, UpgradeTaskEventEnum event) {
        logger.info("Start invoke excuteUpgradeTask:{},{},{}", sn, oui, event);
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewaySerialnumber(sn);
        searchInfo.setGatewayFactoryCode(oui);

        GatewayInfo info = gatewayInfoService.selectGatewayInfo(searchInfo);
        if(null == info)
        {
            return false;
        }

        Map<String, Object> para = new HashMap<>();
        para.put("areaId", info.getGatewayAreaId());
        para.put("event", event.code());
        List<GatewayBackupFileTask> tasks = gatewayBackupFileTaskMapper.selectTask(para);
        if(null == tasks || tasks.size() < 1)
        {
          return false;
        }
        GatewayBackupFileTask task = tasks.get(0);
        Map<String, String> backupMap  = backup(info.getGatewayUuid());
        if(backupMap.get("result").equals("0"))
        {
            if(task.getStatus() != 2)
            {
                task.setStatus(2);
                gatewayBackupFileTaskMapper.updateByPrimaryKeySelective(task);
            }

            GatewayBackupFileTaskDetail detail = buildDetail(task.getId(), info.getGatewayUuid(), backupMap.get("fileId"));
            gatewayBackupFileTaskDetailMapper.insert(detail);
        }
        logger.info("End invoke excuteUpgradeTask!");
        return true;
    }

    /**
     * 定时任务备份
     * @param gatewayId
     */
    @Override
    public Map<String, String> backup(String gatewayId)
    {
        //1.判断是否超过限制
        GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayId);

        Map<String, String> retMap = new HashMap<>();

        //判断网关是否在备份中
        int backupCount = gatewayBackupFileInfoMapper.selectBackupCount(gatewayId);
        if(backupCount > 0)
        {
            retMap.put("result", "1");
            logger.info("网关正在备份中,本次备份任务不执行:{}", retMap);
            return retMap;
        }

        Integer maxNum = gatewayInfo.getBackupFileMaxNumber();
        if (null == maxNum || maxNum < 1) {
            maxNum = defaultMaxNumber;
        }

        int currentNumber = gatewayBackupFileInfoMapper.selectCountByGatewayId(gatewayId);
        if (currentNumber >= maxNum) {

            List<GatewayBackupFileInfo> infos = gatewayBackupFileInfoMapper.selectListByGatewayId(gatewayId);

            //获取最早的备份文件并删除
            GatewayBackupFileInfo info = infos.get(infos.size() -1);

            Map<String, String> retFileServer = rmsBackupService.deleteConfigFile(gatewayId, info.getId(), info.getFileName());
            if(retFileServer.get("result").toString().equals("success"))
            {
                gatewayBackupFileInfoMapper.deleteByPrimaryKey(info.getId());
            }
        }


        //备份文件信息表中的主键ID
        String id = UniqueUtil.uuid();

        //2.调用接口返回备份路径和用户名、密码
        Map<String, String> retFileServer =  rmsBackupService.uploadConfigFile(gatewayId, id);
        logger.info("rmsBackupService uploadConfigFile:{}", retFileServer);

        GatewayBackupFileInfo info = buildBackupFileInfo(id, gatewayId, retFileServer);
        gatewayBackupFileInfoMapper.insert(info);

        //3.下发upload命令
        Map<String, Object> uploadMap = new HashMap<>();
        uploadMap.put("gatewayId", gatewayId);
        uploadMap.put("methodName", "Upload");
        uploadMap.put("commandKey", "");
        uploadMap.put("fileType",  "3 Vendor Configuration File");
        uploadMap.put("url", retFileServer.get("URL"));
        uploadMap.put("userName", retFileServer.get("Username"));
        uploadMap.put("passWord", retFileServer.get("Password"));
        uploadMap.put("delaySeconds", 0);
        try {
            logger.info("backup start invoke executeOne:{}", uploadMap);
            Map<String, Object> respMap = invokeInsService.executeOne(uploadMap);
            logger.info("backup end invoke executeOne:{}", respMap);

            if (null != respMap && (0 == (Integer) respMap.get("resultCode"))) {
                retMap.put("result", "0");
                retMap.put("fileId", id);
                return retMap;
            }
        } catch (Exception e) {
            logger.error("backup exception:{}", e);

        }
        retMap.put("result", "1");
        return retMap;
    }


    /**
     * 构成备份文件对象
     * @return
     */
    private GatewayBackupFileInfo buildBackupFileInfo(String id, String gatewayId, Map<String, String> map)
    {
        GatewayBackupFileInfo info = new GatewayBackupFileInfo();
        info.setId(id);
        info.setGatewayId(gatewayId);
        info.setUserName("");
        info.setPassword("");
        info.setFilePath(map.get("URL").toString());
        info.setStartTime(String.valueOf(System.currentTimeMillis()));
        info.setStatus(1);
        return info;
    }

    /**
     * 构造任务详情对象
     * @return
     */
    private GatewayBackupFileTaskDetail buildDetail(String taskId, String gatewayId, String fileId)
    {
        GatewayBackupFileTaskDetail detail = new GatewayBackupFileTaskDetail();
        detail.setId(UniqueUtil.uuid());
        detail.setTaskId(taskId);
        detail.setGatewayId(gatewayId);
        detail.setBackupFileId(fileId);
        detail.setStatus(1);
        return detail;
    }
}
