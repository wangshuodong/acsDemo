package com.cmiot.acs.model;

import org.w3c.dom.Element;

/**
 * @author zjialin
 */
public class DeleteObject extends AbstractMethod {
    private static final long serialVersionUID = 8407006987085444751L;
    private static final String ObjectName = "ObjectName";
    private static final String ParameterKey = "ParameterKey";

    private String objectName;
    private String parameterKey;

    public DeleteObject() {
        methodName = "DeleteObject";
        objectName = "";
        parameterKey = "";
        this.acs2CpeEnv = false;        // 默认是CPE（Required）,ACS（Optinal）
    }

    public DeleteObject(String objectName, String commandKey) {
        this();
        this.parameterKey = commandKey;
        this.objectName = objectName;
    }

    @Override
    protected void addField2Body(Element body, SoapMessageModel soapMessageModel) {
        Element objItem = soapMessageModel.createElement(ObjectName);
        objItem.setTextContent(objectName);
        Element commanItem = soapMessageModel.createElement(ParameterKey, parameterKey);
        commanItem.setTextContent(parameterKey);
        body.appendChild(objItem);
        body.appendChild(commanItem);

    }

    @Override
    protected void parseBody2Filed(Element body, SoapMessageModel soapMessageModel) {
        objectName = getRequestChildElement(body, ObjectName).getTextContent();
        parameterKey = getRequestChildElement(body, ParameterKey).getTextContent();
    }

    @Override
    public String toString() {
        StringBuilder sbd = new StringBuilder();
        sbd.append(" ObjectName:" + objectName);
        sbd.append(" ParameterKey:" + parameterKey);
        return super.toString() + sbd.toString();
    }


    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getParameterKey() {
        return parameterKey;
    }

    public void setParameterKey(String parameterKey) {
        this.parameterKey = parameterKey;
    }
}
