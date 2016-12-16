package com.cmiot.rms.services.util;

import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.LogTypeEnum;
import com.cmiot.rms.services.LogManagerService;
import com.cmiot.rms.services.impl.LogManagerServiceImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by panmingguo on 2016/4/21.
 */
public class OperationLogUtil {

    private static final String CONTENT = "content";

    private static final String USERNAME = "userName";

    private static final String CATEGORYMENU = "categoryMenu";

    private static final String OPERATION = "operation";

    private static final String ROLENAME = "roleName";

    private static final String CATEGORYMENUNAME = "categoryMenuName";
    
    private static final String LOGTYPE = "logType";

    private static OperationLogUtil operationLogUtil;

    private LogManagerService logManagerService;

    private OperationLogUtil()
    {
        logManagerService = new LogManagerServiceImpl();
    }

    public static OperationLogUtil getInstance()
    {
        if(null == operationLogUtil)
        {
            if(null == operationLogUtil)
            {
                operationLogUtil = new OperationLogUtil();
            }
        }
        return operationLogUtil;
    }


    public void recordOperationLog(String userName, String roleName,
                                   String operation,  String content,
                                   String categoryMenu, String categoryMenuName)
    {
        Map<String, Object> parameterLog = new HashMap<>();

        // 登录用户名称
        parameterLog.put(USERNAME,userName);
        // 角色名称
        parameterLog.put(ROLENAME, roleName);
        // 具体的操作
        parameterLog.put(OPERATION, operation);
        // 操作的数据内容
        parameterLog.put(CONTENT, content);
        // 类目ID(菜单ID)
        parameterLog.put(CATEGORYMENU, categoryMenu);
        // 类目名称
        parameterLog.put(CATEGORYMENUNAME, categoryMenuName);
        // 日志类别
        parameterLog.put(LOGTYPE, LogTypeEnum.LOG_TYPE_OPERATION.code());
        logManagerService.recordOperationLog(parameterLog);
    }
    
    public void recordOperationLog(Map<String,Object> parameter, CategoryEnum categoryEnum, String operation, String content){
    	Map<String, Object> parameterLog = new HashMap<>();

        // 登录用户名称
        parameterLog.put(USERNAME,parameter.get(USERNAME).toString());
        // 角色名称
        parameterLog.put(ROLENAME, parameter.get(ROLENAME).toString());
        // 具体的操作
        parameterLog.put(OPERATION, operation);
        // 操作的数据内容
        parameterLog.put(CONTENT, content);
        // 类目ID(菜单ID)
        parameterLog.put(CATEGORYMENU, categoryEnum.name());
        // 类目名称
        parameterLog.put(CATEGORYMENUNAME, categoryEnum.description());
        // 日志类别
        parameterLog.put(LOGTYPE, LogTypeEnum.LOG_TYPE_OPERATION.code());
        logManagerService.recordOperationLog(parameterLog);
    }
}
