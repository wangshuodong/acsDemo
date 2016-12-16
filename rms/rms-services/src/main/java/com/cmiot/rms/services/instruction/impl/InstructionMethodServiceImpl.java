package com.cmiot.rms.services.instruction.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.model.GetParameterAttributesResponse;
import com.cmiot.acs.model.GetParameterValuesResponse;
import com.cmiot.acs.model.struct.ParameterAttributeStruct;
import com.cmiot.acs.model.struct.ParameterInfoStruct;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.acs.model.struct.SetParameterAttributesStruct;
import com.cmiot.rms.common.enums.InstructionsStatusEnum;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.instruction.InvokeInsService;
import com.cmiot.rms.services.util.InstructionUtil;
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
 * Created by panmingguo on 2016/5/5.
 */
@Service("instructionMethodService")
public class InstructionMethodServiceImpl implements InstructionMethodService {

    private static Logger logger = LoggerFactory.getLogger(InstructionMethodServiceImpl.class);

    @Autowired
    private InvokeInsService invokeInsService;

    @Autowired
    InstructionsService instructionsService;

    @Autowired
    GatewayInfoService gatewayInfoService;

    /**
     * 获取参数名称
     *
     * @param gatewayMacAddress
     * @param path
     * @param nextLevel
     * @return Map
     *         key:参数名称
     *         value：是否可读
     */
    @Override
    public Map<String, Object> getParameterNames(String gatewayMacAddress, String path, boolean nextLevel) {
        logger.info("传入getParameterNames参数:{}, {}, {}", gatewayMacAddress, path, nextLevel);

        if(StringUtils.isEmpty(gatewayMacAddress) || StringUtils.isEmpty(path))
        {
            return new HashMap<>();
        }
        String gatewayId = getGatewayId(gatewayMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", gatewayId);
        map.put("methodName", "GetParameterNames");
        map.put("parameterPath", path);
        map.put("nextLevel", nextLevel);
        Map<String, Object> sendMap = invokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return new HashMap<>();
        }

        Map<String, Object> retMap  =  new HashMap<>();

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = getStatus(insMap.get("status"));
        if (status == InstructionsStatusEnum.STATUS_1.code()) {
            String jsonStr = insMap.get("json");
            if (jsonStr == null) {
                return new HashMap<>();
            }
            JSONObject jsonObject = JSON.parseObject(jsonStr);

            JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(jsonObject.get("parameterList")));
            ParameterInfoStruct parameterInfoStruct;
            for(Object object : jsonArray)
            {
                JSONObject jsonObj = (JSONObject) JSON.toJSON(object);
                parameterInfoStruct = JSON.toJavaObject(jsonObj, ParameterInfoStruct.class);
                retMap.put(parameterInfoStruct.getName(), parameterInfoStruct.isWritable());
            }
        }
        return retMap;
    }

    /**
     * 获取参数值
     *
     * @param gatewayMacAddress
     * @param list
     * @return Map
     *         key:参数名称
     *         value：参数值
     */
    @Override
    public Map<String, Object> getParameterValues(String gatewayMacAddress, List<String> list) {
        logger.info("getParameterValues:{}, {}", gatewayMacAddress, list);

        if(StringUtils.isEmpty(gatewayMacAddress) || null == list || list.size() <= 0)
        {
            return new HashMap<>();
        }
        String gatewayId = getGatewayId(gatewayMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", gatewayId);
        map.put("methodName", "GetParameterValues");
        map.put("parameterList", list);
        Map<String, Object> sendMap = invokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return new HashMap<>();
        }

        Map<String, Object> retMap  =  new HashMap<>();

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = getStatus(insMap.get("status"));
        if (status == InstructionsStatusEnum.STATUS_1.code()) {
            String jsonStr = insMap.get("json");
            if (jsonStr == null) {
                return new HashMap<>();
            }
            JSONObject jsonObject = JSON.parseObject(jsonStr);

            GetParameterValuesResponse getParameterValuesResponse = JSON.toJavaObject(jsonObject, GetParameterValuesResponse.class);
            List<ParameterValueStruct> parameterValueStructs = getParameterValuesResponse.getParameterList().getParameterValueStructs();
            for(ParameterValueStruct struct : parameterValueStructs)
            {
                if(null != struct.getValue())
                {
                    retMap.put(struct.getName(), struct.getValue());
                }

            }
        }
        return retMap;
    }

    /**
     * 设置参数值
     *
     * @param gatewayMacAddress
     * @param list
     * @return Boolean 是否设置成功
     */
    @Override
    public Boolean setParameterValue(String gatewayMacAddress, List<ParameterValueStruct> list) {
        logger.info("setParameterValue:{}, {}", gatewayMacAddress, list);
        if(StringUtils.isEmpty(gatewayMacAddress) || null == list || list.size() <= 0)
        {
            return false;
        }
        String gatewayId = getGatewayId(gatewayMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", gatewayId);
        map.put("methodName", "SetParameterValues");
        map.put("pvList", list);
        Map<String, Object> sendMap = invokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return false;
        }
        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = getStatus(insMap.get("status"));
        if (status == InstructionsStatusEnum.STATUS_1.code())
        {
            return true;
        }
        return false;
    }

    /**
     * 设置参数属性
     *
     * @param gatewayMacAddress
     * @param setParamAttrList
     * @return
     */
    @Override
    public Boolean SetParameterAttributes(String gatewayMacAddress, List<SetParameterAttributesStruct> setParamAttrList) {
        logger.info("SetParameterAttributes:{}, {}",  gatewayMacAddress, setParamAttrList);
        if(StringUtils.isEmpty(gatewayMacAddress) || null == setParamAttrList || setParamAttrList.size() <= 0)
        {
            return false;
        }
        String gatewayId = getGatewayId(gatewayMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", gatewayId);
        map.put("methodName", "SetParameterAttributes");
        map.put("paList", setParamAttrList);
        Map<String, Object> sendMap = invokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return false;
        }

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = getStatus(insMap.get("status"));
        if (status == InstructionsStatusEnum.STATUS_1.code())
        {
            return true;
        }
        return false;
    }

    /**
     * 添加节点
     *
     * @param gatewayMacAddress
     * @param objectName
     * @param parameterKey
     * @return -1：添加失败
     *         >0: AddObjectResponse 返回的InstanceNumber
     */
    @Override
    public int AddObject(String gatewayMacAddress, String objectName, String parameterKey) {
        logger.info("AddObject:{}, {}, {}",  gatewayMacAddress, objectName, parameterKey);
        if(StringUtils.isEmpty(gatewayMacAddress) || StringUtils.isEmpty(objectName))
        {
            return -1;
        }
        String gatewayId = getGatewayId(gatewayMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", gatewayId);
        map.put("methodName", "AddObject");
        map.put("objectName", objectName);
        map.put("parameterKey", parameterKey);
        Map<String, Object> sendMap = invokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return -1;
        }

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = getStatus(insMap.get("status"));
        if (status == InstructionsStatusEnum.STATUS_1.code())
        {
            String jsonStr = insMap.get("json");
            if (jsonStr == null) {
                return -1;
            }
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if(null != jsonObject.get("instanceNumber"))
            {
                return Integer.valueOf(jsonObject.get("instanceNumber").toString());
            }

        }
        return -1;
    }

    /**
     * 删除节点
     *
     * @param gatewayMacAddress
     * @param objectName
     * @param parameterKey
     * @return -1：失败
     *         0,1: 成功
     */
    @Override
    public int DeleteObject(String gatewayMacAddress, String objectName, String parameterKey) {
        logger.info("DeleteObject:{}, {}, {}",  gatewayMacAddress, objectName, parameterKey);
        if(StringUtils.isEmpty(gatewayMacAddress) || StringUtils.isEmpty(objectName))
        {
            return -1;
        }
        String gatewayId = getGatewayId(gatewayMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", gatewayId);
        map.put("methodName", "DeleteObject");
        map.put("objectName", objectName);
        map.put("parameterKey", parameterKey);
        Map<String, Object> sendMap = invokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return -1;
        }

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = getStatus(insMap.get("status"));
        if (status == InstructionsStatusEnum.STATUS_1.code())
        {
            String jsonStr = insMap.get("json");
            if (jsonStr == null) {
                return -1;
            }
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if(null != jsonObject.get("status"))
            {
                return Integer.valueOf(jsonObject.get("status").toString());
            }

        }
        return -1;
    }

    /**
     * 获取参数属性
     *
     * @param gatewayMacAddress
     * @param list
     * @return
     */
    @Override
    public Map<String, Object> getParameterAttributes(String gatewayMacAddress, List<String> list) {
        logger.info("GetParameterAttributes:{}, {}", gatewayMacAddress, list);

        if(StringUtils.isEmpty(gatewayMacAddress))
        {
            return new HashMap<>();
        }
        String gatewayId = getGatewayId(gatewayMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", gatewayId);
        map.put("methodName", "GetParameterAttributes");
        map.put("parameterNames", list);
        Map<String, Object> sendMap = invokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return new HashMap<>();
        }

        Map<String, Object> retMap  =  new HashMap<>();

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = getStatus(insMap.get("status"));
        if (status == InstructionsStatusEnum.STATUS_1.code()) {
            String jsonStr = insMap.get("json");
            if (jsonStr == null) {
                return new HashMap<>();
            }
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            GetParameterAttributesResponse response = JSON.toJavaObject(jsonObject, GetParameterAttributesResponse.class);
            List<ParameterAttributeStruct> paramAttrList = response.getParamAttrList();
            for(ParameterAttributeStruct struct : paramAttrList)
            {
                Map<String, Object> attibute = new HashMap<>();
                attibute.put("Notification", struct.getNotification());
                attibute.put("AccessList", struct.getAccessList());
                retMap.put(struct.getName(), attibute);
            }
        }
        return retMap;
    }

    /**
     * 获取LANDevice下面的{i}值
     *
     * @return
     */
    @Override
    public String getLANDevicePrefix() {
        //默认返回1
        return "InternetGatewayDevice.LANDevice.1.";
    }

    /**
     * 获取WANDevice下面的{i}值
     *
     * @return
     */
    @Override
    public String getWANDevicePrefix() {
        return "InternetGatewayDevice.WANDevice.1.";
    }

    /**
     * 获取LANDevice下面的{i}值
     *
     * @return
     */
    @Override
    public List<String> getLANDevicePrefix(String gatewayMacAddress) {
        //InternetGatewayDevice.LANDevice.{i}.
        Map<String, Object> lanDeviceMap = getParameterNames(gatewayMacAddress,
                "InternetGatewayDevice.LANDevice.", true);
        logger.info("queryHguBusinessStatus getParameterNames lanDeviceMap:{}", lanDeviceMap);

        String regWANDevice = "InternetGatewayDevice.LANDevice.[0-9]+.$";
        List<String> preLANDeviceList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : lanDeviceMap.entrySet()) {
            InstructionUtil.getName(preLANDeviceList, regWANDevice, entry.getKey());
        }
        return preLANDeviceList;
    }

    /**
     * 获取WANDevice下面的{i}值
     * @param gatewayMacAddress
     * @return
     */
    @Override
    public List<String> getWANDevicePrefix(String gatewayMacAddress) {
        //InternetGatewayDevice.WANDevice.{i}.
        Map<String, Object> wanDeviceMap = getParameterNames(gatewayMacAddress,
                "InternetGatewayDevice.WANDevice.", true);
        logger.info("queryHguBusinessStatus getParameterNames wanDeviceMap:{}", wanDeviceMap);

        String regWANDevice =  "InternetGatewayDevice.WANDevice.[0-9]+.$";
        List<String> preWANDeviceList  = new ArrayList<>();
        for(Map.Entry<String, Object> entry : wanDeviceMap.entrySet())
        {
            InstructionUtil.getName(preWANDeviceList, regWANDevice, entry.getKey());
        }
        return preWANDeviceList;
    }

    /**
     * 获取参数名称（带返回值，表明是否是网关正常返回）
     *
     * @param gatewayMacAddress
     * @param path
     * @param nextLevel
     * @return
     */
    @Override
    public Map<String, Object> getParameterNamesResult(String gatewayMacAddress, String path, boolean nextLevel) {
        logger.info("传入getParameterValuesResult参数:{}, {}, {}", gatewayMacAddress, path, nextLevel);

        Map<String, Object> retMap  =  new HashMap<>();
        if(StringUtils.isEmpty(gatewayMacAddress) || StringUtils.isEmpty(path))
        {
            retMap.put("result", -1);
            return retMap;
        }
        String gatewayId = getGatewayId(gatewayMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", gatewayId);
        map.put("methodName", "GetParameterNames");
        map.put("parameterPath", path);
        map.put("nextLevel", nextLevel);
        Map<String, Object> sendMap = invokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            retMap.put("result", -1);
            return retMap;
        }

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = getStatus(insMap.get("status"));
        if (status == InstructionsStatusEnum.STATUS_1.code()) {
            String jsonStr = insMap.get("json");
            if (jsonStr == null) {
                return new HashMap<>();
            }
            JSONObject jsonObject = JSON.parseObject(jsonStr);

            JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(jsonObject.get("parameterList")));
            ParameterInfoStruct parameterInfoStruct;
            for(Object object : jsonArray)
            {
                JSONObject jsonObj = (JSONObject) JSON.toJSON(object);
                parameterInfoStruct = JSON.toJavaObject(jsonObj, ParameterInfoStruct.class);
                retMap.put(parameterInfoStruct.getName(), parameterInfoStruct.isWritable());
            }
            //0表示是网关正常返回
            retMap.put("result", 0);
        }
        else
        {
            //1表示是网关异常返回
            retMap.put("result", -1);
        }
        logger.info("getParameterValuesResult返回:{}", retMap);
        return retMap;
    }

    /**
     * 获取参数值（带返回值，表明是否是网关正常返回）
     *
     * @param gatewayMacAddress
     * @param list
     * @return
     */
    @Override
    public Map<String, Object> getParameterValuesResult(String gatewayMacAddress, List<String> list) {
        logger.info("getParameterValuesResult:{}, {}", gatewayMacAddress, list);

        Map<String, Object> retMap  =  new HashMap<>();
        if(StringUtils.isEmpty(gatewayMacAddress) || null == list || list.size() <= 0)
        {
            retMap.put("result", -1);
            return retMap;
        }
        String gatewayId = getGatewayId(gatewayMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", gatewayId);
        map.put("methodName", "GetParameterValues");
        map.put("parameterList", list);
        Map<String, Object> sendMap = invokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            retMap.put("result", -1);
            return retMap;
        }

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = getStatus(insMap.get("status"));
        if (status == InstructionsStatusEnum.STATUS_1.code()) {
            String jsonStr = insMap.get("json");
            if (jsonStr == null) {
                return new HashMap<>();
            }
            JSONObject jsonObject = JSON.parseObject(jsonStr);

            GetParameterValuesResponse getParameterValuesResponse = JSON.toJavaObject(jsonObject, GetParameterValuesResponse.class);
            List<ParameterValueStruct> parameterValueStructs = getParameterValuesResponse.getParameterList().getParameterValueStructs();
            for(ParameterValueStruct struct : parameterValueStructs)
            {
                if(null != struct.getValue())
                {
                    retMap.put(struct.getName(), struct.getValue());
                }

            }
            //0表示是网关正常返回
            retMap.put("result", 0);
        }
        else
        {
            //1表示是网关异常返回
            retMap.put("result", -1);
        }
        logger.info("getParameterValuesResult返回:{}", retMap);
        return retMap;
    }

    /**
     * 获取参数名称（带返回值和错误码）
     *
     * @param gatewayMacAddress
     * @param path
     * @param nextLevel
     * @return
     */
    @Override
    public Map<String, Object> getParameterNamesErrorCode(String gatewayMacAddress, String path, boolean nextLevel) {
        logger.info("传入getParameterNamesErrorCode参数:{}, {}, {}", gatewayMacAddress, path, nextLevel);

        Map<String, Object> retMap  =  new HashMap<>();
        retMap.put("result", -1);
        if(StringUtils.isEmpty(gatewayMacAddress) || StringUtils.isEmpty(path))
        {
            return retMap;
        }
        String gatewayId = getGatewayId(gatewayMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", gatewayId);
        map.put("methodName", "GetParameterNames");
        map.put("parameterPath", path);
        map.put("nextLevel", nextLevel);
        Map<String, Object> sendMap = invokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return retMap;
        }

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = getStatus(insMap.get("status"));
        if (status == InstructionsStatusEnum.STATUS_1.code()) {
            String jsonStr = insMap.get("json");
            if(jsonStr != null) {
                JSONObject jsonObject = JSON.parseObject(jsonStr);
                JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(jsonObject.get("parameterList")));
                ParameterInfoStruct parameterInfoStruct;
                for (Object object : jsonArray) {
                    JSONObject jsonObj = (JSONObject) JSON.toJSON(object);
                    parameterInfoStruct = JSON.toJavaObject(jsonObj, ParameterInfoStruct.class);
                    retMap.put(parameterInfoStruct.getName(), parameterInfoStruct.isWritable());
                }

                //0表示是网关正常返回
                retMap.put("result", 0);
            }
        }
        else if(status == InstructionsStatusEnum.STATUS_2.code())
        {
            retMap.put("errorCode", instructionsService.getFaultCode(requestId));
        }
        logger.info("getParameterNamesErrorCode返回:{}", retMap);
        return retMap;
    }

    /**
     * 获取参数值（带返回值和错误码）
     *
     * @param gatewayMacAddress
     * @param list
     * @return
     */
    @Override
    public Map<String, Object> getParameterValuesErrorCode(String gatewayMacAddress, List<String> list) {
        logger.info("getParameterValuesErrorCode:{}, {}", gatewayMacAddress, list);

        Map<String, Object> retMap  =  new HashMap<>();
        retMap.put("result", -1);
        if(StringUtils.isEmpty(gatewayMacAddress) || null == list || list.size() <= 0)
        {
            return retMap;
        }
        String gatewayId = getGatewayId(gatewayMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", gatewayId);
        map.put("methodName", "GetParameterValues");
        map.put("parameterList", list);
        Map<String, Object> sendMap = invokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return retMap;
        }

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = getStatus(insMap.get("status"));
        if (status == InstructionsStatusEnum.STATUS_1.code()) {
            String jsonStr = insMap.get("json");
            if (jsonStr != null) {
                JSONObject jsonObject = JSON.parseObject(jsonStr);
                GetParameterValuesResponse getParameterValuesResponse = JSON.toJavaObject(jsonObject, GetParameterValuesResponse.class);
                List<ParameterValueStruct> parameterValueStructs = getParameterValuesResponse.getParameterList().getParameterValueStructs();
                for (ParameterValueStruct struct : parameterValueStructs) {
                    if (null != struct.getValue()) {
                        retMap.put(struct.getName(), struct.getValue());
                    }

                }
                //0表示是网关正常返回
                retMap.put("result", 0);
            }
        }
        else if(status == InstructionsStatusEnum.STATUS_2.code())
        {
            retMap.put("errorCode", instructionsService.getFaultCode(requestId));
        }
        logger.info("getParameterValuesErrorCode返回:{}", retMap);
        return retMap;
    }

    /**
     * 设置参数值（带返回值和错误码）
     *
     * @param gatewayMacAddress
     * @param list
     * @return
     */
    @Override
    public Map<String, Object> setParameterValueErrorCode(String gatewayMacAddress, List<ParameterValueStruct> list) {
        logger.info("setParameterValueErrorCode:{}, {}", gatewayMacAddress, list);
        Map<String, Object> retMap  =  new HashMap<>();
        retMap.put("result", -1);
        if(StringUtils.isEmpty(gatewayMacAddress) || null == list || list.size() <= 0)
        {
            return retMap;
        }
        String gatewayId = getGatewayId(gatewayMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", gatewayId);
        map.put("methodName", "SetParameterValues");
        map.put("pvList", list);
        Map<String, Object> sendMap = invokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return retMap;
        }
        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = getStatus(insMap.get("status"));
        if (status == InstructionsStatusEnum.STATUS_1.code())
        {
            //0表示是网关正常返回
            retMap.put("result", 0);
        }
        else if(status == InstructionsStatusEnum.STATUS_2.code())
        {
            retMap.put("errorCode", instructionsService.getFaultCode(requestId));
        }

        logger.info(" End setParameterValueErrorCode:{}", retMap);
        return retMap;
    }


    /**
     * 获取GatewayID
     * @param gatewayMacAddress
     * @return
     */
    private String  getGatewayId(String gatewayMacAddress)
    {
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayMacaddress(gatewayMacAddress);

        GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        if(null == gatewayInfo)
        {
            return "";
        }
        return gatewayInfo.getGatewayUuid();
    }


    private int getStatus(Object obj)
    {
        int result = 2;
        try {
            if(null != obj)
            {
                result = Integer.valueOf(obj.toString());
            }
        }
        catch (Exception e)
        {
            result = 2;
        }
        return result;
    }
}
