package com.cmiot.rms.services.boxManager.report.impl;

import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.Event;
import com.cmiot.acs.model.struct.EventStruct;
import com.cmiot.rms.services.boxManager.report.BoxInformHandle;
import com.cmiot.rms.services.boxManager.report.BoxInformParse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by panmingguo on 2016/5/31.
 */
@Service
public class BoxInformParseImpl implements BoxInformParse {
    private static final Logger LOGGER = LoggerFactory.getLogger(BoxInformParse.class);

    @Autowired
    private BoxInformHandle boxInformHandle;

    @Override
    public void parseInform(Inform inform) {
        try {
            EventStruct[] events = inform.getEvent().getEventCodes();
            for (EventStruct eventStruct : events) {
                if (eventStruct.getEvenCode().equals(Event.BOOTSTRAP)) {
                    boxInformHandle.bootStrapEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.CONNECTION_REQUEST)) {
                    boxInformHandle.connectionRequestEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.TRANSFER_COMPLETE)) {
                    boxInformHandle.transferCompleteEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.VALUE_CHANGE)) {
                    boxInformHandle.valueChangeEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.DIAGNOSTICS_COMPLETE)) {
                    boxInformHandle.diagnosticsCompleteEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.PERIODIC)) {
                    boxInformHandle.periodicEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.SCHEDULED)) {
                    boxInformHandle.scheduleEvent(inform);
                } else if (eventStruct.getEvenCode().equals(Event.BOOT)) {
                    boxInformHandle.bootEvent(inform);
                }  else if (eventStruct.getEvenCode().equals(Event.XCMCCMONITOR)) {
                    boxInformHandle.xCmccMonitor(inform);
                }
                /*魔白盒关机指令
                else if(eventStruct.getEvenCode().equalsIgnoreCase(Event.XCMCCSHUTDOWN)){
                	boxInformHandle.mXCmccShutdown(inform);
                }*/
                //九州机顶盒关机指令
                else if(eventStruct.getEvenCode().equalsIgnoreCase("X CMCC SHUTDOWN")){
                	boxInformHandle.mXCmccShutdown(inform);
                }
                else {
                    LOGGER.error("event error!");
                }
            }
        } catch (Exception e) {
            LOGGER.error("parseInform error!", e);
        }
    }
}
