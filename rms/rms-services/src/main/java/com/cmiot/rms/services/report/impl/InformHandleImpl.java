package com.cmiot.rms.services.report.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.cmiot.rms.common.constant.Constant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.Event;
import com.cmiot.acs.model.struct.ParameterList;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.cache.TemporaryObject;
import com.cmiot.rms.common.enums.UpgradeTaskEventEnum;
import com.cmiot.rms.dao.mapper.FirmwareInfoMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessExecuteHistoryMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessOpenDetailMapper;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.mapper.GatewayPasswordMapper;
import com.cmiot.rms.dao.mapper.WorkOrderTemplateInfoMapper;
import com.cmiot.rms.dao.model.Area;
import com.cmiot.rms.dao.model.FirmwareInfo;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.BatchSetTaskTrrigerService;
import com.cmiot.rms.services.FirmwareUpgradeTaskInnerService;
import com.cmiot.rms.services.GatewayBackupFileTaskInnerService;
import com.cmiot.rms.services.GatewayFlowrateTaskService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.HomeNetworkConfigService;
import com.cmiot.rms.services.MonitorService;
import com.cmiot.rms.services.WorkOrderInnerService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.message.KafkaProducer;
import com.cmiot.rms.services.report.InformHandle;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.workorder.impl.BusiOperation;
import com.tydic.inter.app.service.GatewayHandleService;
import com.tydic.service.PluginDeviceService;

/**
 * CPE上报Inform处理类
 * Created by panmingguo on 2016/5/10.
 */
@Service("informHandle")
public class InformHandleImpl implements InformHandle {
    private static final Logger LOGGER = LoggerFactory.getLogger(InformHandleImpl.class);

    @Autowired
    FirmwareUpgradeTaskInnerService firmwareUpgradeTaskInnerService;

    @Autowired
    HomeNetworkConfigService homeNetworkConfigService;

    @Autowired
    MonitorService monitorService;

    @Autowired
    BatchSetTaskTrrigerService batchSetTaskTrrigerService;

    @Autowired
    InstructionMethodService instructionMethodService;

    @Autowired
    GatewayInfoService gatewayInfoService;

    @Autowired
    GatewayBackupFileTaskInnerService gatewayBackupFileTaskInnerService;
    
    @Autowired
    GatewayFlowrateTaskService gatewayFlowrateTaskService;

    @Autowired
    AreaService areaService;

    @Autowired
    KafkaProducer kafkaProducer;

    @Autowired
    GatewayPasswordMapper gatewayPasswordMapper;

    @Autowired
    GatewayBusinessMapper gatewayBusinessMapper;

    @Autowired
    WorkOrderTemplateInfoMapper workOrderTemplateInfoMapper;

    @Autowired
    BusiOperation busiOperation;

    @Autowired
    GatewayBusinessExecuteHistoryMapper gatewayBusinessExecuteHistoryMapper;

    @Autowired
    GatewayBusinessOpenDetailMapper gatewayBusinessOpenDetailMapper;

    @Autowired
    GatewayInfoMapper gatewayInfoMapper;

    @Autowired
    private GatewayHandleService gatewayHandleService;

    @Autowired
    private PluginDeviceService pluginDeviceService;

    @Autowired
    private WorkOrderInnerService workOrderInnerService;

    @Value("${is.digest.open}")
    int isDigestOpen;

    @Value("${work.order.fail.count}")
    int workOrderFailCount;

    @Autowired
    private RedisClientTemplate redisClientTemplate;
    
    @Autowired
    private FirmwareInfoMapper firewareInfoMapper;

    /**
     * "0 BOOTSTRAP" 表明会话发起原因是CPE首次安装或ACS的URL发生变化。
     *
     * @param inform
     */
    @Override
    public void bootStrapEvent(Inform inform) {
        LOGGER.info("Start invoke bootStrapEvent：{}", inform);

        // 更新区域和状态
        updateAreaAndStatus(inform);

        /*// 备份
        boolean isBackupTask = gatewayBackupFileTaskInnerService.executeUpgradeTask(inform.getDeviceId().getSerialNubmer(),
                inform.getDeviceId().getOui(),
                UpgradeTaskEventEnum.BOOTSTRAP);

        LOGGER.info("end invoke executeUpgradeTask isBackupTask：{}", isBackupTask);
        //设置参数
        batchSetTaskTrrigerService.batchSetTaskTrriger(isBackupTask, inform.getDeviceId().getSerialNubmer(),
                inform.getDeviceId().getOui(),
                UpgradeTaskEventEnum.BOOTSTRAP);
        LOGGER.info("End invoke bootStrapEvent!");


        //查询流量任务
        gatewayFlowrateTaskService.executeTask(inform.getDeviceId().getSerialNubmer(),inform.getDeviceId().getOui(), 1);*/
        

    }

    /**
     * "1 BOOT" 表明会话发起原因是CPE加电或重置，包括系统首次启动，以及因任何原因而引起的重启，包括使用Reboot方法。
     *
     * @param inform
     */
    @Override
    public void bootEvent(Inform inform) {
        LOGGER.info("Start invoke bootEvent：{}", inform);

        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewaySerialnumber(inform.getDeviceId().getSerialNubmer());
        gatewayInfo.setGatewayFactoryCode(inform.getDeviceId().getOui());
        // 根据 SN 和 OUI查询网关信息
        GatewayInfo selectGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
        if(selectGatewayInfo == null){
            LOGGER.info("invoke bootEvent，网关信息为空");
            return;
        }
        //对首次上报事件加锁，在首次上报流程中，其他事件不能对网关下发固件升级，备份，批量设置，查询流量等指令
        String FirstReportKey = "FirstReport" + selectGatewayInfo.getGatewaySerialnumber();
        String str = redisClientTemplate.get(FirstReportKey);
        LOGGER.info("redisClientTemplate.get 返回：{},{}" ,FirstReportKey,str);
        if(StringUtils.isEmpty(str)) {
            //防止网关在断电时，工单被执行失败，导致工单不能被执行的情况，在1 boot事件时，对执行失败的且次数达到配置的最大次数的工单再执行一遍
//            workOrderInnerService.excuteFailWorkOrder(inform);
            //升级
            boolean isExistTask = firmwareUpgradeTaskInnerService.executeUpgradeTask(gatewayInfo,
                    UpgradeTaskEventEnum.BOOT);
            LOGGER.info("end invoke executeUpgradeTask isExistTask：{}", isExistTask);

            // 备份
            boolean isBackupTask = gatewayBackupFileTaskInnerService.executeUpgradeTask(inform.getDeviceId().getSerialNubmer(),
                    inform.getDeviceId().getOui(),
                    UpgradeTaskEventEnum.BOOT);

            LOGGER.info("end invoke executeUpgradeTask isBackupTask：{}", isBackupTask);

            //设置参数
            batchSetTaskTrrigerService.batchSetTaskTrriger(isBackupTask, inform.getDeviceId().getSerialNubmer(),
                    inform.getDeviceId().getOui(),
                    UpgradeTaskEventEnum.BOOT);

            //查询流量任务
            LOGGER.info("start flowrate task");
            gatewayFlowrateTaskService.executeTask(inform.getDeviceId().getSerialNubmer(), inform.getDeviceId().getOui(), 3);
            LOGGER.info("end flowrate task");
        }
        LOGGER.info("End invoke bootEvent!");
    }

    /**
     * "2 PERIODIC" 表明会话发起原因是定期的Inform引起。
     *
     * @param inform
     */
    @Override
    public void periodicEvent(Inform inform) {
        LOGGER.info("Start invoke periodicEvent：{}", inform);

        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewaySerialnumber(inform.getDeviceId().getSerialNubmer());
        gatewayInfo.setGatewayFactoryCode(inform.getDeviceId().getOui());
        // 根据 SN 和 OUI查询网关信息
        GatewayInfo selectGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
        if(selectGatewayInfo == null){
            LOGGER.info("invoke periodicEvent，网关信息为空");
            return;
        }
        //对首次上报事件加锁，在首次上报流程中，其他事件不能对网关下发固件升级，备份，批量设置，查询流量等指令
        String FirstReportKey = "FirstReport" + selectGatewayInfo.getGatewaySerialnumber();
        String str = redisClientTemplate.get(FirstReportKey);
        LOGGER.info("redisClientTemplate.get 返回：{},{}" ,FirstReportKey,str);
        if(StringUtils.isEmpty(str)) {
            //升级
            boolean isExistTask = firmwareUpgradeTaskInnerService.executeUpgradeTask(gatewayInfo,
                    UpgradeTaskEventEnum.PERIODIC);
            LOGGER.info("end invoke executeUpgradeTask isExistTask：{}", isExistTask);

            // 备份
            boolean isBackupTask = gatewayBackupFileTaskInnerService.executeUpgradeTask(inform.getDeviceId().getSerialNubmer(),
                    inform.getDeviceId().getOui(),
                    UpgradeTaskEventEnum.PERIODIC);
            LOGGER.info("end invoke executeUpgradeTask isBackupTask：{}", isBackupTask);

            //设置参数
            batchSetTaskTrrigerService.batchSetTaskTrriger(isExistTask, inform.getDeviceId().getSerialNubmer(),
                    inform.getDeviceId().getOui(),
                    UpgradeTaskEventEnum.PERIODIC);

            //查询流量任务
            gatewayFlowrateTaskService.executeTask(inform.getDeviceId().getSerialNubmer(), inform.getDeviceId().getOui(), 2);

        }
        LOGGER.info("End invoke periodicEvent!");
    }

    /**
     * "3 SCHEDULED" 表明会话发起原因是调用了ScheduleInform方法。
     *
     * @param inform
     */
    @Override
    public void scheduleEvent(Inform inform) {

    }

    /**
     * "4 VALUE CHANGE" 表明会话发起原因是一个或多个参数值的变化。该参数值包括在Inform方法的调用中。例如CPE分配了新的IP地址。
     *
     * @param inform
     */
    @Override
    public void valueChangeEvent(Inform inform) {
        LOGGER.info("Start invoke valueChangeEvent：{}", inform);
        Map<String, Object> para = new HashMap<>();
        para.put("inform", inform);
        homeNetworkConfigService.reportLanDeviceOnline(para);

        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewaySerialnumber(inform.getDeviceId().getSerialNubmer());
        gatewayInfo.setGatewayFactoryCode(inform.getDeviceId().getOui());
        // 根据 SN 和 OUI查询网关信息
        GatewayInfo selectGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
        if(selectGatewayInfo == null){
            LOGGER.info("invoke valueChangeEvent，网关信息为空");
            return;
        }
        LOGGER.info("VALUE CHANGE事件触发：根据上报信息更新网关所在区域。Inform:"+inform);
        List<ParameterValueStruct> paramList = inform.getParameterList().getParameterValueStructs();
        //回连URL
        String connectionUrl = "";
        for(ParameterValueStruct pvs : paramList){
        	if("InternetGatewayDevice.ManagementServer.ConnectionRequestURL".equals(pvs.getName())){
        		connectionUrl = pvs.getValue()==null?null :pvs.getValue().toString();
        	}
        }
        String ip = "";
        String areaId= "";
        if(connectionUrl != null && !"".equals(connectionUrl)){
        	int i = connectionUrl.indexOf("//") + 2;
            int j = connectionUrl.indexOf(":", 10);
            ip = connectionUrl.substring(i, j);
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("ip", ip);
            Map<String, Object> areaMap = areaService.findGateWayArea(param);
            if (areaMap.containsKey("areaId")) {
                areaId = String.valueOf(areaMap.get("areaId"));
                if(areaId != null && !areaId.equals(selectGatewayInfo.getGatewayAreaId())){
                	selectGatewayInfo.setGatewayAreaId(areaId);
                	gatewayInfoService.updateSelectGatewayInfo(selectGatewayInfo);
                }
            }
        }
        
        
        //对首次上报事件加锁，在首次上报流程中，其他事件不能对网关下发固件升级，备份，批量设置，查询流量等指令
        String FirstReportKey = "FirstReport" + selectGatewayInfo.getGatewaySerialnumber();
        String str = redisClientTemplate.get(FirstReportKey);
        LOGGER.info("redisClientTemplate.get 返回：{},{}" ,FirstReportKey,str);
        if(StringUtils.isEmpty(str)) {
            // 升级
            boolean isExistTask = firmwareUpgradeTaskInnerService.executeUpgradeTask(gatewayInfo,
                    UpgradeTaskEventEnum.VALUECHANGE);
            LOGGER.info("end invoke executeUpgradeTask isExistTask：{}", isExistTask);

            // 备份
            boolean isBackupTask = gatewayBackupFileTaskInnerService.executeUpgradeTask(inform.getDeviceId().getSerialNubmer(),
                    inform.getDeviceId().getOui(),
                    UpgradeTaskEventEnum.VALUECHANGE);
            LOGGER.info("end invoke executeUpgradeTask isBackupTask：{}", isBackupTask);

            //设置参数
            batchSetTaskTrrigerService.batchSetTaskTrriger(isBackupTask, inform.getDeviceId().getSerialNubmer(),
                    inform.getDeviceId().getOui(),
                    UpgradeTaskEventEnum.VALUECHANGE);

            //查询流量任务
            gatewayFlowrateTaskService.executeTask(inform.getDeviceId().getSerialNubmer(), inform.getDeviceId().getOui(), 4);

        }
        LOGGER.info("End invoke valueChangeEvent：{}", inform);
    }

    /**
     * "6 CONNECTION REQUEST" 表明会话发起原因是3.2节中定义的源自服务器的Connection Request
     *
     * @param inform
     */
    @Override
    public void connectionRequestEvent(Inform inform) {

    }

    /**
     * "7 TRANSFER COMPLETE" 表明会话的发起是为了表明以前请求的下载或上载（不管是否成功）已经结束，在此会话中将要调用一次或多次TransferComplete方法。
     *
     * @param inform
     */
    @Override
    public void transferCompleteEvent(Inform inform) {

    }

    /**
     * "8 DIAGNOSTICS COMPLETE" 当完成由ACS发起的诊断测试结束后，重新与ACS建立连接时使用。如DSL环路诊断（见附录B）。
     *
     * @param inform
     */
    @Override
    public void diagnosticsCompleteEvent(Inform inform) {
        LOGGER.info("Start invoke diagnosticsCompleteEvent：{}", inform);
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewaySerialnumber(inform.getDeviceId().getSerialNubmer());
        gatewayInfo.setGatewayFactoryCode(inform.getDeviceId().getOui());
        // 根据 SN 和 OUI查询网关信息
        GatewayInfo resultGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
        if (null == resultGatewayInfo) {
            return;
        }

        LOGGER.info("delete diagnose address key!");
        redisClientTemplate.del(inform.getDeviceId().getOui() + Constant.SEPARATOR + inform.getDeviceId().getSerialNubmer() + Constant.SEPARATOR + "diagnose");

        LOGGER.info("diagnosticsCompleteEvent GatewayInfo:{}", "diagnostics_" + resultGatewayInfo.getGatewayMacaddress());

        TemporaryObject object = RequestCache.get("diagnostics_" + resultGatewayInfo.getGatewayMacaddress());
        if (null != object) {
            synchronized (object) {
                LOGGER.info("diagnosticsCompleteEvent notifyAll mac:{}", "diagnostics_" + resultGatewayInfo.getGatewayMacaddress());
                object.notifyAll();
            }
        } else {
            LOGGER.info("not exist diagnostics cache!");
//            LOGGER.info("KafkaProducer SendMessage mac:{}", "diagnostics_" + resultGatewayInfo.getGatewayMacaddress());
//            kafkaProducer.sendMessage("requestId", "diagnostics_" + resultGatewayInfo.getGatewayMacaddress());
        }
        LOGGER.info("End invoke diagnosticsCompleteEvent");
    }

    @Override
    public void xCmccMonitor(Inform inform) {
        LOGGER.info("Start invoke X CMCC MONITOR：{}", inform);
        monitorService.reportMonitor(inform);
        LOGGER.info("End invoke X CMCC MONITOR!");

    }

    @Override
    public void xCmccBind(Inform inform) {
        LOGGER.info("开始处理{}事件", Event.XCMCCBIND);
        // 1)遍历取出Password的值
        String password = StringUtils.isBlank(getValueByKey(ParameterList.Password, inform))?getValueByKey("InternetGatewayDevice.X_CMCC_UserInfo.UserName", inform):getValueByKey(ParameterList.Password, inform);
        // 2)判断Password是否为空
        if (StringUtils.isBlank(password)) {
            password = getValueByKey("InternetGatewayDevice.X_CMCC_UserInfo.UserId", inform);
            if (StringUtils.isBlank(password)) {
                return;
            }
        }
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewaySerialnumber(inform.getDeviceId().getSerialNubmer());
        gatewayInfo.setGatewayFactoryCode(inform.getDeviceId().getOui());
        // 根据 SN 和 OUI查询网关信息
        GatewayInfo selectGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
        if(selectGatewayInfo == null){
            return;
        }
        /*if(null != selectGatewayInfo.getGatewayPassword()&&!"".equals(selectGatewayInfo.getGatewayPassword())){
            if(!password.equals(selectGatewayInfo.getGatewayPassword())){
                return;
            }
        }else{
            //先判断password是否已经被使用
            GatewayInfo gatewayInfoP = new GatewayInfo();
            gatewayInfoP.setGatewayPassword(password);
            // 根据 password查询是否存在网关信息
            GatewayInfo resultGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfoP);
            if(null != resultGatewayInfo){//password已经被使用
                return ;
            }

            GatewayPassword gatewayPasswordBean = gatewayPasswordMapper.selectByPrimaryKey(password);
            //如果gatewayPassword为空，则password认证失败
            if(gatewayPasswordBean == null){
                return ;
            }
        }*/
        //更新网关为已绑定，放在acs调用checkpassword去做
       /* GatewayInfo updateStatus = new GatewayInfo();
        updateStatus.setGatewayPassword(password);
        updateStatus.setGatewayUuid(selectGatewayInfo.getGatewayUuid());
        updateStatus.setGatewayStatus("已绑定");
        gatewayInfoService.updateSelectGatewayInfo(updateStatus);*/

        //将password同步给BMS
        Map<String,Object> syncMap = new HashMap<>();
        syncMap.put("gatewayInfoMacaddress",selectGatewayInfo.getGatewayMacaddress());
        syncMap.put("passWord",password);
        if(StringUtils.isNotEmpty(selectGatewayInfo.getGatewayAreaId())){
        	syncMap.put("gatewayInfoAreaId",selectGatewayInfo.getGatewayAreaId());
        }
        if(StringUtils.isNotEmpty(selectGatewayInfo.getGatewayFirmwareUuid())){
        	FirmwareInfo fi = firewareInfoMapper.selectByPrimaryKey(selectGatewayInfo.getGatewayFirmwareUuid());
        	if(fi != null){
        		syncMap.put("firmwareVer", fi.getFirmwareVersion());
        	}
        }
        LOGGER.info("开始调用pluginDeviceService.updatePluginLeadGatewayData通知BMS，请求数据为{}", syncMap);
        Map<String,Object> retmap = pluginDeviceService.updatePluginLeadGatewayData(syncMap);
        LOGGER.info("结束调用pluginDeviceService.updatePluginLeadGatewayData通知BMS，返回结果为{}", retmap);
        //update by panmingguo 2016/7/25 begin
       /* if(null != selectGatewayInfo)
        {
            //1.用户恢复出厂设置   2.首次认证   3.固件升级    4.工单拆机恢复出厂设置
            gatewayHandleService.factoryNotify(selectGatewayInfo.getGatewayMacaddress(), "2", false);
        }*/
        //update by panmingguo 2016/7/25 end

        //查询流量任务
//        gatewayFlowrateTaskService.executeTask(inform.getDeviceId().getSerialNubmer(),inform.getDeviceId().getOui(), 1);

        LOGGER.info("结束处理{}事件", Event.XCMCCBIND);
    }

    /**
     * 更新区域和状态
     * @param inform
     */
    private void updateAreaAndStatus(Inform inform)
    {
        // 1)查询网关基础信息
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewayFactoryCode(inform.getDeviceId().getOui());
        gatewayInfo.setGatewaySerialnumber(inform.getDeviceId().getSerialNubmer());
        GatewayInfo selectGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
        if (selectGatewayInfo == null) return;


        // 2)修改信息 (表: t_gateway_info 字段[gateway_status]）
        GatewayInfo updateStatus = new GatewayInfo();
        // 1)遍历取出Ip的值
        String url = getValueByKey("InternetGatewayDevice.ManagementServer.ConnectionRequestURL", inform);
        int i = url.indexOf("//") + 2;
        int j = url.indexOf(":", 10);
        String ip = url.substring(i, j);
        Map<String, Object> fwMap = new HashMap<>();
        fwMap.put("ip", ip);
        Map<String, Object> findGateWayAreaMap = areaService.findGateWayArea(fwMap);
        if (findGateWayAreaMap.containsKey("areaId")) {
            String gatewayAreaId = String.valueOf(findGateWayAreaMap.get("areaId"));
            updateStatus.setGatewayAreaId(gatewayAreaId);
        }
        //设置状态
        updateStatus.setGatewayUuid(selectGatewayInfo.getGatewayUuid());
        //防止将已绑定状态改为已注册状态
        if(!"已绑定".equals(selectGatewayInfo.getGatewayStatus())) {
            updateStatus.setGatewayStatus("已认证");
        }
        // 最近连接时间
        long timeMillis = System.currentTimeMillis();
        long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
        updateStatus.setGatewayJoinTime((int) timeSeconds);
        updateStatus.setGatewayLastConnTime((int) timeSeconds);
        gatewayInfoService.updateSelectGatewayInfo(updateStatus);
    }


    private String getValueByKey(String key, Inform inform) {
        String value = null;
        ParameterList parameterList = inform.getParameterList();
        List<ParameterValueStruct> parameterValueStructList = parameterList.getParameterValueStructs();
        if (parameterValueStructList != null && parameterValueStructList.size() > 0) {
            for (ParameterValueStruct valueStruct : parameterValueStructList) {
                if (valueStruct.getName().equalsIgnoreCase(key)) {
                    value = (String) valueStruct.getValue();
                    break;
                }
            }
        }
        return value;
    }
}
