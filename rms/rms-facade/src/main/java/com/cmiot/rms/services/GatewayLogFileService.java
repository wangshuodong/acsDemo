package com.cmiot.rms.services;

import java.util.Map;

/**
 * Created by panmingguo on 2016/6/8.
 */
public interface GatewayLogFileService {

    /**
     * 设置网关日志开关
     * @param parameter
     * @return
     */
    Map<String, Object> settingLogSwitch (Map<String, Object> parameter);


    /**
     * 上传日志文件
     * @param parameter
     * @return
     */
    Map<String, Object> uploadLogFile (Map<String, Object> parameter);



    /**
     * 文件服务器在文件上传完成后回调该接口更新文件大小、名称、状态
     *
     * @param parameter
     * @return
     */

    Map<String, Object> updateLogFileInfo(Map<String, Object> parameter);


    /**
     * 功能:dubbo服务接口、网关分页查询功能
     * @param map 请求参数
     * @return
     */
    public Map<String,Object> queryList4Page(Map<String,Object> map) ;
}
