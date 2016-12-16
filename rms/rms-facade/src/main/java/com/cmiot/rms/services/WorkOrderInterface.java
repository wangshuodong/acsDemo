package com.cmiot.rms.services;

import java.util.Map;

/**
 * Created by admin on 2016/6/9.
 */
public interface WorkOrderInterface {
    /**
     * 提供给BOSS开通工单服务的接口
     * @param parameter
     * @return
     */
    Map<String,Object> openService(Map<String,Object> parameter);

    /**
     * PBOSS北向接口-新装信息同步
     * @param parameter
     * @return
     */
    Map<String,Object> addNewInstallation(Map<String,Object> parameter);

    /**
     * PBOSS北向接口-拆机信息同步
     * @param parameter
     * @return
     */
    Map<String,Object> broadBandUnsubcribe(Map<String,Object> parameter);
   
    /**
     * 客户申请停机
     * */
    Map<String,Object>  customerRequestStop(Map<String,Object> parameter);
    /**
     * 客户申请复机
     * */
    Map<String,Object>  customerRequestResume(Map<String,Object> parameter);
    /**
     * 订户密码变更信息同步
     * */
    Map<String,Object>  changePPPoEPassword(Map<String,Object> parameter);
    /**
     * 缴费开机信息同步
     * @param parameter
     * @return
     */
    Map<String,Object> paymentBoot(Map<String,Object> parameter);
    
    /**
     * 欠费停机信息同步
     * @param parameter
     * @return
     */
    Map<String,Object> arrearsStop(Map<String,Object> parameter);
    
    /**
     * 修改速率信息同步
     * @param parameter
     * @return
     */
    Map<String,Object> changeBandwidth(Map<String,Object> parameter);
}
