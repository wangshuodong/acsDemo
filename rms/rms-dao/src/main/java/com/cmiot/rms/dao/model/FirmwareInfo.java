package com.cmiot.rms.dao.model;

public class FirmwareInfo extends BaseBean {
    private String id;//固件编号UUID

    private String firmwareName;//固件名称

    private String firmwareVersion;//固件版本号

    private String firmwarePath;//固件文件路径

    private Double firmwareSize;//文件大小BYTES

    private Integer firmwareCreateTime;//创建时间

    private String firmwareDescription;//描述

    private String deviceModel;//设备型号

    private String deviceId;//设备UUID
    
    private Integer checkStatus;//审核状态1:未审核;2:已审核(默认状态为1)
    
    private String uploadMd5;//上传固件文件返回MD5
    
    private String inputMd5;//用户输入的固件文件MD5

    private String areaId;//区域ID

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getFirmwareName() {
        return firmwareName;
    }

    public void setFirmwareName(String firmwareName) {
        this.firmwareName = firmwareName == null ? null : firmwareName.trim();
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion == null ? null : firmwareVersion.trim();
    }

    public String getFirmwarePath() {
        return firmwarePath;
    }

    public void setFirmwarePath(String firmwarePath) {
        this.firmwarePath = firmwarePath == null ? null : firmwarePath.trim();
    }

    public Double getFirmwareSize() {
        return firmwareSize;
    }

    public void setFirmwareSize(Double firmwareSize) {
        this.firmwareSize = firmwareSize;
    }

    public Integer getFirmwareCreateTime() {
        return firmwareCreateTime;
    }

    public void setFirmwareCreateTime(Integer firmwareCreateTime) {
        this.firmwareCreateTime = firmwareCreateTime;
    }

    public String getFirmwareDescription() {
        return firmwareDescription;
    }

    public void setFirmwareDescription(String firmwareDescription) {
        this.firmwareDescription = firmwareDescription == null ? null : firmwareDescription.trim();
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel == null ? null : deviceModel.trim();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId == null ? null : deviceId.trim();
    }

	public Integer getCheckStatus() {
		return checkStatus;
	}

	public void setCheckStatus(Integer checkStatus) {
		this.checkStatus = checkStatus;
	}

	public String getUploadMd5() {
		return uploadMd5;
	}

	public void setUploadMd5(String uploadMd5) {
		this.uploadMd5 = uploadMd5;
	}

	public String getInputMd5() {
		return inputMd5;
	}

	public void setInputMd5(String inputMd5) {
		this.inputMd5 = inputMd5;
	}

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

}