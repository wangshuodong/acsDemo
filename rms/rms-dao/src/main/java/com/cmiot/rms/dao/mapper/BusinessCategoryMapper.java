package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.BusinessCategory;

public interface BusinessCategoryMapper {
   
	int deleteByPrimaryKey(String id);

    int insert(BusinessCategory category);

    Map<String,Object> selectByPrimaryKey(String id);

    int updateByPrimaryKey(BusinessCategory category);
    
    List<Map<String,Object>> queryList(BusinessCategory category);

    List<Map<String,Object>> queryListLike(BusinessCategory category);
    
    List<Map<String,Object>> findGatewayBusinessById(String id);

    List<BusinessCategory> findGatewayBusinessByTemp(BusinessCategory category);

    List<Map<String,Object>> queryListAll(BusinessCategory category);
    List<BusinessCategory> findAllGatewayBusiness();
    //安徽定制分页查询
    List<Map<String,Object>> queryBusinessForList(BusinessCategory category);

    List<Map<String,Object>> queryBusinessForListNew(List<String> list);

    Integer queryCount(Map<String,Object> map);

    List<String> queryBusinessIds(Map<String,Object> map);

    Map<String,Object> queryQueryBusinessAndTemplate(Map<String,Object> map);

    List<Map<String,Object>> queryListTemplate(Map<String,Object> map);

    List<Map<String,Object>> queryListDefaltTemplate(Map<String,Object> map);

    List<Map<String,Object>> queryRelationByTemplate(Map<String,Object> map);
}