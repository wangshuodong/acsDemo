package com.cmiot.rms.dao.model;

public class InformInfo extends BaseBean {
    private String informId;

    private Integer informState;

    private Integer informCreateTime;

    private Integer informModifyTime;

    private String informContent;

    public String getInformId() {
        return informId;
    }

    public void setInformId(String informId) {
        this.informId = informId == null ? null : informId.trim();
    }

    public Integer getInformState() {
        return informState;
    }

    public void setInformState(Integer informState) {
        this.informState = informState;
    }

    public Integer getInformCreateTime() {
        return informCreateTime;
    }

    public void setInformCreateTime(Integer informCreateTime) {
        this.informCreateTime = informCreateTime;
    }

    public Integer getInformModifyTime() {
        return informModifyTime;
    }

    public void setInformModifyTime(Integer informModifyTime) {
        this.informModifyTime = informModifyTime;
    }

    public String getInformContent() {
        return informContent;
    }

    public void setInformContent(String informContent) {
        this.informContent = informContent == null ? null : informContent.trim();
    }
}