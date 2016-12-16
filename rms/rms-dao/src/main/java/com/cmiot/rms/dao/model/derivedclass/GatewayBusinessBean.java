/**
 * 
 */
package com.cmiot.rms.dao.model.derivedclass;

import java.util.List;

import com.cmiot.rms.dao.model.GatewayBusinessExecuteHistory;
import com.cmiot.rms.dao.model.GatewayInfo;

/**
 * @author heping 工单管理bean
 */
public class GatewayBusinessBean extends GatewayInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1663000511158915769L;
	
	private String order_no;
	private String business_type;
	private String business_statu;
	private Integer  create_time;
	private String business_code_boss;
	private String areacode;
	private String adsl_account;
	private Integer bandwidth;
	private String adsl_password;
	private Integer failCount;
	//0不能重新执行 1可以重新执行
	private Integer reExecute;

	private Integer  updateTime;

	public Integer getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(Integer bandwidth) {
		this.bandwidth = bandwidth;
	}

	private List<GatewayBusinessExecuteHistory> executeHistoryList;

	public String getOrder_no() {
		return order_no;
	}

	public void setOrder_no(String order_no) {
		this.order_no = order_no;
	}

	public String getBusiness_type() {
		return business_type;
	}

	public void setBusiness_type(String business_type) {
		this.business_type = business_type;
	}

	public String getBusiness_statu() {
		return business_statu;
	}

	public void setBusiness_statu(String business_statu) {
		this.business_statu = business_statu;
	}

	public Integer getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Integer create_time) {
		this.create_time = create_time;
	}

	public String getBusiness_code_boss() {
		return business_code_boss;
	}

	public void setBusiness_code_boss(String business_code_boss) {
		this.business_code_boss = business_code_boss;
	}

	public String getAreacode() {
		return areacode;
	}

	public void setAreacode(String areacode) {
		this.areacode = areacode;
	}

	public String getAdsl_account() {
		return adsl_account;
	}

	public void setAdsl_account(String adsl_account) {
		this.adsl_account = adsl_account;
	}

	public List<GatewayBusinessExecuteHistory> getExecuteHistoryList() {
		return executeHistoryList;
	}

	public void setExecuteHistoryList(List<GatewayBusinessExecuteHistory> executeHistoryList) {
		this.executeHistoryList = executeHistoryList;
	}

	public String getAdsl_password() {
		return adsl_password;
	}

	public void setAdsl_password(String adsl_password) {
		this.adsl_password = adsl_password;
	}

	public Integer getFailCount() {
		return failCount;
	}

	public void setFailCount(Integer failCount) {
		this.failCount = failCount;
	}

	public Integer getReExecute() {
		return reExecute;
	}

	public void setReExecute(Integer reExecute) {
		this.reExecute = reExecute;
	}

	public Integer getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Integer updateTime) {
		this.updateTime = updateTime;
	}

}
