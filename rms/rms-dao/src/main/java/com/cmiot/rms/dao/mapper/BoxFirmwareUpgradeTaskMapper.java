package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.BoxFirmwareUpgradeTask;
import org.apache.ibatis.annotations.Param;

public interface BoxFirmwareUpgradeTaskMapper {

	int deleteByPrimaryKey(String id);

	int insert(BoxFirmwareUpgradeTask record);

	int insertSelective(BoxFirmwareUpgradeTask record);

	BoxFirmwareUpgradeTask selectByPrimaryKey(String id);

	int updateByPrimaryKeySelective(BoxFirmwareUpgradeTask record);

	List<Map<String, String>> selectAllOrByTaskName(Map<String, Object> map);

	int countSelectAllOrByTaskName(Map<String, Object> map);

	List<Map<String, String>> selectBoxFirmwareInfo(Map<String, Object> map);

	int countSelectBoxFirmwareInfo(Map<String, Object> map);

	List<Map<String, String>> selectBoxFirmwareInfoByBoxIds(Map<String, Object> map);

	int countSelectBoxFirmwareInfoByBoxIds(Map<String, Object> map);

	List<Map<String, Object>> queryUpgradeTaskByMacs(List<String> macList);

	List<Map<String, String>> queryUpgradeSpecifiedVersionInfo(Map<String, Object> map);

	int updateUpgradeTaskStatus(Map<String, Object> map);
	
    int updateBoxFirmwareUuidByPrimaryKey(Map<String, Object> map);

	List<BoxFirmwareUpgradeTask> queryListByStatus(@Param("status") int status);
}