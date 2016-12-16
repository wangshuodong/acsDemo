package com.cmiot.acs.model;


import com.cmiot.acs.model.struct.ParameterList;
import org.w3c.dom.Element;

/**
 * @author zjialin
 */
public class SetParameterValues extends AbstractMethod {
    private static final long serialVersionUID = -7720852412139686283L;
    private static final String ParameterKey = "ParameterKey";

    private ParameterList parameterList;
    private String parameterKey;

    public SetParameterValues() {
        methodName = "SetParameterValues";
        parameterList = new ParameterList();    // TODO
    }


    @Override
    protected void addField2Body(Element body, SoapMessageModel soapMessageModel) {
        parameterList.addThisToBody(body, soapMessageModel);
        Element parameter = soapMessageModel.createElement(ParameterKey);
        parameter.setTextContent(parameterKey);
        body.appendChild(parameter);
    }

    @Override
    protected void parseBody2Filed(Element body, SoapMessageModel soapMessageModel) {
        parameterList.parseBodyOfThis(body, soapMessageModel);
        parameterKey = getRequestElement(body, ParameterKey);
    }

    public String getParameterKey() {
        return parameterKey;
    }

    public void setParameterKey(String parameterKey) {
        this.parameterKey = parameterKey;
    }

    public ParameterList getParameterList() {
        return parameterList;
    }

    public void setParameterList(ParameterList parameterList) {
        this.parameterList = parameterList;
    }
}
