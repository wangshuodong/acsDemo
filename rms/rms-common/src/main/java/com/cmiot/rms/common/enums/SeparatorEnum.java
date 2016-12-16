package com.cmiot.rms.common.enums;

/**
 * 分隔符枚举
 * Created by wangzhen on 2016/1/28.
 */
public enum SeparatorEnum {

    SEPARATOR_ARR1("@##@"),
    SEPARATOR_ARR2("%##%"),
    ;

    /* 编码 */
    private String code;


    SeparatorEnum(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

}
