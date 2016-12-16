package com.cmiot.rms.dao.model;

/**
 * Created by admin on 2016/5/17.
 */
public class BatchSetTask {

    private String id;

    private String areaCode;//地域编码

    private String areaName;//地域名称

    private int isDelete;//删除标志，1为未删除，2为删除

    private String createTime;//创建时间

    private String deleteTime;//删除时间

    private String taskState;//任务状态，0为未开始，1为进行中

    private String trrigerEvent1;//表示初始安装第一次启动时触发，0表示不生效，1表示生效

    private String trrigerEvent2;//表示周期心跳上报时触发；0表示不生效，1表示生效

    private String trrigerEvent3;//表示设备重新启动时触发，0表示不生效，1表示生效

    private String trrigerEvent4;//表示设备参数变化时触发，0表示不生效，1表示生效

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public int getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(int isDelete) {
        this.isDelete = isDelete;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(String deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getTaskState() {
        return taskState;
    }

    public void setTaskState(String taskState) {
        this.taskState = taskState;
    }

    public String getTrrigerEvent1() {
        return trrigerEvent1;
    }

    public void setTrrigerEvent1(String trrigerEvent1) {
        this.trrigerEvent1 = trrigerEvent1;
    }

    public String getTrrigerEvent2() {
        return trrigerEvent2;
    }

    public void setTrrigerEvent2(String trrigerEvent2) {
        this.trrigerEvent2 = trrigerEvent2;
    }

    public String getTrrigerEvent3() {
        return trrigerEvent3;
    }

    public void setTrrigerEvent3(String trrigerEvent3) {
        this.trrigerEvent3 = trrigerEvent3;
    }

	public String getTrrigerEvent4() {
		return trrigerEvent4;
	}

	public void setTrrigerEvent4(String trrigerEvent4) {
		this.trrigerEvent4 = trrigerEvent4;
	}

}
