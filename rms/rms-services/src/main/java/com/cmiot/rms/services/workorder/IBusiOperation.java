package com.cmiot.rms.services.workorder;

import java.util.Map;

/**
 * 业务的执行
 * @author lili
 *
 */
public interface IBusiOperation {
	
	
	/**
	 * 执行模板业务流程
	 * @param xmlTemplate 业务模板
	 * @param paramS 流程执行参数
	 * @return
	 */
	public Map<String,String> excute(String xmlTemplate,Map<String,Object> paramS);

}
