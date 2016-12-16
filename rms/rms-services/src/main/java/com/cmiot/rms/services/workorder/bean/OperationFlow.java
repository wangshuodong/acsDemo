package com.cmiot.rms.services.workorder.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 整个业务的操作流程
 * @author lili
 *
 */
public class OperationFlow {
	
	@Override
	public String toString() {
		return "OperationFlow [flows=" + flows + "serviceName="+serviceName+"]";
	}
	
	private String operationReturn;
	
	private String serviceName;

	/**
	 * 整个流程的操作步骤
	 */
	private List<Flow> flows = new ArrayList<Flow>();

	public List<Flow> getFlows() {
		return flows;
	}
	
	public void addFlow(Flow bean) {
		flows.add(bean);
	}
	
	public String getOperationReturn() {
		return operationReturn;
	}

	public void setOperationReturn(String operationReturn) {
		this.operationReturn = operationReturn;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	

}
