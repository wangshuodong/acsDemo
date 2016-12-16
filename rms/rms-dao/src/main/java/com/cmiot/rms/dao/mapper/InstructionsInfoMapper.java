package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.InstructionsInfo;
import com.cmiot.rms.dao.model.InstructionsInfoWithBLOBs;

public interface InstructionsInfoMapper {
    int deleteByPrimaryKey(String instructionsId);

    int insert(InstructionsInfoWithBLOBs record);

    int insertSelective(InstructionsInfoWithBLOBs record);

    InstructionsInfoWithBLOBs selectByPrimaryKey(String instructionsId);

    int updateByPrimaryKeySelective(InstructionsInfoWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(InstructionsInfoWithBLOBs record);

    int updateByPrimaryKey(InstructionsInfo record);

    InstructionsInfoWithBLOBs queryInstructions(InstructionsInfoWithBLOBs infoWithBLOBs);
}