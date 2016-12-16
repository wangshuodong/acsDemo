/**
 * 
 */
package com.cmiot.rms.services;

import java.util.Map;

/**
 * @author mike
 *
 */
public interface WorkOrderStandardInterface {
	
	/**
     * PBOSS北向接口-新装信息同步
     * @param parameter
     * @return
     */
    Map<String,Object> addNewInstallation(Map<String,Object> parameter);
	
    
    
    
}
