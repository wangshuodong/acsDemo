package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.BaseDao;
import com.cmiot.rms.dao.mapper.FirmwareInfoMapper;
import com.cmiot.rms.dao.mapper.FirmwarePreparedMapper;
import com.cmiot.rms.dao.mapper.MakeInfoMapper;
import com.cmiot.rms.dao.model.*;
import com.cmiot.rms.services.CommonSearchService;
import com.cmiot.rms.services.DeviceInfoService;
import com.cmiot.rms.services.FirmwareInfoService;
import com.cmiot.rms.services.FirmwareUpgradeTaskDetailService;
import com.cmiot.rms.services.util.OperationLogUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by panmingguo on 2016/4/12.
 */
public class FirmwareInfoServiceImpl implements FirmwareInfoService {

    public final Logger logger = LoggerFactory.getLogger(FirmwareInfoServiceImpl.class);

    @Autowired
    BaseDao baseDao;

    @Autowired
    FirmwareInfoMapper firmwareInfoMapper;

    @Autowired
    FirmwarePreparedMapper firmwarePreparedMapper;

    @Autowired
    CommonSearchService commonSearchService;

    @Autowired
    DeviceInfoService deviceInfoService;

    @Autowired
    FirmwareUpgradeTaskDetailService firmwareUpgradeTaskDetailService;

    @Autowired
    MakeInfoMapper makeInfoMapper;

    @Autowired
    AreaService amsAreaService;

    /**
     * 根据生产商、设备型号、固件版本查询固件信息
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> searchFirmwareInfo(Map<String, Object> parameter) {
        logger.info("Start invoke searchFirmwareInfo:{}", parameter);
        List<Object> argsList = new ArrayList<>();
        Map<String, Object> retMap = new HashMap<>();

        try {
            //构造固件查询公共语句
            String commonSql = buildCommonSearchSql(argsList, parameter);

            //查询固件列表总数
            searchTotal(commonSql, argsList, retMap);

            //查询固件列表
            searchListSql(commonSql, argsList, retMap, parameter);

            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        } catch (Exception e) {
            logger.error("searchFirmwareInfo exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }

        logger.info("End invoke searchFirmwareInfo:{}", retMap);

        return retMap;
    }

    /**
     * 根据设备型号查询固件版本号
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> searchFirmwareVersion(Map<String, Object> parameter) {
        logger.info("Start invoke searchFirmwareVersion:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        if (null == parameter.get("deviceId")) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
        } else {
            try {
                FirmwareInfo firmwareInfo = new FirmwareInfo();
                firmwareInfo.setDeviceId(parameter.get("deviceId").toString());
                String isCheckStatus = null != parameter.get("isCheckStatus") ? parameter.get("isCheckStatus").toString() : "";
                String version = null != parameter.get("version") ? parameter.get("version").toString() : "";
                if(StringUtils.isNotBlank(version))
                {
                    firmwareInfo.setFirmwareVersion(version);
                }

                //固件管理页面不对审批状态进行过滤
                if(!"0".equals(isCheckStatus))
                {
                    firmwareInfo.setCheckStatus(2);//审批通过的数据
                }

                String currentVersion = null != parameter.get("currentVersion") ? parameter.get("currentVersion").toString() : "";
                List<FirmwareInfo> firmwareInfoList = firmwareInfoMapper.queryList(firmwareInfo);

                Map<String, String> versionMap = new HashMap<>();

                for (FirmwareInfo firmwareInfoItem : firmwareInfoList) {
                    if(!currentVersion.equals(firmwareInfoItem.getFirmwareVersion()))
                    {
                        versionMap.put(firmwareInfoItem.getId(), firmwareInfoItem.getFirmwareVersion());
                    }
                }

                retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
                retMap.put(Constant.DATA, versionMap);
            } catch (Exception e) {
                logger.error("searchFirmwareVersion exception:{}", e);
                retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
            }

        }

        logger.info("End invoke searchFirmwareVersion:{}", retMap);
        return retMap;
    }

    /**
     * 根据生产商、设备型号、固件版本号查询固件ID
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> searchFirmwareId(Map<String, Object> parameter) {
        logger.info("Start invoke searchFirmwareId:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        if (null == parameter.get("deviceFactory") ||  null == parameter.get("firmwareVersion") || null == parameter.get("deviceModel")) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
        } else {
            try {
                Map<String, Object>  firmwareInfoList = firmwareInfoMapper.queryFirmwareIdList(parameter);
                retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
                retMap.put(Constant.DATA, firmwareInfoList);
            } catch (Exception e) {
                logger.error("searchFirmwareId exception:{}", e);
                retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
            }
        }

        logger.info("End invoke searchFirmwareId:{}", retMap);
        return retMap;
    }

    /**
     * 添加固件
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> addFirmwareInfo(Map<String, Object> parameter) {
        logger.info("Start invoke addFirmwareInfo:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        FirmwareInfo firmwareInfo = new FirmwareInfo();
        try {
            org.apache.commons.beanutils.BeanUtils.populate(firmwareInfo, parameter);
        } catch (Exception e) {
            logger.error("covert map to bean failed!");
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_ERROR.getResultMsg());
            return retMap;
        }

        //判断是否存在相同的固件
        if(!isUnique(firmwareInfo.getFirmwareVersion(), firmwareInfo.getDeviceId(), null))
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.FIRMWARE_EXIST.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.FIRMWARE_EXIST.getResultMsg());
            return retMap;
        }

        //设置创建时间
        long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        firmwareInfo.setFirmwareCreateTime((int) timeSeconds);

        try {
            firmwareInfo.setId(UniqueUtil.uuid());
            firmwareInfoMapper.insertSelective(firmwareInfo);

            //保存待升级版本
            String firmwarePreviousId = null != parameter.get("firmwarePreviousId") ? parameter.get("firmwarePreviousId").toString() : "";
            addFirmwarePreviousVersion(firmwarePreviousId, firmwareInfo.getId());

            //记录操作日志
            recordOperationLog(parameter, "新增固件");

            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        } catch (Exception e) {
            logger.error("addFirmwareInfo exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.INSERT_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.INSERT_ERROR.getResultMsg());
        }

        logger.info("End invoke addFirmwareInfo:{}", retMap);
        return retMap;
    }

    /**
     * 修改固件，提供进入修改页面数据
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> editFirmwareInfo(Map<String, Object> parameter) {
        logger.info("Start invoke editFirmwareInfo:{}", parameter);
        String firmwareId = null != parameter.get("firmwareId") ? parameter.get("firmwareId").toString() : "";
        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isEmpty(firmwareId)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            return retMap;
        }

        try {
            //根据ID查询固件信息
            FirmwareInfo firmwareInfo = firmwareInfoMapper.selectByPrimaryKey(firmwareId);
            retMap.put("firmwareInfo", JSON.toJSON(firmwareInfo));


            //查询设备信息和制造商信息
            DeviceInfo deviceInfo = deviceInfoService.selectByUuid(firmwareInfo.getDeviceId());
            MakeInfo makeInfo = makeInfoMapper.selectByFactoryCode(deviceInfo.getDeviceFactory());
            if(null == deviceInfo || null == makeInfo)
            {
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_ERROR.getResultMsg());
                return retMap;
            }

            //查询制造商信息
            Map<String, Object> manufacturerMap = commonSearchService.queryAllManufacturer();
            List<Factory> manufacturerList = null != manufacturerMap.get(Constant.DATA) ? ((List<Factory>) manufacturerMap.get(Constant.DATA)) : null;
            retMap.put("manufacturerList", JSON.toJSON(manufacturerList));

            //查询生产商信息
            Map<String, Object> paraFactory = new HashMap<>();
            paraFactory.put("manufacturerId", makeInfo.getId());
            Map<String, Object> factoryMap = commonSearchService.queryForManufacturerId(paraFactory);
            List<Factory> factoryList = null != factoryMap.get(Constant.DATA) ? ((List<Factory>) factoryMap.get(Constant.DATA)) : null;
            retMap.put("factoryList", JSON.toJSON(factoryList));

            //查询设备型号
            Map<String, Object> paraModel = new HashMap<>();
            paraModel.put("factoryCode", deviceInfo.getDeviceFactory());
            Map<String, Object> modelMap = commonSearchService.queryDeviceModel(paraModel);
            Map<String, String> deviceModel = null != modelMap.get(Constant.DATA) ? ((Map<String, String>) modelMap.get(Constant.DATA)) : null;
            retMap.put("deviceModel", deviceModel);


            //当前固件的生产商和设备型号
            retMap.put("choosedFactoryCode", deviceInfo.getDeviceFactory());
            retMap.put("choosedDeviceModel", deviceInfo.getDeviceModel());
            retMap.put("choosedManufacturer", makeInfo.getId());

            //此设备选中的待升级版本和所有版本的对应关系
            List<Map<String, Object>> versionList = buildPreviousVersionforEdit(firmwareInfo.getDeviceId(), firmwareInfo.getId(), firmwareInfo.getFirmwareVersion());
            retMap.put("firmwarePreparedList", versionList);

            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        }catch (Exception e)
        {
            logger.error("editFirmwareInfo exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }

        logger.info("End invoke editFirmwareInfo:{}", retMap);
        return retMap;
    }

    /**
     * 更新固件，在修改页面点击提交按钮
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> updateFirmwareInfo(Map<String, Object> parameter) {
        logger.info("Start invoke updateFirmwareInfo:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        FirmwareInfo newFirmwareInfo = new FirmwareInfo();
        try {
            org.apache.commons.beanutils.BeanUtils.populate(newFirmwareInfo, parameter);
        } catch (Exception e) {
            logger.error("covert failed!");
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_ERROR.getResultMsg());
            return retMap;
        }

        //判断是否存在相同的固件
        if(!isUnique(newFirmwareInfo.getFirmwareVersion(), newFirmwareInfo.getDeviceId(), newFirmwareInfo.getId()))
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.FIRMWARE_EXIST.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.FIRMWARE_EXIST.getResultMsg());
            return retMap;
        }

        FirmwareInfo originalFirmwareInfo = firmwareInfoMapper.selectByPrimaryKey(newFirmwareInfo.getId());

        //路径为空，说明没有上传新的固件文件
        if(StringUtils.isEmpty(newFirmwareInfo.getFirmwarePath()) || 0 == newFirmwareInfo.getFirmwareSize())
        {
            newFirmwareInfo.setFirmwarePath(originalFirmwareInfo.getFirmwarePath());
            newFirmwareInfo.setFirmwareSize(originalFirmwareInfo.getFirmwareSize());
            newFirmwareInfo.setFirmwareCreateTime(originalFirmwareInfo.getFirmwareCreateTime());
            newFirmwareInfo.setUploadMd5(originalFirmwareInfo.getUploadMd5());
            newFirmwareInfo.setCheckStatus(originalFirmwareInfo.getCheckStatus());
        }
        //更新不应该修改创建时间
		// else
		// {
		// //设置创建时间
		// long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		// newFirmwareInfo.setFirmwareCreateTime((int) timeSeconds);
		// }

        try
        {
            firmwareInfoMapper.updateByPrimaryKeySelective(newFirmwareInfo);

            //删除旧的待升级版本
            deleteFirmwarePreviousVersion(newFirmwareInfo.getId());

            //保存新的待升级版本
            String firmwarePreviousId = null != parameter.get("firmwarePreviousId") ? parameter.get("firmwarePreviousId").toString() : "";
            addFirmwarePreviousVersion(firmwarePreviousId, newFirmwareInfo.getId());

            //记录操作日志
            recordOperationLog(parameter, "修改固件");

            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        }
        catch (Exception e)
        {
            logger.error("updateFirmwareInfo exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.UPDATE_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.UPDATE_ERROR.getResultMsg());
        }

        logger.info("End invoke updateFirmwareInfo:{}", retMap);
        return retMap;
    }

    /**
     * 删除固件
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> deleteFirmwareInfo(Map<String, Object> parameter) {
        logger.info("Start invoke deleteFirmwareInfo:{}", parameter);
        String firmwareInfoId = null != parameter.get("firmwareId") ? parameter.get("firmwareId").toString() : "";
        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isNotEmpty(firmwareInfoId)) {
            //先判断升级任务是否使用该固件
            int count = firmwareUpgradeTaskDetailService.searchNoSuccessCount(firmwareInfoId);
            int gatewaycCount = firmwareInfoMapper.searchCountByFirmwareId(firmwareInfoId);
            if (count > 0 || gatewaycCount > 0) {
                retMap.put(Constant.CODE, ErrorCodeEnum.FIRMWARE_IS_USING.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.FIRMWARE_IS_USING.getResultMsg());
            } else {
                try
                {
                    firmwareInfoMapper.deleteByPrimaryKey(firmwareInfoId);
                    firmwarePreparedMapper.deleteByFirmwareId(firmwareInfoId);
                    firmwarePreparedMapper.deleteByFirmwarePreId(firmwareInfoId);

                    //记录操作日志
                    recordOperationLog(parameter, "删除固件");

                    retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
                    retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
                }
                catch (Exception e)
                {
                    retMap.put(Constant.CODE, ErrorCodeEnum.DELETE_ERROR.getResultCode());
                    retMap.put(Constant.MESSAGE, ErrorCodeEnum.DELETE_ERROR.getResultMsg());
                }
            }

        } else {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
        }
        logger.info("End invoke deleteFirmwareInfo:{}", retMap);
        return retMap;
    }
    
	@Override
	public Map<String, Object> updateFirmwareCheckStatus(Map<String, Object> parameter) {
		Map<String, Object> rb = new HashMap<>();
		try {
			if (null != parameter && null != parameter.get("firmwareId") && StringUtils.isNotBlank(parameter.get("firmwareId") + "")) {
				FirmwareInfo fi = firmwareInfoMapper.selectByPrimaryKey(parameter.get("firmwareId") + "");
				if (null != fi) {
					if(StringUtils.isNotBlank(fi.getUploadMd5())){
						if (StringUtils.isNotBlank(fi.getInputMd5()) && fi.getUploadMd5().equalsIgnoreCase(fi.getInputMd5())) {
							int st = 1;
							// 如果固件版本的审核状态为1(未审核)执行Update更新固件审核状态
							if (1 == fi.getCheckStatus().intValue()) st = firmwareInfoMapper.updateFirmwareCheckStatus(parameter.get("firmwareId") + "");
							String message = st > 0 ? ErrorCodeEnum.SUCCESS.getResultMsg() : "审批更新状态失败";
							int code = st > 0 ? ErrorCodeEnum.SUCCESS.getResultCode() : ErrorCodeEnum.UPDATE_ERROR.getResultCode();
							packageResultMap(rb, code, message);
						} else {
							packageResultMap(rb, ErrorCodeEnum.UPDATE_ERROR.getResultCode(), "固件文件MD5审批失败,请检查固件文件MD5输入是否正确");
						}
					}else{
						packageResultMap(rb, ErrorCodeEnum.UPDATE_ERROR.getResultCode(), "获取固件上传的MD5失败,请重新上传");
					}
				} else {
					packageResultMap(rb, ErrorCodeEnum.FIRMWARE_NOT_EXIST.getResultCode(), ErrorCodeEnum.FIRMWARE_NOT_EXIST.getResultMsg());
				}
			} else {
				packageResultMap(rb, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode(), ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
			}
		} catch (Exception e) {
			packageResultMap(rb, ErrorCodeEnum.UPDATE_ERROR.getResultCode(), ErrorCodeEnum.UPDATE_ERROR.getResultMsg());
			logger.error("Update Firmware Check Status Exception " + e.getMessage(), e);
		}
		return rb;
	}
	
	/**
	 * 封装返回对象
	 * 
	 * @param rb
	 *            返回对象
	 * @param code
	 *            状态码
	 * @param message
	 *            状态描述
	 */
	private void packageResultMap(Map<String, Object> rb, int code, String message) {
		rb.put(Constant.CODE, code);
		rb.put(Constant.MESSAGE, message);
	}

    /**
     * 构造查询固件列表总数语句
     *
     * @param commonSql
     * @return
     */
    private void searchTotal(String commonSql, List<Object> argsList, Map<String, Object> retMap) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT count(*) total ");
        stringBuilder.append(commonSql);
        Long total = baseDao.queryTotal(stringBuilder.toString(), argsList.toArray());
        retMap.put("total", total);

    }

    /**
     * 构造查询固件列表详情语句
     *
     * @param commonSql
     * @return
     */
    private void searchListSql(String commonSql, List<Object> argsList, Map<String, Object> retMap, Map<String, Object> parameter) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT t.* FROM(");
        stringBuilder.append("SELECT ");
        stringBuilder.append("firmware.id id , manufacturer.manufacturer_name factoryName, device.device_model deviceModel, firmware.firmware_version firmwareVersion, firmware.check_status checkStatus, firmware.area_id areaId, firmware.firmware_path firmwarePath  ");
        stringBuilder.append(commonSql);
        stringBuilder.append("ORDER BY firmware.firmware_create_time DESC) t LIMIT ?,?");

        int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
        int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;

        retMap.put("page", page);
        retMap.put("pageSize", pageSize);

        int firstIndex = (page - 1) * pageSize;
        argsList.add(firstIndex);
        argsList.add(pageSize);

        List<Map<String, Object>> retList = baseDao.queryForMap(stringBuilder.toString(), argsList.toArray());

        //获取所查询固件的待升级版本
        getFirmwarePreviousVersion(retList);

        retMap.put(Constant.DATA, retList);
    }


    /**
     * 构造固件查询公共语句
     *
     * @param argsList
     * @param parameter
     */
    private String buildCommonSearchSql(List<Object> argsList, Map<String, Object> parameter) {

        StringBuilder stringBuilder = new StringBuilder();
        String factoryCode = parameter.get("factoryCode") != null ? parameter.get("factoryCode").toString() : "";
        String deviceModel = parameter.get("deviceModel") != null ? parameter.get("deviceModel").toString() : "";
        String firmwareVersion = parameter.get("firmwareVersion") != null ? parameter.get("firmwareVersion").toString() : "";
        String manufacturerId = parameter.get("ManufacturerId") != null ? parameter.get("ManufacturerId").toString() : ""; //制造商

        stringBuilder.append("FROM ");
        stringBuilder.append("t_firmware_info firmware, t_device_info device, t_manufacturer_info manufacturer ");
        stringBuilder.append("WHERE ");
        stringBuilder.append("device.id = firmware.device_id AND device.device_factory = manufacturer.id ");

        if (StringUtils.isNotBlank(factoryCode)) {
            stringBuilder.append("AND manufacturer.id = ? ");
            argsList.add(factoryCode.toString());
        }
        if (StringUtils.isNotBlank(deviceModel)) {
            stringBuilder.append("AND device.device_model = ? ");
            argsList.add(deviceModel.toString());
        }
        if (StringUtils.isNotBlank(firmwareVersion)) {
            stringBuilder.append("AND firmware.firmware_version = ? ");
            argsList.add(firmwareVersion.toString());
        }
        if (StringUtils.isNotBlank(manufacturerId)) {
        	stringBuilder.append("AND manufacturer.make_id = ? ");
        	argsList.add(manufacturerId.toString());
        }
        return stringBuilder.toString();
    }


    /**
     * 获取所查询固件的待升级版本
     *
     * @return
     */
    private void getFirmwarePreviousVersion(List<Map<String, Object>> firmwareList) {
        List<Map<String, Object>> singleList = null;

		for (Map<String, Object> firmware : firmwareList) {
			singleList = queryFirmwarePreviousVersion(firmware.get("id").toString());
			firmware.put("prepared", singleList);
			// 查询区域名称
			String areaId = null == firmware.get("areaId") ? "" : firmware.get("areaId").toString();
			String areaName = "";
			if (StringUtils.isNotBlank(areaId)) {
				String[] area = areaId.split(",");
				if (null != area && area.length > 0) {
					List<Integer> arealist = new ArrayList<Integer>();
					for (int i = 0, inv = area.length; i < inv; i++) {
						arealist.add(Integer.parseInt(area[i]));
					}
					if (null != arealist && arealist.size() > 0) {
						List<Area> areal = amsAreaService.findAreasByIds(arealist);
						for (Area a : areal) {
							areaName += a.getName() + "，";
						}
                        if(areaName.lastIndexOf("，") > -1){
                            areaName = areaName.substring(0, areaName.lastIndexOf("，"));
                        }

					}
				}
			}
			firmware.put("areaName", areaName);
		}
    }

    /**
     * 查询单个固件的待升级版本
     *
     * @return
     */
    private List<Map<String, Object>> queryFirmwarePreviousVersion(String firmwareId) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append("prepared.firmware_previous_id id, ");
        stringBuilder.append("(SELECT f.firmware_version FROM t_firmware_info f WHERE f.id = prepared.firmware_previous_id) firmwareVersion, ");
        stringBuilder.append("prepared.need_force_upgrade needForceUpgrade ");
        stringBuilder.append("FROM ");
        stringBuilder.append("t_firmware_prepared prepared ");
        stringBuilder.append("WHERE ");
        stringBuilder.append("prepared.firmware_id = ? ");

        return baseDao.queryForMap(stringBuilder.toString(), new Object[]{firmwareId});
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
            FirmwarePrepared firmwarePrepared = null;
            for (int i = 0; i < firmwarePreviousIds.length; i++) {
                String[] idAndForces = firmwarePreviousIds[i].split(",");
                firmwarePrepared = new FirmwarePrepared();
                firmwarePrepared.setFirmwareId(firmwareInfoId);
                firmwarePrepared.setFirmwarePreviousId(idAndForces[0]);
                if (idAndForces[1].equals("1")) {
                    firmwarePrepared.setNeedForceUpgrade(true);
                } else {
                    firmwarePrepared.setNeedForceUpgrade(false);
                }
                firmwarePreparedMapper.insert(firmwarePrepared);
            }
        }
    }

    /**
     * 删除待升级版本
     *
     * @param firmwareInfoId
     */
    private void deleteFirmwarePreviousVersion(String firmwareInfoId) {
        if (StringUtils.isNotEmpty(firmwareInfoId)) {
            firmwarePreparedMapper.deleteByFirmwareId(firmwareInfoId);
        }
    }

    /**
     * 拼接修改固件页面待升级版本信息
     *
     * @return
     */
    private List<Map<String, Object>> buildPreviousVersionforEdit(String deviceId, String firmwareId, String firmwareVersion) {
        List<Map<String, Object>> retList = new ArrayList<>();

        //获取选择的待升级版本
        FirmwarePrepared firmwarePrepared = new FirmwarePrepared();
        firmwarePrepared.setFirmwareId(firmwareId);
        List<FirmwarePrepared> firmwarePreparedList = firmwarePreparedMapper.queryList(firmwarePrepared);

        //获取所有的待升级版本
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("deviceId", deviceId);
        Map<String, Object> versionMap = searchFirmwareVersion(parameter);
        Map<String, String> data = (Map<String, String>) versionMap.get(Constant.DATA);


        Map<String, Object> retMap = null;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            //去除本身版本
            if (entry.getValue().equals(firmwareVersion)) {
                continue;
            }
            retMap = new HashMap<>();
            retMap.put("firmwareId", entry.getKey());
            retMap.put("firmwareVersion", entry.getValue());
            retMap.put("isChoosed", "0");
            for (FirmwarePrepared prepared : firmwarePreparedList) {
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
     * 记录操作日志
     */
    private void recordOperationLog(Map<String, Object> parameter, String operation)
    {
        //记录操作日志
        String userName = null != parameter.get("userName") ? parameter.get("userName").toString() : "";
        String roleName = null != parameter.get("roleName") ? parameter.get("roleName").toString() : "";
        if (StringUtils.isNotEmpty(userName)) {
            OperationLogUtil.getInstance().recordOperationLog(userName, roleName, operation,
                    JSON.toJSON(parameter).toString(), CategoryEnum.UPGRADE_MANAGER_SERVICE.name(),
                    CategoryEnum.UPGRADE_MANAGER_SERVICE.description());
        }
    }

    /**
     * 判断是否存在相同的固件版本（根据版本，生产商、设备型号判断是否唯一）
     * @param firmwareVersion
     * @param deviceId
     * @return
     */
    private Boolean isUnique(String firmwareVersion, String deviceId, String firmwareId)
    {
        Map<String, String> para = new HashMap<>();
        para.put("firmwareVersion",firmwareVersion);
        para.put("deviceId",deviceId);
        para.put("id",firmwareId);
        int count = firmwareInfoMapper.selectCount(para);
        return count < 1 ? true : false;
    }

}
