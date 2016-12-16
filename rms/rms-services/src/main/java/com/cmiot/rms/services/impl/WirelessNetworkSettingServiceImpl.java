package com.cmiot.rms.services.impl;

import com.cmiot.rms.common.enums.PPPErrorCodeEnum;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.WirelessNetworkSettingService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by huqiao on 2016/05/06.
 */
public class WirelessNetworkSettingServiceImpl implements WirelessNetworkSettingService {
	private static Logger logger = LoggerFactory.getLogger(WirelessNetworkSettingServiceImpl.class);
	@Resource
	private InstructionMethodService instructionMethodService;
	@Resource
	private GatewayInfoService gatewayInfoService;

	@Override
	public Map<String, Object> setSSIDConfiguration(Map<String, Object> parameter) {
		//1.根据网关MAC查询网关ID
		Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
		String gatewayMacAddress = macMap.get(Constant.MAC) == null ? null :macMap.get(Constant.MAC).toString();
		parameter.remove(Constant.PARAMETER);
		parameter.remove(Constant.RPCMETHOD);
		if(gatewayMacAddress == null){
			logger.info("请求获取SSID信息时输入MAC地址为空");
			return commoFailedResonMap(-102,"MAC地址为空");
		}else{
			GatewayInfo gatewayInfo = new GatewayInfo();
			gatewayInfo.setGatewayMacaddress(gatewayMacAddress);
			GatewayInfo info = gatewayInfoService.selectGatewayInfo(gatewayInfo);
			if(info  == null){
				logger.info("请求获取SSID信息时输入MAC地址不存在");
				return commoFailedResonMap(-201,"网关MAC"+gatewayMacAddress+"不存在");
			}
		}
		//2.下发指令并获取结果
		/*Map<String,Object> map=instructionMethodService.getParameterNames(gatewayMacAddress, ConstantDiagnose.LANDEVICE, false);*/
		Map<String,Object> mapWlan=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.1.WLANConfiguration.", true);
		Map<String,Object> mapForGuest=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest.", true);
		if(mapWlan == null ){
			logger.info("网关MAC地址："+gatewayMacAddress+"请求获取SSIDX信息失败，原因：网关MAC地址不存在");
			logger.info(" InternetGatewayDevice.LANDevice.1.WLANConfiguration. ");
			return commoFailedResonMap(-201,"网关MAC地址："+gatewayMacAddress+"请求获取SSIDX信息失败，原因：网关MAC地址不存在");
		}else{
			logger.info(" mapWlan："+mapWlan.size());
		}
		if(mapForGuest == null ){
			logger.info("网关MAC地址："+gatewayMacAddress+"请求获取SSIDX信息失败，原因：网关MAC地址不存在");
			logger.info(" InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest. ");
			return commoFailedResonMap(-201,"网关MAC地址："+gatewayMacAddress+"请求获取SSIDX信息失败，原因：网关MAC地址不存在");
		}else{
			logger.info(" mapWlan："+mapWlan.size());
		}
		//3.组装list
		List<ParameterValueStruct> cloList = new ArrayList<>();
		List<String> getValueList = new ArrayList<>();
		String nodeWLANForGuest = "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest.[0-9]";
		String nodeWLANConfiguration = "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+macMap.get("SSIDIndex");
		String guest,gustIndex,hidden,sSID,beaconType,transmitPower,channel,enable,wEPKey,wEPKeyTmp = null,preSharedKey,keyPassphrase,wEPKeyIndex,preSharedKeyTmp = null,beaconTypeTempStr="";
		//str
		sSID = "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+macMap.get("SSIDIndex")+ ".SSID";
		beaconType = nodeWLANConfiguration + ".BeaconType"; //加密方式


		preSharedKey = nodeWLANConfiguration + ".PreSharedKey.1.PreSharedKey";
		keyPassphrase = nodeWLANConfiguration + ".PreSharedKey.1.KeyPassphrase";

		//boolean
		hidden = nodeWLANConfiguration + ".SSIDAdvertisementEnabled";
		logger.info(" hidden " + hidden);
		enable = nodeWLANConfiguration + ".Enable";
		//int

		transmitPower = nodeWLANConfiguration + ".TransmitPower";//PowerLevel
		channel = nodeWLANConfiguration + ".Channel";
		String AutoChannelEnable = nodeWLANConfiguration + ".AutoChannelEnable";
		wEPKeyIndex = nodeWLANConfiguration + ".WEPKeyIndex";
		gustIndex = "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest.[0-9].SSIDIndex";


		Map<String, Object> nameMapStr= new HashMap<>();
		if(!macMap.get("SSIDIndex").equals("0")){
			nameMapStr.put(sSID, macMap.get("SSID"));
		}

		String beaconTypeStr = "";
		int ssidIndex = 0;
		try{
			if(macMap.get("SSIDIndex")!= null&&macMap.get("SSIDIndex")!=""){
				ssidIndex = Integer.parseInt(String.valueOf(macMap.get("SSIDIndex")));
			}
		}catch (Exception e){
			String msg = "获取SSIDIndex信息时失败" + e.toString() ;
			logger.info(msg);
			return commoFailedResonMap(-102,msg);
		}

		if(ssidIndex != 0){
			/*nameMapStr.put(sSIDIndex, macMap.get("SSIDIndex"));*/
			if(null != macMap.get("ENCRYPT") && !"".equals(macMap.get("ENCRYPT")) && null != macMap.get("PWD") && !"".equals(macMap.get("PWD"))){
				if(macMap.get("ENCRYPT").equals("1")){
					beaconTypeStr="None";
				}else if(macMap.get("ENCRYPT").equals("2")){
					beaconTypeStr="Basic";
					//获取wEPKeyIndex
					int wepIndex = 1;
					List<String> cloListGetVal = new ArrayList<>();
					try{
						/*for (Map.Entry<String, Object> entry : mapForGuest.entrySet()) {
							if(entry.getKey().matches(wEPKeyIndex)){*/
								cloListGetVal.add(wEPKeyIndex);
							/*}
						}*/
						Map<String,Object> resultMapVal= new HashMap<>();
						resultMapVal = instructionMethodService.getParameterValues(gatewayMacAddress, cloListGetVal);
						logger.info(" resultMapVal " + String.valueOf(resultMapVal.size()));
						if(resultMapVal.size() > 0){
							for (Map.Entry<String, Object> entryVal : resultMapVal.entrySet()) {
								if (entryVal.getKey().matches(wEPKeyIndex)) {
									wepIndex = (int) entryVal.getValue();
									logger.info(" wepIndex " + wepIndex);
								}
							}
						}else{
							logger.info(" wEPKeyIndex is null!");
						}

					}catch (Exception e){
						e.printStackTrace();
					}
					wEPKey = nodeWLANConfiguration + ".WEPKey."+wepIndex+".WEPKey";
					logger.info(" wEPKey " + wEPKey);
					nameMapStr.put(wEPKey, macMap.get("PWD"));
				}else if(macMap.get("ENCRYPT").equals("3")){
					beaconTypeStr="WPA";
					nameMapStr.put(preSharedKey, macMap.get("PWD"));
					nameMapStr.put(keyPassphrase, macMap.get("PWD"));
				}else if(macMap.get("ENCRYPT").equals("4")){
					beaconTypeStr="WPA2";
					nameMapStr.put(preSharedKey, macMap.get("PWD"));
					nameMapStr.put(keyPassphrase, macMap.get("PWD"));
				}else if(macMap.get("ENCRYPT").equals("5")){
					beaconTypeStr="WPA/WPA2";
					nameMapStr.put(preSharedKey, macMap.get("PWD"));
					nameMapStr.put(keyPassphrase, macMap.get("PWD"));
				}
				if(null != macMap.get("ENCRYPT") && !"".equals(macMap.get("ENCRYPT")) && null != macMap.get("PWD") && !"".equals(macMap.get("PWD"))){
					logger.info(" beaconType is not null!" );
					nameMapStr.put(beaconType, beaconTypeStr);
				}
			}
		}
		Map<String, Object> nameMapInt= new HashMap<>();
		if(ssidIndex != 0) {
			nameMapInt.put(transmitPower, macMap.get("PowerLevel"));
			nameMapInt.put(channel, macMap.get("Channel"));
		}
		Map<String, Object> nameMapBoolHidden= new HashMap<>();

		Map<String, Object> nameMapBoolEn= new HashMap<>();
		/*0表示不启用，1表示启用*/
		if(macMap.get("Enable") != null&& !"".equals( macMap.get("Enable"))){
			if("1".equals(macMap.get("Enable"))){
				nameMapBoolEn.put(enable, true);
			}else if("0".equals(macMap.get("Enable"))){
				nameMapBoolEn.put(enable, false);
			}
			if(macMap.get("Channel") != null && "0".equals(macMap.get("Channel").toString())){
				nameMapBoolEn.put(AutoChannelEnable, true);
			}else{
				nameMapBoolEn.put(AutoChannelEnable, false);
			}

		}
		if(ssidIndex != 0) {
			if (macMap.get("Hidden") != null &&  !"".equals(macMap.get("Hidden"))) {
				if ("0".equals(macMap.get("Hidden"))) {
					nameMapBoolHidden.put(hidden, true);
				}
				if ("1".equals(macMap.get("Hidden"))) {
					nameMapBoolHidden.put(hidden, false);
				}
			}
		}
		//对设备属性进行遍历
		/*int enableCount = 0;
		StringBuffer sb =new StringBuffer();*/
		/*for (Map.Entry<String, Object> entry : map.entrySet()) {
			*//*logger.info(" ~~ entry ~~ " + entry.getKey());*//*
			if(entry.getKey().matches(hidden)){
				logger.info(" get hidden " + hidden);
			}
			if(entry.getKey().equals(hidden)){
				logger.info(" get equals hidden " + hidden);
			}*/

			/*if(ssidIndex == 0){
				if (entry.getKey().matches("InternetGatewayDevice.LANDevice.1.WLANConfiguration.[1-9].Enable")){
					enableCount = enableCount + 1;
					sb.append(entry.getKey() + "::");
				}
			}*/
			for (Map.Entry<String, Object> entry1 : nameMapStr.entrySet()) {
				ParameterValueStruct<String> pvs = new ParameterValueStruct<String>();
					pvs.setName(entry1.getKey());
					pvs.setValue((String) entry1.getValue());
					pvs.setReadWrite(true);
					pvs.setValueType("string");
					cloList.add(pvs);

			}
			if(ssidIndex != 0){
				for (Map.Entry<String, Object> entry2 : nameMapBoolEn.entrySet()) {
					    ParameterValueStruct pvs = new ParameterValueStruct();
						pvs.setName(entry2.getKey());
						pvs.setValue(entry2.getValue());
						pvs.setReadWrite(true);
						pvs.setValueType("boolean");
						cloList.add(pvs);
				}
			}
			if(ssidIndex != 0){
				for (Map.Entry<String, Object> entry3 : nameMapInt.entrySet()) {
					ParameterValueStruct pvs = new ParameterValueStruct();
						pvs.setName(entry3.getKey());
						pvs.setValue(entry3.getValue());
						pvs.setReadWrite(true);
						pvs.setValueType("unsignedInt");
						cloList.add(pvs);
				}
			}

		if(ssidIndex !=0 ){
			if(mapForGuest.size() == 0){
				if(macMap.get("Guest").equals("1")){
					logger.info(" begin create node... ");
					int index = instructionMethodService.AddObject(gatewayMacAddress,
							"InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest.", System.currentTimeMillis() + "");
					if (index > 0) {
						logger.info("create node success " + "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + macMap.get("SSIDIndex"));
					} else {
						logger.info("create node failed " + "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + macMap.get("SSIDIndex"));
					}
					ParameterValueStruct pvs = new ParameterValueStruct();
					pvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + index + ".SSIDIndex");
					pvs.setValue(Integer.parseInt(String.valueOf(macMap.get("SSIDIndex"))));
					pvs.setReadWrite(true);
					pvs.setValueType("unsignedInt");
					cloList.add(pvs);
				}
			}else{
				if(macMap.get("Guest").equals("1")){
					for (Map.Entry<String, Object> entryGuest : mapForGuest.entrySet()){
						if(entryGuest.getKey().matches("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest.[1-9].")){
							ParameterValueStruct pvs = new ParameterValueStruct();
							pvs.setName(entryGuest.getKey() + "SSIDIndex");
							pvs.setValue(Integer.parseInt(String.valueOf(macMap.get("SSIDIndex"))));
							pvs.setReadWrite(true);
							pvs.setValueType("unsignedInt");
							cloList.add(pvs);
						}
					}
				}else if(macMap.get("Guest").equals("0")){
					for (Map.Entry<String, Object> entryGuest : mapForGuest.entrySet()){
							if(entryGuest.getKey().matches("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest.[1-9].")){
							logger.info(" begin delete node... " + "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + macMap.get("SSIDIndex"));
							int index = instructionMethodService.DeleteObject(gatewayMacAddress,entryGuest.getKey(), System.currentTimeMillis() + "");
							if (index > 0) {
								logger.info("delete node success " + "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + macMap.get("SSIDIndex"));
							} else {
								logger.info("delete node failed " + "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + macMap.get("SSIDIndex"));
							}
						}
					}
				}
			}
		}
		/*	for (Map.Entry<String, Object> entryGuest : mapForGuest.entrySet()) {//是否为访客判断*/
		/*if (ssidIndex != 0) {
			List<String> cloListForGust = new ArrayList<>();
			Map<String, Object> resultMap = new HashMap<>();
			if (entryGuest.getKey().matches("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest.[1-9].")) {
				cloListForGust.add(entryGuest.getKey() + ".SSIDIndex" );
				*//*logger.info(" cloListForGust "+cloListForGust.toString());*//*
				resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, cloListForGust);//区验证节点是否存在来确定是否需要添加删除节点
				if (resultMap != null) { //存在节点时
					for (Map.Entry<String, Object> entryGust : resultMap.entrySet()) {
						*//*logger.info(" entryGust "+entryGust.getKey());*//*
						if (entryGust.getKey().equals(entryGuest.getKey())) {
							if (macMap.get("Guest").equals("1")) {
								ParameterValueStruct pvs = new ParameterValueStruct();
								pvs.setName(entryGuest.getKey());
								pvs.setValue(Integer.parseInt(String.valueOf(macMap.get("SSIDIndex"))));
								pvs.setReadWrite(true);
								pvs.setValueType("unsignedInt");
								cloList.add(pvs);
							} else if (macMap.get("Guest").equals("0")) {
								logger.info(" begin delete node... " + "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + macMap.get("SSIDIndex"));
								int index = instructionMethodService.DeleteObject(gatewayMacAddress,
										"InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + macMap.get("SSIDIndex"), System.currentTimeMillis() + "");
								if (index > 0) {
									logger.info("delete node success " + "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + macMap.get("SSIDIndex"));
								} else {
									logger.info("delete node failed " + "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + macMap.get("SSIDIndex"));
								}
							}
						}
					}
				} else {//不存在节点时
					if (macMap.get("Guest").equals("1")) {//创建ssidindex节点
						logger.info(" begin create node... ");
						int index = instructionMethodService.AddObject(gatewayMacAddress,
								"InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + macMap.get("SSIDIndex"), System.currentTimeMillis() + "");
						if (index > 0) {
							logger.info("create node success " + "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + macMap.get("SSIDIndex"));
						} else {
							logger.info("create node failed " + "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + macMap.get("SSIDIndex"));
						}
					}
				}
			}
		}*/
	/*}*/

		/*0表示不启用，1表示启用*/
		if(ssidIndex == 0){
			for (Map.Entry<String, Object> entryWlan : mapWlan.entrySet()) {
				logger.info(" entryWlan: " + entryWlan.getKey());
				ParameterValueStruct pvs = new ParameterValueStruct();
				if(entryWlan.getKey().matches("InternetGatewayDevice.LANDevice.1.WLANConfiguration.[1-9].")){
					pvs.setName(entryWlan.getKey() + "Enable");
					if (macMap.get("Enable").equals("1")) {
						pvs.setValue(true);
					}
					if (macMap.get("Enable").equals("0")) {
						pvs.setValue(false);
					}
					pvs.setReadWrite(true);
					pvs.setValueType("string");
					cloList.add(pvs);
				}
			}
		}

		if(ssidIndex != 0) {
			ParameterValueStruct pvs = new ParameterValueStruct();
			pvs.setName(hidden);
			if (macMap.get("Hidden") != null && macMap.get("Hidden") != "") {
				if (macMap.get("Hidden").equals("0")) {
					pvs.setValue(true);
				}
				if (macMap.get("Hidden").equals("1")) {
					pvs.setValue(false);
				}
			}
			pvs.setReadWrite(true);
			pvs.setValueType("boolean");
			cloList.add(pvs);
		}

		/*logger.info(" node enable " + sb.toString() + " enableCount " + enableCount);*/
		logger.info(" ~cloList "+cloList.size());
		//4.指令设置
		if(cloList.size()>0){
			for(int i=0;i<cloList.size();i++){
				logger.info("cloListName:::"+cloList.get(i).getName() + ":::cloListVal:::" + cloList.get(i).getValue());
			}
			boolean re = instructionMethodService.setParameterValue(gatewayMacAddress, cloList);
			logger.info(" ~re "+re);
			parameter.put(Constant.RESULT, ErrorCodeEnum.SUCCESS.getResultCode());
			parameter.put(Constant.RESULTDATA,"{}");
			return parameter;
		}else{
			String msg = "网关MAC地址："+gatewayMacAddress+"请求设置SSID失败，原因：没有找到对应的SSID信息";
			logger.info(msg);
			parameter.put(Constant.RESULT, -102);
			parameter.put(Constant.FAILREASON, msg);
			return commoFailedResonMap(-102,msg);
		}
	}

	@Override
	public Map<String, Object> getSSIDInfo(Map<String, Object> parameter) {
		logger.info("begin get ssidinfo");
		//1.根据网关MAC查询网关ID
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = macMap.get(Constant.MAC) == null ? null :macMap.get(Constant.MAC).toString();
		parameter.remove(Constant.PARAMETER);
		parameter.remove(Constant.RPCMETHOD);
        if(gatewayMacAddress == null){
			String msg = "请求获取SSID信息时输入MAC地址为空";
        	logger.info(msg);
			return commoFailedResonMap(-102,msg);
        }else{
			GatewayInfo gatewayInfo = new GatewayInfo();
			gatewayInfo.setGatewayMacaddress(gatewayMacAddress);
			GatewayInfo info = gatewayInfoService.selectGatewayInfo(gatewayInfo);
			if(info  == null){
				String msg = "请求获取SSID信息时输入MAC地址"+gatewayMacAddress+"不存在";
				logger.info(msg);
				return commoFailedResonMap(-201,msg);
			}
		}
        //2.下发指令并获取结果 
        /*Map<String,Object> map=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);*/
		Map<String,Object> map=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.1.WLANConfiguration.", true);
		Map<String,Object> mapForGuest=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest.", true);
		/*	Map<String,Object> mapForShare=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANShare.", true);*/

		if(map == null ){
			String msg ="网关MAC地址："+gatewayMacAddress+"请求获取SSIDX信息失败，原因：网关MAC地址不存在";
        	logger.info(msg);
			return commoFailedResonMap(-201,msg);
        }else{
			logger.info(" map " + map.size());
		}

		/*if(mapForGuest == null ){
			String msg ="请求获取 InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest. 信息失败，原因：网关MAC地址不存在";
			logger.info(msg);
			return commoFailedResonMap(-201,msg);
		}else{
			logger.info(" map " + mapForGuest.size());
		}

		if(mapForShare == null ){
			String msg ="请求获取 InternetGatewayDevice.LANDevice.1.X_CMCC_WLANShare. 信息失败，原因：网关MAC地址不存在";
			logger.info(msg);
			return commoFailedResonMap(-201,msg);
		}else{
			logger.info(" map " + mapForShare.size());
		}*/

		//验证map中所有相对应的属性
		List<String> cloList = new ArrayList<>();
		String ssidIndex = (String) macMap.get("SSIDIndex");
		String nodeHeaderSI = "InternetGatewayDevice.LANDevice.1.WLANConfiguration." + ssidIndex;
		String attributeBeaconType = null ,attributeSSID = null ,attributeTransmitPower = null ,attributeChannel = null ,attributeEnable = null ,attributeSSIDindex = null,
				attributeGuest=null, attributeHidden=null,wEPKey =null,preSharedKey = null,wEPKey1;
		if(macMap.get("SSIDIndex")!=null){

			if("0".equalsIgnoreCase(ssidIndex)) {
				for (Map.Entry<String, Object> entryForGuest : mapForGuest.entrySet()) {
					logger.info(" entryForGuest " + entryForGuest.getKey() + " entryForValue " + entryForGuest.getValue());
					if ("0".equalsIgnoreCase(ssidIndex)) {
						attributeGuest = "InternetGatewayDevice.LANDevice.[1-9].X_CMCC_WLANForGuest.[1-9]";
						if (entryForGuest.getKey().matches(attributeGuest)) {
							cloList.add(entryForGuest.getKey()+".SSIDIndex");
						}
					} else {
						attributeGuest = "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + ssidIndex + ".SSIDIndex";
						cloList.add(entryForGuest.getKey());
					}
				}
/*
				for (Map.Entry<String, Object> entryForShare : mapForShare.entrySet()) {
					logger.info(" entryForShare " + entryForShare.getKey() + " entryForValue " + entryForShare.getValue());
					if ("0".equalsIgnoreCase(ssidIndex)) {
						attributeSSIDindex = "InternetGatewayDevice.LANDevice.[1-9].X_CMCC_WLANShare.[1-9]";
						if (entryForShare.getKey().matches(attributeSSIDindex)) {
							cloList.add(entryForShare.getKey()+".SSIDIndex");
						}
					} else {
						attributeGuest = "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + ssidIndex + ".SSIDIndex";
							cloList.add(entryForShare.getKey());
					}
				}*/
			}
			//对设备属性进行遍历
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				logger.info(" map " + entry.getKey());
				logger.info(String.valueOf(entry.getKey().equals("InternetGatewayDevice.LANDevice.1.WLANConfiguration.5.")));
				  if("0".equalsIgnoreCase(ssidIndex)){
					  if(entry.getKey().matches("InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.")){
						 /* cloList.add(entry.getKey() + "BeaconType");*/
						  cloList.add(entry.getKey() + "SSID");
						  cloList.add(entry.getKey() + "TransmitPower");
						  cloList.add(entry.getKey() + "Channel");
						  cloList.add(entry.getKey() + "Enable");
						  cloList.add(entry.getKey() + "SSIDAdvertisementEnabled");
					  }else if(entry.getKey().matches("InternetGatewayDevice.LANDevice.1.WLANConfiguration.2.")){
						 /* cloList.add(entry.getKey() + "BeaconType");*/
						  cloList.add(entry.getKey() + "SSID");
						  cloList.add(entry.getKey() + "TransmitPower");
						  cloList.add(entry.getKey() + "Channel");
						  cloList.add(entry.getKey() + "Enable");
						  cloList.add(entry.getKey() + "SSIDAdvertisementEnabled");
					  }else if(entry.getKey().equals("InternetGatewayDevice.LANDevice.1.WLANConfiguration.5.")){
						 /* cloList.add(entry.getKey() + "BeaconType");*/
						  cloList.add(entry.getKey() + "SSID");
						  cloList.add(entry.getKey() + "TransmitPower");
						  cloList.add(entry.getKey() + "Channel");
						  cloList.add(entry.getKey() + "Enable");
						  cloList.add(entry.getKey() + "SSIDAdvertisementEnabled");
					  }
				  }
				 }
			if(!"0".equalsIgnoreCase(ssidIndex)){
				/*attributeBeaconType = nodeHeaderSI+ ".BeaconType";*/
				attributeSSID = nodeHeaderSI+ ".SSID";
				attributeTransmitPower =nodeHeaderSI+ ".TransmitPower";
				attributeChannel = nodeHeaderSI+ ".Channel";
				attributeEnable =  nodeHeaderSI+ ".Enable";
				/*attributeSSIDindex= "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANShare." + ssidIndex + ".SSIDIndex";*/
				attributeHidden = nodeHeaderSI + ".SSIDAdvertisementEnabled";
				/*attributeGuest = "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest."+ssidIndex+".SSIDIndex";*/
				/*wEPKey = nodeHeaderSI + ".WEPKey.1.WEPKey";
				wEPKey1 = nodeHeaderSI + ".WEPKey.2.WEPKey";
				preSharedKey = nodeHeaderSI + ".PreSharedKey.1.PreSharedKey";*/

				cloList.add(attributeSSID);
				cloList.add(attributeTransmitPower);
				cloList.add(attributeChannel);
				cloList.add(attributeEnable);
				/*if(mapForShare.size()>0) {
					cloList.add(attributeSSIDindex);
				}*/
				cloList.add(attributeHidden);
				if(mapForGuest.size()>0){
					cloList.add(attributeGuest);
				}
			}
		}
		List<Map<String, Object>> wifiList = new ArrayList<>();
		Map<String,Object> WIFIList = new HashMap<>();
		if(cloList.size()>0){
			for(int i =0; i<cloList.size(); i++){
				logger.info(" cloList " + cloList.get(i).toString());
			}
			Map<String,Object> resultMap= new HashMap<>();
        	resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, cloList);
        	Map<String,Object> wifiInfoElement1= new HashMap<>();
			Map<String,Object> wifiInfoElement2= new HashMap<>();
			Map<String,Object> wifiInfoElement5= new HashMap<>();
			Map<String,Object> wifiInfoElementTemp = new HashMap<>();

			Map<String,Object> wifiInfo= new HashMap<>();
			if(!"0".equalsIgnoreCase(ssidIndex)){
				wifiInfo.put("SSIDIndex", String.valueOf(ssidIndex));
				for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
					if (entry.getKey().equals(attributeSSID)) {wifiInfo.put("SSID", entry.getValue());}
					if (entry.getKey().equals(attributeTransmitPower)) {wifiInfo.put("PowerLevel", String.valueOf(entry.getValue()));}
					if (entry.getKey().equals(attributeChannel)) {wifiInfo.put("Channel", String.valueOf(entry.getValue()));}
					if (entry.getKey().equals(attributeHidden)) {
						logger.info(" attributeHidden " + entry.getValue());
						if(entry.getValue().toString().equals("true")){
							wifiInfo.put("Hidden", "0");
						}else{
							wifiInfo.put("Hidden", "1");
						}
					}
					String enableVal = "0";
					if (entry.getKey().equals(attributeEnable)) {
						boolean enableBoo = (boolean) entry.getValue();
						enableVal = enableBoo == true ? "1":"0";
						wifiInfo.put("Enable", enableVal);
					}
					if (entry.getKey().equals(attributeGuest)){wifiInfo.put("Guest", "1");}else{
						wifiInfo.put("Guest", "0");
					}

				}
				wifiList.add(wifiInfo);
				WIFIList.put("WIFIList", wifiList);
			}else{
				wifiList = new ArrayList<>();
				for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
					   logger.info("entry.getKey " + entry.getKey() + " value " + entry.getValue());
					    String indexNum ="1";
						String subStr;
						if (entry.getKey().matches("InternetGatewayDevice.LANDevice.1.WLANConfiguration.[1-5].*")) {
							subStr =  entry.getKey().substring(0,entry.getKey().lastIndexOf("."));
							/*logger.info(" subStr " +subStr);*/
							indexNum = subStr.substring(subStr.lastIndexOf(".") + 1);
							/*logger.info(" indexNum " +indexNum);*/
						}
						if(indexNum.equals("1")){
							wifiInfoElementTemp = wifiInfoElement1;
						}else if(indexNum.equals("2")){
							wifiInfoElementTemp = wifiInfoElement2;
						}else if(indexNum.equals("5")){
							wifiInfoElementTemp = wifiInfoElement5;
						}
						String ssidIndex1 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+indexNum+".SSID";
						String  hidden1 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+indexNum+".SSIDAdvertisementEnabled";
						wifiInfoElementTemp.put("SSIDIndex", indexNum);
						if(entry.getKey().matches(ssidIndex1)){
							wifiInfoElementTemp.put("SSID", entry.getValue());}
						if(entry.getKey().matches("InternetGatewayDevice.LANDevice.1.WLANConfiguration."+indexNum +".TransmitPower")){
							wifiInfoElementTemp.put("PowerLevel", String.valueOf(entry.getValue()));}
						if(entry.getKey().matches( "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+indexNum +".Channel")){
							wifiInfoElementTemp.put("Channel", String.valueOf(entry.getValue()));}
						if (entry.getKey().equals(hidden1)) {
							if(entry.getValue().toString().equals("true")){
								wifiInfoElementTemp.put("Hidden", "0");
							}else{
								wifiInfoElementTemp.put("Hidden", "1");
							}
						}
						String enableVal;
						if (entry.getKey().matches( "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+indexNum +".Enable")) {
							boolean enableBoo = (boolean) entry.getValue();
							enableVal = enableBoo == true ? "1":"0";
							wifiInfoElementTemp.put("Enable", enableVal);
						}
						if (entry.getKey().matches("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + indexNum + ".SSIDIndex")){wifiInfoElement1.put("Guest", "1");}else{
							wifiInfoElementTemp.put("Guest", "0");
						}

					}
				if(wifiInfoElement1.size() > 0){wifiList.add(wifiInfoElement1);}
				if(wifiInfoElement2.size() > 0){wifiList.add(wifiInfoElement2);}
				if(wifiInfoElement5.size() > 0){wifiList.add(wifiInfoElement5);}

				WIFIList.put("WIFIList", wifiList);
				}


        }else{
			String msg = "网关MAC地址："+gatewayMacAddress+"请求获取SSID失败，原因：没有找到对应的SSID信息";
			return commoFailedResonMap(-102,msg);
        }
		try {
			parameter.put(Constant.RESULT, ErrorCodeEnum.SUCCESS.getResultCode());
			parameter.put("ResultData", WIFIList);
			return parameter;
		} catch (Exception e) {
			e.printStackTrace();
			return commonExAndMap("组装SSIDInfo出错！");
		}
	}
	@Override
	public Map<String, Object> getWifiSSIDInfo(Map<String, Object> parameter) {
		logger.info("begin get getWifiSSIDInfo");
		//1.根据网关MAC查询网关ID
		Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
		String gatewayMacAddress = macMap.get(Constant.MAC) == null ? null :macMap.get(Constant.MAC).toString();
		parameter.remove(Constant.PARAMETER);
		parameter.remove(Constant.RPCMETHOD);
		if(gatewayMacAddress == null){
			String msg = "请求获取SSID信息时输入MAC地址为空";
			logger.info(msg);
			return commoFailedResonMap(-102,msg);
		}else{
			GatewayInfo gatewayInfo = new GatewayInfo();
			gatewayInfo.setGatewayMacaddress(gatewayMacAddress);
			GatewayInfo info = gatewayInfoService.selectGatewayInfo(gatewayInfo);
			if(info  == null){
				String msg = "请求获取SSID信息时输入MAC地址"+gatewayMacAddress+"不存在";
				logger.info(msg);
				return commoFailedResonMap(-201,msg);
			}
		}
		//2.下发指令并获取结果
		Map<String,Object> map=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.1.WLANConfiguration.", true);
		Map<String,Object> mapForGuest=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest.", true);

		if(map == null ){
			String msg ="网关MAC地址："+gatewayMacAddress+"请求获取SSIDX信息失败，原因：网关MAC地址不存在";
			logger.info(msg);
			return commoFailedResonMap(-201,msg);
		}else{
			logger.info(" map " + map.size());
		}

		//验证map中所有相对应的属性
		List<String> cloList = new ArrayList<>();
		String ssidIndex = (String) macMap.get("SSIDIndex");
		String nodeHeaderSI = "InternetGatewayDevice.LANDevice.1.WLANConfiguration." + ssidIndex;
		String attributeBeaconType = null ,
			   attributeSSID = null ,
			   attributeTransmitPower = null ,
			   attributeChannel = null ,
			   channelsInUse = null ,
			   attributeEnable = null ,
			   attributeGuest=null,
			   attributeHidden=null;
		if(macMap.get("SSIDIndex")!=null){

			if("0".equalsIgnoreCase(ssidIndex)) {
				for (Map.Entry<String, Object> entryForGuest : mapForGuest.entrySet()) {
					logger.info(" entryForGuest " + entryForGuest.getKey() + " entryForValue " + entryForGuest.getValue());
					attributeGuest = "InternetGatewayDevice.LANDevice.[1-9].X_CMCC_WLANForGuest.[1-9]";
					if (entryForGuest.getKey().matches(attributeGuest)) {
						cloList.add(entryForGuest.getKey()+".SSIDIndex");
					}
				}
			}
			//对设备属性进行遍历
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				logger.info(" map " + entry.getKey());
				logger.info(String.valueOf(entry.getKey().equals("InternetGatewayDevice.LANDevice.1.WLANConfiguration.5.")));
				if("0".equalsIgnoreCase(ssidIndex)){
					if(entry.getKey().matches("InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.")){
						cloList.add(entry.getKey() + "BeaconType");
						cloList.add(entry.getKey() + "SSID");
						cloList.add(entry.getKey() + "TransmitPower");
						cloList.add(entry.getKey() + "Channel");
						cloList.add(entry.getKey() + "Enable");
						cloList.add(entry.getKey() + "SSIDAdvertisementEnabled");
					}else if(entry.getKey().matches("InternetGatewayDevice.LANDevice.1.WLANConfiguration.2.")){
						cloList.add(entry.getKey() + "BeaconType");
						cloList.add(entry.getKey() + "SSID");
						cloList.add(entry.getKey() + "TransmitPower");
						cloList.add(entry.getKey() + "Channel");
						cloList.add(entry.getKey() + "Enable");
						cloList.add(entry.getKey() + "SSIDAdvertisementEnabled");
					}else if(entry.getKey().equals("InternetGatewayDevice.LANDevice.1.WLANConfiguration.5.")){
						cloList.add(entry.getKey() + "BeaconType");
						cloList.add(entry.getKey() + "SSID");
						cloList.add(entry.getKey() + "TransmitPower");
						cloList.add(entry.getKey() + "Channel");
						cloList.add(entry.getKey() + "Enable");
						cloList.add(entry.getKey() + "SSIDAdvertisementEnabled");
					}
				}
			}
			if(!"0".equalsIgnoreCase(ssidIndex)){
				attributeBeaconType = nodeHeaderSI+ ".BeaconType";
				attributeSSID = nodeHeaderSI+ ".SSID";
				attributeTransmitPower =nodeHeaderSI+ ".TransmitPower";
				attributeChannel = nodeHeaderSI+ ".Channel";
				channelsInUse = nodeHeaderSI+ ".ChannelsInUse";
				attributeEnable =  nodeHeaderSI+ ".Enable";
				attributeHidden = nodeHeaderSI + ".SSIDAdvertisementEnabled";

				cloList.add(attributeSSID);
				cloList.add(attributeTransmitPower);
				cloList.add(attributeChannel);
				cloList.add(channelsInUse);
				cloList.add(attributeEnable);
				cloList.add(attributeBeaconType);
				cloList.add(attributeHidden);
				if(mapForGuest.size()>0){
					cloList.add(attributeGuest);
				}
			}
		}
		List<Map<String, Object>> wifiList = new ArrayList<>();
		Map<String,Object> WIFIList = new HashMap<>();
		if(cloList.size()>0){
			for(int i =0; i<cloList.size(); i++){
				logger.info(" cloList " + cloList.get(i).toString());
			}
			Map<String,Object> resultMap= new HashMap<>();
			resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, cloList);
			Map<String,Object> wifiInfoElement1= new HashMap<>();
			Map<String,Object> wifiInfoElement2= new HashMap<>();
			Map<String,Object> wifiInfoElement5= new HashMap<>();
			Map<String,Object> wifiInfoElementTemp = new HashMap<>();

			Map<String,Object> wifiInfo= new HashMap<>();
			if(!"0".equalsIgnoreCase(ssidIndex)){
				wifiInfo.put("SSIDIndex", String.valueOf(ssidIndex));
				for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
					if (entry.getKey().equals(attributeSSID)) {wifiInfo.put("SSID", entry.getValue());}
					if (entry.getKey().equals(attributeTransmitPower)) {wifiInfo.put("PowerLevel", String.valueOf(entry.getValue()));}
					if (entry.getKey().equals(attributeChannel)) {
						if("0".equals(String.valueOf(entry.getValue()))){
							//自动获取信道
							wifiInfo.put("Channel", String.valueOf(resultMap.get(channelsInUse)));
						}else{
							wifiInfo.put("Channel", String.valueOf(entry.getValue()));
						}

					}
					if (entry.getKey().equals(attributeHidden)) {
						logger.info(" attributeHidden " + entry.getValue());
						if(entry.getValue().toString().equals("true")){
							wifiInfo.put("Hidden", "0");
						}else{
							wifiInfo.put("Hidden", "1");
						}
					}
					String enableVal = "0";
					if (entry.getKey().equals(attributeEnable)) {
						boolean enableBoo = (boolean) entry.getValue();
						enableVal = enableBoo == true ? "1":"0";
						wifiInfo.put("Enable", enableVal);
					}
					if (entry.getKey().equals(attributeGuest)){wifiInfo.put("Guest", "1");}else{
						wifiInfo.put("Guest", "0");
					}
					if (entry.getKey().equals(attributeBeaconType)){
						if("None".equals(entry.getValue())){
							wifiInfo.put("PWD", "");
							wifiInfo.put("ENCRYPT", "1");
						}else if("Basic".equals(entry.getValue())){
							wifiInfo.put("PWD", "");
							wifiInfo.put("ENCRYPT", "2");
						}else if("WPA".equals(entry.getValue())){
							wifiInfo.put("PWD", "");
							wifiInfo.put("ENCRYPT", "3");
						}else if("WPA2".equals(entry.getValue())){
							wifiInfo.put("PWD", "");
							wifiInfo.put("ENCRYPT", "4");
						}else{
							wifiInfo.put("PWD", "");
							wifiInfo.put("ENCRYPT", "5");
						}
					}
				}
				wifiList.add(wifiInfo);
				WIFIList.put("WIFIList", wifiList);
			}else{
				wifiList = new ArrayList<>();
				for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
					logger.info("entry.getKey " + entry.getKey() + " value " + entry.getValue());
					String indexNum ="1";
					String subStr;
					if (entry.getKey().matches("InternetGatewayDevice.LANDevice.1.WLANConfiguration.[1-5].*")) {
						subStr =  entry.getKey().substring(0,entry.getKey().lastIndexOf("."));
						/*logger.info(" subStr " +subStr);*/
						indexNum = subStr.substring(subStr.lastIndexOf(".") + 1);
						/*logger.info(" indexNum " +indexNum);*/
					}
					if(indexNum.equals("1")){
						wifiInfoElementTemp = wifiInfoElement1;
					}else if(indexNum.equals("2")){
						wifiInfoElementTemp = wifiInfoElement2;
					}else if(indexNum.equals("5")){
						wifiInfoElementTemp = wifiInfoElement5;
					}
					String ssidIndex1 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+indexNum+".SSID";
					String  hidden1 = "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+indexNum+".SSIDAdvertisementEnabled";
					wifiInfoElementTemp.put("SSIDIndex", indexNum);
					if(entry.getKey().matches(ssidIndex1)){
						wifiInfoElementTemp.put("SSID", entry.getValue());}
					if(entry.getKey().matches("InternetGatewayDevice.LANDevice.1.WLANConfiguration."+indexNum +".TransmitPower")){
						wifiInfoElementTemp.put("PowerLevel", String.valueOf(entry.getValue()));}
					if(entry.getKey().matches( "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+indexNum +".Channel")){
						if("0".equals(String.valueOf(entry.getValue()))){
							//自动获取信道
							wifiInfoElementTemp.put("Channel", String.valueOf(resultMap.get("InternetGatewayDevice.LANDevice.1.WLANConfiguration."+indexNum +".ChannelsInUse")));
						}else{
							wifiInfoElementTemp.put("Channel", String.valueOf(entry.getValue()));
						}

					}
					if (entry.getKey().equals(hidden1)) {
						if(entry.getValue().toString().equals("true")){
							wifiInfoElementTemp.put("Hidden", "0");
						}else{
							wifiInfoElementTemp.put("Hidden", "1");
						}
					}
					String enableVal;
					if (entry.getKey().matches( "InternetGatewayDevice.LANDevice.1.WLANConfiguration."+indexNum +".Enable")) {
						boolean enableBoo = (boolean) entry.getValue();
						enableVal = enableBoo == true ? "1":"0";
						wifiInfoElementTemp.put("Enable", enableVal);
					}
					if (entry.getKey().matches("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANForGuest." + indexNum + ".SSIDIndex")){wifiInfoElement1.put("Guest", "1");}else{
						wifiInfoElementTemp.put("Guest", "0");
					}
					if (entry.getKey().equals(attributeBeaconType)){
						if("None".equals(entry.getValue())){
							wifiInfoElementTemp.put("PWD", "");
							wifiInfoElementTemp.put("ENCRYPT", "1");
						}else if("Basic".equals(entry.getValue())){
							wifiInfoElementTemp.put("PWD", "");
							wifiInfoElementTemp.put("ENCRYPT", "2");
						}else if("WPA".equals(entry.getValue())){
							wifiInfoElementTemp.put("PWD", "");
							wifiInfoElementTemp.put("ENCRYPT", "3");
						}else if("WPA2".equals(entry.getValue())){
							wifiInfoElementTemp.put("PWD", "");
							wifiInfoElementTemp.put("ENCRYPT", "4");
						}else{
							wifiInfoElementTemp.put("PWD", "");
							wifiInfoElementTemp.put("ENCRYPT", "5");
						}
					}
					wifiList.add(wifiInfoElementTemp);
				}
				/*if(wifiInfoElement1.size() > 0){wifiList.add(wifiInfoElement1);}
				if(wifiInfoElement2.size() > 0){wifiList.add(wifiInfoElement2);}
				if(wifiInfoElement5.size() > 0){wifiList.add(wifiInfoElement5);}*/

				WIFIList.put("WIFIList", wifiList);
			}


		}else{
			String msg = "网关MAC地址："+gatewayMacAddress+"请求获取SSID失败，原因：没有找到对应的SSID信息";
			return commoFailedResonMap(-102,msg);
		}
		try {
			parameter.put(Constant.RESULT, ErrorCodeEnum.SUCCESS.getResultCode());
			parameter.put("ResultData", WIFIList);
			return parameter;
		} catch (Exception e) {
			e.printStackTrace();
			return commonExAndMap("组装SSIDInfo出错！");
		}
	}

	/**
	 * 开关SSID 
	 */
	@Override
	public Map<String,Object> setWifiSsidOnoff(Map<String,Object> params){
		//InternetGatewayDevice.LANDevice.{i}. WLANConfiguration.{i}.Enable
		Map<String,Object> backMap=new HashMap<String,Object>();
		backMap.put("ID", params.get("ID"));
		backMap.put("CmdType", params.get("CmdType"));
		backMap.put("SequenceId", params.get("SequenceId"));
		Map parameter=(Map)params.get("Parameter");
		try {
			//验证参数
			if(parameter.get("SSIDIndex")!=null && parameter.get("MAC")!=null && parameter.get("Enable")!=null){
				//获取网关属性
				String mac=parameter.get("MAC")==null?null:parameter.get("MAC").toString().trim();
				
				if(queryGatewayInfoByMac(mac) == null){
					backMap.put("Result", -201);
					Map<String, Object> resultData  = new HashMap<String, Object>();
		        	resultData.put("FailReason", "网关MAC地址不存在");
		        	backMap.put("ResultData", resultData);
					return backMap;
				}
				
				Map<String,Object> map=instructionMethodService.getParameterNames(mac, "InternetGatewayDevice.LANDevice.", false);
				//对设备属性进行遍历
				Map<String,Object> setMap=new ConcurrentHashMap<String,Object>();
				
				List<ParameterValueStruct> list=new ArrayList<ParameterValueStruct>();
				
				if("0".equalsIgnoreCase(parameter.get("SSIDIndex").toString().trim())){
					String attribute="InternetGatewayDevice.LANDevice.[0-9].WLANConfiguration.[0-9].Enable";
					for (Map.Entry<String, Object> entry : map.entrySet()) {
						if(entry.getKey().matches(attribute)){
					    	ParameterValueStruct parameterValueStruct=new ParameterValueStruct();
						    parameterValueStruct.setName(entry.getKey());
						    parameterValueStruct.setValue("1".equals(parameter.get("Enable").toString().trim()));
						    parameterValueStruct.setValueType("boolean");
						    parameterValueStruct.setReadWrite((boolean)entry.getValue());
						    list.add(parameterValueStruct);
					    }
					}
					boolean flag=false;
					if(list.size()>0){
						flag=instructionMethodService.setParameterValue(mac, list);
						if(flag){
							
							backMap.put("Result", 0);
							Map<String, Object> resultData  = new HashMap<String, Object>();
				        	backMap.put("ResultData", resultData);
						}else{
							backMap.put("Result", -400);
							Map<String, Object> resultData = new HashMap<String, Object>();
							resultData.put("FailReason", "网关设置参数失败");
							backMap.put("ResultData", resultData);
						}
					}else{
						backMap.put("Result", -205);
						Map<String, Object> resultData = new HashMap<String, Object>();
						resultData.put("FailReason", "网关节点不存在");
						backMap.put("ResultData", resultData);
					}
			    	return backMap;
				}else{//
					String attribute="InternetGatewayDevice.LANDevice.[0-9].WLANConfiguration."+parameter.get("SSIDIndex").toString().trim()+".SSID";
					List<String> listAttr=new ArrayList<String>();
					for (Map.Entry<String, Object> entry : map.entrySet()) {
						if(entry.getKey().matches(attribute)){
						    listAttr.add(entry.getKey());
						    
						    String profix = entry.getKey().substring(0, entry.getKey().lastIndexOf(".")+1);
						    ParameterValueStruct parameterValueStruct=new ParameterValueStruct();
					    	parameterValueStruct.setName(profix +"Enable");
							parameterValueStruct.setValue( "1".equals(parameter.get("Enable").toString().trim()));
					    	parameterValueStruct.setValueType("boolean");
					    	parameterValueStruct.setReadWrite(true);
					    	list.add(parameterValueStruct);
					    }
					}
					
					/*Map<String, Object> mapValue=instructionMethodService.getParameterValues(mac, listAttr);
					
					for (Map.Entry<String, Object> entry : mapValue.entrySet()) {  
						if(entry.getValue().toString().trim().equalsIgnoreCase(parameter.get("SSIDIndex").toString().trim())){
							String attributeEnable=entry.getKey().substring(0, entry.getKey().lastIndexOf("."))+".Enable";
							ParameterValueStruct parameterValueStruct=new ParameterValueStruct();
					    	parameterValueStruct.setName(attributeEnable);
							parameterValueStruct.setValue( "1".equals(parameter.get("Enable").toString().trim()));
					    	parameterValueStruct.setValueType("boolean");
					    	parameterValueStruct.setReadWrite(true);
					    	list.add(parameterValueStruct);
						}
					}*/
					
					boolean flag=false;
					if(list.size()>0){
						flag=instructionMethodService.setParameterValue(mac, list);
						if(flag){
							backMap.put("Result", 0);
							Map<String, Object> resultData  = new HashMap<String, Object>();
				        	backMap.put("ResultData", resultData);
						}else{
							backMap.put("Result", -400);
							Map<String, Object> resultData = new HashMap<String, Object>();
							resultData.put("FailReason", "网关设置参数失败");
							backMap.put("ResultData", resultData);
						}
					}else{
						backMap.put("Result", -205);
						Map<String, Object> resultData = new HashMap<String, Object>();
						resultData.put("FailReason", "网关节点不存在");
						backMap.put("ResultData", resultData);
					}
			    	return backMap;
				}

			}else{
				backMap.put("Result", -102);
				Map<String, Object> resultData = new HashMap<String, Object>();
				resultData.put("FailReason", "参数错误");
				backMap.put("ResultData", resultData);
		    	return backMap;
			}
		} catch (Exception e) {
			backMap.put("Result", -400);
			Map<String, Object> resultData = new HashMap<String, Object>();
			resultData.put("FailReason", "服务器内部错误");
			backMap.put("ResultData", resultData);
	    	return backMap;
		}
	}
	
	/**
	 * 设置Wi-Fi定时开关
	 */
	@Override	
	public Map<String,Object> setWifiOnoffTimer(Map<String,Object> params){
		
		//InternetGatewayDevice.LANDevice.{i}.X_CMCC_WLANSwitchTimerControl.{i}
		Map<String,Object> backMap=new HashMap<String,Object>();
		backMap.put("ID", params.get("ID"));
		backMap.put("CmdType", params.get("CmdType"));
		backMap.put("SequenceId", params.get("SequenceId"));
		backMap.put("ResultData", new HashMap());
		Map parameter=(Map)params.get("Parameter");
		try {
			//验证参数
			
			parameter.remove(Constant.PARAMETER);
	        parameter.remove(Constant.RPCMETHOD);
			
			if(parameter.get("MAC")!=null && parameter.get("Enable")!=null ){
				
				if(queryGatewayInfoByMac(parameter.get("MAC").toString()) == null){
					backMap.put("Result", "-201");
					Map<String, Object> resultData = new HashMap<String, Object>();
					resultData.put("FailReason", "网关MAC地址不存在");
					backMap.put("ResultData", resultData);
					return backMap;
				}
				
				SimpleDateFormat format = new SimpleDateFormat("HH:mm");
				
				if(parameter.get("StartTime") != null){
					
					try {
						format.parse(parameter.get("StartTime").toString());
					} catch (Exception e) {
						backMap.put(Constant.RESULT, -102);
						Map<String, Object> resultData = new HashMap<String, Object>();
						resultData.put("FailReason", "StartTime格式不正确");
						backMap.put("ResultData", resultData);
			        	return backMap;
					}
				}
				if(parameter.get("EndTime") != null){
					
					try {
						format.parse(parameter.get("EndTime").toString());
					} catch (Exception e) {
						backMap.put(Constant.RESULT, -102);
						Map<String, Object> resultData = new HashMap<String, Object>();
						resultData.put("FailReason", "EndTime格式不正确");
						backMap.put("ResultData", resultData);
						return backMap;
					}
				}
				
				Map<String,Object> map=instructionMethodService.getParameterNames(parameter.get("MAC").toString(), "InternetGatewayDevice.LANDevice.", false);
				//对设备属性进行遍历
				
				String attributeActive="InternetGatewayDevice.LANDevice.[0-9]+.X_CMCC_WLANSwitchTimerControl.[0-9]+.Active";

				String attributeEnable="InternetGatewayDevice.LANDevice.[0-9]+.X_CMCC_WLANSwitchTimerControl.[0-9]+.Enable";
			    
			    String attributeTime="InternetGatewayDevice.LANDevice.[0-9]+.X_CMCC_WLANSwitchTimerControl.[0-9]+.Time";
			    
			    List<String> nameList = new ArrayList<String>();
				
			    Set<String> keys = map.keySet();
			    
			    for(String key : keys){
			    	
			    	if(key.matches(attributeActive)){
			    		
			    		nameList.add(key);
			    		
			    	}
			    }
			    
			    List<ParameterValueStruct> list=new ArrayList<ParameterValueStruct>();
			    if(nameList.size() == 0){
			    	
			    	//添加2个实例
			    	
			    	if(parameter.get("StartTime") != null && !"".equals(parameter.get("StartTime"))){
			    		
			    		//添加开机实例
			    	//	int index = instructionMethodService.AddObject(parameter.get("MAC").toString(), "InternetGatewayDevice.LANDevice." ,System.currentTimeMillis()+"");
			    		int index = instructionMethodService.AddObject(parameter.get("MAC").toString(), "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl." ,System.currentTimeMillis()+"");
			    	//	int index = instructionMethodService.AddObject(parameter.get("MAC").toString(), "InternetGatewayDevice.LANDevice."+ix+".X_CMCC_WLANSwitchTimerControl." ,System.currentTimeMillis()+"");
			    		//设置开机参数
			    		if(index > 0){
			    			
			    			ParameterValueStruct activePvs = new ParameterValueStruct<>();
			    			activePvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Active");
			    			activePvs.setValue( parameter.get("Enable"));
			    			activePvs.setValueType("unsignedInt");
			    			activePvs.setReadWrite(true);
			    			list.add(activePvs);
			    			ParameterValueStruct enablePvs = new ParameterValueStruct<>();
			    			enablePvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Enable");
			    			enablePvs.setValue(1);
			    			enablePvs.setValueType("unsignedInt");
			    			enablePvs.setReadWrite(true);
			    			list.add(enablePvs);
			    			ParameterValueStruct timePvs = new ParameterValueStruct<>();
			    			timePvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Time");
			    			timePvs.setValue(parameter.get("StartTime"));
			    			timePvs.setValueType("string");
			    			timePvs.setReadWrite(true);
			    			list.add(timePvs);
			    			ParameterValueStruct weekdayPvs = new ParameterValueStruct<>();
			    			weekdayPvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Weekday");
			    			weekdayPvs.setValue("1,2,3,4,5,6,7");
			    			weekdayPvs.setValueType("string");
			    			weekdayPvs.setReadWrite(true);
			    			list.add(weekdayPvs);
			    		}else{
			    			backMap.put(Constant.RESULT, -400);
							Map<String, Object> resultData = new HashMap<String, Object>();
							resultData.put("FailReason", "网关新增节点失败");
							backMap.put("ResultData", resultData);
			    			return backMap;
			    		}
			    	}
			    	
		    		//添加关机实例
			    	if(parameter.get("EndTime") != null && !"".equals(parameter.get("EndTime"))){
			    		
			    		int index = instructionMethodService.AddObject(parameter.get("MAC").toString(), "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl." ,System.currentTimeMillis()+"");
			    		//设置关机参数
			    		if(index > 0){
			    			
			    			ParameterValueStruct endActivePvs = new ParameterValueStruct<>();
			    			endActivePvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Active");
			    			endActivePvs.setValue(  parameter.get("Enable"));
			    			endActivePvs.setValueType("unsignedInt");
			    			endActivePvs.setReadWrite(true);
			    			list.add(endActivePvs);
			    			ParameterValueStruct endeEnablePvs = new ParameterValueStruct<>();
			    			endeEnablePvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Enable");
			    			endeEnablePvs.setValue(0);
			    			endeEnablePvs.setValueType("unsignedInt");
			    			endeEnablePvs.setReadWrite(true);
			    			list.add(endeEnablePvs);
			    			ParameterValueStruct endTimePvs = new ParameterValueStruct<>();
			    			endTimePvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Time");
			    			endTimePvs.setValue(parameter.get("EndTime"));
			    			endTimePvs.setValueType("string");
			    			endTimePvs.setReadWrite(true);
			    			list.add(endTimePvs);
			    			ParameterValueStruct weekdayPvs = new ParameterValueStruct<>();
			    			weekdayPvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Weekday");
			    			weekdayPvs.setValue("1,2,3,4,5,6,7");
			    			weekdayPvs.setValueType("string");
			    			weekdayPvs.setReadWrite(true);
			    			list.add(weekdayPvs);
			    		}else{
			    			backMap.put(Constant.RESULT, -400);
							Map<String, Object> resultData = new HashMap<String, Object>();
							resultData.put("FailReason", "网关新增节点失败");
							backMap.put("ResultData", resultData);
			    			return backMap;
			    		}
			    	}
			    	
			    }else if(nameList.size() == 1){
			    	
			    	// 修改1个实例，添加1个实例
			    	//查看存在的实例属于开机还是关机
			    	
			    	String indexName = nameList.get(0);

					//得到其中第一个i的值
					String name = indexName.substring(0, indexName.lastIndexOf("."));
					name = name.substring(0, name.lastIndexOf(".")+1);
			    	
			    	String activeName = indexName.substring(0, indexName.lastIndexOf("."))+ ".Active";
			    	
			    	String enableName = indexName.substring(0, indexName.lastIndexOf("."))+ ".Enable";
			    	
			    	String timeName = indexName.substring(0, indexName.lastIndexOf("."))+ ".Time";
			    	
			    	String weekday = indexName.substring(0, indexName.lastIndexOf("."))+ ".Weekday";
			    	
			    	List<String> queryNameList = new ArrayList<String>();
			    	
			    	queryNameList.add(activeName);
			    	queryNameList.add(enableName);
			    	queryNameList.add(timeName);
			    	Map<String, Object> resultMap = instructionMethodService.getParameterValues(parameter.get("MAC").toString(), queryNameList);
			    	
			    	Object enableValue = resultMap.get(enableName);
			    	
			    	if("1".equals(enableValue.toString())){
			    		if(parameter.get("StartTime") != null && !"".equals(parameter.get("StartTime"))){
							ParameterValueStruct startActivePvs = new ParameterValueStruct<>();
			    			startActivePvs.setName(activeName);
			    			startActivePvs.setValue(parameter.get("Enable"));
			    			startActivePvs.setValueType(ParameterValueStruct.Type_UnsignedInt);
			    			startActivePvs.setReadWrite(true);
			    			list.add(startActivePvs);
			    			ParameterValueStruct startEnablePvs = new ParameterValueStruct<>();
			    			startEnablePvs.setName(enableName);
			    			startEnablePvs.setValue(enableValue);
			    			startEnablePvs.setValueType(ParameterValueStruct.Type_UnsignedInt);
			    			startEnablePvs.setReadWrite(true);
			    			list.add(startEnablePvs);
			    			ParameterValueStruct startTimePvs = new ParameterValueStruct<>();
			    			startTimePvs.setName(timeName);
			    			startTimePvs.setValue(parameter.get("StartTime"));
			    			startTimePvs.setValueType(ParameterValueStruct.Type_String);
			    			startTimePvs.setReadWrite(true);
			    			list.add(startTimePvs);
			    			ParameterValueStruct weekdayPvs = new ParameterValueStruct<>();
			    			weekdayPvs.setName(weekday);
			    			weekdayPvs.setValue("1,2,3,4,5,6,7");
			    			weekdayPvs.setValueType("string");
			    			weekdayPvs.setReadWrite(true);
			    			list.add(weekdayPvs);
			    		}
			    		if(parameter.get("EndTime") != null && !"".equals(parameter.get("EndTime"))){
			    			
			    			
			    			//添加关机实例
				    		int index = instructionMethodService.AddObject(parameter.get("MAC").toString(), name ,System.currentTimeMillis()+"");
				    		//设置关机参数
				    		if(index > 0){
				    			
				    			ParameterValueStruct endActivePvs = new ParameterValueStruct<>();
				    			endActivePvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Active");
				    			endActivePvs.setValue(parameter.get("Enable"));
				    			endActivePvs.setValueType(ParameterValueStruct.Type_UnsignedInt);
				    			endActivePvs.setReadWrite(true);
				    			list.add(endActivePvs);
				    			ParameterValueStruct endeEnablePvs = new ParameterValueStruct<>();
				    			endeEnablePvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Enable");
				    			endeEnablePvs.setValue(0);
				    			endeEnablePvs.setValueType(ParameterValueStruct.Type_UnsignedInt);
				    			endeEnablePvs.setReadWrite(true);
				    			list.add(endeEnablePvs);
				    			ParameterValueStruct endTimePvs = new ParameterValueStruct<>();
				    			endTimePvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Time");
				    			endTimePvs.setValue(parameter.get("EndTime"));
				    			endTimePvs.setValueType(ParameterValueStruct.Type_String);
				    			endTimePvs.setReadWrite(true);
				    			list.add(endTimePvs);
				    			ParameterValueStruct weekdayPvs = new ParameterValueStruct<>();
				    			weekdayPvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Weekday");
				    			weekdayPvs.setValue("1,2,3,4,5,6,7");
				    			weekdayPvs.setValueType("string");
				    			weekdayPvs.setReadWrite(true);
				    			list.add(weekdayPvs);
				    		}else{
				    			backMap.put(Constant.RESULT, -400);
								Map<String, Object> resultData = new HashMap<String, Object>();
								resultData.put("FailReason", "网关新增节点失败");
								backMap.put("ResultData", resultData);
				    			return backMap;
				    		}
			    			
			    		}
			    		
			    	}else{
				    		if(parameter.get("EndTime") != null && !"".equals(parameter.get("EndTime"))){
				    			
				    			ParameterValueStruct startActivePvs = new ParameterValueStruct<>();
				    			startActivePvs.setName(activeName);
				    			startActivePvs.setValue( parameter.get("Enable"));
				    			startActivePvs.setValueType("unsignedInt");
				    			startActivePvs.setReadWrite(true);
				    			list.add(startActivePvs);
				    			ParameterValueStruct startEnablePvs = new ParameterValueStruct<>();
				    			startEnablePvs.setName(enableName);
				    			startEnablePvs.setValue(0);
				    			startEnablePvs.setValueType("unsignedInt");
				    			startEnablePvs.setReadWrite(true);
				    			list.add(startEnablePvs);
				    			ParameterValueStruct startTimePvs = new ParameterValueStruct<>();
				    			startTimePvs.setName(timeName);
				    			startTimePvs.setValue(parameter.get("EndTime"));
				    			startTimePvs.setValueType("string");
				    			startTimePvs.setReadWrite(true);
				    			list.add(startTimePvs);
				    			ParameterValueStruct weekdayPvs = new ParameterValueStruct<>();
				    			weekdayPvs.setName(weekday);
				    			weekdayPvs.setValue("1,2,3,4,5,6,7");
				    			weekdayPvs.setValueType("string");
				    			weekdayPvs.setReadWrite(true);
				    			list.add(weekdayPvs);
				    		}
				    		if(parameter.get("StartTime") != null && !"".equals(parameter.get("StartTime"))){
				    			
				    			//添加开机实例
					    		int index = instructionMethodService.AddObject(parameter.get("MAC").toString(), "InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl." ,System.currentTimeMillis()+"");
					    		//设置开机参数
					    		if(index > 0){
					    			
					    			ParameterValueStruct endActivePvs = new ParameterValueStruct<>();
					    			endActivePvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Active");
					    			endActivePvs.setValue(parameter.get("Enable"));
					    			endActivePvs.setValueType("unsignedInt");
					    			endActivePvs.setReadWrite(true);
					    			list.add(endActivePvs);
					    			ParameterValueStruct endeEnablePvs = new ParameterValueStruct<>();
					    			endeEnablePvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Enable");
					    			endeEnablePvs.setValue(1);
					    			endeEnablePvs.setValueType("unsignedInt");
					    			endeEnablePvs.setReadWrite(true);
					    			list.add(endeEnablePvs);
					    			ParameterValueStruct endTimePvs = new ParameterValueStruct<>();
					    			endTimePvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Time");
					    			endTimePvs.setValue(parameter.get("StartTime"));
					    			endTimePvs.setValueType("string");
					    			endTimePvs.setReadWrite(true);
					    			list.add(endTimePvs);
					    			ParameterValueStruct weekdayPvs = new ParameterValueStruct<>();
					    			weekdayPvs.setName("InternetGatewayDevice.LANDevice.1.X_CMCC_WLANSwitchTimerControl."+index+".Weekday");
					    			weekdayPvs.setValue("1,2,3,4,5,6,7");
					    			weekdayPvs.setValueType("string");
					    			weekdayPvs.setReadWrite(true);
					    			list.add(weekdayPvs);
					    		}else{
					    			backMap.put(Constant.RESULT, -400);
					    			Map<String, Object> resultData = new HashMap<String, Object>();
									resultData.put("FailReason", "网关新增节点失败");
									backMap.put("ResultData", resultData);
					    			return backMap;
					    		}
				    	}
			    	}
		    		
			    		
			    }else{
			    	//2个实例都存在时
			    	Object enableValue = 1;
			    	for(int i = 0; i< nameList.size();i++){
			    		if(nameList.get(i).endsWith(".Active")){
			    			String indexName = nameList.get(i);
					    	
					    	String activeName = indexName.substring(0, indexName.lastIndexOf("."))+ ".Active";
					    	
					    	String enableName = indexName.substring(0, indexName.lastIndexOf("."))+ ".Enable";
					    	
					    	String timeName = indexName.substring(0, indexName.lastIndexOf("."))+ ".Time";
					    	
					    	String weekday = indexName.substring(0, indexName.lastIndexOf("."))+ ".Weekday";
					    	
					    	List<String> queryNameList = new ArrayList<String>();
					    	
					    	queryNameList.add(activeName);
					    	queryNameList.add(enableName);
					    	queryNameList.add(timeName);
					 //   	Map<String, Object> resultMap = instructionMethodService.getParameterValues(parameter.get("MAC").toString(), queryNameList);
					    	
					    	
					    	
					    	//设置属性
			    			if(enableValue != null && "1".equals(enableValue.toString()) && parameter.get("StartTime") != null && !"".equals(parameter.get("StartTime"))){
			    				
			    				ParameterValueStruct startActivePvs = new ParameterValueStruct<>();
			    				startActivePvs.setName(activeName);
			    				startActivePvs.setValue( parameter.get("Enable"));
			    				startActivePvs.setValueType("unsignedInt");
			    				startActivePvs.setReadWrite(true);
			    				list.add(startActivePvs);
			    				ParameterValueStruct startEnablePvs = new ParameterValueStruct<>();
			    				startEnablePvs.setName(enableName);
			    				startEnablePvs.setValue(1);
			    				startEnablePvs.setValueType("unsignedInt");
			    				startEnablePvs.setReadWrite(true);
			    				list.add(startEnablePvs);
			    				ParameterValueStruct startTimePvs = new ParameterValueStruct<>();
			    				startTimePvs.setName(timeName);
			    				startTimePvs.setValue(parameter.get("StartTime"));
			    				startTimePvs.setValueType("string");
			    				startTimePvs.setReadWrite(true);
			    				list.add(startTimePvs);
			    				ParameterValueStruct weekdayPvs = new ParameterValueStruct<>();
				    			weekdayPvs.setName(weekday);
				    			weekdayPvs.setValue("1,2,3,4,5,6,7");
				    			weekdayPvs.setValueType("string");
				    			weekdayPvs.setReadWrite(true);
				    			list.add(weekdayPvs);
			    			}
			    			
			    			if(enableValue != null && "0".equals(enableValue.toString()) && parameter.get("EndTime") != null && !"".equals(parameter.get("EndTime"))){
			    				
			    				//设置属性
			    				
			    				ParameterValueStruct endActivePvs = new ParameterValueStruct<>();
			    				endActivePvs.setName(activeName);
			    				endActivePvs.setValue( parameter.get("Enable"));
			    				endActivePvs.setValueType("unsignedInt");
			    				endActivePvs.setReadWrite(true);
			    				list.add(endActivePvs);
			    				ParameterValueStruct endEnablePvs = new ParameterValueStruct<>();
			    				endEnablePvs.setName(enableName);
			    				endEnablePvs.setValue(0);
			    				endEnablePvs.setValueType("unsignedInt");
			    				endEnablePvs.setReadWrite(true);
			    				list.add(endEnablePvs);
			    				ParameterValueStruct endTimePvs = new ParameterValueStruct<>();
			    				endTimePvs.setName(timeName);
			    				if(parameter.get("EndTime") != null && !"".equals(parameter.get("EndTime"))){
			    					
			    					endTimePvs.setValue(parameter.get("EndTime"));
			    				}
			    				endTimePvs.setValueType("string");
			    				endTimePvs.setReadWrite(true);
			    				list.add(endTimePvs);
			    				ParameterValueStruct weekdayPvs = new ParameterValueStruct<>();
				    			weekdayPvs.setName(weekday);
				    			weekdayPvs.setValue("1,2,3,4,5,6,7");
				    			weekdayPvs.setValueType("string");
				    			weekdayPvs.setReadWrite(true);
				    			list.add(weekdayPvs);
			    			}
			    			
			    			enableValue = 0;
			    			
			    		}
			    		
			    	}
			    }
			    boolean b =  instructionMethodService.setParameterValue(parameter.get("MAC").toString(), list);
			    if(b){
			    	
			    	backMap.put(Constant.RESULT, 0);
			    	Map<String, Object> resultData  = new HashMap<String, Object>();
		        	backMap.put("ResultData", resultData);
			    }else{
			    	
			    	backMap.put(Constant.RESULT, -400);
			    	Map<String, Object> resultData = new HashMap<String, Object>();
					resultData.put("FailReason", "网关设置参数失败");
					backMap.put("ResultData", resultData);
			    }
			}else{
			    backMap.put(Constant.RESULT, -102);
			    Map<String, Object> resultData = new HashMap<String, Object>();
				resultData.put("FailReason", "参数错误");
				backMap.put("ResultData", resultData);
			}
			return backMap;
			

		} catch (Exception e) {
			backMap.put("Result",  -400);
			Map<String, Object> resultData = new HashMap<String, Object>();
			resultData.put("FailReason", "服务器内部错误");
			backMap.put("ResultData", resultData);
	    	return backMap;
		}

	}
	
	/**
	 * 查询Wi-Fi定时开关状态 
	 */
	@Override
	public Map<String,Object> getWifiOnoffTimer(Map<String,Object> params){
		//InternetGatewayDevice.LANDevice.{i}.X_CMCC_WLANSwitchTimerControl.{i}
		Map<String,Object> backMap=new HashMap<String,Object>();
		backMap.put("ID", params.get("ID"));
		backMap.put("CmdType", params.get("CmdType"));
		backMap.put("SequenceId", params.get("SequenceId"));
		Map parameter=(Map)params.get("Parameter");
		try {
			//验证参数
			if(parameter.get("MAC")!=null){
				//获取网关属性
				String mac=parameter.get("MAC")==null?null:parameter.get("MAC").toString().trim();
				
				if(queryGatewayInfoByMac(mac) == null){
					backMap.put("Result", "-201");
					Map<String, Object> resultData = new HashMap<String, Object>();
					resultData.put("FailReason", "网关MAC地址不存在");
					backMap.put("ResultData", resultData);
					return backMap;
				}
				
				Map<String,Object> map=instructionMethodService.getParameterNames(mac, "InternetGatewayDevice.LANDevice.", false);
				//对设备属性进行遍历
				Map<String,Object> setMap=new ConcurrentHashMap<String,Object>();
				
				List<String> list=new ArrayList<String>();
				
				String attributeActive="InternetGatewayDevice.LANDevice.[0-9].X_CMCC_WLANSwitchTimerControl.[0-9]+.Active";
				String attributeEnable="InternetGatewayDevice.LANDevice.[0-9].X_CMCC_WLANSwitchTimerControl.[0-9]+.Enable";
				String attributeTime="InternetGatewayDevice.LANDevice.[0-9].X_CMCC_WLANSwitchTimerControl.[0-9]+.Time";
				for (Map.Entry<String, Object> entry : map.entrySet()) {  

				    if(entry.getKey().matches(attributeActive)){
				    	//查询状态
					    list.add(entry.getKey());  
				    }
				    if(entry.getKey().matches(attributeTime)){
				    	//查询状态
					    list.add(entry.getKey());  
				    }
				    if(entry.getKey().matches(attributeEnable)){
				    	//查询状态
					    list.add(entry.getKey());  
				    }
				}  
				
				if(list.size()>0){
					Map retMap=instructionMethodService.getParameterValues(mac, list);
					if(retMap == null){
						backMap.put("Result", "-400");
						Map<String, Object> resultData = new HashMap<String, Object>();
						resultData.put("FailReason", "未知错误");
						backMap.put("ResultData", resultData);
						return backMap;
					}
					
					Set set=retMap.entrySet();
					Iterator it = set.iterator();  

					String active="";//状态
					String enable="";//是否
					String startTime=""; //时间
					String endTime=""; //关机时间
					while(it.hasNext()){
						Map.Entry<String, Object> entry=(Map.Entry<String, Object>)it.next(); 
						
						if(entry.getKey().matches(attributeEnable) && "1".equals(entry.getValue().toString()) ){
							///开机
							startTime=retMap.get(entry.getKey().substring(0, entry.getKey().lastIndexOf("."))+".Time")+"";
						}
						if(entry.getKey().matches(attributeEnable) && "0".equals(entry.getValue().toString()) ){
							///关机
							endTime=retMap.get(entry.getKey().substring(0, entry.getKey().lastIndexOf("."))+".Time")+"";
						}
						if(entry.getKey().matches(attributeActive)){
					    	//查询状态
							active=entry.getValue()==null?"":entry.getValue().toString();  
					    }
					}
					Map data=new HashMap();
					data.put("StartTime", startTime);
					data.put("EndTime", endTime);
					data.put("Enable", active);
					
					backMap.put("Result", 0);
					backMap.put("ResultData", data);

					return backMap;
				}else{
					logger.info("查询Wi-Fi定时开关状态失败，原因：未找到Wi-Fi定时开关信息！");
					backMap.put("Result", -205);
					Map<String, Object> resultData = new HashMap<String, Object>();
					resultData.put("FailReason", "网关节点不存在");
					backMap.put("ResultData", resultData);
					return backMap;
				}

			}else{
				backMap.put("Result", -102);
				Map<String, Object> resultData = new HashMap<String, Object>();
				resultData.put("FailReason", "参数错误");
				backMap.put("ResultData", resultData);
		    	return backMap;
			}
		} catch (Exception e) {
			backMap.put("Result", -400);
			Map<String, Object> resultData = new HashMap<String, Object>();
			resultData.put("FailReason", "服务器内部错误");
			backMap.put("ResultData", resultData);
	    	return backMap;
		}
	}
	@Override
	public Map<String, Object> getInternetConInformation(Map<String, Object> parameter) {
		//1.根据网关MAC查询网关ID
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = macMap.get(Constant.MAC) == null ? null :macMap.get(Constant.MAC).toString();
		parameter.remove(Constant.PARAMETER);
		parameter.remove(Constant.RPCMETHOD);
        if(gatewayMacAddress == null){
			String msg = "请求获取SSID信息输入MAC地址为空";
        	logger.info(msg);
			return commoFailedResonMap(-102,msg);
        }else{
			GatewayInfo gatewayInfo = new GatewayInfo();
			gatewayInfo.setGatewayMacaddress(gatewayMacAddress);
			GatewayInfo info = gatewayInfoService.selectGatewayInfo(gatewayInfo);
			if(info  == null){
				String msg = "请求获取SSID信息时输入MAC地址"+gatewayMacAddress+"不存在";
				logger.info(msg);
				return commoFailedResonMap(-201,msg);
			}
		}
        //2.下发指令并获取结果 
        Map<String,Object> map =instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.", false);
        if(map == null ){
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求internet网络连接信息错误，原因：网关MAC地址不存在");
        	parameter.put(Constant.RESULT, -201);
        	parameter.put(Constant.FAILREASON, "网关MAC地址不存在");
            return parameter;
        }
        String nodeHeaderReq = "InternetGatewayDevice.WANDevice.[0-9].WANConnectionDevice.[0-9].WANIPConnection.[0-9].";
		String serverList = "InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANIPConnection.1.X_CMCC_ServiceList";

		List<String> cloList = new ArrayList<>();
		List<String> cloListGetCoonType = new ArrayList<>();
        String iPAddress = null,subnetMask = null,defaultGateway=null,iPv6IPAddress=null,defaultIPv6Gateway=null,iPv6PrefixPltime=null,dNSServers = null,iPv6DNSServers = null;

		cloListGetCoonType.add(serverList);

		Map<String, Object> mapGetCoonType = null;
		logger.info("cloListGetCoonType size " + cloList.size());
		if(cloListGetCoonType.size() > 0){
			for(int i =0 ; i < cloList.size(); i++){
				logger.info("cloListGetCoonType " + cloList.get(i));
			}
			mapGetCoonType = instructionMethodService.getParameterValues(gatewayMacAddress, cloListGetCoonType);
		}
		for (Map.Entry<String, Object> entry : mapGetCoonType.entrySet()) {
			logger.info("entry " + entry.getKey().toString() + " value " + entry.getValue().toString());
			if (entry.getKey().equals(serverList)) {
				String cTypeValue = entry.getValue().toString();
				logger.info(" cTypeValue " + cTypeValue);
				if (cTypeValue.contains("INTERNET")){
					nodeHeaderReq = "InternetGatewayDevice.WANDevice.[0-9].WANConnectionDevice.[0-9].WANPPPConnection.[0-9].";
				}
			}
		}

		logger.info(" nodeHeaderReq " + nodeHeaderReq);
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			/*logger.info("~~~~~~~entry " + entry.getKey().toString());*/
			iPAddress = nodeHeaderReq + "ExternalIPAddress";
			subnetMask = nodeHeaderReq + "SubnetMask";
			defaultGateway = nodeHeaderReq + "DefaultGateway";
			iPv6IPAddress = nodeHeaderReq + "X_CMCC_IPv6IPAddress";
			defaultIPv6Gateway = nodeHeaderReq + "X_CMCC_DefaultIPv6Gateway";
			iPv6PrefixPltime = nodeHeaderReq + "X_CMCC_IPv6Prefix";
			dNSServers = nodeHeaderReq + "DNSServers";
			iPv6DNSServers = nodeHeaderReq + "X_CMCC_IPv6DNSServers";
			if(entry.getKey().matches(iPAddress)||entry.getKey().matches(subnetMask)||entry.getKey().matches(defaultGateway)
					||entry.getKey().matches(iPv6IPAddress)||entry.getKey().matches(defaultIPv6Gateway)||entry.getKey().matches(iPv6PrefixPltime)
					||entry.getKey().matches(dNSServers)||entry.getKey().matches(iPv6DNSServers)){//如果与设备属性匹配
				cloList.add(entry.getKey());
			}
		}
        Map<String,Object> ResultData = new HashMap<>();
        if(cloList.size()>0){
			logger.info("cloList size " + cloList.size());
			for(int i =0 ; i < cloList.size(); i++){
				logger.info("cloList " + cloList.get(i));
			}
			Map<String, Object> mapGetParam;
			mapGetParam = instructionMethodService.getParameterValues(gatewayMacAddress, cloList);
			for (Map.Entry<String, Object> entry : mapGetParam.entrySet()) {
				logger.info("entry " + entry.getKey().toString());
				if(entry.getKey().matches(dNSServers)){
					if(entry.getValue()!=""){
						String [] ipv4DNS = entry.getValue().toString().split(",");
						if(ipv4DNS.length > 0){
							ResultData.put("IPv4DNS1",ipv4DNS[0]);
						}
						if(ipv4DNS.length > 1){
							ResultData.put("IPv4DNS2",ipv4DNS[1]);
						}
					}
				}else if(entry.getKey().matches(iPv6DNSServers)){
					String [] ipv4DNS = entry.getValue().toString().split(",");
					if(ipv4DNS.length > 0){
						ResultData.put("IPv6DNS1",ipv4DNS[0]);
					}
					if(ipv4DNS.length > 1){
						ResultData.put("IPv6DNS2",ipv4DNS[1]);
					}
				}else if(entry.getKey().matches(iPAddress)){
					ResultData.put("WanIPAddr", entry.getValue());
				}else if(entry.getKey().matches(subnetMask)){
					ResultData.put("IPv4SUBNETMASK", entry.getValue());
				}else if(entry.getKey().matches(defaultGateway)){
					ResultData.put("IPv4GATEWAY", entry.getValue());
				}else if(entry.getKey().matches(iPv6IPAddress)){
					ResultData.put("IPv6_IPADDRESS", entry.getValue());
				}else if(entry.getKey().matches(defaultIPv6Gateway)){
					ResultData.put("IPv6GATEWAY", entry.getValue());
				}else if(entry.getKey().matches(iPv6PrefixPltime)){
					ResultData.put("IPv6PRIFIX", String.valueOf(entry.getValue()));
				}
			}
		}
		if(ResultData.get("IPv6_IPADDRESS")==null){
			ResultData.put("IPv6_IPADDRESS", nullStr);
		}
		if(ResultData.get("IPv6GATEWAY")==null){
			ResultData.put("IPv6GATEWAY", nullStr);
		}
		if(ResultData.get("IPv6DNS1")==null){
			ResultData.put("IPv6DNS1", nullStr);
		}
		if(ResultData.get("IPv6DNS2")==null){
			ResultData.put("IPv6DNS2", nullStr);
		}
		/*if(ResultData.get("WanIPv6Addr")==null1){
			ResultData.put("WanIPv6Addr", nullStr);
		}*/
		if(ResultData.get("IPv6PREFIXLENGTH")==null){
			ResultData.put("IPv6PREFIXLENGTH", nullStr);
		}
		if(ResultData.get("IPv4SUBNETMASK")==null){
			ResultData.put("IPv4SUBNETMASK", nullStr);
		}
		if(ResultData.get("IPv6PRIFIX")==null){
			ResultData.put("IPv6PRIFIX", nullStr);
		}
		if(ResultData.get("IPv4GATEWAY")==null){
			ResultData.put("IPv4GATEWAY", nullStr);
		}

		parameter.put("ResultData", ResultData);
		parameter.put(Constant.RESULT, ErrorCodeEnum.SUCCESS.getResultCode());
		return parameter;
        
	}

	@Override
	public Map<String, Object> getPPPDailUpStatus(Map<String, Object> parameter) {
		//1.根据网关MAC查询网关ID
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
		parameter.remove(Constant.PARAMETER);
		parameter.remove(Constant.RPCMETHOD);
        String gatewayMacAddress = macMap.get(Constant.MAC) == null ? null :macMap.get(Constant.MAC).toString();
        if(gatewayMacAddress == null){
			String msg ="请求获取SSID信息时输入MAC地址为空";
        	logger.info(msg);
			return commoFailedResonMap(-102,msg);
        }else{
			GatewayInfo gatewayInfo = new GatewayInfo();
			gatewayInfo.setGatewayMacaddress(gatewayMacAddress);
			GatewayInfo info = gatewayInfoService.selectGatewayInfo(gatewayInfo);
			if(info  == null){
				String msg ="请求获取SSID信息时输入MAC地址不存在";
				logger.info(msg);
				return commoFailedResonMap(-201,msg);
			}
		}
        //2.下发指令并获取结果 
        Map<String,Object> map=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.WANDevice.", false);
        if(map == null ){
			String msg = "网关MAC地址："+gatewayMacAddress+"请求获取SSIDX信息失败，原因：网关MAC地址不存在";
        	logger.info(msg);
			return commoFailedResonMap(-102,msg);
        }

        String nodeHeader = "InternetGatewayDevice.WANDevice.[0-9].WANConnectionDevice.[0-9].WANPPPConnection.[0-9].";
        List<String> cloList = new ArrayList<>();
        String attributeDialReason = null,attributeConnectionStatus=null,attributeX_CMCC_IPv6ConnStatus=null,attributeWANStatus=null;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
			logger.info("~~~~~~~~~~~~"+entry.getKey());
			//IPV4
		    attributeDialReason = nodeHeader + "LastConnectionError";
		    attributeConnectionStatus = nodeHeader + "ConnectionStatus";
		    //IPV6
		    attributeX_CMCC_IPv6ConnStatus = nodeHeader + "X_CMCC_IPv6ConnStatus";
		    //ConnectionStatus1
		    //DialReason1
		    if(entry.getKey().matches(attributeDialReason)||entry.getKey().matches(attributeConnectionStatus)||entry.getKey().matches(attributeX_CMCC_IPv6ConnStatus)){//如果与设备属性匹配
		    	cloList.add(entry.getKey());
		    }
		}
		logger.info(" cloList " +cloList.size());
        Map<String,Object> ResultData = new HashMap<>();
        if(cloList.size()>0){
			Map<String, Object> resultMap;
        	resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, cloList);
			logger.info(" ~~resultMap~~ " +resultMap.size());
			ResultData.put("ConnectionStatus", nullStr);
			ResultData.put("WANStatus", nullStr);
			ResultData.put("DialReason", nullStr);
			ResultData.put("ConnectionStatus1", nullStr);
			ResultData.put("WANStatus1", nullStr);
			ResultData.put("DialReason1", nullStr);
			String dailR="";
        	for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
			logger.info(" ~~entry~~ " + entry.getKey() + "~~val~~" + entry.getValue());
        			 if(entry.getKey().matches(attributeDialReason)){
						 logger.info(" attributeDialReason  "+entry.getValue());
						 dailR = entry.getValue().toString();
						 logger.info(" attributeDialReason  "+ dailR);
        			 }
        			 if(entry.getKey().matches(attributeConnectionStatus)){
						 logger.info(" ConnectionStatus " + entry.getValue());
        				 ResultData.put("WANStatus", entry.getValue());
						 ResultData.put("ConnectionStatus", "1");
						 if(entry.getValue()!=null && !"".equals(entry.getValue())){
							 if(entry.getValue().equals("Connected")){
								 ResultData.put("ConnectionStatus", "0");
							 }
						 }
						 
						 if(entry.getValue().equals("Unconfigured")){
								ResultData.put("DialReason",  PPPErrorCodeEnum.ERROR_NO_VALID_CONNECTION + "," + PPPErrorCodeEnum.ERROR_NO_VALID_CONNECTION.description());
							}else if(entry.getValue().equals("Connecting")){
								ResultData.put("DialReason", dailR + "," + PPPErrorCodeEnum.getDescription(dailR));
							}else if(entry.getValue().equals("Connected")){
								ResultData.put("DialReason", PPPErrorCodeEnum.ERROR_NONE + "," + PPPErrorCodeEnum.ERROR_NONE.description());
							}else{
								ResultData.put("DialReason", PPPErrorCodeEnum.ERROR_UNKNOW + "," + PPPErrorCodeEnum.ERROR_UNKNOW.description());
							}
        			 }
					if(entry.getKey().matches(attributeX_CMCC_IPv6ConnStatus)){
						ResultData.put("WANStatus1", entry.getValue());
						ResultData.put("ConnectionStatus1", "1");
						if(entry.getValue()!=null && !"".equals(entry.getValue())){
							if(entry.getValue().equals("Connected")){
								ResultData.put("ConnectionStatus1", "0");
							}
						}
						if(entry.getValue().equals("Unconfigured")){
							ResultData.put("DialReason1", PPPErrorCodeEnum.ERROR_NO_VALID_CONNECTION + "," + PPPErrorCodeEnum.ERROR_NO_VALID_CONNECTION.description());
						}else if(entry.getValue().equals("Connecting")){
							ResultData.put("DialReason1", dailR + "," +   PPPErrorCodeEnum.getDescription(dailR));
						}else if(entry.getValue().equals("Connected")){
							ResultData.put("DialReason1", PPPErrorCodeEnum.ERROR_NONE + "," + PPPErrorCodeEnum.ERROR_NONE.description());
						}else{
							ResultData.put("DialReason1", PPPErrorCodeEnum.ERROR_UNKNOW + "," + PPPErrorCodeEnum.ERROR_UNKNOW.description());
						}
					}
        	}

        }else{
			ResultData.put("ConnectionStatus", nullStr);
			ResultData.put("WANStatus", nullStr);
			ResultData.put("DialReason", nullStr);
			ResultData.put("ConnectionStatus1", nullStr);
			ResultData.put("WANStatus1", nullStr);
			ResultData.put("DialReason1", nullStr);
		}
		parameter.put("ResultData", ResultData);
		parameter.put(Constant.RESULT, ErrorCodeEnum.SUCCESS.getResultCode());
		return parameter;
	}

	@Override
	public Map<String, Object> openWPS(Map<String, Object> parameter) {
		 Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
		parameter.remove(Constant.PARAMETER);
		parameter.remove(Constant.RPCMETHOD);
	        String gatewayMacAddress = macMap.get(Constant.MAC) == null ? null :macMap.get(Constant.MAC).toString();
	        if(gatewayMacAddress == null){
	        	logger.info("请求获取SSID信息时输入MAC地址为空");
	        	parameter.put(Constant.RESULT, -102);
	        	parameter.put(Constant.FAILREASON, "MAC地址为空");
	            return parameter;
	        }else{
				GatewayInfo gatewayInfo = new GatewayInfo();
				gatewayInfo.setGatewayMacaddress(gatewayMacAddress);
				GatewayInfo info = gatewayInfoService.selectGatewayInfo(gatewayInfo);
				if(info  == null){
					logger.info("请求获取SSID信息时输入MAC地址不存在");
					parameter.put(Constant.RESULT, -201);
					parameter.put(Constant.FAILREASON, "网关MAC"+gatewayMacAddress+"不存在");
					return parameter;
				}
			}
		Map<String,Object> map=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);
		List<ParameterValueStruct> cloList = new ArrayList<>();
		List<String> list = new ArrayList<>();
		//验证map中所有相对应的属性
		String attribute="";
		if(macMap.get("SSIDIndex")!=null){
			 attribute="InternetGatewayDevice.LANDevice.[0-9].WLANConfiguration."+macMap.get("SSIDIndex").toString()+".WPS.Enable";
			//对设备属性进行遍历
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				logger.info("map ~~! " + entry.getKey());
			    if(entry.getKey().matches(attribute)){//如果与设备属性匹配
					logger.info("~~! " + entry.getKey());
			    	ParameterValueStruct pvs = new ParameterValueStruct();
			    	pvs.setName(entry.getKey());
			        pvs.setValue(true);
			        pvs.setReadWrite(true);
			        pvs.setValueType("boolean");
			    	cloList.add(pvs);
					list.add(entry.getKey());
			    }
			}
		}else{
			logger.info("请求获取SSID信息时输入SSIDIndex地址为空");
			parameter.put(Constant.RESULT, -102);
			parameter.put(Constant.FAILREASON, "SSIDIndex地址为空");
			return parameter;
		}
		//4.指令设置
		if(cloList.size()>0){
			if(instructionMethodService.setParameterValue(gatewayMacAddress, cloList)){
				Map<String, Object> resultMap =new HashMap();
				resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, list);
				boolean opertionR = false;
				for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
					logger.info(" ~~~resultMap " + entry.getKey());
					if(entry.getKey().matches(attribute)){
						logger.info(" ~~~WPSEnable " + entry.getValue());
						if(((boolean) entry.getValue())){
							opertionR = true;
						}
					}
				}
				if(opertionR){
					parameter.put(Constant.RESULT, ErrorCodeEnum.SUCCESS.getResultCode());
					return parameter;
				}else{
					parameter.put(Constant.RESULT, -102);
					parameter.put(Constant.FAILREASON, "开启WPS指令设置失败 网关返回 WPSEnable " +opertionR );
				}
			}else{
	        	parameter.put(Constant.RESULT, -102);
	        	parameter.put(Constant.FAILREASON, "开启WPS指令设置失败");
			}
        }else{
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求开启WPS失败，原因：没有找到对应WPS信息");
        	parameter.put(Constant.RESULT, -102);
        	parameter.put(Constant.FAILREASON, "组装开启WPS指令失败");
        }
		return parameter;
	}

	@Override
	public Map<String, Object> getWPSCurrentStatus(Map<String, Object> parameter) {
		//1.根据网关MAC查询网关ID
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
		parameter.remove(Constant.PARAMETER);
		parameter.remove(Constant.RPCMETHOD);
        String gatewayMacAddress = macMap.get(Constant.MAC) == null ? null :macMap.get(Constant.MAC).toString();
        if(gatewayMacAddress == null){
			String msg ="请求获取WPS时输入MAC地址为空";
        	logger.info(msg);
			return commoFailedResonMap(-102,msg);
        }else{
			GatewayInfo gatewayInfo = new GatewayInfo();
			gatewayInfo.setGatewayMacaddress(gatewayMacAddress);
			GatewayInfo info = gatewayInfoService.selectGatewayInfo(gatewayInfo);
			if(info  == null){
				String msg ="请求获取SSID信息时输入MAC地址"+gatewayMacAddress+"不存在";
				logger.info(msg);
				return commoFailedResonMap(-201,msg);
			}
		}
        //2.下发指令并获取结果 
        Map<String,Object> map=instructionMethodService.getParameterNames(gatewayMacAddress, ConstantDiagnose.LANDEVICE, false);
        if(map == null ){
			String msg ="网关MAC地址："+gatewayMacAddress+"请求获取WPS当前状态失败，原因：网关MAC地址不存在";
        	logger.info(msg);
			return commoFailedResonMap(-201,msg);
        }
        String State = "InternetGatewayDevice.LANDevice.[0-9].WLANConfiguration.[0-9].WPS.X_CMCC_WPSRegistrationState";//网关缺此参数
        String MACAddress = "InternetGatewayDevice.LANDevice.[0-9].WLANConfiguration.[0-9].AssociatedDevice.[0-9].AssociatedDeviceMACAddress";
        String IPAddress = "InternetGatewayDevice.LANDevice.[0-9].WLANConfiguration.[0-9].AssociatedDevice.[0-9].AssociatedDeviceIPAddress";
        List<String> cloList = new ArrayList<>();
        Map<String,Object> ResultData = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
			logger.info(" ~~~ " + entry.getKey());
		    if(entry.getKey().matches(State)){//如果与设备属性匹配
		    	cloList.add(entry.getKey());
		    }
			if(entry.getKey().matches(MACAddress)){//如果与设备属性匹配
				cloList.add(entry.getKey());
			}
			if(entry.getKey().matches(IPAddress)){//如果与设备属性匹配
				cloList.add(entry.getKey());
			}
		}
        if(cloList.size()>0){
			Map<String, Object> resultMap;
        	resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, cloList);
			String macValue = "";
			String ipValue = "";
			String wpsStatu="";
			for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
				logger.info(" ~~~resultMap " + entry.getKey());
        			 if(entry.getKey().matches(State)){
						 wpsStatu = (String) entry.getValue();
        			 }
        			 if(entry.getKey().matches(MACAddress)){
						 macValue = "MAC:"+entry.getValue();
        			 }
        			 if(entry.getKey().matches(IPAddress)){
						 ipValue = "IP:"+entry.getValue();
        			 }
        	}
			if(wpsStatu.equals("Start")){
				ResultData.put("WPSStatus", "0");
			}else if(wpsStatu.equals("Stop")){
				ResultData.put("WPSStatus", "1");
			}else{
				ResultData.put("WPSStatus",nullStr);
			}
			if(ipValue==null||"".equals(ipValue)||macValue ==null||"".equals(macValue)){
				ResultData.put("DevInfo","");
			}else{
				ResultData.put("DevInfo",ipValue+";"+macValue);
			}
        }
		parameter.put("ResultData", ResultData);
		parameter.put(Constant.RESULT, ErrorCodeEnum.SUCCESS.getResultCode());
		return parameter;
	}

	@Override
	public Map<String, Object> closeWPS(Map<String, Object> parameter) {
		//1.根据网关MAC查询网关ID
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
		parameter.remove(Constant.PARAMETER);
		parameter.remove(Constant.RPCMETHOD);
        String gatewayMacAddress = macMap.get(Constant.MAC) == null ? null :macMap.get(Constant.MAC).toString();
        if(gatewayMacAddress == null){
        	logger.info("请求获取WPS时输入MAC地址为空");
        	parameter.put(Constant.RESULT, -102);
        	parameter.put(Constant.FAILREASON, "MAC地址为空");
            return parameter;
        }else{
			GatewayInfo gatewayInfo = new GatewayInfo();
			gatewayInfo.setGatewayMacaddress(gatewayMacAddress);
			GatewayInfo info = gatewayInfoService.selectGatewayInfo(gatewayInfo);
			if(info  == null){
				logger.info("请求获取SSID信息时输入MAC地址不存在");
				parameter.put(Constant.RESULT, -201);
				parameter.put(Constant.FAILREASON, "网关MAC"+gatewayMacAddress+"不存在");
				return parameter;
			}
		}
		
		Map<String,Object> map=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);
		List<ParameterValueStruct> cloList = new ArrayList<>();
		List<String> list = new ArrayList<>();
		//验证map中所有相对应的属性
		String attribute="";
		/*if(macMap.get("SSIDIndex")!=null){*/
			//对设备属性进行遍历
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				 attribute="InternetGatewayDevice.LANDevice.[0-9].WLANConfiguration.[0-9].WPS.Enable";
			    if(entry.getKey().matches(attribute)){//如果与设备属性匹配
					ParameterValueStruct pvs = new ParameterValueStruct();
			    	pvs.setName(entry.getKey());
			        pvs.setValue(false);
			        pvs.setReadWrite(true);
			        pvs.setValueType("boolean");
			    	cloList.add(pvs);
					list.add(entry.getKey());
			    }
			}

		/*}*/
		//4.指令设置
		if(cloList.size()>0){
			if(instructionMethodService.setParameterValue(gatewayMacAddress, cloList)){
				Map<String, Object> resultMap =new HashMap();
				resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, list);
				boolean opertionR = false;
				for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
					logger.info(" ~~~resultMap " + entry.getKey());
					if(entry.getKey().matches(attribute)){
						logger.info(" ~~~WPSEnable " + entry.getValue());
						if(((boolean) entry.getValue())){
							opertionR = true;
						}
					}
				}
				if(!opertionR){
					parameter.put(Constant.RESULT, ErrorCodeEnum.SUCCESS.getResultCode());
					Map<String,Object> ResultData = new HashMap<>();
					parameter.put("ResultData", ResultData);
					return parameter;
				}else{
					parameter.put(Constant.RESULT, -102);
					parameter.put(Constant.FAILREASON, "开启WPS指令设置失败 网关返回 WPSEnable " +opertionR );
				}
			}else{
	        	parameter.put(Constant.RESULT, -102);
	        	parameter.put(Constant.FAILREASON, "关闭WPS指令设置失败");
			}
        }else{
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求关闭WPS失败，原因：没有找到对应WPS信息");
        	parameter.put(Constant.RESULT, -102);
        	parameter.put(Constant.FAILREASON, "组装关闭WPS指令失败");
        }
		return parameter;
	}
    
    /**
     * 返回出现异常的MAP
     *
     * @param
     * @param
     * @return
     * @throws Exception
     */
    public Map<String, Object> commonExAndMap(String Message){
    	Map<String, Object> returnMapComm = new HashMap<>();
    	returnMapComm.put(Constant.CODE, RespCodeEnum.RC_1.code());
		returnMapComm.put(Constant.MESSAGE, Message);
		returnMapComm.put(Constant.DATA, "");
		return returnMapComm;
    	
    }
    /**
     * 返回出现异常的MAP
     *
     * @param
     * @param
     * @return
     * @throws Exception
     */
    public Map<String, Object> commonExAndMap(String msgLog,String failReson){
	 Map<String, Object> parameter = new HashMap<>();
     logger.info(msgLog);
     parameter.put(Constant.RESULT, -201);
     Map<String, Object> resultData = new HashMap<>();
	 resultData.put(Constant.FAILREASON, failReson);
	 parameter.put(Constant.RESULTDATA, resultData);

     return parameter;
   }
	public Map<String, Object> commoFailedResonMap(int code,String failReson) {
		Map<String, Object> parameter = new HashMap<>();
		parameter.put(Constant.RESULT, code);
		Map<String, Object> resultData = new HashMap<>();
		resultData.put(Constant.FAILREASON, failReson);
		parameter.put(Constant.RESULTDATA, resultData);
		return parameter;
	}
	String nullStr = "";
	
	public GatewayInfo queryGatewayInfoByMac(String macAddress){
		GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayMacaddress(macAddress);
        GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        return gatewayInfo;
	}


	public static void main(String arg[]){

		/*String  aa  = "TR069,INTERNET" ;
		System.out.print(aa.contains("INTERNET"));*/

		 logger.info(PPPErrorCodeEnum.getDescription("ERROR_NONE"));

		/*Map<String, Object> parameter = new HashMap<>();
		parameter.put("InternetGatewayDevice.LANDevice.1.WLANConfiguration.12TransmitPower","A");
		parameter.put("InternetGatewayDevice.LANDevice.1.WLANConfiguration.2.SSIDAdvertisementEnabled","B");
		parameter.put("InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.SSID","C");
		parameter.put("InternetGatewayDevice.LANDevice.1.WLANConfiguration.2.SSID","D");
		for (Map.Entry<String, Object> entry : parameter.entrySet()) {
			String indexNum ="";
			String subStr="";
			if (entry.getKey().matches("InternetGatewayDevice.LANDevice.1.WLANConfiguration.[1-5].*")) {
				logger.info(" entry " +entry.getKey());
				subStr =  entry.getKey().substring(0,entry.getKey().lastIndexOf("."));
				logger.info(" subStr " +subStr);
				indexNum = subStr.substring(subStr.lastIndexOf(".")+1);

				logger.info(" indexNum " +indexNum);
			}
		}*/
	}

}
