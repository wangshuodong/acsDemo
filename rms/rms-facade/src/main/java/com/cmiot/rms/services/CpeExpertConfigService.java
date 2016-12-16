package com.cmiot.rms.services;

import java.util.Map;

/**
 * 网关高级配置管理
 * Created by zjial on 2016/5/6.
 */
public interface CpeExpertConfigService {
    /**
     * 网关重启
     *
     * @param parameter
     * @return
     */
    Map<String, Object> setHgReboot(Map<String, Object> parameter);

    /**
     *恢复客户端所作设置（恢复出厂设置）
     *
     * @param parameter
     * @return
     */
    Map<String, Object> setHgRecover(Map<String, Object> parameter);

    /**
     *应用服务协议管理
     *
     * @param parameter
     * @return
     */
    Map<String, Object> setHgServiceManage(Map<String, Object> parameter);

    /**
     * 应用服务协议账号管理
     *
     * @param parameter
     * @return
     */
    Map<String, Object> setHgServiceAccount(Map<String, Object> parameter);

    /**
     * 应用服务协议信息查询
     *
     * @param parameter
     * @return
     */
    Map<String, Object> getHgServiceInfo(Map<String, Object> parameter);


}
