package com.cmiot.rms.services.thread.backuptask;

import com.cmiot.rms.dao.mapper.GatewayBackupFileTaskAreaMapper;
import com.cmiot.rms.dao.mapper.GatewayBackupFileTaskDetailMapper;
import com.cmiot.rms.dao.mapper.GatewayBackupFileTaskMapper;
import com.cmiot.rms.dao.model.GatewayBackupFileTask;
import com.cmiot.rms.services.GatewayBackupFileTaskInnerService;

import java.util.List;

/**
 * Created by panmingguo on 2016/7/7.
 */
public class BackupTaskParameter {
    private GatewayBackupFileTaskMapper gatewayBackupFileTaskMapper;
    private GatewayBackupFileTaskDetailMapper gatewayBackupFileTaskDetailMapper;
    private GatewayBackupFileTaskInnerService gatewayBackupFileTaskInnerService;
    private GatewayBackupFileTask task;
    private GatewayBackupFileTaskAreaMapper gatewayBackupFileTaskAreaMapper;
    private int startPage;
    private int gatewayNumber;
    private List<Integer> areas;


    public GatewayBackupFileTaskMapper getGatewayBackupFileTaskMapper() {
        return gatewayBackupFileTaskMapper;
    }

    public void setGatewayBackupFileTaskMapper(GatewayBackupFileTaskMapper gatewayBackupFileTaskMapper) {
        this.gatewayBackupFileTaskMapper = gatewayBackupFileTaskMapper;
    }

    public GatewayBackupFileTaskDetailMapper getGatewayBackupFileTaskDetailMapper() {
        return gatewayBackupFileTaskDetailMapper;
    }

    public void setGatewayBackupFileTaskDetailMapper(GatewayBackupFileTaskDetailMapper gatewayBackupFileTaskDetailMapper) {
        this.gatewayBackupFileTaskDetailMapper = gatewayBackupFileTaskDetailMapper;
    }

    public GatewayBackupFileTaskInnerService getGatewayBackupFileTaskInnerService() {
        return gatewayBackupFileTaskInnerService;
    }

    public void setGatewayBackupFileTaskInnerService(GatewayBackupFileTaskInnerService gatewayBackupFileTaskInnerService) {
        this.gatewayBackupFileTaskInnerService = gatewayBackupFileTaskInnerService;
    }

    public GatewayBackupFileTask getTask() {
        return task;
    }

    public void setTask(GatewayBackupFileTask task) {
        this.task = task;
    }

    public GatewayBackupFileTaskAreaMapper getGatewayBackupFileTaskAreaMapper() {
        return gatewayBackupFileTaskAreaMapper;
    }

    public void setGatewayBackupFileTaskAreaMapper(GatewayBackupFileTaskAreaMapper gatewayBackupFileTaskAreaMapper) {
        this.gatewayBackupFileTaskAreaMapper = gatewayBackupFileTaskAreaMapper;
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

    public List<Integer> getAreas() {
        return areas;
    }

    public void setAreas(List<Integer> areas) {
        this.areas = areas;
    }
}
