package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.rpc.RpcContext;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.services.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.constant.BoxConstantDiagnose;
import com.cmiot.rms.common.enums.RebootEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.DiagnoseLogMapper;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.services.boxManager.instruction.BoxInstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.BoxDiagnosticsUtil;

@Service
public class BoxDiagnoisticePingServiceImpl implements BoxDiagnoisticePingService {

	private static Logger log = LoggerFactory.getLogger(BoxDiagnoisticePingServiceImpl.class);

	@Autowired
	BoxInstructionMethodService boxInstructionMethodService;

	@Autowired
	private RedisClientTemplate redisClientTemplate;

	@Autowired
	BoxInfoService boxInfoService;

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

	@Value("${dubbo.provider.port}")
	int providerPort;

	@Override
	public Map<String, Object> pingDiagnostics(Map<String, Object> parameter) {
		long bt = System.currentTimeMillis();
		String logname = "Ping";
		parameter.put("loguuid", UniqueUtil.uuid());
		log.info("LogId:{}" + logname + "获取仿真接口请求参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		String key = null;// Redis锁
		try {
			long cpst = System.currentTimeMillis();
			String[] parmlist = { "gatewayId", "host", "dataBlockSize", "numberOfRepetitions", "timeout", "dscp", "userName", "roleName" };
			if (BoxDiagnosticsUtil.checkParm(parameter, rm, Arrays.asList(parmlist))) {
				log.info("LogId:{}" + logname + "验证参数消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - cpst));
				long qgist = System.currentTimeMillis();
				BoxInfo gi = boxInfoService.selectByPrimaryKey(parameter.get("gatewayId") + "");// 通过机顶盒ID获取机顶盒信息
				log.info("LogId:{}" + logname + "以机顶盒ID为条件查询机顶盒信息:{}消耗时间:{}", parameter.get("loguuid"), gi, (System.currentTimeMillis() - qgist));
				if (null != gi) {
					key = gi.getBoxMacaddress() + "_box_ping";
					// nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在时才进行set，如果取XX，则只有当key已经存在时才进行set;expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
					String ls = redisClientTemplate.set(key, RebootEnum.STATUS_0.code(), "NX", "EX", (null != redisTime && redisTime > 0 ? redisTime : 2 * 60));
					if (StringUtils.isNotBlank(ls)) {
						if (null == RequestCache.get("box_diagnostics_" + gi.getBoxMacaddress())) {
							long spt = System.currentTimeMillis();

							//保存当前诊断调用方地址，供ACS回调使用
							String providerUrl = "dubbo://" + RpcContext.getContext().getLocalHost() + ":" + providerPort + "/" + BoxInterfaceService.class.getName();
							redisClientTemplate.set(gi.getBoxFactoryCode() + Constant.SEPARATOR + gi.getBoxSerialnumber() + Constant.SEPARATOR + "diagnose", providerUrl);

							if (BoxDiagnosticsUtil.settingPingParameterAndRequestGateway(parameter, gi, boxInstructionMethodService, this.setParameter(parameter, gi))) {
								BoxDiagnosticsUtil.temporaryObjectLock(waitTime, gi, log, logname, parameter.get("loguuid") + "");// 临时对象锁,为等待8 DIAGNOSTICS COMPLETE状态唤醒
								BoxDiagnosticsUtil.getDiagnosticsResult(gi, getNodeList(), rm, logname, boxInstructionMethodService, log, parameter.get("loguuid") + "");
							} else {
								BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "机顶盒诊断参数设置失败", "");
							}
							log.info("LogId:{}" + logname + "机顶盒诊断设值,获取诊断状态与获取诊断结果总消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - spt));
						} else {
							log.info("LogId:{}" + logname + "诊断指令已发送,为防止用户重复提交,添加临时对象锁，等待指令异步返回中", parameter.get("loguuid"));
							BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "机顶盒正在诊断中,请无重复提交", "");
						}
						if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
					} else {
						BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "当前机顶盒正在诊断,请稍后再试", "");
					}
				} else {
					BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到机顶盒信息", "");
				}
			}
		} catch (Exception e) {
			if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
			BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_ERROR.code(), logname + "诊断仿真接口异常", "");
			log.error("LogId:{}" + logname + " Diagnostics exception " + e.getMessage(), parameter.get("loguuid"), e);
		}
		// BoxDiagnosticsUtil.saveOperationDiagnoseLog(parameter, rm, "BoxPing诊断", logManagerService);
		log.info("LogId:{}" + logname + "业务诊断返回的结果集:{}总消耗时间:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - bt));
		return rm;
	}

	/**
	 * 在不同的节点下组装设置参数
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
	 * 获取PING诊断的参数地址
	 * 
	 * @return
	 */
	private List<String> getNodeList() {
		List<String> list = new ArrayList<String>();
		list.add(BoxConstantDiagnose.PING_DIAGNOSTICSSTATE);// 诊断数据的情况
		list.add(BoxConstantDiagnose.PING_SUCCESSCOUNT);// 最近的 ping 测试中成功 的次数
		list.add(BoxConstantDiagnose.PING_FAILURECOUNT);// 在最近的 ping 测试中失败 的次数
		list.add(BoxConstantDiagnose.PING_AVERAGERESPONSETIME);// 以毫秒为单位的最近一次 ping 测试所有成功响应的 平均时间
		list.add(BoxConstantDiagnose.PING_MINIMUMRESPONSETIME);// 以毫秒为单位的最近一次 ping 测试所有成功响应的 最短时间
		list.add(BoxConstantDiagnose.PING_MAXIMUMRESPONSETIME);// 以毫秒为单位的最近一次 ping 测试所有成功响应的 最长时间
		return list;
	}
}
