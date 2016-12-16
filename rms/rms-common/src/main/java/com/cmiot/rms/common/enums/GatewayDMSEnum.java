package com.cmiot.rms.common.enums;

/**
 * InternetGatewayDevice.ManagementServer. 枚举
 * Created by wangzhen on 2016/2/17.
 */
public enum GatewayDMSEnum {

    GATEWAYDMS_HARDWAREVERSION("InternetGatewayDevice.DeviceInfo.HardwareVersion","硬件版本"),
    GATEWAYDMS_SOFTWAREVERSION("InternetGatewayDevice.DeviceInfo.SoftwareVersion","软件版本"),
    GATEWAYDMS_EXTERNALIPADDRESS("InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANIPConnection.{i}.ExternalIPAddress","设备IP地址"),
    GATEWAYDMS_CONNECTIONREQUESTURL("InternetGatewayDevice.ManagementServer.ConnectionRequestURL", "RMS 向智能网关发起连接请求通知时所使用的HTTP URL"),
    GATEWAYDMS_MANAGEMENTSERVER_PARAMETERKEY("InternetGatewayDevice.ManagementServer.ParameterKey","requestID"),
    GATEWAYDMS_SERIALNUMBER("InternetGatewayDevice.DeviceInfo.SerialNumber","设备序列号")
    ;

    /* 编码 */
    private String code;

    /* 描述 */
    private String description;

    GatewayDMSEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // 普通方法
    public static String getDescription(String code) {
        for (GatewayDMSEnum ce : GatewayDMSEnum.values()) {
            if (ce.getCode() == code) {
                return ce.getDescription();
            }
        }
        return null;
    }
}
