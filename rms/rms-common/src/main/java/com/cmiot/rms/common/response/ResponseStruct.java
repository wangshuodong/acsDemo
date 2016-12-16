package com.cmiot.rms.common.response;

/**
 * 返回接口数据
 * Created by wangzhen on 2016/4/11.
 */
public class ResponseStruct<T> {
    /**
     * 返回码
     */
    private String code;
    /**
     * 返回业务数据
     */
    private T data;
    /**
     * 错误信息
     */
    private String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
