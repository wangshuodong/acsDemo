package com.cmiot.rms.services.workorder.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置参数操作
 * @author lili
 *
 */
public class FlowBeanSet extends Flow {
	
	@Override
	public String toString() {
		return "FlowBeanSet [params=" + params + ", getId()=" + getId() + ", getRef()=" + getRef() + ", toString()="
				+ super.toString() + "]";
	}

	/**
	 * 需要设置的所有参数
	 */
	private List<Param> params = new ArrayList<Param>();

	public List<Param> getParams() {
		return params;
	}

	public void addParam(Param params) {
		this.params.add(params);
	}


}
