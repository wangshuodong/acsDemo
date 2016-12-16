package com.cmiot.rms.dao.model;

public class GatewayQueue {
    private String gatewayQueueId;

    private String gatewayUuid;

    private Integer synStatu;

    public String getGatewayQueueId() {
        return gatewayQueueId;
    }

    public void setGatewayQueueId(String gatewayQueueId) {
        this.gatewayQueueId = gatewayQueueId == null ? null : gatewayQueueId.trim();
    }

    public String getGatewayUuid() {
        return gatewayUuid;
    }

    public void setGatewayUuid(String gatewayUuid) {
        this.gatewayUuid = gatewayUuid == null ? null : gatewayUuid.trim();
    }

    public Integer getSynStatu() {
        return synStatu;
    }

    public void setSynStatu(Integer synStatu) {
        this.synStatu = synStatu;
    }
}