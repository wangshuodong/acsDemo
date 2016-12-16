package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.dubbo.rpc.RpcContext;
import com.cmiot.rms.common.enums.*;
import com.cmiot.rms.services.*;
import com.cmiot.rms.services.util.InstructionUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSON;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.DiagnoseLogMapper;
import com.cmiot.rms.dao.mapper.DiagnoseThresholdValueMapper;
import com.cmiot.rms.dao.model.DiagnoseLog;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.LANDeviceHostInfo;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.DiagnosticsUtil;

/**
 * Created by wangzhen on 2016/4/11.
 */
public class SendInstructServiceImpl implements SendInstructService {

	private static Logger logger = LoggerFactory.getLogger(SendInstructServiceImpl.class);

	@Autowired
	DiagnoseLogMapper diagnoseLogDao;

	@Autowired
	LogManagerService logManagerService;

	@Autowired
	InstructionsService instructionsService;

	@Autowired
	GatewayInfoService gatewayInfoService;

	@Autowired
	InstructionMethodService instructionMethodService;

	@Autowired
	private RedisClientTemplate redisClientTemplate;
	
	@Autowired
	DiagnoseThresholdValueMapper diagnoseThresholdValueMapper;

	@Value("${ping.hostPath}")
	String hostPath;

	@Value("${ping.hostName}")
	String hostName;

	@Value("${ping.datablocksize}")
	String datablocksize;

	@Value("${ping.numberofrepetitions}")
	String numberofrepetitions;

	@Value("${ping.timeout}")
	String timeout;

	@Value("${diagnostics.temp.wait.time}")
	Integer waitTime;

	@Value("${diagnostics.failure.max.time}")
	Integer failureMaxTime;

	@Value("${diagnostics.redis.wait.time}")
	Integer redisTime;

	// 临时添加从配置文件获取此值
	@Value("${diagnostics.internet.vlanidmark}")
	String vlanidmark;

	@Value("${dubbo.provider.port}")
	int providerPort;

	@Override
	public Map<String, Object> getLANDeviceInfo(Map<String, Object> parameter) {
		parameter.put("loguuid", UniqueUtil.uuid());
		logger.info("LogId:{} 获取路由器LAN口信息，已经无线连接信息接口请求参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();// 返回给页面的数据
		try {
			if (DiagnosticsUtil.checkParm(parameter, rm, Arrays.asList(new String[] { "gatewayId", "userName", "roleName" }))) {
				GatewayInfo gi = gatewayInfoService.selectGatewayInfo(new GatewayInfo(parameter.get("gatewayId") + ""));// 通过网关ID获取网关信息
				if (null != gi) {
					Map<String, Object> result = instructionMethodService.getParameterNames(gi.getGatewayMacaddress(), ConstantDiagnose.LANDEVICE, false);// 1、获取InternetGatewayDevice.LANDevice.下面的参数名称信息
					logger.info("LogId:{} 获取InternetGatewayDevice.LANDevice.下面的参数名称信息:{}", parameter.get("loguuid"), result);
					if (null != result && result.size() > 0) {
						Map<String, List<String>> listMap = this.getNameToReg(result);// 根据正则表达式获取数据
						logger.info("LogId:{} 根据正则表达式获取数据:{}", parameter.get("loguuid"), listMap);
						if (null != listMap && listMap.size() > 0) {
							List<String> lanList = listMap.get("lanList");// 2、获取路由器LAN口的value值
							logger.info("LogId:{} 得到路由器LAN口的节点路径:{}", parameter.get("loguuid"), lanList);
							if (null != lanList && lanList.size() > 0) {
								Map<String, Object> resultLan = instructionMethodService.getParameterValues(gi.getGatewayMacaddress(), lanList);
								logger.info("LogId:{} 网关获取路由器LAN口的value值:{}", parameter.get("loguuid"), resultLan);
								if(null != resultLan && resultLan.size()>0){
									rm = this.getLanValue(resultLan);
								}else{
									packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关获取路由器LAN口信息失败", "");
								}
							}
							// 注释掉获取网关下挂设备数据，另开接口提供
							// List<String> wifiList = (List<String>) listMap.get("wifiList");// 3、获取路由器当前连接无线设备列表
							// List<LANDeviceHostInfo> retrenList = new ArrayList<>();
							// if (null != retrenList && wifiList.size() > 0) {
							// Map<String, Object> resultWifi = instructionMethodService.getParameterValues(gi.getGatewayMacaddress(), wifiList);
							// retrenList = this.getWifiValue(resultWifi);
							// }
							saveDiagnoseLog(parameter, gi.getGatewayMacaddress(), 1);// 保存诊断日志(报表需要)
							packageBackInfo(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), "");
						} else {
							packageBackInfo(rm, RespCodeEnum.RC_1.code(), "没有获取到节点名称", "");
						}
					} else {
						packageBackInfo(rm, RespCodeEnum.RC_1.code(), "没有获取到LAN信息和无线列表信息", "");
					}
				} else {
					packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到网关信息", "");
				}
			}
		} catch (Exception e) {
			packageBackInfo(rm, RespCodeEnum.RC_ERROR.code(), "获取路由器LAN口信息与已无线连接信息异常", "");
			logger.error("getLANDeviceInfo exception " + e.getMessage(), e);
		}
		saveOperationDiagnoseLog(parameter, rm, "获取路由器LAN口信息与已无线连接信息");// 保存操作日志
		logger.info("LogId:{} 获取路由器LAN口信息返回结果:{}", parameter.get("loguuid"), rm);
		return rm;
	}

	@Override
	public Map<String, Object> getUsageValue(Map<String, Object> parameter) {
		logger.info("获取CUP、内存占用比例接口请求参数{}", parameter);
		Map<String, Object> rm = new HashMap<>();// 返回给页面的数据
		try {
			if (DiagnosticsUtil.checkParm(parameter, rm, Arrays.asList(new String[] { "gatewayId", "userName", "roleName" }))) {
				GatewayInfo gi = gatewayInfoService.selectGatewayInfo(new GatewayInfo(parameter.get("gatewayId") + ""));// 通过网关ID获取网关信息
				if (null != gi) {
					List<String> usageList = new ArrayList<String>();
					usageList.add(ConstantDiagnose.CPU_USAGE);
					usageList.add(ConstantDiagnose.RAM_TOTAL);
					usageList.add(ConstantDiagnose.RAM_FREE);
					Map<String, Object> mapValue = instructionMethodService.getParameterValues(gi.getGatewayMacaddress(), usageList);
					int freeRam = 0;
					int totalRam = 0;
					if (null != mapValue && mapValue.size() > 0) {
						Map<String, Object> dataMap = new HashMap<>();
						for (Map.Entry<String, Object> entry : mapValue.entrySet()) {
							String name = entry.getKey();
							String value = entry.getValue() + "";
							if (name.contains(ConstantDiagnose.CPU_USAGE)) {
								dataMap.put("CPUUsage", value);
							} else if (name.contains(ConstantDiagnose.RAM_FREE)) {
								freeRam = Integer.parseInt(value);
							} else if (name.contains(ConstantDiagnose.RAM_TOTAL)) {
								totalRam = Integer.parseInt(value);
							}
						}
						if (freeRam != 0 && totalRam != 0) {
							double ramPercent = (1 - freeRam / (double) totalRam) * 100;
							dataMap.put("RAMUsage", (int) Math.rint(ramPercent));
						} else {
							dataMap.put("RAMUsage", 0);
						}

						//返回总内存大小和未使用内存大小
						dataMap.put("RAMFree", freeRam);
						dataMap.put("RAMTotal", totalRam);

						saveDiagnoseLog(parameter, gi.getGatewayMacaddress(), 2);// 保存诊断日志(报表需要)
						packageBackInfo(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), dataMap);
					} else {
						packageBackInfo(rm, RespCodeEnum.RC_1.code(), "没有查询到CPU与内存的信息", "");
					}
				} else {
					packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到网关信息", "");
				}
			}
		} catch (Exception e) {
			packageBackInfo(rm, RespCodeEnum.RC_ERROR.code(), "CPU、内存占用下发指令网络连接异常", "");
			logger.error("getUsageValue exception " + e.getMessage(), e);
		}
		saveOperationDiagnoseLog(parameter, rm, "获取CUP与内存占用比例");// 保存操作日志
		return rm;
	}

	@Override
	public Map<String, Object> diagnose(Map<String, Object> parameter) {
		long bt = System.currentTimeMillis();
		parameter.put("loguuid", UniqueUtil.uuid());
		logger.info("LogId:{}获取网络诊断接口请求参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		String key = null;// Redis锁
		try {
			DiagnosticsUtil.getPropertiesVlanidMark(parameter, vlanidmark, logger, "Ping-diagnose");// 临时添加从此方法中获取vlanidmark
			String[] parmlist = { "gatewayId", "host", "dataBlockSize", "numberOfRepetitions", "timeout", "userName", "roleName", "vlanidmark" };
			long cpst = System.currentTimeMillis();
			if (DiagnosticsUtil.checkParm(parameter, rm, Arrays.asList(parmlist))) {
				logger.info("LogId:{}验证参数消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - cpst));
				long qgist = System.currentTimeMillis();
				GatewayInfo gi = gatewayInfoService.selectGatewayInfo(new GatewayInfo(parameter.get("gatewayId") + ""));// 通过网关ID获取网关信息
				logger.info("LogId:{}以网关ID为条件查询网关信息:{}消耗时间:{}", parameter.get("loguuid"), gi, (System.currentTimeMillis() - qgist));
				if (null != gi) {
					key = gi.getGatewayMacaddress() + "_ping";
					// nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在时才进行set，如果取XX，则只有当key已经存在时才进行set;expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
					String ls = redisClientTemplate.set(key, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
					if (StringUtils.isNotBlank(ls)) {// 锁存在
						if (null == RequestCache.get("diagnostics_" + gi.getGatewayMacaddress())) {
							long sst = System.currentTimeMillis();
							Pattern pattern = Pattern.compile("InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.(WANIPConnection|WANPPPConnection).[\\d]+.(X_CMCC_ServiceList|ConnectionType|AddressingType|Enable|ConnectionStatus)");
							// 网关上获取Wan连接的状态与路径
							Map<String, Object> rb = DiagnosticsUtil.getWanConnectStatusAndPath(parameter, gi, instructionMethodService, pattern, logger, "Ping-diagnose", "INTERNET", new String[] { "PPPoE_Routed", "IP_Routed" }, null);
							logger.info("LogId:{}Ping-diagnose网关上获取Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
							if (null != rb && !"0".equals(rb.get("status") + "")) {
								// 网关未获取INTERNET的WANl连接,调用创建WAN连接方法
								if ("1".equals(rb.get("status") + "")) {
									rb = DiagnosticsUtil.createDHCPWanConnect(parameter, gi, instructionMethodService, logger, "Ping-diagnose", "INTERNET", rb.get("path") + "");
									logger.info("LogId:{} Ping-diagnose网关上创建Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
									if(null != rb && "2".equals(rb.get("status") + "")){
										rb = DiagnosticsUtil.getWanConnectionStatus(parameter, rb.get("path") + "", 10, gi, instructionMethodService, logger, "Ping-diagnose");
										logger.info("LogId:{} Ping-diagnose 休眠后返回数据:{}", parameter.get("loguuid"), rb);
									}
								}
								if (null != rb && "2".equals(rb.get("status") + "")) {
									if (settingPingParameterAndRequestGateway(parameter, gi, rb.get("path") + "")) {// 在网关节点上设置Ping诊断值
										logger.info("LogId:{}在网关节点上设置Ping诊断值参数消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - sst));
										DiagnosticsUtil.temporaryObjectLock(waitTime, gi, logger, "diagnose", parameter.get("loguuid") + "");// 临时对象锁,为了等待8 DIAGNOSTICS COMPLETE状态唤醒 注:waitTime传值为空为>=0时取默认值为120000
										beginDiagnose(parameter, gi, rm, parameter.get("loguuid") + "", "Ping-diagnose");// 网关上设置Ping指令成功,开始诊断获取数据
										// // ------------暂时保留,后续业务使用
										// int countFailureTime = 0;// 记录失败次数
										// long gst = System.currentTimeMillis();
										// while (true) {
										// String[] rst = DiagnosticsUtil.getDiagnosticsStatus(gi, "Complete", "InternetGatewayDevice.IPPingDiagnostics.DiagnosticsState", instructionMethodService, logger, "diagnose");
										// if (Boolean.parseBoolean(rst[0])) {
										// beginDiagnose(parameter, gi, rm);// 网关上设置Ping指令成功,开始诊断获取数据
										// break;
										// } else {
										// if ("1".equals(rst[1])) {
										// DiagnosticsUtil.temporaryObjectLock(waitTime, gi, logger, "diagnose");// 临时对象锁,为了等待8 DIAGNOSTICS COMPLETE状态唤醒 注:waitTime传值为空为>=0时取默认值为120000
										// // 当失败次数大于限定次数再获取一次网关结果作为返回结果
										// if (countFailureTime >= (null != failureMaxTime && failureMaxTime > 0 ? failureMaxTime : 1)) {
										// // beginDiagnose(parameter, gi, rm);// 网关上设置Ping指令成功,开始诊断获取数据
										// logger.info("diagnose诊断后获取诊断状态已经超出最大失败次数:" + failureMaxTime);
										// DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取网关诊断后状态与规范非一致", "");
										// break;
										// }
										// } else {
										// DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到网关诊断状态", "");
										// break;
										// }
										// }
										// countFailureTime++;
										// }
										// logger.info("While循环耗时间:" + (System.currentTimeMillis() - gst));
										// // --------
										saveDiagnoseLog(parameter, gi.getGatewayMacaddress(), 3);// 保存诊断日志(报表需要)
									} else {
										packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关诊断参数设置失败", "");
									}
								} else {
									DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关不存在IPWAN连接,并且创建IPWAN连接失败", "");
								}
								DiagnosticsUtil.deleteCreateWanConnect(parameter, gi, instructionMethodService, logger, "Ping-diagnose", rb.get("path") + "", (boolean) rb.get("delFlag"));// 删除创建的WAN连接
							} else {
								DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取网关WAN连接信息失败", "");
							}
						} else {
							logger.info("LogId:{}诊断指令已发送,为防止用户重复提交,添加临时对象锁，等待指令异步返回中", parameter.get("loguuid"));
							packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关正在诊断中,请无重复提交", "");
						}
						if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
					} else {
						packageBackInfo(rm, RespCodeEnum.RC_1.code(), "当前网关正在诊断,请稍后再试", "");
					}
				} else {
					packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到网关信息", "");
				}
			}
		} catch (Exception e) {
			if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 异常情况下删除Redis锁
			packageBackInfo(rm, RespCodeEnum.RC_ERROR.code(), "网络诊断接口异常", "");
			logger.error("diagnose exception " + e.getMessage(), parameter.get("loguuid"), e);
		}
		saveOperationDiagnoseLog(parameter, rm, "网络诊断");// 保存操作日志
		logger.info("LogId:{}diagnose业务诊断返回的结果集:{}总消耗时间:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - bt));
		return rm;
	}

	@Override
	public Map<String, Object> getPingAddressInfo(Map<String, Object> parameter) {
		long bt = System.currentTimeMillis();
		parameter.put("loguuid", UniqueUtil.uuid());
		logger.info("LogId:{}获取网关Ping地址平均访问时延接口请求参数{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		String key = null;// Redis锁
		try {
			DiagnosticsUtil.getPropertiesVlanidMark(parameter, vlanidmark, logger, "Ping");// 临时添加从此方法中获取vlanidmark
			if (DiagnosticsUtil.checkParm(parameter, rm, Arrays.asList(new String[] { "gatewayId", "userName", "roleName", "vlanidmark" }))) {
				GatewayInfo gi = gatewayInfoService.selectGatewayInfo(new GatewayInfo(parameter.get("gatewayId") + ""));// 通过网关ID获取网关信息
				logger.info("LogId:{}以网关ID为条件查询网关信息获取参数:{}", parameter.get("loguuid"), gi);
				if (null != gi) {
					key = gi.getGatewayMacaddress() + "_ping";
					logger.info("LogId:{}Ping网关前生成RedisKey值:{}", parameter.get("loguuid"), key);
					// nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在时才进行set，如果取XX，则只有当key已经存在时才进行set
					// expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
					String ls = redisClientTemplate.set(key, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
					logger.info("LogId:{}Ping网关SetRedis状态:{}", parameter.get("loguuid"), ls);
					if (StringUtils.isNotBlank(ls)) {// 锁存在
						if (null != hostPath && null != hostName && !"${ping.hostPath}".equals(hostPath) && !"${ping.hostName}".equals(hostName)) {
							String[] host = hostPath.split(","), name = hostName.split(",");// 拆分ping地址与ping名称
							if (null != host && null != name && host.length > 0 && name.length > 0) {
								Pattern pattern = Pattern.compile("InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.(WANIPConnection|WANPPPConnection).[\\d]+.(X_CMCC_ServiceList|ConnectionType|AddressingType|Enable|ConnectionStatus)");
								// 网关上获取Wan连接的状态与路径
								Map<String, Object> rb = DiagnosticsUtil.getWanConnectStatusAndPath(parameter, gi, instructionMethodService, pattern, logger, "Ping", "INTERNET", new String[] { "PPPoE_Routed", "IP_Routed" }, null);
								logger.info("LogId:{}Ping网关上获取Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
								if (null != rb && !"0".equals(rb.get("status") + "")) {
									// 网关未获取INTERNET的WANl连接,调用创建WAN连接方法
									if ("1".equals(rb.get("status") + "")) {
										rb = DiagnosticsUtil.createDHCPWanConnect(parameter, gi, instructionMethodService, logger, "Ping", "INTERNET", rb.get("path") + "");
										logger.info("LogId:{} Ping网关上创建Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
										if(null != rb && "2".equals(rb.get("status") + "")){
											rb = DiagnosticsUtil.getWanConnectionStatus(parameter, rb.get("path") + "", 10, gi, instructionMethodService, logger, "Ping");
											logger.info("LogId:{} Ping网关休眠后返回数据:{}", parameter.get("loguuid"), rb);
										}
									}
									if (null != rb && "2".equals(rb.get("status") + "")) {
										this.getOtherPamer(parameter);// 获取其它参数
										List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
										for (int i = 0, inv = host.length; i < inv; i++) {
											parameter.put("host", host[i]);// 循环每一次地址
											logger.info(new StringBuffer().append("LogId:{}在网关节点上设置Ping诊断值,host[").append(host[i]).append("]datablocksize[").append(datablocksize).append("]numberofrepetitions[").append(numberofrepetitions).append("]timeout[").append(timeout).append("]").toString(), parameter.get("loguuid"));
											if (settingPingParameterAndRequestGateway(parameter, gi, rb.get("path") + "")) {
												DiagnosticsUtil.temporaryObjectLock(waitTime, gi, logger, "getPingAddressInfo", parameter.get("loguuid") + "");// 临时对象锁,为了等待8 DIAGNOSTICS COMPLETE状态唤醒 注:waitTime传值为空为>=0时取默认值为120000
												if (StringUtils.isNotBlank(host[i]) && StringUtils.isNotBlank(name[i])) {
													packageGetPingAddressInfo(result, host[i].trim(), name[i].trim(), gi, parameter.get("loguuid") + "");
												} else {
													packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取Ping地址与Ping地址名称", "");
													saveOperationDiagnoseLog(parameter, rm, "网关Ping地址平均访问时延");// 保存操作日志
													if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 异常情况下删除Redis锁
													return rm;
												}
												// -------------------暂时保留,后续业务使用
												// int countFailureTime = 0;// 记录失败次数
												// while (true) {
												// String[] rst = DiagnosticsUtil.getDiagnosticsStatus(gi, "Complete", "InternetGatewayDevice.IPPingDiagnostics.DiagnosticsState", instructionMethodService, logger, "getPingAddressInfo");
												// if (Boolean.parseBoolean(rst[0])) {
												// if (StringUtils.isNotBlank(host[i]) && StringUtils.isNotBlank(name[i])) {
												// packageGetPingAddressInfo(result, host[i].trim(), name[i].trim(), gi);
												// } else {
												// packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取Ping地址与Ping地址名称", "");
												// saveOperationDiagnoseLog(parameter, rm, "网关Ping地址平均访问时延");// 保存操作日志
												// if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 异常情况下删除Redis锁
												// return rm;
												// }
												// break;
												// } else {
												// if ("1".equals(rst[1])) {
												// DiagnosticsUtil.temporaryObjectLock(waitTime, gi, logger, "getPingAddressInfo");// 临时对象锁,为了等待8 DIAGNOSTICS COMPLETE状态唤醒 注:waitTime传值为空为>=0时取默认值为120000
												// // 当失败次数大于限定次数再获取一次网关结果作为返回结果
												// if (countFailureTime >= (null != failureMaxTime && failureMaxTime > 0 ? failureMaxTime : 1)) {
												// if (StringUtils.isNotBlank(host[i]) && StringUtils.isNotBlank(name[i])) {
												// logger.info("getPingAddressInfo诊断后获取诊断状态已经超出最大失败次数:" + failureMaxTime);
												// setResultInfo(result, host[i].trim(), name[i].trim(), "-1");
												// } else {
												// packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取Ping地址与Ping地址名称", "");
												// saveOperationDiagnoseLog(parameter, rm, "网关Ping地址平均访问时延");// 保存操作日志
												// if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 异常情况下删除Redis锁
												// return rm;
												// }
												// break;
												// }
												// } else {
												// setResultInfo(result, host[i].trim(), name[i].trim(), "-1");
												// break;
												// }
												// }
												// countFailureTime++;
												// }
												// -------------------
											} else {
												if (StringUtils.isNotBlank(host[i]) && StringUtils.isNotBlank(name[i])) {
													setResultInfo(result, host[i].trim(), name[i].trim(), "-1");
												} else {
													packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取Ping地址与Ping地址名称", "");
													saveOperationDiagnoseLog(parameter, rm, "网关Ping地址平均访问时延");// 保存操作日志
													if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 异常情况下删除Redis锁
													return rm;
												}
											}
										}
										packageBackInfo(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), result);
										saveDiagnoseLog(parameter, gi.getGatewayMacaddress(), 4);// 保存诊断日志(报表需要)
									} else {
										DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关不存在IPWAN连接,并且创建IPWAN连接失败", "");
									}
									DiagnosticsUtil.deleteCreateWanConnect(parameter, gi, instructionMethodService, logger, "Ping", rb.get("path") + "", (boolean) rb.get("delFlag"));// 删除创建的WAN连接
								} else {
									DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取网关WAN连接信息失败", "");
								}
							} else {
								packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到Ping地址相关信息", "");
							}
						} else {
							packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到Ping地址相关信息", "");
						}
						if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
					} else {
						packageBackInfo(rm, RespCodeEnum.RC_1.code(), "当前网关正在诊断,请稍后再试", "");
					}
				} else {
					packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到网关信息", "");
				}
			}
		} catch (Exception e) {
			if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 异常情况下删除Redis锁
			packageBackInfo(rm, RespCodeEnum.RC_ERROR.code(), "网络诊断接口异常", "");
			logger.error("LogId:{}get Ping Address Info exception " + e.getMessage(), parameter.get("loguuid"), e);
		}
		saveOperationDiagnoseLog(parameter, rm, "网关Ping地址平均访问时延");// 保存操作日志
		logger.info("LogId:{}getPingAddressInfo业务诊断返回的结果集:{}总消耗时间:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - bt));
		return rm;
	}

	/**
	 * 获取其它参数
	 * 
	 * @param parameter
	 */
	private void getOtherPamer(Map<String, Object> parameter) {
		datablocksize = (StringUtils.isNotBlank(datablocksize) && !"${ping.datablocksize}".equals("datablocksize")) ? datablocksize : "56";
		parameter.put("dataBlockSize", datablocksize);// 包大小（字节）
		numberofrepetitions = (StringUtils.isNotBlank(numberofrepetitions) && !"${ping.numberofrepetitions}".equals(numberofrepetitions)) ? numberofrepetitions : "4";
		parameter.put("numberOfRepetitions", numberofrepetitions);// PING的次数
		timeout = (StringUtils.isNotBlank(timeout) && !"${ping.timeout}".equals(timeout)) ? timeout : "10000";
		parameter.put("timeout", timeout);// 超时时间（毫秒）
	}

	@Override
	public Map<String, Object> lineDetails(Map<String, Object> parameter) {
		String logname = "lineDetails";
		parameter.put("loguuid", UniqueUtil.uuid());
		logger.info("LogId:{} " + logname + "获取网络诊断线路详细信息接口请求参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		try {
			if (DiagnosticsUtil.checkParm(parameter, rm, Arrays.asList(new String[] { "gatewayId", "userName", "roleName" }))) {
				GatewayInfo gi = gatewayInfoService.selectGatewayInfo(new GatewayInfo(parameter.get("gatewayId") + ""));// 通过网关ID获取网关信息
				if (null != gi) {
					List<String> list = new ArrayList<String>();// 获取诊断信息网关节点路径集
					this.getGponValue(gi.getGatewayMacaddress(), list, rm, parameter.get("loguuid") + "", logname);// 1、处理线路详细信息
					this.getResultMapDate(gi, list, rm, parameter.get("loguuid") + "", logname);// 获取线路网关返回结果数据
					saveDiagnoseLog(parameter, gi.getGatewayMacaddress(), 5);// 保存诊断日志(报表需要)
				} else {
					packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到网关信息", "");
				}
			}
		} catch (Exception e) {
			packageBackInfo(rm, RespCodeEnum.RC_ERROR.code(), "网络诊断线路详细信息接口异常", "");
			logger.error("line details exception " + e.getMessage(), e);
		}
		saveOperationDiagnoseLog(parameter, rm, "诊断线路详细信息");// 保存操作日志
		logger.info("LogId:{} " + logname + "获取网络诊断线路详细信息返回结果:{}", parameter.get("loguuid"), rm);
		return rm;
	}

	/**
	 * 获取网关下挂设备
	 *
	 * @param parameter
	 * @return
	 */
	@Override
	public Map<String, Object> getGatewayLANDevice(Map<String, Object> parameter) {
		logger.info("Start invoke getLANDevice:{}", parameter);
		String gatewayId = null != parameter.get("gatewayId") ? parameter.get("gatewayId").toString() : "";
		Map<String, Object> retMap = new HashMap<>();
		if (StringUtils.isEmpty(gatewayId)) {
			retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
			logger.info("End invoke getLANDevice:{}", retMap);
			return retMap;
		}

		// 1.获取网关
		GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayId);
		if (null == gatewayInfo) {
			retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_ERROR.getResultMsg());
			logger.info("End invoke getLANDevice:{}", retMap);
			return retMap;
		}

		// 2.获取lan节点名称
		Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayInfo.getGatewayMacaddress(), "InternetGatewayDevice.LANDevice.", false);

		logger.info("getGatewayLANDevice getParameterNames nameMap:{}", nameMap);

		List<String> regNameList = new ArrayList<>();
		regNameList.add("InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.MACAddress");
		regNameList.add("InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.IPAddress");
		regNameList.add("InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.HostName");
		regNameList.add("InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.InterfaceType");
		regNameList.add("InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.Active");
		regNameList.add("InternetGatewayDevice.LANDevice.[0-9]+.Hosts.HostNumberOfEntries");

		List<String> nameList = new ArrayList<>();
		// 3.根据正则表达式匹配查找真正的lan节点名称
		for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
			InstructionUtil.getName(nameList, regNameList, entry.getKey());
		}

		// 4.获取下挂设备值
		Map<String, Object> lanValueMap = instructionMethodService.getParameterValues(gatewayInfo.getGatewayMacaddress(), nameList);
		logger.info("getGatewayLANDevice getParameterValues lanValueMap:{}", lanValueMap);
		int hostNumber = 0;
		List<LANDeviceHostInfo> hostList = new ArrayList<>();
		LANDeviceHostInfo lanDeviceHostInfo;
		for (Map.Entry<String, Object> entry : lanValueMap.entrySet()) {
			if (entry.getKey().endsWith("HostNumberOfEntries")) {
				hostNumber += Integer.valueOf(entry.getValue().toString());
			}

			if (entry.getKey().endsWith("MACAddress")) {
				String preHost = entry.getKey().substring(0, entry.getKey().indexOf("MACAddress"));
				lanDeviceHostInfo = new LANDeviceHostInfo();
				lanDeviceHostInfo.setMACAddress(String.valueOf(entry.getValue()));
				lanDeviceHostInfo.setHostName(String.valueOf(lanValueMap.get(preHost + "HostName")));
				lanDeviceHostInfo.setiPAddress(String.valueOf(lanValueMap.get(preHost + "IPAddress")));
				lanDeviceHostInfo.setActive(Boolean.valueOf(lanValueMap.get(preHost + "Active").toString()));

				String interfaceType = String.valueOf(lanValueMap.get(preHost + "InterfaceType"));
				if(interfaceType.equals("Ethernet"))
				{
					lanDeviceHostInfo.setInterfaceType("有线");
				}
				else if(interfaceType.equals("802.11"))
				{
					lanDeviceHostInfo.setInterfaceType("无线");
				}
				else
				{
					lanDeviceHostInfo.setInterfaceType("其他");
				}

				hostList.add(lanDeviceHostInfo);
			}
		}

		String key = "on_offline_" + gatewayInfo.getGatewayUuid();
		Map<String, String> saveMap = redisClientTemplate.hgetAll(key);
		if (null == saveMap || saveMap.size() < 1) {
			redisClientTemplate.hset(key, "hostNumber", String.valueOf(hostNumber));
			for (LANDeviceHostInfo info : hostList) {
				redisClientTemplate.hset(key, info.getMACAddress(), JSON.toJSONString(info));
			}

			retMap.put("online", 0);
			retMap.put("offline", 0);
			retMap.put("onlineList", new ArrayList<>());
			retMap.put("offlineList", new ArrayList<>());
		} else {
			List<LANDeviceHostInfo> oldHostList = new ArrayList<>();
			LANDeviceHostInfo oldLanDeviceHostInfo;
			for (Map.Entry<String, String> entry : saveMap.entrySet()) {
				if (!entry.getKey().equals("hostNumber")) {
					String strInfo = entry.getValue();
					oldLanDeviceHostInfo = JSON.parseObject(strInfo, LANDeviceHostInfo.class);
					oldHostList.add(oldLanDeviceHostInfo);
				}
			}

			List<LANDeviceHostInfo> onlineHostList = new ArrayList<>();
			List<LANDeviceHostInfo> offlineHostList = new ArrayList<>();

			for (LANDeviceHostInfo newInfo : hostList) {
				// 新增的下挂设备，Active状态为true
				if (!oldHostList.contains(newInfo) && newInfo.isActive()) {
					onlineHostList.add(newInfo);
				} else if (oldHostList.contains(newInfo)// 原有设备active由false变为true
						&& newInfo.isActive() && !getLANDeviceHostInfo(oldHostList, newInfo.getMACAddress()).isActive()) {
					onlineHostList.add(newInfo);
				}
			}
			for (LANDeviceHostInfo oldInfo : oldHostList) {
				if (!hostList.contains(oldInfo)) {
					offlineHostList.add(oldInfo);
				} else if (hostList.contains(oldInfo) && oldInfo.isActive() && !getLANDeviceHostInfo(hostList, oldInfo.getMACAddress()).isActive()) {
					offlineHostList.add(oldInfo);
				}
			}

			// 删除旧数据，保存新数据
			redisClientTemplate.del(key);
			redisClientTemplate.hset(key, "hostNumber", String.valueOf(hostNumber));
			for (LANDeviceHostInfo info : hostList) {
				redisClientTemplate.hset(key, info.getMACAddress(), JSON.toJSONString(info));
			}
			retMap.put("online", onlineHostList.size());
			retMap.put("offline", offlineHostList.size());
			retMap.put("onlineList", JSON.toJSON(onlineHostList));
			retMap.put("offlineList", JSON.toJSON(offlineHostList));
		}

		retMap.put("hostNumber", hostNumber);
		retMap.put(Constant.DATA, JSON.toJSON(hostList));
		retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
		retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());

		logger.info("End invoke getLANDevice:{}", retMap);
		return retMap;
	}

	private LANDeviceHostInfo getLANDeviceHostInfo(List<LANDeviceHostInfo> hostList, String macaddress) {
		for (LANDeviceHostInfo info : hostList) {
			if (info.getMACAddress().equals(macaddress)) {
				return info;

			}
		}

		return null;
	}

	/**
	 * 封装获取Ping地址时延值
	 * 
	 * @param result
	 *            返回数据集
	 * @param host
	 *            地址URL
	 * @param name
	 *            地址名称
	 * @param gi
	 *            网关信息对象
	 * @param logid
	 *            日志ID
	 * @throws Exception
	 */
	private void packageGetPingAddressInfo(List<Map<String, Object>> result, String host, String name, GatewayInfo gi, String logid) throws Exception {
		List<String> list = new ArrayList<String>();// 获取诊断信息网关节点路径集
		list.add(ConstantDiagnose.IPPINGDIAGNOSTICS_AVERAGERESPONSETIME);
		Map<String, Object> resultDiagnose = instructionMethodService.getParameterValues(gi.getGatewayMacaddress(), list);// 下发指令获取诊断参数信息
		logger.info("LogId:{}获取Ping地址参数值,网关返回数据对象:{}", logid, resultDiagnose);
		if (null != resultDiagnose && resultDiagnose.size() > 0) {
			for (Map.Entry<String, Object> entry : resultDiagnose.entrySet()) {
				setResultInfo(result, host, name, entry.getValue() + "");
			}
		} else {
			setResultInfo(result, host, name, "-1");
		}
	}

	/**
	 * 封装返回结果内容
	 * 
	 * @param result
	 *            返回对象
	 * @param url
	 *            Ping地址路径
	 * @param hostName
	 *            Ping地址名称
	 * @param averageResponseTime
	 *            平均时延
	 */
	private void setResultInfo(List<Map<String, Object>> result, String url, String hostName, String averageResponseTime) {
		Map<String, Object> vt = new HashMap<>();
		vt.put("url", url);
		vt.put("hostName", hostName);
		vt.put("averageResponseTime", averageResponseTime);
		result.add(vt);
	}

	/**
	 * 保存诊断日志
	 * 
	 * @param parameter
	 *            参数集
	 * @param mac
	 *            mac地址
	 * @param dt
	 *            诊断类型1:获取路由器LAN口信息与已经无线连接信息;2:获取CUP与内存占用比例;3:网络诊断
	 * @throws Exception
	 */
	private void saveDiagnoseLog(Map<String, Object> parameter, String mac, Integer dt) throws Exception {
		DiagnoseLog dl = new DiagnoseLog();
		dl.setGatewayMacaddress(mac);
		dl.setDiagnoseType(dt);
		dl.setDiagnoseOperator(parameter.get("userName") + "");
		dl.setDiagnoseTime(new Date());
		diagnoseLogDao.insert(dl);
	}

	/**
	 * 保存操作日志
	 * 
	 * @param parameter
	 *            参数集
	 * @param rm
	 *            返回对象
	 */
	private void saveOperationDiagnoseLog(Map<String, Object> parameter, Map<String, Object> rm, String operation) {
		parameter.put("categoryMenu", CategoryEnum.GATEWAY_MANAGER_SERVICE.name());// 类目ID
		parameter.put("operation", operation);// 具体的操作
		parameter.put("categoryMenuName", CategoryEnum.GATEWAY_MANAGER_SERVICE.description());// 类目名称
		parameter.put("content", "请求报文" + JSON.toJSONString(parameter) + ",返回报文" + JSON.toJSONString(rm));// 操作的数据内容
		parameter.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());// 日志类别
		logManagerService.recordOperationLog(parameter);
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
	public Boolean settingPingParameterAndRequestGateway(Map<String, Object> parameter, GatewayInfo gi, String path) {
		//保存当前诊断调用方地址，供ACS回调使用
		String providerUrl = "dubbo://" + RpcContext.getContext().getLocalHost() + ":" + providerPort + "/" + AcsInterfaceService.class.getName();
		redisClientTemplate.set(gi.getGatewayFactoryCode() + Constant.SEPARATOR + gi.getGatewaySerialnumber() + Constant.SEPARATOR + "diagnose", providerUrl);
		return instructionMethodService.setParameterValue(gi.getGatewayMacaddress(), this.setParameter(parameter, gi, path));// 组装数据
	}

	/**
	 * 在不同的节点下组装设置参数
	 * 
	 * @param parameter
	 *            参数集
	 * @param gi
	 *            网关对象
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<ParameterValueStruct> setParameter(Map<String, Object> parameter, GatewayInfo gi, String path) {
		List<ParameterValueStruct> list = new ArrayList<>();
		ParameterValueStruct parameterHost = new ParameterValueStruct();
		parameterHost.setName(ConstantDiagnose.IPPINGDIAGNOSTICS_HOST);
		parameterHost.setValue(parameter.get("host") + "".trim());// PING的IP地址
		parameterHost.setValueType("string");
		parameterHost.setReadWrite(true);
		list.add(parameterHost);

		ParameterValueStruct parameterDataBlockSize = new ParameterValueStruct();
		parameterDataBlockSize.setName(ConstantDiagnose.IPPINGDIAGNOSTICS_DATABLOCKSIZE);
		parameterDataBlockSize.setValue(parameter.get("dataBlockSize") + "".trim());// 包大小（字节）
		parameterDataBlockSize.setValueType("unsignedInt");
		parameterDataBlockSize.setReadWrite(true);
		list.add(parameterDataBlockSize);

		ParameterValueStruct parameterNumberOfRepetitions = new ParameterValueStruct();
		parameterNumberOfRepetitions.setName(ConstantDiagnose.IPPINGDIAGNOSTICS_NUMBEROFREPETITIONS);
		parameterNumberOfRepetitions.setValue(parameter.get("numberOfRepetitions") + "".trim());// PING的次数
		parameterNumberOfRepetitions.setValueType("unsignedInt");
		parameterNumberOfRepetitions.setReadWrite(false);
		list.add(parameterNumberOfRepetitions);

		ParameterValueStruct parameterTimeout = new ParameterValueStruct();
		parameterTimeout.setName(ConstantDiagnose.IPPINGDIAGNOSTICS_TIMEOUT);
		parameterTimeout.setValue(parameter.get("timeout") + "".trim());// 超时时间（毫秒）
		parameterTimeout.setValueType("unsignedInt");
		parameterTimeout.setReadWrite(true);
		list.add(parameterTimeout);

		ParameterValueStruct parameterExternalIPaddress = new ParameterValueStruct();
		parameterExternalIPaddress.setName(ConstantDiagnose.IPPINGDIAGNOSTICS_INTERFACE);
		// parameterExternalIPaddress.setValue(gi.getGatewayExternalIPaddress().trim());// WAN连接当前IP地址
		parameterExternalIPaddress.setValue(path);// WAN连接当前IP地址
		parameterExternalIPaddress.setValueType("string");
		parameterExternalIPaddress.setReadWrite(true);
		list.add(parameterExternalIPaddress);

		ParameterValueStruct parameterDiagnosticsState = new ParameterValueStruct();
		parameterDiagnosticsState.setName(ConstantDiagnose.IPPINGDIAGNOSTICS_DIAGNOSTICSSTATE);
		parameterDiagnosticsState.setValue("Requested");
		parameterDiagnosticsState.setValueType("string");
		parameterDiagnosticsState.setReadWrite(true);
		list.add(parameterDiagnosticsState);
		return list;
	}

	/**
	 * 封装返回内容
	 * @param rm 返回对象
	 * @param code 状态码
	 * @param msg 状态码描述内容
	 * @param data 返回数据
	 * @throws
	 */
	private void packageBackInfo(Map<String, Object> rm, String code, String msg, Object data) {
		rm.put(Constant.DATA, data);
		rm.put(Constant.CODE, code);
		rm.put(Constant.MESSAGE, msg);
	}

	/**
	 * 开始诊断
	 * @param parameter 参数集
	 * @param gi 网关对象
	 * @param rm 返回对象
	 * @param logid 日志ID
	 * @param logname 日志名称
	 * @return
	 * @throws
	 */
	private Map<String, Object> beginDiagnose(Map<String, Object> parameter, GatewayInfo gi, Map<String, Object> rm, String logid, String logname) throws Exception {
		List<String> list = new ArrayList<String>();// 获取诊断信息网关节点路径集
		this.packagePingAndDial(list);// 2、组装GetParameterValues指令的参数
		this.getResultMapDate(gi, list, rm, logid, logname);
		return rm;
	}

	/**
	 * 获取网关返回结果数据
	 * 
	 * @param gi 网关对象信息
	 * @param list 获取诊断信息网关节点路径集
	 * @param rm 返回对象
	 * @param logid 日志ID
	 * @param logName 日志名称
	 */
	private void getResultMapDate(GatewayInfo gi, List<String> list, Map<String, Object> rm, String logid, String logName) {
		Map<String, Object> resultDiagnose = instructionMethodService.getParameterValues(gi.getGatewayMacaddress(), list);// 下发指令获取诊断参数信息
		logger.info("LogId:{} " + logName + "获取诊断参数信息:{}", logid, resultDiagnose);
		if (null != resultDiagnose && resultDiagnose.size() > 0) {
			Map<String, Object> result = new HashMap<>();
			for (Map.Entry<String, Object> entry : resultDiagnose.entrySet()) {
				String name = entry.getKey();
				String value = String.valueOf(entry.getValue());
				String resultName = name.substring(name.lastIndexOf(".") + 1, name.length());// 获取名称最后一个“.”后的参数为KEY值
				result.put(resultName, value);
			}
			result = DiagnosticsUtil.getDiagnoseThresholdFlag(result, 1, 1, diagnoseThresholdValueMapper, logid, logger, logName);// 获取诊断值的阈值状态
			packageBackInfo(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), result);
		} else {
			packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取网关诊断信息失败", "");
		}
	}

	/**
	 * 组装PING诊断的参数和拨号的参数
	 *
	 * @param list
	 * @return
	 */
	private void packagePingAndDial(List<String> list) {
		// 拨号信息
		// list.add(ConstantDiagnose.EMULATOR_RESULT);
		// PING结果查询数据
		list.add(ConstantDiagnose.IPPINGDIAGNOSTICS_HOST);
		list.add(ConstantDiagnose.IPPINGDIAGNOSTICS_NUMBEROFREPETITIONS);
		list.add(ConstantDiagnose.IPPINGDIAGNOSTICS_TIMEOUT);
		list.add(ConstantDiagnose.IPPINGDIAGNOSTICS_DATABLOCKSIZE);
		list.add(ConstantDiagnose.IPPINGDIAGNOSTICS_SUCCESSCOUNT);
		list.add(ConstantDiagnose.IPPINGDIAGNOSTICS_FAILURECOUNT);
		list.add(ConstantDiagnose.IPPINGDIAGNOSTICS_AVERAGERESPONSETIME);
		list.add(ConstantDiagnose.IPPINGDIAGNOSTICS_MINIMUMRESPONSETIME);
		list.add(ConstantDiagnose.IPPINGDIAGNOSTICS_MAXIMUMRESPONSETIME);
	}

	/**
	 * 获取线路详细信息的KEY值
	 * 
	 * @param gatewayMacAddress
	 *            网关MAC地址
	 * @param list
	 *            存放获取网关地址集对象
	 * @param rm
	 *            返回对象
	 * @throws Exception
	 */
	private void getGponValue(String gatewayMacAddress, List<String> list, Map<String, Object> rm, String logid, String logname) throws Exception {
		Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayMacAddress, ConstantDiagnose.WANDEVICE, false);// GPON 上行状态和统计父级节点
		logger.info("LogId:{} " + logname + "获取GPON上行状态和统计父级节点:{}", logid , nameMap);
		if (null != nameMap && nameMap.size() > 0) {
			List<String> ServiceList = new ArrayList<>();
			List<String> LineDetailsList = new ArrayList<>();
			for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
				String name = entry.getKey();
				Pattern patternService = Pattern.compile("[.][1-9][.]X_CMCC_ServiceList");
				Matcher matchService = patternService.matcher(name);
				// 规定在本WAN连接承载的什么业务列表，以逗号分割
				if (matchService.find()) {
					ServiceList.add(name);
					continue;
				}
				// 线路详情
				Pattern patternLineDetails = Pattern.compile("[.][1-9][.]X_CMCC_GponInterfaceConfig[.]");
				Matcher matchLineDetails = patternLineDetails.matcher(name);
				if (matchLineDetails.find()) LineDetailsList.add(name);
			}
			// 获取规定在本WAN连接承载的什么业务列表中的值 （“TR069”,”INTERNET”,”VOIP”,”，”OTHER”）
			this.getParameterValuesToServiceList(gatewayMacAddress, list, ServiceList, LineDetailsList, rm, logid, logname);
		} else {
			packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取网关上行状态和统计参数节点失败", "");
		}
	}

	/**
	 * 根据ServiceList来判断线路详情中的参数
	 *
	 * @param gatewayMacAddress
	 *            网关MAC地址
	 * @param list
	 *            返回的参数信息
	 * @param serviceList
	 *            WAN连接承载的什么业务
	 * @param lineDetailsList
	 *            GponInterfaceConfig所有信息
	 * @param rm
	 *            返回对象
	 * @return
	 */
	private void getParameterValuesToServiceList(String gatewayMacAddress, List<String> list, List<String> serviceList, List<String> lineDetailsList, Map<String, Object> rm, String logid, String logname) throws Exception {
		Map<String, Object> map = instructionMethodService.getParameterValues(gatewayMacAddress, serviceList);
		logger.info("LogId:{} " + logname + "获取WAN连接承载的什么业务:{}", logid , map);
		if (null != map && map.size() > 0) {
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String name = entry.getKey();
				String value = (null == entry.getValue())?"":entry.getValue().toString();
				// 判断值中是否包含着两个参数
				if (value.contains(Constant.SERVICELIST_INTERNET) || value.contains(Constant.SERVICELIST_TR069)) {
					// 取出X_CMCC_ServiceList参数的第一个{i}的值进行匹配GponInterfaceConfig配置
					String i_arry = name.split("[.]")[2];
					String WANDevice = ConstantDiagnose.WANDEVICE + i_arry;
					if (lineDetailsList.size() > 0) {
						for (int i = 0; i < lineDetailsList.size(); i++) {
							if (lineDetailsList.get(i).contains(WANDevice)) {
								// 将匹配的线路详情信息名称返回
								list.add(lineDetailsList.get(i));
							}
						}
					}
				}
			}
		} else {
			packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取网关WAN连接承载业务列表失败", "");
		}
	}

	/**
	 * 获取wifi数据信息
	 *
	 * @param resultWifi
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<LANDeviceHostInfo> getWifiValue(Map<String, Object> resultWifi) {
		// 遍历取出子级map的值 组装成对象
		List<LANDeviceHostInfo> list = new ArrayList<>();
		Map<String, Object> mapFather = new HashMap<>();
		for (Map.Entry<String, Object> entry : resultWifi.entrySet()) {
			String name = entry.getKey();
			String value = (String) entry.getValue();
			// 获取第二数字
			String map_i = name.split("[.]")[5];
			Map<String, Object> mapSon = (Map<String, Object>) mapFather.get(map_i);
			// 如果父级中不存在当前的KEY 就new一个子级
			if (mapSon == null) {
				mapSon = new HashMap<>();
			}
			// 获取参数名称为key值
			mapSon.put(name.substring(name.lastIndexOf(".") + 1, name.length()), value);
			mapFather.put(map_i, mapSon);
		}

		for (String s : mapFather.keySet()) {
			LANDeviceHostInfo land = new LANDeviceHostInfo();
			Map<String, Object> getMapSon = (Map<String, Object>) mapFather.get(s);
			String hostName = getMapSon.get("HostName") == "" ? "未知的主机名" : (String) getMapSon.get("HostName");
			land.setHostName(hostName);
			land.setiPAddress((String) getMapSon.get("IPAddress"));
			land.setMACAddress((String) getMapSon.get("MACAddress"));
			list.add(land);
		}

		return list;
	}

	/**
	 * 获取LAN口状态信息
	 *
	 * @param resultLan
	 * @return
	 */
	private Map<String, Object> getLanValue(Map<String, Object> resultLan) {
		Map<String, Object> retrenMap = new HashMap<>();
		for (Map.Entry<String, Object> entry : resultLan.entrySet()) {
			String name = entry.getKey();
			if (StringUtils.isNotBlank(name)) {
				if (name.lastIndexOf(".") > -1) {
					String name1 = name.substring(0, name.lastIndexOf("."));
					String name2 = name1.substring(name1.lastIndexOf(".") + 1, name1.length());
					String lanid = "lan" + name2;
					String value = null == entry.getValue() ? "" : entry.getValue().toString();
					retrenMap.put(lanid, value);
				}
			}
		}
		return retrenMap;
	}

	/***
	 * 根据正则表达式提取名称
	 *
	 * @param result
	 * @return
	 */
	private Map<String, List<String>> getNameToReg(Map<String, Object> result) {
		Map<String, List<String>> retrenMap = new HashMap<>();
		List<String> lanList = new ArrayList<>();
		// List<String> wifiList = new ArrayList<>();
		for (Map.Entry<String, Object> entry : result.entrySet()) {
			String name = entry.getKey();
			String regExLan = "[.][1-9]+[.]LANEthernetInterfaceConfig[.][1-9]+[.]Status";
			Pattern patternLan = Pattern.compile(regExLan);
			Matcher matchLan = patternLan.matcher(name);
			// LAN口状态数
			if (matchLan.find()) {
				lanList.add(name);
				continue;
			}
			// // WiFi连接数
			// String regExWifi = "Host[.][1-9]+[.]IPAddress|Host[.][1-9]+[.]AddressSource|Host[.][1-9]+[.]MACAddress|Host[.][1-9]+[.]HostName";
			// Pattern patternWifi = Pattern.compile(regExWifi);
			// Matcher matchWifi = patternWifi.matcher(name);
			// if (matchWifi.find()) wifiList.add(name);
		}
		retrenMap.put("lanList", lanList);
		// retrenMap.put("wifiList", wifiList);
		return retrenMap;
	}

}
