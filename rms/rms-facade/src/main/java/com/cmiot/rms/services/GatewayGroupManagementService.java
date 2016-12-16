package com.cmiot.rms.services;

import java.util.Map;

public interface GatewayGroupManagementService {

	 Map<String, Object> addGroup(Map<String, Object> parameter);
	 
	 Map<String, Object> updateGroup(Map<String, Object> parameter);
	 
	 Map<String, Object> queryGroup(Map<String, Object> parameter);
	 
	 Map<String, Object> deleteGroup(Map<String, Object> parameter);

	Map<String, Object> queryAllGroup(Map<String, Object> parameter);
}
