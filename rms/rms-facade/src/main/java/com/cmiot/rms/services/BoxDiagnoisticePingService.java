package com.cmiot.rms.services;

import java.util.Map;

public interface BoxDiagnoisticePingService {
	/**
	 * Ping诊断
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> pingDiagnostics(Map<String, Object> parameter);
}
