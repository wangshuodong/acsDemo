package com.cmiot.acs.model;


import com.cmiot.acs.model.struct.FaultStruct;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class Fault extends AbstractMethod {
    private static final long serialVersionUID = -5209440807540799860L;

    private static final String lowerFaultCode = "faultcode";
    private static final String lowerFaultString = "faultstring";
    private static final String detail = "detail";
    private static final String upperFaultCode = "FaultCode";
    private static final String upperFaultString = "FaultString";

    private FaultStruct faultStruct;
    private String faultcode;
    private String faultstring;

    public Fault() {
        methodName = "Fault";
        faultStruct = new FaultStruct();
    }

    public Fault(FaultStruct faultStruct, String faultcode, String faultstring) {
        methodName = "Fault";
        this.faultStruct = faultStruct;
        this.faultcode = faultcode;
        this.faultstring = faultstring;
    }


    protected void parseFaultDetail(NodeList details) {
    }

    @Override
    protected void addField2Body(Element body, SoapMessageModel soapMessageModel) {
        // TODO Auto-generated method stub
        Element faultCodeElement = soapMessageModel.createElement(lowerFaultCode);
        faultCodeElement.setTextContent(faultcode);
        Element faultStringElement = soapMessageModel.createElement(lowerFaultString);
        faultStringElement.setTextContent(faultstring);
        body.appendChild(faultCodeElement);
        body.appendChild(faultStringElement);

        Element detailElement = soapMessageModel.createElement(detail);
        body.appendChild(detailElement);
        Element faultElement = soapMessageModel.createElement(CWMP + ":" + methodName);

        Element upperFaultCodeElement = soapMessageModel.createElement(upperFaultCode);
        upperFaultCodeElement.setTextContent(String.valueOf(faultStruct.getFaultCode()));
        Element upperFaultStringElement = soapMessageModel.createElement(upperFaultString);
        upperFaultStringElement.setTextContent(faultStruct.getFaultString());

        faultElement.appendChild(upperFaultCodeElement);
        faultElement.appendChild(upperFaultStringElement);

        detailElement.appendChild(faultElement);

    }

    @Override
    protected void parseBody2Filed(Element body, SoapMessageModel soapMessageModel) {

        faultcode = getRequestChildElement(body, lowerFaultCode).getTextContent();
        faultstring = getRequestChildElement(body, lowerFaultString).getTextContent();

        Element fElement = getFaultElement(soapMessageModel);
        faultStruct.setFault(Integer.parseInt(getRequestElement(fElement, upperFaultCode)), getRequestElement(fElement, upperFaultString));
    }

    public FaultStruct getFaultStruct() {
        return faultStruct;
    }

    public String getFaultcode() {
        return faultcode;
    }

    public void setFaultcode(String faultcode) {
        this.faultcode = faultcode;
    }

    public String getFaultstring() {
        return faultstring;
    }

    public void setFaultstring(String faultstring) {
        this.faultstring = faultstring;
    }

}
