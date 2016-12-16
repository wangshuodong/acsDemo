package com.cmiot.rms.services.instruction.impl;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.GetParameterAttributes;
import com.cmiot.rms.services.instruction.AbstractInstruction;

import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/5/11.
 */
public class GetParameterAttributesInstruction extends AbstractInstruction {
    @Override
    public AbstractMethod createBody(Map map) {
        GetParameterAttributes getParameterAttributes = new GetParameterAttributes();
        List<String> parameterNames = (List<String>)map.get("parameterNames");
        getParameterAttributes.getParameterNames().addAll(parameterNames);
        return getParameterAttributes;
    }
}
