package com.cmiot.rms.services.boxManager.outerservice.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.model.*;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.enums.InstructionsStatusEnum;
import com.cmiot.rms.common.enums.ReqStatusEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.enums.UpgradeTaskDetailStatusEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.services.BoxFirmwareUpgradeTaskService;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.boxManager.instruction.BoxInstructionMethodService;
import com.cmiot.rms.services.boxManager.outerservice.BoxRequestMgrService;
import com.cmiot.rms.services.boxManager.report.BoxInformParse;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/6/14.
 */
@Service
public class BoxRequestMgrServiceImpl implements BoxRequestMgrService {

    private static Logger logger = LoggerFactory.getLogger(BoxRequestMgrServiceImpl.class);

    @Autowired
    private BoxInfoMapper boxInfoMapper;

    @Autowired
    private InstructionsService instructionsService;
    
    @Autowired
    BoxInstructionMethodService boxInstructionMethodService;

    @Autowired
    BoxInformParse boxInformParse;
    
    @Autowired
    BoxFirmwareUpgradeTaskService boxFirmwareUpgradeTaskService;

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
    	String dlid = UniqueUtil.uuid();
		logger.info("dlid:{} 传入DownloadResponse对象参数:{}" , dlid, jsonObject);
		DownloadResponse downloadResponse = JSON.toJavaObject(jsonObject, DownloadResponse.class);
		String requestId = downloadResponse.getRequestId();
		String taskId = instructionsService.getBeforeContent(requestId, "taskId");
		String logid = instructionsService.getBeforeContent(requestId, "loguuid");
		logger.info("dlid:{} LogId:{} download requestId:{} taskId:{} status:{}", dlid, logid, requestId, taskId, downloadResponse.getStatus());
		if (StringUtils.equals(String.valueOf(downloadResponse.getStatus()), ReqStatusEnum.Req_Status_0.code())) {
			int status = InstructionsStatusEnum.STATUS_1.code();
			if (org.apache.commons.lang.StringUtils.isNotBlank(taskId)) {
				logger.info("dlid:{} LogId:{} TransferComplete download requestId:{}, taskId:{}, Status:{}：", dlid, logid,  requestId, taskId, UpgradeTaskDetailStatusEnum.SUCSSESS.code());
				boxFirmwareUpgradeTaskService.updateTaskDetailStatus(logid, taskId, requestId, UpgradeTaskDetailStatusEnum.SUCSSESS.code());
			}
			logger.info("dlid:{} LogId:{} download instructionsService.updateInstructionsInfo方法执行", dlid, logid);
			return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "DownloadResponse", "下载文件");
		} else {
			Map<String, Object> result = new HashMap<>();
			if (StringUtils.equals(String.valueOf(downloadResponse.getStatus()), ReqStatusEnum.Req_Status_1.code())) {
				// 等待网关 transferComplete 方法
				result.put("code", RespCodeEnum.RC_1000.code());
				result.put("message", "等待transferComplete来确认升级是否成功");
				logger.info("dlid:{} LogId:{} result:{}", dlid, logid, JSON.toJSONString(result));
				return result;
			} else {
				result.put("code", RespCodeEnum.RC_1004.code());
				result.put("message", "Download指令status是未知状态");
				logger.info("dlid:{} LogId:{} result:{}", dlid, logid, JSON.toJSONString(result));
				return result;
			}
		}
    }

    @Override
    public Map<String, Object> transferComplete(JSONObject jsonObject) {
    	String fcid = UniqueUtil.uuid();
		logger.info("fcid:{} 传入TransferComplete对象参数:{}", fcid, jsonObject);
		TransferComplete transferComplete = JSON.toJavaObject(jsonObject, TransferComplete.class);
		String requestId = transferComplete.getCommandKey();// 获取下载指令ID
		String taskId = instructionsService.getBeforeContent(requestId, "taskId");
		String logid = instructionsService.getBeforeContent(requestId, "loguuid");
		logger.info("fcid:{} LogId:{} TransferComplete requestId:{} taskId:{}", fcid, logid, requestId, taskId);
		int status;
		if (transferComplete.getFaultStruct() == null || transferComplete.getFaultStruct().getFaultCode() == 0) {
			status = InstructionsStatusEnum.STATUS_1.code();
			if (org.apache.commons.lang.StringUtils.isNotBlank(taskId))// 说明是升级任务执行的download指令，需要更新升级任务结果
			{
				logger.info("fcid:{} LogId:{} TransferComplete updateTaskDetail requestId:{} taskId:{} Status:{}", fcid, logid, requestId, taskId, UpgradeTaskDetailStatusEnum.SUCSSESS.code());
				boxFirmwareUpgradeTaskService.updateTaskDetailStatus(logid, taskId, requestId, UpgradeTaskDetailStatusEnum.SUCSSESS.code());
			}
		} else {
			status = InstructionsStatusEnum.STATUS_2.code();
			if (org.apache.commons.lang.StringUtils.isNotBlank(taskId))// 说明是升级任务执行的download指令，需要更新升级任务结果
			{
				logger.info("fcid:{} LogId:{} TransferComplete updateTaskDetail requestId:{} taskId:{} Status:{}", fcid, logid, requestId, taskId, UpgradeTaskDetailStatusEnum.FAILURE.code());
				boxFirmwareUpgradeTaskService.updateTaskDetailStatus(logid, taskId, requestId, UpgradeTaskDetailStatusEnum.FAILURE.code());
			}
			logger.info("fcid:{} LogId:{} 失败状态码status:{}" , fcid, logid, transferComplete.getFaultStruct().getFaultCode());
		}
		logger.info("fcid:{} LogId:{} transferComplete instructionsService.updateInstructionsInfo方法执行", fcid, logid);
		return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "TransferComplete", "下载完成");
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
            String boxId = map.get("cpeIdentity");
            if(org.apache.commons.lang.StringUtils.isNotBlank(boxId))
            {
                BoxInfo info = new BoxInfo();
                info.setBoxUuid(boxId);
                info.setBoxDigestAccount("");
                info.setBoxDigestPassword("");
                boxInfoMapper.updateByPrimaryKeySelective(info);
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
        //TODO 看是否需要更新盒子信息

        //解析Inform,根据不同的event完成不同的操作
        boxInformParse.parseInform(inform);

        logger.info("处理上报的机顶盒信息完成");
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
                && requestId.startsWith("BoxBootStrap")
                && (status == InstructionsStatusEnum.STATUS_1.code()))
        {
            saveAccountAndPsw(requestId);
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
        String boxInfoSerialnumber = null != parameter.get("sn") ? parameter.get("sn").toString() :"";
        String boxInfoFactoryCode = null != parameter.get("oui") ? parameter.get("oui").toString() :"";
        if(org.apache.commons.lang.StringUtils.isEmpty(boxInfoSerialnumber)
                || org.apache.commons.lang.StringUtils.isEmpty(boxInfoFactoryCode) )
        {
            return false;
        }
        BoxInfo boxInfo = new BoxInfo();
        boxInfo.setBoxSerialnumber(boxInfoSerialnumber);
        boxInfo.setBoxFactoryCode(boxInfoFactoryCode);
        // 根据 SN 和 OUI查询是否存在网关信息
       List<BoxInfo> boxInfoR = boxInfoMapper.selectBoxInfo(boxInfo);

        boolean isPass = false;

        if (boxInfoR != null && boxInfoR.size()>0) {
            isPass = true;
        }
        return isPass;
    }

    @Override
    public Map<String, Object> fault(JSONObject jsonObject)  {
        logger.info("传入Fault对象参数：" + jsonObject);
        Fault fault = JSON.toJavaObject(jsonObject, Fault.class);
        String requestId = fault.getRequestId();
        int status = InstructionsStatusEnum.STATUS_2.code();
        return instructionsService.updateInstructionsInfo(requestId, jsonObject, status, "Fault", "下发指令错误");

    }


	@Override
	public boolean queryDigest(Map<String, Object> parameter) {
		String boxInfoSerialnumber = null != parameter.get("sn") ? parameter.get("sn").toString() :"";
        String boxInfoFactoryCode = null != parameter.get("oui") ? parameter.get("oui").toString() :"";
        if(org.apache.commons.lang.StringUtils.isEmpty(boxInfoSerialnumber)
                || org.apache.commons.lang.StringUtils.isEmpty(boxInfoFactoryCode) )
        {
            return false;
        }
        BoxInfo boxInfo = new BoxInfo();
        boxInfo.setBoxSerialnumber(boxInfoSerialnumber);
        boxInfo.setBoxFactoryCode(boxInfoFactoryCode);
        // 根据 SN 和 OUI查询是否存在网关信息
        List<BoxInfo> boxInfoR = boxInfoMapper.selectBoxInfo(boxInfo);
        if(boxInfoR != null && boxInfoR.size()>0){
        	List<ParameterValueStruct> list=new ArrayList<ParameterValueStruct>();
			ParameterValueStruct<String> pvsUserName=new ParameterValueStruct<String>();
			pvsUserName.setName(ConstantDiagnose.MANAGEMENT_SERVER_USERNAME);
			pvsUserName.setValue(boxInfoR.get(0).getBoxConnectAccount());
			pvsUserName.setReadWrite(true);
			list.add(pvsUserName);
			ParameterValueStruct<String> pvsPassword=new ParameterValueStruct<String>();
			pvsPassword.setName(ConstantDiagnose.MANAGEMENT_SERVER_PASSWORD);
			pvsPassword.setValue(boxInfoR.get(0).getBoxConnectPassword());
			pvsUserName.setReadWrite(true);
			list.add(pvsPassword);
			return boxInstructionMethodService.setParameterValue(boxInfoR.get(0).getBoxMacaddress(), list);
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
        BoxInfo boxInfo = new BoxInfo();
        boxInfo.setBoxConnectAccount(userName);
        List<BoxInfo> boxInfoR = boxInfoMapper.selectBoxInfo(boxInfo);
        List<String> retList = new ArrayList<>();
        for(BoxInfo item:boxInfoR){
            if(!org.apache.commons.lang.StringUtils.isEmpty(item.getBoxDigestPassword())){
                retList.add(item.getBoxConnectPassword());
            }
        }
        return retList;
    }

    /**
     * 根据OUI-SN查询Digest账号和密码
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryDigestAccAndPw(Map<String, Object> parameter) {
        String boxInfoSerialnumber = null != parameter.get("sn") ? parameter.get("sn").toString() :"";
        String boxInfoFactoryCode = null != parameter.get("oui") ? parameter.get("oui").toString() :"";
        Boolean isFirst = null != parameter.get("isFirst") ? Boolean.valueOf(parameter.get("isFirst").toString()) : false;
        Map<String, Object> retMap = new HashMap<>();
        if(org.apache.commons.lang.StringUtils.isEmpty(boxInfoSerialnumber)
                || org.apache.commons.lang.StringUtils.isEmpty(boxInfoFactoryCode) )
        {
            retMap.put(Constant.CODE, -1);
            retMap.put(Constant.MESSAGE, "参数为空");
            return retMap;
        }
        BoxInfo boxInfo = new BoxInfo();
        boxInfo.setBoxSerialnumber(boxInfoSerialnumber);
        boxInfo.setBoxFactoryCode(boxInfoFactoryCode);
        // 根据 SN 和 OUI查询是否存在网关信息
        List<BoxInfo> boxInfoR = boxInfoMapper.selectBoxInfo(boxInfo);
        if(boxInfoR == null || boxInfoR.size() < 1){
            retMap.put(Constant.CODE, -1);
            retMap.put(Constant.MESSAGE, "机顶盒不存在");
            return retMap;
        }
        else
        {
            retMap.put(Constant.CODE, 0);
            //首次上报并digest账号和密码在数据库中为空
            if(isFirst
                    && (org.apache.commons.lang.StringUtils.isBlank( boxInfoR.get(0).getBoxDigestAccount())
                    && org.apache.commons.lang.StringUtils.isBlank(boxInfoR.get(0).getBoxDigestPassword())))
            {
                retMap.put("userName", boxInfoR.get(0).getBoxSerialnumber());
                retMap.put("password", "STBAdmin");
            }
            else
            {
                retMap.put("userName", boxInfoR.get(0).getBoxDigestAccount());
                retMap.put("password", boxInfoR.get(0).getBoxDigestPassword());
            }
        }
        return retMap;
    }

    /**
     * 保存账号和密码到数据库
     * @param requestId
     */
    private void saveAccountAndPsw(String requestId)
    {
        Map<String, String> map = instructionsService.getInstructionsInfo(requestId);
        String boxId = map.get("cpeIdentity");
        BoxInfo BoxInfo = boxInfoMapper.selectByPrimaryKey(boxId);
        if(null == BoxInfo)
        {
            return;
        }

        String beforeContent = map.get("beforeContent");
        if(org.apache.commons.lang.StringUtils.isEmpty(beforeContent))
        {
            return;
        }

        SetParameterValues parameterValues = JSON.toJavaObject(JSON.parseObject(beforeContent), SetParameterValues.class);
        if(null == parameterValues)
        {
            return;
        }

        BoxInfo updateInfo = new BoxInfo();
        updateInfo.setBoxUuid(BoxInfo.getBoxUuid());

        List<ParameterValueStruct> structs = parameterValues.getParameterList().getParameterValueStructs();
        for(ParameterValueStruct struct : structs)
        {
            if(struct.getName().equals("Device.ManagementServer.ConnectionRequestUsername"))
            {
                updateInfo.setBoxConnectAccount(String.valueOf(struct.getValue()));
            }
            else if(struct.getName().equals("Device.ManagementServer.ConnectionRequestPassword"))
            {
                updateInfo.setBoxConnectPassword(String.valueOf(struct.getValue()));
            }
            else if(struct.getName().equals("Device.ManagementServer.Username"))
            {
                updateInfo.setBoxDigestAccount(String.valueOf(struct.getValue()));
            }
            else if(struct.getName().equals("Device.ManagementServer.Password"))
            {
                updateInfo.setBoxDigestPassword(String.valueOf(struct.getValue()));
            }
        }

        boxInfoMapper.updateByPrimaryKeySelective(updateInfo);
    }
}
