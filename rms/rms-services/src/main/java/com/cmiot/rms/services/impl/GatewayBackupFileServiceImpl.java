package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.acs.model.Download;
import com.cmiot.acs.model.Upload;
import com.cmiot.fileserver.service.RmsBackupService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.GatewayBackupFileInfoMapper;
import com.cmiot.rms.dao.mapper.GatewayBackupFileTaskDetailMapper;
import com.cmiot.rms.dao.model.GatewayBackupFileInfo;
import com.cmiot.rms.dao.model.GatewayBackupFileTaskDetail;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.GatewayBackupFileService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.instruction.InvokeInsService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/6/6.
 */
public class GatewayBackupFileServiceImpl implements GatewayBackupFileService {

    @Autowired
    GatewayInfoService gatewayInfoService;

    @Autowired
    GatewayBackupFileInfoMapper gatewayBackupFileInfoMapper;

    @Autowired
    InvokeInsService invokeInsService;

    @Autowired
    RmsBackupService rmsBackupService;

    @Autowired
    GatewayBackupFileTaskDetailMapper gatewayBackupFileTaskDetailMapper;

    @Value("${gateway.backup.file.default.maxNumber}")
    int defaultMaxNumber;

    private final Logger logger = LoggerFactory.getLogger(GatewayBackupFileServiceImpl.class);

    /**
     * 网关数据备份
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> backup(Map<String, Object> parameter) {
        logger.info("Start invoke backup:{}", parameter);

        String gatewayId = null != parameter.get("gatewayId") ? parameter.get("gatewayId").toString() : "";
        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isBlank(gatewayId)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke backup:{}", retMap);
            return retMap;
        }

        try {

            GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayId);
            if (null == gatewayInfo) {
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_ERROR.getResultMsg());
                logger.info("End invoke backup:{}", retMap);
                return retMap;
            }

            if(StringUtils.isBlank(gatewayInfo.getGatewayStatus())
                    || StringUtils.isBlank(gatewayInfo.getGatewayConnectionrequesturl()))
            {
                retMap.put(Constant.CODE, ErrorCodeEnum.NO_REGISTER.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.NO_REGISTER.getResultMsg());
                logger.info("End invoke backup:{}", retMap);
                return retMap;
            }

            //判断网关是否在备份中
            int backupCount = gatewayBackupFileInfoMapper.selectBackupCount(gatewayId);
            if(backupCount > 0)
            {
                retMap.put(Constant.CODE, ErrorCodeEnum.FILE_BACKUPING.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.FILE_BACKUPING.getResultMsg());
                logger.info("End invoke backup:{}", retMap);
                return retMap;
            }

            Integer maxNum = gatewayInfo.getBackupFileMaxNumber();
            if (null == maxNum || maxNum < 1) {
                maxNum = defaultMaxNumber;
            }

            //1.判断是否超过限制
            int currentNumber = gatewayBackupFileInfoMapper.selectCountByGatewayId(gatewayId);
            if (currentNumber >= maxNum) {
                retMap.put(Constant.CODE, ErrorCodeEnum.EXCEED_MAX_NUMBER.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEED_MAX_NUMBER.getResultMsg());
                logger.info("End invoke backup:{}", retMap);
                return retMap;
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
            uploadMap.put("fileType", "3 Vendor Configuration File");
            uploadMap.put("url", retFileServer.get("URL"));
            uploadMap.put("userName", "");
            uploadMap.put("passWord", "");
            uploadMap.put("delaySeconds", 0);

            logger.info("backup start invoke executeOne:{}", uploadMap);
            Map<String, Object> respMap = invokeInsService.executeOne(uploadMap);
            logger.info("backup end invoke executeOne:{}", respMap);

            if (null == respMap || (0 != (Integer) respMap.get("resultCode"))) {
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.INSTRUCT_SEND_FAILED.getResultMsg());
                retMap.put(Constant.CODE, ErrorCodeEnum.INSTRUCT_SEND_FAILED.getResultCode());
                return retMap;
            }
        } catch (Exception e) {
            retMap.put(Constant.CODE, ErrorCodeEnum.INSTRUCT_SEND_EXCEPTION.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.INSTRUCT_SEND_EXCEPTION.getResultMsg());
            return retMap;
        }

        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        logger.info("End invoke backup:{}", retMap);
        return retMap;
    }

    /**
     * 网关数据恢复
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> restore(Map<String, Object> parameter) {
        logger.info("Start invoke restore:{}", parameter);

        String gatewayId = null != parameter.get("gatewayId") ? parameter.get("gatewayId").toString() : "";
        String backupFileId = null != parameter.get("backupFileId") ? parameter.get("backupFileId").toString() : "";
        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isBlank(gatewayId) || StringUtils.isBlank(backupFileId)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke restore:{}", retMap);
            return retMap;
        }

        GatewayBackupFileInfo info = gatewayBackupFileInfoMapper.selectByPrimaryKey(backupFileId);
        if(null == info)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.BACKUP_FILE_NOT_EXIST.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.BACKUP_FILE_NOT_EXIST.getResultMsg());
            logger.info("End invoke restore:{}", retMap);
            return retMap;
        }
        if(info.getStatus() == 1)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.FILE_BACKUP_NO_COMPLETE.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.FILE_BACKUP_NO_COMPLETE.getResultMsg());
            logger.info("End invoke restore:{}", retMap);
            return retMap;
        }else if(info.getStatus() == 3)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.FILE_BACKUP_NO_SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.FILE_BACKUP_NO_SUCCESS.getResultMsg());
            logger.info("End invoke restore:{}", retMap);
            return retMap;
        }

        Map<String, Object> upgradeJob = new HashMap<>();

        upgradeJob.put("gatewayId", gatewayId);
        upgradeJob.put("commandKey", "");
        upgradeJob.put("methodName", "Download");
        upgradeJob.put("fileType", Download.FT_CONFIG);
        upgradeJob.put("url", info.getFilePath());
        upgradeJob.put("userName", info.getUserName());
        upgradeJob.put("passWord", info.getPassword());
        upgradeJob.put("targetFileName", info.getFileName());
        upgradeJob.put("successURL", "");
        upgradeJob.put("failureURL", "");
        upgradeJob.put("fileSize", info.getFileSize());
        upgradeJob.put("delaySeconds", "0");
        try {
            logger.info("restore start invoke executeOne:{}", upgradeJob);
            Map<String, Object> respMap = invokeInsService.executeOne(upgradeJob);
            logger.info("restore end invoke executeOne:{}", respMap);

            if (null == respMap || (0 != (Integer) respMap.get("resultCode"))) {
                retMap.put(Constant.CODE, ErrorCodeEnum.INSTRUCT_SEND_FAILED.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.INSTRUCT_SEND_FAILED.getResultMsg());
                return retMap;
            }
        } catch (Exception e) {
            retMap.put(Constant.CODE, ErrorCodeEnum.INSTRUCT_SEND_EXCEPTION.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.INSTRUCT_SEND_EXCEPTION.getResultMsg());
            return retMap;
        }

        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        logger.info("End invoke restore:{}", retMap);
        return retMap;
    }

    /**
     * 查询备份文件列表
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryBackupFileList(Map<String, Object> parameter) {
        logger.info("Start invoke queryBackupFileList:{}", parameter);

        String gatewayId = null != parameter.get("gatewayId") ? parameter.get("gatewayId").toString() : "";
        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isBlank(gatewayId)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke queryBackupFileList:{}", retMap);
            return retMap;
        }

        List<GatewayBackupFileInfo> infos = gatewayBackupFileInfoMapper.selectListByGatewayId(gatewayId);

        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        retMap.put(Constant.DATA, JSON.toJSON(infos));
        logger.info("End invoke queryBackupFileList:{}", retMap);
        return retMap;
    }

    /**
     * 设置网关备份文件数量限制
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> saveFileNumberLimit(Map<String, Object> parameter) {
        logger.info("Start invoke setFileNumberLimit:{}", parameter);
        String gatewayIds = null != parameter.get("gatewayIds") ? parameter.get("gatewayIds").toString() : "";
        int maxNumber = null != parameter.get("maxNumber") ? Integer.valueOf(parameter.get("maxNumber").toString()) : 0;
        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isBlank(gatewayIds) || (0 == maxNumber)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke setFileNumberLimit:{}", retMap);
            return retMap;
        }
        gatewayInfoService.updateBackupFileMaxNumber(gatewayIds, maxNumber);

        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        logger.info("End invoke saveFileNumberLimit:{}", retMap);
        return retMap;
    }

    /**
     * 文件服务器在文件上传完成后回调该接口更新文件大小、名称、状态
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> updateBackupFileInfo(Map<String, Object> parameter) {
        logger.info("Start invoke UpdateBackupFileInfo:{}", parameter);
        String gatewayId = null != parameter.get("gatewayId") ? parameter.get("gatewayId").toString() : "";
        String backupFileId = null != parameter.get("backupFileId") ? parameter.get("backupFileId").toString() : "";
        String fileName = null != parameter.get("fileName") ? parameter.get("fileName").toString() : "";
        String filePath = null != parameter.get("filePath") ? parameter.get("filePath").toString() : "";
        Double fileSize = null != parameter.get("fileSize") ? Double.valueOf(parameter.get("fileSize").toString()) : 0;
        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isBlank(gatewayId) || StringUtils.isBlank(backupFileId)
                || StringUtils.isBlank(fileName) || StringUtils.isBlank(filePath)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke UpdateBackupFileInfo:{}", retMap);
            return retMap;
        }

        GatewayBackupFileInfo info = gatewayBackupFileInfoMapper.selectByPrimaryKey(backupFileId);
        if(null == info)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.BACKUP_FILE_NOT_EXIST.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.BACKUP_FILE_NOT_EXIST.getResultMsg());
            logger.info("End invoke UpdateBackupFileInfo:{}", retMap);
            return retMap;
        }
        info.setFileName(fileName);
        info.setFileSize(fileSize);
        info.setFilePath(filePath);
        info.setEndTime(String.valueOf(System.currentTimeMillis()));
        info.setStatus(2);

        gatewayBackupFileInfoMapper.updateByPrimaryKey(info);

        //根据文件ID更新状态
        GatewayBackupFileTaskDetail detail = new GatewayBackupFileTaskDetail();
        detail.setStatus(2);
        detail.setBackupFileId(backupFileId);
        gatewayBackupFileTaskDetailMapper.updateByFileId(detail);

        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        logger.info("End invoke UpdateBackupFileInfo:{}", retMap);
        return retMap;
    }

    /**
     * 删除备份文件
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> deleteBackupFile(Map<String, Object> parameter) {
        logger.info("Start invoke deleteBackupFile:{}", parameter);
        String gatewayId = null != parameter.get("gatewayId") ? parameter.get("gatewayId").toString() : "";
        String backupFileId = null != parameter.get("backupFileId") ? parameter.get("backupFileId").toString() : "";
        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isBlank(gatewayId) || StringUtils.isBlank(backupFileId)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke deleteBackupFile:{}", retMap);
            return retMap;
        }

        GatewayBackupFileInfo info = gatewayBackupFileInfoMapper.selectByPrimaryKey(backupFileId);
        if(null == info)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.BACKUP_FILE_NOT_EXIST.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.BACKUP_FILE_NOT_EXIST.getResultMsg());
            logger.info("End invoke deleteBackupFile:{}", retMap);
            return retMap;
        }
        Map<String, String> retFileServer = rmsBackupService.deleteConfigFile(gatewayId, backupFileId, info.getFileName());
        if(retFileServer.get("result").toString().equals("success"))
        {
            gatewayBackupFileInfoMapper.deleteByPrimaryKey(backupFileId);
        }
        else
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.DELETE_FILE_FAILED.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.DELETE_FILE_FAILED.getResultMsg());
            logger.info("End invoke deleteBackupFile:{}", retMap);
        }
        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        logger.info("End invoke deleteBackupFile:{}", retMap);
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
        info.setFilePath(map.get("URL").toString());
        info.setUserName("");
        info.setPassword("");
        info.setStartTime(String.valueOf(System.currentTimeMillis()));
        info.setStatus(1);
        return info;
    }
}
