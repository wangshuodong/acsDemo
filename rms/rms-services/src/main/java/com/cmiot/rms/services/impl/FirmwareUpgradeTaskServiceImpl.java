package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.enums.UpgradeTaskDetailStatusEnum;
import com.cmiot.rms.common.enums.UpgradeTaskStatusEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.FirmwareInfoMapper;
import com.cmiot.rms.dao.mapper.FirmwareUpgradeTaskMapper;
import com.cmiot.rms.dao.mapper.FirmwareUpgradeTaskTimeMapper;
import com.cmiot.rms.dao.model.*;
import com.cmiot.rms.services.*;
import com.cmiot.rms.services.instruction.InvokeInsService;
import com.cmiot.rms.services.util.InstructionUtil;
import com.cmiot.rms.services.util.OperationLogUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by panmingguo on 2016/1/25.
 */
public class FirmwareUpgradeTaskServiceImpl implements FirmwareUpgradeTaskService {

    private final Logger logger = LoggerFactory.getLogger(FirmwareUpgradeTaskServiceImpl.class);

    @Autowired
    FirmwareUpgradeTaskMapper firmwareUpgradeTaskMapper;

    @Autowired
    FirmwareUpgradeTaskDetailService firmwareUpgradeTaskDetailService;

    @Autowired
    FirmwareUpgradeTaskTimeMapper firmwareUpgradeTaskTimeMapper;

    @Autowired
    GatewayInfoService gatewayInfoService;

    @Autowired
    FirmwareInfoMapper firmwareInfoMapper;

    @Autowired
    DeviceInfoService deviceInfoService;

    @Autowired
    InvokeInsService invokeInsService;

    @Autowired
    private AreaService amsAreaService;
    
    @Autowired
    private SyncInfoToFirstLevelPlatformService syncInfoToFirstLevelPlatformService;

    @Value("${file.server.userName}")
    String userName;

    @Value("${file.server.password}")
    String password;
    

    /**
     * 根据名称查询升级信息
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> searchUpgradeTask(Map<String, Object> parameter) {
        logger.info("Start invoke searchUpgradeTask:{}", parameter);
        String taskName = parameter.get("taskName") != null ? parameter.get("taskName").toString() : null;
        FirmwareUpgradeTask firmwareUpgradeTask = new FirmwareUpgradeTask();
        firmwareUpgradeTask.setTaskName(taskName);

        int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
        int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;

        Map<String, Object> retMap = new HashMap<>();
        try {

            String uid =  null != parameter.get("uid") ? parameter.get("uid").toString(): "";
            if(StringUtils.isNotBlank(uid))
            {
                List<Area> areas = amsAreaService.findAreaByAdmin(uid);
                firmwareUpgradeTask.setAreaId(getAreaIds(areas));
            }
            PageHelper.startPage(page, pageSize);
            List<FirmwareUpgradeTask> list = firmwareUpgradeTaskMapper.queryList4Page(firmwareUpgradeTask);

            //通过ams接口查询区域名称
            for(FirmwareUpgradeTask task : list)
            {
                if(StringUtils.isNotEmpty(task.getAreaId()))
                {
                    Area area = amsAreaService.findAreaById(Integer.valueOf(task.getAreaId()));
                    if(null != area)
                    {
                        task.setAreaName(area.getName());
                    }
                }
            }

            //查询升级进度
            for (FirmwareUpgradeTask task : list) {
                task.setUpgradeProcess(getTaskUpgradeProgress(task.getId()));
            }

            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
            retMap.put(Constant.DATA, JSON.toJSON(list));
            retMap.put("total", ((Page) list).getTotal());
            retMap.put("page", page);
            retMap.put("pageSize", pageSize);
        } catch (Exception e) {
            logger.error("searchUpgradeTask exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }
        logger.info("End invoke searchUpgradeTask:{}", retMap);
        return retMap;
    }

    /**
     * 新建升级任务页面的查询页面
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> upgradeTaskAddSearch(Map<String, Object> parameter) {
        logger.info("Start invoke upgradeTaskAddSearch:{}", parameter);
        int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
        int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;

        Map<String, Object> retMap = new HashMap<>();
        try {
            GatewayInfo gatewayInfo = new GatewayInfo();

            Object factoryCode = parameter.get("factoryCode");
            Object deviceId = parameter.get("deviceId");
            Object firmwareId = parameter.get("firmwareId");
            //生产商，设备型号和固件版本都存在时才做查询
            if(null != factoryCode && null != deviceId && null != firmwareId)
            {
                gatewayInfo.setGatewayFactoryCode(factoryCode.toString());
                gatewayInfo.setGatewayDeviceUuid(deviceId.toString());//设备型号
                gatewayInfo.setGatewayFirmwareUuid(firmwareId.toString());//固件版本

                String gatewayAreaId = null != parameter.get("gatewayAreaId") ? parameter.get("gatewayAreaId").toString() : null;
                setAreaIds(gatewayAreaId, gatewayInfo, parameter);

                Map<String, Object> queryMap = gatewayInfoService.queryList4Page(page, pageSize, gatewayInfo);

                retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
                retMap.put("total", queryMap.get("total"));
                retMap.put("page", page);
                retMap.put("pageSize", pageSize);
                retMap.put(Constant.DATA, JSON.toJSON(queryMap.get(Constant.DATA)));
            }
            else
            {
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            }
        } catch (Exception e) {
            logger.error("upgradeTaskAddSearch exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }

        logger.info("End invoke upgradeTaskAddSearch:{}", retMap);
        return retMap;
    }

    /**
     * 新建升级任务页面的升级任务设置页面
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> upgradeTaskAddSetting(Map<String, Object> parameter) {
        logger.info("Start invoke upgradeTaskAddSetting:{}", parameter);
        //查询方式 check_choose选择复选框方式,fast_choose快速选择方式
        String submitWay = null != parameter.get("submitWay") ? parameter.get("submitWay").toString() : "";

        //网关ID
        String gatewayInfoIds = null != parameter.get("gatewayInfoIds") ? parameter.get("gatewayInfoIds").toString() : null;

        Map<String, Object> retMap = new HashMap<>();
        try {
            if (submitWay.equals("checkChoose") && StringUtils.isNotEmpty(gatewayInfoIds)) {
                String[] ids = gatewayInfoIds.split(",");
                List<String> idList = new ArrayList<>();
                for (String id : ids) {
                    idList.add(id);
                }
                //通过ids查询对应的网关
                List<GatewayInfo> result = gatewayInfoService.queryListByIds(idList);
                for(GatewayInfo gatewayInfo : result)
                {
                    String areaId = gatewayInfo.getGatewayAreaId();
                    if(StringUtils.isNotBlank(areaId))
                    {
                        Area tempArea = amsAreaService.findAreaById(Integer.valueOf(areaId));
                        if(tempArea != null)
                        {
                            gatewayInfo.setGatewayAreaName(tempArea.getName());
                        }
                    }
                }
                retMap.put(Constant.DATA, JSON.toJSON(result));

            } else {//快速选择
                GatewayInfo gatewayInfo = getGatewayInfo(parameter);

                int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
                int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;

                int submitNum = getSubmitNum(parameter);

                if ((submitNum != -1) && pageSize > submitNum) {
                    pageSize = submitNum;
                }

                //查询升级任务
                Map<String, Object> queryMap = gatewayInfoService.queryList4Page(page, pageSize, gatewayInfo);
                int total = Integer.valueOf(queryMap.get("total").toString());
                if ((submitNum != -1) && total > submitNum) {
                    total = submitNum;
                }

                retMap.put("total", total);
                retMap.put(Constant.DATA, JSON.toJSON(queryMap.get(Constant.DATA)));

            }
            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        } catch (Exception e) {
            logger.error("upgradeTaskAddSetting exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }

        logger.info("End invoke upgradeTaskAddSetting:{}", retMap);
        return retMap;
    }

    /**
     * 添加升级任务
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> addUpgradeTask(Map<String, Object> parameter) {
        logger.info("Start invoke addUpgradeTask:{}", JSON.toJSON(parameter));

        Map<String, Object> retMap = new HashMap<>();
        //查询方式 check_choose选择复选框方式,fast_choose快速选择方式
        String submitWay = null != parameter.get("submitWay") ? parameter.get("submitWay").toString() : "";

        List<GatewayInfo> gatewayInfoList = null;

        if (submitWay.equals("fastChoose")) {
            GatewayInfo gatewayInfo = getGatewayInfo(parameter);

            int submitNum = getSubmitNum(parameter);
            int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;

            //查询升级任务
            Map<String, Object> queryMap = gatewayInfoService.queryList4Page(page, submitNum, gatewayInfo);

            gatewayInfoList = (List<GatewayInfo>) queryMap.get(Constant.DATA);

        } else {

            //网关ID
            String gatewayInfoIds = null != parameter.get("gatewayInfoIds") ? parameter.get("gatewayInfoIds").toString() : null;
            if (StringUtils.isNotEmpty(gatewayInfoIds)) {
                String[] ids = gatewayInfoIds.split(",");
                List<String> idList = new ArrayList<>();
                for (String id : ids) {
                    idList.add(id);
                }
                //通过ids查询对应的网关
                gatewayInfoList = gatewayInfoService.queryListByIds(idList);
            }
        }

        if (null != gatewayInfoList && gatewayInfoList.size() > 0) {
            FirmwareInfo firmwareInfo = firmwareInfoMapper.selectByPrimaryKey(null != parameter.get("firmwareId") ? parameter.get("firmwareId").toString() : null);

            if(null == firmwareInfo)
            {
                retMap.put(Constant.CODE, ErrorCodeEnum.FIRMWARE_NOT_EXIST.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.FIRMWARE_NOT_EXIST.getResultMsg());
                return retMap;
            }
            FirmwareUpgradeTask firmwareUpgradeTask = buildFirmwareUpgradeTask(parameter);

            //任务触发方式：1:定时触发 2:条件触发
            String taskTriggerMode = null != parameter.get("taskTriggerMode") ? parameter.get("taskTriggerMode").toString() : "";

            //添加升级任务
            firmwareUpgradeTaskMapper.insert(firmwareUpgradeTask);

            //定时触发时添加升级时间
            if(taskTriggerMode.equals("1"))
            {
                //添加时间
                addUpgradeTime(parameter, firmwareUpgradeTask);
            }

            //添加升级任务详情
            List<String> macList = new ArrayList<String>();
            List<FirmwareUpgradeTaskDetail> detailList = new ArrayList<>();
            for (GatewayInfo gatewayInfo : gatewayInfoList) {
            	
            	macList.add(gatewayInfo.getGatewayMacaddress());
            	
                FirmwareUpgradeTaskDetail taskDetail = buildTaskDetail(gatewayInfo, firmwareInfo, firmwareUpgradeTask.getId(), 0, 0);
                detailList.add(taskDetail);
            }
            if(detailList.size() > 0)
            {
                firmwareUpgradeTaskDetailService.batchInsert(detailList);
            }

            if(taskTriggerMode.equals("1")){
            	//只有当任务触发方式为定时的时候才会有starttime,杭研接口只有starttime不为空的时候才会调用成功
            	//同步升级任务到杭研
            	Map<String, Object> reportMap = new HashMap<String, Object>();
            	reportMap.put("RPCMethod", "Report");
            	reportMap.put("ID", (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
            	reportMap.put("CmdType", "REPORT_UPGRADE_PLAN");
            	reportMap.put(Constant.SEQUENCEID, InstructionUtil.generate8HexString());
            	Map<String, Object> map = new HashMap<String, Object>();
            	map.put("PlanId", firmwareUpgradeTask.getId());

            	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            	String currentYMD = format.format(new Date());
            	currentYMD += " "+parameter.get("startTime");
            	SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            	try {
					map.put("BeginTime", format2.parse(currentYMD).getTime()/1000);
				} catch (ParseException e) {
					e.printStackTrace();
				}

            	map.put("GatewayList", macList);
            	reportMap.put("Parameter", map);
            	syncInfoToFirstLevelPlatformService.report("reportUpgradePlan",reportMap);
            }

            //记录操作日志
            recordOperationLog(parameter);

        }

        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());

        logger.info("End invoke addUpgradeTask:{}", retMap);
        return retMap;
    }

    /**
     * 查询升级任务详情
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> searchUpgradeTaskDetail(Map<String, Object> parameter) {
        logger.info("Start invoke searchUpgradeTaskDetail:{}", parameter);

        Map<String, Object> retMap = new HashMap<>();
        try {
            int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()):1;
            int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()):10;

            Map<String, Object> ret = firmwareUpgradeTaskDetailService.queryListByIdAndStatus(parameter);

            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
            retMap.put(Constant.DATA,  ret.get("list"));
            retMap.put("total", ret.get("total"));
            retMap.put("page", page);
            retMap.put("pageSize", pageSize);
        } catch (Exception e) {
            logger.error("searchUpgradeTaskDetail exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }

        logger.info("End invoke searchUpgradeTaskDetail:{}", retMap);
        return retMap;
    }

    /**
     * 网关页面点击升级时使用
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> upgradeSpecifiedGateway(Map<String, Object> parameter) {
        logger.info("Start invoke upgradeSpecifiedGateway:{}", parameter);
        String gatewayId = null != parameter.get("gatewayId") ? parameter.get("gatewayId").toString() : "";
        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isNotEmpty(gatewayId)) {

            try {
                GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayId);
                if(null == gatewayInfo) {
                	retMap.put(Constant.CODE, ErrorCodeEnum.GATEWAY_NOT_EXIST.getResultCode());
                    retMap.put(Constant.MESSAGE, ErrorCodeEnum.GATEWAY_NOT_EXIST.getResultMsg());
                    return retMap;
                }

                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.setId(StringUtils.isNotBlank(gatewayInfo.getGatewayDeviceUuid())?gatewayInfo.getGatewayDeviceUuid():"");
                //deviceInfo.setDeviceModel(gatewayInfo.getGatewayModel());
                //deviceInfo.setDeviceFactory(gatewayInfo.getGatewayFactoryCode());

                List<DeviceInfo> deviceInfoList = deviceInfoService.queryList(deviceInfo);
                if (null == deviceInfoList || deviceInfoList.size() < 1) {
                    retMap.put(Constant.CODE, ErrorCodeEnum.DEVICE_NOT_EXIST.getResultCode());
                    retMap.put(Constant.MESSAGE, ErrorCodeEnum.DEVICE_NOT_EXIST.getResultMsg());
                    return retMap;
                }

                //查询设备的固件列表
                FirmwareInfo firmwareInfo = new FirmwareInfo();
                firmwareInfo.setDeviceId(deviceInfoList.get(0).getId());
                firmwareInfo.setCheckStatus(2);//审批通过的数据
                List<FirmwareInfo> firmwareInfos = firmwareInfoMapper.queryList(firmwareInfo);

                //去除网关当前版本
                if(null != firmwareInfos && firmwareInfos.size() > 0)
                {
                    for(FirmwareInfo info : firmwareInfos)
                    {
                        if(info.getId().equals(gatewayInfo.getGatewayFirmwareUuid()))
                        {
                            firmwareInfos.remove(info);
                            break;
                        }
                    }
                }
                retMap.put(Constant.DATA, JSON.toJSON(firmwareInfos));
                retMap.put("gatewayId", gatewayId);
                retMap.put("currentFirmwareId", gatewayInfo.getGatewayFirmwareUuid());

                //查询最近一次升级的状态
                FirmwareUpgradeTaskDetail detail  = firmwareUpgradeTaskDetailService.searchLatelyImmediatelyDetail(gatewayId);
                if(null != detail)
                {
                    if(detail.getStatus() == UpgradeTaskDetailStatusEnum.PROCESSING.code())
                    {
                        retMap.put("latelyUpgradeStatus", UpgradeTaskDetailStatusEnum.PROCESSING.code());
                        retMap.put("latelyUpgradeMsg", UpgradeTaskDetailStatusEnum.PROCESSING.description());
                    }
                    else if(detail.getStatus() == UpgradeTaskDetailStatusEnum.FAILURE.code())
                    {
                        retMap.put("latelyUpgradeStatus", UpgradeTaskDetailStatusEnum.FAILURE.code());
                        retMap.put("latelyUpgradeMsg", UpgradeTaskDetailStatusEnum.FAILURE.description());
                    }
                    else if(detail.getStatus() == UpgradeTaskDetailStatusEnum.SUCSSESS.code())
                    {
                        retMap.put("latelyUpgradeStatus", UpgradeTaskDetailStatusEnum.SUCSSESS.code());
                        retMap.put("latelyUpgradeMsg", UpgradeTaskDetailStatusEnum.SUCSSESS.description());
                    }
                    else
                    {
                        retMap.put("latelyUpgradeStatus", -1);
                        retMap.put("latelyUpgradeMsg", "从未升级");
                    }
                }
                else
                {
                    retMap.put("latelyUpgradeStatus", -1);
                    retMap.put("latelyUpgradeMsg", "从未升级");
                }

                retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
            } catch (Exception e) {
                logger.error("upgradeSpecifiedGateway exception:{}", e);
                retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
            }
        } else {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
        }

        logger.info("End invoke upgradeSpecifiedGateway:{}", retMap);
        return retMap;
    }

    /**
     * 单个网关立即升级
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> upgradeImmediately(Map<String, Object> parameter) {
        logger.info("Start invoke upgradeImmediately:{}", parameter);
        String gatewayId = null != parameter.get("gatewayId") ? parameter.get("gatewayId").toString() : "";
        String firmwareId = null != parameter.get("firmwareId") ? parameter.get("firmwareId").toString() : "";
        FirmwareInfo firmwareInfo = firmwareInfoMapper.selectByPrimaryKey(firmwareId);

        GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayId);

        Map<String, Object> retMap = new HashMap<>();

        if (null == firmwareInfo || null == gatewayInfo) {
            retMap.put(Constant.CODE, ErrorCodeEnum.FIRMWARE_NOT_EXIST.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.FIRMWARE_NOT_EXIST.getResultMsg());
            return retMap;
        }

        Map<String, Object> upgradeJob = new HashMap<>();

        upgradeJob.put("gatewayId", gatewayId);
        upgradeJob.put("commandKey", "");
        upgradeJob.put("methodName", "Download");
        upgradeJob.put("fileType", "1 Firmware Upgrade Image");
        upgradeJob.put("url", firmwareInfo.getFirmwarePath());
        upgradeJob.put("userName", userName);
        upgradeJob.put("passWord", password);
        upgradeJob.put("targetFileName", firmwareInfo.getFirmwareName());
        upgradeJob.put("successURL", "");
        upgradeJob.put("failureURL", "");
        upgradeJob.put("fileSize", firmwareInfo.getFirmwareSize());
        upgradeJob.put("delaySeconds", "0");

        //升级任务Id
        String taskId = null != parameter.get("taskId") ? parameter.get("taskId").toString() : "";
        if(StringUtils.isNotEmpty(taskId))
        {
            upgradeJob.put("taskId", taskId);
        }
        else
        {
            FirmwareUpgradeTask firmwareUpgradeTask = buildFirmwareUpgradeTaskForImmediately(gatewayInfo.getGatewayDeviceUuid(), firmwareId);
            //添加升级任务
            firmwareUpgradeTaskMapper.insert(firmwareUpgradeTask);

            FirmwareUpgradeTaskDetail taskDetail = buildTaskDetail(gatewayInfo, firmwareInfo, firmwareUpgradeTask.getId(), 1 , DateTools.getCurrentSecondTime());
            firmwareUpgradeTaskDetailService.addFirmwareUpgradeTaskDetail(taskDetail);
            upgradeJob.put("taskId", firmwareUpgradeTask.getId());
        }

        try {
            logger.info("upgradeImmediately start invoke executeOne:{}", upgradeJob);
            Map<String, Object> respMap = invokeInsService.executeOne(upgradeJob);
            logger.info("upgradeImmediately end invoke executeOne:{}", respMap);

            if (null == respMap || (0 != (Integer) respMap.get("resultCode"))) {
                retMap.put(Constant.CODE, ErrorCodeEnum.INSTRUCT_SEND_FAILED.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.INSTRUCT_SEND_FAILED.getResultMsg());
                return retMap;
            }
        } catch (Exception e) {
            retMap.put(Constant.CODE, ErrorCodeEnum.INSTRUCT_SEND_EXCEPTION.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.INSTRUCT_SEND_EXCEPTION.getResultMsg());
            return retMap;
        }

        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        logger.info("End invoke upgradeImmediately:{}", retMap);
        return retMap;
    }

    /**
     * FirmwareUpgradeTask对象组装
     *
     * @param parameter
     * @return
     */
    private FirmwareUpgradeTask buildFirmwareUpgradeTask(Map<String, Object> parameter) {
        //任务开始时间
        String upgradeStartTime = null != parameter.get("startTime") ? parameter.get("startTime").toString() : "";
        //任务结束时间
        String upgradeEndTime = null != parameter.get("endTime") ? parameter.get("endTime").toString() : "";

        FirmwareUpgradeTask firmwareUpgradeTask = new FirmwareUpgradeTask();

        firmwareUpgradeTask.setTaskName(null != parameter.get("taskName") ? parameter.get("taskName").toString() : "");
        firmwareUpgradeTask.setTaskDescription(null != parameter.get("taskDescription") ? parameter.get("taskDescription").toString() : "");
        firmwareUpgradeTask.setTaskStartTime(upgradeStartTime);
        firmwareUpgradeTask.setTaskEndTime(upgradeEndTime);
        firmwareUpgradeTask.setTaskCreateTime(DateTools.getCurrentSecondTime());

        firmwareUpgradeTask.setTaskStatus(UpgradeTaskStatusEnum.NEW.code());

        Boolean isAutoStart = false;
        if (null != parameter.get("isAutoStart") && parameter.get("isAutoStart").equals("true")) {
            isAutoStart = true;
        }

        firmwareUpgradeTask.setTaskIsAutoStart(isAutoStart);

        firmwareUpgradeTask.setDeviceId(null != parameter.get("deviceId") ? parameter.get("deviceId").toString() : null);
        firmwareUpgradeTask.setFirmwareId(null != parameter.get("firmwareId") ? parameter.get("firmwareId").toString() : null);
        firmwareUpgradeTask.setAreaId(null != parameter.get("gatewayAreaId") ? parameter.get("gatewayAreaId").toString() : "0");


        //任务触发方式：1:定时触发 2:条件触发
        String taskTriggerMode = null != parameter.get("taskTriggerMode") ? parameter.get("taskTriggerMode").toString() : "";
        //为条件触发时添加触发事件：1:初始安装第一次启动时 2：周期心跳上报时 3：设备重新启动时
        if(taskTriggerMode.equals("2"))
        {
            int taskTriggerEvent = null != parameter.get("taskTriggerEvent") ? Integer.valueOf(parameter.get("taskTriggerEvent").toString()) : 1;
            firmwareUpgradeTask.setTaskTriggerEvent(taskTriggerEvent);
            firmwareUpgradeTask.setTaskPeriod(0);
        }
        else if(taskTriggerMode.equals("1"))
        {
            //定时触发包含：当天执行和本周执行
            String taskPeriod = null != parameter.get("taskPeriod") ? parameter.get("taskPeriod").toString() : "0";
            firmwareUpgradeTask.setTaskPeriod(Integer.valueOf(taskPeriod));
            firmwareUpgradeTask.setTaskTriggerEvent(0);
        }

        firmwareUpgradeTask.setTaskTriggerMode(Integer.valueOf(taskTriggerMode));

        firmwareUpgradeTask.setId(UniqueUtil.uuid());

        return firmwareUpgradeTask;
    }


    /**
     * 立即升级FirmwareUpgradeTask对象组装
     * @param deviceId
     * @param firmwareId
     * @return
     */
    private FirmwareUpgradeTask buildFirmwareUpgradeTaskForImmediately(String deviceId, String firmwareId) {

        FirmwareUpgradeTask firmwareUpgradeTask = new FirmwareUpgradeTask();
        firmwareUpgradeTask.setId(UniqueUtil.uuid());

        firmwareUpgradeTask.setTaskName(firmwareUpgradeTask.getId());
        firmwareUpgradeTask.setTaskDescription("");
        firmwareUpgradeTask.setTaskStartTime("");
        firmwareUpgradeTask.setTaskEndTime("");
        firmwareUpgradeTask.setTaskCreateTime(DateTools.getCurrentSecondTime());

        firmwareUpgradeTask.setTaskStatus(UpgradeTaskStatusEnum.PROCESSING.code());
        firmwareUpgradeTask.setTaskIsAutoStart(false);

        firmwareUpgradeTask.setDeviceId(deviceId);
        firmwareUpgradeTask.setFirmwareId(firmwareId);
        firmwareUpgradeTask.setAreaId("");

        firmwareUpgradeTask.setTaskTriggerMode(3);
        firmwareUpgradeTask.setTaskPeriod(0);
        firmwareUpgradeTask.setTaskTriggerEvent(0);

        return firmwareUpgradeTask;
    }

    /**
     * FirmwareUpgradeTaskDetail对象组装
     *
     * @param gatewayInfo
     * @param firmwareInfo
     * @param upgradeTaskId
     * @param  status
     * @param startTime
     * @return
     */
    private FirmwareUpgradeTaskDetail buildTaskDetail(GatewayInfo gatewayInfo, FirmwareInfo firmwareInfo,String upgradeTaskId, int status, int startTime) {
        FirmwareUpgradeTaskDetail firmwareUpgradeTaskDetail = new FirmwareUpgradeTaskDetail();
        firmwareUpgradeTaskDetail.setId(UniqueUtil.uuid());
        firmwareUpgradeTaskDetail.setGatewayId(gatewayInfo.getGatewayUuid());
        firmwareUpgradeTaskDetail.setGatewayName(gatewayInfo.getGatewayName());
        firmwareUpgradeTaskDetail.setFirmwareId(firmwareInfo.getId());
        firmwareUpgradeTaskDetail.setFirmwareVersion(firmwareInfo.getFirmwareVersion());
        firmwareUpgradeTaskDetail.setPreviousFirmwareId("");
        firmwareUpgradeTaskDetail.setPreviousFirmwareVersion("");
        firmwareUpgradeTaskDetail.setUpgradeTaskId(upgradeTaskId);
        firmwareUpgradeTaskDetail.setUpgradeStartTime(startTime);
        firmwareUpgradeTaskDetail.setUpgradeEndTime(0);
        firmwareUpgradeTaskDetail.setStatus(status);
        firmwareUpgradeTaskDetail.setIsRetry(false);
        firmwareUpgradeTaskDetail.setRetryTimes(0);
        return firmwareUpgradeTaskDetail;
    }

    /**
     * 时间转换
     *
     * @param date
     * @return
     */
    private int timeStrToStamp(String date) {
        int time = 100;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date parsedDate = dateFormat.parse(date);
            time = (int) (parsedDate.getTime() / 1000L);
        } catch (Exception e) {
        }
        return time;
    }

    /**
     * 查询升级任务的升级进度
     *
     * @param id
     * @return
     */
    private String getTaskUpgradeProgress(String id) {
        //获取任务的网关总数
        int total = firmwareUpgradeTaskDetailService.searchTaskDetailCount(id, null);
        //获取已经完成的网关数
        int success = firmwareUpgradeTaskDetailService.searchTaskDetailCount(id, "3");
        return success + "/" + total;
    }

    /**
     * 记录操作日志
     */
    private void recordOperationLog(Map<String, Object> parameter)
    {
        //记录操作日志
        String userName = null != parameter.get("userName") ? parameter.get("userName").toString() : "";
        String roleName = null != parameter.get("roleName") ? parameter.get("roleName").toString() : "";
        if (StringUtils.isNotEmpty(userName)) {
            OperationLogUtil.getInstance().recordOperationLog(userName, roleName, "新增升级任务",
                    JSON.toJSON(parameter).toString(), CategoryEnum.UPGRADE_MANAGER_SERVICE.name(),
                    CategoryEnum.UPGRADE_MANAGER_SERVICE.description());
        }
    }

    /**
     * 获取网关查询对象
     * @param parameter
     * @return
     */
    private GatewayInfo getGatewayInfo(Map<String, Object> parameter)
    {
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewayFactoryCode(null != parameter.get("factoryCode") ? parameter.get("factoryCode").toString() : null);
        gatewayInfo.setGatewayDeviceUuid(null != parameter.get("deviceId") ? parameter.get("deviceId").toString() : null);
        gatewayInfo.setGatewayFirmwareUuid(null != parameter.get("firmwareId") ? parameter.get("firmwareId").toString() : null);
        String gatewayAreaId = null != parameter.get("gatewayAreaId") ? parameter.get("gatewayAreaId").toString() : null;
        setAreaIds(gatewayAreaId, gatewayInfo, parameter);
        return gatewayInfo;
    }


    /**
     * 添加升级任务时间
     */
    private void addUpgradeTime( Map<String, Object> parameter,  FirmwareUpgradeTask firmwareUpgradeTask)
    {
        String taskPeriod = null != parameter.get("taskPeriod") ? parameter.get("taskPeriod").toString() : "0";

        //本周内执行
        if("1".equals(taskPeriod))
        {
            List<String> dayList = DateTools.getDayList();
            for(String day : dayList)
            {
                saveFirmwareUpgradeTaskTime(parameter, day, firmwareUpgradeTask);
            }
        }
        else//当天执行
        {
            String currentDay = DateTools.getCurrentDay();
            saveFirmwareUpgradeTaskTime(parameter, currentDay, firmwareUpgradeTask);
        }
    }

    /**
     * 保存升级时间到数据库
     * @param parameter
     * @param day
     * @param firmwareUpgradeTask
     */
    private void saveFirmwareUpgradeTaskTime(Map<String, Object> parameter, String day, FirmwareUpgradeTask firmwareUpgradeTask)
    {
        //任务开始时间
        String upgradeStartTime = null != parameter.get("startTime") ? parameter.get("startTime").toString() : "";
        //任务结束时间
        String upgradeEndTime = null != parameter.get("endTime") ? parameter.get("endTime").toString() : "";

        //比较开始时间是否大于结束时间，是：说明结束时间是第二天时间
        Boolean compareRet = DateTools.compareDate(upgradeStartTime, upgradeEndTime,DateTools.HH_MM_SS);

        String sStartTime = null;
        String sEndTime = null;
        sStartTime = day + " " + upgradeStartTime;
        if(compareRet)
        {
            sEndTime = DateTools.getNextDay(day) +  " " + upgradeEndTime;
        }
        else
        {
            sEndTime = day + " " + upgradeEndTime;
        }

        int iStartTime = timeStrToStamp(sStartTime);
        int iEndTime = timeStrToStamp(sEndTime);

        //保存升级时间记录
        FirmwareUpgradeTaskTime firmwareUpgradeTaskTime = new FirmwareUpgradeTaskTime();
        firmwareUpgradeTaskTime.setId(UniqueUtil.uuid());
        firmwareUpgradeTaskTime.setTaskStartTime(iStartTime);
        firmwareUpgradeTaskTime.setTaskEndTime(iEndTime);
        firmwareUpgradeTaskTime.setUpgradeTaskId(firmwareUpgradeTask.getId());
        firmwareUpgradeTaskTimeMapper.insert(firmwareUpgradeTaskTime);
    }

	@Override
	public Map<String, Object> queryUpgradeTaskByMacs(Map<String, Object> parameter) {
		
		
		//1.根据网关MAC查询网关ID
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        List<String> macList = (List<String>) macMap.get("GatewayList");
        parameter.remove(Constant.PARAMETER);
        parameter.remove(Constant.RPCMETHOD);
        if(macList == null || macList.size() ==0){
        	logger.info("根据MAC地址查询升级任务时失败，原因：MAC列表为空");
        	parameter.put(Constant.RESULTDATA, null);
        	parameter.put(Constant.RESULT, -102);
        }else{
        	
        	List<Map<String, Object>> tasks = firmwareUpgradeTaskMapper.queryUpgradeTaskByMacs(macList);
        	// 2.拼装发挥结果
        	for(Map<String, Object> map : tasks){
        		map.put("Stat", map.get("Stat")+"");
        	}
        	parameter.put(Constant.RESULTDATA, tasks);
        	parameter.put(Constant.RESULT, 0);
        }

        return parameter;
	}


    private String getAreaIds(List<Area> areas)
    {
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


    private void setAreaIds(String gatewayAreaId, GatewayInfo gatewayInfo, Map<String, Object> parameter)
    {
        if(StringUtils.isNotBlank(gatewayAreaId))
        {
            List<Area> areas = amsAreaService.findChildArea(Integer.valueOf(gatewayAreaId));
            gatewayInfo.setGatewayAreaId(getAreaIds(areas));
        }
        else
        {
            String uid = null != parameter.get("uid") ? parameter.get("uid").toString() : "";
            if (StringUtils.isNotBlank(uid)) {
                List<Area> areas = amsAreaService.findAreaByAdmin(uid);
                gatewayInfo.setGatewayAreaId(getAreaIds(areas));
            }
        }
    }

    /**
     * 获取查询条数
     * @param parameter
     * @return
     */
    private int getSubmitNum(Map<String, Object> parameter)
    {
        String strNum  = null != parameter.get("submitNum") ? parameter.get("submitNum").toString() : "";
        int submitNum = 10;
        if(StringUtils.isNotEmpty(strNum))
        {
            //表示查询全部
            if(strNum.equals("all"))
            {
                submitNum = -1;
            }
            else
            {
                submitNum = Integer.valueOf(strNum);
            }
        }
        return submitNum;
    }

}
