/**
 * 
 */
package com.cmiot.rms.services.constants;

/**
 * @author heping
 *
 */
public class Constants {

	/**
	 * PBOSS接口:执行成功
	 */
	public static final Integer SUCCESS = 0;
	
	/**
	 * PBOSS接口：设备不存在
	 */
	public static final Integer FAIL = 1;
	
	/**
	 * PBOSS接口：其他错误
	 */
	public static final Integer OTHER = 9999;
	
	
	/**
	 * 网关恢复出厂设置 rpc 方法
	 */
	public static final String FACTORYRESET = "FactoryReset";
	
	/**
	 * 网关重启 rpc方法
	 */
	public static final String REBOOT  = "Reboot";
	
	/**
	 * 控制网关重启、恢复出厂 锁前缀。
	 */
	public static final String LOCK_PREFIX = "R-F-";

}
