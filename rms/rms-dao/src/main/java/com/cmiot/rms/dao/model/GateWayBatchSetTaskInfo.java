package com.cmiot.rms.dao.model;

/**
 * Created by admin on 2016/5/19.
 */
public class GateWayBatchSetTaskInfo extends BaseBean{
    private String id;

    private String gateWayInfoId;//关联关联t_gateway_info.uuid

    private String isSuccess;//是否设置成功，1表示成功，2表示不成功

    private String batchSetTaskId;//批量设置任务ID，关联批量设置任务表的id

    private String trrigerEvent;//触发事件1、初始安装第一次启动时触发；2、周期心跳上报时触发；3、设备重新启动时触发

    private String gateWayMacAddress;//网关MAC地址，冗余字段，存放查询结果


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGateWayInfoId() {
        return gateWayInfoId;
    }

    public void setGateWayInfoId(String gateWayInfoId) {
        this.gateWayInfoId = gateWayInfoId;
    }

    public String getIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(String isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getBatchSetTaskId() {
        return batchSetTaskId;
    }

    public void setBatchSetTaskId(String batchSetTaskId) {
        this.batchSetTaskId = batchSetTaskId;
    }

    public String getTrrigerEvent() {
        return trrigerEvent;
    }

    public void setTrrigerEvent(String trrigerEvent) {
        this.trrigerEvent = trrigerEvent;
    }

    public String getGateWayMacAddress() {
        return gateWayMacAddress;
    }

    public void setGateWayMacAddress(String gateWayMacAddress) {
        this.gateWayMacAddress = gateWayMacAddress;
    }
}
