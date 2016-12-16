package com.cmiot.rms.services;

import java.util.Map;

/**
 * Created by admin on 2016/6/8.
 */
public interface WorkOrderManagerService {

    /**
     * 导入业务模板
     * @param parameter
     * @return
     */
     Map<String,Object>importWorkOrderTemplate(Map<String, Object> parameter);

    /**
     * 查询模板列表
     * @param parameter
     * @return
     */
     Map<String,Object>queryWorkOrderList(Map<String,Object> parameter);

    /**
     * 分页查询模板列表
     * @param parameter
     * @return
     */
    Map<String, Object> queryWorkOrderList4Page(Map<String,Object> parameter);

    /**
     * 查询网关开通业务进度
     * @param parameter
     * @return
     */
     Map<String,Object>queryGatewayOpenBusinessState(Map<String,Object> parameter);

    /**
     * 2.1.81.	工单管理—删除模板
     * @param parameter
     * @return
     */
    Map<String,Object>deleteWorkOrderTemplate(Map<String,Object> parameter);

    /**
     *2.1.82.	工单管理—修改业务模板
     * @param parameter
     * @return
     */
    Map<String,Object>updateWorkOrderTemplate(Map<String,Object> parameter);
    /**
     *导入工单
     * @param parameter
     * @return
     */
    Map<String,Object> importWorkOrder(Map<String,Object> parameter);
    /**
     *编辑工单
     * @param parameter
     * @return
     */
    Map<String,Object> updateWorkOrder(Map<String,Object> parameter);
    /**
     *删除工单
     * @param parameter
     * @return
     */
    Map<String,Object> deleteWorkOrder(Map<String,Object> parameter);
    
    
    /**
     *重新执行工单
     * @param parameter
     * @return
     */
    Map<String,Object> reExecuteWorkOrder(Map<String,Object> parameter);
}
