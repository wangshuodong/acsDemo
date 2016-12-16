package com.cmiot.acs.model.struct;

import com.cmiot.acs.model.SoapMessageModel;
import org.w3c.dom.Element;

/**
 * tr069中的设备id, 包括了：<br>
 * DeviceID：设备id以及其对应的元素：
 * <li>Manufacturer：显示设备制造商(String 64)<br>
 * <li>OUI：制造商标识，共六位(String 6)，表示为十六进制，并且都要大小和包括任何的0的字符，如00FF11<br>
 * <li>ProductClass：设备类型标识，在该类型下，serialNumbler必须是唯一的(String 64)<br>
 * <li>SerialNumber：设备唯一标识(String 64)<br>
 * 如：
 * "<DeviceId xsi:type="cwmp:DeviceIdStruct">
 * <Manufacturer xsi:type="xsd:string[64]">GDT</Manufacturer>
 * <OUI xsi:type="xsd:string[6]">123456</OUI>
 * <ProductClass xsi:type="xsd:string[64]">IGD</ProductClass>
 * <SerialNumber xsi:type="xsd:string[64]">AAAA-BBBB-CCCC-DDDD</SerialNumber>
 * </DeviceId>"
 * <p/>
 * <br>该结构是在inform中的组成部分。
 *
 * @author zjialin
 */
public class DeviceId extends AbstractStruct {
    private static final long serialVersionUID = -5401425663873055259L;
    /**
     * 设备id标识固定名称，即xml文件<DeviceId xsi:type="cwmp:DeviceIdStruct">中的属性名
     */
    public static final String DEVICEID = "DeviceId";
    /**
     * 制造商标识固定名称，是{@link }的子元素，即xml文件<Manufacturer xsi:type="xsd:string[64]">GDT</Manufacturer>中的属性名
     */
    public static final String MANUFACTURER = "Manufacturer";
    /**
     * 制造商标识固定名称，是{@link }的子元素，即xml文件<OUI xsi:type="xsd:string[6]">123456</OUI>中的属性名
     */
    public static final String OUI = "OUI";
    /**
     * 设备类型标识固定名称，是{@link }的子元素，即xml文件<ProductClass xsi:type="xsd:string[64]">IGD</ProductClass>中的属性名
     */
    public static final String PRODUCTCLASS = "ProductClass";
    /**
     * 设备唯一标识固定名称，是{@link }的子元素，即xml文件<SerialNumber xsi:type="xsd:string[64]">AAAA-BBBB-CCCC-DDDD</SerialNumber>中的属性名
     */
    public static final String SERIALNUMBER = "SerialNumber";
    private String cpeId;        // 设备名称
    /**
     * 一下是{@link DeviceId}的组成元素
     */
    private String manufacturer;        // 显示设备制造商(String 64)
    private String oui;                    // 制造商标识，共六位(String 6)，表示为十六进制，并且都要大小和包括任何的0的字符，如00FF11
    private String productClass;        // 设备类型标识，在该类型下，serialNumbler必须是唯一的(String 64)
    private String serialNubmer;        // 设备唯一标识(String 64)

    public DeviceId() {
    }

    public DeviceId(String manufacturer, String oui, String productClass, String serialNubmer) {
        setDeviceIdInfor(manufacturer, oui, productClass, serialNubmer);
    }

    public String getCpeId() {
        return cpeId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getOui() {
        return oui;
    }

    public String getProductClass() {
        return productClass;
    }

    public String getSerialNubmer() {
        return serialNubmer;
    }


    public void setCpeId(String cpeId) {
        this.cpeId = cpeId;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setOui(String oui) {
        this.oui = oui;
    }

    public void setProductClass(String productClass) {
        this.productClass = productClass;
    }

    public void setSerialNubmer(String serialNubmer) {
        this.serialNubmer = serialNubmer;
    }

    public void setDeviceIdInfor(String manufacturer, String oui, String productClass, String serialNubmer) {
        this.manufacturer = manufacturer;
        if (oui == null || productClass == null || serialNubmer == null) {
            throw new NullPointerException();
        }
        this.oui = oui;
        this.productClass = productClass;
        this.serialNubmer = serialNubmer;
        this.cpeId = oui + productClass + serialNubmer;
    }

    /**
     * 使用前确保所有的字段都初始化
     */
    @Override
    public void addThisToBody(Element body, SoapMessageModel soapMessageModel) {
        // DeviceId,见inform-deviceId的定义
        Element deviceElement = soapMessageModel.createElement(DEVICEID);
        Element manElement = soapMessageModel.createElement(MANUFACTURER);
        manElement.setTextContent(getManufacturer());
        deviceElement.appendChild(manElement);
        Element ouiElement = soapMessageModel.createElement(OUI);
        ouiElement.setTextContent(getOui());
        deviceElement.appendChild(ouiElement);
        Element producElement = soapMessageModel.createElement(PRODUCTCLASS);
        producElement.setTextContent(getProductClass());
        deviceElement.appendChild(producElement);
        Element serialElement = soapMessageModel.createElement(SERIALNUMBER);
        serialElement.setTextContent(getSerialNubmer());
        deviceElement.appendChild(serialElement);
        body.appendChild(deviceElement);
    }

    @Override
    public void parseBodyOfThis(Element body, SoapMessageModel soapMessageModel) {
        Element idElement = getRequestChildElement(soapMessageModel, body, DEVICEID);
        oui = getRequestElement(soapMessageModel, idElement, OUI);
        serialNubmer = getRequestElement(soapMessageModel, idElement, SERIALNUMBER);
        manufacturer = getRequestElement(soapMessageModel, idElement, MANUFACTURER);
        productClass = getRequestElement(soapMessageModel, idElement, PRODUCTCLASS);
        cpeId = getCpeId(oui, productClass, serialNubmer);
    }

    @Override
    public String toString() {
        StringBuilder sbd = new StringBuilder();
        sbd.append("Manufacturer:" + manufacturer);
        sbd.append(" OUI:" + oui);
        sbd.append(" productClass:" + productClass);
        sbd.append(" SerialNubmer:" + serialNubmer);
        return sbd.toString();
    }

    public static String getCpeId(String oui, String productClass, String serialNubmer) {
        return oui + "_" + serialNubmer;
    }
}
