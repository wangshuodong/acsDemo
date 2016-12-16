package com.cmiot.rms.dao.model;

public class GatewayBackupFileTask extends BaseBean{
    private String id;

    private String taskName;

    private Integer taskTriggerMode;

    private Integer taskTriggerEvent;

    //用于前台展示触发条件taskTriggerMode为1时：显示 开始时间-结束时时间； askTriggerMode为2是：显示1:第一次启动 2：周期心跳上报 3：重新启动

    private String taskTriggerCondition;

    private String startTime;//用于前端传入

    private String endTime;//用于前端传入

    private Integer taskCreateTime;

    private Integer taskStartTime;

    private Integer taskEndTime;

    private Integer areaId;

    private String areaName;

    private Integer status;

    private Integer currentTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName == null ? null : taskName.trim();
    }

    public Integer getTaskTriggerMode() {
        return taskTriggerMode;
    }

    public void setTaskTriggerMode(Integer taskTriggerMode) {
        this.taskTriggerMode = taskTriggerMode;
    }

    public Integer getTaskTriggerEvent() {
        return taskTriggerEvent;
    }

    public void setTaskTriggerEvent(Integer taskTriggerEvent) {
        this.taskTriggerEvent = taskTriggerEvent;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime == null ? null : startTime.trim();
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime == null ? null : endTime.trim();
    }

    public Integer getTaskCreateTime() {
        return taskCreateTime;
    }

    public void setTaskCreateTime(Integer taskCreateTime) {
        this.taskCreateTime = taskCreateTime;
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

    public Integer getAreaId() {
        return areaId;
    }

    public void setAreaId(Integer areaId) {
        this.areaId = areaId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getTaskTriggerCondition() {
        return taskTriggerCondition;
    }

    public void setTaskTriggerCondition(String taskTriggerCondition) {
        this.taskTriggerCondition = taskTriggerCondition;
    }

    public Integer getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Integer currentTime) {
        this.currentTime = currentTime;
    }
}