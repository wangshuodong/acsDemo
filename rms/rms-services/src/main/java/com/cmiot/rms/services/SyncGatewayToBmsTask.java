package com.cmiot.rms.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cmiot.ams.service.AppUserService;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.mapper.GatewayQueueMapper;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.GatewayQueue;
import com.cmiot.rms.dao.model.GatewayQueueExample;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.tydic.bean.GatewayDeviceBean;
import com.tydic.service.PluginDeviceService;

/**
 * Created by liucs on 2016/7/19.
 */
@Service
public class SyncGatewayToBmsTask {
	
	private Logger logger = LoggerFactory.getLogger(SyncGatewayToBmsTask.class);
	
	@Value("${timing.task.lock.timeout}")
	int lockTimeout;
	
	@Value("${admin.phoneNo}")
	String phoneNo;
	
	@Autowired
    RedisClientTemplate redisClientTemplate;
	
	@Autowired
	GatewayQueueMapper gatewayQueueMapper;
	
	@Autowired
	GatewayInfoMapper gatewayInfoMapper;
	
	@Autowired
    private AppUserService appUserService;
	
	@Autowired
	PluginDeviceService pluginDeviceService;
	
	
	public void synGatewayToBms() {
        
		logger.info("同步网关任务开始执行");
		try {
            String str = redisClientTemplate.set("synGatewayToBms_lock", "yes", "NX","EX", lockTimeout);
            if (!redisClientTemplate.exists("synGatewayToBms_fail_times")) {
            	redisClientTemplate.set("synGatewayToBms_fail_times", "0");
			}
            
            if (str == null) {// 存在锁
                logger.info("同步网关任务正在执行..");
                return;
            }
            
            boolean flag = true;
            while (flag) {
            	GatewayQueueExample example = new GatewayQueueExample();
                example.createCriteria().andSynStatuEqualTo(0);
                List<GatewayQueue> queryList = gatewayQueueMapper.selectByExample(example);
                
                //发送到bms
                if (queryList.isEmpty()) {
                	flag = false;
                	logger.info("无数据同步到BMS");
                	break;
    			}else{
    				//已经是最后一次
    				if (queryList.size()<5000) {
    					flag = false;
					}
    				
                	List<GatewayInfo> gatewayList = gatewayInfoMapper.queryGatewayListById(queryList);
                	GatewayDeviceBean gatewayDeviceBean;
                	List<GatewayDeviceBean> gatewayDeviceList = new ArrayList<GatewayDeviceBean>();
                	for (GatewayInfo gatewayInfo : gatewayList) {
                		gatewayDeviceBean = new GatewayDeviceBean();
                		gatewayDeviceBean.setDeviceType(gatewayInfo.getGatewayType());
                		gatewayDeviceBean.setFirmwareVer(gatewayInfo.getGatewayVersion());
                		gatewayDeviceBean.setIp(gatewayInfo.getGatewayIpaddress());
                		gatewayDeviceBean.setMacAddress(gatewayInfo.getGatewayMacaddress());
                		gatewayDeviceBean.setModel(gatewayInfo.getGatewayModel());
                		gatewayDeviceBean.setPassword(gatewayInfo.getGatewayPassword());
                		gatewayDeviceBean.setBrand(gatewayInfo.getGatewayFactoryCode());
                		gatewayDeviceBean.setVersion(gatewayInfo.getGatewayVersion());
                		gatewayDeviceBean.setDeviceOui(gatewayInfo.getGatewayFactoryCode());
                		gatewayDeviceBean.setDeviceSn(gatewayInfo.getGatewaySerialnumber());
                		
                		gatewayDeviceBean.setSsidInitLastnumber(gatewayInfo.getSsidInitLastnumber());
                		gatewayDeviceBean.setSsidInitPwd(gatewayInfo.getSsidInitPwd());
                		gatewayDeviceBean.setGatewayUseradminInitPwd(gatewayInfo.getUadminInitPwd());
                		gatewayDeviceList.add(gatewayDeviceBean);
                		
    				}
                    
                	Map<String,Object> map = pluginDeviceService.importGateWayDevices(gatewayDeviceList);
                	//同步成功
                	if (Integer.valueOf(map.get("resultCode").toString()) == 0) {
                		//更新队列记录状态
                		gatewayQueueMapper.batchUpdateGatewayQueue(queryList);
                		//失败次数清零
                		redisClientTemplate.set("synGatewayToBms_fail_times", "0");
                		
                		logger.info("同步"+gatewayList.size()+"条网关到BMS成功");
    				}else {
    					
    					int failTimes =  Integer.valueOf(redisClientTemplate.get("synGatewayToBms_fail_times"))+1;
    					redisClientTemplate.set("synGatewayToBms_fail_times",  String.valueOf(failTimes));
    					//第三次同步失败
    					if (failTimes>2) {
    						//发送短信
    						String[] phones = phoneNo.split(",");
    						Map<String,Object> param = new HashMap<String,Object>();
    						param.put("msg", "同步网关数据到BMS连续"+failTimes+"次失败:"+map.get("resultMsg"));
    						for (int i = 0; i < phones.length; i++) {
    							
    							param.put("phoneNo", phones[i]);
    							appUserService.sendSmsMsg(param); 
    						}
    						
    						logger.info("同步网关数据到BMS连续"+failTimes+"次失败，BMS返回:"+map.get("resultMsg"));
    						
    					}
    					
    					flag = false;
    				}
    			}
			}
            
            
        }
       finally {
            redisClientTemplate.del("synGatewayToBms_lock");
        }
		
    }
}
