package com.cmiot.rms.dao.model.derivedclass;

import com.cmiot.rms.dao.model.BoxInfo;

/**
 * 与数据库不相关的字段存储
 * Created by admin on 2016/6/8.
 */
public class BoxBean extends BoxInfo {

    /*所属区域*/
    private String areaName;

    /*固件版本信息*/
    private String boxVersion;

    public String getAreaName() {
        return areaName;
    }

    public String getBoxVersion() {
        return boxVersion;
    }

    public void setBoxVersion(String boxVersion) {
        this.boxVersion = boxVersion;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
