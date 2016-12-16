package com.cmiot.rms.services;

import java.util.Map;

/**
 * Created by admin on 2016/8/10.
 */
public interface HdVersionService {

    /**
     * 根据设备型号查询硬件版本号
     * @param parameter
     * @return
     */
    Map<String, Object> searchHdVersion(Map<String, Object> parameter);

    /**
     * 新增网关硬件版本
     * @param parameter
     * @return
     */
    Map<String, Object> addHardVersion(Map<String, Object> parameter);

    /**
     * 修改硬件版本
     * @param parameter
     * @return
     */
    Map<String, Object> updateHardVersion(Map<String, Object> parameter);

    /**
     * 删除硬件版本
     * @param parameter
     * @return
     */
    Map<String, Object> deleteHardVersion(Map<String, Object> parameter);

    /**
     * 分页查询硬件版本
     * @param parameter
     * @return
     */
    Map<String, Object> queryHardVersion4Page(Map<String, Object> parameter);
}
