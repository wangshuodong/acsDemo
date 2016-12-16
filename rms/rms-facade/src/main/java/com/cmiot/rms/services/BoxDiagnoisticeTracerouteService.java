package com.cmiot.rms.services;

import java.util.Map;

/**
 * Traceroute诊断
 * 
 * @author shuang
 */
public interface BoxDiagnoisticeTracerouteService {
	/**
	 * Traceroute诊断
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> tracerouteDiagnostics(Map<String, Object> parameter);
}
