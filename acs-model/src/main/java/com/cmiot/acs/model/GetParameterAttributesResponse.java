package com.cmiot.acs.model;

import com.cmiot.acs.model.struct.ParameterAttributeStruct;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;


/**
 * @author zjialin
 */
public class GetParameterAttributesResponse extends AbstractMethod {
    private static final long serialVersionUID = 4386887185073866809L;

    private static final String ParameterList = "ParameterList";
    private static final String ParameterAttributeStruct = "ParameterAttributeStruct";
    private static final String Name = "Name";
    private static final String Notification = "Notification";
    private static final String AccessList = "AccessList";
    private static final String STR = "string";

    private List<ParameterAttributeStruct> paramAttrList;

    public GetParameterAttributesResponse() {
        methodName = "GetParameterAttributesResponse";
        paramAttrList = new ArrayList<ParameterAttributeStruct>();
    }

    public synchronized void addParamAttrStruct(ParameterAttributeStruct pas) {
        paramAttrList.add(pas);
    }

    public List<ParameterAttributeStruct> getParamAttrList() {
        return paramAttrList;
    }

    public void setParamAttrList(List<ParameterAttributeStruct> paramAttrList) {
        this.paramAttrList = paramAttrList;
    }

    @Override
    protected void addField2Body(Element body, SoapMessageModel soapMessageModel) {
        Element paramElement = soapMessageModel.createElement(ParameterList);
        if (paramAttrList.size() > 0) {
            getArrayTypeAttribute(paramElement, ParameterAttributeStruct, paramAttrList.size());
            for (ParameterAttributeStruct pas : paramAttrList) {
                Element paramAttrStructElement = soapMessageModel.createElement(ParameterAttributeStruct);
                Element nameItem = soapMessageModel.createElement(Name);
                nameItem.setTextContent(pas.getName());
                paramAttrStructElement.appendChild(nameItem);
                Element notificationItem = soapMessageModel.createElement(Notification);
                notificationItem.setTextContent(String.valueOf(pas.getNotification()));
                paramAttrStructElement.appendChild(notificationItem);
                String[] accessArray = pas.getAccessList();
                Element accessListItem = soapMessageModel.createElement(AccessList);
                getArrayTypeAttribute(paramAttrStructElement, STR, accessArray.length);
                for (String access : accessArray) {
                    Element strItem = soapMessageModel.createElement(STR);
                    strItem.setTextContent(access);
                    accessListItem.appendChild(strItem);
                }
                paramAttrStructElement.appendChild(accessListItem);
                paramElement.appendChild(paramAttrStructElement);
            }
        }

        body.appendChild(paramElement);
    }

    @Override
    protected void parseBody2Filed(Element body, SoapMessageModel soapMessageModel) {
        Element paramElement = getRequestChildElement(body, ParameterList);
        NodeList nodeList = paramElement.getChildNodes();

        for (int index = 0; index < nodeList.getLength(); index++) {
            Node item = nodeList.item(index);
            if (item.getNodeName().equals(ParameterAttributeStruct)) {
                ParameterAttributeStruct pas = new ParameterAttributeStruct();
                NodeList structList = item.getChildNodes();

                //获取ParameterAttributesStruct下的
                for (int i = 0; i < structList.getLength(); i++) {
                    Node element = structList.item(i);
                    if (element.getNodeName().equals(Name)) {
                        pas.setName(element.getTextContent());

                    } else if (element.getNodeName().equals(Notification)) {
                        pas.setNotification(Integer.parseInt(element.getTextContent()));
                    }
                    //读取STR
                    else if (element.getNodeName().equals(AccessList)) {
                        NodeList accessNodeList = element.getChildNodes();
                        for (int acc = 0; acc < accessNodeList.getLength(); acc++) {
                            Node accElement = accessNodeList.item(acc);
                            if (accElement.getNodeName().equalsIgnoreCase(STR)) {
                                pas.addAccessObj(accElement.getTextContent());
                            }
                        }
                    }
                }
                paramAttrList.add(pas);
            }

        }
    }

    @Override
    public String toString() {
        StringBuilder sbd = new StringBuilder();
        sbd.append("\r\nParamAttrList:");
        for (ParameterAttributeStruct pas : paramAttrList) {
            sbd.append("\r\n" + pas.toString());
        }
        return super.toString() + sbd.toString();
    }

}
