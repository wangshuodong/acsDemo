package com.cmiot.rms.services.impl;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.services.FamilyNetworkSettingService;
import com.cmiot.rms.services.LogManagerService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
        "file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
public class FamilyNetworkSettingServiceImplTest {
	private static Logger logger = LoggerFactory.getLogger(FamilyNetworkSettingServiceImplTest.class);
	@Autowired
	private FamilyNetworkSettingService familyNetworkSettingService;
	@Autowired
	private LogManagerService logManagerService;
	
	@Test
	public void test(){
		Map params = new HashMap<String, Object>();
    	params.put("RPCMethod", "Get");
    	params.put("ID", 123);
    	params.put("CmdType", "GET_HG_NAMELIST");
    	params.put("SequenceId", "58A15856");
    	
    	Map p = new HashMap<String, Object>();
    	//p.put("MAC", "000EF4E00607");
    	p.put("MAC", "00C00286878A");
    	//p.put("MAC", "00C00286878A");
    	params.put("Parameter", p);
    	
    	/*{
    	    "RPCMethod": "Get",
    	    "ID": 123,
    	    "CmdType": "GET_HG_NAMELIST",
    	    "SequenceId": "58A15856",
    	    "Parameter": {
    	        "MAC": "14:14:4B:19:87:34"
    	    }
    	}*/
    	
    	System.out.println("--------------------------"+JSON.toJSONString(params));
    	Map<String, Object> r = familyNetworkSettingService.getHgNamelist(params);
    	logger.info("返回结果为：-----------" + JSON.toJSONString(r));
//    	assertEquals(0, r.get("Result"));

	}
	@Test
	public void testLogger(){
		
	}
}
