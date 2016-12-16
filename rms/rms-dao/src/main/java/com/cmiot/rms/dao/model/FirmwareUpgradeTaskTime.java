package com.cmiot.rms.dao.model;

public class FirmwareUpgradeTaskTime {
    private String id;

    private String upgradeTaskId;

    private Integer taskStartTime;

    private Integer taskEndTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getUpgradeTaskId() {
        return upgradeTaskId;
    }

    public void setUpgradeTaskId(String upgradeTaskId) {
        this.upgradeTaskId = upgradeTaskId == null ? null : upgradeTaskId.trim();
    }

    public Integer getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(Integer taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public Integer getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(Integer taskEndTime) {
        this.taskEndTime = taskEndTime;
    }
}