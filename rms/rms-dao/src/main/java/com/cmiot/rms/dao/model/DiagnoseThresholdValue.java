package com.cmiot.rms.dao.model;

public class DiagnoseThresholdValue {

	private String thresholdName;// 阈值名称

	private String minThresholdValue;// 阈值数值

	private String maxThresholdValue;// 阈值数值

	private Integer diagnoseType;// 诊断类型1:线路诊断;2:Ping诊断;3:Traceroute诊断;4:PPPoE仿真;5:DHCP仿真;6:VoIP诊断;7:HTTP下载仿真

	private Integer equipmentType;// 设备类型1:网关;2:机顶盒

	public DiagnoseThresholdValue(Integer diagnoseType, Integer equipmentType) {
		super();
		this.diagnoseType = diagnoseType;
		this.equipmentType = equipmentType;
	}

	public DiagnoseThresholdValue() {
		super();
	}

	public String getThresholdName() {
		return thresholdName;
	}

	public void setThresholdName(String thresholdName) {
		this.thresholdName = thresholdName == null ? null : thresholdName.trim();
	}

	public Integer getDiagnoseType() {
		return diagnoseType;
	}

	public void setDiagnoseType(Integer diagnoseType) {
		this.diagnoseType = diagnoseType;
	}

	public Integer getEquipmentType() {
		return equipmentType;
	}

	public void setEquipmentType(Integer equipmentType) {
		this.equipmentType = equipmentType;
	}

	public String getMinThresholdValue() {
		return minThresholdValue;
	}

	public void setMinThresholdValue(String minThresholdValue) {
		this.minThresholdValue = minThresholdValue;
	}

	public String getMaxThresholdValue() {
		return maxThresholdValue;
	}

	public void setMaxThresholdValue(String maxThresholdValue) {
		this.maxThresholdValue = maxThresholdValue;
	}

}