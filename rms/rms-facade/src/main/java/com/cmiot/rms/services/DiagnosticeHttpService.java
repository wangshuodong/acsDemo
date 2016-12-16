package com.cmiot.rms.services;

import java.util.Map;

/**
 * HTTP下载仿真
 * 
 * @author shuang
 */
public interface DiagnosticeHttpService {


	/**
	 * HTTP下载仿真诊断
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> httpDiagnostics(Map<String, Object> parameter);
}
