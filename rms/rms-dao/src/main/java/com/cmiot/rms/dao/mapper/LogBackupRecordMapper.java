package com.cmiot.rms.dao.mapper;

import java.util.List;

import com.cmiot.rms.dao.model.LoggingRecord;

public interface LogBackupRecordMapper 
{
	int insert(LoggingRecord record);
	
	int update(LoggingRecord record);
	
	List<LoggingRecord> queryList();
}
