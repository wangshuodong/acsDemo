package com.cmiot.rms.services.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cmiot.rms.services.AcsInterfaceService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
	"file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
public class AcsInterfaceServiceTest {
	
	@Resource
    private AcsInterfaceService acsInterfaceService;
	
	@Test
    public void getbootStrapInstructionsTest() throws Exception {
	    
		Map<String, Object> parameter = new HashMap<>();
		parameter.put("oui", "54BE53");
		parameter.put("sn", "3420054BE534C77A0");
		acsInterfaceService.getbootStrapInstructions(parameter);
    }
	
}
