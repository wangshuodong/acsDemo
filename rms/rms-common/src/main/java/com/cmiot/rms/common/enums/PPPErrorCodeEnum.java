package com.cmiot.rms.common.enums;

/**
 * 重启、恢复出厂操作状态码
 * Created by wangzhen on 2016/4/28.
 */
public enum PPPErrorCodeEnum {

    ERROR_NO_VALID_CONNECTION("ERROR_NO_VALID_CONNECTION", "无有效的PPP连接"),

    ERROR_NONE("ERROR_NONE", "无错误"),
    ERROR_ISP_TIME_OUT("ERROR_ISP_TIME_OUT", "ISP超时"),

    ERROR_COMMAND_ABORTED("ERROR_COMMAND_ABORTED", "拨号退出"),
    ERROR_NOT_ENABLED_FOR_INTERNET("ERROR_NOT_ENABLED_FOR_INTERNET", "未启用INTERNET"),
    ERROR_BAD_PHONE_NUMBER ("ERROR_BAD_PHONE_NUMBER", "电话号码错误"),
    ERROR_USER_DISCONNECT("ERROR_USER_DISCONNECT", "用户断开连接"),
    ERROR_ISP_DISCONNECT("ERROR_ISP_DISCONNECT", "ISP 中断连接"),

    ERROR_IDLE_DISCONNECT("ERROR_IDLE_DISCONNECT", "空闲断开连接"),
    ERROR_FORCED_DISCONNECT("ERROR_FORCED_DISCONNECT", "强制断开连接"),
    ERROR_SERVER_OUT_OF_RESOURCES("ERROR_SERVER_OUT_OF_RESOURCES", "服务器资源耗尽"),
    ERROR_RESTRICTED_LOGON_HOURS("ERROR_RESTRICTED_LOGON_HOURS", "限制登录期内"),
    ERROR_ACCOUNT_DISABLED("ERROR_ACCOUNT_DISABLED", "账户被停用"),

    ERRPR_ACCOUNT_EXPIRED("ERRPR_ACCOUNT_EXPIRED", "账户过期"),
    ERROR_PASSWORD_EXPIRED("ERROR_PASSWORD_EXPIRED", "密码过期"),
    ERROR_AUTHENTICATION_FAULURE("ERROR_AUTHENTICATION_FAULURE", "鉴权失败"),
    ERROR_NO_DIALTONE("ERROR_NO_DIALTONE", "没有拨号音"),
    ERROR_NO_CARRIER("ERROR_NO_CARRIER", "没有载波"),

    ERROR_NO_ANSWER("ERROR_NO_ANSWER", "没有应答"),
    ERROR_LINE_BUSY("ERROR_LINE_BUSY", "线路忙"),
    ERROR_UNSUPPORTED_BITSPERSECOND("ERROR_UNSUPPORTED_BITSPERSECOND", "速率不支持"),
    ERROR_TOO_MANY_LINE_ERRORS("ERROR_TOO_MANY_LINE_ERRORS", "线路错误过多"),
    ERROR_IP_CONFIGURATION("ERROR_IP_CONFIGURATION", "IP配置错误"),

    ERROR_UNKNOW("ERROR_UNKNOW", "未知错误"),
    ;

    /* 编码 */
    private String code;

    /* 描述 */
    private String description;

    PPPErrorCodeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // 普通方法
    public static String getDescription(String code) {
        for (PPPErrorCodeEnum ce : PPPErrorCodeEnum.values()) {
            if (ce.getCode().equals(code)) {
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
