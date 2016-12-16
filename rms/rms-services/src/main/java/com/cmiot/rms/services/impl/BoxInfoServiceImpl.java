package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.cmiot.rms.common.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.EventStruct;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.logback.LogBackRecord;
import com.cmiot.rms.dao.mapper.BoxFirmwareInfoMapper;
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.model.BoxFirmwareInfo;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.services.BoxInfoService;
import com.cmiot.rms.services.boxManager.instruction.BoxInstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.InstructionUtil;

/**
 * Created by zoujiang on 2016/6/13.
 */
@Service("boxInfoService")
public class BoxInfoServiceImpl implements BoxInfoService{
    
	private static Logger logger = LoggerFactory.getLogger(BoxInfoServiceImpl.class);

    @Autowired
    private BoxInfoMapper boxInfoMapper;
    
    @Autowired
    private BoxFirmwareInfoMapper boxFirmwareInfoMapper;

	@Autowired
	BoxInstructionMethodService boxInstructionMethodService;

	@Autowired
	private RedisClientTemplate redisClientTemplate;

   
    @Override
    public void updateInformBoxInfo(Inform inform) {
    	
    	logger.info("处理上报的网关信息为:" + JSON.toJSONString(inform));
        //根据机顶盒SN和厂商code查询机顶盒
        BoxInfo resultBoxInfo = getResultBoxInfo(inform);

        if (resultBoxInfo != null) {

            EventStruct[] list = inform.getEvent().getEventCodes();
            List<String> events = new ArrayList<>();
            for (EventStruct eventStruct : list) {
            	events.add(eventStruct.getEvenCode());
            }

            long timeMillis = System.currentTimeMillis();
            long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
            
            // 最近连接时间
            resultBoxInfo.setBoxLastConnTime((int) timeSeconds);
            //设置机顶盒状态：在线
            resultBoxInfo.setBoxOnline(1);
            // 智能网关首次向网管注册 和 用于智能网关基于设备认证Password认证首次连接智能网关管理平台 修改入网时间
            if(events.contains(EventCodeEnum.EVENT_CODE_BOOTSTRAP.code()) || events.contains(EventCodeEnum.EVENT_CODE_X_CMCC_BIND.code())) {
                //入网时间
            	resultBoxInfo.setBoxJoinTime((int) timeSeconds);
            	//机顶盒已注册
            	resultBoxInfo.setBoxStatus("2");
                //首次连接的时候设置digest帐号和密码,帐号密码随机生成
                String account = InstructionUtil.generateShortUuid();
                String password = InstructionUtil.generateShortUuid();
                //设置digest帐号密码, 管理帐号密码
                boolean flag = setBoxParams(resultBoxInfo.getBoxMacaddress() ,account, password);
                if(flag){
                	resultBoxInfo.setBoxDigestAccount(account);
                	resultBoxInfo.setBoxDigestPassword(password);
                }
            }
            // 事件码为  0 BOOTSTRAP   1 BOOT   4 VALUE CHANGE   M Reboot X CMCC BIND 时需要检查更新数据库
            if (events.contains(EventCodeEnum.EVENT_CODE_BOOTSTRAP.code())
                    || events.contains(EventCodeEnum.EVENT_CODE_BOOT.code())
                    || events.contains(EventCodeEnum.EVENT_CODE_M_REBOOT.code())
                    || events.contains(EventCodeEnum.EVENT_CODE_VALUECHANGE.code())
                    || events.contains(EventCodeEnum.EVENT_CODE_CONNECTIONREQUEST.code())
                    || events.contains(EventCodeEnum.EVENT_CODE_X_CMCC_BIND.code())
                    ) {
                // 处理上报的网关信息
                logger.info("处理上报的网关信息 eventCode 为:" + JSON.toJSONString(events));

                this.executeUpdate(resultBoxInfo, inform,events);
            }
            if(events.contains(EventCodeEnum.EVENT_CODE_M_X_CMCC_SHUTDOWN.code())){
            	//关机
            	resultBoxInfo.setBoxOnline(0);
            }
            
            // 更新网关信息
            boxInfoMapper.updateByPrimaryKeySelective(resultBoxInfo);

        } else {
            logger.info("上报的网关信息不存在：" + JSON.toJSONString(inform));
        }
      
       
    }

  
    private boolean setBoxParams(String mac,String account, String password) {
		
    	List<ParameterValueStruct> list = new ArrayList<ParameterValueStruct>();
    	ParameterValueStruct<String> struct = new ParameterValueStruct<String>();
    	struct.setName(ConstantDiagnose.MANAGEMENT_SERVER_USERNAME);
    	struct.setValue(account);
    	list.add(struct);
    	ParameterValueStruct<String> passwordStruct = new ParameterValueStruct<String>();
    	passwordStruct.setName(ConstantDiagnose.MANAGEMENT_SERVER_PASSWORD);
    	passwordStruct.setValue(password);
    	list.add(passwordStruct);
    	ParameterValueStruct<Integer> statusStruct = new ParameterValueStruct<Integer>();
    	statusStruct.setName("InternetGatewayDevice.X_CMCC_UserInfo.Status");
    	statusStruct.setValue(0);
    	list.add(statusStruct);
    	ParameterValueStruct<Integer> resultStruct = new ParameterValueStruct<Integer>();
    	resultStruct.setName("InternetGatewayDevice.X_CMCC_UserInfo.Result");
    	resultStruct.setValue(0);
    	list.add(resultStruct);
    	
    	boolean flag = boxInstructionMethodService.setParameterValue(mac, list);
    	
    	List<ParameterValueStruct> list2 = new ArrayList<ParameterValueStruct>();
		ParameterValueStruct<Integer> resultStruct2 = new ParameterValueStruct<Integer>();
		resultStruct2.setName("InternetGatewayDevice.X_CMCC_UserInfo.Result");
		resultStruct2.setValue(1);
		list2.add(resultStruct2);
		
		flag = boxInstructionMethodService.setParameterValue(mac, list2);
		
		return flag;
	}

    // 小方法分离
    private BoxInfo getResultBoxInfo(Inform inform) {
        // 查询网关表SN是否储存在
    	BoxInfo boxInfo = new BoxInfo();
        String serialnumber = inform.getDeviceId().getSerialNubmer();
        String gatewayInfoFactoryCode = inform.getDeviceId().getOui();
        // SN号
        boxInfo.setBoxSerialnumber(serialnumber);
        boxInfo.setBoxFactoryCode(gatewayInfoFactoryCode);
        //根据SN和OUI查询是否已经存在机顶盒信息
        List<BoxInfo> boxList = boxInfoMapper.selectBoxInfo(boxInfo);
        if(boxList == null || boxList.size() == 0){
        	return null;
        }
        return boxList.get(0);
    }


    /**
     * 执行存数据库 和 存redis
     *  @param gatewayInfo
     * @param inform
     * @param events
     */
    private void executeUpdate(BoxInfo boxInfo, Inform inform, List<String> events) {
        logger.info("执行网关信息修改操作");

        // 遍历
        List<ParameterValueStruct> parameterValueStructs = inform.getParameterList().getParameterValueStructs();
        for (ParameterValueStruct parameterValueStruct : parameterValueStructs) {
            // 终端回连URL
            if (org.apache.commons.lang.StringUtils.equals(GatewayDMSEnum.GATEWAYDMS_CONNECTIONREQUESTURL.code(), parameterValueStruct.getName())) {
                // RMS 向智能网关发起连接请求通知时所使用的HTTP URL
                String  url = parameterValueStruct.getValue() + "";
                boxInfo.setBoxConnectionrequesturl(url);
                logger.info("ConnectionRequestURL：" + url);
            } else if (org.apache.commons.lang.StringUtils.equals(GatewayDMSEnum.GATEWAYDMS_HARDWAREVERSION.code(), parameterValueStruct.getName())) {
            	// 硬件版本
            	String hardVersion = parameterValueStruct.getValue() + "";
            	boxInfo.setBoxHardwareVersion(hardVersion);
            	logger.info("hardVersion：" + hardVersion);
            } else if (org.apache.commons.lang.StringUtils.equals(GatewayDMSEnum.GATEWAYDMS_SOFTWAREVERSION.code(), parameterValueStruct.getName())) {
            	// 软件版本 固件版本
            	String softwareVersion = parameterValueStruct.getValue() == null ? "" : parameterValueStruct.getValue().toString();
            	//根据固件版本号和机顶盒厂商编号，机顶盒类型，机顶盒型号联合查询固件UUID
            	String boxFirmwareUuid = queryFirmwareUuid(softwareVersion, boxInfo.getBoxFactoryCode(), boxInfo.getBoxType() ,boxInfo.getBoxModel());
            	logger.info("根据固件版本号和机顶盒厂商编号，机顶盒类型，机顶盒型号联合查询固件UUID：" + boxFirmwareUuid);
            	if(boxFirmwareUuid != null ){
            		boxInfo.setBoxFirmwareUuid(boxFirmwareUuid);;
            	}
            } else if (parameterValueStruct.getName().contains(Constant.EXTERNAL_IPADDRESS)) {
                if (getRegex(parameterValueStruct.getName())) {
                    String gatewayIp = parameterValueStruct.getValue() +"";
                    boxInfo.setBoxIpaddress(gatewayIp);
                    logger.info("机顶盒IP地址：" + gatewayIp);
                }
            } else if(org.apache.commons.lang.StringUtils.equals(GatewayDMSEnum.GATEWAYDMS_SERIALNUMBER.code(), parameterValueStruct.getName())){
                // 设备序列号  修改redis
                // 恢复出厂设置 、重启完成
                if (events.contains(EventCodeEnum.EVENT_CODE_BOOT.code())){
                    updateInstructionsInfo(boxInfo.getBoxSerialnumber(),RebootEnum.STATUS_2.code());
                }
            }
        }
        
        // 日志记录 添加上报信息
//        LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(inform), inform.getRequestId(), "", "上报信息", LogTypeEnum.LOG_TYPE_SYSTEM.description());
    }
     
    public void updateInstructionsInfo(String gatewaySerialnumber, String code) {
        logger.info("修改重启、出厂操作指令：gatewaySerialnumber"+gatewaySerialnumber+",code："+code);
        // 释放redis中写入的SN数据的锁
        redisClientTemplate.del("R-F-"+gatewaySerialnumber);
    }


	/**
     * 根据固件版本号和机顶盒厂商编号，机顶盒类型，机顶盒型号联合查询固件UUID
     * 
     * */
    private String queryFirmwareUuid(String softwareVersion,
			String boxFactoryCode, String boxType, String boxModel) {
    	if(!"".equals(softwareVersion)){
    		BoxFirmwareInfo firm = new BoxFirmwareInfo();
    		firm.setFirmwareVersion(softwareVersion);
    		firm.setFactoryCode(boxFactoryCode);
    		firm.setBoxType(boxType);
    		firm.setBoxModel(boxModel);
    		
    		List<BoxFirmwareInfo> firmwareInfoList = boxFirmwareInfoMapper.queryFirmwareInfo(firm);
    		if(firmwareInfoList != null && firmwareInfoList.size() > 0){
    			BoxFirmwareInfo info = firmwareInfoList.get(0);
    			return info.getId();
    		}
    	}
		return null;
	}


	/**
     * 正则表达式验证
     *
     * @param str
     * @return
     */
    public boolean getRegex(String str) {
        logger.info("需要验证的ExternalIPAddress：" + str);
        // 正则表达式 
        String regex = "InternetGatewayDevice[.]WANDevice[.][1-9]+[.]WANConnectionDevice[.][1-9]+[.]WANIPConnection[.][1-9]+[.]ExternalIPAddress";
        //String str1 = "InternetGatewayDevice.WANDevice.4.WANConnectionDevice.2.WANIPConnection.1.ExternalIPAddress";
        boolean isNum = str.matches(regex);
        return isNum;
    }

	@Override
	public void insertSelective(BoxInfo record) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateByPrimaryKeySelective(BoxInfo record) {
		boxInfoMapper.updateByPrimaryKeySelective(record);

	}

	@Override
	public List<BoxInfo> queryListByIds(List<String> boxIds) {
		return boxInfoMapper.queryListByIds(boxIds);
	}


	@Override
	public BoxInfo selectByPrimaryKey(String boxUuid) {
		
		return boxInfoMapper.selectByPrimaryKey(boxUuid);
	}


	@Override
	public int updateBySnSelective(BoxInfo record) {
		return boxInfoMapper.updateBySnSelective(record);
	}

}
