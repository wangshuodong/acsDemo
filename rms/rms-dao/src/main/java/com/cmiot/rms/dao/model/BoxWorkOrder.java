package com.cmiot.rms.dao.model;

import java.util.Date;
import java.util.List;

public class BoxWorkOrder extends BoxInfo {
	private String id;

	private String boxUuid;

	private String businessCodeBoss;

	private String businessCode;

	private String businessName;

	private String parameterList;

	private String businessStatu;

	private String areacode;

	private Integer createTime;

	private String businessType;

	private String boxMac;

	private String orderNo;

	private Integer updateTime;

	private Integer failCount;

	private String provcode;

	private String useridBoss;

	private Date ordertimeBoss;

	private String devicetype;

	private String usernameBoss;

	private String useraddressBoss;

	private String contactpersonBoss;

	private String contactmannerBoss;

	private String ipoeid;

	private String ipoepassword;

	private String userid;

	private String useridpassword;
	
	private String areaName;
	
	private List<BoxBusinessExecuteHistory> historyList;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id == null ? null : id.trim();
	}

	public String getBoxUuid() {
		return boxUuid;
	}

	public void setBoxUuid(String boxUuid) {
		this.boxUuid = boxUuid == null ? null : boxUuid.trim();
	}

	public String getBusinessCodeBoss() {
		return businessCodeBoss;
	}

	public void setBusinessCodeBoss(String businessCodeBoss) {
		this.businessCodeBoss = businessCodeBoss == null ? null : businessCodeBoss.trim();
	}

	public String getBusinessCode() {
		return businessCode;
	}

	public void setBusinessCode(String businessCode) {
		this.businessCode = businessCode == null ? null : businessCode.trim();
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName == null ? null : businessName.trim();
	}

	public String getParameterList() {
		return parameterList;
	}

	public void setParameterList(String parameterList) {
		this.parameterList = parameterList == null ? null : parameterList.trim();
	}

	public String getBusinessStatu() {
		return businessStatu;
	}

	public void setBusinessStatu(String businessStatu) {
		this.businessStatu = businessStatu == null ? null : businessStatu.trim();
	}

	public String getAreacode() {
		return areacode;
	}

	public void setAreacode(String areacode) {
		this.areacode = areacode == null ? null : areacode.trim();
	}

	public Integer getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Integer createTime) {
		this.createTime = createTime;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType == null ? null : businessType.trim();
	}

	public String getBoxMac() {
		return boxMac;
	}

	public void setBoxMac(String boxMac) {
		this.boxMac = boxMac == null ? null : boxMac.trim();
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo == null ? null : orderNo.trim();
	}

	public Integer getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Integer updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getFailCount() {
		return failCount;
	}

	public void setFailCount(Integer failCount) {
		this.failCount = failCount;
	}

	public String getProvcode() {
		return provcode;
	}

	public void setProvcode(String provcode) {
		this.provcode = provcode == null ? null : provcode.trim();
	}

	public String getUseridBoss() {
		return useridBoss;
	}

	public void setUseridBoss(String useridBoss) {
		this.useridBoss = useridBoss == null ? null : useridBoss.trim();
	}

	public Date getOrdertimeBoss() {
		return ordertimeBoss;
	}

	public void setOrdertimeBoss(Date ordertimeBoss) {
		this.ordertimeBoss = ordertimeBoss;
	}

	public String getDevicetype() {
		return devicetype;
	}

	public void setDevicetype(String devicetype) {
		this.devicetype = devicetype == null ? null : devicetype.trim();
	}

	public String getUsernameBoss() {
		return usernameBoss;
	}

	public void setUsernameBoss(String usernameBoss) {
		this.usernameBoss = usernameBoss == null ? null : usernameBoss.trim();
	}

	public String getUseraddressBoss() {
		return useraddressBoss;
	}

	public void setUseraddressBoss(String useraddressBoss) {
		this.useraddressBoss = useraddressBoss == null ? null : useraddressBoss.trim();
	}

	public String getContactpersonBoss() {
		return contactpersonBoss;
	}

	public void setContactpersonBoss(String contactpersonBoss) {
		this.contactpersonBoss = contactpersonBoss == null ? null : contactpersonBoss.trim();
	}

	public String getContactmannerBoss() {
		return contactmannerBoss;
	}

	public void setContactmannerBoss(String contactmannerBoss) {
		this.contactmannerBoss = contactmannerBoss == null ? null : contactmannerBoss.trim();
	}

	public String getIpoeid() {
		return ipoeid;
	}

	public void setIpoeid(String ipoeid) {
		this.ipoeid = ipoeid == null ? null : ipoeid.trim();
	}

	public String getIpoepassword() {
		return ipoepassword;
	}

	public void setIpoepassword(String ipoepassword) {
		this.ipoepassword = ipoepassword == null ? null : ipoepassword.trim();
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid == null ? null : userid.trim();
	}

	public String getUseridpassword() {
		return useridpassword;
	}

	public void setUseridpassword(String useridpassword) {
		this.useridpassword = useridpassword == null ? null : useridpassword.trim();
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public List<BoxBusinessExecuteHistory> getHistoryList() {
		return historyList;
	}

	public void setHistoryList(List<BoxBusinessExecuteHistory> historyList) {
		this.historyList = historyList;
	}
}
