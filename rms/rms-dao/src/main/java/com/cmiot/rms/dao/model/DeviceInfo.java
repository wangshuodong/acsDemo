package com.cmiot.rms.dao.model;

public class DeviceInfo extends BaseBean {

    private String id;

    private String deviceType;

    private String deviceModel;

    private String deviceName;

    private String deviceFactory;

    private String deviceMemo;
    
    private String deviceManufacturer;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType == null ? null : deviceType.trim();
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel == null ? null : deviceModel.trim();
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName == null ? null : deviceName.trim();
    }

    public String getDeviceFactory() {
        return deviceFactory;
    }

    public void setDeviceFactory(String deviceFactory) {
        this.deviceFactory = deviceFactory == null ? null : deviceFactory.trim();
    }

    public String getDeviceMemo() {
        return deviceMemo;
    }

    public void setDeviceMemo(String deviceMemo) {
        this.deviceMemo = deviceMemo == null ? null : deviceMemo.trim();
    }

	public String getDeviceManufacturer() {
		return deviceManufacturer;
	}

	public void setDeviceManufacturer(String deviceManufacturer) {
		this.deviceManufacturer = deviceManufacturer;
	}


}