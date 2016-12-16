package com.cmiot.rms.dao.model;

/**
 * 历史:因业务发展,此对象由原来制造商变为生产商
 */
public class Manufacturer extends BaseBean {
	private String id;// 生产商ID

	private String manufacturerName;// 生产商名称

	private String code;// 生产商编码

	private String makeId;// 制造商ID

	private String makeName;//制造商名称

	public Manufacturer(String id, String manufacturerName, String code, String makeId) {
		super();
		this.id = id;
		this.manufacturerName = manufacturerName;
		this.code = code;
		this.makeId = makeId;
	}

	public Manufacturer() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id == null ? null : id.trim();
	}

	public String getManufacturerName() {
		return manufacturerName;
	}

	public void setManufacturerName(String manufacturerName) {
		this.manufacturerName = manufacturerName == null ? null : manufacturerName.trim();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMakeId() {
		return makeId;
	}

	public void setMakeId(String makeId) {
		this.makeId = makeId;
	}

	public String getMakeName() {
		return makeName;
	}

	public void setMakeName(String makeName) {
		this.makeName = makeName;
	}

}