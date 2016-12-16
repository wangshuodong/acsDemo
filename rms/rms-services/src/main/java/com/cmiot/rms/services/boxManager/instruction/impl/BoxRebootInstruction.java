package com.cmiot.rms.services.boxManager.instruction.impl;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.Reboot;
import com.cmiot.rms.services.boxManager.instruction.BoxAbstractInstruction;
import com.cmiot.rms.services.instruction.AbstractInstruction;

import java.util.Map;

/**
 * @Author xukai
 * Date 2016/2/17
 */
public class BoxRebootInstruction extends BoxAbstractInstruction {
    @Override
    public AbstractMethod createBody(Map map) {
        return new Reboot();
    }
}
