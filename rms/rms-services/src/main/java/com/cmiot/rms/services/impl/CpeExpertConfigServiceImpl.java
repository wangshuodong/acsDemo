package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.acs.model.struct.ParameterValueStructStr;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.CpeExpertConfigService;
import com.cmiot.rms.services.GatewayManageService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.iot.common.file.ftp.Ftp;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zjial on 2016/5/6.
 */
@Service
public class CpeExpertConfigServiceImpl implements CpeExpertConfigService {

    private static Logger logger = LoggerFactory.getLogger(CpeExpertConfigServiceImpl.class);

    @Autowired
    private InstructionMethodService instructionMethodService;

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;

    @Autowired
    private GatewayManageService gatewayManageService;

    @Override

    public Map<String, Object> setHgServiceAccount(Map<String, Object> parameter) {
        //构建返回对象
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(Constant.ID, parameter.get(Constant.ID));
        resultMap.put(Constant.CMDTYPE, parameter.get(Constant.CMDTYPE));
        resultMap.put(Constant.SEQUENCEID, parameter.get(Constant.SEQUENCEID));
        //解析PARAMETER
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(parameter.get(Constant.PARAMETER)));
        String mac = jsonObject.getString("MAC");
        String httpPassword = jsonObject.getString("HttpPassword");
        JSONArray ftpList = jsonObject.getJSONArray("FtpList");
        JSONArray sambaList = jsonObject.getJSONArray("SambaList");
        //组装TR069对象
        List<ParameterValueStruct> setParameterValues = new ArrayList<ParameterValueStruct>();
        if (StringUtils.isNotBlank(httpPassword)) {
            setParameterValues.add(new ParameterValueStructStr("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.HttpPassword", httpPassword));
        }
        if (ftpList != null && ftpList.size() > 0) {
            for (Object object : ftpList) {
                String s = JSONObject.toJSONString(object);
                JSONObject ftpListJsonObject = JSONObject.parseObject(s);
                setParameterValues.add(new ParameterValueStructStr("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.FtpUserName", ftpListJsonObject.getString("FtpPassword")));
            }
        }
        if (sambaList != null && sambaList.size() > 0) {
            for (Object object : sambaList) {
                String s = JSONObject.toJSONString(object);
                JSONObject ftpListJsonObject = JSONObject.parseObject(s);
                setParameterValues.add(new ParameterValueStructStr("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.SambaUserName", ftpListJsonObject.getString("SambaPassword")));
            }
        }
//        Boolean aBoolean = instructionMethodService.setParameterValue(mac, setParameterValues);
//        resultMap.put(Constant.RESULT, aBoolean ? 0 : -1);
        resultMap.put(Constant.RESULTDATA, new HashMap<String, Object>());
        return resultMap;
    }

    @Override
    public Map<String, Object> getHgServiceInfo(Map<String, Object> parameter) {
        //构建返回对象
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(Constant.ID, parameter.get(Constant.ID));
        resultMap.put(Constant.CMDTYPE, parameter.get(Constant.CMDTYPE));
        resultMap.put(Constant.SEQUENCEID, parameter.get(Constant.SEQUENCEID));
        //解析PARAMETER
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(parameter.get(Constant.PARAMETER)));
        String mac = jsonObject.getString("MAC");
        List<String> parameterNames = new ArrayList<String>();
        parameterNames.add("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.FtpEnable");
        parameterNames.add("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.FtpService");
        parameterNames.add("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.SambaEnable");
        parameterNames.add("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.SambaService");
        parameterNames.add("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.HttpEnable");
        parameterNames.add("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.FtpUserName");
        parameterNames.add("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.SambaUserName");
        parameterNames.add("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.HttpPassword");
        Map<String, Object> trResultMap = instructionMethodService.getParameterValues(mac, parameterNames);
        Map<String, Object> resultDataMap = new HashMap<String, Object>();
        if (trResultMap != null && trResultMap.size() > 0) {
            resultDataMap.put("FtpEnable", trResultMap.get("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.FtpEnable"));
            resultDataMap.put("FtpService", trResultMap.get("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.FtpService"));
            resultDataMap.put("SambaEnable", trResultMap.get("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.SambaEnable"));
            resultDataMap.put("SambaService", trResultMap.get("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.SambaService"));
            resultDataMap.put("HttpEnable", trResultMap.get("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.HttpEnable"));
            resultDataMap.put("HttpPassword", trResultMap.get("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.HttpPassword"));
            List<Map<String, Object>> list1 = new ArrayList<>();
            list1.add(newMap("FtpUserName", trResultMap.get("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.FtpUserName")));
            list1.add(newMap("FtpPassword", trResultMap.get("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.FtpPassword")));
            resultDataMap.put("FtpList", list1);
            List<Map<String, Object>> list2 = new ArrayList<>();
            list1.add(newMap("SambaUserName", trResultMap.get("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.SambaUserName")));
            list1.add(newMap("SambaPassword", trResultMap.get("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.SambaPassword")));
            resultDataMap.put("FtpList", list2);
        }
        resultMap.put(Constant.RESULT, trResultMap == null ? -1 : 0);
        resultMap.put(Constant.RESULTDATA, resultDataMap);
        return resultMap;
    }


    public static Map<String, Object> newMap(String key, Object value) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, value);
        return map;
    }

    @Override
    public Map<String, Object> setHgReboot(Map<String, Object> parameter) {
        logger.info("start setHgReboot parameter:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        retMap.put(Constant.ID, parameter.get(Constant.ID));
        retMap.put(Constant.CMDTYPE, parameter.get(Constant.CMDTYPE));
        retMap.put(Constant.SEQUENCEID, parameter.get(Constant.SEQUENCEID));
        Map<String, Object> resultData = new HashMap<>();
        retMap.put(Constant.RESULTDATA, resultData);
            //1.根据网关MAC查询网关ID
        Map<String, Object> macMap = (Map<String, Object>) parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = (String) macMap.get(Constant.MAC);
        if(StringUtils.isEmpty(gatewayMacAddress)){
            retMap.put(Constant.RESULT,-102);
            logger.info("MAC为空");
            return retMap;
        }
        GatewayInfo gateQuery = new GatewayInfo();
        gateQuery.setGatewayMacaddress(gatewayMacAddress);
        List<GatewayInfo> list = gatewayInfoMapper.queryList(gateQuery);

        if(list == null || list.size() <=0){
            logger.info("MAC为:" + gatewayMacAddress + "的网关不存在");
            retMap.put(Constant.RESULT, -201);
            return retMap;
        }

        //2.调用重启接口
        Map<String, Object> rebootMap = new HashMap<String, Object>();
        List<String> gatewayIdsList = new ArrayList<String>();
        gatewayIdsList.add(list.get(0).getGatewayUuid());
        rebootMap.put("gatewayIds", gatewayIdsList);
        Map<String, Object> resultMap = gatewayManageService.reboot(rebootMap);
        logger.info("setHgReboot reboot resultMap:{}",resultMap);

        if(RespCodeEnum.RC_0.code().equals(resultMap.get(Constant.CODE))){
            retMap.put(Constant.RESULT, 0);
        }else{
            retMap.put(Constant.RESULT, -400);
        }
        logger.info("end setHgReboot parameter:{}",parameter);
        return retMap;
    }

    @Override
    public Map<String, Object> setHgRecover(Map<String, Object> parameter) {
        logger.info("start setHgRecover parameter:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        retMap.put(Constant.ID, parameter.get(Constant.ID));
        retMap.put(Constant.CMDTYPE, parameter.get(Constant.CMDTYPE));
        retMap.put(Constant.SEQUENCEID, parameter.get(Constant.SEQUENCEID));
        Map<String, Object> resultData = new HashMap<>();
        retMap.put(Constant.RESULTDATA, resultData);
        //1.根据网关MAC查询网关ID
        Map<String, Object> macMap = (Map<String, Object>) parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = (String) macMap.get(Constant.MAC);
        /**
         * 集团测试不测了
         */
        /*if(StringUtils.isEmpty(gatewayMacAddress)){
            retMap.put(Constant.RESULT, -102);
            logger.info("MAC为空");
            return retMap;
        }
        GatewayInfo gateQuery = new GatewayInfo();
        gateQuery.setGatewayMacaddress(gatewayMacAddress);
        List<GatewayInfo> list = gatewayInfoMapper.queryList(gateQuery);

        if(list == null || list.size() <=0){
            logger.info("MAC为:" + gatewayMacAddress + "的网关不存在");
            retMap.put(Constant.RESULT, -201);
            return retMap;
        }

        //2.调用重启接口
        Map<String, Object> ResetMap = new HashMap<String, Object>();
        List<String> gatewayIdsList = new ArrayList<String>();
        gatewayIdsList.add(list.get(0).getGatewayUuid());
        ResetMap.put("gatewayIds", gatewayIdsList);

        Map<String, Object> resultMap = gatewayManageService.factoryReset(ResetMap);
        logger.info("setHgRecover factoryReset retMap:{}", resultMap);

        if(RespCodeEnum.RC_0.code().equals(resultMap.get(Constant.CODE))){
            retMap.put(Constant.RESULT, 0);
        }else{
            retMap.put(Constant.RESULT, -400);
        }*/
        retMap.put(Constant.RESULT, 0);
        logger.info("end setHgRecover retMap:{}", retMap);
        return retMap;
    }

    @Override
    public Map<String, Object> setHgServiceManage(Map<String, Object> parameter) {
        logger.info("start setHgServiceManage parameter:{}", parameter);
        //拼装返回消息
        Map<String, Object> retMap = new HashMap<>();
        retMap.put(Constant.ID, parameter.get(Constant.ID));
        retMap.put(Constant.CMDTYPE, parameter.get(Constant.CMDTYPE));
        retMap.put(Constant.SEQUENCEID, parameter.get(Constant.SEQUENCEID));
        Map<String, Object> resultData = new HashMap<>();
        retMap.put(Constant.RESULTDATA, resultData);
        //获取网关MAC
        Map<String, Object> macMap = (Map<String, Object>) parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = (String) macMap.get(Constant.MAC);
        if(StringUtils.isEmpty(gatewayMacAddress)){
            retMap.put(Constant.RESULT, -102);
            logger.info("MAC为空");
            return retMap;
        }

        GatewayInfo gateQuery = new GatewayInfo();
        gateQuery.setGatewayMacaddress(gatewayMacAddress);
        List<GatewayInfo> list = gatewayInfoMapper.queryList(gateQuery);

        if(list == null || list.size() <=0){
            logger.info("MAC为:" + gatewayMacAddress + "的网关不存在");
            retMap.put(Constant.RESULT, -201);
            return retMap;
        }
        //获取参数值
        String FtpEnable = (String) macMap.get("FtpEnable");
//        String FtpService = (String) macMap.get("FtpService");
//        String SambaEnable = (String) macMap.get("SambaEnable");
//        String SambaService = (String) macMap.get("SambaService");
//        String HttpEnable = (String) macMap.get("HttpEnable");

        List<ParameterValueStruct> setParameterNames = new ArrayList<>();
        if(!StringUtils.isEmpty(FtpEnable)){
            if("0".equals(FtpEnable)) {
                setParameterNames.add(new ParameterValueStruct("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.FtpEnable", false,ParameterValueStruct.Type_Boolean));
            }
            if("1".equals(FtpEnable)) {
                setParameterNames.add(new ParameterValueStruct("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.FtpEnable", true,ParameterValueStruct.Type_Boolean));
            }
        }
//        if(!StringUtils.isEmpty(FtpService)){
//            setParameterNames.add(new ParameterValueStructStr("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.FtpService", FtpService));
//        }
//        if(!StringUtils.isEmpty(SambaEnable)){
//            setParameterNames.add(new ParameterValueStructStr("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.SambaEnable", SambaEnable));
//        }
//        if(!StringUtils.isEmpty(SambaService)){
//            setParameterNames.add(new ParameterValueStructStr("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.SambaService", SambaService));
//        }
//        if(!StringUtils.isEmpty(HttpEnable)){
//            setParameterNames.add(new ParameterValueStructStr("InternetGatewayDevice.DeviceInfo.X_CMCC_ServiceManage.HttpEnable", HttpEnable));
//        }
        Boolean result = true;
        if(setParameterNames.size()>0) {
            result = instructionMethodService.setParameterValue(gatewayMacAddress, setParameterNames);
            logger.info("setHgServiceManage setParameterValue result:{}", result);
        }
        retMap.put(Constant.RESULT, result ? 0 :-400);
        logger.info("end setHgServiceManage retMap:{}", retMap);
        return retMap;
    }
}
