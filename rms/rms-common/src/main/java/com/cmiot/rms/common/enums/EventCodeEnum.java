package com.cmiot.rms.common.enums;

/**
 * 上报事件枚举
 * Created by wangzhen on 2016/2/18.
 */
public enum EventCodeEnum {

    EVENT_CODE_BOOTSTRAP("0 BOOTSTRAP", "智能网关首次向网管注册"),
    EVENT_CODE_BOOT("1 BOOT", "智能网关重启/开机"),
    EVENT_CODE_PERIODIC("2 PERIODIC", "心跳"),
    EVENT_CODE_VALUECHANGE("4 VALUE CHANGE", "智能网关参数改变"),
    EVENT_CODE_CONNECTIONREQUEST("6 CONNECTION REQUEST", "用于RMS发起会话后的 TR-069传输序列初始化"),
    EVENT_CODE_TRANSFERCOMPLETE("7 TRANSFER COMPLETE", "下载或上传文件完成（不论成功或失败） ，必 须 与 M Downlaod或 M Upload一起使用"),
    EVENT_CODE_DIAGNOSTICSCOMPLETE("8 DIAGNOSTICS COMPLETE", "诊断完成"),
    EVENT_CODE_M_REBOOT("M Reboot", "重启完成，与1 BOOT同时使用"),
    EVENT_CODE_M_DOWNLOAD("M Download", "先 前 RMS 使 用Download方法请求的内容下载已经完成"),
    EVENT_CODE_M_UPLOAD("M Upload", "先 前 RMS 使 用 Upload方法请求的内容上传已经完成"),
    EVENT_CODE_X_CMCC_BIND("X CMCC BIND", "用于智能网关基于设备认证Password认证首次连接RMS"),
    EVENT_CODE_X_CMCC_MONITOR("X CMCC MONITOR", "上报监控参数"),
    EVENT_CODE_M_X_CMCC_SHUTDOWN("M X CMCC SHUTDOWN", "关机"),
    ;

    /* 编码 */
    private String code;

    /* 描述 */
    private String description;

    EventCodeEnum(String code, String description) {
        this.code = code;
        this.description = description;
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

    // 获取枚举
    public static EventCodeEnum getEventCodeEnum(String code) {
        for (EventCodeEnum ec : EventCodeEnum.values()) {
            if (ec.getCode() == code) {
                return ec;
            }
        }
        return null;
    }

}
