package com.cmiot.rms.dao.model;

/**
 * Created by weilei on 2016/5/17.
 */
public class BatchSetTaskDetail {

    private String id;

    private String batchSetTaskId;//关联t_batch_set_task.id

    private String parmName;//参数名称

    private String parmValue;//参数值

    private String parmType;//参数类型

    private int parmLength;//参数长度

    private String parmWriteable;//参数是否可写，1为可写，0为不可写

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBatchSetTaskId() {
        return batchSetTaskId;
    }

    public void setBatchSetTaskId(String batchSetTaskId) {
        this.batchSetTaskId = batchSetTaskId;
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

    public int getParmLength() {
        return parmLength;
    }

    public void setParmLength(int parmLength) {
        this.parmLength = parmLength;
    }

    public String getParmWriteable() {
        return parmWriteable;
    }

    public void setParmWriteable(String parmWriteable) {
        this.parmWriteable = parmWriteable;
    }

    public String getParmValue() {
        return parmValue;
    }

    public void setParmValue(String parmValue) {
        this.parmValue = parmValue;
    }
}
