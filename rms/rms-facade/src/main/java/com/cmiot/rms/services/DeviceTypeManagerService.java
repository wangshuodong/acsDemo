package com.cmiot.rms.services;

import java.util.Map;

/**
 * 设备类型管理接口
 * Created by wangzhen on 2016/4/15.
 */
public interface DeviceTypeManagerService {

    /**
     * 查询设备类型列表
     * @param parameter
     * @return
     */
    public Map<String,Object> queryDeviceTypeInfo(Map<String,Object> parameter);

    /**
     * 设备类型添加
     * @param parameter
     * @return
     */
    public Map<String,Object> addDeviceTypeInfo(Map<String,Object> parameter);

    /**
     * 设备类型修改初始化
     * @param parameter
     * @return
     */
    Map<String, Object> initUpdateDeviceTypeInfo (Map<String, Object> parameter);

    /**
     * 设备类型修改
     * @param parameter
     * @return
     */
    Map<String, Object> updateDeviceTypeInfo (Map<String, Object> parameter);

    /**
     * 设备类型查看
     * @param parameter
     * @return
     */
    Map<String, Object> detailDeviceTypeInfo (Map<String, Object> parameter);

    /**
     * 设备删除
     * @param parameter
     * @return
     */
    Map<String, Object> deleteDeviceTypeInfo(Map<String, Object> parameter);
}
