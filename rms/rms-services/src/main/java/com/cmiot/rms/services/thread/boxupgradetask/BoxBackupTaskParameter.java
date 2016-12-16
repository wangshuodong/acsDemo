package com.cmiot.rms.services.thread.boxupgradetask;

import com.cmiot.rms.dao.mapper.BoxFirmwareUpgradeTaskDetailMapper;
import com.cmiot.rms.dao.model.BoxFirmwareInfo;

import com.cmiot.rms.services.boxManager.instruction.BoxInvokeInsService;

import java.util.List;

/**
 * Created by panmingguo on 2016/9/5.
 */
public class BoxBackupTaskParameter {
    private BoxInvokeInsService boxInvokeInsService;
    private String userName;
    private String password;
    private List<String> boxIds;
    private List<String> detailIds;
    private BoxFirmwareInfo boxFirmwareInfo;
    private String taskId;
    private BoxFirmwareUpgradeTaskDetailMapper detailMapper;


    public BoxInvokeInsService getBoxInvokeInsService() {
        return boxInvokeInsService;
    }

    public void setBoxInvokeInsService(BoxInvokeInsService boxInvokeInsService) {
        this.boxInvokeInsService = boxInvokeInsService;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getBoxIds() {
        return boxIds;
    }

    public void setBoxIds(List<String> boxIds) {
        this.boxIds = boxIds;
    }

    public List<String> getDetailIds() {
        return detailIds;
    }

    public void setDetailIds(List<String> detailIds) {
        this.detailIds = detailIds;
    }

    public BoxFirmwareInfo getBoxFirmwareInfo() {
        return boxFirmwareInfo;
    }

    public void setBoxFirmwareInfo(BoxFirmwareInfo boxFirmwareInfo) {
        this.boxFirmwareInfo = boxFirmwareInfo;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public BoxFirmwareUpgradeTaskDetailMapper getDetailMapper() {
        return detailMapper;
    }

    public void setDetailMapper(BoxFirmwareUpgradeTaskDetailMapper detailMapper) {
        this.detailMapper = detailMapper;
    }
}
