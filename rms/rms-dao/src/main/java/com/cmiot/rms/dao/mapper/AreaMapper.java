package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.Area;
import com.cmiot.rms.dao.model.AreaExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AreaMapper {
    int countByExample(AreaExample example);

    int deleteByExample(AreaExample example);

    int insert(Area record);

    int insertSelective(Area record);

    List<Area> selectByExample(AreaExample example);

    int updateByExampleSelective(@Param("record") Area record, @Param("example") AreaExample example);

    int updateByExample(@Param("record") Area record, @Param("example") AreaExample example);

    Area queryArea(Area record);
    
    List<Area> queryList();
    
}