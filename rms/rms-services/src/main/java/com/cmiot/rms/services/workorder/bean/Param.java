package com.cmiot.rms.services.workorder.bean;

/**
 * 具体的参数
 * @author lili
 *
 */
public class Param {

	@Override
	public String toString() {
		return "Param{" +
				"command='" + command + '\'' +
				", paramKey='" + paramKey + '\'' +
				", defaultV='" + defaultV + '\'' +
				", stitch='" + stitch + '\'' +
				", valueType='" + valueType + '\'' +
				", split='" + split + '\'' +
				", beforeOrAfter='" + beforeOrAfter + '\'' +
				", defaultKey='" + defaultKey + '\'' +
				'}';
	}

	/**
	 * 参数名
	 */
	private String command;
	
	/**
	 * 参数的取值，主要从参数的键值对立面取，
	 * 这里的键对应传入参数的键
	 */
	private String paramKey;
	
	/**
	 * 不需要传输参数的默认值
	 */
	private String defaultV;

	/**
	 * 需要在后面拼接的字符串
	 */
	private String stitch;

	/**
	 * 参数类型
	 */
	private String valueType;

	/**
	 * 字段拆分符号
	 */
	private String split;

	/**
	 * beforeOrAfter表示Command取split的前面的串还是后面的串
	 * before表示取前值
	 * after表示取后值
	 */
	private String beforeOrAfter;

	private String defaultKey;

	public String getDefaultKey() {
		return defaultKey;
	}

	public void setDefaultKey(String defaultKey) {
		this.defaultKey = defaultKey;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getParamKey() {
		return paramKey;
	}

	public void setParamKey(String paramKey) {
		this.paramKey = paramKey;
	}

	public String getDefaultV() {
		return defaultV;
	}

	public void setDefaultV(String defaultV) {
		this.defaultV = defaultV;
	}

	public String getStitch() {
		return stitch;
	}

	public void setStitch(String stitch) {
		this.stitch = stitch;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public String getSplit() {
		return split;
	}

	public void setSplit(String split) {
		this.split = split;
	}

	public String getBeforeOrAfter() {
		return beforeOrAfter;
	}

	public void setBeforeOrAfter(String beforeOrAfter) {
		this.beforeOrAfter = beforeOrAfter;
	}
}
