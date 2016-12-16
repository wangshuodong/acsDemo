package com.cmiot.rms.services;

import java.util.Map;

/**
 * 家庭内网配置管理
 * Created by panmingguo on 2016/5/6.
 */
public interface HomeNetworkConfigService {
    /**
     * 获取网关下挂终端的网络访问控制名单
     * @param parameter
     * @return
     */
    Map<String, Object> getLanAccessNet(Map<String, Object> parameter);

    /**
     * 获取网关下挂终端的存储访问控制名单
     * @param parameter
     * @return
     */
    Map<String, Object> getLanAccessStorage(Map<String, Object> parameter);

    /**
     * 配置网关下挂终端上线消息上报策略
     * @param parameter
     * @return
     */
    Map<String, Object> setLanDeviceOnline(Map<String, Object> parameter);


    /**
     * 网关下挂终端上线消息上报
     * @param parameter
     * @return
     */
    Map<String, Object> reportLanDeviceOnline(Map<String, Object> parameter);

    /**
     * 获取网关上接入设备的消息上报策略
     * @param parameter
     * @return
     */
    Map<String, Object> getLanDeviceOnline(Map<String, Object> parameter);

}
