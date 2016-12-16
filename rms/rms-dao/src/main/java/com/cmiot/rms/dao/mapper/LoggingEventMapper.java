package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.common.page.PageBean;
import com.cmiot.rms.dao.model.LoggingEvent;

import java.util.List;

public interface LoggingEventMapper{
    int deleteByPrimaryKey(Long eventId);

    int insert(LoggingEvent record);

    int insertSelective(LoggingEvent record);

    LoggingEvent selectByPrimaryKey(Long eventId);

    int updateByPrimaryKeySelective(LoggingEvent record);

    int updateByPrimaryKeyWithBLOBs(LoggingEvent record);

    int updateByPrimaryKey(LoggingEvent record);

    List<LoggingEvent> queryList(LoggingEvent record);
}