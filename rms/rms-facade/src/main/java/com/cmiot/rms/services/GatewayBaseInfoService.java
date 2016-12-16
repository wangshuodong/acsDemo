package com.cmiot.rms.services;


import java.util.Map;

/**
 * 网关基本信息查询
 *
 * Created by panmingguo on 2016/5/4.
 */
public interface GatewayBaseInfoService {

    /**
     * 查询网关资源占用率
     * @param params
     * @return
     */
    Map<String, Object> getHgResourceUsage(Map<String, Object> params);

    /**
     * 查询系统信息
     * @param params
     * @return
     */
    Map<String, Object> getHgSystemInfo(Map<String, Object> params);

    /**
     * 获取持续运行时间
     * @param params
     * @return
     */
    Map<String, Object> getHgTimeDuration(Map<String, Object> params);

    /**
     * 获取设备信息和状态快照
     * @param parameter
     * @return
     */
    public Map<String,Object> getDeviceInfoAndStatus(Map<String,Object> parameter);
    
    /**
     * BOSS系统调用接口， 传入网关PASSWORD,AREAID, 然后根据PASSWORD更新对应AREAID
     * @param params
     * @return 
     * 
     * */
    public Map<String,Object> syncGatewayBaseInfo(Map<String, Object> params);
}
