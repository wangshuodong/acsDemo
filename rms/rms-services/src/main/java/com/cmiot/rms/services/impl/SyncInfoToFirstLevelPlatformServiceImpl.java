package com.cmiot.rms.services.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.cmiot.acs.facade.OperationCpeFacade;
import com.cmiot.hoa.facade.hangzhou.HangzhouFacade;
import com.cmiot.rms.services.SyncInfoToFirstLevelPlatformService;
import com.iot.common.http.APIHttpClient;


@Service("syncInfoToFirstLevelPlatformService")
public class SyncInfoToFirstLevelPlatformServiceImpl implements
SyncInfoToFirstLevelPlatformService {

	private static Logger logger = LoggerFactory.getLogger(SyncInfoToFirstLevelPlatformServiceImpl.class);

	@Value("${first.level.platform.url}")
	String url;
	
	//ON,OFF
	@Value("${first.level.platform.sync.lock}")
    String lock;

	@Autowired
	HangzhouFacade hangzhouFacade;
	
	@Override
	public void report(String method ,Map<String, Object> reportMap) {
		logger.info("Start invoke SyncInfoToFirstLevelPlatformServiceImpl report lock:{}, method:{}, parameter:{}", lock, method, reportMap);
		if("ON".equals(lock)){
			Thread thred = new Thread(){

				@Override
				public void run() {
					
					 Map<String, Object> retMap = hangzhouFacade.sendJson(url + method, reportMap);
					//Map<String, Object> retMap = APIHttpClient.post(url + method, JSON.toJSONString(reportMap));
					logger.info("End invoke post:{}", retMap);
					
				}
				
			};
			
			thred.start();
		}
	}
}
