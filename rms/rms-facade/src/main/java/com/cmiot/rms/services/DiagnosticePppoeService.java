package com.cmiot.rms.services;

import java.util.Map;

/**
 * PPPoE仿真
 * 
 * @author shuang
 */
public interface DiagnosticePppoeService {
	/**
	 * PPPoE仿真诊断
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> pppoeDiagnostics(Map<String, Object> parameter);
}
