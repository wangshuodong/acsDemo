package com.cmiot.rms.dao.model;

public class FirmwarePrepared extends BaseBean {
    private String firmwareId;

    private String firmwarePreviousId;

    private Boolean needForceUpgrade;

    public String getFirmwareId() {
        return firmwareId;
    }

    public void setFirmwareId(String firmwareId) {
        this.firmwareId = firmwareId == null ? null : firmwareId.trim();
    }

    public String getFirmwarePreviousId() {
        return firmwarePreviousId;
    }

    public void setFirmwarePreviousId(String firmwarePreviousId) {
        this.firmwarePreviousId = firmwarePreviousId == null ? null : firmwarePreviousId.trim();
    }

    public Boolean getNeedForceUpgrade() {
        return needForceUpgrade;
    }

    public void setNeedForceUpgrade(Boolean needForceUpgrade) {
        this.needForceUpgrade = needForceUpgrade;
    }
}