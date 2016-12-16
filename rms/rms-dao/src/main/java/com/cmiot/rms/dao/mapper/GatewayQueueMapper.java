package com.cmiot.rms.dao.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.cmiot.rms.dao.model.GatewayQueue;
import com.cmiot.rms.dao.model.GatewayQueueExample;

public interface GatewayQueueMapper {
	
    /**
     * 每次最多查询5000条
     * @param example
     * @return
     */
    int countByExample(GatewayQueueExample example);

    int deleteByExample(GatewayQueueExample example);

    int deleteByPrimaryKey(String gatewayQueueId);

    int insert(GatewayQueue record);

    int insertSelective(GatewayQueue record);

    List<GatewayQueue> selectByExample(GatewayQueueExample example);

    GatewayQueue selectByPrimaryKey(String gatewayQueueId);

    int updateByExampleSelective(@Param("record") GatewayQueue record, @Param("example") GatewayQueueExample example);

    int updateByExample(@Param("record") GatewayQueue record, @Param("example") GatewayQueueExample example);

    int updateByPrimaryKeySelective(GatewayQueue record);

    int updateByPrimaryKey(GatewayQueue record);
    
    int batchUpdateGatewayQueue(List<GatewayQueue> list);

    void batchInsertGatewayQueue(List<GatewayQueue> allGatewayQueueDatas);
}