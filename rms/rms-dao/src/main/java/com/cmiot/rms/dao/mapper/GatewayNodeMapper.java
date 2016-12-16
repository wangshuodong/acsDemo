package com.cmiot.rms.dao.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.cmiot.rms.dao.model.GatewayNode;
import com.cmiot.rms.dao.model.GatewayNodeExample;

public interface GatewayNodeMapper {
    int countByExample(GatewayNodeExample example);

    int deleteByExample(GatewayNodeExample example);

    int deleteByPrimaryKey(String id);

    int insert(GatewayNode record);

    int insertSelective(GatewayNode record);

    List<GatewayNode> selectByExample(GatewayNodeExample example);

    GatewayNode selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") GatewayNode record, @Param("example") GatewayNodeExample example);

    int updateByExample(@Param("record") GatewayNode record, @Param("example") GatewayNodeExample example);

    int updateByPrimaryKeySelective(GatewayNode record);

    int updateByPrimaryKey(GatewayNode record);
}