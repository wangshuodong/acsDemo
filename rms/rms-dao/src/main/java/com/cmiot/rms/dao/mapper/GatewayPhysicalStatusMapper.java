package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.GatewayPhysicalStatus;

public interface GatewayPhysicalStatusMapper {
    int deleteByPrimaryKey(String gatewayPhysicalStatusUuid);

    int insert(GatewayPhysicalStatus record);

    int insertSelective(GatewayPhysicalStatus record);

    GatewayPhysicalStatus selectByPrimaryKey(String gatewayPhysicalStatusUuid);

    int updateByPrimaryKeySelective(GatewayPhysicalStatus record);

    int updateByPrimaryKey(GatewayPhysicalStatus record);
}