package com.cmiot.rms.common.enums;

/**
 * Notification 码
 * Created by huqiao on 2016/06/09.
 */
public enum NotificationEnum {


    VALUE_0("0", "通知关闭"),
    VALUE_1("1", "被动的通知"),
    VALUE_2("2", "活动通知"),
    VALUE_3("3", "被动轻量级的通知"),
    VALUE_4("4", "被动轻量级通知被动的通知"),
    VALUE_5("5", "主动轻量级通知"),
    VALUE_6("6", "主动与被动轻巧的通知")
    ;

    /* 编码 */
    private String code;

    /* 描述 */
    private String description;

    NotificationEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // 普通方法
    public static String getDescription(String code) {
        for (NotificationEnum ce : NotificationEnum.values()) {
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
