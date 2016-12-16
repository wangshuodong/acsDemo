package com.cmiot.acs.model.struct;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.SoapMessageModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Event extends AbstractStruct {
    private static final long serialVersionUID = 5220774313188855074L;
    /**
     * EVEN标识固定名称，即xml文件<DeviceId xsi:type="cwmp:DeviceIdStruct">中的属性名
     */
    public static final String EVENT = "Event";
    public static final String EVENTSTRUCT = "EventStruct";
    public static final String EVENTCODE = "EventCode";
    public static final String COMMANDKEY = "CommandKey";
    // 常用evencodes
    public static final String BOOTSTRAP = "0 BOOTSTRAP";
    public static final String BOOT = "1 BOOT";
    public static final String PERIODIC = "2 PERIODIC";
    public static final String SCHEDULED = "3 SCHEDULED";
    public static final String VALUE_CHANGE = "4 VALUE CHANGE";
    public static final String KICKED = "5 KICKED";
    public static final String CONNECTION_REQUEST = "6 CONNECTION REQUEST";
    public static final String TRANSFER_COMPLETE = "7 TRANSFER COMPLETE";
    public static final String DIAGNOSTICS_COMPLETE = "8 DIAGNOSTICS COMPLETE";

    public static final String XCMCCBIND = "X CMCC BIND";
    public static final String XCMCCMONITOR = "X CMCC MONITOR";
    public static final String XCMCCSHUTDOWN = "X CMCC SHUTDOWN";

    private List<EventStruct> eventCodes;

    public Event() {
        eventCodes = new ArrayList<EventStruct>();
    }

    public void setEventCodes(List<EventStruct> eventCodes) {
        this.eventCodes = eventCodes;
    }

    public EventStruct[] getEventCodes() {
        EventStruct[] es = new EventStruct[eventCodes.size()];

        return eventCodes.toArray(es);
    }

    public synchronized void addEventCode(EventStruct evenCode) {
        //TODO 校验？
        if (evenCode == null) {
            return;
        }
        eventCodes.add(evenCode);
    }

    public synchronized void clear() {
        eventCodes.clear();
    }

    public synchronized void removeEventCode(EventStruct evenCode) {
        eventCodes.remove(evenCode);
    }

    @Override
    public void addThisToBody(Element body, SoapMessageModel soapMessageModel) {
        // EVENT,见inform-EVENT的定义
        Element event = soapMessageModel.createElement(EVENT);
        AbstractMethod.getArrayTypeAttribute(event, EVENTSTRUCT, eventCodes.size());
        for (EventStruct es : eventCodes) {
            Element eventStructElement = soapMessageModel.createElement(EVENTSTRUCT);
            Element eventCodeElement = soapMessageModel.createElement(EVENTCODE);
            eventCodeElement.setTextContent(es.getEvenCode());
            eventStructElement.appendChild(eventCodeElement);
            Element commandElement = soapMessageModel.createElement(COMMANDKEY);
            commandElement.setTextContent(es.getCommandKey());
            eventStructElement.appendChild(commandElement);
            event.appendChild(eventStructElement);
        }
        body.appendChild(event);
    }

    @Override
    public void parseBodyOfThis(Element body, SoapMessageModel soapMessageModel) {
        Element eventElement = getRequestChildElement(soapMessageModel, body, EVENT);
        NodeList nodeList = eventElement.getChildNodes();
        for (int index = 0; index < nodeList.getLength(); index++) {
            Node element = nodeList.item(index);
            if (EVENTSTRUCT.equals(element.getNodeName()) && element instanceof Element) {
                Element codeElement = getRequestChildElement(soapMessageModel, (Element) element, EVENTCODE);
                Element keyElement = getRequestChildElement(soapMessageModel, (Element) element, COMMANDKEY);
                EventStruct eventStruct = new EventStruct(codeElement.getTextContent());
                eventStruct.setCommandKey(keyElement.getTextContent());
                eventCodes.add(eventStruct);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sbd = new StringBuilder();
        sbd.append("EventCodes:" + Arrays.toString(eventCodes.toArray()));
        return sbd.toString();
    }
}
