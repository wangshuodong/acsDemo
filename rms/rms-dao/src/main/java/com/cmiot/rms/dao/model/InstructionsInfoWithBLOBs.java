package com.cmiot.rms.dao.model;

public class InstructionsInfoWithBLOBs extends InstructionsInfo {
    private String instructionsBeforeContent;

    private String instructionsAfterContent;

    private String cpeIdentity;//网关设备标示

    public String getInstructionsBeforeContent() {
        return instructionsBeforeContent;
    }

    public void setInstructionsBeforeContent(String instructionsBeforeContent) {
        this.instructionsBeforeContent = instructionsBeforeContent == null ? null : instructionsBeforeContent.trim();
    }

    public String getInstructionsAfterContent() {
        return instructionsAfterContent;
    }

    public void setInstructionsAfterContent(String instructionsAfterContent) {
        this.instructionsAfterContent = instructionsAfterContent == null ? null : instructionsAfterContent.trim();
    }

    public String getCpeIdentity() {
        return cpeIdentity;
    }

    public void setCpeIdentity(String cpeIdentity) {
        this.cpeIdentity = cpeIdentity;
    }
}