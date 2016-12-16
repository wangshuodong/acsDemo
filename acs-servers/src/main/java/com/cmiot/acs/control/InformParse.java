package com.cmiot.acs.control;


import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.Event;
import com.cmiot.acs.model.struct.EventStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author zjialin
 * @date 2016-2-29
 */
public final class InformParse {
    public static Logger logger = LoggerFactory.getLogger(InformParse.class);
    private IInformHandle informHandle;

    public InformParse(IInformHandle informHandle) {
        if (informHandle == null) {
            throw new NullPointerException("Instance InformParse error,IInformHandle is null!");
        }
        this.informHandle = informHandle;
    }

    public IInformHandle getInformHandle() {
        return informHandle;
    }


    public void parseInform(Inform inform) {
        try {
            EventStruct[] events = inform.getEvent().getEventCodes();
            for (EventStruct eventStruct : events) {
                if (eventStruct.getEvenCode().equals(Event.BOOTSTRAP)) {
                    informHandle.bootStrapEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.BOOT)) {
                    informHandle.bootEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.PERIODIC)) {
                    informHandle.periodicEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.SCHEDULED)) {
                    informHandle.scheduleEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.VALUE_CHANGE)) {
                    informHandle.valueChangeEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.KICKED)) {
                    informHandle.kickedEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.CONNECTION_REQUEST)) {
                    informHandle.connectionRequestEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.TRANSFER_COMPLETE)) {
                    informHandle.transferCompleteEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.DIAGNOSTICS_COMPLETE)) {
                    informHandle.diagnosticsCompleteEvent(inform);
                } else if ((eventStruct.getEvenCode().equals(Event.XCMCCBIND))) {
                    informHandle.xCmccBind(inform);
                } else if ((eventStruct.getEvenCode().equals(Event.XCMCCMONITOR))) {
                    // TODO Auto-generated method stub
                } else if ((eventStruct.getEvenCode().equals(Event.XCMCCSHUTDOWN))) {
                    // TODO Auto-generated method stub
                } else {
                    informHandle.selfDefineEvent(inform);
                }
            }
        } catch (Exception e) {
            String msg = "parseInform error!";
            logger.info(msg, e);
        }
    }
}
