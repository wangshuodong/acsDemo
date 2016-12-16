package com.cmiot.rms.services.instruction;


import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.acs.model.struct.SetParameterAttributesStruct;

import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/5/5.
 */
public interface InstructionMethodService {

    /**
     * 获取参数名称
     *
     * @param gatewayMacAddress
     * @param path
     * @param nextLevel
     * @return
     */
     Map<String, Object> getParameterNames(String gatewayMacAddress, String path, boolean nextLevel);

    /**
     * 获取参数值
     *
     * @param gatewayMacAddress
     * @param list
     * @return
     */
    Map<String, Object> getParameterValues(String gatewayMacAddress, List<String> list);


    /**
     * 设置参数值
     *
     * @param gatewayMacAddress
     * @param list
     * @return
     */
    Boolean setParameterValue(String gatewayMacAddress, List<ParameterValueStruct> list);


    /**
     * 设置参数属性
     * @param gatewayMacAddress
     * @param setParamAttrList
     * @return
     */
    Boolean SetParameterAttributes(String gatewayMacAddress, List<SetParameterAttributesStruct> setParamAttrList);


    /**
     * 添加节点
     * @param gatewayMacAddress
     * @param objectName
     * @param parameterKey
     * @return
     */
    int AddObject(String gatewayMacAddress, String objectName, String parameterKey);

    /**
     * 删除节点
     * @param gatewayMacAddress
     * @param objectName
     * @param parameterKey
     * @return
     */
    int DeleteObject(String gatewayMacAddress, String objectName, String parameterKey);


    /**
     * 获取参数属性
     *
     * @param gatewayMacAddress
     * @param list
     * @return
     */
    Map<String, Object> getParameterAttributes(String gatewayMacAddress, List<String> list);

    /**
     *获取LANDevice下面的{i}值
     * @return
     */
    String getLANDevicePrefix();

    /**
     *获取WANDevice下面的{i}值
     * @return
     */
    String getWANDevicePrefix();


    /**
     * 获取参数名称（带返回值，表明是否是网关正常返回）
     *
     * @param gatewayMacAddress
     * @param path
     * @param nextLevel
     * @return
     */
    Map<String, Object> getParameterNamesResult(String gatewayMacAddress, String path, boolean nextLevel);

    /**
     * 获取参数值（带返回值，表明是否是网关正常返回）
     *
     * @param gatewayMacAddress
     * @param list
     * @return
     */
    Map<String, Object> getParameterValuesResult(String gatewayMacAddress, List<String> list);


    /**
     * 获取参数名称（带返回值和错误码）
     *
     * @param gatewayMacAddress
     * @param path
     * @param nextLevel
     * @return
     */
    Map<String, Object> getParameterNamesErrorCode(String gatewayMacAddress, String path, boolean nextLevel);

    /**
     * 获取参数值（带返回值和错误码）
     *
     * @param gatewayMacAddress
     * @param list
     * @return
     */
    Map<String, Object> getParameterValuesErrorCode(String gatewayMacAddress, List<String> list);


    /**
     * 设置参数值（带返回值和错误码）
     *
     * @param gatewayMacAddress
     * @param list
     * @return
     */
    Map<String, Object> setParameterValueErrorCode(String gatewayMacAddress, List<ParameterValueStruct> list);

    /**
     *获取LANDevice下面的{i}值
     * @param gatewayMacAddress
     * @return
     */
    List<String> getLANDevicePrefix(String gatewayMacAddress);

    /**
     *获取WANDevice下面的{i}值
     * @param gatewayMacAddress
     * @return
     */
    List<String> getWANDevicePrefix(String gatewayMacAddress);
}
