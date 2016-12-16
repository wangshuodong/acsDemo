package com.cmiot.rms.dao.model;

public class BoxFirmwareUpgradeTask {
	private String id;// 升级任务编号UUID

	private String taskName;// 升级任务名称

	private String taskStartTime;// 升级任务时间段开始时间

	private String taskEndTime;// 升级任务时间段结束时间

	private Integer taskCreateTime;// 任务创建时间

	private Integer taskStatus;// 升级状态成功 0:新加任务, 1:正在升级处理中, 2:升级任务结束

	private String factoryCode;// 厂商编码

	private String areaId;// 城市区域编号

	private String areaName;// 区域名称,映射表中无此字段

	private String currentFirmwareUuid;// 当前固件版本UUID

	private String targetFirmwareId;// 目标固件文件编号UUID

	private Integer taskTriggerMode;// 任务触发方式 1:即时触发 2:条件触发

	private Integer taskTriggerEvent;// 务触发事件 1:初始安装第一次启动时 2：周期心跳上报时 3：开机登录时

	private String taskDescription;// 描述

	private Integer taskPeriod;// 任务执行周期 0:当天 1:本周

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

	public Integer getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(Integer taskStatus) {
		this.taskStatus = taskStatus;
	}

	public String getFactoryCode() {
		return factoryCode;
	}

	public void setFactoryCode(String factoryCode) {
		this.factoryCode = factoryCode == null ? null : factoryCode.trim();
	}

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId == null ? null : areaId.trim();
	}

	public String getCurrentFirmwareUuid() {
		return currentFirmwareUuid;
	}

	public void setCurrentFirmwareUuid(String currentFirmwareUuid) {
		this.currentFirmwareUuid = currentFirmwareUuid == null ? null : currentFirmwareUuid.trim();
	}

	public String getTargetFirmwareId() {
		return targetFirmwareId;
	}

	public void setTargetFirmwareId(String targetFirmwareId) {
		this.targetFirmwareId = targetFirmwareId == null ? null : targetFirmwareId.trim();
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

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public Integer getTaskPeriod() {
		return taskPeriod;
	}

	public void setTaskPeriod(Integer taskPeriod) {
		this.taskPeriod = taskPeriod;
	}

}