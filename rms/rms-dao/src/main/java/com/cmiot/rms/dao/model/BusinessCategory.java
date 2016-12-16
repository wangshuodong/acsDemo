package com.cmiot.rms.dao.model;

public class BusinessCategory extends BaseBean{

	private String id;
	private String businessCode;
	private String businessName;
	private String deviceModel;
	private int createDate;
	private String businessTemplate;
	private String factoryCode;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public String getBusinessCode() {
		return businessCode;
	}
	public void setBusinessCode(String businessCode) {
		this.businessCode = businessCode;
	}
	public String getDeviceModel() {
		return deviceModel;
	}
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public String getBusinessTemplate() {
		return businessTemplate;
	}
	public void setBusinessTemplate(String businessTemplate) {
		this.businessTemplate = businessTemplate;
	}
	public int getCreateDate() {
		return createDate;
	}
	public void setCreateDate(int createDate) {
		this.createDate = createDate;
	}
	public String getFactoryCode() {
		return factoryCode;
	}
	public void setFactoryCode(String factoryCode) {
		this.factoryCode = factoryCode;
	}
	
}
