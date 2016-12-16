package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.GatewayBackupFileTaskDetail;

public interface GatewayBackupFileTaskDetailMapper {
    int deleteByPrimaryKey(String id);

    int insert(GatewayBackupFileTaskDetail record);

    int insertSelective(GatewayBackupFileTaskDetail record);

    GatewayBackupFileTaskDetail selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(GatewayBackupFileTaskDetail record);

    int updateByPrimaryKey(GatewayBackupFileTaskDetail record);

    int updateByFileId(GatewayBackupFileTaskDetail record);

    int selectCountById(GatewayBackupFileTaskDetail record);
}