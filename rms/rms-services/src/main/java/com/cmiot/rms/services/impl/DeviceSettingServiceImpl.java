/**
 * 
 */
package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.common.enums.RebootEnum;
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessOpenDetailMapper;
import com.cmiot.rms.dao.mapper.GatewayPasswordMapper;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.DeviceSettingService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.boxManager.instruction.BoxInvokeInsService;
import com.cmiot.rms.services.constants.Constants;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.instruction.InvokeInsService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.tydic.inter.app.service.GatewayHandleService;

/**
 * @author heping
 *
 */
public class DeviceSettingServiceImpl implements DeviceSettingService {

	public static final Logger logger = LoggerFactory.getLogger(DeviceSettingServiceImpl.class);

	@Autowired
	private GatewayInfoService gatewayInfoService;

	@Autowired
	InstructionMethodService instructionMethodService;

	@Autowired
	private InvokeInsService invokeInsService;

	@Autowired
	private RedisClientTemplate redisClientTemplate;
	
	@Autowired
	private BoxInfoMapper boxInfoMapper;
	
    @Autowired
    BoxInvokeInsService boxInvokeInsService;
    
    @Autowired
    private GatewayPasswordMapper gatewayPasswordMapper;
    
    @Autowired
    private GatewayBusinessMapper gatewayBusinessMapper;
    
    @Autowired
    private GatewayBusinessOpenDetailMapper gatewayBusinessOpenDetailMapper;

	@Value("${pboss.timeout}")
	int timeout;
	
	@Autowired
    private GatewayHandleService gatewayHandleService;
	

	@Override
	public Map<String, Object> enableSSID(Map<String, Object> parameter) {
		logger.info("enableSSID,parameter: {}", parameter.toString());

		parameter.put("Enable", "TRUE");
		return setSSID(parameter);
	}

	@Override
	public Map<String, Object> disableSSID(Map<String, Object> parameter) {
		logger.info("disableSSID,parameter: {}", parameter.toString());

		parameter.put("Enable", "FALSE");
		return setSSID(parameter);
	}

	@Override
	public Map<String, Object> modifySSIDPwd(Map<String, Object> parameter) {
		logger.info("modifySSIDPwd,parameter: {}", parameter.toString());

		Map<String, Object> result = new HashMap<String, Object>();
		String cpeid = (String) parameter.get("CPEID");
		String password = (String) parameter.get("Password");
		int instanceID = null == (Integer) parameter.get("InstanceID") ? 1 : (Integer) parameter.get("InstanceID");
		if (validateParameter(cpeid, password)) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			return result;
		}

		// 检测是否存在网关
		GatewayInfo searchInfo = new GatewayInfo();
		searchInfo.setGatewaySerialnumber(cpeid.substring(cpeid.indexOf("-") + 1));
		GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
		if (null == gatewayInfo) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			logger.info("网关不存在");
			return result;
		}

		Map<String, Object> wanDeviceMap = instructionMethodService
				.getParameterNamesErrorCode(gatewayInfo.getGatewayMacaddress(), "InternetGatewayDevice.LANDevice.", false);
		if(Integer.valueOf(wanDeviceMap.get("result").toString()) == -1)
		{
			if(null != wanDeviceMap.get("errorCode"))
			{
				result.put("Result", Integer.valueOf(wanDeviceMap.get("errorCode").toString()));
			}
			else
			{
				result.put("Result", Constants.FAIL);
			}
			result.put("CPEID", cpeid);
			return result;
		}

		String regLANDevice = "InternetGatewayDevice.LANDevice.[0-9]+.WLANConfiguration." + instanceID
				+ ".PreSharedKey.[0-9]+.KeyPassphrase";
		String preSharedKeyPath = "",keyPassphrase = "";


		for (Map.Entry<String, Object> entry : wanDeviceMap.entrySet()) {
			Pattern pattern = Pattern.compile(regLANDevice);
			Matcher matcher = pattern.matcher(entry.getKey());
			if (matcher.find()) {
				preSharedKeyPath = entry.getKey();
				break;
			}
		}

		keyPassphrase = preSharedKeyPath.replaceAll("KeyPassphrase", "PreSharedKey");
		logger.info("keyPassphrase:{}", keyPassphrase);
		logger.info("preSharedKeyPath:{}", preSharedKeyPath);
		
		List<ParameterValueStruct> list = new ArrayList<ParameterValueStruct>();
		ParameterValueStruct nameStruct = new ParameterValueStruct();
		nameStruct.setName(preSharedKeyPath);
		nameStruct.setValue(password);
		nameStruct.setValueType("string");
		nameStruct.setReadWrite(true);

		ParameterValueStruct valueStruct = new ParameterValueStruct();
		valueStruct.setName(keyPassphrase);
		valueStruct.setValue(password);
		valueStruct.setValueType("string");
		valueStruct.setReadWrite(true);

		list.add(nameStruct);
		list.add(valueStruct);
		Map<String, Object> setMap = instructionMethodService.setParameterValueErrorCode(gatewayInfo.getGatewayMacaddress(), list);
		if(Integer.valueOf(setMap.get("result").toString()) == -1)
		{
			if(null != setMap.get("errorCode"))
			{
				result.put("Result", Integer.valueOf(setMap.get("errorCode").toString()));
			}
			else
			{
				result.put("Result", Constants.FAIL);
			}
			result.put("CPEID", cpeid);
			logger.info("节点设置失败");
			return result;
		}

		result.put("Result", Constants.SUCCESS);
		result.put("CPEID", cpeid);
	
		return result;
	}

	@Override
	public Map<String, Object> factoryReset(Map<String, Object> parameter) {
		logger.info("factoryReset,parameter: {}", parameter.toString());

		parameter.put("methodName", Constants.FACTORYRESET);
		Map<String, Object> result = sendCmd(parameter);
		if(Constants.SUCCESS == (int)result.get("Result")){
			//成功，清空宽带帐号 和 各业务的开通状态
			String cpeid = (String) parameter.get("CPEID");
			GatewayInfo searchInfo = new GatewayInfo();
			searchInfo.setGatewaySerialnumber(cpeid.substring(cpeid.indexOf("-") + 1));
			GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
			//删除t_gateway_password的记录
           /* int m = gatewayPasswordMapper.deleteByAdslNo(gatewayInfo.getGatewayAdslAccount());
            logger.info(" invoke broadBandUnsubcribe删除t_gateway_password的记录,m:{},PppoeAccount:{}",m,gatewayInfo.getGatewayAdslAccount());
*/
			try {
     			Map<String, Object> bmsResult =  gatewayHandleService.factoryNotify(gatewayInfo.getGatewayMacaddress(), "4", false);
     			logger.info("恢复出厂通知BMS结果,GatewayMacaddress:{},执行结果:{},结果描述:{}", gatewayInfo.getGatewayMacaddress(), bmsResult.get("resultCode"), bmsResult.get("resultMsg"));
     			logger.info("通知BMS恢复出厂设置，请求MAC:"+ gatewayInfo.getGatewayMacaddress() +",由于异步处理，不等待BMS返回，所以返回结果为空");
			} catch (Exception e) {
     			//logger.error("恢复出厂通知BMS异常", e);
     		}
			
            //更新业务开通状态为未开通
            Map<String, Object> parm = new HashMap<String, Object>();
            parm.put("status", "0");
            parm.put("gatewayUuid", gatewayInfo.getGatewayUuid());
            gatewayBusinessOpenDetailMapper.updateOpenStatusByGatewayUuid(parm);
			
			gatewayInfo.setGatewayAdslAccount("");
			gatewayInfoService.updateSelectGatewayInfo(gatewayInfo);
			
		}
		
		return result;
	}

	@Override
	public Map<String, Object> reboot(Map<String, Object> parameter) {
		logger.info("reboot,parameter:{}", parameter.toString());

		parameter.put("methodName", Constants.REBOOT);
		return sendCmd(parameter);
	}

	@Override
	public Map<String, Object> resetpppoe(Map<String, Object> parameter) {
		logger.info("resetpppoe,parameter:{}", parameter.toString());

		Map<String, Object> result = new HashMap<String, Object>();
		String cpeid = (String) parameter.get("CPEID");
		String username = (String) parameter.get("Username");
		String password = (String) parameter.get("Password");

		if (validate(parameter)) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			return result;
		}

		// 检测是否存在网关
		GatewayInfo searchInfo = new GatewayInfo();
		searchInfo.setGatewaySerialnumber(cpeid.substring(cpeid.indexOf("-") + 1));
		GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
		if (null == gatewayInfo) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			logger.info("网关不存在");
			return result;
		}

		Map<String, Object> wanDeviceMap = instructionMethodService
				.getParameterNamesErrorCode(gatewayInfo.getGatewayMacaddress(), "InternetGatewayDevice.WANDevice.", false);

		if(Integer.valueOf(wanDeviceMap.get("result").toString()) == -1)
		{
			if(null != wanDeviceMap.get("errorCode"))
			{
				result.put("Result", Integer.valueOf(wanDeviceMap.get("errorCode").toString()));
			}
			else
			{
				result.put("Result", Constants.FAIL);
			}
			result.put("CPEID", cpeid);
			logger.info("查询节点名称失败");
			return result;
		}

		String regWANDevice = "InternetGatewayDevice.WANDevice.[0-9]+.WANConnectionDevice.[0-9]+.WANPPPConnection.[0-9]+.Username";
		String namePath = null;
		String valuePath = null;

		for (Map.Entry<String, Object> entry : wanDeviceMap.entrySet()) {
			Pattern pattern = Pattern.compile(regWANDevice);
			Matcher matcher = pattern.matcher(entry.getKey());
			if (matcher.find()) {
				namePath = entry.getKey();
				break;
			}
		}

		if (null == namePath) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			logger.info("没有匹配到用户名");
			return result;
		}
		valuePath = namePath.replaceAll("Username", "Password");
		logger.info("Username:{}",namePath);
		logger.info("Password:{}", valuePath);

		List<ParameterValueStruct> list = new ArrayList<ParameterValueStruct>();
		ParameterValueStruct nameStruct = new ParameterValueStruct();
		nameStruct.setName(namePath);
		nameStruct.setValue(username);
		nameStruct.setValueType("string");
		nameStruct.setReadWrite(true);

		ParameterValueStruct valueStruct = new ParameterValueStruct();
		valueStruct.setName(valuePath);
		valueStruct.setValue(password);
		valueStruct.setValueType("string");
		valueStruct.setReadWrite(true);

		list.add(nameStruct);
		list.add(valueStruct);

		Map<String, Object> setMap = instructionMethodService.setParameterValueErrorCode(gatewayInfo.getGatewayMacaddress(), list);
		if(Integer.valueOf(setMap.get("result").toString()) == -1)
		{
			if(null != setMap.get("errorCode"))
			{
				result.put("Result", Integer.valueOf(setMap.get("errorCode").toString()));
			}
			else
			{
				result.put("Result", Constants.FAIL);
			}
			result.put("CPEID", cpeid);
			logger.info("节点设置失败");
			return result;
		}else if(Integer.valueOf(setMap.get("result").toString()) == 0){
			gatewayInfo.setGatewayAdslAccount(username);
			gatewayInfoService.updateSelectGatewayInfo(gatewayInfo);

		}

		result.put("Result", Constants.SUCCESS);
		result.put("CPEID", cpeid);
		return result;
	}

	private boolean validate(Map<String, Object> parameter) {

		String cpeid = (String) parameter.get("CPEID");
		String username = (String) parameter.get("Username");
		String password = (String) parameter.get("Password");

		if (StringUtils.isBlank(cpeid)) {
			return true;
		}

		if (StringUtils.isBlank(username)) {
			return true;
		}

		if (StringUtils.isBlank(password)) {
			return true;
		}
		// cpeid为HguId 或 StbId，格式为：OUI-SN
		if (cpeid.indexOf("-") == -1) {
			return true;
		}

		return false;
	}

	private Map<String, Object> setSSID(Map<String, Object> parameter) {

		Map<String, Object> result = new HashMap<String, Object>();
		List<String> preLANDeviceList = new ArrayList<String>();
		String cpeid = (String) parameter.get("CPEID");
		Integer instanceID = null == (Integer) parameter.get("InstanceID") ? 1 : (Integer) parameter.get("InstanceID");
		String mac = null;

		if (validate(cpeid)) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			return result;
		}

		// 检测是否存在网关
		GatewayInfo searchInfo = new GatewayInfo();
		searchInfo.setGatewaySerialnumber(cpeid.substring(cpeid.indexOf("-") + 1));
		GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
		if (null == gatewayInfo) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			return result;
		}

		mac = gatewayInfo.getGatewayMacaddress();
		preLANDeviceList = instructionMethodService.getLANDevicePrefix(mac);
		logger.info("preLANDeviceList:{}", preLANDeviceList.toString());
		if (preLANDeviceList.size() == 0) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			return result;
		}

		if (!setParameterValue(preLANDeviceList, parameter, mac, result)) {
			result.put("CPEID", cpeid);
			return result;
		}

		result.put("Result", Constants.SUCCESS);
		result.put("CPEID", cpeid);
		return result;
	}

	@SuppressWarnings("rawtypes")
	private Boolean setParameterValue(List<String> preLANDeviceList,Map<String, Object> parameter, String mac, Map<String, Object> result ) {

		String preLANDevice = preLANDeviceList.get(0);
		Integer instanceID = null == (Integer) parameter.get("InstanceID") ? 1 : (Integer) parameter.get("InstanceID");
		String key = preLANDevice + "WLANConfiguration." + instanceID + ".Enable";
		String enable = (String)parameter.get("Enable");
		logger.info("key :{}", key);

		ParameterValueStruct parameterValueStruct = new ParameterValueStruct();
		parameterValueStruct.setName(key);
		parameterValueStruct.setValue(enable);
		parameterValueStruct.setValueType("boolean");
		parameterValueStruct.setReadWrite(true);

		List<ParameterValueStruct> list = new ArrayList<ParameterValueStruct>();
		list.add(parameterValueStruct);

		Map<String, Object> retMap = instructionMethodService.setParameterValueErrorCode(mac, list);
		if(Integer.valueOf(retMap.get("result").toString()) == -1)
		{
			if(null != retMap.get("errorCode"))
			{
				result.put("Result", Integer.valueOf(retMap.get("errorCode").toString()));
			}
			else
			{
				result.put("Result", Constants.FAIL);
			}

			return false;
		}

		return true;
	}

	/**
	 * 校验参数，且获取landevice列表
	 *
	 * @param preLANDeviceList
	 * @param mac
	 * @return
	 */
	private boolean validate(List<String> preLANDeviceList, Map<String, Object> parameter, String mac) {
		String cpeid = (String) parameter.get("CPEID");
		Integer instanceID = (Integer) parameter.get("InstanceID");

		if (StringUtils.isBlank(cpeid)) {
			return true;
		}

		if (instanceID == null) {
			return true;
		}

		// cpeid为HguId 或 StbId，格式为：OUI-SN
		if (cpeid.indexOf("-") == -1) {
			return true;
		}

		// 检测是否存在网关
		GatewayInfo searchInfo = new GatewayInfo();
		searchInfo.setGatewaySerialnumber(cpeid.substring(cpeid.indexOf("-") + 1));
		GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
		if (null == gatewayInfo) {
			return true;
		}

		mac = gatewayInfo.getGatewayMacaddress();
		preLANDeviceList = instructionMethodService.getLANDevicePrefix(mac);
		logger.info("preLANDeviceList:{}", preLANDeviceList.toString());
		if (preLANDeviceList.size() == 0) {
			return true;
		}

		return false;
	}

	private boolean validate(String cpeid) {

		if (StringUtils.isBlank(cpeid)) {
			return true;
		}

		// cpeid为HguId 或 StbId，格式为：OUI-SN
		if (cpeid.indexOf("-") == -1) {
			return true;
		}

		return false;
	}

	private Map<String, Object> sendCmd(Map<String, Object> parameter) {
		logger.info("parameter:{}", parameter);

		Map<String, Object> result = new HashMap<String, Object>();
		String cpeid = (String) parameter.get("CPEID");
		String methodName = (String) parameter.get("methodName");

		if (validate(cpeid)) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			return result;
		}

		// 检测是否存在网关
		GatewayInfo searchInfo = new GatewayInfo();
		searchInfo.setGatewaySerialnumber(cpeid.substring(cpeid.indexOf("-") + 1));
		GatewayInfo gatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
		if (null == gatewayInfo) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			logger.info("网关不存在");
			return result;
		}

		// 检查该网关是否正在执行重启或者恢复出厂
		if (checkCmd(gatewayInfo.getGatewaySerialnumber(), methodName)) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			logger.info("命令正在执行");
			return result;
		}

		Map<String, Object> map = new HashMap<>();
		map.put("gatewayIds", new ArrayList<String>(Arrays.asList(gatewayInfo.getGatewayUuid())));
		map.put("methodName", methodName);
		Map<String, Object> resultMap = invokeInsService.executeBatch(map);

		if (resultMap == null) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			logger.info("指令下发失败");
		} else {
			result.put("Result", Constants.SUCCESS);
			result.put("CPEID", cpeid);
		}

		return result;
	}

	private boolean checkCmd(String gatewaySerialnumber, String methodName) {

		String serialnumber = Constants.LOCK_PREFIX + gatewaySerialnumber;// 为了保证重启和恢复出厂key唯一且不和其他指令冲突，加上前缀R-F-
		
		//区分重启还是恢复出厂设置
		String code= null;
		if(methodName.equals(Constants.FACTORYRESET))
		{
			code = RebootEnum.STATUS_1.code();
		}	
		else 
		{
			code = RebootEnum.STATUS_0.code();
		}	
		// SN为唯一，对其锁处理，设置超时时间为10分钟
		String str = redisClientTemplate.set(serialnumber, code, "NX", "EX", timeout);
		logger.info("恢复出厂或者设备重启在redis添加key为:" + serialnumber + "的锁,返回的状态为:" + str);
		if (str == null) {
			// 存在锁
			return true;
		}

		return false;
	}

	private boolean validateParameter(String cpeid, String password) {

		if (StringUtils.isBlank(cpeid)) {
			return true;
		}

		if (StringUtils.isBlank(password)) {
			return true;
		}
		// cpeid为HguId 或 StbId，格式为：OUI-SN
		if (cpeid.indexOf("-") == -1) {
			return true;
		}
		return false;
	}

	@Override
	public Map<String, Object> boxFactoryReset(Map<String, Object> parameter) {
		logger.info("boxFactoryReset,parameter:{}", parameter.toString());
		
		parameter.put("methodName", Constants.FACTORYRESET);
		return sendBoxCmd(parameter);
	}

	@Override
	public Map<String, Object> boxReboot(Map<String, Object> parameter) {
		logger.info("boxReboot,parameter:{}", parameter.toString());
		
		parameter.put("methodName", Constants.REBOOT);
		return sendBoxCmd(parameter);
	}	


	private Map<String, Object> sendBoxCmd(Map<String, Object> parameter) {
		logger.info("parameter:{}", parameter);

		Map<String, Object> result = new HashMap<String, Object>();
		String cpeid = (String) parameter.get("CPEID");
		String methodName = (String) parameter.get("methodName");

		if (validate(cpeid)) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			return result;
		}
		
		//检测机顶盒是否存在
		BoxInfo boxInfo = new BoxInfo();
		boxInfo.setBoxSerialnumber(cpeid.substring(cpeid.indexOf("-") + 1));
		BoxInfo box = boxInfoMapper.selectGatewayInfo(boxInfo );
		if(null == box)
		{
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			logger.info("机顶盒不存在");
			return result;
		}
		
		// 检查该网关是否正在执行重启或者恢复出厂
		if (checkCmd(box.getBoxSerialnumber(), methodName)) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			logger.info("命令正在执行");
			return result;
		}

		//下发指令给机顶盒
		Map<String, Object> map = new HashMap<>();
        map.put("boxIds", new ArrayList<String>(Arrays.asList(box.getBoxUuid())));
        map.put("methodName", methodName);
        Map<String, Object> resultMap = boxInvokeInsService.executeBatch(map);
        
        if (resultMap == null) {
			result.put("Result", Constants.FAIL);
			result.put("CPEID", cpeid);
			logger.info("指令下发失败");
		} else {
			result.put("Result", Constants.SUCCESS);
			result.put("CPEID", cpeid);
		}
			
		return result;
	}

}
