package com.cmiot.rms.services.boxManager.instruction.impl;

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
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.boxManager.instruction.BoxInstructionMethodService;
import com.cmiot.rms.services.boxManager.instruction.BoxInvokeInsService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/6/14.
 */
@Service("boxInstructionMethodService")
public class BoxInstructionMethodServiceImpl implements BoxInstructionMethodService {

    private static Logger logger = LoggerFactory.getLogger(BoxInstructionMethodServiceImpl.class);

    @Autowired
    private BoxInvokeInsService boxInvokeInsService;

    @Autowired
    InstructionsService instructionsService;

    @Autowired
    BoxInfoMapper boxInfoMapper;

    /**
     * 获取参数名称
     *
     * @param boxMacAddress
     * @param path
     * @param nextLevel
     * @return Map
     *         key:参数名称
     *         value：是否可读
     */
    @Override
    public Map<String, Object> getParameterNames(String boxMacAddress, String path, boolean nextLevel) {
        logger.info("传入getParameterNames参数:{}, {}, {}", boxMacAddress, path, nextLevel);

        if(StringUtils.isEmpty(boxMacAddress) || StringUtils.isEmpty(path))
        {
            return new HashMap<>();
        }
        String boxUuid = getBoxUuid(boxMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("boxUuid", boxUuid);
        map.put("methodName", "GetParameterNames");
        map.put("parameterPath", path);
        map.put("nextLevel", nextLevel);
        Map<String, Object> sendMap = boxInvokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return new HashMap<>();
        }

        Map<String, Object> retMap  =  new HashMap<>();

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = 0;
        if(insMap==null){
            status = 2;
        }else {
            status = null != insMap.get("status") ? Integer.parseInt(insMap.get("status")) : 2;
        }
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
     * @param boxMacAddress
     * @param list
     * @return Map
     *         key:参数名称
     *         value：参数值
     */
    @Override
    public Map<String, Object> getParameterValues(String boxMacAddress, List<String> list) {
        logger.info("getParameterValues:{}, {}", boxMacAddress, list);

        if(StringUtils.isEmpty(boxMacAddress) || null == list || list.size() <= 0)
        {
            return new HashMap<>();
        }
        String boxUuid = getBoxUuid(boxMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("boxUuid", boxUuid);
        map.put("methodName", "GetParameterValues");
        map.put("parameterList", list);
        Map<String, Object> sendMap = boxInvokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return new HashMap<>();
        }

        Map<String, Object> retMap  =  new HashMap<>();

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = 0;
        if(insMap==null){
            status = 2;
        }else {
            status = null != insMap.get("status") ? Integer.parseInt(insMap.get("status")) : 2;
        }
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
     * @param boxMacAddress
     * @param list
     * @return Boolean 是否设置成功
     */
    @Override
    public Boolean setParameterValue(String boxMacAddress, List<ParameterValueStruct> list) {
        logger.info("setParameterValue:{}, {}", boxMacAddress, list);
        if(StringUtils.isEmpty(boxMacAddress) || null == list || list.size() <= 0)
        {
            return false;
        }
        String boxUuid = getBoxUuid(boxMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("boxUuid", boxUuid);
        map.put("methodName", "SetParameterValues");
        map.put("pvList", list);
        Map<String, Object> sendMap = boxInvokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return false;
        }
        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = 0;
        if(insMap==null){
            status = 2;
        }else {
            status = null != insMap.get("status") ? Integer.parseInt(insMap.get("status")) : 2;
        }
        if (status == InstructionsStatusEnum.STATUS_1.code())
        {
            return true;
        }
        return false;
    }

    /**
     * 设置参数属性
     *
     * @param boxMacAddress
     * @param setParamAttrList
     * @return
     */
    @Override
    public Boolean SetParameterAttributes(String boxMacAddress, List<SetParameterAttributesStruct> setParamAttrList) {
        logger.info("SetParameterAttributes:{}, {}",  boxMacAddress, setParamAttrList);
        if(StringUtils.isEmpty(boxMacAddress) || null == setParamAttrList || setParamAttrList.size() <= 0)
        {
            return false;
        }
        String boxUuid = getBoxUuid(boxMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("boxUuid", boxUuid);
        map.put("methodName", "SetParameterAttributes");
        map.put("paList", setParamAttrList);
        Map<String, Object> sendMap = boxInvokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return false;
        }

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = 0;
        if(insMap==null){
            status = 2;
        }else {
            status = null != insMap.get("status") ? Integer.parseInt(insMap.get("status")) : 2;
        }
        if (status == InstructionsStatusEnum.STATUS_1.code())
        {
            return true;
        }
        return false;
    }

    /**
     * 添加节点
     *
     * @param boxMacAddress
     * @param objectName
     * @param parameterKey
     * @return -1：添加失败
     *         >0: AddObjectResponse 返回的InstanceNumber
     */
    @Override
    public int AddObject(String boxMacAddress, String objectName, String parameterKey) {
        logger.info("AddObject:{}, {}, {}",  boxMacAddress, objectName, parameterKey);
        if(StringUtils.isEmpty(boxMacAddress) || StringUtils.isEmpty(objectName))
        {
            return -1;
        }
        String boxUuid = getBoxUuid(boxMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("boxUuid", boxUuid);
        map.put("methodName", "AddObject");
        map.put("objectName", objectName);
        map.put("parameterKey", parameterKey);
        Map<String, Object> sendMap = boxInvokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return -1;
        }

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = 0;
        if(insMap==null){
            status = 2;
        }else {
            status = null != insMap.get("status") ? Integer.parseInt(insMap.get("status")) : 2;
        }
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
     * @param boxMacAddress
     * @param objectName
     * @param parameterKey
     * @return -1：失败
     *         0,1: 成功
     */
    @Override
    public int DeleteObject(String boxMacAddress, String objectName, String parameterKey) {
        logger.info("DeleteObject:{}, {}, {}",  boxMacAddress, objectName, parameterKey);
        if(StringUtils.isEmpty(boxMacAddress) || StringUtils.isEmpty(objectName))
        {
            return -1;
        }
        String boxUuid = getBoxUuid(boxMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("boxUuid", boxUuid);
        map.put("methodName", "DeleteObject");
        map.put("objectName", objectName);
        map.put("parameterKey", parameterKey);
        Map<String, Object> sendMap = boxInvokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return -1;
        }

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = 0;
        if(insMap==null){
            status = 2;
        }else {
            status = null != insMap.get("status") ? Integer.parseInt(insMap.get("status")) : 2;
        }
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
     * @param boxMacAddress
     * @param list
     * @return
     */
    @Override
    public Map<String, Object> getParameterAttributes(String boxMacAddress, List<String> list) {
        logger.info("GetParameterAttributes:{}, {}", boxMacAddress, list);

        if(StringUtils.isEmpty(boxMacAddress))
        {
            return new HashMap<>();
        }
        String boxUuid = getBoxUuid(boxMacAddress);

        Map<String, Object> map = new HashMap<>();
        map.put("boxUuid", boxUuid);
        map.put("methodName", "GetParameterAttributes");
        map.put("parameterNames", list);
        Map<String, Object> sendMap = boxInvokeInsService.executeOne(map);
        if(null == sendMap || sendMap.get("resultCode").toString().equals("1"))
        {
            return new HashMap<>();
        }

        Map<String, Object> retMap  =  new HashMap<>();

        String requestId = sendMap.get("requestId").toString();
        Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
        int status = 0;
        if(insMap==null){
            status = 2;
        }else {
            status = null != insMap.get("status") ? Integer.parseInt(insMap.get("status")) : 2;
        }
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
     * 获取boxUuid
     * @param boxMacAddress
     * @return
     */
    private String  getBoxUuid(String boxMacAddress)
    {
        BoxInfo boxInfo = new BoxInfo();
        boxInfo.setBoxMacaddress(boxMacAddress);

        List<BoxInfo> boxInfoR = boxInfoMapper.selectBoxInfo(boxInfo);
        if(null == boxInfoR || boxInfoR.size() <1)
        {
            return "";
        }
        return boxInfoR.get(0).getBoxUuid();
    }
}
