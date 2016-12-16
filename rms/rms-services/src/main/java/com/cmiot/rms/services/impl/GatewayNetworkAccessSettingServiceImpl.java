package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.GatewayNetworkAccessSettingService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.util.InstructionUtil;

public class GatewayNetworkAccessSettingServiceImpl implements GatewayNetworkAccessSettingService {
	private static Logger logger = LoggerFactory.getLogger(GatewayNetworkAccessSettingServiceImpl.class);
	@Autowired
	private GatewayInfoService gatewayInfoService;
	@Autowired
	private InstructionMethodService instructionMethodService;
	
	
	@Override
	public Map<String, Object> setLanAccess(Map<String, Object> parameter) {
		logger.info("start invoke GatewayNetworkAccessSettingService.setLanAccess:{}", parameter);
		//1.获取网关MAC
//        Map<String, Object> parameterMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = null != parameter.get(Constant.MAC) ? parameter.get(Constant.MAC).toString() : "";
        String DeviceMAC = null != parameter.get("DeviceMAC") ? parameter.get("DeviceMAC").toString() : "";
        String NetAccessRight = null != parameter.get("NetAccessRight") ? parameter.get("NetAccessRight").toString() : "";
        String StorageAccessRight = null != parameter.get("StorageAccessRight") ? parameter.get("StorageAccessRight").toString() : "";
        List<Map<String, Object>> durationList = (List<Map<String, Object>>) parameter.get("DurationList");
        String returnNetAccessRight = NetAccessRight;
        
        parameter.remove(Constant.PARAMETER);
        parameter.remove(Constant.RPCMETHOD);
        if("".equals(gatewayMacAddress) || "".equals(DeviceMAC) || "".equals(NetAccessRight) ||"".equals(StorageAccessRight)){
        	
        	parameter.put(Constant.RESULT, -102);
        	Map<String, Object> resultData  = new HashMap<String, Object>();
        	resultData.put("FailReason", "参数错误");
        	parameter.put("ResultData", resultData);
        	logger.info("invoke GatewayNetworkAccessSettingService.setLanAccess fail,参数错误:{}", parameter);
        	return parameter;
        }
        if(!validateDurationList(durationList)){
        	parameter.put(Constant.RESULT, -102);
        	Map<String, Object> resultData  = new HashMap<String, Object>();
        	resultData.put("FailReason", "参数错误");
        	parameter.put("ResultData", resultData);
        	logger.info("invoke GatewayNetworkAccessSettingService.setLanAccess fail,参数错误:{}", parameter);
        	return parameter;
        }
        if(queryGatewayInfoByMac(gatewayMacAddress) == null){
        	parameter.put("Result", -201);
        	Map<String, Object> resultData  = new HashMap<String, Object>();
        	resultData.put("FailReason", "网关MAC地址不存在");
        	parameter.put("ResultData", resultData);
        	logger.info("invoke GatewayNetworkAccessSettingService.setLanAccess fail,网关MAC地址不存在:{}", parameter);
        	return parameter;
		}
        Map<String, Object> retMap = InstructionUtil.getResultMap(parameter);
        //家长控制方式
        if("OFF".equals(NetAccessRight)){
        	
        	//1.创建家长控制过滤策略模板
        	int i = instructionMethodService.AddObject(gatewayMacAddress, "InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.Templates.", System.currentTimeMillis()+"");
        	if(i>0){
        		List<ParameterValueStruct> nameParamsList = new ArrayList<ParameterValueStruct>();
        		ParameterValueStruct pvs = new ParameterValueStruct();
        		pvs.setName("InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.Templates."+i+".Name");
        		pvs.setReadWrite(true);
        		pvs.setValue(gatewayMacAddress + (System.currentTimeMillis()/1000));
        		pvs.setValueType(ParameterValueStruct.Type_String);
        		nameParamsList.add(pvs);
        		ParameterValueStruct UrlFilterPolicypvs = new ParameterValueStruct();
        		UrlFilterPolicypvs.setName("InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.Templates."+i+".UrlFilterPolicy");
        		UrlFilterPolicypvs.setReadWrite(true);
        		UrlFilterPolicypvs.setValue(true);
        		UrlFilterPolicypvs.setValueType(ParameterValueStruct.Type_Boolean);
        		nameParamsList.add(UrlFilterPolicypvs);
        		ParameterValueStruct UrlFilterRightpvs = new ParameterValueStruct();
        		UrlFilterRightpvs.setName("InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.Templates."+i+".UrlFilterRight");
        		UrlFilterRightpvs.setReadWrite(true);
        		UrlFilterRightpvs.setValue(true);
        		UrlFilterRightpvs.setValueType(ParameterValueStruct.Type_Boolean);
        		nameParamsList.add(UrlFilterRightpvs);
        		boolean setvalueState = instructionMethodService.setParameterValue(gatewayMacAddress, nameParamsList);
        		if(setvalueState){
        			//2.儿童设备MAC地址列表
        			int macIndex = instructionMethodService.AddObject(gatewayMacAddress, "InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.MAC.", System.currentTimeMillis()+"");
        			if(macIndex > 0){
        				nameParamsList = new ArrayList<ParameterValueStruct>();
        				ParameterValueStruct MACAddressPvs = new ParameterValueStruct();
        				MACAddressPvs.setName("InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.MAC."+macIndex+".MACAddress");
        				MACAddressPvs.setReadWrite(true);
        				MACAddressPvs.setValue(DeviceMAC);
        				MACAddressPvs.setValueType(ParameterValueStruct.Type_String);
        				nameParamsList.add(MACAddressPvs);
        				ParameterValueStruct DescriptionPvs = new ParameterValueStruct();
        				DescriptionPvs.setName("InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.MAC."+macIndex+".Description");
        				DescriptionPvs.setReadWrite(true);
        				DescriptionPvs.setValue(DeviceMAC+"限制接入");
        				DescriptionPvs.setValueType(ParameterValueStruct.Type_String);
        				nameParamsList.add(DescriptionPvs);
        				ParameterValueStruct TemplateInstPvs = new ParameterValueStruct();
        				TemplateInstPvs.setName("InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.MAC."+macIndex+".TemplateInst");
        				TemplateInstPvs.setReadWrite(true);
        				TemplateInstPvs.setValue(i);
        				TemplateInstPvs.setValueType(ParameterValueStruct.Type_UnsignedInt);
        				nameParamsList.add(TemplateInstPvs);
        				boolean b = instructionMethodService.setParameterValue(gatewayMacAddress, nameParamsList);
        				if(b){
        					//3.模板内的上网时间段列表
        					boolean bl = true;
        					String durationPreNode = "InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.Templates."+i+".Duration.";
        					for(int j=0;j<durationList.size();j++){
        						Map<String,Object> duration = durationList.get(j);
        						int durationIndex = instructionMethodService.AddObject(gatewayMacAddress, durationPreNode, System.currentTimeMillis()+"");
        						if(durationIndex > 0){
        							nameParamsList = new ArrayList<ParameterValueStruct>();
        	        				ParameterValueStruct StartTimePvs = new ParameterValueStruct();
        	        				StartTimePvs.setName(durationPreNode + durationIndex + ".StartTime");
        	        				StartTimePvs.setReadWrite(true);
        	        				StartTimePvs.setValue(duration.get("StartTime").toString());
        	        				StartTimePvs.setValueType(ParameterValueStruct.Type_String);
        	        				nameParamsList.add(StartTimePvs);
        	        				ParameterValueStruct EndTimePvs = new ParameterValueStruct();
        	        				EndTimePvs.setName(durationPreNode+durationIndex+".EndTime");
        	        				EndTimePvs.setReadWrite(true);
        	        				EndTimePvs.setValue(duration.get("EndTime").toString());
        	        				EndTimePvs.setValueType(ParameterValueStruct.Type_String);
        	        				nameParamsList.add(EndTimePvs);
        	        				ParameterValueStruct RepeatDayPvs = new ParameterValueStruct();
        	        				RepeatDayPvs.setName(durationPreNode+durationIndex+".RepeatDay");
        	        				RepeatDayPvs.setReadWrite(true);
        	        				RepeatDayPvs.setValue(duration.get("RepeatDay").toString());
        	        				RepeatDayPvs.setValueType(ParameterValueStruct.Type_String);
        	        				nameParamsList.add(RepeatDayPvs);
        	        				boolean bo = instructionMethodService.setParameterValue(gatewayMacAddress, nameParamsList);
        	        				if(!bo){
        	        					logger.info("invoke GatewayNetworkAccessSettingService.setLanAccess fail,网关设置节点{}的参数失败", durationPreNode + durationIndex);
        	        					bl = false;
        	        					break;
        	        				}
        						}else{
        							logger.info("invoke GatewayNetworkAccessSettingService.setLanAccess fail,网关新增节点{}失败", durationPreNode);
        							bl = false;
        							break;
        						}
        					}
        					if(bl){
        						returnNetAccessRight = "OFF";
            					retMap.put("Result", 0);
        					}else{
        						returnNetAccessRight="ON";
            					retMap.put(Constant.RESULT, -400);
            					Map<String, Object> resultData  = new HashMap<String, Object>();
            		        	resultData.put("FailReason", "网关设置参数失败");
            		        	retMap.put("ResultData", resultData);
            		        	logger.info("end invoke GatewayNetworkAccessSettingService.setLanAccess:{}", retMap);
            					return retMap;
        					}
        				}else{
        					returnNetAccessRight="ON";
        					retMap.put(Constant.RESULT, -400);
        					Map<String, Object> resultData  = new HashMap<String, Object>();
        		        	resultData.put("FailReason", "网关设置参数失败");
        		        	retMap.put("ResultData", resultData);
        		        	logger.info("end invoke GatewayNetworkAccessSettingService.setLanAccess:{}", retMap);
        					return retMap;
        				}
        			}else{
        				returnNetAccessRight="ON";
        				retMap.put(Constant.RESULT, -400);
        				Map<String, Object> resultData  = new HashMap<String, Object>();
    		        	resultData.put("FailReason", "网关新增节点失败");
    		        	retMap.put("ResultData", resultData);
    		        	logger.info("end invoke GatewayNetworkAccessSettingService.setLanAccess:{}", retMap);
        				return retMap;
        			}
        		}else{
        			returnNetAccessRight="ON";
        			retMap.put(Constant.RESULT, -400);
        			Map<String, Object> resultData  = new HashMap<String, Object>();
		        	resultData.put("FailReason", "网关设置参数失败");
		        	retMap.put("ResultData", resultData);
		        	logger.info("end invoke GatewayNetworkAccessSettingService.setLanAccess:{}", retMap);
        			return retMap;
        		}
        	}else{
        		returnNetAccessRight="ON";
        		retMap.put(Constant.RESULT, -400);
        		Map<String, Object> resultData  = new HashMap<String, Object>();
	        	resultData.put("FailReason", "网关新增节点失败");
	        	retMap.put("ResultData", resultData);
	        	logger.info("end invoke GatewayNetworkAccessSettingService.setLanAccess:{}", retMap);
        		return retMap;
        	}
        }else if("ON".equals(NetAccessRight)){
        	Map<String,Object> map=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.MAC.", false);
        	String  wlanMacAddress ="InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.MAC.[0-9]+.MACAddress";
			
			List<String> list=new ArrayList<String>();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				if(entry.getKey().matches(wlanMacAddress)){
					list.add(entry.getKey().trim());
				}
			}
			Map<String, Object> valuesMap = instructionMethodService.getParameterValues(gatewayMacAddress, list);
			if(valuesMap.size() == 0){
				returnNetAccessRight = "ON";
				retMap.put("Result", 0);
			}else{
				
				for (Map.Entry<String, Object> entry : valuesMap.entrySet()) {
					if(DeviceMAC.equals(entry.getValue()==null?null:entry.getValue().toString())){
						//删除该节点
						int i = instructionMethodService.DeleteObject(gatewayMacAddress, entry.getKey().substring(0, entry.getKey().lastIndexOf(".")+1), System.currentTimeMillis()+"");
						if(i>-1){
							returnNetAccessRight = "ON";
							retMap.put("Result", 0);
						}else{
							returnNetAccessRight="OFF";
							retMap.put(Constant.RESULT, -400);
							Map<String, Object> resultData  = new HashMap<String, Object>();
				        	resultData.put("FailReason", "网关删除节点失败");
				        	retMap.put("ResultData", resultData);
				        	logger.info("end invoke GatewayNetworkAccessSettingService.setLanAccess:{}", retMap);
							return retMap;
						}
					}
				}
			}
        }
        
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("NetAccessRight", returnNetAccessRight);
        resultData.put("StorageAccessRight", StorageAccessRight);
        retMap.put(Constant.RESULTDATA, resultData);
        logger.info("end invoke GatewayNetworkAccessSettingService.setLanAccess:{}", retMap);
        return retMap;
	}
	private boolean validateDurationList(List<Map<String, Object>> durationList) {
		boolean isTrue = true;
		if(durationList != null && durationList.size() > 0){
			for(int i=0;i<durationList.size();i++){
	    		Map<String, Object> duration = durationList.get(i);
	    		if(null == duration.get("StartTime") || duration.get("StartTime").toString().equals("")){
	    			isTrue = false;
	    			break;
	    		}
	    		if(null == duration.get("EndTime") || duration.get("EndTime").toString().equals("")){
	    			isTrue = false;
	    			break;
	    		}
	    		if(null == duration.get("RepeatDay") || duration.get("RepeatDay").toString().equals("")){
	    			isTrue = false;
	    			break;
	    		}
	    	}
		}else{
			isTrue = false;
		}
		return isTrue;
		
	}
	private GatewayInfo queryGatewayInfoByMac(String macAddress){
		GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayMacaddress(macAddress);
        GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        return gatewayInfo;
	}
}
