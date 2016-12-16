package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.FirmwareUpgradeTask;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FirmwareUpgradeTaskMapper {
    int deleteByPrimaryKey(String id);

    int insert(FirmwareUpgradeTask record);

    int insertSelective(FirmwareUpgradeTask record);

    FirmwareUpgradeTask selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FirmwareUpgradeTask record);

    int updateByPrimaryKey(FirmwareUpgradeTask record);
    
    
    List<FirmwareUpgradeTask> queryList(FirmwareUpgradeTask record);
    
    List<FirmwareUpgradeTask> queryListByStatus(@Param("status") int status);

    List<FirmwareUpgradeTask> queryList4Page(FirmwareUpgradeTask record);

    int selectTaskCount(@Param("firmwareId") String firmwareId);

	List<Map<String, Object>> queryUpgradeTaskByMacs(List<String> macList);

    int updateStatus(Map<String, Object> para);

    int batchUpdateStatus(Map<String, Object> para);
    
}