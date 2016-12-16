package com.cmiot.rms.services;

import com.cmiot.acs.model.Inform;

/**
 * Created by weilei on 2016/5/12.
 */
public interface MonitorService {

    /**
     *周期采样监控上报
     * @param inform
     */
    void reportMonitor(Inform inform);
}
