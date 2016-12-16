package com.cmiot.rms.common.enums;

/**
 * 升级任务状态码
 * 
 */
public enum UpgradeTaskStatusEnum {

    NEW(0, "新加任务"),
    PROCESSING(1, "正在升级处理中"),
    PROCESSED(2, "升级任务结束"),;

    
    
    /** 编码 */
    private int code;

    /** 描述 */
    private String description;

    UpgradeTaskStatusEnum(int code, String description){
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }
}
