package com.cmiot.acs.control.impl;


import com.cmiot.acs.control.DefaultInformHandleAdpter;
import com.cmiot.acs.model.Inform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author zjialin
 * @date 2016-2-28
 */
public class InformHandleImpl extends DefaultInformHandleAdpter {
    public static Logger logger = LoggerFactory.getLogger(InformHandleImpl.class);

    @Override
    public void bootEvent(Inform inform) {
        // TODO Auto-generated method stub
    }

    @Override
    public void bootStrapEvent(Inform inform) {
        // TODO Auto-generated method stub
    }

    @Override
    public void periodicEvent(Inform inform) {
        super.periodicEvent(inform);
    }

    @Override
    public void connectionRequestEvent(Inform inform) {
        // TODO Auto-generated method stub
    }

    @Override
    public void scheduleEvent(Inform inform) {
        // TODO Auto-generated method stub
    }

    @Override
    public void selfDefineEvent(Inform inform) {
        // TODO Auto-generated method stub
    }


    @Override
    public void transferCompleteEvent(Inform inform) {
        // TODO Auto-generated method stub
    }

    @Override
    public void valueChangeEvent(Inform inform) {
        // TODO Auto-generated method stub
    }

    @Override
    public void xCmccBind(Inform inform) {

    }
}
