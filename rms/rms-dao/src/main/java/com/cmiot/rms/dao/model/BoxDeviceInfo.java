package com.cmiot.rms.dao.model;

public class BoxDeviceInfo {
    private String id;

    private String factoryCode;

    private String boxModel;
    
    private String deviceName;
    
    private String remark;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFactoryCode() {
        return factoryCode;
    }

    public void setFactoryCode(String factoryCode) {
        this.factoryCode = factoryCode;
    }

    public String getBoxModel() {
        return boxModel;
    }

    public void setBoxModel(String boxModel) {
        this.boxModel = boxModel;
    }

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
    
}