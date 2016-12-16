package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.HardwareAblity;

import java.util.List;

public interface HardwareAblityMapper   extends BaseMapper<HardwareAblity> {
	int deleteByPrimaryKey(String hardwareAblityUuid);

	int insert(HardwareAblity record);

	int insertSelective(HardwareAblity record);

	HardwareAblity selectByPrimaryKey(String hardwareAblityUuid);

	HardwareAblity selectByDeviceUuid(String deviceId);

	HardwareAblity selectByGatewayInfoUuid(String gatewayInfoUuid);

	int updateByPrimaryKeySelective(HardwareAblity record);

	int updateByPrimaryKey(HardwareAblity record);

	List<HardwareAblity> queryList();

	void batchInsertHardwareAblity(List<HardwareAblity> allHardwareAblityDatas);
}