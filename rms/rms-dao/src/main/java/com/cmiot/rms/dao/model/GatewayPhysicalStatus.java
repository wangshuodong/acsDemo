package com.cmiot.rms.dao.model;

public class GatewayPhysicalStatus extends BaseBean {
    private String gatewayPhysicalStatusUuid;

    private String gatewayPhysicalStatusCpuUsage;

    private String gatewayPhysicalStatusMemUsage;

    private String gatewayPhysicalStatusProcessName;

    private String gatewayPhysicalStatusLanStatus;

    private String gatewayPhysicalStatusWanStatus;

    private String gatewayInfoUuid;

    public String getGatewayPhysicalStatusUuid() {
        return gatewayPhysicalStatusUuid;
    }

    public void setGatewayPhysicalStatusUuid(String gatewayPhysicalStatusUuid) {
        this.gatewayPhysicalStatusUuid = gatewayPhysicalStatusUuid == null ? null : gatewayPhysicalStatusUuid.trim();
    }

    public String getGatewayPhysicalStatusCpuUsage() {
        return gatewayPhysicalStatusCpuUsage;
    }

    public void setGatewayPhysicalStatusCpuUsage(String gatewayPhysicalStatusCpuUsage) {
        this.gatewayPhysicalStatusCpuUsage = gatewayPhysicalStatusCpuUsage == null ? null : gatewayPhysicalStatusCpuUsage.trim();
    }

    public String getGatewayPhysicalStatusMemUsage() {
        return gatewayPhysicalStatusMemUsage;
    }

    public void setGatewayPhysicalStatusMemUsage(String gatewayPhysicalStatusMemUsage) {
        this.gatewayPhysicalStatusMemUsage = gatewayPhysicalStatusMemUsage == null ? null : gatewayPhysicalStatusMemUsage.trim();
    }

    public String getGatewayPhysicalStatusProcessName() {
        return gatewayPhysicalStatusProcessName;
    }

    public void setGatewayPhysicalStatusProcessName(String gatewayPhysicalStatusProcessName) {
        this.gatewayPhysicalStatusProcessName = gatewayPhysicalStatusProcessName == null ? null : gatewayPhysicalStatusProcessName.trim();
    }

    public String getGatewayPhysicalStatusLanStatus() {
        return gatewayPhysicalStatusLanStatus;
    }

    public void setGatewayPhysicalStatusLanStatus(String gatewayPhysicalStatusLanStatus) {
        this.gatewayPhysicalStatusLanStatus = gatewayPhysicalStatusLanStatus == null ? null : gatewayPhysicalStatusLanStatus.trim();
    }

    public String getGatewayPhysicalStatusWanStatus() {
        return gatewayPhysicalStatusWanStatus;
    }

    public void setGatewayPhysicalStatusWanStatus(String gatewayPhysicalStatusWanStatus) {
        this.gatewayPhysicalStatusWanStatus = gatewayPhysicalStatusWanStatus == null ? null : gatewayPhysicalStatusWanStatus.trim();
    }

    public String getGatewayInfoUuid() {
        return gatewayInfoUuid;
    }

    public void setGatewayInfoUuid(String gatewayInfoUuid) {
        this.gatewayInfoUuid = gatewayInfoUuid == null ? null : gatewayInfoUuid.trim();
    }
}