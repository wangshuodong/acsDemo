package com.cmiot.rms.services.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.alibaba.fastjson.JSON;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.cache.TemporaryObject;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.LogTypeEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.dao.mapper.DiagnoseLogMapper;
import com.cmiot.rms.dao.mapper.DiagnoseThresholdValueMapper;
import com.cmiot.rms.dao.model.DiagnoseLog;
import com.cmiot.rms.dao.model.DiagnoseThresholdValue;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.LogManagerService;
import com.cmiot.rms.services.instruction.InstructionMethodService;

/**
 * 诊断相关工具类
 * 
 * @author chuan
 * @version 
 */
public class PbossDiagnosticsUtil {
	
	
	/**
	 * 删除创建的WAN连接
	 * 
	 * @param parameter
	 *            参数集
	 * @param gi
	 *            网关对象
	 * @param ims
	 *            请求ACS实现类
	 * @param log
	 *            日志对象
	 * @param logName
	 *            日志名称
	 * @param path
	 *            节点路径
	 * @param statue
	 *            是否删除状态
	 */
	public static void deleteCreateWanConnect(Map<String, Object> parameter, GatewayInfo gi, InstructionMethodService ims, Logger log, String logName, String path, Boolean statue) {
		if (statue) {// 删除创建的WAN连接
			long dst = System.currentTimeMillis();
			int rdb = PbossDiagnosticsUtil.deleteObject(gi, ims, path + ".");
			log.info("LogId:{}" + logName + "网关删除创建的WAN连接节点地址:{}节点返回为:{}消耗时间:{}", parameter.get("loguuid"), path, rdb, (System.currentTimeMillis() - dst));
		}
	}

	/**
	 * 创建DHCP的WAN连接
	 * 
	 * @param parameter
	 *            参数集
	 * @param gi
	 *            网关对象
	 * @param ims
	 *            请求ACS实现类
	 * @param log
	 *            日志对象
	 * @param logName
	 *            日志名称
	 * @param serviceList
	 *            规定在本WAN连接承载的什么业务列表
	 * @param path
	 *            节点路径
	 * @return
	 */
	public static Map<String, Object> createDHCPWanConnect(Map<String, Object> rm, Map<String, Object> parameter, GatewayInfo gi, InstructionMethodService ims, Logger log, String logName, String serviceList, String path) {
		Map<String, Object> rb = new HashMap<String, Object>();
		rb.put("status", "3");
		rb.put("delFlag", Boolean.FALSE);
		log.info("LogId:{}" + logName + "创建WAN连接的原始节点路径:{}", parameter.get("loguuid"), path);
		if (!checkPattern(path, Pattern.compile("InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.WANIPConnection.[\\d]+"))) {
			// 添加InternetGatewayDevice.WANDevice.网关返回Invalid arguments错误信息,所以默认此值为1
			int rootnum = 1;// AddObject(gi, ims, "InternetGatewayDevice.WANDevice.");
			// log.info("LogId:{}" + logName + "网关添加InternetGatewayDevice.WANDevice.对象返回节点值:{}", parameter.get("loguuid"), rootnum);
			if (rootnum > 0) {
				int elemnum = AddObject(gi, ims, "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice.");
				log.info("LogId:{}" + logName + "网关添加InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice.对象返回节点值:{}", parameter.get("loguuid"), elemnum);
				if (elemnum > 0) {
					int childnum = AddObject(gi, ims, "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANIPConnection.");
					log.info("LogId:{}" + logName + "网关添加InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANIPConnection.对象返回节点值:{}", parameter.get("loguuid"), childnum);
					if (childnum > 0) {
						path = "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANIPConnection." + childnum;
					}
				}
			}
		}
		if (StringUtils.isNotBlank(path)) {
			if (path.lastIndexOf(".") > 70) {
				path = path.substring(0, path.lastIndexOf(".") + 1);
				log.info("LogId:{}" + logName + "创建WAN连接的原始节点路径:{}", parameter.get("loguuid"), path);
				int childnum = AddObject(gi, ims, path);
				log.info("LogId:{}" + logName + path + "对象返回节点值:{}", parameter.get("loguuid"), childnum);
				if (childnum > 0) {
					path = path + childnum;
					log.info("LogId:{}" + "解释后创建AN连接的节点路径:{}", parameter.get("loguuid"), path);
					packageDHCPParameterValueStruct(rm, parameter, gi, path, rb, ims, log, logName, serviceList);
				}
			}
		}
		return rb;
	}

	/**
	 * 封装设置添加参数集
	 * 
	 * @param parameter
	 *            参数集
	 * @param gi
	 *            网关对象
	 * @param path
	 *            节点路径
	 * @param rb
	 *            返回对象
	 * @param ims
	 *            请求ACS实现类
	 * @param log
	 *            日志对象
	 * @param logName
	 *            日志名称
	 * @param serviceList
	 *            规定在本WAN连接承载的什么业务列表
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void packageDHCPParameterValueStruct(Map<String, Object> rm, Map<String, Object> parameter, GatewayInfo gi, String path, Map<String, Object> rb, InstructionMethodService ims, Logger log, String logName, String serviceList) {
		List<ParameterValueStruct> pvslist = new ArrayList<ParameterValueStruct>();
		// String path = "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANIPConnection." + childnum;
		ParameterValueStruct vlanMode = new ParameterValueStruct();
		// VLAN启用模式，以下值之一：0表示不启用1表示保持原来的标志位值2表示重写VLAN
		vlanMode.setName(path + ".X_CMCC_VLANMode");
		vlanMode.setValue(2);
		vlanMode.setReadWrite(true);
		vlanMode.setValueType("unsignedInt");
		pvslist.add(vlanMode);
		// WAN连接的VLANID
		ParameterValueStruct vLANIDMark = new ParameterValueStruct();
		vLANIDMark.setName(path + ".X_CMCC_VLANIDMark");
		vLANIDMark.setValue(parameter.get("vlanidmark"));
		vLANIDMark.setReadWrite(true);
		vLANIDMark.setValueType("unsignedInt");
		pvslist.add(vLANIDMark);
		// WAN连接的类型，如下值之一：IP_Routed IP_Bridged
		ParameterValueStruct connectionType = new ParameterValueStruct();
		connectionType.setName(path + ".ConnectionType");
		connectionType.setValue("IP_Routed");
		connectionType.setReadWrite(true);
		connectionType.setValueType("string");
		pvslist.add(connectionType);
		// AddressingType
		ParameterValueStruct username = new ParameterValueStruct();
		username.setName(path + ".AddressingType");
		username.setValue("DHCP");
		username.setReadWrite(true);
		username.setValueType("string");
		pvslist.add(username);
		// 规定在本WAN连接承载的什么业务列表，以逗号分割。目前定义：“TR069”,”INTERNET”,”VOIP”,”，”OTHER”。
		ParameterValueStruct pvsServiceList = new ParameterValueStruct();
		pvsServiceList.setName(path + ".X_CMCC_ServiceList");
		pvsServiceList.setValue(serviceList);
		pvsServiceList.setReadWrite(true);
		pvsServiceList.setValueType("string");
		pvslist.add(pvsServiceList);
		// 和LAN侧的绑定关(暂时不设置)
		// ParameterValueStruct lanInterface = new ParameterValueStruct();
		// lanInterface.setName(path + ".X_CMCC_LanInterface");
		// lanInterface.setValue();
		// lanInterface.setReadWrite(true);
		// lanInterface.setValueType("string");
		// pvslist.add(lanInterface);
		// WAN连接是否启用。TRUE表示启用，FALSE表示禁用
		ParameterValueStruct enable = new ParameterValueStruct();
		enable.setName(path + ".Enable");
		enable.setValue(Boolean.TRUE);
		enable.setReadWrite(true);
		enable.setValueType("boolean");
		pvslist.add(enable);
		long st = System.currentTimeMillis();
		Map<String, Object> result = ims.setParameterValueErrorCode(gi.getGatewayMacaddress(), pvslist);
		boolean isresult = false;
		if(result.get("result").toString().equals("0")){
			isresult = true;
		}
		rm.putAll(result);
		log.info("LogId:{}" + logName + "网关添加{}个对象参数值返回状态:{}消耗时间:{}", parameter.get("loguuid"), pvslist.size(), result, (System.currentTimeMillis() - st));
		PbossDiagnosticsUtil.deleteCreateWanConnect(parameter, gi, ims, log, logName, path, (boolean) !isresult);// 删除创建的WAN连接
		rb.put("delFlag", Boolean.TRUE);
		rb.put("status", isresult ? "2" : "3");
		rb.put("path", path);
	}

	/**
	 * 创建PPPoE的WAN连接
	 * 
	 * @param parameter
	 *            参数集
	 * @param gi
	 *            网关对象
	 * @param ims
	 *            请求ACS实现类
	 * @param log
	 *            日志对象
	 * @param logName
	 *            日志名称
	 * @param serviceList
	 *            规定在本WAN连接承载的什么业务列表
	 * @param path
	 *            节点路径
	 * @return
	 */
	public static Map<String, Object> createPPPoEWanConnect(Map<String, Object> rm, Map<String, Object> parameter, GatewayInfo gi, InstructionMethodService ims, Logger log, String logName, String serviceList, String path) {
		Map<String, Object> rb = new HashMap<String, Object>();
		rb.put("status", "3");
		rb.put("delFlag", Boolean.FALSE);
		if (!checkPattern(path, Pattern.compile("InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.WANPPPConnection.[\\d]+"))) {
			// 添加InternetGatewayDevice.WANDevice.网关返回Invalid arguments错误信息,所以默认此值为1
			int rootnum = 1;// AddObject(gi, ims, "InternetGatewayDevice.WANDevice.");
			// log.info("LogId:{}" + logName + "网关添加InternetGatewayDevice.WANDevice.对象返回节点值:{}", parameter.get("loguuid"), rootnum);
			if (rootnum > 0) {
				int elemnum = AddObject(gi, ims, "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice.");
				log.info("LogId:{}" + logName + "网关添加InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice.对象返回节点值:{}", parameter.get("loguuid"), elemnum);
				if (elemnum > 0) {
					int childnum = AddObject(gi, ims, "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANPPPConnection.");
					log.info("LogId:{}" + logName + "网关添加InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANPPPConnection.对象返回节点值:{}", parameter.get("loguuid"), childnum);
					if (childnum > 0) {
						path = "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANPPPConnection." + childnum;
					}
				}
			}
		}
		if (StringUtils.isNotBlank(path)) {
			if (path.lastIndexOf(".") > 70) {
				path = path.substring(0, path.lastIndexOf(".") + 1);
				log.info("LogId:{}" + logName + "创建WAN连接的原始节点路径:{}", parameter.get("loguuid"), path);
				int childnum = AddObject(gi, ims, path);
				log.info("LogId:{}" + logName + path + "对象返回节点值:{}", parameter.get("loguuid"), childnum);
				if (childnum > 0) {
					path = path + childnum;
					log.info("LogId:{}" + "解释后创建AN连接的节点路径:{}", parameter.get("loguuid"), path);
					packagePPPoEParameterValueStruct(rm, parameter, gi, path, rb, ims, log, logName, serviceList);
				}
			}
		}
		return rb;
	}

	/**
	 * 封装设置添加参数集
	 * 
	 * @param parameter
	 *            参数集
	 * @param gi
	 *            网关对象
	 * @param path
	 *            节点路径
	 * @param rb
	 *            返回对象
	 * @param ims
	 *            请求ACS实现类
	 * @param log
	 *            日志对象
	 * @param logName
	 *            日志名称
	 * @param serviceList
	 *            规定在本WAN连接承载的什么业务列表
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void packagePPPoEParameterValueStruct(Map<String, Object> rm, Map<String, Object> parameter, GatewayInfo gi, String path, Map<String, Object> rb, InstructionMethodService ims, Logger log, String logName, String serviceList) {
		List<ParameterValueStruct> pvslist = new ArrayList<ParameterValueStruct>();
		// String path = "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANPPPConnection." + childnum;
		ParameterValueStruct vlanMode = new ParameterValueStruct();
		// VLAN启用模式，以下值之一：0表示不启用1表示保持原来的标志位值2表示重写VLAN
		vlanMode.setName(path + ".X_CMCC_VLANMode");
		vlanMode.setValue(2);
		vlanMode.setReadWrite(true);
		vlanMode.setValueType("unsignedInt");
		pvslist.add(vlanMode);
		// WAN连接的VLANID
		ParameterValueStruct vLANIDMark = new ParameterValueStruct();
		vLANIDMark.setName(path + ".X_CMCC_VLANIDMark");
		vLANIDMark.setValue(parameter.get("vlanidmark"));
		vLANIDMark.setReadWrite(true);
		vLANIDMark.setValueType("unsignedInt");
		pvslist.add(vLANIDMark);
		// WAN连接的类型，如下值之一：PPPoE_Routed;PPPoE_Bridged
		ParameterValueStruct connectionType = new ParameterValueStruct();
		connectionType.setName(path + ".ConnectionType");
		connectionType.setValue("PPPoE_Routed");
		connectionType.setReadWrite(true);
		connectionType.setValueType("string");
		pvslist.add(connectionType);
		// PPPoE鉴权的用户名
		ParameterValueStruct username = new ParameterValueStruct();
		username.setName(path + ".Username");
		username.setValue(parameter.get("username"));
		username.setReadWrite(true);
		username.setValueType("string");
		pvslist.add(username);
		// PPPoE鉴权的密码
		ParameterValueStruct password = new ParameterValueStruct();
		password.setName(path + ".Password");
		password.setValue(parameter.get("password"));
		password.setReadWrite(true);
		password.setValueType("string");
		pvslist.add(password);
		// 规定在本WAN连接承载的什么业务列表，以逗号分割。目前定义：“TR069”,”INTERNET”,”VOIP”,”，”OTHER”。
		ParameterValueStruct serviceListpvs = new ParameterValueStruct();
		serviceListpvs.setName(path + ".X_CMCC_ServiceList");
		serviceListpvs.setValue(serviceList);
		serviceListpvs.setReadWrite(true);
		serviceListpvs.setValueType("string");
		pvslist.add(serviceListpvs);
		// 和LAN侧的绑定关(暂时不设置)
		// ParameterValueStruct lanInterface = new ParameterValueStruct();
		// lanInterface.setName(path + ".X_CMCC_LanInterface");
		// lanInterface.setValue();
		// lanInterface.setReadWrite(true);
		// lanInterface.setValueType("string");
		// pvslist.add(lanInterface);
		// WAN连接是否启用。TRUE表示启用，FALSE表示禁用
		ParameterValueStruct enable = new ParameterValueStruct();
		enable.setName(path + ".Enable");
		enable.setValue(Boolean.TRUE);
		enable.setReadWrite(true);
		enable.setValueType("boolean");
		pvslist.add(enable);
		long st = System.currentTimeMillis();
		boolean isresult = false;
		Map<String, Object> result = ims.setParameterValueErrorCode(gi.getGatewayMacaddress(), pvslist);
		if(result.get("result").toString().equals("0")){
			isresult = true;
		}
		rm.putAll(result);
		
		log.info("LogId:{}" + logName + "网关添加{}个对象参数值返回状态:{}消耗时间:{}", parameter.get("loguuid"), pvslist.size(), isresult, (System.currentTimeMillis() - st));
		PbossDiagnosticsUtil.deleteCreateWanConnect(parameter, gi, ims, log, logName, path, (boolean) !isresult);// 删除创建的WAN连接
		rb.put("delFlag", Boolean.TRUE);
		rb.put("status", isresult ? "2" : "3");
		rb.put("path", path);
	}


	/**
	 * 获取Wan连接的状态与路径
	 * 
	 * @param parameter
	 *            参数集
	 * @param gi
	 *            网关对象
	 * @param ims
	 *            请求ACS实现类
	 * @param pattern
	 *            正则
	 * @param log
	 *            日志对象
	 * @param logName
	 *            日志名称
	 * @param serviceList
	 *            规定在本WAN连接承载的什么业务列表
	 * @param connectionType
	 *            WAN连接的类型(0:PPPoE_Routed;1:IP_Routed)
	 * @param addressingType
	 *            IPv4地址分配方式(为空则无要求)
	 * @return {
	 *         status->0:未获取网关WAN连接信息;1:无WAN连接信息;2:获取WAN连接成功;
	 *         path:WAN连接地址;
	 *         delFlag:是否要删除标识
	 *         }
	 */
	public static Map<String, Object> getWanConnectStatusAndPath(Map<String, Object> rm, Map<String, Object> parameter, GatewayInfo gi, InstructionMethodService ims, Pattern pattern, Logger log, String logName, String serviceList, String[] connectionType, String addressingType) {
		Map<String, Object> rb = new HashMap<String, Object>();
		rb.put("status", "0");// 目前不考虑网关无此InternetGatewayDevice.WANDevice.节点情况,所以网关返回为空就认为网关超时或未正常获取数据信息
		rb.put("delFlag", Boolean.FALSE);
		long wst = System.currentTimeMillis();
		Map<String, Object> result = ims.getParameterNamesErrorCode(gi.getGatewayMacaddress(), "InternetGatewayDevice.WANDevice.", false);
		log.info("LogId:{}" + logName + "获取网关InternetGatewayDevice.WANDevice.返回数据:{}验证参数消耗时间:{}", parameter.get("loguuid"), result, (System.currentTimeMillis() - wst));
		if (null != result && result.size() > 0) {
			rm.putAll(result);
			if(null != result.get("result") && result.get("result").toString().equals("0")){
				result.remove("result");
				rb.put("status", "1");// 网关无正则验证的节点路径,所以需要创建Wan连接再进行诊断
				List<String> addressList = PbossDiagnosticsUtil.getNamesList(result, pattern);
				log.info("LogId:{}" + logName + "获取WAN连接节点路径:{}", parameter.get("loguuid"), addressList);
				if (null != addressList && addressList.size() > 0) {
					long wanst = System.currentTimeMillis();
					result = ims.getParameterValuesErrorCode(gi.getGatewayMacaddress(), addressList);
					log.info("LogId:{}" + logName + "获取WAN连接数据:{}消耗时间:{}", parameter.get("loguuid"), result, (System.currentTimeMillis() - wanst));
					if (null != result && result.size() > 0) {
						rm.clear();
						rm.putAll(result);
						if(null != result.get("result") && result.get("result").toString().equals("0")){
							result.remove("result");
							// 首先考虑pppoe当中有WAN连接
							String rg = "InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.WANPPPConnection.[\\d]+.(X_CMCC_ServiceList|ConnectionType|Enable|ConnectionStatus)";
							log.info("LogId:{}" + logName + "首先查询PPoE当中是否有WAN连接,进入根据正则:{}获取PPPoEWAN状态方法", parameter.get("loguuid"), rg);
							pattern = Pattern.compile(rg);
							// 根据上面正则获取出PPPOEWAN连接是否存在
							if (PbossDiagnosticsUtil.getServiceListStatus(result, pattern, rb, parameter, log, logName, serviceList, connectionType[0])) {
								// 如果不存在PPPOEWAN连接则查询是否存在IPWAN连接,如果IPWAN连接也不存则返回status为1,需要创建Wan连接再进行诊断
								rg = "InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.WANIPConnection.[\\d]+.(X_CMCC_ServiceList|ConnectionType|AddressingType|Enable|ConnectionStatus)";
								log.info("LogId:{}" + logName + "PPoE当中无WAN连接,所以进入根据正则:{}获取IPWAN状态方法", parameter.get("loguuid"), rg);
								pattern = Pattern.compile(rg);
								PbossDiagnosticsUtil.getStandardValueStatus(result, pattern, rb, parameter, log, logName, serviceList, connectionType[1], addressingType);
							}
						}
					}
				}
			}
		}else{
			rm.put("result", -1);
		}
		return rb;
	}

	/**
	 * 根据正则获取不同WAN状态
	 * 
	 * @param result
	 *            网关获取的数据
	 * @param pattern
	 *            正则
	 * @param rb
	 *            返回状态
	 * @param serviceList
	 *            规定在本WAN连接承载的什么业务列表
	 * @param parameter
	 *            参数集
	 * @param log
	 *            日志对象
	 * @param logName
	 *            日志名称
	 * @param serviceList
	 *            规定在本WAN连接承载的什么业务列表
	 * @param connectionType
	 *            WAN连接的类型
	 * @return
	 */
	private static Boolean getServiceListStatus(Map<String, Object> result, Pattern pattern, Map<String, Object> rb, Map<String, Object> parameter, Logger log, String logName, String serviceList, String connectionType) {
		log.info("LogId:{}" + logName + "即将从:{}获取相应数据", parameter.get("loguuid"), result);
		Set<String> set = new HashSet<String>();// 去重复数据
		for (Map.Entry<String, Object> entry : result.entrySet()) {
			String node = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
			if (StringUtils.isNotBlank(node)) {
				set.add(node);
			}
			log.info("LogId:{}" + logName + "将满足正则数据的节点地址:{} 添加到 Set集合当中去重", parameter.get("loguuid"), node);
		}
		boolean istrue = true;
		String mapServiceList = "", mapConnectionType = "", mapConnectionStatus = "";
		log.info("LogId:{}" + logName + "即将遍历取出Set集合当中节点值,当前集合大小:{} 节点集为:{}", parameter.get("loguuid"), set.size(), set);
		if (null != set && set.size() > 0) {
			for (String p : set) {
				// IPv4协议的连接状态
				mapConnectionStatus = null != result.get(p + ".ConnectionStatus") && StringUtils.isNotBlank(result.get(p + ".ConnectionStatus").toString()) ? result.get(p + ".ConnectionStatus").toString().trim() : null;
				log.info("LogId:{}" + logName + "当前:{} 节点获取IPv4协议的连接状态:{} 而规定要求在本WANIPv4协议的连接状态:{}", parameter.get("loguuid"), p + ".ConnectionStatus", result.get(p + ".ConnectionStatus").toString(), "Connected");
				if (StringUtils.isNotBlank(mapConnectionStatus) && "Connected".equals(mapConnectionStatus)) {
					// 承载的业务
					mapServiceList = null != result.get(p + ".X_CMCC_ServiceList") && StringUtils.isNotBlank(result.get(p + ".X_CMCC_ServiceList").toString()) ? result.get(p + ".X_CMCC_ServiceList").toString().trim() : null;
					log.info("LogId:{}" + logName + "当前:{} 节点获取承载的业务:{} 而规定要求在本WAN连接承载的业务列表:{}", parameter.get("loguuid"), p + ".X_CMCC_ServiceList", result.get(p + ".X_CMCC_ServiceList").toString(), serviceList);
					// WAN连接的类型
					mapConnectionType = null != result.get(p + ".ConnectionType") && StringUtils.isNotBlank(result.get(p + ".ConnectionType").toString()) ? result.get(p + ".ConnectionType").toString().trim() : null;
					log.info("LogId:{}" + logName + "当前:{} 节点获取WAN连接的类型:{} 而规定要求在本WAN连接的类型:{}", parameter.get("loguuid"), p + ".ConnectionType", result.get(p + ".ConnectionType").toString(), connectionType);
					if (StringUtils.isNotBlank(mapServiceList) && StringUtils.isNotBlank(mapConnectionType) && mapServiceList.equals(serviceList.trim()) && mapConnectionType.equals(connectionType)) {
						rb.put("path", p);
						rb.put("status", "2");
						istrue = false;
					}
				}
				if (!istrue) break;
			}
		}
//		boolean istrue = true, sl = false, ct = false;// 性能标识状态
//		String serviceListPath = "", connectionTypePath = "";//规定在本WAN连接承载的什么业务列表与WAN连接的类型的节点路径相同才能使用这个WAN连接
//		for (Map.Entry<String, Object> entry : result.entrySet()) {
//			log.info("LogId:{}" + logName + "获取节点地址为:{} 规定承载的业务:{} WAN连接的类型:{}", parameter.get("loguuid"), entry.getKey(), serviceList, connectionType);
//			if ((pattern.matcher(entry.getKey().trim()).matches())) {
//				log.info("LogId:{}" + logName + "根据正则获取不同WAN状态满足数据的节点地址为:{} 满WAN连接承载的什么业务列表:{}", parameter.get("loguuid"), entry.getKey(), entry.getValue());
//				Object rvalue = entry.getValue();// WAN连接承载的什么业务列表
//				if (null != rvalue && StringUtils.isNotBlank(rvalue + "")) {
//					String[] vl = rvalue.toString().split(",");
//					if (null != vl && vl.length > 0) {
//						for (String v : vl) {
//							if (serviceList.equals(String.valueOf(v))) {
//								serviceListPath = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
//								log.info("LogId:{}" + logName + "规定要求在本WAN连接承载的业务列表:{} 获取到WAN连接承载的业务列表:{} 获取到WAN连接承载的业务列表的路径:{}", parameter.get("loguuid"), serviceList, v, serviceListPath);
//								sl = true;
//								break;
//							}
//						}
//					}
////					if (StringUtils.isNotBlank(connectionType) && connectionType.equals(rvalue.toString())) ct = true;
////					if (sl && ct) {
//					if (StringUtils.isNotBlank(connectionType) && connectionType.equals(rvalue.toString())) {
//						connectionTypePath = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
//						log.info("LogId:{}" + logName + "规定要求在本WAN连接的类型:{} 获取到WAN连接的类型:{} 获取到WAN连接的类型的路径:{}", parameter.get("loguuid"), connectionType, rvalue, connectionTypePath);
//						ct = true;
//					}
//					if (sl && ct && StringUtils.isNotBlank(serviceListPath) && StringUtils.isNotBlank(connectionTypePath) && serviceListPath.trim().equals(connectionTypePath.trim())) {
//						rb.put("path", entry.getKey().substring(0, entry.getKey().lastIndexOf(".")));
//						rb.put("status", "2");
//						istrue = false;
//					}
//				}
//			}
//			if (!istrue) break;
//		}
		return istrue;
	}

	/**
	 * 获取标准值状态
	 * 
	 * @param result
	 *            网关获取的数据
	 * @param pattern
	 *            正则
	 * @param rb
	 *            返回状态
	 * @param serviceList
	 *            规定在本WAN连接承载的什么业务列表
	 * @param parameter
	 *            参数集
	 * @param log
	 *            日志对象
	 * @param logName
	 *            日志名称
	 * @param serviceList
	 *            规定在本WAN连接承载的什么业务列表
	 * @param connectionType
	 *            WAN连接的类型
	 * @param addressingType
	 *            IPv4地址分配方式
	 */
	private static void getStandardValueStatus(Map<String, Object> result, Pattern pattern, Map<String, Object> rb, Map<String, Object> parameter, Logger log, String logName, String serviceList, String connectionType, String addressingType) {
		log.info("LogId:{}" + logName + "即将从:{}获取相应数据", parameter.get("loguuid"), result);
		Set<String> set = new HashSet<String>();// 去重复数据
		for (Map.Entry<String, Object> entry : result.entrySet()) {
			String node = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
			if (StringUtils.isNotBlank(node)) {
				set.add(node);
			}
			log.info("LogId:{}" + logName + "将满足正则数据的节点地址:{} 添加到 Set集合当中去重", parameter.get("loguuid"), node);
		}
		boolean istrue = false;
		String mapServiceList = "", mapConnectionType = "", mapAddressingType = "", mapConnectionStatus = "";
		log.info("LogId:{}" + logName + "即将遍历取出Set集合当中节点值,当前集合大小:{} 节点集为:{}", parameter.get("loguuid"), set.size(), set);
		if (null != set && set.size() > 0) {
			for (String p : set) {
				// IPv4协议的连接状态
				mapConnectionStatus = null != result.get(p + ".ConnectionStatus") && StringUtils.isNotBlank(result.get(p + ".ConnectionStatus").toString()) ? result.get(p + ".ConnectionStatus").toString().trim() : null;
				log.info("LogId:{}" + logName + "当前:{} 节点获取IPv4协议的连接状态:{} 而规定要求在本WANIPv4协议的连接状态:{}", parameter.get("loguuid"), p + ".ConnectionStatus", result.get(p + ".ConnectionStatus").toString(), "Connected");
				if (StringUtils.isNotBlank(mapConnectionStatus) && "Connected".equals(mapConnectionStatus)) {
					// 承载的业务
					mapServiceList = null != result.get(p + ".X_CMCC_ServiceList") && StringUtils.isNotBlank(result.get(p + ".X_CMCC_ServiceList").toString()) ? result.get(p + ".X_CMCC_ServiceList").toString().trim() : null;
					log.info("LogId:{}" + logName + "当前:{} 节点获取承载的业务:{} 而规定要求在本WAN连接承载的业务列表:{}", parameter.get("loguuid"), p + ".X_CMCC_ServiceList", result.get(p + ".X_CMCC_ServiceList").toString(), serviceList);
					// WAN连接的类型
					mapConnectionType = null != result.get(p + ".ConnectionType") && StringUtils.isNotBlank(result.get(p + ".ConnectionType").toString()) ? result.get(p + ".ConnectionType").toString().trim() : null;
					log.info("LogId:{}" + logName + "当前:{} 节点获取WAN连接的类型:{} 而规定要求在本WAN连接的类型:{}", parameter.get("loguuid"), p + ".ConnectionType", result.get(p + ".ConnectionType").toString(), connectionType);
					// IPv4地址分配方式
					if (StringUtils.isNotBlank(addressingType)) {
						mapAddressingType = null != result.get(p + ".AddressingType") && StringUtils.isNotBlank(result.get(p + ".AddressingType").toString()) ? result.get(p + ".AddressingType").toString().trim() : null;
						log.info("LogId:{}" + logName + "当前:{} 节点获取IPv4地址分配方式:{} 而规定要求IPv4地址分配方式:{}", parameter.get("loguuid"), p + ".AddressingType", result.get(p + ".AddressingType").toString(), connectionType);
					}
					if (StringUtils.isNotBlank(mapServiceList) && StringUtils.isNotBlank(mapConnectionType) && mapServiceList.equals(serviceList.trim()) && mapConnectionType.equals(connectionType)) {
						if (StringUtils.isBlank(addressingType)) {
							rb.put("path", p);
							rb.put("status", "2");
							istrue = true;
						} else {
							if (StringUtils.isNotBlank(mapAddressingType) && mapAddressingType.equals(addressingType.trim())) {
								rb.put("path", p);
								rb.put("status", "2");
								istrue = true;
							}
						}
					}
				}
				if (istrue) break;
			}
		}
//		boolean istrue = false, sl = false, ct = false, at = false;
//		String serviceListPath = "", connectionTypePath = "" , addressingTypePath = "";//规定在本WAN连接承载的什么业务列表与WAN连接的类型的节点路径相同才能使用这个WAN连接与IPv4地址分配方式
//		for (Map.Entry<String, Object> entry : result.entrySet()) {
//			log.info("LogId:{}" + logName + "获取节点地址为:{} 规定承载的业务:{} WAN连接的类型:{} IPv4地址分配方式:{}", parameter.get("loguuid"), entry.getKey(), serviceList, connectionType, addressingType);
//			if ((pattern.matcher(entry.getKey().trim()).matches())) {
//				log.info("LogId:{}" + logName + "获取满足正则数据的节点地址为:{} 值为:{}", parameter.get("loguuid"), entry.getKey(), entry.getValue());
//				Object rvalue = entry.getValue();// WAN连接的类型
//				if (null != rvalue && StringUtils.isNotBlank(rvalue + "")) {
//					// 承载的业务
//					String[] vl = rvalue.toString().split(",");
//					if (null != vl && vl.length > 0) {
//						for (String v : vl) {
//							if (serviceList.equals(String.valueOf(v))) {
//								serviceListPath = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
//								log.info("LogId:{}" + logName + "规定要求在本WAN连接承载的业务列表:{} 获取到WAN连接承载的业务列表:{} 获取到WAN连接承载的业务列表的路径:{}", parameter.get("loguuid"), serviceList, v, serviceListPath);
//								sl = true;
//								break;
//							}
//						}
//					}
//					// WAN连接的类型
////					if (StringUtils.isNotBlank(connectionType) && connectionType.equals(rvalue.toString())) ct = true;
////					// IPv4地址分配方式,除了DHCP外其它都可以任意选择所以下面不做判断
////					if (StringUtils.isBlank(addressingType) || addressingType.equals(rvalue.toString())) at = true;
////					if (sl && ct && at) {
//					
//					if (StringUtils.isNotBlank(connectionType) && connectionType.equals(rvalue.toString())){
//						connectionTypePath = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
//						log.info("LogId:{}" + logName + "规定要求在本WAN连接的类型:{} 获取到WAN连接的类型:{} 获取到WAN连接的类型的路径:{}", parameter.get("loguuid"), connectionType, rvalue, connectionTypePath);
//						ct = true;
//					} 
//					// IPv4地址分配方式,除了DHCP外其它都可以任意选择所以下面不做判断
//					if (StringUtils.isBlank(addressingType) || addressingType.equals(rvalue.toString())){
//						addressingTypePath = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
//						log.info("LogId:{}" + logName + "规定要求的IPv4地址分配方式:{} 获取到IPv4地址分配方式:{} 获取到IPv4地址分配方式的路径:{}", parameter.get("loguuid"), addressingType, rvalue, addressingTypePath);
//						 at = true;
//					}
//					if (sl && ct && at && StringUtils.isNotBlank(serviceListPath) && StringUtils.isNotBlank(connectionTypePath) && StringUtils.isNotBlank(addressingTypePath) && serviceListPath.trim().equals(connectionTypePath.trim()) && connectionTypePath.trim().equals(addressingTypePath.trim())) {
//						rb.put("path", entry.getKey().substring(0, entry.getKey().lastIndexOf(".")));
//						rb.put("status", "2");
//						istrue = true;
//					}
//				}
//			}
//			if (istrue) break;
//		}
	}

	/**
	 * 获取网关节点名集合
	 * 
	 * @param result
	 *            数据源
	 * @param pattern
	 *            节点名的规则
	 * @return
	 */
	public static List<String> getNamesList(Map<String, Object> result, Pattern pattern) {
		List<String> namesList = new ArrayList<>();
		for (String key : result.keySet()) {
			if (pattern.matcher(key.trim()).matches()) namesList.add(key.trim());
		}
		return namesList;
	}

	/**
	 * 获取网关节单个点名
	 * 
	 * @param result
	 *            数据源
	 * @param pattern
	 *            节点名的规则
	 * @return
	 */
	public static String getNames(Map<String, Object> result, Pattern pattern) {
		String path = null;
		List<String> namesList = getNamesList(result, pattern);
		if (null != namesList && namesList.size() > 0) {
			path = namesList.get(0);
		}
		return path;
	}

	/**
	 * 验证数据是否满足正则要求
	 * 
	 * @param data
	 *            要验证数据
	 * @param pattern
	 *            正则
	 * @return
	 */
	public static Boolean checkPattern(String data, Pattern pattern) {
		return pattern.matcher(data).matches();
	}

	/**
	 * 在网关节点上设置Ping诊断值
	 * 
	 * @param parameter
	 *            参数信息
	 * @param gi
	 *            网关信息对象
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String,Object> settingPingParameterAndRequestGateway(Map<String, Object> parameter, GatewayInfo gi, InstructionMethodService ims, List<ParameterValueStruct> pvslist) {
		return ims.setParameterValueErrorCode(gi.getGatewayMacaddress(), pvslist);// 组装数据
	}

	/**
	 * 封装网关AddObject
	 * 
	 * @param gi
	 *            网关对象
	 * @param ims
	 *            请求ACS实现类
	 * @param pathNode
	 *            路径节点
	 * @return
	 */
	public static Integer AddObject(GatewayInfo gi, InstructionMethodService ims, String pathNode) {
		return ims.AddObject(gi.getGatewayMacaddress(), pathNode, System.currentTimeMillis() + "");
	}

	/**
	 * 封装网关delObject
	 * 
	 * @param gi
	 *            网关对象
	 * @param ims
	 *            请求ACS实现类
	 * @param pathNode
	 *            路径节点
	 * @return
	 */
	public static Integer deleteObject(GatewayInfo gi, InstructionMethodService ims, String pathNode) {
		return ims.DeleteObject(gi.getGatewayMacaddress(), pathNode, System.currentTimeMillis() + "");
	}
	/**
	 * 获取配置文件中的VlanidMark
	 * 
	 * @param parameter
	 *            参数集
	 * @param vlanidmark
	 *            配置文件中VlanidMark
	 * @param log
	 *            日志对象
	 * @param logName
	 *            日志名称
	 */
	public static void getPropertiesVlanidMark(Map<String, Object> parameter, String vlanidmark, Logger log, String logname) {
		Object vim = parameter.get("vlanidmark");
		log.info("LogId:{}" + logname + "获取配置文件中的vlanidmark值为:{}", parameter.get("loguuid"), vim, vlanidmark);
		if (null == vim || StringUtils.isBlank(vim.toString())) {
			parameter.put("vlanidmark", vlanidmark);
			log.info("LogId:{}" + logname + "将配置文件中获取vlanidmark添加到参数中,parameter.get(vlanidmark)值为:{}", parameter.get("loguuid"), parameter.get("vlanidmark"));
		}
	}
}
