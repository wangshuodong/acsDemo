package com.cmiot.rms.dao.model;

import java.io.Serializable;

/**
 * Created by wangzhen on 2016/1/28.
 */
public class LoggingBean implements Serializable {
    /**
     * 标记是接口调用日志 还是操作日志
     */
    private String mark;

    /**
     * 登录账户名称
     */
    private String userName;

    /**
     * 角色组
     */
    private String roleId;

    /**
     * 角色组名称
     */
    private String roleName;

    /**
     * 类目菜单
     */
    private String categoryMenu;

    /**
     * 类目菜单名称
     */
    private String categoryMenuName;

    /**
     * 操作
     */
    private String operation;

    /**
     * 参数
     */
    private String datas;

    /**
     * 操作前内容
     */
    private String beforeContent;

    /**
     * 操作后内容
     */
    private String afterContent;

    /**
     * 访问路径
     */
    private String urlPattern;

    /**
     * 日期  格式：2016-01-11 14:11
     */
    private String strDate;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 查询条件
     */
    private String query;

    /**
     * 下发指令唯一ID
     */
    private String requestId;

    /**
     * 网关ID
     */
    private String gatewayId;

    /**
     * 存入内容
     */
    private String content;
    
    /**
     * 日志类别名称
     */
    private String logTypeName;
    
    /**
     * 告警IP（告警日志返回用）
     */
    private String alarmAddress;

    public String getCategoryMenuName() {
        return categoryMenuName;
    }

    public void setCategoryMenuName(String categoryMenuName) {
        this.categoryMenuName = categoryMenuName;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getCategoryMenu() {
        return categoryMenu;
    }

    public void setCategoryMenu(String categoryMenu) {
        this.categoryMenu = categoryMenu;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getDatas() {
        return datas;
    }

    public void setDatas(String datas) {
        this.datas = datas;
    }

    public String getBeforeContent() {
        return beforeContent;
    }

    public void setBeforeContent(String beforeContent) {
        this.beforeContent = beforeContent;
    }

    public String getAfterContent() {
        return afterContent;
    }

    public void setAfterContent(String afterContent) {
        this.afterContent = afterContent;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getStrDate() {
        return strDate;
    }

    public void setStrDate(String strDate) {
        this.strDate = strDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

	public String getLogTypeName() {
		return logTypeName;
	}

	public void setLogTypeName(String logTypeName) {
		this.logTypeName = logTypeName;
	}

	public String getAlarmAddress() {
		return alarmAddress;
	}

	public void setAlarmAddress(String alarmAddress) {
		this.alarmAddress = alarmAddress;
	}
    
    
}
