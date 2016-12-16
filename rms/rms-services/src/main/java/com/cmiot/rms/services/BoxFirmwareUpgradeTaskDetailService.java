package com.cmiot.rms.services;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.BoxFirmwareUpgradeTaskDetail;

/**
 * Created by fuwanhong on 2016/1/25.
 */
public interface BoxFirmwareUpgradeTaskDetailService {

	/**
	 * 根据升级任务ID和任务详情状态查询任务详情
	 * 
	 * @param parameter
	 * @return
	 */
	List<Map<String, Object>> queryListByIdAndStatus(Map<String, Object> parameter);

	/**
	 * 添加升级任务详情
	 * 
	 * @param firmwareUpgradeTaskDetail
	 */
	// void addFirmwareUpgradeTaskDetail(BoxFirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail);

	/**
	 * 查询升级任务详情总数
	 * 
	 * @param upgradeTaskId
	 * @param status
	 * @return
	 */
	 int searchTaskDetailCount(String upgradeTaskId, String status);

	/**
	 * 查询非升级成功的总数
	 * 
	 * @param firmwareId
	 * @return
	 */
	// int searchNoSuccessCount(String firmwareId);

	/**
	 * 根据网关ID和任务ID更新任务状态
	 * 
	 * @param record
	 */
	// void updateTaskDetailStatus(BoxFirmwareUpgradeTaskDetail record);

	/**
	 * 查询升级任务详情列表
	 * 
	 * @param firmwareUpgradeTaskDetail
	 * @return
	 */
	// List<BoxFirmwareUpgradeTaskDetail> queryList(BoxFirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail);

	/**
	 * 根据网关ID查询该网关是否存在立即升级的任务
	 * 
	 * @param gatewayId
	 * @return
	 */
	// BoxFirmwareUpgradeTaskDetail searchLatelyImmediatelyDetail(String gatewayId);

	/**
	 * 批量插入
	 * 
	 * @param detailList
	 */
	void batchInsert(List<BoxFirmwareUpgradeTaskDetail> detailList);
}