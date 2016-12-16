package com.cmiot.rms.services.impl;

import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.enums.PPPErrorCodeEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.GatewayBaseInfoService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.GatewayPluginAccessControlService;
import com.cmiot.rms.services.instruction.InstructionMethodService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网关基本信息查询（横向接口）
 * Created by panmingguo on 2016/5/4.
 */
public class GatewayBaseInfoServiceImpl implements GatewayBaseInfoService {

    private static Logger logger = LoggerFactory.getLogger(GatewayBaseInfoServiceImpl.class);

    @Autowired
    InstructionMethodService instructionMethodService;

    @Autowired
    GatewayInfoService gatewayInfoService;
    
    @Autowired
    private AreaService amsAreaService;

    @Autowired
    GatewayPluginAccessControlService pluginAccessControlService;

    /**
     * 查询网关资源占用率
     * @param params
     * @return
     */
    @Override
    public Map<String, Object> getHgResourceUsage(Map<String, Object> params) {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put(Constant.ID, params.get(Constant.ID));
        retMap.put(Constant.CMDTYPE, params.get(Constant.CMDTYPE));
        retMap.put(Constant.SEQUENCEID, params.get(Constant.SEQUENCEID));

        //1.提取网关MAC
        Map<String, Object> macMap = (Map<String, Object>)params.get(Constant.PARAMETER);
        String gatewayMacAddress = macMap.get(Constant.MAC).toString();
        if (gatewayMacAddress == null || gatewayMacAddress.equals("")) {
            retMap.put(Constant.RESULT, -102);
            return retMap;
        }

        //2.查询网关信息
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayMacaddress(gatewayMacAddress);
        GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        if (gatewayInfo == null) {
            retMap.put(Constant.RESULT, -201);
            return retMap;
        }

        //3.下发指令并获取结果
        List<String> namesList = new ArrayList<>();
        namesList.add(ConstantDiagnose.CPU_USAGE);
        namesList.add(ConstantDiagnose.RAM_TOTAL);
        namesList.add(ConstantDiagnose.RAM_FREE);
        Map<String, Object> resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, namesList);
        if (resultMap == null || resultMap.size() == 0) {
            retMap.put(Constant.RESULT, -400);
            return retMap;
        }

        //4.拼装返回结果
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("CPUPercent", "" + resultMap.get(ConstantDiagnose.CPU_USAGE));

        int freeRam = 0;
        if(resultMap.get(ConstantDiagnose.RAM_FREE) != null &&
                resultMap.get(ConstantDiagnose.RAM_FREE) != ""){
            freeRam = (int) resultMap.get(ConstantDiagnose.RAM_FREE);
        }

        int totalRam = 0;
        if(resultMap.get(ConstantDiagnose.RAM_TOTAL) != null &&
                resultMap.get(ConstantDiagnose.RAM_TOTAL) != "") {
            totalRam = (int) resultMap.get(ConstantDiagnose.RAM_TOTAL);
        }

        int ramPercent = 0;
        if(freeRam != 0 && totalRam != 0 && freeRam < totalRam){
            ramPercent = (totalRam - freeRam) * 100 / totalRam;
        }

        resultData.put("RAMPercent", "" + ramPercent);
        retMap.put(Constant.RESULTDATA, resultData);
        retMap.put(Constant.RESULT, 0);

        return retMap;
    }

    /**
     * 查询插件运行环境
     * @param params
     * @return
     */
    public Map<String, Object> queryPluginExecEnv(Map<String, Object> params) {
        String mac = "" + params.get(Constant.MAC);
        if (null == params || params.size() == 0 || org.apache.commons.lang.StringUtils.isBlank(mac)) {
            return Collections.emptyMap();
        }

        Map<String, Object> names = instructionMethodService.getParameterNames(mac, "InternetGatewayDevice.SoftwareModules.ExecEnv.", true);
        if (names == null || names.size() < 2) {
            // 实际必须有两个环境:jvm,osgi
            logger.warn("插件运行环境至少需含有jvm和osgi:" + mac);
            return Collections.EMPTY_MAP;
        }

        List<String> fullNames = new ArrayList<>();
        for (String name : names.keySet()) {
            fullNames.add(name + "Name");
            fullNames.add(name + "Vendor");
            fullNames.add(name + "Version");
            fullNames.add(name + "ParentExecEnv");
            fullNames.add(name + "Enable");
        }

        Map<String, Object> values = instructionMethodService.getParameterValues(mac, fullNames);
        Map<String, Object> data = new HashMap<>();

        for (String name : names.keySet()) {
            //忽略不可用的
            boolean enable = (boolean) values.get(name + "Enable");
            if (!enable) {
                continue;
            }

            if (((String) (values.get(name + "Name"))).equalsIgnoreCase("OSGI") || values.get(name + "ParentExecEnv") != null) {
                Map<String, Object> osgiEnv = new HashMap<>();
                osgiEnv.put("name", values.get(name + "Name"));
                osgiEnv.put("vendor", values.get(name + "Vendor"));
                osgiEnv.put("version", values.get(name + "Version"));
                data.put("osgi", osgiEnv);
            }

            if (((String) (values.get(name + "Name"))).equalsIgnoreCase("JVM") || values.get(name + "ParentExecEnv") == null) {
                Map<String, Object> jvmEnv = new HashMap<>();
                jvmEnv.put("name", values.get(name + "Name"));
                jvmEnv.put("vendor", values.get(name + "Vendor"));
                jvmEnv.put("version", values.get(name + "Version"));
                data.put("jvm", jvmEnv);
            }
        }

        return data;
    }

    @Override
    public Map<String, Object> getHgSystemInfo(Map<String, Object> params) {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put(Constant.ID, params.get(Constant.ID));
        retMap.put(Constant.CMDTYPE, params.get(Constant.CMDTYPE));
        retMap.put(Constant.SEQUENCEID, params.get(Constant.SEQUENCEID));

        //1.提取网关MAC
        Map<String, Object> paramsMap = (Map<String, Object>)params.get(Constant.PARAMETER);
        String gatewayMacAddress = (String) paramsMap.get(Constant.MAC);
        if (gatewayMacAddress == null || gatewayMacAddress.equals("")) {
            retMap.put(Constant.RESULT, -102);
            return retMap;
        }

        //2.查询网关信息
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayMacaddress(gatewayMacAddress);
        GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        if (gatewayInfo == null) {
            retMap.put(Constant.RESULT, -201);
            return retMap;
        }

        Map<String, Object> resultData = new HashMap<>();
        resultData.put("DEVType", gatewayInfo.getGatewayType());
        resultData.put("ProductCLass", gatewayInfo.getGatewayModel());
        resultData.put("HDVersion", gatewayInfo.getGatewayHardwareVersion());
        resultData.put("SWVersion", gatewayInfo.getGatewayVersion());

        //动态查询osgi信息
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("MAC", gatewayMacAddress);
        Map<String, Object> envs = queryPluginExecEnv(param);
        Map<String, Object> osgi = (Map<String, Object>) envs.get("osgi");
        if (osgi != null) {
            resultData.put("OSGIVersion", osgi.get("version"));
        } else {
            resultData.put("OSGIVersion", "");
        }

        //3.下发指令并获取结果

        //下发参数全路径
        List<String> namesList = new ArrayList<>();
        namesList.add(ConstantDiagnose.GATEWAY_ALIAS);
        namesList.add(ConstantDiagnose.CPU_CLASS);
        namesList.add(ConstantDiagnose.RAM_TOTAL);

        //WIFI信息从网关获取
        //获取LanDevice所有子节点
        List<String> wlanPrefixes = new ArrayList<>();
        Map<String, Object> lanDevices = instructionMethodService.getParameterNames(gatewayMacAddress,
                "InternetGatewayDevice.LANDevice.", false);
        Pattern pattern = Pattern.compile("InternetGatewayDevice.LANDevice.[0-9]+.WLANConfiguration.[0-9]+.$");
        for (String name : lanDevices.keySet()) {
            Matcher m = pattern.matcher(name);
            if (m.find()) {
                wlanPrefixes.add(name);
            }
        }

        for (String p : wlanPrefixes) {
            namesList.add(p + "Standard");
        }

        //取网关web维护地址
        String webIp = null;
        Pattern webIPPattern = Pattern.compile("InternetGatewayDevice.LANDevice.[0-9]+.LANHostConfigManagement.IPInterface.[0-9]+.IPInterfaceIPAddress$");
        for (String name : lanDevices.keySet()) {
            Matcher m = webIPPattern.matcher(name);
            if (m.find()) {
                webIp = name;
                namesList.add(webIp);
                break;
            }
        }

        //WAN连接
        List<String> prefixes = getWANConnectionPrefix(gatewayMacAddress);
        String prefix = "";
        for (String p : prefixes) {
            if (p.indexOf("WANPPPConnection") > 0) {
                //优先找ppp wan
                prefix = p;
                break;
            }

            //否则,取最后一条ip wan
            prefix = p;
        }

        if (!prefix.equals("")) {
            logger.info("wan prefix:" + prefix);

            namesList.add(prefix + "Enable");
            namesList.add(prefix + "X_CMCC_IPMode");
            namesList.add(prefix + "ExternalIPAddress");
            namesList.add(prefix + "X_CMCC_IPv6IPAddress");
            namesList.add(prefix + "X_CMCC_Dslite_Enable");

            //是否是PPP
            if (prefix.indexOf("WANPPPConnection") >= 0) {
                namesList.add(prefix + "Username");
            }
        }

        Map<String, Object> resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, namesList);
        if (resultMap == null || resultMap.size() == 0) {
            retMap.put(Constant.RESULT, -400);
            return retMap;
        }

        resultData.put("DevName", resultMap.get(ConstantDiagnose.GATEWAY_ALIAS));
        resultData.put("CPUClass",resultMap.get(ConstantDiagnose.CPU_CLASS));
        if (resultMap.get(ConstantDiagnose.RAM_TOTAL) != null) {
            resultData.put("RamSize", "" + (int)resultMap.get(ConstantDiagnose.RAM_TOTAL) / 1024);
        } else {
            resultData.put("RamSize", "0");
        }

        //可能多个wlan的standard会重复,先去重
        Set<String> standards = new HashSet<>();
        for (String p : wlanPrefixes) {
            //返回值可能以,分隔
            String standard = (String) resultMap.get(p + "Standard");
            if (standard != null) {
                String[] array = standard.split(",");
                for (String a : array) {
                    standards.add(a);
                }
            }
        }
        StringBuffer buffer = new StringBuffer();
        for (String s : standards) {
            buffer.append("802.11" + s + "/");
        }
        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        if (buffer.length() == 0) {
            resultData.put("WiFiMode", null);
            logger.warn("InternetGatewayDevice.LANDevice.{i}. WLANConfiguration.{i}.Standard无值:" + gatewayMacAddress);
        } else {
            resultData.put("WiFiMode", buffer.toString());
        }

        resultData.put("LanIPAddr", resultMap.get(webIp));

        //TODO FlashSize,OSVersion
        resultData.put("FlashSize", null);
        resultData.put("OSVersion", null);

        //处理IPv6Status
        Integer ipMode = (Integer) resultMap.get(prefix + "X_CMCC_IPMode");
        if (ipMode != null) {
            if (ipMode == 2 || ipMode == 3) {
                //ipv6模式
                boolean enable = (boolean) resultMap.get(prefix + "Enable");
                boolean deliteEnable = (boolean) resultMap.get(prefix + "X_CMCC_Dslite_Enable");
                if (!enable) {
                    resultData.put("IPv6Status", "0");
                } else {
                    if (deliteEnable) {
                        resultData.put("IPv6Status", "2");
                    } else {
                        resultData.put("IPv6Status", "1");
                    }
                }
            } else {
                //ipv4模式
                resultData.put("IPv6Status", "0");
            }
        }

        //处理WanIPv4Addr,WanIPv6Addr
        resultData.put("WanIPv4Addr", resultMap.get(prefix + "ExternalIPAddress"));
        resultData.put("WanIPv6Addr", resultMap.get(prefix + "X_CMCC_IPv6IPAddress"));
        //宽带账号
        resultData.put("Broadband_Account", resultMap.get(prefix + "Username"));

        retMap.put(Constant.RESULTDATA, resultData);
        retMap.put(Constant.RESULT, 0);

        return retMap;
    }

    @Override
    public Map<String, Object> getHgTimeDuration(Map<String, Object> params) {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put(Constant.ID, params.get(Constant.ID));
        retMap.put(Constant.CMDTYPE, params.get(Constant.CMDTYPE));
        retMap.put(Constant.SEQUENCEID, params.get(Constant.SEQUENCEID));

        //1.提取网关MAC
        Map<String, Object> macMap = (Map<String, Object>)params.get(Constant.PARAMETER);
        String mac = macMap.get(Constant.MAC).toString();
        if (mac == null || mac.equals("")) {
            retMap.put(Constant.RESULT, -102);
            return retMap;
        }

        //2.查询网关信息
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayMacaddress(mac);
        GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        if (gatewayInfo == null) {
            retMap.put(Constant.RESULT, -201);
            return retMap;
        }

        //3.下发指令并获取结果
        List<String> namesList = new ArrayList<>();
        namesList.add(ConstantDiagnose.SYSTEM_UP_TIME);
        namesList.add(ConstantDiagnose.PON_UP_TIME);

        //PPPOE拨号成功持续时间
        List<String> prefixes = getWANConnectionPrefix(mac);
        String p = null;
        for (String prefix : prefixes) {
            if (prefix.indexOf("WANPPPConnection") >= 0) {
                p = prefix;
                namesList.add(prefix + "Uptime");
                break;
            }
        }

        Map<String, Object> resultMap = instructionMethodService.getParameterValues(mac, namesList);
        if (resultMap == null || resultMap.size() == 0) {
            retMap.put(Constant.RESULT, -400);
            return retMap;
        }

        //4.拼装返回结果
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("SYSDuration", "" + resultMap.get(ConstantDiagnose.SYSTEM_UP_TIME));
        resultData.put("PONDuration",  "" + resultMap.get(ConstantDiagnose.PON_UP_TIME));
        resultData.put("PPPoEDuration", "" + resultMap.get(p + "Uptime"));

        retMap.put(Constant.RESULTDATA, resultData);
        retMap.put(Constant.RESULT, 0);

        return retMap;
    }

    /**
     * 获取所有承载INTERNET业务的WAN连接前缀
     * @param mac
     * @return 不存在时返回null,存在时返回InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANPPPConnection.
     * 或InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANIPConnection.
     */
    public List<String> getWANConnectionPrefix(String mac) {
        List<String> prefixes = new ArrayList<>();

        //获取WanDevice所有子节点
        Map<String, Object> wanDevices = instructionMethodService.getParameterNames(mac,
                "InternetGatewayDevice.WANDevice.1.", false);

        //查找承载Internet的WAN连接
        List<String> namesList = new ArrayList<>();

        String pppServicePattern = "InternetGatewayDevice.WANDevice.[0-9]+.WANConnectionDevice.[0-9]+.WANPPPConnection.[0-9]+.X_CMCC_ServiceList";
        Pattern pattern = Pattern.compile(pppServicePattern);
        for (String name : wanDevices.keySet()) {
            Matcher m = pattern.matcher(name);
            if (m.find()) {
                namesList.add(name);
            }
        }

        String ipServicePattern = "InternetGatewayDevice.WANDevice.[0-9]+.WANConnectionDevice.[0-9]+.WANIPConnection.[0-9]+.X_CMCC_ServiceList";
        pattern = Pattern.compile(ipServicePattern);
        for (String name : wanDevices.keySet()) {
            Matcher m = pattern.matcher(name);
            if (m.find()) {
                namesList.add(name);
            }
        }

        if (namesList.size() == 0) {
            logger.warn("wan prefix not found, mac:" + mac);
            return prefixes;
        }

        Map<String, Object> resultMap = instructionMethodService.getParameterValues(mac, namesList);
        for (String name : namesList) {
            String v = (String) resultMap.get(name);
            if (v != null && v.indexOf("INTERNET") >= 0) {
                //确定WAN连接前缀
                String prefix = name.substring(0, name.indexOf("X_CMCC_ServiceList"));
                prefixes.add(prefix);
            }
        }

        return prefixes;
    }

    /**
     * 返回WLAN前缀
     * @param mac
     * @return InternetGatewayDevice.LANDevice.{i}. WLANConfiguration.{i}.
     */
    public List<String> getWLANPrefix(String mac) {
        List<String> prefixes = new ArrayList<>();

        //获取LanDevice所有子节点
        Map<String, Object> lanDevices = instructionMethodService.getParameterNames(mac,
                "InternetGatewayDevice.LANDevice.", false);
        Pattern pattern = Pattern.compile("InternetGatewayDevice.LANDevice.[0-9]+.WLANConfiguration.[0-9]+.$");
        for (String name : lanDevices.keySet()) {
            Matcher m = pattern.matcher(name);
            if (m.find()) {
                prefixes.add(name);
            }
        }

        return prefixes;
    }

    @Override
    public Map<String, Object> getDeviceInfoAndStatus(Map<String, Object> parameter) {
        //1.根据网关MAC查询网关ID
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = macMap.get(Constant.MAC) == null ? null :macMap.get(Constant.MAC).toString();
        parameter.remove(Constant.PARAMETER);
        parameter.remove(Constant.RPCMETHOD);
        if(gatewayMacAddress == null){
            logger.info("请求获取SSID信息时输入MAC地址为空");
            parameter.put(Constant.RESULT, -102);
            Map<String, Object> resultData = new HashMap<String, Object>();
            resultData.put(Constant.FAILREASON, "MAC地址为空");
            parameter.put(Constant.RESULTDATA, resultData);
            return parameter;
        }else{
            GatewayInfo gatewayInfo = new GatewayInfo();
            gatewayInfo.setGatewayMacaddress(gatewayMacAddress);
            GatewayInfo info = gatewayInfoService.selectGatewayInfo(gatewayInfo);
            if(info  == null){
                String msg = "请求获取SSID信息时输入MAC地址"+gatewayMacAddress+"不存在";
                logger.info(msg);
                parameter.put(Constant.RESULT, -201);
                Map<String, Object> resultData = new HashMap<String, Object>();
                resultData.put(Constant.FAILREASON, msg);
                parameter.put(Constant.RESULTDATA, resultData);
                return parameter;
            }
        }
        //2.下发指令并获取结果
        Map<String,Object> map=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.DeviceInfo.", false);
        if(map == null ){
            return commonExAndMap("网关MAC地址："+gatewayMacAddress+"请求获取SSIDX信息失败，原因：网关MAC地址不存在","网关MAC地址不存在");
        }
        Map<String,Object> map1=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.Services.", false);
        if(map1 == null ){
            return commonExAndMap("网关MAC地址："+gatewayMacAddress+"请求获取SSIDX信息失败，原因：网关MAC地址不存在","网关MAC地址不存在");
        }
        Map<String,Object> map2=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);
        if(map2 == null ){
            return commonExAndMap("网关MAC地址："+gatewayMacAddress+"请求获取SSIDX信息失败，原因：网关MAC地址不存在","网关MAC地址不存在");
        }
        Map<String,Object> map3=instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.WANDevice.", false);
        if(map3 == null ){
            return commonExAndMap("网关MAC地址："+gatewayMacAddress+"请求获取SSIDX信息失败，原因：网关MAC地址不存在","网关MAC地址不存在");
        }
        //验证map中所有相对应的属性
        List<String> cloList = new ArrayList<>();
        String nodeHeader =  "InternetGatewayDevice.DeviceInfo";
        String nodeHeader1 = "InternetGatewayDevice.Services.VoiceService.[0-9]";

        String nodeHeader2 = "InternetGatewayDevice.LANDevice";
        String nodeHeader3 = "InternetGatewayDevice.WANDevice.[0-9].WANConnectionDevice.[0-9]";

        String productCLass,sWVersion,hDVersion,cPUPercent,rAMPercent,sYSDuration,pONUpTimec,sSID,
                wifiEnable,voIPName,voIPRegStatus,devNum,hostNumberOfEntries,dNSServers,iPv4WANStatus,iPv6WANStatus,iPv4v6DialReason,time,serviceList,uptime,
                totalMemory, freeMemory;
        productCLass = "InternetGatewayDevice.DeviceInfo.ProductClass";
        sWVersion = nodeHeader +    ".SoftwareVersion";
        hDVersion = nodeHeader +    ".HardwareVersion";
        cPUPercent = nodeHeader+    ".ProcessStatus.CPUUsage";
       // rAMPercent = nodeHeader+     ".X_CMCC_RAMUsage";
        totalMemory = nodeHeader+     ".MemoryStatus.Total";
        freeMemory = nodeHeader+     ".MemoryStatus.Free";
        sYSDuration =  nodeHeader + ".UpTime";
        pONUpTimec= nodeHeader +    ".X_CMCC_PONUpTime";
        sSID = nodeHeader2+".[0-9].WLANConfiguration.1.SSID";
        voIPName = nodeHeader1 +    ".VoiceProfile.[0-9].Line.[0-9].SIP.URI";
        voIPRegStatus = nodeHeader1 + ".PhyInterface.[0-9].Tests.X_CMCC_SimulateTest.Status"; //找不到的参数字段
        devNum = nodeHeader2+      ".[0-9].Hosts.Host.[0-9].InterfaceType"; //802.11
        hostNumberOfEntries   ="InternetGatewayDevice.LANDevice.[0-9].Hosts.HostNumberOfEntries"; //下挂主机条数
        dNSServers = nodeHeader3 + ".WANPPPConnection.[0-9].DNSServers";  /*20160813 add remark 测试要求改成PPP*/
        iPv4WANStatus = nodeHeader3 + ".WANPPPConnection.[0-9].ConnectionStatus";  /*20160813 add remark 测试要求改成PPP*/
        iPv6WANStatus = nodeHeader3 + ".WANPPPConnection.[0-9].X_CMCC_IPv6ConnStatus";
        iPv4v6DialReason = nodeHeader3 + ".WANPPPConnection.[0-9].LastConnectionError";
        wifiEnable = nodeHeader3 + ".WANIPConnection.[0-9].Enable";
        serviceList = nodeHeader3 + ".WANIPConnection.[0-9].X_CMCC_ServiceList";
        uptime = nodeHeader3 +".WANPPPConnection.[0-9].Uptime";
        time = "InternetGatewayDevice.Time.CurrentLocalTime";
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            logger.info("  map~~ "+entry.getKey());
            if(entry.getKey().equals(productCLass)){
                cloList.add(entry.getKey());
            }
            if(entry.getKey().matches(sWVersion)||entry.getKey().matches(hDVersion)||
                    entry.getKey().matches(cPUPercent)||entry.getKey().matches(sWVersion)||entry.getKey().matches(totalMemory)||entry.getKey().matches(freeMemory)
                    ||entry.getKey().matches(sYSDuration)||entry.getKey().matches(pONUpTimec)){//如果与设备属性匹配
                cloList.add(entry.getKey());
            }
        }
        for (Map.Entry<String, Object> entry1 : map1.entrySet()) {
            logger.info("  map1~~ "+entry1.getKey());
            if(entry1.getKey().matches(voIPName)||entry1.getKey().matches(voIPRegStatus)){//如果与设备属性匹配
                cloList.add(entry1.getKey());
            }
        }
        for (Map.Entry<String, Object> entry2 : map2.entrySet()) {
            logger.info("  map2~~ "+entry2.getKey());
            if(entry2.getKey().matches(sSID)){
                cloList.add(entry2.getKey());
            }
        }
        for (Map.Entry<String, Object> entry2 : map2.entrySet()) {
            if(entry2.getKey().matches(devNum)){//如果与设备属性匹配
                cloList.add(entry2.getKey());
            }
        }
        for (Map.Entry<String, Object> entry2 : map2.entrySet()) {
            if(entry2.getKey().matches(hostNumberOfEntries)){//如果与设备属性匹配
                cloList.add(entry2.getKey());
            }
        }
        for (Map.Entry<String, Object> entry3 : map3.entrySet()) {
            logger.info("  map3~~ "+entry3.getKey());
            if(entry3.getKey().matches(dNSServers)||entry3.getKey().matches(iPv4WANStatus)||entry3.getKey().matches(iPv6WANStatus)
                    ||entry3.getKey().matches(iPv4v6DialReason)||entry3.getKey().matches(wifiEnable)||entry3.getKey().matches(serviceList)||entry3.getKey().matches(uptime)){//如果与设备属性匹配
                cloList.add(entry3.getKey());
            }
        }
        cloList.add(time);

        String updateTimeTemp ="";
        int hostNumbers = 0 ;
        Map<String,Object> wifiInfo= new HashMap<>();
        logger.info(" cloList size" + cloList.size());
        if(cloList.size()>0){
            Map<String,Object> resultMap= new HashMap<>();
            for(int j =0;j<cloList.size();j++){
                logger.info("  Command issued  "+cloList.get(j));
            }
            resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, cloList);
            
            String totalMemoryValue ="";
            String freeMemoryValue = "";
            int wifiConnectDevNum = 0;
            for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                logger.info("  return key  " + entry.getKey() + "  return val " + entry.getValue());
                if(entry.getKey().equals(uptime)){
                    updateTimeTemp = (String) entry.getValue();
                }
                if(entry.getKey().matches(serviceList)){
                    if(entry.getValue().equals("Internet")){
                        wifiInfo.put("PPPoEDuration", updateTimeTemp);
                    }
                }
                if(entry.getKey().matches(dNSServers)){
                    if(entry.getValue()!=""){
                        String [] ipv4DNS = entry.getValue().toString().split(",");
                        if(ipv4DNS.length > 0){
                            wifiInfo.put("InternetDNS1",ipv4DNS[0]);
                        }
                        if(ipv4DNS.length > 1){
                            wifiInfo.put("InternetDNS2",ipv4DNS[1]);
                        }
                    }
                }
               /* if(entry.getKey().matches("InternetGatewayDevice.LANDevice.[0-9].Hosts.HostNumberOfEntries")){
                    logger.info(" hostNumbers " + entry.getValue().toString());
                    hostNumbers = (int) entry.getValue();
                    wifiInfo.put("DevNum", String.valueOf(hostNumbers));
                }*/
                if(entry.getKey().matches(devNum)){
                    logger.info("InterfaceType " + entry.getValue());
                    if(entry.getValue().equals("802.11")){
                    	wifiConnectDevNum += 1;
                       // wifiInfo.put("DevNum", String.valueOf(hostNumbers));
                    }else{
                      //  wifiInfo.put("DevNum", "0");
                    }
                }
                if(entry.getKey().equals(productCLass)){wifiInfo.put("ProductCLass", entry.getValue());}
                if(entry.getKey().matches(sWVersion)){wifiInfo.put("SWVersion", entry.getValue());}
                if(entry.getKey().matches(hDVersion)){wifiInfo.put("HDVersion", entry.getValue());}
                if(entry.getKey().matches(cPUPercent)){wifiInfo.put("CPUPercent", entry.getValue() == null?"":entry.getValue().toString());}
               // if(entry.getKey().matches(rAMPercent)){wifiInfo.put("RAMPercent", entry.getValue());}
                if(entry.getKey().matches(totalMemory)){
                	totalMemoryValue = entry.getValue() == null ? "0" : entry.getValue().toString();
                }
                if(entry.getKey().matches(freeMemory)){
                	freeMemoryValue= entry.getValue()== null ? "0" : entry.getValue().toString();
                }
                if(entry.getKey().matches(sYSDuration)){wifiInfo.put("SYSDuration", entry.getValue() == null?"":entry.getValue().toString());}
                if(entry.getKey().matches(pONUpTimec)){wifiInfo.put("PONDuration", entry.getValue());}
                if(entry.getKey().matches(sSID)){wifiInfo.put("SSID1", entry.getValue());}
                if(entry.getKey().matches(voIPName)){wifiInfo.put("VoIPName", entry.getValue());}
                if(entry.getKey().matches(voIPRegStatus)){wifiInfo.put("VoIPRegStatus", entry.getValue());}
                if(entry.getKey().matches(iPv4WANStatus)){
                    wifiInfo.put("IPv4WANStatus", entry.getValue());
                    if(entry.getValue().equals("Connected")){
                        wifiInfo.put("IPv4ConnectionStatus", "0");
                    }
                }
                if(entry.getKey().matches(iPv6WANStatus)){
                    wifiInfo.put("IPv6WANStatus1", entry.getValue());
                    if(entry.getValue().equals("Connected")){
                        wifiInfo.put("IPv6ConnectionStatus1", "0");
                    }
                }
                if(entry.getKey().matches(iPv4v6DialReason)){
                    for (Map.Entry<String, Object> tm : resultMap.entrySet()) {
                    	if(tm.getKey().matches(iPv6WANStatus)){
                    		if(tm.getValue().equals("Unconfigured")){
                                wifiInfo.put("IPv6DialReason1", PPPErrorCodeEnum.ERROR_NO_VALID_CONNECTION + "," + PPPErrorCodeEnum.ERROR_NO_VALID_CONNECTION.description());
                            }else{
                            	wifiInfo.put("IPv6DialReason1", entry.getValue());
                            }
                    	}
                    	if(tm.getKey().matches(iPv4WANStatus)){
                            if(tm.getValue().equals("Unconfigured")){
                                wifiInfo.put("IPv4DialReason", PPPErrorCodeEnum.ERROR_NO_VALID_CONNECTION + "," + PPPErrorCodeEnum.ERROR_NO_VALID_CONNECTION.description());
                            }else{
                            	wifiInfo.put("IPv4DialReason",entry.getValue());
                            }
                        }
                    }
                 }
                if(entry.getKey().matches(wifiEnable)){
                    if(entry.getValue().equals(true)){
                        wifiInfo.put("WifiEnable", "1");
                    }else{
                        wifiInfo.put("WifiEnable", "0");
                    }
                }
                if(entry.getKey().matches(time)){wifiInfo.put("Time", entry.getValue());}

            }
            
            wifiInfo.put("DevNum", wifiConnectDevNum);
            
            if(!"0".equals(totalMemoryValue)){
            	double totalMemoryValLong = Double.parseDouble(totalMemoryValue);
            	double freeMemoryValLong = Double.parseDouble(freeMemoryValue);
            	int ramPercent =(int) (((totalMemoryValLong - freeMemoryValLong) / totalMemoryValLong) * 100);   
            	wifiInfo.put("RAMPercent", ramPercent);
            }else{
            	wifiInfo.put("RAMPercent", 100);
            }
            
            
            
        }else{
            logger.info("网关MAC地址："+gatewayMacAddress+"请求获取VoIP状态失败，原因：没有找到对应的VoIP信息");
            parameter.put(Constant.RESULT, -201);
            Map<String, Object> resultData = new HashMap<String, Object>();
            resultData.put(Constant.FAILREASON, "SSID信息不存在");
            parameter.put(Constant.RESULTDATA, resultData);
        }

            if(wifiInfo.get("CPUPercent")==null){
                wifiInfo.put("CPUPercent", "");
            }
            if(wifiInfo.get("RAMPercent")==null){
                wifiInfo.put("RAMPercent", "");
            }
            if(wifiInfo.get("VoIPName")==null){
                wifiInfo.put("VoIPName", "");
            }
            if(wifiInfo.get("VoIPRegStatus")==null){
                wifiInfo.put("VoIPRegStatus", "");
            }
            if(wifiInfo.get("IPv4WANStatus")==null){
                wifiInfo.put("IPv4WANStatus", "");
            }
            if(wifiInfo.get("InternetDNS1")==null){
            	wifiInfo.put("InternetDNS1", "");
            }
            if(wifiInfo.get("InternetDNS2")==null){
            	wifiInfo.put("InternetDNS2", "");
            }
            if(wifiInfo.get("SYSDuration")==null){
                wifiInfo.put("SYSDuration", "");
            }
            if(wifiInfo.get("SYSDuration")==null){
                wifiInfo.put("SYSDuration", "");
            }
            if(wifiInfo.get("PonRegStatus")==null){
                wifiInfo.put("PonRegStatus", "");
            }
            if(wifiInfo.get("PPPoEDuration")==null){
                wifiInfo.put("PPPoEDuration", "");
            }
            if(wifiInfo.get("SSID1")==null){
                wifiInfo.put("SSID1", "");
            }
            if(wifiInfo.get("IPv4ConnectionStatus")==null){
                wifiInfo.put("IPv4ConnectionStatus", "1");
            }
            if(wifiInfo.get("IPv4DialReason")==null){
                wifiInfo.put("IPv4DialReason", "");
            }
            if(wifiInfo.get("IPv6ConnectionStatus")==null){
                wifiInfo.put("IPv6ConnectionStatus", "");
            }
            if(wifiInfo.get("IPv6WANStatus")==null){
                wifiInfo.put("IPv6WANStatus", "");
            }
            if(wifiInfo.get("IPv6DialReason1")==null){
                wifiInfo.put("IPv6DialReason1", "");
            }
            if(wifiInfo.get("DevNum")==null){
                wifiInfo.put("DevNum", "");
            }
        try {
            Map<String,Object> reMap = getCommonReturnMap(parameter,0);
            reMap.put("ResultData", wifiInfo);
            return reMap;
        } catch (Exception e) {
            e.printStackTrace();
            parameter.put(Constant.RESULT, -1);
            Map<String, Object> resultData = new HashMap<String, Object>();
            resultData.put(Constant.FAILREASON, "组装SSIDInfo出错！");
            parameter.put(Constant.RESULTDATA, resultData);

            return parameter;
        }
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
        parameter.put(Constant.FAILREASON, failReson);
        return parameter;
    }

    /**
     * 获取返回MAP
     *
     * @param parameter
     * @param result
     * @return
     * @throws Exception
     */
    public Map<String, Object> getCommonReturnMap(Map<String, Object> parameter,int result) throws Exception {
        Map<String, Object> returnMapComm = new HashMap<>();
        if(parameter!=null){
            returnMapComm.put("Result", result);
            returnMapComm.put("ID", parameter.get("ID"));
            returnMapComm.put("CmdType", parameter.get("CmdType"));
            returnMapComm.put("SequenceId", parameter.get("SequenceId"));
        }
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
    public Map<String, Object> commonExAndMap(String Message){
        Map<String, Object> returnMapComm = new HashMap<>();
        returnMapComm.put(Constant.CODE, RespCodeEnum.RC_1.code());
        returnMapComm.put(Constant.MESSAGE, Message);
        returnMapComm.put(Constant.DATA, "");
        return returnMapComm;

    }

	@Override
	public Map<String, Object> syncGatewayBaseInfo(Map<String, Object> params) {
		
		 Map<String, Object> returnMapComm = new HashMap<>();
		 
		
		if(params == null || params.isEmpty()){
			returnMapComm.put(Constant.CODE, RespCodeEnum.RC_1002.code());
		    returnMapComm.put(Constant.MESSAGE, "请求参数为空");
			return returnMapComm;
		}
		if(params.containsKey("data")){
			
			List<Map<String, Object>> data = (List<Map<String, Object>>) params.get("data");
			if(data != null && data.size()>0){
				for(Map<String, Object> map : data){
					String password = map.get("password") == null ? "" : map.get("password").toString();
					String areaId = map.get("areaId") == null ? "" : map.get("areaId").toString();
					if(!"".equals(password) && !"".equals(areaId)){
						//调用AMS查询areaId是否存在
						Area area = amsAreaService.findAreaById(Integer.parseInt(areaId));
						if(area != null){
							GatewayInfo gatewayInfo = new GatewayInfo();
							gatewayInfo.setGatewayPassword(password);
							gatewayInfo.setGatewayAreaId(areaId);
							gatewayInfoService.updateGatewayAreaIdByPassword(gatewayInfo);
						}
					}
				}
			}
			returnMapComm.put(Constant.CODE, RespCodeEnum.RC_0.code());
			returnMapComm.put(Constant.MESSAGE, "成功");
		}else{
			returnMapComm.put(Constant.CODE, RespCodeEnum.RC_1002.code());
			returnMapComm.put(Constant.MESSAGE, "请求参数错误");
		}
		
		return returnMapComm;
	}
}
