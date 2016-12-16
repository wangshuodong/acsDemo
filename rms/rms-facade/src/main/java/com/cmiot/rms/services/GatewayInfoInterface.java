package com.cmiot.rms.services;

import java.util.Map;

/**
 * 网关数据查询接口
 * 
 * Created by fuwanhong on 2016/1/25.
 */
public interface GatewayInfoInterface {
  
    /**
     * 功能:dubbo服务接口、网关分页查询功能
     * @param map 请求参数
     * @return
     */
    public Map<String,Object> queryDubboList4Page(Map<String,Object> map) ;
    
    /**
     * 功能:dubbo服务接口、获取网关属性
     * @param parameter
     * @return
     */
    public Map<String,Object> getParameterNames(Map<String,Object> parameter);
    
    /**
     * 功能:dubbo服务接口、获取网关属性参数
     * @param parameter
     * @return
     */
    public Map<String,Object> getParameterValues(Map<String, Object> parameter);
    
    /**
     * 功能:dubbo服务接口、设置网关属性参数
     * @param parameter
     * @return
     */
    public Map<String,Object> setParameterValues(Map<String, Object> map);
    
    /**
     * 功能:dubbo服务接口、批量配置获取网关属性
     * @param parameter
     * @return
     */
    public Map<String,Object> getBatchParameterNames(Map<String,Object> parameter);
    
    /**
     * 功能:dubbo服务接口、批量配置设置网关属性参数
     * @param parameter
     * @return
     */
    public Map<String,Object> setBatchParameterValues(Map<String, Object> map);
    /**
     * 获取网关物理接口连接状态
     * @return PonStatus,LAN1Status,LAN2Status,LAN3Status, LAN4Status, WIFIStatus 
     * */
    public Map<String,Object> getGatewayPhysicalInterfaceState(Map<String, Object> map);
    /**
     * 获取VoIP状态
     * */
    public Map<String, Object> getVoIPStatus(Map<String, Object> map);

    
}
