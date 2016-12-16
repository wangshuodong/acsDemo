package com.cmiot.rms.services;

import java.util.Map;

/**
 * Created by admin on 2016/6/7.
 */
public interface BoxManageService {
    /**
     * 机顶盒导入
     * @param parameter
     * @return
     */
    public Map<String,Object> importBox(Map<String,Object> parameter);

    /**
     * 2.1.54.	机顶盒-首页分页查询
     * @param parameter
     * @return
     */
    public Map<String,Object> queryList4Page(Map<String,Object> parameter);

    /**
     * 2.1.55.	机顶盒-修改机顶盒信息
     * @param parameter
     * @return
     */
    public Map<String,Object> updateBoxInfo(Map<String,Object> parameter);

    /**
     *2.1.56.	机顶盒-重启
     * @param parameter
     * @return
     */
    public Map<String,Object> rebootBox(Map<String,Object> parameter);

    /**
     * 2.1.57.	机顶盒-恢复出厂设置
     * @param parameter
     * @return
     */
    public Map<String,Object> factoryResetBox(Map<String,Object> parameter);

    /**
     * 2.1.93.	机顶盒管理-机顶盒分页查接口定义
     * @param parameter
     * @return
     */
    public Map<String,Object> queryBoxListForPage(Map<String,Object> parameter);
    
    /**
     * 修改机顶盒维护账号
     * @param parameter
     * @return
     */
    public Map<String,Object> modFamilyAccountPwd(Map<String,Object> parameter);

    /**
     * 2.1.88.	机顶盒管理—获取机顶盒属性功能接口定义
     * @param parameter
     * @return
     */
    public Map<String,Object> getParameterNames(Map<String,Object> parameter);

    /**
     * 2.1.89.	机顶盒管理—获取机顶盒属性参数功能接口定义
     * @param parameter
     * @return
     */
    public Map<String,Object> getParameterValues(Map<String,Object> parameter);

    /**
     *2.1.90.	机顶盒管理—设置机顶盒属性参数功能接口定义
     * @param parameter
     * @return
     */
    public Map<String,Object> setParameterValues(Map<String,Object> parameter);
    
    /**
     * 查询维护账号、密码、所属区域
     * @param parameter
     * @return
     */
    public Map<String,Object> queryBoxBaseInfo(Map<String,Object> parameter);

    /**
     * 功能:机顶盒详情查询功能
     * @param map 请求参数
     * @return
     */
    public Map<String,Object> queryBoxDetail(Map<String,Object> map) ;

    /**
     * 功能:查询机顶盒节点属性
     * @param map 请求参数
     * @return
     */
    public Map<String,Object> getAttribute(Map<String,Object> map) ;

    /**
     *设置机顶盒节点属性
     * @param map 请求参数
     * @return
     */
    public Map<String,Object> setAttribute(Map<String,Object> map) ;
}
