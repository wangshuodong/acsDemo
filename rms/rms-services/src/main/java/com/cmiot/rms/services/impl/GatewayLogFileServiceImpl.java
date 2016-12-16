package com.cmiot.rms.services.impl;

import com.cmiot.acs.model.Upload;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.fileserver.service.RmsBackupService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.GatewayLogFileInfoMapper;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.GatewayLogFileInfo;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.GatewayLogFileService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.instruction.InvokeInsService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/6/8.
 */
public class GatewayLogFileServiceImpl implements GatewayLogFileService {

    private final Logger logger = LoggerFactory.getLogger(GatewayLogFileServiceImpl.class);

    @Autowired
    InstructionMethodService instructionMethodService;

    @Autowired
    InvokeInsService invokeInsService;

    @Autowired
    GatewayLogFileInfoMapper gatewayLogFileInfoMapper;

    @Autowired
    RmsBackupService rmsBackupService;


    @Autowired
    GatewayInfoService gatewayInfoService;

    /**
     * 设置网关日志开关
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> settingLogSwitch(Map<String, Object> parameter) {
        logger.info("Start invoke SettingLogSwitch:{}", parameter);
        String gatewayId = null != parameter.get("gatewayId") ? parameter.get("gatewayId").toString() : "";
        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isBlank(gatewayId)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke SettingLogSwitch:{}", retMap);
            return retMap;
        }

        Boolean status = null != parameter.get("status") ? Boolean.valueOf(parameter.get("status").toString()) : true;

        List<ParameterValueStruct> structs = new ArrayList<>();
        ParameterValueStruct struct = new ParameterValueStruct();
        struct.setName("InternetGatewayDevice.DeviceInfo.X_CMCC_Syslog.Enable");
        struct.setValue(status);
        struct.setValueType("boolean");
        struct.setReadWrite(true);
        structs.add(struct);

        GatewayInfo info = gatewayInfoService.selectByUuid(gatewayId);
        if(null == info)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.GATEWAY_NOT_EXIST.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.GATEWAY_NOT_EXIST.getResultMsg());
            return retMap;
        }

        if(StringUtils.isBlank(info.getGatewayStatus())
                || StringUtils.isBlank(info.getGatewayConnectionrequesturl()))
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.NO_REGISTER.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.NO_REGISTER.getResultMsg());
            return retMap;
        }

        Boolean isSuccess = instructionMethodService.setParameterValue(info.getGatewayMacaddress(), structs);
        if(isSuccess)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
            gatewayInfoService.updateLogSwitchStatus(info.getGatewayUuid(), status ? 1 : 0);
        }
        else
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.SET_PARAMETR_VALUE_FAILED.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SET_PARAMETR_VALUE_FAILED.getResultMsg());
        }
        logger.info("End invoke SettingLogSwitch:{}", retMap);
        return retMap;
    }

    /**
     * 上传日志文件
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> uploadLogFile(Map<String, Object> parameter) {
        logger.info("Start invoke uploadLogFile:{}", parameter);
        String gatewayId = null != parameter.get("gatewayId") ? parameter.get("gatewayId").toString() : "";
        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isBlank(gatewayId)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke uploadLogFile:{}", retMap);
            return retMap;
        }
        try {
            GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayId);
            if(null == gatewayInfo)
            {
                retMap.put(Constant.CODE, ErrorCodeEnum.GATEWAY_NOT_EXIST.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.GATEWAY_NOT_EXIST.getResultMsg());
                return retMap;
            }

            if(StringUtils.isBlank(gatewayInfo.getGatewayStatus())
                    || StringUtils.isBlank(gatewayInfo.getGatewayConnectionrequesturl()))
            {
                retMap.put(Constant.CODE, ErrorCodeEnum.NO_REGISTER.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.NO_REGISTER.getResultMsg());
                return retMap;
            }

            //日志文件信息表中的主键ID
            String id = UniqueUtil.uuid();

            //调用接口返回备份路径和用户名、密码
            Map<String, String> retFileServer =  rmsBackupService.uploadLogFile(gatewayId, id);
            logger.info("rmsBackupService uploadConfigFile:{}", retFileServer);

            GatewayLogFileInfo info = buildLogFileInfo(id, gatewayId, retFileServer);
            gatewayLogFileInfoMapper.insert(info);

            //下发upload命令
            Map<String, Object> uploadMap = new HashMap<>();
            uploadMap.put("gatewayId", gatewayId);
            uploadMap.put("methodName", "Upload");
            uploadMap.put("commandKey", "");
            uploadMap.put("fileType", Upload.FT_LOG);
            uploadMap.put("url", retFileServer.get("URL"));
            uploadMap.put("userName", "");
            uploadMap.put("passWord", "");
            uploadMap.put("delaySeconds", 0);

            logger.info("backup start invoke executeOne:{}", uploadMap);
            Map<String, Object> respMap = invokeInsService.executeOne(uploadMap);
            logger.info("backup end invoke executeOne:{}", respMap);

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
        logger.info("End invoke uploadLogFile:{}", retMap);
        return retMap;
    }

    /**
     * 文件服务器在文件上传完成后回调该接口更新文件大小、名称、状态
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> updateLogFileInfo(Map<String, Object> parameter) {
        logger.info("Start invoke updateLogFileInfo:{}", parameter);
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
            logger.info("End invoke updateLogFileInfo:{}", retMap);
            return retMap;
        }

        GatewayLogFileInfo info = gatewayLogFileInfoMapper.selectByPrimaryKey(backupFileId);
        if(null == info)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.BACKUP_FILE_NOT_EXIST.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.BACKUP_FILE_NOT_EXIST.getResultMsg());
            logger.info("End invoke updateLogFileInfo:{}", retMap);
            return retMap;
        }
        info.setFileName(fileName);
        info.setFileSize(fileSize);
        info.setFilePath(filePath);
        info.setEndTime(String.valueOf(System.currentTimeMillis()));
        info.setStatus(2);

        gatewayLogFileInfoMapper.updateByPrimaryKey(info);

        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        logger.info("End invoke updateLogFileInfo:{}", retMap);
        return retMap;
    }

    @Override
    public Map<String, Object> queryList4Page(Map<String, Object> parameter) {
        Map<String, Object> backMap = new HashMap<>();
        int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
        int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 3;
        String gatewayId = (String) parameter.get("gatewayId");
        if(StringUtils.isBlank(gatewayId)){
            backMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
            backMap.put(Constant.MESSAGE,"网关ID不能为空");
        }
        PageHelper.startPage(page, pageSize);

        try {
            List<GatewayLogFileInfo> list = gatewayLogFileInfoMapper.selectGatewayLogFileInfoList(gatewayId);
            backMap.put("page", page);
            backMap.put("pageSize", pageSize);
            backMap.put("total", ((Page) list).getTotal());
            backMap.put("list", list);
            backMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            backMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
            backMap.put(Constant.MESSAGE,"查询日志失败");
        }
        return backMap;
    }

    /**
     * 构造GatewayLogFileInfo对象
     * @param id
     * @param gatewayId
     * @param map
     * @return
     */
    private GatewayLogFileInfo buildLogFileInfo(String id, String gatewayId, Map<String, String> map)
    {
        GatewayLogFileInfo info = new GatewayLogFileInfo();
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
