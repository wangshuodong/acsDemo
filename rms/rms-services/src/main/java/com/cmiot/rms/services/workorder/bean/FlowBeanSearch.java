package com.cmiot.rms.services.workorder.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * AddObject 操作
 * @author lili
 *
 */
public class FlowBeanSearch extends Flow {

	@Override
	public String toString() {
		return "FlowBeanSearch{" +
				"SplitPath='" + SplitPath + '\'' +
				", list=" + list +
				", SplitStr='" + SplitStr + '\'' +
				", Command='" + Command + '\'' +
				'}';
	}

	/**
	 * 匹配正则表达式
	 */
	private String SplitPath;
	
	/**
	 * 需要匹配的集合
	 */
	private List<FlowBeanMatch> list =  new ArrayList<FlowBeanMatch>();
	
	/**
	 * 节点截取的依据串
	 */
	private String SplitStr;

	/**
	 * 指令
	 */
	private String Command;

	public String getSplitPath() {
		return SplitPath;
	}

	public void setSplitPath(String splitPath) {
		SplitPath = splitPath;
	}

	public List<FlowBeanMatch> getList() {
		return list;
	}

	public void setList(List<FlowBeanMatch> list) {
		this.list = list;
	}

	public String getSplitStr() {
		return SplitStr;
	}

	public void setSplitStr(String splitStr) {
		SplitStr = splitStr;
	}

	public String getCommand() {
		return Command;
	}

	public void setCommand(String command) {
		Command = command;
	}

	public void addList(FlowBeanMatch flowBeanMatch){
		this.list.add(flowBeanMatch);
	}
}
