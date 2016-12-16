/**
 * 
 */
package com.cmiot.rms.services.impl;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cmiot.rms.dao.mapper.GatewayPasswordMapper;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.GatewayPassword;
import com.cmiot.rms.dao.model.GatewayQueue;
import com.cmiot.rms.dao.model.GatewayQueueExample;

/**
 * @author lcs
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
	"file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
public class GatewayPasswordTest {

	@Autowired
	GatewayPasswordMapper gatewayPasswordMapper;
	
	@Test
    public void selectBySelectiveTest() throws Exception {
	   
		GatewayPassword gatewayPassword = new GatewayPassword();
//		gatewayPassword.setAdslAccount("ggg");
//		GatewayPassword gatewayPassword2 =gatewayPasswordMapper.selectBySelective(gatewayPassword);
//		System.out.println(gatewayPassword2);
		
		gatewayPassword.setGatewayPassword("yyyy");
		GatewayPassword gatewayPassword3 =gatewayPasswordMapper.selectByPassword(gatewayPassword);
		System.out.println(gatewayPassword3);
		
    }
	
	
	@Test
    public void insertTest() throws Exception {
	   
		GatewayPassword gatewayPassword = new GatewayPassword();
        gatewayPassword.setGatewayPassword("yyyy");
        gatewayPassword.setOrderNo("4444");  
        gatewayPassword.setAdslAccount("5555");
//        int i = gatewayPasswordMapper.insert(gatewayPassword);
//        System.out.println(i);
        
        gatewayPasswordMapper.updateByPrimaryKeySelective(gatewayPassword);
		
    }
}
