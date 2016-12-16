package com.cmiot.rms.services.workorder.bean;

/**
 * AddObject 操作
 * @author lili
 *
 */
public class FlowBeanAdd extends Flow {
	
	@Override
	public String toString() {
		return "FlowBeanAdd [command=" + command + ", ParamKey=" + ParamKey + ", getId()=" + getId() + ", getRef()="
				+ getRef() + ", toString()=" + super.toString() + "]";
	}
	
	/**
	 * 是否先查询节点
	 */
	private String searchFirst;
	
	/**
	 * 缓存中查找是否已有
	 */
	private String cached;

	/**
	 * 执行Add操作的命令
	 */
	private String command;
	
	/**
	 * 传入参数的键值
	 */
	private String ParamKey;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getParamKey() {
		return ParamKey;
	}

	public void setParamKey(String paramKey) {
		ParamKey = paramKey;
	}
	
	public String getSearchFirst() {
		return searchFirst;
	}

	public void setSearchFirst(String searchFirst) {
		this.searchFirst = searchFirst;
	}
	
	public String getCached() {
		return cached;
	}

	public void setCached(String cached) {
		this.cached = cached;
	}

}
