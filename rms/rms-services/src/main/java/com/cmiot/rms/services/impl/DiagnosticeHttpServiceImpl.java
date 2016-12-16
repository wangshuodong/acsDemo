package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import com.cmiot.rms.services.DiagnosticeHttpService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.LogManagerService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.DiagnosticsUtil;

public class DiagnosticeHttpServiceImpl implements DiagnosticeHttpService {

	private static Logger log = LoggerFactory.getLogger(DiagnosticeHttpServiceImpl.class);
	
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
	public Map<String, Object> httpDiagnostics(Map<String, Object> parameter) {
		long bt = System.currentTimeMillis();
		String logname = "HTTP";
		parameter.put("loguuid", UniqueUtil.uuid());
		log.info("LogId:{}" + logname + "获取仿真接口请求参数{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		String key = null;// Redis锁
		try {
			long cpst = System.currentTimeMillis();
			DiagnosticsUtil.getPropertiesVlanidMark(parameter, vlanidmark, log, logname);// 临时添加从此方法中获取vlanidmark
			String[] parmlist = { "gatewayId", "userName", "roleName", "downloadURL", "dscp", "ethernetPriority", "vlanidmark" };
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
										DiagnosticsUtil.getDiagnosticsResult(gi, getNodeList(), rm, logname, instructionMethodService, log, parameter.get("loguuid") + "", 7, diagnoseThresholdValueMapper);
										// //--------暂时保留,后续业务使用
										// long wt = System.currentTimeMillis();
										// int countFailureTime = 0;// 记录失败次数
										// while (true) {
										// String[] rst = DiagnosticsUtil.getDiagnosticsStatus(gi, "Completed", ConstantDiagnose.HTTP_DIAGNOSTICSSTATE, instructionMethodService, log, logname);
										// if (Boolean.parseBoolean(rst[0])) {
										// DiagnosticsUtil.getDiagnosticsResult(gi, getNodeList(), rm, logname, instructionMethodService, log);
										// break;
										// } else {
										// if ("1".equals(rst[1])) {
										// DiagnosticsUtil.temporaryObjectLock(waitTime, gi, log, logname);// 临时对象锁,为等待8 DIAGNOSTICS COMPLETE状态唤醒
										// // 当失败次数大于限定次数再获取一次网关结果作为返回结果
										// if (countFailureTime >= (null != failureMaxTime && failureMaxTime > 0 ? failureMaxTime : 1)) {
										// // DiagnosticsUtil.getDiagnosticsResult(gi, getNodeList(), rm, logname, instructionMethodService, log);
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
										// //------------
										DiagnosticsUtil.saveDiagnoseLog(parameter, gi, 10, diagnoseLogDao);// 保存诊断日志(报表)
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
		DiagnosticsUtil.saveOperationDiagnoseLog(parameter, rm, "HTTP下载仿真", logManagerService);
		log.info("LogId:{}" + logname + "业务诊断返回的结果集:{}总消耗时间:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - bt));
		return rm;

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
		// 诊断状态
		ParameterValueStruct httpdspvs = new ParameterValueStruct();
		httpdspvs.setName(ConstantDiagnose.HTTP_DIAGNOSTICSSTATE);
		httpdspvs.setValue("Requested");
		httpdspvs.setReadWrite(true);
		httpdspvs.setValueType("string");
		list.add(httpdspvs);
		// WAN连接（TR069节点全路径）针对桥接和路由WAN均适用
		ParameterValueStruct wanInterface = new ParameterValueStruct();
		wanInterface.setName(ConstantDiagnose.HTTP_INTERFACE);
		// wanInterface.setValue(gi.getGatewayExternalIPaddress().trim());
		wanInterface.setValue(path);
		wanInterface.setReadWrite(true);
		wanInterface.setValueType("string");
		list.add(wanInterface);
		// 用于下载的URL
		ParameterValueStruct downloadURL = new ParameterValueStruct();
		downloadURL.setName(ConstantDiagnose.HTTP_DOWNLOADURL);
		downloadURL.setValue(parameter.get("downloadURL"));
		downloadURL.setReadWrite(true);
		downloadURL.setValueType("string");
		list.add(downloadURL);
		// 用来测试包的DSCP值
		ParameterValueStruct dscp = new ParameterValueStruct();
		dscp.setName(ConstantDiagnose.HTTP_DSCP);
		dscp.setValue(parameter.get("dscp"));
		dscp.setReadWrite(true);
		dscp.setValueType("unsignedInt");
		list.add(dscp);
		// 用来测试包的优先级
		ParameterValueStruct ethernetPriority = new ParameterValueStruct();
		ethernetPriority.setName(ConstantDiagnose.HTTP_ETHERNETPRIORITY);
		ethernetPriority.setValue(parameter.get("ethernetPriority"));
		ethernetPriority.setReadWrite(true);
		ethernetPriority.setValueType("unsignedInt");
		list.add(ethernetPriority);
		return list;
	}

	/**
	 * 得到获取网关DHCP仿真节点
	 * 
	 * @return
	 */
	private List<String> getNodeList() {
		List<String> list = new ArrayList<>();
		list.add(ConstantDiagnose.HTTP_DIAGNOSTICSSTATE);// 诊断状态
		list.add(ConstantDiagnose.HTTP_ROMTIME);// 请求收到时间
		list.add(ConstantDiagnose.HTTP_BOMTIME);// 传输开始时间
		list.add(ConstantDiagnose.HTTP_EOMTIME);// 传输结束时间
		list.add(ConstantDiagnose.HTTP_TESTBYTESRECEIVED);// 接收字节数，包括控制头
		list.add(ConstantDiagnose.HTTP_TOTALBYTESRECEIVED);// 接收字节数
		list.add(ConstantDiagnose.HTTP_TCPOPENREQUESTTIME);// TCP请求时间
		list.add(ConstantDiagnose.HTTP_TCPOPENRESPONSETIME);// TCP响应时间
		return list;
	}
}