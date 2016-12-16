package com.cmiot.rms.common.logback;

import java.io.Serializable;

/**
 * Created by wangzhen on 2016/1/27.
 */
public class LogBackBean implements Serializable {
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
    private String content;


    /**
     * 访问路径
     */
    private String urlPattern;
    /**
     * 日志类别
     */
    private String logType;
    /**
     * 日志类别名称
     */
    private String logTypeName;
    public String getCategoryMenuName() {
        return categoryMenuName;
    }

    public void setCategoryMenuName(String categoryMenuName) {
        this.categoryMenuName = categoryMenuName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDatas() {
        return datas;
    }

    public void setDatas(String datas) {
        this.datas = datas;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public String getLogTypeName() {
		return logTypeName;
	}

	public void setLogTypeName(String logTypeName) {
		this.logTypeName = logTypeName;
	}
    
}
