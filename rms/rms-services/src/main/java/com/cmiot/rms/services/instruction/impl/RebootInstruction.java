package com.cmiot.rms.services.instruction.impl;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.Reboot;
import com.cmiot.rms.services.instruction.AbstractInstruction;

import java.util.Map;

/**
 * @Author xukai
 * Date 2016/2/17
 */
public class RebootInstruction extends AbstractInstruction {
    @Override
    public AbstractMethod createBody(Map map) {
        return new Reboot();
    }
}
