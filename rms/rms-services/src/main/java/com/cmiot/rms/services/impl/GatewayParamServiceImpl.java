package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.rms.dao.model.GatewayParam;
import com.cmiot.rms.dao.mapper.GatewayParamMapper;
import com.cmiot.rms.services.GatewayParamService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by liwei on 2016/2/25.
 */
@Service("gatewayParamService")
public class GatewayParamServiceImpl implements GatewayParamService {

	@Resource
	private GatewayParamMapper gatewayParamMapper;

	@Override
	public GatewayParam selectByPrimaryKey(Integer parmId) {
		// TODO Auto-generated method stub
		GatewayParam gatewayParam =gatewayParamMapper.selectByPrimaryKey(parmId);
		return gatewayParam;
	}

	@Override
	public List<GatewayParam> selectObject(GatewayParam gatewayParam) {
		// TODO Auto-generated method stub
		List<GatewayParam> list =gatewayParamMapper.selectObject(gatewayParam);
		return list;
	}

	@Override
	public int deleteByPrimaryKey(Integer parmId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int insertObject(GatewayParam param) {
		// TODO Auto-generated method stub
		return gatewayParamMapper.insertObject(param);
	}

	
}
