package com.cmiot.rms.services.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.DiagnoseLog;
import com.cmiot.rms.services.LogManagerService;
import com.cmiot.rms.services.boxManager.instruction.BoxInstructionMethodService;

/**
 * 诊断相关工具类
 * 
 * @author shuang
 * @version [版本号, 2016年6月6日]
 */
public class BoxDiagnosticsUtil {

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
	 * 在网关节点上设置Ping诊断值
	 * 
	 * @param parameter
	 *            参数信息
	 * @param gi
	 *            网关信息对象
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Boolean settingPingParameterAndRequestGateway(Map<String, Object> parameter, BoxInfo gi, BoxInstructionMethodService ims, List<ParameterValueStruct> pvslist) {
		return ims.setParameterValue(gi.getBoxMacaddress(), pvslist);// 组装数据
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
	public static String[] getDiagnosticsStatus(BoxInfo gi, String succ, String node, BoxInstructionMethodService ims, Logger log, String logName) {
		String temp = null;
		String[] rb = { "false", "0" };
		long st = System.currentTimeMillis();
		List<String> list = new ArrayList<>();
		list.add(node);// DHCP仿真状态
		Map<String, Object> result = ims.getParameterValues(gi.getBoxMacaddress(), list);
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
	 */
	public static void getDiagnosticsResult(BoxInfo gi, List<String> node, Map<String, Object> rm, String logName, BoxInstructionMethodService boxims, Logger log, String logid) {
		long st = System.currentTimeMillis();
		Map<String, Object> result = boxims.getParameterValues(gi.getBoxMacaddress(), node);
		log.info("LogId:{}" + logName + "获取网关诊断结果返回结果集:{}消耗时间:{}", logid, result, (System.currentTimeMillis() - st));
		if (null != result && result.size() > 0) {
			long fst = System.currentTimeMillis();
			result = BoxDiagnosticsUtil.getResultMapDate(result);// 获取返回封装数据
			BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), result);
			log.info("LogId:{}" + logName + "将诊断结果返回结果集:{}封装消耗时间:{}", logid, result, (System.currentTimeMillis() - fst));
		} else {
			BoxDiagnosticsUtil.packageBackInfo(rm, RespCodeEnum.RC_1.code(), "获取网关诊断信息失败", "");
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
	public static void temporaryObjectLock(Integer waitTime, BoxInfo gi, Logger log, String logName, String logid) throws Exception {
		TemporaryObject temporaryObject = new TemporaryObject("box_diagnostics_" + gi.getBoxMacaddress());
		log.info("LogId:{}" + logName + "添加临时对象锁,网关MAC地址:{}", logid, "box_diagnostics_" + temporaryObject.getRequestId());
		RequestCache.set("box_diagnostics_" + gi.getBoxMacaddress(), temporaryObject);
		long st = System.currentTimeMillis();
		synchronized (temporaryObject) {
			log.info("LogId:{}" + logName + "设置临时对象锁等待时长:{}", logid, waitTime);
			temporaryObject.wait((null != waitTime && 0 < waitTime) ? waitTime : 120000);// 等待8 DIAGNOSTICS COMPLETE状态唤醒
		}
		log.info("LogId:{}" + logName + "临时对象解锁消耗时间:{}", logid, (System.currentTimeMillis() - st));
		log.info("LogId:{}" + logName + "删除临时对象锁,网关MAC地址:{}", logid, "box_diagnostics_" + temporaryObject.getRequestId());
		RequestCache.delete("box_diagnostics_" + gi.getBoxMacaddress());// 删除锁定对象
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
		parameter.put("categoryMenu", CategoryEnum.BOX_MANAGER_SERVICE.name());// 类目ID
		parameter.put("operation", operation);// 具体的操作
		parameter.put("categoryMenuName", CategoryEnum.BOX_MANAGER_SERVICE.description());// 类目名称
		parameter.put("content", "请求报文" + JSON.toJSONString(parameter) + ",返回报文" + JSON.toJSONString(rm));// 操作的数据内容
		parameter.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
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
	public static void saveDiagnoseLog(Map<String, Object> parameter, BoxInfo gi, Integer dt, DiagnoseLogMapper diagnoseLogDao) throws Exception {
		DiagnoseLog dl = new DiagnoseLog();
		dl.setGatewayMacaddress(gi.getBoxMacaddress());
		dl.setDiagnoseType(dt);
		dl.setDiagnoseOperator(parameter.get("userName") + "");
		dl.setDiagnoseTime(new Date());
		diagnoseLogDao.insert(dl);
	}
}
