package com.cmiot.rms.services;

import java.util.Map;

/**
 * Created by 魏磊 on 2016/5/17.
 * 批量配置任务接口
 */
public interface GatewayBatchSetTaskInterface {
    /**
     * 批量设置任务分页查询功能接口
     * @param parameter
     * @return
     */
    Map<String, Object> queryBatchSetTaskPage(Map<String, Object> parameter);
    /**
     * 删除批量设置任务
     * @param parameter
     * @return
     */
    Map<String, Object> deleteBatchSetTask(Map<String, Object> parameter);
    /**
     * 新增批量设置任务
     * @param parameter
     * @return
     */
    Map<String, Object> addBatchSetTask(Map<String, Object> parameter);
    /**
     * 更新批量设置任务
     * @param parameter
     * @return
     */
    Map<String, Object> updateBatchSetTask(Map<String, Object> parameter);
}
