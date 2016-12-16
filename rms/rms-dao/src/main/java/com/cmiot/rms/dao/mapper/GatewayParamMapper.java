package com.cmiot.rms.dao.mapper;

import com.cmiot.rms.dao.model.GatewayParam;

import java.util.List;

public interface GatewayParamMapper {

    public GatewayParam selectByPrimaryKey(Integer parmId);

    public List<GatewayParam> selectObject(GatewayParam gatewayParam);
    
    public int deleteByPrimaryKey(Integer parmId);
    
    public int insertObject(GatewayParam param);

}