package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.cmiot.rms.dao.model.Factory;

public interface FactoryMapper {
	int deleteByPrimaryKey(String id);

	int insert(Factory record);

	int insertSelective(Factory record);

	Factory selectByPrimaryKey(String id);

	int updateByPrimaryKeySelective(Factory record);

	int updateByPrimaryKey(Factory record);

	List<Factory> queryForManufacturerId(@Param("manufacturerId") String manufacturerId);

	List<Factory> queryFactoryInfo(@Param("factoryCode") String factoryCode);

	List<Factory> queryList(Factory record);

	int countQueryList(Factory record);

	List<Factory> selectPage(Map<String, Object> map);

	int countSelectPage(Map<String, Object> map);

	List<Factory> queryFactoryCodeOui();

	List<Factory> selectFactoryByName(Factory record);
}