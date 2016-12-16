package com.cmiot.rms.services.outerservice;

import com.alibaba.fastjson.JSONObject;

/**
 * 接口分发
 * Created by wangzhen on 2016/1/29.
 */
public interface InterfaceHandOut {
    // 业务分发
    Object handOut(JSONObject jsonObj);
}
