package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.enums.UpgradeTaskDetailStatusEnum;
import com.cmiot.rms.common.enums.UpgradeTaskEventEnum;
import com.cmiot.rms.common.enums.UpgradeTaskStatusEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.FirmwareInfoMapper;
import com.cmiot.rms.dao.mapper.FirmwareUpgradeTaskDetailMapper;
import com.cmiot.rms.dao.mapper.FirmwareUpgradeTaskMapper;
import com.cmiot.rms.dao.model.FirmwareInfo;
import com.cmiot.rms.dao.model.FirmwareUpgradeTaskDetail;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.InstructionsInfoWithBLOBs;
import com.cmiot.rms.services.FirmwareUpgradeTaskInnerService;
import com.cmiot.rms.services.FirmwareUpgradeTaskService;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.instruction.AbstractInstruction;
import com.cmiot.rms.services.instruction.impl.DownloadInstruction;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/5/18.
 */
@Service("firmwareUpgradeTaskInnerService")
public class FirmwareUpgradeTaskInnerServiceImpl implements FirmwareUpgradeTaskInnerService {

    private static Logger logger = LoggerFactory.getLogger(FirmwareUpgradeTaskInnerServiceImpl.class);

    @Autowired
    FirmwareUpgradeTaskDetailMapper detailMapper;

    @Autowired
    FirmwareUpgradeTaskService service;

    @Autowired
    FirmwareUpgradeTaskMapper firmwareUpgradeTaskMapper;

    @Autowired
    FirmwareInfoMapper firmwareInfoMapper;

    @Autowired
    private InstructionsService instructionsService;

    @Value("${file.server.userName}")
    String userName;

    @Value("${file.server.password}")
    String password;

    /**
     * 网关上报Inform信息时，判断是否配置相关时间的升级任务，如有，执行升级任务
     * 目前存在3中事件可以配置升级任务：1:初始安装第一次启动时 2：周期心跳上报时 3：设备重新启动时
     *
     * @param gatewayInfo
     * @param event
     */
    @Override
    public Boolean executeUpgradeTask(GatewayInfo gatewayInfo, UpgradeTaskEventEnum event) {
        logger.info("Start invoke excuteUpgradeTask:{},{},{}", gatewayInfo.getGatewaySerialnumber(), gatewayInfo.getGatewayFactoryCode(), event);
        Map<String, Object> para = new HashMap<>();
        para.put("gatewayId", gatewayInfo.getGatewayUuid());
        para.put("event", event.code());
        List<FirmwareUpgradeTaskDetail> details = detailMapper.selectGateWayInfo(para);
        if(null == details || details.size() == 0)
        {
            logger.info("no task need to execute!");
            return false;
        }

        //多个时执行一个
        FirmwareUpgradeTaskDetail detail = details.get(0);

        Map<String, Object> servicePara = new HashMap<>();
        servicePara.put("gatewayId", detail.getGatewayId());
        servicePara.put("firmwareId", detail.getFirmwareId());
        servicePara.put("taskId", detail.getUpgradeTaskId());
        Map<String, Object> retMap =service.upgradeImmediately(servicePara);
        if(Integer.valueOf(retMap.get(Constant.CODE).toString()) == ErrorCodeEnum.SUCCESS.getResultCode())
        {
            Map<String, Object> paramter = new HashMap<>();
            paramter.put("id", detail.getUpgradeTaskId());
            paramter.put("taskStatus", UpgradeTaskStatusEnum.PROCESSING.code());
            firmwareUpgradeTaskMapper.updateStatus(paramter);

            detail.setStatus(UpgradeTaskDetailStatusEnum.PROCESSING.code());
            detail.setUpgradeStartTime(DateTools.getCurrentSecondTime());
            detailMapper.updateByPrimaryKeySelective(detail);
        }

        logger.info("End invoke excuteUpgradeTask!{}", retMap);
        return true;
    }

    /**
     * 获取升级任务指令
     * @param gatewayInfo
     * @return
     */
    @Override
    public AbstractMethod getUpgradeInstruction(GatewayInfo gatewayInfo) {
        Map<String, Object> para = new HashMap<>();
        para.put("gatewayId", gatewayInfo.getGatewayUuid());
        para.put("event", UpgradeTaskEventEnum.BOOTSTRAP.code());
        List<FirmwareUpgradeTaskDetail> details = detailMapper.selectGateWayInfo(para);
        if(null == details || details.size() == 0)
        {
            logger.info("no upgrade task!");
            return null;
        }

        //多个时执行一个
        FirmwareUpgradeTaskDetail detail = details.get(0);
        FirmwareInfo firmwareInfo = firmwareInfoMapper.selectByPrimaryKey(detail.getFirmwareId());
        if (null == firmwareInfo) {
            return null;
        }

        Map<String, Object> upgradeJob = new HashMap<>();

        upgradeJob.put("fileType", "1 Firmware Upgrade Image");
        upgradeJob.put("url", firmwareInfo.getFirmwarePath());
        upgradeJob.put("userName", userName);
        upgradeJob.put("passWord", password);
        upgradeJob.put("targetFileName", firmwareInfo.getFirmwareName());
        upgradeJob.put("successURL", "");
        upgradeJob.put("failureURL", "");
        upgradeJob.put("fileSize", firmwareInfo.getFirmwareSize());
        upgradeJob.put("delaySeconds", "0");

        InstructionsInfoWithBLOBs is = new InstructionsInfoWithBLOBs();
        //生成指令ID作为请求的requestId
        String insId = UniqueUtil.uuid();
        is.setInstructionsId(insId);
        is.setCpeIdentity(detail.getGatewayId());

        upgradeJob.put("commandKey", insId);

        AbstractInstruction ins = new DownloadInstruction();

        AbstractMethod abstractMethod = ins.createIns(is, gatewayInfo, upgradeJob);

        if(StringUtils.isNotBlank(gatewayInfo.getGatewayConnectionrequestUsername())
                && StringUtils.isNotBlank(gatewayInfo.getGatewayConnectionrequestPassword()))
        {
            abstractMethod.setCpeUserName(gatewayInfo.getGatewayConnectionrequestUsername());
            abstractMethod.setCpePassword(gatewayInfo.getGatewayConnectionrequestPassword());
        }

        //down命令包含任务ID时，需要储存任务ID，供返回时更新状态使用
        JSONObject beforeContent = JSON.parseObject(JSON.toJSONString(abstractMethod));
        beforeContent.put("taskId", detail.getUpgradeTaskId());
        is.setInstructionsBeforeContent(beforeContent.toJSONString());

        instructionsService.addInstructionsInfo(is);

        //更新状态
        Map<String, Object> paramter = new HashMap<>();
        paramter.put("id", detail.getUpgradeTaskId());
        paramter.put("taskStatus", UpgradeTaskStatusEnum.PROCESSING.code());
        firmwareUpgradeTaskMapper.updateStatus(paramter);

        detail.setStatus(UpgradeTaskDetailStatusEnum.PROCESSING.code());
        detail.setUpgradeStartTime(DateTools.getCurrentSecondTime());
        detailMapper.updateByPrimaryKeySelective(detail);

        return abstractMethod;
    }
}
