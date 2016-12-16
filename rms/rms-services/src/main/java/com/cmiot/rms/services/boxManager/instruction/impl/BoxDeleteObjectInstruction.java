package com.cmiot.rms.services.boxManager.instruction.impl;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.DeleteObject;
import com.cmiot.rms.services.boxManager.instruction.BoxAbstractInstruction;
import com.cmiot.rms.services.instruction.AbstractInstruction;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Created by panmingguo on 2016/5/11.
 */
public class BoxDeleteObjectInstruction extends BoxAbstractInstruction {
    @Override
    public AbstractMethod createBody(Map map) {

        String objectName = null != map.get("objectName") ? map.get("objectName").toString() : "";
        String parameterKey = null != map.get("parameterKey") ? map.get("parameterKey").toString() : null;

        if(StringUtils.isEmpty(objectName))
        {
            return null;
        }

        DeleteObject deleteObject = new DeleteObject();
        deleteObject.setObjectName(objectName);
        deleteObject.setParameterKey(parameterKey);

        return deleteObject;
    }
}