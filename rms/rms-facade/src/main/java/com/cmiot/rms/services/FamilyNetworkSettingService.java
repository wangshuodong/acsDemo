package com.cmiot.rms.services;

import java.util.Map;

/**
 * 功能:家庭内网配置管理接口
 */
public interface FamilyNetworkSettingService {

    /**
     * 设置网关设备名称
     * @param parameter
     * @return
     */
    public Map<String,Object> setHgName(Map<String,Object> parameter);
    
    /**
     * 设置下挂的设备别名
     * @param parameter
     * @return
     */
    public Map<String,Object> setLanDeviceName(Map<String,Object> parameter);
    
    /**
     * 获取网关和下挂的设备名称
     * @param parameter
     * @return
     */
    public Map<String,Object> getHgNamelist(Map<String,Object> parameter);
    
    /**
     * 获取家庭内网拓扑信息
     * @param parameter
     * @return
     */
    public Map<String,Object> getLanNetInfo(Map<String,Object> parameter);
    
    /**
     * 设置网关下挂的终端接入控制
     * @param parameter
     * @return
     */
    public Map<String,Object> setLanAccess(Map<String,Object> parameter);
}
