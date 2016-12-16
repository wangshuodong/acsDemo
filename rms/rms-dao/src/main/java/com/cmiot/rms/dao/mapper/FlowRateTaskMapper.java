package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.FlowRateTask;

public interface FlowRateTaskMapper {
    int deleteByPrimaryKey(String id);

    int insert(FlowRateTask record);

    int insertSelective(FlowRateTask record);

    FlowRateTask selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FlowRateTask record);

    int updateByPrimaryKey(FlowRateTask record);

	List<FlowRateTask> selectList(Map<String, Object> queryMap);

	List<Map<String, Object>> queryExistTaskWithGatewayUuid(Map<String, Object> param);

	List<Map<String, Object>> selectTimingTask(int currentTime);

    int batchUpdateStatus(Map<String, Object> para);
}