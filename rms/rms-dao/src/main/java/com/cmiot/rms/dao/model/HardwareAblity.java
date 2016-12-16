package com.cmiot.rms.dao.model;

public class HardwareAblity extends BaseBean {
    private String hardwareAblityUuid;

    private Integer hardwareAblityLanCount;

    private Integer hardwareAblityUsbCount;

    private Boolean hardwareAblitySupportWifi;

    private String hardwareAblityWifiLoc;

    private Integer hardwareAblityWifiCount;

    private String hardwareAblityWifiSize;

    private String hardwareAblitySupportWifi24ghz;

    private String hardwareAblitySupportWifi58ghz;

    private String hardwareAblityIpv4v6;

    private String deviceId;

    private String gatewayForm;


    private String gatewayOSType;

    private String gatewayOSVersion;

    public String getHardwareAblityUuid() {
        return hardwareAblityUuid;
    }

    public void setHardwareAblityUuid(String hardwareAblityUuid) {
        this.hardwareAblityUuid = hardwareAblityUuid == null ? null : hardwareAblityUuid.trim();
    }

    public Integer getHardwareAblityLanCount() {
        return hardwareAblityLanCount;
    }

    public void setHardwareAblityLanCount(Integer hardwareAblityLanCount) {
        this.hardwareAblityLanCount = hardwareAblityLanCount;
    }

    public Integer getHardwareAblityUsbCount() {
        return hardwareAblityUsbCount;
    }

    public void setHardwareAblityUsbCount(Integer hardwareAblityUsbCount) {
        this.hardwareAblityUsbCount = hardwareAblityUsbCount;
    }

    public Boolean getHardwareAblitySupportWifi() {
        return hardwareAblitySupportWifi;
    }

    public void setHardwareAblitySupportWifi(Boolean hardwareAblitySupportWifi) {
        this.hardwareAblitySupportWifi = hardwareAblitySupportWifi;
    }

    public String getHardwareAblityWifiLoc() {
        return hardwareAblityWifiLoc;
    }

    public void setHardwareAblityWifiLoc(String hardwareAblityWifiLoc) {
        this.hardwareAblityWifiLoc = hardwareAblityWifiLoc == null ? null : hardwareAblityWifiLoc.trim();
    }

    public Integer getHardwareAblityWifiCount() {
        return hardwareAblityWifiCount;
    }

    public void setHardwareAblityWifiCount(Integer hardwareAblityWifiCount) {
        this.hardwareAblityWifiCount = hardwareAblityWifiCount;
    }

    public String getHardwareAblityWifiSize() {
        return hardwareAblityWifiSize;
    }

    public void setHardwareAblityWifiSize(String hardwareAblityWifiSize) {
        this.hardwareAblityWifiSize = hardwareAblityWifiSize == null ? null : hardwareAblityWifiSize.trim();
    }

    public String getHardwareAblitySupportWifi24ghz() {
        return hardwareAblitySupportWifi24ghz;
    }

    public void setHardwareAblitySupportWifi24ghz(String hardwareAblitySupportWifi24ghz) {
        this.hardwareAblitySupportWifi24ghz = hardwareAblitySupportWifi24ghz == null ? null : hardwareAblitySupportWifi24ghz.trim();
    }

    public String getHardwareAblitySupportWifi58ghz() {
        return hardwareAblitySupportWifi58ghz;
    }

    public void setHardwareAblitySupportWifi58ghz(String hardwareAblitySupportWifi58ghz) {
        this.hardwareAblitySupportWifi58ghz = hardwareAblitySupportWifi58ghz == null ? null : hardwareAblitySupportWifi58ghz.trim();
    }

    public String getHardwareAblityIpv4v6() {
        return hardwareAblityIpv4v6;
    }

    public void setHardwareAblityIpv4v6(String hardwareAblityIpv4v6) {
        this.hardwareAblityIpv4v6 = hardwareAblityIpv4v6 == null ? null : hardwareAblityIpv4v6.trim();
    }

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getGatewayForm() {
		return gatewayForm;
	}

	public void setGatewayForm(String gatewayForm) {
		this.gatewayForm = gatewayForm;
	}

	public String getGatewayOSType() {
		return gatewayOSType;
	}

	public void setGatewayOSType(String gatewayOSType) {
		this.gatewayOSType = gatewayOSType;
	}

	public String getGatewayOSVersion() {
		return gatewayOSVersion;
	}

	public void setGatewayOSVersion(String gatewayOSVersion) {
		this.gatewayOSVersion = gatewayOSVersion;
	}

}