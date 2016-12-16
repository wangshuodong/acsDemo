package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.FactoryMapper;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.mapper.MakeInfoMapper;
import com.cmiot.rms.dao.mapper.ManufacturerMapper;
import com.cmiot.rms.dao.model.DeviceInfo;
import com.cmiot.rms.dao.model.Factory;
import com.cmiot.rms.dao.model.MakeInfo;
import com.cmiot.rms.dao.model.Manufacturer;
import com.cmiot.rms.services.CommonSearchService;
import com.cmiot.rms.services.DeviceInfoService;
import com.cmiot.rms.services.util.OperationLogUtil;

/**
 * Created by panmingguo on 2016/4/8.
 */
public class CommonSearchServiceImpl implements CommonSearchService {

	private static Logger logger = LoggerFactory.getLogger(CommonSearchServiceImpl.class);

	@Autowired
	MakeInfoMapper makeInfoMapper;// 制造商

	@Autowired
	private FactoryMapper factoryMapper;// OUI

	@Autowired
	private ManufacturerMapper manufacturerMapper;// 生产商

	@Autowired
	GatewayInfoMapper gatewayInfoMapper;

	@Autowired
	DeviceInfoService deviceInfoService;

	/**
	 * 查询所有制造商
	 *
	 * @return
	 */
	@Override
	public Map<String, Object> queryAllManufacturer() {
		long st = System.currentTimeMillis();
		String logid = UUID.randomUUID().toString().replaceAll("-", "");
		logger.info("logid:{} Start invoke queryAllManufacturer！", logid);
		Map<String, Object> retMap = new HashMap<>();
		try {
			long sqt = System.currentTimeMillis();
			List<MakeInfo> list = makeInfoMapper.querySelective(new MakeInfo(null, null));
			logger.info("logid:{} makeInfoMapper.querySelective consume time:{}", logid, System.currentTimeMillis() - sqt);
			List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
			for (MakeInfo mi : list) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", mi.getId());
				map.put("manufacturerName", mi.getMakeName());
				mapList.add(map);
			}
			retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
			retMap.put(Constant.DATA, JSON.toJSON(mapList));
		} catch (Exception e) {
			logger.error("queryAllManufacturer exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
		}
		logger.info("logid:{} End invoke queryAllManufacturer:{} consume time:{}", logid, retMap, System.currentTimeMillis() - st);
		return retMap;
	}

	/**
	 * 根据制造商ID查询生产商
	 *
	 * @param parameter
	 * @return
	 */
	@Override
	public Map<String, Object> queryForManufacturerId(Map<String, Object> parameter) {
		long st = System.currentTimeMillis();
		String logid = UUID.randomUUID().toString().replaceAll("-", "");
		logger.info("logid:{} Start invoke queryForManufacturerId:{}", logid, parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String manufacturerId = null != parameter.get("manufacturerId") ? parameter.get("manufacturerId").toString() : null;
			long sqt = System.currentTimeMillis();
			List<Manufacturer> list = manufacturerMapper.querySelective(new Manufacturer(null, null, null, manufacturerId));
			logger.info("logid:{} manufacturerMapper.querySelective consume time:{}", logid, System.currentTimeMillis() - sqt);
			List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
			for (Manufacturer mf : list) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", mf.getId());
				map.put("factoryName", mf.getManufacturerName());
				map.put("factoryCode", mf.getId());
				map.put("manufacturerId", mf.getMakeId());
				mapList.add(map);
			}
			retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
			retMap.put(Constant.DATA, JSON.toJSON(mapList));
		} catch (Exception e) {
			logger.error("queryForManufacturerId exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
		}

		logger.info("logid:{}  End invoke queryForManufacturerId:{} onsume time:{}", logid, retMap, System.currentTimeMillis() - st);
		return retMap;
	}

	/**
	 * 根据生产商ID查询OUI
	 */
	@Override
	public Map<String, Object> queryOuiByProductionId(Map<String, Object> parameter) {
		long st = System.currentTimeMillis();
		String logid = UUID.randomUUID().toString().replaceAll("-", "");
		logger.info("logid:{} Start invoke queryOuiByProductionId:{}", logid, parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String manufacturerId = null != parameter.get("manufacturerId") ? parameter.get("manufacturerId").toString() : null;
			long sqt = System.currentTimeMillis();
			List<Factory> list = factoryMapper.queryForManufacturerId(manufacturerId);
			logger.info("logid:{} factoryMapper.queryForManufacturerId consume time:{}", logid, System.currentTimeMillis() - sqt);
			retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
			retMap.put(Constant.DATA, JSON.toJSON(list));
		} catch (Exception e) {
			logger.error("queryOuiByProductionId exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
		}

		logger.info("logid:{} End invoke queryOuiByProductionId:{} onsume time:{}", logid, retMap, System.currentTimeMillis() - st);
		return retMap;
	}

	/**
	 * 根据生产商编码查询设备型号
	 *
	 * @param parameter
	 * @return
	 */
	@Override
	public Map<String, Object> queryDeviceModel(Map<String, Object> parameter) {
		long st = System.currentTimeMillis();
		String logid = UUID.randomUUID().toString().replaceAll("-", "");
		logger.info("logid:{} Start invoke queryDeviceModel:{}", logid, parameter);
		Map<String, Object> retMap = new HashMap<>();
		// 要求不传参数查询所有设备型号
		// if (null == parameter || null == parameter.get("factoryCode")) {
		// retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
		// retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
		// logger.info("End invoke queryDeviceModel:{}", retMap);
		// return retMap;
		// }
		try {
			Map<String, String> dvMap = new HashMap<>();
			DeviceInfo deviceInfoToSearch = new DeviceInfo();
			deviceInfoToSearch.setDeviceFactory(parameter.get("factoryCode").toString());
			long sqt = System.currentTimeMillis();
			List<DeviceInfo> deviceInfoList = deviceInfoService.queryList(deviceInfoToSearch);
			logger.info("logid:{} deviceInfoService.queryList consume time:{}", logid, System.currentTimeMillis() - sqt);
			for (DeviceInfo deviceInfo : deviceInfoList) {
				dvMap.put(deviceInfo.getId(), deviceInfo.getDeviceModel());
			}
			retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
			retMap.put(Constant.DATA, dvMap);
		} catch (Exception e) {
			logger.error("queryDeviceModel exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
		}

		logger.info("logid:{} End invoke queryDeviceModel:{} consume time:{}", logid, retMap, System.currentTimeMillis() - st);
		return retMap;
	}

	/**
	 * 查询所有设备型号
	 *
	 * @return
	 */
	@Override
	public Map<String, Object> queryAllDeviceModel() {
		long st = System.currentTimeMillis();
		String logid = UUID.randomUUID().toString().replaceAll("-", "");
		logger.info("logid:{} Start invoke queryAllDeviceModel", logid);
		Map<String, Object> retMap = new HashMap<>();
		try {
			Map<String, String> dvMap = new HashMap<>();
			DeviceInfo searchInfo = new DeviceInfo();
			long sqt = System.currentTimeMillis();
			List<DeviceInfo> deviceInfoList = deviceInfoService.queryList(searchInfo);
			logger.info("logid:{} queryAllDeviceModel.deviceInfoService.queryList consume time:{}", logid, System.currentTimeMillis() - sqt);
			for (DeviceInfo deviceInfo : deviceInfoList) {
				dvMap.put(deviceInfo.getId(), deviceInfo.getDeviceModel());
			}
			retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
			retMap.put(Constant.DATA, dvMap);
		} catch (Exception e) {
			logger.error("queryAllDeviceModel exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
		}

		logger.info("logid:{} End invoke queryAllDeviceModel:{} consume time:{}", logid, retMap, System.currentTimeMillis() - st);
		return retMap;
	}

	@Override
	public Map<String, Object> addOrUpdateMakeInfo(Map<String, Object> parameter) {
		logger.info("Start invoke addOrUpdateMakeInfoList:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String makeId = null != parameter.get("makeId") ? parameter.get("makeId").toString().trim() : "";
			String makeName = null != parameter.get("makeName") ? parameter.get("makeName").toString() : null;
			if (StringUtils.isBlank(makeName)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
				return retMap;
			}
			List<MakeInfo> mil = makeInfoMapper.queryMakeInfoByMakeName(makeName);
			MakeInfo mi = makeInfoMapper.selectByPrimaryKey(makeId);
			int rce = 0;
			if (null == mi) {
				if (null != mil && mil.size() > 0) {
					retMap.put(Constant.CODE, ErrorCodeEnum.NAME_EXIST.getResultCode());
					retMap.put(Constant.MESSAGE, ErrorCodeEnum.NAME_EXIST.getResultMsg());
					return retMap;
				}
				rce = makeInfoMapper.insert(new MakeInfo(UUID.randomUUID().toString().replace("-", ""), makeName));
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "新增制造商", JSON.toJSONString(parameter));
			} else {
				if (null != mil && mil.size() > 0) {
					for (MakeInfo m : mil) {
						if (!m.getId().equals(mi.getId()) && m.getMakeName().equals(makeName)) {
							retMap.put(Constant.CODE, ErrorCodeEnum.NAME_EXIST.getResultCode());
							retMap.put(Constant.MESSAGE, ErrorCodeEnum.NAME_EXIST.getResultMsg());
							return retMap;
						}
					}
				}
				rce = makeInfoMapper.updateByPrimaryKeySelective(new MakeInfo(mi.getId(), makeName));
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "更新制造商", JSON.toJSONString(parameter));
			}
			if (rce > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
			} else {
				retMap.put(Constant.CODE, ErrorCodeEnum.OPERATION_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.OPERATION_ERROR.getResultMsg());
			}
		} catch (Exception e) {
			logger.error("addOrUpdateMakeInfoList exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		return retMap;
	}

	@Override
	public Map<String, Object> delMakeInfo(Map<String, Object> parameter) {
		logger.info("Start invoke delMakeInfoList:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String makeId = null != parameter.get("makeId") ? parameter.get("makeId").toString().trim() : "";
			if (StringUtils.isBlank(makeId)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
				return retMap;
			}
			int count = manufacturerMapper.countQuerySelective(new Manufacturer(null, null, null, makeId));
			if (count >= 1) {
				retMap.put(Constant.CODE, ErrorCodeEnum.DEL_RELATION_EXIST_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.DEL_RELATION_EXIST_ERROR.getResultMsg());
				return retMap;
			} else {
				int rec = makeInfoMapper.deleteByPrimaryKey(makeId);
				if (rec > 0) {
					retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
					retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
					OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "删除制造商", JSON.toJSONString(parameter));
				} else {
					retMap.put(Constant.CODE, ErrorCodeEnum.DELETE_ERROR.getResultCode());
					retMap.put(Constant.MESSAGE, ErrorCodeEnum.DELETE_ERROR.getResultMsg());
				}
			}
		} catch (Exception e) {
			logger.error("addOrUpdateMakeInfoList exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		return retMap;
	}

	@Override
	public Map<String, Object> queryMakeInfoList(Map<String, Object> parameter) {
		long st = System.currentTimeMillis();
		String logid = UUID.randomUUID().toString().replaceAll("-", "");
		logger.info("logid:{} Start invoke queryMakeInfoList:{}", logid, parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String makeName = null != parameter.get("makeName") ? parameter.get("makeName").toString().trim() : null;
			int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
			int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;
			int lbound = (page - 1) * pageSize;
			int mbound = pageSize;
			Map<String, Object> sql = new HashMap<>();
			sql.put("makeName", makeName);
			sql.put("lbound", lbound);
			sql.put("mbound", mbound);
			long sqt = System.currentTimeMillis();
			List<MakeInfo> list = makeInfoMapper.selectByPage(sql);
			logger.info("logid:{} makeInfoMapper.selectByPage consume time:{}", logid, System.currentTimeMillis() - sqt);
			int total = 0;
			if (null != list && list.size() > 0) {
				long scqt = System.currentTimeMillis();
				total = makeInfoMapper.countSelectByPage(sql);
				logger.info("logid:{} makeInfoMapper.countSelectByPage consume time:{}", logid, System.currentTimeMillis() - scqt);
			}
			setPackagePageResult(retMap, total, page, pageSize);
			setPackageBasisResult(retMap, ErrorCodeEnum.SUCCESS.getResultCode(), ErrorCodeEnum.SUCCESS.getResultMsg(), JSON.toJSON(list));
		} catch (Exception e) {
			logger.error("queryMakeInfoList exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		logger.info("logid:{} End invoke queryMakeInfoList:{} consume time:{}", logid, retMap, System.currentTimeMillis() - st);
		return retMap;
	}

	@Override
	public Map<String, Object> queryManufacturerList(Map<String, Object> parameter) {
		long st = System.currentTimeMillis();
		String logid = UUID.randomUUID().toString().replaceAll("-", "");
		logger.info("logid:{} Start invoke queryManufacturerList:{}", logid, parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String manufacturerName = null != parameter.get("manufacturerName") ? parameter.get("manufacturerName").toString().trim() : null;
			String code = null != parameter.get("code") ? parameter.get("code").toString().trim() : null;
			String makeId = null != parameter.get("makeId") ? parameter.get("makeId").toString().trim() : null;
			int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
			int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;
			int lbound = (page - 1) * pageSize;
			int mbound = pageSize;
			Map<String, Object> sql = new HashMap<>();
			sql.put("manufacturerName", manufacturerName);
			sql.put("code", code);
			sql.put("makeId", makeId);
			sql.put("lbound", lbound);
			sql.put("mbound", mbound);
			long sqt = System.currentTimeMillis();
			List<Manufacturer> list = manufacturerMapper.selectPage(sql);
			logger.info("logid:{} manufacturerMapper.selectPage consume time:{}", logid, System.currentTimeMillis() - sqt);
			int total = 0;
			if (null != list && list.size() > 0) {
				long sfqt = System.currentTimeMillis();
				for (Manufacturer mf : list) {
					if (StringUtils.isNotBlank(mf.getMakeId())) mf.setMakeName(makeInfoMapper.selectByPrimaryKey(mf.getMakeId()).getMakeName());
				}
				logger.info("logid:{} setMakeName consume time:{}", logid, System.currentTimeMillis() - sfqt);
				long scqt = System.currentTimeMillis();
				total = manufacturerMapper.countSelectPage(sql);
				logger.info("logid:{} makeInfoMapper.countSelectByPage consume time:{}", logid, System.currentTimeMillis() - scqt);
			}
			setPackagePageResult(retMap, total, page, pageSize);
			setPackageBasisResult(retMap, ErrorCodeEnum.SUCCESS.getResultCode(), ErrorCodeEnum.SUCCESS.getResultMsg(), JSON.toJSON(list));
		} catch (Exception e) {
			logger.error("queryManufacturerList exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		logger.info("logid:{} End invoke queryManufacturerList:{} consume time:{}", logid, retMap, System.currentTimeMillis() - st);
		return retMap;
	}

	@Override
	public Map<String, Object> addOrUpdateManufacturerInfo(Map<String, Object> parameter) {
		logger.info("Start invoke addOrUpdateManufacturerInfo:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String id = null != parameter.get("manufacturerId") ? parameter.get("manufacturerId").toString().trim() : null;
			String manufacturerName = null != parameter.get("manufacturerName") ? parameter.get("manufacturerName").toString().trim() : null;
			String code = null != parameter.get("code") ? parameter.get("code").toString().trim() : null;
			String makeId = null != parameter.get("makeId") ? parameter.get("makeId").toString().trim() : null;
			if (StringUtils.isBlank(manufacturerName) && StringUtils.isBlank(code) && StringUtils.isBlank(makeId)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
				return retMap;
			}
			List<Manufacturer> mfl = manufacturerMapper.queryManufactureByName(new Manufacturer(null, manufacturerName, code, null));
			Manufacturer mf = manufacturerMapper.selectByPrimaryKey(id);
			int rec = 0;
			if (null == mf) {
				if (null != mfl && mfl.size() > 0) {
					for (Manufacturer m : mfl) {
						// if (makeId.equals(m.getMakeId())) {
						if (code.equalsIgnoreCase(m.getCode())) {
							retMap.put(Constant.CODE, ErrorCodeEnum.CODE_EXIST.getResultCode());
							retMap.put(Constant.MESSAGE, ErrorCodeEnum.CODE_EXIST.getResultMsg());
							return retMap;
						}
						// if (manufacturerName.equals(m.getManufacturerName())) {
						// retMap.put(Constant.CODE, ErrorCodeEnum.NAME_EXIST.getResultCode());
						// retMap.put(Constant.MESSAGE, ErrorCodeEnum.NAME_EXIST.getResultMsg());
						// return retMap;
						// }
						// }
					}
				}
				rec = manufacturerMapper.insertSelective(new Manufacturer(UUID.randomUUID().toString().replace("-", ""), manufacturerName, code, makeId));
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "新增生产商", JSON.toJSONString(parameter));
			} else {
				if (null != mfl && mfl.size() > 0) {
					for (Manufacturer m : mfl) {
						if (!m.getId().equals(mf.getId())) {
							// if(makeId.equals(m.getMakeId())){
							if (code.equalsIgnoreCase(m.getCode())) {
								retMap.put(Constant.CODE, ErrorCodeEnum.CODE_EXIST.getResultCode());
								retMap.put(Constant.MESSAGE, ErrorCodeEnum.CODE_EXIST.getResultMsg());
								return retMap;
							}
							// if (manufacturerName.equals(m.getManufacturerName())) {
							// retMap.put(Constant.CODE, ErrorCodeEnum.NAME_EXIST.getResultCode());
							// retMap.put(Constant.MESSAGE, ErrorCodeEnum.NAME_EXIST.getResultMsg());
							// return retMap;
							// }
							// }
						}
					}
				}
				rec = manufacturerMapper.updateByPrimaryKeySelective(new Manufacturer(mf.getId(), manufacturerName, code, makeId));
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "更新生产商", JSON.toJSONString(parameter));
			}
			if (rec > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
			} else {
				retMap.put(Constant.CODE, ErrorCodeEnum.OPERATION_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.OPERATION_ERROR.getResultMsg());
			}
		} catch (Exception e) {
			logger.error("addOrUpdateManufacturerInfo exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		return retMap;
	}

	@Override
	public Map<String, Object> delManufacturerInfo(Map<String, Object> parameter) {
		logger.info("Start invoke delManufacturerInfo:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String manufacturerId = null != parameter.get("manufacturerId") ? parameter.get("manufacturerId").toString().trim() : null;
			if (StringUtils.isBlank(manufacturerId)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
				return retMap;
			}
			int count = factoryMapper.countQueryList(new Factory(null, null, null, manufacturerId));
			if (count >= 1) {
				retMap.put(Constant.CODE, ErrorCodeEnum.DEL_RELATION_EXIST_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.DEL_RELATION_EXIST_ERROR.getResultMsg());
				return retMap;
			} else {
				int rec = manufacturerMapper.deleteByPrimaryKey(manufacturerId);
				if (rec > 0) {
					retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
					retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
					OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "删除制造商", JSON.toJSONString(parameter));
				} else {
					retMap.put(Constant.CODE, ErrorCodeEnum.DELETE_ERROR.getResultCode());
					retMap.put(Constant.MESSAGE, ErrorCodeEnum.DELETE_ERROR.getResultMsg());
				}
			}
		} catch (Exception e) {
			logger.error("delManufacturerInfo exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		return retMap;
	}

	@Override
	public Map<String, Object> queryOuiList(Map<String, Object> parameter) {
		long st = System.currentTimeMillis();
		String logid = UUID.randomUUID().toString().replaceAll("-", "");
		logger.info("logid:{} Start invoke queryOuiList:{}", logid, parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String factoryName = null != parameter.get("factoryName") ? parameter.get("factoryName").toString().trim() : null;
			String factoryCode = null != parameter.get("factoryCode") ? parameter.get("factoryCode").toString().trim() : null;
			String manufacturerId = null != parameter.get("manufacturerId") ? parameter.get("manufacturerId").toString().trim() : null;
			int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
			int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;
			int lbound = (page - 1) * pageSize;
			int mbound = pageSize;
			Map<String, Object> sql = new HashMap<>();
			sql.put("ouiName", factoryName);
			sql.put("ouicode", factoryCode);
			sql.put("manufacturerId", manufacturerId);
			sql.put("lbound", lbound);
			sql.put("mbound", mbound);
			long sqt = System.currentTimeMillis();
			List<Factory> list = factoryMapper.selectPage(sql);
			logger.info("logid:{} factoryMapper.selectPage consume time:{}", logid, System.currentTimeMillis() - sqt);
			int total = 0;
			if (null != list && list.size() > 0) {
				long sfqt = System.currentTimeMillis();
				for (Factory f : list) {
					if (StringUtils.isNotBlank(f.getManufacturerId())) {
						Manufacturer mf = manufacturerMapper.selectByPrimaryKey(f.getManufacturerId());
						if (null != mf) {
							f.setManufacturerName(mf.getManufacturerName());
							f.setMakeId(mf.getMakeId());
						}
					}
				}
				logger.info("logid:{} setManufacturerName consume time:{}", logid, System.currentTimeMillis() - sfqt);
				long scqt = System.currentTimeMillis();
				total = factoryMapper.countSelectPage(sql);
				logger.info("logid:{} factoryMapper.countSelectPage consume time:{}", logid, System.currentTimeMillis() - scqt);
			}
			setPackagePageResult(retMap, total, page, pageSize);
			setPackageBasisResult(retMap, ErrorCodeEnum.SUCCESS.getResultCode(), ErrorCodeEnum.SUCCESS.getResultMsg(), JSON.toJSON(list));
		} catch (Exception e) {
			logger.error("queryOuiList exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		logger.info("logid:{} End invoke queryManufacturerList:{} consume time:{}", logid, retMap, System.currentTimeMillis() - st);
		return retMap;
	}

	@Override
	public Map<String, Object> addOrUpdateOuiInfo(Map<String, Object> parameter) {
		logger.info("Start invoke addOrUpdateOuiInfo:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String id = null != parameter.get("id") ? parameter.get("id").toString().trim() : null;
			String ouiName = null != parameter.get("ouiName") ? parameter.get("ouiName").toString().trim() : null;
			String ouicode = null != parameter.get("ouiCode") ? parameter.get("ouiCode").toString().trim() : null;
			String manufacturerId = null != parameter.get("manufacturerId") ? parameter.get("manufacturerId").toString().trim() : null;
			if (StringUtils.isBlank(ouicode) || StringUtils.isBlank(manufacturerId)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
				return retMap;
			}
			List<Factory> fl = factoryMapper.selectFactoryByName(new Factory(null, ouicode, null, null));
			Factory factory = factoryMapper.selectByPrimaryKey(id);
			int rec = 0;
			if (null == factory) {
				if (null != fl && fl.size() > 0) {
					for (Factory f : fl) {
						// 生产商相同下
						// if (manufacturerId.equals(f.getManufacturerId())) {
						if (ouicode.equalsIgnoreCase(f.getFactoryCode())) {
							retMap.put(Constant.CODE, ErrorCodeEnum.CODE_EXIST.getResultCode());
							retMap.put(Constant.MESSAGE, ErrorCodeEnum.CODE_EXIST.getResultMsg());
							return retMap;
						}
						// 页面未传OUI名称
						// if (ouiName.equals(f.getFactoryName())) {
						// retMap.put(Constant.CODE, ErrorCodeEnum.NAME_EXIST.getResultCode());
						// retMap.put(Constant.MESSAGE, ErrorCodeEnum.NAME_EXIST.getResultMsg());
						// return retMap;
						// }
						// }
					}
				}
				rec = factoryMapper.insertSelective(new Factory(UUID.randomUUID().toString().replace("-", ""), ouicode, ouiName, manufacturerId));
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "新增OUT", JSON.toJSONString(parameter));
			} else {
				if (null != fl && fl.size() > 0) {
					for (Factory f : fl) {
						// 相同OUI信息下生产商相同下
						// if (!f.getId().equals(factory.getId()) && manufacturerId.equals(f.getManufacturerId())) {
						if (!f.getId().equals(factory.getId()) && ouicode.equalsIgnoreCase(f.getFactoryCode())) {
							retMap.put(Constant.CODE, ErrorCodeEnum.CODE_EXIST.getResultCode());
							retMap.put(Constant.MESSAGE, ErrorCodeEnum.CODE_EXIST.getResultMsg());
							return retMap;
						}
						// 页面未传OUI名称
						// if (ouicode.equals(f.getFactoryCode())) {
						// retMap.put(Constant.CODE, ErrorCodeEnum.CODE_EXIST.getResultCode());
						// retMap.put(Constant.MESSAGE, ErrorCodeEnum.CODE_EXIST.getResultMsg());
						// return retMap;
						// }
						// }
					}
				}
				rec = factoryMapper.updateByPrimaryKeySelective(new Factory(factory.getId(), ouicode, ouiName, manufacturerId));
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "更新OUI", JSON.toJSONString(parameter));
				if (rec > 0 && !ouicode.equals(factory.getFactoryCode())) {
					// 更新网关表里面的gateway_factory_code
					Map<String, Object> par = new HashMap<String, Object>();
					par.put("newCode", ouicode);
					par.put("oldCode", factory.getFactoryCode());
					gatewayInfoMapper.updateGatewayFactoryCode(par);
				}
			}
			if (rec > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
			} else {
				retMap.put(Constant.CODE, ErrorCodeEnum.OPERATION_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.OPERATION_ERROR.getResultMsg());
			}
		} catch (Exception e) {
			logger.error("addOrUpdateOuiInfo exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		return retMap;
	}

	@Override
	public Map<String, Object> delOuiInfo(Map<String, Object> parameter) {
		logger.info("Start invoke delOuiInfo:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String ouiId = null != parameter.get("ouiId") ? parameter.get("ouiId").toString().trim() : null;
			if (StringUtils.isBlank(ouiId)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
				return retMap;
			}
			int count = 0;
			Factory factory = factoryMapper.selectByPrimaryKey(ouiId);
			if (null != factory && StringUtils.isNotBlank(factory.getFactoryCode())) {
				count = gatewayInfoMapper.countSelectGatewayInfoByCode(factory.getFactoryCode());
			}
			if (count >= 1) {
				retMap.put(Constant.CODE, ErrorCodeEnum.DEL_RELATION_EXIST_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.DEL_RELATION_EXIST_ERROR.getResultMsg());
				return retMap;
			} else {
				int rec = factoryMapper.deleteByPrimaryKey(ouiId);
				if (rec > 0) {
					retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
					retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
					OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "删除OUI", JSON.toJSONString(parameter));
				} else {
					retMap.put(Constant.CODE, ErrorCodeEnum.DELETE_ERROR.getResultCode());
					retMap.put(Constant.MESSAGE, ErrorCodeEnum.DELETE_ERROR.getResultMsg());
				}
			}
		} catch (Exception e) {
			logger.error("delOuiInfo exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		return retMap;
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
	private void setPackageBasisResult(Map<String, Object> rm, Integer code, String msg, Object data) {
		rm.put(Constant.CODE, code);
		rm.put(Constant.MESSAGE, msg);
		rm.put(Constant.DATA, data);
	}

	@Override
	public Map<String, Object> addMakeInfo(Map<String, Object> parameter) {
		logger.info("Start invoke addMakeInfo:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String makeName = null != parameter.get("makeName") ? parameter.get("makeName").toString() : null;
			if (StringUtils.isBlank(makeName)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
				return retMap;
			}
			List<MakeInfo> mil = makeInfoMapper.queryMakeInfoByMakeName(makeName);
			if (null != mil && mil.size() > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.NAME_EXIST.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.NAME_EXIST.getResultMsg());
				return retMap;
			}
			int rce = makeInfoMapper.insert(new MakeInfo(UniqueUtil.uuid(), makeName));

			if (rce > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "新增制造商", JSON.toJSONString(parameter));
			} else {
				retMap.put(Constant.CODE, ErrorCodeEnum.OPERATION_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.OPERATION_ERROR.getResultMsg());
			}
		} catch (Exception e) {
			logger.error("addMakeInfo exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		logger.info("End invoke addMakeInfo:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> updateMakeInfo(Map<String, Object> parameter) {
		logger.info("Start invoke updateMakeInfo:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String makeId = null != parameter.get("makeId") ? parameter.get("makeId").toString().trim() : "";
			String makeName = null != parameter.get("makeName") ? parameter.get("makeName").toString() : null;
			if (StringUtils.isBlank(makeName) || StringUtils.isBlank(makeId)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
				return retMap;
			}
			List<MakeInfo> mil = makeInfoMapper.queryMakeInfoByMakeName(makeName);
			MakeInfo mi = makeInfoMapper.selectByPrimaryKey(makeId);
			if (null != mil && mil.size() > 0) {
				for (MakeInfo m : mil) {
					if (!m.getId().equals(mi.getId()) && m.getMakeName().equals(makeName)) {
						retMap.put(Constant.CODE, ErrorCodeEnum.NAME_EXIST.getResultCode());
						retMap.put(Constant.MESSAGE, ErrorCodeEnum.NAME_EXIST.getResultMsg());
						return retMap;
					}
				}
			}
			int rce = makeInfoMapper.updateByPrimaryKeySelective(new MakeInfo(mi.getId(), makeName));
		
			if (rce > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "更新制造商", JSON.toJSONString(parameter));
			} else {
				retMap.put(Constant.CODE, ErrorCodeEnum.OPERATION_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.OPERATION_ERROR.getResultMsg());
			}
		} catch (Exception e) {
			logger.error("updateMakeInfo exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		logger.info("End invoke updateMakeInfo:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> addManufacturerInfo(Map<String, Object> parameter) {
		logger.info("Start invoke addManufacturerInfo:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String manufacturerName = null != parameter.get("manufacturerName") ? parameter.get("manufacturerName").toString().trim() : null;
			String code = null != parameter.get("code") ? parameter.get("code").toString().trim() : null;
			String makeId = null != parameter.get("makeId") ? parameter.get("makeId").toString().trim() : null;
			if (StringUtils.isBlank(manufacturerName) || StringUtils.isBlank(code) || StringUtils.isBlank(makeId)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
				return retMap;
			}
			int rec = 0;
			List<Manufacturer> mfl = manufacturerMapper.queryManufactureByName(new Manufacturer(null, manufacturerName, code, null));
			if (null != mfl && mfl.size() > 0) {
				for (Manufacturer m : mfl) {
					if (code.equalsIgnoreCase(m.getCode())) {
						retMap.put(Constant.CODE, ErrorCodeEnum.CODE_EXIST.getResultCode());
						retMap.put(Constant.MESSAGE, ErrorCodeEnum.CODE_EXIST.getResultMsg());
						return retMap;
					}
				}
			}
			MakeInfo mi = makeInfoMapper.selectByPrimaryKey(makeId);
			if(mi != null){
				rec = manufacturerMapper.insertSelective(new Manufacturer(UniqueUtil.uuid(), manufacturerName, code, makeId));
			}
			if (rec > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "新增生产商", JSON.toJSONString(parameter));
			} else {
				retMap.put(Constant.CODE, ErrorCodeEnum.OPERATION_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.OPERATION_ERROR.getResultMsg());
			}
		} catch (Exception e) {
			logger.error("addManufacturerInfo exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		logger.info("End invoke addManufacturerInfo:{}", retMap);
		return retMap; 
	}

	@Override
	public Map<String, Object> updateManufacturerInfo(Map<String, Object> parameter) {
		logger.info("Start invoke updateManufacturerInfo:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String id = null != parameter.get("manufacturerId") ? parameter.get("manufacturerId").toString().trim() : null;
			String manufacturerName = null != parameter.get("manufacturerName") ? parameter.get("manufacturerName").toString().trim() : null;
			String code = null != parameter.get("code") ? parameter.get("code").toString().trim() : null;
			String makeId = null != parameter.get("makeId") ? parameter.get("makeId").toString().trim() : null;
			if (StringUtils.isBlank(manufacturerName) || StringUtils.isBlank(code) || StringUtils.isBlank(makeId) || StringUtils.isBlank(id)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
				return retMap;
			}
			List<Manufacturer> mfl = manufacturerMapper.queryManufactureByName(new Manufacturer(null, manufacturerName, code, null));
			Manufacturer mf = manufacturerMapper.selectByPrimaryKey(id);
			int rec = 0;
			if (null != mfl && mfl.size() > 0) {
				for (Manufacturer m : mfl) {
					if (!m.getId().equals(mf.getId())) {
						if (code.equalsIgnoreCase(m.getCode())) {
							retMap.put(Constant.CODE, ErrorCodeEnum.CODE_EXIST.getResultCode());
							retMap.put(Constant.MESSAGE, ErrorCodeEnum.CODE_EXIST.getResultMsg());
							return retMap;
						}
					}
				}
			}
			MakeInfo mi = makeInfoMapper.selectByPrimaryKey(makeId);
			if(mi != null){
				rec = manufacturerMapper.updateByPrimaryKeySelective(new Manufacturer(mf.getId(), manufacturerName, code, makeId));
			}
			if (rec > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "更新生产商", JSON.toJSONString(parameter));
			} else {
				retMap.put(Constant.CODE, ErrorCodeEnum.OPERATION_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.OPERATION_ERROR.getResultMsg());
			}
		} catch (Exception e) {
			logger.error("updateManufacturerInfo exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		logger.info("End invoke updateManufacturerInfo:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> addOuiInfo(Map<String, Object> parameter) {
		logger.info("Start invoke addOuiInfo:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String ouiName = null != parameter.get("ouiName") ? parameter.get("ouiName").toString().trim() : null;
			String ouicode = null != parameter.get("ouiCode") ? parameter.get("ouiCode").toString().trim() : null;
			String manufacturerId = null != parameter.get("manufacturerId") ? parameter.get("manufacturerId").toString().trim() : null;
			if (StringUtils.isBlank(ouicode) || StringUtils.isBlank(manufacturerId)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
				return retMap;
			}
			List<Factory> fl = factoryMapper.selectFactoryByName(new Factory(null, ouicode, null, null));
			int rec = 0;
			if (null != fl && fl.size() > 0) {
				for (Factory f : fl) {
					// 生产商相同下
					if (ouicode.equalsIgnoreCase(f.getFactoryCode())) {
						retMap.put(Constant.CODE, ErrorCodeEnum.CODE_EXIST.getResultCode());
						retMap.put(Constant.MESSAGE, ErrorCodeEnum.CODE_EXIST.getResultMsg());
						return retMap;
					}
					// 页面未传OUI名称
				}
			}
			rec = factoryMapper.insertSelective(new Factory(UniqueUtil.uuid(), ouicode, ouiName, manufacturerId));
			if (rec > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "新增OUI", JSON.toJSONString(parameter));
			} else {
				retMap.put(Constant.CODE, ErrorCodeEnum.OPERATION_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.OPERATION_ERROR.getResultMsg());
			}
		} catch (Exception e) {
			logger.error("addOuiInfo exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		logger.info("End invoke addOuiInfo:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> updateOuiInfo(Map<String, Object> parameter) {
		logger.info("Start invoke updateOuiInfo:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String id = null != parameter.get("id") ? parameter.get("id").toString().trim() : null;
			String ouiName = null != parameter.get("ouiName") ? parameter.get("ouiName").toString().trim() : null;
			String ouicode = null != parameter.get("ouiCode") ? parameter.get("ouiCode").toString().trim() : null;
			String manufacturerId = null != parameter.get("manufacturerId") ? parameter.get("manufacturerId").toString().trim() : null;
			if (StringUtils.isBlank(ouicode) || StringUtils.isBlank(manufacturerId) || StringUtils.isBlank(id)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
				return retMap;
			}
			List<Factory> fl = factoryMapper.selectFactoryByName(new Factory(null, ouicode, null, null));
			Factory factory = factoryMapper.selectByPrimaryKey(id);
			int rec = 0;
			if (null != fl && fl.size() > 0) {
				for (Factory f : fl) {
					// 相同OUI信息下生产商相同下
					// if (!f.getId().equals(factory.getId()) && manufacturerId.equals(f.getManufacturerId())) {
					if (!f.getId().equals(factory.getId()) && ouicode.equalsIgnoreCase(f.getFactoryCode())) {
						retMap.put(Constant.CODE, ErrorCodeEnum.CODE_EXIST.getResultCode());
						retMap.put(Constant.MESSAGE, ErrorCodeEnum.CODE_EXIST.getResultMsg());
						return retMap;
					}
				}
			}
			rec = factoryMapper.updateByPrimaryKeySelective(new Factory(factory.getId(), ouicode, ouiName, manufacturerId));
			if (rec > 0 && !ouicode.equals(factory.getFactoryCode())) {
				// 更新网关表里面的gateway_factory_code
				Map<String, Object> par = new HashMap<String, Object>();
				par.put("newCode", ouicode);
				par.put("oldCode", factory.getFactoryCode());
				gatewayInfoMapper.updateGatewayFactoryCode(par);
			}
			if (rec > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.FACTORYCODE_MANAGER_SERVICE, "更新OUI", JSON.toJSONString(parameter));
			} else {
				retMap.put(Constant.CODE, ErrorCodeEnum.OPERATION_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.OPERATION_ERROR.getResultMsg());
			}
		} catch (Exception e) {
			logger.error("updateOuiInfo exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.EXCEPTION_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.EXCEPTION_ERROR.getResultMsg());
		}
		logger.info("End invoke updateOuiInfo:{}", retMap);
		return retMap;
	}
}
