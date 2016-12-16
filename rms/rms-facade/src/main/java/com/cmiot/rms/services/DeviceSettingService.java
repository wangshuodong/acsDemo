/**
 * 
 */
package com.cmiot.rms.services;

import java.util.Map;

/**
 * @author heping
 *
 */
public interface DeviceSettingService 
{
	/**
	 * 8.1	设置WIFI开启
	 * @param parameter
	 * @return
	 */
	public Map<String,Object> enableSSID(Map<String, Object>parameter);
	
	/**
	 * 8.2	设置WIFI关闭
	 * @param parameter
	 * @return
	 */
	public Map<String,Object> disableSSID(Map<String, Object>parameter);
	
	/**
	 * 8.3	修改SSID密码
	 * @param parameter
	 * @return
	 */
	public Map<String,Object> modifySSIDPwd(Map<String, Object>parameter);
	
	/**
	 * 8.4	恢复出厂设置(网关)
	 * @param parameter
	 * @return
	 */
	public Map<String,Object> factoryReset(Map<String, Object>parameter);
	
	/**
	 * 8.5	重启设备(网关)
	 * @param parameter
	 * @return
	 */
	public Map<String,Object> reboot(Map<String, Object>parameter);
	
	/**
	 * 8.6	账号密码重置
	 * @param parameter
	 * @return
	 */
	public Map<String,Object> resetpppoe(Map<String, Object>parameter);
	
	/**
	 *  机顶盒恢复出厂设置
	 * @param parameter
	 * @return
	 */
	public Map<String,Object> boxFactoryReset(Map<String, Object>parameter);
	
	/**
	 * 机顶盒重启
	 * @param parameter
	 * @return
	 */
	public Map<String,Object> boxReboot(Map<String, Object>parameter);
}
