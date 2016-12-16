package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.GatewayBackupFileTask;

import java.util.List;
import java.util.Map;

public interface GatewayBackupFileTaskMapper {
    int deleteByPrimaryKey(String id);

    int insert(GatewayBackupFileTask record);

    int insertSelective(GatewayBackupFileTask record);

    GatewayBackupFileTask selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(GatewayBackupFileTask record);

    int updateByPrimaryKey(GatewayBackupFileTask record);

    List<GatewayBackupFileTask> selectList(Map<String, Object> para);

    List<GatewayBackupFileTask> selectTimingTask(int currentTime);

    List<GatewayBackupFileTask> selectTask(Map<String, Object> para);

    List<Map<String, Object>> selectGatewayIdsByArea(Map<String, Object> para);

    int selectGatewayCountByArea(List<Integer> areas);

    int selectSameTaskForEvent(GatewayBackupFileTask record);

    int selectSameTaskForTime(GatewayBackupFileTask record);

    int selectSameTaskByName(GatewayBackupFileTask record);

    List<GatewayBackupFileTask> selectProcessingTask(Integer currenTime);

    int batchUpdateTaskStatus(Map<String, Object> para);
}