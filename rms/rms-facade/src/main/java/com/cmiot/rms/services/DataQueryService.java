package com.cmiot.rms.services;

import java.util.Map;

/**
 * 省级数字家庭管理平台与网管系统接口规范
 * 数据查询接口
 * Hgu:家庭网关
 * Stb：机顶盒
 * Created by panmingguo on 2016/9/6.
 */
public interface DataQueryService {

    /**
     * 查询设备信息(Ihgu)
     * @param parameter
     * @return
     */
    Map<String, Object> queryIhguEquipmentInfo(Map<String, Object> parameter);

    /**
     * 查询设备信息(Hgu)
     * @param parameter
     * @return
     */
    Map<String, Object> queryHguEquipmentInfo(Map<String, Object> parameter);

    /**
     * 查询设备信息(Stb)
     * @param parameter
     * @return
     */
    Map<String, Object> queryStbEquipmentInfo(Map<String, Object> parameter);

    /**
     * 查询设备业务状态(Hgu)
     * @param parameter
     * @return
     */
    Map<String, Object> queryHguBusinessStatus(Map<String, Object> parameter);

    /**
     * 查询DNS地址和拨号错误码(Hgu)
     * @param parameter
     * @return
     */
    Map<String, Object> queryHguDnsdhcpInfo(Map<String, Object> parameter);


    /**
     * 告警查询(Hgu)
     * @param parameter
     * @return
     */
    Map<String, Object> queryHguAlarminfo(Map<String, Object> parameter);

    /**
     * 查询LAN口性能(Hgu)
     * @param parameter
     * @return
     */
    Map<String, Object> queryHguLanPerformanceInfo(Map<String, Object> parameter);


}
