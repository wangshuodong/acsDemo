package com.cmiot.rms.services.impl;


import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.services.BoxInfoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../rms-api/src/main/resources/META-INF/spring/dubbo-*.xml",
	"file:../rms-api/src/main/resources/META-INF/spring/applicationContext.xml"})
public class BoxInfoServiceTest {
	
	@Autowired
	BoxInfoService boxInfoService;
	
	@Test
	public void updateBySnSelectiveTest() throws Exception {
		
		BoxInfo box = new BoxInfo();
		box.setBoxOnline(1);
		box.setBoxSerialnumber("10");
		long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
			// 最近连接时间
    	 box.setBoxLastConnTime((int) timeSeconds);
		int result = boxInfoService.updateBySnSelective(box);
		System.out.println("执行结果:"+result);
	}
	
}
