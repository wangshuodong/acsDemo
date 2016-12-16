package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.cmiot.rms.services.DiagnosticeVoipService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.LogManagerService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.DiagnosticsUtil;

public class DiagnosticeVoipServiceImpl implements DiagnosticeVoipService {

	private static Logger log = LoggerFactory.getLogger(DiagnosticeVoipServiceImpl.class);
	
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
	@Value("${diagnostics.voip.vlanidmark}")
	String vlanidmark;

	@Value("${dubbo.provider.port}")
	int providerPort;

	@Override
	public Map<String, Object> voipDiagnostics(Map<String, Object> parameter) {
		String key = null;// Redis锁
		String logname = "VoIP";
		long bt = System.currentTimeMillis();
		parameter.put("loguuid", UniqueUtil.uuid());
		Map<String, Object> rm = new HashMap<>();
		log.info("LogId:{}" + logname + "获取接口请求参数{}", parameter.get("loguuid"), parameter);
		try {
			DiagnosticsUtil.getPropertiesVlanidMark(parameter, vlanidmark, log, logname);// 临时添加从此方法中获取vlanidmark
			String[] parmlist = { "gatewayId", "userName", "roleName", "testType", "vlanidmark" };
			long cpst = System.currentTimeMillis();
			if (DiagnosticsUtil.checkParm(parameter, rm, Arrays.asList(parmlist))) {
				if (checkCalledNumber(parameter, rm)) {
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
								if (checkVoIPStatus(parameter, rm, gi, instructionMethodService, log, logname)) {// 验证用户VoIP状态
									long voipNt = System.currentTimeMillis();
									Map<String, Object> result = instructionMethodService.getParameterNames(gi.getGatewayMacaddress(), ConstantDiagnose.VOIP_ROOT, false);// 获取所有节点数据
									log.info("LogId:{}" + logname + "查询VOIP根节点返回的数据:{}消耗时间:{}", parameter.get("loguuid"), result, (System.currentTimeMillis() - voipNt));
									if (null != result && result.size() > 0) {
										Pattern pattern = Pattern.compile("InternetGatewayDevice.Services.VoiceService.[\\d]+.PhyInterface.[\\d]+.Tests.X_CMCC_SimulateTest.(TestType|CalledNumber|DailDTMFConfirmEnable|DailDTMFConfirmNumber|DailDTMFConfirmResult)");
										List<String> list = DiagnosticsUtil.getNamesList(result, pattern);
										log.info("LogId:{}" + logname + "根据正则规则获取节点数据:{}", parameter.get("loguuid"), list);
										if (null != list && list.size() > 0) {
											String[] node = getOnlyNode(list);
											if (null != node && Boolean.parseBoolean(node[0])) {
												//// ------因接口规范当中无WAN连接状态设置所以下面业务不处理
												// Pattern patternServiceList = Pattern.compile("InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.(WANIPConnection|WANPPPConnection).[\\d]+.X_CMCC_ServiceList");
												// // 网关上获取Wan连接的状态与路径
												// Map<String, Object> rb = DiagnosticsUtil.getWanConnectStatusAndPath(parameter, gi, instructionMethodService, patternServiceList, log, logname, "VOIP");
												// log.info("LogId:{}" + logname + "网关上获取Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
												// if (null != rb && !"0".equals(rb.get("status") + "")) {
												// // 网关未获取INTERNET的WANl连接,调用创建WAN连接方法
												// if ("1".equals(rb.get("status") + "")) {
												// rb = DiagnosticsUtil.createDHCPWanConnect(parameter, gi, instructionMethodService, log, logname, "VOIP", rb.get("path") + "");
												// log.info("LogId:{}" + logname + "网关上创建Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
												// }
												// if (null != rb && "2".equals(rb.get("status") + "")) {
												//保存当前诊断调用方地址，供ACS回调使用
												String providerUrl = "dubbo://" + RpcContext.getContext().getLocalHost() + ":" + providerPort + "/" + AcsInterfaceService.class.getName();
												redisClientTemplate.set(gi.getGatewayFactoryCode() + Constant.SEPARATOR + gi.getGatewaySerialnumber() + Constant.SEPARATOR + "diagnose", providerUrl);

												if (DiagnosticsUtil.settingPingParameterAndRequestGateway(parameter, gi, instructionMethodService, this.setOnlyNodeParameter(parameter, node[1]))) {
													DiagnosticsUtil.temporaryObjectLock(waitTime, gi, log, logname, parameter.get("loguuid") + "");// 临时对象锁,为等待8 DIAGNOSTICS COMPLETE状态唤醒
													DiagnosticsUtil.getDiagnosticsResult(gi, getOnlyNodeList(node[1]), rm, logname, instructionMethodService, log, parameter.get("loguuid") + "", 6, diagnoseThresholdValueMapper);// 获取网关诊断结果
													// //-------暂时保留,后续业务使用
													// int countFailureTime = 0;// 记录失败次数
													// while (true) {
													// String[] rst = DiagnosticsUtil.getDiagnosticsStatus(gi, "Testend", node[1] + "Status", instructionMethodService, log, logname);// 获取诊断状态
													// if (Boolean.parseBoolean(rst[0])) {
													// DiagnosticsUtil.getDiagnosticsResult(gi, getOnlyNodeList(node[1]), rm, logname, instructionMethodService, log);// 获取网关诊断结果
													// break;
													// } else {
													// if ("1".equals(rst[1])) {
													// DiagnosticsUtil.temporaryObjectLock(waitTime, gi, log, logname);// 临时对象锁,为等待8 DIAGNOSTICS COMPLETE状态唤醒
													// // 当失败次数大于限定次数再获取一次网关结果作为返回结果
													// if (countFailureTime >= (null != failureMaxTime && failureMaxTime > 0 ? failureMaxTime : 1)) {
													// // DiagnosticsUtil.getDiagnosticsResult(gi, getOnlyNodeList(node[1]), rm, logname, instructionMethodService, log);
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
													// //---------
													DiagnosticsUtil.saveDiagnoseLog(parameter, gi, 7, diagnoseLogDao);// 保存诊断日志(报表)
													//// ------因接口规范当中无WAN连接状态设置所以下面业务不处理
													// DiagnosticsUtil.deleteCreateWanConnect(parameter, gi, instructionMethodService, log, logname, rb.get("path") + "", (boolean) rb.get("delFlag"));// 删除创建的WAN连接
												} else {
													DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关诊断参数设置失败", "");
												}
												//// ------因接口规范当中无WAN连接状态设置所以下面业务不处理
												// } else {
												// DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关不存在IPWAN连接,并且创建IPWAN连接失败", "");
												// }
												// } else {
												// DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取网关WAN连接信息失败", "");
												// }
											} else {
												DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关返回多个不同节点地址", "");
											}
										} else {
											DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到网关可设值节点信息", "");
										}
									} else {
										DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到网关节点信息", "");
									}
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
			}
		} catch (Exception e) {
			if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
			DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_ERROR.code(), logname + "诊断仿真接口异常", "");
			log.error("LogId:{}" + logname + " Diagnostics exception " + e.getMessage(), parameter.get("loguuid"), e);
		}
		DiagnosticsUtil.saveOperationDiagnoseLog(parameter, rm, "DHCP仿真", logManagerService);
		log.info("LogId:{}" + logname + "业务诊断返回的结果集:{}总消耗时间:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - bt));
		return rm;
	}

	/**
	 * 验证用户VoIP状态
	 * 
	 * @param parameter
	 *            参数集
	 * @param rm
	 *            返回数据对象
	 * @param gi
	 *            网关对像
	 * @param ims
	 *            请求ACS实现类
	 * @param log
	 *            日志对象
	 * @param logName
	 *            日志名称
	 * @return
	 */
	private Boolean checkVoIPStatus(Map<String, Object> parameter, Map<String, Object> rm, GatewayInfo gi, InstructionMethodService ims, Logger log, String logName) {
		boolean istrue = false;
		switch (DiagnosticsUtil.queryVoIPStatus(parameter, gi, ims, log, logName).intValue()) {
		case 0:
			DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关查询VoIP状态失败", "");
			break;
		case 1:
			istrue = true;
			break;
		case 2:
			DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "用户启用但未注册VoIP", "");
			break;
		case 3:
			DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "用户注册VoIP但未启用", "");
			break;
		case 4:
			DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "用户未启用并且未注册VoIP", "");
			break;
		default:
			DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "网关查询VoIP状态失败", "");
			break;
		}
		return istrue;
	}

	/**
	 * 验证仿真类型
	 * 
	 * @param parameter
	 *            参数集
	 * @param rm
	 *            返回对象
	 * @return
	 */
	private Boolean checkCalledNumber(Map<String, Object> parameter, Map<String, Object> rm) {
		boolean it = true;
		if ("Caller".equals(parameter.get("testType") + "".trim())) {
			if (StringUtils.isBlank(parameter.get("calledNumber") + "")) {
				it = false;
				DiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "仿真测试类型(testType)为主叫仿(Caller)时,主叫仿真时的被叫号码(calledNumber)为必选项", "");
			}
		}
		return it;
	}

	/**
	 * 获取只有一个节点路径下的诊断参数地址
	 * 
	 * @param node
	 *            节点
	 * @return
	 */
	private List<String> getOnlyNodeList(String node) {
		List<String> list = new ArrayList<>();
		list.add(node + "DailDTMFConfirmResult");// 拨号确认结果
		list.add(node + "Status");// 仿真当前状态
		list.add(node + "Conclusion");// 仿真结果
		list.add(node + "CallerFailReason");// 主叫仿真失败原因
		list.add(node + "CalledFailReason");// 被叫仿真失败原因
		list.add(node + "FailedResponseCode");// 仿真失败时接收到的错误码0表示无错误码
		return list;
	}

	/**
	 * 获取唯一的节点路径
	 * 
	 * @param nodelist
	 *            返回的节点集
	 * @return
	 */
	private String[] getOnlyNode(List<String> nodelist) {
		String[] rb = { "false", "0" };
		Iterator<String> it = nodelist.iterator();
		HashSet<String> nodes = new HashSet<>();
		String node = null;
		while (it.hasNext()) {
			node = it.next();
			if (StringUtils.isNotBlank(node)) {
				node = node.substring(0, node.lastIndexOf(".") + 1);
				nodes.add(node);
			}
		}
		if (null != nodes && nodes.size() == 1) {
			rb[0] = "true";
			rb[1] = node;
		} else {
			rb[1] = nodes.size() + "";
		}
		return rb;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<ParameterValueStruct> setOnlyNodeParameter(Map<String, Object> parameter, String node) {
		List<ParameterValueStruct> list = new ArrayList<>();
		String tsn = node.substring(0, (node.length() - "X_CMCC_SimulateTest.".length()));
		// 测试状态指示和设置，以下值之一：None： 无测试Requested ：测试启动中Complete：测试结束 写操作只能为Requested。
		ParameterValueStruct testState = new ParameterValueStruct();
		testState.setName(tsn + "TestState");
		testState.setValue("Requested");
		testState.setReadWrite(true);
		testState.setValueType("string");
		list.add(testState);
		// 测试选择，以下值之一：PhoneConnectivityTest X_CMCC_SimulateTest仿真呼叫
		ParameterValueStruct testSelector = new ParameterValueStruct();
		testSelector.setName(tsn + "TestSelector");
		testSelector.setValue("X_CMCC_SimulateTest");
		testSelector.setReadWrite(true);
		testSelector.setValueType("string");
		list.add(testSelector);
		// 仿真测试类型，以下值之一：Caller 主叫仿真;Called 被叫仿真;None 取消仿真
		ParameterValueStruct testType = new ParameterValueStruct();
		testType.setName(node + "TestType");
		testType.setValue(parameter.get("testType"));
		testType.setReadWrite(true);
		testType.setValueType("string");
		list.add(testType);
		// 主叫仿真时的被叫号码
		ParameterValueStruct calledNumber = new ParameterValueStruct();
		calledNumber.setName(node + "CalledNumber");
		calledNumber.setValue(parameter.get("calledNumber"));
		calledNumber.setReadWrite(true);
		calledNumber.setValueType("string");
		list.add(calledNumber);
		// 拨号确认是否开启,是指接通主叫或被叫仿真后，另一侧的测试人员可以通过拨号确认其听到测试提示音清晰与否来确认通话是否OK。
		if (StringUtils.isNotBlank(parameter.get("dailDTMFConfirmEnable") + "")) {
			ParameterValueStruct dailDTMFConfirmEnable = new ParameterValueStruct();
			dailDTMFConfirmEnable.setName(node + "DailDTMFConfirmEnable");
			dailDTMFConfirmEnable.setValue(parameter.get("dailDTMFConfirmEnable"));
			dailDTMFConfirmEnable.setReadWrite(true);
			dailDTMFConfirmEnable.setValueType("boolean");
			list.add(dailDTMFConfirmEnable);
		}
		// 拨号确认的号码只能是*#0-9的字符,默认值#
		if (StringUtils.isNotBlank(parameter.get("dailDTMFConfirmNumber") + "")) {
			ParameterValueStruct dailDTMFConfirmNumber = new ParameterValueStruct();
			dailDTMFConfirmNumber.setName(node + "DailDTMFConfirmNumber");
			dailDTMFConfirmNumber.setValue(parameter.get("dailDTMFConfirmNumber"));
			dailDTMFConfirmNumber.setReadWrite(true);
			dailDTMFConfirmNumber.setValueType("string");
			list.add(dailDTMFConfirmNumber);
		}
		return list;
	}

	/**
	 * 给所有节点设置值
	 * 
	 * @param nodelist
	 *            返回的节点集
	 * @param parameter
	 *            参数集
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<ParameterValueStruct> settingAllNodePingParameterAndRequestGateway(List<String> nodelist, Map<String, Object> parameter) {
		List<ParameterValueStruct> list = new ArrayList<>();
		Iterator<String> it = nodelist.iterator();
		while (it.hasNext()) {
			String node = it.next();
			if (StringUtils.isNotBlank(node)) {
				String name = node.substring(node.lastIndexOf(".") + 1, node.length());
				if (StringUtils.isNotBlank(name)) {
					switch (name.trim()) {
					case "TestType":
						ParameterValueStruct testType = new ParameterValueStruct();
						testType.setName(node);
						testType.setValue(parameter.get("testType"));
						testType.setReadWrite(true);
						testType.setValueType("string");
						list.add(testType);
						break;
					case "CalledNumber":
						ParameterValueStruct calledNumber = new ParameterValueStruct();
						calledNumber.setName(node);
						calledNumber.setValue(parameter.get("calledNumber"));
						calledNumber.setReadWrite(true);
						calledNumber.setValueType("string");
						list.add(calledNumber);
						break;
					case "DailDTMFConfirmEnable":
						if (StringUtils.isNotBlank(parameter.get("dailDTMFConfirmEnable") + "")) {
							ParameterValueStruct dailDTMFConfirmEnable = new ParameterValueStruct();
							dailDTMFConfirmEnable.setName(node);
							dailDTMFConfirmEnable.setValue(parameter.get("dailDTMFConfirmEnable"));
							dailDTMFConfirmEnable.setReadWrite(true);
							dailDTMFConfirmEnable.setValueType("boolean");
							list.add(dailDTMFConfirmEnable);
						}
						break;
					case "DailDTMFConfirmNumber":
						if (StringUtils.isNotBlank(parameter.get("dailDTMFConfirmNumber") + "")) {
							ParameterValueStruct dailDTMFConfirmNumber = new ParameterValueStruct();
							dailDTMFConfirmNumber.setName(node);
							dailDTMFConfirmNumber.setValue(parameter.get("dailDTMFConfirmNumber"));
							dailDTMFConfirmNumber.setReadWrite(true);
							dailDTMFConfirmNumber.setValueType("string");
							list.add(dailDTMFConfirmNumber);
						}
						break;
					default:
						break;
					}
				}
			}
		}
		return list;
	}

}
