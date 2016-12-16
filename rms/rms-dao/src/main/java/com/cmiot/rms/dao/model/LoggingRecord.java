package com.cmiot.rms.dao.model;

import java.io.Serializable;

public class LoggingRecord implements Serializable
{
	private String recordId;
	private String backupTime;
	private String logType;
	
	/**
	 * 0:备份成功，1：备份失败，2：备份中
	 */
	private String status;
	private String filePath;
	
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getRecordId() {
		return recordId;
	}
	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}
	public String getBackupTime() {
		return backupTime;
	}
	public void setBackupTime(String backupTime) {
		this.backupTime = backupTime;
	}
	public String getLogType() {
		return logType;
	}
	public void setLogType(String logType) {
		this.logType = logType;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
