package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.LogAutoBak;
import com.cmiot.rms.dao.model.LogAutoBakExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LogAutoBakMapper {
    int countByExample(LogAutoBakExample example);

    int deleteByExample(LogAutoBakExample example);

    int deleteByPrimaryKey(String id);

    int insert(LogAutoBak record);

    int insertSelective(LogAutoBak record);

    List<LogAutoBak> selectByExample(LogAutoBakExample example);

    LogAutoBak selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") LogAutoBak record, @Param("example") LogAutoBakExample example);

    int updateByExample(@Param("record") LogAutoBak record, @Param("example") LogAutoBakExample example);

    int updateByPrimaryKeySelective(LogAutoBak record);

    int updateByPrimaryKey(LogAutoBak record);
}