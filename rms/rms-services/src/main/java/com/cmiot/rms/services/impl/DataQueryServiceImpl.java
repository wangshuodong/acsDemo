package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSONArray;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.enums.PbossDeviceType;
import com.cmiot.rms.dao.mapper.BoxFirmwareInfoMapper;
import com.cmiot.rms.dao.mapper.BoxFirmwareUpgradeTaskDetailMapper;
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.model.BoxFirmwareInfo;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.DataQueryService;
import com.cmiot.rms.services.FirmwareUpgradeTaskDetailService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.InstructionUtil;
import com.tydic.inter.app.service.GatewayHandleService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 省级数字家庭管理平台与网管系统接口规范
 * 数据查询接口
 * Hgu:家庭网关
 * Stb：机顶盒
 * Created by panmingguo on 2016/9/6.
 */
public class DataQueryServiceImpl implements DataQueryService {

    private static Logger logger = LoggerFactory.getLogger(DataQueryServiceImpl.class);

    @Autowired
    GatewayInfoService gatewayInfoService;

    @Autowired
    private GatewayHandleService gatewayHandleService;

    @Autowired
    FirmwareUpgradeTaskDetailService firmwareUpgradeTaskDetailService;

    @Autowired
    BoxInfoMapper boxInfoMapper;

    @Autowired
    BoxFirmwareInfoMapper boxFirmwareInfoMapper;

    @Autowired
    private RedisClientTemplate redisClientTemplate;

    @Autowired
    BoxFirmwareUpgradeTaskDetailMapper boxFirmwareUpgradeTaskDetailMapper;

    @Autowired
    InstructionMethodService instructionMethodService;

    @Autowired
    GatewayInfoMapper gatewayInfoMapper;

    private final static Map<String, Integer> ALRAMLEVEL = new HashMap<>();

    private final static Map<String, String> LANSTATUS = new HashMap<>();

    static
    {
        ALRAMLEVEL.put("严重告警", 1);
        ALRAMLEVEL.put("主要告警", 2);
        ALRAMLEVEL.put("主要/次要告警", 3);
        ALRAMLEVEL.put("次要告警", 4);

        LANSTATUS.put("Up", "up");
        LANSTATUS.put("NoLink", "Nolink");
        LANSTATUS.put("Error", "error");
        LANSTATUS.put("Disabled", "disabled");
    }

    /**
     * 查询设备信息(Ihgu)
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryIhguEquipmentInfo(Map<String, Object> parameter) {
        logger.info("Start invoke queryIhguEquipmentInfo:{}", parameter);
        Map<String, Object> retMap = getEquipmentInfoResult(parameter, PbossDeviceType.IHGU);
        queryEquipmentInfo(parameter, retMap);
        logger.info("End invoke queryIhguEquipmentInfo:{}", retMap);
        return retMap;
    }

    /**
     * 查询设备信息(Hgu)
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryHguEquipmentInfo(Map<String, Object> parameter) {
        logger.info("Start invoke queryHguEquipmentInfo:{}", parameter);
        Map<String, Object> retMap = getEquipmentInfoResult(parameter, PbossDeviceType.HGU);
        queryEquipmentInfo(parameter, retMap);
        logger.info("End invoke queryHguEquipmentInfo:{}", retMap);
        return retMap;
    }


    /**
     * 查询智能网关和网关基本信息
     * @param parameter
     * @param retMap
     */
    private void queryEquipmentInfo(Map<String, Object> parameter, Map<String, Object> retMap)
    {
        if(null == parameter.get("CPEID"))
        {
            return;
        }

        //解析OUI-SN
        String cpeId =  parameter.get("CPEID").toString();
        String cpeIds[] = cpeId.split("-");
        if(cpeIds.length != 2)
        {
            return;
        }

        //查询网关信息
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayFactoryCode(cpeIds[0]);
        searchInfo.setGatewaySerialnumber(cpeIds[1]);
        GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        if(null == gatewayInfo)
        {
            return;
        }

        //未注册返回不存在
        if(StringUtils.isBlank(gatewayInfo.getGatewayStatus())
                || "未注册".equals(gatewayInfo.getGatewayStatus()))
        {
            return;
        }

        //返回数据封装
        retMap.put("Result", 0);
        retMap.put("ProductClass", gatewayInfo.getGatewayModel());
        retMap.put("DeviceID", gatewayInfo.getGatewaySerialnumber());
        retMap.put("MAC", gatewayInfo.getGatewayMacaddress());
        retMap.put("PublicIP", gatewayInfo.getGatewayIpaddress());
        retMap.put("OUI", gatewayInfo.getGatewayFactoryCode());
        retMap.put("SoftVersion", gatewayInfo.getGatewayVersion());
        retMap.put("HardVersion", gatewayInfo.getGatewayHardwareVersion());
        if("已绑定".equals(gatewayInfo.getGatewayStatus()))
        {
            retMap.put("RegistStatus", 1);
        }
        else
        {
            retMap.put("RegistStatus", 0);
        }


        //查询是否正在升级
        int count  = firmwareUpgradeTaskDetailService.searchProcessingCount(gatewayInfo.getGatewayUuid());
        if(count > 0)
        {
            //网关状态为升级中
            retMap.put("onLineStatus", 2);
        }
        else
        {
            //调用BMS接口查询网关在线状态
            Map<String, Object> para = new HashMap<>();
            JSONArray array = new JSONArray();
            array.add(gatewayInfo.getGatewayMacaddress());
            para.put("macList", array);
            Map<String, Object> statusMap = gatewayHandleService.checkGatewayOnline(para);
            logger.info("checkGatewayOnline:{}", statusMap);
            if(null != statusMap
                    && statusMap.size() > 0
                    && null != statusMap.get(Constant.CODE)
                    && (Integer.valueOf(statusMap.get(Constant.CODE).toString()) == 0))
            {
                List<Map<String, Object>> retMacList = (List<Map<String, Object>>)statusMap.get("macList");

                if(null != retMacList && retMacList.size() > 0
                        && Integer.valueOf(retMacList.get(0).get("onLine").toString()) == 1)
                {
                    retMap.put("onLineStatus", 0);
                }
                else
                {
                    retMap.put("onLineStatus", 1);
                }

            }
            else
            {
                retMap.put("onLineStatus", 1);
            }

        }
    }

    /**
     * 查询设备信息(Stb)
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryStbEquipmentInfo(Map<String, Object> parameter) {
        logger.info("Start invoke queryStbEquipmentInfo:{}", parameter);
        Map<String, Object> retMap = getEquipmentInfoResult(parameter, PbossDeviceType.STB);
        if(null == parameter.get("CPEID"))
        {
            return retMap;
        }

        //解析OUI-SN
        String cpeId =  parameter.get("CPEID").toString();
        String cpeIds[] = cpeId.split("-");
        if(cpeIds.length != 2)
        {
            return retMap;
        }

        BoxInfo searchInfo = new BoxInfo();
        searchInfo.setBoxFactoryCode(cpeIds[0]);
        searchInfo.setBoxSerialnumber(cpeIds[1]);

        BoxInfo boxInfo = boxInfoMapper.selectGatewayInfo(searchInfo);
        if(null == boxInfo)
        {
            return retMap;
        }

        //返回数据封装
        retMap.put("Result", 0);
        retMap.put("ProductClass", boxInfo.getBoxModel());
        retMap.put("DeviceID", boxInfo.getBoxSerialnumber());
        retMap.put("MAC", boxInfo.getBoxMacaddress());
        retMap.put("PublicIP", boxInfo.getBoxIpaddress());
        retMap.put("OUI", boxInfo.getBoxFactoryCode());
        retMap.put("HardVersion", boxInfo.getBoxHardwareVersion());

        //软件版本
        BoxFirmwareInfo firmwareInfo = boxFirmwareInfoMapper.selectByPrimaryKey(boxInfo.getBoxFirmwareUuid());
        if(null != firmwareInfo)
        {
            retMap.put("SoftVersion", firmwareInfo.getFirmwareVersion());
        }

        //注册状态
        if("2".equals(boxInfo.getBoxStatus()))
        {
            retMap.put("RegistStatus", 1);
        }
        else
        {
            retMap.put("RegistStatus", 0);
        }

        //查询是否正在升级
        int count  = boxFirmwareUpgradeTaskDetailMapper.searchProcessingCount(boxInfo.getBoxUuid());
        if(count > 0)
        {
            //网关状态为升级中
            retMap.put("onLineStatus", 2);
        }
        else
        {
            if(0 == boxInfo.getBoxOnline())
            {
                retMap.put("onLineStatus", 1);
            }
            else
            {
                //数据库中是在线，需要核对redis中状态是否为在线
                if(1 == boxInfo.getBoxOnline()
                        && ("1".equals(redisClientTemplate.get(Constant.BOX_ONLINE + boxInfo.getBoxSerialnumber()))))
                {
                    retMap.put("onLineStatus", 0);
                }
                else
                {
                    retMap.put("onLineStatus", 1);
                }
            }
        }

        logger.info("End invoke queryStbEquipmentInfo:{}", retMap);
        return retMap;
    }

    /**
     * 查询设备业务状态(Hgu)
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryHguBusinessStatus(Map<String, Object> parameter) {
        logger.info("Start invoke queryHguBusinessStatus:{}", parameter);
        Map<String, Object> retMap = getBusinessStatusResult(parameter);
        if(null == parameter.get("CPEID"))
        {
            return retMap;
        }

        //解析OUI-SN
        String cpeId =  parameter.get("CPEID").toString();
        String cpeIds[] = cpeId.split("-");
        if(cpeIds.length != 2)
        {
            return retMap;
        }

        //查询网关信息
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayFactoryCode(cpeIds[0]);
        searchInfo.setGatewaySerialnumber(cpeIds[1]);
        GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        if(null == gatewayInfo)
        {
            return retMap;
        }

        Boolean isSuccess;
        //获取连接方式和宽带连接的IP地址
        isSuccess = queryConnectionTypeAndIP(gatewayInfo, retMap);
        if(!isSuccess)
        {
            logger.info("End invoke queryHguBusinessStatus:{}", retMap);
            return retMap;
        }

        //获取LAN口状态
        isSuccess = queryLANStatus(gatewayInfo, retMap);
        if(!isSuccess)
        {
            logger.info("End invoke queryHguBusinessStatus:{}", retMap);
            return retMap;
        }

        //获取SSID的名称和状态和wifi开关
        isSuccess = querySSIDInfo(gatewayInfo, retMap);
        if(!isSuccess)
        {
            logger.info("End invoke queryHguBusinessStatus:{}", retMap);
            return retMap;
        }


        //查询语音口注册用户
        isSuccess = queryVoipAuthName(gatewayInfo, retMap);
        if(!isSuccess)
        {
            logger.info("End invoke queryHguBusinessStatus:{}", retMap);
            return retMap;
        }

        retMap.put("Result", 0);

        logger.info("End invoke queryHguBusinessStatus:{}", retMap);
        return retMap;
    }

    /**
     * 查询DNS地址和拨号错误码(Hgu)
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryHguDnsdhcpInfo(Map<String, Object> parameter) {
        logger.info("Start invoke queryHguDnsdhcpInfo:{}", parameter);
        Map<String, Object> retMap = getHguDnsdhcpInfoResult(parameter);
        if(null == parameter.get("CPEID") || null == parameter.get("VlanID") || null == parameter.get("ServiceList"))
        {
            return retMap;
        }

        //解析OUI-SN
        String cpeId =  parameter.get("CPEID").toString();
        String cpeIds[] = cpeId.split("-");
        if(cpeIds.length != 2)
        {
            return retMap;
        }

        //查询网关信息
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayFactoryCode(cpeIds[0]);
        searchInfo.setGatewaySerialnumber(cpeIds[1]);
        GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        if(null == gatewayInfo)
        {
            return retMap;
        }

        String vlanId = parameter.get("VlanID").toString();
        String serviceList = parameter.get("ServiceList").toString();

        //获取wan连接节点路径
        List<String> connectionList = new ArrayList<>();

        if(!getWanConnectionList(gatewayInfo.getGatewayMacaddress(), connectionList, retMap))
        {
            logger.info("End invoke queryHguDnsdhcpInfo:{}", retMap);
            return retMap;
        }


        List<String> valueList = new ArrayList<>();
        for(String pppConnection: connectionList)
        {
            valueList.add(pppConnection + "X_CMCC_ServiceList");
            valueList.add(pppConnection + "X_CMCC_VLANIDMark");
            valueList.add(pppConnection + "X_CMCC_LanInterface-DHCPEnable");
            valueList.add(pppConnection + "LastConnectionError");
            valueList.add(pppConnection + "DNSServers");
        }
        logger.info("queryHguDnsdhcpInfo valueList:{}", valueList);

        Map<String, Object> valueMap = instructionMethodService.getParameterValuesErrorCode(gatewayInfo.getGatewayMacaddress(), valueList);
        logger.info("queryHguDnsdhcpInfo getParameterValues valueMap:{}", valueMap);
        if(Integer.valueOf(valueMap.get("result").toString()) == -1)
        {
            if(null != valueMap.get("errorCode"))
            {
                retMap.put("Result", Integer.valueOf(valueMap.get("errorCode").toString()));
            }
            logger.info("End invoke queryHguDnsdhcpInfo:{}", retMap);
            return retMap;
        }

        Boolean dhcpEnable =false;
        for(Map.Entry<String, Object> entry : valueMap.entrySet())
        {
            if(entry.getKey().endsWith("X_CMCC_VLANIDMark") && vlanId.equals(entry.getValue().toString())) {
                String preKey = entry.getKey().substring(0, entry.getKey().indexOf("X_CMCC_VLANIDMark"));
                String sList = null != valueMap.get(preKey + "X_CMCC_ServiceList") ? valueMap.get(preKey + "X_CMCC_ServiceList").toString() : "";
                if (sList.contains(serviceList)) {
                    retMap.put("DNSServers", null != valueMap.get(preKey + "DNSServers") ? valueMap.get(preKey + "DNSServers").toString() : "");
                    retMap.put("LastConnectionError", valueMap.get(preKey + "LastConnectionError") != null ? valueMap.get(preKey + "LastConnectionError").toString() : "");
                    dhcpEnable = null != valueMap.get(preKey + "X_CMCC_LanInterface-DHCPEnable") ? Boolean.valueOf(valueMap.get(preKey + "X_CMCC_LanInterface-DHCPEnable").toString()) : false;
                    break;
                }
            }
        }

        //InternetGatewayDevice.LANDevice.{i}.
        List<String> preLANDeviceList = instructionMethodService.getLANDevicePrefix(gatewayInfo.getGatewayMacaddress());
        logger.info("queryHguDnsdhcpInfo getParameterNames lanDeviceMap:{}", preLANDeviceList);

        List<String> lanList = new ArrayList<>();
        for(String lan : preLANDeviceList)
        {
            lanList.add(lan + "LANHostConfigManagement.DHCPServerEnable");
            lanList.add(lan + "LANHostConfigManagement.MinAddress");
            lanList.add(lan + "LANHostConfigManagement.MaxAddress");
        }
        logger.info("queryHguDnsdhcpInfo lanList:{}", lanList);
        Map<String, Object> lanValueMap = instructionMethodService.getParameterValuesErrorCode(gatewayInfo.getGatewayMacaddress(), lanList);
        logger.info("queryHguDnsdhcpInfo getParameterValues lanValueMap:{}", lanValueMap);
        if(Integer.valueOf(lanValueMap.get("result").toString()) == -1)
        {
            if(null != lanValueMap.get("errorCode"))
            {
                retMap.put("Result", Integer.valueOf(lanValueMap.get("errorCode").toString()));
            }
            logger.info("End invoke  queryHguDnsdhcpInfo:{}", retMap);
            return retMap;
        }

        for(Map.Entry<String, Object> entry : lanValueMap.entrySet())
        {
            if(entry.getKey().endsWith("DHCPServerEnable")) {
                String preKey = entry.getKey().substring(0, entry.getKey().indexOf("DHCPServerEnable"));
                if(Boolean.valueOf(entry.getValue().toString()) && dhcpEnable)
                {
                    retMap.put("DHCPServerEnable", 1);
                    retMap.put("MinAddress", lanValueMap.get(preKey + "MinAddress"));
                    retMap.put("MaxAddress", lanValueMap.get(preKey + "MaxAddress"));
                }
                //DHCPServerEnable取值为FALSE时，X_CMCC_LanInterface-DHCPEnable配置无效。
                else if(!Boolean.valueOf(entry.getValue().toString()) || !dhcpEnable)
                {
                    //禁用时MinAddress和MaxAddress为空
                    retMap.put("DHCPServerEnable", 0);
                }
            }
        }
        retMap.put("Result", 0);
        logger.info("End invoke queryHguDnsdhcpInfo:{}", retMap);
        return retMap;
    }

    /**
     * 告警查询(Hgu)
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryHguAlarminfo(Map<String, Object> parameter) {
        logger.info("Start invoke queryHguAlarminfo:{}", parameter);
        List<Map<String ,Object>> alarms = new ArrayList<>();
        Map<String, Object> retMap = getAlarminfoResult(parameter, alarms);
        if(null == parameter.get("CPEID"))
        {
            return retMap;
        }

        //解析OUI-SN
        String cpeId =  parameter.get("CPEID").toString();
        String cpeIds[] = cpeId.split("-");
        if(cpeIds.length != 2)
        {
            return retMap;
        }

        //查询网关信息
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayFactoryCode(cpeIds[0]);
        searchInfo.setGatewaySerialnumber(cpeIds[1]);
        GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        if(null == gatewayInfo)
        {
            return retMap;
        }

        retMap.put("Result", 0);
        List<String> nameList = new ArrayList<>();
        nameList.add(ConstantDiagnose.ALARM_NUMBER);
        nameList.add(ConstantDiagnose.GATEWAY_ALIAS);
        Map<String, Object> valueMap = instructionMethodService.getParameterValuesErrorCode(gatewayInfo.getGatewayMacaddress(), nameList);
        logger.info("queryHguAlarminfo getParameterValues valueMap:{}", valueMap);
        if(valueMap == null || valueMap.isEmpty()){
            return retMap;
        }

        if(Integer.valueOf(valueMap.get("result").toString()) == -1)
        {
            if(null != valueMap.get("errorCode"))
            {
                retMap.put("Result", Integer.valueOf(valueMap.get("errorCode").toString()));
            }
            logger.info("End invoke queryHguAlarminfo:{}", retMap);
            return retMap;
        }

        Object alarmCodes = valueMap.get(ConstantDiagnose.ALARM_NUMBER);
        logger.info("AlarmNumber:{}", alarmCodes);
        if(alarmCodes != null && !"".equals(alarmCodes)){

            String[] codes = alarmCodes.toString().split(",");
            List<Map<String, Object>> alarmInfoList = gatewayInfoMapper.queryGatewayAlarmInfo(codes);
            logger.info("queryGatewayAlarmInfo:{}", alarmInfoList);
            if(null != alarmInfoList && alarmInfoList.size() > 0)
            {
                Map<String, Object> alarmMap;
                for(Map<String, Object> alarm : alarmInfoList)
                {
                    alarmMap = getAlarminfo();
                    alarmMap.put("alarmTitle", alarm.get("alarm_name"));
                    alarmMap.put("alarmType", alarm.get("alarm_type"));
                    alarmMap.put("alarmId", alarm.get("alarm_code"));
                    alarmMap.put("specificProblemID", alarm.get("alarm_code"));
                    alarmMap.put("specificProblem", alarm.get("alarm_trigger"));
                    alarmMap.put("origSeverity", ALRAMLEVEL.get(alarm.get("alarm_level")));
                    alarmMap.put("neUID", cpeId);

                    //终端设备名称，如无名称，则填写OUI-SN
                    if(null != valueMap.get(ConstantDiagnose.GATEWAY_ALIAS))
                    {
                        alarmMap.put("neName", valueMap.get(ConstantDiagnose.GATEWAY_ALIAS));
                    }
                    else
                    {
                        alarmMap.put("neName", cpeId);
                    }
                    alarms.add(alarmMap);
                }
                logger.info("alarms:{}", alarms);
            }
        }
        logger.info("End invoke queryHguAlarminfo:{}", retMap);
        return retMap;
    }

    /**
     * 查询LAN口性能(Hgu)
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryHguLanPerformanceInfo(Map<String, Object> parameter) {
        logger.info("Start invoke queryHguLanPerformanceInfo:{}", parameter);
        Map<String, Object> retMap = getHguLanPerformanceInfoResult(parameter);
        if(null == parameter.get("CPEID") || null == parameter.get("LANID"))
        {
            return retMap;
        }

        //解析OUI-SN
        String cpeId =  parameter.get("CPEID").toString();
        String cpeIds[] = cpeId.split("-");
        if(cpeIds.length != 2)
        {
            return retMap;
        }

        //查询网关信息
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayFactoryCode(cpeIds[0]);
        searchInfo.setGatewaySerialnumber(cpeIds[1]);
        GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        if(null == gatewayInfo)
        {
            return retMap;
        }

        int lanId = Integer.valueOf(parameter.get("LANID").toString());

        //InternetGatewayDevice.LANDevice.{i}.
        List<String> preLANDeviceList = instructionMethodService.getLANDevicePrefix(gatewayInfo.getGatewayMacaddress());
        logger.info("getLANEthernetInterfaceConfigList getParameterNames lanDeviceMap:{}", preLANDeviceList);
        for(String preLan : preLANDeviceList)
        {
            String lanPath = preLan + "LANEthernetInterfaceConfig." + lanId + ".";

            List<String> nameList = new ArrayList<>();
            nameList.add(lanPath + "Enable");
            nameList.add(lanPath + "Status");
            nameList.add(lanPath + "MACAddress");
            nameList.add(lanPath + "DuplexMode");

            Map<String, Object> statsMap = instructionMethodService.getParameterNamesErrorCode(gatewayInfo.getGatewayMacaddress(),
                    lanPath + "Stats.", true);
            logger.info("queryHguLanPerformanceInfo getParameterNames statsMap:{}", statsMap);
            if(Integer.valueOf(statsMap.get("result").toString()) == -1)
            {
                if(null != statsMap.get("errorCode"))
                {
                    retMap.put("Result", Integer.valueOf(statsMap.get("errorCode").toString()));
                }
                logger.info("End invoke queryHguLanPerformanceInfo:{}", retMap);
                continue;
            }
            for (Map.Entry<String, Object> entry : statsMap.entrySet()) {
                if(entry.getKey().endsWith("BytesSend") || (entry.getKey().endsWith("BytesReceived")) || entry.getKey().endsWith("BytesSent") )
                {
                    nameList.add(entry.getKey());
                }
            }

            Map<String, Object> valueMap = instructionMethodService.getParameterValuesErrorCode(gatewayInfo.getGatewayMacaddress(), nameList);
            logger.info("queryHguLanPerformanceInfo getParameterValues valueMap:{}", valueMap);
            if(Integer.valueOf(valueMap.get("result").toString()) == -1)
            {
                if(null != valueMap.get("errorCode"))
                {
                    retMap.put("Result", Integer.valueOf(valueMap.get("errorCode").toString()));
                }
                logger.info("End invoke  queryHguLanPerformanceInfo:{}", retMap);
                continue;
            }

            if(null != valueMap && valueMap.size() > 0)
            {
                retMap.put("Enable", valueMap.get(lanPath + "Enable"));
                retMap.put("Status", LANSTATUS.get(String.valueOf(valueMap.get(lanPath + "Status"))));
                retMap.put("MACAddress", valueMap.get(lanPath + "MACAddress"));
                retMap.put("DuplexMode", valueMap.get(lanPath + "DuplexMode"));
                if(null != valueMap.get(lanPath + "Stats.BytesSend"))
                {
                    retMap.put("BytesSent", String.valueOf(valueMap.get(lanPath + "Stats.BytesSend")));
                }
                else if(null != valueMap.get(lanPath + "Stats.BytesSent"))
                {
                    retMap.put("BytesSent", String.valueOf(valueMap.get(lanPath + "Stats.BytesSent")));
                }

                if(null != valueMap.get(lanPath + "Stats.BytesReceived"))
                {
                    retMap.put("BytesReceived", String.valueOf(valueMap.get(lanPath + "Stats.BytesReceived")));
                }

            }
            retMap.put("Result", 0);
            break;

        }
        logger.info("End invoke queryHguLanPerformanceInfo:{}", retMap);
        return retMap;
    }


    /**
     * 构建查询设备信息返回值
     * @param parameter
     * @param type
     * @return
     */
    private Map<String, Object> getEquipmentInfoResult(Map<String, Object> parameter, PbossDeviceType type)
    {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("Result", 1);
        retMap.put("CPEID", parameter.get("CPEID"));
        retMap.put("DeviceType", type.getType());
        retMap.put("ProductClass", "");
        retMap.put("DeviceID", "");
        retMap.put("MAC", "");
        retMap.put("PublicIP", "");
        retMap.put("OUI", "");
        retMap.put("SoftVersion", "");
        retMap.put("HardVersion", "");
        retMap.put("RegistStatus", "");
        retMap.put("onLineStatus", "");
        return retMap;
    }


    /**
     * 构建查询设备业务状态返回值
     * @param parameter
     * @return
     */
    private Map<String, Object> getBusinessStatusResult(Map<String, Object> parameter)
    {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("Result", 1);
        retMap.put("CPEID", parameter.get("CPEID"));
        retMap.put("VoipAuthName", "");
        retMap.put("VoipAuthName2", "");
        retMap.put("ConnectionType", "");
        retMap.put("InternetAddr", "");
        retMap.put("LANStatus", "");
        retMap.put("WifiSwitch", "");
        retMap.put("SsidName", "");
        retMap.put("SsidStatus", "");
        return retMap;
    }


    /**
     * 构建查询DNS地址和拨号错误码返回值
     * @param parameter
     * @return
     */
    private Map<String, Object> getHguDnsdhcpInfoResult(Map<String, Object> parameter)
    {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("Result", 1);
        retMap.put("CPEID", parameter.get("CPEID"));
        retMap.put("DNSServers", "");
        retMap.put("LastConnectionError", "");
        retMap.put("DHCPServerEnable", "");
        retMap.put("MinAddress", "");
        retMap.put("MaxAddress", "");
        retMap.put("ReservedAddresses", "");
        return retMap;
    }


    /**
     * 构建告警查询返回值
     * @param parameter
     * @return
     */
    private Map<String, Object> getAlarminfoResult(Map<String, Object> parameter, List<Map<String ,Object>> alarms)
    {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("Result", 1);
        retMap.put("CPEID", parameter.get("CPEID"));
        retMap.put("alarms", alarms);
        return retMap;
    }

    /**
     * 构建单个告警信息
     * @return
     */
    private Map<String, Object> getAlarminfo()
    {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("alarmTitle", "");
        retMap.put("alarmStatus", "");
        retMap.put("alarmType", "");
        retMap.put("origSeverity", "");
        retMap.put("eventTime", "");
        retMap.put("alarmId", "");
        retMap.put("specificProblemID", "");
        retMap.put("specificProblem", "");
        retMap.put("neUID", "");
        retMap.put("neName", "");
        retMap.put("neType", "ihgu");
        retMap.put("objectUID", "");
        retMap.put("objectName", "");
        retMap.put("objectType", "");
        retMap.put("locationInfo", "");
        return retMap;
    }

    /**
     * 构建LAN口性能返回值
     * @param  parameter
     * @return
     */
    private Map<String, Object> getHguLanPerformanceInfoResult(Map<String, Object> parameter)
    {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("Result", 1);
        retMap.put("CPEID", parameter.get("CPEID"));
        retMap.put("Enable", "");
        retMap.put("Status", "");
        retMap.put("MACAddress", "");
        retMap.put("DuplexMode", "");
        retMap.put("BytesSent", "");
        retMap.put("BytesReceived", "");
        return retMap;
    }



    /**
     * 查询语音口注册用户
     * @param gatewayInfo
     * @param retMap
     */
    private Boolean queryVoipAuthName(GatewayInfo gatewayInfo, Map<String, Object> retMap)
    {
        Map<String, Object> nameMap = instructionMethodService.getParameterNamesErrorCode(gatewayInfo.getGatewayMacaddress(),
                "InternetGatewayDevice.Services.VoiceService.", true);
        logger.info("queryVoipAuthName getParameterNames nameMap:{}", nameMap);

        if(Integer.valueOf(nameMap.get("result").toString()) == -1)
        {
            if(null != nameMap.get("errorCode"))
            {
                retMap.put("Result", Integer.valueOf(nameMap.get("errorCode").toString()));
            }

            return false;
        }

        String regVoiceService = "InternetGatewayDevice.Services.VoiceService.[0-9]+";

        List<String> voiceServiceList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
            InstructionUtil.getName(voiceServiceList, regVoiceService, entry.getKey());
        }
        logger.info("queryVoipAuthName voiceServiceList:{}", voiceServiceList);

        List<String> voiceProfileList = new ArrayList<>();
        for(String voiceService : voiceServiceList)
        {
            Map<String, Object> voiceProfileMap = instructionMethodService.getParameterNamesErrorCode(gatewayInfo.getGatewayMacaddress(),
                    voiceService + "VoiceProfile.", true);
            logger.info("queryVoipAuthName getParameterNames voiceProfileMap:{}", nameMap);

            if(Integer.valueOf(voiceProfileMap.get("result").toString()) == -1)
            {
                if(null != voiceProfileMap.get("errorCode"))
                {
                    retMap.put("Result", Integer.valueOf(voiceProfileMap.get("errorCode").toString()));
                }

                return false;
            }

            String regVoiceProfile = voiceService + "VoiceProfile.[0-9]+";
            for (Map.Entry<String, Object> entry : voiceProfileMap.entrySet()) {
                InstructionUtil.getName(voiceProfileList, regVoiceProfile, entry.getKey());
            }
        }
        logger.info("queryVoipAuthName voiceProfileList:{}", voiceProfileList);


        List<String> lineList = new ArrayList<>();
        for(String voiceService : voiceProfileList)
        {
            Map<String, Object> lineMap = instructionMethodService.getParameterNamesErrorCode(gatewayInfo.getGatewayMacaddress(),
                    voiceService + "Line.", true);
            logger.info("queryVoipAuthName getParameterNames lineMap:{}", lineMap);
            if(Integer.valueOf(lineMap.get("result").toString()) == -1)
            {
                if(null != lineMap.get("errorCode"))
                {
                    retMap.put("Result", Integer.valueOf(lineMap.get("errorCode").toString()));
                }

                return false;
            }
            String reglineList = voiceService + "Line.[0-9]+";
            for (Map.Entry<String, Object> entry : lineMap.entrySet()) {
                InstructionUtil.getName(lineList, reglineList, entry.getKey());
            }
        }
        logger.info("queryVoipAuthName lineList:{}", lineList);

        List<String> valueList = new ArrayList<>();
        for(String line : lineList)
        {
            valueList.add(line + "SIP.AuthUserName");
        }
        logger.info("queryVoipAuthName valueList:{}", valueList);

        Map<String, Object> authNameMap = instructionMethodService.getParameterValuesErrorCode(gatewayInfo.getGatewayMacaddress(), valueList);
        logger.info("queryVoipAuthName getParameterValues authNameMap:{}", authNameMap);
        if(Integer.valueOf(authNameMap.get("result").toString()) == -1)
        {
            if(null != authNameMap.get("errorCode"))
            {
                retMap.put("Result", Integer.valueOf(authNameMap.get("errorCode").toString()));
            }

            return false;
        }

        String nameIndex = "";
        for(Map.Entry<String, Object> entry : authNameMap.entrySet())
        {
            retMap.put("VoipAuthName" + nameIndex, entry.getValue());
            if("2".equals(nameIndex))
            {
                break;
            }
            nameIndex = "2";
        }

        return true;
    }


    /**
     * 获取连接方式和宽带连接的IP地址
     * @param gatewayInfo
     * @param retMap
     */
    private Boolean queryConnectionTypeAndIP(GatewayInfo gatewayInfo, Map<String, Object> retMap)
    {
        //获取wan连接节点路径
        List<String> connectionList= new ArrayList<>();
        Boolean isSuccess = getWanConnectionList(gatewayInfo.getGatewayMacaddress(), connectionList, retMap);
        if(!isSuccess)
        {
            return false;
        }

        List<String> valueList = new ArrayList<>();
        for(String pppConnection: connectionList)
        {
            valueList.add(pppConnection + "Enable");
            valueList.add(pppConnection + "ConnectionType");
            valueList.add(pppConnection + "ExternalIPAddress");
        }

        logger.info("queryConnectionTypeAndIP valueList:{}", valueList);
        Map<String, Object> valueMap = instructionMethodService.getParameterValuesErrorCode(gatewayInfo.getGatewayMacaddress(), valueList);
        logger.info("queryConnectionTypeAndIP getParameterValues valueMap:{}", valueMap);
        if(Integer.valueOf(valueMap.get("result").toString()) == -1)
        {
            if(null != valueMap.get("errorCode"))
            {
                retMap.put("Result", Integer.valueOf(valueMap.get("errorCode").toString()));
            }

            return false;
        }

        for(Map.Entry<String, Object> entry : valueMap.entrySet())
        {
            if(entry.getKey().endsWith("Enable") && Boolean.valueOf(entry.getValue().toString()))
            {
                String preKey = entry.getKey().substring(0, entry.getKey().indexOf("Enable"));
                retMap.put("ConnectionType", valueMap.get(preKey + "ConnectionType"));
                retMap.put("InternetAddr", valueMap.get(preKey + "ExternalIPAddress"));
                break;
            }
        }

        return true;
    }

    /**
     * 获取wan连接节点路径
     * @param gatewayMacAddress
     * @return
     */
    private  boolean getWanConnectionList(String gatewayMacAddress, List<String> resultList, Map<String, Object> retMap)
    {
        //InternetGatewayDevice.WANDevice.{i}.
        List<String> preWANDeviceList = instructionMethodService.getWANDevicePrefix(gatewayMacAddress);
        logger.info("getWanConnectionList getParameterNames wanDeviceMap:{}", preWANDeviceList);

        //InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.
        List<String> preWanConnectionList = new ArrayList<>();
        for(String wan : preWANDeviceList)
        {
            String wanConnectionDevice = wan + "WANConnectionDevice.";
            Map<String, Object> wanConnectionMap = instructionMethodService.getParameterNamesErrorCode(gatewayMacAddress,
                    wanConnectionDevice, true);
            if(Integer.valueOf(wanConnectionMap.get("result").toString()) == -1)
            {
                if(null != wanConnectionMap.get("errorCode"))
                {
                    retMap.put("Result", Integer.valueOf(wanConnectionMap.get("errorCode").toString()));
                }

                return false;
            }

            String regWanConnection =  wanConnectionDevice + "[0-9]+";
            for(Map.Entry<String, Object> entry : wanConnectionMap.entrySet())
            {
                InstructionUtil.getName(preWanConnectionList, regWanConnection, entry.getKey());
            }
        }

        //InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANPPPConnection.{i}.
        List<String> preWanPPPConnectionList = new ArrayList<>();
        for(String Connection : preWanConnectionList)
        {
            String wanPPPConnection = Connection + "WANPPPConnection.";
            Map<String, Object> wanPPPConnectionMap = instructionMethodService.getParameterNamesErrorCode(gatewayMacAddress,
                    wanPPPConnection, true);
            if(Integer.valueOf(wanPPPConnectionMap.get("result").toString()) == -1)
            {
                if(null != wanPPPConnectionMap.get("errorCode"))
                {
                    retMap.put("Result", Integer.valueOf(wanPPPConnectionMap.get("errorCode").toString()));
                }

                return false;
            }
            String regWanPPPConnection =  wanPPPConnection + "[0-9]+";
            for(Map.Entry<String, Object> entry : wanPPPConnectionMap.entrySet())
            {
                InstructionUtil.getName(preWanPPPConnectionList, regWanPPPConnection, entry.getKey());
            }
        }

        //InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANIPConnection.{i}.
        List<String> preWanIPConnectionList = new ArrayList<>();
        for(String Connection : preWanConnectionList) {
            String wanIPConnection = Connection + "WANIPConnection.";
            Map<String, Object> wanIPConnectionMap = instructionMethodService.getParameterNamesErrorCode(gatewayMacAddress,
                    wanIPConnection, true);
            if(Integer.valueOf(wanIPConnectionMap.get("result").toString()) == -1)
            {
                if(null != wanIPConnectionMap.get("errorCode"))
                {
                    retMap.put("Result", Integer.valueOf(wanIPConnectionMap.get("errorCode").toString()));
                }

                return false;
            }
            String regWanPPPConnection = wanIPConnection + "[0-9]+";
            for (Map.Entry<String, Object> entry : wanIPConnectionMap.entrySet()) {
                InstructionUtil.getName(preWanIPConnectionList, regWanPPPConnection, entry.getKey());
            }
        }

        resultList.addAll(preWanPPPConnectionList);
        resultList.addAll(preWanIPConnectionList);

        logger.info("getWanConnectionList connectionList:{}", resultList);

        return true;
    }

    /**
     * 获取LAN口状态
     * @param gatewayInfo
     * @param retMap
     */
    private Boolean queryLANStatus(GatewayInfo gatewayInfo, Map<String, Object> retMap) {
        //获取LAN口节点路径前缀
        List<String> preInterfaceConfigList = new ArrayList<>();

        Boolean isSuccess = getLANEthernetInterfaceConfigList(gatewayInfo.getGatewayMacaddress(), preInterfaceConfigList, retMap);
        if(!isSuccess)
        {
            return false;
        }

        List<String> valueList = new ArrayList<>();
        for(String cfg : preInterfaceConfigList)
        {
            valueList.add(cfg + "Status");
        }
        logger.info("queryLANStatus valueList:{}", valueList);

        Map<String, Object> valueMap = instructionMethodService.getParameterValuesErrorCode(gatewayInfo.getGatewayMacaddress(), valueList);
        logger.info("queryLANStatus getParameterValues valueMap:{}", valueMap);
        if(Integer.valueOf(valueMap.get("result").toString()) == -1)
        {
            if(null != valueMap.get("errorCode"))
            {
                retMap.put("Result", Integer.valueOf(valueMap.get("errorCode").toString()));
            }

            return false;
        }

        List<String> statusList = new ArrayList<>();
        for(Map.Entry<String, Object> entry : valueMap.entrySet())
        {
            statusList.add(entry.getKey());
        }

        //根据节点中的{i}排序
        Collections.sort(statusList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        int i = 1;
        StringBuilder statusBuilder = new StringBuilder();
        for(String key : statusList)
        {
            String status =  "Up".equals(valueMap.get(key).toString()) ? "1" : "0";
            statusBuilder.append("LAN" + i + "=" + status);
            if(i != statusList.size())
            {
                statusBuilder.append(",");
            }
            ++i;
        }
        retMap.put("LANStatus", statusBuilder);
        return true;
    }

    /**
     * 获取LAN口节点路径
     * @param gatewayMacAddress
     * @return
     */
    private Boolean getLANEthernetInterfaceConfigList(String gatewayMacAddress, List<String> resultList, Map<String, Object> retMap)
    {
        //InternetGatewayDevice.LANDevice.{i}.
        List<String> preLANDeviceList = instructionMethodService.getLANDevicePrefix(gatewayMacAddress);
        logger.info("getLANEthernetInterfaceConfigList getParameterNames lanDeviceMap:{}", preLANDeviceList);

        //InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{i}.
        for (String lan : preLANDeviceList) {
            String interfaceConfig = lan + "LANEthernetInterfaceConfig.";
            Map<String, Object> wanConnectionMap = instructionMethodService.getParameterNamesErrorCode(gatewayMacAddress,
                    interfaceConfig, true);
            if(Integer.valueOf(wanConnectionMap.get("result").toString()) == -1)
            {
                if(null != wanConnectionMap.get("errorCode"))
                {
                    retMap.put("Result", Integer.valueOf(wanConnectionMap.get("errorCode").toString()));
                }

                return false;
            }
            String regInterfaceConfig = interfaceConfig + "[0-9]+";
            for (Map.Entry<String, Object> entry : wanConnectionMap.entrySet()) {
                InstructionUtil.getName(resultList, regInterfaceConfig, entry.getKey());
            }
        }

        logger.info("getLANEthernetInterfaceConfigList preInterfaceConfigList:{}", resultList);
        return true;
    }


    /**
     * 获取SSID的名称和状态
     * @param gatewayInfo
     * @param retMap
     */
    private Boolean querySSIDInfo(GatewayInfo gatewayInfo, Map<String, Object> retMap) {
        //InternetGatewayDevice.LANDevice.{i}.
        List<String> preWANDeviceList = instructionMethodService.getLANDevicePrefix(gatewayInfo.getGatewayMacaddress());
        logger.info("querySSIDInfo getParameterNames wanDeviceMap:{}", preWANDeviceList);

        //InternetGatewayDevice.LANDevice.{i}.WLANConfiguration.{i}.
        List<String> preWanCfgList = new ArrayList<>();
        for(String wan : preWANDeviceList)
        {
            String wanCfg = wan + "WLANConfiguration.";
            Map<String, Object> wanConnectionMap = instructionMethodService.getParameterNamesErrorCode(gatewayInfo.getGatewayMacaddress(),
                    wanCfg, true);
            if(Integer.valueOf(wanConnectionMap.get("result").toString()) == -1)
            {
                if(null != wanConnectionMap.get("errorCode"))
                {
                    retMap.put("Result", Integer.valueOf(wanConnectionMap.get("errorCode").toString()));
                }

                return false;
            }
            String regWanCfg =  wanCfg + "[0-9]+";
            for(Map.Entry<String, Object> entry : wanConnectionMap.entrySet())
            {
                InstructionUtil.getName(preWanCfgList, regWanCfg, entry.getKey());
            }
        }

        List<String> valueList = new ArrayList<>();
        for(String cfg: preWanCfgList)
        {
            valueList.add(cfg + "SSID");
            valueList.add(cfg + "Status");
            valueList.add(cfg + "X_CMCC_APModuleEnable");
        }
        logger.info("querySSIDInfo valueList:{}", valueList);

        Map<String, Object> valueMap = instructionMethodService.getParameterValuesErrorCode(gatewayInfo.getGatewayMacaddress(), valueList);
        logger.info("querySSIDInfo getParameterValues valueMap:{}", valueMap);
        if(Integer.valueOf(valueMap.get("result").toString()) == -1)
        {
            if(null != valueMap.get("errorCode"))
            {
                retMap.put("Result", Integer.valueOf(valueMap.get("errorCode").toString()));
            }

            return false;
        }


        List<String> ssidList = new ArrayList<>();
        for(Map.Entry<String, Object> entry : valueMap.entrySet())
        {
            if(entry.getKey().endsWith("SSID"))
            {
                ssidList.add(entry.getKey());
            }
        }

        //根据节点中的{i}排序
        Collections.sort(ssidList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });


        int i = 1;
        StringBuilder nameBuilder = new StringBuilder();
        StringBuilder statusBuilder = new StringBuilder();
        int wifiSwitch = 0;
        for(String key : ssidList)
        {
            String preName = key.substring(0, key.indexOf("SSID"));
            String status = "Up".equals(valueMap.get(preName + "Status").toString()) ? "true" : "false";

            nameBuilder.append("SSID" + i + "=" + valueMap.get(key));
            statusBuilder.append("SSID" + i + "=" + status);
            if(i != ssidList.size())
            {
                nameBuilder.append(",");
                statusBuilder.append(",");
            }

            if(wifiSwitch == 0)
            {
                wifiSwitch = Boolean.valueOf(valueMap.get(preName + "X_CMCC_APModuleEnable").toString()) ? 1 : 0;
            }
            ++i;
        }
        retMap.put("WifiSwitch", wifiSwitch);
        retMap.put("SsidName", nameBuilder);
        retMap.put("SsidStatus", statusBuilder);

        return true;
    }
}
