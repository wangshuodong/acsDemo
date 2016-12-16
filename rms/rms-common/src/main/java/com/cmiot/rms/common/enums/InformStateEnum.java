package com.cmiot.rms.common.enums;

/**
 * 上报处理状态
 * Created by wangzhen on 2016/2/2.
 */
public enum InformStateEnum {

    INFORM_STATE_0("0", "成功"),
    INFORM_STATE_1("1", "SN号不存在"),;

    /* 编码 */
    private String code;

    /* 描述 */
    private String description;

    InformStateEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // 普通方法
    public static String getDescription(String code) {
        for (InformStateEnum ce : InformStateEnum.values()) {
            if (ce.getCode() == code) {
                return ce.getDescription();
            }
        }
        return null;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
