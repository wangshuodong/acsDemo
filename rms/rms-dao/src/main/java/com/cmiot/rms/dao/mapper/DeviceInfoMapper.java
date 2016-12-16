package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.common.page.PageBean;
import com.cmiot.rms.dao.model.DeviceInfo;
import com.cmiot.rms.dao.model.derivedclass.DeviceBean;

import java.util.List;


public interface DeviceInfoMapper  extends BaseMapper<DeviceInfo> {
    int deleteByPrimaryKey(String id);

    int insert(DeviceInfo record);

    int insertSelective(DeviceInfo record);

    DeviceInfo selectByPrimaryKey(String id);
    
    List<DeviceInfo> selectByFactory(String deviceInfoFactory);


    int updateByPrimaryKeySelective(DeviceInfo record);

    int updateByPrimaryKey(DeviceInfo record);
    
    List<DeviceInfo> queryList(DeviceInfo record);
    
    List<DeviceInfo> searchList(String deviceInfoFactory);
    
    List<DeviceInfo> searchDeviceModel(DeviceInfo record);

    List<DeviceInfo> searchByCodeAndModel(DeviceInfo record);

    List<DeviceInfo> searchAllDeviceModel();

    PageBean<?> queryDeviceInfoList4Page(PageBean<?> pagerBean);


    List<DeviceBean> queryDeviceInfoList(DeviceInfo deviceInfo);

    DeviceBean selectByDeviceId(String id);
}