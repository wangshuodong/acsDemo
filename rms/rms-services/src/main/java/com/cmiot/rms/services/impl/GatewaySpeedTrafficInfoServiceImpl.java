package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.acs.model.struct.ParameterValueStructStr;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.utils.IpV4Util;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.GatewaySpeedTrafficInfoService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.InstructionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网速和流量统计
 * Created by panmingguo on 2016/5/6.
 */
@Service
public class GatewaySpeedTrafficInfoServiceImpl implements GatewaySpeedTrafficInfoService {

    private static Logger logger = LoggerFactory.getLogger(GatewaySpeedTrafficInfoServiceImpl.class);

    @Autowired
    InstructionMethodService instructionMethodService;

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;

    @Autowired
    private RedisClientTemplate redisClientTemplate;

    @Autowired
    private GatewayInfoService gatewayInfoService;
    /**
     * 获取网关WAN侧和LAN侧流量统计
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> getHgPortsTrafficStatus(Map<String, Object> parameter) {
        logger.info("Start invoke getHgPortsTrafficStatus:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        //1.获取网关MAC
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = null != macMap.get(Constant.MAC) ? macMap.get(Constant.MAC).toString() : "";
        if(StringUtils.isEmpty(gatewayMacAddress)){
        	retMap.put(Constant.RESULT, -102);
            retMap.put(Constant.RESULTDATA, new HashMap<>().put(Constant.FAILREASON, "网关MAC为空"));
            return retMap;
        }
        if(null == gatewayIsExist(gatewayMacAddress)){
        	retMap.put(Constant.RESULT, -201);
            retMap.put(Constant.RESULTDATA, new HashMap<>().put(Constant.FAILREASON, "网关MAC不存在"));
            return retMap;
        }
        //2.获取wan节点名称
        Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.WANDevice.", false);

        logger.info("getHgPortsTrafficStatus getParameterNames nameMap:{}", nameMap);

        //WAN 上行数据
        //Gpon上行
        String gponSent = "InternetGatewayDevice.WANDevice.[0-9]+.X_CMCC_GponInterfaceConfig.Stats.BytesSent";
        String gponReceived = "InternetGatewayDevice.WANDevice.[0-9]+.X_CMCC_GponInterfaceConfig.Stats.BytesReceived";

        //epon上行
        String eponSent = "InternetGatewayDevice.WANDevice.[0-9]+.X_CMCC_EPONInterfaceConfig.Stats.BytesSent";
        String eponReceived = "InternetGatewayDevice.WANDevice.[0-9]+.X_CMCC_EPONInterfaceConfig.Stats.BytesReceived";

        //lan上行
        String lanSent = "InternetGatewayDevice.WANDevice.[0-9]+.WANEthernetInterfaceConfig.Stats.BytesSent";
        String lanReceived = "InternetGatewayDevice.WANDevice.[0-9]+.WANEthernetInterfaceConfig.Stats.BytesReceived";

        List<String> regWanNameList = new ArrayList<>();
        regWanNameList.add(gponSent);
        regWanNameList.add(gponReceived);
        regWanNameList.add(eponSent);
        regWanNameList.add(eponReceived);
        regWanNameList.add(lanSent);
        regWanNameList.add(lanReceived);

        List<String> wanNameList = new ArrayList<>();
        //3.根据正则表达式匹配查找真正的wan节点名称
        for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
            InstructionUtil.getName(wanNameList, regWanNameList, entry.getKey());
        }
        logger.info("getHgPortsTrafficStatus wanNameList:{}", wanNameList);

        Map<String, Object> resultData = new HashMap<>();
        buildHgPortsTrafficStatusData(resultData);
        //4.根据具体的节点获取相应的value
        Map<String, Object> wanValueMap = instructionMethodService.getParameterValues(gatewayMacAddress, wanNameList);
        logger.info("getHgPortsTrafficStatus getParameterValues wanValueMap:{}", wanValueMap);

        //5.将wan数据封装到返回值中
        for (Map.Entry<String, Object> entry : wanValueMap.entrySet()) {
            if(entry.getKey().endsWith("BytesSent"))
            {
                resultData.put("WANTX", String.valueOf(Integer.valueOf(entry.getValue().toString()) / 1024));
            }
            else if(entry.getKey().endsWith("BytesReceived"))
            {
                resultData.put("WANRX", String.valueOf(Integer.valueOf(entry.getValue().toString()) / 1024));
            }
        }

        //6.获取lan节点名称
        Map<String, Object> nameLanMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);
        logger.info("getHgPortsTrafficStatus getParameterNames nameLanMap:{}", nameLanMap);
        List<String> lanNameList = new ArrayList<>();

        //LAN 发送数据
        String regLanSent = "InternetGatewayDevice.LANDevice.[0-9]+.LANEthernetInterfaceConfig.[0-9]+.Stats.BytesSent";
        //LAN 接收数据
        String regLanReceived = "InternetGatewayDevice.LANDevice.[0-9]+.LANEthernetInterfaceConfig.[0-9]+.Stats.BytesReceived";
        //无线接口频段: 0：表示2.4GHz; 1：表示5.8GHz
        String regWlanRFBand = "InternetGatewayDevice.LANDevice.[0-9]+.WLANConfiguration.[0-9]+.X_CMCC_RFBand";
        //WLAN 发送数据
        String regWlanSent = "InternetGatewayDevice.LANDevice.[0-9]+.WLANConfiguration.[0-9]+.TotalBytesSent";
        //WLAN 接收数据
        String regWlanReceived = "InternetGatewayDevice.LANDevice.[0-9]+.WLANConfiguration.[0-9]+.TotalBytesReceived";

        List<String> regLanNameList = new ArrayList<>();
        regLanNameList.add(regLanSent);
        regLanNameList.add(regLanReceived);
        regLanNameList.add(regWlanRFBand);
        regLanNameList.add(regWlanSent);
        regLanNameList.add(regWlanReceived);

        //7.根据正则表达式匹配查找真正的lan节点名称
        for(Map.Entry<String, Object> entry : nameLanMap.entrySet())
        {
            InstructionUtil.getName(lanNameList, regLanNameList, entry.getKey());
        }

        logger.info("getHgPortsTrafficStatus lanNameList:{}", lanNameList);
        //8.根据具体的节点获取相应的value
        Map<String, Object> lanValueMap = instructionMethodService.getParameterValues(gatewayMacAddress, lanNameList);
        logger.info("getHgPortsTrafficStatus getParameterValues lanValueMap:{}", lanValueMap);

        List<String> lanSentList = new ArrayList<>();
        List<String> lanReceivedList = new ArrayList<>();
        //9.将wlan数据封装到返回值中
        long wlan1Sent = 0;
        long wlan1Received = 0;
        long wlan2Sent = 0;
        long wlan2Received = 0;
        for (Map.Entry<String, Object> entry : lanValueMap.entrySet()) {
            if(entry.getKey().toString().endsWith("X_CMCC_RFBand"))
            {
                
                String preWlan = entry.getKey().substring(0, entry.getKey().indexOf("X_CMCC_RFBand"));
                if(entry.getValue().toString().equals("0"))
                {
                    wlan1Sent =  wlan1Sent + (Integer.valueOf(lanValueMap.get(preWlan + "TotalBytesSent").toString()) / 1024);
                    wlan1Received =  wlan1Received + (Integer.valueOf(lanValueMap.get(preWlan + "TotalBytesReceived").toString()) / 1024);
                }
                else if(entry.getValue().toString().equals("1"))
                {
                    wlan2Sent =  wlan2Sent + (Integer.valueOf(lanValueMap.get(preWlan + "TotalBytesSent").toString()) / 1024);
                    wlan2Received =  wlan2Received + (Integer.valueOf(lanValueMap.get(preWlan + "TotalBytesReceived").toString()) / 1024);
                }

            }
            else if(entry.getKey().endsWith("BytesSent"))
            {
                lanSentList.add(entry.getKey());
            }
            else if(entry.getKey().endsWith("BytesReceived"))
            {
                lanReceivedList.add(entry.getKey());
            }

        }
        resultData.put("WLAN1RX", String.valueOf(wlan1Received));
        resultData.put("WLAN1TX", String.valueOf(wlan1Sent));
        resultData.put("WLAN2RX", String.valueOf(wlan2Received));
        resultData.put("WLAN2TX", String.valueOf(wlan2Sent));
        //10.根据节点中的{i}排序
        Collections.sort(lanSentList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        Collections.sort(lanReceivedList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        //11.将lan数据封装到返回值中
        int i = 1;//根据接口规范是返回port1-port4的数据
        for(String name :lanSentList)
        {
        	if(i < 5){
        		resultData.put("Port" + i + "TX", String.valueOf(Integer.valueOf(lanValueMap.get(name).toString()) / 1024));
        	}
            i++;
        }
        i = 1;
        for(String name :lanReceivedList)
        {	
        	if(i < 5){
        		resultData.put("Port" + i + "RX", String.valueOf(Integer.valueOf(lanValueMap.get(name).toString()) / 1024));
        	}
            i++;
        }

        // 3.拼装返回结果
        
        retMap.put(Constant.ID, parameter.get(Constant.ID));
        retMap.put(Constant.CMDTYPE, parameter.get(Constant.CMDTYPE));
        retMap.put(Constant.SEQUENCEID, parameter.get(Constant.SEQUENCEID));
        retMap.put(Constant.RESULTDATA, resultData);
        retMap.put(Constant.RESULT, 0);
        resultData.put("Timestamp", String.valueOf(System.currentTimeMillis()));

        logger.info("End invoke getHgPortsTrafficStatus:{}", retMap);

        return retMap;
    }

    /**
     * 设置下挂设备的上下行最大带宽限制
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> setLanDeviceBandwidth(Map<String, Object> parameter) {
        logger.info("Start invoke setLanDeviceBandwidth:{}", parameter);
        //1.获取网关MAC
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = null != macMap.get(Constant.MAC) ? macMap.get(Constant.MAC).toString() : "";
        String deviceMacAddress = null != macMap.get(Constant.DEVICEMAC) ? macMap.get(Constant.DEVICEMAC).toString() : "";
        Double usBandwidth = null != macMap.get("UsBandwidth") ? Double.valueOf(macMap.get("UsBandwidth").toString()) : 0d;

        Map<String, Object> retMap = InstructionUtil.getResultMap(parameter);
        if(StringUtils.isEmpty(gatewayMacAddress))
        {
            retMap.put(Constant.RESULT, -102);
            retMap.put(Constant.RESULTDATA, new HashMap<>().put(Constant.FAILREASON, "网关MAC为空"));
            return retMap;
        }
        if(null == gatewayIsExist(gatewayMacAddress)){
        	retMap.put(Constant.RESULT, -201);
            retMap.put(Constant.RESULTDATA, new HashMap<>().put(Constant.FAILREASON, "网关MAC不存在"));
            return retMap;
        }
        if(StringUtils.isEmpty(deviceMacAddress)){
        	retMap.put(Constant.RESULT, -102);
            retMap.put(Constant.RESULTDATA, new HashMap<>().put(Constant.FAILREASON, "网关下挂设备DeviceMAC为空"));
            return retMap;
        }
        
        //2.获取lan节点名称
        Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);
        logger.info("setLanDeviceBandwidth getParameterNames nameMap:{}", nameMap);

        //下挂设备的Ip和MAC(下挂设备限速是根据IP来做限速,接口传入的是设备的MAC，需要通过MAC先找到IP，然后通过IP来获取限速)
        String ipAddress = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.IPAddress";
        String macAddress = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.MACAddress";

        //上行数据限速模式
        String speedModeUp = "InternetGatewayDevice.LANDevice.[0-9]+.X_CMCC_DataSpeedLimit.SpeedLimitMode_UP";
        String ipLimitUp = "InternetGatewayDevice.LANDevice.[0-9]+.X_CMCC_DataSpeedLimit.IPLimit_UP";

        List<String> regLanNameList = new ArrayList<>();
        regLanNameList.add(ipAddress);
        regLanNameList.add(macAddress);

        regLanNameList.add(speedModeUp);
        regLanNameList.add(ipLimitUp);

        List<String> nameList = new ArrayList<>();
        //3.根据正则表达式匹配查找真正的wan节点名称
        for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
            InstructionUtil.getName(nameList, regLanNameList, entry.getKey());
        }

        logger.info("setLanDeviceBandwidth nameList:{}", nameList);

        //4.根据具体的节点获取相应的value
        Map<String, Object> valueMap = instructionMethodService.getParameterValues(gatewayMacAddress, nameList);

        logger.info("setLanDeviceBandwidth valueMap:{}", valueMap);

        //5.通过MAC获取Ip
        String deviceIp = null;
        String macAddressName = null;
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            if(entry.getKey().endsWith("MACAddress") && entry.getValue().toString().equals(deviceMacAddress))
            {
                String preMac = entry.getKey().substring(0, entry.getKey().indexOf("MACAddress"));
                deviceIp = valueMap.get(preMac + "IPAddress").toString();
                macAddressName = entry.getKey();
                break;
            }
        }

        logger.info("setLanDeviceBandwidth deviceIp:{}", deviceIp);

        if(StringUtils.isEmpty(deviceIp))
        {
            retMap.put(Constant.RESULT, "-202");
            retMap.put(Constant.RESULTDATA, new HashMap<>());
            return retMap;
        }

        String preMac = macAddressName.substring(0, macAddressName.indexOf("Hosts"));

        List<ParameterValueStruct> list = new ArrayList<>();

        
        //查询是否已经设置过值， 防止被覆盖
        Object oldIPLimitUp = valueMap.get(preMac + "X_CMCC_DataSpeedLimit.IPLimit_UP");

        ParameterValueStruct structUpMode = new ParameterValueStruct();
        structUpMode.setName(preMac + "X_CMCC_DataSpeedLimit.SpeedLimitMode_UP");
        structUpMode.setValueType("unsignedInt");
        structUpMode.setReadWrite(true);
        list.add(structUpMode);

        //为0，不限速；不为0，使用Ip限速模式
        if(usBandwidth == 0)
        {
            structUpMode.setValue(0);
        }
        else
        {
            structUpMode.setValue(3);

            ParameterValueStruct structUpIp = new ParameterValueStruct();
            structUpIp.setName(preMac + "X_CMCC_DataSpeedLimit.IPLimit_UP");
            structUpIp.setValueType("string");
            structUpIp.setReadWrite(true);
            //基于IP地址段限速（上行），以”m1/n1,m2/n2”方式设置。
            // 其中：”m”为IP地址段，格式要求：IPv4为“x1.x2.x3.x4-y1.y2.y3.y4”，
            // IPv6为“x1:x2:x3::x4-y1:y2:y3::y4”；
            // “n”为限速值，单位为512Kbps
            String v = "";
            String temp = "";
            if(oldIPLimitUp != null && !"".equals(oldIPLimitUp.toString())){
            	v = oldIPLimitUp.toString();
            	if(v.contains(deviceIp +"-"+ deviceIp)){
            		//已经包含了，则把旧的去掉
            		String[] vs = v.split(",");
            		for(String child : vs){
            			if(!child.contains(deviceIp +"-"+ deviceIp)){
            				temp += child+",";
            			}
            		}
            	}
            	if(temp.length() >0 ){
            		temp = temp.substring(0, temp.length() -1);
            	}
            		v = temp +","+deviceIp +"-"+ deviceIp + "/" + ((long)Math.ceil(usBandwidth / 512));
            	
            }else{
            	v += deviceIp +"-"+ deviceIp + "/" + ((long)Math.ceil(usBandwidth / 512));
            }
            
            structUpIp.setValue(v);
            list.add(structUpIp);
        }
       
        logger.info("setLanDeviceBandwidth setParameterValue list:{}", list);

        //6.设值
        Boolean bRet = instructionMethodService.setParameterValue(gatewayMacAddress, list);

        if(!bRet)
        {
            retMap.put(Constant.RESULT, Constant.FAILEDCODE);
        }
        retMap.put(Constant.RESULTDATA, new HashMap<>());
        logger.info("End invoke setLanDeviceBandwidth:{}", retMap);
        return retMap;
    }

    /**
     * 获取下挂的设备上下行最大带宽限制
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> getLanDeviceBandth(Map<String, Object> parameter) {
        logger.info("Start invoke getLanDeviceBandth:{}", parameter);

        //1.获取网关MAC
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = null != macMap.get(Constant.MAC) ? macMap.get(Constant.MAC).toString() : "";

        Map<String, Object> retMap = InstructionUtil.getResultMap(parameter);
        String deviceMacAddress = null != macMap.get(Constant.DEVICEMAC) ? macMap.get(Constant.DEVICEMAC).toString() : "";
        if(StringUtils.isEmpty(gatewayMacAddress))
        {
            retMap.put(Constant.RESULT, -102);
            retMap.put(Constant.RESULTDATA, new HashMap<>().put(Constant.FAILREASON, "网关MAC为空"));
            return retMap;
        }
        if(null == gatewayIsExist(gatewayMacAddress)){
        	retMap.put(Constant.RESULT, -201);
            retMap.put(Constant.RESULTDATA, new HashMap<>().put(Constant.FAILREASON, "网关MAC不存在"));
            return retMap;
        }
        if(StringUtils.isEmpty(deviceMacAddress))
        {
            retMap.put(Constant.RESULT, -102);
            retMap.put(Constant.RESULTDATA, new HashMap<>().put(Constant.FAILREASON, "网关下挂设备DeviceMAC为空"));
            return retMap;
        }
        //2.获取lan节点名称
        Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);
        logger.info("getLanDeviceBandth getParameterNames nameMap:{}", nameMap);

        //下挂设备的Ip和MAC(下挂设备限速是根据IP来做限速,接口传入的是设备的MAC，需要通过MAC先找到IP，然后通过IP来获取限速)
        String ipAddress = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.IPAddress";
        String macAddress = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.MACAddress";

        //上行数据限速模式
        String speedModeUp = "InternetGatewayDevice.LANDevice.[0-9]+.X_CMCC_DataSpeedLimit.SpeedLimitMode_UP";
        String ipLimitUp = "InternetGatewayDevice.LANDevice.[0-9]+.X_CMCC_DataSpeedLimit.IPLimit_UP";

        List<String> regLanNameList = new ArrayList<>();
        regLanNameList.add(ipAddress);
        regLanNameList.add(macAddress);

        regLanNameList.add(speedModeUp);
        regLanNameList.add(ipLimitUp);

        List<String> nameList = new ArrayList<>();
        //3.根据正则表达式匹配查找真正的wan节点名称
        for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
            InstructionUtil.getName(nameList, regLanNameList, entry.getKey());
        }

        logger.info("getLanDeviceBandth nameList:{}", nameList);

        //4.根据具体的节点获取相应的value
        Map<String, Object> valueMap = instructionMethodService.getParameterValues(gatewayMacAddress, nameList);

        logger.info("getLanDeviceBandth valueMap:{}", valueMap);

        String deviceIp = "";
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            if(entry.getKey().endsWith("MACAddress") && entry.getValue().toString().equals(deviceMacAddress))
            {
                String preMac = entry.getKey().substring(0, entry.getKey().indexOf("MACAddress"));
                deviceIp = valueMap.get(preMac + "IPAddress").toString();
                break;
            }
        }

        if(StringUtils.isEmpty(deviceIp))
        {
            retMap.put(Constant.RESULT, 0);
            retMap.put(Constant.RESULTDATA, new HashMap<>());//无结果，返回空,result为0
            return retMap;
        }
        logger.info("getLanDeviceBandth deviceIp:{}", deviceIp);

        Map<String, Object> resultData = new HashMap<>();
        resultData.put(Constant.DEVICEMAC, deviceMacAddress);
        resultData.put("UsBandwidth", "");
        resultData.put("DsBandwidth", "");

        //5.上下行数据处理
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            if(entry.getKey().endsWith("SpeedLimitMode_UP"))
            {
                if(null != entry.getValue() && (3 == Integer.valueOf(entry.getValue().toString())))
                {
                    String preUP = entry.getKey().substring(0, entry.getKey().indexOf("SpeedLimitMode_UP"));
                    String iplimitUp = valueMap.get(preUP + "IPLimit_UP").toString();
                    String ups[] = iplimitUp.split(",");
                    for(String up: ups)
                    {
                        String ipLimit[] = up.split("/");
                        String ips[] = ipLimit[0].split("-");
                        if(IpV4Util.checkIpV4(ips[0], ips[1], deviceIp))
                        {
                            resultData.put("UsBandwidth", String.valueOf(Long.valueOf(ipLimit[1]) * 512));
                            break;
                        }

                    }
                }
            }
        }
        retMap.put(Constant.RESULTDATA, resultData);
        logger.info("End invoke getLanDeviceBandth:{}", retMap);
        return retMap;
    }

    @Override
    public Map<String, Object> setLanDeviceSpeedTest(Map<String, Object> parameter) {
        //测试周期采样监控返回的结果
        logger.info("Start invoke setLanDeviceSpeedTest:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();

        //1.获取网关MAC
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = null != macMap.get(Constant.MAC) ? macMap.get(Constant.MAC).toString() : "";
        String Enable = null ==  macMap.get("Enable")?"":(String) macMap.get("Enable");
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("Enable", macMap.get("Enable"));
        retMap.put(Constant.ID, parameter.get(Constant.ID));
        retMap.put(Constant.CMDTYPE, parameter.get(Constant.CMDTYPE));
        retMap.put(Constant.SEQUENCEID, parameter.get(Constant.SEQUENCEID));
        retMap.put(Constant.RESULTDATA, resultData);
        retMap.put(Constant.RESULT, 0);
        if(org.apache.commons.lang.StringUtils.isEmpty(gatewayMacAddress)){
            retMap.put(Constant.RESULT,-102);
            logger.info("MAC为空");
            return retMap;
        }
        if(org.apache.commons.lang.StringUtils.isEmpty(Enable)){
            retMap.put(Constant.RESULT,-102);
            logger.info("Enable为空");
            return retMap;
        }
        GatewayInfo gateQuery = new GatewayInfo();
        gateQuery.setGatewayMacaddress(gatewayMacAddress);
        List<GatewayInfo> list = gatewayInfoMapper.queryList(gateQuery);

        if(list == null || list.size() <=0){
            logger.info("MAC为:" + gatewayMacAddress + "的网关不存在");
            retMap.put(Constant.RESULT, -201);
            return retMap;
        }
        //经过黄川和耿亮沟通，他们确定“网关不支持流量开关，如需要支持该功能可在平台处实现，但不对网关做实际操作。”
        //2016.7.26 改为该接口控制速率上报
        String reportSpeedTestKey = gatewayMacAddress + "reportSpeedTestKey";
        if("1".equals(Enable)) {//启用
            redisClientTemplate.set(reportSpeedTestKey,"true");
            logger.debug("invoke setLanDeviceSpeedTest set reportSpeedTestKey:{}","true");
        }else if("0".equals(Enable)){//不启用
            redisClientTemplate.set(reportSpeedTestKey,"false");
            logger.debug("invoke setLanDeviceSpeedTest set reportSpeedTestKey:{}","false");
        }
        logger.info("End invoke setLanDeviceSpeedTest:{}", retMap);
        return retMap;
    }

    @Override
    public Map<String, Object> getLanDeviceTrafficStatus(Map<String, Object> parameter) {
        logger.info("Start invoke getLanDeviceTrafficStatus:{}", parameter);

        //1.获取网关MAC
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = null != macMap.get(Constant.MAC) ? macMap.get(Constant.MAC).toString() : "";
        //获取下挂设备MAC
        String subDeviceMac = null != macMap.get(Constant.DEVICEMAC) ? macMap.get(Constant.DEVICEMAC).toString() : "";

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("UsStats", null);
        resultData.put("DsStats", null);
        resultData.put("DeviceMAC", subDeviceMac);
        resultData.put("Timestamp", System.currentTimeMillis() + "");
        Map<String, Object> retMap = new HashMap<>();
        retMap.put(Constant.ID, parameter.get(Constant.ID));
        retMap.put(Constant.CMDTYPE, parameter.get(Constant.CMDTYPE));
        retMap.put(Constant.SEQUENCEID, parameter.get(Constant.SEQUENCEID));
        retMap.put(Constant.RESULTDATA, resultData);
        retMap.put(Constant.RESULT, 0);
        if(org.apache.commons.lang.StringUtils.isEmpty(gatewayMacAddress)){
            retMap.put(Constant.RESULT,-102);
            logger.info("MAC为空");
            return retMap;
        }
        GatewayInfo gateQuery = new GatewayInfo();
        gateQuery.setGatewayMacaddress(gatewayMacAddress);
        List<GatewayInfo> list = gatewayInfoMapper.queryList(gateQuery);

        if(list == null || list.size() <=0){
            logger.info("MAC为:" + gatewayMacAddress + "的网关不存在");
            retMap.put(Constant.RESULT, -201);
            return retMap;
        }


        //2.获取wan节点名称
        Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);
        logger.info("getLanDeviceTrafficStatus getParameterNames nameMap:{}", nameMap);

        //下挂设备MAC地址
        String subMacAddress = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.MACAddress";
        //下挂主机发送的总字节数（上行）
        String BytesSend = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.X_CMCC_Stats.BytesSen[d|t]";
        //下挂主机接收的总字节数（下行）
        String BytesReceived = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.X_CMCC_Stats.BytesReceived";

        List<String> regWanNameList = new ArrayList<String>();
        regWanNameList.add(subMacAddress);
        regWanNameList.add(BytesSend);
        regWanNameList.add(BytesReceived);

        List<String> wanNameList = new ArrayList<>();
        //3.根据正则表达式匹配查找所有下挂终端MAC节点名称
        for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
            InstructionUtil.getName(wanNameList, regWanNameList, entry.getKey());
        }


        //4.根据具体的节点获取相应的value
        Map<String, Object> wanValueMap = instructionMethodService.getParameterValues(gatewayMacAddress, wanNameList);
        logger.info("getLanDeviceTrafficStatus getParameterValues wanValueMap:{}", wanValueMap);

        //5.将上下行流量数据封装到返回值中
        for (Map.Entry<String, Object> entry : wanValueMap.entrySet()) {

            if(entry.getValue().toString().equals(subDeviceMac))
            {
                //匹配的MAC地址节点全路径
                String subMacFullName = entry.getKey();
                //匹配的上行流量
                int UsStats = 0;
                if (wanValueMap.containsKey(subMacFullName.substring(0,subMacFullName.indexOf("MACAddress")) + "X_CMCC_Stats.BytesSend")) {
                	UsStats = (int) wanValueMap.get(subMacFullName.substring(0,subMacFullName.indexOf("MACAddress")) + "X_CMCC_Stats.BytesSend");
				}else if (wanValueMap.containsKey(subMacFullName.substring(0,subMacFullName.indexOf("MACAddress")) + "X_CMCC_Stats.BytesSent")) {
					UsStats = (int) wanValueMap.get(subMacFullName.substring(0,subMacFullName.indexOf("MACAddress")) + "X_CMCC_Stats.BytesSent");
				}

                //匹配的下行流量
                int DsStats = (int) wanValueMap.get(subMacFullName.substring(0,subMacFullName.indexOf("MACAddress")) + "X_CMCC_Stats.BytesReceived");

                if(!StringUtils.isEmpty(UsStats)){
                    resultData.put("UsStats", UsStats/1024 + "");
                }
                if(!StringUtils.isEmpty(DsStats)){
                    resultData.put("DsStats", DsStats/1024 + "");
                }
            }

        }
        //封装返回消息
        resultData.put("DeviceMAC", subDeviceMac);
        resultData.put("Timestamp", System.currentTimeMillis() + "");
        logger.info("End invoke getLanDeviceTrafficStatus:{}", retMap);
        return retMap;
    }

    /**
     * 配置策略，就是配置周期采样监控
     * 由于周期采样监控单位为分钟，策略周期为秒，故策略周期不足一分钟时，本接口自动处理为一分钟
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> setLanSpeedReportPolicy(Map<String, Object> parameter) {
        //测试周期采样监控返回的结果
        logger.info("Start invoke getLanDeviceTrafficStatus:{}", parameter);

        //1.获取网关MAC
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);

        // 拼装返回消息
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("Enable", macMap.get("Enable"));
        Map<String, Object> retMap = new HashMap<>();
        retMap.put(Constant.ID, parameter.get(Constant.ID));
        retMap.put(Constant.CMDTYPE, parameter.get(Constant.CMDTYPE));
        retMap.put(Constant.SEQUENCEID, parameter.get(Constant.SEQUENCEID));
        retMap.put(Constant.RESULTDATA, resultData);

       String gatewayMacAddress = null != macMap.get(Constant.MAC) ? macMap.get(Constant.MAC).toString() : "";
        //获取下挂设备MAC
        List<String> subDeviceMacList = (List)macMap.get(Constant.DEVICEMACS);
        //获取开关
        String Enable = (String) macMap.get("Enable");
        //获取周期
        String Times = (String) macMap.get("Time");
        if(Integer.valueOf(Times) < 60)
        {
            Times = "60";
        }
        String Time = String.valueOf(getMyInt(Integer.valueOf(Times), 60));

        if(org.apache.commons.lang.StringUtils.isEmpty(gatewayMacAddress)){
            retMap.put(Constant.RESULT,-102);
            logger.info("MAC为空");
            return retMap;
        }
        GatewayInfo gateQuery = new GatewayInfo();
        gateQuery.setGatewayMacaddress(gatewayMacAddress);
        List<GatewayInfo> list = gatewayInfoMapper.queryList(gateQuery);

        if(list == null || list.size() <=0){
            logger.info("MAC为:" + gatewayMacAddress + "的网关不存在");
            retMap.put(Constant.RESULT, -201);
            return retMap;
        }
        //2.获取下挂设备所有节点名称
        Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);
        logger.info("setLanSpeedReportPolicy getParameterNames nameMap:{}", nameMap);

        //获取周期采样所有节点名称
        Map<String, Object> monitorNameMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor.MonitorConfig.", false);
        logger.info("setLanSpeedReportPolicy getParameterNames monitorNameMap:{}", monitorNameMap);

        //下挂设备MAC地址
        String subMacAddress = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.MACAddress";
        //周期采样监控开关
        String samplingEnable = "InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor.Enable";
        //需要监控的关键参数
        String needMonitorParams = "InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor.MonitorConfig.[0-9]+.ParaList";
        ///需要监控的关键参数的采样周期
        String needMonitorParamsTime = "InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor.MonitorConfig.[0-9]+.TimeList";

        List<String> subMacNameList = new ArrayList<>();
        subMacNameList.add(subMacAddress);
        subMacNameList.add(needMonitorParams);
        subMacNameList.add(needMonitorParamsTime);

        List<String> subMacMatchNameList = new ArrayList<>();
        subMacMatchNameList.add(samplingEnable);
        //3.根据正则表达式匹配查找所有下挂终端MAC节点名称
        for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
            InstructionUtil.getName(subMacMatchNameList, subMacNameList, entry.getKey());
        }
        for (Map.Entry<String, Object> entry : monitorNameMap.entrySet()) {
            InstructionUtil.getName(subMacMatchNameList, subMacNameList, entry.getKey());
        }
        //4.根据具体的节点获取相应的value
        Map<String, Object> subMacValueMap = instructionMethodService.getParameterValues(gatewayMacAddress, subMacMatchNameList);
        logger.info("setLanSpeedReportPolicy getParameterValues subMacValueMap:{}", subMacValueMap);
        //需要配置的参数节点全路径
        List<String> paramFullNameList = new ArrayList<>();

        for (Map.Entry<String, Object> entry : subMacValueMap.entrySet()) {

            for(int i = 0 ;i < subDeviceMacList.size();i++){
                if(entry.getValue().toString().equals(subDeviceMacList.get(i)))
                {
                    //匹配的MAC地址节点全路径
                    String subMacFullName = entry.getKey();
                    //匹配的上行流量
                    String sendName = "";
                    if (nameMap.containsKey(subMacFullName.substring(0,subMacFullName.indexOf("MACAddress")) + "X_CMCC_Stats.BytesSend")) {
                    	sendName = subMacFullName.substring(0,subMacFullName.indexOf("MACAddress")) + "X_CMCC_Stats.BytesSend";
					}else if (nameMap.containsKey(subMacFullName.substring(0,subMacFullName.indexOf("MACAddress")) + "X_CMCC_Stats.BytesSent")) {
						sendName =subMacFullName.substring(0,subMacFullName.indexOf("MACAddress")) + "X_CMCC_Stats.BytesSent";
					}
                    String reciveName = subMacFullName.substring(0,subMacFullName.indexOf("MACAddress")) + "X_CMCC_Stats.BytesReceived";
                    //把同一组上下行的设置在一起,逗号分割
                    paramFullNameList.add(sendName+","+reciveName);
                }
            }
        }
        //获取周期采样监控开关
        Boolean samplingEnableValue = (Boolean) subMacValueMap.get(samplingEnable);
        if(samplingEnableValue == null){
            logger.info("获取周期采样监控开关失败");
            retMap.put(Constant.RESULT, -400);
            return retMap;
        }
        //要设置的参数集合
        List<ParameterValueStruct> setParamList = new ArrayList<>();
        //正则匹配
        Pattern pattern = Pattern.compile(needMonitorParams);
//        Matcher matcher = pattern.matcher(test);
        if("0".equals(Enable)){
            //如果是要关闭策略,则找出相应的参数并去掉
            for (Map.Entry<String, Object> entry : subMacValueMap.entrySet()) {
                Matcher matcher = pattern.matcher(entry.getKey());
                if(matcher.matches()){
                    String paraListStr = (String) entry.getValue();
                    for(int j = 0;j<paramFullNameList.size();j++){
                        if(paraListStr.equals(paramFullNameList.get(j))){
                            //当在ParaList中找到要关闭的参数时，就把ParaList和TimeList中相应的参数去掉
                        	instructionMethodService.DeleteObject(gatewayMacAddress,entry.getKey().substring(0,entry.getKey().indexOf("ParaList")), System.currentTimeMillis() + "");
                        }
                    }
                }
            }
        }else if("1".equals(Enable)){
            //如果是要开启策略
            //首先要判断周期上报是否开启，如果是关闭的，则需要开启
            if(!samplingEnableValue){
                setParamList.add(new ParameterValueStruct(samplingEnable,true,"boolean"));
            }
            //然后判断已有的节点是否有该参数，如果没有就新增对象，由于ParaList长度限制为256，故以3个参数为一个ParaList
            List<String> notExistParamList = new ArrayList<>();

            for(int i = 0;i<paramFullNameList.size();i++){
                //遍历所有参数，查询是否存在，不存在就要设置
                boolean isHave = false;//是否存在，默认不存在
                for (Map.Entry<String, Object> entry : subMacValueMap.entrySet()) {
                    Matcher matcher = pattern.matcher(entry.getKey());
                    if(matcher.matches()){
                        String paraListStr = (String) entry.getValue();
                      //  List<String> paraList = Arrays.asList(paraListStr.split(","));
                       // for( int j = 0;j<paraList.size();j++) {
                            if (paraListStr.equals(paramFullNameList.get(i))) {
                            	setParamList.add(new ParameterValueStructStr(entry.getKey().substring(0, entry.getKey().indexOf("ParaList")) + "TimeList", Time));
                                isHave = true;
                            }
                      //  }
                    }
                }
                if(isHave){
                    //如果存在，则重置isHave,继续遍历
                    isHave = false;
                }else{
                    notExistParamList.add(paramFullNameList.get(i));
                }
            }
            //组装要设置的参数
            if(notExistParamList.size()>0) {
                for(int k = 0;k<notExistParamList.size();k++){
                    //添加实例,添加实例网关会返回添加成功的i值，并且会生成i节点下的所有子节点
                    int addNum;
                    addNum = instructionMethodService.AddObject(gatewayMacAddress,"InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor.MonitorConfig.",System.currentTimeMillis()+"");
                    logger.info("setLanSpeedReportPolicy AddObject addNum:{}", addNum);
                    if(addNum>=1) {
                        //添加参数
                        List<String> paraValueList = new ArrayList<>();
                        List<String> timeValueList = new ArrayList<>();
                        paraValueList.add(notExistParamList.get(k));
                        timeValueList.add(Time);
                        setParamList.add(new ParameterValueStructStr("InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor.MonitorConfig." + addNum + ".ParaList", listToString(paraValueList, ",")));
                        setParamList.add(new ParameterValueStructStr("InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor.MonitorConfig." + addNum + ".TimeList", listToString(timeValueList, ",")));
                    }
                }
            }
        }else{
            logger.info("Enable传值错误，Enable:{}", Enable);
            retMap.put(Constant.RESULT, -102);
            return retMap;
        }
        if(setParamList.size()>0){
            instructionMethodService.setParameterValue(gatewayMacAddress,setParamList);
        }
        retMap.put(Constant.RESULT, 0);
        String gateWaySpeedPolicy = gatewayMacAddress + "gateWaySpeedPolicy";
        //查询redis中的配置
        String policyMapStr = redisClientTemplate.get(gateWaySpeedPolicy);
        logger.info("setLanSpeedReportPolicy get redis policyMapStr:{}",policyMapStr);
        Map<String, Object> policyMap = new HashMap<String, Object>();
        if(!org.apache.commons.lang.StringUtils.isEmpty(policyMapStr)) {
        	
        	 policyMap = JSON.parseObject(policyMapStr, Map.class);
        	 for(int i = 0 ;i < subDeviceMacList.size();i++){
                 policyMap.put(subDeviceMacList.get(i),Enable);
             }
        }else{
        	for(int i = 0 ;i < subDeviceMacList.size();i++){
                policyMap.put(subDeviceMacList.get(i),Enable);
            }
        }
        
        redisClientTemplate.set(gateWaySpeedPolicy, JSON.toJSONString(policyMap));
        logger.info("End invoke setLanSpeedReportPolicy:{}", retMap);
        return retMap;
    }


    @Override
    public Map<String, Object> getLanSpeedReportPolicy(Map<String, Object> parameter) {
        logger.info("Start invoke getLanDeviceTrafficStatus:{}", parameter);
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);

        // 拼装返回消息
        Map<String, Object> resultData = new HashMap<>();
        Map<String, Object> retMap = new HashMap<>();
        retMap.put(Constant.ID, parameter.get(Constant.ID));
        retMap.put(Constant.CMDTYPE, parameter.get(Constant.CMDTYPE));
        retMap.put(Constant.SEQUENCEID, parameter.get(Constant.SEQUENCEID));
        retMap.put("ResultData",resultData);
        //获取网关MAC地址
        String gatewayMacAddress = null != macMap.get(Constant.MAC) ? macMap.get(Constant.MAC).toString() : "";

        if(org.apache.commons.lang.StringUtils.isEmpty(gatewayMacAddress)){
            retMap.put(Constant.RESULT,-102);
            resultData.put(Constant.FAILREASON, "MAC为空");
            logger.info("MAC为空");
            return retMap;
        }
        GatewayInfo gateQuery = new GatewayInfo();
        gateQuery.setGatewayMacaddress(gatewayMacAddress);
        List<GatewayInfo> list = gatewayInfoMapper.queryList(gateQuery);

        if(list == null || list.size() <=0){
            logger.info("MAC为:" + gatewayMacAddress + "的网关不存在");
            retMap.put(Constant.RESULT, -201);
            resultData.put(Constant.FAILREASON, "MAC不存在");
            return retMap;
        }
        //改为从redis获取下挂设备速率策略
        List<Map<String,Object>> subDevicesList = new ArrayList<>();
        String gateWaySpeedPolicy = gatewayMacAddress + "gateWaySpeedPolicy";
        String policyMapStr = redisClientTemplate.get(gateWaySpeedPolicy);
        logger.info("getLanSpeedReportPolicy get redis policyMapStr:{}",policyMapStr);
        if(!org.apache.commons.lang.StringUtils.isEmpty(policyMapStr)) {
            //从redis取5.5.4.	设置下挂设备实时速率统计开关接口设置的值，如果是false，则不处理了
            String reportSpeedTestKey = gatewayMacAddress + "reportSpeedTestKey";
            String isCanReport = redisClientTemplate.get(reportSpeedTestKey);
            logger.info("getLanSpeedReportPolicy get redis isCanReport:{}",isCanReport);
            boolean isClose = false;
            if((!org.apache.commons.lang.StringUtils.isEmpty(isCanReport))&&"false".equals(isCanReport)){
                isClose = true;
            }
            Map<String, Object> policyMap = JSON.parseObject(policyMapStr, Map.class);
            for (Map.Entry<String, Object> entry : policyMap.entrySet()) {
                Map<String, Object> subDevicesMap = new HashMap<>();
                if(isClose) {//如果总开关是关闭，则所有下挂设备的开关都是关闭
                    subDevicesMap.put("Enable", "0");
                }else{
                    subDevicesMap.put("Enable", entry.getValue());
                }
                subDevicesMap.put("DeviceMAC", entry.getKey());
                subDevicesList.add(subDevicesMap);
            }
        }
       /*         //2.获取下挂设备所有节点名称
        Map < String, Object > nameMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);
        logger.info("getLanSpeedReportPolicy getParameterNames nameMap:{}", nameMap);
        //获取周期采样所有节点名称
        Map<String, Object> monitorNameMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor.MonitorConfig.", false);
        logger.info("getLanSpeedReportPolicy getParameterNames monitorNameMap:{}", monitorNameMap);

        //下挂设备MAC地址
        String subMacAddress = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.MACAddress";
        //周期采样监控开关
        String samplingEnable = "InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor.Enable";
        //需要监控的关键参数
        String needMonitorParams = "InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor.MonitorConfig.[0-9]+.ParaList";
        ///需要监控的关键参数的采样周期
        String needMonitorParamsTime = "InternetGatewayDevice.DeviceInfo.X_CMCC_Monitor.MonitorConfig.[0-9]+.TimeList";

        List<String> subMacNameList = new ArrayList<>();
        subMacNameList.add(subMacAddress);
        subMacNameList.add(needMonitorParams);
        subMacNameList.add(needMonitorParamsTime);

        List<String> subMacMatchNameList = new ArrayList<>();
        subMacMatchNameList.add(samplingEnable);
        //3.根据正则表达式匹配查找所有下挂终端MAC节点名称
        for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
            InstructionUtil.getName(subMacMatchNameList, subMacNameList, entry.getKey());
        }
        for (Map.Entry<String, Object> entry : monitorNameMap.entrySet()) {
            InstructionUtil.getName(subMacMatchNameList, subMacNameList, entry.getKey());
        }
        //4.根据具体的节点获取相应的value
        Map<String, Object> subMacValueMap = instructionMethodService.getParameterValues(gatewayMacAddress, subMacMatchNameList);
        logger.info("getLanSpeedReportPolicy getParameterValues subMacValueMap:{}", subMacValueMap);
        Boolean isEnable = (Boolean) subMacValueMap.get(samplingEnable);
        Pattern pattern = Pattern.compile(subMacAddress);
        Pattern patternPara = Pattern.compile(needMonitorParams);
        List<Map<String,Object>> subDevicesList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : subMacValueMap.entrySet()) {
            Map<String,Object> subDevicesMap = new HashMap<>();
            Matcher matcher = pattern.matcher(entry.getKey());
            if(matcher.matches()){
                subDevicesMap.put("DeviceMAC",entry.getValue());
                if(!isEnable){
                    subDevicesMap.put("Enable","0");
                }else {
                    String bytesSend = entry.getKey().substring(0, entry.getKey().indexOf("MACAddress")) + "X_CMCC_Stats.BytesReceived";
                    Boolean isHaveBytesSend = false;
                    for (Map.Entry<String, Object> entry1 : subMacValueMap.entrySet()) {
                        Matcher matcherPara = patternPara.matcher(entry1.getKey());
                        if (matcherPara.matches()) {
                            String paraListStr = (String) entry1.getValue();
                            List<String> paraList = Arrays.asList(paraListStr.split(","));
                            for (int i = 0; i < paraList.size(); i++) {
                                if (paraList.get(i).equals(bytesSend)) {
                                    isHaveBytesSend = true;
                                }
                            }
                        }
                    }
                    if(isHaveBytesSend){
                        subDevicesMap.put("Enable","1");
                    }else{
                        subDevicesMap.put("Enable","0");
                    }
                }
                subDevicesList.add(subDevicesMap);
            }
        }*/
        resultData.put("Devices", subDevicesList);
        retMap.put("ResultData",resultData);
        retMap.put(Constant.RESULT, 0);
        logger.info("End invoke getLanSpeedReportPolicy:{}", retMap);
        return retMap;
    }

    /**
     * list按照格式拼装为字符串
     * @param list
     * @param regx
     * @return
     */
    public  String listToString(List<String> list,String regx){
        if(list == null || list.size() < 1){
            return "";
        }
        StringBuffer str = new StringBuffer();
        for(int i = 0;i<list.size();i++){
            if(i == list.size() -1){
                str.append(list.get(i));
            }else{
                str.append(list.get(i) + ",");
            }
        }
        return str.toString();
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
     * 除法向上取整
     * @param a
     * @param b
     * @return
     */
    public int getMyInt(int a,int b) {
        return(((double)a/(double)b)>(a/b)?a/b+1:a/b);
    }

    /**
     * 构造获取网关WAN侧和LAN侧流量统计接口返回数据key
     * @param resultData
     */
    private void buildHgPortsTrafficStatusData(Map<String, Object> resultData)
    {
        resultData.put("WANRX", "");
        resultData.put("WANTX", "");
        resultData.put("WLAN1RX", "");
        resultData.put("WLAN1TX", "");
        resultData.put("WLAN2RX", "");
        resultData.put("WLAN2TX", "");
        resultData.put("Port1RX", "");
        resultData.put("Port1TX", "");
        resultData.put("Port2RX", "");
        resultData.put("Port2TX", "");
        resultData.put("Port3RX", "");
        resultData.put("Port3TX", "");
        resultData.put("Port4RX", "");
        resultData.put("Port4TX", "");
    }
}
