package com.cmiot.rms.dao.model;

public class MakeInfo {
	private String id;// 制造商ID

	private String makeName;// 制造商名称

	public MakeInfo(String id, String makeName) {
		super();
		this.id = id;
		this.makeName = makeName;
	}

	public MakeInfo() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id == null ? null : id.trim();
	}

	public String getMakeName() {
		return makeName;
	}

	public void setMakeName(String makeName) {
		this.makeName = makeName == null ? null : makeName.trim();
	}
}