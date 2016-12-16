/**
 * 
 */
package com.cmiot.rms.services;

import java.util.Map;

/**
 * @author heping
 * 工单管理接口，增删改查
 */
public interface BoxBusinessManagerService {
	
	public Map<String,Object> addBoxBusiness(Map<String, Object>parameter); 

	public Map<String,Object> delBoxBusiness(Map<String, Object>parameter);
	
	public Map<String,Object> updateBoxBusiness(Map<String, Object>parameter);
	
	public Map<String,Object> queryBoxBusiness(Map<String, Object>parameter);
	
	public Map<String,Object> queryBoxBusinessDetail(Map<String, Object>parameter); 
}
