package com.cmiot.rms.services.impl;

import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.enums.UpgradeTaskEventEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BatchSetTaskDetailMapper;
import com.cmiot.rms.dao.mapper.BatchSetTaskMapper;
import com.cmiot.rms.dao.mapper.GateWayBatchSetTaskInfoMapper;
import com.cmiot.rms.dao.model.BatchSetTask;
import com.cmiot.rms.dao.model.BatchSetTaskDetail;
import com.cmiot.rms.dao.model.GateWayBatchSetTaskInfo;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.BatchSetTaskTrrigerService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/5/19.
 */
@Service
public class BatchSetTaskTrrigerServiceImpl implements BatchSetTaskTrrigerService {

    private Logger logger = LoggerFactory.getLogger(BatchSetTaskTrrigerServiceImpl.class);

    @Autowired
    private GateWayBatchSetTaskInfoMapper gateWayBatchSetTaskInfoMapper;

    @Autowired
    private BatchSetTaskDetailMapper batchSetTaskDetailMapper;

    @Autowired
    private BatchSetTaskMapper batchSetTaskMapper;

    @Autowired
    private InstructionMethodService instructionMethodService;

    @Autowired
    private GatewayInfoService gatewayInfoService;

    @Autowired
    private AreaService areaService;

    @Override
    public void batchSetTaskTrriger(Boolean isHaveUpTask,String sn, String oui, UpgradeTaskEventEnum event) {
        logger.info("Start invoke batchSetTaskTrriger:{},{},{},{}",isHaveUpTask, sn, oui, event);
        try {
            /*if (isHaveUpTask) {
                logger.info("该网关正在进行备份，不能进行参数设置，SN:{}，oui:{},event:{}", sn, oui, event);
                return;
            }*/
            //查询网关是否存在批量设置任务
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("gateWaySerialnumber", sn);
            paramMap.put("oui", oui);
            String trrigerEvent = "";
            switch (event.code()) {
                case 1:
                    paramMap.put("trrigerEvent1", "1");
                    trrigerEvent = "1";
                    break;
                case 2:
                    paramMap.put("trrigerEvent2", "1");
                    trrigerEvent = "2";
                    break;
                case 3:
                    paramMap.put("trrigerEvent3", "1");
                    trrigerEvent = "3";
                    break;
                case 4:
                	paramMap.put("trrigerEvent4", "1");
                	trrigerEvent = "4";
                	break;
                default:
                    break;
            }
            //先查询出网关信息
            GatewayInfo gatewayInfo = new GatewayInfo();
            // SN号
            gatewayInfo.setGatewaySerialnumber(sn);
            gatewayInfo.setGatewayFactoryCode(oui);
            //根据SN和OUI查询是否已经存在CPE
            GatewayInfo resultGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
            if(resultGatewayInfo == null){
                logger.info("该网关不存在，SN:{}，oui:{},event:{}", sn, oui, event);
                return;
            }
            String areaCode = resultGatewayInfo.getGatewayAreaId();
            if(StringUtils.isEmpty(areaCode)){
                logger.info("该网关区域编码为空，SN:{}，oui:{},event:{}", sn, oui, event);
                return;
            }
            //获取其父节点
            List<Area> areas = areaService.findParents(Integer.valueOf(areaCode));
            List<String> areaIds = new ArrayList<>();
            if(areas!= null && areas.size()>0){
                for(Area area :areas){
                    areaIds.add(area.getId() + "");
                }
            }
            areaIds.add(areaCode);
            paramMap.put("areaIds",areaIds);
            //验证网关是否有有效的批量设置任务
            //查询任务集合，按照创建时间正序排序
            List<GateWayBatchSetTaskInfo> queryList = gateWayBatchSetTaskInfoMapper.selectTasByGateWay(paramMap);
            if (queryList == null || queryList.size() <= 0) {
                logger.info("该网关没有有效的批量设置任务，SN:{}，oui:{},event:{}", sn, oui, event);
                return;
            }
            for (GateWayBatchSetTaskInfo gwbsti : queryList) {
                //循环查询每个任务是否有执行过设置任务，如果没有则设置，并做记录，一个任务对一个终端只做一次触发。
                Map<String, Object> pMap = new HashMap<>();
                pMap.put("deviceInfoId",resultGatewayInfo.getGatewayUuid());
                pMap.put("batchSetTaskId", gwbsti.getBatchSetTaskId());
                pMap.put("isSuccess", "1");
                List<GateWayBatchSetTaskInfo> gateTaskList = gateWayBatchSetTaskInfoMapper.selectByParm(pMap);
                if (gateTaskList == null || gateTaskList.size() <= 0) {
                    //如果没设置过，则查询出参数集合，进行设置
                    Map<String, Object> detailPaMap = new HashMap<>();
                    detailPaMap.put("batchSetTaskId", gwbsti.getBatchSetTaskId());
                    List<BatchSetTaskDetail> detailList = batchSetTaskDetailMapper.selectByParm(detailPaMap);
                    if (detailList != null && detailList.size() > 0) {
                        List<ParameterValueStruct> setParameterNames = new ArrayList<>();
                        for (BatchSetTaskDetail bstd : detailList) {
                            setParameterNames.add(new ParameterValueStruct(bstd.getParmName(), bstd.getParmValue(), bstd.getParmType()));
                        }
                        BatchSetTask batchSetTask = new BatchSetTask();
                        batchSetTask.setId(gwbsti.getBatchSetTaskId());
                        batchSetTask.setTaskState("1");
                        batchSetTaskMapper.updateByPrimaryKey(batchSetTask);
                        Boolean isAddSuccess = instructionMethodService.setParameterValue(resultGatewayInfo.getGatewayMacaddress(), setParameterNames);
                        GateWayBatchSetTaskInfo gateTaskAdd = new GateWayBatchSetTaskInfo();
                        gateTaskAdd.setBatchSetTaskId(gwbsti.getBatchSetTaskId());
                        gateTaskAdd.setId(UniqueUtil.uuid());
                        gateTaskAdd.setGateWayInfoId(resultGatewayInfo.getGatewayUuid());
                        gateTaskAdd.setTrrigerEvent(trrigerEvent);
                        if (isAddSuccess) {
                            gateTaskAdd.setIsSuccess("1");
                        } else {
                            gateTaskAdd.setIsSuccess("2");
                        }
                        gateWayBatchSetTaskInfoMapper.insert(gateTaskAdd);
                    }
                }
            }
        }catch (Exception e){
            logger.error("batchSetTaskTrriger error" ,e);
        }
    }
}
