package com.cmiot.rms.dao.model;

public class Area extends BaseBean{
    private Integer areaTableId;

    private String areaId;

    private String areaName;

    private String areaType;

    private String areaParentAreaId;

    public Integer getAreaTableId() {
        return areaTableId;
    }

    public void setAreaTableId(Integer areaTableId) {
        this.areaTableId = areaTableId;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId == null ? null : areaId.trim();
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName == null ? null : areaName.trim();
    }

    public String getAreaType() {
        return areaType;
    }

    public void setAreaType(String areaType) {
        this.areaType = areaType == null ? null : areaType.trim();
    }

    public String getAreaParentAreaId() {
        return areaParentAreaId;
    }

    public void setAreaParentAreaId(String areaParentAreaId) {
        this.areaParentAreaId = areaParentAreaId == null ? null : areaParentAreaId.trim();
    }
}