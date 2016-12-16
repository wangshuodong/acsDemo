package com.cmiot.rms.common.enums;

/**
 * 响应码
 * Created by wangzhen on 2016/2/2.
 */
public enum RespCodeEnum {
    /** 通用响应码：统一以【10】开头 */
    RC_1000("1000", "成功"),
    RC_1001("1001", "服务器内部错误"),
    RC_1002("1002", "消息格式错误"),
    RC_1003("1003", "方法名为空"),
    RC_1004("1004", "非法请求"),
    RC_0("0", "成功"),
    RC_1("-1", "逻辑拒绝错误"),
    RC_2("-2", "当前网关正在被操作,请稍后再请求"),
    RC_ERROR("1", "异常错误")
    ;


    /** 编码 */
    private String code;

    /** 描述 */
    private String description;

    RespCodeEnum(String code, String description){
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
