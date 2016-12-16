package com.cmiot.rms.services.outerservice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.facade.OperationCpeFacade;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.AddObjectResponse;
import com.cmiot.acs.model.DeleteObjectResponse;
import com.cmiot.acs.model.DownloadResponse;
import com.cmiot.acs.model.FactoryResetResponse;
import com.cmiot.acs.model.Fault;
import com.cmiot.acs.model.GetParameterAttributesResponse;
import com.cmiot.acs.model.GetParameterNamesResponse;
import com.cmiot.acs.model.GetParameterValuesResponse;
import com.cmiot.acs.model.GetRPCMethodsResponse;
import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.SetParameterAttributesResponse;
import com.cmiot.acs.model.SetParameterValues;
import com.cmiot.acs.model.SetParameterValuesResponse;
import com.cmiot.acs.model.TransferComplete;
import com.cmiot.acs.model.UploadResponse;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.acs.model.struct.ParameterValueStructInt;
import com.cmiot.acs.model.struct.ParameterValueStructStr;
import com.cmiot.ams.domain.Area;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.cache.TemporaryObject;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.enums.InstructionsStatusEnum;
import com.cmiot.rms.common.enums.LogTypeEnum;
import com.cmiot.rms.common.enums.RebootEnum;
import com.cmiot.rms.common.enums.ReqStatusEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.enums.UpgradeTaskDetailStatusEnum;
import com.cmiot.rms.common.enums.UpgradeTaskEventEnum;
import com.cmiot.rms.common.logback.LogBackRecord;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.GatewayAdslAccountMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessExecuteHistoryMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessOpenDetailMapper;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.mapper.GatewayNodeMapper;
import com.cmiot.rms.dao.mapper.GatewayPasswordMapper;
import com.cmiot.rms.dao.mapper.WorkOrderTemplateInfoMapper;
import com.cmiot.rms.dao.model.FirmwareUpgradeTaskDetail;
import com.cmiot.rms.dao.model.GatewayAdslAccount;
import com.cmiot.rms.dao.model.GatewayBusiness;
import com.cmiot.rms.dao.model.GatewayBusinessExecuteHistory;
import com.cmiot.rms.dao.model.GatewayBusinessOpenDetail;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.GatewayNode;
import com.cmiot.rms.dao.model.GatewayNodeExample;
import com.cmiot.rms.dao.model.GatewayNodeExample.Criteria;
import com.cmiot.rms.dao.model.GatewayPassword;
import com.cmiot.rms.dao.model.InstructionsInfoWithBLOBs;
import com.cmiot.rms.services.BatchSetTaskTrrigerService;
import com.cmiot.rms.services.FirmwareUpgradeTaskDetailService;
import com.cmiot.rms.services.GatewayBackupFileTaskInnerService;
import com.cmiot.rms.services.GatewayFlowrateTaskService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.SyncInfoToFirstLevelPlatformService;
import com.cmiot.rms.services.instruction.AbstractInstruction;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.instruction.impl.SetParameterValuesInstruction;
import com.cmiot.rms.services.outerservice.RequestMgrService;
import com.cmiot.rms.services.report.InformParse;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.InstructionUtil;
import com.cmiot.rms.services.workorder.impl.BusiOperation;
import com.tydic.inter.app.service.GatewayHandleService;

/**
 * Created by wangzhen on 2016/1/29.
 */
@Service("requestMgrService")
public class RequestMgrServiceImpl implements RequestMgrService {

    private static Logger logger = LoggerFactory.getLogger(RequestMgrServiceImpl.class);

    @Autowired
    private GatewayInfoService gatewayInfoService;

    @Autowired
    private InstructionsService instructionsService;

    @Autowired
    private FirmwareUpgradeTaskDetailService firmwareUpgradeTaskDetailService;

    @Autowired
    InstructionMethodService instructionMethodService;

    @Autowired
    InformParse informParse;

    @Autowired
    private GatewayHandleService gatewayHandleService;
    
    @Autowired
    SyncInfoToFirstLevelPlatformService syncInfoToFirstLevelPlatformService;

    @Autowired
    GatewayPasswordMapper gatewayPasswordMapper;
    
    @Autowired
    private GatewayNodeMapper gatewayNodeMapper;


    @Autowired
    private GatewayAdslAccountMapper gatewayAdslAccountMapper;

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
    OperationCpeFacade operationCpeFacade;

    @Autowired
    private GatewayBackupFileTaskInnerService gatewayBackupFileTaskInnerService;

    @Autowired
    private  BatchSetTaskTrrigerService batchSetTaskTrrigerService;

    @Autowired
    private GatewayFlowrateTaskService gatewayFlowrateTaskService;

    @Autowired
    private com.cmiot.ams.service.AreaService areaService;

    @Autowired
    private RedisClientTemplate redisClientTemplate;
    
    @Autowired
    private com.tydic.service.PluginDeviceService pluginDeviceService;
    
    @Value("${instructionSendTimeOut}")
    int instructionSendTimeOut;

    @Value("${first.report.flow.lock.time}")
    int firstReportLockTime;
    
    /**
     * 由ACS发起，用于对一个特定的多实例对象创建一个新的实例
     *
     * @param jsonObject
     */
    @Override
    public Map<String, Object> addObject(JSONObject jsonObject) {
        logger.info("传入AddObjectResponse对象参数：" + jsonObject);

        AddObjectResponse addObjectResponse = JSON.toJavaObject(jsonObject, AddObjectResponse.class);
        String requestId = addObjectResponse.getRequestId();
        int status = InstructionsStatusEnum.STATUS_1.code();
        if (StringUtils.equals(String.valueOf(addObjectResponse.getStatus()), ReqStatusEnum.Req_Status_1.code())) {
            status = InstructionsStatusEnum.STATUS_2.code();
        }
        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "AddObjectResponse", "创建对象");
    }

    /**
     * 由ACS发起，用于删除一个对象的特定实例
     *
     * @param jsonObject
     */
    @Override
    public Map<String, Object> deleteObject(JSONObject jsonObject) {
        logger.info("传入DeleteObjectResponse对象参数：" + jsonObject);
        DeleteObjectResponse deleteObjectResponse = JSON.toJavaObject(jsonObject, DeleteObjectResponse.class);
        String requestId = deleteObjectResponse.getRequestId();
        int status = InstructionsStatusEnum.STATUS_1.code();
        if (StringUtils.equals(String.valueOf(deleteObjectResponse.getStatus()), ReqStatusEnum.Req_Status_1.code())) {
            status = InstructionsStatusEnum.STATUS_2.code();
        }
        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "DeleteObjectResponse", "删除对象");
    }

    /**
     * 由ACS发起，用于要求CPE终端在指定的位置下载指定的文件
     *
     * @param jsonObject
     */
    @Override
    public Map<String, Object> download(JSONObject jsonObject) {
        logger.info("传入DownloadResponse对象参数：" + jsonObject);
        DownloadResponse downloadResponse = JSON.toJavaObject(jsonObject, DownloadResponse.class);
        String requestId = downloadResponse.getRequestId();
        String taskId= instructionsService.getBeforeContent(requestId, "taskId");
        if (StringUtils.equals(String.valueOf(downloadResponse.getStatus()), ReqStatusEnum.Req_Status_0.code())) {
            int status = InstructionsStatusEnum.STATUS_1.code();
            if(org.apache.commons.lang.StringUtils.isNotBlank(taskId))
            {
                logger.info("TransferComplete download requestId:{}, taskId:{}, Status:{}：", requestId, taskId, UpgradeTaskDetailStatusEnum.SUCSSESS.code());
                updateTaskDetail(taskId, requestId, UpgradeTaskDetailStatusEnum.SUCSSESS.code());

                //同步升级任务完成到杭研，调用删除升级任务
                Map<String, String> map = instructionsService.getInstructionsInfo(requestId);
                String gatewayId = map.get("cpeIdentity");
                syncInfoToFirstLevelPlat(taskId,gatewayId);
            }
            return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "DownloadResponse", "下载文件");
        } else {
            Map<String, Object> result = new HashMap<>();
            if (StringUtils.equals(String.valueOf(downloadResponse.getStatus()), ReqStatusEnum.Req_Status_1.code())) {
                //等待网关 transferComplete 方法
                result.put("code", RespCodeEnum.RC_1000.code());
                result.put("message", "等待transferComplete来确认升级是否成功");
                logger.info(JSON.toJSONString(result));
                return result;
            } else {
                result.put("code", RespCodeEnum.RC_1004.code());
                result.put("message", "Download指令status是未知状态");
                logger.info(JSON.toJSONString(result));
                return result;
            }
        }
    }

    @Override
    public Map<String, Object> transferComplete(JSONObject jsonObject) {
        logger.info("传入TransferComplete对象参数：" + jsonObject);
        TransferComplete transferComplete = JSON.toJavaObject(jsonObject, TransferComplete.class);
        String requestId = transferComplete.getCommandKey();//获取下载指令ID
        String taskId= instructionsService.getBeforeContent(requestId, "taskId");
        logger.info("TransferComplete requestId:{}, taskId:{}：", requestId, taskId);
        int status;
        if (transferComplete.getFaultStruct() == null || transferComplete.getFaultStruct().getFaultCode() == 0) {
            status = InstructionsStatusEnum.STATUS_1.code();
            if(org.apache.commons.lang.StringUtils.isNotBlank(taskId))//说明是升级任务执行的download指令，需要更新升级任务结果
            {
                logger.info("TransferComplete updateTaskDetail requestId{}, taskId{}, Status{}：", requestId, taskId, UpgradeTaskDetailStatusEnum.SUCSSESS.code());
                updateTaskDetail(taskId, requestId, UpgradeTaskDetailStatusEnum.SUCSSESS.code());
            }
        } else {
            status = InstructionsStatusEnum.STATUS_2.code();
            if(org.apache.commons.lang.StringUtils.isNotBlank(taskId))//说明是升级任务执行的download指令，需要更新升级任务结果
            {
                logger.info("TransferComplete updateTaskDetail requestId:{}, taskId:{}, Status:{}：", requestId, taskId, UpgradeTaskDetailStatusEnum.FAILURE.code());
                updateTaskDetail(taskId, requestId, UpgradeTaskDetailStatusEnum.FAILURE.code());
            }
            logger.info("失败状态码status:" + transferComplete.getFaultStruct().getFaultCode());
        }
        

        Map<String, String> map = instructionsService.getInstructionsInfo(requestId);
        String gatewayId = map.get("cpeIdentity");

        //同步升级任务完成到杭研，调用删除升级任务
        syncInfoToFirstLevelPlat(taskId ,gatewayId);

        //通过taskid来判断是否是固件升级事件，若是，就进行后续指令下发
        excuteFirmwareTaskJudge(taskId, gatewayId);

        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "TransferComplete", "下载完成");
    }



    private void syncInfoToFirstLevelPlat(String taskId, String gatewayId)
    {
        //同步升级任务完成到杭研，调用删除升级任务
        if(org.apache.commons.lang.StringUtils.isNotBlank(taskId))//说明是升级任务执行的download指令，需要把结果同步到一级平台
        {
            Map<String, Object> reportMap = new HashMap<String, Object>();
            reportMap.put("RPCMethod", "Report");
            reportMap.put("ID", (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
            reportMap.put("CmdType", "REPORT_DELETE_UPGRADE_PLAN");
            reportMap.put(Constant.SEQUENCEID, InstructionUtil.generate8HexString());
            Map<String, Object> map1 = new HashMap<String, Object>();
            map1.put("PlanId", taskId);
        	List<String> gatewayList = new ArrayList<String>();
        	GatewayInfo gw = gatewayInfoService.selectByUuid(gatewayId);
        	if(gw != null){
        		gatewayList.add(gw.getGatewayMacaddress());
        	}
        	map1.put("GatewayList", gatewayList);
            reportMap.put("Parameter", map1);
            syncInfoToFirstLevelPlatformService.report("reportDeleteUpgradePlan",reportMap);
        }
    }

    private void excuteFirmwareTaskJudge(String taskId, String gatewayId) {
    	if(org.springframework.util.StringUtils.isEmpty(taskId) || org.springframework.util.StringUtils.isEmpty(gatewayId)){
    		return;
    	}
    	FirmwareUpgradeTaskDetail queryDetail = new FirmwareUpgradeTaskDetail();
        queryDetail.setGatewayId(gatewayId);
        queryDetail.setUpgradeTaskId(taskId);
        List<FirmwareUpgradeTaskDetail> details = firmwareUpgradeTaskDetailService.queryList(queryDetail);
        if (null == details || details.size() <= 0) {
            return;
        }
        GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayId);
        if(org.springframework.util.StringUtils.isEmpty(gatewayInfo.getGatewayDigestPassword()) 
        		|| org.springframework.util.StringUtils.isEmpty(gatewayInfo.getGatewayConnectionrequestUsername()) 
        		|| org.springframework.util.StringUtils.isEmpty(gatewayInfo.getGatewayConnectionrequestPassword()) 
        		|| org.springframework.util.StringUtils.isEmpty(gatewayInfo.getGatewayFamilyPassword()) 
        		|| org.springframework.util.StringUtils.isEmpty(gatewayInfo.getGatewayDigestAccount())){
        	//3.获取digest账号，家庭网关维护账号指令

			String username = "cpe" + RandomStringUtils.randomAlphanumeric(8);
	        String password = "cpe" + RandomStringUtils.randomAlphanumeric(8);
	        String connectionRequestUsername = "RMS" + RandomStringUtils.randomAlphanumeric(8);
	        String connectionRequestPassword = "RMS" + RandomStringUtils.randomAlphanumeric(8);
	        String familyPassword = "CMCCAdmin" + RandomStringUtils.randomAlphanumeric(8);
	        List<ParameterValueStruct> structs = new ArrayList<>();
	        structs.add(new ParameterValueStructStr("InternetGatewayDevice.ManagementServer.Username", username));
	        structs.add(new ParameterValueStructStr("InternetGatewayDevice.ManagementServer.Password", password));
	        structs.add(new ParameterValueStructStr("InternetGatewayDevice.ManagementServer.ConnectionRequestUsername", connectionRequestUsername));
	        structs.add(new ParameterValueStructStr("InternetGatewayDevice.ManagementServer.ConnectionRequestPassword", connectionRequestPassword));
	        
//	        structs.add(new ParameterValueStructInt("InternetGatewayDevice.X_CMCC_UserInfo.Status", 0));
	        structs.add(new ParameterValueStructInt("InternetGatewayDevice.X_CMCC_UserInfo.Result", 0));
	        
	        GatewayNodeExample node = new GatewayNodeExample();
	        GatewayNodeExample.Criteria criteria = node.createCriteria();
	        criteria.andFactoryCodeEqualTo(gatewayInfo.getGatewayFactoryCode());
	        criteria.andHdVersionEqualTo(gatewayInfo.getGatewayHardwareVersion());
	        criteria.andFirmwareVersionEqualTo(gatewayInfo.getGatewayVersion());
	        List<GatewayNode> nodeList= gatewayNodeMapper.selectByExample(node);
	        if (!nodeList.isEmpty()&&org.apache.commons.lang.StringUtils.isNotBlank(nodeList.get(0).getLoginPasswordNode())) {
	        	logger.info("适配节点路径:"+nodeList.get(0).getLoginPasswordNode());
	           structs.add(new ParameterValueStructStr(nodeList.get(0).getLoginPasswordNode(), familyPassword));
	        }else {
	        	structs.add(new ParameterValueStructStr("InternetGatewayDevice.DeviceInfo.X_CMCC_TeleComAccount.Password", familyPassword));
	        }
	        
	        
	       excuteMessage(gatewayInfo, structs, "bootStrap_"+ InstructionUtil.generate16Uuid());
            /*int _status = getStatus(insMap.get("status"));
            if (_status == InstructionsStatusEnum.STATUS_1.code())
            {
            	//4.获取设置业务下发结果指令
            	List<ParameterValueStruct> _structs = new ArrayList<ParameterValueStruct>();
            	_structs.add(new ParameterValueStructInt("InternetGatewayDevice.X_CMCC_UserInfo.Result", 1));

                excuteMessage(gatewayInfo, _structs, UniqueUtil.uuid());
            }*/
            
        }
	}

	/**
     * 更新升级任务中任务详情的状态和网关的固件版本
     *
     * @param taskId
     * @param requestId
     * @param status
     */
    private void updateTaskDetail(String taskId, String requestId, int status) {
        logger.info("updateTaskDetail taskId:{}, requestId:{}, status:{}", taskId, requestId, status);
        Map<String, String> map = instructionsService.getInstructionsInfo(requestId);
        String gatewayId = map.get("cpeIdentity");
        FirmwareUpgradeTaskDetail queryDetail = new FirmwareUpgradeTaskDetail();
        queryDetail.setGatewayId(gatewayId);
        queryDetail.setUpgradeTaskId(taskId);
        List<FirmwareUpgradeTaskDetail> details = firmwareUpgradeTaskDetailService.queryList(queryDetail);
        logger.info("updateTaskDetail list:{}", details);
        if (null == details || details.size() <= 0) {
            return;
        }

        FirmwareUpgradeTaskDetail updateDetail = details.get(0);
        updateDetail.setStatus(status);

        long timeMillis = System.currentTimeMillis();
        long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
        int upgradeEndTime = (int) timeSeconds;
        updateDetail.setUpgradeEndTime(upgradeEndTime);
        firmwareUpgradeTaskDetailService.updateTaskDetailStatus(updateDetail);

        //升级成功，更新网关的固件ID
        if (status == UpgradeTaskDetailStatusEnum.SUCSSESS.code()) {
            logger.info("updateTaskDetail update GatewayInfo");
            GatewayInfo gatewayInfo = new GatewayInfo();
            gatewayInfo.setGatewayUuid(gatewayId);
            gatewayInfo.setGatewayFirmwareUuid(updateDetail.getFirmwareId());
            gatewayInfo.setGatewayVersion(updateDetail.getFirmwareVersion());
            gatewayInfoService.updateGatewayFirmwareInfo(gatewayInfo);

            //update by panmingguo 2016/7/25 begin
            GatewayInfo resultGateway = gatewayInfoService.selectByUuid(gatewayId);
            //1.用户恢复出厂设置   2.首次认证   3.固件升级    4.工单拆机恢复出厂设置
            gatewayHandleService.factoryNotify(resultGateway.getGatewayMacaddress(), "3" , false);
            //update by panmingguo 2016/7/25 end
        }
    }


    /**
     * 由ACS发起，用于要求特定的CPE终端恢复出厂设置
     *
     * @param jsonObject
     */
    @Override
    public Map<String, Object> factoryReset(JSONObject jsonObject) {
        logger.info("传入FactoryResetResponse对象参数：" + jsonObject);
        FactoryResetResponse factoryResetReq = JSON.toJavaObject(jsonObject, FactoryResetResponse.class);
        String requestId = factoryResetReq.getRequestId();

        Map<String, String> map = instructionsService.getInstructionsInfo(requestId);
        if(null != map && map.size() > 0)
        {
            String gatewayId = map.get("cpeIdentity");
            if(org.apache.commons.lang.StringUtils.isNotBlank(gatewayId))
            {
                GatewayInfo info = new GatewayInfo();
                info.setGatewayUuid(gatewayId);
                info.setGatewayDigestAccount("");
                info.setGatewayDigestPassword("");
                info.setGatewayConnectionrequestUsername("");
                info.setGatewayConnectionrequestPassword("");
                info.setGatewayStatus("");
                gatewayInfoService.updateSelectGatewayInfo(info);
                
            }
        }

        int status = InstructionsStatusEnum.STATUS_1.code();
        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "FactoryResetResponse", "恢复出厂设置");
    }

    /**
     * @param jsonObject
     */
    @Override
    public Map<String, Object> getParameterAttributes(JSONObject jsonObject) {
        logger.info("传入GetParameterAttributesResponse对象参数：" + jsonObject);
        GetParameterAttributesResponse getParameterAttributesResponse = JSON.toJavaObject(jsonObject, GetParameterAttributesResponse.class);
        String requestId = getParameterAttributesResponse.getRequestId();
        int status = InstructionsStatusEnum.STATUS_1.code();
        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "GetParameterAttributesResponse", "查找参数的属性");
    }

    /**
     * 由ACS发起，用于发现CPE上的可访问参数
     *
     * @param jsonObject
     */
    @Override
    public Map<String, Object> getParameterNames(JSONObject jsonObject) {
        logger.info("传入GetParameterNamesResponse对象参数：" + jsonObject);
        GetParameterNamesResponse getParameterNamesResponse = JSON.toJavaObject(jsonObject, GetParameterNamesResponse.class);
        String requestId = getParameterNamesResponse.getRequestId();
        int status = InstructionsStatusEnum.STATUS_1.code();
        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "GetParameterNamesResponse", "发现CPE上的可访问参数");
    }

    /**
     * 由ACS发起，用于查找CPE上一个或者多个参数的值
     *
     * @param jsonObject
     */
    @Override
    public Map<String, Object> getParameterValues(JSONObject jsonObject) {
        logger.info("传入GetParameterValuesResponse对象参数：" + jsonObject);
        GetParameterValuesResponse getParameterValuesResponse = JSON.toJavaObject(jsonObject, GetParameterValuesResponse.class);
        String requestId = getParameterValuesResponse.getRequestId();
        int status = InstructionsStatusEnum.STATUS_1.code();
        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "GetParameterValuesResponse", "查找参数的值");
    }

    /**
     * 由CPE或者是ACS发起，用于发现另一方所支持的方法集
     *
     * @param jsonObject
     */
    @Override
    public Map<String, Object> getRPCMethods(JSONObject jsonObject) {
        logger.info("传入GetRPCMethodsResponse对象参数：" + jsonObject);
        GetRPCMethodsResponse getRPCMethodsResponse = JSON.toJavaObject(jsonObject, GetRPCMethodsResponse.class);
        String requestId = getRPCMethodsResponse.getRequestId();
        int status = InstructionsStatusEnum.STATUS_1.code();
        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "GetRPCMethodsResponse", "发现另一方支持的方法集");
    }

    @Override
    public Map<String, Object> inform(JSONObject jsonObject) {
        logger.info("传入InformReq对象参数：" + jsonObject);
        Inform inform = JSON.toJavaObject(jsonObject, Inform.class);
        gatewayInfoService.updateInformGatewayInfo(inform);
        //解析Inform,根据不同的event完成不同的操作
        informParse.parseInform(inform);

        logger.info("处理上报的网关信息完成");
        Map<String, Object> result = new HashMap<>();
        result.put(Constant.CODE, RespCodeEnum.RC_0.code());
        result.put(Constant.MESSAGE, RespCodeEnum.RC_0.description());
        return result;
    }


    @Override
    public void reboot(JSONObject jsonObject) {
        logger.info("重启设备");
    }

    @Override
    public Map<String, Object> setParameterAttributes(JSONObject jsonObject) {
        logger.info("传入SetParameterAttributesResponse对象参数：" + jsonObject);
        SetParameterAttributesResponse setParameterAttributesResponse = JSON.toJavaObject(jsonObject, SetParameterAttributesResponse.class);
        String requestId = setParameterAttributesResponse.getRequestId();
        int status = InstructionsStatusEnum.STATUS_1.code();
        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "SetParameterAttributesResponse", "设置参数属性");
    }

    /**
     * 由ACS发起，用于修改CPE上一个或者多个参数的值
     *
     * @param jsonObject
     */
    @Override
    public Map<String, Object> setParameterValues(JSONObject jsonObject) {
        logger.info("SetParameterValuesResponse：" + jsonObject);
        SetParameterValuesResponse setParameterValuesResponse = JSON.toJavaObject(jsonObject, SetParameterValuesResponse.class);
        String requestId = setParameterValuesResponse.getRequestId();

        int status = InstructionsStatusEnum.STATUS_1.code();
        if (StringUtils.equals(String.valueOf(setParameterValuesResponse.getStatus()), ReqStatusEnum.Req_Status_1.code())) {
            status = InstructionsStatusEnum.STATUS_2.code();
        }

        //首次上报执行的设置账户密码存在需要更新数据库
        if(org.apache.commons.lang.StringUtils.isNotEmpty(requestId)
                && requestId.startsWith("bootStrap"))
        {
            saveAccountAndPsw(requestId,status);

        }
        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "SetParameterValuesResponse", "修改参数的值");
    }

    /**
     * 由ACS发起，用于要求CPE终端向指定位置上传某一特定文件
     *
     * @param jsonObject
     */
    @Override
    public Map<String, Object> upload(JSONObject jsonObject) {
        logger.info("UploadResponse：" + jsonObject);
        UploadResponse uploadResponse = JSON.toJavaObject(jsonObject, UploadResponse.class);
        String requestId = uploadResponse.getRequestId();
        int status = InstructionsStatusEnum.STATUS_1.code();
        if (StringUtils.equals(String.valueOf(uploadResponse.getStatus()), ReqStatusEnum.Req_Status_1.code())) {
            status = InstructionsStatusEnum.STATUS_2.code();
        }
        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "UploadResponse", "上传文件");
    }


    @Override
    public boolean certification(Map<String, Object> parameter) {
        String gatewayInfoSerialnumber = null != parameter.get("sn") ? parameter.get("sn").toString() : "";
        String gatewayInfoFactoryCode = null != parameter.get("oui") ? parameter.get("oui").toString() : "";
        if (org.apache.commons.lang.StringUtils.isEmpty(gatewayInfoSerialnumber)
                || org.apache.commons.lang.StringUtils.isEmpty(gatewayInfoFactoryCode)) {
            return false;
        }
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewaySerialnumber(gatewayInfoSerialnumber);
        gatewayInfo.setGatewayFactoryCode(gatewayInfoFactoryCode);
        // 根据 SN 和 OUI查询是否存在网关信息
        GatewayInfo resultGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);

        boolean isPass = false;

        if (resultGatewayInfo != null) {
            isPass = true;
        }
        return isPass;
    }

    @Override
    public Map<String, Object> fault(JSONObject jsonObject) {
        logger.info("传入Fault对象参数：" + jsonObject);
        Fault fault = JSON.toJavaObject(jsonObject, Fault.class);
        String requestId = fault.getRequestId();
        int status = InstructionsStatusEnum.STATUS_2.code();
        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "Fault", "下发指令错误");

    }

    @Override
    public boolean checkPassword(Map<String, Object> parameter) {

        String gatewayPassword = parameter.get("gatewayPassword") == null ? "" : parameter.get("gatewayPassword").toString();
        String sn = parameter.get("sn") == null ? "" : parameter.get("sn").toString();
        String oui =  parameter.get("oui") == null ? "" : parameter.get("oui").toString();
        if ("".equals(gatewayPassword) || "".equals(sn) || "".equals(oui)) {
            logger.info("password,sn,oui不能为空，password:{},sn:{},oui:{}",gatewayPassword,sn,oui);
            return false;
        }
        //先通过oui sn 查询网关信息
        GatewayInfo gatewayInfoParam = new GatewayInfo();
        gatewayInfoParam.setGatewaySerialnumber(sn);
        gatewayInfoParam.setGatewayFactoryCode(oui);
        // 根据 SN 和 OUI查询网关信息
        GatewayInfo selectGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfoParam);
        if(selectGatewayInfo == null){
            logger.warn("网关不存在");
            return false;
        }
        if(null == selectGatewayInfo.getGatewayPassword()||"".equals(selectGatewayInfo.getGatewayPassword())){
            //先判断password是否已经被使用
            GatewayInfo gatewayInfo = new GatewayInfo();
            gatewayInfo.setGatewayPassword(gatewayPassword);
            // 根据 password查询是否存在网关信息
            GatewayInfo resultGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
            if(null != resultGatewayInfo){//password已经被使用
                logger.warn("password已经被使用");
                return false;
            }

            GatewayPassword gatewayPasswordBean = gatewayPasswordMapper.selectByPrimaryKey(gatewayPassword);
            //如果gatewayPassword为空，则password认证失败
            if(gatewayPasswordBean == null){
                logger.warn("工单表不存在password");
                return false;
            }else{
                return true;
            }
        }else{
            if(gatewayPassword.equals(selectGatewayInfo.getGatewayPassword())){
                return true;
            }else{
                logger.warn("password与网关表信息不符");
                return false;
            }
        }
    }

    @Override
    public boolean queryDigest(Map<String, Object> parameter) {
        String gatewayInfoSerialnumber = null != parameter.get("sn") ? parameter.get("sn").toString() : "";
        String gatewayInfoFactoryCode = null != parameter.get("oui") ? parameter.get("oui").toString() : "";
        if (org.apache.commons.lang.StringUtils.isEmpty(gatewayInfoSerialnumber)
                || org.apache.commons.lang.StringUtils.isEmpty(gatewayInfoFactoryCode)) {
            return false;
        }
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewaySerialnumber(gatewayInfoSerialnumber);
        gatewayInfo.setGatewayFactoryCode(gatewayInfoFactoryCode);
        // 根据 SN 和 OUI查询是否存在网关信息
        GatewayInfo resultGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
        if (resultGatewayInfo != null) {
            List<ParameterValueStruct> list = new ArrayList<ParameterValueStruct>();
            ParameterValueStruct<String> pvsUserName = new ParameterValueStruct<String>();
            pvsUserName.setName(ConstantDiagnose.MANAGEMENT_SERVER_USERNAME);
            pvsUserName.setValue(resultGatewayInfo.getGatewayDigestAccount());
            pvsUserName.setReadWrite(true);
            list.add(pvsUserName);
            ParameterValueStruct<String> pvsPassword = new ParameterValueStruct<String>();
            pvsPassword.setName(ConstantDiagnose.MANAGEMENT_SERVER_PASSWORD);
            pvsPassword.setValue(resultGatewayInfo.getGatewayDigestPassword());
            pvsUserName.setReadWrite(true);
            list.add(pvsPassword);
            return instructionMethodService.setParameterValue(resultGatewayInfo.getGatewayMacaddress(), list);
        }
        return false;
    }

    /**
     * 根据用户名查询digest密码
     *
     * @param userName
     * @return
     */
    @Override
    public List<String> queryDigestPassword(String userName) {
        return gatewayInfoService.queryDigestPassword(userName);
    }

    /**
     * 根据OUI-SN查询Digest账号和密码
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryDigestAccAndPw(Map<String, Object> parameter) {
        Map<String, Object> retMap = new HashMap<>();
        String gatewayInfoSerialnumber = null != parameter.get("sn") ? parameter.get("sn").toString() : "";
        String gatewayInfoFactoryCode = null != parameter.get("oui") ? parameter.get("oui").toString() : "";
        Boolean isFirst = null != parameter.get("isFirst") ? Boolean.valueOf(parameter.get("isFirst").toString()) : false;
        if (org.apache.commons.lang.StringUtils.isEmpty(gatewayInfoSerialnumber)
                || org.apache.commons.lang.StringUtils.isEmpty(gatewayInfoFactoryCode)) {
            retMap.put(Constant.CODE, -1);
            retMap.put(Constant.MESSAGE, "参数为空");
            return retMap;
        }
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewaySerialnumber(gatewayInfoSerialnumber);
        gatewayInfo.setGatewayFactoryCode(gatewayInfoFactoryCode);
        // 根据 SN 和 OUI查询是否存在网关信息
        GatewayInfo resultGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
        if (resultGatewayInfo == null) {
            retMap.put(Constant.CODE, -1);
            retMap.put(Constant.MESSAGE, "网关不存在");
            return retMap;
        }
        else
        {
            retMap.put(Constant.CODE, 0);
            //首次上报并digest账号和密码在数据库中为空
            if(isFirst
                    && (org.apache.commons.lang.StringUtils.isBlank(resultGatewayInfo.getGatewayDigestAccount())
                    && org.apache.commons.lang.StringUtils.isBlank(resultGatewayInfo.getGatewayDigestPassword())))
            {
                retMap.put("userName", "cpe");
                retMap.put("password", "cpe");
            }
            else
            {
                retMap.put("userName", resultGatewayInfo.getGatewayDigestAccount());
                retMap.put("password", resultGatewayInfo.getGatewayDigestPassword());
            }
        }
        return retMap;
    }


    /**
     * 保存账号和密码到数据库
     * @param requestId
     */
    private void saveAccountAndPsw(String requestId,int status)
    {
        logger.info("enter saveAccountAndPsw:{},{}", requestId, status);
        Map<String, String> map = instructionsService.getInstructionsInfo(requestId);
        String gatewayId = map.get("cpeIdentity");

        logger.info("saveAccountAndPsw gatewayId: {}", gatewayId);

        if(org.apache.commons.lang.StringUtils.isBlank(gatewayId))
        {
            return;
        }
        GatewayInfo selectGatewayInfo = gatewayInfoService.selectByUuid(gatewayId);
        if(null == selectGatewayInfo)
        {
            logger.info("saveAccountAndPsw  {} not exist!", gatewayId);
            return;
        }

        if(status == InstructionsStatusEnum.STATUS_1.code()) {
            String beforeContent = map.get("beforeContent");
            if (org.apache.commons.lang.StringUtils.isEmpty(beforeContent)) {
                return;
            }

            SetParameterValues parameterValues = JSON.toJavaObject(JSON.parseObject(beforeContent), SetParameterValues.class);
            if (null == parameterValues) {
                return;
            }

            GatewayInfo updateInfo = new GatewayInfo();
            updateInfo.setGatewayUuid(selectGatewayInfo.getGatewayUuid());

            List<ParameterValueStruct> structs = parameterValues.getParameterList().getParameterValueStructs();
            for (ParameterValueStruct struct : structs) {
                if (struct.getName().equals("InternetGatewayDevice.ManagementServer.Username")) {
                    updateInfo.setGatewayDigestAccount(String.valueOf(struct.getValue()));
                } else if (struct.getName().equals("InternetGatewayDevice.ManagementServer.Password")) {
                    updateInfo.setGatewayDigestPassword(String.valueOf(struct.getValue()));
                } else if (struct.getName().equals("InternetGatewayDevice.ManagementServer.ConnectionRequestUsername")) {
                    updateInfo.setGatewayConnectionrequestUsername(String.valueOf(struct.getValue()));
                } else if (struct.getName().equals("InternetGatewayDevice.ManagementServer.ConnectionRequestPassword")) {
                    updateInfo.setGatewayConnectionrequestPassword(String.valueOf(struct.getValue()));
                }
                //标准网关
                else if (struct.getName().equals("InternetGatewayDevice.DeviceInfo.X_CMCC_TeleComAccount.Password")) {
                    updateInfo.setGatewayFamilyAccount("CMCCAdmin");
                    updateInfo.setGatewayFamilyPassword(String.valueOf(struct.getValue()));
                } else {
                    //适配非标准网关登录节点
                    GatewayNodeExample node = new GatewayNodeExample();
                    Criteria criteria = node.createCriteria();
                    criteria.andFactoryCodeEqualTo(selectGatewayInfo.getGatewayFactoryCode());
                    criteria.andHdVersionEqualTo(selectGatewayInfo.getGatewayHardwareVersion());
                    criteria.andFirmwareVersionEqualTo(selectGatewayInfo.getGatewayVersion());
                    List<GatewayNode> nodeList = gatewayNodeMapper.selectByExample(node);
                    if (!nodeList.isEmpty()) {
                        if (struct.getName().equals(nodeList.get(0).getLoginPasswordNode())) {
                            updateInfo.setGatewayFamilyAccount("CMCCAdmin");
                            updateInfo.setGatewayFamilyPassword(String.valueOf(struct.getValue()));
                        }
                    }
                }
            }
            logger.info("saveAccountAndPsw  updateSelectGatewayInfo:{} ", updateInfo);
            gatewayInfoService.updateSelectGatewayInfo(updateInfo);

            //设置指令执行结果
            List<ParameterValueStruct> setParameterNames = new ArrayList<>();
            setParameterNames.add(new ParameterValueStructInt("InternetGatewayDevice.X_CMCC_UserInfo.Result", 1));
            Boolean isSuccess = instructionMethodService.setParameterValue(selectGatewayInfo.getGatewayMacaddress(), setParameterNames);
            logger.info("saveAccountAndPsw isSuccess:" + isSuccess);
            if(isSuccess) {
                //开始执行工单
                if (isSuccess && selectGatewayInfo.getGatewayStatus().equals("已绑定")) {
                    this.excuteWorkOrder(selectGatewayInfo);
                }

                //前面执行多步，网关信息被更新了，此处在数据库取最新的网关信息
                GatewayInfo selectGatewayInfoNew = gatewayInfoService.selectByUuid(gatewayId);
                //同步数据到一级家开平台
                syncInfoToFirstLevelPlatform(selectGatewayInfoNew);

                //通知BMS
                String bootState = redisClientTemplate.get("R-F-" + selectGatewayInfoNew.getGatewaySerialnumber());
                if (null != bootState && RebootEnum.STATUS_1.code().equals(bootState)) {
                    //1.用户恢复出厂设置   2.首次认证   3.固件升级    4.工单拆机恢复出厂设置
                    String opType = "1";
                    //工单拆机恢复出厂
                    if (1 == redisClientTemplate.del("WORKORDER-R-F-" + selectGatewayInfoNew.getGatewaySerialnumber())) {
                        opType = "4";
                    }
                    //通知BMS
                    Map<String, Object> retmap = gatewayHandleService.factoryNotify(selectGatewayInfoNew.getGatewayMacaddress(), opType, false);
                   /* if (null != retmap && 0 == Integer.valueOf(retmap.get("resultCode").toString())) {
                        logger.info("网关{}恢复出厂，通知到BMS成功", selectGatewayInfoNew.getGatewayMacaddress());
                    } else {
                        logger.info("网关{}恢复出厂，通知到BMS失败", selectGatewayInfoNew.getGatewayMacaddress());
                    }*/
                    logger.info("网关{}恢复出厂通知BMS结果,由于不需要等待BMS的处理，所以这里想当时异步处理，所以执行结果为空",selectGatewayInfoNew.getGatewayMacaddress());
                    //删除redis锁
                    redisClientTemplate.del("R-F-" + selectGatewayInfoNew.getGatewaySerialnumber());
                }
                //每次都同步到BMS，BMS更新预置插件列表
                try {
					gatewayHandleService.factoryNotify(selectGatewayInfo.getGatewayMacaddress(), "2", false);
				} catch (Exception e) {
				}

                //执行第一次启动需要执行的任务
                // 备份
                logger.info("start invoke executeUpgradeTask");
                boolean isBackupTask = gatewayBackupFileTaskInnerService.executeUpgradeTask(selectGatewayInfo.getGatewaySerialnumber(),
                        selectGatewayInfo.getGatewayFactoryCode(),
                        UpgradeTaskEventEnum.BOOTSTRAP);

                logger.info("end invoke executeUpgradeTask isBackupTask：{}", isBackupTask);
                //设置参数
                logger.info("start invoke batchSetTaskTrriger");
                batchSetTaskTrrigerService.batchSetTaskTrriger(isBackupTask, selectGatewayInfo.getGatewaySerialnumber(),
                        selectGatewayInfo.getGatewayFactoryCode(),
                        UpgradeTaskEventEnum.BOOTSTRAP);
                logger.info("end invoke batchSetTaskTrriger");
                //查询流量任务
                logger.info("start invoke gatewayFlowrateTaskService.executeTask");
                gatewayFlowrateTaskService.executeTask(selectGatewayInfo.getGatewaySerialnumber(),
                        selectGatewayInfo.getGatewayFactoryCode(), 1);
                logger.info("end invoke gatewayFlowrateTaskService.executeTask");
            }

        }else {
            setUserInfoResultInstruction(selectGatewayInfo, 2);
        }
        //执行完了，对流程解锁
        String FirstReportKey = "FirstReport" + selectGatewayInfo.getGatewaySerialnumber();
        redisClientTemplate.del(FirstReportKey);
        logger.info("执行完了，对流程解锁，{}",FirstReportKey);
    }

    private int getStatus(Object obj)
    {
        int result = 2;
        try {
            if(null != obj)
            {
                result = Integer.valueOf(obj.toString());
            }
        }
        catch (Exception e)
        {
            result = 2;
        }
        return result;
    }

    private Map<String,Object> excuteMessage(GatewayInfo gatewayInfo, List<ParameterValueStruct> structs, String insId){
        Map<String, Object> map = new HashMap<>();
        map.put("pvList", structs);

        InstructionsInfoWithBLOBs is = new InstructionsInfoWithBLOBs();
        //生成指令ID作为请求的requestId
        is.setInstructionsId(insId);
        is.setCpeIdentity(gatewayInfo.getGatewayUuid());
        map.put("requestId", insId);
        AbstractInstruction ins = new SetParameterValuesInstruction();
        AbstractMethod abstractMethod = ins.createIns(is, gatewayInfo, map);

        if(!org.springframework.util.StringUtils.isEmpty(gatewayInfo.getGatewayConnectionrequestUsername())
                && !org.springframework.util.StringUtils.isEmpty(gatewayInfo.getGatewayConnectionrequestPassword()))
        {
            abstractMethod.setCpeUserName(gatewayInfo.getGatewayConnectionrequestUsername());
            abstractMethod.setCpePassword(gatewayInfo.getGatewayConnectionrequestPassword());
        }

        is.setInstructionsBeforeContent(JSON.toJSONString(abstractMethod));


        instructionsService.addInstructionsInfo(is);

        Map<String, Object> returnMap ;
        logger.info("发送json到acs 指令id:{} 网关id：{} json:{}", insId, gatewayInfo.getGatewayUuid(), JSON.toJSONString(abstractMethod) );

        //为指令添加临时对象锁，等待指令异步返回
        TemporaryObject temporaryObject = new TemporaryObject(insId);
        RequestCache.set(insId, temporaryObject);

        List<AbstractMethod> abstractMethods = new ArrayList<>();
        abstractMethods.add(abstractMethod);
        returnMap = operationCpeFacade.doACSEMethods(abstractMethods);

        /*logger.info("Start wait: {}", temporaryObject.getRequestId());
        synchronized (temporaryObject) {

            returnMap = operationCpeFacade.doACSEMethods(abstractMethods);
            returnMap.put("requestId", insId);
            logger.info("连接ACS返回结果：" + returnMap);

            try {
                temporaryObject.wait(instructionSendTimeOut);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }

        }
        logger.info("End wait: {}", temporaryObject.getRequestId());
        RequestCache.delete(insId);
        Map<String, String> insMap = instructionsService.getInstructionsInfo(insId);*/
        // 记录接口调用日志
        LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(abstractMethod), insId, gatewayInfo.getGatewayUuid(), "SetParameterValues",LogTypeEnum.LOG_TYPE_SYSTEM.description());
        return returnMap;
    }

    /**
     * 执行工单的方法抽离
     * @param selectGatewayInfo
     */
    public void excuteWorkOrder(GatewayInfo selectGatewayInfo){
        try {
            logger.info("start invoke RequestMgrServiceImpl.excuteWorkOrder,password绑定触发");
            //当是平台主动发起或者工单发起的恢复出厂设置时，不用执行工单
            if(null != redisClientTemplate.get("R-F-" + selectGatewayInfo.getGatewaySerialnumber()) || null != redisClientTemplate.get("WORKORDER-R-F-" + selectGatewayInfo.getGatewaySerialnumber())){
               logger.info("当是平台主动发起或者工单发起的恢复出厂设置时，不用执行工单,{},{}",redisClientTemplate.get("R-F-" + selectGatewayInfo.getGatewaySerialnumber()),redisClientTemplate.get("WORKORDER-R-F-" + selectGatewayInfo.getGatewaySerialnumber()));
               return;
            }
            HashMap<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("gatewayPassword", selectGatewayInfo.getGatewayPassword());
            if(!org.apache.commons.lang.StringUtils.isEmpty(selectGatewayInfo.getGatewayAdslAccount())){
                paramMap.put("adslAccount",selectGatewayInfo.getGatewayAdslAccount());
            }
            List<String> statusList = new ArrayList<>();
            statusList.add("0");//未开通
            statusList.add("2");//开通失败
            statusList.add("1");//开通成功了的（用户发起的恢复出厂设置）
            paramMap.put("businessStatuList", statusList);
            //查询有效的工单，工单按照创建时间正序排序
            List<GatewayBusiness> listBusiness = gatewayBusinessMapper.selectBusinessList(paramMap);
            if (listBusiness == null || listBusiness.isEmpty()){
        		//下发无业务下发指令
                setOneCommand(selectGatewayInfo.getGatewayMacaddress(), "InternetGatewayDevice.X_CMCC_UserInfo.Result", "99");
            }else{
                //把wband工单放到第一位执行
                GatewayBusiness wbandGatewayBusiness = null;
                int wbandLocation = 0;
                boolean isHaveWband = false;
                for(int m = 0;m<listBusiness.size();m++){
                    if("wband".equals(listBusiness.get(m).getBusinessCode())){
                        wbandGatewayBusiness = listBusiness.get(m);
                        wbandLocation = m;
                        isHaveWband = true;
                    }
                }
                if(isHaveWband) {
                    listBusiness.remove(wbandLocation);
                    listBusiness.add(0, wbandGatewayBusiness);
                }
                
                //下发业务条数
                boolean sendNumResult = setOneCommand(selectGatewayInfo.getGatewayMacaddress(), "InternetGatewayDevice.X_CMCC_UserInfo.ServiceNum", String.valueOf(listBusiness.size()));
                if (!sendNumResult) {
                	logger.info("下发工单条数到网关password失败:{}", selectGatewayInfo.getGatewayPassword());
                	return;
    			}
                //遍历执行工单
                for (GatewayBusiness gb : listBusiness) {
                    //如果有开通和目，则直接开通
                    if ("andmu".equals(gb.getBusinessCode())) {
                        gb.setBusinessStatu("1");
                        gatewayBusinessMapper.updateByPrimayKey(gb);
                        //记录开通明细
                        updateBusinessDetaiStatus(gb, selectGatewayInfo.getGatewayUuid(), "1");
                        
                        GatewayBusinessExecuteHistory gheh = new GatewayBusinessExecuteHistory();
                        gheh.setOrderNo(gb.getOrderNo());
                        gheh.setExecuteStatus(1);
                        gheh.setExecuteTime(DateTools.getCurrentSecondTime());
                        //写操作记录
                        gatewayBusinessExecuteHistoryMapper.insert(gheh);

                    } else {
                        //查询模板信息
                        Map<String, Object> paMap = new HashMap<>();
                        paMap.put("businessCode", gb.getBusinessCode());
                        paMap.put("gatewayMacaddress", selectGatewayInfo.getGatewayMacaddress());
                        Map selectResultMap = workOrderTemplateInfoMapper.selectByBusinessCode(paMap);
                        if (null == selectResultMap) {
                            selectResultMap = workOrderTemplateInfoMapper.selectDefaultTemplate(paMap);
                        }
                        if (null != selectResultMap) {
                            String template_message = (String) selectResultMap.get("template_message");
                            if (!org.apache.commons.lang.StringUtils.isEmpty(template_message)) {
                                Map<String, String> resultMap = new HashMap<>();
                                String parameterList = gb.getParameterList();
                                Map<String, Object> parameterMap = JSON.parseObject(parameterList, Map.class);
                                parameterMap.put("gateWayMac", selectGatewayInfo.getGatewayMacaddress());
                                //执行之前把工单状态改为执行中
                                gb.setBusinessStatu("3");
                                gatewayBusinessMapper.updateByPrimayKey(gb);

                                resultMap = busiOperation.excute(template_message, parameterMap);
                                //                            resultMap.put("MSG_CODE", "0");
                                GatewayBusinessExecuteHistory gheh = new GatewayBusinessExecuteHistory();
                                gheh.setOrderNo(gb.getOrderNo());
                                if ("0".equals(resultMap.get("MSG_CODE"))) {
                                    String businessType = gb.getBusinessType();
                                    GatewayInfo updateGatewayInfo = new GatewayInfo();
                                    updateGatewayInfo.setGatewayUuid(selectGatewayInfo.getGatewayUuid());
                                    boolean isNeedUpdate = false;
                                    if (!org.apache.commons.lang.StringUtils.isEmpty(businessType)) {
                                        switch (businessType) {
                                            case "1":
                                                updateGatewayInfo.setBusinessStatus("1");
                                                isNeedUpdate = true;
                                                break;
                                            case "2":
                                                updateGatewayInfo.setBusinessStatus("3");
                                                isNeedUpdate = true;
                                                break;
                                            case "3":
                                                updateGatewayInfo.setBusinessStatus("2");
                                                isNeedUpdate = true;
                                                break;
                                            case "4":
                                                updateGatewayInfo.setBusinessStatus("4");
                                                isNeedUpdate = true;
                                                break;
                                            case "5":
                                                updateGatewayInfo.setBusinessStatus("5");
                                                isNeedUpdate = true;
                                                break;
                                            case "8":
                                                updateGatewayInfo.setBusinessStatus("6");
                                                isNeedUpdate = true;
                                                break;
                                            case "9":
                                                updateGatewayInfo.setBusinessStatus("1");
                                                isNeedUpdate = true;
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                    
                                   //宽带新装
                                   if (gb.getBusinessCode().startsWith("wband")&&("1".equals(gb.getBusinessType())||"9".equals(gb.getBusinessType()))) {
                                	   afterWbandInstall(gb, selectGatewayInfo);
                                   }else if (isNeedUpdate) {
                                	   gatewayInfoMapper.updateByPrimaryKeySelective(updateGatewayInfo);
                                   }
                                    
                                    //宽带、VOIP、OTTTV新装成功后公共处理
	                           		gb.setBusinessStatu("1");
	                           		//这里只有新装/修改 没有拆机  开机、停机等不修改状态
	                           		if ("1".equals(gb.getBusinessType())||"9".equals(gb.getBusinessType())) {
	                           			updateBusinessDetaiStatus(gb, selectGatewayInfo.getGatewayUuid(), "1");
									}
                                    //执行成功
                                    gb.setBusinessStatu("1");
                                    gheh.setExecuteStatus(1);
                                   
                                } else {
                                    //执行失败
                                    gb.setBusinessStatu("2");
                                    if (gb.getFailCount() == null) {
                                        gb.setFailCount(1);
                                    } else {
                                        gb.setFailCount(gb.getFailCount() + 1);
                                    }
                                    gheh.setExecuteStatus(2);
                                    
                                }
                                
                                gheh.setExecuteTime(DateTools.getCurrentSecondTime());
                                //写操作记录
                                gatewayBusinessExecuteHistoryMapper.insert(gheh);
                                //更新工单状态
                                gatewayBusinessMapper.updateByPrimayKey(gb);

                            } else {
                                logger.info("工单的模板类容为空,gateWayPassword:{},businessCode:{}", selectGatewayInfo.getGatewayPassword(), gb.getBusinessCode());
                            }
                        } else {
                            logger.info("工单的模板类容为空,gateWayPassword:{},businessCode:{}", selectGatewayInfo.getGatewayPassword(), gb.getBusinessCode());
                        }
                    }
                }
            } 
        }catch (Exception e){
            logger.error("password认证执行工单报错,Exception:{}", e);
        }
    }

    
    /**
     * @param gatewayBusiness
     * wband新装成功后处理
     */
    private void afterWbandInstall(GatewayBusiness gb,GatewayInfo gatewayInfo){
    	//更新网关的宽带账号
        String pppoeAccount = gb.getAdslAccount();
        String AreaCode = gb.getAreacode();
        Integer area = null;
        if(!org.apache.commons.lang.StringUtils.isEmpty(AreaCode)){
            area = areaService.findIdByCode(AreaCode);
        }
        GatewayInfo updateGatewayInfo = new GatewayInfo();
        updateGatewayInfo.setGatewayUuid(gatewayInfo.getGatewayUuid());
    	updateGatewayInfo.setBusinessStatus("1");
        updateGatewayInfo.setGatewayAdslAccount(pppoeAccount);
        if (null != area) {
        	updateGatewayInfo.setGatewayAreaId(area + "");
		}
        gatewayInfoMapper.updateByPrimaryKeySelective(updateGatewayInfo);
        
        try {
            GatewayAdslAccount adslAccount = new GatewayAdslAccount();
            adslAccount.setAdslAccount(pppoeAccount == null ? "" : pppoeAccount.toString());
            adslAccount.setCreateTime(Integer.valueOf((System.currentTimeMillis() + "").substring(0, 10)));
            adslAccount.setGatewayMAC(gatewayInfo.getGatewayMacaddress());
            String adslUuid = UniqueUtil.uuid();
            adslAccount.setId(adslUuid);
            if (null != area) {
                adslAccount.setAreaId(area);
            }
            gatewayAdslAccountMapper.insert(adslAccount);
        }catch (Exception e){
            logger.error("gatewayAdslAccountMapper.insert error,e:{}" ,e);
        }
        
        if(null != area) {
            try {
				//通知BMS
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("gatewayInfoMacaddress", gatewayInfo.getGatewayMacaddress());
				map.put("gatewayInfoAreaId", area + "");
				Map<String,Object> bmsResult = pluginDeviceService.updatePluginLeadGatewayData(map);
				logger.info("更新区域通知BMS结果,GatewayMacaddress:{},执行结果:{},结果描述:{}", gatewayInfo.getGatewayMacaddress(), bmsResult.get("resultCode"), bmsResult.get("resultMsg"));
			} catch (Exception e) {
				logger.error("更新区域通知BMS异常,e:{}" ,e);
			}
        }
    }
    
    
    /**
     * @param gatewayBusiness
     * 业务详情状态更新
     */
    private void updateBusinessDetaiStatus(GatewayBusiness gatewayBusiness,String gatewayUuid,String status){
    	
    	GatewayBusinessOpenDetail gatewayBusinessOpenDetailParam = new GatewayBusinessOpenDetail();
        gatewayBusinessOpenDetailParam.setBusinessCodeBoss(gatewayBusiness.getBusinessCodeBoss());
        gatewayBusinessOpenDetailParam.setGatewayUuid(gatewayUuid);
        GatewayBusinessOpenDetail gatewayBusinessOpenDetailResult = gatewayBusinessOpenDetailMapper.selectByParam(gatewayBusinessOpenDetailParam);
        if (gatewayBusinessOpenDetailResult == null) {
            GatewayBusinessOpenDetail gatewayBusinessOpenDetail = new GatewayBusinessOpenDetail();
            gatewayBusinessOpenDetail.setBusinessCodeBoss(gatewayBusiness.getBusinessCodeBoss());
            gatewayBusinessOpenDetail.setOrderNo(gatewayBusiness.getOrderNo());
            gatewayBusinessOpenDetail.setGatewayUuid(gatewayUuid);
            gatewayBusinessOpenDetail.setOpenStatus(status);
            gatewayBusinessOpenDetail.setId(UniqueUtil.uuid());
            int n = gatewayBusinessOpenDetailMapper.insert(gatewayBusinessOpenDetail);
            logger.info("invoke newInstallation，写入工单开通工单明细结果,n:{},gatewayBusinessOpenDetail:{}", n, gatewayBusinessOpenDetail);
        } else {
            if (!status.equals(gatewayBusinessOpenDetailResult.getOpenStatus())) {
                gatewayBusinessOpenDetailResult.setOpenStatus(status);
                int n = gatewayBusinessOpenDetailMapper.updateByPrimaryKey(gatewayBusinessOpenDetailResult);
                logger.info("invoke newInstallation，更新工单开通工单明细结果,n:{},gatewayBusinessOpenDetailResult:{}", n, gatewayBusinessOpenDetailResult);
            }
        }
        
    }
    
    /**
     * 获取设置业务下发结果指令
     * @param gatewayInfo
     * @param result 0：表示开始下发业务  1：业务下发成功 2：业务下发失败 99：无业务下发
     * @return
     */
    private void setUserInfoResultInstruction(GatewayInfo gatewayInfo ,int result)
    {
        logger.info("开始设置业务下发结果指令，sn:{},result:{}",gatewayInfo.getGatewaySerialnumber(),result);
        List<ParameterValueStruct> structs = new ArrayList<>();
        structs.add(new ParameterValueStructInt("InternetGatewayDevice.X_CMCC_UserInfo.Result", result));

        Map<String, Object> map = new HashMap<>();
        map.put("pvList", structs);

        InstructionsInfoWithBLOBs is = new InstructionsInfoWithBLOBs();
        //生成指令ID作为请求的requestId
        String insId = UniqueUtil.uuid();
        is.setInstructionsId(insId);
        is.setCpeIdentity(gatewayInfo.getGatewayUuid());
        map.put("requestId", insId);
        AbstractInstruction ins = new SetParameterValuesInstruction();
        AbstractMethod abstractMethod = ins.createIns(is, gatewayInfo, map);

        if(org.apache.commons.lang.StringUtils.isNotBlank(gatewayInfo.getGatewayConnectionrequestUsername())
                && org.apache.commons.lang.StringUtils.isNotBlank(gatewayInfo.getGatewayConnectionrequestPassword()))
        {
            abstractMethod.setCpeUserName(gatewayInfo.getGatewayConnectionrequestUsername());
            abstractMethod.setCpePassword(gatewayInfo.getGatewayConnectionrequestPassword());
        }

        is.setInstructionsBeforeContent(JSON.toJSONString(abstractMethod));
        instructionsService.addInstructionsInfo(is);

        List<AbstractMethod> abstractMethods = new ArrayList<>();
        abstractMethods.add(abstractMethod);
        Map retMap =  operationCpeFacade.doACSEMethods(abstractMethods);
        logger.info("完成调用ACS设置业务下发结果指令，sn:{},ACS返回结果:{}",gatewayInfo.getGatewaySerialnumber(),retMap);
    }

    private void syncInfoToFirstLevelPlatform(GatewayInfo gatewayInfo){
        logger.info("start invoke syncInfoToFirstLevelPlatform");
        try {
            String WanIPAddr = "";
            String HDVersion = gatewayInfo.getGatewayHardwareVersion();
            String SWVersion = gatewayInfo.getGatewayVersion();
            int topAreaId = 0;
            String url = gatewayInfo.getGatewayConnectionrequesturl();
            if (!org.apache.commons.lang.StringUtils.isEmpty(url)) {
                int i = url.indexOf("//") + 2;
                int j = url.indexOf(":", 10);
                WanIPAddr = url.substring(i, j);
               /* Map<String, Object> param = new HashMap<String, Object>();
                param.put("ip", WanIPAddr);
                Map<String, Object> areaMap = areaService.findGateWayArea(param);
                if (areaMap.containsKey("areaId")) {
                    areaId = String.valueOf(areaMap.get("areaId"));
                    com.cmiot.ams.domain.Area area = (com.cmiot.ams.domain.Area) areaMap.get("area");
                    topAreaId = area.getFirst();
                }*/
            }
            List<com.cmiot.ams.domain.Area> areaList = areaService.findAllArea();
            for(Area area : areaList){
            	if(area.getPid() == 0){
            		topAreaId = area.getId();
            		break;
            	}
            }
            
            //上报给杭研
            Map<String, Object> reportMap = new HashMap<String, Object>();
            reportMap.put("RPCMethod", "Report");
            reportMap.put("ID", (int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
            reportMap.put("CmdType", "REPROT_BOOT_INITIATION");
            reportMap.put(Constant.SEQUENCEID, InstructionUtil.generate8HexString());
            Map<String, Object> map = new HashMap<String, Object>();

            String vendor = "";
            Map<String, Object> vendorMap = gatewayInfoService.queryManufacturerCodeByFactoryCode(gatewayInfo.getGatewayFactoryCode());
            if(vendorMap!= null && !vendorMap.isEmpty()){

            	vendor = vendorMap.get("manufacturerCode") == null ? "" : vendorMap.get("manufacturerCode").toString();
            }
            map.put("Vendor", vendor);
            map.put("ProuductClass", gatewayInfo.getGatewayModel());

            map.put("OSType", "0");
            map.put("OSVersion", "V1.0");
            map.put("PROTVersion", "V1.1.0");
            map.put("WanIPv6Addr", "");
            map.put("Province_code", topAreaId+"");
            map.put("WanIPAddr", WanIPAddr);
            map.put("HDVersion", HDVersion);
            map.put("SWVersion", SWVersion);
            map.put("MAC", gatewayInfo.getGatewayMacaddress());
            map.put("BDAccount", gatewayInfo.getGatewayAdslAccount());
            map.put("Password", gatewayInfo.getGatewayPassword());
            reportMap.put("Parameter", map);
            if(gatewayInfo.getGatewayAdslAccount() != null && !"".equals(gatewayInfo.getGatewayAdslAccount())
            		&& gatewayInfo.getGatewayPassword() != null && !"".equals(gatewayInfo.getGatewayPassword())
            		){

            	syncInfoToFirstLevelPlatformService.report("reportBootInitiation", reportMap);
            	logger.info("end invoke syncInfoToFirstLevelPlatform,reportMap:{}",reportMap);
            }
        }catch (Exception e){
            logger.error("同步信息到一级平台错误,方法是reportBootInitiation：{}",e);
        }
    }
   
    /**
     * @param mac
     * @param command
     * @param value
     * @return
     * 下发一个指令
     */
    private boolean setOneCommand(String mac,String command,String value){
		
    	List<ParameterValueStruct> list = new ArrayList<ParameterValueStruct>();
		ParameterValueStructStr pvs = new ParameterValueStructStr(command, value);
		list.add(pvs);
		
		boolean isSuccess = instructionMethodService.setParameterValue(mac, list);
		logger.debug("设置参数：{},值：{},结果：{}", command,value,isSuccess);
    	
    	return isSuccess;
    	
    }
    
}
