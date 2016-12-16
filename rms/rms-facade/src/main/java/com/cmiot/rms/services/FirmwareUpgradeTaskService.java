package com.cmiot.rms.services;

import java.util.Map;

/**
 * Created by panmingguo on 2016/4/15.
 */
public interface FirmwareUpgradeTaskService {
    /**
     * 根据名称查询升级信息
     * @param parameter
     * @return
     */
    Map<String, Object> searchUpgradeTask(Map<String, Object> parameter);



    /**
     * 新建升级任务页面的查询页面
     * @param parameter
     * @return
     */
    Map<String, Object> upgradeTaskAddSearch(Map<String, Object> parameter);


    /**
     * 新建升级任务页面的升级任务设置页面
     * @param parameter
     * @return
     */
    Map<String, Object> upgradeTaskAddSetting(Map<String, Object> parameter);


    /**
     * 添加升级任务
     * @param parameter
     * @return
     */
    Map<String, Object> addUpgradeTask(Map<String, Object> parameter);


    /**
     * 查询升级任务详情
     * @param parameter
     * @return
     */
    Map<String, Object> searchUpgradeTaskDetail(Map<String, Object> parameter);

    /**
     * 网关页面点击升级时使用
     * @param parameter
     * @return
     */
    Map<String, Object> upgradeSpecifiedGateway(Map<String, Object> parameter);

    /**
     * 单个网关立即升级
     * @param parameter
     * @return
     */
    Map<String, Object> upgradeImmediately(Map<String, Object> parameter);
    
    /**
     * 根据网关MAC地址查询升级任务
     * */
    Map<String, Object> queryUpgradeTaskByMacs(Map<String, Object> parameter);
}
