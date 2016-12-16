package com.cmiot.rms.dao.model;

public class GatewayParam extends BaseBean {
	
    private Integer parmId;

    private String parmName;
    
    private String parmType;
    
    private Integer parmLength;
    
    private Boolean parmWriteable;
    
    private String parmValue="";
    
    private String parmFactory;
    
    private String gateWayUuid;//网关uuid
    

	public Integer getParmId() {
		return parmId;
	}

	public void setParmId(Integer parmId) {
		this.parmId = parmId;
	}

	public String getParmName() {
		return parmName;
	}

	public void setParmName(String parmName) {
		this.parmName = parmName;
	}

	public String getParmType() {
		return parmType;
	}

	public void setParmType(String parmType) {
		this.parmType = parmType;
	}

	public Integer getParmLength() {
		return parmLength;
	}

	public void setParmLength(Integer parmLength) {
		this.parmLength = parmLength;
	}

	public Boolean getParmWriteable() {
		return parmWriteable;
	}

	public void setParmWriteable(Boolean parmWriteable) {
		this.parmWriteable = parmWriteable;
	}

	public String getParmValue() {
		return parmValue;
	}

	public void setParmValue(String parmValue) {
		this.parmValue = parmValue;
	}

	public String getParmFactory() {
		return parmFactory;
	}

	public void setParmFactory(String parmFactory) {
		this.parmFactory = parmFactory;
	}

	public String getGateWayUuid() {
		return gateWayUuid;
	}

	public void setGateWayUuid(String gateWayUuid) {
		this.gateWayUuid = gateWayUuid;
	}
}