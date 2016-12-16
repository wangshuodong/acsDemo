package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.FirmwareUpgradeTaskTime;

public interface BoxFirmwareUpgradeTaskTimeMapper {
	
	int deleteByPrimaryKey(String id);

	int insert(FirmwareUpgradeTaskTime record);

	int insertSelective(FirmwareUpgradeTaskTime record);

	FirmwareUpgradeTaskTime selectByPrimaryKey(String id);

	int updateByPrimaryKeySelective(FirmwareUpgradeTaskTime record);

	int updateByPrimaryKey(FirmwareUpgradeTaskTime record);

	int searchCount(String upgradeTaskId);
}
