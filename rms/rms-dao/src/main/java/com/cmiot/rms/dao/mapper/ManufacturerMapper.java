package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.Manufacturer;

public interface ManufacturerMapper {
    int deleteByPrimaryKey(String id);

    int insert(Manufacturer record);

    int insertSelective(Manufacturer record);

    Manufacturer selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Manufacturer record);

    int updateByPrimaryKey(Manufacturer record);

    List<Manufacturer> queryAll();

	List<Manufacturer> querySelective(Manufacturer record);

	int countQuerySelective(Manufacturer record);

	List<Manufacturer> selectPage(Map<String, Object> map);

	int countSelectPage(Map<String, Object> map);

	List<Manufacturer> queryManufactureByName(Manufacturer record);
}