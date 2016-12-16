/**
 * 
 */
package com.cmiot.rms.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cmiot.rms.dao.mapper.GatewayBusinessMapper;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.model.GatewayBusiness;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.WorkOrderInterface;

/**
 * @author lcs
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
	"file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
public class WorkOrderInterfaceTest {

	@Resource
    private WorkOrderInterface workOrderInterface;
	
	@Autowired
    private GatewayInfoMapper gatewayInfoMapper;
	
	@Autowired
    private GatewayBusinessMapper gatewayBusinessMapper;
	
	@Test
	public void broadBandUnsubcribeTest()throws Exception{
		
		Map parameter = new HashMap<String,String>();
		parameter.put("OrderNo", "18646115");
		parameter.put("ServiceCode", "otttv");
		parameter.put("PppoeAccount", "test35");
		
		Map map = new HashMap<>();
		map.put("Parameter", parameter);
		Map result = workOrderInterface.broadBandUnsubcribe(map);
		System.out.println(result.get("Result"));
		
		/*GatewayBusiness installBusiness = new GatewayBusiness();
        installBusiness.setGatewayPassword("9735abcd");
        installBusiness.setBusinessType("1");
        installBusiness.setBusinessStatu("4");
        installBusiness.setBusinessCodeBoss("otttv");
        List<GatewayBusiness> installBusinessList = gatewayBusinessMapper.selectExist(installBusiness);
        System.out.println(installBusinessList.get(0).getAreacode());*/
		
	}
	
}
