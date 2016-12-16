package com.cmiot.rms.services.boxManager.instruction.impl;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.SetParameterValues;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.services.boxManager.instruction.BoxAbstractInstruction;
import com.cmiot.rms.services.instruction.AbstractInstruction;

import java.util.List;
import java.util.Map;

/**
 * @Author xukai
 * Date 2016/2/18
 */
public class BoxSetParameterValuesInstruction extends BoxAbstractInstruction {
    @Override
    public AbstractMethod createBody(Map map) {
        List<ParameterValueStruct> pvList = (List<ParameterValueStruct>) map.get("pvList");

        SetParameterValues setParameterValues = new SetParameterValues();
        setParameterValues.getParameterList().setParameterValueStructs(pvList);

        if(null != map.get("requestId"))
        {
            setParameterValues.setParameterKey(map.get("requestId").toString());//下发指令唯一ID
        }

        return setParameterValues;
    }
}
