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

import com.cmiot.rms.dao.mapper.BoxBusinessCategoryMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessMapper;
import com.cmiot.rms.dao.mapper.WorkOrderTemplateInfoMapper;
import com.cmiot.rms.dao.model.BoxBusinessCategory;
import com.cmiot.rms.dao.model.GatewayBusiness;
import com.cmiot.rms.services.WorkOrderStandardInterface;
import com.cmiot.rms.services.workorder.impl.BusiOperation;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
	"file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
public class WorkOrderStandardInterfaceTest {
	
	@Resource
    private WorkOrderStandardInterface workOrderStandardInterface;
	
	@Autowired
	private BoxBusinessCategoryMapper boxBusinessCategoryMapper;
	
	@Autowired
    WorkOrderTemplateInfoMapper workOrderTemplateInfoMapper;
	
	@Autowired
    BusiOperation busiOperation;
	
	@Autowired
	private GatewayBusinessMapper gatewayBusinessMapper;
	
	@Test
    public void addNewInstallationTest() throws Exception {
	   
		Map<String,Object> parameter = new HashMap<String,Object>();
		parameter.put("orderNo", "ewte4634576463");
		parameter.put("provCode", "BJ");
		parameter.put("areaCode", "5322");
		parameter.put("userId", "346547546");
		parameter.put("orderTime", "346436fdgfdg");
		parameter.put("deviceType", "ihgu");
//		parameter.put("deviceType", "ott");
		parameter.put("serviceCode", "wband");
//		parameter.put("serviceCode", "stb");
		parameter.put("operationType", "C");
//		parameter.put("operationType", "Z");
		parameter.put("serviceMode", "wband_ppp");
//		parameter.put("serviceMode", "wband_dhcp");
//		parameter.put("serviceMode", "wband_static");
//		parameter.put("serviceMode", "wband_bridge");
//		parameter.put("serviceMode", "ott_ppp");
//		parameter.put("serviceMode", "ott_dhcp");
//		parameter.put("serviceMode", "iptv_bridge");
		
		
		parameter.put("bindCode", "ciot445567");
		
		parameter.put("userName", "nnnnn");
		parameter.put("userAddress", "mmmmmm");
		parameter.put("contactPerson", "rrrrrr");
		parameter.put("contactManner", "dddddd");
		parameter.put("orderTime", "2016-10-25 15:30:25");
		
		Map<String,Object> argues = new HashMap<String,Object>();
		//wband_ppp
//		argues.put("Username", "Username");
//		argues.put("Password", "Password");
		
		//wband_dhcp
//		argues.put("VLANID", "67");
//		argues.put("LanInterface", "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1");
		
		//wband_static
//		argues.put("VLANID", "42");
//		argues.put("ExternalIPAddress", "172.19.10.8");
//		argues.put("SubnetMask", "255.0.0.1");
//		argues.put("DefaultGateway", "255.0.2.1");
//		argues.put("LanInterface", "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1");
		
		
		//wband_bridge
//		argues.put("VLANID", "42");
//		argues.put("LanInterface", "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1");
		
		//ott_stb
//		argues.put("IPOEID", "srhgfh43646");
//		argues.put("IPOEPassword", "dgfdhgfdg");
//		argues.put("UserID", "345436");
//		argues.put("UserIDPassword", "34658kjhkj");
		
		//ott_ppp
//		argues.put("VLANID", "42");
//		argues.put("Username", "dgfdhgfdg");
//		argues.put("Password", "345436");
//		argues.put("LanInterface", "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.3");
//		argues.put("DefaultConnectionService", "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANPPPConnection.1");
//		argues.put("DestIPAddress", "172.19.10.11");
//		argues.put("DestSubnetMask", "255.0.0.1");
		
		//ott_dhcp
//		argues.put("VLANID", "42");
//		argues.put("LanInterface", "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.3");
//		argues.put("DefaultConnectionService", "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANPPPConnection.1");
//		argues.put("DestIPAddress", "172.19.10.11");
//		argues.put("DestSubnetMask", "255.0.0.1");
		
		//iptv_bridge
		argues.put("VLANID", "42");
		argues.put("LanInterface", "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.3");
		parameter.put("vectorArgues", "VLANID=200^Username=test10^Password=1234");
		
//		Map<?, ?> resultMap = workOrderStandardInterface.addNewInstallation(parameter);
//	   System.out.println(resultMap.get("Result"));
//	   System.out.println(resultMap.get("errorParams"));
		
		
		/*BoxBusinessCategory category = new BoxBusinessCategory();
		category.setBusinessCode("iptv_stb");//进行业务编码的唯一验证
		List<Map<String,Object>> boxBusinessCategory = boxBusinessCategoryMapper.queryListAll(category);
		System.out.println(boxBusinessCategory.get(0).get("businessName"));*/
	   
	   
	   /*Map<String, Object> paMap = new HashMap<>();
       paMap.put("businessCode", "wband_bridge_Z");
	   Map selectResultMap = workOrderTemplateInfoMapper.selectDefaultTemplate(paMap);
	   String template_message = (String) selectResultMap.get("template_message");
	   Map<String, Object> parameterMap = new HashMap<String, Object>();
	   parameterMap.put("VLANID", "srhgfh43646");
	   parameterMap.put("LanInterface", "InternetGatewayDevice.LANDevice.1.LANEthernetInterfaceConfig.1");
	   parameterMap.put("gateWayMac", "werewr");
	   Map<?, ?> resultMap = busiOperation.excute(template_message, parameterMap);*/
	   
	   GatewayBusiness record = new GatewayBusiness();
	   record.setId("001ef2be64604a04b420911933b556e1");
	   gatewayBusinessMapper.updateByPrimayKey(record);
    }
	
}
