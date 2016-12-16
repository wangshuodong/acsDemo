package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.FirmwarePrepared;

import java.util.List;

public interface FirmwarePreparedMapper  extends BaseMapper<FirmwarePrepared> {
    int insert(FirmwarePrepared record);

    int insertSelective(FirmwarePrepared record);

    List<FirmwarePrepared> queryList(FirmwarePrepared firmwarePrepared);

    int deleteByFirmwareId(String firmwareId);

    int deleteByFirmwarePreId(String firmwarePreviousId);
}