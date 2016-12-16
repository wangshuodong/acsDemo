package com.cmiot.rms.common.exception;

/**
 * 逻辑异常类
 * Created by wangzhen on 2016/3/7.
 */
public class LogicException extends RuntimeException {

    /**
     * 异常描述
     */
    private String message;

    public LogicException(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
