package com.cmiot.rms.dao.model;

public class FirmwareUpgradeTask extends BaseBean {
    private String id;

    private String taskName;

    private String taskStartTime;

    private String taskEndTime;

    private Integer taskCreateTime;

    private Integer taskPeriod;

    private Boolean taskIsAutoStart;

    private Integer taskStatus;

    private String deviceId;

    private String deviceName="";//设备名称

    private String areaId;

    private String areaName="";//区域名称

    private String firmwareId;

    private String firmwareVersion="";//固件版本

    private String upgradeProcess; //升级进度

    private Integer currentTime; //当前时间，用于查询使用

    private Integer taskTriggerMode;

    private Integer taskTriggerEvent;

    private String taskDescription;


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

    public String getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(String taskStartTime) {
        this.taskStartTime = taskStartTime == null ? null : taskStartTime.trim();
    }

    public String getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(String taskEndTime) {
        this.taskEndTime = taskEndTime == null ? null : taskEndTime.trim();
    }

    public Integer getTaskCreateTime() {
        return taskCreateTime;
    }

    public void setTaskCreateTime(Integer taskCreateTime) {
        this.taskCreateTime = taskCreateTime;
    }

    public Integer getTaskPeriod() {
        return taskPeriod;
    }

    public void setTaskPeriod(Integer taskPeriod) {
        this.taskPeriod = taskPeriod;
    }

    public Boolean getTaskIsAutoStart() {
        return taskIsAutoStart;
    }

    public void setTaskIsAutoStart(Boolean taskIsAutoStart) {
        this.taskIsAutoStart = taskIsAutoStart;
    }

    public Integer getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(Integer taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId == null ? null : deviceId.trim();
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId == null ? null : areaId.trim();
    }

    public String getFirmwareId() {
        return firmwareId;
    }

    public void setFirmwareId(String firmwareId) {
        this.firmwareId = firmwareId == null ? null : firmwareId.trim();
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getUpgradeProcess() {
        return upgradeProcess;
    }

    public void setUpgradeProcess(String upgradeProcess) {
        this.upgradeProcess = upgradeProcess;
    }

    public Integer getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Integer currentTime) {
        this.currentTime = currentTime;
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

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription == null ? null : taskDescription.trim();
    }
}