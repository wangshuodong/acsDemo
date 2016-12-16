package com.cmiot.rms.services.workorder.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 子流程对象 ，包含有FlowBeanAdd 和 FlowBeanSET
 * @author lili 
 *
 */
public class FlowBean extends Flow {
	
	/**
	 * 所有的操作执行步骤
	 */
	private List<Flow> flows = new ArrayList<Flow>();

	public List<Flow> getFlows() {
		return flows;
	}
	
	public void addFlow(Flow bean) {
		flows.add(bean);
	}
	

}
