package com.cmiot.rms.services.impl;

import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.acs.model.struct.SetParameterAttributesStruct;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.HomeNetworkConfigService;
import com.cmiot.rms.services.SyncInfoToFirstLevelPlatformService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.InstructionUtil;
import com.iot.common.date.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  家庭内网配置管理
 * Created by panmingguo on 2016/5/6.
 */
public class HomeNetworkConfigServiceImpl implements HomeNetworkConfigService {
    private static Logger logger = LoggerFactory.getLogger(HomeNetworkConfigServiceImpl.class);

    @Autowired
    private GatewayInfoService gatewayInfoService;

    @Autowired
    InstructionMethodService instructionMethodService;

    @Autowired
    RedisClientTemplate redisClientTemplate;

    @Autowired
    SyncInfoToFirstLevelPlatformService syncInfoToFirstLevelPlatformService;

    @Value("${first.level.platform.url}")
    String url;

    /**
     * 获取网关下挂终端的网络访问控制名单
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> getLanAccessNet(Map<String, Object> parameter) {
        logger.info("Start invoke getLanAccessNet:{}", parameter);
        //1.获取网关MAC
        Map<String, Object> parameterMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = null != parameterMap.get(Constant.MAC) ? parameterMap.get(Constant.MAC).toString() : "";
        Map<String, Object> retMap = InstructionUtil.getResultMap(parameter);
        if(StringUtils.isEmpty(gatewayMacAddress))
        {
            retMap.put(Constant.RESULT, -102);
            return retMap;
        }

        if(null == gatewayIsExist(gatewayMacAddress))
        {
            retMap.put(Constant.RESULT, -201);
            return retMap;
        }
        /**本实现不能满足业务
        String mode = "InternetGatewayDevice.Services.X_CMCC_MWBAND.Mode";
        String terminalType = "InternetGatewayDevice.Services.X_CMCC_MWBAND.TerminalType";
        List<String> nameList = new ArrayList<>();
        nameList.add(mode);
        nameList.add(terminalType);

        //2.获取限制同时接入公网的终端数量的方式 0：代表采用源IP方式； 1：代表采用源MAC方式
        Map<String, Object> valueMap = instructionMethodService.getParameterValues(gatewayMacAddress, nameList);
        logger.info("getLanAccessNet getParameterValues valueMap:{}", valueMap);

        //3.采用MAC方式时进行下一步查询，否则返回空
        if((null == valueMap.get(mode) || 1 != Integer.valueOf(valueMap.get(mode).toString()))
                || (null == valueMap.get(terminalType) || 1 != Integer.valueOf(valueMap.get(terminalType).toString())))
        {
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("DeviceMACList", new ArrayList<>());
            retMap.put(Constant.RESULTDATA, resultData);
            return retMap;
        }


        //4.获取lan节点名称
        Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayMacAddress,
                "InternetGatewayDevice.LANInterfaces.LANEthernetInterfaceConfig.", false);

        logger.info("getLanAccessNet getParameterNames nameMap:{}", nameMap);

        String regFilter ="InternetGatewayDevice.LANInterfaces.LANEthernetInterfaceConfig.[0-9]+.X_CMCC_EtherTypeFilter";
        String regSource ="InternetGatewayDevice.LANInterfaces.LANEthernetInterfaceConfig.[0-9]+.SourceMAC";
        String regDestination ="InternetGatewayDevice.LANInterfaces.LANEthernetInterfaceConfig.[0-9]+.DestinationMAC";

        List<String> regNameList = new ArrayList<>();
        regNameList.add(regFilter);
        regNameList.add(regSource);
        regNameList.add(regDestination);

        //5.根据正则表达式匹配找到全路径
        List<String> lanNameList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
            InstructionUtil.getName(lanNameList, regNameList, entry.getKey());
        }

        logger.info("getLanAccessNet lanNameList:{}", lanNameList);

        //6.获取值
        Map<String, Object> lanValueMap = instructionMethodService.getParameterValues(gatewayMacAddress, lanNameList);

        logger.info("getLanAccessNet getParameterValues lanValueMap:{}", lanValueMap);

        List<String> macList = new ArrayList<>();
        for(Map.Entry<String, Object> entry : lanValueMap.entrySet())
        {
            if(entry.getKey().endsWith("SourceMAC"))
            {
                macList.add(entry.getValue().toString());
            }
        }

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("DeviceMACList", macList);
        retMap.put(Constant.RESULTDATA, resultData);
		**/
        //获取家长控制节点列表
        Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayMacAddress,
                "InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.MAC.", false);
        logger.info("getLanAccessNet getParameterNames nameMap:{}", nameMap);
        
        String regMac ="InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.MAC.[0-9]+.MACAddress";
        String regTemplate ="InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.MAC.[0-9]+.TemplateInst";

        List<String> regNameList = new ArrayList<>();
        regNameList.add(regMac);
        regNameList.add(regTemplate);

        List<String> macList = new ArrayList<>();
        //根据正则表达式匹配找到全路径
        List<String> lanNameList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
            InstructionUtil.getName(lanNameList, regNameList, entry.getKey());
        }
        logger.info("getLanAccessNet lanNameList:{}", lanNameList);
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("DeviceMACList", macList);
        retMap.put(Constant.RESULTDATA, resultData);
        if(!lanNameList.isEmpty() && lanNameList.size() > 0){
        	Map<String, Object> tempMap = new HashMap<String, Object>();
        	Map<String, Object> lanValueMap = instructionMethodService.getParameterValues(gatewayMacAddress, lanNameList);
            logger.info("getLanAccessNet getParameterValues lanValueMap:{}", lanValueMap);

            for(Map.Entry<String, Object> entry : lanValueMap.entrySet())
            {	
            	String macKey = entry.getKey().toString();
                if(macKey.endsWith("MACAddress"))
                {
                	tempMap.put(entry.getValue().toString(), lanValueMap.get(macKey.substring(0, macKey.indexOf("MACAddress")) + "TemplateInst").toString());
                }
                
            }
            List<String> tempNodeList = new ArrayList<>();
        	for(Map.Entry<String, Object> et1 : tempMap.entrySet()){
        		tempNodeList.add("InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.Templates." + et1.getValue().toString() + ".UrlFilterPolicy");
        		tempNodeList.add("InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.Templates." + et1.getValue().toString() + ".UrlFilterRight");
        	}
            //获取策略
            Map<String, Object> valueMap = instructionMethodService.getParameterValues(gatewayMacAddress, tempNodeList);
            logger.info("getLanAccessNet getParameterNames valueMap:{}", valueMap);
            for(Map.Entry<String, Object> entry : tempMap.entrySet()){
            	boolean isBlack = false,isOn = false;
            	String filterPolicyKey = "InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.Templates." + entry.getValue().toString() + ".UrlFilterPolicy";
            	String filterRightKey = "InternetGatewayDevice.X_CMCC_Security.ParentalCtrl.Templates." + entry.getValue().toString() + ".UrlFilterRight";;
        		if(null != valueMap.get(filterPolicyKey) && Boolean.valueOf(valueMap.get(filterPolicyKey).toString()) == false){
        			isBlack = true;           				
        		}
        		if(null != valueMap.get(filterRightKey) && Boolean.valueOf(valueMap.get(filterRightKey).toString()) == true){
        			isOn = true; 
        		}
            	if(isBlack && isOn){
            		macList.add(entry.getKey().toString());
            	}
            }
            
        }
        
        
        logger.info("End invoke getLanAccessNet:{}", retMap);
        return retMap;
    }

    /**
     * 获取网关下挂终端的存储访问控制名单
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> getLanAccessStorage(Map<String, Object> parameter) {
        logger.info("Start invoke getLanAccessStorage:{}", parameter);
        Map<String, Object> parameterMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = null != parameterMap.get(Constant.MAC) ? parameterMap.get(Constant.MAC).toString() : "";
        Map<String, Object> retMap = InstructionUtil.getResultMap(parameter);
        if(StringUtils.isEmpty(gatewayMacAddress))
        {
            retMap.put(Constant.RESULT, -102);
            return retMap;
        }

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("DeviceMACList", new ArrayList<>());
        retMap.put(Constant.RESULTDATA, resultData);

        logger.info("End invoke getLanAccessStorage:{}", retMap);
        return retMap;
    }

    /**
     * 配置网关下挂终端上线消息上报策略
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> setLanDeviceOnline(Map<String, Object> parameter) {
        logger.info("Start invoke setLanDeviceOnline:{}", parameter);
        //1.获取网关MAC
        Map<String, Object> parameterMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = null != parameterMap.get(Constant.MAC) ? parameterMap.get(Constant.MAC).toString() : "";

        Map<String, Object> retMap = InstructionUtil.getResultMap(parameter);
        List<Map<String, Object>> devices = null != parameterMap.get("Devices") ? (List<Map<String, Object>>)parameterMap.get("Devices"): null;
        if(StringUtils.isEmpty(gatewayMacAddress) || null == devices)
        {
            retMap.put(Constant.RESULT, -102);
            return retMap;
        }

        GatewayInfo gatewayInfo = gatewayIsExist(gatewayMacAddress);
        if(null == gatewayInfo)
        {
            retMap.put(Constant.RESULT, -201);
            return retMap;
        }

        //2.获取lan节点名称
        Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);

        logger.info("setLanDeviceOnline getParameterNames nameMap:{}", nameMap);

        String regMACAddress = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.MACAddress";
        String regActive = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.Active";
        String regNumber = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.HostNumberOfEntries";

        String regIPAddress = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.IPAddress";
        String regInterfaceType= "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.InterfaceType";
        String regHostName = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.HostName";
        
        List<String> regNameList = new ArrayList<>();
        regNameList.add(regMACAddress);
        regNameList.add(regActive);
        regNameList.add(regNumber);
        regNameList.add(regIPAddress);
        regNameList.add(regInterfaceType);
        regNameList.add(regHostName);
        
        List<String> nameList = new ArrayList<>();

        //3.根据正则表达式匹配查找真正的lan节点名称
        for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
            InstructionUtil.getName(nameList, regNameList, entry.getKey());
        }
        logger.info("setLanDeviceOnline nameList:{}", nameList);

        List<SetParameterAttributesStruct> setParamAttrList = new ArrayList<>();

        Boolean isAllconfig = isAllConfig(devices);
        logger.info("setLanDeviceOnline isAllconfig:{}", isAllconfig);

        //4.对HostNumberOfEntries进行监控，当HostNumberOfEntries值改变时，说明有设备上下线
        // 华为网关设备下线时节点仍存在，监控Active判断上下线
        for(String name : nameList)
        {
            if(name.endsWith("HostNumberOfEntries")
                    || (isAllconfig && name.endsWith("Active")))
            {
                setStruct(name, 2, setParamAttrList);
            }
        }

        // 获取下挂设备值
        Map<String, Object> lanValueMap = instructionMethodService.getParameterValues(gatewayMacAddress, nameList);
        logger.info("setLanDeviceOnline getParameterValues lanValueMap:{}", lanValueMap);

        for(Map.Entry<String, Object> entry : lanValueMap.entrySet())
        {
            for(Map<String, Object> device : devices)
            {
                if(entry.getKey().endsWith("MACAddress") && entry.getValue().equals(device.get("DeviceMAC").toString()))
                {
                    String preMac = entry.getKey().substring(0, entry.getKey().indexOf("MACAddress"));
                    setStruct(preMac + "Active", Integer.valueOf(device.get("Enable").toString()), setParamAttrList);
                }
            }
        }

        logger.info("setLanDeviceOnline setParamAttrList:{}", setParamAttrList);

        //5.设置value change监控属性
        Boolean bRet = instructionMethodService.SetParameterAttributes(gatewayMacAddress, setParamAttrList);

        logger.info("setLanDeviceOnline bRet:{}", bRet);

        //6.拼装返回值
        Map<String, Object> resultData = new HashMap<>();
        //resultData.put("MAC", gatewayMacAddress);
        //返回配置的下挂设备的MAC和设置状态
        retMap.put(Constant.RESULTDATA, resultData);
        if(bRet)
        {
            String key = getDeviceKey(gatewayInfo);

            //保存上线通知
             saveHangingDevice(isAllconfig, key, lanValueMap, devices);
            //将存在的设备上报一级平台
            reportOnlineToFirst(isAllconfig, lanValueMap, devices, gatewayInfo);

            resultData.put("MAC", gatewayMacAddress);

            if(isAllDisable(devices))
            {
                resultData.put("Enable", "0");
            }
            else
            {
                resultData.put("Enable", "1");
            }
            retMap.put(Constant.RESULTDATA, resultData);

        }
        else
        {
        	retMap.put(Constant.RESULT, -400);
        	resultData.put("FailReason", "配置下挂设备消息上报策略失败");
        }
        logger.info("End invoke setLanDeviceOnline:{}", retMap);
        return retMap;
    }
    //将设置上报策略成功的设备，上报到一级平台
    private void reportOnlineToFirst(Boolean isAllconfig, Map<String, Object> lanValueMap,
			List<Map<String, Object>> devices, GatewayInfo gatewayInfo) {
    	List<String> macNameKey = new ArrayList<String>();
		if(isAllconfig){
			for(Map.Entry<String, Object> entry : lanValueMap.entrySet()){
				if(entry.getKey().endsWith("MACAddress")){
					macNameKey.add(entry.getKey().toString());
				}
			}
		}else{
			for(Map<String, Object> device : devices)
            {
                for(Map.Entry<String, Object> entry : lanValueMap.entrySet()) {
                    if(device.get("DeviceMAC").equals(entry.getValue()) && "1".equals(device.get("Enable").toString()))
                    {
                    	macNameKey.add(entry.getKey().toString());
                    }
                }
            }
		}
		for(String mac : macNameKey)
        {
            String preName = mac.substring(0, mac.indexOf("MACAddress"));
            reportOnlineInfo(gatewayInfo,  preName, lanValueMap);
        }
		
	}

	private void reportOnlineInfo(GatewayInfo gatewayInfo, String preName, Map<String, Object> lanValueMap) {
		try {
			Map<String, Object> paraMap = new HashMap<>();
			paraMap.put("RPCMethod", "Report");
			paraMap.put(Constant.ID, (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			paraMap.put(Constant.CMDTYPE, "REPORT_LAN_DEVICE_ONLINE");
			paraMap.put(Constant.SEQUENCEID, InstructionUtil.generate8HexString());


			Map<String, Object> data = new HashMap<>();
			data.put("MAC", gatewayInfo.getGatewayMacaddress());
			paraMap.put(Constant.PARAMETER, data);


			data.put("PowerLevel", "");
			data.put("DevName", getDevName(gatewayInfo.getGatewayMacaddress(), lanValueMap.get(preName + "MACAddress").toString()));
			data.put("Port", "");
			data.put("SSIDIndex","");
			data.put("DevHostname", lanValueMap.get(preName + "HostName"));
			data.put("DeviceMAC", lanValueMap.get(preName + "MACAddress"));
			data.put("OnlineTime", DateTools.format(String.valueOf(System.currentTimeMillis()), DateTools.DATE_FORMAT_YYYYMMDDHHMMSS));
			data.put("IP", lanValueMap.get(preName + "IPAddress"));

			String interfaceType = null != lanValueMap.get(preName + "InterfaceType") ? lanValueMap.get(preName + "InterfaceType").toString():"";
			if(interfaceType.equals("Ethernet"))//有线
			{
			    data.put("ConnectType", "0");
			    data.put("Port", "");
			}
			else if(interfaceType.equals("802.11"))//无线
			{
			    data.put("ConnectType", "1");
			    data.put("SSIDIndex",getWLanSSID(gatewayInfo.getGatewayMacaddress(),
			    		preName.substring(0, preName.indexOf("Hosts")),
			            lanValueMap.get(preName + "MACAddress").toString()));
			}
			else
			{
			    data.put("ConnectType", "Other");
			}

			//调用一级加开平台接口
			logger.info("Start invoke third interface:{}", paraMap);
			syncInfoToFirstLevelPlatformService.report("reportLanDeviceOnline", paraMap);
		} catch (Exception e) {
			logger.info("report exception:{}", e);
		}
		
	}

	/**
     * 网关下挂终端上线消息上报
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> reportLanDeviceOnline(Map<String, Object> parameter) {
        logger.info("Start invoke reportLanDeviceOnline:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        try {
            Inform inform = (Inform)parameter.get("inform");

            List<ParameterValueStruct> structs = inform.getParameterList().getParameterValueStructs();
            String regNumber = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.HostNumberOfEntries";
            String regActive = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.Active";

            ParameterValueStruct numberStruct = null;
            ParameterValueStruct activeStruct = null;
            for(ParameterValueStruct struct : structs)
            {
                Pattern pattern = Pattern.compile(regNumber);
                Matcher matcher = pattern.matcher(struct.getName());
                if (matcher.find()) {
                    numberStruct = struct;
                }

                Pattern patternActive = Pattern.compile(regActive);
                Matcher matcherActive = patternActive.matcher(struct.getName());
                if (matcherActive.find()) {
                    activeStruct = struct;
                }
            }

            if(null == numberStruct && null == activeStruct)
            {
                retMap.put(Constant.CODE, RespCodeEnum.RC_0);
                retMap.put(Constant.MESSAGE, "下挂设备状态没有改变，不需要上报!");
                return retMap;
            }

            GatewayInfo gatewayInfo = new GatewayInfo();
            gatewayInfo.setGatewaySerialnumber(inform.getDeviceId().getSerialNubmer());
            gatewayInfo.setGatewayFactoryCode(inform.getDeviceId().getOui());
            //1 根据 SN 和 OUI查询网关信息
            GatewayInfo retGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
            if(null == retGatewayInfo)
            {
                retMap.put(Constant.CODE, RespCodeEnum.RC_1);
                retMap.put(Constant.MESSAGE, "网关不存在!");
                return retMap;
            }

            String key = retGatewayInfo.getGatewayUuid() + "_" + retGatewayInfo.getGatewaySerialnumber();
            logger.info("reportLanDeviceOnline key:{}", key);

            if(null != numberStruct)
            {
                String preName = numberStruct.getName().substring(0, numberStruct.getName().indexOf("HostNumberOfEntries"));

                //2.获取lan节点名称
                Map<String, Object> nameMap = instructionMethodService.getParameterNames(retGatewayInfo.getGatewayMacaddress(), preName + "Host.", false);

                logger.info("reportLanDeviceOnline getParameterNames nameMap:{}", nameMap);

                List<String> regNameList = new ArrayList<>();
                regNameList.add(preName + "Host.[0-9]+.MACAddress");
                regNameList.add(preName + "Host.[0-9]+.IPAddress");
                regNameList.add(preName + "Host.[0-9]+.HostName");
                regNameList.add(preName + "Host.[0-9]+.InterfaceType");

                List<String> nameList = new ArrayList<>();
                //3.根据正则表达式匹配查找真正的lan节点名称
                for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
                    InstructionUtil.getName(nameList, regNameList, entry.getKey());
                }

                // 4.获取下挂设备值
                Map<String, Object> lanValueMap = instructionMethodService.getParameterValues(retGatewayInfo.getGatewayMacaddress(), nameList);
                logger.info("reportLanDeviceOnline getParameterValues lanValueMap:{}", lanValueMap);

                Map<String, String> saveMap = redisClientTemplate.hgetAll(key);

                //5.判断哪些设备需要上报
                List<String> reportList = getReportList(saveMap, lanValueMap);

                for(String mac : reportList)
                {
                    preName = mac.substring(0, mac.indexOf("MACAddress"));
                    report(numberStruct, retGatewayInfo,  preName, lanValueMap);
                }
            }

            if(null != activeStruct)
            {
                if(!Boolean.valueOf(activeStruct.getValue().toString()))
                {
                    retMap.put(Constant.CODE, RespCodeEnum.RC_0);
                    retMap.put(Constant.MESSAGE, "下挂设备下线，不需要上报!");
                    return retMap;
                }
                String preName = activeStruct.getName().substring(0, activeStruct.getName().indexOf("Active"));
                List<String> nameList = new ArrayList<>();
                nameList.add(preName + "MACAddress");
                nameList.add(preName + "IPAddress");
                nameList.add(preName + "HostName");
                nameList.add(preName + "InterfaceType");

                //获取下挂设备值
                Map<String, Object> lanValueMap = instructionMethodService.getParameterValues(retGatewayInfo.getGatewayMacaddress(), nameList);
                logger.info("reportLanDeviceOnline getParameterValues lanValueMap:{}", lanValueMap);

                report(activeStruct, retGatewayInfo,  preName, lanValueMap);
            }
        }
        catch (Exception e)
        {
            logger.error("reportLanDeviceOnline exception:{}", e);
        }

        retMap.put(Constant.CODE, RespCodeEnum.RC_0);
        return retMap;
    }

    /**
     * 获取网关上接入设备的消息上报策略
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> getLanDeviceOnline(Map<String, Object> parameter) {
        logger.info("Start invoke getLanDeviceOnline:{}", parameter);
        //1.获取网关MAC
        Map<String, Object> parameterMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = null != parameterMap.get(Constant.MAC) ? parameterMap.get(Constant.MAC).toString() : "";
        Map<String, Object> retMap = InstructionUtil.getResultMap(parameter);
        if(StringUtils.isEmpty(gatewayMacAddress))
        {
            retMap.put(Constant.RESULT, -102);
            return retMap;
        }

        GatewayInfo gatewayInfo = gatewayIsExist(gatewayMacAddress);
        if(null == gatewayInfo)
        {
            retMap.put(Constant.RESULT, -201);
            return retMap;
        }

        List<Map<String, Object>> Devices = new ArrayList<>();
        Map<String, Object> device;
        Map<String, String> reportMap = redisClientTemplate.hgetAll(getDeviceKey(gatewayInfo));
        if(null != reportMap && reportMap.size() > 0)
        {
            for(Map.Entry<String, String> entry : reportMap.entrySet())
            {
                device = new HashMap<>();
                device.put("DeviceMAC", entry.getKey());
                device.put("Enable", entry.getValue());
                Devices.add(device);
            }
        }
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("Devices", Devices);
        retMap.put(Constant.RESULTDATA, resultData);

        logger.info("End invoke getLanDeviceOnline:{}", retMap);
        return retMap;
    }


    /**
     * 判断是否对所有下挂设备进行配置通知
     * @param devices
     */
    private Boolean isAllConfig( List<Map<String, Object>> devices)
    {
        if(null == devices || devices.size() < 1)
        {
            return true;
        }
        for(Map<String, Object> device : devices)
        {
            if(null == device.get("DeviceMAC") || StringUtils.isEmpty(device.get("DeviceMAC").toString()))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断是否都是设置为关闭
     * @param devices
     */
    private Boolean isAllDisable( List<Map<String, Object>> devices)
    {
        if(null == devices || devices.size() < 1)
        {
            return false;
        }
        for(Map<String, Object> device : devices)
        {
            if(device.get("Enable").equals("1") ||  device.get("Enable").equals("2") )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * 设置参数属性
     * @param name
     * @param setParamAttrList
     */
    private void setStruct(String name, int notification, List<SetParameterAttributesStruct> setParamAttrList)
    {
        SetParameterAttributesStruct struct = new SetParameterAttributesStruct();
        struct.setName(name);
        struct.setNotification(notification);
        struct.setAccessListChange(true);
        struct.setNotificationChange(true);
        setParamAttrList.add(struct);
    }


    /**
     * 获取下挂设备别名
     * @return
     */
    private String getDevName(String gatewayMacAddress, String deviceMac)
    {
        //1.获取lan节点名称
        Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);

        logger.info("getDevName getParameterNames nameMap:{}", nameMap);

        String regMac =  "InternetGatewayDevice.LANDevice.[0-9]+.X_CMCC_HostCustomise.[0-9]+.MACAddress";
        String regName =  "InternetGatewayDevice.LANDevice.[0-9]+.X_CMCC_HostCustomise.[0-9]+.Name";
        List<String> nameList = new ArrayList<>();

        //2.根据正则表达式匹配查找真正的lan节点名称
        for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
            InstructionUtil.getName(nameList, regMac, entry.getKey());
            InstructionUtil.getName(nameList, regName, entry.getKey());
        }
        logger.info("getDevName nameList:{}", nameList);

        //3.获取值
        Map<String, Object> valueMap = instructionMethodService.getParameterValues(gatewayMacAddress, nameList);

        logger.info("getDevName getParameterValues valueMap:{}", valueMap);
        for(Map.Entry<String, Object> entry : valueMap.entrySet())
        {
            if(entry.getKey().endsWith("MACAddress") && entry.getValue().equals(deviceMac))
            {
                String preMac = entry.getKey().substring(0, entry.getKey().indexOf("MACAddress"));
                return String.valueOf(valueMap.get(preMac + "Name"));
            }
        }
        return "";
    }


    /**
     * 下挂设备通过无线接入，获取连接的SSIDIndex
     * @param gatewayMacAddress
     * @param prekey 格式"InternetGatewayDevice.LANDevice.1"
     * @param deviceMac
     * @return
     */
    private String getWLanSSID(String gatewayMacAddress, String prekey, String deviceMac)
    {
        //1.获取lan节点名称
        Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayMacAddress, prekey + "WLANConfiguration.", false);
        logger.info("getWLanSSID getParameterNames nameMap:{}", nameMap);

        String regSSID = prekey + "WLANConfiguration.[0-9]+.SSID";
        String regMac =  prekey + "WLANConfiguration.[0-9]+.AssociatedDevice.[0-9]+.AssociatedDeviceMACAddress";

        List<String> regNameList = new ArrayList<>();
        regNameList.add(regSSID);
        regNameList.add(regMac);

        List<String> nameList = new ArrayList<>();

        //2.根据正则表达式匹配查找真正的lan节点名称
        for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
            InstructionUtil.getName(nameList, regNameList, entry.getKey());
        }
        logger.info("getWLanSSID nameList:{}", nameList);

        //3.获取值
        Map<String, Object> valueMap = instructionMethodService.getParameterValues(gatewayMacAddress, nameList);

        logger.info("getWLanSSID getParameterValues valueMap:{}", valueMap);
        for(Map.Entry<String, Object> entry : valueMap.entrySet())
        {
            if(entry.getValue().equals(deviceMac))
            {
                String preSSID = entry.getKey().substring(0, entry.getKey().indexOf("AssociatedDevice"));
                return null != valueMap.get(preSSID + "SSID") ? valueMap.get(preSSID + "SSID").toString():"";
            }
        }
        return "";
    }


    /**
     * 判断是否需要上报
     * redis中储存下挂设备的状态为0时，不需要上报，新添加的和已经设置为需要上报的则上报
     * @param saveMap
     * @param lanValueMap
     * @return
     */
    private List<String> getReportList(Map<String, String> saveMap, Map<String, Object> lanValueMap)
    {
        List<String> macList = new ArrayList<>();
        for(Map.Entry<String, Object> entry : lanValueMap.entrySet())
        {
            //已经设置上报
            if(entry.getKey().endsWith("MACAddress")
                    && null != saveMap.get(entry.getValue()))
            {
                String status = saveMap.get(entry.getValue()).toString();
                if(status.equals("1") || status.equals("2"))//上线通知
                {
                    macList.add(entry.getKey());
                }

            }
            //未设置上报，新添加的
            else if(entry.getKey().endsWith("MACAddress") && null == saveMap.get(entry.getValue()))
            {
                macList.add(entry.getKey());
            }
        }

        return macList;
    }

    /**
     *
     * @param gatewayInfo
     * @return
     */
    private String getDeviceKey(GatewayInfo gatewayInfo)
    {
        String key = gatewayInfo.getGatewayUuid() + "_" + gatewayInfo.getGatewaySerialnumber();
        logger.info("setLanDeviceOnline key:{}", key);
        return key;
    }


    /**
     * 保存下挂设备
     * redis中采用hset储存格式，key:网关ID+sn
     *                        field：[{"14144B198734, '1'"}, {"14144B198735, '0'"} ]
     *                        （key：下挂设备mac： value：是否上线通知(0:不通知， 1：通知)
     * @param isAllconfig
     * @param key
     * @param lanValueMap
     * @param devices
     */
    private List<Map<String,Object>> saveHangingDevice(Boolean isAllconfig, String key, Map<String, Object> lanValueMap, List<Map<String, Object>> devices)
    {
    	List<Map<String,Object>> reList = new ArrayList<Map<String,Object>>();
        List<String> macList = new ArrayList<>();
        // 对所有下挂设备进行配置
        if(isAllconfig)
        {
            for(Map.Entry<String, Object> entry : lanValueMap.entrySet())
            {
                if(entry.getKey().endsWith("MACAddress"))
                {
                    redisClientTemplate.hset(key, entry.getValue().toString(), "2");
                    Map<String,Object> deviceMap = new HashMap<String,Object>();
                    deviceMap.put("MAC", entry.getValue().toString());
                    deviceMap.put("Enable", "2");
                    reList.add(deviceMap);
                }
            }
        }
        else//监控指定设备
        {
            //判断需要设置的下挂设备是否存在,不管是否存在都存储
//            Boolean bIsExist = false;
            for(Map<String, Object> device : devices)
            {
//                for(Map.Entry<String, Object> entry : lanValueMap.entrySet()) {
//                    if(device.get("DeviceMAC").equals(entry.getValue()))
//                    {
//                        bIsExist = true;
//                        break;
//                    }
//                }
                Map<String,Object> deviceMap = new HashMap<>();
//                if(bIsExist)
//                {
//                    redisClientTemplate.hset(key, device.get("DeviceMAC").toString(), device.get("Enable").toString());
//                    deviceMap.put("MAC", device.get("DeviceMAC").toString());
//                    deviceMap.put("Enable", device.get("Enable").toString());
//                    
//                }
//                else
//                {
//                    
//                	deviceMap.put("MAC", device.get("DeviceMAC").toString());
//                    deviceMap.put("Enable", "0");
//                }
                redisClientTemplate.hset(key, device.get("DeviceMAC").toString(), device.get("Enable").toString());
                deviceMap.put("MAC", device.get("DeviceMAC").toString());
                deviceMap.put("Enable", device.get("Enable").toString());
                reList.add(deviceMap);
//                bIsExist = false;
            }
        }

        return reList;
    }

    /**
     * 判断网关是否存在
     * @param gatewayMacAddress
     * @return
     */
    private GatewayInfo gatewayIsExist(String gatewayMacAddress)
    {
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayMacaddress(gatewayMacAddress);
        return gatewayInfoService.selectGatewayInfo(searchInfo);
    }

    /**
     * 网关上报
     * @param retGatewayInfo
     * @param preName
     * @param lanValueMap
     */
    private void report(ParameterValueStruct struct, GatewayInfo retGatewayInfo,  String preName, Map<String, Object> lanValueMap)
    {
        try {
            //返回值组装
            Map<String, Object> paraMap = new HashMap<>();
            paraMap.put("RPCMethod", "Report");
            paraMap.put(Constant.ID, (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
            paraMap.put(Constant.CMDTYPE, "REPORT_LAN_DEVICE_ONLINE");
            paraMap.put(Constant.SEQUENCEID, InstructionUtil.generate8HexString());


            Map<String, Object> data = new HashMap<>();
            data.put("MAC", retGatewayInfo.getGatewayMacaddress());
            paraMap.put(Constant.PARAMETER, data);


            data.put("PowerLevel", "");
            data.put("DevName", getDevName(retGatewayInfo.getGatewayMacaddress(), lanValueMap.get(preName + "MACAddress").toString()));
            data.put("Port", "");
            data.put("SSIDIndex","");
            data.put("DevHostname", lanValueMap.get(preName + "HostName"));
            data.put("DeviceMAC", lanValueMap.get(preName + "MACAddress"));
            data.put("OnlineTime", DateTools.format(String.valueOf(System.currentTimeMillis()), DateTools.DATE_FORMAT_YYYYMMDDHHMMSS));
            data.put("IP", lanValueMap.get(preName + "IPAddress"));

            String interfaceType = null != lanValueMap.get(preName + "InterfaceType") ? lanValueMap.get(preName + "InterfaceType").toString():"";
            if(interfaceType.equals("Ethernet"))//有线
            {
                data.put("ConnectType", "0");
                data.put("Port", "");
            }
            else if(interfaceType.equals("802.11"))//无线
            {
                data.put("ConnectType", "1");
                data.put("SSIDIndex",getWLanSSID(retGatewayInfo.getGatewayMacaddress(),
                        struct.getName().substring(0, struct.getName().indexOf("Hosts")),
                        lanValueMap.get(preName + "MACAddress").toString()));
            }
            else
            {
                data.put("ConnectType", "Other");
            }

            //调用一级加开平台接口
            logger.info("Start invoke third interface:{}", paraMap);
            syncInfoToFirstLevelPlatformService.report("reportLanDeviceOnline", paraMap);
        }
        catch (Exception e)
        {
            logger.info("report exception:{}", e);
        }
    }

}
