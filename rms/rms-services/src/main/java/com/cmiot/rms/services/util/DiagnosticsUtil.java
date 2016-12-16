package com.cmiot.rms.services.util;

import java.util.ArrayList;
import java.util.Arrays;
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
 * @author shuang
 * @version [版本号, 2016年6月6日]
 */
public class DiagnosticsUtil {

	/**
	 * 获取WAN连接IPv4协议的连接状态
	 * @param parameter
	 *            参数
	 * @param path
	 *            创建 WAN 连接地址
	 * @param waitTime
	 *            等带赶时间(单位秒)
	 * @param gi
	 *            网关
	 * @param ims
	 *            请求ACS实现类
	 * @param log
	 *            日志
	 * @param logName
	 *            日志名称
	 * @return
	 */
	public static Map<String, Object> getWanConnectionStatus(Map<String, Object> parameter, String path, Integer waitTime, GatewayInfo gi, InstructionMethodService ims, Logger log, String logName) throws Exception {
		log.info("LogId:{}" + logName + "进入获取WAN连接IPv4协议的连接状态方法串", parameter.get("loguuid"));
		Thread.sleep(1000 * waitTime);// 现业务要求
		Map<String, Object> rb = new HashMap<String, Object>();
		rb.put("status", "3");
		rb.put("path", path);
		rb.put("delFlag", Boolean.TRUE);
		Map<String, Object> result = ims.getParameterValues(gi.getGatewayMacaddress(), Arrays.asList(path + ".ConnectionStatus"));
		log.info("LogId:{}" + logName + "获取创建的WAN:{} 节点WAN连接IPv4协议的连接状态,休眠:{} 后获取网关的数据:{}", parameter.get("loguuid"), Arrays.asList(path + ".ConnectionStatus"), waitTime, result);
		if (null != result && result.size() > 0) {
			String connectionStatus = (null != result.get(path + ".ConnectionStatus") && StringUtils.isNotBlank(result.get(path + ".ConnectionStatus").toString())) ? result.get(path + ".ConnectionStatus").toString().trim() : null;
			log.info("LogId:{}" + logName + "在返回的结果中获取KEY:{} 的IPv4协议的连接状态:{}", parameter.get("loguuid"), path + ".ConnectionStatus", connectionStatus);
			if (StringUtils.isNotBlank(connectionStatus) && "Connected".equals(connectionStatus)) {
				rb.put("status", "2");
			}
		}
		return rb;
	}

	/**
	 * 验证MAC格式
	 * @param mac
	 * @return
	 */
	public static Boolean patternMac(String mac) {
		String patternMac = "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$";// [A-F0-9]{2}(-[A-F0-9]{2}){5}
		if (!Pattern.compile(patternMac).matcher(mac).find()) {
			return false;
		}
		return true;
	}

	/**
	 * 获取诊断值的阈值状态
	 * 
	 * @param result
	 *            网关返回的数据
	 * @param diagnoseType
	 *            诊断类型1:线路诊断;2:Ping诊断;3:Traceroute诊断;4:PPPoE仿真;5:DHCP仿真;6:VoIP诊断;7:HTTP下载仿真
	 * @param equipmentType
	 *            设备类型1:网关;2:机顶盒
	 * @param dtvdao
	 *            阈值DAO
	 * @return
	 */
	public static Map<String, Object> getDiagnoseThresholdFlag(Map<String, Object> result, Integer diagnoseType, Integer equipmentType, DiagnoseThresholdValueMapper dtvdao, String logid, Logger log, String logname) {
		Map<String, Object> data = new HashMap<String, Object>();
		log.info("LogId:{}" + logname + "获取诊断值的阈值状态传入网关获取数据:{} 诊断类型(diagnoseType):{} 设备类型:{}", logid, result, diagnoseType, equipmentType);
		if (null != result && result.size() > 0 && null != dtvdao) {
			long st = System.currentTimeMillis();
			List<DiagnoseThresholdValue> dtvList = dtvdao.selectDiagnoseThresholdValue(new DiagnoseThresholdValue(diagnoseType, equipmentType));
			log.info("LogId:{}" + logname + "查询阈值数据返回数据总数:{} 消耗时间:{}", logid, dtvList.size(), (System.currentTimeMillis() - st));
			if (null != dtvList && dtvList.size() > 0) {
				for (Map.Entry<String, Object> entry : result.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					log.info("LogId:{}" + logname + "循环获取到网关的数据Key:{} value:{} ", logid, key, value);
					if (StringUtils.isNotBlank(key) && null != value) {
						int thresholdFlag = 0;// 其它:正常范围
						for (DiagnoseThresholdValue dtv : dtvList) {
							log.info("LogId:{}" + logname + "循环数据库获取到阈值数据ThresholdName:{} MinThresholdValue:{} MaxThresholdValue:{} DiagnoseType:{} EquipmentType:{}", logid, dtv.getThresholdName(), dtv.getMinThresholdValue(), dtv.getMaxThresholdValue(),
									dtv.getDiagnoseType(), dtv.getEquipmentType());
							log.info("LogId:{}" + logname + "正则验证网关的Value是否为数字([\\d]+\\.?[\\d]+):{}", logid, DiagnosticsUtil.checkIsNumber(Pattern.compile("[\\d]+\\.?[\\d]+"), value.toString()));
							if (DiagnosticsUtil.checkIsNumber(Pattern.compile("[\\d]+\\.?[\\d]+"), value.toString()) && key.trim().equalsIgnoreCase(dtv.getThresholdName())) {// 网关返回的名称与阈值名称忽略大小写相同
								log.info("LogId:{}" + logname + "正则验证阈值最小值是否为数字([\\d]+\\.?[\\d]+):{}", logid, DiagnosticsUtil.checkIsNumber(Pattern.compile("[\\d]+\\.?[\\d]+"), dtv.getMinThresholdValue()));
								log.info("LogId:{}" + logname + "正则验证阈值最大值是否为数字([\\d]+\\.?[\\d]+):{}", logid, DiagnosticsUtil.checkIsNumber(Pattern.compile("[\\d]+\\.?[\\d]+"), dtv.getMaxThresholdValue()));
								if (DiagnosticsUtil.checkIsNumber(Pattern.compile("[\\d]+\\.?[\\d]+"), dtv.getMaxThresholdValue()) && DiagnosticsUtil.get2Double(value.toString()) >= DiagnosticsUtil.get2Double(dtv.getMaxThresholdValue())) {
									thresholdFlag = 1;// 数值偏高
								} else if (DiagnosticsUtil.checkIsNumber(Pattern.compile("[\\d]+\\.?[\\d]+"), dtv.getMinThresholdValue()) && DiagnosticsUtil.get2Double(value.toString()) <= DiagnosticsUtil.get2Double(dtv.getMinThresholdValue())) {
									thresholdFlag = 2;// 数值偏低
								}
							}
							data.put(key, value);
							data.put(key + "ThresholdFlag", thresholdFlag);
						}
					}
				}
			} else {
				data = result;// 数据库无阈值
			}
		}
		return data;
	}

	/**
	 * 字符串转DOUBLE
	 * 
	 * @param value
	 * @return
	 */
	public static Double get2Double(String value) {
		double v = 0.0;
		if (StringUtils.isNotBlank(value)) v = Double.parseDouble(value.trim());
		return v;
	}

	/**
	 * 验证字符是否满足正则
	 * 
	 * @param pattern
	 *            正则
	 * @param value
	 *            字符值
	 * @return
	 */
	public static Boolean checkIsNumber(Pattern pattern, String value) {
		boolean isFalse = false;
		if (StringUtils.isNotBlank(value) && pattern.matcher(value.trim()).matches()) isFalse = true;
		return isFalse;
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
		log.info("LogId:{}" + logname + "获取参数当中vlanidmark值为:{}与配置文件中的vlanidmark值为:{}", parameter.get("loguuid"), vim, vlanidmark);
		if (null == vim || StringUtils.isBlank(vim.toString())) {
			parameter.put("vlanidmark", vlanidmark);
			log.info("LogId:{}" + logname + "将配置文件中获取vlanidmark添加到参数中,parameter.get(vlanidmark)值为:{}", parameter.get("loguuid"), parameter.get("vlanidmark"));
		}
	}

	/**
	 * 查询用户VoIP的状态
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
	 * @return {status==0:网关查询VoIP状态失败;status==1:用户启用并注册VoIP;status==2:用户启用但未注册VoIP;status==3:用户注册VoIP但未启用;status==4:用户未启用并且未注册VoIP;}
	 */
	public static Integer queryVoIPStatus(Map<String, Object> parameter, GatewayInfo gi, InstructionMethodService ims, Logger log, String logName) {
		int status = 0, enableStatus = 0, registerStatus = 0;// status:0-网关查询状态失败;enableStatus:0-禁用;registerStatus:0-未注册
		long rrt = System.currentTimeMillis();
		Map<String, Object> rootResult = ims.getParameterNames(gi.getGatewayMacaddress(), "InternetGatewayDevice.Services.VoiceService.", false);
		log.info("LogId:{}" + logName + "网关获取InternetGatewayDevice.Services.VoiceService.参数返回数据:{} 耗时:{}", parameter.get("loguuid"), rootResult, (System.currentTimeMillis() - rrt));
		if (null != rootResult && rootResult.size() > 0) {
			Pattern pattern = Pattern.compile("InternetGatewayDevice.Services.VoiceService.[\\d]+.VoiceProfile.[\\d]+.Line.[\\d]+.(Enable|Status)");
			List<String> list = DiagnosticsUtil.getNamesList(rootResult, pattern);
			log.info("LogId:{}" + logName + "获取正则规则后获取网关数据的节点路径:{}", parameter.get("loguuid"), list);
			if (null != list && list.size() > 0) {
				long rvrt = System.currentTimeMillis();
				Map<String, Object> rootValueResult = ims.getParameterValues(gi.getGatewayMacaddress(), list);
				log.info("LogId:{}" + logName + "网关获取节点返回数据:{} 耗时:{}", parameter.get("loguuid"), rootValueResult, (System.currentTimeMillis() - rvrt));
				if (null != rootValueResult && rootValueResult.size() > 0) {
					// 目前 VOIP 仿真存在多条线路,如果一条状态满足要求就仿真
					Set<String> set = new HashSet<String>();
					for (Map.Entry<String, Object> entry : rootValueResult.entrySet()) {
						String node = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
						log.info("LogId:{}" + logName + "获取正则规则后获取网关数据的节点路径:{} 存放SET集合去重", parameter.get("loguuid"), node);
						if (StringUtils.isNotBlank(node)) {
							set.add(node);
						}
					}
					String mapEnable = null, mapStatus = null;
					if (null != set && set.size() > 0) {
						for (String p : set) {
							mapEnable = null != rootValueResult.get(p + ".Enable") && StringUtils.isNotBlank(rootValueResult.get(p + ".Enable").toString()) ? rootValueResult.get(p + ".Enable").toString().trim() : null;
							log.info("LogId:{}" + logName + "获取当前节点:{} 的Enable状态:{}", parameter.get("loguuid"), p + ".Enable", mapEnable);
							mapStatus = null != rootValueResult.get(p + ".Status") && StringUtils.isNotBlank(rootValueResult.get(p + ".Status").toString()) ? rootValueResult.get(p + ".Status").toString().trim() : null;
							log.info("LogId:{}" + logName + "获取当前节点:{} 的Status状态:{}", parameter.get("loguuid"), p + ".Status", mapStatus);
							if (StringUtils.isNotBlank(mapEnable) && StringUtils.isNotBlank(mapStatus) && "Enabled".equals(mapEnable) && "Up".equals(mapStatus)) {
								enableStatus = 1;
								registerStatus = 1;
								break;
							}
						}
					}
					// ------前期业务逻辑,可能后期业务会变 VOIP 仿真存在多条线路,如果一条状态不满足要求都不能仿真
					// for (Map.Entry<String, Object> entry : rootValueResult.entrySet()) {
					// String statusName = entry.getKey();
					// log.info("LogId:{}" + logName + "获取原始数据名称:{} 原始数据名称值:{}", parameter.get("loguuid"), statusName, entry.getValue());
					// if (StringUtils.isNotBlank(statusName) && statusName.lastIndexOf(".") > 0) {
					// statusName = statusName.substring(statusName.lastIndexOf(".") + 1);
					// log.info("LogId:{}" + logName + "获取解析数据名称:{} 解析数据名称值:{}", parameter.get("loguuid"), statusName, entry.getValue().toString());
					// if (StringUtils.isNotBlank(statusName)) {
					// if ("Enable".equals(statusName.trim())) {
					// switch (entry.getValue().toString()) {
					// case "Enabled":// 启用
					// enableStatus = 1;
					// break;
					// default:// 其它全认为禁用
					// enableStatus = 0;
					// break;
					// }
					// }
					// if ("Status".equals(statusName.trim())) {
					// switch (entry.getValue().toString()) {
					// case "Up":// 注册
					// registerStatus = 1;
					// break;
					// default:// 其它全认为未注册
					// registerStatus = 0;
					// break;
					// }
					// }
					// }
					// }
					// }
				}
			}
		}
		/**
		 * status==0:网关查询VoIP状态失败;
		 * status==1:用户启用并注册VoIP;
		 * status==2:用户启用但未注册VoIP;
		 * status==3:用户注册VoIP但未启用;
		 * status==4:用户未启用并且未注册VoIP;
		 */
		status = (enableStatus == 1 && registerStatus == 1) ? 1 : (enableStatus == 1 && registerStatus == 0) ? 2 : (enableStatus == 0 && registerStatus == 1) ? 3 : (status == 0 && enableStatus == 0 && registerStatus == 0) ? 0 : 4;
		return status;
	}

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
			int rdb = DiagnosticsUtil.deleteObject(gi, ims, path + ".");
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
	public static Map<String, Object> createDHCPWanConnect(Map<String, Object> parameter, GatewayInfo gi, InstructionMethodService ims, Logger log, String logName, String serviceList, String path) {
		Map<String, Object> rb = new HashMap<String, Object>();
		rb.put("status", "3");
		rb.put("delFlag", Boolean.FALSE);
		log.info("LogId:{}" + logName + "创建WAN连接的原始节点路径:{}", parameter.get("loguuid"), path);
		if (!checkPattern(path, Pattern.compile("InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.WANIPConnection.[\\d]+"))) {
			// 添加InternetGatewayDevice.WANDevice.网关返回Invalid arguments错误信息,所以默认此值为1
			int rootnum = 1;// AddObject(gi, ims, "InternetGatewayDevice.WANDevice.");
			// log.info("LogId:{}" + logName + "网关添加InternetGatewayDevice.WANDevice.对象返回节点值:{}", parameter.get("loguuid"), rootnum);
			if (rootnum > 0) {
				int elemnum = rootnum;
				// int elemnum = AddObject(gi, ims, "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice.");
				// log.info("LogId:{}" + logName + "网关添加InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice.对象返回节点值:{}", parameter.get("loguuid"), elemnum);
				if (elemnum > 0) {
					int childnum = AddObject(gi, ims, "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANIPConnection.");
					log.info("LogId:{}" + logName + "网关添加InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANIPConnection.对象返回节点值:{}", parameter.get("loguuid"), childnum);
					if (childnum > 0) {
						path = "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANIPConnection." + childnum;
						log.info("LogId:{}" + "创建AN连接的节点路径:{}", parameter.get("loguuid"), path);
						packageDHCPParameterValueStruct(parameter, gi, path, rb, ims, log, logName, serviceList);
					}
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
	private static void packageDHCPParameterValueStruct(Map<String, Object> parameter, GatewayInfo gi, String path, Map<String, Object> rb, InstructionMethodService ims, Logger log, String logName, String serviceList) {
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
		boolean isresult = ims.setParameterValue(gi.getGatewayMacaddress(), pvslist);
		log.info("LogId:{}" + logName + "网关添加{}个对象参数值返回状态:{}消耗时间:{}", parameter.get("loguuid"), pvslist.size(), isresult, (System.currentTimeMillis() - st));
		DiagnosticsUtil.deleteCreateWanConnect(parameter, gi, ims, log, logName, path, (boolean) !isresult);// 删除创建的WAN连接
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
	public static Map<String, Object> createPPPoEWanConnect(Map<String, Object> parameter, GatewayInfo gi, InstructionMethodService ims, Logger log, String logName, String serviceList, String path) {
		Map<String, Object> rb = new HashMap<String, Object>();
		rb.put("status", "3");
		rb.put("delFlag", Boolean.FALSE);
		if (!checkPattern(path, Pattern.compile("InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.WANPPPConnection.[\\d]+"))) {
			// 添加InternetGatewayDevice.WANDevice.网关返回Invalid arguments错误信息,所以默认此值为1
			int rootnum = 1;// AddObject(gi, ims, "InternetGatewayDevice.WANDevice.");
			// log.info("LogId:{}" + logName + "网关添加InternetGatewayDevice.WANDevice.对象返回节点值:{}", parameter.get("loguuid"), rootnum);
			if (rootnum > 0) {
				int elemnum = rootnum;
				// int elemnum = AddObject(gi, ims, "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice.");
				// log.info("LogId:{}" + logName + "网关添加InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice.对象返回节点值:{}", parameter.get("loguuid"), elemnum);
				if (elemnum > 0) {
					int childnum = AddObject(gi, ims, "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANPPPConnection.");
					log.info("LogId:{}" + logName + "网关添加InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANPPPConnection.对象返回节点值:{}", parameter.get("loguuid"), childnum);
					if (childnum > 0) {
						path = "InternetGatewayDevice.WANDevice." + rootnum + ".WANConnectionDevice." + elemnum + ".WANPPPConnection." + childnum;
						log.info("LogId:{}" + "创建AN连接的节点路径:{}", parameter.get("loguuid"), path);
						packagePPPoEParameterValueStruct(parameter, gi, path, rb, ims, log, logName, serviceList);
					}
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
	private static void packagePPPoEParameterValueStruct(Map<String, Object> parameter, GatewayInfo gi, String path, Map<String, Object> rb, InstructionMethodService ims, Logger log, String logName, String serviceList) {
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
		boolean isresult = ims.setParameterValue(gi.getGatewayMacaddress(), pvslist);
		log.info("LogId:{}" + logName + "网关添加{}个对象参数值返回状态:{}消耗时间:{}", parameter.get("loguuid"), pvslist.size(), isresult, (System.currentTimeMillis() - st));
		DiagnosticsUtil.deleteCreateWanConnect(parameter, gi, ims, log, logName, path, (boolean) !isresult);// 删除创建的WAN连接
		rb.put("delFlag", Boolean.TRUE);
		rb.put("status", isresult ? "2" : "3");
		rb.put("path", path);
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
	public static Map<String, Object> getWanConnectStatusAndPath(Map<String, Object> parameter, GatewayInfo gi, InstructionMethodService ims, Pattern pattern, Logger log, String logName, String serviceList, String[] connectionType, String addressingType) {
		Map<String, Object> rb = new HashMap<String, Object>();
		rb.put("status", "0");// 目前不考虑网关无此InternetGatewayDevice.WANDevice.节点情况,所以网关返回为空就认为网关超时或未正常获取数据信息
		rb.put("delFlag", Boolean.FALSE);
		long wst = System.currentTimeMillis();
		Map<String, Object> result = ims.getParameterNames(gi.getGatewayMacaddress(), "InternetGatewayDevice.WANDevice.", false);
		log.info("LogId:{}" + logName + "获取网关InternetGatewayDevice.WANDevice.返回数据:{}验证参数消耗时间:{}", parameter.get("loguuid"), result, (System.currentTimeMillis() - wst));
		if (null != result && result.size() > 0) {
			rb.put("status", "1");// 网关无正则验证的节点路径,所以需要创建Wan连接再进行诊断
			List<String> addressList = DiagnosticsUtil.getNamesList(result, pattern);
			log.info("LogId:{}" + logName + "获取WAN连接节点路径:{}", parameter.get("loguuid"), addressList);
			if (null != addressList && addressList.size() > 0) {
				long wanst = System.currentTimeMillis();
				result = ims.getParameterValues(gi.getGatewayMacaddress(), addressList);
				log.info("LogId:{}" + logName + "获取WAN连接数据:{}消耗时间:{}", parameter.get("loguuid"), result, (System.currentTimeMillis() - wanst));
				if (null != result && result.size() > 0) {
					// 首先考虑pppoe当中有WAN连接
					String rg = "InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.WANPPPConnection.[\\d]+.(X_CMCC_ServiceList|ConnectionType|Enable|ConnectionStatus)";
					log.info("LogId:{}" + logName + "首先查询PPoE当中是否有WAN连接,进入根据正则:{}获取PPPoEWAN状态方法", parameter.get("loguuid"), rg);
					pattern = Pattern.compile(rg);
					// 根据上面正则获取出PPPOEWAN连接是否存在
					if (DiagnosticsUtil.getServiceListStatus(result, pattern, rb, parameter, log, logName, serviceList, connectionType[0])) {
						// 如果不存在PPPOEWAN连接则查询是否存在IPWAN连接,如果IPWAN连接也不存则返回status为1,需要创建Wan连接再进行诊断
						rg = "InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.WANIPConnection.[\\d]+.(X_CMCC_ServiceList|ConnectionType|AddressingType|Enable|ConnectionStatus)";
						log.info("LogId:{}" + logName + "PPoE当中无WAN连接,所以进入根据正则:{}获取IPWAN状态方法", parameter.get("loguuid"), rg);
						pattern = Pattern.compile(rg);
						DiagnosticsUtil.getStandardValueStatus(result, pattern, rb, parameter, log, logName, serviceList, connectionType[1], addressingType);
					}
				}
			}
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
		// boolean istrue = true, sl = false, ct = false;// 性能标识状态
		// String serviceListPath = "", connectionTypePath = "";// 规定在本WAN连接承载的什么业务列表与WAN连接的类型的节点路径相同才能使用这个WAN连接
		// for (Map.Entry<String, Object> entry : result.entrySet()) {
		// log.info("LogId:{}" + logName + "获取节点地址为:{} 规定承载的业务:{} WAN连接的类型:{}", parameter.get("loguuid"), entry.getKey(), serviceList, connectionType);
		// if ((pattern.matcher(entry.getKey().trim()).matches())) {
		// log.info("LogId:{}" + logName + "根据正则获取不同WAN状态满足数据的节点地址为:{} 满WAN连接承载的什么业务列表:{}", parameter.get("loguuid"), entry.getKey(), entry.getValue());
		// Object rvalue = entry.getValue();// WAN连接承载的什么业务列表
		// if (null != rvalue && StringUtils.isNotBlank(rvalue + "")) {
		// String[] vl = rvalue.toString().split(",");
		// // 承载的业务
		// if (null != vl && vl.length > 0) {
		// for (String v : vl) {
		// if (serviceList.equals(String.valueOf(v))) {
		// serviceListPath = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
		// log.info("LogId:{}" + logName + "规定要求在本WAN连接承载的业务列表:{} 获取到WAN连接承载的业务列表:{} 获取到WAN连接承载的业务列表的路径:{}", parameter.get("loguuid"), serviceList, v, serviceListPath);
		// sl = true;
		// break;
		// }
		// }
		// }
		//
		// // WAN连接的类型
		// if (StringUtils.isNotBlank(connectionType) && connectionType.equals(rvalue.toString())) {
		// connectionTypePath = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
		// log.info("LogId:{}" + logName + "规定要求在本WAN连接的类型:{} 获取到WAN连接的类型:{} 获取到WAN连接的类型的路径:{}", parameter.get("loguuid"), connectionType, rvalue, connectionTypePath);
		// ct = true;
		// }
		//
		// // 逻辑判断
		// if (sl && ct && StringUtils.isNotBlank(serviceListPath) && StringUtils.isNotBlank(connectionTypePath) && serviceListPath.trim().equals(connectionTypePath.trim())) {
		// rb.put("path", entry.getKey().substring(0, entry.getKey().lastIndexOf(".")));
		// rb.put("status", "2");
		// istrue = false;
		// }
		// }
		// }
		// if (!istrue) break;
		// }
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

		// 前期业务
		// boolean sl = false, ct = false, at = false;
		// String serviceListPath = "", connectionTypePath = "", addressingTypePath = "";// 规定在本WAN连接承载的什么业务列表与WAN连接的类型的节点路径相同才能使用这个WAN连接与IPv4地址分配方式
		// for (Map.Entry<String, Object> entry : result.entrySet()) {
		// log.info("LogId:{}" + logName + "获取节点地址为:{} 规定承载的业务:{} WAN连接的类型:{} IPv4地址分配方式:{}", parameter.get("loguuid"), entry.getKey(), serviceList, connectionType,
		// addressingType);
		// if ((pattern.matcher(entry.getKey().trim()).matches())) {
		// log.info("LogId:{}" + logName + "获取满足正则数据的节点地址为:{} 值为:{}", parameter.get("loguuid"), entry.getKey(), entry.getValue());
		// Object rvalue = entry.getValue();// WAN连接的类型
		// if (null != rvalue && StringUtils.isNotBlank(rvalue + "")) {
		// String[] vl = rvalue.toString().split(",");
		// // 承载的业务
		// if (null != vl && vl.length > 0) {
		// for (String v : vl) {
		// if (serviceList.equals(String.valueOf(v))) {
		// serviceListPath = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
		// log.info("LogId:{}" + logName + "规定要求在本WAN连接承载的业务列表:{} 获取到WAN连接承载的业务列表:{} 获取到WAN连接承载的业务列表的路径:{}", parameter.get("loguuid"), serviceList, v, serviceListPath);
		// sl = true;
		// break;
		// }
		// }
		// }
		//
		// // WAN连接的类型
		// if (StringUtils.isNotBlank(connectionType) && connectionType.equals(rvalue.toString())) {
		// connectionTypePath = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
		// log.info("LogId:{}" + logName + "规定要求在本WAN连接的类型:{} 获取到WAN连接的类型:{} 获取到WAN连接的类型的路径:{}", parameter.get("loguuid"), connectionType, rvalue, connectionTypePath);
		// ct = true;
		// }
		//
		// // IPv4地址分配方式,除了DHCP外其它都可以任意选择所以下面不做判断
		// if (StringUtils.isNotBlank(addressingType) && addressingType.equals(rvalue.toString())) {
		// addressingTypePath = entry.getKey().substring(0, entry.getKey().lastIndexOf("."));
		// log.info("LogId:{}" + logName + "规定要求的IPv4地址分配方式:{} 获取到IPv4地址分配方式:{} 获取到IPv4地址分配方式的路径:{}", parameter.get("loguuid"), addressingType, rvalue, addressingTypePath);
		// at = true;
		// }
		//
		// // 逻辑判断
		// if (sl && ct && StringUtils.isNotBlank(serviceListPath) && StringUtils.isNotBlank(connectionTypePath) && serviceListPath.trim().equals(connectionTypePath.trim())) {
		// if (StringUtils.isBlank(addressingType)) {
		// rb.put("path", entry.getKey().substring(0, entry.getKey().lastIndexOf(".")));
		// rb.put("status", "2");
		// istrue = true;
		// } else {
		// if (at && StringUtils.isNotBlank(addressingTypePath) && connectionTypePath.trim().equals(addressingTypePath.trim())) {
		// rb.put("path", entry.getKey().substring(0, entry.getKey().lastIndexOf(".")));
		// rb.put("status", "2");
		// istrue = true;
		// }
		// }
		// }
		// }
		// }
		// if (istrue) break;
		// }
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
	public static Boolean settingPingParameterAndRequestGateway(Map<String, Object> parameter, GatewayInfo gi, InstructionMethodService ims, List<ParameterValueStruct> pvslist) {
		return ims.setParameterValue(gi.getGatewayMacaddress(), pvslist);// 组装数据
	}

	/**
	 * 获取诊断状态
	 * 
	 * @param gi
	 *            网关对象
	 * @param succ
	 *            成功判断标识
	 * @param node
	 *            获取诊断状态节点
	 * @param ims
	 *            请求ACS实现类
	 * @param log
	 *            日志对象
	 * @param logName
	 *            日志描述说明
	 * @return
	 */
	public static String[] getDiagnosticsStatus(GatewayInfo gi, String succ, String node, InstructionMethodService ims, Logger log, String logName) {
		String temp = null;
		String[] rb = { "false", "0" };
		long st = System.currentTimeMillis();
		List<String> list = new ArrayList<>();
		list.add(node);// DHCP仿真状态
		Map<String, Object> result = ims.getParameterValues(gi.getGatewayMacaddress(), list);
		if (null != result && result.size() > 0) {
			for (Iterator<Object> i = result.values().iterator(); i.hasNext();) {
				temp = i.next() + "";
				if (StringUtils.isNotBlank(temp) && succ.equals(temp.trim())) {
					rb[0] = "true";
					break;
				} else {
					rb[1] = "1";
					break;
				}
			}
		}
		log.info(logName + "获取诊断消耗时间:" + (System.currentTimeMillis() - st) + ",诊断状态:" + temp);
		return rb;
	}

	/**
	 * 获取网关诊断结果
	 * 
	 * @param gi
	 *            网关对象
	 * @param node
	 *            获取结果节点
	 * @param rm
	 *            返回对象
	 * @param logName
	 *            日志描述说明
	 * @param ims
	 *            请求ACS实现类
	 * @param log
	 *            日志对象
	 * @param logid
	 *            日志ID
	 * @param diagnoseType
	 *            诊断类型1:线路诊断;2:Ping诊断;3:Traceroute诊断;4:PPPoE仿真;5:DHCP仿真;6:VoIP诊断;7:HTTP下载仿真
	 * @param dtvdao
	 *            阈值DAO
	 */
	public static void getDiagnosticsResult(GatewayInfo gi, List<String> node, Map<String, Object> rm, String logName, InstructionMethodService ims, Logger log, String logid, int diagnoseType, DiagnoseThresholdValueMapper dtvdao) {
		long st = System.currentTimeMillis();
		Map<String, Object> result = ims.getParameterValues(gi.getGatewayMacaddress(), node);
		log.info("LogId:{}" + logName + "获取网关诊断返回结果集:{}消耗时间:{}", logid, result, (System.currentTimeMillis() - st));
		if (null != result && result.size() > 0) {
			long fst = System.currentTimeMillis();
			result = DiagnosticsUtil.getResultMapDate(result);// 获取返回封装数据
			// 目前只有DHCP限定业务要去获取状态,线路诊断,Ping诊断,Traceroute是自己的业务不在公共方法中
			if (diagnoseType == 5) result = DiagnosticsUtil.getDiagnoseThresholdFlag(result, diagnoseType, 1, dtvdao, logid, log, logName);// 获取诊断值的阈值状态
			DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), result);
			log.info("LogId:{}" + logName + "将诊断结果封装返回结果集:{}消耗时间:{}", logid, result, (System.currentTimeMillis() - fst));
		} else {
			DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取网关诊断信息失败", "");
		}
	}

	/**
	 * 临时对象锁,为了等待8 DIAGNOSTICS COMPLETE状态唤醒
	 * 注:waitTime传值为空为<=0时取默认值为120000
	 * 
	 * @param waitTime
	 *            等待时间(毫秒)
	 * @param gi
	 *            网关对象信息
	 * @param log
	 *            日志对象
	 * @param logid
	 *            日志ID
	 * @throws Exception
	 */
	public static void temporaryObjectLock(Integer waitTime, GatewayInfo gi, Logger log, String logName, String logid) throws Exception {
		TemporaryObject temporaryObject = new TemporaryObject("diagnostics_" + gi.getGatewayMacaddress());
		log.info("LogId:{}" + logName + "添加临时对象锁,网关MAC地址:{}", logid, "diagnostics_" + temporaryObject.getRequestId());
		RequestCache.set("diagnostics_" + gi.getGatewayMacaddress(), temporaryObject);
		long st = System.currentTimeMillis();
		synchronized (temporaryObject) {
			log.info("LogId:{}" + logName + "设置临时对象锁等待时长:{}", logid, waitTime);
			temporaryObject.wait((null != waitTime && 0 < waitTime) ? waitTime : 120000);// 等待8 DIAGNOSTICS COMPLETE状态唤醒
		}
		log.info("LogId:{}" + logName + "临时对象解锁消耗时间:{}", logid, (System.currentTimeMillis() - st));
		log.info("LogId:{}" + logName + "删除临时对象锁,网关MAC地址:{}", logid, "diagnostics_" + temporaryObject.getRequestId());
		RequestCache.delete("diagnostics_" + gi.getGatewayMacaddress());// 删除锁定对象
	}

	/**
	 * 获取返回封装数据
	 * 
	 * @param result
	 * @return
	 */
	public static Map<String, Object> getResultMapDate(Map<String, Object> result) {
		Map<String, Object> data = new HashMap<>();
		for (Map.Entry<String, Object> entry : result.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue() + "";
			String resultName = name.substring(name.lastIndexOf(".") + 1, name.length());// 获取名称最后一个“.”后的参数为KEY值
			data.put(resultName, value);
		}
		return data;
	}

	/**
	 * 验证参数
	 * 
	 * @param parameter
	 *            参数集
	 * @param rm
	 *            返回对象
	 * @return
	 */
	public static Boolean checkParm(Map<String, Object> parameter, Map<String, Object> rm, List<String> parmlist) {
		boolean isTrue = false;
		if (null != parameter && parameter.size() > 0) {
			int countParm = parmlist.size();// 需要验证的参数个数
			for (String parm : parmlist) {
				for (Map.Entry<String, Object> entry : parameter.entrySet()) {
					String key = entry.getKey();
					if (StringUtils.isNotBlank(key) && parm.equals(key)) {
						countParm--;// 当参数名称相同时删除需要验证的参数个数
						if (StringUtils.isNotBlank(entry.getValue() + "")) {
							isTrue = true;
						} else {
							packageBackInfo(rm, RespCodeEnum.RC_1.code(), parm + "参数不能为空", "");
							return false;// 目前所有的参数都为必传所以只要有一个为空返加false
						}
					}
				}
			}
			if (countParm > 0) {// 请求参数与要求参数个数不匹配
				isTrue = false;
				packageBackInfo(rm, RespCodeEnum.RC_1.code(), "接口必传参数不全", "");
			}
		} else {
			packageBackInfo(rm, RespCodeEnum.RC_1.code(), "请求接口参数为空", "");// 请求参数集为空
		}
		return isTrue;
	}

	/**
	 * 封装返回内容
	 * @param rm 返回对象
	 * @param code 状态码
	 * @param msg 状态码描述内容
	 * @param data 返回数据
	 * @throws
	 */
	public static void packageBackInfo(Map<String, Object> rm, String code, String msg, Object data) {
		rm.put(Constant.DATA, data);
		rm.put(Constant.CODE, code);
		rm.put(Constant.MESSAGE, msg);
	}

	/**
	 * 保存操作日志
	 * 
	 * @param parameter
	 *            参数集
	 * @param rm
	 *            返回对
	 * @param operation
	 *            操作
	 * @param logManagerService
	 *            日志服务
	 */
	public static void saveOperationDiagnoseLog(Map<String, Object> parameter, Map<String, Object> rm, String operation, LogManagerService logManagerService) {
		parameter.put("categoryMenu", CategoryEnum.GATEWAY_MANAGER_SERVICE.name());// 类目ID
		parameter.put("operation", operation);// 具体的操作
		parameter.put("categoryMenuName", CategoryEnum.GATEWAY_MANAGER_SERVICE.description());// 类目名称
		parameter.put("content", "请求报文" + JSON.toJSONString(parameter) + ",返回报文" + JSON.toJSONString(rm));// 操作的数据内容
		parameter.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());// 日志类别
		logManagerService.recordOperationLog(parameter);
	}

	/**
	 * 保存诊断日志(报表)
	 * 
	 * @param parameter
	 *            参数集
	 * @param gi
	 *            网关对象
	 * @param dt
	 *            诊断类型
	 *            1:获取路由器LAN口信息与已经无线连接信息;2:获取CUP与内存占用比例;3:网络诊断;4:网关Ping地址平均访问时延;
	 *            5:线路详细信息;6:PPPoE仿真;7:DHCP仿真;8:Traceroute诊断;9:VoIP诊断;10:HTTP下载仿真
	 * @param diagnoseLogDao
	 *            业务DAO
	 * @throws Exception
	 */
	public static void saveDiagnoseLog(Map<String, Object> parameter, GatewayInfo gi, Integer dt, DiagnoseLogMapper diagnoseLogDao) throws Exception {
		DiagnoseLog dl = new DiagnoseLog();
		dl.setGatewayMacaddress(gi.getGatewayMacaddress());
		dl.setDiagnoseType(dt);
		dl.setDiagnoseOperator(parameter.get("userName") + "");
		dl.setDiagnoseTime(new Date());
		diagnoseLogDao.insert(dl);
	}
}
