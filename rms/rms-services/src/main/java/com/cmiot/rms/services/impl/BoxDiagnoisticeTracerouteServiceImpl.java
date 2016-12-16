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
import com.cmiot.rms.services.boxManager.instruction.impl.BoxInstructionMethodServiceImpl;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.BoxDiagnosticsUtil;

@Service
public class BoxDiagnoisticeTracerouteServiceImpl implements BoxDiagnoisticeTracerouteService {

	private static Logger log = LoggerFactory.getLogger(BoxDiagnoisticeTracerouteServiceImpl.class);

	@Autowired
	BoxInstructionMethodServiceImpl boxInstructionMethodService;

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
	public Map<String, Object> tracerouteDiagnostics(Map<String, Object> parameter) {
		long bt = System.currentTimeMillis();
		String logname = "Traceroute";
		parameter.put("loguuid", UniqueUtil.uuid());
		log.info("LogId:{}" + logname + "获取仿真接口请求参数{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		String key = null;// Redis锁
		try {
			long cpst = System.currentTimeMillis();
			String[] parmlist = { "gatewayId", "userName", "roleName", "host", "timeout", "dataBlockSize", "dscp", "maxHopCount" };
			if (BoxDiagnosticsUtil.checkParm(parameter, rm, Arrays.asList(parmlist))) {
				log.info("LogId:{}" + logname + "验证参数消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - cpst));
				long qgist = System.currentTimeMillis();
				BoxInfo gi = boxInfoService.selectByPrimaryKey(parameter.get("gatewayId") + "");// 通过盒子ID获取盒子信息
				log.info("LogId:{}" + logname + "以盒子ID为条件查询盒子信息:{}消耗时间:{}", parameter.get("loguuid"), gi, (System.currentTimeMillis() - qgist));
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
								this.getDiagnosticsResult(gi, getNodeList(), rm, logname, boxInstructionMethodService, log, parameter.get("loguuid") + "");
							} else {
								BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "盒子诊断参数设置失败", "");
							}
							log.info("LogId:{}" + logname + "盒子诊断设值,获取诊断状态与获取诊断结果总消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - spt));
						} else {
							log.info("LogId:{}" + logname + "诊断指令已发送,为防止用户重复提交,添加临时对象锁，等待指令异步返回中", parameter.get("loguuid"));
							BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "盒子正在诊断中,请无重复提交", "");
						}
						if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
					} else {
						BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "当前盒子正在诊断,请稍后再试", "");
					}
				} else {
					BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取到盒子信息", "");
				}
			}
		} catch (Exception e) {
			if (StringUtils.isNotBlank(key)) redisClientTemplate.del(key);// 删除Redis锁
			BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_ERROR.code(), logname + "诊断仿真接口异常", "");
			log.error("LogId:{}" + logname + " Diagnostics exception " + e.getMessage(), parameter.get("loguuid"), e);
		}
		// BoxDiagnosticsUtil.saveOperationDiagnoseLog(parameter, rm, "盒子Traceroute诊断", logManagerService);
		log.info("LogId:{}" + logname + "业务诊断返回的结果集:{}总消耗时间:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - bt));
		return rm;
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
	private void getDiagnosticsResult(BoxInfo gi, List<String> node, Map<String, Object> rm, String logName, BoxInstructionMethodService ims, Logger log, String logid) {
		long st = System.currentTimeMillis();
		Map<String, Object> result = ims.getParameterValues(gi.getBoxMacaddress(), node);
		log.info("LogId:{}" + logName + "获取盒子诊断结果返回结果集:{}消耗时间:{}", logid, result, (System.currentTimeMillis() - st));
		if (null != result && result.size() > 0) {
			long fst = System.currentTimeMillis();
			result = BoxDiagnosticsUtil.getResultMapDate(result);// 获取返回封装数据
			result.put("tdl", getTracerouteDiagnosticeList(gi, logid));
			BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), result);
			log.info("LogId:{}" + logName + "将诊断结果返回结果集:{}封装消耗时间:{}", logid, (System.currentTimeMillis() - fst), result);
		} else {
			BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取盒子诊断信息失败", "");
		}
	}

	/**
	 * 获取Traceroute诊断记录列表
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
		log.info("LogId:{}获取用于表示被发现的路由对象,如果路由无法达到,则这个对象没有实例.获取数据为:{}", logid, result);
		List<Map<String, Object>> tdl = new ArrayList<Map<String, Object>>();
		if (null != result && result.size() > 0) {
			HashSet<String> nameList = getNamesList(result, Pattern.compile("Device.LAN.TraceRouteDiagnostics.RouteHops.[\\d]+.HopHost"));// 获取Traceroute诊断记录列表
			log.info("LogId:{}根据正则获取节点查询数据数据为:{}", logid, nameList);
			Iterator<String> it = nameList.iterator();
			while (it.hasNext()) {
				String node = it.next();
				if (StringUtils.isNotBlank(node)) {
					Map<String, Object> resultlist = boxInstructionMethodService.getParameterValues(gi.getBoxMacaddress(), this.getChildrenNodePath(node));
					log.info("LogId:{}路由路径上被发现的路由对象的主机名或地址数据:{}", logid, resultlist);
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
		return list;
	}

	/**
	 * 获取盒子节点名集合
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
	 *            盒子对象
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<ParameterValueStruct> setParameter(Map<String, Object> parameter, BoxInfo gi) {
		List<ParameterValueStruct> list = new ArrayList<>();
		// 诊断状态
		ParameterValueStruct traceroutedspvs = new ParameterValueStruct();
		traceroutedspvs.setName(BoxConstantDiagnose.TRACEROUTE_DIAGNOSTICSSTATE);
		traceroutedspvs.setValue("Requested");
		traceroutedspvs.setReadWrite(true);
		traceroutedspvs.setValueType("string");
		list.add(traceroutedspvs);

		// Traceroute测试的主机名或主机地址
		ParameterValueStruct host = new ParameterValueStruct();
		host.setName(BoxConstantDiagnose.TRACEROUTE_HOST);
		host.setValue(parameter.get("host"));
		host.setReadWrite(true);
		host.setValueType("string");
		list.add(host);

		// 诊断超时时间(单位：ms)
		ParameterValueStruct timeout = new ParameterValueStruct();
		timeout.setName(BoxConstantDiagnose.TRACEROUTE_TIMEOUT);
		timeout.setValue(parameter.get("timeout"));
		timeout.setReadWrite(true);
		timeout.setValueType("unsignedInt");
		list.add(timeout);

		// 每个Traceroute包发送的数据块大小（单位：字节）
		ParameterValueStruct dataBlockSize = new ParameterValueStruct();
		dataBlockSize.setName(BoxConstantDiagnose.TRACEROUTE_DATABLOCKSIZE);
		dataBlockSize.setValue(parameter.get("dataBlockSize"));
		dataBlockSize.setReadWrite(true);
		dataBlockSize.setValueType("unsignedInt");
		list.add(dataBlockSize);

		// 最大跳数
		ParameterValueStruct maxHopCount = new ParameterValueStruct();
		maxHopCount.setName(BoxConstantDiagnose.TRACEROUTE_MAXHOPCOUNT);
		maxHopCount.setValue(parameter.get("maxHopCount"));
		maxHopCount.setReadWrite(true);
		maxHopCount.setValueType("unsignedInt");
		list.add(maxHopCount);

		// 用来测试包的DSCP值
		ParameterValueStruct dscp = new ParameterValueStruct();
		dscp.setName(BoxConstantDiagnose.TRACEROUTE_DSCP);
		dscp.setValue(parameter.get("dscp"));
		dscp.setReadWrite(true);
		dscp.setValueType("unsignedInt");
		list.add(dscp);

		return list;
	}

	/**
	 * 得到获取盒子DHCP仿真节点
	 * 
	 * @return
	 */
	private List<String> getNodeList() {
		List<String> list = new ArrayList<>();
		list.add(BoxConstantDiagnose.TRACEROUTE_DIAGNOSTICSSTATE);// 诊断数据的情况
		list.add(BoxConstantDiagnose.TRACEROUTE_NUMBEROFROUTEHOPS);// 诊断状态
		list.add(BoxConstantDiagnose.TRACEROUTE_RESPONSETIME);// 诊断响应时间(单位：ms)
		return list;
	}
}
