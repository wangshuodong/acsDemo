package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.EventStruct;
import com.cmiot.acs.model.struct.ParameterList;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.common.utils.secret.MD5Util;
import com.cmiot.rms.dao.model.GateWaySpeedReportBean;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.InformInfo;
import com.cmiot.rms.dao.model.SubDeviceSpeedReportBean;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.InformService;
import com.cmiot.rms.services.MonitorService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.InstructionUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016/5/12.
 */
@Service
public class MonitorServiceImpl implements MonitorService {

    private static Logger logger = LoggerFactory.getLogger(MonitorServiceImpl.class);

    @Autowired
    private GatewayInfoService gatewayInfoService;

    @Autowired
    private InformService informService;

    @Autowired
    private GatewayInfoServiceImpl gatewayInfoServiceImpl;

    @Autowired
    private InstructionMethodService instructionMethodService;

    @Autowired
    private RedisClientTemplate redisClientTemplate;

    @Autowired
    private SyncInfoToFirstLevelPlatformServiceImpl syncInfoToFirstLevelPlatformService;


    @Override
    public void reportMonitor(Inform inform) {

        logger.info("invoke MonitorServiceImpl reportMonitor inform:{}",inform);
        try {
            // 查询获取网关新
            GatewayInfo resultGatewayInfo = getResultGatewayInfo(inform);
            //从redis取5.5.4.	设置下挂设备实时速率统计开关接口设置的值，如果是false，则不处理了
            String reportSpeedTestKey = resultGatewayInfo.getGatewayMacaddress() + "reportSpeedTestKey";
            String isCanReport = redisClientTemplate.get(reportSpeedTestKey);
            logger.info("MonitorServiceImpl reportMonitor get redis isCanReport:{}",isCanReport);
            if((!StringUtils.isEmpty(isCanReport))&&"false".equals(isCanReport)){
                return;
            }
            //查询redis中上报策略
            String gateWaySpeedPolicy = redisClientTemplate.get(resultGatewayInfo.getGatewayMacaddress() +"gateWaySpeedPolicy");
            String policyMapStr = redisClientTemplate.get(gateWaySpeedPolicy);
            logger.info("getLanSpeedReportPolicy get redis policyMapStr:{}",policyMapStr);
            List<String> macInReportPolicy = new ArrayList<String>();
            if(!org.apache.commons.lang.StringUtils.isEmpty(policyMapStr)) {
            	
            	 Map<String, Object> policyMap = JSON.parseObject(policyMapStr, Map.class);
            	 for (Map.Entry<String, Object> entry : policyMap.entrySet()) {
            		 if("1".equals(entry.getValue())){
            			 macInReportPolicy.add(entry.getKey());
            		 }
            	 }
            }else{
            	//没有配置上报策略
            	logger.info("该网关没有配置上报策略");
            	return;
            }
            
            if (resultGatewayInfo != null) {
                ParameterList paramlist = inform.getParameterList();
                List<ParameterValueStruct> parameterValueStructsList = paramlist.getParameterValueStructs();
                //本方法暂时只处理上报的下挂终端流量，用于上报下挂终端的速率
                String bytesSend = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.X_CMCC_Stats.BytesSen[d|t]";
                String bytesReceived = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.X_CMCC_Stats.BytesReceived";
                String lanDeviceMAC = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.MACAddress";
                EventStruct[] list = inform.getEvent().getEventCodes();
                List<String> events = new ArrayList<>();
                for (EventStruct eventStruct : list) {
                    events.add(eventStruct.getEvenCode());
                }

                long timeMillis = System.currentTimeMillis();
                long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
                GatewayInfo gatewayInfo = new GatewayInfo();
                gatewayInfo.setGatewayUuid(resultGatewayInfo.getGatewayUuid());
                // 最近连接时间
                gatewayInfo.setGatewayLastConnTime((int) timeSeconds);
                // 每次inform上报都要更新一次最近连接时间
                gatewayInfoServiceImpl.updateSelectGatewayInfo(gatewayInfo);
                //获取网关MAC地址
                String gateWayMac = resultGatewayInfo.getGatewayMacaddress();

                GateWaySpeedReportBean gateWaySpeedReportBean = new GateWaySpeedReportBean();
               // gateWaySpeedReportBean.setGateWayMac(gateWayMac);
               
                gateWaySpeedReportBean.setTime(System.currentTimeMillis());
                List<SubDeviceSpeedReportBean> subDeviceRbeanList = new ArrayList<>();


                Pattern pattrenSend = Pattern.compile(bytesSend);
                Pattern pattrenReceive = Pattern.compile(bytesReceived);
                Pattern pattrenLanDeviceMAC = Pattern.compile(lanDeviceMAC);

                //过滤无效参数
                for (int i = 0; i < parameterValueStructsList.size(); i++) {
                    ParameterValueStruct parameterValueStruct = parameterValueStructsList.get(i);
                    String paramName = parameterValueStruct.getName();
                    Matcher matcherSend = pattrenSend.matcher(paramName);
                    Matcher matcherReceive = pattrenReceive.matcher(paramName);
                    if (matcherSend.matches()) {
                        String subDeviceMacName = paramName.substring(0, paramName.indexOf("X_CMCC_Stats")) + "MACAddress";
                        String value = parameterValueStruct.getValue().toString();
                        addSubDeviceReportBean(subDeviceMacName, subDeviceRbeanList, value, 0);
                    } else if (matcherReceive.matches()) {
                        //判断是不是上报的接收字节数统计
                        String subDeviceMacName = paramName.substring(0, paramName.indexOf("X_CMCC_Stats")) + "MACAddress";
                        String value = parameterValueStruct.getValue().toString();
                        addSubDeviceReportBean(subDeviceMacName, subDeviceRbeanList, value, 1);
                    }
                }
                //需要查询参数的集合
                if (subDeviceRbeanList.size() > 0) {
                    List<String> wanNameList = new ArrayList<>();
                    //通过某个deviceMac的路径获取到网关下挂主机列表路径
                    String subDeviceMacName = subDeviceRbeanList.get(0).getSubDeviceMacName();
                    String hostListName = subDeviceMacName.substring(0, subDeviceMacName.indexOf("Hosts.Host")) + "Hosts.Host.";
                    //查询网关下挂主机列表的所有节点名称
                    Map<String, Object> nameMap = instructionMethodService.getParameterNames(gateWayMac, hostListName, false);
                    logger.info(" MonitorServiceImpl reportMonitor getParameterNames nameMap:{}", nameMap);
                    List<String> regWanNameList = new ArrayList<String>();
                    regWanNameList.add(bytesSend);
                    regWanNameList.add(bytesReceived);
                    regWanNameList.add(lanDeviceMAC);
                    for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
                        InstructionUtil.getName(wanNameList, regWanNameList, entry.getKey());
                    }
                    //根据具体的节点获取相应的value
                    Map<String, Object> wanValueMap = instructionMethodService.getParameterValues(gateWayMac, wanNameList);
                    logger.info(" MonitorServiceImpl reportMonitor getParameterValues wanValueMap:{}", wanValueMap);
                    //处理查询到的值，一种是下挂设备的MAC，一种是根据每个下挂设备的流量计算网关的流量
                    Long sendBytes = 0l;
                    Long receivedBytes = 0l;
                    
                    List<Map<String, Object>> deviceInfoList = new ArrayList<Map<String,Object>>();
                    double totalHGDsSpeed = 0.0;
                    double totalHGUsSpeed = 0.0;
                    String speedUnit = "kbps";
                    DecimalFormat df2  = new DecimalFormat("0.00");
                    for (Map.Entry<String, Object> entry : wanValueMap.entrySet()) {
                        SubDeviceSpeedReportBean lanDevice = new SubDeviceSpeedReportBean();
                       
                        Map<String, Object> deviceInfo = new HashMap<String, Object>();
                        Matcher matcherLanDeviceMAC = pattrenLanDeviceMAC.matcher(entry.getKey());
                        if(matcherLanDeviceMAC.matches()){
                        	//下挂设备MAC
                        	String lanDeviceSendname = null;
                            if (wanValueMap.containsKey(entry.getKey().substring(0, entry.getKey().indexOf("MACAddress")) + "X_CMCC_Stats.BytesSend")) {
                            	lanDeviceSendname = entry.getKey().substring(0, entry.getKey().indexOf("MACAddress")) + "X_CMCC_Stats.BytesSend";
							}else if (wanValueMap.containsKey(entry.getKey().substring(0, entry.getKey().indexOf("MACAddress")) + "X_CMCC_Stats.BytesSent")) {
								lanDeviceSendname = entry.getKey().substring(0, entry.getKey().indexOf("MACAddress")) + "X_CMCC_Stats.BytesSent";
							}
                        	
                            sendBytes = wanValueMap.get(lanDeviceSendname) == null ? 0l : Long.parseLong(wanValueMap.get(lanDeviceSendname).toString());
                        	String lanDeviceReceivedname = entry.getKey().substring(0, entry.getKey().indexOf("X_CMCC_Stats")) + "X_CMCC_Stats.BytesReceived";
                        	receivedBytes = wanValueMap.get(lanDeviceReceivedname) == null ? 0l : Long.parseLong(wanValueMap.get(lanDeviceReceivedname).toString());
                        	
                        	List<SubDeviceSpeedReportBean> currentLanDeviceList = new ArrayList<>(); //存放当前下挂设备信息
                        	lanDevice.setSubDeviceUpBytes(wanValueMap.get(lanDeviceSendname).toString());
                            lanDevice.setSubDeviceMac(entry.getValue().toString());
                            lanDevice.setSubDeviceDownBytes(wanValueMap.get(lanDeviceReceivedname).toString());
                            currentLanDeviceList.add(lanDevice);
                        	
                            
                            GateWaySpeedReportBean lanDeviceSpeedReportBean = new GateWaySpeedReportBean();
                            
                            //用下挂设备MAC
                            lanDeviceSpeedReportBean.setGateWayMac(entry.getValue().toString());
                            lanDeviceSpeedReportBean.setTime(System.currentTimeMillis());
                            lanDeviceSpeedReportBean.setDownBytes(String.valueOf(receivedBytes));
                            lanDeviceSpeedReportBean.setUpBytes(String.valueOf(sendBytes));
                          //  lanDeviceSpeedReportBean.setSubDeviceSpeedReportBeanList(subDeviceRbeanList);
                            lanDeviceSpeedReportBean.setGetHGByteTime(System.currentTimeMillis());
                            lanDeviceSpeedReportBean.setLanDeviceList(currentLanDeviceList);
                            
                            //定义下挂设备在redis上存储的键值
                            String gateWaySpeedKey = entry.getValue().toString() + "gateWaySpeedKey";
                            //先获取，如果存在就用上次的做计算速率，并上报给一级家开平台，如果不存在，则保存到reids
                            String redisStr = redisClientTemplate.get(gateWaySpeedKey);
                            logger.info(" MonitorServiceImpl reportMonitor redisClientTemplate.get redisStr:{}", redisStr);
                            if (StringUtils.isEmpty(redisStr)) {
                                redisClientTemplate.set(gateWaySpeedKey, JSON.toJSONString(lanDeviceSpeedReportBean));
                            } else {
                                //更新redis数据
                                redisClientTemplate.set(gateWaySpeedKey, JSON.toJSONString(lanDeviceSpeedReportBean));

                                GateWaySpeedReportBean gateWaySpeedReportBeanFind = JSON.parseObject(redisStr, GateWaySpeedReportBean.class);
                                deviceInfo.put("DeviceMAC", entry.getValue().toString());
                                long intervalTime = (Long.valueOf(gateWaySpeedReportBean.getGetHGByteTime()) - Long.valueOf(gateWaySpeedReportBeanFind.getGetHGByteTime())) / 1000;
                                long hGDownByte = Long.valueOf(lanDeviceSpeedReportBean.getDownBytes()) - Long.valueOf(gateWaySpeedReportBeanFind.getDownBytes());
                                long hGUpByte = Long.valueOf(lanDeviceSpeedReportBean.getUpBytes()) - Long.valueOf(gateWaySpeedReportBeanFind.getUpBytes());
                                double hGDsSpeed = 0.0;
                                double hGUsSpeed = 0.0;
                                if(intervalTime > 0){
                                	hGDsSpeed = (hGDownByte/(double)intervalTime)/1024;
                                	hGUsSpeed = (hGUpByte/(double)intervalTime)/1024;
                                }else{
                                	hGDsSpeed = 0d;
                                	hGUsSpeed = 0d;
                                }
                                totalHGDsSpeed += hGDsSpeed;
                                totalHGUsSpeed += hGUsSpeed;
                                
                                deviceInfo.put("UsBandwidth", df2.format(hGUsSpeed) + speedUnit);
                                deviceInfo.put("DsBandwidth", df2.format(hGDsSpeed) + speedUnit);
                               
                                if(macInReportPolicy.contains(entry.getValue().toString())){
                                	//只返回配置了上报策略的下挂设备
                                	deviceInfoList.add(deviceInfo);
                                }
                            }
                        }
                    }
                    Map<String, Object> reportMap = new HashMap<>();
                    reportMap.put(Constant.RPCMETHOD, "Report");
                    reportMap.put(Constant.ID, (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                    reportMap.put(Constant.CMDTYPE, "REPORT_LAN_DEVICE_SPEED");
                    reportMap.put(Constant.SEQUENCEID, InstructionUtil.generate8HexString());
                    Map<String, Object> parameterMap = new HashMap<>();
                    parameterMap.put("MAC", gateWayMac);
                    parameterMap.put("time", DateTools.format(gateWaySpeedReportBean.getTime(), DateTools.DATE_FORMAT_YYYYMMDDHHMMSS));    
                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("Devices", deviceInfoList);
                    parameterMap.put("HGUsSpeed", df2.format(totalHGUsSpeed) + speedUnit);
                    parameterMap.put("HGDsSpeed", df2.format(totalHGDsSpeed) + speedUnit);
                    parameterMap.put("Message", messageMap);
                    String md5Str = MD5Util.md5(DateTools.format(gateWaySpeedReportBean.getTime(), DateTools.DATE_FORMAT_YYYYMMDDHHMMSS)  + JSON.toJSONString(messageMap) + gateWayMac + inform.getDeviceId().getSerialNubmer());
                    parameterMap.put("MD5", md5Str);
                    reportMap.put(Constant.PARAMETER, parameterMap);
                    //向一级家开平台上报数据
                    logger.info(" MonitorServiceImpl reportMonitor report reportMap:{}", reportMap);
                    if(deviceInfoList.size()>0) {
                        syncInfoToFirstLevelPlatformService.report("reportLanDevicespeed", reportMap);
                    }  
                }
            } 
        }catch (Exception e){
            logger.error("report X CMCC MONITOR error!",e);
        }
    }

    // 小方法分离
    private GatewayInfo getResultGatewayInfo(Inform inform) {
        // 查询网关表SN是否储存在
        GatewayInfo gatewayInfo = new GatewayInfo();
        String serialnumber = inform.getDeviceId().getSerialNubmer();
        String gatewayInfoFactoryCode = inform.getDeviceId().getOui();
        // SN号
        gatewayInfo.setGatewaySerialnumber(serialnumber);
        gatewayInfo.setGatewayFactoryCode(gatewayInfoFactoryCode);
        //根据SN和OUI查询是否已经存在CPE
        GatewayInfo resultGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
        return resultGatewayInfo;
    }

    /**
     * 小方法分离 更新subDeviceRbeanList
     * @param subDeviceMacName
     * @param subDeviceRbeanList
     * @param value 上行或者下行的字节数
     * @param upOrDown 上行或者下行 0表示上行 1表示下行
     */
    private void addSubDeviceReportBean(String subDeviceMacName,List<SubDeviceSpeedReportBean> subDeviceRbeanList,String value,int upOrDown){
        boolean isAdd = false;
        if(0 == upOrDown) {
            //判断是不是上报的发送字节数统计
            for (int j = 0; j < subDeviceRbeanList.size(); j++) {
                if (subDeviceMacName.equals(subDeviceRbeanList.get(j).getSubDeviceMacName())) {
                    isAdd = true;
                    subDeviceRbeanList.get(j).setSubDeviceUpBytes(value);
                }
            }
            if (!isAdd) {
                SubDeviceSpeedReportBean subDeviceSpeedReportBean = new SubDeviceSpeedReportBean();
                subDeviceSpeedReportBean.setSubDeviceMacName(subDeviceMacName);
                subDeviceSpeedReportBean.setSubDeviceUpBytes(value);
                subDeviceRbeanList.add(subDeviceSpeedReportBean);
            }
        }else if(1 == upOrDown){
            //判断是不是上报的发送字节数统计
            for (int j = 0; j < subDeviceRbeanList.size(); j++) {
                if (subDeviceMacName.equals(subDeviceRbeanList.get(j).getSubDeviceMacName())) {
                    isAdd = true;
                    subDeviceRbeanList.get(j).setSubDeviceDownBytes(value);
                }
            }
            if (!isAdd) {
                SubDeviceSpeedReportBean subDeviceSpeedReportBean = new SubDeviceSpeedReportBean();
                subDeviceSpeedReportBean.setSubDeviceMacName(subDeviceMacName);
                subDeviceSpeedReportBean.setSubDeviceDownBytes(value);
                subDeviceRbeanList.add(subDeviceSpeedReportBean);
            }
        }
    }
    /**
     * 添加上报信息
     *
     * @param informInfo
     * @param informReq
     */
    private void addInformInfo(InformInfo informInfo, Inform informReq) {
        //生产厂家
        informInfo.setInformId(UniqueUtil.uuid());
        informInfo.setInformContent(JSON.toJSONString(informReq));
        long timeMillis = System.currentTimeMillis();
        long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
        informInfo.setInformCreateTime((int) timeSeconds);
        informInfo.setInformModifyTime((int) timeSeconds);


        // 写入上报信息表
//        informService.addInformInfo(informInfo);
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName() + "成功!详情：" + JSON.toJSONString(informInfo));

        // 日志记录 添加上报信息
//        LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(informReq), informReq.getRequestId(), "", "上报信息",LogTypeEnum.LOG_TYPE_SYSTEM.description());

    }

}
