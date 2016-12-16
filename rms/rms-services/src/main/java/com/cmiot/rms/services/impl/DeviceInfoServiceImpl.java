package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.LogMarkEnum;
import com.cmiot.rms.common.exception.LogicException;
import com.cmiot.rms.common.logback.LogBackRecord;
import com.cmiot.rms.common.page.PageBean;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.model.DeviceInfo;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.mapper.DeviceInfoMapper;
import com.cmiot.rms.services.DeviceInfoService;
import com.cmiot.rms.services.GatewayInfoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuwanhong on 2016/1/25.
 */
@Service("deviceInfoService")
public class DeviceInfoServiceImpl implements DeviceInfoService {
    private static Logger logger = LoggerFactory.getLogger(DeviceInfoServiceImpl.class);

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Autowired
    GatewayInfoService gatewayInfoService;

    private void logBackRecord(DeviceInfo device, String url, String description) {
//        LogBackRecord.logBackBean(LogMarkEnum.LOG_MARK_OPERATION.code(), JSON.toJSONString(device), device.getId(),
//                device.getUserName(), CategoryEnum.CATEGORY_DEVICE_MANAGER.code(), "v1.0", description, url, device.getRoleName(), CategoryEnum.CATEGORY_ACCOUNT_MANAGER.description());
    }

    @Override
    public List<DeviceInfo> queryList(DeviceInfo record) {
        return deviceInfoMapper.queryList(record);
    }

    @Override
    public PageBean<DeviceInfo> queryList4Page(PageBean<DeviceInfo> page) {
        return deviceInfoMapper.queryList4Page(page);
    }
    
    public PageBean<DeviceInfo> queryDeviceInfoList4Page(PageBean<DeviceInfo> page) {
        return (PageBean<DeviceInfo>) deviceInfoMapper.queryDeviceInfoList4Page(page);
    }
    


    @Override
    public List<DeviceInfo> searchList(DeviceInfo searchObj) {
        return deviceInfoMapper.searchList(searchObj.getDeviceFactory());
    }

    @Override
    public void addDeviceInfo(DeviceInfo deviceInfo) {
        logger.info("操作用户：" + deviceInfo.getUserName() + ",操作方法：" + Thread.currentThread().getStackTrace()[1].getMethodName());
        deviceInfo.setId(UniqueUtil.uuid());
        deviceInfoMapper.insert(deviceInfo);
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName() + "添加设备成功!详情：" + JSON.toJSONString(deviceInfo));
        try {
            this.logBackRecord(deviceInfo, "/system/device/add", "添加设备");
        } catch (Exception e) {

        }
    }

    @Override
    public void updateDeviceInfo(DeviceInfo deviceInfo) {
        logger.info("操作用户：" + deviceInfo.getUserName() + ",操作方法：" + Thread.currentThread().getStackTrace()[1].getMethodName());
        deviceInfoMapper.updateByPrimaryKey(deviceInfo);
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName() + "成功!详情：" + JSON.toJSONString(deviceInfo));
        try {
            this.logBackRecord(deviceInfo, "/system/device/update", "修改设备");
        } catch (Exception e) {

        }
    }

    @Override
    public void updateSelectDeviceInfo(DeviceInfo deviceInfo) {
        logger.info("操作用户：" + deviceInfo.getUserName() + ",操作方法：" + Thread.currentThread().getStackTrace()[1].getMethodName());
        deviceInfoMapper.updateByPrimaryKeySelective(deviceInfo);
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName() + "成功!详情：" + JSON.toJSONString(deviceInfo));
        try {
            this.logBackRecord(deviceInfo, "/system/device/update", "修改设备");
        } catch (Exception e) {

        }
    }

    @Override
    public void delDeviceInfo(String deviceInfoUuid) {
//        logger.info("操作用户：" + user.getAdminAccount() + ",操作方法：" + Thread.currentThread().getStackTrace()[1].getMethodName());

        //检查此设备类型是否已经被使用
        GatewayInfo gatewayInfoToSearch = new GatewayInfo();
        gatewayInfoToSearch.setGatewayDeviceUuid(deviceInfoUuid);
        ArrayList<GatewayInfo> gatewayInfoList= (ArrayList<GatewayInfo>) gatewayInfoService.queryList(gatewayInfoToSearch);

        if (gatewayInfoList.size()<1) {
            deviceInfoMapper.deleteByPrimaryKey(deviceInfoUuid);
            logger.info(Thread.currentThread().getStackTrace()[1].getMethodName() + "成功!详情：" + JSON.toJSONString(deviceInfoUuid));
            try {
                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.setId(deviceInfoUuid);
                this.logBackRecord(deviceInfo, "/system/device/delete", "删除设备");
            } catch (Exception e) {
            }
        } else {
            logger.error("此设备类型 已经被使用  不能删除 uuid "+ deviceInfoUuid);
            throw new LogicException("此设备类型已经被使用 ,不能删除!");
        }
    }

    @Override
    public DeviceInfo selectByUuid(String deviceInfoUuid) {
        return deviceInfoMapper.selectByPrimaryKey(deviceInfoUuid);
    }

    /**
     * 功能:根据设备厂商查询设备型号
     */
    @Override
    public List<DeviceInfo> selectByFactory(String deviceInfoFactory) {
        return deviceInfoMapper.selectByFactory(deviceInfoFactory);
    }


	public List<DeviceInfo> selectDeviceModel(DeviceInfo deviceInfo) {
		return deviceInfoMapper.searchDeviceModel(deviceInfo);
	}

}
