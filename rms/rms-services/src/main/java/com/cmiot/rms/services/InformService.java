package com.cmiot.rms.services;

import com.cmiot.rms.dao.model.InformInfo;

/**
 * 上报信息
 * Created by wangzhen on 2016/2/2.
 */
public interface InformService {
    // 新增上报信息
    void addInformInfo(InformInfo informInfo);
}
