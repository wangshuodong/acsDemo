package com.cmiot.rms.common.enums;

/**
 * 响应码
 * Created by hj on 2016/2/2.
 */
public enum InstructionsStatusEnum {
    /** 通用响应码：统一以【00】开头 */
    STATUS_0(0, "发送中"),
    STATUS_1(1, "成功"),
    STATUS_2(2, "失败"),
    STATUS_3(3, "超时"),
    ;


    /** 编码 */
    private Integer code;

    /** 描述 */
    private String description;

    InstructionsStatusEnum(Integer code, String description){
        this.code = code;
        this.description = description;
    }

    public Integer code() {
        return code;
    }

    public String description() {
        return description;
    }
}
