package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.GatewayBackupFileTaskArea;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface GatewayBackupFileTaskAreaMapper {
    int deleteByPrimaryKey(String id);

    int insert(GatewayBackupFileTaskArea record);

    int insertSelective(GatewayBackupFileTaskArea record);

    GatewayBackupFileTaskArea selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(GatewayBackupFileTaskArea record);

    int updateByPrimaryKey(GatewayBackupFileTaskArea record);

    int batchInsert(List<GatewayBackupFileTaskArea> taskAreaList);

    int deleteByTaskId(String taskId);

    List<Map<String, Object>> selectAreasById(@Param("taskId") String taskId);
}

