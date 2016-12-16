package com.cmiot.rms.dao.model;

public class GatewayAdslAccount extends BaseBean{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id ;
	private String gatewayMAC;
	private String adslAccount;
	private int  createTime;
	private int areaId;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getGatewayMAC() {
		return gatewayMAC;
	}
	public void setGatewayMAC(String gatewayMAC) {
		this.gatewayMAC = gatewayMAC;
	}
	public String getAdslAccount() {
		return adslAccount;
	}
	public void setAdslAccount(String adslAccount) {
		this.adslAccount = adslAccount;
	}
	public int getCreateTime() {
		return createTime;
	}
	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}
	public int getAreaId() {
		return areaId;
	}
	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}

}
