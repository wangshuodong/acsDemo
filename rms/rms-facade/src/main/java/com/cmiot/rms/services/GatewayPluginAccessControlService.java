package com.cmiot.rms.services;

import java.util.Map;

/**
 * 插件API权限管理
 * 
 * @author shuang
 */
public interface GatewayPluginAccessControlService {
	
	/**
	 * 根据插件名称添加插件
	 * @param map
	 * @return
	 */
	Map<String, Object> addPluginByNume(Map<String, Object> parameter);

	/**
	 * 查询插件运行环境权限控制能力集
	 * 
	 * @param map
	 * @return
	 */
	Map<String, Object> queryPluginCapabilitySet(Map<String, Object> map);

	/**
	 * 查询插件权限控制权限列表
	 * 
	 * @param map
	 * @return
	 */
	Map<String, Object> queryPluginApiList(Map<String, Object> map);

	/**
	 * 添加插件
	 * 
	 * @param map
	 * @return
	 */
	Map<String, Object> addPluginApi(Map<String, Object> parameter);

	/**
	 * 删除插件
	 * 
	 * @param map
	 * @return
	 */
	Map<String, Object> delPluginApi(Map<String, Object> map);

	/**
	 * 查询插件运行环境
	 * 
	 * @param params
	 * @return
	 */
	Map<String, Object> queryPluginExecEnv(Map<String, Object> params);
}
