package com.cmiot.rms.services.instruction.impl;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.GetParameterNames;
import com.cmiot.rms.services.instruction.AbstractInstruction;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * @Author xukai
 * Date 2016/2/17
 */
public class GetParameterNamesInstruction extends AbstractInstruction {
    @Override
    public AbstractMethod createBody(Map map) {
        String parameterPath = null != map.get("parameterPath") ? map.get("parameterPath").toString() : "";
        Boolean nextLevel = null != map.get("nextLevel") ? Boolean.valueOf(map.get("nextLevel").toString()) : true;

        if(StringUtils.isEmpty(parameterPath))
        {
            return null;
        }
        GetParameterNames getParameterNames = new GetParameterNames();
        getParameterNames.setParameterPath(parameterPath);
        getParameterNames.setNextLevel(nextLevel);

        return getParameterNames;
    }
}
