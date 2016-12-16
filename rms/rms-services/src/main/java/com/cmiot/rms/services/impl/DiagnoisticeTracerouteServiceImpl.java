package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.dubbo.rpc.RpcContext;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.services.AcsInterfaceService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.enums.RebootEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.DiagnoseLogMapper;
import com.cmiot.rms.dao.mapper.DiagnoseThresholdValueMapper;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.DiagnoisticeTracerouteService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.LogManagerService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.DiagnosticsUtil;

public class DiagnoisticeTracerouteServiceImpl implements DiagnoisticeTracerouteService {

	private static Logger log = LoggerFactory.getLogger(DiagnoisticeTracerouteServiceImpl.class);
	
	@Autowired
	DiagnoseThresholdValueMapper diagnoseThresholdValueMapper;

	@Autowired
	InstructionMethodService instructionMethodService;

	@Autowired
	private RedisClientTemplate redisClientTemplate;

	@Autowired
	GatewayInfoService gatewayInfoService;

	@Autowired
	LogManagerService logManagerService;

	@Autowired
	DiagnoseLogMapper diagnoseLogDao;

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
	public Map<String, Object> tracerouteDiagnostics(Map<String, Object> parameter) {
		long bt = System.currentTimeMillis();
		String logname = "Traceroute";
		parameter.put("loguuid", UniqueUtil.uuid());
		log.info("LogId:{}" + logname + "获取仿真接口请求参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		String key = null;// Redis锁
		try {
			long cpst = System.currentTimeMillis();
			DiagnosticsUtil.getPropertiesVlanidMark(parameter, vlanidmark, log, logname);// 临时添加从此方法中获取vlanidmark
			String[] parmlist = { "gatewayId", "userName", "roleName", "mode", "host", "numberOfTries", "timeout", "dataBlockSize", "dscp", "maxHopCount", "vlanidmark" };
			if (DiagnosticsUtil.checkParm(parameter, rm, Arrays.asList(parmlist))) {
				log.info("LogId:{}" + logname + "验证参数消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - cpst));
				long qgist = System.currentTimeMillis();
				GatewayInfo gi = gatewayInfoService.selectGatewayInfo(new GatewayInfo(parameter.get("gatewayId") + ""));// 通过网关ID获取网关信息
				log.info("LogId:{}" + logname + "以网关ID为条件查询网关信息:{}消耗时间:{}", parameter.get("loguuid"), gi, (System.currentTimeMillis() - qgist));
				if (null != gi) {
					key = gi.getGatewayMacaddress() + "_ping";
					// nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在时才进行set，如果取XX，则只有当key已经存在时才进行set;expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
					String ls = redisClientTemplate.set(key, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
					if (StringUtils.isNotBlank(ls)) {
						if (null == RequestCache.get("diagnostics_" + gi.getGatewayMacaddress())) {
							long spt = System.currentTimeMillis();
							Pattern pattern = Pattern.compile("InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.(WANIPConnection|WANPPPConnection).[\\d]+.(X_CMCC_ServiceList|ConnectionType|AddressingType|Enable|ConnectionStatus)");
							// 网关上获取Wan连接的状态与路径
							Map<String, Object> rb = DiagnosticsUtil.getWanConnectStatusAndPath(parameter, gi, instructionMethodService, pattern, log, logname, "INTERNET", new String[] { "PPPoE_Routed", "IP_Routed" }, null);
							log.info("LogId:{}" + logname + "网关上获取Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
							if (null != rb && !"0".equals(rb.get("status") + "")) {
								// 网关未获取INTERNET的WANl连接,调用创建WAN连接方法
								if ("1".equals(rb.get("status") + "")) {
									rb = DiagnosticsUtil.createDHCPWanConnect(parameter, gi, instructionMethodService, log, logname, "INTERNET", rb.get("path") + "");
									log.info("LogId:{}" + logname + "网关上创建Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
									if(null != rb && "2".equals(rb.get("status") + "")){
										rb = DiagnosticsUtil.getWanConnectionStatus(parameter, rb.get("path") + "", 10, gi, instructionMethodService, log, logname);
										log.info("LogId:{}" + logname + "休眠后返回数据:{}", parameter.get("loguuid"), rb);
									}
								}
								if (null != rb && "2".equals(rb.get("status") + "")) {
									//保存当前诊断调用方地址，供ACS回调使用
									String providerUrl = "dubbo://" + RpcContext.getContext().getLocalHost() + ":" + providerPort + "/" + AcsInterfaceService.class.getName();
									redisClientTemplate.set(gi.getGatewayFactoryCode() + Constant.SEPARATOR + gi.getGatewaySerialnumber() + Constant.SEPARATOR + "diagnose", providerUrl);

									if (DiagnosticsUtil.settingPingParameterAndRequestGateway(parameter, gi, instructionMethodService, this.setParameter(parameter, gi, rb.get("path") + ""))) {
										DiagnosticsUtil.temporaryObjectLock(waitTime, gi, log, logname, parameter.get("loguuid") + "");// 临时对象锁,为等待8 DIAGNOSTICS COMPLETE状态唤醒
										this.getDiagnosticsResult(gi, getNodeList(), rm, logname, instructionMethodService, log, parameter.get("loguuid") + "");
										// //---------暂时保留,后续业务使用
										// long wt = System.currentTimeMillis();
										// int countFailureTime = 0;// 记录失败次数
										// while (true) {
										// String[] rst = DiagnosticsUtil.getDiagnosticsStatus(gi, "Complete", ConstantDiagnose.TRACEROUTE_DIAGNOSTICSSTATE, instructionMethodService, log, logname);
										// if (Boolean.parseBoolean(rst[0])) {
										// this.getDiagnosticsResult(gi, getNodeList(), rm, logname, instructionMethodService, log);
										// break;
										// } else {
										// if ("1".equals(rst[1])) {
										// DiagnosticsUtil.temporaryObjectLock(waitTime, gi, log, logname);// 临时对象锁,为等待8 DIAGNOSTICS COMPLETE状态唤醒
										// // 当失败次数大于限定次数再获取一次网关结果作为返回结果
										// if (countFailureTime >= (null != failureMaxTime && failureMaxTime > 0 ? failureMaxTime : 1)) {
										// // this.getDiagnosticsResult(gi, getNodeList(), rm, logname, instructionMethodService, log);
										// log.info(logname + "诊断后获取诊断状态已经超出最大失败次数:" + failureMaxTime);
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
										// log.info("获取状态与获取网关诊断值总消耗时间:" + (System.currentTimeMillis() - wt));
										// //----------
										DiagnosticsUtil.saveDiagnoseLog(parameter, gi, 8, diagnoseLogDao);// 保存诊断日志(报表)
									} else {
										DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关诊断参数设置失败", "");
									}
								} else {
									DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关不存在IPWAN连接,并且创建IPWAN连接失败", "");
								}
								DiagnosticsUtil.deleteCreateWanConnect(parameter, gi, instructionMethodService, log, logname, rb.get("path") + "", (boolean) rb.get("delFlag"));// 删除创建的WAN连接
							} else {
								DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取网关WAN连接信息失败", "");
							}
							log.info("LogId:{}" + logname + "网关诊断设值,获取诊断状态与获取诊断结果总消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - spt));
						} else {
							log.info("LogId:{}" + logname + "诊断指令已发送,为防止用户重复提交,添加临时对象锁，等待指令异步返回中", parameter.get("loguuid"));
							DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关正在诊断中,请无重复提交", "");
						}
						if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
					} else {
						DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "当前网关正在诊断,请稍后再试", "");
					}
				} else {
					DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到网关信息", "");
				}
			}
		} catch (Exception e) {
			if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
			DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_ERROR.code(), logname + "诊断仿真接口异常", "");
			log.error("LogId:{}" + logname + " Diagnostics exception " + e.getMessage(), parameter.get("loguuid"), e);
		}
		DiagnosticsUtil.saveOperationDiagnoseLog(parameter, rm, "Traceroute诊断", logManagerService);
		log.info("LogId:{}" + logname + "业务诊断返回的结果集:{}总消耗时间:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - bt));
		return rm;
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
	 */
	private void getDiagnosticsResult(GatewayInfo gi, List<String> node, Map<String, Object> rm, String logName, InstructionMethodService ims, Logger log, String logid) {
		long st = System.currentTimeMillis();
		Map<String, Object> result = ims.getParameterValues(gi.getGatewayMacaddress(), node);
		log.info("LogId:{}" + logName + "获取网关诊断返回结果集:{}消耗时间:{}", logid, result, (System.currentTimeMillis() - st));
		if (null != result && result.size() > 0) {
			long fst = System.currentTimeMillis();
			result = DiagnosticsUtil.getResultMapDate(result);// 获取返回封装数据
			result = DiagnosticsUtil.getDiagnoseThresholdFlag(result, 3, 1, diagnoseThresholdValueMapper, logid, log, logName);//获取诊断值的阈值状态
			result.put("tdl", getTracerouteDiagnosticeList(gi, logName, logid));
			DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), result);
			log.info("LogId:{}" + logName + "诊断返回结果集:{}封装消耗时间:{}", logid, result, (System.currentTimeMillis() - fst));
		} else {
			DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取网关诊断信息失败", "");
		}
	}

	/**
	 * 获取Traceroute诊断记录列表
	 * 
	 * @param gi
	 *            网关对象
	 * @param logName
	 *            日志名称
	 * @param logid
	 *            日志ID
	 *            返回列表集合对象
	 */
	private List<Map<String, Object>> getTracerouteDiagnosticeList(GatewayInfo gi, String logName, String logid) {
		Map<String, Object> result = instructionMethodService.getParameterNames(gi.getGatewayMacaddress(), ConstantDiagnose.TRACEROUTE_OBJECT, false);
		log.info("LogId:{}" + logName + "获取Traceroute诊断记录列表:{}", logid, result);
		List<Map<String, Object>> tdl = new ArrayList<Map<String, Object>>();
		if (null != result && result.size() > 0) {
			HashSet<String> nameList = getNamesList(result, Pattern.compile("InternetGatewayDevice.TraceRouteDiagnostics.RouteHops.[\\d]+.(HopHost|HopHostAddress|HopErrorCode|HopRTTimes)"));// 获取Traceroute诊断记录列表
			log.info("LogId:{}" + logName + "获取获取网关节点名集合:{}", logid, nameList);
			Iterator<String> it = nameList.iterator();
			while (it.hasNext()) {
				String node = it.next();
				if (StringUtils.isNotBlank(node)) {
					Map<String, Object> resultlist = instructionMethodService.getParameterValues(gi.getGatewayMacaddress(), this.getChildrenNodePath(node));
					log.info("LogId:{}" + logName + "获取Traceroute诊断记录列表:{}", logid, resultlist);
					if (null != resultlist && resultlist.size() > 0) {
						Map<String, Object> map = new HashMap<>();
						for (Map.Entry<String, Object> entry : resultlist.entrySet()) {
							String name = entry.getKey();
							String value = entry.getValue() + "";
							String resultName = name.substring(name.lastIndexOf(".") + 1, name.length());// 获取名称最后一个“.”后的参数为KEY值
							map.put(resultName, value);
						}
						tdl.add(map);
					}
				}
			}
		}
		return tdl;
	}

	/**
	 * <一句话功能简述>
	 * <功能详细描述>
	 * 
	 * @param node
	 * @return
	 */
	private List<String> getChildrenNodePath(String node) {
		List<String> list = new ArrayList<>();
		list.add(node + "HopHost");
		list.add(node + "HopHostAddress");
		list.add(node + "HopErrorCode");
		list.add(node + "HopRTTimes");
		return list;
	}

	/**
	 * 获取网关节点名集合
	 * 
	 * @param date
	 *            数据源
	 * @param patternService
	 *            节点名的规则
	 * @return
	 */
	private HashSet<String> getNamesList(Map<String, Object> date, Pattern patternService) {
		HashSet<String> nodes = new HashSet<>();// 去重复
		Set<String> keys = date.keySet();
		for (String key : keys) {
			Matcher matchService = patternService.matcher(key);
			if (matchService.matches()) {
				nodes.add(key.substring(0, key.lastIndexOf(".") + 1));
			}
		}
		return nodes;
	}

	/**
	 * 在不同的节点下组装设置参数
	 * 
	 * @param parameter
	 *            参数集
	 * @param gi
	 *            网关对象
	 * @param path
	 *            WAN连接承载的业务路径
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<ParameterValueStruct> setParameter(Map<String, Object> parameter, GatewayInfo gi, String path) {
		List<ParameterValueStruct> list = new ArrayList<>();
		// Traceroute诊断采用的协议类型：UDP;ICMP
		ParameterValueStruct mode = new ParameterValueStruct();
		mode.setName(ConstantDiagnose.TRACEROUTE_MODE);
		mode.setValue(parameter.get("mode"));
		mode.setReadWrite(true);
		mode.setValueType("string");
		list.add(mode);
		// 诊断状态
		ParameterValueStruct traceroutedspvs = new ParameterValueStruct();
		traceroutedspvs.setName(ConstantDiagnose.TRACEROUTE_DIAGNOSTICSSTATE);
		traceroutedspvs.setValue("Requested");
		traceroutedspvs.setReadWrite(true);
		traceroutedspvs.setValueType("string");
		list.add(traceroutedspvs);
		// WAN连接（TR069节点全路径）针对桥接和路由WAN均适用
		ParameterValueStruct wanInterface = new ParameterValueStruct();
		wanInterface.setName(ConstantDiagnose.TRACEROUTE_INTERFACE);
		// wanInterface.setValue(gi.getGatewayExternalIPaddress().trim());
		wanInterface.setValue(path);
		wanInterface.setReadWrite(true);
		wanInterface.setValueType("string");
		list.add(wanInterface);
		// Traceroute测试的主机名或主机地址
		ParameterValueStruct host = new ParameterValueStruct();
		host.setName(ConstantDiagnose.TRACEROUTE_HOST);
		host.setValue(parameter.get("host"));
		host.setReadWrite(true);
		host.setValueType("string");
		list.add(host);
		// 每跳重复次数
		ParameterValueStruct numberOfTries = new ParameterValueStruct();
		numberOfTries.setName(ConstantDiagnose.TRACEROUTE_NUMBEROFTRIES);
		numberOfTries.setValue(parameter.get("numberOfTries"));
		numberOfTries.setReadWrite(true);
		numberOfTries.setValueType("unsignedInt");
		list.add(numberOfTries);
		// 诊断超时时间(单位：ms)
		ParameterValueStruct timeout = new ParameterValueStruct();
		timeout.setName(ConstantDiagnose.TRACEROUTE_TIMEOUT);
		timeout.setValue(parameter.get("timeout"));
		timeout.setReadWrite(true);
		timeout.setValueType("unsignedInt");
		list.add(timeout);
		// 每个Traceroute包发送的数据块大小（单位：字节）
		ParameterValueStruct dataBlockSize = new ParameterValueStruct();
		dataBlockSize.setName(ConstantDiagnose.TRACEROUTE_DATABLOCKSIZE);
		dataBlockSize.setValue(parameter.get("dataBlockSize"));
		dataBlockSize.setReadWrite(true);
		dataBlockSize.setValueType("unsignedInt");
		list.add(dataBlockSize);
		// 用来测试包的DSCP值
		ParameterValueStruct dscp = new ParameterValueStruct();
		dscp.setName(ConstantDiagnose.TRACEROUTE_DSCP);
		dscp.setValue(parameter.get("dscp"));
		dscp.setReadWrite(true);
		dscp.setValueType("unsignedInt");
		list.add(dscp);
		// 最大跳数
		ParameterValueStruct maxHopCount = new ParameterValueStruct();
		maxHopCount.setName(ConstantDiagnose.TRACEROUTE_MAXHOPCOUNT);
		maxHopCount.setValue(parameter.get("maxHopCount"));
		maxHopCount.setReadWrite(true);
		maxHopCount.setValueType("unsignedInt");
		list.add(maxHopCount);
		return list;
	}

	/**
	 * 得到获取网关DHCP仿真节点
	 * 
	 * @return
	 */
	private List<String> getNodeList() {
		List<String> list = new ArrayList<>();
		list.add(ConstantDiagnose.TRACEROUTE_DIAGNOSTICSSTATE);// 诊断状态
		list.add(ConstantDiagnose.TRACEROUTE_RESPONSETIME);// 诊断响应时间(单位：ms)
		list.add(ConstantDiagnose.TRACEROUTE_HOPSNUMBEROFENTRIES);// 实际探测到的总跳数
		return list;
	}
}
