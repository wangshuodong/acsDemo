package com.cmiot.rms.services.impl;

import com.cmiot.rms.common.page.PageBean;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.model.HardwareAblity;
import com.cmiot.rms.dao.mapper.HardwareAblityMapper;
import com.cmiot.rms.services.HardwareAblityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by fuwanhong on 2016/1/25.
 */
@Service("hardwareAblityService")
public class HardwareAblityServiceImpl implements HardwareAblityService {

	@Autowired
	private HardwareAblityMapper hardwareAblityMapper;


	@Override
	public List<HardwareAblity> queryList() {
		return hardwareAblityMapper.queryList();
	}

 
	@Override
	public  PageBean<HardwareAblity> queryList4Page(PageBean<HardwareAblity> page){
		return hardwareAblityMapper.queryList4Page(page);
	}

	@Override
	public void addHardwareAblity(HardwareAblity hardwareAblity) {

		hardwareAblity.setHardwareAblityUuid(UniqueUtil.uuid());
		hardwareAblityMapper.insertSelective(hardwareAblity);
	}

	@Override
	public void updateHardwareAblity(HardwareAblity hardwareAblity) {
		hardwareAblityMapper.updateByPrimaryKey(hardwareAblity);
	}

	@Override
	public void updateSelectHardwareAblity(HardwareAblity hardwareAblity) {
		hardwareAblityMapper.updateByPrimaryKeySelective(hardwareAblity);
	}

	@Override
	public void delHardwareAblity(String hardwareAblityUuid) {
		hardwareAblityMapper.deleteByPrimaryKey(hardwareAblityUuid);
	}
	
	@Override
	public  HardwareAblity  selectByUuid(String hardwareAblityUuid) {
		return hardwareAblityMapper.selectByPrimaryKey(hardwareAblityUuid);
	}

	@Override
	public  HardwareAblity  selectByGatewayInfoUuid(String gatewayInfoUuid) {
		return hardwareAblityMapper.selectByDeviceUuid(gatewayInfoUuid);
	}


	@Override
	public void batchInsertHardwareAblity(
			List<HardwareAblity> allHardwareAblityDatas) {
		
		hardwareAblityMapper.batchInsertHardwareAblity(allHardwareAblityDatas);
	}

 
}
