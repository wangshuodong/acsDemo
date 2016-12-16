package com.cmiot.rms.services;

import java.util.Map;

/**
 * DHCP仿真
 * 
 * @author shuang
 */
public interface DiagnosticsDhcpService {

	/**
	 * DHCP仿真诊断
	 * 
	 * @param parameter
	 * @return
	 */
	Map<String, Object> dhcpDiagnostics(Map<String, Object> parameter);

}
