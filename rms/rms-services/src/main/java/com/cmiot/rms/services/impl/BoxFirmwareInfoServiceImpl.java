package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.StringLocalUtils;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BoxDeviceInfoMapper;
import com.cmiot.rms.dao.mapper.BoxFirmwareInfoMapper;
import com.cmiot.rms.dao.mapper.BoxFirmwarePreparedMapper;
import com.cmiot.rms.dao.model.*;
import com.cmiot.rms.services.BoxCommonSearchService;
import com.cmiot.rms.services.BoxFirmwareInfoService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机顶盒固件管理服务实现类
 * Created by panmingguo on 2016/6/13.
 */
public class BoxFirmwareInfoServiceImpl implements BoxFirmwareInfoService {

    private final Logger logger = LoggerFactory.getLogger(BoxFirmwareInfoServiceImpl.class);

	@Autowired
	BoxFirmwareInfoMapper boxFirmwareInfoMapper;

	@Autowired
	BoxFirmwarePreparedMapper boxFirmwarePreparedMapper;

	@Autowired
	BoxCommonSearchService boxCommonSearchService;

	@Autowired
	BoxDeviceInfoMapper boxDeviceInfoMapper;

	/**
	 * 根据生产商、设备型号、固件版本查询机顶盒固件信息
	 *
	 * @param parameter
	 * @return
	 */
	@Override
	public Map<String, Object> searchBoxFirmwareInfo(Map<String, Object> parameter) {
		logger.info("Start invoke searchBoxFirmwareInfo:{}", parameter);
		//String factoryCode = StringLocalUtils.ObjectToNull(parameter.get("factoryCode"));
		//String boxModel = StringLocalUtils.ObjectToNull(parameter.get("boxModel"));
		//String firmwareVersion = StringLocalUtils.ObjectToNull(parameter.get("firmwareVersion"));
		String firmwareId = StringLocalUtils.ObjectToNull(parameter.get("firmwareId"));

		int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
		int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;

		Map<String, Object> retMap = new HashMap<>();
		try {
			BoxFirmwareInfo searchInfo = new BoxFirmwareInfo();
			//searchInfo.setFactoryCode(factoryCode);
			//searchInfo.setBoxModel(boxModel);
			//searchInfo.setFirmwareVersion(firmwareVersion);
			searchInfo.setId(firmwareId);

			PageHelper.startPage(page, pageSize);
			List<BoxFirmwareInfo> infoList = boxFirmwareInfoMapper.queryFirmwareInfo(searchInfo);
			List<Map<String, Object>> preList;
			for (BoxFirmwareInfo info : infoList) {
				preList = boxFirmwarePreparedMapper.queryPreparedByFirmwareId(info.getId());
				info.setPrepared(preList);
			}

			retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
			retMap.put(Constant.DATA, JSON.toJSON(infoList));
			retMap.put(Constant.TOTAL, ((Page) infoList).getTotal());
			retMap.put(Constant.PAGE, page);
			retMap.put(Constant.PAGESIZE, pageSize);
		} catch (Exception e) {
			logger.error("searchBoxFirmwareInfo exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
		}
		logger.info("End invoke searchBoxFirmwareInfo:{}", retMap);
		return retMap;
	}

	/**
	 * 根据设备型号查询机顶盒固件版本号
	 *
	 * @param parameter
	 * @return
	 */
	@Override
	public Map<String, Object> searchBoxFirmwareVersion(Map<String, Object> parameter) {
		logger.info("Start invoke searchBoxFirmwareVersion:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		String boxDeviceId = StringLocalUtils.ObjectToNull(parameter.get("boxModel"));// boxModel 实际传的是t_box_device 表的id 表被从新设计过后的修改
		if (null == boxDeviceId) {
			retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
		} else {
			try {
				BoxFirmwareInfo searchInfo = new BoxFirmwareInfo();
				searchInfo.setDeviceId(boxDeviceId);
				List<BoxFirmwareInfo> infoList = boxFirmwareInfoMapper.queryFirmware(searchInfo);
				Map<String, String> versionMap = new HashMap<>();
				String currentVersion = null != parameter.get("currentVersion") ? parameter.get("currentVersion").toString() : "";
				for (BoxFirmwareInfo info : infoList) {
					if (!currentVersion.equals(info.getFirmwareVersion())) {
						versionMap.put(info.getId(), info.getFirmwareVersion());
					}
				}
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
				retMap.put(Constant.DATA, versionMap);
			} catch (Exception e) {
				logger.error("searchBoxFirmwareVersion exception:{}", e);
				retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
			}
		}

		logger.info("End invoke searchBoxFirmwareVersion:{}", retMap);
		return retMap;
	}

	/**
	 * 根据生产商、设备型号、固件版本号查询机顶盒固件ID
	 *
	 * @param parameter
	 * @return
	 */
	@Override
	public Map<String, Object> searchBoxFirmwareId(Map<String, Object> parameter) {
		logger.info("Start invoke searchBoxFirmwareId:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		String factoryCode = StringLocalUtils.ObjectToNull(parameter.get("factoryCode"));
		 String boxModel = StringLocalUtils.ObjectToNull(parameter.get("boxModel"));
		String firmwareVersion = StringLocalUtils.ObjectToNull(parameter.get("firmwareVersion"));
		if (null == factoryCode || null == firmwareVersion) {
			retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
		} else {
			try {
				BoxFirmwareInfo searchInfo = new BoxFirmwareInfo();
				searchInfo.setFactoryCode(factoryCode);
				searchInfo.setFirmwareVersion(firmwareVersion);
				searchInfo.setBoxModel(boxModel);
				List<BoxFirmwareInfo> infoList = boxFirmwareInfoMapper.queryFirmwareInfo(searchInfo);
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
				retMap.put(Constant.DATA, infoList);
			} catch (Exception e) {
				logger.error("searchBoxFirmwareId exception:{}", e);
				retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
			}
		}
		logger.info("End invoke searchBoxFirmwareId:{}", retMap);
		return retMap;
	}

	/**
	 * 添加机顶盒固件
	 *
	 * @param parameter
	 * @return
	 */
	@Override
	public Map<String, Object> addBoxFirmwareInfo(Map<String, Object> parameter) {
		logger.info("Start invoke addBoxFirmwareInfo:{}", parameter);
		BoxFirmwareInfo info = new BoxFirmwareInfo();
		Map<String, Object> retMap = new HashMap<>();
		try {
			org.apache.commons.beanutils.BeanUtils.populate(info, parameter);
		} catch (Exception e) {
			logger.error("addBoxFirmwareInfo covert map to bean failed!");
			retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_ERROR.getResultMsg());
			return retMap;
		}

		if (!isUnique(info.getFirmwareVersion(), null, null, null, info.getDeviceId())) {
			retMap.put(Constant.CODE, ErrorCodeEnum.FIRMWARE_EXIST.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.FIRMWARE_EXIST.getResultMsg());
			return retMap;
		}

		info.setFirmwareCreateTime(DateTools.getCurrentSecondTime());
		info.setId(UniqueUtil.uuid());
		try {
			boxFirmwareInfoMapper.insert(info);
		} catch (Exception e) {
			logger.error("addBoxFirmwareInfo Exception {}", e.getMessage(), e);
			retMap.put(Constant.CODE, ErrorCodeEnum.INSERT_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, "保存固件失败");
			return retMap;
		}
		// 保存待升级版本
		String firmwarePreviousId = null != parameter.get("firmwarePreviousId") ? parameter.get("firmwarePreviousId").toString() : "";
		addFirmwarePreviousVersion(firmwarePreviousId, info.getId());

		retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
		retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
		return retMap;
	}

	/**
	 * 修改机顶盒固件
	 *
	 * @param parameter
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> editBoxFirmwareInfo(Map<String, Object> parameter) {
		String logname = "editBoxFirmwareInfo";// 日志名称
		Map<String, Object> rm = new HashMap<>();// 返回JSON对象
		parameter.put("loguuid", UniqueUtil.uuid());// 日志ID
		try {
			logger.info("LogId:{} " + logname + "接口请求参数:{}", parameter.get("loguuid"), parameter);
			if (checkParm(parameter, rm, Arrays.asList(new String[] { "firmwareId" }))) {
				String firmwareId = null != parameter.get("firmwareId") ? parameter.get("firmwareId").toString() : "";
				BoxFirmwareInfo info = boxFirmwareInfoMapper.selectByPrimaryKey(firmwareId);
				if (null != info) {
					logger.info("LogId:{} " + logname + "根据DeviceID:{}查询设备信息", parameter.get("loguuid"), info.getDeviceId());
					BoxDeviceInfo bdi = boxDeviceInfoMapper.selectByParimaryKeyToMode(info.getDeviceId());
					if (null != bdi) {
						rm.put("firmwarePreparedList", buildPreviousVersionforEdit(info.getDeviceId(), info.getId(), info.getFirmwareVersion()));// 此设备选中的待升级版本和所有版本的对应关系
						Map<String, Object> factoryMap = boxCommonSearchService.queryFactoryInfo(new HashMap<>());// 查询生产商信息
						rm.put("factoryList", JSON.toJSON(null != factoryMap.get(Constant.DATA) ? ((List<BoxFactoryInfo>) factoryMap.get(Constant.DATA)) : null));
						BoxDeviceInfo bdisql = new BoxDeviceInfo();
						bdisql.setFactoryCode(bdi.getFactoryCode());
						rm.put("deviceList", JSON.toJSON(boxDeviceInfoMapper.selectByParimaryMode(bdisql)));// 设备信息
						rm.put("choosedFactoryCode", bdi.getFactoryCode());// 当前固件的生产商
						rm.put("choosedDeviceID", bdi.getId());// 当前固件的设备型号
						rm.put("firmwareInfo", JSON.toJSON(info));// 返回固件信息
						packageBackInfo(rm, RespCodeEnum.RC_0.code(), RespCodeEnum.RC_0.description(), "");
					} else {
						packageBackInfo(rm, RespCodeEnum.RC_1.code(), "查询设备信息失败", "");
					}
				} else {
					packageBackInfo(rm, RespCodeEnum.RC_1.code(), "查询固件信息失败", "");
				}
			}
		} catch (Exception e) {
			rm.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
			rm.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
		}
		logger.info("LogId:{} " + logname + "业务返回的结果集:{}", parameter.get("loguuid"), rm);
		return rm;
		// 旧方法
		// logger.info("Start invoke editBoxFirmwareInfo:{}", parameter);
		// String firmwareId = null != parameter.get("firmwareId") ? parameter.get("firmwareId").toString() : "";
		// Map<String, Object> retMap = new HashMap<>();
		// if (StringUtils.isEmpty(firmwareId)) {
		// retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
		// retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
		// return retMap;
		// }
		// try {
		// BoxFirmwareInfo info = boxFirmwareInfoMapper.selectByPrimaryKey(firmwareId);
		// retMap.put("firmwareInfo", JSON.toJSON(info));
		//
		// // 查询生产商信息
		// Map<String, Object> factoryMap = boxCommonSearchService.queryFactoryInfo(new HashMap<>());
		// List<BoxFactoryInfo> factoryList = null != factoryMap.get(Constant.DATA) ? ((List<BoxFactoryInfo>) factoryMap.get(Constant.DATA)) : null;
		// retMap.put("factoryList", JSON.toJSON(factoryList));
		//
		// // 查询设备型号
		// Map<String, Object> paraModel = new HashMap<>();
		// paraModel.put("deviceId", info.getDeviceId());
		// Map<String, Object> modelMap = boxCommonSearchService.queryBoxModel(paraModel);
		// Map<String, String> deviceModel = null != modelMap.get(Constant.DATA) ? ((Map<String, String>) modelMap.get(Constant.DATA)) : null;
		// retMap.put("boxModel", deviceModel);
		//
		// // 当前固件的生产商和设备型号
		// retMap.put("choosedFactoryCode", info.getFactoryCode());
		// retMap.put("choosedBoxModel", info.getBoxModel());
		//
		// // 此设备选中的待升级版本和所有版本的对应关系
		// List<Map<String, Object>> versionList = buildPreviousVersionforEdit(info.getDeviceId(), info.getId(), info.getFirmwareVersion());
		// retMap.put("firmwarePreparedList", versionList);
		// retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
		// retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
		// } catch (Exception e) {
		// logger.error("editBoxFirmwareInfo exception:{}", e);
		// retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
		// retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
		// }
		//
		// logger.info("End invoke editBoxFirmwareInfo:{}", retMap);
		// return retMap;
	}

	/**
	 * 更新机顶盒固件
	 *
	 * @param parameter
	 * @return
	 */
	@Override
	public Map<String, Object> updateBoxFirmwareInfo(Map<String, Object> parameter) {
		logger.info("Start invoke updateBoxFirmwareInfo:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		BoxFirmwareInfo newFirmwareInfo = new BoxFirmwareInfo();
		try {
			org.apache.commons.beanutils.BeanUtils.populate(newFirmwareInfo, parameter);
		} catch (Exception e) {
			logger.error("updateBoxFirmwareInfo covert failed!");
			retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_ERROR.getResultMsg());
			return retMap;
		}

		if (!isUnique(newFirmwareInfo.getFirmwareVersion(), null, null, newFirmwareInfo.getId(), newFirmwareInfo.getDeviceId())) {
			retMap.put(Constant.CODE, ErrorCodeEnum.FIRMWARE_EXIST.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.FIRMWARE_EXIST.getResultMsg());
			return retMap;
		}

		boxFirmwareInfoMapper.updateByPrimaryKeySelective(newFirmwareInfo);
		deleteFirmwarePreviousVersion(newFirmwareInfo.getId());

		// 保存新的待升级版本
		String firmwarePreviousId = null != parameter.get("firmwarePreviousId") ? parameter.get("firmwarePreviousId").toString() : "";
		addFirmwarePreviousVersion(firmwarePreviousId, newFirmwareInfo.getId());

		retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
		retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());

		logger.info("End invoke updateBoxFirmwareInfo:{}", retMap);
		return retMap;
	}

	/**
	 * 删除机顶盒固件
	 *
	 * @param parameter
	 * @return
	 */
	@Override
	public Map<String, Object> deleteBoxFirmwareInfo(Map<String, Object> parameter) {
		logger.info("Start invoke deleteBoxFirmwareInfo:{}", parameter);
		String firmwareInfoId = null != parameter.get("firmwareId") ? parameter.get("firmwareId").toString() : "";
		Map<String, Object> retMap = new HashMap<>();
		if (StringUtils.isNotEmpty(firmwareInfoId)) {
			// 先判断升级任务是否使用该固件
			int count = 0;
			if (count > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.FIRMWARE_IS_USING.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.FIRMWARE_IS_USING.getResultMsg());
			} else {

				boxFirmwareInfoMapper.deleteByPrimaryKey(firmwareInfoId);
				boxFirmwarePreparedMapper.deleteByFirmwareId(firmwareInfoId);

				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
			}

		} else {
			retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
			retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
		}
		logger.info("End invoke deleteBoxFirmwareInfo:{}", retMap);
		return retMap;
	}

	/**
	 * 判断是否存在相同的固件版本（根据版本，生产商、设备型号判断是否唯一）
	 * 
	 * @param firmwareVersion
	 * @param factoryCode
	 * @param boxModel
	 * @param id
	 * @return
	 */
	private Boolean isUnique(String firmwareVersion, String factoryCode, String boxModel, String id, String deviceId) {
		Map<String, Object> para = new HashMap<>();
		para.put("firmwareVersion", firmwareVersion);
		para.put("factoryCode", factoryCode);
		para.put("boxModel", boxModel);
		para.put("id", id);
		para.put("deviceId", deviceId);
		int count = boxFirmwareInfoMapper.selectCount(para);
		return count < 1 ? true : false;
	}

	/**
	 * 保存待升级版本
	 *
	 * @param firmwarePreviousId
	 * @param firmwareInfoId
	 */
	private void addFirmwarePreviousVersion(String firmwarePreviousId, String firmwareInfoId) {
		if (!StringUtils.isEmpty(firmwarePreviousId)) {
			String[] firmwarePreviousIds = firmwarePreviousId.split(";");
			BoxFirmwarePrepared firmwarePrepared;
			for (int i = 0; i < firmwarePreviousIds.length; i++) {
				String[] idAndForces = firmwarePreviousIds[i].split(",");
				firmwarePrepared = new BoxFirmwarePrepared();
				firmwarePrepared.setFirmwareId(firmwareInfoId);
				firmwarePrepared.setFirmwarePreviousId(idAndForces[0]);
				if (idAndForces[1].equals("1")) {
					firmwarePrepared.setNeedForceUpgrade(true);
				} else {
					firmwarePrepared.setNeedForceUpgrade(false);
				}
				boxFirmwarePreparedMapper.insert(firmwarePrepared);
			}
		}
	}

	/**
	 * 拼接修改固件页面待升级版本信息
	 *
	 * @return
	 */
	private List<Map<String, Object>> buildPreviousVersionforEdit(String boxModel, String firmwareId, String firmwareVersion) {
		List<Map<String, Object>> retList = new ArrayList<>();

		// 获取选择的待升级版本
		BoxFirmwarePrepared firmwarePrepared = new BoxFirmwarePrepared();
		firmwarePrepared.setFirmwareId(firmwareId);
		List<BoxFirmwarePrepared> preparedList = boxFirmwarePreparedMapper.queryList(firmwarePrepared);

		// 获取所有的待升级版本
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("boxModel", boxModel);
		Map<String, Object> versionMap = searchBoxFirmwareVersion(parameter);
		Map<String, String> data = (Map<String, String>) versionMap.get(Constant.DATA);

		Map<String, Object> retMap;
		for (Map.Entry<String, String> entry : data.entrySet()) {
			// 去除本身版本
			if (entry.getValue().equals(firmwareVersion)) {
				continue;
			}
			retMap = new HashMap<>();
			retMap.put("firmwareId", entry.getKey());
			retMap.put("firmwareVersion", entry.getValue());
			retMap.put("isChoosed", "0");
			for (BoxFirmwarePrepared prepared : preparedList) {
				if (prepared.getFirmwarePreviousId().equals(entry.getKey())) {
					retMap.put("isChoosed", "1");
					retMap.put("needForceUpgrade", prepared.getNeedForceUpgrade());
					break;
				}
			}
			retList.add(retMap);
		}
		return retList;
	}

	/**
	 * 删除待升级版本
	 *
	 * @param firmwareInfoId
	 */
	private void deleteFirmwarePreviousVersion(String firmwareInfoId) {
		if (StringUtils.isNotEmpty(firmwareInfoId)) {
			boxFirmwarePreparedMapper.deleteByFirmwareId(firmwareInfoId);
		}
	}
    /**
     * 根据机顶盒的当前版本查询待升级版本
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryBoxPreparedVersion(Map<String, Object> parameter) {
        logger.info("Start invoke queryBoxPreparedVersion:{}", parameter);
        String firmwareId = null != parameter.get("firmwareId") ? parameter.get("firmwareId").toString() : "";
        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isEmpty(firmwareId)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            return retMap;
        }
        try {

            List<Map<String, Object>> preList = boxFirmwarePreparedMapper.queryPreparedByFirmwareId(firmwareId);
            Map<String, String> map = new HashMap<>();
            for(Map<String, Object> pre : preList)
            {
                map.put(pre.get("id").toString(), pre.get("firmwareVersion").toString());
            }
            retMap.put(Constant.DATA, map);
            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        }
        catch (Exception e)
        {
            logger.error("queryBoxPreparedVersion exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }

        logger.info("End invoke queryBoxPreparedVersion:{}", retMap);
        return retMap;
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
	private void packageBackInfo(Map<String, Object> rm, String code, String msg, Object data) {
		rm.put(Constant.DATA, data);
		rm.put(Constant.CODE, code);
		rm.put(Constant.MESSAGE, msg);
	}
}
