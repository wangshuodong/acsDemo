package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.GatewayAdslAccount;

public interface GatewayAdslAccountMapper  {

	int insert(GatewayAdslAccount account);

	GatewayAdslAccount selectByPrimaryKey(String id);

	long queryCount(Map<String, Object> map);
	
	List<GatewayAdslAccount> queryGatewayInfoList(GatewayAdslAccount account);




}
