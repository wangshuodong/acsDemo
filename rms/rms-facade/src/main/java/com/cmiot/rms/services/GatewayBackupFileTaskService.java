package com.cmiot.rms.services;

import java.util.Map;

/**
 * 网关备份文件任务服务类
 * Created by panmingguo on 2016/6/7.
 */
public interface GatewayBackupFileTaskService {

    /**
     * 根据条件查询该网关的备份文件任务列表
     * @param parameter
     * @return
     */
    Map<String, Object> queryBackupFileTaskList(Map<String, Object> parameter);

    /**
     * 新增任务
     * @param parameter
     * @return
     */
    Map<String, Object> addBackupFileTask(Map<String, Object> parameter);


    /**
     * 编辑备份文件任务，提供进入修改页面数据
     * @param parameter
     * @return
     */
    Map<String, Object> editBackupFileTask(Map<String, Object> parameter);


    /**
     * 更新备份文件任务
     * @param parameter
     * @return
     */
    Map<String, Object> updateBackupFileTask(Map<String, Object> parameter);


    /**
     * 删除备份文件任务
     * @param parameter
     * @return
     */
    Map<String, Object> deleteBackupFileTask(Map<String, Object> parameter);
}
