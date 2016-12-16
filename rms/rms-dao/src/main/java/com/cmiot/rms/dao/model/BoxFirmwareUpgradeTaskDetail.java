package com.cmiot.rms.dao.model;

public class BoxFirmwareUpgradeTaskDetail {
	private String id;//任务详情编号UUID

	private String upgradeTaskId;//升级任务编号UUID

	private String boxId;//机顶盒UUID
	
	private Integer status;//升级状态 0:新加任务, 1:失败, 2:成功 ， 3:升级中

	private Integer upgradeStartTime;//此网关升级的实际开始时间

	private Integer upgradeEndTime;//此网关升级的实际结束时间

	private Boolean isRetry;//升级失败后是否重新发起升级 0:否 1:是

	private Integer retryTimes;//升级失败后重新发起升级的次数

	// private String firmwareId;// 固件文件编号UUID
	//
	// private String firmwareVersion;// 固件文件版本号
	//
	// private String previousFirmwareId;// 上一次固件文件编号UUID
	//
	// private String previousFirmwareVersion;// 上一次固件文件版本号

//	public String getFirmwareId() {
//		return firmwareId;
//	}
//
//	public void setFirmwareId(String firmwareId) {
//		this.firmwareId = firmwareId;
//	}
//
//	public String getFirmwareVersion() {
//		return firmwareVersion;
//	}
//
//	public void setFirmwareVersion(String firmwareVersion) {
//		this.firmwareVersion = firmwareVersion;
//	}
//
//	public String getPreviousFirmwareId() {
//		return previousFirmwareId;
//	}
//
//	public void setPreviousFirmwareId(String previousFirmwareId) {
//		this.previousFirmwareId = previousFirmwareId;
//	}
//
//	public String getPreviousFirmwareVersion() {
//		return previousFirmwareVersion;
//	}
//
//	public void setPreviousFirmwareVersion(String previousFirmwareVersion) {
//		this.previousFirmwareVersion = previousFirmwareVersion;
//	}

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

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId == null ? null : boxId.trim();
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getUpgradeStartTime() {
		return upgradeStartTime;
	}

	public void setUpgradeStartTime(Integer upgradeStartTime) {
		this.upgradeStartTime = upgradeStartTime;
	}

	public Integer getUpgradeEndTime() {
		return upgradeEndTime;
	}

	public void setUpgradeEndTime(Integer upgradeEndTime) {
		this.upgradeEndTime = upgradeEndTime;
	}

	public Boolean getIsRetry() {
		return isRetry;
	}

	public void setIsRetry(Boolean isRetry) {
		this.isRetry = isRetry;
	}

	public Integer getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(Integer retryTimes) {
		this.retryTimes = retryTimes;
	}
}