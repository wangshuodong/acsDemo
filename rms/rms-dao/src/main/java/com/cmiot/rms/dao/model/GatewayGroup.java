package com.cmiot.rms.dao.model;

public class GatewayGroup {
    private String groupUuid;

    private String groupName;

    private String gatewayUuid;

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid == null ? null : groupUuid.trim();
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName == null ? null : groupName.trim();
    }

    public String getGatewayUuid() {
        return gatewayUuid;
    }

    public void setGatewayUuid(String gatewayUuid) {
        this.gatewayUuid = gatewayUuid == null ? null : gatewayUuid.trim();
    }
}