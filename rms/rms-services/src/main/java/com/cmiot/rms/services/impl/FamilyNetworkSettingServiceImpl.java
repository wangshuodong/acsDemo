package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.FamilyNetworkSettingService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.HomeNetworkConfigService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.util.InstructionUtil;



/**
 * 功能:家庭内网配置管理接口实现
 */
public class FamilyNetworkSettingServiceImpl implements FamilyNetworkSettingService{

	@Resource
	private InstructionMethodService instructionMethodService;
	@Resource
	private HomeNetworkConfigService homeNetworkConfigService;
	@Resource
	private GatewayInfoService gatewayInfoService;
	
	
	
	Logger logger = Logger.getLogger(FamilyNetworkSettingServiceImpl.class);
	/**
	 * 设置网关设备名称
	 */
	@Override
	public Map<String, Object> setHgName(Map<String, Object> params) {
		//InternetGatewayDevice.DeviceInfo.X_CMCC_CustomiseName
		Map<String,Object> backMap=new HashMap<String,Object>();
		backMap.put("ID", params.get("ID"));
		backMap.put("CmdType", params.get("CmdType"));
		backMap.put("SequenceId", params.get("SequenceId"));
		backMap.put("ResultData", new HashMap());
		Map parameter=(Map)params.get("Parameter");
		try {
			//网关MAC和网关各称必须存在
			if(parameter.get("MAC")!=null && parameter.get("DevName")!=null){
				
				if(queryGatewayInfoByMac(parameter.get("MAC").toString()) == null){
					backMap.put("Result", -201);
					Map<String, Object> resultData  = new HashMap<String, Object>();
		        	resultData.put("FailReason", "网关MAC地址不存在");
		        	backMap.put("ResultData", resultData);
					return backMap;
				}
				
				String mac=parameter.get("MAC").toString().trim();
				String name=parameter.get("DevName").toString().trim();
				List<ParameterValueStruct> list=new ArrayList<ParameterValueStruct>();
				ParameterValueStruct pvStruct=new ParameterValueStruct();
				pvStruct.setName("InternetGatewayDevice.DeviceInfo.X_CMCC_CustomiseName");
				pvStruct.setValue(name);
				pvStruct.setValueType("string");
				pvStruct.setReadWrite(true);
				list.add(pvStruct);
				
				boolean flag=instructionMethodService.setParameterValue(mac, list);
				if(flag){
					backMap.put("Result", 0);
				}else{
					backMap.put("Result", -400);
					Map<String, Object> resultData  = new HashMap<String, Object>();
		        	resultData.put("FailReason", "网关设置参数失败");
		        	backMap.put("ResultData", resultData);
				}
			}else{
				backMap.put("Result", -102);
				Map<String, Object> resultData  = new HashMap<String, Object>();
	        	resultData.put("FailReason", "参数错误");
	        	backMap.put("ResultData", resultData);
			}
			
			return backMap;
		} catch (Exception e) {
			backMap.put("Result", -400);
			Map<String, Object> resultData  = new HashMap<String, Object>();
        	resultData.put("FailReason", "服务器内部错误");
        	backMap.put("ResultData", resultData);
			return backMap;
		}
	}

	/**
	 * 设置下挂的设备别名
	 */
	@Override
	public Map<String, Object> setLanDeviceName(Map<String, Object> params) {
		//InternetGatewayDevice.LANDevice.{i}.X_CMCC_HostCustomise.{i}.
		Map<String,Object> backMap=new HashMap<String,Object>();
		backMap.put("ID", params.get("ID"));
		backMap.put("CmdType", params.get("CmdType"));
		backMap.put("SequenceId", params.get("SequenceId"));
		backMap.put("ResultData", new HashMap());
		Map parameter=(Map)params.get("Parameter");
		try {
			//网关MAC、下挂设备名称和下挂设备MAC必须存在
 			if(parameter.get("MAC")!=null && parameter.get("DevName")!=null && parameter.get("DeviceMAC")!=null){
				//获取网关属性
				String mac=parameter.get("MAC").toString().trim();
				String devName=parameter.get("DevName").toString().trim();
				String devMac=parameter.get("DeviceMAC").toString().trim();
				
				if(queryGatewayInfoByMac(mac) == null){
					backMap.put("Result", -201);
					Map<String, Object> resultData  = new HashMap<String, Object>();
		        	resultData.put("FailReason", "网关MAC地址不存在");
		        	backMap.put("ResultData", resultData);
					return backMap;
				}
				
				Map<String,Object> map=instructionMethodService.getParameterNames(mac, "InternetGatewayDevice.LANDevice.", false);	
				
				String attributeDevMac="InternetGatewayDevice.LANDevice.[0-9]+.X_CMCC_HostCustomise.[0-9]+.MACAddress";
				String shortAttributeDevMac="InternetGatewayDevice.LANDevice.[0-9]+.";
				//String attributeDevName="InternetGatewayDevice.LANDevice.[0-9].X_CMCC_HostCustomise.[0-9].Name";
				
				String attributeDevName="";
				List<String> names = new ArrayList<String>();
				Set<Map.Entry<String, Object>> keys = map.entrySet();
				for (Map.Entry<String, Object> entry : keys) {
					if(entry.getKey().matches(attributeDevMac)){
						names.add(entry.getKey());
					}
				}
				Map<String, Object> maps =	instructionMethodService.getParameterValues(mac, names);
				for (Map.Entry<String, Object> entry : maps.entrySet()) {
					if(entry.getValue().toString().trim().equalsIgnoreCase(devMac)){
						String attribute = entry.getKey();
						attributeDevName=attribute.substring(0, attribute.lastIndexOf("."))+".Name";
						break;
					}
				}
				
				if(!"".equals(attributeDevName)){
					List<ParameterValueStruct> list=new ArrayList<ParameterValueStruct>();
					ParameterValueStruct pvStruct=new ParameterValueStruct();
					pvStruct.setName(attributeDevName);
					pvStruct.setValue(devName);
					pvStruct.setValueType("string");
					pvStruct.setReadWrite(true);
					list.add(pvStruct);
					boolean flag=instructionMethodService.setParameterValue(mac, list);				
					if(flag){
						backMap.put("Result", 0);
					}else{
						backMap.put("Result", -400);
						Map<String, Object> resultData  = new HashMap<String, Object>();
			        	resultData.put("FailReason", "设置网关参数失败");
			        	backMap.put("ResultData", resultData);
					}
				}else{
					//别名节点不存在，添加节点
					//设备MAC地址
					String MACAddress = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.MACAddress";

					List<String> list=new ArrayList<String>();
					for (Map.Entry<String, Object> entry : map.entrySet()) {
						if(entry.getKey().matches(MACAddress) ){

							list.add(entry.getKey().trim());
						}
					}

					Map<String, Object> retMap=instructionMethodService.getParameterValues(mac, list);

					String url = "";
					for (Map.Entry<String, Object> entry : retMap.entrySet()) {
						if(entry.getKey().matches(MACAddress)){
							String attribute=entry.getKey();
							String deviceMac = entry.getValue().toString();
							if(parameter.get("DeviceMAC").toString().equals(deviceMac)){
								url = entry.getKey();
								break;
							}
						}
					}

					if(!"".equals(url)){
						String index = url.substring(url.indexOf("InternetGatewayDevice.LANDevice.")+"InternetGatewayDevice.LANDevice.".length(), url.length());
						index = index.substring(0,index.indexOf("."));
						int i = instructionMethodService.AddObject( parameter.get("MAC").toString(), "InternetGatewayDevice.LANDevice."+index+".X_CMCC_HostCustomise.", System.currentTimeMillis()+"");
						//TODO 这里添加失败，先当成添加成功
						if(i < 0){
							backMap.put("Result", -400);
							Map<String, Object> resultData  = new HashMap<String, Object>();
				        	resultData.put("FailReason", "增加网关节点失败");
				        	backMap.put("ResultData", resultData);
							return backMap;
						}
						List<ParameterValueStruct> setList =new ArrayList<ParameterValueStruct>();
						ParameterValueStruct pvStruct=new ParameterValueStruct();
						pvStruct.setName("InternetGatewayDevice.LANDevice."+index+".X_CMCC_HostCustomise."+i+".Name");
						pvStruct.setValue(parameter.get("DevName").toString());
						pvStruct.setValueType("string");
						pvStruct.setReadWrite(true);
						setList.add(pvStruct);
						
						ParameterValueStruct pvStructMac=new ParameterValueStruct();
						pvStructMac.setName("InternetGatewayDevice.LANDevice."+index+".X_CMCC_HostCustomise."+i+".MACAddress");
						pvStructMac.setValue(parameter.get("DeviceMAC").toString());
						pvStructMac.setValueType("string");
						pvStructMac.setReadWrite(true);
						setList.add(pvStructMac);
						boolean flag=instructionMethodService.setParameterValue(mac, setList);
						if(flag){
							backMap.put("Result", 0);
						}else{
							backMap.put("Result", -205);
							Map<String, Object> resultData  = new HashMap<String, Object>();
				        	resultData.put("FailReason", "设置网关参数失败");
				        	backMap.put("ResultData", resultData);
						}
						return backMap;
					}else{
						logger.info("设置下挂的设备别名失败：下挂设备mac不存在！");
						backMap.put("Result", -202);
						Map<String, Object> resultData  = new HashMap<String, Object>();
			        	resultData.put("FailReason", "网关下设备MAC地址不存在");
			        	backMap.put("ResultData", resultData);
					}
				
				}
			}else{
				backMap.put("Result", -102);
				Map<String, Object> resultData  = new HashMap<String, Object>();
	        	resultData.put("FailReason", "参数错误");
	        	backMap.put("ResultData", resultData);
			}
			return backMap;
		} catch (Exception e) {
			backMap.put("Result", -400);
			Map<String, Object> resultData  = new HashMap<String, Object>();
        	resultData.put("FailReason", "服务器内部错误");
        	backMap.put("ResultData", resultData);
			return backMap;
		}
	}

	/**
	 * 获取网关和下挂的设备名称
	 */
	@Override
	public Map<String, Object> getHgNamelist(Map<String, Object> params) {
		Map<String,Object> backMap=new HashMap<String,Object>();
		backMap.put("ID", params.get("ID"));
		backMap.put("CmdType", params.get("CmdType"));
		backMap.put("SequenceId", params.get("SequenceId"));
		Map ResultData=new HashMap();
		backMap.put("ResultData", ResultData);
		Map parameter=(Map)params.get("Parameter");
		try {
			//网关MAC、下挂设备名称和下挂设备MAC必须存在
			if(parameter.get("MAC")!=null ){
				//获取网关属性
				String mac=parameter.get("MAC").toString().trim();
				
				if(queryGatewayInfoByMac(mac) == null){
					backMap.put("Result", -201);
					Map<String, Object> resultData = new HashMap<String, Object>();
					resultData.put("FailReason", "网关MAC地址不存在");
					backMap.put("ResultData", resultData);
					return backMap;
				}
				
				Map<String,Object> map=instructionMethodService.getParameterNames(mac, "InternetGatewayDevice.LANDevice.", false);	
				
				String attributeDevMac="InternetGatewayDevice.LANDevice.[0-9].X_CMCC_HostCustomise.[0-9].MACAddress";
				String attributeDevName="InternetGatewayDevice.LANDevice.[0-9].X_CMCC_HostCustomise.[0-9].Name";

				//String attributeDevHostName="InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.HostName";
				//设备MAC地址
				String MACAddress = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.MACAddress";

				List<String> list=new ArrayList<String>();
				for (Map.Entry<String, Object> entry : map.entrySet()) {  
				if(entry.getKey().matches(attributeDevMac) 
						|| entry.getKey().matches(attributeDevName)
						|| entry.getKey().matches(MACAddress)
						){

						list.add(entry.getKey().trim());
					}
				}
				
				Map<String, Object> retMap=instructionMethodService.getParameterValues(mac, list);
				List newNameList=new ArrayList();
				//是否包含网关本身
				boolean flag = false;
				List nameList=new ArrayList();
				for (Map.Entry<String, Object> entry : retMap.entrySet()) {
					if(entry.getKey().matches(MACAddress)){
						String devMac = entry.getValue()==null?null:entry.getValue().toString();
						if(devMac != null){
							if(mac.equals(devMac)){
								flag = true;
								boolean isExist = false;
								for (Map.Entry<String, Object> entry1 : retMap.entrySet()) {

									if(entry1.getKey().matches(attributeDevMac)){
										if(devMac.equals(entry1.getValue())){
											isExist = true;
											//如果下挂设备 存在别名
											attributeDevName=entry1.getKey().substring(0, entry1.getKey().lastIndexOf("."))+".Name";
											Map mapDev=new HashMap();
											mapDev.put("DeviceMAC", devMac);
											mapDev.put("DevName", retMap.get(attributeDevName));
											newNameList.add(mapDev);

										}
									}
								}
								if(!isExist){
									Map mapDev=new HashMap();
									mapDev.put("DeviceMAC", devMac);
									mapDev.put("DevName", "");
									newNameList.add(mapDev);
								}
							}else{
								boolean isExist = false;
								for (Map.Entry<String, Object> entry1 : retMap.entrySet()) {

									if(entry1.getKey().matches(attributeDevMac)){
										if(devMac.equals(entry1.getValue())){
											isExist = true;
											//如果下挂设备 存在别名
											attributeDevName=entry1.getKey().substring(0, entry1.getKey().lastIndexOf("."))+".Name";
											Map mapDev=new HashMap();
											mapDev.put("DeviceMAC", devMac);
											mapDev.put("DevName", retMap.get(attributeDevName));
											nameList.add(mapDev);

										}
									}
								}
								if(!isExist){
									Map mapDev=new HashMap();
									mapDev.put("DeviceMAC", devMac);
									mapDev.put("DevName", "");
									nameList.add(mapDev);
								}

							}

						}
						
					}
					
				}

				//把网关本身添加到返回列表中
				if(!flag){
					Map mapDev=new HashMap();
					mapDev.put("DeviceMAC", mac);
					list = new ArrayList<String>();
					list.add("InternetGatewayDevice.DeviceInfo.X_CMCC_CustomiseName");
					retMap=instructionMethodService.getParameterValues(mac, list);
					if(retMap != null && !retMap.isEmpty()){
						mapDev.put("DevName", retMap.get("InternetGatewayDevice.DeviceInfo.X_CMCC_CustomiseName"));
					}else{
						mapDev.put("DevName", "");
					}
					newNameList.add(mapDev);
				}
				newNameList.addAll(nameList);

				ResultData.put("NameList", newNameList);
				
				backMap.put("Result", 0);
				backMap.put("ResultData", ResultData);
			}else{
				backMap.put("Result", -102);
				Map<String, Object> resultData = new HashMap<String, Object>();
				resultData.put("FailReason", "参数错误");
				backMap.put("ResultData", resultData);
			}
			return backMap;
		} catch (Exception e) {
			backMap.put("Result", -400);
			Map<String, Object> resultData = new HashMap<String, Object>();
			resultData.put("FailReason", "服务器内部错误");
			backMap.put("ResultData", resultData);
			return backMap;
		}
	}

	/**
	 * 获取家庭内网拓扑信息
	 */
	@Override
	public Map<String, Object> getLanNetInfo(Map<String, Object> params) {
		//InternetGatewayDevice.LANDevice.{i}.X_CMCC_HostCustomise.{i}.
		Map<String,Object> backMap=new HashMap<String,Object>();
		backMap.put("ID", params.get("ID"));
		backMap.put("CmdType", params.get("CmdType"));
		backMap.put("SequenceId", params.get("SequenceId"));
		Map ResultData=new HashMap();
		backMap.put("ResultData", ResultData);
		Map parameter=(Map)params.get("Parameter");
		
		
		try {
			List<String> blackMacList = null;
			//网关MAC、下挂设备名称和下挂设备MAC必须存在
			if(parameter.get("MAC")!=null ){
				
				if(queryGatewayInfoByMac(parameter.get("MAC").toString()) == null){
					backMap.put("Result", -201);
					Map<String, Object> resultData  = new HashMap<String, Object>();
		        	resultData.put("FailReason", "网关MAC地址不存在");
		        	backMap.put("ResultData", resultData);
					return backMap;
				}
				
				
				//获取网络黑名单列表getLanAccessNet
				Map<String, Object> netBlackList =  homeNetworkConfigService.getLanAccessNet(params);
				if(netBlackList != null && netBlackList.get(Constant.RESULT) != null && Integer.parseInt(netBlackList.get(Constant.RESULT).toString()) == 0){
					if(netBlackList.get(Constant.RESULTDATA) != null){
						Map<String, Object> map =   (Map<String, Object>) netBlackList.get(Constant.RESULTDATA);
						blackMacList = (List<String>) map.get("DeviceMACList");
					}
				}
				//获取网关属性
				String mac=parameter.get("MAC").toString().trim();

//				Map<String,Object> map=instructionMethodService.getParameterNames(mac, "InternetGatewayDevice.LANDevice.", false);

				//获取LANDevice前缀
				String lanDevicePrefix = instructionMethodService.getLANDevicePrefix();

//				//设备名称
//				String attributeDevName= lanDevicePrefix + ".X_CMCC_HostCustomise.[0-9]+.Name";
				//设备MAC
				String attributeDevMac= lanDevicePrefix + "X_CMCC_HostCustomise.[0-9]+.MACAddress";

//				//设备HostName
//				String attributeDevHostName= lanDevicePrefix + "Hosts.Host.[0-9]+.HostName";
//				//设备IP
//				String attributeDevIp=lanDevicePrefix + ".Hosts.Host.[0-9]+.IPAddress";
				//设备MAC地址
				String MACAddress = lanDevicePrefix + "Hosts.Host.[0-9]+.MACAddress";
				//设备连接形式Ethernet 、802.11、Other
//				String InterfaceType = lanDevicePrefix + ".Hosts.Host.[0-9]+.InterfaceType";
				//有线连接Lan口
				//String lanIndex = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.Layer2Interface";
				//有线物理接口mac
				String  lanMacAddress = lanDevicePrefix +  "LANEthernetInterfaceConfig.[0-9]+.MACAddress";
				//接口的MAC地址
				String  wlanMacAddress = lanDevicePrefix + "WLANConfiguration.[0-9]+.AssociatedDevice.[0-9]+.AssociatedDeviceMACAddress";

				List<String> list=new ArrayList<>();
				Map<String,Object> mapHostCustomise = instructionMethodService.getParameterNames(mac, lanDevicePrefix + "X_CMCC_HostCustomise.", true);
				for (Map.Entry<String, Object> entry : mapHostCustomise.entrySet()) {
					list.add(entry.getKey().trim() + "Name");
					list.add(entry.getKey().trim() + "MACAddress");
				}

				Map<String,Object> mapHost = instructionMethodService.getParameterNames(mac, lanDevicePrefix + "Hosts.Host.", true);
				for (Map.Entry<String, Object> entry : mapHost.entrySet()) {
					list.add(entry.getKey().trim() + "HostName");
					list.add(entry.getKey().trim() + "IPAddress");
					list.add(entry.getKey().trim() + "MACAddress");
					list.add(entry.getKey().trim() + "InterfaceType");
				}

				Map<String,Object> mapLANConfig = instructionMethodService.getParameterNames(mac, lanDevicePrefix +  "LANEthernetInterfaceConfig.", true);
				for (Map.Entry<String, Object> entry : mapLANConfig.entrySet()) {
					list.add(entry.getKey().trim() + "MACAddress");
				}

				List<String> wlanconfigList = new ArrayList<>();
				Map<String,Object> mapWLANConfiguration = instructionMethodService.getParameterNames(mac, lanDevicePrefix +  "WLANConfiguration.", true);
				for (Map.Entry<String, Object> entry : mapWLANConfiguration.entrySet()) {
					wlanconfigList.add(entry.getKey().trim() + "AssociatedDevice.");
				}

				for(String wlanconfigStr : wlanconfigList)
				{
					Map<String,Object> mapAssociatedDevice = instructionMethodService.getParameterNames(mac, wlanconfigStr, true);
					for (Map.Entry<String, Object> entry : mapAssociatedDevice.entrySet()) {
						list.add(entry.getKey().trim() + "AssociatedDeviceMACAddress");
					}
				}

				Map<String, Object> retMap=instructionMethodService.getParameterValues(mac, list);

				List nameList=new ArrayList();
				
				//mac为key，  name为value
				Map<String, Object> macMap = new HashMap<String, Object>();
						
				for (Map.Entry<String, Object> entry : retMap.entrySet()) {
					
					if(entry.getKey().matches(attributeDevMac)){
						macMap.put(entry.getValue().toString(), entry.getKey());
					}
				}
				for (Map.Entry<String, Object> entry : retMap.entrySet()) {
					
					if(entry.getKey().matches(MACAddress)){
						String attribute1 = entry.getKey();
						String deviceMac = entry.getValue().toString();
						Map mapInfo=new HashMap<>();
						//获取别名
						if(macMap != null && !macMap.isEmpty()){
							String name = macMap.get(deviceMac) == null ? "" : macMap.get(deviceMac).toString();
							if(!"".equals(name)){
								//得到别名URL
								String devNameUrl = name.substring(0, name.lastIndexOf("."))+".Name";
								
								mapInfo.put("Devname",retMap.get(devNameUrl));
							}else{
								mapInfo.put("Devname",retMap.get(""));
							}
						}
						
						//MAC为下挂终端的MAC地址
						mapInfo.put("DeviceMAC", deviceMac);
						String ipaddress=attribute1.substring(0, attribute1.lastIndexOf("."))+".IPAddress";
						//IP为下挂终端的IP地址
						mapInfo.put("IP", retMap.get(ipaddress));
						String hostName=attribute1.substring(0, attribute1.lastIndexOf("."))+".HostName";
						//DevHostname为设备hostname
						mapInfo.put("DevHostname", retMap.get(hostName));
						//AccessInternet 是否允许访问网络	1:允许访问网络 0:禁止访问网络
						if(blackMacList == null){
							mapInfo.put("AccessInternet", "1");
						}else{
							if(blackMacList.contains(deviceMac)){
								mapInfo.put("AccessInternet", "0");
							}else{
								mapInfo.put("AccessInternet", "1");
							}
						}
						//ConnectType为下挂终端和网关的连接形式；0：有线/1：无线
						String interfaceType=attribute1.substring(0, attribute1.lastIndexOf("."))+".InterfaceType";
						if("Ethernet".equals(retMap.get(interfaceType))){
							mapInfo.put("ConnectType", "0");
							
							/*for (Map.Entry<String, Object> et : retMap.entrySet()) {
								if(et.getKey().matches(lanMacAddress) &&   entry.getValue().equals(et.getValue()) ){
									String url = et.getKey();
									//获取URL第二个i节点的值， 则为LAN口的顺序
									url =url.substring(0,url.lastIndexOf("."));
									String ix = url.substring(url.lastIndexOf(".")+1);
									mapInfo.put("Port", "lan"+ix);
									break;
								}
							}*/
						}else {
							mapInfo.put("ConnectType", "1");
							
							for (Map.Entry<String, Object> et : retMap.entrySet()) {
								if(et.getKey().matches(wlanMacAddress) &&   entry.getValue().toString().equalsIgnoreCase(et.getValue().toString()) ){
									
									String url = et.getKey();
									//.[0-9]+.AssociatedDevice
									String i = url.substring(url.indexOf("WLANConfiguration.")+"WLANConfiguration.".length(), url.indexOf(".AssociatedDevice"));
									mapInfo.put("SSIDIndex", i);
									break;
								}
							}
						}
						mapInfo.put("Port", null);
						mapInfo.put("PowerLevel", null);
						mapInfo.put("OnlineTime", null);
						mapInfo.put("AccessStorage ", null);
						if(!mapInfo.containsKey("SSIDIndex")){
							
							mapInfo.put("SSIDIndex", null);
						}
						nameList.add(mapInfo);
					}
				}
				if(nameList.size() > 0){
					
					ResultData.put("Info", nameList);
					ResultData.put("Num", nameList.size()+"");
				}

				backMap.put("Result", 0);
				backMap.put("ResultData", ResultData);
			}else{
				backMap.put("Result", -102);
				Map<String, Object> resultData  = new HashMap<String, Object>();
	        	resultData.put("FailReason", "参数错误");
	        	backMap.put("ResultData", resultData);
			}
			return backMap;
		} catch (Exception e) {
			backMap.put("Result", -400);
			Map<String, Object> resultData  = new HashMap<String, Object>();
        	resultData.put("FailReason", "服务器内部错误");
        	backMap.put("ResultData", resultData);
			return backMap;
		}
	}

	@Override
	public Map<String, Object> setLanAccess(Map<String, Object> parameter) {
		
        //1.获取网关MAC
        Map<String, Object> parameterMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = null != parameterMap.get(Constant.MAC) ? parameterMap.get(Constant.MAC).toString() : "";
        String DeviceMAC = null != parameterMap.get("DeviceMAC") ? parameterMap.get("DeviceMAC").toString() : "";
        String NetAccessRight = null != parameterMap.get("NetAccessRight") ? parameterMap.get("NetAccessRight").toString() : "";
        String StorageAccessRight = null != parameterMap.get("StorageAccessRight") ? parameterMap.get("StorageAccessRight").toString() : "";

        String returnNetAccessRight = NetAccessRight;
        
        parameter.remove(Constant.PARAMETER);
        parameter.remove(Constant.RPCMETHOD);
        if("".equals(gatewayMacAddress) || "".equals(DeviceMAC) || "".equals(NetAccessRight) ||"".equals(StorageAccessRight)){
        	
        	parameter.put(Constant.RESULT, -102);
        	Map<String, Object> resultData  = new HashMap<String, Object>();
        	resultData.put("FailReason", "参数错误");
        	parameter.put("ResultData", resultData);
        	return parameter;
        }
        
        if(queryGatewayInfoByMac(gatewayMacAddress) == null){
        	parameter.put("Result", -201);
        	Map<String, Object> resultData  = new HashMap<String, Object>();
        	resultData.put("FailReason", "网关MAC地址不存在");
        	parameter.put("ResultData", resultData);
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
        					returnNetAccessRight = "OFF";
        					retMap.put("Result", 0);
        				}else{
        					returnNetAccessRight="ON";
        					retMap.put(Constant.RESULT, -400);
        					Map<String, Object> resultData  = new HashMap<String, Object>();
        		        	resultData.put("FailReason", "网关设置参数失败");
        		        	retMap.put("ResultData", resultData);
        					return retMap;
        				}
        			}else{
        				returnNetAccessRight="ON";
        				retMap.put(Constant.RESULT, -400);
        				Map<String, Object> resultData  = new HashMap<String, Object>();
    		        	resultData.put("FailReason", "网关新增节点失败");
    		        	retMap.put("ResultData", resultData);
        				return retMap;
        			}
        		}else{
        			returnNetAccessRight="ON";
        			retMap.put(Constant.RESULT, -400);
        			Map<String, Object> resultData  = new HashMap<String, Object>();
		        	resultData.put("FailReason", "网关设置参数失败");
		        	retMap.put("ResultData", resultData);
        			return retMap;
        		}
        	}else{
        		returnNetAccessRight="ON";
        		retMap.put(Constant.RESULT, -400);
        		Map<String, Object> resultData  = new HashMap<String, Object>();
	        	resultData.put("FailReason", "网关新增节点失败");
	        	retMap.put("ResultData", resultData);
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

        return retMap;
	}
	public GatewayInfo queryGatewayInfoByMac(String macAddress){
		
		GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayMacaddress(macAddress);
        GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        return gatewayInfo;
	}
}
