package com.cmiot.rms.dao.model;

import java.io.Serializable;

public class OperationLog implements Serializable {
    private Integer logId;

    private Integer logOperationType;

    private Integer logModuleId;

    private Integer logOperatorId;

    private String logIp;

    private String logUrlPattern;

    private String logContent;

    private String logBeforeContent;

    private Integer logCreateTime;

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Integer getLogOperationType() {
        return logOperationType;
    }

    public void setLogOperationType(Integer logOperationType) {
        this.logOperationType = logOperationType;
    }

    public Integer getLogModuleId() {
        return logModuleId;
    }

    public void setLogModuleId(Integer logModuleId) {
        this.logModuleId = logModuleId;
    }

    public Integer getLogOperatorId() {
        return logOperatorId;
    }

    public void setLogOperatorId(Integer logOperatorId) {
        this.logOperatorId = logOperatorId;
    }

    public String getLogIp() {
        return logIp;
    }

    public void setLogIp(String logIp) {
        this.logIp = logIp == null ? null : logIp.trim();
    }

    public String getLogUrlPattern() {
        return logUrlPattern;
    }

    public void setLogUrlPattern(String logUrlPattern) {
        this.logUrlPattern = logUrlPattern == null ? null : logUrlPattern.trim();
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent == null ? null : logContent.trim();
    }

    public String getLogBeforeContent() {
        return logBeforeContent;
    }

    public void setLogBeforeContent(String logBeforeContent) {
        this.logBeforeContent = logBeforeContent == null ? null : logBeforeContent.trim();
    }

    public Integer getLogCreateTime() {
        return logCreateTime;
    }

    public void setLogCreateTime(Integer logCreateTime) {
        this.logCreateTime = logCreateTime;
    }
}