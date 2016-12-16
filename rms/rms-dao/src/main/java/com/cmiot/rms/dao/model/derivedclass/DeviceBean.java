package com.cmiot.rms.dao.model.derivedclass;

import com.cmiot.rms.dao.model.DeviceInfo;
import com.cmiot.rms.dao.model.Factory;
import com.cmiot.rms.dao.model.HardwareAblity;
import com.cmiot.rms.dao.model.Manufacturer;

import java.util.List;

/**
 * Created by wangzhen on 2016/4/19.
 */
public class DeviceBean extends DeviceInfo {
    /**
     * 制造商
     */
    private List<Manufacturer> manufacturerList;

    /**
     * 生产商
     */
    private List<Factory> factoryList;

    private String deviceManufacturerName;

    private String deviceFactoryName;

    private HardwareAblity hardwareAblity;


    public List<Manufacturer> getManufacturerList() {
        return manufacturerList;
    }

    public void setManufacturerList(List<Manufacturer> manufacturerList) {
        this.manufacturerList = manufacturerList;
    }

    public List<Factory> getFactoryList() {
        return factoryList;
    }

    public void setFactoryList(List<Factory> factoryList) {
        this.factoryList = factoryList;
    }

    public String getDeviceFactoryName() {
        return deviceFactoryName;
    }

    public void setDeviceFactoryName(String deviceFactoryName) {
        this.deviceFactoryName = deviceFactoryName;
    }

    public String getDeviceManufacturerName() {
        return deviceManufacturerName;
    }

    public void setDeviceManufacturerName(String deviceManufacturerName) {
        this.deviceManufacturerName = deviceManufacturerName;
    }

	public HardwareAblity getHardwareAblity() {
		return hardwareAblity;
	}

	public void setHardwareAblity(HardwareAblity hardwareAblity) {
		this.hardwareAblity = hardwareAblity;
	}

}
