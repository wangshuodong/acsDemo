package com.cmiot.acs.model;

import org.w3c.dom.Element;

/**
 * Created by zjialin on 2016/3/3.
 */
public class RequestDownloadResponse extends AbstractMethod {

    private static final long serialVersionUID = -5705827893272950614L;

    public RequestDownloadResponse() {
        methodName = "RequestDownloadResponse";
    }


    @Override
    protected void addField2Body(Element body, SoapMessageModel soapMessageModel) {

    }

    @Override
    protected void parseBody2Filed(Element body, SoapMessageModel soapMessageModel) {

    }
}
