package com.cmiot.acs.model.struct;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.SoapMessageModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author zjialin
 */
public class ParameterList extends AbstractStruct {

    private static final long serialVersionUID = 9119333307832052200L;
    private static final String ParameterListNode = "ParameterList";
    private static final String ParameterValueStructNode = "ParameterValueStruct";
    private static final String NameNode = "Name";
    private static final String ValueNode = "Value";
    // Intert网关必备
    public static final String SpecVesion = "InternetGatewayDevice.DeviceInfo.SpecVersion";
    public static final String HardwareVersion = "InternetGatewayDevice.DeviceInfo.HardwareVersion";
    public static final String SoftwareVesion = "InternetGatewayDevice.DeviceInfo.SoftwareVersion";
    public static final String ProvisioningCode = "InternetGatewayDevice.DeviceInfo.ProvisioningCode";
    public static final String ConnectionRequestURL = "InternetGatewayDevice.ManagementServer.ConnectionRequestURL";
    public static final String ParameterKey = "InternetGatewayDevice.ManagementServer.ParameterKey";
    public static final String Password = "InternetGatewayDevice.X_CMCC_UserInfo.Password";
    // TODO??
    public static final String ExternalIPAddress = "InternetGatewayDevice.WANDevice.{i}WANConnectionDevice.{j}.WAN{***}Connection.{k}.ExternalIPAddress";
    @SuppressWarnings("unchecked")
    private List<ParameterValueStruct> parameterValueStructs;

    @SuppressWarnings("unchecked")
    public ParameterList() {
        parameterValueStructs = new ArrayList<ParameterValueStruct>();
    }

    @SuppressWarnings("unchecked")
    public List<ParameterValueStruct> getParameterValueStructs() {
        return parameterValueStructs;
    }

    @SuppressWarnings("unchecked")
    public synchronized void addParamValues(ParameterValueStruct... paramValues) {
        //TODO 校验？
        if (paramValues == null) {
            return;
        }
        addParamValues(Arrays.asList(paramValues));
    }

    @SuppressWarnings("unchecked")
    public synchronized void addParamValues(List<ParameterValueStruct> pvsList) {
        if (pvsList != null) {
            parameterValueStructs.addAll(pvsList);
        }
    }

    public int size() {
        return parameterValueStructs.size();
    }

    public synchronized void clear() {
        parameterValueStructs.clear();
    }

    @SuppressWarnings("unchecked")
    public synchronized void removeParamValues(ParameterValueStruct paramValue) {
        parameterValueStructs.remove(paramValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addThisToBody(Element body, SoapMessageModel soapMessageModel) {
        Element paramListElement = soapMessageModel.createElement(ParameterListNode);
        if (parameterValueStructs.size() > 0) {
            AbstractMethod.getArrayTypeAttribute(paramListElement, ParameterValueStructNode, parameterValueStructs.size());
            for (ParameterValueStruct ps : parameterValueStructs) {
                Element paramenterValueStruct = soapMessageModel.createElement(ParameterValueStructNode);
                Element nameElement = soapMessageModel.createElement(NameNode);
                nameElement.setTextContent(ps.getName());
                Element valueElement = soapMessageModel.createElement(ValueNode);
                valueElement.setTextContent(String.valueOf(ps.getValue()));
                valueElement.setAttribute("xsi:type", "xsd:" + ps.getValueType());
                paramenterValueStruct.appendChild(nameElement);
                paramenterValueStruct.appendChild(valueElement);
                paramListElement.appendChild(paramenterValueStruct);
            }
        }
        body.appendChild(paramListElement);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parseBodyOfThis(Element body, SoapMessageModel soapMessageModel) {
        Element paramListElement = getRequestChildElement(soapMessageModel, body, ParameterListNode);
        NodeList nodeList = paramListElement.getElementsByTagName(ParameterValueStructNode);
        for (int index = 0; index < nodeList.getLength(); index++) {
            Node item = nodeList.item(index);
            if (item instanceof Element) {
                Element nameElement = getRequestChildElement(soapMessageModel, (Element) item, NameNode);
                Element valueElement = getRequestChildElement(soapMessageModel, (Element) item, ValueNode);
                // TODO区分不同类型
                ParameterValueStruct paramStruct = null;
                String valueAttr = valueElement.getAttribute("xsi:type");
                String nameElementTc = nameElement.getTextContent();
                String valueElementTc = valueElement.getTextContent();
                if (valueAttr == null || valueAttr.trim().equals("")) {
                    // 默认为Object方式
                    paramStruct = new ParameterValueStructObject(nameElementTc, valueElementTc);
                } else {
                    try {
                        String valueType = valueAttr.split("xsd:")[1];
                        if (valueType.equals(ParameterValueStruct.Type_String)) {
                            paramStruct = new ParameterValueStructStr(nameElementTc, valueElementTc);
                        } else if (valueType.equals(ParameterValueStruct.Type_Int)) {
                            if (valueElementTc != null && valueElementTc.length() > 0) {
                                paramStruct = new ParameterValueStructInt(nameElementTc, Integer.valueOf(valueElementTc));
                            } else {
                                paramStruct = new ParameterValueStructInt(nameElementTc, null);
                            }
                        } else if (valueType.equals(ParameterValueStruct.Type_UnsignedInt)) {
                            if (valueElementTc != null && valueElementTc.length() > 0) {
                                paramStruct = new ParameterValueStructUnsignedInt(nameElementTc, Long.valueOf(valueElementTc));
                            } else {
                                paramStruct = new ParameterValueStructUnsignedInt(nameElementTc, null);
                            }
                        } else if (valueType.equals(ParameterValueStruct.Type_DateTime)) {
                            if (valueElementTc != null && valueElementTc.length() > 0) {
                                paramStruct = new ParameterValueStructDateTime(nameElementTc, valueElementTc);
                            } else {
                                paramStruct = new ParameterValueStructDateTime(nameElementTc, null);
                            }
                        } else if (valueType.equals(ParameterValueStruct.Type_Boolean)) {
                            if (valueElementTc != null && valueElementTc.length() > 0) {
                                valueElementTc = valueElementTc.equals("1") ? "true" : valueElementTc.equals("0") ? "false" : valueElementTc;
                                paramStruct = new ParameterValueStructBoolean(nameElementTc, Boolean.parseBoolean(valueElementTc));
                            } else {
                                paramStruct = new ParameterValueStructBoolean(nameElementTc, null);
                            }
                        } else {
                            paramStruct = new ParameterValueStructObject(nameElementTc, valueElementTc);
                        }
                    } catch (Exception e) {
                    }
                }
                parameterValueStructs.add(paramStruct);
            }
        }
    }

    public void setParameterValueStructs(List<ParameterValueStruct> parameterValueStructs) {
        this.parameterValueStructs = parameterValueStructs;
    }


    @Override
    public String toString() {
        StringBuilder sbd = new StringBuilder();
        sbd.append("ParameterValueStructs:" + Arrays.toString(parameterValueStructs.toArray()));
        return sbd.toString();
    }
}
