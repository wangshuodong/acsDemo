package com.cmiot.rms.dao.model;

import java.io.Serializable;

/**
 * wifi连接信息
 * Created by wangzhen on 2016/4/12.
 */
public class LANDeviceHostInfo implements Serializable{

    /**
     *主机名
     */
    private String hostName;
    /**
     * IP地址
     */
    private String iPAddress;
    /**
     * MAC地址
     */
    private String MACAddress;

    /**
     * 上网方式：Ethernet:有线 802.11:无线  Other:其他
     */
    private String interfaceType;

    private boolean active;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getiPAddress() {
        return iPAddress;
    }

    public void setiPAddress(String iPAddress) {
        this.iPAddress = iPAddress;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public void setMACAddress(String MACAddress) {
        this.MACAddress = MACAddress;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj instanceof LANDeviceHostInfo){
            LANDeviceHostInfo info =(LANDeviceHostInfo) obj;
            return MACAddress.equals(info.getMACAddress());
        }else{
            return super.equals(obj);
        }
    }
}
