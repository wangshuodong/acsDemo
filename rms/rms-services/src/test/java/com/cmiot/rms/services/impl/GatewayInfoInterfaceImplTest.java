package com.cmiot.rms.services.impl;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.model.Inform;
import com.cmiot.ams.domain.Area;
import com.cmiot.hoa.facade.hangzhou.HangzhouFacade;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.services.BusinessCategoryService;
import com.cmiot.rms.services.FamilyNetworkSettingService;
import com.cmiot.rms.services.FirmwareUpgradeTaskService;
import com.cmiot.rms.services.GatewayBaseInfoService;
import com.cmiot.rms.services.GatewayFlowrateTaskService;
import com.cmiot.rms.services.GatewayInfoInterface;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.GatewayManageService;
import com.cmiot.rms.services.GatewaySpeedTrafficInfoService;
import com.cmiot.rms.services.HomeNetworkConfigService;
import com.cmiot.rms.services.WirelessNetworkSettingService;
import com.cmiot.rms.services.WorkOrderInterface;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.outerservice.RequestMgrService;
import com.cmiot.rms.services.util.InstructionUtil;

@RunWith(SpringJUnit4ClassRunner.class)
	@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
	        "file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
	public class GatewayInfoInterfaceImplTest {

	    @Autowired
	    private GatewayInfoInterface gatewayInfoInterface;
	    @Autowired
	    private GatewayManageService gatewayManageService;
	    @Autowired
	    private FirmwareUpgradeTaskService firmwareUpgradeTaskService;
	    @Autowired
	    private WirelessNetworkSettingService wirelessNetworkSettingService;
	    @Autowired
	    private FamilyNetworkSettingService familyNetworkSettingService;
		@Autowired
		private GatewayInfoService gatewayInfoService;
		@Autowired
		private RequestMgrService requestMgrService;
		@Autowired
		private HomeNetworkConfigService homeNetworkConfigService;
		@Autowired
		private BusinessCategoryService businessCategoryService;
		@Autowired
		private GatewayFlowrateTaskService gatewayFlowrateTaskService;
		@Autowired
		private com.cmiot.ams.service.AreaService amsAreaService;
		@Autowired
		private GatewayBaseInfoService gatewayBaseInfoService;
		@Autowired
		private GatewaySpeedTrafficInfoService gatewaySpeedTrafficInfoService;
		@Autowired
		private WorkOrderInterface workOrderInterface;
		@Resource
		private InstructionMethodService instructionMethodService;
		@Autowired
		HangzhouFacade hangzhouFacade;
		
		//杭研 ， 新网----ok!!
	    @Test
	    public void testGetGatewayPhysicalInterfaceState() throws Exception {
	        Map params = new HashMap<String, Object>();
	        params.put("RPCMethod", "GET");
	        params.put("ID", 123);
	        params.put("CmdType", "GET_HG_PORTS_STATUS");
	        params.put("SequenceId", "EFAB9000");

	        Map p = new HashMap<String, Object>();
	        p.put("MAC", "14144B208734");
	        params.put("Parameter", p);

	        Map<String, Object> r = gatewayInfoInterface.getGatewayPhysicalInterfaceState(params);
	        assertEquals(0, r.get("Result"));
	    }
	    
	    //杭研OK   星网NO
	    @Test
	    public void testGetVoIPStatus() throws Exception {
	        Map params = new HashMap<String, Object>();
	        params.put("RPCMethod", "GET");
	        params.put("ID", 123);
	        params.put("CmdType", "GET_VOIP_STATUS");
	        params.put("SequenceId", "EFAB9000");

	        Map p = new HashMap<String, Object>();
	        p.put("MAC", "14144B208734");
	        params.put("Parameter", p);

	        Map<String, Object> r = gatewayInfoInterface.getVoIPStatus(params);
	        assertEquals(0, r.get("Result"));
	    }
	    //5.6.6.修改网关用户管理员密码
	    //杭研星网OK
	    @Test
	    public void testSetHgAdminPwd() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	params.put("RPCMethod", "Set");
	    	params.put("ID", 123);
	    	params.put("CmdType", "SET_HG_ADMIN_PWD");
	    	params.put("SequenceId", "EFAB9000");
	    	
	    	Map p = new HashMap<String, Object>();
	    	p.put("MAC", "1C25E1000030");
	    	p.put("NEWPASSWORD", "111111");
	    	p.put("PASSWORD", "111111");
	    	params.put("Parameter", p);
	    	
	    	Map<String, Object> r = gatewayManageService.setHgAdminPwd(params);
	    	assertEquals(0, r.get("Result"));
	    }

	    @Test
	    public void testQueryAlarmInfo() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	params.put("RPCMethod", "GET");
	    	params.put("ID", 123);
	    	params.put("CmdType", "GET_HG_SYSTEM_INFO");
	    	params.put("SequenceId", "EFAB9000");
	    	
	    	Map p = new HashMap<String, Object>();
	    	p.put("gatewayMacaddress", "14:14:4B:19:87:34");
	    	params.put("Parameter", p);
	    	
	    	Map<String, Object> r = gatewayManageService.queryAlarmInfo(params);
	    	assertEquals(0, r.get("Result"));
	    }

	    @Test
	    public void testQueryUpgradeTaskByMacs() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	params.put("RPCMethod", "GET");
	    	params.put("ID", 123);
	    	params.put("CmdType", "NOTIFY_QUERY_UPGRADE_STAT");
	    	params.put("SequenceId", "EFAB9000");
	    	
	    	Map p = new HashMap<String, Object>();
	    	List<String> gatewayList = new ArrayList<String>();
	    	gatewayList.add("14144B208734");
	    	p.put("GatewayList", gatewayList);
	    	params.put("Parameter", p);
	    	
	    	Map<String, Object> r = firmwareUpgradeTaskService.queryUpgradeTaskByMacs(params);
	    	assertEquals(0, r.get("Result"));
	    }
	  /*  @Test
	    public void testQueryAlarmInfo() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	
	    	//params.put("gatewayMacaddress", "14144B198734");
	    	params.put("gatewayMacaddress", "00C00286878A");
	    	
	    	Map<String, Object> r = gatewayManageService.queryAlarmInfo(params);
	    	assertEquals(0, r.get("Result"));
	    }*/
	    //开关SSID           ok!*******************************************************************************ok!
	    //杭研  星网OK
	    @Test
	    public void testSetWifiSsidOnoff() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	params.put("RPCMethod", "Set");
	    	params.put("ID", 123);
	    	params.put("CmdType", "SET_WIFI_SSID_ONOFF");
	    	params.put("SequenceId", "EFAB9000");
	    	
	    	Map p = new HashMap<String, Object>();
	    	p.put("MAC", "1C25E1012345");
	    	p.put("SSIDIndex", "0");
	    	p.put("Enable", "0");
	    	params.put("Parameter", p);
	    	
	    	Map<String, Object> r = wirelessNetworkSettingService.setWifiSsidOnoff(params);
	    	assertEquals(0, r.get("Result"));
	    }
	    //设置Wi-Fi定时开关  没有对应节点！！！！
	    //星网 节点不存在，，并且添加节点失败
	    @Test
	    public void testSetWifiOnoffTimer() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	params.put("RPCMethod", "Set");
	    	params.put("ID", 123);
	    	params.put("CmdType", "SET_WIFI_ONOFF_TIMER");
	    	params.put("SequenceId", "EFAB9000");
	    	
	    	Map p = new HashMap<String, Object>();
	    	//p.put("MAC", "1C25E1012345");
	    	p.put("MAC", "dfgfhtryry43646");
	    	p.put("StartTime", "00:01");
	    	p.put("EndTime", "23:59");
	    	p.put("Enable", "1");
	    	params.put("Parameter", p);
	    	
	    	Map<String, Object> r = wirelessNetworkSettingService.setWifiOnoffTimer(params);
	    	assertEquals(0, r.get("Result"));
	    }
	    //查询Wi-Fi定时开关   没有对应节点！！！！
	    @Test
	    public void testGetWifiOnoffTimer() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	params.put("RPCMethod", "Get");
	    	params.put("ID", 123);
	    	params.put("CmdType", "GET_WIFI_ONOFF_TIMER");
	    	params.put("SequenceId", "EFAB9000");
	    	
	    	Map p = new HashMap<String, Object>();
	    	p.put("MAC", "14144B198734");
	    	params.put("Parameter", p);
	    	
	    	Map<String, Object> r = wirelessNetworkSettingService.getWifiOnoffTimer(params);
	    	assertEquals(0, r.get("Result"));
	    }
	    //设置网关设备名称   ok!*******************************************************************************************ok!
	    //杭研网关OK！
	    //新网网关OK！
	    @Test
	    public void testSetHgName() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	params.put("RPCMethod", "Set");
	    	params.put("ID", 123);
	    	params.put("CmdType", "SET_HG_NAME");
	    	params.put("SequenceId", "EFAB9000");
	    	
	    	Map p = new HashMap<String, Object>();
	    	p.put("MAC", "14144B208734");
	    	p.put("DevName", "设备名称11");
	    	params.put("Parameter", p);
	    	
	    	Map<String, Object> r = familyNetworkSettingService.setHgName(params);
	    	assertEquals(0, r.get("Result"));
	    }
	    //设置下挂的设备别名    没有对应节点！！！！   ok!********************************************************************ok!
	    //杭研 ，星网 OK！！！
	    
	    @Test
	    public void testSetLanDeviceName() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	params.put("RPCMethod", "Set");
	    	params.put("ID", 123);
	    	params.put("CmdType", "SET_LAN_DEVICE_NAME");
	    	params.put("SequenceId", "EFAB9000");
	    	
	    	Map p = new HashMap<String, Object>();
	    	p.put("MAC", "00C00286878A");
	    	//p.put("MAC", "00C00286878A");
	    	p.put("DeviceMAC", "3c:a3:48:ba:73:08");  //LT
	    	p.put("DevName", "zody'1");
	    	params.put("Parameter", p);
	    	
	    	Map<String, Object> r = familyNetworkSettingService.setLanDeviceName(params);
	    	assertEquals(0, r.get("Result"));
	    }
	    //获取网关和下挂的设备名称  OK!*******************************************************************************************OK!
	    //杭研， 星网OK
	    @Test
	    public void testGetHgNamelist() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	params.put("RPCMethod", "Get");
	    	params.put("ID", 123);
	    	params.put("CmdType", "GET_HG_NAMELIST");
	    	params.put("SequenceId", "58A15856");
	    	
	    	Map p = new HashMap<String, Object>();
	    	//p.put("MAC", "000EF4E00607");
	    	//p.put("MAC", "14144B198734");
	    	p.put("MAC", "00C00286878A");
	    	params.put("Parameter", p);
	    	System.out.println("--------------------------"+com.alibaba.fastjson.JSON.toJSONString(params));
	    	Map<String, Object> r = familyNetworkSettingService.getHgNamelist(params);
	    	assertEquals(0, r.get("Result"));
	    }
	    //获取家庭内网拓扑信息
	    @Test
	    public void testGetLanNetInfo() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	params.put("RPCMethod", "Get");
	    	params.put("ID", 123);
	    	params.put("CmdType", "GET_LAN_NET_INFO");
	    	params.put("SequenceId", "EFAB9000");
	    	
	    	Map p = new HashMap<String, Object>();
	    	//p.put("MAC", "00:0E:F4:E0:06:07");
	    	//p.put("MAC", "1C25E10123451");
	    	//p.put("MAC", "14144B198734");
	    	p.put("MAC", "abcd");
	    	params.put("Parameter", p);
	    	
	    	Map<String, Object> r = familyNetworkSettingService.getLanNetInfo(params);
	    	assertEquals(0, r.get("Result"));
	    }
	    //获取网关下挂终端的网络访问控制名单
	    @Test
	    public void testGetLanAccessNet() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	params.put("RPCMethod", "Get");
	    	params.put("ID", 123);
	    	params.put("CmdType", "GET_LAN_ACCESS_NET");
	    	params.put("SequenceId", "EFAB9000");
	    	
	    	Map p = new HashMap<String, Object>();
	    	//p.put("MAC", "00:0E:F4:E0:06:07");
	    	//p.put("MAC", "00C00286878A");
	    	p.put("MAC", "1C25E10123451");
	    	params.put("Parameter", p);
	    	
	    	Map<String, Object> r = homeNetworkConfigService.getLanAccessNet(params);
	    	assertEquals(0, r.get("Result"));
	    }
	    //获取网关下挂终端的网络访问控制名单
	    //杭研 星网OK
	    @Test
	    public void testSetLanAccess() throws Exception {
	    	Map params = new HashMap<String, Object>();
	    	params.put("RPCMethod", "Set");
	    	params.put("ID", 123);
	    	params.put("CmdType", "SET_LAN_ACCESS_NET");
	    	params.put("SequenceId", "EFAB9000");
	    	
	    	Map p = new HashMap<String, Object>();
	    	p.put("MAC", "");
	    	//p.put("MAC", "1C25E10123451");
	    	//p.put("MAC", "00C00286878A");
	    	p.put("DeviceMAC", "3C:A3:48:BA:73:08");
	    	p.put("NetAccessRight", "OFF");
	    	p.put("StorageAccessRight", "OFF");
	    	//p.put("MAC", "14:14:4B:19:87:34");
	    	params.put("Parameter", p);
	    	
	    	Map<String, Object> r = familyNetworkSettingService.setLanAccess(params);
	    	assertEquals(0, r.get("Result"));
	    }
	   
	  
		//测试INFORM CONNECTREQUESTURL
		@Test
		public void testUpdateInformGatewayInfo() throws Exception {

			String info = "{\"acs2CpeEnv\":true,\"cpePassword\":\"cpe\",\"cpeUserName\":\"cpe\",\"currentTime\":\"2016-06-02T15:12:42+08:00\",\"deviceId\":{\"cpeId\":\"CIOT_SHYGW0000008D\",\"manufacturer\":\"CMCC\",\"oui\":\"14144B\",\"productClass\":\"CMCC\",\"serialNubmer\":\"CIOT00198734\"},\"event\":{\"eventCodes\":[{\"commandKey\":\"\",\"evenCode\":\"0 BOOTSTRAP\"},{\"commandKey\":\"\",\"evenCode\":\"1 BOOT\"},{\"commandKey\":\"\",\"evenCode\":\"4 VALUE CHANGE\"}]},\"fault\":false,\"holdReqs\":false,\"maxEnvelopes\":1,\"methodName\":\"Inform\",\"noMoreReqs\":false,\"parameterList\":{\"parameterValueStructs\":[{\"name\":\"InternetGatewayDevice.DeviceSummary\",\"readWrite\":false,\"value\":\"InternetGatewayDevice:1.0[](Baseline:1, IPPing:1, WiFiLAN:1)\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.DeviceInfo.SpecVersion\",\"readWrite\":false,\"value\":\"1.0\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.DeviceInfo.HardwareVersion\",\"readWrite\":false,\"value\":\"1.0\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.DeviceInfo.SoftwareVersion\",\"readWrite\":false,\"value\":\"ZL_FW_16_05_27_100\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.DeviceInfo.ProvisioningCode\",\"readWrite\":false,\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.ManagementServer.ConnectionRequestURL\",\"readWrite\":false,\"value\":\"http://192.168.205.159:7547/tr691\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.ManagementServer.ParameterKey\",\"readWrite\":false,\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANIPConnection.1.ExternalIPAddress\",\"readWrite\":false,\"value\":\"192.168.205.159\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.X_CMCC_UserInfo.Password\",\"readWrite\":false,\"value\":\"00000001\",\"valueType\":\"string\"}]},\"requeired\":true,\"requestId\":\"16006\",\"retryCount\":4,\"urnCwmpVersion\":\"urn:dslforum-org:cwmp-1-1\"}";
			
/*JSONObject json = JSONObject.parseObject(info);
			Inform inform = new Inform();
			String cpeId = json.getJSONObject("deviceId").getString("cpeId");
			String manufacturer = json.getJSONObject("deviceId").getString("manufacturer");
			String oui = json.getJSONObject("deviceId").getString("oui");
			String productClass = json.getJSONObject("deviceId").getString("productClass");
			String serialNubmer = json.getJSONObject("deviceId").getString("serialNubmer");
			DeviceId device = new DeviceId();
			device.setDeviceIdInfor(manufacturer, oui, productClass, serialNubmer );
			inform.setDeviceId(device);
			//"event":{"eventCodes":[{"commandKey":"","evenCode":"4 VALUE CHANGE"},{"commandKey":"","evenCode":"1 BOOT"}]}
			Event event = new Event();
			EventStruct es = new EventStruct("0 BOOTSTRAP");
			event.addEventCode(es);
			inform.setEvent(event);*/


		Inform inform = JSON.toJavaObject(JSONObject.parseObject(info), Inform.class);
		System.out.println(inform);
			gatewayInfoService.updateInformGatewayInfo(inform);
			
			
			//requestMgrService.inform(JSONObject.parseObject(info));
			//assertEquals(0, r.get("Result"));
		}
		//测试导入网关
		@Test
		public void testQueryGatewayDetail() throws Exception {
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("gatewayUuid", "2");
			gatewayManageService.uploadFile(params);
			
			
			//requestMgrService.inform(JSONObject.parseObject(info));
			//assertEquals(0, r.get("Result"));
		}
		
		
		public static void main(String[] args) {
			
			String info = "{\"acs2CpeEnv\":true,\"currentTime\":\"1970-01-01T00:00:56+00:00\",\"deviceId\":{\"cpeId\":\"CIOT_31198734\",\"manufacturer\":\"CIOT\",\"oui\":\"CIOT\",\"productClass\":\"GM219-A\",\"serialNubmer\":\"31198734\"},\"event\":{\"eventCodes\":[{\"commandKey\":\"\",\"evenCode\":\"4 VALUE CHANGE\"},{\"commandKey\":\"\",\"evenCode\":\"1 BOOT\"}]},\"holdReqs\":false,\"maxEnvelopes\":1,\"methodName\":\"Inform\",\"noMoreReqs\":false,\"parameterList\":{\"parameterValueStructs\":[{\"name\":\"InternetGatewayDevice.DeviceSummary\",\"readWrite\":false,\"value\":\"InternetGatewayDevice:1.5[](Baseline:1, EthernetLAN:1, USBLAN:1, Time:1, IPPing:1, DeviceAssociation:1, QoS:1, WiFiLAN:1, Download:1, Upload:1, DownloadTCP:1, UploadTCP:1, UDPEcho:1, UDPEchoPlus:1) , VoiceService:1.0[1](Endpoint:1, SIPEndpoint:1)\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.DeviceInfo.SpecVersion\",\"readWrite\":false,\"value\":\"1.0\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.DeviceInfo.HardwareVersion\",\"readWrite\":false,\"value\":\"HV.01.1.1.0.0\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.DeviceInfo.SoftwareVersion\",\"readWrite\":false,\"value\":\"V.01.1.1.0.0\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.DeviceInfo.ProvisioningCode\",\"readWrite\":false,\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.ManagementServer.ConnectionRequestURL\",\"readWrite\":false,\"value\":\"http://192.168.205.5:300051/\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.ManagementServer.ParameterKey\",\"readWrite\":false,\"value\":\"ba2bbb9f89b940cca084ed3d5fe5317b\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANIPConnection.1.ExternalIPAddress\",\"readWrite\":false,\"value\":\"192.168.205.5\",\"valueType\":\"string\"},{\"name\":\"InternetGatewayDevice.X_CMCC_UserInfo.Password\",\"readWrite\":false,\"value\":\"2500146622\",\"valueType\":\"string\"}]},\"requeired\":true,\"requestId\":\"846930886\",\"retryCount\":0,\"urnCwmpVersion\":\"urn:dslforum-org:cwmp-1-1\"}";
			Inform inform = JSON.toJavaObject(JSONObject.parseObject(info), Inform.class);
			System.out.println(inform);

			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	try {
				System.out.println(sdf.parse("2016-04-04 04:04:04").getTime()/1000);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		//测试配置列表网关查询
		@Test
		public void testQueryDubboList4Page() throws Exception {
			
			Map<String, Object> params = new HashMap<String, Object>();
			//params.put("gatewayInfoAreaId", "530000");
			params.put("uid", "0efb1f61593e4ee8af7c33254576b2dc");
			gatewayInfoInterface.queryDubboList4Page(params);
			
		}
		//测试查询批量配置网关流量查询任务
		@Test
		public void testQueryFlowrateTaskList() throws Exception {
			
			Map<String, Object> parameter = new HashMap<String, Object>();
			//params.put("gatewayInfoAreaId", "530000");
			parameter.put("uid", "admin");
			gatewayFlowrateTaskService.queryFlowrateTaskList(parameter);
			
		}
		//测试新增批量配置网关流量查询任务
		@Test
		public void testAddFlowrateTask() throws Exception {
			
			Map<String, Object> parameter = new HashMap<String, Object>();
			//params.put("gatewayInfoAreaId", "530000");
			parameter.put("taskName", "add flowrate task");
			parameter.put("taskTriggerMode", 1);
			parameter.put("startTime", "");
			parameter.put("endTime", 1);
			parameter.put("areaId", "530000");
			gatewayFlowrateTaskService.addFlowrateTask(parameter);
			
		}
		//测试查询批量配置网关流量查询任务
		@Test
		public void testEditFlowrateTask() throws Exception {
			
			Map<String, Object> parameter = new HashMap<String, Object>();
			//params.put("gatewayInfoAreaId", "530000");
			parameter.put("id", "e091a7e576804fb08d3c5f6ee9d8d2f6");
			gatewayFlowrateTaskService.editFlowrateTask(parameter);
			
		}
		//测试更新批量配置网关流量查询任务
		@Test
		public void testUpdateFlowrateTask() throws Exception {
			
			Map<String, Object> parameter = new HashMap<String, Object>();
			//params.put("gatewayInfoAreaId", "530000");
			parameter.put("id", "e091a7e576804fb08d3c5f6ee9d8d2f6");
			parameter.put("taskName", "222222");
			gatewayFlowrateTaskService.updateFlowrateTask(parameter);
			
		}
		
		@Test
		public void testAmsAreaService() throws Exception {
		/*
			Map<String, Object> parameter = new HashMap<String, Object>();
			//params.put("gatewayInfoAreaId", "530000");
			parameter.put("ip", "192.168.205.193");
			Map<String, Object> map =  amsAreaService.findGateWayArea(parameter);
			System.out.println(map.containsKey("areaId"));
			System.out.println(map);*/
			
			int topAreaId = -1;
			List<com.cmiot.ams.domain.Area> areaList = amsAreaService.findAllArea();
            for(Area area : areaList){
            	if(area.getPid() == 0){
            		topAreaId = area.getId();
            		break;
            	}
            }
			System.out.println(topAreaId);
		}
		@Test
		public void testSyncGatewayBaseInfo() throws Exception {
			
			List<Map<String, Object>> parameter = new ArrayList<Map<String,Object>>();
			Map<String, Object> reqmap = new HashMap<String, Object>();
			reqmap.put("password", "s027");
			reqmap.put("areaId", "53");  //5325
			Map<String, Object> reqmap2 = new HashMap<String, Object>();
			reqmap2.put("password", "s024");
			reqmap2.put("areaId", "53");  //5313
			parameter.add(reqmap);
			parameter.add(reqmap2);
			Map<String, Object> map =  gatewayBaseInfoService.syncGatewayBaseInfo(reqmap2);
			
		}
		@Test
		public void testBoxConnectionUrl() throws Exception {
			
			List<Map<String, Object>> parameter = new ArrayList<Map<String,Object>>();
			Map<String, Object> reqmap = new HashMap<String, Object>();
			reqmap.put("password", "s027");
			reqmap.put("areaId", "53");  //5325
			Map<String, Object> reqmap2 = new HashMap<String, Object>();
			reqmap2.put("password", "s024");
			reqmap2.put("areaId", "53");  //5313
			parameter.add(reqmap);
			parameter.add(reqmap2);
			Map<String, Object> map =  gatewayBaseInfoService.syncGatewayBaseInfo(reqmap2);
			
		}
		
		//测试查询批量配置网关流量查询任务
		@Test
		public void testQueryFlowrateTaskDetails() throws Exception {
			
			Map<String, Object> parameter = new HashMap<String, Object>();
			//params.put("gatewayInfoAreaId", "530000");
			parameter.put("id", "eecba8493d15463882910da848ca698a");
			gatewayFlowrateTaskService.queryFlowrateTaskDetails(parameter);
			
		}
		//测试查询批量配置网关流量查询任务
		@Test
		public void testSyncGatewayOnlineStatus() throws Exception {
			
			Map<String, Object> parameter = new HashMap<String, Object>();
			parameter.put("gatewayUuid", "0");
			parameter.put("timeout", "30");
			gatewayManageService.syncGatewayOnlineStatus(parameter);
			
		}
		  @Test
		   public void testGetHgPortsTrafficStatus() throws Exception {
		    	Map params = new HashMap<String, Object>();
		    	params.put("RPCMethod", "Set");
		    	params.put("ID", 123);
		    	params.put("CmdType", "SET_LAN_ACCESS_NET");
		    	params.put("SequenceId", "EFAB9000");
		    	
		    	Map p = new HashMap<String, Object>();
		    	p.put("MAC", "14144B198734");
		    	//p.put("MAC", "1C25E10123451");
		    	params.put("Parameter", p);
		    	
		    	Map<String, Object> r = gatewaySpeedTrafficInfoService.getHgPortsTrafficStatus(params);
		    	assertEquals(0, r.get("Result"));
		    }
		 
		  @Test
		  public void testCustomerRequestResume() throws Exception {
			  Map params = new HashMap<String, Object>();
			  params.put("RPCMethod", "Set");
			  params.put("ID", 123);
			  params.put("CmdType", "CUSTOMER_REQUEST_RESUME");
			  params.put("SequenceId", "EFAB9000");
			  
			  Map p = new HashMap<String, Object>();
			  p.put("OrderNo", "14144B1987344");
			  p.put("PppoeAccount", "1C25E10123451");
			  p.put("ServiceCode", "wband");
			  params.put("Parameter", p);
			  
			  Map<String, Object> r = workOrderInterface.customerRequestResume(params);
			  System.out.println(r);
			  assertEquals(0, r.get("Result"));
		  }	  
		  @Test
		  public void testCustomerRequestStop() throws Exception {
			  Map params = new HashMap<String, Object>();
			  params.put("RPCMethod", "Set");
			  params.put("ID", 123);
			  params.put("CmdType", "CUSTOMER_REQUEST_STOP");
			  params.put("SequenceId", "EFAB9000");
			  
			  Map p = new HashMap<String, Object>();
			  p.put("OrderNo", "14144B1987314");
			  p.put("PppoeAccount", "1C25E10123451");
			  p.put("ServiceCode", "wband");
			  params.put("Parameter", p);
			  
			  Map<String, Object> r = workOrderInterface.customerRequestStop(params);
			  System.out.println(r);
			  assertEquals(0, r.get("Result"));
		  }	  
		  @Test
		  public void testHangzhouFacade() throws Exception {
			  
			//  {RPCMethod=Report, Parameter={GatewayList=[1C25E10001F8], PlanId=7110d2c726f44a1ebc59dc7202e09295, BeginTime=1474429839}, ID=1474429447, CmdType=REPORT_UPGRADE_PLAN, SequenceId=b2365e8b}
			  
			  
			  
			Map<String, Object> reportMap = new HashMap<String, Object>();
          	reportMap.put("RPCMethod", "Report");
          	reportMap.put("ID", (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
          	reportMap.put("CmdType", "REPORT_UPGRADE_PLAN");
          	reportMap.put(Constant.SEQUENCEID, InstructionUtil.generate8HexString());
          	Map<String, Object> map = new HashMap<String, Object>();
          	map.put("PlanId", "7110d2c726f44a1ebc59dc7202e09295");

          	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
          	SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
          	try {
					map.put("BeginTime", format2.parse("2016-09-21 16:03:00").getTime()/1000);
				} catch (ParseException e) {
					e.printStackTrace();
				}
          	List<String> macList = new ArrayList<String>();
          	macList.add("1C25E10001F8");
          	map.put("GatewayList", macList);
          	reportMap.put("Parameter", map);
          	
          	
          	
			  
			  Map<String, Object> r = hangzhouFacade.sendJson("http://112.54.207.62:30080/", reportMap);
			  System.out.println(r);
			  assertEquals(0, r.get("Result"));
		  }	  
		  @Test
		  public void testHWGateway() throws Exception {
			
		/*	Map<String,Object> map=instructionMethodService.getParameterNames("abcd", "InternetGatewayDevice.", false);
			  List<String> list = new ArrayList<String>();
			  for (Map.Entry<String, Object> entry : map.entrySet()) {  
					list.add(entry.getKey());  
			  }  
			  String keys = "";
			  List<String> templist = list.subList(0, 300);
			  Map<String, Object> retMap = instructionMethodService.getParameterValues("abcd",  templist);
			  for (Map.Entry<String, Object> entry : retMap.entrySet()) {
					if("cmccadmin".equals(entry.getValue())  || "cmccadmin25703777".equals(entry.getValue())){
						keys += entry.getKey()+";";
					}
				}
			 */

			  List<String> wlanconfigList = new ArrayList<>();
				Map<String,Object> mapWLANConfiguration = instructionMethodService.getParameterNames("1C25E1000030", "InternetGatewayDevice.LANDevice.1." +  "WLANConfiguration.", true);
				for (Map.Entry<String, Object> entry : mapWLANConfiguration.entrySet()) {
					System.out.println(entry.getKey().trim());
					wlanconfigList.add(entry.getKey().trim() + "AssociatedDevice.");
				}

			  





			  
		  }	  
		  
		
		  
}

