package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.dao.mapper.*;
import com.cmiot.rms.dao.model.*;
import com.cmiot.rms.services.workorder.impl.BusiOperation;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.EventStruct;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.enums.EventCodeEnum;
import com.cmiot.rms.common.enums.GatewayDMSEnum;
import com.cmiot.rms.common.enums.InformStateEnum;
import com.cmiot.rms.common.enums.InstructionsStatusEnum;
import com.cmiot.rms.common.enums.LogTypeEnum;
import com.cmiot.rms.common.enums.RebootEnum;
import com.cmiot.rms.common.logback.LogBackRecord;
import com.cmiot.rms.common.page.PageBean;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.services.FirmwareInfoService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.InformService;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.SyncInfoToFirstLevelPlatformService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.InstructionUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.tydic.inter.app.service.GatewayHandleService;
import com.tydic.service.PluginDeviceService;

/**
 * Created by fuwanhong on 2016/1/25.
 */
@Service("gatewayInfoService")
public class GatewayInfoServiceImpl implements GatewayInfoService{
    private static Logger logger = LoggerFactory.getLogger(GatewayInfoServiceImpl.class);

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;
    @Autowired
    private GatewayAdslAccountMapper gatewayAdslAccountMapper;

    @Autowired
    private InformService informService;

    @Autowired
    private InstructionsService instructionsService;
    
	@Autowired
	InstructionMethodService instructionMethodService;
	
	@Autowired
	SyncInfoToFirstLevelPlatformService syncInfoToFirstLevelPlatformService;

   @Autowired
    private FirmwareInfoService firmwareInfoService;
	@Autowired
	private RedisClientTemplate redisClientTemplate;

    @Autowired
    private AreaService amsAreaService;

    @Autowired
    private GatewayHandleService gatewayHandleService;
    
    @Autowired
	private HardwareAblityMapper hardwareAblityMapper;
    
    @Autowired
    GatewayQueueMapper gatewayQueueMapper;

    @Autowired
    private GatewayBusinessMapper gatewayBusinessMapper;

    @Autowired
    private WorkOrderTemplateInfoMapper workOrderTemplateInfoMapper;

    @Autowired
    private BusiOperation busiOperation;

    @Autowired
    private GatewayBusinessOpenDetailMapper gatewayBusinessOpenDetailMapper;

    @Autowired
    private GatewayBusinessExecuteHistoryMapper gatewayBusinessExecuteHistoryMapper;
    
    @Autowired
    private PluginDeviceService pluginDeviceService;
    
    
    @Override
    public List<GatewayInfo> queryList(GatewayInfo gatewayInfo) {
        return gatewayInfoMapper.queryList(gatewayInfo);
    }

    @Override
    public List<GatewayInfo> queryByDeviceArea(GatewayInfo gatewayInfo) {
        return gatewayInfoMapper.queryByDeviceArea(gatewayInfo);
    }

    @Override
    public Map queryList4Page(int page,int pageSize,GatewayInfo gatewayInfo) {
    	Map backMap=new HashMap<>();
        if(-1 != pageSize)
        {
            PageHelper.startPage(page, pageSize);
        }
		List<GatewayInfo> list=gatewayInfoMapper.queryListPage(gatewayInfo);

        String gatewayAreaId;
        Area area;
        for(GatewayInfo info : list)
        {
            gatewayAreaId = info.getGatewayAreaId();
            if(StringUtils.isNotBlank(gatewayAreaId))
            {
                area = amsAreaService.findAreaById(Integer.valueOf(gatewayAreaId));
                if(null != area)
                {
                    info.setGatewayAreaName(area.getName());
                }
            }
        }

        if(-1 != pageSize)
        {
            backMap.put("page", ((Page)list).getPageNum());
            backMap.put("pageSize", ((Page)list).getPageSize());
            backMap.put("total", ((Page)list).getTotal());
            backMap.put(Constant.DATA, ((Page)list).getResult());
        }
        else
        {
            backMap.put(Constant.DATA, list);
        }

    	return backMap;
    }

    
    @Override
    public PageBean<GatewayInfo> queryListPage(PageBean<GatewayInfo> page) {
        return null;//gatewayInfoMapper.queryListPage(page);
    }

    @Override
    public void addGatewayInfo(GatewayInfo gatewayInfo) {
        gatewayInfo.setGatewayUuid(gatewayInfo.getGatewayFactoryCode() + Constant.SEPARATOR + gatewayInfo.getGatewaySerialnumber());
        gatewayInfoMapper.insert(gatewayInfo);
    }

    @Override
    public void addSelectiveGatewayInfo(GatewayInfo gatewayInfo) {
        gatewayInfo.setGatewayUuid(UniqueUtil.uuid());
        gatewayInfoMapper.insertSelective(gatewayInfo);
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName() + "成功!详情：" + JSON.toJSONString(gatewayInfo));

    }

    @Override
    public void updateGatewayInfo(GatewayInfo gatewayInfo) {
        gatewayInfoMapper.updateByPrimaryKey(gatewayInfo);
    }

    @Override
    public void updateSelectGatewayInfo(GatewayInfo gatewayInfo) {
        gatewayInfoMapper.updateByPrimaryKeySelective(gatewayInfo);
        logger.info(Thread.currentThread().getStackTrace()[1].getMethodName() + "成功!详情：" + JSON.toJSONString(gatewayInfo));
    }

    @Override
    public void delGatewayInfo(String gatewayInfoUuid) {
        gatewayInfoMapper.deleteByPrimaryKey(gatewayInfoUuid);
    }

    @Override
    public GatewayInfo selectByUuid(String gatewayInfoUuid) {
        return gatewayInfoMapper.selectByPrimaryKey(gatewayInfoUuid);
    }

    @Override
    public GatewayInfo selectGatewayInfo(GatewayInfo gatewayInfo) {
        return gatewayInfoMapper.selectGatewayInfo(gatewayInfo);
    }

    @Override
    public List<GatewayInfo> queryListByIds(List<String> ids) {
        return gatewayInfoMapper.queryListByIds(ids);
    }


    @Override
    public List<GatewayInfo> getDeviceFactory() {
        // TODO Auto-generated method stub
        return gatewayInfoMapper.getDeviceFactory();
    }

    @Override
    public void updateInformGatewayInfo(Inform inform) {
        // 查询获取网关信息
        GatewayInfo gatewayInfo = getResultGatewayInfo(inform);
        if(gatewayInfo == null){ 
        	return;
        }
        EventStruct[] list = inform.getEvent().getEventCodes();
        List<String> events = new ArrayList<>();
        for (EventStruct eventStruct : list) {
            events.add(eventStruct.getEvenCode());
        }
        InformInfo informInfo = new InformInfo();

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
            this.executeUpdate(gatewayInfo, inform, events);
            informInfo.setInformState(Integer.parseInt(InformStateEnum.INFORM_STATE_0.code()));
        }
        //当event为重启完成
        if(events.contains(EventCodeEnum.EVENT_CODE_M_REBOOT.code()) || events.contains(EventCodeEnum.EVENT_CODE_BOOTSTRAP.code())){
        	//redis已经设置了超时时间，不管有无，直接做删除处理(重启和恢复出厂指令key加上了R-F-前缀， 保证唯一不和其他指令冲突)
        	redisClientTemplate.del("R-F-" + gatewayInfo.getGatewaySerialnumber());
        }
//        // 更新网关信息
//        updateSelectGatewayInfo(gatewayInfo);
        // TODO 诊断完成后 线路详细信息 拨号检查结果 DHCP检查结果 PING检查结果 都会上报 之后修改直接填充页面
        if (events.contains(EventCodeEnum.EVENT_CODE_DIAGNOSTICSCOMPLETE.code())) {
            instructionsService.updateInstructionsInfo(getParameterKey(inform), InstructionsStatusEnum.STATUS_1.code(), informInfo);
        }
        this.addInformInfo(informInfo, inform);
    }

    /**
     * 获取承载INTERNET业务的WAN连接前缀
     * @param mac
     * @return 不存在时返回null,存在时返回InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANPPPConnection.
     * 或InternetGatewayDevice.WANDevice.{i}.WANConnectionDevice.{i}.WANIPConnection.
     */
    public String getWANConnectionPrefix(String mac) {
        //获取WanDevice所有子节点
        Map<String, Object> wanDevices = instructionMethodService.getParameterNames(mac,
                "InternetGatewayDevice.WANDevice.", false);

        //查找承载Internet的WAN连接
        List<String> services = new ArrayList<>();
        List<String> namesList = new ArrayList<>();

        String pppServicePattern = "InternetGatewayDevice.WANDevice.[0-9]+.WANConnectionDevice.[0-9]+.WANPPPConnection.[0-9]+.X_CMCC_ServiceList";
        Pattern pattern = Pattern.compile(pppServicePattern);
        for (String name : wanDevices.keySet()) {
            Matcher m = pattern.matcher(name);
            if (m.find()) {
                services.add(name);
                namesList.add(name);
            }
        }

        String ipServicePattern = "InternetGatewayDevice.WANDevice.[0-9]+.WANConnectionDevice.[0-9]+.WANIPConnection.[0-9]+.X_CMCC_ServiceList";
        pattern = Pattern.compile(ipServicePattern);
        for (String name : wanDevices.keySet()) {
            Matcher m = pattern.matcher(name);
            if (m.find()) {
                services.add(name);
                namesList.add(name);
            }
        }

        Map<String, Object> resultMap = instructionMethodService.getParameterValues(mac, namesList);
        String servicePath = null;
        for (String service : services) {
            String v = (String) resultMap.get(service);
            if (v != null && v.indexOf("INTERNET") > 0) {
                servicePath = service;
                break;
            }
        }

        String prefix = null;
        if (servicePath != null) {
            //确定WAN连接前缀
            prefix = servicePath.substring(0, servicePath.indexOf("X_CMCC_ServiceList"));
        }

        return prefix;
    }
    
    
    @Override
    public void updateGatewayFirmwareInfo(GatewayInfo record) {
       gatewayInfoMapper.updateGatewayFirmwareInfo(record);
    }

    /*private boolean setGatewayParams(String mac,String account, String password, String familyPassword, String rmsAccount, String rmsPassword) {
		
    	List<ParameterValueStruct> list = new ArrayList<ParameterValueStruct>();
    	ParameterValueStruct<String> struct = new ParameterValueStruct<String>();
    	struct.setName(ConstantDiagnose.MANAGEMENT_SERVER_USERNAME);
    	struct.setValue(account);
    	struct.setReadWrite(true);
    	struct.setValueType(ParameterValueStruct.Type_String);
    	list.add(struct);
    	ParameterValueStruct<String> passwordStruct = new ParameterValueStruct<String>();
    	passwordStruct.setName(ConstantDiagnose.MANAGEMENT_SERVER_PASSWORD);
    	passwordStruct.setValue(password);
    	passwordStruct.setReadWrite(true);
    	passwordStruct.setValueType(ParameterValueStruct.Type_String);
    	list.add(passwordStruct);
    	ParameterValueStruct<String> familyPasswordStruct = new ParameterValueStruct<String>();
    	familyPasswordStruct.setName("InternetGatewayDevice.DeviceInfo.X_CMCC_TeleComAccount.Password");
    	familyPasswordStruct.setValue(familyPassword);
    	familyPasswordStruct.setReadWrite(true);
    	familyPasswordStruct.setValueType(ParameterValueStruct.Type_String);
    	list.add(familyPasswordStruct);
    	ParameterValueStruct<Integer> statusStruct = new ParameterValueStruct<Integer>();
    	statusStruct.setName("InternetGatewayDevice.X_CMCC_UserInfo.Status");
    	statusStruct.setValue(0);
    	statusStruct.setReadWrite(true);
    	statusStruct.setValueType(ParameterValueStruct.Type_UnsignedInt);
    	list.add(statusStruct);
    	ParameterValueStruct<Integer> resultStruct = new ParameterValueStruct<Integer>();
    	resultStruct.setName("InternetGatewayDevice.X_CMCC_UserInfo.Result");
    	resultStruct.setValue(0);
    	resultStruct.setReadWrite(true);
    	resultStruct.setValueType(ParameterValueStruct.Type_UnsignedInt);
    	list.add(resultStruct);
    	ParameterValueStruct<String> rmsAccountStruct = new ParameterValueStruct<String>();
    	rmsAccountStruct.setName("InternetGatewayDevice.ManagementServer.ConnectionRequestUsername");
    	rmsAccountStruct.setValue(rmsAccount);
    	rmsAccountStruct.setReadWrite(true);
    	rmsAccountStruct.setValueType(ParameterValueStruct.Type_String);
    	list.add(rmsAccountStruct);
    	ParameterValueStruct<String> rmsPasswordStruct = new ParameterValueStruct<String>();
    	rmsPasswordStruct.setName("InternetGatewayDevice.ManagementServer.ConnectionRequestPassword");
    	rmsPasswordStruct.setValue(rmsPassword);
    	rmsPasswordStruct.setReadWrite(true);
    	rmsPasswordStruct.setValueType(ParameterValueStruct.Type_String);
    	list.add(rmsPasswordStruct);
    	
    	boolean flag = instructionMethodService.setParameterValue(mac, list);
    	
    	List<ParameterValueStruct> list2 = new ArrayList<ParameterValueStruct>();
		ParameterValueStruct<Integer> resultStruct2 = new ParameterValueStruct<Integer>();
		resultStruct2.setName("InternetGatewayDevice.X_CMCC_UserInfo.Result");
		resultStruct2.setValue(1);
		resultStruct2.setReadWrite(true);
		resultStruct2.setValueType(ParameterValueStruct.Type_UnsignedInt);
		list2.add(resultStruct2);
		
		instructionMethodService.setParameterValue(mac, list2);
		
		return flag;
	}*/

	private String getParameterKey(Inform informReq) {
        // 遍历
        List<ParameterValueStruct> parameterValueStructs = informReq.getParameterList().getParameterValueStructs();
        for (ParameterValueStruct parameterValueStruct : parameterValueStructs) {
            // 获取requestId
            if (org.apache.commons.lang.StringUtils.equals(GatewayDMSEnum.GATEWAYDMS_MANAGEMENTSERVER_PARAMETERKEY.code(), parameterValueStruct.getName())) {
                // RMS 向智能网关发起连接请求通知时所使用的HTTP URL
                return parameterValueStruct.getValue() + "";
            }
        }
        return "";
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
        GatewayInfo resultGatewayInfo = selectGatewayInfo(gatewayInfo);
        return resultGatewayInfo;
    }


    /**
     * 执行存数据库 和 存redis
     *  @param gatewayInfo
     * @param inform
     * @param events
     */
    private void executeUpdate(GatewayInfo gatewayInfo, Inform inform, List<String> events) {
        logger.info("执行网关信息修改操作");
/*
        AdminUser user = (AdminUser) SecurityUtils.getSubject().getSession().getAttribute(Constant.SESSION_LOGIN_ACCOUNT);
        logger.info("操作用户：" + user.getAdminAccount() + ",操作方法：" + Thread.currentThread().getStackTrace()[1].getMethodName());
*/

        Object url = null;
        Object version = null;
        Object hardVersion = null;
        Object externalIPaddress = null;
        Object gatewayIp = null;
        // 遍历
        List<ParameterValueStruct> parameterValueStructs = inform.getParameterList().getParameterValueStructs();
        for (ParameterValueStruct parameterValueStruct : parameterValueStructs) {
            // 终端回连URL
            if (org.apache.commons.lang.StringUtils.equals(GatewayDMSEnum.GATEWAYDMS_CONNECTIONREQUESTURL.code(), parameterValueStruct.getName())) {
                // RMS 向智能网关发起连接请求通知时所使用的HTTP URL
                url = parameterValueStruct.getValue();
                logger.info("ConnectionRequestURL：" + url);
            } else if (org.apache.commons.lang.StringUtils.equals(GatewayDMSEnum.GATEWAYDMS_SOFTWAREVERSION.code(), parameterValueStruct.getName())) {
                // 软件版本
                version = parameterValueStruct.getValue();
                logger.info("SoftwareVersion：" + version);
            } else if (org.apache.commons.lang.StringUtils.equals(GatewayDMSEnum.GATEWAYDMS_HARDWAREVERSION.code(), parameterValueStruct.getName())) {
                // 硬件版本
            	hardVersion = parameterValueStruct.getValue();
                logger.info("HardwareVersion：" + hardVersion);
            }else if (parameterValueStruct.getName().contains(Constant.EXTERNAL_IPADDRESS)) {
                // 网关使用哪一个WAN连接（TR069节点全路径）进行PING是否满足条件
                if (getRegex(parameterValueStruct.getName())) {
                    externalIPaddress = parameterValueStruct.getName().substring(0, parameterValueStruct.getName().lastIndexOf("."));
                    gatewayIp = parameterValueStruct.getValue();
                    logger.info("externalIPaddress：" + externalIPaddress);
                }
            } else if(org.apache.commons.lang.StringUtils.equals(GatewayDMSEnum.GATEWAYDMS_SERIALNUMBER.code(), parameterValueStruct.getName())){
                // 设备序列号  修改redis
                // 恢复出厂设置 、重启完成
                if (events.contains(EventCodeEnum.EVENT_CODE_BOOT.code())){
                    instructionsService.updateInstructionsInfo(gatewayInfo.getGatewaySerialnumber(),RebootEnum.STATUS_2.code());
                }
            }
        }

        GatewayInfo updatebean = new GatewayInfo();
        updatebean.setGatewayUuid(gatewayInfo.getGatewayUuid());//update 主键
        updatebean.setGatewayAdslAccount(gatewayInfo.getGatewayAdslAccount());
        
        updatebean.setGatewayConnectionrequesturl(url == null? null : url.toString());
        updatebean.setGatewayVersion(version  == null? null : version.toString());
        updatebean.setGatewayHardwareVersion(hardVersion==null?null:hardVersion.toString());
        updatebean.setGatewayExternalIPaddress(externalIPaddress == null? null : externalIPaddress.toString());
        updatebean.setGatewayIpaddress(gatewayIp==null?null:gatewayIp.toString());

        if (gatewayInfo.getGatewayDeviceUuid() != null) {
            String firmId = getCurFirmId(gatewayInfo.getGatewayDeviceUuid(), version+"");
            if (!"".equals(firmId) &&  !firmId.equals(gatewayInfo.getGatewayFirmwareUuid()) ) {
                updatebean.setGatewayFirmwareUuid(firmId);
                updatebean.setGatewayVersion(version+"");
            }
        }
        
        //将version同步给BMS
        try {
			Map<String,Object> syncMap = new HashMap<>();
			syncMap.put("gatewayInfoMacaddress",gatewayInfo.getGatewayMacaddress());
			syncMap.put("firmwareVer", version);
			logger.info("executeUpdate开始调用pluginDeviceService.updatePluginLeadGatewayData通知BMS，请求数据为{}", syncMap);
			Map<String,Object> retmap = pluginDeviceService.updatePluginLeadGatewayData(syncMap);
			logger.info("executeUpdate结束调用pluginDeviceService.updatePluginLeadGatewayData通知BMS，返回结果为{}", retmap);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("executeUpdate调用pluginDeviceService.updatePluginLeadGatewayData通知BMS时异常"+e);
		}
        
        
     // 最近连接时间
        long timeMillis = System.currentTimeMillis();
        long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
        updatebean.setGatewayLastConnTime((int) timeSeconds);
        
        //验证 Inform上报当中的设备型号(ProductClass)与网关设备型号(gateway_model)是否一至
		logger.info("获取验证 Inform上报当中设备型号(ProductClass):{} 与网关设备型号(gateway_model):{}是否一至条件", (null == inform ? null : inform.getDeviceId().getProductClass()), (null == gatewayInfo ? null : gatewayInfo.getGatewayModel()));
		if (null != inform && null != gatewayInfo && StringUtils.isNotBlank(inform.getDeviceId().getProductClass()) && StringUtils.isNotBlank(gatewayInfo.getGatewayModel())) {
			if (!inform.getDeviceId().getProductClass().trim().equals(gatewayInfo.getGatewayModel().trim())) {
				logger.info("获取 inform 上报的 OUI为:{} 固件版本为相关参数集:{}", inform.getDeviceId().getOui(), null != inform.getParameterList() ? inform.getParameterList().getParameterValueStructs() : null);
				if (StringUtils.isNotBlank(inform.getDeviceId().getOui()) && null != inform.getParameterList() && inform.getParameterList().getParameterValueStructs().size() > 0) {
					for (ParameterValueStruct<?> pv : inform.getParameterList().getParameterValueStructs()) {
						logger.info("获取到 inform 上报ParameterValueStruct名称:{} 值为:{}", pv.getName(), pv.getValue());
						if (StringUtils.isNotBlank(pv.getName()) && "InternetGatewayDevice.DeviceInfo.SoftwareVersion".equals(pv.getName().trim())) {
							String firmwareVersion = null != pv.getValue() && StringUtils.isNotBlank(pv.getValue().toString()) ? pv.getValue().toString().trim() : null;
							if (StringUtils.isNotBlank(firmwareVersion)) {
								Map<String,Object> sql = new HashMap<String,Object>();
								sql.put("factoryCode", inform.getDeviceId().getOui().trim());
								sql.put("deviceModel", inform.getDeviceId().getProductClass().trim());
								sql.put("firmwareVersion", firmwareVersion);
								List<Map<String,Object>> result = gatewayInfoMapper.selectDeviceOrOuiOrFirmwareInfo(sql);
								logger.info("根据查询条件:{} 查询对应的设备与固件信息:{} 如果查询无结果与数据有多条则不执行数据维护", sql, result);
								if (null != result && result.size() == 1) {
									String firmwareId = null != result.get(0).get("firmwareId") && StringUtils.isNotBlank(result.get(0).get("firmwareId").toString()) ? result.get(0).get("firmwareId").toString().trim() : null;
									String deviceId = null != result.get(0).get("deviceId") && StringUtils.isNotBlank(result.get(0).get("deviceId").toString()) ? result.get(0).get("deviceId").toString().trim() : null;
									if(StringUtils.isNotBlank(firmwareId) && StringUtils.isNotBlank(deviceId)){
										updatebean.setGatewayModel(inform.getDeviceId().getProductClass().trim());
										updatebean.setGatewayFirmwareUuid(firmwareId);
										//上面已经有此字段更新
										//updatebean.setGatewayVersion(firmwareVersion);
										updatebean.setGatewayDeviceUuid(deviceId);
										logger.info("更新网关的设备网关型号(gateway_model):{} 设备UUID(gateway_device_uuid):{} 当前所用的固件文件编号UUID(gateway_firmware_uuid):{}", inform.getDeviceId().getProductClass().trim(), deviceId,
												firmwareId);
									}
								}
							}
						}
					}
				}
				// ----------修改网关设备型号对应查询条件
				// select
				// a.id deviceId,
				// a.device_type deviceType,
				// a.device_model deviceModel,
				// a.device_name deviceName,
				// a.device_factory deviceFactory,
				// a.device_memo deviceMemo,
				// b.id ouiId,
				// b.factory_code ouiCode,
				// b.manufacturer_id manufacturerId,
				// c.id firmwareId,
				// c.firmware_name firmwareName,
				// c.firmware_version firmwareVersion,
				// c.firmware_path firmwarePath,
				// c.firmware_size firmwareSize,
				// c.firmware_create_time firmwareCreate_time,
				// c.firmware_description firmwareDescription,
				// c.check_status checkStatus,
				// c.upload_md5 uploadMd5,
				// c.input_md5 inputMd5,
				// c.area_id areaId
				// from
				// t_device_info a ,
				// t_factory_info b ,
				// t_firmware_info c
				// where
				// 1=1
				// -- inform 上报OUI
				// and b.factory_code = '1C25E1'
				// and b.manufacturer_id = a.device_factory
				// -- inform 上报设备型号
				// and a.device_model = 'GM219-S'
				// and c.device_id = a.id
				// and c.device_model = a.device_model
				// -- inform 上报固件版本
				// and c.firmware_version = 'V1.0.00.054'
				// -----------修改网关设备型号对应查询条件
        		
        	 }
        }
        
        // 根据SN更新网关url 版本 固件id信息
        updateSelectGatewayInfo(updatebean);
        

    }

    private String getCurFirmId(String deviceId, String version) {
        //从固件表中根据设备ID和版本查询
        try {
//            FirmwareInfo firmwareInfo = new FirmwareInfo();
//            firmwareInfo.setDeviceId(deviceId);
//            firmwareInfo.setFirmwareVersion(version);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("deviceId", deviceId);
            params.put("version", version);

            Map<String, Object> result = firmwareInfoService.searchFirmwareVersion(params);
            logger.info("firmwareInfoService.findFirmwareInfo成功!详情：" + JSON.toJSONString(result));
            if(result != null && !result.isEmpty() && result.get("resultCode").equals(0) && result.get("data") != null){
            	Map<String, String> data = (Map<String, String>) result.get("data");
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    if(version.equals(entry.getValue()))
                    {
                        return entry.getKey();
                    }
                }
            }
           /* if (result == null) {
                //插入一条固件信息
                firmwareInfoService.addFirmwareInfo(firmwareInfo);
                logger.info("firmwareInfoService.addFirmwareInfo成功!详情：" + JSON.toJSONString(firmwareInfo));
                return firmwareInfo.getFirmwareInfoUuid();
            } else {
                return result.getFirmwareInfoUuid();
            }*/
        } catch (Exception e) {
            logger.error("查找固件版本失败", e);
        }
        return "";

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
//        LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(informReq), informReq.getRequestId(), "", "上报信息", LogTypeEnum.LOG_TYPE_SYSTEM.description());

    }


    @Override
    public List<GatewayInfo> queryGatewayInfoList(GatewayInfo gatewayInfo) {
        // TODO Auto-generated method stub
        return gatewayInfoMapper.queryGatewayInfoList(gatewayInfo);
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
	public void batchInsertGatewayInfo(List<GatewayInfo> allDatas) {

		gatewayInfoMapper.batchInsertGatewayInfo(allDatas);
	}

    @Override
    public int insertSelective(GatewayInfo info) {
        return gatewayInfoMapper.insertSelective(info);
    }

    /**
     * 更新备份文件数量限制
     *
     * @param gatewayIds 多个逗号隔开
     * @param maxNumber
     * @return
     */
    @Override
    public void updateBackupFileMaxNumber(String gatewayIds, int maxNumber) {
        if(StringUtils.isBlank(gatewayIds))
        {
            return;
        }
        String[] ids = gatewayIds.split(",");
        if(ids.length < 1)
        {
            return;
        }

        Map<String, Object> para = new HashMap<>();
        para.put("ids", ids);
        para.put("backupFileMaxNumber", maxNumber);
        gatewayInfoMapper.updateBackupFileMaxNumber(para);
    }

    /**
     * 更新网关日志开关状态
     *
     * @param gatewayId
     * @param status
     */
    @Override
    public void updateLogSwitchStatus(String gatewayId, int status) {
        if(StringUtils.isBlank(gatewayId))
        {
            return;
        }

        Map<String, Object> para = new HashMap<>();
        para.put("gatewayId", gatewayId);
        para.put("logSwitchStatus", status);
        gatewayInfoMapper.updateLogSwitchStatus(para);
    }

    /**
     * 查询digest账号密码
     *
     * @param gatewayDigestAccount
     * @return
     */
    @Override
    public List<String> queryDigestPassword(String gatewayDigestAccount) {
        List<Map<String, Object>> mapList = gatewayInfoMapper.queryDigestPassword(gatewayDigestAccount);
        List<String> retList = new ArrayList<>();
        if(null != mapList && mapList.size() > 0)
        {
            for(Map<String, Object> map : mapList)
            {
                if(StringUtils.isNotBlank(map.get("password").toString()))
                {
                    retList.add(map.get("password").toString());
                }

            }
        }
        return retList;
    }

	@Override
	public List<Map<String, Object>> queryGatewayListByAreas(Map<String, Object> areaIds) {
		
		return gatewayInfoMapper.queryGatewayListByAreas(areaIds);
	}

	@Override
	public void updateGatewayAreaIdByPassword(GatewayInfo gatewayInfo) {
		
		gatewayInfoMapper.updateGatewayAreaIdByPassword(gatewayInfo);
	}

	@Override
	public List<Map<String, Object>> queryGatewayFirmVersionAndFlowrateByAreaIds(
			Map<String, Object> params) {
		
		return gatewayInfoMapper.queryGatewayFirmVersionAndFlowrateByAreaIds(params);
		
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void addbatchGatewayHardwareQueue(List<GatewayInfo> gatewayList, List<HardwareAblity> hardwareList, List<GatewayQueue> queueList) throws Exception {

		int loop = 0;
		int lastSize = gatewayList.size() % 5000;
		if (lastSize == 0) {
			loop = gatewayList.size() / 5000;
		} else {
			loop = gatewayList.size() / 5000 + 1;
		}

		for (int j = 0; j < loop; j++) {
			//最后一次
			if (j+1 == loop&&lastSize>0) {
				gatewayInfoMapper.batchInsertGatewayInfoNew(gatewayList.subList(j * 5000, j * 5000+lastSize));
				
			}else {
				gatewayInfoMapper.batchInsertGatewayInfoNew(gatewayList.subList(j * 5000, (j + 1) * 5000));
			}

		}

        lastSize = queueList.size() % 5000;
        if (lastSize == 0) {
            loop = queueList.size() / 5000;
        } else {
            loop = queueList.size() / 5000 + 1;
        }

        for (int j = 0; j < loop; j++) {
            //最后一次
            if (j+1 == loop&&lastSize>0) {
                gatewayQueueMapper.batchInsertGatewayQueue(queueList.subList(j * 5000, j * 5000+lastSize));
            }else {
                gatewayQueueMapper.batchInsertGatewayQueue(queueList.subList(j * 5000, (j + 1) * 5000));
            }

        }

	}

	@Override
	public Map<String, Object> queryManufacturerCodeByFactoryCode(
			String gatewayFactoryCode) {

		return gatewayInfoMapper.queryManufacturerCodeByFactoryCode(gatewayFactoryCode);
	}

    /**
     * 清楚网关宽带账号和密码
     *
     * @param gatewayInfo
     */
    @Override
    public void clearPasswordAndAdslAccount(GatewayInfo gatewayInfo) {
        gatewayInfoMapper.clearPasswordAndAdslAccount(gatewayInfo);
    }

}
