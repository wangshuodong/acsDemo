package com.cmiot.rms.services;

import com.cmiot.rms.common.page.PageBean;
import com.cmiot.rms.dao.model.HardwareAblity;

import java.util.List;

/**
 * Created by fuwanhong on 2016/1/25.
 */
public interface HardwareAblityService {

    List<HardwareAblity> queryList();

    PageBean<HardwareAblity> queryList4Page(PageBean<HardwareAblity> page) ;
    
    void addHardwareAblity(HardwareAblity hardwareAblity);
    
    void updateHardwareAblity(HardwareAblity hardwareAblity);

    void updateSelectHardwareAblity(HardwareAblity hardwareAblity);
    
    void delHardwareAblity(String hardwareAblityUuid);

    HardwareAblity  selectByUuid(String hardwareAblityUuid);
 
	HardwareAblity  selectByGatewayInfoUuid(String gatewayInfoUuid);

	void batchInsertHardwareAblity(List<HardwareAblity> allHardwareAblityDatas);
}
