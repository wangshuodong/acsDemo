package com.cmiot.rms.dao.model;

public class FirmwareUpgradeTaskDetail extends BaseBean {
    private String id;

    private String gatewayId;

    private String gatewayName;

    private Integer status;

    private String firmwareId;

    private String firmwareVersion;

    private String previousFirmwareId;

    private String previousFirmwareVersion;

    private Integer upgradeStartTime;

    private Integer upgradeEndTime;

    private Boolean isRetry;

    private Integer retryTimes;

    private String upgradeTaskId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId == null ? null : gatewayId.trim();
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName == null ? null : gatewayName.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFirmwareId() {
        return firmwareId;
    }

    public void setFirmwareId(String firmwareId) {
        this.firmwareId = firmwareId == null ? null : firmwareId.trim();
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion == null ? null : firmwareVersion.trim();
    }

    public String getPreviousFirmwareId() {
        return previousFirmwareId;
    }

    public void setPreviousFirmwareId(String previousFirmwareId) {
        this.previousFirmwareId = previousFirmwareId == null ? null : previousFirmwareId.trim();
    }

    public String getPreviousFirmwareVersion() {
        return previousFirmwareVersion;
    }

    public void setPreviousFirmwareVersion(String previousFirmwareVersion) {
        this.previousFirmwareVersion = previousFirmwareVersion == null ? null : previousFirmwareVersion.trim();
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

    public String getUpgradeTaskId() {
        return upgradeTaskId;
    }

    public void setUpgradeTaskId(String upgradeTaskId) {
        this.upgradeTaskId = upgradeTaskId == null ? null : upgradeTaskId.trim();
    }
}