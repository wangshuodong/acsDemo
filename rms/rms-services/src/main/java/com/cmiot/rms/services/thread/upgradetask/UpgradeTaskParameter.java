package com.cmiot.rms.services.thread.upgradetask;

import com.cmiot.rms.dao.mapper.FirmwareInfoMapper;
import com.cmiot.rms.dao.mapper.FirmwareUpgradeTaskDetailMapper;
import com.cmiot.rms.dao.model.FirmwareUpgradeTask;
import com.cmiot.rms.services.instruction.InvokeInsService;

/**
 * Created by panmingguo on 2016/7/7.
 */
public class UpgradeTaskParameter {

    private FirmwareUpgradeTask firmwareUpgradeTask;
    private FirmwareUpgradeTaskDetailMapper detailMapper;
    private FirmwareInfoMapper firmwareInfoMapper;
    private InvokeInsService invokeInsService;
    private String userName;
    private String password;
    private int startPage;
    private int gatewayNumber;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public FirmwareInfoMapper getFirmwareInfoMapper() {
        return firmwareInfoMapper;
    }

    public void setFirmwareInfoMapper(FirmwareInfoMapper firmwareInfoMapper) {
        this.firmwareInfoMapper = firmwareInfoMapper;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public InvokeInsService getInvokeInsService() {
        return invokeInsService;
    }

    public void setInvokeInsService(InvokeInsService invokeInsService) {
        this.invokeInsService = invokeInsService;
    }

    public FirmwareUpgradeTaskDetailMapper getDetailMapper() {
        return detailMapper;
    }

    public void setDetailMapper(FirmwareUpgradeTaskDetailMapper detailMapper) {
        this.detailMapper = detailMapper;
    }

    public FirmwareUpgradeTask getFirmwareUpgradeTask() {
        return firmwareUpgradeTask;
    }

    public void setFirmwareUpgradeTask(FirmwareUpgradeTask firmwareUpgradeTask) {
        this.firmwareUpgradeTask = firmwareUpgradeTask;
    }

    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getGatewayNumber() {
        return gatewayNumber;
    }

    public void setGatewayNumber(int gatewayNumber) {
        this.gatewayNumber = gatewayNumber;
    }
}
