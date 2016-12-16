package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.BoxBusinessCategory;


public interface BoxBusinessCategoryMapper {
	int insert(BoxBusinessCategory category);
	int updateByPrimaryKey(BoxBusinessCategory category);
	int deleteByPrimaryKey(String id);
	Map<String,Object> selectByPrimaryKey(String id);
	List<Map<String,Object>> findBoxBusinessById(String id);
	List<Map<String,Object>> queryListAll(BoxBusinessCategory category);
	List<Map<String,Object>> queryBusinessForList(Map<String,Object> map);
}
