package com.cmiot.rms.common.enums;

/**
 * Created by wangzhen on 2016/1/27.
 */
public enum LogMarkEnum {

    LOG_MARK_OPERATION("1", "操作日志"),
    LOG_MARK_OUTER_PORT("2", "调用外部接口日志"),
    LOG_MARK_PORT("3", "接口被调用日志"),
    ;

    /* 编码 */
    private String code;

    /* 描述 */
    private String description;

    LogMarkEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }
}
