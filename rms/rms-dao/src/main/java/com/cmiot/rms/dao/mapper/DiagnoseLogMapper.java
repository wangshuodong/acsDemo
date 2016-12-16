package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.DeviceInfo;
import com.cmiot.rms.dao.model.DiagnoseLog;

public interface DiagnoseLogMapper extends BaseMapper<DeviceInfo> {

	int deleteByPrimaryKey(Integer id);

	int insert(DiagnoseLog record);

	int insertSelective(DiagnoseLog record);

	DiagnoseLog selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(DiagnoseLog record);

	int updateByPrimaryKey(DiagnoseLog record);
}