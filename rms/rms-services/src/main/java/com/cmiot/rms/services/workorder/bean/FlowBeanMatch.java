package com.cmiot.rms.services.workorder.bean;

/**
 * AddObject 操作
 * @author lili
 *
 */
public class FlowBeanMatch extends Flow {

	@Override
	public String toString() {
		return "FlowBeanMatch{" +
				"Pattern='" + Pattern + '\'' +
				", MatchValue='" + MatchValue + '\'' +
				", leaf='" + leaf + '\'' +
				'}';
	}

	/**
	 * 匹配正则表达式
	 */
	private String Pattern;
	
	/**
	 * 匹配的值-
	 */
	private String MatchValue;

	/**
	 * 路径的叶子节点
	 */
	private String leaf;



	public String getPattern() {
		return Pattern;
	}

	public void setPattern(String pattern) {
		Pattern = pattern;
	}

	public String getMatchValue() {
		return MatchValue;
	}

	public void setMatchValue(String matchValue) {
		MatchValue = matchValue;
	}

	public String getLeaf() {
		return leaf;
	}

	public void setLeaf(String leaf) {
		this.leaf = leaf;
	}
}
