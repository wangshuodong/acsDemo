/**
 * 
 */
package com.cmiot.rms.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.aspectj.weaver.ast.HasAnnotation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.dao.mapper.GatewayBusinessMapper;
import com.cmiot.rms.dao.model.GatewayBusiness;
import com.cmiot.rms.services.GatewayBusinessService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.WorkOrderManagerService;

/**
 * @author lcs
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
	"file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
public class WorkOrderManagerServiceTest {
	
	@Resource
    private GatewayBusinessMapper gatewayBusinessMapper;
	
	@Resource
    private GatewayInfoService gatewayInfoService;
	
	@Resource
    private WorkOrderManagerService workOrderManagerService;
	
	@Resource
    private GatewayBusinessService gatewayBusinessService;
	@Resource
	private com.cmiot.ams.service.AreaService amsAreaService;
	
	@Test
	public void daoTest() {
		
//		GatewayInfo gatewayInfo = new GatewayInfo();
//		gatewayInfo.setGatewaySerialnumber("etre");
//		GatewayInfo gatewayInfo2 = gatewayInfoMapper.selectGatewayInfo(gatewayInfo);
//		System.out.println(gatewayInfo2);
		
		GatewayBusiness gatewayBusiness = new GatewayBusiness();
		gatewayBusiness.setBusinessStatu("2");
		gatewayBusiness.setOrderNo("3454365435");
		List<GatewayBusiness> businessList =  gatewayBusinessMapper.selectByParam(gatewayBusiness);
		System.out.println(businessList.isEmpty());
	}
	
	
	@Test
	public void reExecuteWorkOrderTest(){
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("orderNo", "20160725");
		parameter.put("gatewaySerialnumber", "CIOT00000222");
		Map<String, Object> result = workOrderManagerService.reExecuteWorkOrder(parameter);
		System.out.println(result.get("resultCode"));
	}
	
	
	@Test
	public  void queryListTest(){
		Map<String, Object> map = new HashMap<>();
		map.put("order_no", "20160725");
		Map map2 = gatewayBusinessService.queryList4Page(map);
		System.out.println(map2);
	}
	@Test
	public  void importWorkOrder(){
		Map<String, Object> map = new HashMap<>();
		map.put("order_no", "20160725");
		Map map2 = workOrderManagerService.importWorkOrder(map);
		System.out.println(map2);
	}
	@Test
	public  void findAreaByAdmin(){
		List<com.cmiot.ams.domain.Area> userAreaList = this.amsAreaService.findAreaByAdmin("a9c6b71e85b248019c15c2257f2ca2fa");
		System.out.println(userAreaList);
	}
}
