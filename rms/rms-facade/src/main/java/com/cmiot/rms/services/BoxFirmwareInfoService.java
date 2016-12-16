package com.cmiot.rms.services;

import java.util.Map;

/**
 * 机顶盒固件管理服务类
 * Created by panmingguo on 2016/6/13.
 */
public interface BoxFirmwareInfoService {

    /**
     * 根据生产商、设备型号、固件版本查询机顶盒固件信息
     * @param parameter
     * @return
     */

    Map<String, Object> searchBoxFirmwareInfo(Map<String, Object> parameter);

    /**
     * 根据设备型号查询机顶盒固件版本号
     * @param parameter
     * @return
     */
    Map<String, Object> searchBoxFirmwareVersion(Map<String, Object> parameter);


    /**
     * 根据生产商、设备型号、固件版本号查询机顶盒固件ID
     * @param parameter
     * @return
     */
    Map<String, Object> searchBoxFirmwareId(Map<String, Object> parameter);

    /**
     * 添加机顶盒固件
     * @param parameter
     * @return
     */
    Map<String, Object> addBoxFirmwareInfo(Map<String, Object> parameter);


    /**
     * 修改机顶盒固件
     * @param parameter
     * @return
     */
    Map<String, Object> editBoxFirmwareInfo(Map<String, Object> parameter);


    /**
     *  更新机顶盒固件
     * @param parameter
     * @return
     */
    Map<String, Object> updateBoxFirmwareInfo(Map<String, Object> parameter);


    /**
     * 删除机顶盒固件
     * @param parameter
     * @return
     */
    Map<String, Object> deleteBoxFirmwareInfo(Map<String, Object> parameter);


    /**
     * 根据机顶盒的当前版本查询待升级版本
     * @param parameter
     * @return
     */
    Map<String, Object> queryBoxPreparedVersion(Map<String, Object> parameter);
}
