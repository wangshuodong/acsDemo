
package com.cmiot.rms.services.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cmiot.rms.services.BoxDeviceManageService;

@RunWith(SpringJUnit4ClassRunner.class)
	@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
	        "file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
	public class BoxDeviceInfoServiceImplTest {

	    @Autowired
	    private BoxDeviceManageService boxDeviceManageService;

		//测试查询批量配置网关流量查询任务
		@Test
		public void testQueryFlowrateTaskList() throws Exception {
			
			Map<String, Object> parameter = new HashMap<String, Object>();
			//params.put("gatewayInfoAreaId", "530000");
			//parameter.put("uid", "admin");
			boxDeviceManageService.queryDeviceInfoList(parameter);
			
		}
		//测试新增批量配置网关流量查询任务
		@Test
		public void testAddFlowrateTask() throws Exception {
			
			Map<String, Object> parameter = new HashMap<String, Object>();
			//params.put("gatewayInfoAreaId", "530000");
			parameter.put("factoryCode", "990105");
			parameter.put("boxModel", "1112332");
			parameter.put("deviceName", "1112332");
			parameter.put("remark", "1112332");
			boxDeviceManageService.addDeviceInfo(parameter);
			
		}
		//测试查询批量配置网关流量查询任务
		@Test
		public void testEditFlowrateTask() throws Exception {
			
			Map<String, Object> parameter = new HashMap<String, Object>();
			//params.put("gatewayInfoAreaId", "530000");
			parameter.put("id", "761981aa4c794237bca8482a9ed367ff");
			boxDeviceManageService.queryDeviceInfo(parameter);
			
		}
		//测试更新批量配置网关流量查询任务
		@Test
		public void testUpdateFlowrateTask() throws Exception {
			
			Map<String, Object> parameter = new HashMap<String, Object>();
			//params.put("gatewayInfoAreaId", "530000");
			parameter.put("id", "761981aa4c794237bca8482a9ed367ff");
			parameter.put("factoryId", "11122211");
			parameter.put("boxModel", "221112332");
			boxDeviceManageService.updateDeviceInfo(parameter);
			
		}
		
		@Test
		public void testDeleteFlowrateTask() throws Exception {
			
			Map<String, Object> parameter = new HashMap<String, Object>();
			//params.put("gatewayInfoAreaId", "530000");
			parameter.put("ids", "e091a7e576804fb08d3c5f6ee9d8d2f6");
			boxDeviceManageService.deleteDeviceInfo(parameter);
			
		}
		
		
		  
}

