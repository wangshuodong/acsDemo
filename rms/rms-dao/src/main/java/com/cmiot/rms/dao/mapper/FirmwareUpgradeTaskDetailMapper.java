package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.FirmwareUpgradeTaskDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface FirmwareUpgradeTaskDetailMapper  extends BaseMapper<FirmwareUpgradeTaskDetail>{
    int deleteByPrimaryKey(String id);

    int insert(FirmwareUpgradeTaskDetail record);

    int insertSelective(FirmwareUpgradeTaskDetail record);

    FirmwareUpgradeTaskDetail selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(FirmwareUpgradeTaskDetail record);

    int updateByPrimaryKey(FirmwareUpgradeTaskDetail record);

    List<FirmwareUpgradeTaskDetail> queryList(FirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail);

    List<FirmwareUpgradeTaskDetail> queryListByTaskId(Map<String, Object> para);
//
//    FirmwareUpgradeTaskDetail selectFirmwareUpgradeTaskDetail(FirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail);
//
//    PageBean<Map> queryListByTaskUuid4Page(PageBean<Map> pagerBean);
//
//    int updateSelectFirmwareUpgradeTaskDetailByGateUuid(FirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail);
//
    int searchTaskDetailCount(Map<String, String> map);

    List<Map<String, Object>> queryListByIdAndStatus(Map<String, String> para);

    int searchNoSuccessCount(@Param("firmwareId") String firmwareId);

    int updateTaskDetailStatus(FirmwareUpgradeTaskDetail record);

    List<FirmwareUpgradeTaskDetail> selectGateWayInfo(Map<String, Object> para);

    /**
     * 根据网关ID查询该网关最近一次立即升级的任务
     * @param gatewayId
     * @return
     */
    List<FirmwareUpgradeTaskDetail> searchLatelyImmediatelyDetail(@Param("gatewayId") String gatewayId);

    /**
     * 批量更新状态和升级开始时间
     * @param para
     * @return
     */
    int batchUpdateProcessingStatusAndTime(Map<String, Object> para);

    /**
     * 批量插入
     * @return
     */
    int batchInsert(List<FirmwareUpgradeTaskDetail> detailList);

    /**
     * 查询为完成的列表，状态包括：WAIT(0, "等待升级"),PROCESSING(1, "升级中")
     * @param firmwareUpgradeTaskDetail
     * @return
     */
    List<FirmwareUpgradeTaskDetail> queryListNoComplete(FirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail);

    int queryCount(Map<String, Object> para);

    /**
     * 查询网关升级数，用于判断网关是否在升级中
     * @param gatewayId
     * @return
     */
    int searchProcessingCount(@Param("gatewayId") String gatewayId);

}