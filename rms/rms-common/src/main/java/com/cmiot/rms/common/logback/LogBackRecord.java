package com.cmiot.rms.common.logback;

import com.cmiot.rms.common.enums.LogMarkEnum;
import com.cmiot.rms.common.enums.SeparatorEnum;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记录日志信息到数据库
 * Created by wangzhen on 2016/1/27.
 */
public class LogBackRecord {

    private static Logger logger = LoggerFactory.getLogger(LogBackBean.class);
    private static LogBackRecord ourInstance = new LogBackRecord();

    private LogBackRecord() {
    }

    public static LogBackRecord getInstance() {
        return ourInstance;
    }

    /**
     * 记录日志参数处理
     *
     * @param mark
     * @param content
     * @param roleId
     * @param userName
     * @param categoryMenu
     * @param datas
     * @param operation
     * @param urlPattern
     * @param roleName
     * @param categoryMenuName
     */
    public static void logBackBean(String mark,
                                   String content,
                                   String roleId,
                                   String userName,
                                   String categoryMenu,
                                   String datas,
                                   String operation,
                                   String urlPattern,
                                   String roleName,
                                   String categoryMenuName,
                                   String logType,
                                   String logTypeName) {


        LogBackBean logBackBean = new LogBackBean();
        logBackBean.setMark(mark);
        logBackBean.setContent(content);
        logBackBean.setRoleId(roleId);
        logBackBean.setUserName(userName);
        logBackBean.setCategoryMenu(categoryMenu);
        logBackBean.setDatas(datas);
        logBackBean.setOperation(operation);
        logBackBean.setUrlPattern(urlPattern);
        logBackBean.setRoleName(roleName);
        logBackBean.setCategoryMenuName(categoryMenuName);
        logBackBean.setLogType(logType);
        logBackBean.setLogTypeName(logTypeName);
        LogBackRecord.getInstance().recordOperationLog(logBackBean);
    }

    /***
     * 向数据库写入操作日志信息
     *
     * @param logBackBean
     */
    public void recordOperationLog(LogBackBean logBackBean) {

        StringBuffer stringBuffer = new StringBuffer();
//        if (StringUtils.equals(LogMarkEnum.LOG_MARK_OPERATION.code(), logBackBean.getMark())) {
//            stringBuffer.append(CategoryEnum.getDescription(logBackBean.getCategoryMenuName()));
//        } else if (StringUtils.equals(LogMarkEnum.LOG_MARK_OUTER_PORT.code(), logBackBean.getMark())) {
//            stringBuffer.append(LogMarkEnum.LOG_MARK_OUTER_PORT.description());
//        } else {
//            stringBuffer.append(LogMarkEnum.LOG_MARK_PORT.description());
//        }
        stringBuffer.append(logBackBean.getCategoryMenuName());
        stringBuffer.append("-" + logBackBean.getOperation() + SeparatorEnum.SEPARATOR_ARR1.code());
//        stringBuffer.append("mark={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("logType={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("categoryMenu={}" + SeparatorEnum.SEPARATOR_ARR2.code());
       
        stringBuffer.append("roleName={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("userName={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("operation={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("content={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("datas={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("roleId={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("logTypeName={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        
        stringBuffer.append("categoryMenu={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("categoryMenuName={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("urlPattern={}");


        // arg0->mark,arg1->categoryMenu,arg2->roleName,arg3->userName
        logger.info(stringBuffer.toString(),
//                logBackBean.getMark(),
                logBackBean.getLogType(),
                logBackBean.getCategoryMenu(),
               
                logBackBean.getRoleName(),
                logBackBean.getUserName(),
                logBackBean.getOperation(),
                logBackBean.getContent(),
                logBackBean.getDatas(),
                logBackBean.getRoleId(),
                logBackBean.getLogTypeName(),
                
                logBackBean.getCategoryMenu(),
                logBackBean.getCategoryMenuName(),
                logBackBean.getUrlPattern());
    }

    /**
     * 向数据库写入调用接口日志信息
     * @param mark 日志标记
     * @param content 下发指令内容或者接收回调内容
     * @param requestId 下发指令唯一标示
     * @param deviceId 网关或机顶盒ID
     * @param operation 做的什么操作
     */
    public void recordInvokingLog(String logType,String content,String requestId,String deviceId,String operation, String logTypeName) {
        StringBuffer stringBuffer = new StringBuffer();
//        if (StringUtils.equals(LogMarkEnum.LOG_MARK_OUTER_PORT.code(), mark)) {
//            stringBuffer.append(LogMarkEnum.LOG_MARK_OUTER_PORT.description());
//        } else {
//            stringBuffer.append(LogMarkEnum.LOG_MARK_PORT.description());
//        }

        // 日志标记、requestId、网关ID
        stringBuffer.append("-" + operation + SeparatorEnum.SEPARATOR_ARR1.code());
//        stringBuffer.append("mark={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("logType={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("requestId={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("deviceId={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("userName={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("operation={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("content={}" + SeparatorEnum.SEPARATOR_ARR2.code());
        stringBuffer.append("logTypeName={}" );

        // arg0->mark,arg1->requestId,arg2->gatewayId,arg3->operation
        logger.info(stringBuffer.toString(),logType,requestId,deviceId,"admin",operation,content,logTypeName);
    }


}
