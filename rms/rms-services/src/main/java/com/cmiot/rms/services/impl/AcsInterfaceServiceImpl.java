package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.facade.OperationCpeFacade;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.acs.model.struct.ParameterValueStructInt;
import com.cmiot.acs.model.struct.ParameterValueStructStr;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.*;
import com.cmiot.rms.dao.model.*;
import com.cmiot.rms.dao.model.GatewayNodeExample.Criteria;
import com.cmiot.rms.services.*;
import com.cmiot.rms.services.instruction.AbstractInstruction;
import com.cmiot.rms.services.instruction.impl.SetParameterValuesInstruction;
import com.cmiot.rms.services.outerservice.InterfaceHandOut;
import com.cmiot.rms.services.outerservice.RequestMgrService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.InstructionUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 提供给acs调用接口
 * Created by panmingguo on 2016/5/10.
 */
public class AcsInterfaceServiceImpl implements AcsInterfaceService {

    private static Logger logger = LoggerFactory.getLogger(AcsInterfaceServiceImpl.class);

    @Autowired
    private InterfaceHandOut interfaceHandOut;

    @Autowired
    private RequestMgrService requestMgrService;

    @Autowired
    GatewayInfoService gatewayInfoService;

    @Autowired
    private InstructionsService instructionsService;

    @Autowired
    FirmwareUpgradeTaskInnerService firmwareUpgradeTaskInnerService;

    @Autowired
    GatewayBackupFileTaskInnerService gatewayBackupFileTaskInnerService;

    @Autowired
    BatchSetTaskTrrigerService batchSetTaskTrrigerService;

    @Autowired
    FactoryMapper factoryMapper;

    @Autowired
    DeviceInfoService deviceInfoService;

    @Autowired
    FirmwareInfoMapper firmwareInfoMapper;
    
    @Autowired
    private GatewayNodeMapper gatewayNodeMapper;

    @Autowired
    private GatewayPasswordMapper gatewayPasswordMapper;

    @Autowired
    private OperationCpeFacade operationCpeFacade;

    @Autowired
    RedisClientTemplate redisClientTemplate;

    @Autowired
    ManufacturerMapper manufacturerMapper;

    @Value("${first.report.flow.lock.time}")
    int firstReportLockTime;

    @Value("${is.digest.open}")
    int isDigestOpen;


    /**
     * ACS上报接口
     *
     * @param abstractMethod
     * @return
     */
    @Override
    public Map<String, Object> reportInfo(AbstractMethod abstractMethod) {
        logger.info("Start invoke reportInfo:{}", abstractMethod);

        JSONObject jsonObj = JSON.parseObject(JSONObject.toJSONString(abstractMethod));
        Map<String, Object> retmap = (Map<String, Object>)interfaceHandOut.handOut(jsonObj);

        logger.info("End invoke reportInfo:{}", retmap);
        return retmap;
    }

    /**
     * OUI-SN验证接口
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> checkOuiSn(Map<String, Object> parameter) {
        logger.info("Start invoke checkOuiSn:{}", parameter);
        boolean isSuccess = requestMgrService.certification(parameter);
        Map<String, Object> retMap = new HashMap<>();
        if(isSuccess)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        }
        else
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.OUISN_CHECK_FAILED.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.OUISN_CHECK_FAILED.getResultMsg());
        }
        logger.info("End invoke checkOuiSn:{}", retMap);
        return retMap;
    }

    /**
     * password验证接口
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> checkPassword(Map<String, Object> parameter) {
        logger.info("Start invoke checkPassword:{}", parameter);
        Map<String, Object> retMap = new HashMap<>();
        boolean isPass = true;
        String statu = "0";
        String gatewayPassword = parameter.get("gatewayPassword") == null ? "" : parameter.get("gatewayPassword").toString();
        String sn = parameter.get("sn") == null ? "" : parameter.get("sn").toString();
        String oui =  parameter.get("oui") == null ? "" : parameter.get("oui").toString();
        if ("".equals(gatewayPassword) || "".equals(sn) || "".equals(oui)) {
            logger.info("password,sn,oui不能为空，password:{},sn:{},oui:{}",gatewayPassword,sn,oui);
            retMap.put(Constant.CODE, ErrorCodeEnum.PASSWORD_CHECK_FAILED.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PASSWORD_CHECK_FAILED.getResultMsg());
        }else {
            //先通过oui sn 查询网关信息
            GatewayInfo gatewayInfoParam = new GatewayInfo();
            gatewayInfoParam.setGatewaySerialnumber(sn);
            gatewayInfoParam.setGatewayFactoryCode(oui);
            // 根据 SN 和 OUI查询网关信息
            GatewayInfo selectGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfoParam);

            //宽带账号
            String asdlAccount="";

            if (selectGatewayInfo == null) {
                logger.info("网关不存在");
                retMap.put(Constant.CODE, ErrorCodeEnum.PASSWORD_CHECK_FAILED.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.PASSWORD_CHECK_FAILED.getResultMsg());
            }else {
                if (null == selectGatewayInfo.getGatewayPassword() || "".equals(selectGatewayInfo.getGatewayPassword())) {
                    //先判断password是否已经被使用
                    GatewayInfo gatewayInfo = new GatewayInfo();
                    gatewayInfo.setGatewayPassword(gatewayPassword);
                    // 根据 password查询是否存在网关信息
                    GatewayInfo resultGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
                    if (null != resultGatewayInfo) {//password已经被使用
                        logger.info("password已经被使用, 解绑原有网关，将password使用在现有网关！");
                        asdlAccount = resultGatewayInfo.getGatewayAdslAccount();
                        //如果存在网关使用password, 清楚原有网关的password和宽带账号
                        resultGatewayInfo.setGatewayPassword(null);
                        resultGatewayInfo.setGatewayAdslAccount("");
                        gatewayInfoService.clearPasswordAndAdslAccount(resultGatewayInfo);
                    }
                    else
                    {
                        //网关表中不存在password，则去工单表中查询
                        GatewayPassword gatewayPasswordBean = gatewayPasswordMapper.selectByPrimaryKey(gatewayPassword);
                        //如果gatewayPassword为空，则password认证失败
                        if (gatewayPasswordBean == null) {
                            logger.info("工单表不存在password");
                            isPass = false;
                            statu = "1";
                        }
                    }

                } else {
                    if (!gatewayPassword.equals(selectGatewayInfo.getGatewayPassword())) {
                        logger.info("password与网关表信息不符");
                        isPass = false;
                        statu = "1";
                    }
                }
                if (isPass) {
                    //验证通过，将网关改为已绑定
                    GatewayInfo updateStatus = new GatewayInfo();
                    updateStatus.setGatewayPassword(gatewayPassword);
                    updateStatus.setGatewayUuid(selectGatewayInfo.getGatewayUuid());
                    if(StringUtils.isNotBlank(asdlAccount))
                    {
                        updateStatus.setGatewayAdslAccount(asdlAccount);
                    }
                    updateStatus.setGatewayStatus("已绑定");
                    gatewayInfoService.updateSelectGatewayInfo(updateStatus);
                    retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
                    retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
                } else {
                    retMap.put(Constant.CODE, ErrorCodeEnum.PASSWORD_CHECK_FAILED.getResultCode());
                    retMap.put(Constant.MESSAGE, ErrorCodeEnum.PASSWORD_CHECK_FAILED.getResultMsg());
                }
                AbstractMethod abstractMethod = getCheckPassInstruction(selectGatewayInfo, statu);
                List<AbstractMethod> abstractMethods = new ArrayList<>();
                abstractMethods.add(abstractMethod);
                operationCpeFacade.doACSEMethods(abstractMethods);
            }
        }
        logger.info("End invoke checkPassword:{}", retMap);
        return retMap;
    }

	@Override
	public Map<String, Object> queryDigestAccount(Map<String, Object> parameter) {
		logger.info("Start invoke queryDigestAccount:{}", parameter);
		boolean isPass = requestMgrService.queryDigest(parameter);
        Map<String, Object> retMap = new HashMap<>();
        if(isPass)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        }
        else
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.PASSWORD_CHECK_FAILED.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PASSWORD_CHECK_FAILED.getResultMsg());
        }
        logger.info("End invoke queryDigestAccount:{}", retMap);
        return retMap;
	}

    /**
     * 根据用户名查询digest密码
     *
     * @param userName
     * @return
     */
    @Override
    public List<String> queryDigestPassword(String userName) {
        logger.info("Start invoke queryDigestPassword:{}", userName);
        List<String> ret = requestMgrService.queryDigestPassword(userName);
        logger.info("End invoke queryDigestPassword:{}", ret);
        return ret;
    }

    /**
     * 网关首次上报ACS从RMS获取需要下发的指令
     * @param parameter
     * @return
     */
    @Override
    public List<AbstractMethod> getbootStrapInstructions(Map<String, Object> parameter) {
        logger.info("Start invoke getbootStrapInstructions:{}", parameter);
        String oui = null != parameter.get("oui") ? parameter.get("oui").toString() : "";
        String sn = null != parameter.get("sn") ? parameter.get("sn").toString() : "";
        String url = null != parameter.get("url") ? parameter.get("url").toString() : "";
        if(StringUtils.isEmpty(oui) || StringUtils.isEmpty(sn) || StringUtils.isEmpty(url))
        {
            return new ArrayList<>();
        }

        //1.查询网关
        GatewayInfo searchInfo = new GatewayInfo();
        searchInfo.setGatewayFactoryCode(oui);
        searchInfo.setGatewaySerialnumber(sn);
        GatewayInfo selectGatewayInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
        if (selectGatewayInfo == null)
        {
            return new ArrayList<>();
        }

        //先更新URL,确保后续的指令下发都存在URL
        selectGatewayInfo.setGatewayConnectionrequesturl(url);
        gatewayInfoService.updateSelectGatewayInfo(selectGatewayInfo);


        //对首次上报事件加锁，在首次上报流程中，其他事件不能对网关下发固件升级，备份，批量设置，查询流量等指令
        String FirstReportKey = "FirstReport" + selectGatewayInfo.getGatewaySerialnumber();
        redisClientTemplate.set(FirstReportKey, "yes", "NX","EX", firstReportLockTime);
        logger.info("给首次启动流程加锁，{}",FirstReportKey);


        List<AbstractMethod> methodList = new ArrayList<>();

        //2.获取升级任务指令
        AbstractMethod upgradeMethod = firmwareUpgradeTaskInnerService.getUpgradeInstruction(selectGatewayInfo);
        if(null != upgradeMethod)
        {
            methodList.add(upgradeMethod);
        }else{

            //3.获取digest账号，家庭网关维护账号指令
            AbstractMethod accountMethod =  getAccountInstruction(selectGatewayInfo);
            if(null != accountMethod)
            {
                methodList.add(accountMethod);
            }

            /*//4.获取设置业务下发结果指令
            AbstractMethod userInfoResultInstruction = getUserInfoResultInstruction(selectGatewayInfo);
            if(null != userInfoResultInstruction)
            {
                methodList.add(userInfoResultInstruction);
            }*/
        }
        logger.info("End invoke getbootStrapInstructions:{}", methodList);
        return methodList;
    }

    /**
     * 当rms为导入网关基础信息时，主动发现
     *
     * @param inform
     * @return
     */
    @Override
    public Boolean createGatewayInfo(Inform inform) {
        logger.info("Start invoke createGatewayInfo:{}", inform);
        Boolean ret = false;
        try {
            GatewayInfo gatewayInfo = new GatewayInfo();
            gatewayInfo.setGatewayUuid(UniqueUtil.uuid());

            String softwareVersion = null;
            String hardwareVersion = null;
            String connectionrequesturl = null;
            String password = "";
            List<ParameterValueStruct> structList = inform.getParameterList().getParameterValueStructs();
            for(ParameterValueStruct struct : structList)
            {
                if(struct.getName().equals("InternetGatewayDevice.DeviceInfo.SoftwareVersion"))
                {
                    softwareVersion = String.valueOf(struct.getValue());
                }
                else if(struct.getName().equals("InternetGatewayDevice.DeviceInfo.HardwareVersion"))
                {
                    hardwareVersion = String.valueOf(struct.getValue());
                }
                else if(struct.getName().equals("InternetGatewayDevice.ManagementServer.ConnectionRequestURL"))
                {
                    connectionrequesturl = String.valueOf(struct.getValue());
                }
                else if(struct.getName().equals("InternetGatewayDevice.X_CMCC_UserInfo.Password"))
                {
                    password = String.valueOf(struct.getValue());
                }
            }

            //1、验证password
            if(StringUtils.isBlank(password))
            {
                logger.info("网关上报的password为空!");
                return false;
            }
            //宽带账号
            String asdlAccount="";

            //先判断password是否已经被使用
            GatewayInfo searchInfo = new GatewayInfo();
            searchInfo.setGatewayPassword(password);
            // 根据 password查询是否存在网关信息
            GatewayInfo resultInfo = gatewayInfoService.selectGatewayInfo(searchInfo);
            if (null != resultInfo) {//password已经被使用
                logger.info("password已经被使用, 解绑原有网关，将password使用在现有网关！");
                asdlAccount = resultInfo.getGatewayAdslAccount();
                //如果存在网关使用password, 清除原有网关的password和宽带账号
                resultInfo.setGatewayAdslAccount("");
                resultInfo.setGatewayPassword(null);
                gatewayInfoService.clearPasswordAndAdslAccount(resultInfo);
            }
            else
            {
                //网关表中不存在password，则去工单表中查询
                GatewayPassword gatewayPasswordBean = gatewayPasswordMapper.selectByPrimaryKey(password);
                //如果gatewayPassword为空，则password认证失败
                if (gatewayPasswordBean == null) {
                    logger.info("工单表不存在password！");
                    return false;
                }
            }

            //2、是否存在OUI
            List<Factory> factorys = factoryMapper.queryFactoryInfo(inform.getDeviceId().getOui());
            if(null == factorys || factorys.size() < 1)
            {
            	logger.info("网关inform上报，主动发现处理失败，原因为无与该网关{}匹配的OUI", "DeviceOUI=" + inform.getDeviceId().getOui());
                return ret;
            }

            Manufacturer manufacturer;
            for(Factory factory : factorys)
            {
                //3.是否存在生产商
                manufacturer = manufacturerMapper.selectByPrimaryKey(factory.getManufacturerId());
                if(null == manufacturer)
                {logger.info("网关inform上报，主动发现处理失败，原因为无与该网关{}匹配的生产商", "DeviceOUI=" + inform.getDeviceId().getOui());
                    continue;
                }

                gatewayInfo.setGatewayFactoryCode(inform.getDeviceId().getOui());
                gatewayInfo.setGatewayFactory(manufacturer.getManufacturerName());
                gatewayInfo.setGatewaySerialnumber(inform.getDeviceId().getSerialNubmer());
                gatewayInfo.setNewFactoryCode(manufacturer.getId());

                //4、是否存在设备型号
                DeviceInfo deviceInfoToSearch = new DeviceInfo();
                deviceInfoToSearch.setDeviceFactory(manufacturer.getId());
                deviceInfoToSearch.setDeviceModel(inform.getDeviceId().getProductClass());
                List<DeviceInfo> deviceInfoList = deviceInfoService.queryList(deviceInfoToSearch);
                if(null == deviceInfoList || deviceInfoList.size() < 1)
                {
                    logger.info("网关inform上报，主动发现处理失败，原因为无与该网关{}匹配的设备型号", "DeviceModel=" + inform.getDeviceId().getProductClass());
                    continue;
                }
                gatewayInfo.setGatewayModel(inform.getDeviceId().getProductClass());
                gatewayInfo.setGatewayDeviceUuid(deviceInfoList.get(0).getId());


                //5、固件版本是否存在
                Map<String, Object> firmwareMap = new HashMap<>();
                firmwareMap.put("deviceFactory", manufacturer.getId());
                firmwareMap.put("firmwareVersion", softwareVersion);
                firmwareMap.put("deviceModel", inform.getDeviceId().getProductClass());
                Map<String, Object>  firmwareInfo = firmwareInfoMapper.queryFirmwareIdList(firmwareMap);
                if(null == firmwareInfo || null == firmwareInfo.get("firmwareId"))
                {
                    logger.info("网关inform上报，主动发现处理失败，原因为无与该网关{}匹配的固件", "firmwareVersion=" + softwareVersion);
                    continue;
                }


                gatewayInfo.setGatewayPassword(password);
                gatewayInfo.setGatewayAdslAccount(asdlAccount);
                gatewayInfo.setGatewayStatus("已绑定");

                gatewayInfo.setGatewayVersion(softwareVersion);
                gatewayInfo.setGatewayFirmwareUuid(firmwareInfo.get("firmwareId").toString());
                gatewayInfo.setGatewayHardwareVersion(hardwareVersion);
                gatewayInfo.setGatewayConnectionrequesturl(connectionrequesturl);

                //MAC地址格式：OUi+SN后六位
                String sn = inform.getDeviceId().getSerialNubmer();
                gatewayInfo.setGatewayMacaddress(inform.getDeviceId().getOui() + sn.substring(sn.length() - 6, sn.length()));

                long timeMillis = System.currentTimeMillis();
                long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
                gatewayInfo.setGatewayJoinTime((int)timeSeconds);
                gatewayInfo.setGatewayLastConnTime((int)timeSeconds);

                gatewayInfo.setGatewayType("");
                gatewayInfo.setGatewayName("");
                gatewayInfo.setGatewayMemo("");
                gatewayInfo.setGatewayDigestAccount("");
                gatewayInfo.setGatewayDigestPassword("");
                gatewayInfo.setGatewayFamilyAccount("");
                gatewayInfo.setGatewayFamilyPassword("");

                gatewayInfoService.insertSelective(gatewayInfo);
                ret = true;
                break;
            }

        }
        catch (Exception e)
        {
            logger.info("createGatewayInfo exception:{}", e);
            ret = false;
        }
        logger.info("End invoke createGatewayInfo:{}", ret);
        return ret;
    }

    /**
     * 根据OUI—SN查询网关的Digest的账号和密码
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryDigestAccAndPw(Map<String, Object> parameter) {
        logger.info("Start invoke queryDigestAccAndPw:{}", parameter);
        Map<String, Object> ret = requestMgrService.queryDigestAccAndPw(parameter);
        logger.info("End invoke queryDigestAccAndPw:{}", ret);
        return ret;
    }


    /**
     * 获取digest账号，家庭网关维护账号指令
     * @return
     */
    private AbstractMethod getAccountInstruction(GatewayInfo gatewayInfo)
    {
        if (isDigestOpen != 0) {
           return null;
        }

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
//        structs.add(new ParameterValueStructInt("InternetGatewayDevice.X_CMCC_UserInfo.Status", 0));这个应该是在password认证的时候设置
        structs.add(new ParameterValueStructInt("InternetGatewayDevice.X_CMCC_UserInfo.Result", 0));
        
        GatewayNodeExample node = new GatewayNodeExample();
        Criteria criteria = node.createCriteria();
        criteria.andFactoryCodeEqualTo(gatewayInfo.getGatewayFactoryCode());
        criteria.andHdVersionEqualTo(gatewayInfo.getGatewayHardwareVersion());
        criteria.andFirmwareVersionEqualTo(gatewayInfo.getGatewayVersion());
        List<GatewayNode> nodeList= gatewayNodeMapper.selectByExample(node);
        if (!nodeList.isEmpty()&&StringUtils.isNotBlank(nodeList.get(0).getLoginPasswordNode())) {
        	logger.info("节点适配查询"+nodeList.get(0).getLoginPasswordNode());
        	structs.add(new ParameterValueStructStr(nodeList.get(0).getLoginPasswordNode(), familyPassword));
		}else {
			logger.info("标准节点"+"InternetGatewayDevice.DeviceInfo.X_CMCC_TeleComAccount.Password");
			structs.add(new ParameterValueStructStr("InternetGatewayDevice.DeviceInfo.X_CMCC_TeleComAccount.Password", familyPassword));
		}

        Map<String, Object> map = new HashMap<>();
        map.put("pvList", structs);

        InstructionsInfoWithBLOBs is = new InstructionsInfoWithBLOBs();
        //生成指令ID作为请求的requestId
        String insId = "bootStrap_"+ InstructionUtil.generate16Uuid();
        is.setInstructionsId(insId);
        is.setCpeIdentity(gatewayInfo.getGatewayUuid());
        map.put("requestId", insId);
        AbstractInstruction ins = new SetParameterValuesInstruction();
        AbstractMethod abstractMethod = ins.createIns(is, gatewayInfo, map);

        if(StringUtils.isNotBlank(gatewayInfo.getGatewayConnectionrequestUsername())
                && StringUtils.isNotBlank(gatewayInfo.getGatewayConnectionrequestPassword()))
        {
            abstractMethod.setCpeUserName(gatewayInfo.getGatewayConnectionrequestUsername());
            abstractMethod.setCpePassword(gatewayInfo.getGatewayConnectionrequestPassword());
        }

        is.setInstructionsBeforeContent(JSON.toJSONString(abstractMethod));
        instructionsService.addInstructionsInfo(is);

        return abstractMethod;

    }

   /* *//**
     * 获取设置业务下发结果指令
     * @param gatewayInfo
     * @return
     *//*
    private AbstractMethod getUserInfoResultInstruction(GatewayInfo gatewayInfo)
    {
        List<ParameterValueStruct> structs = new ArrayList<>();
        structs.add(new ParameterValueStructInt("InternetGatewayDevice.X_CMCC_UserInfo.Result", 1));

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

        if(StringUtils.isNotBlank(gatewayInfo.getGatewayConnectionrequestUsername())
                && StringUtils.isNotBlank(gatewayInfo.getGatewayConnectionrequestPassword()))
        {
            abstractMethod.setCpeUserName(gatewayInfo.getGatewayConnectionrequestUsername());
            abstractMethod.setCpePassword(gatewayInfo.getGatewayConnectionrequestPassword());
        }

        is.setInstructionsBeforeContent(JSON.toJSONString(abstractMethod));
        instructionsService.addInstructionsInfo(is);

        return abstractMethod;
    }*/

    /**
     * password验证后，设置状态
     * @param gatewayInfo
     * @param statu 0：成功 1：Password不存在 4：超时 5：已经注册过且无新的工单要执行  99：缺省值，表示无认证结果信息
     * @return
     */
    private AbstractMethod getCheckPassInstruction(GatewayInfo gatewayInfo,String statu)
    {
        List<ParameterValueStruct> structs = new ArrayList<>();
        //适配表不支持此处的适配，适配表只做了Password节点的适配，所以暂时不做适配
        structs.add(new ParameterValueStructStr("InternetGatewayDevice.X_CMCC_UserInfo.Status", statu));

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

        return abstractMethod;
    }


}
