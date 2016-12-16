package com.cmiot.acs.model.struct;

import com.cmiot.acs.model.SoapMessageModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;


@SuppressWarnings("serial")
public abstract class AbstractStruct implements Serializable {
    /**
     * 将相应的数据结构转换至body中
     *
     * @param body
     * @param soapMessageModel
     */
    public abstract void addThisToBody(Element body, SoapMessageModel soapMessageModel);

    /**
     * 解析相应的数据结构通过body
     *
     * @param body
     * @param soapMessageModel
     */
    public abstract void parseBodyOfThis(Element body, SoapMessageModel soapMessageModel);

    protected String getRequestElement(SoapMessageModel soapMessageModel, Element req, String name) {
        return getRequestChildElement(soapMessageModel, req, name).getTextContent();
    }

    protected Element getRequestChildElement(SoapMessageModel soapMessageModel, Element req, String name) {
        NodeList nodeList = req.getChildNodes();
        Element childElement = null;
        for (int index = 0; index < nodeList.getLength(); index++) {
            Node item = nodeList.item(index);
            if (item.getNodeName().equals(name) && item instanceof Element) {
                childElement = (Element) item;
                break;
            }
        }
        return childElement;
    }
}
