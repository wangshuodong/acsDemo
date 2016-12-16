package com.cmiot.rms.dao.model;

/**
 * 历史:因业务发展,此对象由原来生产商变为OUI
 */
public class Factory extends BaseBean {
	private String id;// OUIID

	private String factoryCode;// OUI编码

	private String factoryName;// OUI名称

	private String manufacturerId;// 生产商ID

	private String manufacturerName;// 生产商名称

	private String oui;

	private String makeId;// 制造商ID

	public Factory(String id, String factoryCode, String factoryName, String manufacturerId) {
		super();
		this.id = id;
		this.factoryCode = factoryCode;
		this.factoryName = factoryName;
		this.manufacturerId = manufacturerId;
	}

	public Factory() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id == null ? null : id.trim();
	}

	public String getFactoryCode() {
		return factoryCode;
	}

	public void setFactoryCode(String factoryCode) {
		this.factoryCode = factoryCode == null ? null : factoryCode.trim();
	}

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName == null ? null : factoryName.trim();
	}

	public String getManufacturerId() {
		return manufacturerId;
	}

	public void setManufacturerId(String manufacturerId) {
		this.manufacturerId = manufacturerId == null ? null : manufacturerId.trim();
	}

	public String getManufacturerName() {
		return manufacturerName;
	}

	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName;
	}

	public String getOui() {
		return oui;
	}

	public void setOui(String oui) {
		this.oui = oui;
	}

	public String getMakeId() {
		return makeId;
	}

	public void setMakeId(String makeId) {
		this.makeId = makeId;
	}

}