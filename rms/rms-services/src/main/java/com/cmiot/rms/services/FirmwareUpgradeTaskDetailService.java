package com.cmiot.rms.services;

import com.cmiot.rms.dao.model.FirmwareUpgradeTaskDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by fuwanhong on 2016/1/25.
 */
public interface FirmwareUpgradeTaskDetailService {

    /**
     * 根据升级任务ID和任务详情状态查询任务详情
     * @param parameter
     * @return
     */
    Map<String, Object> queryListByIdAndStatus(Map<String, Object> parameter);

    /**
     * 添加升级任务详情
     * @param firmwareUpgradeTaskDetail
     */
    void addFirmwareUpgradeTaskDetail(FirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail);

    /**
     *查询升级任务详情总数
     * @param upgradeTaskId
     * @param status
     * @return
     */
    int searchTaskDetailCount(String upgradeTaskId, String status);


    /**
     * 查询非升级成功的总数
     * @param firmwareId
     * @return
     */
    int searchNoSuccessCount(String firmwareId);

    /**
     * 根据网关ID和任务ID更新任务状态
     * @param record
     */
    void updateTaskDetailStatus(FirmwareUpgradeTaskDetail record);

    /**
     * 查询升级任务详情列表
     * @param firmwareUpgradeTaskDetail
     * @return
     */
    List<FirmwareUpgradeTaskDetail> queryList(FirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail);


    /**
     * 根据网关ID查询该网关是否存在立即升级的任务
     * @param gatewayId
     * @return
     */
    FirmwareUpgradeTaskDetail searchLatelyImmediatelyDetail(String gatewayId);

    /**
     * 批量插入
     * @param detailList
     */
    void batchInsert(List<FirmwareUpgradeTaskDetail> detailList);


    /**
     * 查询网关升级数，用于判断网关是否在升级中
     * @param gatewayId
     * @return
     */
    int searchProcessingCount(String gatewayId);
}
