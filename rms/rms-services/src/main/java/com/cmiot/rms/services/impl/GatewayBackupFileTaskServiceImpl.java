package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.enums.UpgradeTaskEventEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.GatewayBackupFileTaskAreaMapper;
import com.cmiot.rms.dao.mapper.GatewayBackupFileTaskMapper;
import com.cmiot.rms.dao.model.GatewayBackupFileTask;
import com.cmiot.rms.dao.model.GatewayBackupFileTaskArea;
import com.cmiot.rms.services.GatewayBackupFileTaskService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/6/7.
 */
public class GatewayBackupFileTaskServiceImpl implements GatewayBackupFileTaskService {

    private final Logger logger = LoggerFactory.getLogger(GatewayBackupFileTaskServiceImpl.class);

    @Autowired
    GatewayBackupFileTaskMapper gatewayBackupFileTaskMapper;

    @Autowired
    GatewayBackupFileTaskAreaMapper gatewayBackupFileTaskAreaMapper;

    @Autowired
    private AreaService amsAreaService;

    /**
     * 根据条件查询该网关的备份文件任务列表
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryBackupFileTaskList(Map<String, Object> parameter) {
        logger.info("Start invoke queryBackupFileTaskList:{}", parameter);
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
            List<GatewayBackupFileTask> tasks = gatewayBackupFileTaskMapper.selectList(queryMap);

            long total = ((Page) tasks).getTotal();

            for(GatewayBackupFileTask task : tasks)
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
            logger.error("queryBackupFileTaskList exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }
        logger.info("End invoke queryBackupFileTaskList:{}", retMap);
        return retMap;
    }

    /**
     * 新增任务
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> addBackupFileTask(Map<String, Object> parameter) {
        logger.info("Start invoke addBackupFileTask:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        GatewayBackupFileTask fileTask = new GatewayBackupFileTask();
        try {
            org.apache.commons.beanutils.BeanUtils.populate(fileTask, parameter);
        } catch (Exception e) {
            logger.error("addBackupFileTask covert failed!");
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
        if(isExistSameNameTask(fileTask.getTaskName(), null))
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.BACKUPTASK_EXIST_SAME_NAME.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.BACKUPTASK_EXIST_SAME_NAME.getResultMsg());
            return retMap;
        }

        fileTask.setId(UniqueUtil.uuid());
        if(!setGatewayBackupFileTask(fileTask, null))
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.BACKUPTASK_EXIST.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.BACKUPTASK_EXIST.getResultMsg());
            return retMap;
        }

        if(null != fileTask.getAreaId())
        {
            insetArea(fileTask.getAreaId(), fileTask.getId());
        }

        fileTask.setStatus(1);
        gatewayBackupFileTaskMapper.insert(fileTask);

        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        logger.info("End invoke addBackupFileTask:{}", retMap);
        return retMap;
    }

    /**
     * 编辑备份文件任务，提供进入修改页面数据
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> editBackupFileTask(Map<String, Object> parameter) {
        logger.info("Start invoke editBackupFileTask:{}", parameter);
        String id = null != parameter.get("id") ? parameter.get("id").toString(): "";

        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isBlank(id)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke editBackupFileTask:{}", retMap);
            return retMap;
        }

        GatewayBackupFileTask task = gatewayBackupFileTaskMapper.selectByPrimaryKey(id);
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

        logger.info("End invoke editBackupFileTask:{}", retMap);
        return retMap;
    }

    /**
     * 更新备份文件任务
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> updateBackupFileTask(Map<String, Object> parameter) {
        logger.info("Start invoke updateBackupFileTask:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        GatewayBackupFileTask newTask = new GatewayBackupFileTask();
        try {
            org.apache.commons.beanutils.BeanUtils.populate(newTask, parameter);
        } catch (Exception e) {
            logger.error("updateBackupFileTask covert failed!");
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_ERROR.getResultMsg());
            return retMap;
        }

        //获取原始的数据
        GatewayBackupFileTask originalTask = gatewayBackupFileTaskMapper.selectByPrimaryKey(newTask.getId());
        if(originalTask.getStatus() == 2)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.TASK_IS_RUNNING.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.TASK_IS_RUNNING.getResultMsg());
            return retMap;
        }

        if(StringUtils.isBlank(newTask.getTaskName()))
        {
            newTask.setTaskName(originalTask.getTaskName());
        }

        if(isParameterISNull(newTask))
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            return retMap;
        }

        if(isExistSameNameTask(newTask.getTaskName(), originalTask.getId()))
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.BACKUPTASK_EXIST_SAME_NAME.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.BACKUPTASK_EXIST_SAME_NAME.getResultMsg());
            return retMap;
        }

        if(!setGatewayBackupFileTask(newTask, originalTask.getId()))
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.BACKUPTASK_EXIST.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.BACKUPTASK_EXIST.getResultMsg());
            return retMap;
        }

        if(0 != newTask.getAreaId().compareTo(originalTask.getAreaId()))
        {
            gatewayBackupFileTaskAreaMapper.deleteByTaskId(originalTask.getId());
            if(null != newTask.getAreaId())
            {
                insetArea(newTask.getAreaId(), newTask.getId());
            }
        }

        newTask.setStatus(1);
        newTask.setTaskCreateTime(originalTask.getTaskCreateTime());
        gatewayBackupFileTaskMapper.updateByPrimaryKey(newTask);

        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        logger.info("End invoke updateBackupFileTask:{}", retMap);
        return retMap;
    }

    /**
     * 删除备份文件任务
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> deleteBackupFileTask(Map<String, Object> parameter) {
        logger.info("Start invoke deleteBackupFileTask:{}", parameter);
        String id = null != parameter.get("id") ? parameter.get("id").toString(): "";

        Map<String, Object> retMap = new HashMap<>();
        if (StringUtils.isBlank(id)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke deleteBackupFileTask:{}", retMap);
            return retMap;
        }

        gatewayBackupFileTaskMapper.deleteByPrimaryKey(id);
        gatewayBackupFileTaskAreaMapper.deleteByTaskId(id);
        retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
        retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());

        logger.info("End invoke deleteBackupFileTask:{}", retMap);
        return retMap;
    }


    /**
     * 插入区域
     * @param areaId
     * @param taskId
     */
    private void insetArea(Integer areaId, String taskId)
    {
        List<GatewayBackupFileTaskArea> taskAreas = new ArrayList<>();
        List<Area> areas = amsAreaService.findChildArea(areaId);
        GatewayBackupFileTaskArea taskArea;
        for(Area area: areas)
        {
            taskArea = new GatewayBackupFileTaskArea();
            taskArea.setId(UniqueUtil.uuid());
            taskArea.setTaskId(taskId);
            taskArea.setAreaId(area.getId());
            taskAreas.add(taskArea);
        }

        if(taskAreas.size() > 0)
        {
            gatewayBackupFileTaskAreaMapper.batchInsert(taskAreas);
        }
    }

    /**
     * 设置值，并判断是否存在相同的任务
     * @param fileTask
     * @return
     */
    private Boolean setGatewayBackupFileTask(GatewayBackupFileTask fileTask, String originalTaskId)
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

            GatewayBackupFileTask sTask = new GatewayBackupFileTask();
            sTask.setTaskStartTime(fileTask.getTaskStartTime());
            sTask.setTaskEndTime(fileTask.getTaskEndTime());
            sTask.setAreaId(fileTask.getAreaId());
            if(StringUtils.isNotBlank(originalTaskId))
            {
                sTask.setId(originalTaskId);
            }
            int count = gatewayBackupFileTaskMapper.selectSameTaskForTime(sTask);
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

            GatewayBackupFileTask sTask = new GatewayBackupFileTask();
            sTask.setTaskTriggerEvent(fileTask.getTaskTriggerEvent());
            sTask.setAreaId(fileTask.getAreaId());
            if(StringUtils.isNotBlank(originalTaskId))
            {
                sTask.setId(originalTaskId);
            }
            int count = gatewayBackupFileTaskMapper.selectSameTaskForEvent(sTask);
            if(count > 0)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否存在相同名称的任务
     * @param taskName
     * @param originalTaskId
     * @return
     */
    private Boolean isExistSameNameTask(String taskName, String originalTaskId)
    {
        GatewayBackupFileTask task = new GatewayBackupFileTask();
        task.setTaskName(taskName);
        if(StringUtils.isNotBlank(originalTaskId))
        {
            task.setId(originalTaskId);
        }

        int count = gatewayBackupFileTaskMapper.selectSameTaskByName(task);
        if(count > 0 )
        {
            return true;
        }
        return false;
    }

    /**
     * 判断必要的参数是否为空
     * @param fileTask
     * @return
     */
    private Boolean isParameterISNull(GatewayBackupFileTask fileTask)
    {
        if(StringUtils.isBlank(fileTask.getTaskName()) || (null == fileTask.getAreaId() || 0 == fileTask.getAreaId())
                || (null == fileTask.getTaskTriggerMode() || 0 == fileTask.getTaskTriggerMode()))
        {
            return true;
        }

        return false;
    }
}
