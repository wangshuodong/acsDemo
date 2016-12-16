package com.cmiot.rms.services.boxManager.instruction.impl;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.SetParameterAttributes;
import com.cmiot.acs.model.struct.SetParameterAttributesStruct;
import com.cmiot.rms.services.boxManager.instruction.BoxAbstractInstruction;
import com.cmiot.rms.services.instruction.AbstractInstruction;

import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/5/10.
 */
public class BoxSetParameterAttributesInstruction extends BoxAbstractInstruction {
    @Override
    public AbstractMethod createBody(Map map) {
        List<SetParameterAttributesStruct> setParamAttrList = (List<SetParameterAttributesStruct>)map.get("paList");
        SetParameterAttributes setParameterAttributes = new SetParameterAttributes();
        setParameterAttributes.getSetParamAttrList().addAll(setParamAttrList);
        return setParameterAttributes;
    }
}
