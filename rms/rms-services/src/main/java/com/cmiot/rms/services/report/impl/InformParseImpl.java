package com.cmiot.rms.services.report.impl;

import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.Event;
import com.cmiot.acs.model.struct.EventStruct;
import com.cmiot.rms.services.report.InformHandle;
import com.cmiot.rms.services.report.InformParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by panmingguo on 2016/5/31.
 */
@Service("informParse")
public class InformParseImpl implements InformParse {
    private static final Logger LOGGER = LoggerFactory.getLogger(InformParse.class);

    @Autowired
    private InformHandle informHandle;

    @Override
    public void parseInform(Inform inform) {
        try {
            EventStruct[] events = inform.getEvent().getEventCodes();
            for (EventStruct eventStruct : events) {
                if (eventStruct.getEvenCode().equals(Event.BOOTSTRAP)) {
                    informHandle.bootStrapEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.CONNECTION_REQUEST)) {
                    informHandle.connectionRequestEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.TRANSFER_COMPLETE)) {
                    informHandle.transferCompleteEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.VALUE_CHANGE)) {
                    informHandle.valueChangeEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.DIAGNOSTICS_COMPLETE)) {
                    informHandle.diagnosticsCompleteEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.PERIODIC)) {
                    informHandle.periodicEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.SCHEDULED)) {
                    informHandle.scheduleEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.BOOT)) {
                    informHandle.bootEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.XCMCCMONITOR)) {
                    informHandle.xCmccMonitor(inform);
                } else if (eventStruct.getEvenCode().equals(Event.XCMCCBIND)) {
                    informHandle.xCmccBind(inform);
                } else {
                    LOGGER.error("event error!");
                }
            }
        } catch (Exception e) {
            LOGGER.error("parseInform error!", e);
        }
    }
}
