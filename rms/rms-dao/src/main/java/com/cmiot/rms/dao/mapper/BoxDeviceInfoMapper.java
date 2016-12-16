package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.BoxDeviceInfo;

public interface BoxDeviceInfoMapper {
    int deleteByPrimaryKey(String id);

    int insert(BoxDeviceInfo record);

    int insertSelective(BoxDeviceInfo record);

    Map<String, Object> selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BoxDeviceInfo record);
    
    BoxDeviceInfo selectByParimaryKeyToMode(String id);
    
    List<BoxDeviceInfo> selectByParimaryMode(BoxDeviceInfo record);

    int updateByPrimaryKey(BoxDeviceInfo record);

	List<Map<String, Object>> selectBoxDeviceInfo(BoxDeviceInfo deviceInfo);
}