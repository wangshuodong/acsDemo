package com.cmiot.rms.services.boxManager.report.impl;

import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.EventStruct;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.cache.TemporaryObject;
import com.cmiot.rms.common.enums.RebootEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BoxFirmwareInfoMapper;
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.model.BoxFirmwareInfo;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.BoxFirmwareUpgradeTaskService;
import com.cmiot.rms.services.BoxInfoService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.boxManager.instruction.BoxInstructionMethodService;
import com.cmiot.rms.services.boxManager.report.BoxInformHandle;
import com.cmiot.rms.services.message.KafkaProducer;
import com.cmiot.rms.services.template.RedisClientTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * CPE上报Inform处理类
 * Created by panmingguo on 2016/5/10.
 */
@Service
public class BoxInformHandleImpl implements BoxInformHandle {
	private static final Logger LOGGER = LoggerFactory.getLogger(BoxInformHandleImpl.class);

	@Autowired
	private BoxInfoMapper boxInfoMapper;

	@Autowired
	private BoxFirmwareInfoMapper boxFirmwareInfoMapper;

	@Autowired
	BoxInstructionMethodService boxInstructionMethodService;

	@Autowired
	private RedisClientTemplate redisClientTemplate;

	@Autowired
	private GatewayInfoService gatewayInfoService;

	@Autowired
	private BoxFirmwareUpgradeTaskService boxFirmwareUpgradeTaskService;

    @Value("${stun.server.ip}")
    String ip;
    @Value("${stun.server.port}")
    String port;
         
	@Autowired
	KafkaProducer kafkaProducer;

	@Autowired
	BoxInfoService boxInfoService;

	/**
	 * "0 BOOTSTRAP" 表明会话发起原因是CPE首次安装或ACS的URL发生变化。
	 *
	 * @param inform
	 */
	@Override
	public void bootStrapEvent(Inform inform) {
		LOGGER.info("Start invoke bootStrapEvent：{}", inform);

		// 根据机顶盒SN和厂商code查询机顶盒
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
			// 设置机顶盒状态：在线
			resultBoxInfo.setBoxOnline(1);
			// 智能网关首次向网管注册 和 用于智能网关基于设备认证Password认证首次连接智能网关管理平台 修改入网时间
			// 入网时间
			resultBoxInfo.setBoxJoinTime((int) timeSeconds);
			// 机顶盒已注册
			resultBoxInfo.setBoxStatus("2");

			this.executeUpdate(resultBoxInfo, inform, events);
			LOGGER.info("更新机顶盒信息："+resultBoxInfo);
			// 更新机顶盒信息
			boxInfoMapper.updateByPrimaryKeySelective(resultBoxInfo);
			//删除回复出厂的redis锁
			updateInstructionsInfo(resultBoxInfo.getBoxSerialnumber(), RebootEnum.STATUS_2.code());
			// 日志记录 添加上报信息
//			LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(inform), inform.getRequestId(), resultBoxInfo.getBoxUuid(), "上报信息", LogTypeEnum.LOG_TYPE_SYSTEM.description());
			LOGGER.info("End invoke bootStrapEvent!");
		}
	}	
   

	/**
	 * "1 BOOT" 表明会话发起原因是CPE加电或重置，包括系统首次启动，以及因任何原因而引起的重启，包括使用Reboot方法。
	 *
	 * @param inform
	 */
	@Override
	public void bootEvent(Inform inform) {
		LOGGER.info("Start invoke bootEvent：{}", inform);
		// 升级
		if (null != inform) {
			LOGGER.info("开机或重启Inform上报数据,开始固件升级");
			String serialNumber = inform.getDeviceId().getSerialNubmer();
			String factoryCode = inform.getDeviceId().getOui();
			String logid = UniqueUtil.uuid();
			boxFirmwareUpgradeTaskService.bootEventUpgradeImmediately(logid, serialNumber, factoryCode, 3);
			LOGGER.info("开机或重启Inform上报数据,结束固件升级");
		}
		// 根据机顶盒SN和厂商code查询机顶盒
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
			// 设置机顶盒状态：在线
			resultBoxInfo.setBoxOnline(1);
			executeUpdate(resultBoxInfo, inform, events);
			boxInfoMapper.updateByPrimaryKeySelective(resultBoxInfo);
		}
		updateInstructionsInfo(resultBoxInfo.getBoxSerialnumber(), RebootEnum.STATUS_2.code());

		// 日志记录 添加上报信息
//		LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(inform), inform.getRequestId(), resultBoxInfo.getBoxUuid(), "上报信息", LogTypeEnum.LOG_TYPE_SYSTEM.description());
		LOGGER.info("End invoke bootEvent!");
	}

	public void updateInstructionsInfo(String gatewaySerialnumber, String code) {
		LOGGER.info("修改重启、出厂操作指令：gatewaySerialnumber" + gatewaySerialnumber + ",code：" + code);
		// 释放redis中写入的SN数据的锁
		redisClientTemplate.del("R-F-" + gatewaySerialnumber);
	}

	/**
	 * "2 PERIODIC" 表明会话发起原因是定期的Inform引起。
	 *
	 * @param inform
	 */
	@Override
	public void periodicEvent(Inform inform) {
		LOGGER.info("Start invoke periodicEvent：{}", inform);

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
		// 更新数据库数据（硬件版本、软件版本、终端回连URL、变化的参数）
		String sn = inform.getDeviceId().getSerialNubmer();
		String factoryCode = inform.getDeviceId().getOui();
		BoxInfo boxInfoParam = new BoxInfo();
		boxInfoParam.setBoxFactoryCode(factoryCode);
		boxInfoParam.setBoxSerialnumber(sn);
		BoxInfo boxInfo = boxInfoMapper.selectGatewayInfo(boxInfoParam);
		if (boxInfo == null) {
			LOGGER.info("valueChangeEvent 机顶盒不存在，sn:{},factoryCode:{}", sn, factoryCode);
			return;
		}
		
		EventStruct[] list = inform.getEvent().getEventCodes();
		List<String> events = new ArrayList<>();
		for (EventStruct eventStruct : list) {
			events.add(eventStruct.getEvenCode());
		}
		
		// 获取TCP/UDP连接地址
		/*String udpName = "Device.ManagementServer.UDPConnectionRequestAddress";
		List<String> nameList = new ArrayList<String>();
		nameList.add(udpName);
		String connectType = "";
		String connectUrl = "";
		Map<String, Object> valuesMap = boxInstructionMethodService.getParameterValues(boxInfo.getBoxMacaddress(), nameList);
		if (valuesMap != null && !valuesMap.isEmpty()) {
			Object udpUrl = valuesMap.get(udpName);
			if (udpUrl != null && !"".equals(udpUrl)) {
				connectType = "UDP";
				connectUrl = udpUrl.toString();
				
				boxInfo.setBoxConnType(connectType);
				boxInfo.setBoxConnectionrequesturl(connectUrl);
			}

		}*/
		
	/*	List<ParameterValueStruct> parameterValueStructs = inform.getParameterList().getParameterValueStructs();
		for (ParameterValueStruct parameterValueStruct : parameterValueStructs) {
			// 终端回连URL
			if (org.apache.commons.lang.StringUtils.equals("Device.ManagementServer.ConnectionRequestURL", parameterValueStruct.getName()))

			{
				// RMS 向智能网关发起连接请求通知时所使用的HTTP URL
				String url = parameterValueStruct.getValue() + "";
				boxInfo.setBoxConnectionrequesturl(url);
			}
		}*/
		
		long timeMillis = System.currentTimeMillis();
		long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
		// 最近连接时间
		boxInfo.setBoxLastConnTime((int) timeSeconds);
		executeUpdate(boxInfo, inform, events);
		int i = boxInfoMapper.updateByPrimaryKeySelective(boxInfo);
		LOGGER.info("End invoke valueChangeEvent：{},更新数据条数:{}", inform, i);
		// 日志记录 添加上报信息
//		LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(inform), inform.getRequestId(), boxInfo.getBoxUuid(), "上报信息", LogTypeEnum.LOG_TYPE_SYSTEM.description());
		LOGGER.info("End invoke valueChangeEvent：{},更新数据条数:{}", inform, i);
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
		LOGGER.info("Start invoke boxdiagnosticsCompleteEvent：{}", inform);
		BoxInfo boxInfo = new BoxInfo();
		boxInfo.setBoxSerialnumber(inform.getDeviceId().getSerialNubmer().trim());
		boxInfo.setBoxFactoryCode(inform.getDeviceId().getOui().trim());
		// 根据 SN 和 OUI查询网关信息
		BoxInfo resultBoxInfo = boxInfoMapper.selectGatewayInfo(boxInfo);
		;
		if (null == resultBoxInfo) {
			return;
		}
		LOGGER.info("boxdiagnosticsCompleteEvent GatewayInfo:{}", "box_diagnostics_" + resultBoxInfo.getBoxMacaddress());
		TemporaryObject object = RequestCache.get("box_diagnostics_" + resultBoxInfo.getBoxMacaddress());
		if (null != object) {
			synchronized (object) {
				LOGGER.info("boxdiagnosticsCompleteEvent notifyAll mac:{}", "box_diagnostics_" + resultBoxInfo.getBoxMacaddress());
				object.notifyAll();
			}
		} else {
			LOGGER.info("not exist box diagnostics cache!");
//			LOGGER.info("boxKafkaProducer SendMessage mac:{}", "box_diagnostics_" + resultBoxInfo.getBoxMacaddress());
//			kafkaProducer.sendMessage("requestId", "box_diagnostics_" + resultBoxInfo.getBoxMacaddress());
		}
		LOGGER.info("End invoke boxdiagnosticsCompleteEvent");
	}

	@Override
	public void xCmccMonitor(Inform inform) {
		LOGGER.info("Start invoke X CMCC MONITOR：{}", inform);

		LOGGER.info("End invoke X CMCC MONITOR!");
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
		// 根据SN和OUI查询是否已经存在机顶盒信息
		List<BoxInfo> boxList = boxInfoMapper.selectBoxInfo(boxInfo);
		if (boxList == null || boxList.size() == 0) {
			return null;
		}
		return boxList.get(0);
	}

	
	/**
	 * 执行存数据库 和 存redis
	 * 
	 * @param boxInfo
	 * @param inform
	 * @param events
	 */
	private void executeUpdate(BoxInfo boxInfo, Inform inform, List<String> events) {

		String oui = "";
		String sn = "";
		// 遍历
		List<ParameterValueStruct> parameterValueStructs = inform.getParameterList().getParameterValueStructs();
		for (ParameterValueStruct parameterValueStruct : parameterValueStructs) {
			// 终端回连URL
			if (org.apache.commons.lang.StringUtils.equals("Device.ManagementServer.ConnectionRequestURL", parameterValueStruct.getName()))

			{
				// RMS 向智能网关发起连接请求通知时所使用的HTTP URL
			//	String url = parameterValueStruct.getValue() + "";
			//	boxInfo.setBoxConnectionrequesturl(url);
			} else if (org.apache.commons.lang.StringUtils.equals("Device.DeviceInfo.HardwareVersion", parameterValueStruct.getName())) {
				// 硬件版本
				String hardVersion = parameterValueStruct.getValue() + "";
				boxInfo.setBoxHardwareVersion(hardVersion);
			} else if (org.apache.commons.lang.StringUtils.equals("Device.DeviceInfo.SoftwareVersion", parameterValueStruct.getName())) {
				// 软件版本 固件版本
				String softwareVersion = parameterValueStruct.getValue() == null ? "" : parameterValueStruct.getValue().toString();
				// 根据固件版本号和机顶盒厂商编号，机顶盒类型，机顶盒型号联合查询固件UUID
				String boxFirmwareUuid = queryFirmwareUuid(softwareVersion, boxInfo.getBoxFactoryCode(), boxInfo.getBoxType()

						, boxInfo.getBoxModel());
				if (boxFirmwareUuid != null) {
					boxInfo.setBoxFirmwareUuid(boxFirmwareUuid);
					;
				}
			} else if ("Device.GatewayInfo.ManufacturerOUI".equals(parameterValueStruct.getName())) {
				oui = parameterValueStruct.getValue() == null ? "" : parameterValueStruct.getValue().toString();

			} else if ("Device.GatewayInfo.SerialNumber".equals(parameterValueStruct.getName())) {
				sn = parameterValueStruct.getValue() == null ? "" : parameterValueStruct.getValue().toString();

			} else if ("Device.LAN.IPAddress".equals(parameterValueStruct.getName())) {
				boxInfo.setBoxIpaddress(parameterValueStruct.getValue() + "");
			}else if ("Device.ManagementServer.UDPConnectionRequestAddress".equals(parameterValueStruct.getName())) {
				
				boxInfo.setBoxConnType("UDP");
				boxInfo.setBoxConnectionrequesturl(parameterValueStruct.getValue() + "");
				
			}
		}
		if (!"".equals(oui) && !"".equals(sn)) {
			GatewayInfo gateway = findGatewayInfoByOuiSn(oui, sn);
			if (gateway != null) {

				boxInfo.setGatewayUuid(gateway.getGatewayUuid());
			}
		}
		
	}

	/**
	 * 根据OUI SN 查询网关信息
	 */
	private GatewayInfo findGatewayInfoByOuiSn(String oui, String sn) {

		GatewayInfo gatewayInfo = new GatewayInfo();
		gatewayInfo.setGatewayFactoryCode(oui);
		gatewayInfo.setGatewaySerialnumber(sn);
		GatewayInfo gateway = gatewayInfoService.selectGatewayInfo(gatewayInfo);
		return gateway;
	}

	/**
	 * 根据固件版本号和机顶盒厂商编号，机顶盒类型，机顶盒型号联合查询固件UUID
	 */
	private String queryFirmwareUuid(String softwareVersion, String boxFactoryCode, String boxType, String boxModel) {

		if (!"".equals(softwareVersion)) {
			BoxFirmwareInfo firm = new BoxFirmwareInfo();
			firm.setFirmwareVersion(softwareVersion);
			firm.setFactoryCode(boxFactoryCode);
			firm.setBoxType(boxType);
			firm.setBoxModel(boxModel);

			List<BoxFirmwareInfo> firmwareInfoList = boxFirmwareInfoMapper.queryFirmwareInfo(firm);
			if (firmwareInfoList != null && firmwareInfoList.size() > 0) {
				BoxFirmwareInfo info = firmwareInfoList.get(0);
				return info.getId();
			}
		}
		return null;
	}

	@Override
	public void mXCmccShutdown(Inform inform) {
		// 关机
		LOGGER.info("Start invoke mXCmccShutdownEvent：{}", inform);

		// 根据机顶盒SN和厂商code查询机顶盒
		BoxInfo resultBoxInfo = getResultBoxInfo(inform);

		if (resultBoxInfo != null) {

			resultBoxInfo.setBoxOnline(0);
			boxInfoMapper.updateByPrimaryKey(resultBoxInfo);
		}
		// 日志记录 添加上报信息
//		LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(inform), inform.getRequestId(), resultBoxInfo.getBoxUuid(), "上报信息", LogTypeEnum.LOG_TYPE_SYSTEM.description());
		LOGGER.info("End invoke mXCmccShutdownEvent");

	}

}
