package com.cmiot.rms.services;

import java.util.Map;

/**
 * 网速和流量统计
 * Created by panmingguo on 2016/5/6.
 */
public interface GatewaySpeedTrafficInfoService {
    /**
     * 获取网关WAN侧和LAN侧流量统计
     * @param parameter
     * @return
     */
    Map<String, Object> getHgPortsTrafficStatus(Map<String, Object> parameter);

    /**
     * 设置下挂设备的上下行最大带宽限制
     * @param parameter
     * @return
     */
    Map<String, Object> setLanDeviceBandwidth(Map<String, Object> parameter);

    /**
     * 获取下挂的设备上下行最大带宽限制
     * @param parameter
     * @return
     */
    Map<String, Object> getLanDeviceBandth(Map<String, Object> parameter);

    /**
     * 设置下挂设备实时流量统计开关
     * @param parameter
     * @return
     */
    Map<String, Object> setLanDeviceSpeedTest(Map<String, Object> parameter);

    /**
     * 查询下挂的设备实时流量统计
     * @param parameter
     * @return
     */
    Map<String, Object> getLanDeviceTrafficStatus(Map<String, Object> parameter);

    /**
     * 配置下挂设备实时速率上报策略
     * @param parameter
     * @return
     */
    Map<String, Object> setLanSpeedReportPolicy(Map<String, Object> parameter);

    /**
     * 获取网关下挂设备的实时速率的上报策略
     * @param parameter
     * @return
     */
    Map<String, Object> getLanSpeedReportPolicy(Map<String, Object> parameter);
}
