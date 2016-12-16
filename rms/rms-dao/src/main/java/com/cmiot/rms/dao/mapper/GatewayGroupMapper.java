package com.cmiot.rms.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.cmiot.rms.dao.model.GatewayGroup;
import com.cmiot.rms.dao.model.GatewayGroupExample;

public interface GatewayGroupMapper {
    int countByExample(GatewayGroup example);

    int deleteByExample(GatewayGroup example);

    int insert(GatewayGroup record);

    int insertSelective(GatewayGroup record);

    List<GatewayGroup> selectByExample(GatewayGroup example);

    GatewayGroup selectByParam(GatewayGroup example);

    int updateByExampleSelective(GatewayGroup record);

    int updateByExample(@Param("record") GatewayGroup record, @Param("example") GatewayGroupExample example);
}