package com.cmiot.rms.services.boxManager.instruction.impl;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.GetParameterValues;
import com.cmiot.rms.services.boxManager.instruction.BoxAbstractInstruction;
import com.cmiot.rms.services.instruction.AbstractInstruction;

import java.util.List;
import java.util.Map;

/**
 * @Author xukai
 * Date 2016/2/17
 */
public class BoxGetParameterValuesInstruction extends BoxAbstractInstruction {
    @Override
    public AbstractMethod createBody(Map map) {

        List<String> parameterList = null != map.get("parameterList") ? (List<String>)map.get("parameterList") : null;
        if(null == parameterList)
        {
            return null;
        }

        GetParameterValues getParameterValues = new GetParameterValues();
        getParameterValues.getParameterNames().addAll(parameterList);

        return getParameterValues;
    }
}
