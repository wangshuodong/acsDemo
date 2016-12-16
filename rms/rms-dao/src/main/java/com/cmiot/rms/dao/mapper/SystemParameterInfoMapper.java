package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.SystemParameterInfo;
import org.apache.ibatis.annotations.Param;

public interface SystemParameterInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(SystemParameterInfo record);

    int insertSelective(SystemParameterInfo record);

    SystemParameterInfo selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SystemParameterInfo record);

    int updateByPrimaryKey(SystemParameterInfo record);

    int updateByName(SystemParameterInfo record);

    SystemParameterInfo searchByName(@Param("parameterName") String parameterName);
}