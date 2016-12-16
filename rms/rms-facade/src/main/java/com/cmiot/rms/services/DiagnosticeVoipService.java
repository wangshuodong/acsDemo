package com.cmiot.rms.services;

import java.util.Map;

/**
 * VoIP诊断
 * 
 * @author shuang
 */
public interface DiagnosticeVoipService {

	/**
	 * VoIP诊断
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> voipDiagnostics(Map<String, Object> parameter);

}
