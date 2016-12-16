package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.FirmwareInfo;

import java.util.List;
import java.util.Map;

public interface FirmwareInfoMapper   extends BaseMapper<FirmwareInfo> {
    int deleteByPrimaryKey(String id);

    int insert(FirmwareInfo record);
    
    List<FirmwareInfo> queryList(FirmwareInfo firmwareInfo);

    /**
     * 功能:根据设备uuid查询需要升级的固件
     * @param deviceId
     * @return
     */
    List<FirmwareInfo> selectByDeviceId(String deviceId);
   
    int insertSelective(FirmwareInfo record);

    FirmwareInfo selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FirmwareInfo record);

    int updateByPrimaryKey(FirmwareInfo record);

    FirmwareInfo selectSelective(FirmwareInfo record);

    int selectCount(Map<String, String> para);

    List<FirmwareInfo> selectPreparedFirmware(String id);

    /**
     * 通过生产商编码，设备型号，固件版本查询固件ID
     * @param para
     * @return
     */
    Map<String, Object> queryFirmwareIdList(Map<String, Object> para);

    /**
     * 通过生产商编码，设备型号，固件版本查询固件ID
     * @param
     * @return
     */
    List<Map<String, Object>> queryFirmwareIdListForImport();

    /**
     * 根据固件ID查询是否存在网关使用该固件
     * @param id
     * @return
     */
    int searchCountByFirmwareId(String id);
    
    /**
     * 根据固件ID更新审核状态为已审核
     * @param id
     * @return
     */
    int updateFirmwareCheckStatus(String id);
}