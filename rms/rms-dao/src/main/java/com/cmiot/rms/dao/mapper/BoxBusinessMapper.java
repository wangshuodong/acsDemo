package com.cmiot.rms.dao.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.cmiot.rms.dao.model.BoxBusiness;
import com.cmiot.rms.dao.model.BoxBusinessExample;

public interface BoxBusinessMapper {
    int countByExample(BoxBusinessExample example);

    int deleteByExample(BoxBusinessExample example);

    int deleteByPrimaryKey(String id);

    int insert(BoxBusiness record);

    int insertSelective(BoxBusiness record);

    List<BoxBusiness> selectByExample(BoxBusinessExample example);

    BoxBusiness selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") BoxBusiness record, @Param("example") BoxBusinessExample example);

    int updateByExample(@Param("record") BoxBusiness record, @Param("example") BoxBusinessExample example);

    int updateByPrimaryKeySelective(BoxBusiness record);

    int updateByPrimaryKey(BoxBusiness record);
}