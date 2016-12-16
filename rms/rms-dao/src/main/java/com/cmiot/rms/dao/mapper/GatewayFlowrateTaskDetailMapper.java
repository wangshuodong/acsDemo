package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.GatewayFlowrateTaskDetail;

public interface GatewayFlowrateTaskDetailMapper {
    int deleteByPrimaryKey(String id);

    int insert(GatewayFlowrateTaskDetail record);

    int insertSelective(GatewayFlowrateTaskDetail record);

    GatewayFlowrateTaskDetail selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(GatewayFlowrateTaskDetail record);

    int updateByPrimaryKey(GatewayFlowrateTaskDetail record);

	List<Map<String, Object>> selectByTaskId(Map<String, Object> params);
}