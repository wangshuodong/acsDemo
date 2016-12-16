package com.cmiot.rms.services;

import java.util.Map;

/**
 * box业务模板接口
 * 
 * @author zhangchuan
 *
 */
public interface BoxWorkOrderManagerService {
	/**
	 * 导入业务模板
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> importBoxWorkOrderTemplate(Map<String, Object> parameter);

	/**
	 * 查询模板列表
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> queryBoxWorkOrderList(Map<String, Object> parameter);

	/**
	 * 分页查询模板列表
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> queryBoxWorkOrderList4Page(Map<String, Object> parameter);

	/**
	 * 2.1.81. 工单管理—删除模板
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> deleteBoxWorkOrderTemplate(Map<String, Object> parameter);

	/**
	 * 2.1.82. 工单管理—修改业务模板
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> updateBoxWorkOrderTemplate(Map<String, Object> parameter);

	/**
	 * 
	 * @param parameter
	 * @return
	 */
	public Map<String, Object> delBoxWorkOrder(Map<String, Object> parameter);

	public Map<String, Object> updateBoxWorkOrder(Map<String, Object> parameter);

	public Map<String, Object> queryBoxWorkOrder(Map<String, Object> parameter);

	public Map<String, Object> queryBoxWorkOrderDetail(Map<String, Object> parameter);

}
