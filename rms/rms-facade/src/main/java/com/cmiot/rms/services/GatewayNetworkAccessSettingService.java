package com.cmiot.rms.services;

import java.util.Map;

/**
 * 设置网关内网相关配置（供BMS调用）
 * @author chuan
 *
 */
public interface GatewayNetworkAccessSettingService {
	/**
     * 设置网关下挂的终端接入控制
     * @param parameter
     * @return
     */
    public Map<String,Object> setLanAccess(Map<String,Object> parameter);
}
