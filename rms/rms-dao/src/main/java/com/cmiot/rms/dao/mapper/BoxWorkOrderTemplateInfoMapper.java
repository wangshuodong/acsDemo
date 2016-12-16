package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.BoxWorkOrderTemplateInfo;

public interface BoxWorkOrderTemplateInfoMapper {
	int deleteByPrimaryKey(String id);


    int insert(BoxWorkOrderTemplateInfo record);


    int insertSelective(BoxWorkOrderTemplateInfo record);


    BoxWorkOrderTemplateInfo selectByPrimaryKey(String id);

    List<BoxWorkOrderTemplateInfo> selectByParam(BoxWorkOrderTemplateInfo boxWorkOrderTemplateInfo);


    List<BoxWorkOrderTemplateInfo> selectByParamMap(Map map);

    int selectCountByParam(Map map);

    int updateByPrimaryKeySelective(BoxWorkOrderTemplateInfo record);
}
