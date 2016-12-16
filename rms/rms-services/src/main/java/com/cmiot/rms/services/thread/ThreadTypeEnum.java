package com.cmiot.rms.services.thread;

/**
 * Created by panmingguo on 2016/7/7.
 */
public enum ThreadTypeEnum {
    EXECUTE_UPGREAD_TASK(1, "执行升级任务"),
    EXECUTE_BACKUP_TASK(2, "执行备份任务"),
    EXECUTE_FLOWRATE_TASK(3, "执行流量任务"),
    EXECUTE_UPGREAD_DETAIL_TASK(4, "执行升级详情任务"),
    EXECUTE_BACKUP_DETAIL_TASK(5, "执行备份详情任务"),
    EXECUTE_NEW_INSTALLATION_PARAM_SET_TASK(6, "新装机网关参数设置任务"),
    ;

    private Integer type;

    private String message;

    ThreadTypeEnum(Integer type, String message)
    {
        this.type = type;
        this.message = message;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
