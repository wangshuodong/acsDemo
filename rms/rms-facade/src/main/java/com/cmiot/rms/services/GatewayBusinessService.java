/**
 * 
 */
package com.cmiot.rms.services;

import java.util.Map;

/**
 * @author heping
 *	工单管理接口
 *
 */
public interface GatewayBusinessService 
{
	/**
	 * 分页查询工单列表
	 * @param map
	 * @return
	 */
	public Map<String,Object> queryList4Page(Map<String,Object> map) ;
	
	/**
	 * 查询工单详情
	 * @param map
	 * @return
	 */
	public Map<String,Object> queryBusinessDetail(Map<String,Object> map) ;
}
