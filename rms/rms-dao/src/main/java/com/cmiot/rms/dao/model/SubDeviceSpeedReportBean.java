package com.cmiot.rms.dao.model;

import java.io.Serializable;

/**
 * Created by weilei on 2016/5/13.
 */
public class SubDeviceSpeedReportBean implements Serializable {

    private String subDeviceMac;//下挂终端MAC

    private String subDeviceMacName;//下挂终端MAC节点全路径

    private String subDeviceUpBytes;//上行字节数

    private String subDeviceDownBytes;//下行字节数

    public String getSubDeviceMac() {
        return subDeviceMac;
    }

    public void setSubDeviceMac(String subDeviceMac) {
        this.subDeviceMac = subDeviceMac;
    }

    public String getSubDeviceMacName() {
        return subDeviceMacName;
    }

    public void setSubDeviceMacName(String subDeviceMacName) {
        this.subDeviceMacName = subDeviceMacName;
    }

    public String getSubDeviceUpBytes() {
        return subDeviceUpBytes;
    }

    public void setSubDeviceUpBytes(String subDeviceUpBytes) {
        this.subDeviceUpBytes = subDeviceUpBytes;
    }

    public String getSubDeviceDownBytes() {
        return subDeviceDownBytes;
    }

    public void setSubDeviceDownBytes(String subDeviceDownBytes) {
        this.subDeviceDownBytes = subDeviceDownBytes;
    }
}
