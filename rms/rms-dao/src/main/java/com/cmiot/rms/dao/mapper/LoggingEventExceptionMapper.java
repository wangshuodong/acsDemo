package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.LoggingEventException;
import com.cmiot.rms.dao.model.LoggingEventExceptionKey;

public interface LoggingEventExceptionMapper extends BaseMapper<LoggingEventException>{
    int deleteByPrimaryKey(LoggingEventExceptionKey key);

    int insert(LoggingEventException record);

    int insertSelective(LoggingEventException record);

    LoggingEventException selectByPrimaryKey(LoggingEventExceptionKey key);

    int updateByPrimaryKeySelective(LoggingEventException record);

    int updateByPrimaryKey(LoggingEventException record);
}