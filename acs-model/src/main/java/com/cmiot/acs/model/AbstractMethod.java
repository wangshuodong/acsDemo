package com.cmiot.acs.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.NoSuchElementException;


/**
 * 模型抽象类，所有CPE,ACS方法（模型）都是基于该类的<br>
 * <b>主要功能是：</b>将模型转换为SoapMessage进行发送；或者接收SoapMessage并解析到模型中。<br>
 * <b>SoapMessage</b>即SOAPXml,包括了SoapEnvelope,SoapHeader,SoapBody三个部分组成。
 * 其中SoapPart和 SoapHeader在该类中默认生成或者解析，SoapBody由继承该类的模型进行生成或者解析 (见
 * <br>
 * <p/>
 * SOAP是基于HTTP传输的xml信息，<b>SoapMessage</b>封装了SOAPXml信息，并通{@code
 * #writeTo(OutputStream)} 或者{@code #writeTo(URL)}转换为HTTP或者底层数据流，进行网络传输。<br>
 * <p/>
 * <pre>
 * <b>附：</b>
 *  SOAP 是基于 XML 的简易协议，可使应用程序在 HTTP 之上进行信息交换。 或者更简单地说：SOAP 是用于访问网络服务的协议。
 *  一条 SOAP 消息就是一个普通的 XML 文档，包含下列元素： 必需的 Envelope 元素，可把此 XML 文档标识为一条 SOAP 消息
 *  可选的 Header元素，包含头部信息 必需的 Body 元素，包含所有的调用和响应信息 可选的 Fault 元素，提供有关在处理此消
 *  息所发生错误的信息。
 *
 *  <b>语法规则</b>
 * 	这里是一些重要的语法规则：
 * 	SOAP 消息必须用 XML 来编码
 * 	SOAP 消息必须使用 SOAP Envelope 命名空间
 * 	SOAP 消息必须使用 SOAP Encoding 命名空间
 * 	SOAP 消息不能包含 DTD 引用
 * 	SOAP 消息不能包含 XML 处理指令
 *
 * 	<b>SOAP 消息的基本结构（实例）</b>
 * 		<?xml version="1.0"?>
 * 		<soap:Envelope
 * 		xmlns:soap="http://www.w3.org/2001/12/soap-envelope"
 * 		soap:encodingStyle="http://www.w3.org/2001/12/soap-encoding">
 * 		<soap:Header>
 * 		  ...
 * 		  ...
 * 		</soap:Header>
 * 		<soap:Body>
 * 		  ...
 * 		  ...
 * 		  <soap:Fault>
 * 		    ...
 * 		    ...
 * 		  </soap:Fault>
 * 		</soap:Body>
 * 		</soap:Envelope>
 *
 * @author zjialin
 * @date 2016-01-06
 */
public abstract class AbstractMethod implements Serializable {
    public static final String METHOD_CLASS_PATH = AbstractMethod.class.getName().substring(0, AbstractMethod.class.getName().lastIndexOf(".") + 1);
    private static final long serialVersionUID = -5424823002848936758L;
    //所有soap-xml命名声明全局变量，TODO cwmp-1-1?
    public static final String URN_CWMP1_0 = "urn:dslforum-org:cwmp-1-0";
    public static final String URN_CWMP1_1 = "urn:dslforum-org:cwmp-1-1";
    public static final String URL_XSD = "http://www.w3.org/2001/XMLSchema";
    public static final String URL_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String URL_ENCODE = "http://schemas.xmlsoap.org/soap/encoding/";
    public static final String URL_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String CWMP = "cwmp";
    public static final String XSD = "xsd";
    public static final String XSI = "xsi";
    public static final String SOAP_ENC = "SOAP-ENC";
    public static final String SOAP_ENV = "SOAP-ENV";
    public static final String XMLNS = "xmlns";
    // 所有soap-xml头声明全局变量
    private static final String ID = "ID";                                                        // 必填项
    private static final String HoldRequests = "HoldRequests";                 // 从cpe回复到acs中不需要,默认0 为false,1为true
    private static final String NoMoreRequests = "NoMoreRequests";     // 默认0 为false,1为true
    private static final String MustUnderstand = "SOAP-ENV:mustUnderstand";    // 标识是否存在
    // 通用属性（主要是头信息）
    protected String requestId;                   // 每一次请求的id，由请求方决定
    protected String methodName;           // 方法名称
    protected boolean acs2CpeEnv;          // 是否是acs端到cpe端的信包
    private boolean noMoreReqs;             // 是否有更多请求
    private boolean holdReqs;                  // 如果ACS需要更新控制流，可以设置，并且mustunderstand为1，只能使用在acs中
    private boolean requeired;                  // 是否该方法是必选的
    private String urnCwmpVersion;         // 选用CWMP版本
    // 反向连接设备必要属性
    private String cpeId;                             //ACS向CPE发送指令的时候需要 cpeId
    private String cpeUrl;                            //ACS向CPE发送指令的时候需要 cpeUrl的公网地址
    private String cpeUserName = "cpe";  //维护账号用户名。省级数字家庭管理平台反响连接设备是使用。
    private String cpePassword = "cpe";    //维护账号的密码，省级数字家庭管理平台反响连接设备是使用。
    private String gid;                                 //ACS使用流水号
    private long deviceType = 1;               //设备类型(1网关/2机顶盒)
    // 反向连接设备用HTTP或UDP
    private String isHttpOrUdp = "HTTP";  // 反向连接设备用HTTP或UDP

    private String callbackUrl;

    /**
     * 默认代表从ACS到CPE的信包
     */
    public AbstractMethod() {
        iniModel(true);
    }

    /**
     * @param acs2CpeEnv 为true时代表从ACS到CPE的信包，否则反过来
     */
    public AbstractMethod(boolean acs2CpeEnv) {
        iniModel(acs2CpeEnv);
    }

    private void iniModel(boolean acs2CpeEnv) {
        this.acs2CpeEnv = acs2CpeEnv;
        this.noMoreReqs = false;
        this.holdReqs = false;
        this.requeired = true;
        this.methodName = "";
        this.requestId = "";
        urnCwmpVersion = URN_CWMP1_1;        // 默认版本
    }

    /**
     * 构造各自的SoapBody对应的xml文件
     */
    protected abstract void addField2Body(Element body, SoapMessageModel soapMessageModel);

    /**
     * 解析各自的SoapBody对应的xml文件至模型字段中
     */
    protected abstract void parseBody2Filed(Element body, SoapMessageModel soapMessageModel);


    /**
     * 将SOAPMessage 解析值相应的数据模型值中
     */
    public void parse(SoapMessageModel soapMessageModel) throws NoSuchElementException {
        if (soapMessageModel.getSoapHeader() != null && soapMessageModel.getSoapHeader().hasChildNodes()) {
            NodeList headList = soapMessageModel.getSoapHeader().getChildNodes();
            for (int index = 0; index < headList.getLength(); index++) {
                Node headItem = headList.item(index);
                if (headItem.getNodeName().startsWith(CWMP + ":" + ID)) {
                    requestId = headItem.getTextContent();
                }
                if (headItem.getNodeName().startsWith(CWMP + ":" + HoldRequests)) {
                    String value = headItem.getTextContent();
                    holdReqs = Boolean.parseBoolean(value);
                }
                if (headItem.getNodeName().startsWith(CWMP + ":" + NoMoreRequests)) {
                    String value = headItem.getTextContent();
                    noMoreReqs = Boolean.parseBoolean(value);
                }
            }
        }

        try {
            Element body = null;
            if (!isFault()) {
                body = getRequestChildElement(soapMessageModel.getSoapBody(), CWMP + ":" + methodName);

            } else {
                body = getRequestChildElement(soapMessageModel.getSoapBody(), SOAP_ENV + ":" + methodName);
            }
            parseBody2Filed(body, soapMessageModel);
        } catch (Exception e) {
        }

    }

    /**
     * 将数据模型值解析到SoapMessageModel中
     *
     * @param soapMessageModel
     */
    public void addAllSoap(SoapMessageModel soapMessageModel) {
        // 写到Cpe端，需要添加<?xml version=1.0 encoding='utf-8'>
        soapMessageModel.getDocument().setXmlStandalone(true);
        addNameSpaceDeclaretion(soapMessageModel);
        addIdElement2Header(soapMessageModel, requestId);
        if (acs2CpeEnv) {
            if (holdReqs) {
                addHoldReqsElement2Header(soapMessageModel, holdReqs);
            }
        }
        if (noMoreReqs) {
            addNoMoreReqsElement2Header(soapMessageModel, noMoreReqs);
        }
        Element bodyElement = null;
        if (methodName.equals("Fault")) {
            bodyElement = soapMessageModel.createElement(SOAP_ENV + ":" + methodName);
        } else {
            bodyElement = soapMessageModel.createElementNS(methodName, CWMP, urnCwmpVersion);
        }
        soapMessageModel.getSoapBody().appendChild(bodyElement);
        try {
            addField2Body(bodyElement, soapMessageModel);
        } catch (Exception e) {
        }

    }


    /**
     * 获取 requestId
     *
     * @param soapMessageModel
     * @return
     */
    public static String getRequestId(SoapMessageModel soapMessageModel) {
        String reqId = "";
        if (soapMessageModel.getSoapHeader() != null && soapMessageModel.getSoapHeader().hasChildNodes()) {
            NodeList headList = soapMessageModel.getSoapHeader().getChildNodes();
            for (int index = 0; index < headList.getLength(); index++) {
                Node headItem = headList.item(index);
                if (headItem.getNodeName().startsWith(CWMP + ":" + ID)) {
                    reqId = headItem.getTextContent();
                }
            }
        }
        return reqId;
    }


    //===================================通用属性（主要是头信息）======================================
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String _requestId) {
        this.requestId = _requestId;
    }

    public boolean isAcs2CpeEnv() {
        return acs2CpeEnv;
    }

    public void setAcs2CpeEnv(boolean acs2CpeEnv) {
        this.acs2CpeEnv = acs2CpeEnv;
    }

    public boolean isNoMoreReqs() {
        return noMoreReqs;
    }

    public void setNoMoreReqs(boolean noMoreReqs) {
        this.noMoreReqs = noMoreReqs;
    }

    public boolean isHoldReqs() {
        return holdReqs;
    }

    public void setHoldReqs(boolean holdReqs) {
        this.holdReqs = holdReqs;
    }

    public boolean isRequeired() {
        return requeired;
    }

    public void setRequeired(boolean requeired) {
        this.requeired = requeired;
    }

    public String getUrnCwmpVersion() {
        return urnCwmpVersion;
    }

    public void setUrnCwmpVersion(String urnCwmpVersion) {
        if (URN_CWMP1_0.equalsIgnoreCase(urnCwmpVersion)) {
            this.urnCwmpVersion = URN_CWMP1_0;
        } else {
            this.urnCwmpVersion = URN_CWMP1_1;
        }
    }

    //===================================反向连接设备必要属性======================================
    public String getCpeId() {
        return cpeId;
    }

    public void setCpeId(String cpeId) {
        this.cpeId = cpeId;
    }

    public String getCpeUrl() {
        return cpeUrl;
    }

    public void setCpeUrl(String cpeUrl) {
        this.cpeUrl = cpeUrl;
    }

    public String getCpeUserName() {
        return cpeUserName;
    }

    public void setCpeUserName(String cpeUserName) {
        this.cpeUserName = cpeUserName;
    }

    public String getCpePassword() {
        return cpePassword;
    }

    public void setCpePassword(String cpePassword) {
        this.cpePassword = cpePassword;
    }

    //====================================ACS 自定义扩展=================================================
    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public long getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(long deviceType) {
        this.deviceType = deviceType;
    }

    public String getIsHttpOrUdp() {
        return isHttpOrUdp;
    }

    public void setIsHttpOrUdp(String isHttpOrUdp) {
        this.isHttpOrUdp = isHttpOrUdp;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @Override
    public String toString() {
        StringBuilder sbd = new StringBuilder();
        sbd.append("MethodName:");
        sbd.append(methodName);
        sbd.append(" RequestId:");
        sbd.append(requestId);
        return sbd.toString();
    }


    public boolean isFault() {
        return methodName.equalsIgnoreCase("Fault");
    }

    protected String b2s(boolean b) {
        return (b) ? "1" : "0";
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (requestId != null) {
            byte[] bytes = requestId.getBytes();
            for (byte b : bytes) {
                result += b;
            }
        }
        if (methodName != null) {
            byte[] bytes = methodName.getBytes();
            for (byte b : bytes) {
                result += b;
            }
        }
        return result * 37;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AbstractMethod) {
            // 方法名称和请求ID相等时，才可以认为是同一个对象
            AbstractMethod method = (AbstractMethod) obj;
            if (method.getMethodName().equals(this.getMethodName()) && (method.getRequestId().equals(this.getRequestId()))) {
                return true;
            }
        }
        return false;
    }

    protected static Element getFaultElement(SoapMessageModel soapMessageModel) {
        Element soapFaultElement = getRequestChildElement(soapMessageModel.getSoapBody(), SOAP_ENV + ":" + "Fault");
        Element detailElement = getRequestChildElement(soapFaultElement, "detail");
        Element fElement = getRequestChildElement(detailElement, CWMP + ":" + "Fault");
        return fElement;
    }

    /**
     * 添加所有的命名空间
     *
     * @throws SOAPException
     */
    private void addNameSpaceDeclaretion(SoapMessageModel soapMessageModel) {
        // 需添加前缀xmlns
        soapMessageModel.getRootNode().setAttribute(XMLNS + ":" + SOAP_ENV, URL_ENVELOPE);
        soapMessageModel.getRootNode().setAttribute(XMLNS + ":" + SOAP_ENC, URL_ENCODE);
        soapMessageModel.getRootNode().setAttribute(XMLNS + ":" + XSD, URL_XSD);
        soapMessageModel.getRootNode().setAttribute(XMLNS + ":" + XSI, URL_XSI);
        soapMessageModel.getRootNode().setAttribute(XMLNS + ":" + CWMP, urnCwmpVersion);
    }

    private void addIdElement2Header(SoapMessageModel soapMessageModel, String idValue) {
        Element cwmpIdE = soapMessageModel.createElementNS(ID, CWMP, urnCwmpVersion);
        // 设置element值;setTextContent此属性返回此节点及其后代的文本内容;setNodeValue()此节点的值，取决于其类型
        cwmpIdE.setTextContent(idValue);
        // 必须标识为1
        cwmpIdE.setAttribute(MustUnderstand, "1");
        soapMessageModel.getSoapHeader().appendChild(cwmpIdE);
    }

    private void addHoldReqsElement2Header(SoapMessageModel soapMessageModel, boolean request) {
        Element cwmpIdE = soapMessageModel.createElementNS(HoldRequests, CWMP, urnCwmpVersion);
        // 设置element值;setTextContent此属性返回此节点及其后代的文本内容;setNodeValue()此节点的值，取决于其类型
        cwmpIdE.setTextContent(request == true ? "1" : "0");
        // 必须标识为1
        cwmpIdE.setAttribute(MustUnderstand, "1");
        soapMessageModel.getSoapHeader().appendChild(cwmpIdE);
    }

    private void addNoMoreReqsElement2Header(SoapMessageModel soapMessageModel, boolean noMoreReqs) {
        Element cwmpIdE = soapMessageModel.createElementNS(NoMoreRequests, CWMP, urnCwmpVersion);
        // 设置element值;setTextContent此属性返回此节点及其后代的文本内容;setNodeValue()此节点的值，取决于其类型
        cwmpIdE.setTextContent(noMoreReqs == true ? "1" : "0");
        // 必须标识为1
        cwmpIdE.setAttribute(MustUnderstand, noMoreReqs == true ? "1" : "0");
        soapMessageModel.getSoapHeader().appendChild(cwmpIdE);
    }

    private static ByteArrayOutputStream getMsg2OutStream(SoapMessageModel soapMessageModel) throws Exception {
        ByteArrayOutputStream myOutStr = new ByteArrayOutputStream();
        StreamResult strResult = new StreamResult();
        strResult.setOutputStream(myOutStr);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty(OutputKeys.METHOD, "xml");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        t.transform(new DOMSource(soapMessageModel.getRootNode()), strResult);
        return myOutStr;
    }

    protected static String getRequestElement(Element req, String name) {
        return getRequestChildElement(req, name).getTextContent();
    }

    protected static Element getRequestChildElement(Element req, String name) {
        Element result = null;
        NodeList nodeList = req.getChildNodes();
        for (int index = 0; index < nodeList.getLength(); index++) {
            Node item = nodeList.item(index);
            if (item.getNodeName().equalsIgnoreCase(name) && item instanceof Element) {
                result = (Element) item;
                break;
            }
        }
        return result;
    }


    public static void getArrayTypeAttribute(Element paramAttrElement, String arrayType, int arrayLen) {
        paramAttrElement.setAttribute((XSI + ":type"), (SOAP_ENC + ":Array"));
        paramAttrElement.setAttribute((SOAP_ENC + ":arrayType"), (CWMP + ":" + arrayType + "[" + String.valueOf(arrayLen) + "]"));
    }

    public static Element getRequest(SoapMessageModel soapMessageModel) {
        Element request = null;
        NodeList nodeList = soapMessageModel.getSoapBody().getChildNodes();
        for (int index = 0; index < nodeList.getLength(); index++) {
            Node n = nodeList.item(index);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                request = (Element) n;
            }
        }
        return request;
    }

    public static String getRequestName(SoapMessageModel soapMessageModel) {
        String name = getRequest(soapMessageModel).getNodeName();
        if (name.startsWith("cwmp:")) {
            name = name.substring(5);
        } else {
            name = "Fault";
        }
        return name;
    }

    /**
     * 将SoapMessageModel解析成String
     *
     * @param soapMessageModel
     */
    public static String getMsgToString(SoapMessageModel soapMessageModel) {
        try {
            ByteArrayOutputStream myOutStr = getMsg2OutStream(soapMessageModel);
            return myOutStr.toString();
        } catch (Exception e) {
        }
        return "";
    }


    /**
     * 将String解析成SoapMessageModel
     *
     * @param string
     */
    public static SoapMessageModel getStringToMsg(String string) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new ByteArrayInputStream(string.trim().getBytes()));
            SoapMessageModel soapMessageModel = new SoapMessageModel(document);
            return soapMessageModel;
        } catch (Exception e) {
            return null;
        }
    }


}
