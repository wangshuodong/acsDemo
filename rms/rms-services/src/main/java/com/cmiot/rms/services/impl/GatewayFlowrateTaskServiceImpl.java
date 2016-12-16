package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.enums.UpgradeTaskEventEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.FlowRateTaskMapper;
import com.cmiot.rms.dao.mapper.GatewayFlowrateTaskDetailMapper;
import com.cmiot.rms.dao.model.FlowRateTask;
import com.cmiot.rms.dao.model.GatewayFlowrateTaskDetail;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.GatewayFlowrateTaskService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

/**
 * Created by zoujiang  on 2016/6/16.
 */

public class GatewayFlowrateTaskServiceImpl implements GatewayFlowrateTaskService {

    private final Logger logger = LoggerFactory.getLogger(GatewayFlowrateTaskServiceImpl.class);

    @Autowired
    FlowRateTaskMapper flowRateTaskMapper; 

    @Autowired
    private AreaService amsAreaService;

    @Autowired
    private GatewayInfoService gatewayInfoService;
    
    @Autowired
    private InstructionMethodService instructionMethodService;
    @Autowired
    private GatewayFlowrateTaskDetailMapper gatewayFlowrateTaskDetailMapper;
    
    /**
     * 根据条件查询该网关的流量查询任务列表
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryFlowrateTaskList(Map<String, Object> parameter) {
        logger.info("Start invoke queryFlowrateTaskList:{}", parameter);
        String taskName = null != parameter.get("taskName") ? parameter.get("taskName").toString(): null;
        Integer taskTriggerMode = null != parameter.get("taskTriggerMode") ? Integer.valueOf(parameter.get("taskTriggerMode").toString()): null;
        Integer areaId = null != parameter.get("areaId") ? Integer.valueOf(parameter.get("areaId").toString()): null;

        int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
        int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;

        Map<String, Object> retMap = new HashMap<>();
        try
        {
            //区域查询（查询当前区域的所有子区域）
            List<Integer> areaIds = null;
            Map<Integer, String> areaMap = new HashMap<>();
            List<Area> areas = null;
            if(null != areaId)
            {
                areas = amsAreaService.findChildArea(areaId);
            }
            else
            {
                String uid =  null != parameter.get("uid") ? parameter.get("uid").toString(): "";
                if(StringUtils.isNotBlank(uid))
                {
                    areas = amsAreaService.findAreaByAdmin(uid);
                }
            }

            if(null != areas && areas.size() > 0)
            {
                areaIds = new ArrayList<>();
                for(Area area :areas)
                {
                    areaIds.add(area.getId());
                    areaMap.put(area.getId(), area.getName());
                }
            }

            //查询列表
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("taskName", taskName);
            queryMap.put("taskTriggerMode", taskTriggerMode);
            queryMap.put("areaIds", areaIds);

            PageHelper.startPage(page, pageSize);
            List<FlowRateTask> tasks = flowRateTaskMapper.selectList(queryMap);

            long total = ((Page) tasks).getTotal();

            for(FlowRateTask task : tasks)
            {
                if(null != task.getAreaId())
                {
                    if(areaMap.containsKey(task.getAreaId()))
                    {
                        task.setAreaName(areaMap.get(task.getAreaId()));
                    }
                    else
                    {
                        Area area = amsAreaService.findAreaById(Integer.valueOf(task.getAreaId()));
                        if(null != area)
                        {
                            task.setAreaName(area.getName());
                        }
                    }

                }
                if(task.getTaskTriggerMode() == 1)
                {
                    task.setTaskTriggerCondition(DateTools.format(task.getTaskStartTime().toString() + "000", DateTools.YYYY_MM_DD_HH_MM_SS) + "-"
                            + DateTools.format(task.getTaskEndTime().toString() + "000", DateTools.YYYY_MM_DD_HH_MM_SS));
                }
                else if(task.getTaskTriggerMode() == 2)
                {
                    task.setTaskTriggerCondition(UpgradeTaskEventEnum.getDescription(task.getTaskTriggerEvent()));
                }
            }

            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
            retMap.put(Constant.DATA, JSON.toJSON(tasks));
            retMap.put("total", total);
            retMap.put("page", page);
            retMap.put("pageSize", pageSize);
        }
        catch (Exception e)
        {
            logger.error("queryFlowrateTaskList exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }
        logger.info("End invoke queryFlowrateTaskList:{}", retMap);
        return retMap;
    }

    /**
     * 新增任务
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> addFlowrateTask(Map<String, Object> parameter) {
        logger.info("Start invoke addFlowrateTask:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        FlowRateTask fileTask = new FlowRateTask();
        try {
            org.apache.commons.beanutils.BeanUtils.populate(fileTask, parameter);
        } catch (Exception e) {
            logger.error("addFlowrateTask covert failed!");
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_ERROR.getResultMsg());
            return retMap;
        }
        if(isParameterISNull(fileTask))
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            return retMap;
        }
        
        //查询是否存在相同任务
        boolean flag = isExistSameTask(parameter);
        if(flag){
        	retMap.put(Constant.CODE, ErrorCodeEnum.BACKUPTASK_EXIST_SAME_NAME.getResultCode());
            retMap.put(Constant.MESSAGE, "已存在相同名称的批量操作任务");
            return retMap;
        }
        fileTask.setId(UniqueUtil.uuid());


        fileTask.setTaskCreateTime((int)(System.currentTimeMillis()/1000));
        if(fileTask.getTaskTriggerMode() == 1){
        	//定时
        	fileTask.setStatus(1);
        }else if(fileTask.getTaskTriggerMode() == 2){
        	//事件触发
        	fileTask.setStatus(2);
        	fileTask.setTaskStartTime((int)(System.currentTimeMillis()/1000));
        }
        try {
			flowRateTaskMapper.insertSelective(fileTask);
		} catch (Exception e) {
			e.printStackTrace();
		}

        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        logger.info("End invoke addFlowrateTask:{}", retMap);
        return retMap;
    }

    private boolean isExistSameTask(Map<String, Object> parameter) {

    	String taskName = null != parameter.get("taskName") ? parameter.get("taskName").toString(): null;
        Integer taskTriggerMode = null != parameter.get("taskTriggerMode") ? Integer.valueOf(parameter.get("taskTriggerMode").toString()): null;
        Integer taskTriggerEvent = null != parameter.get("taskTriggerEvent") ? Integer.valueOf(parameter.get("taskTriggerEvent").toString()): null;
        Integer areaId = null != parameter.get("areaId") ? Integer.valueOf(parameter.get("areaId").toString()): null;
        Integer  startTime = parameter.get("startTime") == null ? null : Integer.parseInt(parameter.get("startTime").toString());
        Integer  endTime = parameter.get("endTime") == null ? null : Integer.parseInt(parameter.get("endTime").toString());
        
         //区域查询（查询当前区域的所有子区域）
        List<Integer> areaIds = null;
        Map<Integer, String> areaMap = new HashMap<>();
        List<Area> areas = null;
        if(null != areaId)
        {
            areas = amsAreaService.findChildArea(areaId);
        }
        else
        {
            String uid =  null != parameter.get("uid") ? parameter.get("uid").toString(): "";
            if(StringUtils.isNotBlank(uid))
            {
                areas = amsAreaService.findAreaByAdmin(uid);
            }
        }

        if(null != areas && areas.size() > 0)
        {
            areaIds = new ArrayList<>();
            for(Area area :areas)
            {
                areaIds.add(area.getId());
                areaMap.put(area.getId(), area.getName());
            }
        }

        //查询列表
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("taskName", taskName);
        queryMap.put("taskTriggerMode", taskTriggerMode);
        queryMap.put("areaIds", areaIds);
        queryMap.put("taskTriggerEvent", taskTriggerEvent);
        queryMap.put("startTime", startTime);
        queryMap.put("endTime", endTime);

        List<FlowRateTask> tasks = flowRateTaskMapper.selectList(queryMap);
    	if(tasks != null && tasks.size() > 0){
    		if(parameter.get("id") == null || "".equals(parameter.get("id").toString())){
    			//新增时， 如果有相同的，说明存在本次任务相同的任务
    			return true;
    		}
    		
    		if(parameter.get("id") != null && !tasks.get(0).getId().equals(parameter.get("id").toString())){
    			//编辑的时候 ，如果查询出来的任务id和当前id不一样， 标识存在其他的任务和本次任务相同
    			return true;
    		}
    		
    	}
    	
		return false;
	}

	/**
     * 编辑流量查询任务，提供进入修改页面数据
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> editFlowrateTask(Map<String, Object> parameter) {
        logger.info("Start invoke editFlowrateTask:{}", parameter);
        String id = null != parameter.get("id") ? parameter.get("id").toString(): "";

        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isBlank(id)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke editFlowrateTask:{}", retMap);
            return retMap;
        }

        FlowRateTask task = flowRateTaskMapper.selectByPrimaryKey(id);
        if(null != task.getAreaId())
        {
            Area area = amsAreaService.findAreaById(Integer.valueOf(task.getAreaId()));
            if(null != area)
            {
                task.setAreaName(area.getName());
            }
        }
        if(task.getTaskTriggerMode() == 1)
        {
            task.setStartTime(task.getTaskStartTime().toString());
            task.setEndTime(task.getTaskEndTime().toString());
        }
        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        retMap.put(Constant.DATA, JSON.toJSON(task));

        logger.info("End invoke editFlowrateTask:{}", retMap);
        return retMap;
    }

    /**
     * 更新流量查询任务
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> updateFlowrateTask(Map<String, Object> parameter) {
        logger.info("Start invoke updateFlowrateTask:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        FlowRateTask newTask = new FlowRateTask();
        try {
            org.apache.commons.beanutils.BeanUtils.populate(newTask, parameter);
        } catch (Exception e) {
            logger.error("updateFlowrateTask covert failed!");
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_ERROR.getResultMsg());
            return retMap;
        }

        //获取原始的数据
        FlowRateTask originalTask = flowRateTaskMapper.selectByPrimaryKey(newTask.getId());
        if(originalTask.getStatus() == 2)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.TASK_IS_RUNNING.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.TASK_IS_RUNNING.getResultMsg());
            return retMap;
        }

        if(newTask.getTaskName() != null && !"".equals(newTask.getTaskName())){
        	originalTask.setTaskName(newTask.getTaskName());
        }
        if(newTask.getTaskTriggerMode() != null && newTask.getTaskTriggerMode() > 0){
        	originalTask.setTaskTriggerMode(newTask.getTaskTriggerMode());
        }
        if(newTask.getTaskTriggerEvent() != null && newTask.getTaskTriggerEvent() > 0){
        	originalTask.setTaskTriggerEvent(newTask.getTaskTriggerEvent());
        }
        if(newTask.getStartTime() != null && !"".equals(newTask.getStartTime())){
        	originalTask.setStartTime(newTask.getStartTime());
        }
        if(newTask.getEndTime() != null && !"".equals(newTask.getEndTime())){
        	originalTask.setEndTime(newTask.getEndTime());
        }
        if(newTask.getTaskStartTime() != null &&  newTask.getTaskStartTime() > 0){
        	originalTask.setTaskStartTime(newTask.getTaskStartTime());
        }
        if(newTask.getTaskEndTime() != null && newTask.getTaskEndTime()> 0){
        	originalTask.setTaskEndTime(newTask.getTaskEndTime());
        }
        if(newTask.getAreaId() != null && newTask.getAreaId() > 0){
        	originalTask.setAreaId(newTask.getAreaId());
        }
        
        //查询是否存在相同任务
        boolean flag = isExistSameTask(parameter);
        if(flag){
        	retMap.put(Constant.CODE, ErrorCodeEnum.BACKUPTASK_EXIST_SAME_NAME.getResultCode());
            retMap.put(Constant.MESSAGE, "已存在相同名称的批量操作任务");
            return retMap;
        }
        if(originalTask.getTaskTriggerMode() == 1){
        	//定时
        	originalTask.setStatus(1);
        }else if(originalTask.getTaskTriggerMode() == 2){
        	//事件触发
        	originalTask.setStatus(2);
        }
        flowRateTaskMapper.updateByPrimaryKey(originalTask);

        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        logger.info("End invoke updateFlowrateTask:{}", retMap);
        return retMap;
    }

    /**
     * 删除流量查询任务
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> deleteFlowrateTask(Map<String, Object> parameter) {
        logger.info("Start invoke deleteFlowrateTask:{}", parameter);
        String id = null != parameter.get("id") ? parameter.get("id").toString(): "";

        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isBlank(id)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke deleteFlowrateTask:{}", retMap);
            return retMap;
        }

        flowRateTaskMapper.deleteByPrimaryKey(id);
       
        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());

        logger.info("End invoke deleteFlowrateTask:{}", retMap);
        return retMap;
    }



    /**
     * 设置值，并判断是否存在相同的任务
     * @param fileTask
     * @return
     */
 /*   private Boolean setGatewayFlowrateTask(FlowRateTask fileTask, String originalTaskId)
    {
        if(fileTask.getTaskTriggerMode() == 1)
        {
            fileTask.setTaskStartTime(DateTools.timeStrToStamp(fileTask.getStartTime()));
            fileTask.setTaskEndTime(DateTools.timeStrToStamp(fileTask.getEndTime()));
            fileTask.setTaskTriggerEvent(0);
            if(StringUtils.isBlank(originalTaskId))
            {
                fileTask.setTaskCreateTime(DateTools.getCurrentSecondTime());
            }

            FlowRateTask sTask = new FlowRateTask();
            sTask.setTaskStartTime(fileTask.getTaskStartTime());
            sTask.setTaskEndTime(fileTask.getTaskEndTime());
            sTask.setAreaId(fileTask.getAreaId());
            if(StringUtils.isNotBlank(originalTaskId))
            {
                sTask.setId(originalTaskId);
            }
            int count = flowRateTaskMapper.selectSameTaskForTime(sTask);
            if(count > 0)
            {
                return false;
            }


        }
        else if(fileTask.getTaskTriggerMode() == 2)
        {
            fileTask.setTaskStartTime(0);
            fileTask.setTaskEndTime(0);
            if(StringUtils.isBlank(originalTaskId))
            {
                fileTask.setTaskCreateTime(DateTools.getCurrentSecondTime());
            }

            FlowRateTask sTask = new FlowRateTask();
            sTask.setTaskTriggerEvent(fileTask.getTaskTriggerEvent());
            sTask.setAreaId(fileTask.getAreaId());
            if(StringUtils.isNotBlank(originalTaskId))
            {
                sTask.setId(originalTaskId);
            }
            int count = flowRateTaskMapper.selectSameTaskForEvent(sTask);
            if(count > 0)
            {
                return false;
            }
        }
        return true;
    }*/

    /**
     * 判断是否存在相同名称的任务
     * @param taskName
     * @param originalTaskId
     * @return
     */
   /* private Boolean isExistSameNameTask(String taskName, String originalTaskId)
    {
        FlowRateTask task = new FlowRateTask();
        task.setTaskName(taskName);
        if(StringUtils.isNotBlank(originalTaskId))
        {
            task.setId(originalTaskId);
        }

        int count = flowRateTaskMapper.selectSameTaskByName(task);
        if(count > 0 )
        {
            return true;
        }
        return false;
    }
*/
    /**
     * 判断必要的参数是否为空
     * @param fileTask
     * @return
     */
    private Boolean isParameterISNull(FlowRateTask fileTask)
    {
        if(StringUtils.isBlank(fileTask.getTaskName()) || (null == fileTask.getAreaId() || 0 == fileTask.getAreaId())
                || (null == fileTask.getTaskTriggerMode() || 0 == fileTask.getTaskTriggerMode()))
        {
            return true;
        }

        return false;
    }

	@Override
	public void executeTask(String sn,String oui, int eventCode) {
		logger.info("begin execute flowrate Task...sn:"+ sn + "-oui:"+oui +"-eventCode:"+eventCode);
		//查询该网关是否在网关版本，流量 查询任务中， 如果在则执行
		GatewayInfo queryGW = new GatewayInfo();
		queryGW.setGatewaySerialnumber(sn);
		queryGW.setGatewayFactoryCode(oui);
		GatewayInfo gateway = gatewayInfoService.selectGatewayInfo(queryGW);
		if(gateway != null && gateway.getGatewayAreaId() != null && !"".equals(gateway.getGatewayAreaId())){
			//区域匹配的时候执行任务
			logger.info("begin execute flowrate Task...areaId:"+ gateway.getGatewayAreaId());
	        List<Area> areas = amsAreaService.findParents(Integer.parseInt(gateway.getGatewayAreaId()));
	        
	        List<Integer>  areaIds = new ArrayList<>();
            for(Area area :areas)
            {
                areaIds.add(area.getId());
            }
            areaIds.add(Integer.parseInt(gateway.getGatewayAreaId()));
            logger.info("begin execute flowrate Task...paraent areaId size:"+ areaIds.size());
	        
	        Map<String, Object> param = new HashMap<String, Object>();
			param.put("eventCode", eventCode);
			param.put("areaIds", areaIds);
			logger.info("begin execute flowrate Task...paraent areaIds size:"+ areaIds.size());
	        List<Map<String, Object>> list = flowRateTaskMapper.queryExistTaskWithGatewayUuid(param);
	        logger.info("begin execute flowrate Task...paraent task size:"+ list.size());
	        if(list !=  null && list.size() > 0){
	        	//该网关需要查询流量
	        	getFlowrateAndUpdateGateway(gateway, list);
	        }
		
		}
		
		
	}

	private void getFlowrateAndUpdateGateway(GatewayInfo gateway, List<Map<String, Object>> taskList) {
		
		String gpon = "InternetGatewayDevice.WANDevice.[0-9]+.X_CMCC_GponInterfaceConfig.Stats.BytesReceived";
		String epon = "InternetGatewayDevice.WANDevice.[0-9]+.X_CMCC_EPONInterfaceConfig.Stats.BytesReceived";
		String lan = "InternetGatewayDevice.WANDevice.[0-9]+.WANEthernetInterfaceConfig.Stats.BytesReceived";
		
		List<String> nameList = new ArrayList<String>();
        Map<String, Object> nameMap = instructionMethodService.getParameterNames(gateway.getGatewayMacaddress(), "InternetGatewayDevice.WANDevice.", false);
        logger.info("begin execute flowrate Task...total:"+ nameMap.size());
        
        Set<String> keys = nameMap.keySet();
        for(String key : keys){
        	if(key.matches(gpon)|| key.matches(epon)|| key.matches(lan)){
        		
        		nameList.add(key);
        	}
        }
        logger.info("begin execute flowrate Task...match size:"+ nameList.size());
        Map<String, Object> valueMap = instructionMethodService.getParameterValues(gateway.getGatewayMacaddress(), nameList);
        logger.info("begin execute flowrate Task...match values:"+valueMap);
        Collection values = valueMap.values();
        Iterator it = values.iterator();
        String flowrate = "";
        while(it.hasNext()){
        	Object value = it.next();
        	if(value != null && !"".equals(value) && Integer.parseInt(value.toString()) > 0){
        		flowrate =value.toString();
        		break;
        	}
        }
        //存入明细表
        for(Map<String, Object> taskIdMap : taskList){
        	
        	GatewayFlowrateTaskDetail detail = new GatewayFlowrateTaskDetail();
        	detail.setId(UniqueUtil.uuid());
        	detail.setTaskId(taskIdMap.get("id").toString());
        	detail.setGatewayId(gateway.getGatewayUuid());
        	detail.setFlowrate(flowrate);
        	detail.setCreateDate((int) (System.currentTimeMillis()/1000));
        	detail.setFirmwareId(gateway.getGatewayFirmwareUuid());
        	detail.setGatewayMac(gateway.getGatewayMacaddress());
        	gatewayFlowrateTaskDetailMapper.insert(detail);
        }
        
        logger.info("begin execute flowrate Task...flowrate:"+ flowrate);
        if(!"".equals(flowrate) && !flowrate.equals(gateway.getFlowRate())){
        	//流量不为空且跟数据库中的值不等的时候更新到数据库
        	GatewayInfo info = new GatewayInfo();
        	info.setGatewayUuid(gateway.getGatewayUuid());
        	info.setFlowRate(flowrate);
        	gatewayInfoService.updateSelectGatewayInfo(info);
        }
	}
	
	 /**
     * 根据条件查询该网关的流量查询任务明细
     * 返回在升级范围内的网关的固件版本和流量信息
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryFlowrateTaskDetails(Map<String, Object> parameter) {
        logger.info("Start invoke queryFlowrateTaskList:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        String id = null != parameter.get("id") ? parameter.get("id").toString(): null;
        if(id == null || "".equals(id)){
        	retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            return retMap;
        }
        
        int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
        int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;

        try
        {
        	
        	Map<String, Object> params =new HashMap<String, Object>();
        	params.put("taskId", id);
        	PageHelper.startPage(page, pageSize);
        	List<Map<String, Object>> list = gatewayFlowrateTaskDetailMapper.selectByTaskId(params);
        	long total = ((Page) list).getTotal();

            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
            retMap.put(Constant.DATA, JSON.toJSON(list));
            retMap.put("total", total);
            retMap.put("page", page);
            retMap.put("pageSize", pageSize);
        	
        }
        catch (Exception e)
        {
            logger.error("queryFlowrateTaskDetails exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }
        logger.info("End invoke queryFlowrateTaskDetails:{}", retMap);
        return retMap;
    }

	
}
