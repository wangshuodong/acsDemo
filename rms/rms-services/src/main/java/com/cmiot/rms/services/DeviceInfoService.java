package com.cmiot.rms.services;

import com.cmiot.rms.common.page.PageBean;
import com.cmiot.rms.dao.model.DeviceInfo;

import java.util.List;

/**
 * Created by fuwanhong on 2016/1/25.
 */
public interface DeviceInfoService {

	List<DeviceInfo> queryList(DeviceInfo record);

	PageBean<DeviceInfo> queryList4Page(PageBean<DeviceInfo> page) ;
	
	PageBean<DeviceInfo> queryDeviceInfoList4Page(PageBean<DeviceInfo> page) ;
	
	List<DeviceInfo> searchList(DeviceInfo searchObj);

	void addDeviceInfo(DeviceInfo deviceInfo);

	void updateDeviceInfo(DeviceInfo deviceInfo);

	void delDeviceInfo(String deviceInfoUuid);

	DeviceInfo  selectByUuid(String deviceInfoUuid);
	
	List<DeviceInfo>  selectByFactory(String deviceInfoFactory);

	void updateSelectDeviceInfo(DeviceInfo deviceInfo);
	
	List<DeviceInfo>  selectDeviceModel(DeviceInfo deviceInfo);
	
}
