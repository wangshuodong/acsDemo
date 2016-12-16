package com.cmiot.rms.services;

import com.alibaba.fastjson.JSONArray;
import com.cmiot.rms.dao.model.GatewayParam;

import java.util.List;

/**
 * Created by liwei on 2016/2/25.
 */
public interface GatewayParamService {

	public GatewayParam selectByPrimaryKey(Integer parmId);

	public List<GatewayParam> selectObject(GatewayParam gatewayParam);
	
    public int deleteByPrimaryKey(Integer parmId);
    
    public int insertObject(GatewayParam param) ;

}
