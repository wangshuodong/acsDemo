package com.cmiot.rms.services;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by panmingguo on 2016/4/12.
 */
public interface FirmwareInfoService {

    /**
     * 根据生产商、设备型号、固件版本查询固件信息
     * @param parameter
     * @return
     */

    Map<String, Object> searchFirmwareInfo(Map<String, Object> parameter);

    /**
     * 根据设备型号查询固件版本号
     * @param parameter
     * @return
     */
    Map<String, Object> searchFirmwareVersion(Map<String, Object> parameter);


    /**
     * 根据生产商、设备型号、固件版本号查询固件ID
     * @param parameter
     * @return
     */
    Map<String, Object> searchFirmwareId(Map<String, Object> parameter);

    /**
     * 添加固件
     * @param parameter
     * @return
     */
    Map<String, Object> addFirmwareInfo(Map<String, Object> parameter);


    /**
     * 修改固件
     * @param parameter
     * @return
     */
    Map<String, Object> editFirmwareInfo(Map<String, Object> parameter);


    /**
     *  更新固件
     * @param parameter
     * @return
     */
    Map<String, Object> updateFirmwareInfo(Map<String, Object> parameter);


    /**
     * 删除固件
     * @param parameter
     * @return
     */
    Map<String, Object> deleteFirmwareInfo(Map<String, Object> parameter);
    
    /**
     * 固件审核接
     * @param parameter
     * @return
     */
    Map<String, Object>  updateFirmwareCheckStatus(Map<String, Object> parameter);

}
