package com.cmiot.rms.dao.model;

public class GatewayFlowrateTaskDetail {
    private String id;

    private String taskId;

    private String gatewayId;

    private String flowrate;

    private Integer createDate;
    
    private String firmwareId;
    
    private String gatewayMac;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getFlowrate() {
        return flowrate;
    }

    public void setFlowrate(String flowrate) {
        this.flowrate = flowrate;
    }

    public Integer getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Integer createDate) {
        this.createDate = createDate;
    }

	public String getFirmwareId() {
		return firmwareId;
	}

	public void setFirmwareId(String firmwareId) {
		this.firmwareId = firmwareId;
	}

	public String getGatewayMac() {
		return gatewayMac;
	}

	public void setGatewayMac(String gatewayMac) {
		this.gatewayMac = gatewayMac;
	}
    
}