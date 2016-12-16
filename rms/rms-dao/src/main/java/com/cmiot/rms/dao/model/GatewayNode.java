package com.cmiot.rms.dao.model;

public class GatewayNode {
    private String id;

    private String factoryCode;

    private String hdVersion;

    private String firmwareVersion;

    private String loginPasswordNode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getFactoryCode() {
        return factoryCode;
    }

    public void setFactoryCode(String factoryCode) {
        this.factoryCode = factoryCode == null ? null : factoryCode.trim();
    }

    public String getHdVersion() {
        return hdVersion;
    }

    public void setHdVersion(String hdVersion) {
        this.hdVersion = hdVersion == null ? null : hdVersion.trim();
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion == null ? null : firmwareVersion.trim();
    }

    public String getLoginPasswordNode() {
        return loginPasswordNode;
    }

    public void setLoginPasswordNode(String loginPasswordNode) {
        this.loginPasswordNode = loginPasswordNode == null ? null : loginPasswordNode.trim();
    }
}