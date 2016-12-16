package com.cmiot.rms.services.thread.flowratetask;

import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.dao.mapper.FlowRateTaskMapper;
import com.cmiot.rms.dao.mapper.GatewayFlowrateTaskDetailMapper;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.instruction.InstructionMethodService;

import java.util.Map;

/**
 * Created by panmingguo on 2016/7/7.
 */
public class FlowrateTaskParameter {
    private AreaService amsAreaService;
    private GatewayInfoService gatewayInfoService;
    private FlowRateTaskMapper flowRateTaskMapper;
    private InstructionMethodService instructionMethodService;
    private Map<String,Object> flowrateMap;
    private GatewayFlowrateTaskDetailMapper gatewayFlowrateTaskDetailMapper;

    public FlowRateTaskMapper getFlowRateTaskMapper() {
        return flowRateTaskMapper;
    }

    public void setFlowRateTaskMapper(FlowRateTaskMapper flowRateTaskMapper) {
        this.flowRateTaskMapper = flowRateTaskMapper;
    }

    public GatewayInfoService getGatewayInfoService() {
        return gatewayInfoService;
    }

    public void setGatewayInfoService(GatewayInfoService gatewayInfoService) {
        this.gatewayInfoService = gatewayInfoService;
    }

    public AreaService getAmsAreaService() {
        return amsAreaService;
    }

    public void setAmsAreaService(AreaService amsAreaService) {
        this.amsAreaService = amsAreaService;
    }

    public InstructionMethodService getInstructionMethodService() {
        return instructionMethodService;
    }

    public void setInstructionMethodService(InstructionMethodService instructionMethodService) {
        this.instructionMethodService = instructionMethodService;
    }

    public Map<String, Object> getFlowrateMap() {
        return flowrateMap;
    }

    public void setFlowrateMap(Map<String, Object> flowrateMap) {
        this.flowrateMap = flowrateMap;
    }

	public GatewayFlowrateTaskDetailMapper getGatewayFlowrateTaskDetailMapper() {
		return gatewayFlowrateTaskDetailMapper;
	}

	public void setGatewayFlowrateTaskDetailMapper(
			GatewayFlowrateTaskDetailMapper gatewayFlowrateTaskDetailMapper) {
		this.gatewayFlowrateTaskDetailMapper = gatewayFlowrateTaskDetailMapper;
	}


}
