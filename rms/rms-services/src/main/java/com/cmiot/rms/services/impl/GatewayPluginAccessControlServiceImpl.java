package com.cmiot.rms.services.impl;

import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.RebootEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.GatewayPluginAccessControlService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GatewayPluginAccessControlServiceImpl implements GatewayPluginAccessControlService {

	@Autowired
	InstructionMethodService instructionMethodService;

	@Autowired
	private RedisClientTemplate redisClientTemplate;

	@Autowired
	GatewayInfoService gatewayInfoService;

	@Value("${plugin.redis.lock.time}")
	String pluginRedisTime;

	private static Logger log = LoggerFactory.getLogger(GatewayPluginAccessControlServiceImpl.class);

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> addPluginApi(Map<String, Object> parameter) {
		parameter.put("loguuid", UniqueUtil.uuid());
		long st = System.currentTimeMillis();
		log.info("LogId:{}添加插件传入参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();// 返回结果集
		try {
			if (checkParm(parameter, rm, Arrays.asList(new String[] { Constant.MAC, "addType" }))) {
				List<Map<String, Object>> cslist = (List<Map<String, Object>>) parameter.get("capabilitySet");// 插件集合
				List<String> lostUrlPath = new ArrayList<String>();// 保存插件名称节点为了防止API插件安装失败后做删除处理
				switch (parameter.get("addType") + "") {
				case "node":// 节点添加
					if (null != cslist && cslist.size() > 0) {
						List<ParameterValueStruct> dulist = new ArrayList<ParameterValueStruct>();
						List<ParameterValueStruct> apilist = new ArrayList<ParameterValueStruct>();
						for (Map<String, Object> cs : cslist) {
							String duName = cs.get("duName") + "";// 服务类名
							String duUrl = cs.get("duUrl") + "";// 服务类名URL
							List<Map<String, Object>> nodeApiList = (List<Map<String, Object>>) cs.get("nodeApiList");// API功能集
							if (StringUtils.isNotBlank(duName) && nodeApiList.size() > 0 && StringUtils.isNotBlank(duUrl)) {
								lostUrlPath.add(duUrl);
								ParameterValueStruct dupvs = new ParameterValueStruct();
								dupvs.setName(duUrl);
								dupvs.setValue(duName);
								dupvs.setReadWrite(true);
								dupvs.setValueType("string");
								dulist.add(dupvs);
								for (Map<String, Object> api : nodeApiList) {
									ParameterValueStruct apipvs = new ParameterValueStruct();
									String apiUrl = api.get("apiUrl") + "";// API URL
									String apiName = api.get("apiName") + "";// API name
									apipvs.setName(apiUrl);
									apipvs.setValue(apiName);
									apipvs.setReadWrite(true);
									apipvs.setValueType("string");
									apilist.add(apipvs);
								}
							}
						}
						Boolean backDuResult = false, backApiResult = false;
						long dut = System.currentTimeMillis();
						if (dulist.size() > 0) backDuResult = instructionMethodService.setParameterValue(parameter.get(Constant.MAC) + "", dulist);// 网关设置服务类名
						log.info("LogId:{}node方式网关上批量安装插件名称(duName)数据:{}安装后返回的状态:{}耗时:{}", parameter.get("loguuid"), dulist, backDuResult, (System.currentTimeMillis() - dut));
						long aut = System.currentTimeMillis();
						if (apilist.size() > 0 && backDuResult) backApiResult = instructionMethodService.setParameterValue(parameter.get(Constant.MAC) + "", apilist);// 网关设置API
						log.info("LogId:{}node方式网关上批量安装插件Api数据:{}安装后返回的状态:{}耗时:{}", parameter.get("loguuid"), apilist, backApiResult, (System.currentTimeMillis() - aut));
						if (!backDuResult) deleteDuName(parameter, lostUrlPath);// 失败删除安装的插件名称
						packageBackInfo(rm, backDuResult && backApiResult ? RespCodeEnum.RC_0.code() : RespCodeEnum.RC_1.code(), "批量网关设置服务类名" + (backDuResult ? "成功" : "失败") + ",批量网关设置API" + (backApiResult ? "成功" : "失败"), "");
					} else {
						packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取插件集合数据", "");
					}
					break;
				case "free":// 网关自动创建节点添加
					if (null != cslist && cslist.size() > 0) {
						List<ParameterValueStruct> dulistpvs = new ArrayList<ParameterValueStruct>();
						List<ParameterValueStruct> apilistpvs = new ArrayList<ParameterValueStruct>();
						for (Map<String, Object> cs : cslist) {
							String duName = cs.get("duName") + "";// 服务类名
							List<String> freeApiList = (List<String>) cs.get("freeApiList");// API功能集
							if (null != duName && null != freeApiList && StringUtils.isNotBlank(duName) && freeApiList.size() > 0) {
								lostUrlPath.add(duName);
								int rai = instructionMethodService.AddObject(parameter.get(Constant.MAC) + "", "InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.", System.currentTimeMillis() + "");
								if (rai > 0) {
									ParameterValueStruct pvs = new ParameterValueStruct();
									pvs.setName("InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission." + rai + ".DUName");
									pvs.setValue(duName);
									pvs.setReadWrite(true);
									pvs.setValueType("string");
									dulistpvs.add(pvs);
									for (String api : freeApiList) {
										int aiprai = instructionMethodService.AddObject(parameter.get(Constant.MAC) + "", "InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission." + rai + ".API.", System.currentTimeMillis() + "");
										if (aiprai > 0) {
											ParameterValueStruct apipvs = new ParameterValueStruct();
											apipvs.setName("InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission." + rai + ".API." + aiprai + ".Name");
											apipvs.setValue(api);
											apipvs.setReadWrite(true);
											apipvs.setValueType("string");
											apilistpvs.add(apipvs);
										}
									}
								}
							}
						}
						Boolean backDuResult = false, backApiResult = false;
						long dut = System.currentTimeMillis();
						if (dulistpvs.size() > 0) backDuResult = instructionMethodService.setParameterValue(parameter.get(Constant.MAC) + "", dulistpvs);
						log.info("LogId:{}free方式网关上批量安装插件名称(duName)数据:{}安装后返回的状态:{}耗时:{}", parameter.get("loguuid"), dulistpvs, backDuResult, (System.currentTimeMillis() - dut));
						long aut = System.currentTimeMillis();
						if (apilistpvs.size() > 0 && backDuResult) backApiResult = instructionMethodService.setParameterValue(parameter.get(Constant.MAC) + "", apilistpvs);
						log.info("LogId:{}free方式网关上批量安装插件Api数据:{}安装后返回的状态:{}耗时:{}", parameter.get("loguuid"), apilistpvs, backApiResult, (System.currentTimeMillis() - aut));
						if (!backDuResult) deleteDuName(parameter, lostUrlPath);// 失败删除安装的插件名称
						packageBackInfo(rm, backDuResult && backApiResult ? RespCodeEnum.RC_0.code() : RespCodeEnum.RC_1.code(), "批量网关设置服务类名" + (backDuResult ? "成功" : "失败") + ",批量网关设置API" + (backApiResult ? "成功" : "失败"), "");

					} else {
						packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获取插件集合数据", "");
					}
					break;
				default:
					packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取添加方式失败", "");
					break;
				}
			}
		} catch (Exception e) {
			packageBackInfo(rm, RespCodeEnum.RC_1.code(), "添加插件功能异常", "");
			log.error("LogId:{}添加插件功能异常：" + e.getMessage(), parameter.get("loguuid"), e);
		}
		log.info("LogId:{}添加插件返回数据:{}总耗时:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - st));
		return rm;
	}

	private void deleteDuName(Map<String, Object> parameter, List<String> lostUrlPath) throws Exception {
		if (null != lostUrlPath && lostUrlPath.size() > 0) {
			for (String duName : lostUrlPath) {
				duName = duName.substring(0, duName.lastIndexOf(".") + 1);
				long st = System.currentTimeMillis();
				int rb = instructionMethodService.DeleteObject(parameter.get(Constant.MAC) + "", duName, System.currentTimeMillis() + "");
				log.info("LogId:{}网关上批量安装插件名称失败删除数据:{}删除返回的状态:{}耗时:{}", parameter.get("loguuid"), duName, rb, (System.currentTimeMillis() - st));
			}
		}
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
	public Boolean checkParm(Map<String, Object> parameter, Map<String, Object> rm, List<String> parmlist) {
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

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> delPluginApi(Map<String, Object> parameter) {
		parameter.put("loguuid", UniqueUtil.uuid());
		long st = System.currentTimeMillis();
		log.info("LogId:{}删除插件传入参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();// 返回结果集
		try {
			if (checkParm(parameter, rm, Arrays.asList(new String[] { Constant.MAC, "delType" }))) {
				List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
				switch (parameter.get("delType") + "") {
				case "node":// 通过节点方式
					List<String> dels = (List<String>) parameter.get("delUrl");
					log.info("LogId:{}node方式删除的数据集:{}", parameter.get("loguuid"), dels);
					if (null != dels && dels.size() > 0) {
						int error = 0;
						for (String del : dels) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("delUrl", del);
							long dst = System.currentTimeMillis();
							int rbn = instructionMethodService.DeleteObject(parameter.get(Constant.MAC) + "", del, System.currentTimeMillis() + "");
							if (rbn != 0) {
								error++;
							}
							log.info("LogId:{}node方式循环删除的单节点路径:{},目前删除失败的数量:{}耗时:{}", parameter.get("loguuid"), del, error, (System.currentTimeMillis() - dst));
							map.put("status", rbn);
							result.add(map);
						}
						packageBackInfo(rm, error == 0 ? RespCodeEnum.RC_0.code() : RespCodeEnum.RC_1.code(), "删除插件总数:" + result.size() + ",失败条数:", result);
					} else {
						packageBackInfo(rm, RespCodeEnum.RC_1.code(), "未获到到要删除插件信息", "");
					}
					break;
				case "all":// 删除服务名下全部
					String duName = (null != parameter.get("duName") && StringUtils.isNotBlank(parameter.get("duName") + "") ? parameter.get("duName") + "" : null);
					if (StringUtils.isNotBlank(duName)) {
						long rbt = System.currentTimeMillis();
						List<String> rblist = getXCmccDuPermission(parameter, rm);// 获取插件权限控制节点路径
						log.info("LogId:{}all方式删除,查询网关插件节点数据:{}耗时:{}", parameter.get("loguuid"), rblist, (System.currentTimeMillis() - rbt));
						if (null != rblist && rblist.size() > 0) {
							long rbtv = System.currentTimeMillis();
							Map<String, Object> rbmap = instructionMethodService.getParameterValues(parameter.get(Constant.MAC) + "", rblist);
							log.info("LogId:{}all方式删除,查询网关插件名称数据:{}耗时:{}", parameter.get("loguuid"), rbmap, (System.currentTimeMillis() - rbtv));
							if (null != rbmap && rbmap.size() > 0) {
								List<String> delNodeLidt = new ArrayList<String>();
								for (Map.Entry<String, Object> entry : rbmap.entrySet()) {
									String value = entry.getValue() + "";
									if (StringUtils.isNotBlank(value) && duName.trim().equals(value.trim())) {
										delNodeLidt.add(entry.getKey());
									}
								}
								log.info("LogId:{}all方式删除的数据集:{}", parameter.get("loguuid"), delNodeLidt);
								if (null != delNodeLidt && delNodeLidt.size() > 0) {
									int error = 0;
									for (String del : delNodeLidt) {
										String node = del.substring(0, del.lastIndexOf(".") + 1);
										if (StringUtils.isNotBlank(node)) {
											Map<String, Object> map = new HashMap<String, Object>();
											map.put("delUrl", del);
											long dst = System.currentTimeMillis();
											int rbn = instructionMethodService.DeleteObject(parameter.get(Constant.MAC) + "", node, System.currentTimeMillis() + "");
											if (rbn != 0) {
												error++;
											}
											log.info("LogId:{}all方式循环删除的单节点路径:{},目前删除失败的数量:{}耗时:{}", parameter.get("loguuid"), del, error, (System.currentTimeMillis() - dst));
											map.put("status", rbn);
											result.add(map);
										}
									}
									packageBackInfo(rm, error == 0 ? RespCodeEnum.RC_0.code() : RespCodeEnum.RC_1.code(), "删除插件总数:" + result.size() + ",失败条数:", result);
								} else {
									packageBackInfo(rm, "2", "删除的插件名在网关未查询到数据或网关无此插件", "");// 状态2是与BMS确认返回值,BMS认为网关上无此插件
								}
							} else {
								packageBackInfo(rm, "2", "网关未返回插件节点数据或网关无此插件", "");
							}
						} else {
							packageBackInfo(rm, "2", "网关未返回插件节点路径数据或网关无此插件", "");
						}
					} else {
						packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取插件名称失败", "");
					}
					break;
				default:
					packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取添删除方式失败", "");
					break;
				}
			}
		} catch (Exception e) {
			packageBackInfo(rm, RespCodeEnum.RC_1.code(), "删除插件功能异常", "");
			log.error("delPluginApi Exception：" + e.getMessage(), e);
		}
		log.info("LogId:{}删除插件返回数据:{}总耗时:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - st));
		return rm;
	}

	@Override
	public Map<String, Object> queryPluginExecEnv(Map<String, Object> params) {
		String mac = "" + params.get(Constant.MAC);
		if (null == params || params.size() == 0 || StringUtils.isBlank(mac)) {
			return Collections.emptyMap();
		}

		Map<String, Object> retMap = new HashMap<>();// 返回结果集
		Map<String, Object> names = instructionMethodService.getParameterNames(mac, "InternetGatewayDevice.SoftwareModules.ExecEnv.", true);
		if (names == null || names.size() < 2) {
			// 实际必须有两个环境:jvm,osgi
			retMap.put("resultCode", -1);
			retMap.put("resultMsg", "插件运行环境至少需含有jvm和osgi");
			log.warn("插件运行环境至少需含有jvm和osgi:" + mac);
			return retMap;
		}

		List<String> fullNames = new ArrayList<>();
		for (String name : names.keySet()) {
			fullNames.add(name + "Name");
			fullNames.add(name + "Vendor");
			fullNames.add(name + "Version");
			fullNames.add(name + "ParentExecEnv");
			fullNames.add(name + "Enable");
		}

		// 获取网关的基本信息
		String customiseName = "InternetGatewayDevice.DeviceInfo.X_CMCC_CustomiseName";
		String cpuClass = "InternetGatewayDevice.DeviceInfo.X_CMCC_CPUClass";
		String upTime = "InternetGatewayDevice.DeviceInfo.UpTime";
		String ponUpTime = "InternetGatewayDevice.DeviceInfo.X_CMCC_PONUpTime";

		fullNames.add(customiseName);
		fullNames.add(cpuClass);
		fullNames.add(upTime);
		fullNames.add(ponUpTime);

		Map<String, Object> values = instructionMethodService.getParameterValues(mac, fullNames);
		Map<String, Object> data = new HashMap<>();

//      String osgiVersion = null;
//      String jvmVersion = null;
		for (String name : names.keySet()) {
			//忽略不可用的
			boolean enable = (boolean) values.get(name + "Enable");
			if (!enable) {
				continue;
			}

			if (((String) (values.get(name + "Name"))).equalsIgnoreCase("OSGI") || values.get(name + "ParentExecEnv") != null) {
				Map<String, Object> osgiEnv = new HashMap<>();
				osgiEnv.put("name", values.get(name + "Name"));
				osgiEnv.put("vendor", values.get(name + "Vendor"));
				osgiEnv.put("version", values.get(name + "Version"));
				data.put("osgi", osgiEnv);
//              osgiVersion = (String) values.get(name + "Version");
			}

			if (((String) (values.get(name + "Name"))).equalsIgnoreCase("JVM") || values.get(name + "ParentExecEnv") == null) {
				Map<String, Object> jvmEnv = new HashMap<>();
				jvmEnv.put("name", values.get(name + "Name"));
				jvmEnv.put("vendor", values.get(name + "Vendor"));
				jvmEnv.put("version", values.get(name + "Version"));
				data.put("jvm", jvmEnv);
//              jvmVersion = (String) values.get(name + "Version");
			}
		}

		String gatewayName = null != values.get(customiseName) ? values.get(customiseName).toString() : "";
		data.put("customiseName", gatewayName);
		data.put("cpuClass", null != values.get(cpuClass) ? values.get(cpuClass).toString() : "");
		data.put("upTime", null != values.get(upTime) ? values.get(upTime).toString() : "");
		data.put("ponUpTime", null != values.get(ponUpTime) ? values.get(ponUpTime).toString() : "");

		// 将网关名称更新到数据库中
		GatewayInfo searchInfo = new GatewayInfo();
		searchInfo.setGatewayMacaddress(mac);
		GatewayInfo info = gatewayInfoService.selectGatewayInfo(searchInfo);
		if (null != info) {
			GatewayInfo update = new GatewayInfo();
			update.setGatewayUuid(info.getGatewayUuid());
			update.setGatewayName(gatewayName);
//          update.setOsgi(osgiVersion);
//          update.setJvm(jvmVersion);
			gatewayInfoService.updateSelectGatewayInfo(update);
		}

		retMap.put("data", data);
		retMap.put("resultCode", 0);

		return retMap;
	}

	@Override
	public Map<String, Object> queryPluginApiList(Map<String, Object> map) {
		map.put("loguuid", UniqueUtil.uuid());
		long st = System.currentTimeMillis();
		log.info("LogId:{}查询插件权限控制权限列表传入参数:{}", map.get("loguuid"), map);
		Map<String, Object> retMap = new HashMap<>();// 返回结果集
		try {
			long stl = System.currentTimeMillis();
			List<String> namesList = getXCmccDuPermission(map, retMap);// 获取插件权限控制节点名
			log.info("LogId:{}获取插件权限控制地址集数据:{}耗时:{}", map.get("loguuid"), namesList, (System.currentTimeMillis() - stl));
			List<Map<String, Object>> dumaplist = new ArrayList<Map<String, Object>>();// 返回列表
			if (null != namesList && namesList.size() > 0) {
				// for (String path : namesList) {
				long rst = System.currentTimeMillis();
				Map<String, Object> rsn = instructionMethodService.getParameterValues(map.get(Constant.MAC) + "", namesList);// 查询服务类名
				log.info("LogId:{}获取插名称数据:{}耗时:{}", map.get("loguuid"), rsn, (System.currentTimeMillis() - rst));
				if (null != rsn && rsn.size() > 0) {
					for (Map.Entry<String, Object> entry : rsn.entrySet()) {
						Map<String, Object> dumap = new HashMap<String, Object>();
						String key = entry.getKey();
						String value = entry.getValue() + "";
						List<Map<String, String>> apin = new ArrayList<Map<String, String>>();
						long rast = System.currentTimeMillis();
						Map<String, Object> ran = instructionMethodService.getParameterNames(map.get(Constant.MAC) + "", key.substring(0, key.lastIndexOf(".") + 1) + "API.", false);// 获取参数名称
						log.info("LogId:{}查询插件API节点据:{}耗时:{}", map.get("loguuid"), ran, (System.currentTimeMillis() - rast));
						if (null != ran && ran.size() > 0) {
							List<String> rapinlist = getNamesList(ran, Pattern.compile("InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.[\\d]+.API.[\\d]+.Name"));// 获取节点下权限
							log.info("LogId:{}获取插件API名称节点数据:{}耗时:{}", map.get("loguuid"), rapinlist);
							if (null != rapinlist && rapinlist.size() > 0) {
								long ranst = System.currentTimeMillis();
								Map<String, Object> rapis = instructionMethodService.getParameterValues(map.get(Constant.MAC) + "", rapinlist);// 查询服务类名
								log.info("LogId:{}获取插件API节点名称数据:{}耗时:{}", map.get("loguuid"), rapis, (System.currentTimeMillis() - ranst));
								if (null != rapis && rapis.size() > 0) {
									for (Map.Entry<String, Object> api : rapis.entrySet()) {
										Map<String, String> apimap = new HashMap<>();
										String apikey = api.getKey();
										String apivalue = api.getValue() + "";
										apimap.put("aipUrl", apikey);
										apimap.put("aipName", apivalue);
										apin.add(apimap);
									}
								}
							}
						}
						dumap.put("duUrl", key);// 插件服务类名节点
						dumap.put("duName", value);// 插件服务类名称
						dumap.put("apiList", apin);
						dumaplist.add(dumap);
					}
				}
			}
			packageBackInfo(retMap, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), dumaplist);
		} catch (Exception e) {
			packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "查询插件权限控制权限列表功能异常", "");
			log.error("查询插件权限控制权限列表功能异常：" + e.getMessage(), e);
		}
		log.info("LogId:{}查询插件权限控制权限列表返回数据:{}总耗时:{}", map.get("loguuid"), retMap, (System.currentTimeMillis() - st));
		return retMap;
	}

	@Override
	public Map<String, Object> queryPluginCapabilitySet(Map<String, Object> map) {
		map.put("loguuid", UniqueUtil.uuid());
		long st = System.currentTimeMillis();
		log.info("LogId:{}查询插件运行环境权限控制能力集接口传入参数:{}", map.get("loguuid"), map);
		Map<String, Object> retMap = new HashMap<>();// 返回结果集
		try {
			List<String> pathList = new ArrayList<>();
			long stl = System.currentTimeMillis();
			getPackagePlugin(map, retMap, pathList, "X_CMCC_APICapabilites.");// 获取插件SOGI类型下的地址节点
			log.info("LogId:{}获取插件SOGI类型下的地址节点数据:{}耗时:{}", map.get("loguuid"), pathList, (System.currentTimeMillis() - stl));
			if (null != pathList && pathList.size() > 0) {
				List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
				for (String path : pathList) {
					Map<String, Object> result = instructionMethodService.getParameterNames(map.get(Constant.MAC) + "", path, false);// 获取参数名称
					log.debug("LogId:{}查询:{}地址返回的参数集:{}", map.get("loguuid"), path, result);
					if (null == result || result.size() <= 0) {
						packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "查询插件运行环境权限控制能力集数据,网关返回结果为空", "");
						return retMap;
					}
					Pattern patternService = Pattern.compile(path + "[\\d]+.Name");// 权限名称,权限粒度控制为API服务类名。取值为服务类全名。
					List<String> namesList = getNamesList(result, patternService);// 获取网关节点名集合
					if (null == namesList || namesList.size() <= 0) {
						packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "未获插件运行环境权限控制能力集权限名称,权限粒度与取值", "");
						return retMap;
					}
					result = instructionMethodService.getParameterValues(map.get(Constant.MAC) + "", namesList);// 获取参数值
					log.info("LogId:{}获取节点:{}数据:{}耗时:{}", map.get("loguuid"), namesList, result, (System.currentTimeMillis() - stl));
					if (null == result || result.size() <= 0) {
						packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "未获插件运行环境权限控制能力集权限取值", "");
						return retMap;
					}
					for (Map.Entry<String, Object> entry : result.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue() + "";
						if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
							Map<String, Object> re = new HashMap<>();// 返回结果集
							re.put("path", key);
							re.put("name", value);
							resultList.add(re);
						}
					}
				}
				packageBackInfo(retMap, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), resultList);
			} else {
				this.packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "未获取插件OSGi运行环境节点下权限控制能力集地址", "");
			}
		} catch (Exception e) {
			packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "查询插件运行环境权限控制能力集功能异常", "");
			log.error("LogId:{}查询插件运行环境权限控制能力集功能异常：" + e.getMessage(), map.get("loguuid"), e);
		}
		log.info("LogId:{}查询插件运行环境权限控制能力集接返回数据:{}总耗时:{}", map.get("loguuid"), retMap, (System.currentTimeMillis() - st));
		return retMap;
	}

	/**
	 * 获取插件权限控制节点路径
	 * 
	 * @param map
	 *            参数
	 * @param retMap
	 *            返回对象
	 * @return
	 */
	private List<String> getXCmccDuPermission(Map<String, Object> map, Map<String, Object> retMap) {
		List<String> namesList = null;
		if (null != map && map.size() > 0 && StringUtils.isNotBlank(map.get(Constant.MAC) + "")) {
			Map<String, Object> result = instructionMethodService.getParameterNames(map.get(Constant.MAC) + "", "InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.", false);// 获取参数名称
			log.debug("LogId:{}查询InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.返回的{}", map.get("loguuid"), result);
			if (null != result && result.size() > 0) {
				Pattern patternService = Pattern.compile("InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.[\\d]+.DUName");// 查询插件权限控制节点路径
				namesList = getNamesList(result, patternService);// 获取网关节点名集合
			} else {
				packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "网关不支持插件列表查询", "");
			}
		} else {
			packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "请求参数不全", "");
		}
		return namesList;
	}

	/**
	 * 获取插件SOGI类型下的地址节点
	 * 
	 * @param map
	 *            参数集
	 * @param retMap
	 *            返回对象集
	 * @param pathList
	 *            节点路径集
	 * @param nodeName
	 *            节点名称
	 * @return
	 * @throws Exception
	 */
	private void getPackagePlugin(Map<String, Object> map, Map<String, Object> retMap, List<String> pathList, String nodeName) throws Exception {
		if (null != map && map.size() > 0 && StringUtils.isNotBlank(map.get(Constant.MAC) + "")) {
			Map<String, Object> result = instructionMethodService.getParameterNames(map.get(Constant.MAC) + "", "InternetGatewayDevice.SoftwareModules.", false);// 获取参数名称
			log.debug("LogId:{}查询InternetGatewayDevice.SoftwareModules.返回的{}", map.get("loguuid"), result);
			if (null != result && result.size() > 0) {
				Pattern patternService = Pattern.compile("InternetGatewayDevice.SoftwareModules.ExecEnv.[\\d]+.Type");// 智能网关运行环境类型,可能Type=OSGi或Type=JVM,但目前只考虑OSGi
				List<String> namesList = getNamesList(result, patternService);// 获取网关节点名集合
				log.debug("LogId:{}查询插件运行环境类型地址集{}", map.get("loguuid"), namesList);
				if (null != namesList && namesList.size() > 0) {
					result = instructionMethodService.getParameterValues(map.get(Constant.MAC) + "", namesList);// 获取参数值
					// result = getInternetGatewayDevice_SoftwareModules_ExecEnv_0_9_Type();// 获取参数值打桩数据
					log.debug("LogId:{}查询插件运行环境类型返回的参数集{}", map.get("loguuid"), result);
					if (null != result && result.size() > 0) {
						log.debug("LogId:{}获取:{}节点获取数据", map.get("loguuid"), nodeName);
						switch (nodeName) {
						case "X_CMCC_APICapabilites.":// 插件运行环境权限控制能力集
							getApiCapabilitesNodes(retMap, result, pathList);// 获取插件OSGi运行环境节点下权限控制能力集地址
							break;
						default:
							packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "未获节点名称", "");
							break;
						}
					} else {
						packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "未获取智能网关运行环境类型节点值", "");
					}
				} else {
					packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "未获取智能网关运行环境类型节点", "");
				}
			} else {
				packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "查询网关插件API权限,网关返回结果为空", "");
			}
		} else {
			packageBackInfo(retMap, RespCodeEnum.RC_1.code(), "请求参数不全", "");
		}
	}

	/**
	 * 获取插件OSGi运行环境节点下权限控制能力集地址
	 * 
	 * @param map
	 *            参数集
	 * @param result
	 *            查询插件运行环境类型返回的参数集
	 * @param pathList
	 *            节点路径
	 * @return
	 */
	private void getApiCapabilitesNodes(Map<String, Object> map, Map<String, Object> result, List<String> pathList) {
		Set<String> keys = result.keySet();// 获取Key
		for (String key : keys) {
			log.debug("LogId:{}查询插件运行环境返回类型:{}", map.get("loguuid"), result.get(key));
			// 目前根据网关接口返回的数据查询tyep=OSGi Release 4.2
			if ("OSGi".indexOf(StringUtils.isNotBlank(result.get(key) + "") ? String.valueOf(result.get(key)).substring(0, "OSGi".length()) : "") >= 0) {
				String path = key.substring(0, key.lastIndexOf(".") + 1) + "X_CMCC_APICapabilites.";// 组装插件运行环境权限控制能力集地址
				// 本应该在这个FOR循环进行网关插件运行环境权限控制能力集接口查询,但是网关的不稳定所以前将数据保存到集合中
				pathList.add(path);
			}
		}
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
	private List<String> getNamesList(Map<String, Object> date, Pattern patternService) {
		List<String> namesList = new ArrayList<>();
		Set<String> keys = date.keySet();
		for (String key : keys) {
			Matcher matchService = patternService.matcher(key);
			if (matchService.matches()) {
				namesList.add(key);
			}
		}
		return namesList;
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
		rm.put(Constant.CODE, code);
		rm.put(Constant.MESSAGE, msg);
		rm.put(Constant.DATA, data);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> addPluginByNume(Map<String, Object> parameter) {
		String redisKey = null;// Redis锁
		long st = System.currentTimeMillis();
		parameter.put("loguuid", UniqueUtil.uuid());
		Map<String, Object> rm = new HashMap<>();// 返回结果集
		try {
			log.info("LogId:{} 根据插件名称添加插件接口传入参数:{}", parameter.get("loguuid"), parameter);
			if (checkParm(parameter, rm, Arrays.asList(new String[] { Constant.MAC }))) {
				List<Map<String, Object>> cslist = (List<Map<String, Object>>) parameter.get("capabilitySet");// 插件集合
				if (null == cslist || cslist.size() <= 0) return packageBackInfo(RespCodeEnum.RC_1.code(), "未获取插件集合数据", null);
				redisKey = parameter.get(Constant.MAC) + "_addPluginByNume";
				pluginRedisTime = StringUtils.isNotBlank(pluginRedisTime) && !"plugin.redis.lock.time".equals(pluginRedisTime) ? pluginRedisTime : "120000";
				// nxxx的值只能取NX或者XX，如果取NX，则只有当key不存在时才进行set，如果取XX，则只有当key已经存在时才进行set;expx的值只能取EX或者PX，代表数据过期时间的单位，EX代表秒，PX代表毫秒
				String ls = redisClientTemplate.set(redisKey, RebootEnum.STATUS_0.code(), "NX", "PX", Integer.parseInt(pluginRedisTime));
				if (StringUtils.isBlank(ls)) return packageBackInfo(RespCodeEnum.RC_2.code(), RespCodeEnum.RC_2.description(), null);
				boolean resultStatus = false;
				for (Map<String, Object> cs : cslist) {
					String duName = (null == cs.get("DUName") || StringUtils.isBlank(cs.get("DUName").toString()) ? null : cs.get("DUName").toString());// 服务类名
					// 接口传入的插件名称不能为空
					if (StringUtils.isNotBlank(duName)) {
						List<String> nodeApiList = (List<String>) cs.get("apiList");// API功能集
						long stl = System.currentTimeMillis();
						Map<String, Object> result = instructionMethodService.getParameterNames(parameter.get(Constant.MAC) + "", "InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.", false);// 获取参数名称
						log.debug("LogId:{} 查询InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.返回的{}", parameter.get("loguuid"), result);
						if (null == result || result.size() <= 0) return packageBackInfo(RespCodeEnum.RC_1.code(), "网关查询插件节点失败", null);// 网关查询插件节点失败
						Pattern patternService = Pattern.compile("InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.[\\d]+.DUName");// 查询插件权限控制节点路径
						List<String> namesList = getNamesList(result, patternService);// 获取网关节点名称集合
						log.info("LogId:{} 获取插件权限控制地址集数据:{} 耗时:{}", parameter.get("loguuid"), namesList, (System.currentTimeMillis() - stl));
						// namesList为空说明网关无此节点需要添加
						if (null == namesList || namesList.size() <= 0) {
							log.info("LogId:{} 进入创建指定权限的插件的名称业务", parameter.get("loguuid"));
							resultStatus = allAddPlugin(nodeApiList, parameter, duName);
						} else {
							long rst = System.currentTimeMillis();
							Map<String, Object> rsn = instructionMethodService.getParameterValues(parameter.get(Constant.MAC) + "", namesList);// 获取插件名称
							log.info("LogId:{} 获取插名称数据:{} 耗时:{}", parameter.get("loguuid"), rsn, (System.currentTimeMillis() - rst));
							if (null == rsn || rsn.size() <= 0) return packageBackInfo(RespCodeEnum.RC_1.code(), "网关获取插件名称集合失败", null);// 网关获取插件名称集合失败
							boolean addNewPlugin = true;// 如果此值为true时则要创建插件名称并添加插件API
							for (Map.Entry<String, Object> entry : rsn.entrySet()) {
								String value = (null == entry.getValue() || StringUtils.isBlank(entry.getValue().toString()) ? null : entry.getValue().toString());// 需指定权限的插件的名称
								log.info("LogId:{} 获取权限的插件名称:{} 接口传入的插件名称:{}", parameter.get("loguuid"), value, duName);
								// 获取网关的插件名称与接口传入的插件名称相同时处理业务
								if (StringUtils.isNotBlank(value) && duName.trim().equals(value.trim())) {
									addNewPlugin = false;// 有插件名称,所以无需再创建插件名称节点
									String key = entry.getKey();
									long rast = System.currentTimeMillis();
									Map<String, Object> ran = instructionMethodService.getParameterNames(parameter.get(Constant.MAC) + "", key.substring(0, key.lastIndexOf(".") + 1) + "API.", false);// 获取参数名称
									log.info("LogId:{} 查询插件API节点据:{} 耗时:{}", parameter.get("loguuid"), ran, (System.currentTimeMillis() - rast));

									// 网关获取的API集合
									if (null != ran && ran.size() > 0) {
										List<String> rapinlist = getNamesList(ran, Pattern.compile("InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.[\\d]+.API.[\\d]+.Name"));// 获取节点下权限
										log.info("LogId:{} 获取插件API名称节点数据:{} 耗时:{}", parameter.get("loguuid"), rapinlist);

										// 接口传的插件API为空则说明要将当前插件名称下的所有API删除,只保留插件名称(网关有API插件,但是此次传的API集小于网关上的API集包含此业务逻辑,暂时注销)
										// if ((null == nodeApiList || nodeApiList.size() <= 0) && null != rapinlist && rapinlist.size() > 0) {
										// log.info("LogId:{} 进入接口传的插件API为空则说明要将当前插件名称下的所有API删除,只保留插件名称业务", parameter.get("loguuid"));
										// int rbns = 0;
										// for (String del : rapinlist) {
										// if (del.lastIndexOf(".") > 0) {
										// del = del.substring(0, del.lastIndexOf(".") + 1);
										// }
										// int rbn = instructionMethodService.DeleteObject(parameter.get(Constant.MAC) + "", del, System.currentTimeMillis() + "");
										// rbns += rbn;
										// log.info("LogId:{} 删除网关上多于的API节点地址:{} 网关返回状态:{}", parameter.get("loguuid"), del, rbn);
										// }
										// resultStatus = rbns >= 0 ? true : false;
										// }

										// 网关上的插件集合与传入的API插件集合都为空集合
										if ((null == rapinlist || rapinlist.size() <= 0) && (null == nodeApiList || nodeApiList.size() <= 0)) {
											log.info("LogId:{} 进入网关上的插件集合与传入的API插件集合都为空集合的业务", parameter.get("loguuid"));
											resultStatus = true;
										}

										// 网关上无API权限的插件但是此次传入的API集合不为空时,则创建节点再添加
										if ((null == rapinlist || rapinlist.size() <= 0) && null != nodeApiList && nodeApiList.size() > 0) {
											log.info("LogId:{} 进入网关上无API权限的插件但是此次传入的API集合不为空时,则创建节点再添加的业务", parameter.get("loguuid"));
											resultStatus = addApiByPluginNameNodePath(nodeApiList, parameter, key.substring(0, key.lastIndexOf(".") + 1));
										}

										// 网关有API插件,但是此次传的API集大于网关上的API集,所于需要在网关上添加API节点并设置API值
										if (null != rapinlist && rapinlist.size() > 0 && nodeApiList.size() >= rapinlist.size()) {
											log.info("LogId:{} 进入网关有API插件,但是此次传的API集大于网关上的API集,所于需要在网关上添加API节点并设置API值的业务", parameter.get("loguuid"));
											// 获取此次传入的API条数为网关上已经有的条数
											List<String> haveApiNode = nodeApiList.subList(0, rapinlist.size());
											log.info("LogId:{} 获取此次传入的API条数为网关上已经有的条数:{}", parameter.get("loguuid"), haveApiNode);
											boolean haveBoo = false, maxBoo = false;
											if (null != haveApiNode && haveApiNode.size() > 0) haveBoo = setApiByPluginByApiNodePath(parameter, haveApiNode, rapinlist);
											// 获取超出网关API集的此次传入的API条数
											List<String> maxApiNode = nodeApiList.subList(rapinlist.size(), nodeApiList.size());
											log.info("LogId:{} 获取超出网关API集的此次传入的API条数:{}", parameter.get("loguuid"), maxApiNode);
											if (null != maxApiNode && maxApiNode.size() > 0) maxBoo = addApiByPluginNameNodePath(maxApiNode, parameter, key.substring(0, key.lastIndexOf(".") + 1));
											resultStatus = haveBoo && (null == maxApiNode || maxApiNode.size() <= 0) ? true : haveBoo && maxBoo ? true : false;
										}

										// 网关有API插件,但是此次传入的API集小于网关上的API集,所于获取此次传入的API集的数量的网关API节点集的数量并将API值设置,多于的网关节点将删除
										if (null != rapinlist && rapinlist.size() > 0 && nodeApiList.size() < rapinlist.size()) {
											log.info("LogId:{} 进入网关有API插件,但是此次传入的API集小于网关上的API集,所于获取此次传入的API集的数量的网关API节点集的数量并将API值设置,多于的网关节点将删除的业务", parameter.get("loguuid"));
											// 获取此次传入的API条数相对应该值的网关API节点地址
											List<String> haveApiPath = rapinlist.subList(0, nodeApiList.size());
											log.info("LogId:{} 获取此次传的API条数相对应该值的网关API节点地址:{}", parameter.get("loguuid"), haveApiPath);
											boolean haveBoo = false, delBoo = false;
											if (null != haveApiPath && haveApiPath.size() > 0) haveBoo = setApiByPluginByApiNodePath(parameter, nodeApiList, haveApiPath);
											// 获取网关上多于的API节点地址
											List<String> delApiPath = rapinlist.subList(nodeApiList.size(), rapinlist.size());
											log.info("LogId:{} 获取网关上多于的API节点地址:{}", parameter.get("loguuid"), delApiPath);
											if (null != delApiPath && delApiPath.size() > 0) {
												int rbns = 0;
												for (String del : delApiPath) {
													if (del.lastIndexOf(".") > 0) {
														del = del.substring(0, del.lastIndexOf(".") + 1);
													}
													int rbn = instructionMethodService.DeleteObject(parameter.get(Constant.MAC) + "", del, System.currentTimeMillis() + "");
													rbns += rbn;
													log.info("LogId:{} 网关上多于的API节点地址:{} 网关返回状态:{}", parameter.get("loguuid"), del, rbn);
												}
												delBoo = rbns >= 0 ? true : false;
											}
											// 存在此次传入的API集为空所以多次判断
											resultStatus = haveBoo && (null == delApiPath || delApiPath.size() <= 0) ? true : haveBoo && delBoo ? true : (null == haveApiPath || haveApiPath.size() <= 0) && delBoo ? true : false;
										}
									}
								}
							}
							if (addNewPlugin) {
								// 当所有的插件名称都不相同时则要创建插件名称并添加插件API
								resultStatus = allAddPlugin(nodeApiList, parameter, duName);
							}
						}
					}
				}
				if (resultStatus) {
					rm = packageBackInfo(RespCodeEnum.RC_0.code(), "业务操作成功", null);
				} else {
					rm = packageBackInfo(RespCodeEnum.RC_1.code(), "业务操作失败", null);
				}
			}
			if (StringUtils.isNotBlank(redisKey)) redisClientTemplate.del(redisKey);// 删除Redis锁
		} catch (Exception e) {
			rm = packageBackInfo(RespCodeEnum.RC_1.code(), "业务操作失败", null);
			if (StringUtils.isNotBlank(redisKey)) redisClientTemplate.del(redisKey);// 删除Redis锁
			log.error("LogId:{}根据插件名称添加插件功能异常：" + e.getMessage(), parameter.get("loguuid"), e);
		}
		log.info("LogId:{} 根据插件名称添加插件返回内容:{} 耗时:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - st));
		return rm;
	}

	/**
	 * 根据插件名称节点添加 API
	 * @param nodeApiList
	 *            将要添加的api能力集
	 * @param parameter
	 *            参数
	 * @param pluginNameNodePath
	 *            插件名称节点
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Boolean addApiByPluginNameNodePath(List<String> nodeApiList, Map<String, Object> parameter, String pluginNameNodePath) {
		boolean backApiResult = false;
		List<ParameterValueStruct> apilistpvs = new ArrayList<ParameterValueStruct>();
		log.info("LogId:{} 将在插件节点路径:{} 下添加:{} API节点", parameter.get("loguuid"), pluginNameNodePath, nodeApiList);
		for (String api : nodeApiList) {
			int aiprai = instructionMethodService.AddObject(parameter.get(Constant.MAC) + "", pluginNameNodePath + "API.", System.currentTimeMillis() + "");
			log.info("LogId:{} 网关添加根据插件名称组装API插件节点路径:{} 返回状态值:{}", parameter.get("loguuid"), pluginNameNodePath, aiprai);
			if (aiprai > 0) {
				if (StringUtils.isNotBlank(api)) {
					ParameterValueStruct apipvs = new ParameterValueStruct();
					apipvs.setName(pluginNameNodePath + "API." + aiprai + ".Name");
					apipvs.setValue(api);
					apipvs.setReadWrite(true);
					apipvs.setValueType("string");
					apilistpvs.add(apipvs);
				}
			}
		}
		long aut = System.currentTimeMillis();
		if (apilistpvs.size() > 0) backApiResult = instructionMethodService.setParameterValue(parameter.get(Constant.MAC) + "", apilistpvs);
		log.info("LogId:{} 因插件的名称下无API所以根据插件名称节点:{} 创建API节点并设值的数据集:{} 安装后返回的状态:{} 耗时:{}", parameter.get("loguuid"), pluginNameNodePath, apilistpvs, backApiResult, (System.currentTimeMillis() - aut));
		return backApiResult;
	}

	/**
	 * 在原有的API节点上设置API值
	 * @param parameter
	 *            参数
	 * @param haveApiNode
	 *            将要设置到网关上的API集
	 * @param rapinlist
	 *            网关上已经存在的API节点集
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Boolean setApiByPluginByApiNodePath(Map<String, Object> parameter, List<String> haveApiNode, List<String> rapinlist) {
		int i = 0;
		boolean backApiResult = false;
		List<ParameterValueStruct> apilistpvs = new ArrayList<ParameterValueStruct>();
		for (String api : haveApiNode) {
			if (StringUtils.isNotBlank(api)) {
				ParameterValueStruct apipvs = new ParameterValueStruct();
				apipvs.setName(rapinlist.get(i++));
				apipvs.setValue(api);
				apipvs.setReadWrite(true);
				apipvs.setValueType("string");
				apilistpvs.add(apipvs);
			}
		}
		long aut = System.currentTimeMillis();
		if (apilistpvs.size() > 0) backApiResult = instructionMethodService.setParameterValue(parameter.get(Constant.MAC) + "", apilistpvs);
		log.info("LogId:{} 在原有的API节点:{} 上设置此次API值:{} 组装好的API插件集:{} 安装后返回的状态:{} 耗时:{}", parameter.get("loguuid"), rapinlist, haveApiNode, apilistpvs, backApiResult, (System.currentTimeMillis() - aut));
		return backApiResult;
	}

	/**
	 * 网关无插件名称节点,所以创建插件名称节点,再创建API节点再添加
	 * @param apiList
	 *            接口传入的API集
	 * @param parameter
	 *            参数
	 * @param duName
	 *            插件名称
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Boolean allAddPlugin(List<String> apiList, Map<String, Object> parameter, String duName) throws Exception {
		List<ParameterValueStruct> dulistpvs = new ArrayList<ParameterValueStruct>();
		List<ParameterValueStruct> apilistpvs = new ArrayList<ParameterValueStruct>();
		List<String> lostUrlPath = new ArrayList<String>();// 保存插件名称节点为了防止API插件安装失败后做删除处理
		if (null != duName && StringUtils.isNotBlank(duName)) {
			long dut = System.currentTimeMillis();
			int rai = instructionMethodService.AddObject(parameter.get(Constant.MAC) + "", "InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission.", System.currentTimeMillis() + "");
			log.info("LogId:{} 网关上添加插件返回的状态:{}耗时:{}", parameter.get("loguuid"), rai, (System.currentTimeMillis() - dut));
			if (rai > 0) {
				ParameterValueStruct pvs = new ParameterValueStruct();
				lostUrlPath.add("InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission." + rai + ".DUName");
				pvs.setName("InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission." + rai + ".DUName");
				pvs.setValue(duName);
				pvs.setReadWrite(true);
				pvs.setValueType("string");
				dulistpvs.add(pvs);
				// 可能会存在只有插件名称没有插件API,此时应该在网上添加插件名
				if (null != apiList && apiList.size() > 0) {
					for (String api : apiList) {
						long ddut = System.currentTimeMillis();
						int aiprai = instructionMethodService.AddObject(parameter.get(Constant.MAC) + "", "InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission." + rai + ".API.", System.currentTimeMillis() + "");
						log.info("LogId:{} 在网关上插件地址为:{}添加节点 返回的状态:{}耗时:{}", parameter.get("loguuid"), "InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission." + rai + ".API.", aiprai, (System.currentTimeMillis() - ddut));
						if (aiprai > 0) {
							ParameterValueStruct apipvs = new ParameterValueStruct();
							apipvs.setName("InternetGatewayDevice.SoftwareModules.X_CMCC_DUPermission." + rai + ".API." + aiprai + ".Name");
							apipvs.setValue(api);
							apipvs.setReadWrite(true);
							apipvs.setValueType("string");
							apilistpvs.add(apipvs);
						}
					}
				}
			}
		}
		Boolean backDuResult = false, backApiResult = false;
		if (dulistpvs.size() > 0) {
			long dut = System.currentTimeMillis();
			backDuResult = instructionMethodService.setParameterValue(parameter.get(Constant.MAC) + "", dulistpvs);
			log.info("LogId:{} 网关上批量安装插件名称(duName)数据:{} 安装后返回的状态:{} 耗时:{}", parameter.get("loguuid"), dulistpvs, backDuResult, (System.currentTimeMillis() - dut));
		}
		if (apilistpvs.size() > 0 && backDuResult) {
			long aut = System.currentTimeMillis();
			backApiResult = instructionMethodService.setParameterValue(parameter.get(Constant.MAC) + "", apilistpvs);
			log.info("LogId:{} 网关上批量安装插件Api数据:{} 安装后返回的状态:{} 耗时:{}", parameter.get("loguuid"), apilistpvs, backApiResult, (System.currentTimeMillis() - aut));
		}
		if (!backDuResult) deleteDuName(parameter, lostUrlPath);// 失败删除安装的插件名称
		return backDuResult && (null == apilistpvs || apilistpvs.size() <= 0) ? true : backDuResult && backApiResult ? true : false;
	}

	/**
	 * 封装返回内容
	 * @param rm 返回对象
	 * @param code 状态码
	 * @param msg 状态码描述内容
	 * @param data 返回数据
	 * @throws
	 */
	private Map<String, Object> packageBackInfo(String code, String msg, Object data) {
		Map<String, Object> rm = new HashMap<>();// 返回结果集
		rm.put(Constant.CODE, code);
		rm.put(Constant.MESSAGE, msg);
		rm.put(Constant.DATA, data);
		return rm;
	}
}
