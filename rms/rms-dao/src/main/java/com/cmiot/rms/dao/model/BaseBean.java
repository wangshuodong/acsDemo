package com.cmiot.rms.dao.model;

import java.io.Serializable;

/**
 * 实体公共部分
 * Created by wangzhen on 2016/4/8.
 */
public abstract class BaseBean implements Serializable{
    /**
     * 账户名称
     */
    private String userName;
    /**
     * 角色组名称
     */
    private String roleName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
