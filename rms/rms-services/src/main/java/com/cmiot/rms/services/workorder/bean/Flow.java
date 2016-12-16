package com.cmiot.rms.services.workorder.bean;

/**
 * 所有的操作的一个抽象
 * @author lili
 * 
 */
public abstract class Flow {
	
	/**
	 * 操作的ID
	 */
	private String id;
	
	/**
	 * 操作关联的Flow id,如果没有就为空
	 */
	private String ref;

	/**
	 * 判断关联的查询Flow id,如果没有就为空
	 */
	private String refSearch;

	/**
	 * 根据某一步的执行结果来判断是否需要执行本FLOW，如果结果为空，则需要执行本步骤，如果结果不为空，则不需要执行本步骤
	 */
	private String isNeedExcute;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getRefSearch() {
		return refSearch;
	}

	public void setRefSearch(String refSearch) {
		this.refSearch = refSearch;
	}

	public String getIsNeedExcute() {
		return isNeedExcute;
	}

	public void setIsNeedExcute(String isNeedExcute) {
		this.isNeedExcute = isNeedExcute;
	}
}
