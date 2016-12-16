package com.cmiot.rms.common.enums;

/**
 * Created by panmingguo on 2016/9/6.
 */
public enum PbossDeviceType {

    HGU("hgu", "网关"),
    IHGU("ihgu", "智能网关"),
    STB("stb", "机顶盒"),;

    private String type;

    private String description;

    PbossDeviceType(String type, String description)
    {
        this.type = type;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }
}
