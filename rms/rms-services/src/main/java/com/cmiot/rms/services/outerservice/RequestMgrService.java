package com.cmiot.rms.services.outerservice;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 外部调用接口管理
 * Created by wangzhen on 2016/1/29.
 */
public interface RequestMgrService {
    /**
     * 由ACS发起，用于对一个特定的多实例对象创建一个新的实例
     *
     * @param jsonObject
     */
    Map<String, Object> addObject(JSONObject jsonObject);

    /**
     * 由ACS发起，用于删除一个对象的特定实例
     *
     * @param jsonObject
     */
    Map<String, Object> deleteObject(JSONObject jsonObject);

    /**
     * 由ACS发起，用于要求CPE终端在指定的位置下载指定的文件
     *
     * @param jsonObject
     */
    Map<String, Object> download(JSONObject jsonObject);

    /**
     * 由ACS发起，用于要求特定的CPE终端恢复出厂设置
     *
     * @param jsonObject
     */
    Map<String, Object> factoryReset(JSONObject jsonObject);

    /**
     * @param jsonObject
     */
    Map<String, Object> getParameterAttributes(JSONObject jsonObject);

    /**
     * 由ACS发起，用于发现CPE上的可访问参数
     *
     * @param jsonObject
     */
    Map<String, Object> getParameterNames(JSONObject jsonObject);

    /**
     * 由ACS发起，用于查找CPE上一个或者多个参数的值
     *
     * @param jsonObject
     */
    Map<String, Object> getParameterValues(JSONObject jsonObject);

    /**
     * 由CPE或者是ACS发起，用于发现另一方所支持的方法集
     *
     * @param jsonObject
     */
    Map<String, Object> getRPCMethods(JSONObject jsonObject);

    /**
     * 由ACS发起，用于修改CPE上一个或者多个参数的值
     *
     * @param jsonObject
     */
    Map<String, Object> setParameterValues(JSONObject jsonObject);

    /**
     * 由ACS发起，用于要求CPE终端向指定位置上传某一特定文件
     *
     * @param jsonObject
     */
    Map<String, Object> upload(JSONObject jsonObject);

    /**
     * 信息上报
     *
     * @param jsonObject
     */
    Map<String, Object> inform(JSONObject jsonObject);

    /**
     * 由ACS发起，用于重启指定的CPE终端
     *
     * @param jsonObject
     */
    void reboot(JSONObject jsonObject);

    /**
     * 用于修改CPE上一个或者多个参数的属性
     *
     * @param jsonObject
     */
    Map<String, Object> setParameterAttributes(JSONObject jsonObject);

    /**
     * @param jsonObject
     */
    Map<String, Object> transferComplete(JSONObject jsonObject);

    /**
     * 智能网关设备首次连接 RMS（ OUI-SN 认证）
     *
     * @param parameter
     * @return
     */
    public boolean certification(Map<String, Object> parameter);

    /**
     * @param jsonObject
     * @return
     */
    Map<String, Object> fault(JSONObject jsonObject);
    /**
     * 智能网关设备首次连接 PASSWORD 认证
     *
     * @param parameter
     * @return
     */
	boolean checkPassword(Map<String, Object> parameter);
	
	/**
	 * 由ACS发起查询Digest账号和密码 
	 * @param parameter
	 * @return
	 */
	boolean queryDigest(Map<String, Object> parameter);

    /**
     * 根据用户名查询digest密码
     * @param userName
     * @return
     */
    List<String> queryDigestPassword(String userName);


    /**
     * 根据OUI-SN查询Digest账号和密码
     * @param parameter
     * @return
     */
    Map<String, Object> queryDigestAccAndPw(Map<String, Object> parameter);
}
