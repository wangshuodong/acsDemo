package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.WorkOrderTemplateInfo;
import org.apache.ibatis.annotations.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface WorkOrderTemplateInfoMapper {

    int deleteByPrimaryKey(String id);


    int insert(WorkOrderTemplateInfo record);


    int insertSelective(WorkOrderTemplateInfo record);


    WorkOrderTemplateInfo selectByPrimaryKey(String id);

    List<WorkOrderTemplateInfo> selectByParam(WorkOrderTemplateInfo workOrderTemplateInfo);


    List<WorkOrderTemplateInfo> selectByParamMap(Map map);

    int selectCountByParam(Map map);

    int updateByPrimaryKeySelective(WorkOrderTemplateInfo record);


    int updateByPrimaryKey(WorkOrderTemplateInfo record);

    Map selectByBusinessCode(Map<String,Object> map);

    Map selectDefaultTemplate(Map<String,Object> map);
}