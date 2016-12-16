package com.cmiot.rms.services.impl;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cmiot.hoa.facade.hangzhou.HangzhouFacade;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.GatewayBusinessMapper;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.mapper.GatewayQueueMapper;
import com.cmiot.rms.dao.model.DeviceInfo;
import com.cmiot.rms.dao.model.GatewayBusiness;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.GatewayQueue;
import com.cmiot.rms.dao.model.GatewayQueueExample;
import com.cmiot.rms.dao.model.HardwareAblity;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.GatewayManageService;
import com.cmiot.rms.services.HardwareAblityService;
import com.cmiot.rms.services.SyncGatewayToBmsTask;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
	"file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
public class SyncGatewayToBmsTaskTest {
	
	@Autowired
	GatewayQueueMapper gatewayQueueMapper;
	
	@Autowired
	GatewayInfoMapper gatewayInfoMapper;
	
	@Resource
    private GatewayInfoService gatewayInfoService;
	
	@Resource
    private HardwareAblityService hardwareAblityService;
	
	@Resource
    private GatewayManageService gatewayManageService;
	
	@Resource
    private SyncGatewayToBmsTask syncGatewayToBmsTask;
	
	@Resource
    private HangzhouFacade hangzhouFacade;
	

	@Test
	public void selectByExampleTest() throws Exception {
		
		hangzhouFacade.sendJson("Http://www.baidu.com", new HashMap<>());
		   
//		GatewayQueueExample example = new GatewayQueueExample();
//        example.createCriteria().andSynStatuEqualTo(0);
//		List<GatewayQueue> list = gatewayQueueMapper.selectByExample(example);
//		System.out.println(list.size());
	}
	
	@Test
    public void queryGatewayListByIdTest() throws Exception {
	   
		GatewayQueueExample example = new GatewayQueueExample();
        example.createCriteria().andSynStatuEqualTo(0);
		List<GatewayQueue> list = gatewayQueueMapper.selectByExample(example);
	   
	   List<GatewayInfo> gatewayList = gatewayInfoMapper.queryGatewayListById(list);
	   System.out.println(gatewayList.size());
    }
	
	
	@Test
    public void batchUpdateGatewayQueueTest() throws Exception {
	   
		GatewayQueueExample example = new GatewayQueueExample();
        example.createCriteria().andSynStatuEqualTo(0);
		List<GatewayQueue> list = gatewayQueueMapper.selectByExample(example);
	   
		int i = gatewayQueueMapper.batchUpdateGatewayQueue(list);
	   System.out.println(i);
    }
	
	
	@Test
    public void batchInsertTest() throws Exception {
	   
		
		List<GatewayInfo> gatewayList = new ArrayList<GatewayInfo>();
		
		List<HardwareAblity> hardwareList = new ArrayList<HardwareAblity>();
		
		List<GatewayQueue> queueList = new ArrayList<GatewayQueue>();
		
		GatewayQueue gatewayQueue;
		GatewayInfo gatewayInfo;
		HardwareAblity hardwareAblity;
		
		for (int i = 0; i < 50000; i++) {
			
			gatewayInfo = new GatewayInfo();
			gatewayInfo.setGatewayUuid(UniqueUtil.uuid());
			gatewayInfo.setGatewayUuid(UniqueUtil.uuid());
			// 设置网关终端信息默认值
			gatewayInfo.setGatewayDeviceUuid(i + "" + i);
			gatewayInfo.setGatewayFirmwareUuid(i + "" + i);
			gatewayInfo.setGatewayType("类型1");
			gatewayInfo.setGatewayModel("DSPGM219");
			gatewayInfo.setGatewayName("");
			gatewayInfo.setGatewayVersion("V1.0.0.1");
			gatewayInfo.setGatewaySerialnumber("CIOT0001234"+i);

			// 厂家名称和厂家编码
			gatewayInfo.setGatewayFactoryCode("1C25E1");
			gatewayInfo.setGatewayFactory("");
			gatewayInfo.setGatewayMemo("");
			gatewayInfo.setGatewayHardwareVersion("V1.0.0");
			gatewayInfo.setGatewayJoinTime(null);
			gatewayInfo.setGatewayLastConnTime(null);
			gatewayInfo.setGatewayAdslAccount("");
			gatewayInfo.setGatewayIpaddress("192.168.205.101");
			gatewayInfo.setGatewayMacaddress("CIOT000123461"+i);
			gatewayInfo.setGatewayStatus("");
			gatewayInfo.setGatewayDigestAccount("");
			gatewayInfo.setGatewayDigestPassword("");
			gatewayInfo.setGatewayConnectionrequesturl("");
			gatewayInfo.setOsgi("osgi");
            gatewayInfo.setJvm("jvm");
			
			gatewayList.add(gatewayInfo);

			hardwareAblity = new HardwareAblity();
//			hardwareAblity.setGatewayInfoUuid(gatewayInfo.getGatewayUuid());
			hardwareAblity.setHardwareAblityLanCount(5);
			hardwareAblity.setHardwareAblityUsbCount(5);
			hardwareAblity.setHardwareAblitySupportWifi(true);
			hardwareAblity.setHardwareAblityWifiLoc("dfdfgfdg");
			hardwareAblity.setHardwareAblityWifiCount(5);
			hardwareAblity.setHardwareAblityWifiSize("5ffff");
			hardwareAblity.setHardwareAblitySupportWifi24ghz("dsgdfgg");
			hardwareAblity.setHardwareAblitySupportWifi58ghz("dfgdfg");
			hardwareAblity.setHardwareAblityIpv4v6("192.168.205.101");
			hardwareAblity.setHardwareAblityUuid(UniqueUtil.uuid());
			hardwareList.add(hardwareAblity);
			
			
			gatewayQueue = new GatewayQueue();
			gatewayQueue.setGatewayQueueId(i+"");
			gatewayQueue.setGatewayUuid(gatewayInfo.getGatewayUuid());
			gatewayQueue.setSynStatu(0);
			queueList.add(gatewayQueue);
		}
		
		
		long time = System.currentTimeMillis();
		
		
		try {
			gatewayInfoService.addbatchGatewayHardwareQueue(gatewayList, hardwareList, queueList);
		} catch (Exception e) {
			System.out.println("Exception happend"+e.getMessage());
		}
//		gatewayInfoService.batchInsertGatewayInfo(gatewayList);
//		hardwareAblityService.batchInsertHardwareAblity(hardwareList);
//        gatewayQueueMapper.batchInsertGatewayQueue(queueList);
        System.out.println("花费时间---------------------------------:"+(System.currentTimeMillis()-time));
    }
	
	@Test
	public void SyncGatewayToBmsTaskTest() {

		syncGatewayToBmsTask.synGatewayToBms();
	}
	
}
