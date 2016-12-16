package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.GatewayLogFileInfo;

import java.util.List;

public interface GatewayLogFileInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(GatewayLogFileInfo record);

    int insertSelective(GatewayLogFileInfo record);

    GatewayLogFileInfo selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(GatewayLogFileInfo record);

    int updateByPrimaryKey(GatewayLogFileInfo record);

    List<GatewayLogFileInfo> selectGatewayLogFileInfoList(String gatewayId);
}