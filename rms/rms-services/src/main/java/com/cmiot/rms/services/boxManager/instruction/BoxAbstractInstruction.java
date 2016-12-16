package com.cmiot.rms.services.boxManager.instruction;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.InstructionsInfoWithBLOBs;

import java.util.Map;

/**
 * @Author xukai
 * Date 2016/2/17
 */
public abstract class BoxAbstractInstruction {

    public abstract AbstractMethod createBody(Map map);

    public AbstractMethod createIns(InstructionsInfoWithBLOBs is, BoxInfo boxInfo, Map map) {
        AbstractMethod body = createBody(map);
        body.setRequestId(is.getInstructionsId());
        body.setCpeId(boxInfo.getBoxFactoryCode() + Constant.SEPARATOR + boxInfo.getBoxSerialnumber());
        body.setCpeUrl(boxInfo.getBoxConnectionrequesturl());
        body.setCpeUserName(boxInfo.getBoxConnectAccount());
        body.setCpePassword(boxInfo.getBoxConnectPassword());
        return body;
    }
    public String getString(Object object)
    {
        if(null != object)
        {
            return object.toString();
        }
        return null;
    }


    public Integer getInt(Object object)
    {
        try {
            if(null != object)
            {
                return Integer.valueOf(object.toString());
            }
        }
        catch (Exception e)
        {
        }
        return 0;
    }
}
