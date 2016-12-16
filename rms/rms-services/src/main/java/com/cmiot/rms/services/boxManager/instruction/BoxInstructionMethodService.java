package com.cmiot.rms.services.boxManager.instruction;


import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.acs.model.struct.SetParameterAttributesStruct;

import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/5/5.
 */
public interface BoxInstructionMethodService {

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

}
