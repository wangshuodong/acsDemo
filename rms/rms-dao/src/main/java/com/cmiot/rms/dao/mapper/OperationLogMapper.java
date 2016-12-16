package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.OperationLog;

public interface OperationLogMapper {
    int deleteByPrimaryKey(Integer logId);

    int insert(OperationLog record);

    int insertSelective(OperationLog record);

    OperationLog selectByPrimaryKey(Integer logId);

    int updateByPrimaryKeySelective(OperationLog record);

    int updateByPrimaryKey(OperationLog record);
}