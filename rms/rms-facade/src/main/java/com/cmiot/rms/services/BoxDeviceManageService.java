package com.cmiot.rms.services;

import java.util.Map;

/**
 * Created by zj on 2016/8/9.
 */
public interface BoxDeviceManageService {
    /**
     * 新增机顶盒设备类型
     * @param parameter
     * @return
     */
    public Map<String,Object> addDeviceInfo(Map<String,Object> parameter);
    /**
     * 查询机顶盒设备类型
     * @param parameter
     * @return
     */
    public Map<String,Object> queryDeviceInfoList(Map<String,Object> parameter);
    /**
     * 查询单个机顶盒设备类型
     * @param parameter
     * @return
     */
    public Map<String,Object> queryDeviceInfo(Map<String,Object> parameter);
    /**
     * 编辑机顶盒设备类型
     * @param parameter
     * @return
     */
    public Map<String,Object> updateDeviceInfo(Map<String,Object> parameter);
    /**
     * 删除机顶盒设备类型
     * @param parameter
     * @return
     */
    public Map<String,Object> deleteDeviceInfo(Map<String,Object> parameter);


}
