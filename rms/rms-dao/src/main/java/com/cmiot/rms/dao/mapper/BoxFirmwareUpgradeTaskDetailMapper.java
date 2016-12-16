package com.cmiot.rms.dao.mapper;

import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.BoxFirmwareUpgradeTaskDetail;
import org.apache.ibatis.annotations.Param;

public interface BoxFirmwareUpgradeTaskDetailMapper {

	List<Map<String, String>> queryListByIdAndStatus(Map<String, Object> map);

	int countQueryListByIdAndStatus(Map<String, Object> map);

	int deleteByPrimaryKey(String id);

	int insert(BoxFirmwareUpgradeTaskDetail record);

	int insertSelective(BoxFirmwareUpgradeTaskDetail record);

	BoxFirmwareUpgradeTaskDetail selectByPrimaryKey(String id);

	int updateByPrimaryKeySelective(BoxFirmwareUpgradeTaskDetail record);

	int updateByPrimaryKey(BoxFirmwareUpgradeTaskDetail record);

	int batchInsert(List<BoxFirmwareUpgradeTaskDetail> detailList);

	int searchTaskDetailCount(Map<String, String> map);

	BoxFirmwareUpgradeTaskDetail selectByBoxIdAndTaskTriggerMode(Map<String, Object> map);

	/**
	 * 根据任务触发事件,升级状态,机顶盒序列号,机顶盒厂商编码
	 * 
	 * @param map
	 *            {evnt:"任务触发事件",status:升级状态,serialNumber:机顶盒序列号,factoryCode:机顶盒厂商编码}
	 * @return map
	 *         {taskDetailId:任务明细ID,boxId:机顶盒ID,taskId:任务ID,firmwareId:固件ID}
	 */
	List<Map<String, Object>> selectByFactoryCodeSerialNumberEvntStatus(Map<String, Object> map);

	List<BoxFirmwareUpgradeTaskDetail> selectTaskDetailByTaskIdAndBoxId(BoxFirmwareUpgradeTaskDetail record);

	/**
	 * 统计固件升级不成功总数
	 * 
	 * @param map
	 * @return
	 */
	int countSuccessStatusTaskDetail(String upgradeTaskId);

	/**
	 * 批量更新状态和升级开始时间
	 * @param para
	 * @return
	 */
	int batchUpdateProcessingStatusAndTime(Map<String, Object> para);

	List<BoxFirmwareUpgradeTaskDetail> queryListByTaskId(Map<String, Object> para);

	int queryCountNoComplete(BoxFirmwareUpgradeTaskDetail record);

	/**
	 * 查询机顶盒升级数，用于判断机顶盒是否在升级中
	 * @param boxId
	 * @return
	 */
	int searchProcessingCount(@Param("boxId") String boxId);
}