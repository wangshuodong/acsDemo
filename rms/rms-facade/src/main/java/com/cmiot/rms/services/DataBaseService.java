/**
 * 
 */
package com.cmiot.rms.services;

import java.util.Map;

/**
 * @author heping
 *
 */
public interface DataBaseService 
{		
	public Map<String, Object> restoreDatabase(Map<String, Object>params) ;
	
	public Map<String, Object> queryList(Map<String, Object>params);
	
	public Map<String, Object> addBackupTask(Map<String, Object>params);
}
