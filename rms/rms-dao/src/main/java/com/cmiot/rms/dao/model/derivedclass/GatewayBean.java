package com.cmiot.rms.dao.model.derivedclass;

import com.cmiot.rms.dao.model.GatewayInfo;

/**
 * 与数据库不相关的字段存储
 * Created by wangzhen on 2016/4/28.
 */
public class GatewayBean extends GatewayInfo{

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}


	private String businessName;

	private String status;

	public String parameterList;



	public String getParameterList() {
		return parameterList;
	}

	public void setParameterList(String parameterList) {
		this.parameterList = parameterList;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
