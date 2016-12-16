package com.cmiot.rms.services;

import java.util.Map;

public interface WirelessNetworkSettingService {
	
	/**
     * 获取设备信息和状态快照
     * @param parameter
     * @return
     */
    public Map<String,Object> getInternetConInformation(Map<String,Object> parameter);

    /**
     * 查询PPP拨号状态
     * @param parameter
     * @return
     */
    public Map<String,Object> getPPPDailUpStatus(Map<String,Object> parameter);
    /**
     * 设置SSID配置 
     * @param parameter
     * @return
     */
    public Map<String,Object> setSSIDConfiguration(Map<String,Object> parameter);
    /**
     * 查询SSID信息  一二级接口
     * @param parameter
     * @return
     */
    public Map<String,Object> getSSIDInfo(Map<String,Object> parameter);
    
    /**
     * 查询SSID信息  提供给BMS
     * @param parameter
     * @return
     */
    public Map<String,Object> getWifiSSIDInfo(Map<String,Object> parameter);

    /**
     * 开启WPS
     * @param parameter
     * @return
     */
    public Map<String,Object> openWPS(Map<String,Object> parameter);
    
    /**
     * 查询WPS当前状态
     * @param parameter
     * @return
     */
    public Map<String,Object> getWPSCurrentStatus(Map<String,Object> parameter);
    
    /**
     * 关闭WPS
     * @param parameter
     * @return
     */
    public Map<String,Object> closeWPS(Map<String,Object> parameter);
    
    /**
     * 开关SSID 
     * @param parameter
     * @return
     */
    public Map<String,Object> setWifiSsidOnoff(Map<String,Object> parameter);
    
    /**
     * 设置Wi-Fi定时开关 
     * @param parameter
     * @return
     */
    public Map<String,Object> setWifiOnoffTimer(Map<String,Object> parameter);
    
    /**
     * 查询Wi-Fi定时开关状态 
     * @param parameter
     * @return
     */
    public Map<String,Object> getWifiOnoffTimer(Map<String,Object> parameter);
    

}
