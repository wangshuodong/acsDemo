package com.cmiot.rms.services;

import java.util.Map;

/**
 * 日志管理
 * Created by wangzhen on 2016/4/18.
 */
public interface LogManagerService {

    /**
     * 查询日志,包括操作日志、系统日志、安全日志、告警日志的查询（操作日志有用户查询权限控制）
     * @param parameter
     * @return
     */
    public Map<String,Object> queryOperationLog(Map<String,Object> parameter);

    /**
     * 记录日志
     * @param parameter
     * @return
     */
    public Map<String,Object> recordOperationLog(Map<String,Object> parameter);


    /**
     * 导出日志
     * @param parameter
     * @return
     */
    public Map<String,Object> exportLogFile(Map<String,Object> parameter);


    /**
     * 更新日志自动备份配置
     * @param parameter
     * @return
     */
    public Map<String,Object> updateLogAutoBak(Map<String,Object> parameter);
    
    /**
     * 手动备份日志（集团测试需求）
     * @param parameter
     * @return
     */
    public Map<String,Object> backupLog(Map<String, Object> parameter);
    
    /**
     * 手动还原日志（集团测试需求）
     * @param parameter
     * @return
     */
    public Map<String,Object> restore(Map<String,Object> parameter);
    
    /**
     * 查询备份列表（集团测试需求）
     */
    public Map<String,Object> queryBakList(Map<String,Object> parameter);
}
