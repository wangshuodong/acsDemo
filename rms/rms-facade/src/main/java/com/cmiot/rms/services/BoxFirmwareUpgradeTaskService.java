package com.cmiot.rms.services;

import java.util.Map;

public interface BoxFirmwareUpgradeTaskService {

	/**
	 * 查询固件升级任务列表
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> searchUpgradeTask(Map<String, Object> parameter);

	/**
	 * 新建升级任务网关信息查询
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> upgradeTaskAddSearch(Map<String, Object> parameter);

	/**
	 * 新建升级任务设置页面网关信息查询
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> upgradeTaskAddSetting(Map<String, Object> parameter);

	/**
	 * 新增升级任务
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> addUpgradeTask(Map<String, Object> parameter);

	/**
	 * 查询升级任务的明细
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> queryUpgradeTaskDetail(Map<String, Object> parameter);

	/**
	 * 立即升级信息查询
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> queryUpgradeSpecified(Map<String, Object> parameter);

	/**
	 * 立即升级
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> upgradeImmediately(Map<String, Object> parameter);

	/**
	 * 机顶盒开机或重启立即升级
	 * 
	 * @param inform
	 *            上报信息对象
	 * @param evnt
	 *            事件 3:开机或重启
	 * @param logid
	 *            日志ID
	 * @return
	 */
	void bootEventUpgradeImmediately(String logid, String serialNumber, String factoryCode, int evnt);

	/**
	 * 更新固件任务状态
	 * 
	 * @param taskId
	 *            任务ID
	 * @param requestId
	 *            请求ID
	 * @param status
	 *            状态
	 * @param logid
	 *            日志ID
	 */
	void updateTaskDetailStatus(String logid, String taskId, String requestId, int status);

}
