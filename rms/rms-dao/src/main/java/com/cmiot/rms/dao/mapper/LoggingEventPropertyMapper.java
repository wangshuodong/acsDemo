package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.LoggingEventProperty;
import com.cmiot.rms.dao.model.LoggingEventPropertyKey;

public interface LoggingEventPropertyMapper extends BaseMapper<LoggingEventProperty> {
    int deleteByPrimaryKey(LoggingEventPropertyKey key);

    int insert(LoggingEventProperty record);

    int insertSelective(LoggingEventProperty record);

    LoggingEventProperty selectByPrimaryKey(LoggingEventPropertyKey key);

    int updateByPrimaryKeySelective(LoggingEventProperty record);

    int updateByPrimaryKeyWithBLOBs(LoggingEventProperty record);
}