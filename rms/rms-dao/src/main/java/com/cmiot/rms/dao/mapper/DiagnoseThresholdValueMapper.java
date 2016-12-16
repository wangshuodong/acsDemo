package com.cmiot.rms.dao.mapper;

import java.util.List;

import com.cmiot.rms.dao.model.DiagnoseThresholdValue;

public interface DiagnoseThresholdValueMapper {

	int insert(DiagnoseThresholdValue record);

	int insertSelective(DiagnoseThresholdValue record);

	int insertBatch(List<DiagnoseThresholdValue> dtvList);

	List<DiagnoseThresholdValue> selectDiagnoseThresholdValue(DiagnoseThresholdValue record);

	int updateDiagnoseThresholdValue(DiagnoseThresholdValue record);

	int updateBatch(List<DiagnoseThresholdValue> dtvList);
}