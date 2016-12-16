package com.cmiot.rms.dao.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by admin on 2016/5/13.
 */
public class GateWaySpeedReportBean implements Serializable{

    private String gateWayMac;//网关MAC

    private Long time;//上报消息的时间戳

    private String hgSpeed;//网关速率

    private Long getHGByteTime;//获取网关速率的时间

    public String getGateWayMac() {
        return gateWayMac;
    }

    public void setGateWayMac(String gateWayMac) {
        this.gateWayMac = gateWayMac;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getHgSpeed() {
        return hgSpeed;
    }

    public void setHgSpeed(String hgSpeed) {
        this.hgSpeed = hgSpeed;
    }

    public String getUpBytes() {
        return upBytes;
    }

    public void setUpBytes(String upBytes) {
        this.upBytes = upBytes;
    }

    public String getDownBytes() {
        return downBytes;
    }

    public void setDownBytes(String downBytes) {
        this.downBytes = downBytes;
    }

    public List<SubDeviceSpeedReportBean> getSubDeviceSpeedReportBeanList() {
        return subDeviceSpeedReportBeanList;
    }

    public void setSubDeviceSpeedReportBeanList(List<SubDeviceSpeedReportBean> subDeviceSpeedReportBeanList) {
        this.subDeviceSpeedReportBeanList = subDeviceSpeedReportBeanList;
    }

    public Long getGetHGByteTime() {
        return getHGByteTime;
    }

    public void setGetHGByteTime(Long getHGByteTime) {
        this.getHGByteTime = getHGByteTime;
    }

    private String upBytes;//网关上行总字节数

    private String downBytes;//网关下行总字节数

    private List<SubDeviceSpeedReportBean> subDeviceSpeedReportBeanList;//上报的下挂终端集合

    private List<SubDeviceSpeedReportBean> lanDeviceList;//下挂终端集合

    public List<SubDeviceSpeedReportBean> getLanDeviceList() {
        return lanDeviceList;
    }

    public void setLanDeviceList(List<SubDeviceSpeedReportBean> lanDeviceList) {
        this.lanDeviceList = lanDeviceList;
    }
}
