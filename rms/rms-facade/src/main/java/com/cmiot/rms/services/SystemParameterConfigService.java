package com.cmiot.rms.services;

import java.util.Map;

/**
 * 系统参数配置服务
 * Created by panmingguo on 2016/10/25.
 */
public interface SystemParameterConfigService {

    /**
     *  更新参数
     * @param parameter
     * @return
     */
    Map<String, Object> updateParameter(Map<String, Object> parameter);


    /**
     *  查询参数
     * @param parameter
     * @return
     */
    Map<String, Object> searchParameter(Map<String, Object> parameter);
}
