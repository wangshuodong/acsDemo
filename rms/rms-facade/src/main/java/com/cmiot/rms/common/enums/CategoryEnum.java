package com.cmiot.rms.common.enums;

/**
 * 类目枚举
 * Created by wangzhen on 2016/1/27.
 */
public enum CategoryEnum {

    USER_MANAGER_SERVICE("用户管理"),
    GATEWAY_MANAGER_SERVICE("网关管理"),
    UPGRADE_MANAGER_SERVICE( "升级管理"),
    LOG_MANAGER_SERVICE("日志管理"),
    PLUGIN_MANAGER_SERVICE("插件管理"),
    CONSUMER_MANAGER_SERVICE("客户管理"),
    BOX_MANAGER_SERVICE("机顶盒管理"),
    WORKORDER_MANAGER_SERVICE("工单管理"),
    FACTORYCODE_MANAGER_SERVICE("厂商维护"),
    SYSTEMCONFIG_MANAGER_SERVICE("系统配置"),
    ;

    /* 描述 */
    private String description;

    CategoryEnum(String description) {
        this.description = description;
    }


    public String description() {
        return description;
    }


    // 普通方法
    public static String getDescription(String description) {
        for (CategoryEnum ce : CategoryEnum.values()) {
            if (ce.description().equals(description)) {
                return ce.name()+"";
            }
        }
        return null;
    }
}
