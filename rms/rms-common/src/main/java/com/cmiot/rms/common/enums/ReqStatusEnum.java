package com.cmiot.rms.common.enums;

/**
 * 请求响应状态码
 * Created by wangzhen on 2016/2/3.
 */
public enum ReqStatusEnum {

    Req_Status_0("0", "参数的改动已经验证并应用"),
    Req_Status_1("1", "参数的改动已经验证并提交，只是尚未应用"),
    ;


    /** 编码 */
    private String code;

    /** 描述 */
    private String description;

    ReqStatusEnum(String code, String description){
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
