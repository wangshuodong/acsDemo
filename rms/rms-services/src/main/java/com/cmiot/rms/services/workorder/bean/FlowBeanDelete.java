/**
 * 
 */
package com.cmiot.rms.services.workorder.bean;

/**
 * @author lcs
 *
 */
public class FlowBeanDelete extends Flow {
	
	/**
	 * 执行delete操作的命令
	 */
	private String command;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
	
	
	@Override
	public String toString() {
		return "FlowBeanDelete{" +
				", Command='" + command + '\'' +
				'}';
	}
}
