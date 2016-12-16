package com.cmiot.rms.common.enums;

/**
 * 重启、恢复出厂操作状态码
 * Created by wangzhen on 2016/4/28.
 */
public enum RebootEnum {


    STATUS_0("0", "重启发送中"),
    STATUS_1("1", "恢复出厂发送中"),
    STATUS_2("2", "操作完成"),
    ;

    /* 编码 */
    private String code;

    /* 描述 */
    private String description;

    RebootEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // 普通方法
    public static String getDescription(String code) {
        for (RebootEnum ce : RebootEnum.values()) {
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
