package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.InformInfo;

public interface InformInfoMapper {
    int deleteByPrimaryKey(String informId);

    int insert(InformInfo record);

    int insertSelective(InformInfo record);

    InformInfo selectByPrimaryKey(String informId);

    int updateByPrimaryKeySelective(InformInfo record);

    int updateByPrimaryKeyWithBLOBs(InformInfo record);

    int updateByPrimaryKey(InformInfo record);
}