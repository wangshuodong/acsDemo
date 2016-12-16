package com.cmiot.rms.dao.model;

import java.io.Serializable;

public class LogAutoBak implements Serializable{
    private String id;

    private Integer intervalDate;

    private Long lastWorkTime;

    private Boolean effective;

    private String savePath;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public Integer getIntervalDate() {
        return intervalDate;
    }

    public void setIntervalDate(Integer intervalDate) {
        this.intervalDate = intervalDate;
    }

    public Long getLastWorkTime() {
        return lastWorkTime;
    }

    public void setLastWorkTime(Long lastWorkTime) {
        this.lastWorkTime = lastWorkTime;
    }

    public Boolean getEffective() {
        return effective;
    }

    public void setEffective(Boolean effective) {
        this.effective = effective;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath == null ? null : savePath.trim();
    }
}