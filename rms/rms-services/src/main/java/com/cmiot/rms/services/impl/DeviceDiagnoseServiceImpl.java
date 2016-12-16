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
import com.cmiot.rms.services.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSON;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.constant.BoxConstantDiagnose;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.enums.RebootEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.enums.ServiceListEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.mapper.DiagnoseLogMapper;
import com.cmiot.rms.dao.mapper.DiagnoseThresholdValueMapper;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.boxManager.instruction.BoxInstructionMethodService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.BoxDiagnosticsUtil;
import com.cmiot.rms.services.util.DiagnosticsUtil;
import com.cmiot.rms.services.util.PbossDiagnosticsUtil;

/**
 * 省级数字家庭管理平台与网管系统接口规范
 * 设备测试接口
 * @author chuan
 *
 */
public class DeviceDiagnoseServiceImpl implements DeviceDiagnoseService {

	private static Logger logger = LoggerFactory.getLogger(DeviceDiagnoseServiceImpl.class);
	
	private static final String HGU_PING_RESULT_SUFFIX_KEY = "-hgu-ping-result";
	private static final String HGU_PING_LOCK_SUFFIX_KEY = "-hgu-ping-lock";
	private static final String HGU_PPPOE_RESULT_SUFFIX_KEY = "-hgu-pppoe-result";
	private static final String HGU_PPPOE_LOCK_SUFFIX_KEY = "-hgu-pppoe-lock";
	private static final String HGU_TRACEROUTE_RESULT_SUFFIX_KEY = "-hgu-traceroute-result";
	private static final String HGU_TRACEROUTE_LOCK_SUFFIX_KEY = "-hgu-traceroute-lock";
	private static final String STB_PING_RESULT_SUFFIX_KEY = "-stb-ping-result";
	private static final String STB_PING_LOCK_SUFFIX_KEY = "-stb-ping-lock";
	private static final String STB_TRACEROUTE_RESULT_SUFFIX_KEY = "-stb-traceroute-result";
	private static final String STB_TRACEROUTE_LOCK_SUFFIX_KEY = "-stb-traceroute-lock";
	
	@Autowired
	private GatewayInfoService gatewayInfoService;
	@Autowired
	private RedisClientTemplate redisClientTemplate;
	@Autowired
	private InstructionMethodService instructionMethodService;
	@Autowired
	private DiagnoseLogMapper diagnoseLogDao;
	@Autowired
	private DiagnoseThresholdValueMapper diagnoseThresholdValueMapper;
	@Autowired
	private LogManagerService logManagerService;
	@Autowired
	private BoxInfoMapper boxInfoMapper;
	@Autowired
	private BoxInstructionMethodService boxInstructionMethodService;
	
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
	public Map<String, Object> pingHguDiagnose(Map<String, Object> parameter) {
		logger.info("start invoke pingHguDiagnose:{}", parameter);
		Map<String,Object> retMap = commonResult(parameter);
		if(!validatePringParam(parameter)){
			retMap.put("Result", 1);
			return retMap;
		}
		//CPEID为OUI-SN
		String cpeId = parameter.get("CPEID").toString();
		String[] cpeIds = cpeId.split("-");
		if(cpeIds.length != 2){
			retMap.put("Result", 1);
			return retMap;
		}
		String serviceList = ServiceListEnum.getName(parameter.get("ServiceList").toString());
        if(StringUtils.isEmpty(serviceList)){
        	retMap.put("Result", 1);
			return retMap;
        }
		//查询网关信息
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayFactoryCode(cpeIds[0]);
        searchInfo.setGatewaySerialnumber(cpeIds[1]);
        GatewayInfo gi = gatewayInfoService.selectGatewayInfo(searchInfo);
        if(null == gi)
        {
        	retMap.put("Result", 1);
            return retMap;
        }
        String logName = "pboss请求ping诊断测试,";
        String key = null,pingKey = cpeId + HGU_PING_LOCK_SUFFIX_KEY,resultKey = cpeId + HGU_PING_RESULT_SUFFIX_KEY;// Redis锁
        Map<String, Object> rm = new HashMap<>();
        parameter.put("loguuid", UniqueUtil.uuid());
		logger.info(logName + "LogId:{}获取网络诊断接口请求参数:{}", parameter.get("loguuid"), parameter);
		try {
			PbossDiagnosticsUtil.getPropertiesVlanidMark(parameter, vlanidmark, logger, "Ping-diagnose");// 临时添加从此方法中获取vlanidmark
			//参数转换
			convertParam(parameter);
			//ping诊断流程，将结果存放redis
			key = gi.getGatewayMacaddress() + "_ping";
			// nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在时才进行set，如果取XX，则只有当key已经存在时才进行set;expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
			String ls = redisClientTemplate.set(key, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
			// 为便于异步查询ping结果状态，设置redis锁
			String lc = redisClientTemplate.set(pingKey, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
			if (StringUtils.isNotBlank(ls) && StringUtils.isNotBlank(lc)) {// 锁存在
				//开始诊断前，删除上次诊断结果
				Long rt = redisClientTemplate.del(resultKey);
				logger.info(logName + "LogId:{}Ping-diagnose删除redis中key为:{}的上次PING诊断结果,状态为:{}", parameter.get("loguuid"), resultKey, rt.toString());
				if (null == RequestCache.get("diagnostics_" + gi.getGatewayMacaddress())) {
					long sst = System.currentTimeMillis();
					Pattern pattern = Pattern.compile("InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.(WANIPConnection|WANPPPConnection).[\\d]+.(X_CMCC_ServiceList|ConnectionType|AddressingType|Enable|ConnectionStatus)");
					// 网关上获取Wan连接的状态与路径
					Map<String, Object> rb = PbossDiagnosticsUtil.getWanConnectStatusAndPath(rm, parameter, gi, instructionMethodService, pattern, logger, "Ping-diagnose", serviceList, new String[] { "PPPoE_Routed", "IP_Routed" }, null);
					logger.info(logName + "LogId:{}Ping-diagnose网关上获取Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
					if (null != rb && !"0".equals(rb.get("status") + "") && isSuccess(rm)) {
						// 网关未获取INTERNET的WANl连接,调用创建WAN连接方法
						if ("1".equals(rb.get("status") + "")) {
							rb = PbossDiagnosticsUtil.createDHCPWanConnect(rm, parameter, gi, instructionMethodService, logger, "Ping-diagnose", serviceList, rb.get("path") + "");
							logger.info(logName + "LogId:{}Ping-diagnose网关上创建Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
						}
						if (null != rb && "2".equals(rb.get("status") + "") && isSuccess(rm)) {
							rm = settingPingParameterAndRequestGateway(parameter, gi, rb.get("path") + "");
							if (isSuccess(rm)) {// 在网关节点上设置Ping诊断值
								logger.info(logName + "LogId:{}在网关节点上设置Ping诊断值参数成功,消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - sst));
								String path = rb.get("path") + "", lockKey = key;
								boolean delFlag = (boolean) rb.get("delFlag");
								//新开线程等待后续8事件
								new Thread(new Runnable(){

									@Override
									public void run() {
										try {
											logger.info(logName + "LogId:{}开始异步等待网关上报8事件", parameter.get("loguuid"));
											Map<String, Object> m = new HashMap<>();
											DiagnosticsUtil.temporaryObjectLock(waitTime, gi, logger, "diagnose", parameter.get("loguuid") + "");// 临时对象锁,为了等待8 DIAGNOSTICS COMPLETE状态唤醒 注:waitTime传值为空为>=0时取默认值为120000
											beginDiagnose(parameter, gi, m, parameter.get("loguuid") + "", "Ping-diagnose");// 网关上设置Ping指令成功,开始诊断获取数据
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
								
											DiagnosticsUtil.saveDiagnoseLog(parameter, gi, 3, diagnoseLogDao);// 保存诊断日志(报表需要)
											DiagnosticsUtil.deleteCreateWanConnect(parameter, gi, instructionMethodService, logger, "Ping-diagnose", path, delFlag);// 删除创建的WAN连接
											delDiagnoseRedisKey(lockKey, pingKey);
										} catch (Exception e) {
											logger.error(e.getMessage());
										}
									}
								}).start();
							} else {
								logger.info(logName + "网关诊断参数设置失败");
								retMap.put("Result", 1);
								delDiagnoseRedisKey(key, pingKey);
							}
						} else {
							logger.info(logName + "网关不存在IPWAN连接,并且创建IPWAN连接失败");
							retMap.put("Result", 1);
							delDiagnoseRedisKey(key, pingKey);
						}
					} else {
						logger.info(logName + "获取网关WAN连接信息失败");
						retMap.put("Result", 1);
						delDiagnoseRedisKey(key, pingKey);
					}
				} else {
					logger.info(logName + "LogId:{}诊断指令已发送,为防止用户重复提交,添加临时对象锁，等待指令异步返回中", parameter.get("loguuid"));
					retMap.put("Result", 1);
					delDiagnoseRedisKey(key, pingKey);
				}
//				if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
//				redisClientTemplate.del(pingKey);
			} else {
				logger.info(logName + "当前网关正在诊断,请稍后再试");
				retMap.put("Result", 1);
			}
		} catch (Exception e) {
			if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 异常情况下删除Redis锁
			redisClientTemplate.del(pingKey);
			retMap.put("Result", 1);
			logger.error("pboss ping diagnose exception " + e.getMessage(), parameter.get("loguuid"), e);
		}
//		DiagnosticsUtil.saveOperationDiagnoseLog(parameter, rm, "pboss请求Ping诊断测试", logManagerService);
		if(null == rm.get("result")){
			retMap.put("Result", 1);
		}else if(rm.get("result").toString().equals("-1")){
			retMap.put("Result", 1);
			if(null != rm.get("errorCode")){
				retMap.put("Result", rm.get("errorCode"));
			}
		}
        logger.info("end invoke pingHguDiagnose:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> pppoeHguDiagnose(Map<String, Object> parameter) {
		logger.info("start invoke pppoeHguDiagnose:{}", parameter);
		Map<String,Object> retMap = commonResult(parameter);
		if(!validatePppoeParam(parameter)){
			retMap.put("Result", 1);
			return retMap;
		}
		//CPEID为OUI-SN
		String cpeId = parameter.get("CPEID").toString();
		String[] cpeIds = cpeId.split("-");
		if(cpeIds.length != 2){
			retMap.put("Result", 1);
			return retMap;
		}
		//查询网关信息
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayFactoryCode(cpeIds[0]);
        searchInfo.setGatewaySerialnumber(cpeIds[1]);
        GatewayInfo gi = gatewayInfoService.selectGatewayInfo(searchInfo);
        if(null == gi)
        {
        	retMap.put("Result", 1);
            return retMap;
        }
        String logname = "pboss请求PPPoE仿真测试";
        String key = null,pppoeKey = cpeId + HGU_PPPOE_LOCK_SUFFIX_KEY, resultKey = cpeId + HGU_PPPOE_RESULT_SUFFIX_KEY;// Redis锁
        Map<String, Object> rm = new HashMap<>();
        parameter.put("loguuid", UniqueUtil.uuid());
        logger.info("LogId:{}" + logname + "获取仿真接口请求参数:{}", parameter.get("loguuid"), parameter);
		try {
			PbossDiagnosticsUtil.getPropertiesVlanidMark(parameter, vlanidmark, logger, logname);// 临时添加从此方法中获取vlanidmark
			//参数转换
			convertPppoeParam(parameter, gi);
			key = gi.getGatewayMacaddress() + "_ping";
			// nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在时才进行set，如果取XX，则只有当key已经存在时才进行set;expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
			String ls = redisClientTemplate.set(key, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
			// 为便于异步查询pppoe结果状态，设置redis锁
			String lc = redisClientTemplate.set(pppoeKey, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
			if (StringUtils.isNotBlank(ls) && StringUtils.isNotBlank(lc)) {
				//开始诊断前，删除上次诊断结果
				Long rt = redisClientTemplate.del(resultKey);
				logger.info(logname + "LogId:{}删除redis中key为:{}的上次PPPOE诊断结果,状态为:{}", parameter.get("loguuid"), resultKey, rt.toString());
				if (null == RequestCache.get("diagnostics_" + gi.getGatewayMacaddress())) {
					long spt = System.currentTimeMillis();
					Map<String, Object> rb = new HashMap<String, Object>();
					//判断WAN连接节点路径,参数有误就查询
					if(!validHguWanPath(rb, parameter.get("WANInterface") + "")){
						Pattern pattern = Pattern.compile("InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.WANPPPConnection.[\\d]+.(X_CMCC_ServiceList|ConnectionType|Enable|ConnectionStatus)");
						// 网关上获取Wan连接的状态与路径
						rb = PbossDiagnosticsUtil.getWanConnectStatusAndPath(rm, parameter, gi, instructionMethodService, pattern, logger, logname, "INTERNET", new String[] { "PPPoE_Routed", "" }, null);
						logger.info("LogId:{}" + logname + "网关上获取Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
					}else{
						rm.put("result", "0");
						logger.info("LogId:{}" + logname + "获取Wan连接的状态:{}与路径:{}", parameter.get("loguuid"), rb, parameter.get("WANInterface")+"");
					}
					
					// 网关未获取INTERNET的WANl连接,调用创建WAN连接方法
					if (null != rb && !"0".equals(rb.get("status") + "") && isSuccess(rm)) {
						// 网关未获取INTERNET的WANl连接,调用创建WAN连接方法
						if ("1".equals(rb.get("status") + "")) {
							rb = PbossDiagnosticsUtil.createPPPoEWanConnect(rm, parameter, gi, instructionMethodService, logger, logname, "INTERNET", rb.get("path") + "");
							logger.info("LogId:{}" + logname + "网关上创建Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
						}else{
							//未创建WAN连接，就不做删除处理
							rb.put("delFlag", Boolean.FALSE);
						}
						if (null != rb && "2".equals(rb.get("status") + "") && isSuccess(rm)) {
							//保存当前诊断调用方地址，供ACS回调使用
							String providerUrl = "dubbo://" + RpcContext.getContext().getLocalHost() + ":" + providerPort + "/" + AcsInterfaceService.class.getName();
							redisClientTemplate.set(gi.getGatewayFactoryCode() + Constant.SEPARATOR + gi.getGatewaySerialnumber() + Constant.SEPARATOR + "diagnose", providerUrl);

							rm = PbossDiagnosticsUtil.settingPingParameterAndRequestGateway(parameter, gi, instructionMethodService, this.setPppoeParameter(parameter, gi, rb.get("path") + ""));
							if (isSuccess(rm)) {
								logger.info(logname + "LogId:{}在网关节点上设置PPPOE诊断值参数成功", parameter.get("loguuid"));
								String path = rb.get("path") + "", lockKey = key;
								boolean delFlag = (boolean) rb.get("delFlag");
								new Thread(new Runnable(){

									@Override
									public void run() {
										try{
											logger.info(logname + "LogId:{}开始异步等待网关上报8事件", parameter.get("loguuid"));
											Map<String, Object> m = new HashMap<>();
											DiagnosticsUtil.temporaryObjectLock(waitTime, gi, logger, logname, parameter.get("loguuid") + "");// 临时对象锁,为等待8 DIAGNOSTICS COMPLETE状态唤醒
											getDiagnosticsResult(gi, getPppoeNodeList(), m, logname, instructionMethodService, logger, parameter, 4, diagnoseThresholdValueMapper);
											// //---------暂时保留,后续业务使用
											// long wt = System.currentTimeMillis();
											// int countFailureTime = 0;// 记录失败次数
											// while (true) {
											// String[] rst = DiagnosticsUtil.getDiagnosticsStatus(gi, "Complete", ConstantDiagnose.PPPOE_DIAGNOSTICSSTATE, instructionMethodService, log, logname);
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
											// //-------
											DiagnosticsUtil.saveDiagnoseLog(parameter, gi, 6, diagnoseLogDao);// 保存诊断日志(报表)
											DiagnosticsUtil.deleteCreateWanConnect(parameter, gi, instructionMethodService, logger, logname, path, delFlag);// 删除创建的WAN连接
											delDiagnoseRedisKey(lockKey, pppoeKey);
										}catch(Exception e){
											logger.error(e.getMessage());
										}
									}
								}).start();
								
							} else {
								logger.info(logname + ",网关诊断参数设置失败");
								retMap.put("Result", 1);
								delDiagnoseRedisKey(key, pppoeKey);
							}
						} else {
							logger.info(logname + ",网关不存在PPPWAN连接,并且创建PPPWAN连接失败");
							retMap.put("Result", 1);
							delDiagnoseRedisKey(key, pppoeKey);
						}
					} else {
						logger.info(logname + ",获取网关WAN连接信息失败");
						retMap.put("Result", 1);
						delDiagnoseRedisKey(key, pppoeKey);
					}
					logger.info("LogId:{}" + logname + "网关诊断设值,获取诊断状态与获取诊断结果总消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - spt));
				} else {
					logger.info("LogId:{}" + logname + "诊断指令已发送,为防止用户重复提交,添加临时对象锁，等待指令异步返回中", parameter.get("loguuid"));
					retMap.put("Result", 1);
					delDiagnoseRedisKey(key, pppoeKey);
				}
//				if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
//				redisClientTemplate.del(pppoeKey);
			} else {
				logger.info(logname + ",当前网关正在诊断,请稍后再试");
				retMap.put("Result", 1);
			}
		} catch (Exception e) {
			if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
			redisClientTemplate.del(pppoeKey);
			retMap.put("Result", 1);
			logger.error("LogId:{}" + logname + " Diagnostics exception " + e.getMessage(), parameter.get("loguuid"), e);
		}
		//DiagnosticsUtil.saveOperationDiagnoseLog(parameter, rm, "pboss请求Pppoe仿真测试", logManagerService);
		if(null == rm.get("result")){
			retMap.put("Result", 1);
		}else if(rm.get("result").toString().equals("-1")){
			retMap.put("Result", 1);
			if(null != rm.get("errorCode")){
				retMap.put("Result", rm.get("errorCode"));
			}
		}
		logger.info("end invoke pppoeHguDiagnose:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> tracerouteHguDiagnose(Map<String, Object> parameter) {
		logger.info("start invoke tracerouteHguDiagnose:{}", parameter);
		Map<String,Object> retMap = commonResult(parameter);
		if(!validateTracerouteParam(parameter)){
			retMap.put("Result", 1);
			return retMap;
		}
		//CPEID为OUI-SN
		String cpeId = parameter.get("CPEID").toString();
		String[] cpeIds = cpeId.split("-");
		if(cpeIds.length != 2){
			retMap.put("Result", 1);
			return retMap;
		}
		//查询网关信息
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayFactoryCode(cpeIds[0]);
        searchInfo.setGatewaySerialnumber(cpeIds[1]);
        GatewayInfo gi = gatewayInfoService.selectGatewayInfo(searchInfo);
        if(null == gi)
        {
        	retMap.put("Result", 1);
            return retMap;
        }
        
		String logname = "pboss请求Traceroute诊断测试";
		parameter.put("loguuid", UniqueUtil.uuid());
		Map<String,Object> rm = new HashMap<>();
		logger.info("LogId:{}" + logname + "获取仿真接口请求参数:{}", parameter.get("loguuid"), parameter);
		String key = null,tracerouteKey = cpeId + HGU_TRACEROUTE_LOCK_SUFFIX_KEY, resultKey = cpeId + HGU_TRACEROUTE_RESULT_SUFFIX_KEY;// Redis锁
		try {
			PbossDiagnosticsUtil.getPropertiesVlanidMark(parameter, vlanidmark, logger, logname);// 临时添加从此方法中获取vlanidmark
			//参数转换
			convertTracerouteParam(parameter);
			key = gi.getGatewayMacaddress() + "_ping";
			// nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在时才进行set，如果取XX，则只有当key已经存在时才进行set;expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
			String ls = redisClientTemplate.set(key, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
			// 为便于异步查询traceroute结果状态，设置redis锁
			String lc = redisClientTemplate.set(tracerouteKey, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
			if (StringUtils.isNotBlank(ls) && StringUtils.isNotBlank(lc)) {
				//开始诊断前，删除上次诊断结果
				Long rt = redisClientTemplate.del(resultKey);
				logger.info(logname + "LogId:{}删除redis中key为:{}的上次TRACEROUTE诊断结果,状态为:{}", parameter.get("loguuid"), resultKey, rt.toString());
				if (null == RequestCache.get("diagnostics_" + gi.getGatewayMacaddress())) {
					long spt = System.currentTimeMillis();
					//判断WAN连接节点路径,参数有误就查询
					Map<String, Object> rb = new HashMap<String, Object>();
					if(!validHguWanPath(rb, parameter.get("Interface") + "")){
						Pattern pattern = Pattern.compile("InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.(WANIPConnection|WANPPPConnection).[\\d]+.(X_CMCC_ServiceList|ConnectionType|AddressingType|Enable|ConnectionStatus)");
						// 网关上获取Wan连接的状态与路径
						rb = PbossDiagnosticsUtil.getWanConnectStatusAndPath(rm, parameter, gi, instructionMethodService, pattern, logger, logname, "INTERNET", new String[] { "PPPoE_Routed", "IP_Routed" }, null);
						logger.info("LogId:{}" + logname + "网关上获取Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
					}else{
						rm.put("result", "0");
						logger.info("LogId:{}" + logname + "网关上获取Wan连接的状态:{}与路径:{}", parameter.get("loguuid"), rb, parameter.get("Interface") + "");
					}
					
					if (null != rb && !"0".equals(rb.get("status") + "") && isSuccess(rm)) {
						// 网关未获取INTERNET的WANl连接,调用创建WAN连接方法
						if ("1".equals(rb.get("status") + "")) {
							rb = PbossDiagnosticsUtil.createDHCPWanConnect(rm, parameter, gi, instructionMethodService, logger, logname, "INTERNET", rb.get("path") + "");
							logger.info("LogId:{}" + logname + "网关上创建Wan连接的状态与路径:{}", parameter.get("loguuid"), rb);
						}else{
							//未创建WAN连接，就不做删除处理
							rb.put("delFlag", Boolean.FALSE);
						}
						if (null != rb && "2".equals(rb.get("status") + "") && isSuccess(rm)) {
							//保存当前诊断调用方地址，供ACS回调使用
							String providerUrl = "dubbo://" + RpcContext.getContext().getLocalHost() + ":" + providerPort + "/" + AcsInterfaceService.class.getName();
							redisClientTemplate.set(gi.getGatewayFactoryCode() + Constant.SEPARATOR + gi.getGatewaySerialnumber() + Constant.SEPARATOR + "diagnose", providerUrl);
							rm = PbossDiagnosticsUtil.settingPingParameterAndRequestGateway(parameter, gi, instructionMethodService, this.setTracerouteParameter(parameter, gi, rb.get("path") + ""));
							if (isSuccess(rm)) {
								logger.info(logname + "LogId:{}在网关节点上设置Traceroute诊断值参数成功", parameter.get("loguuid"));
								String path = rb.get("path") + "", lockKey = key;
								boolean delFlag = (boolean) rb.get("delFlag");
								new Thread(new Runnable(){

									@Override
									public void run() {
										try{
											logger.info(logname + "LogId:{}开始异步等待网关上报8事件", parameter.get("loguuid"));
											Map<String,Object> m = new HashMap<>();
											DiagnosticsUtil.temporaryObjectLock(waitTime, gi, logger, logname, parameter.get("loguuid") + "");// 临时对象锁,为等待8 DIAGNOSTICS COMPLETE状态唤醒
											getTracerouteDiagnosticsResult(gi, getTracerouteNodeList(), m, logname, instructionMethodService, logger, parameter);
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
											DiagnosticsUtil.deleteCreateWanConnect(parameter, gi, instructionMethodService, logger, logname, path, delFlag);// 删除创建的WAN连接
											delDiagnoseRedisKey(lockKey, tracerouteKey);
										}catch(Exception e){
											logger.error(e.getMessage());
										}
									}
								}).start();
							} else {
								logger.info(logname + "网关诊断参数设置失败", "");
								retMap.put("Result", 1);
								delDiagnoseRedisKey(key, tracerouteKey);
							}
						} else {
							logger.info(logname + "网关不存在IPWAN连接,并且创建IPWAN连接失败");
							retMap.put("Result", 1);
							delDiagnoseRedisKey(key, tracerouteKey);
						}
					} else {
						logger.info(logname + "获取网关WAN连接信息失败");
						retMap.put("Result", 1);
						delDiagnoseRedisKey(key, tracerouteKey);
					}
					logger.info("LogId:{}" + logname + "网关诊断设值,获取诊断状态与获取诊断结果总消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - spt));
				} else {
					logger.info("LogId:{}" + logname + "诊断指令已发送,为防止用户重复提交,添加临时对象锁，等待指令异步返回中", parameter.get("loguuid"));
					retMap.put("Result", 1);
					delDiagnoseRedisKey(key, tracerouteKey);
				}
//				if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
//				redisClientTemplate.del(tracerouteKey);
			} else {
				logger.info(logname + "当前网关正在诊断,请稍后再试");
				retMap.put("Result", 1);
			}
		} catch (Exception e) {
			if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
			redisClientTemplate.del(tracerouteKey);
			logger.error("LogId:{}" + logname + " Diagnostics exception " + e.getMessage(), parameter.get("loguuid"), e);
		}
		//DiagnosticsUtil.saveOperationDiagnoseLog(parameter, rm, "pboss请求Traceroute诊断测试", logManagerService);
		if(null == rm.get("result")){
			retMap.put("Result", 1);
		}else if(rm.get("result").toString().equals("-1")){
			retMap.put("Result", 1);
			if(null != rm.get("errorCode")){
				retMap.put("Result", rm.get("errorCode"));
			}
		}
		logger.info("end invoke tracerouteHguDiagnose:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> getHguPingInfo(Map<String, Object> parameter) {
		logger.info("start invoke getHguPingInfo:{}", parameter);
		Map<String,Object> retMap = commonResult(parameter);
		if(parameter.get("CPEID") == null || StringUtils.isBlank(parameter.get("CPEID").toString())){
			retMap.put("Result", 1);
			return retMap;
		}
		String cpeId = parameter.get("CPEID").toString();
		String pingState = redisClientTemplate.get(cpeId + HGU_PING_LOCK_SUFFIX_KEY);
		if(pingState != null){
			retMap.put("Result", 1);
			return retMap;
		}
		String pingResult = redisClientTemplate.get(cpeId + HGU_PING_RESULT_SUFFIX_KEY);
		if(StringUtils.isEmpty(pingResult)){
			retMap.put("Result", 2);
			return retMap;
		}
		Map<String,Object> pingResultData = JSON.parseObject(pingResult, Map.class);
		//转换结果参数名为文档定义的名称
		retMap.put("SuccessCount", pingResultData.get("SuccessCount"));
		retMap.put("FailureCount", pingResultData.get("FailureCount"));
		retMap.put("AverageRespTime", pingResultData.get("AverageResponseTime"));
		retMap.put("MinimumRespTime", pingResultData.get("MinimumResponseTime"));
		retMap.put("MaxRespTime", pingResultData.get("MaximumResponseTime"));
		//retMap.putAll(pingResultData);
		logger.info("end invoke getHguPingInfo:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> getHguPppoeInfo(Map<String, Object> parameter) {
		logger.info("start invoke getHguPppoeInfo:{}", parameter);
		Map<String,Object> retMap = commonResult(parameter);
		if(parameter.get("CPEID") == null || StringUtils.isBlank(parameter.get("CPEID").toString())){
			retMap.put("Result", 1);
			return retMap;
		}
		String cpeId = parameter.get("CPEID").toString();
		String pppoeState = redisClientTemplate.get(cpeId + HGU_PPPOE_LOCK_SUFFIX_KEY);
		if(pppoeState != null){
			retMap.put("Result", 1);
			return retMap;
		}
		String pppoeResult = redisClientTemplate.get(cpeId + HGU_PPPOE_RESULT_SUFFIX_KEY);
		if(StringUtils.isEmpty(pppoeResult)){
			retMap.put("Result", 2);
			return retMap;
		}
		Map<String,Object> pingResultData = JSON.parseObject(pppoeResult, Map.class);
		//转换结果参数名为文档定义的名称
		retMap.put("PPPoEResult", pingResultData.get("Result"));
		retMap.put("ExternalIPAddress", pingResultData.get("ExternalIPAddress"));
		retMap.put("DefaultGateway", pingResultData.get("DefaultGateway"));
//		retMap.putAll(pingResultData);
		logger.info("end invoke getHguPppoeInfo:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> getHguTracerouteInfo(Map<String, Object> parameter) {
		logger.info("start invoke getHguTracerouteInfo:{}", parameter);
		Map<String,Object> retMap = commonResult(parameter);
		if(parameter.get("CPEID") == null || StringUtils.isBlank(parameter.get("CPEID").toString())){
			retMap.put("Result", 1);
			return retMap;
		}
		String cpeId = parameter.get("CPEID").toString();
		String tracerouteState = redisClientTemplate.get(cpeId + HGU_TRACEROUTE_LOCK_SUFFIX_KEY);
		if(tracerouteState != null){
			retMap.put("Result", 1);
			return retMap;
		}
		String tracerouteResult = redisClientTemplate.get(cpeId + HGU_TRACEROUTE_RESULT_SUFFIX_KEY);
		if(StringUtils.isEmpty(tracerouteResult)){
			retMap.put("Result", 2);
			return retMap;
		}
		Map<String,Object> pingResultData = JSON.parseObject(tracerouteResult, Map.class);
		List<Map<String,Object>> hopsEntries = (List<Map<String, Object>>) pingResultData.get("tdl");
		//转换结果参数名为文档定义的名称
		retMap.put("ResponseTime", pingResultData.get("ResponseTime"));
		retMap.put("HopsNumberOfEntries", pingResultData.get("HopsNumberOfEntries"));
		retMap.put("HopsEntries", hopsEntries);
		logger.info("end invoke getHguTracerouteInfo:{}", retMap);
		return retMap;
	}
	/**
	 * 验证PING诊断的参数
	 * @param parameter
	 * @return
	 */
	private boolean validatePringParam(Map<String, Object> parameter){
		List<String> paramList = Arrays.asList(new String[]{"CPEID","Host","DataBlockSize","ServiceList","NumberOfRepet","Timeout"});
		boolean isTrue = true;
		if(parameter == null || parameter.size() == 0){
			isTrue = false;
		}
		for(String param : paramList){
			if(parameter.get(param) == null || StringUtils.isBlank(parameter.get(param).toString())){
				isTrue = false;
			}
		}
		return isTrue;
	}
	/**
	 * 验证PPPOE仿真的参数
	 * @param parameter
	 * @return
	 */
	private boolean validatePppoeParam(Map<String, Object> parameter){
		List<String> paramList = Arrays.asList(new String[]{"CPEID","Username","Password","WANInterface","PPPAuthenticationProtocol","RetryTimes"});
		boolean isTrue = true;
		if(parameter == null || parameter.size() == 0){
			isTrue = false;
		}
		for(String param : paramList){
			if(parameter.get(param) == null || StringUtils.isBlank(parameter.get(param).toString())){
				isTrue = false;
			}
		}
		return isTrue;
	}
	/**
	 * 验证TRACEROUTE诊断的参数
	 * @param parameter
	 * @return
	 */
	private boolean validateTracerouteParam(Map<String, Object> parameter){
		List<String> paramList = Arrays.asList(new String[]{"CPEID","Interface","Host","NumberOfTries","Timeout","DataBlockSize","DSCP","MaxHopCount"});
		boolean isTrue = true;
		if(parameter == null || parameter.size() == 0){
			isTrue = false;
		}
		for(String param : paramList){
			if(parameter.get(param) == null || StringUtils.isBlank(parameter.get(param).toString())){
				isTrue = false;
			}
		}
		return isTrue;
	}
	/**
	 * 公共返回结果
	 * @param parameter
	 * @return
	 */
	private Map<String,Object> commonResult(Map<String, Object> parameter){
		Map<String,Object> resultMap = new HashMap<>();
		resultMap.put("CPEID", parameter.get("CPEID"));
		resultMap.put("Result", 0);
		return resultMap;
	}
	/**
	 * PING诊断参数转换（处理请求的参数为适配的参数，避免对系统公共方法的修改）
	 * @param parameter
	 * @param gi
	 * @return
	 */
	private Map<String,Object> convertParam(Map<String, Object> parameter){
		
		parameter.put("host", parameter.get("Host"));
		parameter.put("dataBlockSize", parameter.get("DataBlockSize"));
		parameter.put("numberOfRepetitions", parameter.get("NumberOfRepet"));
		parameter.put("timeout", parameter.get("Timeout"));
		//用于写入超级管理的操作日志
		parameter.put("userName", "admin");
		parameter.put("roleName", "超级管理员");
		return parameter;
	}
	/**
	 * PPPOE仿真参数转换（处理请求的参数为适配的参数，避免对系统公共方法的修改）
	 * @param parameter
	 * @param gi
	 * @return
	 */
	private Map<String,Object> convertPppoeParam(Map<String, Object> parameter, GatewayInfo gi){
		
		parameter.put("username", parameter.get("Username"));
		parameter.put("password", parameter.get("Password"));
		parameter.put("pppAuthenticationProtocol", parameter.get("PPPAuthenticationProtocol"));
		parameter.put("retryTimes", parameter.get("RetryTimes"));
		//用于写入超级管理的操作日志
		parameter.put("userName", "admin");
		parameter.put("roleName", "超级管理员");
		return parameter;
	}
	/**
	 * TRACEROUTE诊断参数转换（处理请求的参数为适配的参数，避免对系统公共方法的修改）
	 * @param parameter
	 * @param gi
	 * @return
	 */
	private Map<String,Object> convertTracerouteParam(Map<String, Object> parameter){
		
		parameter.put("host", parameter.get("Host"));
		parameter.put("numberOfTries", parameter.get("NumberOfTries"));
		parameter.put("timeout", parameter.get("Timeout"));
		parameter.put("dataBlockSize", parameter.get("DataBlockSize"));
		parameter.put("dscp", parameter.get("DSCP"));
		parameter.put("maxHopCount", parameter.get("MaxHopCount"));
		//设置确实的参数mode,取值范围UDP,ICMP,这里设置为UDP(待确认)
		parameter.put("mode", "UDP");
		//用于写入超级管理的操作日志
		parameter.put("userName", "admin");
		parameter.put("roleName", "超级管理员");
		return parameter;
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
	public Map<String,Object> settingPingParameterAndRequestGateway(Map<String, Object> parameter, GatewayInfo gi, String path) {
		//保存当前诊断调用方地址，供ACS回调使用
		String providerUrl = "dubbo://" + RpcContext.getContext().getLocalHost() + ":" + providerPort + "/" + AcsInterfaceService.class.getName();
		redisClientTemplate.set(gi.getGatewayFactoryCode() + Constant.SEPARATOR + gi.getGatewaySerialnumber() + Constant.SEPARATOR + "diagnose", providerUrl);
		return instructionMethodService.setParameterValueErrorCode(gi.getGatewayMacaddress(), this.setParameter(parameter, gi, path));// 组装数据
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
	private void beginDiagnose(Map<String, Object> parameter, GatewayInfo gi, Map<String, Object> rm, String logid, String logname) throws Exception {
		List<String> list = new ArrayList<String>();// 获取诊断信息网关节点路径集
		this.packagePingAndDial(list);// 2、组装GetParameterValues指令的参数
		this.saveResultMapData(parameter, gi, list, rm, logid, logname);

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
	 * 得到获取网关DHCP仿真节点(TRACEROUTE)
	 * 
	 * @return
	 */
	private List<String> getTracerouteNodeList() {
		List<String> list = new ArrayList<>();
		list.add(ConstantDiagnose.TRACEROUTE_DIAGNOSTICSSTATE);// 诊断状态
		list.add(ConstantDiagnose.TRACEROUTE_RESPONSETIME);// 诊断响应时间(单位：ms)
		list.add(ConstantDiagnose.TRACEROUTE_HOPSNUMBEROFENTRIES);// 实际探测到的总跳数
		return list;
	}
	/**
	 * 得到获取网关DHCP仿真节点(PPPOE)
	 * 
	 * @return
	 */
	private List<String> getPppoeNodeList() {
		List<String> list = new ArrayList<>();
		list.add(ConstantDiagnose.PPPOE_DIAGNOSTICSSTATE);// 诊断状态
		list.add(ConstantDiagnose.PPPOE_RESULT);// 诊断结果
		list.add(ConstantDiagnose.PPPOE_PPPSESSIONID);// 会话ID
		list.add(ConstantDiagnose.PPPOE_EXTERNALIPADDRESS);// IP地址
		list.add(ConstantDiagnose.PPPOE_DEFAULTGATEWAY);// 默认网关
		return list;
	}
	/**
	 * 
	 * 
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
	 * 保存网关PING诊断返回结果数据，并保存
	 * 
	 * @param gi 网关对象信息
	 * @param list 获取诊断信息网关节点路径集
	 * @param rm 返回对象
	 * @param logid 日志ID
	 * @param logName 日志名称
	 */
	private void saveResultMapData(Map<String, Object> parameter,GatewayInfo gi, List<String> list, Map<String, Object> rm, String logid, String logName) {
		Map<String, Object> resultDiagnose = instructionMethodService.getParameterValuesErrorCode(gi.getGatewayMacaddress(), list);// 下发指令获取诊断参数信息
		logger.info(logName + "LogId:{}网关诊断调用getParameterValues返回结果为:{}", parameter.get("loguuid"), resultDiagnose);
		if (null != resultDiagnose && resultDiagnose.size() > 0) {
			rm.putAll(resultDiagnose);
			if(null != resultDiagnose.get("result") && resultDiagnose.get("result").toString().equals("0")){
				resultDiagnose.remove("result");
			
				Map<String, Object> result = new HashMap<>();
				for (Map.Entry<String, Object> entry : resultDiagnose.entrySet()) {
					String name = entry.getKey();
					String value = String.valueOf(entry.getValue());
					String resultName = name.substring(name.lastIndexOf(".") + 1, name.length());// 获取名称最后一个“.”后的参数为KEY值
					result.put(resultName, value);
				}
				result = DiagnosticsUtil.getDiagnoseThresholdFlag(result, 1, 1, diagnoseThresholdValueMapper, logid, logger, logName);// 获取诊断值的阈值状态
				String cpeId = parameter.get("CPEID").toString();
				logger.info(logName + "LogId:{}网关诊断结果为:{}", parameter.get("loguuid"), result);
				//将结果存入redis
				redisClientTemplate.set(cpeId + HGU_PING_RESULT_SUFFIX_KEY, JSON.toJSONString(result));
//				rm.putAll(result);
			}
		}else{
			rm.put("result", -1);
			logger.info(logName + "获取网关诊断信息失败");
		}
	}
	
	/**
	 * 获取网关诊断结果(PPPOE)
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
	public void getDiagnosticsResult(GatewayInfo gi, List<String> node, Map<String,Object> rm, String logName, InstructionMethodService ims, Logger log, Map<String,Object> parameter, int diagnoseType, DiagnoseThresholdValueMapper dtvdao) {
		long st = System.currentTimeMillis();
		String logid = parameter.get("loguuid")+ "";
		String cpeId = parameter.get("CPEID").toString();
		Map<String, Object> result = ims.getParameterValuesErrorCode(gi.getGatewayMacaddress(), node);
		log.info("LogId:{}" + logName + "获取网关诊断返回结果集:{}消耗时间:{}", logid, result, (System.currentTimeMillis() - st));
		if (null != result && result.size() > 0) {
			rm.putAll(result);
			if(null != result.get("result") && result.get("result").toString().equals("0")){
				result.remove("result");
				long fst = System.currentTimeMillis();
				result = DiagnosticsUtil.getResultMapDate(result);// 获取返回封装数据
				// 目前只有DHCP限定业务要去获取状态,线路诊断,Ping诊断,Traceroute是自己的业务不在公共方法中
				if (diagnoseType == 5) result = DiagnosticsUtil.getDiagnoseThresholdFlag(result, diagnoseType, 1, dtvdao, logid, log, logName);// 获取诊断值的阈值状态
				//将结果存入redis
				redisClientTemplate.set(cpeId + HGU_PPPOE_RESULT_SUFFIX_KEY, JSON.toJSONString(result));
				log.info("LogId:{}" + logName + "将诊断结果封装返回结果集:{}消耗时间:{}", logid, result, (System.currentTimeMillis() - fst));
				//rm.putAll(result);
			}
		}else{
			rm.put("result", -1);
			log.info(logName + "获取网关诊断信息失败");
		}
	}
	/**
	 * 获取网关Traceroute诊断结果,并保存
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
	private void getTracerouteDiagnosticsResult(GatewayInfo gi, List<String> node, Map<String, Object> rm, String logName, InstructionMethodService ims, Logger log, Map<String, Object> parameter) {
		long st = System.currentTimeMillis();
		String logid = parameter.get("loguuid").toString();
		String cpeId = parameter.get("CPEID").toString();
		log.info("LogId:{}" + logName + "getParameterValues获取网关诊断返回结果", logid);
		Map<String, Object> result = ims.getParameterValuesErrorCode(gi.getGatewayMacaddress(), node);
		log.info("LogId:{}" + logName + "获取网关诊断返回结果集:{}消耗时间:{}", logid, result, (System.currentTimeMillis() - st));
		if (null != result && result.size() > 0) {
			rm.putAll(result);
			if(null != result.get("result") && result.get("result").toString().equals("0")){
				result.remove("result");
				long fst = System.currentTimeMillis();
				result = DiagnosticsUtil.getResultMapDate(result);// 获取返回封装数据
				result = DiagnosticsUtil.getDiagnoseThresholdFlag(result, 3, 1, diagnoseThresholdValueMapper, logid, log, logName);//获取诊断值的阈值状态
				result.put("tdl", transferTracerouteResult(getTracerouteDiagnosticeList(rm, gi, logName, logid)));
				redisClientTemplate.set(cpeId + HGU_TRACEROUTE_RESULT_SUFFIX_KEY, JSON.toJSONString(result));
				log.info("LogId:{}" + logName + "诊断返回结果集:{}封装消耗时间:{}", logid, result, (System.currentTimeMillis() - fst));
				//rm.putAll(result);
			}
		} else {
			rm.put("result", -1);
			logger.info(logName + "获取网关诊断信息失败");
		}
	}
	/**
	 * 在不同的节点下组装设置参数(PING)
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
	 * 在不同的节点下组装设置参数(PPPOE)
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
	private List<ParameterValueStruct> setPppoeParameter(Map<String, Object> parameter, GatewayInfo gi, String path) {
		List<ParameterValueStruct> list = new ArrayList<>();
		// 诊断状态
		ParameterValueStruct ppoedspvs = new ParameterValueStruct();
		ppoedspvs.setName(ConstantDiagnose.PPPOE_DIAGNOSTICSSTATE);
		ppoedspvs.setValue("Start");
		ppoedspvs.setReadWrite(true);
		ppoedspvs.setValueType("string");
		list.add(ppoedspvs);
		// PPPoE用户名
		ParameterValueStruct username = new ParameterValueStruct();
		username.setName(ConstantDiagnose.PPPOE_USERNAME);
		username.setValue(parameter.get("username"));
		username.setReadWrite(true);
		username.setValueType("string");
		list.add(username);
		// PPPoE用户密码
		ParameterValueStruct password = new ParameterValueStruct();
		password.setName(ConstantDiagnose.PPPOE_PASSWORD);
		password.setValue(parameter.get("password"));
		password.setReadWrite(true);
		password.setValueType("string");
		list.add(password);
		// WAN连接（TR069节点全路径）针对桥接和路由WAN均适用
		ParameterValueStruct wanInterface = new ParameterValueStruct();
		wanInterface.setName(ConstantDiagnose.PPPOE_WANINTERFACE);
		// 从数据库获取Wan连接节点路径
		// wanInterface.setValue(gi.getGatewayExternalIPaddress().trim());
		// 从网关获取Wan连接节点路径
		wanInterface.setValue(path);
		wanInterface.setReadWrite(true);
		wanInterface.setValueType("string");
		list.add(wanInterface);
		// 认证模式，如下值之一："PAP" "CHAP"
		ParameterValueStruct pppAuthenticationProtocol = new ParameterValueStruct();
		pppAuthenticationProtocol.setName(ConstantDiagnose.PPPOE_PPPAUTHENTICATIONPROTOCOL);
		pppAuthenticationProtocol.setValue(parameter.get("pppAuthenticationProtocol"));
		pppAuthenticationProtocol.setReadWrite(true);
		pppAuthenticationProtocol.setValueType("string");
		list.add(pppAuthenticationProtocol);
		// 重试次数
		ParameterValueStruct retryTimes = new ParameterValueStruct();
		retryTimes.setName(ConstantDiagnose.PPPOE_RETRYTIMES);
		retryTimes.setValue(parameter.get("retryTimes"));
		retryTimes.setReadWrite(true);
		retryTimes.setValueType("unsignedInt");
		list.add(retryTimes);
		return list;
	}
	/**
	 * 在不同的节点下组装设置参数(TRACEROUTE)
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
	private List<ParameterValueStruct> setTracerouteParameter(Map<String, Object> parameter, GatewayInfo gi, String path) {
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
	private List<Map<String, Object>> getTracerouteDiagnosticeList(Map<String,Object> rm, GatewayInfo gi, String logName, String logid) {
		Map<String, Object> result = instructionMethodService.getParameterNamesErrorCode(gi.getGatewayMacaddress(), ConstantDiagnose.TRACEROUTE_OBJECT, false);
		logger.info("LogId:{}" + logName + "获取Traceroute诊断记录列表:{}", logid, result);
		List<Map<String, Object>> tdl = new ArrayList<Map<String, Object>>();
		if (null != result && result.size() > 0) {
			rm.putAll(result);
			if(null != result.get("result") && result.get("result").toString().equals("0")){
				result.remove("result");
				HashSet<String> nameList = getNamesList(result, Pattern.compile("InternetGatewayDevice.TraceRouteDiagnostics.RouteHops.[\\d]+.(HopHost|HopHostAddress|HopErrorCode|HopRTTimes)"));// 获取Traceroute诊断记录列表
				logger.info("LogId:{}" + logName + "获取获取网关节点名集合:{}", logid, nameList);
				Iterator<String> it = nameList.iterator();
				while (it.hasNext()) {
					String node = it.next();
					if (StringUtils.isNotBlank(node)) {
						Map<String, Object> resultlist = instructionMethodService.getParameterValuesErrorCode(gi.getGatewayMacaddress(), this.getChildrenNodePath(node));
						logger.info("LogId:{}" + logName + "获取Traceroute诊断记录列表:{}", logid, resultlist);
						if (null != resultlist && resultlist.size() > 0) {
							rm.clear();
							rm.putAll(resultlist);
							if(null != resultlist.get("result") && resultlist.get("result").toString().equals("0")){
								Map<String, Object> map = new HashMap<>();
								resultlist.remove("result");
								for (Map.Entry<String, Object> entry : resultlist.entrySet()) {
									String name = entry.getKey();
									String value = entry.getValue() + "";
									String resultName = name.substring(name.lastIndexOf(".") + 1, name.length());// 获取名称最后一个“.”后的参数为KEY值
									map.put(resultName, value);
								}
								tdl.add(map);
							}
						}else{
							rm.put("result", -1);
						}
					}
				}
			}
			
		}else{
			rm.put("result", -1);
		}
		return tdl;
	}
	/**
	 * 获取网关节点名集合(traceroute)
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

	@Override
	public Map<String, Object> pingStbDiagnose(Map<String, Object> parameter) {
		logger.info("start invoke pingStbDiagnose:{}", parameter);
		long bt = System.currentTimeMillis();
		Map<String,Object> retMap = commonResult(parameter);
		if(!validatePringParam(parameter)){
			retMap.put("Result", 1);
			return retMap;
		}
		//CPEID为OUI-SN
		String[] cpeId = parameter.get("CPEID").toString().split("-");
		if(cpeId.length != 2){
			retMap.put("Result", 1);
			return retMap;
		}
		String serviceList = ServiceListEnum.getName(parameter.get("ServiceList").toString());
        if(StringUtils.isEmpty(serviceList)){
        	retMap.put("Result", 1);
			return retMap;
        }
        //查询机顶盒信息
        BoxInfo searchInfo = new BoxInfo();
        searchInfo.setBoxFactoryCode(cpeId[0]);
        searchInfo.setBoxSerialnumber(cpeId[1]);

        BoxInfo gi = boxInfoMapper.selectGatewayInfo(searchInfo);
        if(null == gi)
        {
        	retMap.put("Result", 1);
            return retMap;
        }
        String logname = "pboss请求机顶盒Ping诊断,";
		parameter.put("loguuid", UniqueUtil.uuid());
		logger.info("LogId:{}" + logname + "获取仿真接口请求参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		String key = null, pingKey = cpeId + STB_PING_LOCK_SUFFIX_KEY;// Redis锁
        try {
        	//开始诊断前，删除上次诊断结果
			redisClientTemplate.del(cpeId + STB_PING_RESULT_SUFFIX_KEY);
        	//参数转换
			convertParam(parameter);
			key = gi.getBoxMacaddress() + "_box_ping";
			// nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在时才进行set，如果取XX，则只有当key已经存在时才进行set;expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
			String ls = redisClientTemplate.set(key, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
			// 为便于异步查询ping结果状态，设置redis锁
			redisClientTemplate.set(pingKey, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
			if (StringUtils.isNotBlank(ls)) {
				if (null == RequestCache.get("box_diagnostics_" + gi.getBoxMacaddress())) {
					long spt = System.currentTimeMillis();
					if (BoxDiagnosticsUtil.settingPingParameterAndRequestGateway(parameter, gi, boxInstructionMethodService, this.setParameter(parameter, gi))) {
						BoxDiagnosticsUtil.temporaryObjectLock(waitTime, gi, logger, logname, parameter.get("loguuid") + "");// 临时对象锁,为等待8 DIAGNOSTICS COMPLETE状态唤醒
						getDiagnosticsResult(gi, getBoxPingNodeList(), rm, logname, boxInstructionMethodService, logger, parameter);
					} else {
						retMap.put("Result", 1);
						logger.info("LogId:{}" + logname + "机顶盒诊断参数设置失败", parameter.get("loguuid") + "");
					}
					logger.info("LogId:{}" + logname + "机顶盒诊断设值,获取诊断状态与获取诊断结果总消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - spt));
				} else {
					retMap.put("Result", 1);
					logger.info("LogId:{}" + logname + "诊断指令已发送,为防止用户重复提交,添加临时对象锁，等待指令异步返回中", parameter.get("loguuid"));
				}
				if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
				redisClientTemplate.del(pingKey);
			} else {
				retMap.put("Result", 1);
				logger.info("LogId:{}" + logname + "当前机顶盒正在诊断,请稍后再试", parameter.get("loguuid"));
			}
		} catch (Exception e) {
			if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
			redisClientTemplate.del(pingKey);
			retMap.put("Result", 1);
			logger.error("LogId:{}" + logname + " Diagnostics exception " + e.getMessage(), parameter.get("loguuid"), e);
		}
        logger.info("LogId:{}" + logname + "业务诊断返回的结果集:{}总消耗时间:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - bt));
        logger.info("end invoke pingStbDiagnose:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> tracerouteStbDiagnose(Map<String, Object> parameter) {
		logger.info("start invoke tracerouteStbDiagnose:{}", parameter);
		long bt = System.currentTimeMillis();
		Map<String,Object> retMap = commonResult(parameter);
		if(!validateTracerouteParam(parameter)){
			retMap.put("Result", 1);
			return retMap;
		}
		//CPEID为OUI-SN
		String[] cpeId = parameter.get("CPEID").toString().split("-");
		if(cpeId.length != 2){
			retMap.put("Result", 1);
			return retMap;
		}
        //查询机顶盒信息
        BoxInfo searchInfo = new BoxInfo();
        searchInfo.setBoxFactoryCode(cpeId[0]);
        searchInfo.setBoxSerialnumber(cpeId[1]);

        BoxInfo gi = boxInfoMapper.selectGatewayInfo(searchInfo);
        if(null == gi)
        {
        	retMap.put("Result", 1);
            return retMap;
        }
		String logname = "pboss请求机顶盒Traceroute诊断测试,";
		parameter.put("loguuid", UniqueUtil.uuid());
		logger.info("LogId:{}" + logname + "获取仿真接口请求参数{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		String key = null, tracerouteKey = cpeId + STB_TRACEROUTE_LOCK_SUFFIX_KEY;// Redis锁
		try {
			//开始诊断前，删除上次诊断结果
			redisClientTemplate.del(cpeId + STB_TRACEROUTE_RESULT_SUFFIX_KEY);
			//参数转换
			convertTracerouteParam(parameter);
			key = gi.getBoxMacaddress() + "_box_ping";
			// nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在时才进行set，如果取XX，则只有当key已经存在时才进行set;expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
			String ls = redisClientTemplate.set(key, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
			// 为便于异步查询traceroute结果状态，设置redis锁
			redisClientTemplate.set(tracerouteKey, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
			if (StringUtils.isNotBlank(ls)) {
				if (null == RequestCache.get("box_diagnostics_" + gi.getBoxMacaddress())) {
					long spt = System.currentTimeMillis();
					if (BoxDiagnosticsUtil.settingPingParameterAndRequestGateway(parameter, gi, boxInstructionMethodService, this.setParameter(parameter, gi))) {
						BoxDiagnosticsUtil.temporaryObjectLock(waitTime, gi, logger, logname, parameter.get("loguuid") + "");// 临时对象锁,为等待8 DIAGNOSTICS COMPLETE状态唤醒
						this.getBoxDiagnosticsResult(gi, getBoxTracerouteNodeList(), rm, logname, boxInstructionMethodService, logger, parameter);
					} else {
						retMap.put("Result", 1);
						logger.info("LogId:{}" + logname + "盒子诊断参数设置失败", parameter.get("loguuid"));
					}
					logger.info("LogId:{}" + logname + "盒子诊断设值,获取诊断状态与获取诊断结果总消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - spt));
				} else {
					retMap.put("Result", 1);
					logger.info("LogId:{}" + logname + "诊断指令已发送,为防止用户重复提交,添加临时对象锁，等待指令异步返回中", parameter.get("loguuid"));
				}
				if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
				redisClientTemplate.del(tracerouteKey);
			} else {
				retMap.put("Result", 1);
				logger.info("LogId:{}" + logname + "当前盒子正在诊断,请稍后再试", parameter.get("loguuid"));
			}
		} catch (Exception e) {
			if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
			redisClientTemplate.del(tracerouteKey);
			retMap.put("Result", 1);
			logger.error("LogId:{}" + logname + " Diagnostics exception " + e.getMessage(), parameter.get("loguuid"), e);
		}
		
		logger.info("LogId:{}" + logname + "业务诊断返回的结果集:{}总消耗时间:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - bt));
		logger.info("end invoke tracerouteStbDiagnose:{}", retMap);
		return retMap;
	}

	
	/**
	 * 获取PING诊断的参数地址(box)
	 * 
	 * @return
	 */
	private List<String> getBoxPingNodeList() {
		List<String> list = new ArrayList<String>();
		list.add(BoxConstantDiagnose.PING_DIAGNOSTICSSTATE);// 诊断数据的情况
		list.add(BoxConstantDiagnose.PING_SUCCESSCOUNT);// 最近的 ping 测试中成功 的次数
		list.add(BoxConstantDiagnose.PING_FAILURECOUNT);// 在最近的 ping 测试中失败 的次数
		list.add(BoxConstantDiagnose.PING_AVERAGERESPONSETIME);// 以毫秒为单位的最近一次 ping 测试所有成功响应的 平均时间
		list.add(BoxConstantDiagnose.PING_MINIMUMRESPONSETIME);// 以毫秒为单位的最近一次 ping 测试所有成功响应的 最短时间
		list.add(BoxConstantDiagnose.PING_MAXIMUMRESPONSETIME);// 以毫秒为单位的最近一次 ping 测试所有成功响应的 最长时间
		return list;
	}
	/**
	 * 得到获取盒子DHCP仿真节点
	 * 
	 * @return
	 */
	private List<String> getBoxTracerouteNodeList() {
		List<String> list = new ArrayList<>();
		list.add(BoxConstantDiagnose.TRACEROUTE_DIAGNOSTICSSTATE);// 诊断数据的情况
		list.add(BoxConstantDiagnose.TRACEROUTE_NUMBEROFROUTEHOPS);// 诊断状态
		list.add(BoxConstantDiagnose.TRACEROUTE_RESPONSETIME);// 诊断响应时间(单位：ms)
		return list;
	}
	/**
	 * 在不同的节点下组装设置参数(box ping)
	 * 
	 * @param parameter
	 *            参数集
	 * @param gi
	 *            机顶盒对象
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<ParameterValueStruct> setParameter(Map<String, Object> parameter, BoxInfo gi) {
		List<ParameterValueStruct> list = new ArrayList<>();
		ParameterValueStruct parameterDiagnosticsState = new ParameterValueStruct();
		parameterDiagnosticsState.setName(BoxConstantDiagnose.PING_DIAGNOSTICSSTATE);// 诊断数据的状态
		parameterDiagnosticsState.setValue("Requested");
		parameterDiagnosticsState.setValueType("string");
		parameterDiagnosticsState.setReadWrite(true);
		list.add(parameterDiagnosticsState);

		ParameterValueStruct parameterHost = new ParameterValueStruct();
		parameterHost.setName(BoxConstantDiagnose.PING_HOST);
		parameterHost.setValue(parameter.get("host") + "".trim());// PING的IP地址
		parameterHost.setValueType("string");
		parameterHost.setReadWrite(true);
		list.add(parameterHost);

		ParameterValueStruct parameterNumberOfRepetitions = new ParameterValueStruct();
		parameterNumberOfRepetitions.setName(BoxConstantDiagnose.PING_NUMBEROFREPETITIONS);
		parameterNumberOfRepetitions.setValue(parameter.get("numberOfRepetitions") + "".trim());// PING的次数
		parameterNumberOfRepetitions.setValueType("unsignedInt");
		parameterNumberOfRepetitions.setReadWrite(true);
		list.add(parameterNumberOfRepetitions);

		ParameterValueStruct parameterTimeout = new ParameterValueStruct();
		parameterTimeout.setName(BoxConstantDiagnose.PING_TIMEOUT);
		parameterTimeout.setValue(parameter.get("timeout") + "".trim());// 超时时间（毫秒）
		parameterTimeout.setValueType("unsignedInt");
		parameterTimeout.setReadWrite(true);
		list.add(parameterTimeout);

		ParameterValueStruct parameterDataBlockSize = new ParameterValueStruct();
		parameterDataBlockSize.setName(BoxConstantDiagnose.PING_DATABLOCKSIZE);
		parameterDataBlockSize.setValue(parameter.get("dataBlockSize") + "".trim());// 包大小（字节）
		parameterDataBlockSize.setValueType("unsignedInt");
		parameterDataBlockSize.setReadWrite(true);
		list.add(parameterDataBlockSize);

		ParameterValueStruct parameterDhcp = new ParameterValueStruct();
		parameterDhcp.setName(BoxConstantDiagnose.PING_DSCP);
		parameterDhcp.setValue(parameter.get("dscp"));// 测试包中用于 DiffServ 的 码点,默认值为 0.
		parameterDhcp.setValueType("string");
		parameterDhcp.setReadWrite(true);
		list.add(parameterDhcp);

		return list;
	}
	
	/**
	 * 获取机顶盒PING诊断结果
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
	public void getDiagnosticsResult(BoxInfo gi, List<String> node, Map<String, Object> rm, String logName, BoxInstructionMethodService boxims, Logger log, Map<String, Object> parameter) {
		long st = System.currentTimeMillis();
		String logid = parameter.get("loguuid").toString();
		String cpeid = parameter.get("CPEID").toString();
		Map<String, Object> result = boxims.getParameterValues(gi.getBoxMacaddress(), node);
		log.info("LogId:{}" + logName + "获取机顶盒诊断结果返回结果集:{}消耗时间:{}", logid, result, (System.currentTimeMillis() - st));
		if (null != result && result.size() > 0) {
			long fst = System.currentTimeMillis();
			result = BoxDiagnosticsUtil.getResultMapDate(result);// 获取返回封装数据
			redisClientTemplate.set(cpeid + STB_PING_RESULT_SUFFIX_KEY, JSON.toJSONString(result));
			rm.putAll(result);
			log.info("LogId:{}" + logName + "将诊断结果返回结果集:{}封装消耗时间:{}", logid, result, (System.currentTimeMillis() - fst));
		} else {
			log.info("LogId:{}" + logName + "获取机顶盒诊断信息失败", logid);
		}
	}
	/**
	 * 获取盒子诊断结果
	 * 
	 * @param gi
	 *            盒子对象
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
	 */
	private void getBoxDiagnosticsResult(BoxInfo gi, List<String> node, Map<String, Object> rm, String logName, BoxInstructionMethodService ims, Logger log, Map<String, Object> parameter) {
		long st = System.currentTimeMillis();
		String logid = parameter.get("loguuid").toString();
		String cpeId = parameter.get("CPEID").toString();
		Map<String, Object> result = ims.getParameterValues(gi.getBoxMacaddress(), node);
		log.info("LogId:{}" + logName + "获取盒子诊断结果返回结果集:{}消耗时间:{}", logid, result, (System.currentTimeMillis() - st));
		if (null != result && result.size() > 0) {
			long fst = System.currentTimeMillis();
			result = BoxDiagnosticsUtil.getResultMapDate(result);// 获取返回封装数据
			result.put("tdl", getTracerouteDiagnosticeList(gi, logid));
			rm.putAll(result);
			redisClientTemplate.set(cpeId + STB_TRACEROUTE_RESULT_SUFFIX_KEY, JSON.toJSONString(result));
			log.info("LogId:{}" + logName + "将诊断结果返回结果集:{}封装消耗时间:{}", logid, (System.currentTimeMillis() - fst), result);
		} else {
			BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取盒子诊断信息失败", "");
			log.info("LogId:{}" + logName + "获取盒子诊断信息失败", logid);
		}
	}
	/**
	 * 获取Box Traceroute诊断记录列表
	 * 
	 * @param gi
	 *            盒子对象
	 * @param logid
	 *            日志ID
	 * @param map
	 *            返回列表集合对象
	 */
	private List<Map<String, Object>> getTracerouteDiagnosticeList(BoxInfo gi, String logid) {
		Map<String, Object> result = boxInstructionMethodService.getParameterNames(gi.getBoxMacaddress(), BoxConstantDiagnose.TRACEROUTE_ROUTEHOPS, false);
		logger.info("LogId:{}获取用于表示被发现的路由对象,如果路由无法达到,则这个对象没有实例.获取数据为:{}", logid, result);
		List<Map<String, Object>> tdl = new ArrayList<Map<String, Object>>();
		if (null != result && result.size() > 0) {
			HashSet<String> nameList = getNamesList(result, Pattern.compile("Device.LAN.TraceRouteDiagnostics.RouteHops.[\\d]+.HopHost"));// 获取Traceroute诊断记录列表
			logger.info("LogId:{}根据正则获取节点查询数据数据为:{}", logid, nameList);
			Iterator<String> it = nameList.iterator();
			while (it.hasNext()) {
				String node = it.next();
				if (StringUtils.isNotBlank(node)) {
					Map<String, Object> resultlist = boxInstructionMethodService.getParameterValues(gi.getBoxMacaddress(), this.getChildrenNodePath(node));
					logger.info("LogId:{}路由路径上被发现的路由对象的主机名或地址数据:{}", logid, resultlist);
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
	
	private boolean validHguWanPath(Map<String,Object> rb, String path){
		boolean isHavePath = false;
		if(!StringUtils.isEmpty(path)){
			String pppoePath = "InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.WANPPPConnection.[\\d]+";
			String ipPath = "InternetGatewayDevice.WANDevice.[\\d]+.WANConnectionDevice.[\\d]+.WANIPConnection.[\\d]+";
			Pattern pattern = Pattern.compile(pppoePath);
			Pattern _pattern = Pattern.compile(ipPath);
			if(pattern.matcher(path).find() || _pattern.matcher(path).find()){
				isHavePath = true;
				rb.put("status", "2");
				rb.put("path", path);
			}
		}
		return isHavePath;
	}

	@Override
	public Map<String, Object> getStbPingInfo(Map<String, Object> parameter) {
		logger.info("start invoke getStbPingInfo:{}", parameter);
		Map<String,Object> retMap = commonResult(parameter);
		if(parameter.get("CPEID") == null || StringUtils.isBlank(parameter.get("CPEID").toString())){
			retMap.put("Result", -1);
			return retMap;
		}
		String cpeId = parameter.get("CPEID").toString();
		String pingState = redisClientTemplate.get(cpeId + STB_PING_LOCK_SUFFIX_KEY);
		if(pingState != null){
			retMap.put("Result", 1);
			return retMap;
		}
		String pingResult = redisClientTemplate.get(cpeId + STB_PING_RESULT_SUFFIX_KEY);
		if(StringUtils.isEmpty(pingResult)){
			retMap.put("Result", 2);
			return retMap;
		}
		Map<String,Object> pingResultData = JSON.parseObject(pingResult, Map.class);
		//转换结果参数名为文档定义的名称
		retMap.put("SuccessCount", pingResultData.get("SuccessCount"));
		retMap.put("FailureCount", pingResultData.get("FailureCount"));
		retMap.put("AverageRespTime", pingResultData.get("AverageResponseTime"));
		retMap.put("MinimumRespTime", pingResultData.get("MinimumResponseTime"));
		retMap.put("MaxRespTime", pingResultData.get("MaximumResponseTime"));
		logger.info("end invoke getStbPingInfo:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> getStbTracerouteInfo(Map<String, Object> parameter) {
		logger.info("start invoke getStbTracerouteInfo:{}", parameter);
		Map<String,Object> retMap = commonResult(parameter);
		if(parameter.get("CPEID") == null || StringUtils.isBlank(parameter.get("CPEID").toString())){
			retMap.put("Result", -1);
			return retMap;
		}
		String cpeId = parameter.get("CPEID").toString();
		String tracerouteState = redisClientTemplate.get(cpeId + STB_TRACEROUTE_LOCK_SUFFIX_KEY);
		if(tracerouteState != null){
			retMap.put("Result", 1);
			return retMap;
		}
		String tracerouteResult = redisClientTemplate.get(cpeId + STB_TRACEROUTE_RESULT_SUFFIX_KEY);
		if(StringUtils.isEmpty(tracerouteResult)){
			retMap.put("Result", 2);
			return retMap;
		}
		Map<String,Object> pingResultData = JSON.parseObject(tracerouteResult, Map.class);
		List<Map<String,Object>> hopsEntries = (List<Map<String, Object>>) pingResultData.get("tdl");
		//转换结果参数名为文档定义的名称
		retMap.put("ResponseTime", pingResultData.get("ResponseTime"));
		retMap.put("HopsNumberOfEntries", pingResultData.get("HopsNumberOfEntries"));
		retMap.put("HopsEntries", hopsEntries);
		logger.info("end invoke getStbTracerouteInfo:{}", retMap);
		return retMap;
	}
	//判断透传回来的结果
	private boolean isSuccess(Map<String,Object> rm){
		boolean isSucc = false;
		if(null != rm.get("result") && rm.get("result").toString().equals("0")){
			isSucc = true;
		}
		return isSucc;
	}
	//删除redis的锁
	public void delDiagnoseRedisKey(String lockKey, String diagKey){
		if (StringUtils.isNotBlank(lockKey)) redisClientTemplate.del(lockKey);// 删除Redis锁
		redisClientTemplate.del(diagKey);
	}
	/**
	 * 处理getParameterValue返回的节点value为null被过滤的情况
	 * @param list
	 * @return
	 */
	private List<Map<String,Object>> transferTracerouteResult(List<Map<String,Object>> list){
		if(list != null && list.size() > 0){
			for(int i=0;i<list.size();i++){
				Map<String,Object> m = list.get(i);
				if(null == m.get("HopHost")){
					m.put("HopHost ", "");
				}
				if(null == m.get("HopHostAddress")){
					m.put("HopHostAddress", "");
				}
				if(null == m.get("HopRTTimes")){
					m.put("HopRTTimes", "");
				}
			}
		}
		return list;
	}
}
