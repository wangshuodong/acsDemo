package com.cmiot.rms.services;

import java.util.Map;

public interface DeviceDiagnoseService {
	/**
	 * ping测试（ihgu）
	 * @param parameter
	 * @return
	 */
	Map<String,Object> pingHguDiagnose(Map<String,Object> parameter);
	
	/**
	 * pppoe仿真测试（ihgu）
	 * @param parameter
	 * @return
	 */
	Map<String,Object> pppoeHguDiagnose(Map<String,Object> parameter);
	
	/**
	 * traceroute诊断测试（ihgu）
	 * @param parameter
	 * @return
	 */
	Map<String,Object> tracerouteHguDiagnose(Map<String,Object> parameter);
	
	/**
	 * 查询ping测试结果(ihgu)
	 * @param parameter
	 * @return
	 */
	Map<String,Object> getHguPingInfo(Map<String,Object> parameter);
	
	/**
	 * 查询pppoe仿真测试结果(ihgu)
	 * @param parameter
	 * @return
	 */
	Map<String,Object> getHguPppoeInfo(Map<String,Object> parameter);
	
	/**
	 * 查询traceroute测试结果(ihgu)
	 * @param parameter
	 * @return
	 */
	Map<String,Object> getHguTracerouteInfo(Map<String,Object> parameter);
	
	/**
	 * ping诊断测试(stb)
	 * @param parameter
	 * @return
	 */
	Map<String,Object> pingStbDiagnose(Map<String,Object> parameter);
	
	/**
	 * traceroute诊断测试(stb)
	 * @param parameter
	 * @return
	 */
	Map<String,Object> tracerouteStbDiagnose(Map<String,Object> parameter);
	/**
	 * 查询ping测试结果(stb)
	 * @param parameter
	 * @return
	 */
	Map<String,Object> getStbPingInfo(Map<String,Object> parameter);
	
	/**
	 * 查询traceroute测试结果(stb)
	 * @param parameter
	 * @return
	 */
	Map<String,Object> getStbTracerouteInfo(Map<String,Object> parameter);
}
