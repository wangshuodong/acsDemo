package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.DiagnoseThresholdValueMapper;
import com.cmiot.rms.dao.model.DiagnoseThresholdValue;
import com.cmiot.rms.services.DiagnoseThresholdValueService;

public class DiagnoseThresholdValueServiceImpl implements DiagnoseThresholdValueService {

	@Autowired
	DiagnoseThresholdValueMapper diagnoseThresholdValueMapper;

	private static Logger log = LoggerFactory.getLogger(DiagnoseThresholdValueServiceImpl.class.getName());

	@Override
	public Map<String, Object> queryDiagnoseThresholdValue(Map<String, Object> parameter) {
		long st = System.currentTimeMillis();
		parameter.put("loguuid", UniqueUtil.uuid());
		Map<String, Object> rm = new HashMap<String, Object>();
		log.info("LogId:{} 查询诊断阈值接口请求参数:{}", parameter.get("loguuid"), parameter);
		try {
			if (checkParm(parameter, rm, Arrays.asList(new String[] { "diagnoseType" }))) {
				long qst = System.currentTimeMillis();
				List<DiagnoseThresholdValue> dtvList = diagnoseThresholdValueMapper.selectDiagnoseThresholdValue(new DiagnoseThresholdValue(Integer.parseInt(parameter.get("diagnoseType") + ""), 1));
				log.info("LogId:{} 查询数据库阈值数据总数:{} 查询消耗时间:{}", parameter.get("loguuid"), dtvList.size(), (System.currentTimeMillis() - qst));
				if (null != dtvList && dtvList.size() > 0) {
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					for (DiagnoseThresholdValue dtv : dtvList) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("thresholdName", dtv.getThresholdName());
						map.put("minThresholdValue", dtv.getMinThresholdValue());
						map.put("maxThresholdValue", dtv.getMaxThresholdValue());
						map.put("diagnoseType", dtv.getDiagnoseType());
						map.put("equipmentType", dtv.getEquipmentType());
						list.add(map);
					}
					packageBackResult(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), JSON.toJSON(list));
				} else {
					packageBackResult(rm, RespCodeEnum.RC_0.code(), "未查询到诊断阈值", "");
				}
			}
		} catch (Exception e) {
			packageBackResult(rm, RespCodeEnum.RC_1.code(), "查询诊断阈值失败", "");
			log.error("Query Diagnose Threshold Value " + e.getMessage(), e);
		}
		log.info("LogId:{} 查询诊断阈值接口返回数据:{} 查询消耗时间:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - st));
		return rm;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> saveDiagnoseThresholdValue(Map<String, Object> parameter) {
		long st = System.currentTimeMillis();
		parameter.put("loguuid", UniqueUtil.uuid());
		Map<String, Object> rm = new HashMap<String, Object>();
		log.info("LogId:{} 保存或更新阈值接口请求参数:{}", parameter.get("loguuid"), parameter);
		try {
			if (checkParm(parameter, rm, Arrays.asList(new String[] { "diagnoseType" }))) {
				List<Map<String, Object>> tnvlist = (List<Map<String, Object>>) parameter.get("tnvList");
				if (null != tnvlist && tnvlist.size() > 0) {
					int equipmentType = 1;// 设备类型1:网关;2:机顶盒
					List<DiagnoseThresholdValue> add = new ArrayList<DiagnoseThresholdValue>();
					List<DiagnoseThresholdValue> update = new ArrayList<DiagnoseThresholdValue>();
					long qst = System.currentTimeMillis();
					List<DiagnoseThresholdValue> dtvList = diagnoseThresholdValueMapper.selectDiagnoseThresholdValue(new DiagnoseThresholdValue(Integer.parseInt(parameter.get("diagnoseType") + ""), equipmentType));
					log.info("LogId:{} 查询数据库阈值数据总数:{} 查询消耗时间:{}", parameter.get("loguuid"), dtvList.size(), (System.currentTimeMillis() - qst));
					if (null != dtvList && dtvList.size() > 0) {
						updateOrAdd(parameter, dtvList, tnvlist, add, update, equipmentType);
					} else {
						for (Map<String, Object> map : tnvlist) {
							String thresholdName = null != map.get("thresholdName") ? map.get("thresholdName").toString().trim() : "";
							String minThresholdValue = null != map.get("minThresholdValue") ? map.get("minThresholdValue").toString().trim() : "";
							String maxThresholdValue = null != map.get("maxThresholdValue") ? map.get("maxThresholdValue").toString().trim() : "";
							add(parameter, add, equipmentType, thresholdName, minThresholdValue, maxThresholdValue);
						}
					}
					// 批量更新的瓶颈为200条,但是目前业务中100条的批量都未超出,固代码中无分批处理逻辑
					if (null != add && add.size() > 0) {
						diagnoseThresholdValueMapper.insertBatch(add);
					}
					if (null != update && update.size() > 0) {
						diagnoseThresholdValueMapper.updateBatch(update);
					}
					log.info("LogId:{} 添加数据总数:{} 更新数据总数:{}", parameter.get("loguuid"), add.size(), update.size());
				}
				packageBackResult(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), "");
			}
		} catch (Exception e) {
			packageBackResult(rm, RespCodeEnum.RC_1.code(), "保存诊断阈值失败", "");
			log.error("Query Diagnose Threshold Value " + e.getMessage(), e);
		}
		log.info("LogId:{} 保存或更新阈值接口返回数据:{} 查询消耗时间:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - st));
		return rm;
	}

	/**
	 * 更新或添加阈值
	 * 
	 * @param parameter
	 *            参数集
	 * @param dtvList
	 *            库查询出的阈值
	 * @param tnvlist
	 *            页面传入的阈值
	 * @param add
	 *            添加集合对象
	 * @param update
	 *            更新集合对象
	 * @param equipmentType
	 *            设备类型1:网关;2:机顶盒
	 */
	private void updateOrAdd(Map<String, Object> parameter, List<DiagnoseThresholdValue> dtvList, List<Map<String, Object>> tnvlist, List<DiagnoseThresholdValue> add, List<DiagnoseThresholdValue> update, int equipmentType) {
		for (Map<String, Object> map : tnvlist) {
			boolean flag = true;
			String thresholdName = null != map.get("thresholdName") ? map.get("thresholdName").toString().trim() : "";
			String minThresholdValue = null != map.get("minThresholdValue") ? map.get("minThresholdValue").toString().trim() : "";
			String maxThresholdValue = null != map.get("maxThresholdValue") ? map.get("maxThresholdValue").toString().trim() : "";
			if (StringUtils.isNotBlank(thresholdName) && StringUtils.isNotBlank(minThresholdValue) && StringUtils.isNotBlank(maxThresholdValue)) {
				for (DiagnoseThresholdValue dtv : dtvList) {
					if (thresholdName.equals(dtv.getThresholdName())) {
						log.info("LogId:{} " + thresholdName + "数据库的min值:{} 更新min值:{} 数据库的max值:{} 更新max值:{}", parameter.get("loguuid"), dtv.getMinThresholdValue(), minThresholdValue, dtv.getMaxThresholdValue(), maxThresholdValue);
						dtv.setMinThresholdValue(minThresholdValue);
						dtv.setMaxThresholdValue(maxThresholdValue);
						update.add(dtv);
						flag = false;
					}
				}
				if (flag) add(parameter, add, equipmentType, thresholdName, minThresholdValue, maxThresholdValue);
			}
		}

	}

	/**
	 * 添加阈值
	 * 
	 * @param parameter
	 *            参数集
	 * @param add
	 *            添加集合对象
	 * @param equipmentType
	 *            设备类型1:网关;2:机顶盒
	 * @param thresholdName
	 *            阈值名称
	 * @param minThresholdValue
	 *            低阈值数值
	 * @param maxThresholdValue
	 *            高阈值数值
	 */
	private void add(Map<String, Object> parameter, List<DiagnoseThresholdValue> add, int equipmentType, String thresholdName, String minThresholdValue, String maxThresholdValue) {
		log.info("LogId:{} " + thresholdName + "插入min值:{} 插入max值:{}", parameter.get("loguuid"), minThresholdValue, maxThresholdValue);
		DiagnoseThresholdValue adtv = new DiagnoseThresholdValue();
		// 诊断类型1:线路诊断;2:Ping诊断;3:Traceroute诊断;4:PPPoE仿真;5:DHCP仿真;6:VoIP诊断;7:HTTP下载仿真
		adtv.setDiagnoseType(Integer.parseInt(parameter.get("diagnoseType").toString().trim()));
		adtv.setThresholdName(thresholdName);// 阈值名称
		adtv.setMinThresholdValue(minThresholdValue);// 低阈值数值
		adtv.setMaxThresholdValue(maxThresholdValue);// 高阈值数值
		adtv.setEquipmentType(equipmentType);// 设备类型1:网关;2:机顶盒
		add.add(adtv);
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
	private Boolean checkParm(Map<String, Object> parameter, Map<String, Object> rm, List<String> parmlist) {
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
							packageBackResult(rm, RespCodeEnum.RC_1.code(), parm + "参数不能为空", "");
							return false;// 目前所有的参数都为必传所以只要有一个为空返加false
						}
					}
				}
			}
			if (countParm > 0) {// 请求参数与要求参数个数不匹配
				isTrue = false;
				packageBackResult(rm, RespCodeEnum.RC_1.code(), "接口必传参数不全", "");
			}
		} else {
			packageBackResult(rm, RespCodeEnum.RC_1.code(), "请求接口参数为空", "");// 请求参数集为空
		}
		return isTrue;
	}

	/**
	 * 封装返回数据对象
	 * 
	 * @param rm
	 *            返回对象
	 * @param code
	 *            状态码
	 * @param msg
	 *            状态描述
	 * @param data
	 *            返回数据对象
	 */
	private void packageBackResult(Map<String, Object> rm, String code, String msg, Object data) {
		rm.put(Constant.DATA, data);
		rm.put(Constant.CODE, code);
		rm.put(Constant.MESSAGE, msg);
	}

}
