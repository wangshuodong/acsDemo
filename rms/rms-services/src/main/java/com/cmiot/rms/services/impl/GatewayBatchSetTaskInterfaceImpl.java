package com.cmiot.rms.services.impl;

import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BatchSetTaskDetailMapper;
import com.cmiot.rms.dao.mapper.BatchSetTaskMapper;
import com.cmiot.rms.dao.model.BatchSetTask;
import com.cmiot.rms.dao.model.BatchSetTaskDetail;
import com.cmiot.rms.services.GatewayBatchSetTaskInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by admin on 2016/5/18.
 */
public class GatewayBatchSetTaskInterfaceImpl implements GatewayBatchSetTaskInterface {

    private Logger logger = LoggerFactory.getLogger(GatewayBatchSetTaskInterfaceImpl.class);

    @Autowired
    private BatchSetTaskMapper batchSetTaskMapper;

    @Autowired
    private BatchSetTaskDetailMapper batchSetTaskDetailMapper;

    @Autowired
    private AreaService areaService;

    @Override
    public Map<String, Object> queryBatchSetTaskPage(Map<String, Object> parameter) {
        logger.info("Start invoke queryBatchSetTaskPage:{}",parameter);
        //返回参数
        Map<String,Object> retMap = new HashMap<>();
        try {
            String uid = (String) parameter.get("uid");
            if(StringUtils.isEmpty(uid)){
                logger.error("查询批量设置任务,uid不能为空，uid:{}",parameter);
                retMap.put("resultCode",10000);
                retMap.put("resultMsg","参数不能为空");
            }
            int page = parameter.get("page") == null ? 1 : Integer.valueOf(parameter.get("page") + "");
            int pageSize = parameter.get("pageSize") == null ? 1 : Integer.valueOf(parameter.get("pageSize") + "");
            String areaCode = parameter.get("areaCode") + "";
            String parmName = parameter.get("parmName") + "";
            String trrigerEvent = (String) parameter.get("trrigerEvent");
            Map<String, Object> parmMap = new HashMap<>();
            parmMap.put("start", (page - 1) * pageSize);
            parmMap.put("end", page * pageSize);
            //当用户选择了区域时，就用areaCode作为查询条件，当用户未选择时，用defaultAreaCodes
            if (!"".equals(areaCode)) {
//              parmMap.put("areaCode", areaCode);
                List<String> areaIds = new ArrayList<>();
//                logger.info("Start invoke areaService.findChildArea:{}",areaCode);
                List<Area> listArea = areaService.findChildArea(Integer.valueOf(areaCode));
//                logger.info("End invoke areaService.findChildArea:{}",listArea);
                if(!(null == listArea)) {
                    for (Area area : listArea) {
                        if (!(null == area)) {
                            areaIds.add(area.getId() + "");
                        }
                    }
                    if(areaIds.size()>0) {
                        parmMap.put("areaCode", areaIds);
                    }
                }
            }else{
                List<String> areaIds = new ArrayList<>();
//                logger.info("Start invoke areaService.findAreaByAdmin:{}",uid);
                List<Area> listArea = areaService.findAreaByAdmin(uid);
//                logger.info("End invoke areaService.findAreaByAdmin1:{}",listArea);
                if(!(null == listArea)) {
                    for (Area area : listArea) {
                        if (!(null == area)) {
                            areaIds.add(area.getId() + "");
                        }
                    }
                    if(areaIds.size()>0) {
                        parmMap.put("areaCode", areaIds);
                    }
                }
            }
            if (!"".equals(parmName)) {
                parmMap.put("parmName", parmName);
            }
            if(!StringUtils.isEmpty(trrigerEvent)){
                switch (trrigerEvent) {
                    case "1":
                        parmMap.put("trrigerEvent1", 1);
                        break;
                    case "2":
                        parmMap.put("trrigerEvent2", 1);
                        break;
                    case "3":
                        parmMap.put("trrigerEvent3", 1);
                        break;
                    case "4":
                    	parmMap.put("trrigerEvent4", 1);
                    	break;
                    default:
                }
            }
            List<BatchSetTask> listBatch = batchSetTaskMapper.selectByParm(parmMap);
            int count = batchSetTaskMapper.selectCountByParm(parmMap);
            retMap.put("resultCode",0);
            retMap.put("resultMsg","查询成功");
            retMap.put("page",page);
            retMap.put("pageSize",pageSize);
            retMap.put("total",count);
            List<Map<String,Object>> listBatchForRet = new ArrayList<>();
            for(BatchSetTask batchSetTasks : listBatch){
                Map<String,Object> ret = new HashMap<>();
                ret.put("id",batchSetTasks.getId());
                ret.put("areaCode",batchSetTasks.getAreaCode());
                ret.put("areaName",batchSetTasks.getAreaName());
                ret.put("isDelete", batchSetTasks.getIsDelete());
                ret.put("createTime",batchSetTasks.getCreateTime());
                ret.put("taskState",batchSetTasks.getTaskState());
                String trrigerEvents = "";
                if("1".equals(batchSetTasks.getTrrigerEvent1())){
                    trrigerEvents = "1";
                }
                if("1".equals(batchSetTasks.getTrrigerEvent2())){
                    if(trrigerEvents == ""){
                        trrigerEvents = "2";
                    }else{
                        trrigerEvents += ",2";
                    }
                }
                if("1".equals(batchSetTasks.getTrrigerEvent3())){
                    if(trrigerEvents == ""){
                        trrigerEvents = "3";
                    }else{
                        trrigerEvents += ",3";
                    }
                }
                if("1".equals(batchSetTasks.getTrrigerEvent4())){
                	if(trrigerEvents == ""){
                		trrigerEvents = "4";
                	}else{
                		trrigerEvents += ",4";
                	}
                }
                ret.put("trrigerEvent",trrigerEvents);
                Map<String,Object> detailParamMap = new HashMap<>();
                detailParamMap.put("batchSetTaskId",batchSetTasks.getId());
                List<BatchSetTaskDetail> taskDetail = batchSetTaskDetailMapper.selectByParm(detailParamMap);
                List<Map<String,Object>> detailList = new ArrayList<>();
                for(BatchSetTaskDetail batchSetTaskDetail : taskDetail){
                    Map<String,Object> detailMap = new HashMap<>();
                    detailMap.put("id",batchSetTaskDetail.getId());
                    detailMap.put("parmName",batchSetTaskDetail.getParmName());

                    detailMap.put("parmType",batchSetTaskDetail.getParmType());
                    detailMap.put("parmLength",batchSetTaskDetail.getParmLength());
                    detailMap.put("parmWriteable",batchSetTaskDetail.getParmWriteable());
                    detailMap.put("parmValue",batchSetTaskDetail.getParmValue());
                    detailList.add(detailMap);
                }
                ret.put("taskDetail",detailList);
                listBatchForRet.add(ret);
            }
            retMap.put("data", listBatchForRet);
        }catch (Exception e){
            logger.error("查询批量设置任务失败！",e);
            retMap.put("resultCode",10005);
            retMap.put("resultMsg","查询错误");
        }
        logger.info("End invoke queryBatchSetTaskPage:{}",retMap);
        return retMap;
    }

    @Override
    public Map<String, Object> deleteBatchSetTask(Map<String, Object> parameter) {
        logger.info("Start invoke deleteBatchSetTask:{}",parameter);
        //返回参数
        Map<String,Object> retMap = new HashMap<>();
        try{
            String id = (String) parameter.get("id");
            if(StringUtils.isEmpty(id)){
                retMap.put("resultCode",10000);
                retMap.put("resultMsg","参数id不能为空");
                return retMap;
            }
            int delete = batchSetTaskMapper.deleteByPrimaryKey(id);
            if(delete>0){
                retMap.put("resultCode",0);
                retMap.put("resultMsg","删除成功");
            }else{
                retMap.put("resultCode",10004);
                retMap.put("resultMsg","删除失败");
            }
        }catch (Exception e){
            logger.error("查询批量设置任务失败！",e);
            retMap.put("resultCode",10004);
            retMap.put("resultMsg","删除错误");
        }
        logger.info("End invoke deleteBatchSetTask:{}",retMap);
        return retMap;
    }

    @Override
    public Map<String, Object> addBatchSetTask(Map<String, Object> parameter) {
        logger.info("Start invoke addBatchSetTask:{}",parameter);
        //返回参数
        Map<String,Object> retMap = new HashMap<>();
        String areaCode = (String) parameter.get("areaCode");
        String trrigerEvent = (String) parameter.get("trrigerEvent");
        List<HashMap<String,Object>> detailList = (List<HashMap<String,Object>>) parameter.get("taskDetail");
        if(StringUtils.isEmpty(areaCode)) {
            retMap.put("resultCode", 10000);
            retMap.put("resultMsg", "请选择地区");
            return retMap;
        }
        if(StringUtils.isEmpty(trrigerEvent)) {
            retMap.put("resultCode", 10000);
            retMap.put("resultMsg", "请选择触发时间");
            return retMap;
        }
        if(detailList ==null||detailList.size()<=0){
            retMap.put("resultCode", 10000);
            retMap.put("resultMsg", "请添加配置参数");
            return retMap;
        }
        //先写任务数据，再写明细表数据
        //先根据areaCode获取areaName;
        Area area = areaService.findAreaById(Integer.valueOf(areaCode));
        if(area == null || area.getName() == null || area.getName() == "")
        {
            retMap.put("resultCode", 10000);
            retMap.put("resultMsg", "地区错误");
            return retMap;
        }
        String areaName = area.getName();
        BatchSetTask batchSetTaskAdd = new BatchSetTask();
        batchSetTaskAdd.setAreaCode(areaCode);
        batchSetTaskAdd.setAreaName(areaName);
        String createTime = "";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(DateTools.YYYY_MM_DD_HH_MM_SS);
        createTime = df.format(cal.getTime());
        batchSetTaskAdd.setCreateTime(createTime);
        batchSetTaskAdd.setTaskState("0");
        if(!StringUtils.isEmpty(trrigerEvent)){
            if(trrigerEvent.indexOf("1")>=0)
            {
                batchSetTaskAdd.setTrrigerEvent1("1");
            }
            if(trrigerEvent.indexOf("2")>=0)
            {
                batchSetTaskAdd.setTrrigerEvent2("1");
            }
            if(trrigerEvent.indexOf("3")>=0)
            {
                batchSetTaskAdd.setTrrigerEvent3("1");
            }
            if(trrigerEvent.indexOf("4")>=0)
            {
            	batchSetTaskAdd.setTrrigerEvent4("1");
            }
        }
        String batchSetId = UniqueUtil.uuid();
        batchSetTaskAdd.setId(batchSetId);
        batchSetTaskAdd.setIsDelete(1);
        int addResult = batchSetTaskMapper.insert(batchSetTaskAdd);
        if(addResult>0){
            //再添加明细
            for(Map<String,Object> batchSetMap : detailList){
                BatchSetTaskDetail batchSetTaskDetail = new BatchSetTaskDetail();
                batchSetTaskDetail.setParmLength(Integer.valueOf(batchSetMap.get("parmLength") + ""));
                batchSetTaskDetail.setParmName((String) batchSetMap.get("parmName"));
                batchSetTaskDetail.setParmValue((String) batchSetMap.get("parmValue"));
                batchSetTaskDetail.setParmType((String) batchSetMap.get("parmType"));
                batchSetTaskDetail.setParmWriteable((String) batchSetMap.get("parmWriteable"));
                batchSetTaskDetail.setId(UniqueUtil.uuid());
                batchSetTaskDetail.setBatchSetTaskId(batchSetId);
                batchSetTaskDetailMapper.insert(batchSetTaskDetail);
            }
            retMap.put("resultCode",0);
            retMap.put("resultMsg","新增成功");
        }else{
            retMap.put("resultCode",10004);
            retMap.put("resultMsg","新增失败");
        }
        logger.info("End invoke addBatchSetTask:{}",retMap);
        return retMap;
    }

    @Override
    public Map<String, Object> updateBatchSetTask(Map<String, Object> parameter) {
        logger.info("Start invoke updateBatchSetTask:{}",parameter);
        //返回参数
        Map<String,Object> retMap = new HashMap<>();
        String id = (String) parameter.get("id");
        String trrigerEvent = (String) parameter.get("trrigerEvent");
        List<Map<String,Object>> detailList = (List<Map<String,Object>>) parameter.get("taskDetail");
        if(StringUtils.isEmpty(id)) {
            retMap.put("resultCode", 10000);
            retMap.put("resultMsg", "参数id不能为空");
            return retMap;
        }
        if(StringUtils.isEmpty(trrigerEvent) &&(detailList ==null||detailList.size()<=0)) {
            retMap.put("resultCode", 10000);
            retMap.put("resultMsg", "参数trrigerEvent和detailList不能全为空");
            return retMap;
        }
        //先查询ID存在不
        BatchSetTask querBatchSetTask = batchSetTaskMapper.selectByPrimaryKey(id);
        if (querBatchSetTask == null){
            retMap.put("resultCode", 10001);
            retMap.put("resultMsg", "id不存在");
            return retMap;
        }
        if(querBatchSetTask.getTaskState().equals("1")){
            retMap.put("resultCode", 10003);
            retMap.put("resultMsg", "任务已经开始，不能更新");
            return retMap;
        }
        if(!StringUtils.isEmpty(trrigerEvent)){
            //更新
            BatchSetTask batchSetTaskUpdate = new BatchSetTask();
            batchSetTaskUpdate.setId(id);
            if(trrigerEvent.indexOf("1")>=0)
            {
                batchSetTaskUpdate.setTrrigerEvent1("1");
            }else{
                batchSetTaskUpdate.setTrrigerEvent1("0");
            }
            if(trrigerEvent.indexOf("2")>=0)
            {
                batchSetTaskUpdate.setTrrigerEvent2("1");
            }else{
                batchSetTaskUpdate.setTrrigerEvent2("0");
            }
            if(trrigerEvent.indexOf("3")>=0)
            {
                batchSetTaskUpdate.setTrrigerEvent3("1");
            }else{
                batchSetTaskUpdate.setTrrigerEvent3("0");
            }
            if(trrigerEvent.indexOf("4")>=0)
            {
            	batchSetTaskUpdate.setTrrigerEvent4("1");
            }else{
            	batchSetTaskUpdate.setTrrigerEvent4("0");
            }
            int updateResult = batchSetTaskMapper.updateByPrimaryKey(batchSetTaskUpdate);
            if(updateResult<=0){
                retMap.put("resultCode",10003);
                retMap.put("resultMsg","更新失败");
            }
        }
        if(!(detailList ==null||detailList.size()<=0)){
            //删除原来的然后新增
            batchSetTaskDetailMapper.deleteByBatchSetTaskId(id);
            //再添加明细
            for(Map<String,Object> batchSetMap : detailList){
                BatchSetTaskDetail batchSetTaskDetail = new BatchSetTaskDetail();
                batchSetTaskDetail.setParmLength(Integer.valueOf(batchSetMap.get("parmLength") + ""));
                batchSetTaskDetail.setParmName((String) batchSetMap.get("parmName"));
                batchSetTaskDetail.setParmValue((String) batchSetMap.get("parmValue"));
                batchSetTaskDetail.setParmType((String) batchSetMap.get("parmType"));
                batchSetTaskDetail.setParmWriteable((String) batchSetMap.get("parmWriteable"));
                batchSetTaskDetail.setId(UniqueUtil.uuid());
                batchSetTaskDetail.setBatchSetTaskId(id);
                batchSetTaskDetailMapper.insert(batchSetTaskDetail);
            }
        }
        retMap.put("resultCode",0);
        retMap.put("resultMsg","成功");
        logger.info("End invoke updateBatchSetTask:{}",retMap);
        return retMap;
    }
}
