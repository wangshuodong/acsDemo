package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.MakeInfo;

public interface MakeInfoMapper {
	int deleteByPrimaryKey(String id);

	int insert(MakeInfo record);

	int insertSelective(MakeInfo record);

	MakeInfo selectByPrimaryKey(String id);

	int updateByPrimaryKeySelective(MakeInfo record);

	int updateByPrimaryKey(MakeInfo record);

	List<MakeInfo> querySelective(MakeInfo record);

	int countQuerySelective(MakeInfo record);

	List<MakeInfo> selectByPage(Map<String, Object> map);

	int countSelectByPage(Map<String, Object> map);

	MakeInfo selectByFactoryCode(String code);

	List<MakeInfo> queryMakeInfoByMakeName(String makeName);
}