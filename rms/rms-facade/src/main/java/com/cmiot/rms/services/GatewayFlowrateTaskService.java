package com.cmiot.rms.services;

import java.util.Map;


/**
 * 网关查询流量
 * Created by zoujiang on 2016/6/16.
 */
public interface GatewayFlowrateTaskService {

    /**
     * 根据条件查询该网关的流量查询任务列表
     * @param parameter
     * @return
     */
    Map<String, Object> queryFlowrateTaskList(Map<String, Object> parameter);
    /**
     * 根据条件查询该网关的流量查询任务详情
     * 返回在本次升级任务中所有网关的版本和流量信息
     * @param parameter
     * @return
     */
    Map<String, Object> queryFlowrateTaskDetails(Map<String, Object> parameter);

    /**
     * 新增任务
     * @param parameter
     * @return
     */
    Map<String, Object> addFlowrateTask(Map<String, Object> parameter);


    /**
     * 编辑流量查询任务，提供进入修改页面数据
     * @param parameter
     * @return
     */
    Map<String, Object> editFlowrateTask(Map<String, Object> parameter);


    /**
     * 更新流量查询任务
     * @param parameter
     * @return
     */
    Map<String, Object> updateFlowrateTask(Map<String, Object> parameter);


    /**
     * 删除流量查询任务
     * @param parameter
     * @return
     */
    Map<String, Object> deleteFlowrateTask(Map<String, Object> parameter);


    /**
     *
	 *  1:第一次启动 2：周期心跳上报 3：重新启动4：参数改变
     * */
	void executeTask(String sn,String oui, int eventCode);
}
