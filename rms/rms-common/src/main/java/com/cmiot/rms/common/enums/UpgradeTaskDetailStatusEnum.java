package com.cmiot.rms.common.enums;

/**
 * 升级任务状态码
 * 
 */
public enum UpgradeTaskDetailStatusEnum {

    WAIT(0, "等待升级"),
    PROCESSING(1, "升级中"),
    FAILURE(2, "升级失败"),
    SUCSSESS(3, "升级成功"),;

    
    
    /** 编码 */
    private int code;

    /** 描述 */
    private String description;

    UpgradeTaskDetailStatusEnum(int code, String description){
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
