package com.cmiot.rms.services.thread.flowratetask;

import com.cmiot.ams.domain.Area;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.GatewayFlowrateTaskDetailMapper;
import com.cmiot.rms.dao.model.FlowRateTask;
import com.cmiot.rms.dao.model.GatewayFlowrateTaskDetail;
import com.cmiot.rms.dao.model.GatewayInfo;

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by panmingguo on 2016/7/7.
 */
public class FlowrateTaskThread implements Runnable {

    private FlowrateTaskParameter flowrateTaskParameter;

    public FlowrateTaskThread(FlowrateTaskParameter flowrateTaskParameter)
    {
        this.flowrateTaskParameter = flowrateTaskParameter;
    }

    @Override
    public void run() {
        String taskId = flowrateTaskParameter.getFlowrateMap().get("id").toString();
        int areaId = Integer.parseInt(flowrateTaskParameter.getFlowrateMap().get("areaId").toString());
        List<Area> areas = flowrateTaskParameter.getAmsAreaService().findChildArea(areaId);
        if(null != areas && areas.size() > 0)
        {
            List<Integer> areaIds = new ArrayList<>();
            for(Area area :areas)
            {
                areaIds.add(area.getId());
            }
            if(areaIds.size() > 0){
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("areaIds", areaIds);
                List<Map<String, Object>> gatewayList = flowrateTaskParameter.getGatewayInfoService().queryGatewayListByAreas(param);
                if(gatewayList != null && gatewayList.size() > 0){
                    for(Map<String, Object> gateway : gatewayList){

                        String gatewayUuid = gateway.get("gatewayUuid") == null ? "" : gateway.get("gatewayUuid").toString();
                        String mac = gateway.get("gatewayMac") == null ? "" : gateway.get("gatewayMac").toString();
                        String url = gateway.get("url") == null ? "" : gateway.get("url").toString();
                        if(StringUtils.isNotBlank(mac) && StringUtils.isNotBlank(url)){
                            getFlowrateAndUpdateGateway(gatewayUuid, mac, taskId,gateway);
                        }
                    }

                }
            }
        }
        //更新task状态
        FlowRateTask ft = new FlowRateTask();
        ft.setId(taskId);
        ft.setStatus(3);
        flowrateTaskParameter.getFlowRateTaskMapper().updateByPrimaryKeySelective(ft);
    }

    private void getFlowrateAndUpdateGateway(String gatewayUuid, String mac, String taskId, Map<String, Object> gateway) {

    	
    	
        String gpon = "InternetGatewayDevice.WANDevice.[0-9]+.X_CMCC_GponInterfaceConfig.Stats.BytesReceived";
        String epon = "InternetGatewayDevice.WANDevice.[0-9]+.X_CMCC_EPONInterfaceConfig.Stats.BytesReceived";
        String lan = "InternetGatewayDevice.WANDevice.[0-9]+.WANEthernetInterfaceConfig.Stats.BytesReceived";

        List<String> nameList = new ArrayList<String>();
        Map<String, Object> nameMap = flowrateTaskParameter.getInstructionMethodService().getParameterNames(mac, "InternetGatewayDevice.WANDevice.", false);
        Set<String> keys = nameMap.keySet();
        for(String key : keys){
            if(key.matches(gpon)|| key.matches(epon)|| key.matches(lan)){

                nameList.add(key);
            }
        }
        Map<String, Object> valueMap = flowrateTaskParameter.getInstructionMethodService().getParameterValues(mac, nameList);
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
        GatewayFlowrateTaskDetail detail = new GatewayFlowrateTaskDetail();
        detail.setId(UniqueUtil.uuid());
        detail.setTaskId(taskId);
        detail.setGatewayId(gatewayUuid);
        detail.setFlowrate(flowrate);
        detail.setCreateDate((int) (System.currentTimeMillis()/1000));
        detail.setFirmwareId(gateway.get("firmwareUuid")==null?"":gateway.get("firmwareUuid").toString());
        detail.setGatewayMac(gateway.get("gatewayMac").toString());
        flowrateTaskParameter.getGatewayFlowrateTaskDetailMapper().insert(detail);
        
        if(!"".equals(flowrate) ){
            //流量不为空的时候更新到数据库
            GatewayInfo info = new GatewayInfo();
            info.setGatewayUuid(gatewayUuid);
            info.setFlowRate(flowrate);
            flowrateTaskParameter.getGatewayInfoService().updateSelectGatewayInfo(info);
        }
    }
}
