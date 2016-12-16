package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.cmiot.rms.services.thread.boxupgradetask.BoxBackupTaskParameter;
import com.cmiot.rms.services.thread.boxupgradetask.BoxUpgradeTaskThread;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSON;
import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.LogTypeEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.enums.UpgradeTaskDetailStatusEnum;
import com.cmiot.rms.common.enums.UpgradeTaskStatusEnum;
import com.cmiot.rms.common.logback.LogBackRecord;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BoxFirmwareInfoMapper;
import com.cmiot.rms.dao.mapper.BoxFirmwareUpgradeTaskDetailMapper;
import com.cmiot.rms.dao.mapper.BoxFirmwareUpgradeTaskMapper;
import com.cmiot.rms.dao.mapper.BoxFirmwareUpgradeTaskTimeMapper;
import com.cmiot.rms.dao.model.BoxFirmwareInfo;
import com.cmiot.rms.dao.model.BoxFirmwareUpgradeTask;
import com.cmiot.rms.dao.model.BoxFirmwareUpgradeTaskDetail;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.services.BoxFirmwareUpgradeTaskService;
import com.cmiot.rms.services.BoxInfoService;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.LogManagerService;
import com.cmiot.rms.services.boxManager.instruction.BoxInvokeInsService;

/**
 * Created by panmingguo on 2016/1/25.
 */
public class BoxFirmwareUpgradeTaskServiceImpl implements BoxFirmwareUpgradeTaskService {

	private final Logger logger = LoggerFactory.getLogger(BoxFirmwareUpgradeTaskServiceImpl.class);

	@Autowired
	BoxFirmwareUpgradeTaskMapper boxFirmwareUpgradeTaskMapper;

	@Autowired
	BoxFirmwareUpgradeTaskDetailMapper boxFirmwareUpgradeTaskDetailMapper;

	@Autowired
	BoxFirmwareUpgradeTaskTimeMapper boxFirmwareUpgradeTaskTimeMapper;

	@Autowired
	BoxInfoService boxInfoService;

	@Autowired
	BoxFirmwareInfoMapper boxFirmwareInfoMapper;

	@Autowired
	BoxInvokeInsService boxInvokeInsService;

	@Autowired
	InstructionsService instructionsService;

	@Autowired
	private AreaService amsAreaService;

	@Autowired
	private LogManagerService logManagerService;

	@Value("${box.file.server.userName}")
	String userName;

	@Value("${box.file.server.password}")
	String password;

	@Override
	public Map<String, Object> queryUpgradeSpecified(Map<String, Object> parameter) {
		parameter.put("loguuid", UniqueUtil.uuid());
		long st = System.currentTimeMillis();
		logger.info("LogId:{}立即升级信息查传入参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		try {
			if (checkParm(parameter, rm, Arrays.asList(new String[] { "boxId" }))) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("boxId", parameter.get("boxId"));// 机顶盒ID
				// map.put("taskTriggerMode", 1);// 任务触发方式 1:立即执行
				long rvst = System.currentTimeMillis();
				List<Map<String, String>> rv = boxFirmwareUpgradeTaskMapper.queryUpgradeSpecifiedVersionInfo(map);
				logger.info("LogId:{}根据查询参数:{}查询固件立即升级的版本信息:{}耗时:{}", parameter.get("loguuid"), map, rv, (System.currentTimeMillis() - rvst));
				long bfutdt = System.currentTimeMillis();
				BoxFirmwareUpgradeTaskDetail bfutd = boxFirmwareUpgradeTaskDetailMapper.selectByBoxIdAndTaskTriggerMode(map);
				logger.info("LogId:{}根据查询参数:{}查询固件最近立即升级的状态信息:{}耗时:{}", parameter.get("loguuid"), map, bfutd, (System.currentTimeMillis() - bfutdt));
				if (null != bfutd) {
					switch (bfutd.getStatus().intValue()) {
					case 1:
						setUpgradeStatus(rm, UpgradeTaskDetailStatusEnum.PROCESSING.code(), UpgradeTaskDetailStatusEnum.PROCESSING.description());
						break;
					case 2:
						setUpgradeStatus(rm, UpgradeTaskDetailStatusEnum.FAILURE.code(), UpgradeTaskDetailStatusEnum.FAILURE.description());
						break;
					case 3:
						setUpgradeStatus(rm, UpgradeTaskDetailStatusEnum.SUCSSESS.code(), UpgradeTaskDetailStatusEnum.SUCSSESS.description());
						break;
					default:
						setUpgradeStatus(rm, -1, "从未升级");
						break;
					}
				} else {
					setUpgradeStatus(rm, -1, "从未升级");
				}
				setPackageBasisResult(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), rv);
			}
		} catch (Exception e) {
			logger.error("LogId:{}query Upgrade Specified Exception " + e.getMessage(), parameter.get("loguuid"), e);
			setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "查询立即升级查询版本信息接口异常", "");
		}
		logger.info("LogId:{}立即升级信息查返回数据:{}业务处理总耗时:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - st));
		return rm;
	}

	/**
	 * 组装返回状态内容
	 * 
	 * @param rm
	 *            返回对象
	 * @param st
	 *            状态
	 * @param msg
	 *            说明
	 */
	private void setUpgradeStatus(Map<String, Object> rm, int st, String msg) {
		rm.put("latelyUpgradeStatus", st);
		rm.put("latelyUpgradeMsg", msg);
	}

	@Override
	public Map<String, Object> queryUpgradeTaskDetail(Map<String, Object> parameter) {
		parameter.put("loguuid", UniqueUtil.uuid());
		long st = System.currentTimeMillis();
		logger.info("LogId:{}查询升级任务的明细传入参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		try {
			int total = 0;
			int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
			int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;
			String[] parmlist = { "upgradeTaskId", "type" };
			if (checkParm(parameter, rm, Arrays.asList(parmlist))) {
				long qst = System.currentTimeMillis();
				List<Map<String, String>> result = boxFirmwareUpgradeTaskDetailMapper.queryListByIdAndStatus(getQueryUpgradeTaskDetailSql(parameter, page, pageSize));
				logger.info("LogId:{}根据查询条件:{}查询升级任务的明细返回数据:{}耗时:{}", parameter.get("loguuid"), getQueryUpgradeTaskDetailSql(parameter, page, pageSize), result, (System.currentTimeMillis() - qst));
				if (null != result && result.size() > 0) {
					long cqst = System.currentTimeMillis();
					total = boxFirmwareUpgradeTaskDetailMapper.countQueryListByIdAndStatus(getQueryUpgradeTaskDetailSql(parameter, page, pageSize));
					logger.info("LogId:{}根据查询条件:{}统计升级任务的明细总数据:{}耗时:{}", parameter.get("loguuid"), getQueryUpgradeTaskDetailSql(parameter, page, pageSize), total, (System.currentTimeMillis() - cqst));
					getAreaName(result, 0, parameter);
				}
				setPackagePageResult(rm, total, page, pageSize);
				setPackageBasisResult(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), result);
			}
		} catch (Exception e) {
			logger.error("LogId:{}query Upgrade Task Detail Exception " + e.getMessage(), parameter.get("loguuid"), e);
			setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "查询升级任务明细接口异常", "");
		}
		logger.info("LogId:{}查询升级任务的明细返回数据:{}业务处理总耗时:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - st));
		return rm;
	}

	/**
	 * 查询明细的SQL条件
	 * 
	 * @param parameter
	 *            参数集
	 * @param page
	 *            分页当前值
	 * @param pageSize
	 *            每页多少条数据
	 * @return
	 */
	private Map<String, Object> getQueryUpgradeTaskDetailSql(Map<String, Object> parameter, int page, int pageSize) {
		String upgradeTaskId = null != parameter.get("upgradeTaskId") ? parameter.get("upgradeTaskId").toString() : null;
		String type = null != parameter.get("type") ? parameter.get("type").toString() : null;
		int lbound = (page - 1) * pageSize;
		int mbound = pageSize;
		Map<String, Object> sql = new HashMap<>();
		sql.put("upgradeTaskId", upgradeTaskId);
		sql.put("type", type);
		sql.put("lbound", lbound);
		sql.put("mbound", mbound);
		return sql;
	}

	@Override
	public Map<String, Object> searchUpgradeTask(Map<String, Object> parameter) {
		parameter.put("loguuid", UniqueUtil.uuid());
		long st = System.currentTimeMillis();
		logger.info("LogId:{}查询固件升级任务列表获取传入参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		try {
			int total = 0;
			String taskName = StringUtils.isNotBlank(parameter.get("taskName") + "") ? parameter.get("taskName") + "" : null;// 任务名称
			int page = Integer.parseInt(StringUtils.isNotBlank(parameter.get("page") + "") ? parameter.get("page") + "" : "1");
			int pageSize = Integer.parseInt(StringUtils.isNotBlank(parameter.get("pageSize") + "") ? parameter.get("pageSize") + "" : "10");
			Map<String, Object> sqlmap = new HashMap<String, Object>();
			sqlmap.put("taskName", taskName);
			int lbound = (page - 1) * pageSize;
			int mbound = pageSize;
			sqlmap.put("lbound", lbound);
			sqlmap.put("mbound", mbound);
			long sqlst = System.currentTimeMillis();
			List<Map<String, String>> result = boxFirmwareUpgradeTaskMapper.selectAllOrByTaskName(sqlmap);
			logger.info("LogId:{}根据查询条件查询:{}查询固件升级任务列表数据:{}SQL查询耗时:{}", parameter.get("loguuid"), sqlmap, result, (System.currentTimeMillis() - sqlst));
			if (null != result && result.size() > 0) {
				long csqlst = System.currentTimeMillis();
				total = boxFirmwareUpgradeTaskMapper.countSelectAllOrByTaskName(sqlmap);
				logger.info("LogId:{}根据查询条件查询:{}统计固件升级任务列表数据:{}SQL查询耗时:{}", parameter.get("loguuid"), sqlmap, result, (System.currentTimeMillis() - csqlst));
				getAreaName(result, 1, parameter);
			}
			setPackagePageResult(rm, total, page, pageSize);
			setPackageBasisResult(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), result);
		} catch (Exception e) {
			logger.error("LogId:{} searchUpgradeTask exception " + e.getMessage(), parameter.get("loguuid"), e);
			setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "获取升级任务列表息接口异常", "");
		}
		logger.info("LogId:{}查询固件升级任务列表返回数据:{}业务处理总耗时:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - st));
		return rm;
	}

	@Override
	public Map<String, Object> upgradeTaskAddSearch(Map<String, Object> parameter) {
		parameter.put("loguuid", UniqueUtil.uuid());
		long st = System.currentTimeMillis();
		logger.info("LogId:{}新建升级任务网关信息查询传入参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		try {
			String[] parmlist = { "factoryCode", "firmwareId", "boxModel", "uid" };// 机顶盒厂商编码,固件编号UUID,机顶盒UUID
			if (checkParm(parameter, rm, Arrays.asList(parmlist))) {
				int total = 0;
				int page = Integer.parseInt(StringUtils.isNotBlank(parameter.get("page") + "") ? parameter.get("page") + "" : "1");
				int pageSize = Integer.parseInt(StringUtils.isNotBlank(parameter.get("pageSize") + "") ? parameter.get("pageSize") + "" : "10");
				long sqlst = System.currentTimeMillis();
				List<Map<String, String>> result = boxFirmwareUpgradeTaskMapper.selectBoxFirmwareInfo(getPackageBoxInfo(parameter, page, pageSize));
				logger.info("LogId:{}根据查询条件查询:{}查询新建升级任务网关信息数据:{}SQL查询耗时:{}", parameter.get("loguuid"), getPackageBoxInfo(parameter, page, pageSize), result, (System.currentTimeMillis() - sqlst));
				if (null != result && result.size() > 0) {
					long csqlst = System.currentTimeMillis();
					total = boxFirmwareUpgradeTaskMapper.countSelectBoxFirmwareInfo(getPackageBoxInfo(parameter, page, pageSize));
					logger.info("LogId:{}根据查询条件查询:{}统计新建升级任务网关信息数据:{}SQL查询耗时:{}", parameter.get("loguuid"), getPackageBoxInfo(parameter, page, pageSize), result, (System.currentTimeMillis() - csqlst));
					getAreaName(result, 0, parameter);
				}
				setPackagePageResult(rm, total, page, pageSize);
				setPackageBasisResult(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), result);
			}
		} catch (Exception e) {
			logger.error("LogId:{}upgradeTaskAddSearch exception:{}", parameter.get("loguuid"), e);
			setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "新建升级任务页面的查询页面接口异常", "");
		}
		logger.info("LogId:{}查询新建升级任务网关信息返回数据:{}业务处理总耗时:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - st));
		return rm;
	}

	@Override
	public Map<String, Object> upgradeTaskAddSetting(Map<String, Object> parameter) {
		parameter.put("loguuid", UniqueUtil.uuid());
		long st = System.currentTimeMillis();
		logger.info("LogId:{}新建升级任务设置页面网关信息查询传入参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		try {
			String[] parmlist = { "factoryCode", "firmwareId", "boxModel", "uid" };
			if (checkParm(parameter, rm, Arrays.asList(parmlist))) {
				Map<String, Integer> total = new HashMap<String, Integer>();// JAVA里只有引用
				total.put("total", 0);// 数据总数
				int page = Integer.parseInt(StringUtils.isNotBlank(parameter.get("page") + "") ? parameter.get("page") + "" : "1");
				int pageSize = Integer.parseInt(StringUtils.isNotBlank(parameter.get("pageSize") + "") ? parameter.get("pageSize") + "" : "10");
				long crt = System.currentTimeMillis();
				List<Map<String, String>> result = getChoosedResult(parameter, rm, page, pageSize, total);// 返回数据集
				logger.info("LogId:{}获取根据选择结果查询的数据:{}耗时:{}", parameter.get("loguuid"), parameter, (System.currentTimeMillis() - crt));
				setPackagePageResult(rm, total.get("total"), page, pageSize);
				setPackageBasisResult(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), result);
			}
		} catch (Exception e) {
			logger.error("LogId:{}upgradeTaskAddSetting exception:{}", parameter.get("loguuid"), e);
			setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "新建升级任务设置页面接口异常", "");
		}
		logger.info("LogId:{}查询新建升级任务设置页面网关信息返回数据:{}业务处理总耗时:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - st));
		return rm;
	}

	@Override
	public Map<String, Object> addUpgradeTask(Map<String, Object> parameter) {
		parameter.put("loguuid", UniqueUtil.uuid());
		long st = System.currentTimeMillis();
		logger.info("LogId:{}新增升级任务传入参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		try {
			Map<String, Integer> total = new HashMap<String, Integer>();// JAVA里只有引用
			total.put("total", 0);// 数据总数
			int page = Integer.parseInt(StringUtils.isNotBlank(parameter.get("page") + "") ? parameter.get("page") + "" : "1");
			int pageSize = Integer.parseInt(StringUtils.isNotBlank(parameter.get("pageSize") + "") ? parameter.get("pageSize") + "" : "10");
			long crt = System.currentTimeMillis();
			List<Map<String, String>> result = getChoosedResult(parameter, rm, page, pageSize, total);// 返回数据集
			logger.info("LogId:{}获取根据选择结果查询的数据:{}耗时:{}", parameter.get("loguuid"), parameter, (System.currentTimeMillis() - crt));
			if (null != result && result.size() > 0) {
				String firmwareId = StringUtils.isNotBlank(parameter.get("firmwareId") + "") ? parameter.get("firmwareId") + "" : null;// 固件编号UUID
				long fst = System.currentTimeMillis();
				BoxFirmwareInfo bfi = boxFirmwareInfoMapper.selectByPrimaryKey(firmwareId);
				logger.info("LogId:{}获取根据:{}查询的数据:{}耗时:{}", parameter.get("loguuid"), firmwareId, (System.currentTimeMillis() - fst));
				if (null != bfi) {
					long svt = System.currentTimeMillis();
					BoxFirmwareUpgradeTask boxFirmwareUpgradeTask = boxBuildFirmwareUpgradeTask(parameter, bfi);
					// 任务触发方式：1:定时触发 2:条件触发
					// String taskTriggerMode = StringUtils.isNotBlank(parameter.get("taskTriggerMode") + "") ? parameter.get("taskTriggerMode") + "" : "";
					// 添加升级任务
					boxFirmwareUpgradeTaskMapper.insertSelective(boxFirmwareUpgradeTask);
					logger.info("LogId:{}保存固件升级信息耗时:{}", parameter.get("loguuid"), (System.currentTimeMillis() - svt));
					// // 定时触发时添加升级时间
					// if (taskTriggerMode.equals("1")) {
					// // 添加时间
					// addUpgradeTime(parameter, boxFirmwareUpgradeTask);
					// }
					// 添加升级任务详情
					long svtd = System.currentTimeMillis();
					List<BoxFirmwareUpgradeTaskDetail> detailList = new ArrayList<>();
					List<String> boxIds = new ArrayList<>();
					List<String> detailIds = new ArrayList<>();
					for (Map<String, String> bf : result) {
						BoxFirmwareUpgradeTaskDetail taskDetail = buildTaskDetail(bf.get("boxUuid"), boxFirmwareUpgradeTask.getId(), 0, 0);
						detailList.add(taskDetail);
						boxIds.add(taskDetail.getBoxId());
						detailIds.add(taskDetail.getId());
					}
					if (detailList.size() > 0) {
						boxFirmwareUpgradeTaskDetailMapper.batchInsert(detailList);
					}
					logger.info("LogId:{}保存固件升级明细信息耗时:{}", parameter.get("loguuid"), (System.currentTimeMillis() - svtd));


					//事件为立即升级立即执行任务
					String taskTriggerMode = null != parameter.get("taskTriggerMode") ? parameter.get("taskTriggerMode").toString() : "";
					int taskTriggerEvent = null != parameter.get("taskTriggerEvent") ? Integer.valueOf(parameter.get("taskTriggerEvent").toString()) : 1;
					if("2".equals(taskTriggerMode) && (0 == taskTriggerEvent))
					{
						BoxBackupTaskParameter taskParameter = new BoxBackupTaskParameter();
						taskParameter.setUserName(userName);
						taskParameter.setPassword(password);
						taskParameter.setBoxFirmwareInfo(bfi);
						taskParameter.setBoxInvokeInsService(boxInvokeInsService);
						taskParameter.setTaskId(boxFirmwareUpgradeTask.getId());
						taskParameter.setBoxIds(boxIds);
						taskParameter.setDetailIds(detailIds);
						taskParameter.setDetailMapper(boxFirmwareUpgradeTaskDetailMapper);

						new Thread(new BoxUpgradeTaskThread(taskParameter)).start();
					}

					saveOperationDiagnoseLog(parameter, rm, "新增升级任务", logManagerService);// 记录操作日志
					setPackageBasisResult(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), "");
				} else {
					setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "未获取机顶盒固件信息", "");
				}
			} else {
				setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "未查询到机顶盒信息", "");
			}
		} catch (Exception e) {
			logger.error("LogId:{}addUpgradeTask Exception " + e.getMessage(), parameter.get("loguuid"), e);
		}
		logger.info("LogId:{}保存固件升级任务返回数据:{}业务处理总耗时:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - st));
		return rm;
	}

	/**
	 * 获取根据选择结果
	 * 
	 * @param parameter
	 *            参数
	 * @param rm
	 *            返回对象
	 * @param page
	 *            当前页数
	 * @param pageSize
	 *            每页面条数
	 * @param total
	 *            数据总数
	 * @return
	 */
	private List<Map<String, String>> getChoosedResult(Map<String, Object> parameter, Map<String, Object> rm, int page, int pageSize, Map<String, Integer> total) {
		String submitWay = StringUtils.isNotBlank(parameter.get("submitWay") + "") ? parameter.get("submitWay") + "" : "";// 查询方式check_choose选择复选框方式,fast_choose快速选择方式
		List<Map<String, String>> result = null;// 返回数据集
		switch (submitWay) {
		case "checkChoose":// 快速选择方式
			String boxInfoIds = StringUtils.isNotBlank(parameter.get("boxInfoIds") + "") ? parameter.get("boxInfoIds") + "" : null;// 选择的机顶盒ID集合
			if (StringUtils.isNotBlank(boxInfoIds)) {
				String[] ids = boxInfoIds.split(",");
				if (null != ids && ids.length > 0) {
					long ccsqlt = System.currentTimeMillis();
					result = boxFirmwareUpgradeTaskMapper.selectBoxFirmwareInfoByBoxIds(getPackageBoxIds(parameter, page, pageSize, Arrays.asList(ids)));
					logger.info("LogId:{}获取根据机顶盒ID{}查询的数据:{}耗时:{}", parameter.get("loguuid"), ids, result, (System.currentTimeMillis() - ccsqlt));
					if (null != result && result.size() > 0) {
						long ccsqltc = System.currentTimeMillis();
						total.put("total", boxFirmwareUpgradeTaskMapper.countSelectBoxFirmwareInfoByBoxIds(getPackageBoxIds(parameter, page, pageSize, Arrays.asList(ids))));
						logger.info("LogId:{}统计根据机顶盒ID{}查询的数据总数:{}耗时:{}", parameter.get("loguuid"), ids, total.get("total"), (System.currentTimeMillis() - ccsqltc));
						getAreaName(result, 0, parameter);
					}
				} else {
					setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "未获取机顶盒信息ID值", "");
				}
			} else {
				setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "请勾选要升级的机顶盒信息", "");
			}
			break;
		case "all":// 全部
			// int submitNum = StringUtils.isNotBlank(parameter.get("submitNum") + "") ? Integer.valueOf(parameter.get("submitNum") + "") : 10;// 快速选择条数
			// if (pageSize > submitNum) pageSize = submitNum;
			long ast = System.currentTimeMillis();
			result = boxFirmwareUpgradeTaskMapper.selectBoxFirmwareInfo(getPackageBoxInfo(parameter, page, pageSize));// 查询升级
			logger.info("LogId:{}根据查询条件{}获取所有查询的数据:{}耗时:{}", parameter.get("loguuid"), getPackageBoxInfo(parameter, page, pageSize), result, (System.currentTimeMillis() - ast));
			if (null != result && result.size() > 0) {
				long cast = System.currentTimeMillis();
				total.put("total", boxFirmwareUpgradeTaskMapper.countSelectBoxFirmwareInfo(getPackageBoxInfo(parameter, page, pageSize)));// 查询升级总数
				logger.info("LogId:{}根据查询条件{}统计所有查询的数据总数:{}耗时:{}", parameter.get("loguuid"), getPackageBoxInfo(parameter, page, pageSize), total.get("total"), (System.currentTimeMillis() - cast));
				// if (total.get("total").intValue() > submitNum) total.put("total", submitNum);
				getAreaName(result, 0, parameter);
			}
			break;
		default:
			setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "未知的取查询方式", "");
			break;
		}

		return result;
	}

	/**
	 * 获取区域名称
	 * 
	 * @param result
	 *            数据库查询数据
	 * @param taskId
	 *            是否查询升级进度0:否;1:是
	 * @param parameter
	 *            参数集
	 */
	private void getAreaName(List<Map<String, String>> result, int taskId, Map<String, Object> parameter) {
		List<Integer> areaIds = new ArrayList<>();// 存放区域ID集合
		long areast = System.currentTimeMillis();
		for (Map<String, String> rb : result) {
			if (StringUtils.isNotBlank(rb.get("areaId"))) {
				areaIds.add(Integer.parseInt(rb.get("areaId").trim()));
			}
			if (1 == taskId) {// 查询升级任务完成升级进度
				rb.put("upgradeProcess", getTaskUpgradeProgress(rb.get("id"), parameter));
			}
		}
		logger.info("LogId:{}根据查询区域返回数据:{}组装批量查询区域信息数据耗时:{}", parameter.get("loguuid"), result, (System.currentTimeMillis() - areast));
		long dart = System.currentTimeMillis();
		if (null != areaIds && areaIds.size() > 0) {
			List<Area> arealist = amsAreaService.findAreasByIds(areaIds);
			if (null != arealist && arealist.size() > 0) {
				for (Map<String, String> rb : result) {
					if (StringUtils.isNotBlank(rb.get("areaId"))) {
						for (Area a : arealist) {
							if (null != a && a.getId().intValue() == Integer.parseInt(rb.get("areaId").trim())) {
								rb.put("areaName", a.getName());
							}
						}
					}
				}
			}
		}
		logger.info("LogId:{}根据arealist数据返回中文区域名称数据:{}耗时:{}", parameter.get("loguuid"), result, (System.currentTimeMillis() - dart));
	}

	/**
	 * 根据参数封装查询盒子对象
	 * 
	 * @param parameter
	 *            参数集
	 * @return
	 */
	private Map<String, Object> getPackageBoxInfo(Map<String, Object> parameter, int page, int pageSize) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("factoryCode", parameter.get("factoryCode"));// 机顶盒厂商编码
		map.put("firmwareId", parameter.get("firmwareId"));// 固件编号UUID
		map.put("boxModel", parameter.get("boxModel") + "");// 机顶盒型号
		map.put("firmwarePreviousId", parameter.get("firmwarePreviousId"));
		String areaId = null != parameter.get("areaId") ? parameter.get("areaId").toString() : null;
		// 页面未传区域ID则获取用户ID查询用户所有区域
		long st = System.currentTimeMillis();
		areaId = StringUtils.isNotBlank(areaId) ? getAreaIds(amsAreaService.findChildArea(Integer.valueOf(areaId))) : getAreaIds(amsAreaService.findAreaByAdmin(parameter.get("uid") + ""));
		logger.info("LogId:{}根据区域ID:{}或用户ID:{}查询区域编码:{}耗时:{}", parameter.get("loguuid"), parameter.get("areaId"), parameter.get("uid"), areaId, (System.currentTimeMillis() - st));
		map.put("areaId", areaId);// 区域
		int lbound = (page - 1) * pageSize;
		int mbound = pageSize;
		map.put("lbound", lbound);
		map.put("mbound", mbound);
		return map;
	}

	/**
	 * 根据参数封装查询盒子对象
	 * 
	 * @param parameter
	 * @param page
	 * @param pageSize
	 * @param boxIds
	 * @return
	 */
	private Map<String, Object> getPackageBoxIds(Map<String, Object> parameter, int page, int pageSize, List<String> boxIds) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("firmwareId", parameter.get("firmwareId"));// 固件编号UUID
		map.put("firmwarePreviousId", parameter.get("firmwarePreviousId"));
		String areaId = null != parameter.get("areaId") ? parameter.get("areaId").toString() : null;
		// 页面未传区域ID则获取用户ID查询用户所有区域
		long st = System.currentTimeMillis();
		areaId = StringUtils.isNotBlank(areaId) ? getAreaIds(amsAreaService.findChildArea(Integer.valueOf(areaId))) : getAreaIds(amsAreaService.findAreaByAdmin(parameter.get("uid") + ""));
		logger.info("LogId:{}根据区域ID:{}或用户ID:{}查询区域编码:{}耗时:{}", parameter.get("loguuid"), parameter.get("areaId"), parameter.get("uid"), areaId, (System.currentTimeMillis() - st));
		map.put("areaId", areaId);// 区域
		int lbound = (page - 1) * pageSize;
		int mbound = pageSize;
		map.put("lbound", lbound);
		map.put("mbound", mbound);
		map.put("boxIds", boxIds);
		return map;
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
							setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), parm + "参数不能为空", "");
							return false;// 目前所有的参数都为必传所以只要有一个为空返加false
						}
					}
				}
			}
			if (countParm > 0) {// 请求参数与要求参数个数不匹配
				isTrue = false;
				setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "接口必传参数不全", "");
			}
		} else {
			setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "请求接口参数为空", "");
		}
		return isTrue;
	}

	/**
	 * 查询升级任务的升级进度
	 *
	 * @param id
	 *            任务ID
	 * @param parameter
	 *            参数集
	 * @return
	 */
	private String getTaskUpgradeProgress(String id, Map<String, Object> parameter) {
		Map<String, String> map = new HashMap<>();
		map.put("upgradeTaskId", id);
		map.put("status", null);
		long stt = System.currentTimeMillis();
		int total = boxFirmwareUpgradeTaskDetailMapper.searchTaskDetailCount(map);// 获取任务的网关总数
		logger.info("LogId:{}根据条件:{}获取任务的网关总数:{}耗时{}", parameter.get("loguuid"), map, total, (System.currentTimeMillis() - stt));

		BoxFirmwareUpgradeTaskDetail detail = new BoxFirmwareUpgradeTaskDetail();
		detail.setUpgradeTaskId(id);
		int nosuccess = boxFirmwareUpgradeTaskDetailMapper.queryCountNoComplete(detail);// 获取已经完成的网关数
		logger.info("LogId:{}根据条件:{}获取已经完成的网关数:{}耗时:{}", parameter.get("loguuid"), map, (total - nosuccess), (System.currentTimeMillis() - stt));
		return (total - nosuccess) + "/" + total;
	}

	/**
	 * 封装最基础的返回内容
	 * 
	 * @param rm
	 *            返回对象
	 * @param code
	 *            返回状态代码
	 * @param msg
	 *            状态代码描述
	 * @param data
	 *            返回数据
	 */
	private void setPackageBasisResult(Map<String, Object> rm, String code, String msg, Object data) {
		rm.put(Constant.CODE, code);
		rm.put(Constant.MESSAGE, msg);
		rm.put(Constant.DATA, data);
	}

	/**
	 * 封装返回分页内容
	 * 
	 * @param rm
	 *            返回对象
	 * @param total
	 *            数据总数
	 * @param page
	 *            当前页下标号
	 * @param pageSize
	 *            每页大小
	 */
	private void setPackagePageResult(Map<String, Object> rm, int total, int page, int pageSize) {
		rm.put("total", total);
		rm.put("page", page);
		rm.put("pageSize", pageSize);
	}

	/**
	 * BoxFirmwareUpgradeTask对象组装
	 *
	 * @param parameter
	 *            参数集
	 * @return
	 */
	private BoxFirmwareUpgradeTask boxBuildFirmwareUpgradeTask(Map<String, Object> parameter, BoxFirmwareInfo bfi) {
		BoxFirmwareUpgradeTask bfut = new BoxFirmwareUpgradeTask();
		bfut.setId(UniqueUtil.uuid());// 升级任务编号UUID
		bfut.setTaskName(null != parameter.get("taskName") ? parameter.get("taskName").toString() : "");// 任务名称
		bfut.setTaskStartTime(StringUtils.isNotBlank(parameter.get("startTime") + "") ? parameter.get("startTime") + "" : "");// 任务开始时间
		bfut.setTaskEndTime(StringUtils.isNotBlank(parameter.get("endTime") + "") ? parameter.get("endTime") + "" : "");// 任务结束时间
		bfut.setTaskCreateTime(DateTools.getCurrentSecondTime());// 创建时间
		bfut.setTaskStatus(UpgradeTaskStatusEnum.NEW.code());// 任务状态
		bfut.setFactoryCode(bfi.getFactoryCode());// 厂商编码
		bfut.setAreaId(null != parameter.get("areaId") ? parameter.get("areaId").toString() : "0");// 区域ID
		bfut.setCurrentFirmwareUuid(bfi.getId());// 当前固件版本UUID
		// bfut.setTargetFirmwareId(targetFirmwareId);// 目标固件文件编号UUID
		// 任务触发方式：1:定时触发 2:条件触发
		String taskTriggerMode = null != parameter.get("taskTriggerMode") ? parameter.get("taskTriggerMode").toString() : "";
		bfut.setTaskTriggerMode(Integer.valueOf(taskTriggerMode));
		// 为条件触发时添加触发事件：任务触发事件 1:初始安装第一次启动时 2：周期心跳上报时 3：开机登录时
		switch (taskTriggerMode) {
		case "1":
			// 定时触发包含：当天执行和本周执行
			// String taskPeriod = null != parameter.get("taskPeriod") ? parameter.get("taskPeriod").toString() : "0";
			// bfut.setTaskPeriod(Integer.valueOf(taskPeriod));// 升级任务执行周期
			bfut.setTaskTriggerEvent(0);//
			break;
		case "2":
			int taskTriggerEvent = null != parameter.get("taskTriggerEvent") ? Integer.valueOf(parameter.get("taskTriggerEvent").toString()) : 1;
			bfut.setTaskTriggerEvent(taskTriggerEvent);
			if(0 == taskTriggerEvent)
			{
				bfut.setTaskStatus(UpgradeTaskStatusEnum.PROCESSING.code());// 立即升级，修改状态为：正在升级处理中
			}
			// bfut.setTaskPeriod(0);
			break;
		default:
			break;
		}
		bfut.setTaskDescription(null != parameter.get("taskDescription") ? parameter.get("taskDescription").toString() : "");// 任务描述
		return bfut;
	}

	/**
	 * 获取区域ID,以英文逗号分隔
	 * 
	 * @param areas
	 *            区域对象
	 * @return
	 */
	private String getAreaIds(List<Area> areas) {
		if (null != areas && areas.size() > 0) {
			StringBuffer sb = new StringBuffer();
			sb.append("(");
			for (Area area : areas) {
				sb.append("," + area.getId());
			}
			sb.append(")");
			String areaId = sb.toString().replaceFirst(",", "");
			return areaId;
		}
		return null;
	}

	@Override
	public Map<String, Object> upgradeImmediately(Map<String, Object> parameter) {
		long st = System.currentTimeMillis();
		if (null == parameter.get("loguuid") || StringUtils.isBlank(parameter.get("loguuid") + "")) {
			parameter.put("loguuid", UniqueUtil.uuid());// Inform上报时会带loguuid
		}
		logger.info("LogId:{}获取立即升级请求参数:{}", parameter.get("loguuid"), parameter);
		Map<String, Object> rm = new HashMap<>();
		try {
			if (checkParm(parameter, rm, Arrays.asList(new String[] { "boxId", "firmwareId" }))) {
				long bist = System.currentTimeMillis();
				BoxInfo bi = boxInfoService.selectByPrimaryKey(parameter.get("boxId") + "");
				logger.info("LogId:{}查询机顶盒信息:{}消耗时间:{}", parameter.get("loguuid"), bi, (System.currentTimeMillis() - bist));
				if (null != bi) {
					long bfist = System.currentTimeMillis();
					BoxFirmwareInfo bfi = boxFirmwareInfoMapper.selectByPrimaryKey(parameter.get("firmwareId") + "");
					logger.info("LogId:{}查询固件信息:{}消耗时间:{}", parameter.get("loguuid"), bfi, (System.currentTimeMillis() - bfist));
					if (null != bfi) {
						long sty = System.currentTimeMillis();
						String taskId = null != parameter.get("taskId") ? parameter.get("taskId") + "" : "";// 升级任务Id
						logger.info("LogId:{}获取taskId参数:{},如果为空创建固件任务再升级", parameter.get("loguuid"), taskId);
						if (StringUtils.isNotBlank(taskId)) {
							executionUpgradeImmediately(parameter, rm, taskId, bi, bfi);// 执行立即升级
						} else {
							taskId = getTaskIdAndSaveTaskInfo(bi, bfi);// 获取固件任务ID并且保存升级任务
							SaveTaskDetailInfo(bi, bfi, taskId);// 保存固件升级明细信息
							executionUpgradeImmediately(parameter, rm, taskId, bi, bfi);// 执行立即升级
						}
						logger.info("LogId:{}业务处理消耗时间:{}", parameter.get("loguuid"), (System.currentTimeMillis() - sty));
					} else {
						setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "未查询到固件信息信息", "");
					}
				} else {
					setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "未查询到机顶盒信息", "");
				}
			}
		} catch (Exception e) {
			logger.error("LogId:{}upgrade Immediately Exception " + e.getMessage(), parameter.get("loguuid"), e);
			setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "固件立即升级接口异常", "");
		}
		logger.info("LogId:{}立即升级接口返回内容:{}总消耗时间:{}", parameter.get("loguuid"), rm, (System.currentTimeMillis() - st));
		return rm;
	}

	/**
	 * 执行立即升级
	 * 
	 * @param parameter
	 *            参数集
	 * @param rm
	 *            返回对象
	 * @param taskId
	 *            任务ID
	 * @param bi
	 *            机顶盒对象
	 * @param bfi
	 *            固件对象
	 */
	private void executionUpgradeImmediately(Map<String, Object> parameter, Map<String, Object> rm, String taskId, BoxInfo bi, BoxFirmwareInfo bfi) {
		Map<String, Object> upgradeJob = new HashMap<>();
		upgradeJob.put("boxUuid", bi.getBoxUuid());
		upgradeJob.put("commandKey", "");
		upgradeJob.put("methodName", "Download");
		upgradeJob.put("fileType", "1 Firmware Upgrade Image");
		upgradeJob.put("url", bfi.getFirmwarePath());
		upgradeJob.put("userName", userName);
		upgradeJob.put("passWord", password);
		String targetFileName = bfi.getFirmwarePath();
		logger.info("LogId:{}固件升级文件路径:{}", parameter.get("loguuid"), targetFileName);
		if (StringUtils.isNotBlank(targetFileName) && targetFileName.lastIndexOf("/") > 0) {
			targetFileName = targetFileName.substring(targetFileName.lastIndexOf("/") + 1);
			upgradeJob.put("targetFileName", targetFileName);
			logger.info("LogId:{}根据固件升级文件路径获取目标文件的名字:{}", parameter.get("loguuid"), targetFileName);
		} else {
			upgradeJob.put("targetFileName", "update.zip");
			logger.info("LogId:{}未获取目标文件的名字取默认的文件名称:{}", parameter.get("loguuid"), "update.zip");
		}
		upgradeJob.put("successURL", "");
		upgradeJob.put("failureURL", "");
		upgradeJob.put("fileSize", bfi.getFirmwareSize());
		upgradeJob.put("delaySeconds", "0");
		upgradeJob.put("taskId", taskId);
		upgradeJob.put("loguuid", parameter.get("loguuid"));
		long bist = System.currentTimeMillis();
		logger.info("LogId:{}执行立即升级请求的参数:{}", parameter.get("loguuid"), upgradeJob);
		Map<String, Object> respMap = boxInvokeInsService.executeOne(upgradeJob);
		logger.error("LogId:{}接口返回的参数:{}返回消耗时间:{}", parameter.get("loguuid"), respMap, (System.currentTimeMillis() - bist));
		if (respMap.size() > 0 && StringUtils.isNotBlank(String.valueOf(respMap.get("resultCode"))) && 0 == (Integer) respMap.get("resultCode")) {
			setPackageBasisResult(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), "");
		} else {
			setPackageBasisResult(rm, RespCodeEnum.RC_1.code(), "执行立即升级失败", "");
		}
		String operation = null != parameter.get("operation") && StringUtils.isNotBlank(parameter.get("operation") + "") ? parameter.get("operation") + "" : "页面触发固件升级任务";
		saveOperationDiagnoseLog(parameter, rm, operation, logManagerService);
	}

	/**
	 * 获取固件任务ID并且保存升级任务
	 * 
	 * @param bi
	 *            机顶盒对象
	 * @param bfi
	 *            固件对象
	 * @return
	 */
	private String getTaskIdAndSaveTaskInfo(BoxInfo bi, BoxFirmwareInfo bfi) {
		String taskId = UniqueUtil.uuid();
		BoxFirmwareUpgradeTask bfut = new BoxFirmwareUpgradeTask();
		bfut.setId(taskId);// 升级任务编号UUID
		bfut.setTaskName("立即升级");// 升级任务名称
		bfut.setTaskStartTime("");// 升级任务时间段开始时间
		bfut.setTaskEndTime("");// 升级任务时间段结束时间'
		bfut.setTaskCreateTime(DateTools.getCurrentSecondTime());// 任务创建时间
		bfut.setTaskStatus(UpgradeTaskStatusEnum.PROCESSING.code());// 升级状态成功 0:新加任务, 1:正在升级处理中, 2:升级任务结束
		bfut.setFactoryCode(bi.getBoxFactoryCode());// 厂商编码
		bfut.setAreaId(bi.getBoxAreaId());// 城市区域编号
		bfut.setCurrentFirmwareUuid(bfi.getId());// 当前固件版本UUID
		bfut.setTargetFirmwareId("");// 目标固件文件编号UUID
		bfut.setTaskTriggerMode(1);// 任务触发方式 1:即时触发 2:条件触发
		bfut.setTaskTriggerEvent(0);// 任务触发事件 0:未知 1:初始安装第一次启动时 2：周期心跳上报时 3：开机登录时
		bfut.setTaskDescription("执行立即升级");// 描述
		bfut.setTaskPeriod(0);// 任务执行周期 0:当天 1:本周
		boxFirmwareUpgradeTaskMapper.insertSelective(bfut);
		return taskId;
	}

	/**
	 * 保存固件升级明细信息
	 * 
	 * @param bi
	 *            机顶盒对象
	 * @param bfi
	 *            固件对象
	 * @param taskId
	 *            升级任务ID
	 * @throws Exception
	 */
	private void SaveTaskDetailInfo(BoxInfo bi, BoxFirmwareInfo bfi, String taskId) throws Exception {
		BoxFirmwareUpgradeTaskDetail bfutd = new BoxFirmwareUpgradeTaskDetail();
		bfutd.setId(UniqueUtil.uuid());// 任务详情编号UUID
		bfutd.setUpgradeTaskId(taskId);// 升级任务编号UUID
		bfutd.setBoxId(bi.getBoxUuid());// 机顶盒UUID
		bfutd.setStatus(UpgradeTaskStatusEnum.PROCESSING.code());// 升级状态 0:等待升级, 1:升级中, 2:升级失败 ， 3:升级成功
		bfutd.setUpgradeStartTime(DateTools.getCurrentSecondTime());// 此网关升级的实际开始时间
		bfutd.setUpgradeEndTime(0);// 此网关升级的实际结束时间
		bfutd.setIsRetry(false);// 升级失败后是否重新发起升级 0:否 1:是
		bfutd.setRetryTimes(0);// 升级失败后重新发起升级的次数
		boxFirmwareUpgradeTaskDetailMapper.insertSelective(bfutd);
	}

	/**
	 * 构建任务明细对象
	 * 
	 * @param boxUuid
	 *            机顶盒UUID
	 * @param upgradeTaskId
	 *            升级任务编号UUID
	 * @param status
	 *            状态
	 * @param startTime
	 *            开始时间
	 * @return
	 */
	private BoxFirmwareUpgradeTaskDetail buildTaskDetail(String boxUuid, String upgradeTaskId, int status, int startTime) {
		BoxFirmwareUpgradeTaskDetail bfutd = new BoxFirmwareUpgradeTaskDetail();
		bfutd.setId(UniqueUtil.uuid());// 任务详情编号UUID
		bfutd.setUpgradeTaskId(upgradeTaskId);// 升级任务编号UUID
		bfutd.setBoxId(boxUuid);// 机顶盒UUID
		bfutd.setStatus(status);// 升级状态 0:新加任务, 1:失败, 2:成功 ， 3:升级中
		bfutd.setUpgradeStartTime(startTime);// 此网关升级的实际开始时间
		bfutd.setUpgradeEndTime(0);// 此网关升级的实际结束时间
		bfutd.setIsRetry(false);// 升级失败后是否重新发起升级 0:否 1:是
		bfutd.setRetryTimes(0);// 升级失败后重新发起升级的次数
		return bfutd;
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
		parameter.put("categoryMenu", CategoryEnum.UPGRADE_MANAGER_SERVICE.name());// 类目ID
		parameter.put("operation", operation);// 具体的操作
		parameter.put("categoryMenuName", CategoryEnum.UPGRADE_MANAGER_SERVICE.description());// 类目名称
		parameter.put("content", "请求报文" + JSON.toJSONString(parameter) + ",返回报文" + JSON.toJSONString(rm));// 操作的数据内容
		if (null == parameter.get("userName") || null == parameter.get("roleName") || StringUtils.isBlank(parameter.get("userName") + "") || StringUtils.isBlank(parameter.get("roleName") + "")) {
			parameter.put("logType", LogTypeEnum.LOG_TYPE_SYSTEM.code());// 为上报触发升级时，写系统日志
			LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(parameter), "", parameter.get("boxId").toString(), operation, LogTypeEnum.LOG_TYPE_SYSTEM.description());
		} else {
			parameter.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());// 为页面操作升级是，写操作日志
			logManagerService.recordOperationLog(parameter);// 写操作日志
		}

	}

	@Override
	public void bootEventUpgradeImmediately(String logid, String serialNumber, String factoryCode, int evnt) {
		try {
			logger.info("LogId:{}Inform上报固件升级serialNumber参数:{}factoryCode参数:{}evnt参数:{}", logid, serialNumber, factoryCode, evnt);
			if (StringUtils.isNotBlank(serialNumber) && StringUtils.isNotBlank(factoryCode)) {
				Map<String, Object> sql = new HashMap<>();
				sql.put("evnt", evnt);
				sql.put("status", 0);
				sql.put("serialNumber", serialNumber);
				sql.put("factoryCode", factoryCode);
				List<Map<String, Object>> bfutdlist = boxFirmwareUpgradeTaskDetailMapper.selectByFactoryCodeSerialNumberEvntStatus(sql);
				logger.info("LogId:{}据任务触发事件-升级状态-机顶盒序列号-机顶盒厂商编码条件:{}查询数据返回数据条数:{}", logid, sql, bfutdlist.size());
				if (null != bfutdlist && bfutdlist.size() > 0) {
					Map<String, Object> taskDetail = bfutdlist.get(0);// 多条数据执行一个,其余的等下次Inform上报再处理
					logger.info("LogId:{}当前升级对象信息taskDetailId-boxId-taskId-IDfirmwareId:{}", logid, taskDetail);
					taskDetail.put("loguuid", logid);// 日志ID
					taskDetail.put("operation", "BOOT触发固件升级任务");// 记录日志操作说明
					Map<String, Object> result = upgradeImmediately(taskDetail);// 执行立即升级
					if (null != result && result.size() > 0 && "0".equals(result.get(Constant.CODE))) {
						Map<String, Object> paramter = new HashMap<>();
						paramter.put("taskId", taskDetail.get("taskId"));
						paramter.put("taskStatus", UpgradeTaskStatusEnum.PROCESSING.code());
						boxFirmwareUpgradeTaskMapper.updateUpgradeTaskStatus(paramter);
						logger.info("LogId:{}更新固件升级计划ID为:{}的状态:{}", logid, taskDetail.get("taskId"), UpgradeTaskStatusEnum.PROCESSING.code());
						BoxFirmwareUpgradeTaskDetail bfutd = new BoxFirmwareUpgradeTaskDetail();
						bfutd.setId(taskDetail.get("taskDetailId") + "");
						bfutd.setStatus(UpgradeTaskDetailStatusEnum.PROCESSING.code());
						bfutd.setUpgradeStartTime(DateTools.getCurrentSecondTime());
						boxFirmwareUpgradeTaskDetailMapper.updateByPrimaryKeySelective(bfutd);
						logger.info("LogId:{}更新固件升级计划明细ID为:{}的状态:{}", logid, taskDetail.get("taskDetailId"), UpgradeTaskStatusEnum.PROCESSING.code());
					}
				}
			}
		} catch (Exception e) {
			logger.error("LogId:{} bootEventUpgradeImmediately Exception " + e.getMessage(), e);
		}
		logger.info("LogId:{}bootEventUpgradeImmediately方法执行完成", logid);
	}

	@Override
	public void updateTaskDetailStatus(String logid, String taskId, String requestId, int status) {
		logger.info("LogId:{}进入updateTaskDetailStatus方法,taskId:{} requestId:{} status:{}", logid, taskId, requestId, status);
		try {
			if (StringUtils.isNotBlank(taskId) && StringUtils.isNotBlank(requestId)) {
				Map<String, String> map = instructionsService.getInstructionsInfo(requestId);
				logger.info("LogId:{}通过requestId查询信息为:{} null!=map:{} map.size()>0:{}", logid, map, null != map, map.size() > 0);
				if (null != map && map.size() > 0) {
					String boxId = map.get("cpeIdentity");
					logger.info("LogId:{}map.get(cpeIdentity):{} StringUtils.isNotBlank(boxId):{}", logid, boxId, StringUtils.isNotBlank(boxId));
					if (StringUtils.isNotBlank(boxId)) {
						BoxFirmwareUpgradeTaskDetail bfutd = new BoxFirmwareUpgradeTaskDetail();
						bfutd.setBoxId(boxId);// 盒子ID
						bfutd.setUpgradeTaskId(taskId);// 固件升级ID
						List<BoxFirmwareUpgradeTaskDetail> bfutdlist = boxFirmwareUpgradeTaskDetailMapper.selectTaskDetailByTaskIdAndBoxId(bfutd);
						logger.info("LogId:{}boxId为:{}taskId为:{}查询固件升级任务明细数为:{}条 null!=bfutdlist:{} bfutdlist.size()>0:{}", logid, boxId, taskId, bfutdlist.size(), null != bfutdlist, bfutdlist.size() > 0);
						if (null != bfutdlist && bfutdlist.size() > 0) {
							bfutd = bfutdlist.get(0);
							bfutd.setStatus(status);// 修改的状态
							int et = (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
							bfutd.setUpgradeEndTime(et);// 完成时间
							boxFirmwareUpgradeTaskDetailMapper.updateByPrimaryKeySelective(bfutd);
							logger.info("LogId:{}更新固件升级明细数据setStatus为:{} setUpgradeEndTime为:{} status==UpgradeTaskDetailStatusEnum.SUCSSESS.code():{}", logid, status, et, status == UpgradeTaskDetailStatusEnum.SUCSSESS.code());
							// 升级成功，更新机顶盒的固件ID
							if (status == UpgradeTaskDetailStatusEnum.SUCSSESS.code()) {
								BoxFirmwareUpgradeTask bfut = boxFirmwareUpgradeTaskMapper.selectByPrimaryKey(taskId);
								logger.info("LogId:{}通过taskId查询BoxFirmwareUpgradeTask对象是不为空:{}", logid, (null != bfut ? true : false));
								if (null != bfut) {
									Map<String, Object> upbis = new HashMap<String, Object>();
									upbis.put("boxUuid", bfutd.getBoxId());// 机顶盒UUID
									upbis.put("boxFirmwareUuid", bfut.getCurrentFirmwareUuid());// 升级后的固件版本信息
									boxFirmwareUpgradeTaskMapper.updateBoxFirmwareUuidByPrimaryKey(upbis);// 更新机顶盒版
									logger.info("LogId:{}更新机顶盒信息setBoxUuid为:{} setBoxFirmwareUuid为:{}", logid, bfutd.getBoxId(), bfut.getCurrentFirmwareUuid());
									// 根据任务ID与任务完成状态统计任务详情是否都完成了,完成则更新任务状态
									Map<String, String> mapsql = new HashMap<>();
									mapsql.put("upgradeTaskId", taskId);
									mapsql.put("status", null);
									long stt = System.currentTimeMillis();
									int total = boxFirmwareUpgradeTaskDetailMapper.searchTaskDetailCount(mapsql);// 获取任务的网关总数
									logger.info("LogId:{}根据条件:{}获取任务的网关总数:{}耗时{}", logid, mapsql, total, (System.currentTimeMillis() - stt));
									mapsql.put("status", "3");
									int success = boxFirmwareUpgradeTaskDetailMapper.searchTaskDetailCount(mapsql);// 获取已经完成的网关数
									logger.info("LogId:{}根据条件:{}获取升级成功败网关数:{}耗时:{}", logid, mapsql, success, (System.currentTimeMillis() - stt));
									mapsql.put("status", "2");
									int lost = boxFirmwareUpgradeTaskDetailMapper.searchTaskDetailCount(mapsql);// 获取已经完成的网关数
									logger.info("LogId:{}根据条件:{}获取升级失败网关数:{}耗时:{}", logid, mapsql, success, (System.currentTimeMillis() - stt));
									if ((total + lost) == success) {
										bfut = new BoxFirmwareUpgradeTask();
										bfut.setId(taskId);
										bfut.setTaskStatus(2);
										boxFirmwareUpgradeTaskMapper.updateByPrimaryKeySelective(bfut);
										logger.info("LogId:{}更新:{}的升级任务状态:{}", logid, taskId, 2);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("LogId:{} updateTaskDetailStatus Exception " + e.getMessage(), e);
		}
		logger.info("LogId:{}updateTaskDetailStatus方法执行完成", logid);
	}
}