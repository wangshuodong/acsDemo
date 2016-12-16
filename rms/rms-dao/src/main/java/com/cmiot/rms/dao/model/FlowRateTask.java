package com.cmiot.rms.dao.model;

public class FlowRateTask {
    private String id;

    private String taskName;

    private Integer taskTriggerMode;

    private Integer taskTriggerEvent;

    private Integer taskCreateTime;

    private Integer taskStartTime;

    private Integer taskEndTime;

    private Integer areaId;

    private Integer status;
    
    private String areaName;
    
    private String taskTriggerCondition;
    
    private String startTime;
    
    private String endTime;
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
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

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
    
}