package com.cmiot.rms.services;

import java.util.Map;

/**
 * 指令发送接口类
 * Created by wangzhen on 2016/4/11.
 */
public interface SendInstructService {
	/**
	 * 获取路由器LAN口信息，已经无线连接信息
	 * 
	 * @param parameter
	 * @return
	 */
	public Map<String, Object> getLANDeviceInfo(Map<String, Object> parameter);

	/**
	 * 获取CUP、内存占用比例
	 * 
	 * @param parameter
	 * @return
	 */
	public Map<String, Object> getUsageValue(Map<String, Object> parameter);

	/**
	 * 网络诊断接口
	 * 
	 * @param parameter
	 * @return
	 */
	public Map<String, Object> diagnose(Map<String, Object> parameter);

	/**
	 * 网关Ping地址平均访问时延
	 * 
	 * @param parameter
	 * @return
	 */
	public Map<String, Object> getPingAddressInfo(Map<String, Object> parameter);

	/**
	 * 线路详细信息
	 * 
	 * @param parameter
	 * @return
	 */
	public Map<String, Object> lineDetails(Map<String, Object> parameter);

	/**
	 * 获取网关下挂设备
	 * @param parameter
	 * @return
     */
	Map<String, Object> getGatewayLANDevice(Map<String, Object> parameter);
}
