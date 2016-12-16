package com.cmiot.rms.dao.model;

public class InstructionsInfo extends BaseBean {
    private String instructionsId;

    private String instructionsBeforeClassname;

    private String instructionsAfterClassname;

    private Integer instructionsState;

    private Integer instructionsRoleCreateTime;

    private Integer instructionsRoleModifyTime;

    public String getInstructionsId() {
        return instructionsId;
    }

    public void setInstructionsId(String instructionsId) {
        this.instructionsId = instructionsId == null ? null : instructionsId.trim();
    }

    public String getInstructionsBeforeClassname() {
        return instructionsBeforeClassname;
    }

    public void setInstructionsBeforeClassname(String instructionsBeforeClassname) {
        this.instructionsBeforeClassname = instructionsBeforeClassname == null ? null : instructionsBeforeClassname.trim();
    }

    public String getInstructionsAfterClassname() {
        return instructionsAfterClassname;
    }

    public void setInstructionsAfterClassname(String instructionsAfterClassname) {
        this.instructionsAfterClassname = instructionsAfterClassname == null ? null : instructionsAfterClassname.trim();
    }

    public Integer getInstructionsState() {
        return instructionsState;
    }

    public void setInstructionsState(Integer instructionsState) {
        this.instructionsState = instructionsState;
    }

    public Integer getInstructionsRoleCreateTime() {
        return instructionsRoleCreateTime;
    }

    public void setInstructionsRoleCreateTime(Integer instructionsRoleCreateTime) {
        this.instructionsRoleCreateTime = instructionsRoleCreateTime;
    }

    public Integer getInstructionsRoleModifyTime() {
        return instructionsRoleModifyTime;
    }

    public void setInstructionsRoleModifyTime(Integer instructionsRoleModifyTime) {
        this.instructionsRoleModifyTime = instructionsRoleModifyTime;
    }
}