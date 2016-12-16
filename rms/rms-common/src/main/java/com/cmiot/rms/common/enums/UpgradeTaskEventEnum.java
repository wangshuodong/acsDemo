package com.cmiot.rms.common.enums;

/**
 * Created by panmingguo on 2016/5/18.
 */
public enum UpgradeTaskEventEnum {

    BOOTSTRAP(1, "第一次启动"),
    PERIODIC(2, "周期心跳上报"),
    BOOT(3, "重新启动"),
    VALUECHANGE(4, "参数改变"),;


    /** 编码 */
    private int code;

    /** 描述 */
    private String description;

    UpgradeTaskEventEnum(int code, String description){
        this.code = code;
        this.description = description;
    }

    public int code() {
        return code;
    }

    public String description() {
        return description;
    }

    public static String getDescription(int code) {
        for (UpgradeTaskEventEnum ce : UpgradeTaskEventEnum.values()) {
            if (ce.code() == code) {
                return ce.description();
            }
        }
        return null;
    }
}
