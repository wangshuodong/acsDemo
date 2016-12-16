package com.cmiot.rms.common.enums;

/**
 * Created by panmingguo on 2016/4/21.
 */
public enum ErrorCodeEnum {
    SUCCESS(0, "成功"),
    PATAMETER_IS_NULL(10000, "参数为空"),
    PATAMETER_ERROR(10001, "参数错误"),
    INSERT_ERROR(10002, "插入错误"),
    UPDATE_ERROR(10003, "更新错误"),
    DELETE_ERROR(10004, "删除错误"),
    SEARCH_ERROR(10005, "查询错误"),
    OUISN_CHECK_FAILED(10006, "OUI-SN验证失败"),
    PASSWORD_CHECK_FAILED(10007, "PASSWORD验证失败"),
    SET_PARAMETR_VALUE_FAILED(10008, "设置失败"),
    EXCEPTION_ERROR(10009, "系统异常,请重新登录再试"),
    OPERATION_ERROR(10010, "操作失败"),
    DEL_RELATION_EXIST_ERROR(10011, "当前数据被使用不能删除"),
    FILE_UPLOAD_ERROR(20001, "文件上传错误"),
    FIRMWARE_IS_USING(20002, "该固件正在被使用，不能删除"),
    DEVICE_NOT_EXIST(20003, "设备不存在"),
    INSTRUCT_SEND_FAILED(20004, "指令发送失败"),
    INSTRUCT_SEND_EXCEPTION(20005, "指令发送异常"),
    FIRMWARE_NOT_EXIST(20006, "固件不存在"),
    FIRMWARE_EXIST(20007, "已存在该固件"),
    NAME_EXIST(20008, "已存在该名称"),
    CODE_EXIST(20009, "已存在该编码CODE"),
    EXCEED_MAX_NUMBER(30001, "超过备份配置文件数量限制，请手动删除配置文件后再次备份!"),
    BACKUP_FILE_NOT_EXIST(30002, "备份文件不存在"),
    DELETE_FILE_FAILED(30003, "删除备份文件失败!"),
    TASK_IS_RUNNING(30004, "任务已经开始，不能更新！"),
    GATEWAY_NOT_EXIST(30005, "网关不存在"),
    BACKUPTASK_EXIST(30006, "已存在相同条件的备份任务！"),
    BACKUPTASK_EXIST_SAME_NAME(30007, "已存在相同名称的备份任务！"),
    FILE_BACKUP_NO_COMPLETE(30008, "文件未备份完成！"),
    FILE_BACKUP_NO_SUCCESS(30009, "文件未备份成功！"),
    FILE_BACKUPING(30010, "正在备份中，请稍后重试!"),
    NO_REGISTER(30011, "网关未注册!"),
    ;


    private int resultCode;

    private String resultMsg;

    ErrorCodeEnum(int resultCode, String resultMsg)
    {
        this.resultCode = resultCode;
        this.resultMsg = resultMsg;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }
}
