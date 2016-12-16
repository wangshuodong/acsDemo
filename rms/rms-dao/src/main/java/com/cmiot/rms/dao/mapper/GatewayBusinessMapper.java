package com.cmiot.rms.dao.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cmiot.rms.dao.model.GatewayBusiness;

public interface GatewayBusinessMapper {

    int deleteByPrimaryKey(String id);


//    int insert(GatewayBusiness record);


    int insertSelective(GatewayBusiness record);


    GatewayBusiness selectByPrimaryKey(String id);


    int updateByPrimaryKeySelective(GatewayBusiness record);


    int updateByPrimaryKey(GatewayBusiness record);

    List<GatewayBusiness> selectByParam(GatewayBusiness gatewayBusiness);

    int updateByPrimayKey(GatewayBusiness gatewayBusiness);

    List<GatewayBusiness> selectExist(GatewayBusiness gatewayBusiness);

    List<GatewayBusiness> selectAll(GatewayBusiness gatewayBusinessParam);

    List<GatewayBusiness> selectMaxFail(GatewayBusiness gatewayBusinessParam);

    List<GatewayBusiness> selectByOrderNos(List<String> list);

    int batchUpdateStatus(Map<String, Object> para);
    
    int batchInsert(List<GatewayBusiness> list);

    List<GatewayBusiness> selectBusinessList(HashMap<String,Object> map);

    int updateStatusByPassword(GatewayBusiness gatewayBusiness);

    List<GatewayBusiness> selectByGatewayInfo(GatewayBusiness gatewayBusiness);
    
    int updateStatusByPwdAndBusiness(GatewayBusiness gatewayBusiness);

	List<GatewayBusiness> selectByLoids(List<String> loidList);

    int updateStatusByOrderno(GatewayBusiness gatewayBusiness);

}
