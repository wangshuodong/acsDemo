package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.facade.OperationCpeFacade;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.struct.ParameterInfoStruct;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.acs.model.struct.SetParameterAttributesStruct;
import com.cmiot.ams.domain.Admin;
import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AdminService;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.cache.TemporaryObject;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.enums.*;
import com.cmiot.rms.common.ftp.FtpBusiness;
import com.cmiot.rms.common.logback.LogBackRecord;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.*;
import com.cmiot.rms.dao.model.*;
import com.cmiot.rms.dao.model.derivedclass.GatewayBean;
import com.cmiot.rms.dao.vo.GateWayExcelContent;
import com.cmiot.rms.dao.vo.GateWayInfoExcelContent;
import com.cmiot.rms.services.*;
import com.cmiot.rms.services.instruction.AbstractInstruction;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.instruction.InvokeInsService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.InstructionUtil;
import com.cmiot.rms.services.util.OperationLogUtil;
import com.cmiot.rms.services.validator.ValidatorManagement;
import com.cmiot.rms.services.validator.result.ValidateResult;
import com.cmiot.rms.services.workorder.impl.BusiOperation;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.iot.common.file.poi.ExcelUtil;
import com.tydic.inter.app.service.GatewayHandleService;

import org.apache.poi.hssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网关管理接口
 */
public class GatewayManageServiceImpl implements GatewayManageService {

    private static Logger logger = LoggerFactory.getLogger(GatewayManageServiceImpl.class);

    @Resource
    private GatewayInfoMapper gatewayInfoMapper;

    @Resource
    private GatewayInfoService gatewayInfoService;

    @Resource
    private HardwareAblityService hardwareAblityService;

    @Autowired
    private HardwareAblityMapper hardwareAblityMapper;

    @Resource
    private ManufacturerMapper manufacturerMapper;

    @Autowired
    private InvokeInsService invokeInsService;

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Autowired
    private LogManagerService logManagerService;

    @Autowired
    private RedisClientTemplate redisClientTemplate;
    
	@Autowired
	InstructionMethodService instructionMethodService;

    @Autowired
    AreaService areaService;
    @Autowired
    AdminService adminService;

    @Autowired
    FirmwareInfoService firmwareInfoService;
    
    @Autowired
    SyncInfoToFirstLevelPlatformService syncInfoToFirstLevelPlatformService;

    @Autowired
    private InstructionsService instructionsService;

    @Autowired
    private  OperationCpeFacade operationCpeFacade;
    
    @Autowired
    GatewayBusinessMapper gatewayBusinessMapper;
    
    @Autowired
    BusinessCategoryMapper businessCategoryMapper;
    @Autowired
    BusiOperation busiOperation;
    @Autowired
    private GatewayBusinessOpenDetailMapper gatewayBusinessOpenDetailMapper;
    
    @Autowired
    GatewayQueueMapper gatewayQueueMapper;

    @Autowired
    BusinessCodeMapper businessCodeMapper;

    @Autowired
    FactoryMapper factoryMapper;

    @Autowired
    private GatewayNodeMapper gatewayNodeMapper;

    @Autowired
    GatewayGroupMapper gatewayGroupMapper;
    
    @Autowired
    private GatewayHandleService gatewayHandleService;
    
    @Value("${rebootTimeOut}")
    int rebootTimeOut;

    @Value("${factoryResetTimeOut}")
    int factoryResetTimeOut;
    
    @Value("${first.level.platform.url}")
    String url;

    @Value("${query.gatewaypeedlock.timeout}")
    int gatewaySpeedLockTimeout;

    @Value("${file.server.url}")
    String ftpUrl;

    @Value("${ftpserver.localSaveFolder}")
    String excelSaveFolder;

    @Value("${gateway.type.conf}")
    String gateWayTypeConf;

    //ON,OFF
    @Value("${first.level.platform.sync.lock}")
    String lock;

    @Autowired
    ValidatorManagement validatorManagement;

    private List<Map<String, Object>> deviceInfoList;  //记录查询出的结果集

    private List<Map<String, Object>> nonDeviceInfoList; //记录查询过，但是没有的网关设备信息
    /**
     * 功能:dubbo服务接口、网关分页查询功能
     *
     * @param parameter 请求参数
     * @return
     */
    public Map<String, Object> queryList4Page(Map<String, Object> parameter) {
        logger.info("  ~~queryList4Page ~~");
        GatewayInfo gatewayInfo = new GatewayInfo();
        Map<String, Object> backMap = new HashMap<String, Object>();
        try {
        	if(parameter.get("gatewaySerialnumber")!=null 
            		&& !"".equals(parameter.get("gatewaySerialnumber"))){
        		gatewayInfo.setGatewaySerialnumber(parameter.get("gatewaySerialnumber").toString().trim());
            }

        	if(parameter.get("makeMerchantsId")!=null 
        			&& !"".equals(parameter.get("makeMerchantsId"))){
        		gatewayInfo.setMakeMerchantsId(parameter.get("makeMerchantsId").toString().trim());
        	}
            //生产商编号
            if (parameter.get("factoryName") != null
                    && !"".equals(parameter.get("factoryName").toString().trim())) {
                gatewayInfo.setGatewayFactoryCode(parameter.get("factoryName").toString().trim());
            }

            //网关密码
            if (parameter.get("gatewayPassword") != null
                    && !"".equals(parameter.get("gatewayPassword").toString().trim())) {
                gatewayInfo.setGatewayPassword(parameter.get("gatewayPassword").toString().trim());
            }

            if (parameter.get("uId") != null
                    && !"".equals(parameter.get("uId").toString().trim())) {
                logger.info(" token uId " + (String) parameter.get("uId"));
                Admin userInfo = adminService.transToken((String) parameter.get("uId"));
                logger.info(" userInfo " + userInfo.getId());
                List<com.cmiot.ams.domain.Area> userAreaList = areaService.findAreaByAdmin(userInfo.getId());
                if (userAreaList != null && userAreaList.size() > 0) {
                    //区域编号
                    if (parameter.get("areaCode") != null
                            && !"".equals(parameter.get("areaCode").toString().trim())) {
                        List<com.cmiot.ams.domain.Area> areaList = areaService.findChildArea(Integer.parseInt((String) parameter.get("areaCode")));
                        if (areaList != null && areaList.size() > 0) {
                            StringBuffer sb = new StringBuffer();
                            sb.append("(");
                            for (int j = 0; j < userAreaList.size(); j++) {
                                for (int i = 0; i < areaList.size(); i++) {
                                    if(userAreaList.get(j).getId().equals(areaList.get(i).getId())){
                                        sb.append("," + areaList.get(i).getId());
                                        /*logger.info(" userAreaList " + userAreaList.get(j).getId()  + " ~~areaList~~ " + areaList.get(i).getId());*/
                                    }
                                }
                            }
                            sb.append(")");
                            String par = sb.toString().replaceFirst(",", "");
                            logger.info(" areaCode is " + par);
                            gatewayInfo.setGatewayAreaId(par);
                        }
                    }else{

                            StringBuffer sbNoArea = new StringBuffer();
                            sbNoArea.append("(");
                            for (int j = 0; j < userAreaList.size(); j++) {
                                sbNoArea.append("," + userAreaList.get(j).getId());
                            }
                            sbNoArea.append(")");
                            String par = sbNoArea.toString().replaceFirst(",", "");
                            logger.info(" areaCode is " + par.toString());
                            gatewayInfo.setGatewayAreaId(par);
                    }

                }else{
                    gatewayInfo.setGatewayAreaId("("+"'"+"'"+")");
                    logger.info(" userAreaList size is " + userAreaList.size());
                }
            }else{
                logger.info(" no uId ");
            }

            //用户地址
            if (parameter.get("gatewayMacaddress") != null
                    && !"".equals(parameter.get("gatewayMacaddress").toString().trim())) {
                gatewayInfo.setGatewayMacaddress(parameter.get("gatewayMacaddress").toString().trim());
            }
            //宽带账号
            if (parameter.get("gatewayAdslAccount") != null
                    && !"".equals(parameter.get("gatewayAdslAccount").toString().trim())) {
                gatewayInfo.setGatewayAdslAccount(parameter.get("gatewayAdslAccount").toString().trim());
            }
            //固件版本
            if (parameter.get("gatewayVersion") != null
                    && !"".equals(parameter.get("gatewayVersion").toString().trim())) {
                gatewayInfo.setGatewayVersion(parameter.get("gatewayVersion").toString().trim());
            }
            //设备型号
            if (parameter.get("gatewayModel") != null
                    && !"".equals(parameter.get("gatewayModel").toString().trim())) {//gatewayModel可重复
                DeviceInfo record = new DeviceInfo();
                record.setId(parameter.get("gatewayModel").toString().trim());
                List<DeviceInfo>  deviceInfoList  = deviceInfoMapper.queryList(record);
                if(deviceInfoList != null && deviceInfoList.size() > 0){
                    logger.info(parameter.get("gatewayModel").toString().trim() + " DeviceInfo "  + deviceInfoList.get(0).getDeviceModel());
                    gatewayInfo.setGatewayModel(deviceInfoList.get(0).getDeviceModel());
                }else{
                    logger.info(" gatewayModel "  + parameter.get("gatewayModel").toString().trim());
                }
            }
            if (parameter.get("businessCode") != null
                    && !"".equals(parameter.get("businessCode").toString().trim())) {
                gatewayInfo.setBusinessCode(parameter.get("businessCode").toString().trim());
            }
            if (parameter.get("gatewayConfType") != null
                    && !"".equals(parameter.get("gatewayConfType").toString().trim())) {
                gatewayInfo.setGatewayType(parameter.get("gatewayConfType").toString().trim());
            }
            if (parameter.get("groupId") != null
                    && !"".equals(parameter.get("groupId").toString().trim())) {
                logger.info(" groupId " + parameter.get("groupId"));
                GatewayGroup gatewayGroup = new GatewayGroup();
                gatewayGroup.setGroupUuid(parameter.get("groupId").toString());
                gatewayGroup =  gatewayGroupMapper.selectByParam(gatewayGroup);
                if(gatewayGroup != null){
                    gatewayInfo.setGatewayUuid(makeUidStr(gatewayGroup));
                }else{
                    gatewayInfo.setGatewayUuid("('nonedata')");
                }
            }

            if (parameter.get("groupName") != null
                    && !"".equals(parameter.get("groupName").toString().trim())) {
                logger.info(" groupName " + parameter.get("groupName"));
                GatewayGroup gatewayGroupByName = new GatewayGroup();
                gatewayGroupByName.setGroupName(parameter.get("groupName").toString());
                gatewayGroupByName =  gatewayGroupMapper.selectByParam(gatewayGroupByName);
                if(gatewayGroupByName != null){
                    gatewayInfo.setGatewayUuid(makeUidStr(gatewayGroupByName));
                }else{
                    gatewayInfo.setGatewayUuid("('nonedata')");
                }
            }
            
            int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
            int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;
            PageHelper.startPage(page, pageSize);
            List<GatewayBean> list;
            if(parameter.get("areaCode") != null && !"".equals(parameter.get("areaCode").toString().trim())){
                list = gatewayInfoMapper.queryList4PageNoArea(gatewayInfo);
            }else{
                list = gatewayInfoMapper.queryList4Page(gatewayInfo);
            }

            if(null != list && list.size()>0){
                List<Integer> areasByIds = new ArrayList<>();
                for (int i=0; i<list.size(); i++){
                    if(list.get(i).getGatewayAreaId()==null||list.get(i).getGatewayAreaId().length() <= 0){
                        logger.debug(i + " areaId is null ");
                    }else{
                        logger.debug(i + " areaId is " + list.get(i).getGatewayAreaId());
                        areasByIds.add(Integer.parseInt(list.get(i).getGatewayAreaId()));
                    }
                   /* if(list.get(i).getGatewayFactoryCode()==null||list.get(i).getGatewayFactoryCode().length() <= 0){
                        logger.debug(i + " getGatewayFactoryCode is null ");
                    }else{
                        logger.debug(i + " factoryInfory is " + list.get(i).getGatewayFactoryCode());
                        Factory factoryInfory = factoryMapper.queryFactoryInfo(list.get(i).getGatewayFactoryCode());
                        if(factoryInfory != null){
                            list.get(i).setGatewayFactory(factoryInfory.getFactoryName());
                        }else{
                            list.get(i).setGatewayFactory("");
                        }
                    }*/
                }
                List<com.cmiot.ams.domain.Area> areaList;
                areaList = areaService.findAreasByIds(areasByIds);
                if(null != areaList && areaList.size() > 0 ){
                    for (int i = 0; i<list.size(); i++){
                        for (int area = 0; area<areaList.size(); area++) {
                            logger.debug(" GatewayAreaId is " + list.get(i).getGatewayAreaId() + " areaId is " +areaList.get(area).getId() +" areaName is "+areaList.get(area).getName());
                            if(list.get(i).getGatewayAreaId()==null || list.get(i).getGatewayAreaId().length()<= 0){

                            }else{
                                if (Integer.parseInt(list.get(i).getGatewayAreaId()) == areaList.get(area).getId()) {
                                    list.get(i).setGatewayAreaName(areaList.get(area).getName());
                                }
                            }

                        }
                    }
                }
            }
            backMap.put("page", page);
            backMap.put("pageSize", pageSize);
            backMap.put("total", ((Page) list).getTotal());

            backMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
            backMap.put(Constant.MESSAGE, "网关管理分页查询");
//            List result = ((Page) list).getResult();
            // 处理网关返回数据 从redis里面查询网关重启  恢复出厂操作状态

            for (GatewayBean gatewayBean : list) {
                if(gatewayBean.getGatewayStatus() == null || gatewayBean.getGatewayStatus().length() <= 0){ //网关新增未注册状态
                    gatewayBean.setStatus("-1");
                }else{
                    String status = redisClientTemplate.get(gatewayBean.getGatewaySerialnumber());
                    if (StringUtils.isEmpty(status)) status = "";
                    gatewayBean.setStatus(status);
                }
            }
            backMap.put(Constant.DATA, JSON.toJSON(list));
            return backMap;
        } catch (Exception e) {
            exceptionInfo (e);
            logger.info(exceptionInfo(e));
            backMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
            backMap.put(Constant.MESSAGE, "网关管理分页查询");
            backMap.put(Constant.DATA, null);
            return backMap;
        }
    }

    public String makeUidStr(GatewayGroup gatewayGroupByName){
        String [] gateWayuuidArr = null;
        StringBuffer sb = new StringBuffer();
        String par = "";
        if(gatewayGroupByName != null){
            logger.info("");
            if(gatewayGroupByName.getGroupUuid()!= null){
                gateWayuuidArr = gatewayGroupByName.getGatewayUuid().toString().split(",");
                if(gateWayuuidArr.length > 0){
                    sb.append("(");
                    for (int i = 0; i < gateWayuuidArr.length; i++) {
                        sb.append("," + "'" + gateWayuuidArr[i].trim() + "'");
                    }
                    sb.append(")");
                }
                par = sb.toString().replaceFirst(",", "");
                logger.info(" gatewayUuid arr is " + par);
            }
        }
        return par;
    }

    /**
     * 功能:dubbo服务接口、网关详情查询功能
     *
     * @param map 请求参数
     * @return
     */
    public Map<String, Object> queryGatewayDetail(Map<String, Object> map) {
        // TODO Auto-generated method stub
        Map<String, Object> backMap = new HashMap<String, Object>();
        boolean rightFlag = false;
        if(!"".equals(map.get("uid").toString()) || null != map.get("uid").toString()){
            rightFlag = adminService.isSafeManager(map.get("uid").toString());
        }else{
            logger.info("queryGatewayDetail method  uid can't get!");
        }
        try {
            GateWayDetail gateWayDetail = new GateWayDetail();
            String gatewayUuid = map.get("gatewayUuid").toString();
            GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayUuid);
            HardwareAblity hardwareAblity = null;
            if(gatewayInfo != null){
                hardwareAblity = hardwareAblityMapper.selectByDeviceUuid(gatewayInfo.getGatewayDeviceUuid());
            }else{
                logger.info("gatewayInfo is null ! ");
                backMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
                backMap.put(Constant.MESSAGE, "找不到网关 " +gatewayUuid + " 的详细信息");
                backMap.put(Constant.DATA, null);
                return backMap;
            }
            if (hardwareAblity != null) {
                if(hardwareAblity.getHardwareAblityUuid() == null || "".equals(hardwareAblity.getHardwareAblityUuid())){}else{
                    gateWayDetail.setHardwareAblityUuid(hardwareAblity.getHardwareAblityUuid());
                }
                if(hardwareAblity.getHardwareAblityLanCount() == null || "".equals(hardwareAblity.getHardwareAblityLanCount())){}else{
                    gateWayDetail.setHardwareAblityLanCount(hardwareAblity.getHardwareAblityLanCount());
                }
                if(hardwareAblity.getHardwareAblityUsbCount() == null || "".equals(hardwareAblity.getHardwareAblityUsbCount())){}else{
                    gateWayDetail.setHardwareAblityUsbCount(hardwareAblity.getHardwareAblityUsbCount());
                }
                if(hardwareAblity.getHardwareAblitySupportWifi() != null){
                    if (hardwareAblity.getHardwareAblitySupportWifi()) {
                        gateWayDetail.setHardwareAblitySupportWifi("是");
                    } else {
                        gateWayDetail.setHardwareAblitySupportWifi("否");
                    }
                }
                if(hardwareAblity.getHardwareAblityWifiLoc() == null || "".equals(hardwareAblity.getHardwareAblityWifiLoc())){}else{
                    gateWayDetail.setHardwareAblityWifiLoc(hardwareAblity.getHardwareAblityWifiLoc());
                }
                if(hardwareAblity.getHardwareAblityWifiCount() == null || "".equals(hardwareAblity.getHardwareAblityWifiCount())){}else{
                    gateWayDetail.setHardwareAblityWifiCount(hardwareAblity.getHardwareAblityWifiCount());
                }
                if(hardwareAblity.getHardwareAblityWifiSize() == null || "".equals(hardwareAblity.getHardwareAblityWifiSize())){}else{
                    gateWayDetail.setHardwareAblityWifiSize(hardwareAblity.getHardwareAblityWifiSize());
                }
                if(hardwareAblity.getHardwareAblitySupportWifi24ghz() == null || "".equals(hardwareAblity.getHardwareAblitySupportWifi24ghz())){}else{
                    gateWayDetail.setHardwareAblitySupportWifi24ghz(hardwareAblity.getHardwareAblitySupportWifi24ghz());
                }
                if(hardwareAblity.getHardwareAblitySupportWifi58ghz() == null || "".equals(hardwareAblity.getHardwareAblitySupportWifi58ghz())){}else{
                    gateWayDetail.setHardwareAblitySupportWifi58ghz(hardwareAblity.getHardwareAblitySupportWifi58ghz());}
                if(hardwareAblity.getHardwareAblityIpv4v6() == null || "".equals(hardwareAblity.getHardwareAblityIpv4v6())){}else{
                    gateWayDetail.setHardwareAblityIpv4v6(hardwareAblity.getHardwareAblityIpv4v6());
                }
            }else{
                logger.info("hardwareAblity is null ! ");
            }
            if (gatewayInfo != null) {
                gateWayDetail.setGatewayInfoUuid(gatewayInfo.getGatewayUuid());

                if(gatewayInfo.getGatewayType().equals("iHGU"))
                {
                    gateWayDetail.setGatewayInfoType("智能网关");
                }
                else if(gatewayInfo.getGatewayType().equals("HGU"))
                {
                    gateWayDetail.setGatewayInfoType("非智能网关");
                }
                else
                {
                    gateWayDetail.setGatewayInfoType(gatewayInfo.getGatewayType());
                }

                gateWayDetail.setGatewayInfoModel(gatewayInfo.getGatewayModel());
                gateWayDetail.setGatewayInfoVersion(gatewayInfo.getGatewayVersion());
                gateWayDetail.setGatewayInfoSerialnumber(gatewayInfo.getGatewaySerialnumber());
                gateWayDetail.setGatewayInfoHardwareVersion(gatewayInfo.getGatewayHardwareVersion());
                gateWayDetail.setGatewayInfoStatus(gatewayInfo.getGatewayStatus());
                gateWayDetail.setGatewayInfoIpaddress(gatewayInfo.getGatewayIpaddress());
                gateWayDetail.setGatewayInfoMacaddress(gatewayInfo.getGatewayMacaddress());
                gateWayDetail.setGatewayInfoJoinTime(gatewayInfo.getGatewayJoinTime());
                gateWayDetail.setGatewayInfoLastConnTime(gatewayInfo.getGatewayLastConnTime());
                gateWayDetail.setGatewayConnectionrequesturl(gatewayInfo.getGatewayConnectionrequesturl());
                gateWayDetail.setGatewayFamilyAccount(gatewayInfo.getGatewayFamilyAccount());
                gateWayDetail.setGatewayDigestAccount(gatewayInfo.getGatewayDigestAccount());
                gateWayDetail.setOsgi(gatewayInfo.getOsgi());
                gateWayDetail.setJvm(gatewayInfo.getJvm());
                gateWayDetail.setGatewayConnectionrequestUsername(gatewayInfo.getGatewayConnectionrequestUsername());
                gateWayDetail.setOui(gatewayInfo.getGatewayFactoryCode());
                if(rightFlag){
                    gateWayDetail.setGatewayPassword(gatewayInfo.getGatewayPassword());
                    gateWayDetail.setGatewayDigestPassword(gatewayInfo.getGatewayDigestPassword());
                    gateWayDetail.setGatewayConnectionrequestPassword(gatewayInfo.getGatewayConnectionrequestPassword());
                    gateWayDetail.setGatewayFamilyPassword(gatewayInfo.getGatewayFamilyPassword());
                }else{
                    String nullPwd="******";
                    gateWayDetail.setGatewayPassword(nullPwd);
                    gateWayDetail.setGatewayDigestPassword(nullPwd);
                    gateWayDetail.setGatewayConnectionrequestPassword(nullPwd);
                    gateWayDetail.setGatewayFamilyPassword(nullPwd);
                }
                gateWayDetail.setFlowrate(gatewayInfo.getFlowRate());
                gateWayDetail.setGatewayName(gatewayInfo.getGatewayName());
            }

            String facName="";
            if(null!=gatewayInfo.getNewFactoryCode()||!"".equals(gatewayInfo.getNewFactoryCode())){
                logger.info(" NewFactoryCode " + gatewayInfo.getNewFactoryCode());
                Manufacturer record = new Manufacturer();
                //因表结构设计变固不使用下面方法
				// record.setCode(gatewayInfo.getNewFactoryCode());
                record.setId(gatewayInfo.getNewFactoryCode());
                List<Manufacturer> manufacturerList = manufacturerMapper.querySelective(record);
                if(manufacturerList.size() > 0){
                    facName = manufacturerList.get(0).getManufacturerName();
                }
            }
            gateWayDetail.setGatewayInfoFactory(facName);

            backMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
            backMap.put(Constant.MESSAGE, "网关详情查询功能");
            backMap.put(Constant.DATA, JSON.toJSON(gateWayDetail));
            return backMap;
        } catch (Exception e) {
            e.printStackTrace();
            backMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
            backMap.put(Constant.MESSAGE, "网关详情查询出错");
            backMap.put(Constant.DATA, null);
            return backMap;
        }
    }

    @Override
    public Map<String, Object> uploadFile(Map<String, Object> parameter) {
        Map<String, Object> resultMap = new HashMap<>();
        String str = JSON.toJSONString(parameter.get("gateWayExcels"));
        logger.info("Excel文件上传传入参数："+str);
        List<GateWayExcelContent> listBooks;
        try {
            listBooks = JSON.parseArray(str, GateWayExcelContent.class);
        }catch (Exception e){
            e.printStackTrace();
            logger.info("上传失败,请确认上传的字段类型是否正确！");
            resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
            resultMap.put(Constant.MESSAGE, "上传失败,请确认上传的字段类型是否正确！");
            resultMap.put(Constant.DATA, false);
            return resultMap;
        }

        try {
            if (listBooks != null) {
                if (listBooks == null || listBooks.size() < 1) {
                    logger.info("上传失败 请确认上传文件类型和模板一致！");
                    resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                    resultMap.put(Constant.MESSAGE, "上传失败 请确认上传文件类型和模板一致！");
                    resultMap.put(Constant.DATA, false);
                    return resultMap;
                } else {
                	Map<String, Object> retmap = checkContent(listBooks);
                    if (retmap == null || retmap.isEmpty()) {//excel表格是否填寫完整
                    	Map<String, Object> errorContentMap = checkDevice(listBooks);
                        if (errorContentMap.isEmpty()) {//每个网关都能够找到设备
                        	errorContentMap = checkFireware(listBooks);
                            if (errorContentMap.isEmpty()) {
                                //所有验证通过
                                boolean flag = insertOrUpdateGateway(listBooks);
                                if(!flag){
                                	//固件不存在
                                	resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                                    resultMap.put(Constant.MESSAGE, "导入网关失败");
                                    resultMap.put(Constant.DATA, false);
                                    return resultMap;

                                }

                                //导入网关信息同步到杭研   importFileUrl


                                if(parameter.get("importFileUrl") != null && !"".equals(parameter.get("importFileUrl"))){
                                	Map<String, Object> reportMap = new HashMap<String, Object>();
                                	reportMap.put("RPCMethod", "Report");
                                	reportMap.put("ID", (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                                	reportMap.put("CmdType", "REPORT_HG_BASIC_INFO");
                                	reportMap.put(Constant.SEQUENCEID, InstructionUtil.generate8HexString());
                                	Map<String, Object> reqmap = new HashMap<String, Object>();
                                	reqmap.put("InfoFilePath", parameter.get("importFileUrl"));
                                	reportMap.put("Parameter", reqmap);
                                	syncInfoToFirstLevelPlatformService.report("reportHgBasicInfo",reportMap);
                                }

                                Map<String, Object> parameterLog = new HashMap<>();
                                // 操作的数据内容
                                parameterLog.put("content", JSON.toJSONString(listBooks));
                                // 登录用户名称
                                parameterLog.put("userName", parameter.get("userName"));
                                // 类目ID(菜单ID)
                                parameterLog.put("categoryMenu", CategoryEnum.GATEWAY_MANAGER_SERVICE.name());
                                // 具体的操作
                                parameterLog.put("operation", "导入网关信息");
                                // 角色名称
                                parameterLog.put("roleName", parameter.get("roleName"));
                                // 类目名称
                                parameterLog.put("categoryMenuName", CategoryEnum.GATEWAY_MANAGER_SERVICE.description());
                                // 日志类型
                                parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
                                logManagerService.recordOperationLog(parameterLog);

                                resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
                                resultMap.put(Constant.MESSAGE, "");
                                resultMap.put(Constant.DATA, true);
                                return resultMap;
                            } else {
                                String msg = "上传失败,第"+errorContentMap.get("index")+"条数据错误,请在固件管理模块录入厂商编码:" + ((GateWayExcelContent)errorContentMap.get("gateway")).getGatewayInfoFactoryCode()
                                        + "  设备型号:" + ((GateWayExcelContent)errorContentMap.get("gateway")).getGatewayInfoModel() + "  固件版本:" + ((GateWayExcelContent)errorContentMap.get("gateway")).getGatewayInfoVersion() + ",再次上传！";
                                logger.info(msg);
                                resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                                resultMap.put(Constant.MESSAGE, msg);
                                resultMap.put(Constant.DATA, false);
                                return resultMap;
                            }
                        } else {
                            String msg = "上传失败,第"+errorContentMap.get("index")+"条数据错误，请在设备管理模块录入厂家编码:" + ((GateWayExcelContent)errorContentMap.get("gateway")).getGatewayInfoFactoryCode()
                                    + "  设备型号:" + ((GateWayExcelContent)errorContentMap.get("gateway")).getGatewayInfoModel() + ",再次上传！";

                            logger.info(msg);
                            resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                            resultMap.put(Constant.MESSAGE, msg);
                            resultMap.put(Constant.DATA, false);
                            return resultMap;
                        }
                    } else {
                        logger.info("上传失败,请按照下载的导入模版填入全部字段后再上传！");
                        resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                        resultMap.put(Constant.MESSAGE, "上传失败,第"+retmap.get("index")+"条数据错误,"+retmap.get("filed")+"不能为空，请按照下载的导入模版填入全部字段后再上传！");
                        resultMap.put(Constant.DATA, false);
                        return resultMap;
                    }
                }
            } else {
                logger.info("上传失败,文件上传为空");
                resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                resultMap.put(Constant.MESSAGE, "上传失败,文件上传为空");
                resultMap.put(Constant.DATA, false);
                return resultMap;
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.info(" 上传失败错误信息 " +e.toString());
            resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
            resultMap.put(Constant.MESSAGE, "上传失败,系统内部错误" );
            resultMap.put(Constant.DATA, false);
            return resultMap;
        }
    }

    @Override
    public Map<String, Object> reboot(Map<String, Object> parameter) {
        Map<String, Object> resultMap = new HashMap<>();
        List<String> list = (List<String>) parameter.get("gatewayIds");
        List<String> gatewayIdLlist = new ArrayList<>();
        try {
            List<GatewayInfo> cpes = gatewayInfoService.queryListByIds(list);
            // 对网关状态的排查
            for (GatewayInfo cpe : cpes) {
                String gatewayId = cpe.getGatewayUuid();
                String serialnumber = "R-F-" + cpe.getGatewaySerialnumber();//为了保证重启和恢复出厂key唯一且不和其他指令冲突，加上前缀R-F-
                logger.info(" serialnumber ：" + serialnumber);
                // SN为唯一，对其锁处理，设置超时时间为10分钟
                String str = redisClientTemplate.set(serialnumber,RebootEnum.STATUS_0.code(),"NX","EX",rebootTimeOut);
                logger.info("网关重启在redis添加key为:"+serialnumber+"的锁,返回的状态为:"+str);
                logger.info("redis查询数据 锁后值："+str);
                if (str == null){// 存在锁
                    // 移除已经处于指令下发状态的网关
                    list.remove(gatewayId);
                }else {
                    // 写入redis的SN数据信息
                    gatewayIdLlist.add(serialnumber);
                }
            }
            // 操作的网关已经被另外一个操作  无需重复操作
            if (list.size() == 0){
                logger.info("网关正在重启中，无需重复操作");
                resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
                resultMap.put(Constant.MESSAGE, "网关正在重启中，无需重复操作");
                resultMap.put(Constant.DATA, true);
                return resultMap;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("gatewayIds", list);
            map.put("methodName", "Reboot");
            Map<String, Object> result = invokeInsService.executeBatch(map);
            if(result == null){
            	resultMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
                resultMap.put(Constant.MESSAGE, "重启指令下发失败");
                resultMap.put(Constant.DATA, false);
            }else{
	            resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
	            resultMap.put(Constant.MESSAGE, "成功");
	            resultMap.put(Constant.DATA, true);
            }
            Map<String, Object> parameterLog = new HashMap<>();
            // 操作的数据内容
            parameterLog.put("content", JSON.toJSONString(list));
            // 登录用户名称
            parameterLog.put("userName", parameter.get("userName"));
            // 类目ID(菜单ID)
            parameterLog.put("categoryMenu", CategoryEnum.GATEWAY_MANAGER_SERVICE.name());
            // 具体的操作
            parameterLog.put("operation", "设备重启");
            // 角色名称
            parameterLog.put("roleName", parameter.get("roleName"));
            // 类目名称
            parameterLog.put("categoryMenuName", CategoryEnum.GATEWAY_MANAGER_SERVICE.description());
            // 日志类型
            parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
            logManagerService.recordOperationLog(parameterLog);
			/**网关重启后上报时进行删除锁的处理
            // 不关成功还是失失败删除锁
			for (String str : gatewayIdLlist) {
				redisClientTemplate.del(str);
				 logger.info("网关重启业务处理后状态为:"+resultMap.get(Constant.MESSAGE)+",将删除redis锁的key为:"+str);
			}**/
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            // 释放写入redis中的SN数据
            for (String str : gatewayIdLlist){
                // 抛异常的时候释放锁
                redisClientTemplate.del(str);
                logger.info("网关重启业务处理异常时删除redis锁的key为:"+str);
            }
            logger.error(e.getMessage(), e.getCause());
            resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
            resultMap.put(Constant.MESSAGE, "重启指令下发网络连接异常");
            resultMap.put(Constant.DATA, false);
            return resultMap;
        }
    }

    @Override
    public Map<String, Object> factoryReset(Map<String, Object> parameter) {
    	logger.info("start invoke factoryReset:{}", parameter);
        Map<String, Object> resultMap = new HashMap<>();
        List<String> list = (List<String>) parameter.get("gatewayIds");
        // 组装新的网关ID
        List<String> gatewayIdLlist = new ArrayList<>();
        try {
            List<GatewayInfo> cpes = gatewayInfoService.queryListByIds(list);
            List<GatewayInfo> needExecuteList = new ArrayList<GatewayInfo>();
            // 对网关状态的排查
            for (GatewayInfo cpe : cpes) {
                String gatewayId = cpe.getGatewayUuid();
                String serialnumber = "R-F-" + cpe.getGatewaySerialnumber();//为了保证重启和恢复出厂key唯一且不和其他指令冲突，加上前缀R-F-
                // SN为唯一，对其锁处理，设置超时时间为10分钟
                String str = redisClientTemplate.set(serialnumber,RebootEnum.STATUS_1.code(),"NX","EX",factoryResetTimeOut);
                logger.info("网关恢复出厂在redis添加key为:"+serialnumber+"的锁,返回的状态为:"+str);
                if (str == null){// 存在锁
                    // 移除已经处于指令下发状态的网关
                    list.remove(gatewayId);
                }else {
                	needExecuteList.add(cpe);
                    // 写入redis的SN数据信息
                    gatewayIdLlist.add(serialnumber);
                }
            }
            // 操作的网关已经被另外一个操作  无需重复操作
            if (list.size() == 0){
                logger.info("网关正在恢复出厂中，无需重复操作");
                resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
                resultMap.put(Constant.MESSAGE, "网关正在恢复出厂中，无需重复操作");
                resultMap.put(Constant.DATA, true);
                return resultMap;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("gatewayIds", list);
            map.put("methodName", "FactoryReset");
            Map<String, Object> result = invokeInsService.executeBatch(map);
            if(result == null){
            	resultMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
                resultMap.put(Constant.MESSAGE, "恢复出厂设置下发指令失败");
                resultMap.put(Constant.DATA, false);
            }else{
            	for(GatewayInfo gw : needExecuteList){
            		//清理网关表、网关密码表中的password和宽带账号信息，网关业务状态置为未开通
            		String gatewayMac = gw.getGatewayMacaddress();
            		String gatewayId = gw.getGatewayUuid();
            		gw.setGatewayAdslAccount("");
            		gw.setGatewayPassword("");
            		gatewayInfoMapper.updateByPrimaryKey(gw);
            		GatewayBusinessOpenDetail gbod = new GatewayBusinessOpenDetail();
            		gbod.setGatewayUuid(gatewayId);
            		gbod.setOpenStatus("0");
            		gatewayBusinessOpenDetailMapper.updateByGatewayUuid(gbod);
            		logger.info("网关恢复出厂，清理MAC为:{}的网关的password和宽带账号信息，网关业务状态置为未开通", gatewayMac);
            		//通知BMS
            		try {
            			Map<String,Object> resultbms = gatewayHandleService.factoryNotify(gw.getGatewayMacaddress(), "4", false);
            			logger.info("通知BMS恢复出厂设置并调用一级平台销户接口，请求MAC:"+ gw.getGatewayMacaddress() +",返回结果"+ resultbms.get("resultMsg"));
            			logger.info("通知BMS恢复出厂设置并调用一级平台销户接口，请求MAC:"+ gw.getGatewayMacaddress() +",由于异步处理，不等待BMS返回，所以返回结果为空");
            			
            		} catch (Exception e) {
            			//logger.info("通知BMS恢复出厂设置并调用一级平台销户接口，请求MAC:"+ gw.getGatewayMacaddress() +",异常:"+ e.getMessage());
            		}
            	}
            	
	            resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
	            resultMap.put(Constant.MESSAGE, "成功");
	            resultMap.put(Constant.DATA, true);
            }
            Map<String, Object> parameterLog = new HashMap<>();
            // 操作的数据内容
            parameterLog.put("content", JSON.toJSONString(list));
            // 登录用户名称
            parameterLog.put("userName", parameter.get("userName"));
            // 类目ID(菜单ID)
            parameterLog.put("categoryMenu", CategoryEnum.GATEWAY_MANAGER_SERVICE.name());
            // 具体的操作
            parameterLog.put("operation", "设备恢复出厂设置");
            // 角色名称
            parameterLog.put("roleName", parameter.get("roleName"));
            // 类目名称
            parameterLog.put("categoryMenuName", CategoryEnum.GATEWAY_MANAGER_SERVICE.description());
            // 日志类型
            parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
            logManagerService.recordOperationLog(parameterLog);
            return resultMap;
        } catch (Exception e) {
            // 释放写入redis中的SN数据
            for (String str : gatewayIdLlist){
                // 抛异常的时候释放锁
                redisClientTemplate.del(str);
            }
            logger.error(e.getMessage(), e.getCause());
            resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
            resultMap.put(Constant.MESSAGE, "恢复出厂设置下发指令网络连接下发异常");
            resultMap.put(Constant.DATA, false);
            return resultMap;
        }
    }

    // 检查excel表格是否填写完整
    private Map<String, Object> checkContent(List<GateWayExcelContent> listBooks) {
    	Map<String, Object> retMap = new HashMap<String, Object>();
        for (int i=0;i<listBooks.size();i++) {
        	 GateWayExcelContent gateWayExcelContent = listBooks.get(i);
            if (
                    StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoFactoryCode()) ||
                            StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoHardwareVersion()) ||
                            StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoMacaddress()) ||
/*                            StringUtils.isEmpty(gateWayExcelContent.getHardwareAblityLanCount()) ||
                            StringUtils.isEmpty(gateWayExcelContent.getHardwareAblityUsbCount()) ||
                            StringUtils.isEmpty(gateWayExcelContent.getHardwareAblitySupportWifi()) ||
                            StringUtils.isEmpty(gateWayExcelContent.getHardwareAblityWifiLoc()) ||
                            StringUtils.isEmpty(gateWayExcelContent.getHardwareAblityWifiCount()) ||
                            StringUtils.isEmpty(gateWayExcelContent.getHardwareAblityWifiSize()) ||
                            StringUtils.isEmpty(gateWayExcelContent.getHardwareAblitySupportWifi24ghz()) ||
                            StringUtils.isEmpty(gateWayExcelContent.getHardwareAblitySupportWifi58ghz()) ||
                            StringUtils.isEmpty(gateWayExcelContent.getHardwareAblityIpv4v6()) ||*/
                            StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoModel()) ||
                            StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoVersion()) ||
                            StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoSerialnumber())
                    ) {
            	
            	retMap.put("index", i+1);
                return retMap;
            }
            
            if(StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoFactoryCode())){
            	retMap.put("index", i+1);
            	retMap.put("filed", "网关厂家编码");
                return retMap;
            }
            if(StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoHardwareVersion())){
            	retMap.put("index", i+1);
            	retMap.put("filed", "硬件版本");
            	return retMap;
            }
            if(StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoMacaddress())){
            	retMap.put("index", i+1);
            	retMap.put("filed", "MAC地址");
            	return retMap;
            }
            if(StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoModel())){
            	retMap.put("index", i+1);
            	retMap.put("filed", "网关型号");
            	return retMap;
            }
            if(StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoVersion())){
            	retMap.put("index", i+1);
            	retMap.put("filed", "固件版本");
            	return retMap;
            }
            if(StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoSerialnumber())){
            	retMap.put("index", i+1);
            	retMap.put("filed", "网关SN");
            	return retMap;
            }
            
        }
        return retMap;
    }

    //根据厂商编码和设备类型查找设备
    private Map<String, Object> checkDevice(List<GateWayExcelContent> listBooks) {
        Map<String, Object> retMap = new HashMap<String, Object>();
        
    	for (int i=0;i<listBooks.size();i++) {
        	GateWayExcelContent gateWayExcelContent = listBooks.get(i);
            DeviceInfo deviceInfo = getDeviceInfo(gateWayExcelContent.getGatewayInfoFactoryCode(), gateWayExcelContent.getGatewayInfoModel());
            if (deviceInfo == null) {
            	retMap.put("index", i+1);
            	retMap.put("gateway", gateWayExcelContent);
                return retMap;
            }
        }
        return retMap;
    }

    //根据设备查找固件 与网关的固件版本进行对比
    private Map<String, Object> checkFireware(List<GateWayExcelContent> listBooks) {
    	
    	
    	
    	Map<String, Object> retMap =new HashMap<String, Object>();
    	for (int i=0;i<listBooks.size();i++ ) {
    		Map<String, Object> params = new HashMap<String, Object>();
    		
    		
    		GateWayExcelContent gateWayExcelContent = listBooks.get(i);
    		params.put("deviceFactory", gateWayExcelContent.getGatewayInfoFactoryCode());
    		params.put("firmwareVersion", gateWayExcelContent.getGatewayInfoVersion());
    		params.put("deviceModel", gateWayExcelContent.getGatewayInfoModel());
    		Map<String, Object> map  = firmwareInfoService.searchFirmwareId(params);
    		if(map != null && Integer.parseInt(map.get("resultCode").toString()) == 0){
    			if(map.get("data") != null){
    				
    				Map<String, Object> m = (Map<String, Object>) map.get("data");
    				if(m.isEmpty()){
    					retMap.put("index", i+1);
    					retMap.put("gateway", gateWayExcelContent);
    					return retMap;
    				}
    			}else{
    				retMap.put("index", i+1);
                	retMap.put("gateway", gateWayExcelContent);
                    return retMap;
    			}
	   		    
	   		}else{
	   			retMap.put("index", i+1);
            	retMap.put("gateway", gateWayExcelContent);
                return retMap;
	   		}
    		
    		
          /*  DeviceInfo deviceInfo = getDeviceInfo(gateWayExcelContent.getGatewayInfoFactoryCode(), gateWayExcelContent.getGatewayInfoModel());
            //根据设备ID查找固件
            FirmwareInfo firmwareInfo1 = getFirmwareInfo(deviceInfo, gateWayExcelContent);
            if (firmwareInfo1 == null) {
            	
            	retMap.put("index", i+1);
            	retMap.put("gateway", gateWayExcelContent);
                return retMap;
            }*/
        }
        return retMap;
    }

    private DeviceInfo getDeviceInfo(String code, String model) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceFactory(code);
        deviceInfo.setDeviceModel(model);
        List<DeviceInfo> deviceInfoList = deviceInfoMapper.searchByCodeAndModel(deviceInfo);
        if (deviceInfoList.size() < 1) {
            return null;
        }
        return deviceInfoList.get(0);
    }

    private boolean insertOrUpdateGateway(List<GateWayExcelContent> listBooks) {
    	List<GatewayInfo> allDatas = new ArrayList<GatewayInfo>();
    	List<HardwareAblity> allHardwareAblityDatas = new ArrayList<HardwareAblity>();
    	boolean flag = true;
    	for (int k=0; k<listBooks.size(); k++) {
    	//	for (GateWayExcelContent gateWayExcelContent : listBooks) {
          //  String uuid = gateWayExcelContent.getGatewayInfoFactory() + Constant.SEPARATOR + gateWayExcelContent.getGatewayInfoSerialnumber();
           // if (gatewayInfoService.selectByUuid(uuid) == null) {
    		GateWayExcelContent gateWayExcelContent = listBooks.get(k);
            logger.info(String.valueOf(gateWayExcelContent.toString()));
    		String frimwareUUID = "";
    		if(deviceInfoList == null){
    			
    			Map<String, Object> map = queryFirmwareInfo(gateWayExcelContent);
    			if(map == null){
    				logger.info("固件版本不存在，导入本条数据失败："+gateWayExcelContent);
    				//continue;
    				flag = false;
    				return flag;
    			}else{
                    deviceInfoList = new ArrayList<>();
    				frimwareUUID = map.get("firmwareId").toString();
    				deviceInfoList.add(map);
    			}
    		}else{
    			for(int i=0; i<deviceInfoList.size();i++ ){
    				
    				Map<String, Object> map = deviceInfoList.get(i);
    				if(map.get("deviceFactory").equals(gateWayExcelContent.getGatewayInfoFactory())
    						&& map.get("firmwareVersion").equals(gateWayExcelContent.getGatewayInfoHardwareVersion())
    						&& map.get("deviceModel").equals(gateWayExcelContent.getGatewayInfoModel())){
    					frimwareUUID = map.get("firmwareId").toString();
    				}
    				if(!"".equals(frimwareUUID)){
    					break;
    				}
    			}
    			//已经查询的列表中还是没有找到uuid，就继续调用接口查询
    			if("".equals(frimwareUUID)){
    				//遍历历史查询中没有对应固件版本的列表，如果在此列表中，就不再调用接口
    				if(nonDeviceInfoList != null && nonDeviceInfoList.size()>0){
    					Map<String, Object> params = new HashMap<String, Object>();
    					params.put("deviceFactory", gateWayExcelContent.getGatewayInfoFactory());
    					params.put("firmwareVersion", gateWayExcelContent.getGatewayInfoHardwareVersion());
    					params.put("deviceModel", gateWayExcelContent.getGatewayInfoModel());
    					if(nonDeviceInfoList.contains(params)){
    						//continue;
    						flag = false;
    	    				return flag;
    					}
    				}
    				
    				Map<String, Object> map = queryFirmwareInfo(gateWayExcelContent);
        			if(map == null){
        				logger.info("固件版本不存在，导入本条数据失败："+gateWayExcelContent);
        				flag = false;
        				return flag;
        				//continue;
        			}else{
        				deviceInfoList.add(map);
        				frimwareUUID = map.get("firmwareId").toString();
        			}
    			}
    		}
    		
    		GatewayInfo gatewayInfo = new GatewayInfo();
            gatewayInfo.setGatewayMacaddress(gateWayExcelContent.getGatewayInfoMacaddress());
            gatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
            if (gatewayInfo == null) {
                //不存在此网关 插入
                gatewayInfo = new GatewayInfo();
                gatewayInfo.setGatewayUuid(UniqueUtil.uuid());
                HardwareAblity hardwareAblity = new HardwareAblity();
                DeviceInfo deviceInfo = getDeviceInfo(gateWayExcelContent.getGatewayInfoFactoryCode(), gateWayExcelContent.getGatewayInfoModel());
   //            FirmwareInfo firmwareInfo = getFirmwareInfo(deviceInfo, gateWayExcelContent);
                //设置网关终端信息默认值
                gatewayInfo.setGatewayDeviceUuid(deviceInfo.getId());
   //             gatewayInfo.setGatewayFirmwareUuid(firmwareInfo.getId());
                gatewayInfo.setGatewayFirmwareUuid(frimwareUUID);
                gatewayInfo.setGatewayType(gateWayExcelContent.getGatewayInfoType());
                gatewayInfo.setGatewayModel(gateWayExcelContent.getGatewayInfoModel());
                gatewayInfo.setGatewayName("");
                gatewayInfo.setGatewayVersion(gateWayExcelContent.getGatewayInfoVersion());
                gatewayInfo.setGatewaySerialnumber(gateWayExcelContent.getGatewayInfoSerialnumber());

                //厂家名称和厂家编码
                gatewayInfo.setGatewayFactoryCode(gateWayExcelContent.getGatewayInfoFactoryCode());
                gatewayInfo.setGatewayFactory("");
                gatewayInfo.setGatewayMemo("");
                gatewayInfo.setGatewayHardwareVersion(gateWayExcelContent.getGatewayInfoHardwareVersion());
                gatewayInfo.setGatewayJoinTime(null);
                gatewayInfo.setGatewayLastConnTime(null);
                gatewayInfo.setGatewayAdslAccount("");
                gatewayInfo.setGatewayIpaddress(gateWayExcelContent.getGatewayInfoIpaddress());
                gatewayInfo.setGatewayMacaddress(gateWayExcelContent.getGatewayInfoMacaddress());
                gatewayInfo.setGatewayStatus("");
                gatewayInfo.setGatewayDigestAccount("");
                gatewayInfo.setGatewayDigestPassword("");
                gatewayInfo.setGatewayConnectionrequesturl("");
                /*gatewayInfo.setGatewayAreaId(gateWayExcelContent.getGatewayInfoAreaId());*/
                if(!StringUtils.isEmpty(gateWayExcelContent.getGatewayPassword())){
                    gatewayInfo.setGatewayPassword(gateWayExcelContent.getGatewayPassword());
                }
                gatewayInfo.setOsgi(gateWayExcelContent.getOsgi());
                gatewayInfo.setJvm(gateWayExcelContent.getJvm());
         //       gatewayInfoService.addSelectiveGatewayInfo(gatewayInfo);
                allDatas.add(gatewayInfo);
                

                // 终端硬件能力
                /*if(gatewayInfo.getGatewayUuid() == null || "".equals(gatewayInfo.getGatewayUuid())){
                }else{
                    hardwareAblity.setGatewayInfoUuid(gatewayInfo.getGatewayUuid());
                }*/
                if(gateWayExcelContent.getHardwareAblityLanCount() == null || "".equals(gateWayExcelContent.getHardwareAblityLanCount())){
                }else{
                    hardwareAblity.setHardwareAblityLanCount(gateWayExcelContent.getHardwareAblityLanCount());
                }
                if(gateWayExcelContent.getHardwareAblityUsbCount() == null || "".equals(gateWayExcelContent.getHardwareAblityUsbCount())){
                }else{
                    hardwareAblity.setHardwareAblityUsbCount(gateWayExcelContent.getHardwareAblityUsbCount());
                }

                if(gateWayExcelContent.getHardwareAblitySupportWifi() == null || "".equals(gateWayExcelContent.getHardwareAblitySupportWifi())) {
                }else{
                    if (gateWayExcelContent.getHardwareAblitySupportWifi().equals("是")) {
                        hardwareAblity.setHardwareAblitySupportWifi(true);
                    } else {
                        hardwareAblity.setHardwareAblitySupportWifi(false);
                    }
                }
                if(gateWayExcelContent.getHardwareAblityWifiLoc() == null || "".equals(gateWayExcelContent.getHardwareAblityWifiLoc())) {
                }else{
                    hardwareAblity.setHardwareAblityWifiLoc(gateWayExcelContent.getHardwareAblityWifiLoc());
                }
                if(gateWayExcelContent.getHardwareAblityWifiCount() == null||"".equals(gateWayExcelContent.getHardwareAblityWifiCount())){
                }else{
                    hardwareAblity.setHardwareAblityWifiCount(gateWayExcelContent.getHardwareAblityWifiCount());
                }
                if(gateWayExcelContent.getHardwareAblityWifiSize() == null||"".equals(gateWayExcelContent.getHardwareAblityWifiSize())){
                }else{
                    hardwareAblity.setHardwareAblityWifiSize(gateWayExcelContent.getHardwareAblityWifiSize());
                }
                if(gateWayExcelContent.getHardwareAblitySupportWifi24ghz() == null||"".equals(gateWayExcelContent.getHardwareAblitySupportWifi24ghz())){
                }else{
                    hardwareAblity.setHardwareAblitySupportWifi24ghz(gateWayExcelContent.getHardwareAblitySupportWifi24ghz());
                }
                if(gateWayExcelContent.getHardwareAblitySupportWifi58ghz() == null || "".equals(gateWayExcelContent.getHardwareAblitySupportWifi58ghz())){
                }else{
                    hardwareAblity.setHardwareAblitySupportWifi58ghz(gateWayExcelContent.getHardwareAblitySupportWifi58ghz());
                }
                if(gateWayExcelContent.getHardwareAblityIpv4v6()==null || "".equals(gateWayExcelContent.getHardwareAblityIpv4v6())){
                }else{
                    hardwareAblity.setHardwareAblityIpv4v6(gateWayExcelContent.getHardwareAblityIpv4v6());
                }
                //           hardwareAblityService.addHardwareAblity(hardwareAblity);
                if(gatewayInfo.getGatewayUuid() == null && gateWayExcelContent.getHardwareAblityLanCount() == null && gateWayExcelContent.getHardwareAblityUsbCount() == null
                        && gateWayExcelContent.getHardwareAblitySupportWifi()== null && gateWayExcelContent.getHardwareAblityWifiLoc()==null
                        && gateWayExcelContent.getHardwareAblityWifiCount()==null && gateWayExcelContent.getHardwareAblityWifiSize()==null &&
                        gateWayExcelContent.getHardwareAblitySupportWifi24ghz()==null && gateWayExcelContent.getHardwareAblitySupportWifi58ghz()==null
                        &&gateWayExcelContent.getHardwareAblityIpv4v6()==null){
                }else{
                    hardwareAblity.setHardwareAblityUuid(UniqueUtil.uuid());
                    allHardwareAblityDatas.add(hardwareAblity);
                }
                logger.info(" gateWayExcelContent wifiCount " + String.valueOf(gateWayExcelContent.getHardwareAblityWifiCount()));
                logger.info(" gateWayExcelContent wifi24ghz " + String.valueOf(gateWayExcelContent.getHardwareAblitySupportWifi24ghz()));
                logger.info(" gateWayExcelContent wifisize " + String.valueOf(gateWayExcelContent.getHardwareAblityWifiSize()));
            } else {
                //已经导入的网关暂时不做处理
            	flag = false;
				return flag;
            }
        }

    	if(flag){
    		//批量导入数据
    		gatewayInfoService.batchInsertGatewayInfo(allDatas);
            if(allHardwareAblityDatas.size() > 0){
                for(int j = 0 ; j < allHardwareAblityDatas.size(); j++){
                    logger.info(" wifiCount " + String.valueOf(allHardwareAblityDatas.get(j).getHardwareAblityWifiCount()));
                    logger.info(" wifi24ghz " + String.valueOf(allHardwareAblityDatas.get(j).getHardwareAblitySupportWifi24ghz()));
                    logger.info(" wifisize " + String.valueOf(allHardwareAblityDatas.get(j).getHardwareAblityWifiSize()));
                }
                hardwareAblityService.batchInsertHardwareAblity(allHardwareAblityDatas);
            }
    	}
    	
		return flag;
    }
    
    public Map<String, Object> queryFirmwareInfo(GateWayExcelContent gateWayExcelContent){
    	
    	Map<String, Object> params = new HashMap<String, Object>();
		params.put("deviceFactory", gateWayExcelContent.getGatewayInfoFactoryCode());
		params.put("firmwareVersion", gateWayExcelContent.getGatewayInfoVersion());
		params.put("deviceModel", gateWayExcelContent.getGatewayInfoModel());
		Map<String, Object> map  = firmwareInfoService.searchFirmwareId(params);
		if(map != null && Integer.parseInt(map.get("resultCode").toString()) == 0){
		     return (Map<String, Object>) map.get("data");
		}else{
			nonDeviceInfoList.add(params);
			return null;
		}
    }
    
    
    @Override
	public Map<String, Object> setHgAdminPwd(Map<String, Object> parameter) {
		
		
		//1.根据网关MAC查询网关ID
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = macMap.get(Constant.MAC) == null ? null :macMap.get(Constant.MAC).toString();
        
        parameter.remove(Constant.PARAMETER);
        parameter.remove(Constant.RPCMETHOD);
        if(gatewayMacAddress == null || "".equals(gatewayMacAddress)){
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求修改管理员密码失败，原因：网关MAC为空");
        	parameter.put(Constant.RESULT, -102);
        	Map<String, Object> failReason = new HashMap<String, Object>();
        	failReason.put(Constant.FAILREASON, "MAC地址为空");
        	parameter.put(Constant.RESULTDATA, failReason);
        	return parameter;
        }
        String newPwd = macMap.get("NEWPASSWORD").toString();
        String oldPwd = macMap.get("PASSWORD").toString();

        //2.下发指令并获取结果  暂时先不验证旧密码，直接修改密码
        /*List<String> namesList = new ArrayList<>();
        namesList.add(ConstantDiagnose.TELECOMACCOUNT_PASSWORD);
        Map<String, Object> resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, namesList);
        if(null == resultMap)
        {
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求修改管理员密码失败，原因：网关MAC地址不存在");
        	parameter.put(Constant.RESULT, -201);
        	parameter.put(Constant.FAILREASON, "网关MAC地址不存在");
            return parameter;
        }
        Object password = resultMap.get(ConstantDiagnose.TELECOMACCOUNT_PASSWORD);
        if(password != null && !oldPwd.equals(password)){
        	//password为空的时候不跳出， 防止还没有初始化密码而设置不了密码
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求修改管理员密码失败，原因：原始密码不匹配");
        	parameter.put(Constant.RESULT, -204);
        	//parameter.put(Constant.FAILREASON, "原始密码不匹配");
            return parameter;
        }*/
        
        GatewayInfo gatewayInfo  = new GatewayInfo();
        gatewayInfo.setGatewayMacaddress(gatewayMacAddress);
        GatewayInfo gw = gatewayInfoService.selectGatewayInfo(gatewayInfo);
        if(gw == null){
        	parameter.put(Constant.RESULT, -201);
        	Map<String, Object> failReason = new HashMap<String, Object>();
        	failReason.put(Constant.FAILREASON, "网关MAC地址不存在");
        	parameter.put(Constant.RESULTDATA, failReason);
        	return parameter;
        }
        if(!oldPwd.equals(gw.getGatewayFamilyPassword())){
        	parameter.put(Constant.RESULT, -102);
        	Map<String, Object> failReason = new HashMap<String, Object>();
        	failReason.put(Constant.FAILREASON, "旧密码不正确");
        	parameter.put(Constant.RESULTDATA, failReason);
        	return parameter;
        }
        List<ParameterValueStruct> list = new ArrayList<ParameterValueStruct>();
        ParameterValueStruct<String> pvs = new ParameterValueStruct<String>();
        pvs.setName(ConstantDiagnose.TELECOMACCOUNT_PASSWORD);
        pvs.setValue(newPwd);
        pvs.setReadWrite(true);
        pvs.setValueType("String");
        list.add(pvs);
        
        boolean b = instructionMethodService.setParameterValue(gatewayMacAddress, list);
        if(!b){
        	parameter.put(Constant.RESULT, -400);
        	Map<String, Object> failReason = new HashMap<String, Object>();
        	failReason.put(Constant.FAILREASON, "网关设置参数失败");
        	parameter.put(Constant.RESULTDATA, failReason);
        	return parameter;
        }
        gw.setGatewayFamilyPassword(newPwd);
        gatewayInfoService.updateSelectGatewayInfo(gw);
        // 3.拼装发挥结果
        Map<String, Object> resultData = new HashMap<>();
        parameter.put(Constant.RESULTDATA, resultData);
        parameter.put(Constant.RESULT, 0);
        logger.info("网关MAC地址："+gatewayMacAddress+"请求修改管理员密码成功，返回："+ JSON.toJSONString(parameter));
        return parameter;
	}
	@Override
	public Map<String, Object> queryAlarmInfo(Map<String, Object> params) {
		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
		String gatewayMacaddress = params.get("gatewayMacaddress") == null ? null : params.get("gatewayMacaddress").toString();
		if(gatewayMacaddress == null || "".equals(gatewayMacaddress)){
			returnMap.put("resultCode", 10000);
			returnMap.put("resultMsg", "网关MAC地址不能为空");
			return returnMap;
		}
		List<String> nameList = new ArrayList<String>();
		nameList.add(ConstantDiagnose.ALARM_NUMBER);
		
		Map<String, Object> map = instructionMethodService.getParameterValues(gatewayMacaddress, nameList);
		if(map == null || map.isEmpty()){
			returnMap.put("resultCode", 0);
			returnMap.put("resultMsg", "暂无告警信息");
			return returnMap;
		}
		Object alarmCodes = map.get(ConstantDiagnose.ALARM_NUMBER);
		if(alarmCodes != null && !"".equals(alarmCodes)){
			
			String[] codes = alarmCodes.toString().split(",");
			data = gatewayInfoMapper.queryGatewayAlarmInfo(codes);
		}else{
			returnMap.put("resultCode", 0);
			returnMap.put("resultMsg", "暂无告警信息");
			returnMap.put("data", data);
			return returnMap;
		}
		
		returnMap.put("resultCode", 0);
		returnMap.put("resultMsg", "");
		returnMap.put("data", data);
		return returnMap;
	}


	@Override
	public Map<String, Object> addObjectInfo(Map<String, Object> params) {
		Map<String, Object> returnMap = new HashMap<String, Object>();

        String gatewayUuid = params.get("id") == null ? null : params.get("id").toString();

        if(gatewayUuid == null || "".equals(gatewayUuid)){
            returnMap.put("resultCode", 10000);
            returnMap.put(Constant.MESSAGE, "gatewayUuid不能为空");
            return returnMap;
        }

        GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayUuid);
        String gatewayMacaddress = "";
        if(gatewayInfo!=null){
            gatewayMacaddress =gatewayInfo.getGatewayMacaddress();
        }
		String ObjectName = params.get("pathName") == null ? null : params.get("pathName").toString();
		if(gatewayMacaddress == null || "".equals(gatewayMacaddress)){
			returnMap.put("resultCode", 10000);
			returnMap.put(Constant.MESSAGE, "网关MAC地址不能为空");
			return returnMap;
		}
		if(ObjectName == null || "".equals(ObjectName)){
			returnMap.put("resultCode", 10000);
			returnMap.put(Constant.MESSAGE, "pathName不能为空");
			return returnMap;
		}else{
            ObjectName = ObjectName +".";
        }
		try{
            int resultData = instructionMethodService.AddObject(gatewayMacaddress, ObjectName, System.currentTimeMillis()+"");
            logger.info(" 新增节点指令返回值: " + resultData);
            if(resultData==-1){
                returnMap.put("resultCode", -1);
                returnMap.put(Constant.MESSAGE, "新增节点在下发指令时失败，网关返回 : " + resultData);
                return returnMap;
            }else{
            	OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.GATEWAY_MANAGER_SERVICE, "创建对象实例", JSON.toJSONString(params));
                returnMap.put("resultCode", 0);
                returnMap.put(Constant.MESSAGE, "成功请求添加节点");
                returnMap.put("data", resultData);
                return returnMap;
            }
        }catch (Exception e){
            e.printStackTrace();
            returnMap.put("resultCode", -1);
            returnMap.put(Constant.MESSAGE, "下发新增节点指令失败"+e.toString());
            return returnMap;
        }
	}

	@Override
	public Map<String, Object> deleteObjectInfo(Map<String, Object> params) {
		Map<String, Object> returnMap = new HashMap<String, Object>();
        String gatewayUuid = params.get("id") == null ? null : params.get("id").toString();
        if(gatewayUuid == null || "".equals(gatewayUuid)){
            returnMap.put("resultCode", 10000);
            returnMap.put(Constant.MESSAGE, "gatewayUuid不能为空");
            return returnMap;
        }
        GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayUuid);
        String gatewayMacaddress ="";
        if(gatewayInfo!=null){
            gatewayMacaddress =gatewayInfo.getGatewayMacaddress();
        }
		String objectName = params.get("pathName") == null ? null : params.get("pathName").toString();
		if(gatewayMacaddress == null || "".equals(gatewayMacaddress)){
			returnMap.put("resultCode", 10000);
			returnMap.put(Constant.MESSAGE, "网关MAC地址不能为空");
			return returnMap;
		}
		if(objectName == null || "".equals(objectName)){
			returnMap.put("resultCode", 10000);
			returnMap.put(Constant.MESSAGE, "pathName不能为空");
			return returnMap;
		}else{
            objectName = objectName +".";
        }
        try{
            int resultData = instructionMethodService.DeleteObject(gatewayMacaddress, objectName, System.currentTimeMillis()+"");
            logger.info(" 删除节点指令返回值: " + resultData);
            if(resultData==-1){
                returnMap.put("resultCode", -1);
                returnMap.put(Constant.MESSAGE, "删除节点在下发指令时失败，网关返回 : " + resultData);
                return returnMap;
            }else{
            	OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.GATEWAY_MANAGER_SERVICE, "删除对象实例", JSON.toJSONString(params));
                returnMap.put("resultCode", 0);
                returnMap.put(Constant.MESSAGE, "成功请求删除节点");
                returnMap.put("data", resultData);
                return returnMap;
            }
        }catch (Exception e){
            e.printStackTrace();
            returnMap.put("resultCode", -1);
            returnMap.put(Constant.MESSAGE, "下发删除节点指令失败"+e.toString());
            return returnMap;
        }
	}


	@Override
	public Map<String, Object> modFamilyAccountPwd(Map<String, Object> params) {

		Map<String, Object> returnMap = new HashMap<>();
        String gatewayUuid = params.get("id") == null ? null : params.get("id").toString();
        if(gatewayUuid == null || "".equals(gatewayUuid)){
            returnMap.put("resultCode", 10000);
            returnMap.put("resultMsg", "gatewayUuid不能为空");
            return returnMap;
        }
        GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayUuid);
        String gatewayMacaddress ="";
        if(gatewayInfo!=null){
            gatewayMacaddress =gatewayInfo.getGatewayMacaddress();
        }
		/*String username = params.get("username") == null ? null : params.get("username").toString();*/
        String password = params.get("password") == null ? null : params.get("password").toString();
        String customiseName = params.get("customiseName") == null ? null : params.get("customiseName").toString();

        if(gatewayMacaddress == null || "".equals(gatewayMacaddress)){
			returnMap.put("resultCode", 10000);
			returnMap.put("resultMsg", "网关MAC地址不能为空");
			return returnMap;
		}
		/*if(username == null || "".equals(username)){
			returnMap.put(Constant.MESSAGE, 10000);
			returnMap.put("resultMsg", "username不能为空");
			return returnMap;
		}*/
        List<ParameterValueStruct> paramList = new ArrayList<>();
        if(org.apache.commons.lang.StringUtils.isNotBlank(password)){
            String nodeStr;
            GatewayNodeExample node = new GatewayNodeExample();
            GatewayNodeExample.Criteria criteria = node.createCriteria();
            criteria.andFactoryCodeEqualTo(gatewayInfo.getGatewayFactoryCode());
            criteria.andHdVersionEqualTo(gatewayInfo.getGatewayHardwareVersion());
            criteria.andFirmwareVersionEqualTo(gatewayInfo.getGatewayVersion());
            List<GatewayNode> nodeList= gatewayNodeMapper.selectByExample(node);
            if (!nodeList.isEmpty()&&org.apache.commons.lang.StringUtils.isNotBlank(nodeList.get(0).getLoginPasswordNode())) {
                nodeStr =  nodeList.get(0).getLoginPasswordNode();
            }else {
                nodeStr = "InternetGatewayDevice.DeviceInfo.X_CMCC_TeleComAccount.Password";
            }
            logger.info(" nodeStr " + nodeStr);

            ParameterValueStruct<String> pwd = new ParameterValueStruct<>();
            pwd.setName(nodeStr);
            pwd.setValue(password);
            pwd.setReadWrite(true);
            pwd.setValueType("string");
            paramList.add(pwd);
        }

        if(org.apache.commons.lang.StringUtils.isNotBlank(customiseName)){

            ParameterValueStruct<String> customiseNameStruct = new ParameterValueStruct<>();
            customiseNameStruct.setName("InternetGatewayDevice.DeviceInfo.X_CMCC_CustomiseName");
            customiseNameStruct.setValue(customiseName);
            customiseNameStruct.setReadWrite(true);
            customiseNameStruct.setValueType("string");
            paramList.add(customiseNameStruct);
        }

	     /*ParameterValueStruct<String> enable = new ParameterValueStruct<String>();
	     enable.setName(node + ".Enable");
	     enable.setValue("TRUE");
	     enable.setReadWrite(true);
	     enable.setValueType("boolean");
	     ParameterValueStruct<String> uName = new ParameterValueStruct<String>();
         uName.setName(node + ".Username");
         uName.setValue(username);
         uName.setReadWrite(true);
         uName.setValueType("string");*/

        if(paramList.size() > 0)
        {
            try {
                if(instructionMethodService.setParameterValue(gatewayMacaddress, paramList)){
                    GatewayInfo updatebean = new GatewayInfo();
                    updatebean.setGatewayUuid(gatewayUuid);//update 主键
                   /*updatebean.setGatewayFamilyAccount(username);*/
                    if(org.apache.commons.lang.StringUtils.isNotBlank(password))
                    {
                        updatebean.setGatewayFamilyPassword(password);
                    }
                    if(org.apache.commons.lang.StringUtils.isNotBlank(customiseName))
                    {
                        updatebean.setGatewayName(customiseName);
                    }
                    updatebean.setGatewayAreaId((String) params.get("areaCode"));
                    updatebean.setGatewayModel((String)params.get("deviceModel"));
                    gatewayInfoService.updateSelectGatewayInfo(updatebean);
                    returnMap.put("resultCode", 0);
                    returnMap.put("resultMsg", "修改成功");
                    OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.GATEWAY_MANAGER_SERVICE, "修改设备维护账号和密码", JSON.toJSONString(updatebean));
                    return returnMap;
                }else{
                    returnMap.put("resultCode", -1);
                    returnMap.put("resultMsg", "下发修改指令失败");
                    return returnMap;
                }
            }catch (Exception e){
                e.printStackTrace();
                returnMap.put("resultCode", -1);
                returnMap.put("resultMsg", "下发修改指令失败");
                return returnMap;

            }
        }
        else
        {
            try {
                GatewayInfo updatebean = new GatewayInfo();
                updatebean.setGatewayUuid(gatewayUuid);//update 主键
                updatebean.setGatewayAreaId((String) params.get("areaCode"));
                updatebean.setGatewayModel((String)params.get("deviceModel"));
                gatewayInfoService.updateSelectGatewayInfo(updatebean);
                returnMap.put("resultCode", 0);
                returnMap.put("resultMsg", "修改成功");
                OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.GATEWAY_MANAGER_SERVICE, "修改设备维护账号和密码", JSON.toJSONString(updatebean));
                return returnMap;
            }
            catch (Exception e)
            {
                returnMap.put("resultCode", -1);
                returnMap.put("resultMsg", "修改归属区域和设备型号失败");
                return returnMap;
            }
        }
	}

    @Override
    public Map<String, Object> queryGatewayBaseInfo(Map<String, Object> params) {
        String gateWayUuid = params.get("id") == null ? null : params.get("id").toString();
        if(gateWayUuid == null || "".equals(gateWayUuid)){
            params.clear();
            params.put("resultCode", 10000);
            params.put(Constant.MESSAGE, "gatewayUuid不能为空");
            return params;
        }
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewayUuid(gateWayUuid);
        List<GatewayInfo> dataList = gatewayInfoMapper.queryListPage(gatewayInfo);
        params.clear();
        if(dataList!=null&&dataList.size()>0){
            String areaId = dataList.get(0).getGatewayAreaId();
            String areaName ="";
            try {
                com.cmiot.ams.domain.Area area = areaService.findAreaById(Integer.parseInt(areaId));
                areaName = area.getName();
            }catch (Exception e){
            }
            String account = dataList.get(0).getGatewayFamilyAccount();
            String password = dataList.get(0).getGatewayFamilyPassword();
            String gatewayUuid = dataList.get(0).getGatewayUuid();
            String gatewayMacaddress = dataList.get(0).getGatewayMacaddress();
            String deviceModel = dataList.get(0).getGatewayModel();

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("gatewayUuid",gatewayUuid);
            dataMap.put("areaId",areaId);
            dataMap.put("areaName",areaName);
            dataMap.put("account",account);
            dataMap.put("password",password);
            dataMap.put("gatewayMacaddress",gatewayMacaddress);
            dataMap.put("deviceModel",deviceModel);
            dataMap.put("customiseName",dataList.get(0).getGatewayName());

            if(dataList.get(0).getGatewayFactoryCode().toString()==null||"".equals(dataList.get(0).getGatewayFactoryCode().toString())){
                logger.info(" gatewayFactoryCode is null");
                dataMap.put("deviceInfoList",null);
            }else {
                logger.info(" oui " + dataList.get(0).getGatewayFactoryCode().toString());
                DeviceInfo deviceInfoToSearch = new DeviceInfo();
                deviceInfoToSearch.setDeviceFactory(dataList.get(0).getGatewayFactoryCode().toString());
                List<DeviceInfo> deviceInfoList = deviceInfoMapper.queryList(deviceInfoToSearch);
                dataMap.put("deviceInfoList",deviceInfoList);
            }
            params.put("resultCode", 0);
            params.put(Constant.MESSAGE, "返回网关信息成功");
            params.put(Constant.DATA, dataMap);
            return params;
        }else{
            params.put("resultCode", -1);
            params.put(Constant.MESSAGE, "无网关信息返回");
            return params;
        }
    }


	@Override
	public Map<String, Object> queryGatewayState(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<>();
		try {
			String gatewayId = (String) params.get("gatewayId");
			if(StringUtils.isEmpty(gatewayId)){
				resultMap.put(Constant.CODE, RespCodeEnum.RC_1002.code());
			    resultMap.put(Constant.MESSAGE, "参数gatewayId为空");
			    resultMap.put(Constant.DATA, null);
			    return resultMap;
			}
			GatewayInfo gatewayInfo = new GatewayInfo();
			gatewayInfo.setGatewayUuid(gatewayId);
			gatewayInfo = gatewayInfoMapper.selectGatewayInfo(gatewayInfo);
			if(gatewayInfo == null){
				resultMap.put(Constant.CODE, RespCodeEnum.RC_1004.code());
			    resultMap.put(Constant.MESSAGE, "该网关不存在");
			    resultMap.put(Constant.DATA, null);
			    return resultMap;
			}
			String status = redisClientTemplate.get("R-F-" + gatewayInfo.getGatewaySerialnumber());//查询重启或恢复出厂状态
			if (StringUtils.isEmpty(status)) status = "";
			Map<String,Object> data = new HashMap<>();
			data.put("gatewayId", gatewayId);
			data.put("status", status);
			gatewayInfo.setGatewayStatus(status);
			resultMap.put(Constant.DATA, JSON.toJSON(data));
			resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			resultMap.put(Constant.MESSAGE, RespCodeEnum.RC_0.description());
		} catch (Exception e) {
			logger.error("网关状态查询失败:", e);
			resultMap.put(Constant.DATA, null);
			resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			resultMap.put(Constant.MESSAGE, "网关状态查询失败");
		}
		return resultMap;
	}

    @Override
    public Map<String, Object> queryObjectInfo(Map<String, Object> params) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        String gatewayUuid = params.get("id") == null ? null : params.get("id").toString();
        if(gatewayUuid == null || "".equals(gatewayUuid)){
            returnMap.put("resultCode", 10000);
            returnMap.put(Constant.MESSAGE, "gatewayUuid不能为空");
            return returnMap;
        }
        GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayUuid);
        String gatewayMacaddress ="";
        if(gatewayInfo!=null){
            gatewayMacaddress =gatewayInfo.getGatewayMacaddress();
        }
        String objectName = params.get("pathName") == null ? null : params.get("pathName").toString();
        if(gatewayMacaddress == null || "".equals(gatewayMacaddress)){
            returnMap.put("resultCode", 10000);
            returnMap.put(Constant.MESSAGE, "网关MAC地址不能为空");
            return returnMap;
        }
        if(objectName == null || "".equals(objectName)){
            returnMap.put("resultCode", 10000);
            returnMap.put(Constant.MESSAGE, "pathName不能为空");
            return returnMap;
        }

        List<String> attrList = new ArrayList<>();
        attrList.add(0,objectName);
        try{
            Map<String, Object> resultData = instructionMethodService.getParameterAttributes(gatewayMacaddress, attrList);
            logger.info(" 查询节点属性指令返回值: " + resultData);
            if(resultData == null || resultData.isEmpty()){
                returnMap.put("resultCode", -1);
                returnMap.put(Constant.MESSAGE, "查询节点属性指令失败，网关返回 : " + resultData);
                return returnMap;
            }else{
                Map<String,Object> dataMap = new HashMap<>();
                //处理InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.X_CMCC_Stats.BytesSen[d|t]，请求节点和返回节点不同的情况
                String regBytesSend = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.X_CMCC_Stats.BytesSen[d|t]";
                Pattern pattern = Pattern.compile(regBytesSend);
                if(pattern.matcher(objectName).matches()){
                	for(Map.Entry<String, Object> entry : resultData.entrySet()){
                		if(pattern.matcher(entry.getKey()).matches() && !entry.getKey().equals(objectName)){
                			objectName = entry.getKey();
                			break;
                		}
                	}
                }
                Map<String,Object> attribute = (Map<String, Object>) resultData.get(objectName);
                dataMap.put("Name",params.get("pathName"));
                dataMap.put("Notification", Integer.parseInt(String.valueOf(attribute.get("Notification"))));
                String[] array = (String[]) attribute.get("AccessList");
                List<String> accessList = new ArrayList<>();
                for (String a : array) {
                    accessList.add(a);
                }
                dataMap.put("AccessList", accessList);

                returnMap.put("resultCode", 0);
                returnMap.put(Constant.MESSAGE, "成功查询节点属性指令");
                returnMap.put("data", dataMap);
                return returnMap;
            }
        }catch (Exception e){
            e.printStackTrace();
            returnMap.put("resultCode", -1);
            returnMap.put(Constant.MESSAGE, "查询节点属性指令失败"+e.toString());
            return returnMap;
        }
    }

    @Override
    public Map<String, Object> modObjectInfo(Map<String, Object> params) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        String gatewayUuid = params.get("id") == null ? null : params.get("id").toString();
        if(gatewayUuid == null || "".equals(gatewayUuid)){
            returnMap.put("resultCode", 10000);
            returnMap.put(Constant.MESSAGE, "gatewayUuid不能为空");
            return returnMap;
        }

        GatewayInfo gatewayInfo = gatewayInfoService.selectByUuid(gatewayUuid);
        String gatewayMacaddress ="";
        if(gatewayInfo!=null){
            gatewayMacaddress =gatewayInfo.getGatewayMacaddress();
        }

        List<Map<String,Object>> detailList = (List<Map<String,Object>>) params.get("listS");
        if(detailList == null || detailList.size() == 0){
            returnMap.put("resultCode", 10000);
            returnMap.put(Constant.MESSAGE, "listS不能为空");
            return returnMap;
        }
        List<SetParameterAttributesStruct> setParamAttrList = new ArrayList<>();
        for(Map<String,Object> nodeValueMap : detailList){
            SetParameterAttributesStruct setParam = new SetParameterAttributesStruct();
            List<String> accessListP = (List<String>) nodeValueMap.get("AccessList");
            logger.info(" accessListP is " + accessListP);
            if(accessListP == null && (nodeValueMap.get("Notification").equals("")||nodeValueMap.get("Notification")==null)){
                returnMap.put("resultCode", -1);
                returnMap.put(Constant.MESSAGE, "修改节点属性指令失败 : Notification 或 AccessList 都为空值!" );
                return returnMap;
            }
            if(accessListP != null){
                setParam.setAccesslist(accessListP);
                setParam.setAccessListChange(true);
            }
            logger.info(" accessListP " +accessListP.size());
            setParam.setName(String.valueOf(nodeValueMap.get("Name")));
            setParam.setNotification(Integer.parseInt(String.valueOf(nodeValueMap.get("Notification"))));
            setParam.setNotificationChange(true);
            setParamAttrList.add(setParam);
        }
        if(setParamAttrList.size()>0){
            for (int l = 0; l < setParamAttrList.size(); l++){
                logger.info(" Name " +setParamAttrList.get(l).getName());
                logger.info(" Notification " +setParamAttrList.get(l).getNotification());
                logger.info(" AccessList " +setParamAttrList.get(l).getAccessList());
            }
        }else{
            logger.info(" setParamAttrList is null! ");
        }

        try{
            Boolean opeResult = instructionMethodService.SetParameterAttributes(gatewayMacaddress, setParamAttrList);
            logger.info(" 修改节点属性指令返回值: " + opeResult);
            if(!opeResult){
                returnMap.put("resultCode", -1);
                returnMap.put(Constant.MESSAGE, "修改节点属性指令失败，网关返回 : " + opeResult);
                return returnMap;
            }else{
            	OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.GATEWAY_MANAGER_SERVICE, "修改对象节点相关属性", JSON.toJSONString(params));
                returnMap.put("resultCode", 0);
                returnMap.put(Constant.MESSAGE, "修改节点属性属性指令");
                returnMap.put("data", opeResult);
                return returnMap;
            }
        }catch (Exception e){
            e.printStackTrace();
            returnMap.put("resultCode", -1);
            returnMap.put(Constant.MESSAGE, "查询节点属性指令失败"+e.toString());
            return returnMap;
        }
    }



    static Map<Integer, Double> map = new HashMap<Integer, Double>();
	public static void main(String[] args) {
		long current = System.currentTimeMillis();
		  for (int i = 0; i < 10000; i++) {
	            System.out.println("Fibonacci(" + i + ") is " + fib(i));
	        }
		 System.out.println(System.currentTimeMillis() - current +"ms"); 
	}
	static double fib(int n){
		double val = 0;
			if (n <= 1) {
				map.put(n, val);
	             val = n;
	        } else {
	        	 val = map.get(n-2)+map.get(n-1);
	        }
			map.put(n, val);
			return val;
	}


	@Override
	public Map<String, Object> getComplexData(Map<String, Object> params) {
		Map<String, Object> data = new HashMap<String, Object>();
        String gatewayId = params.get("gatewayId") == null ? null :params.get("gatewayId").toString();
        Map<String, Object> retMap = new HashMap<String, Object>();
        if(gatewayId == null){
        	logger.info("请求获取WAN口状态查询，上下行连接速率， 时输入gatewayId为空");
        	retMap.put(Constant.CODE, 10000);
        	retMap.put(Constant.MESSAGE, "gatewayId为空");
            return retMap;
        }
        List<String> namesList = new ArrayList<>();
        Map<String, Object> resultMap = null;
        //数据库查询WAN口节点前缀
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewayUuid(gatewayId);
        gatewayInfo = gatewayInfoMapper.selectGatewayInfo(gatewayInfo);
        if(gatewayInfo == null){
        	logger.info("请求获取WAN口状态，上下行连接速率失败，该网关gatewayId不存在");
        	retMap.put(Constant.CODE, 10000);
        	retMap.put(Constant.MESSAGE, "网关gatewayId不存在");
            return retMap;
        }
        String gatewayMacAddress = gatewayInfo.getGatewayMacaddress();
        String externalipAddress = gatewayInfo.getGatewayExternalIPaddress();
        if(!StringUtils.isEmpty(externalipAddress)){
        	namesList.add(externalipAddress + ".ConnectionStatus");
        }else{
	        resultMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.WANDevice.", false);
	        if(resultMap == null ){
	        	logger.info("网关MAC地址："+gatewayMacAddress+"请求获取WAN口状态查询，上下行连接速率，丢包率，网关PPPOE状态失败，原因：没有查询有相关数据");
	        	retMap.put(Constant.CODE, 10005);
	        	retMap.put(Constant.MESSAGE, "获取节点参数错误");
	            return retMap;
	        }
	        //获取WAN口状态
	        String regExService= "InternetGatewayDevice.WANDevice.[0-9]+.WANConnectionDevice.[0-9]+.WANIPConnection.[0-9]+.ConnectionStatus";
	        String _regExService = "InternetGatewayDevice.WANDevice.[0-9]+.WANConnectionDevice.[0-9]+.WANPPPConnection.[0-9]+.ConnectionStatus";
	        Pattern patternService = Pattern.compile(regExService);
	        Pattern _patternService = Pattern.compile(_regExService);
	        Set<String> keys = resultMap.keySet();
	        
	        for(String key : keys){
	        	if(_patternService.matcher(key).matches()){
	        		namesList.add(key);
	        	}
	        	if(patternService.matcher(key).matches()){
		        	namesList.add(key);
	        	}
	        }
        }
        //返回值  Up  NoLink  Error  Disabled
        String WANState = "";
        //查状态用
        String url = "";
        
        String lineStatus = "";
        if(namesList.size()>0){
        	resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, namesList);
        	for(String name : namesList){
        		Object enable = resultMap.get(name);
        		if(enable != null && !"".equals(enable)){
        			WANState = enable.toString();
        			url = name;
        			break;
        		}
        	}
        }else{
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求获取WAN口状态查询失败，原因：没有找到对应的信息");
        	WANState = "";
        }
        data.put("WANState", WANState);
        logger.info("网关MAC地址："+gatewayMacAddress+"请求获取WAN口状态："+WANState);
        namesList.clear();
        //获取上下行速率 InternetGatewayDevice.LANDevice.{i}.LANEthernetInterfaceConfig.{i}.MaxBitRate
       /* resultMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);
        String regExService= "InternetGatewayDevice.LANDevice.[0-9]+.LANEthernetInterfaceConfig.[0-9]+.MaxBitRate";
        Pattern patternService = Pattern.compile(regExService);
        Set<String> keys = resultMap.keySet();
        namesList = new ArrayList<>();
        for(String key : keys){
        	
        	Matcher matchService = patternService.matcher(key);
        	if(matchService.matches()){
	        	namesList.add(key);
        	}
        }
        //上下行速率
        String maxBitRate = "";
        //查状态用
        if(namesList.size()>0){
        	resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, namesList);
        	for(String name : namesList){
        		Object enable = resultMap.get(name);
        		if(enable != null && !"".equals(enable)){
        			maxBitRate = enable.toString();
        			break;
        		}
        	}
        }
        data.put("ConnectionRate", maxBitRate);
        logger.info("网关MAC地址："+gatewayMacAddress+"请求获取上下行速率："+maxBitRate);
        */
        //TODO 丢包率 暂不支持
        //TODO PPPoE状态  分配到另一个接口
      
        
        retMap.put(Constant.DATA, data);
        retMap.put(Constant.CODE, 0);
        retMap.put(Constant.MESSAGE, "success");
		return retMap;
	}


	@Override
	public Map<String, Object> getPppoeAndAccount(Map<String, Object> params) {
		Map<String, Object> data = new HashMap<String, Object>();
		String gatewayId = params.get("gatewayId") == null ? null :params.get("gatewayId").toString();
        Map<String, Object> retMap = new HashMap<String, Object>();
        if(gatewayId == null){
        	logger.info("请求查询pppoe状态、上网账号失败， 输入gatewayId为空");
        	retMap.put(Constant.CODE, 10000);
        	retMap.put(Constant.MESSAGE, "gatewayId为空");
            return retMap;
        }
        List<String> namesList = new ArrayList<>();
        Map<String, Object> resultMap = null;
        //数据库查询WAN口节点前缀
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewayUuid(gatewayId);
        gatewayInfo = gatewayInfoMapper.selectGatewayInfo(gatewayInfo);
        if(gatewayInfo == null){
        	logger.info("请求查询pppoe状态、上网账号失败，该网关gatewayId不存在");
        	retMap.put(Constant.CODE, 10005);
        	retMap.put(Constant.MESSAGE, "网关gatewayId不存在");
            return retMap;
        }
        String gatewayMacAddress = gatewayInfo.getGatewayMacaddress();
    
        resultMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.WANDevice.", false);
        if(resultMap == null || resultMap.isEmpty()){
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求查询pppoe状态、上网账号失败，原因：没有查询有相关数据");
        	retMap.put(Constant.CODE, 10005);
        	retMap.put(Constant.MESSAGE, "获取节点参数错误");
            return retMap;
        }
        //获取WAN口状态 
        String regExService= "InternetGatewayDevice.WANDevice.[0-9]+.WANConnectionDevice.[0-9]+.WANPPPConnection.[0-9]+.Username";
        String _regExService = "InternetGatewayDevice.WANDevice.[0-9]+.WANConnectionDevice.[0-9]+.WANPPPConnection.[0-9]+.ConnectionStatus";
        Pattern patternService = Pattern.compile(regExService);
        Pattern _patternService = Pattern.compile(_regExService);
        Set<String> keys = resultMap.keySet();
        
        for(String key : keys){
        	if(_patternService.matcher(key).matches()){
        		namesList.add(key);
        	}
        	if(patternService.matcher(key).matches()){
	        	namesList.add(key);
        	}
        }
        
        String WANState = "",pppoeUserName = "";       
        if(namesList.size()>0){
        	resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, namesList);
        	for(Map.Entry<String, Object> entry : resultMap.entrySet()){
        		String name = entry.getKey();
        		if(name.endsWith("ConnectionStatus")){
	        		Object enable = resultMap.get(name);
	        		Object username = resultMap.get(name.substring(0, name.indexOf("ConnectionStatus")) + "Username");
	        		pppoeUserName = username == null ? "" : username.toString();
	        		if(enable != null && !"".equals(enable)){
	        			WANState = enable.toString();
	        			break;
	        		}
        		}
        	}
        }else{
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求查询pppoe状态和账号失败，原因：没有找到对应的信息");
        	retMap.put(Constant.CODE, 10005);
        	retMap.put(Constant.MESSAGE, "查询pppoe状态和账号失败,没有找到对应的信息");
        	return retMap;
        }
        data.put("ConnectionStatus", WANState);
        data.put("Username", pppoeUserName);
        logger.info("网关MAC地址："+gatewayMacAddress+"请求查询pppoe状态为："+WANState + "账号为：" + pppoeUserName);
        
        retMap.put(Constant.DATA, data);
        retMap.put(Constant.CODE, 0);
        retMap.put(Constant.MESSAGE, "success");
		return retMap;
	}

    //TODO 网关批量导入
    @Override
    public Map<String, Object> ImportGatewayInfo(Map<String, Object> parameter)
    {
        String str = (String)parameter.get("filePath");
        logger.info("Excel file Remote path " + str);
        try
        {
            URL website = new URL(str);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            String [] excelSaveFolderPath = excelSaveFolder.split("/");
            StringBuffer pathSb = new StringBuffer();
            if(excelSaveFolderPath.length>0){
                for(int l = 0; l < excelSaveFolderPath.length; l++){
                    if(l == 0){
                        pathSb.append(File.separator + excelSaveFolderPath[l] + File.separator);
                    }else{
                        pathSb.append( excelSaveFolderPath[l] + File.separator);
                    }
                }
            }else{
                pathSb.append(File.separator + excelSaveFolder + File.separator);
            }
            String filelocalPath =  pathSb + "gateway" + DateTools.getCurrentTimeMillis() + ".xls";
            createFolder(pathSb.toString());
            logger.info(" filelocalPath " + filelocalPath );
            FileOutputStream fos = new FileOutputStream(filelocalPath);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            rbc.close();
            fos.close();
            FileInputStream fis = new FileInputStream(filelocalPath);
            ExcelUtil util = new ExcelUtil(GateWayInfoExcelContent.class);
            logger.info(util.toString());
            List listBooks = util.importExcel("网关导入信息", fis);
            fis.close();

            try {
                if (listBooks != null) {
                    if (listBooks == null || listBooks.size() < 1) {
                        String msg = "上传失败 请确认上传文件类型和模板一致!";
                        logger.info(msg);
                        return reutrnMap(RespCodeEnum.RC_1.code(), msg, false);
                    } else {
                        logger.info(" begin check Data " + DateTools.getCurrentTimeMillis());

                        long startTime = System.currentTimeMillis();
                        String errorMsg = check(listBooks);
                        logger.info("check Data cost : " +  (System.currentTimeMillis() - startTime));


                        if(org.apache.commons.lang.StringUtils.isNotBlank(errorMsg))
                        {
                            logger.info("import failed:{}", errorMsg);
                            return reutrnMap(RespCodeEnum.RC_ERROR.code(), errorMsg, false);
                        }

                        long startTimeInsert = System.currentTimeMillis();
                        //所有验证通过
                        boolean flag = insertGatewayInfo(listBooks);
                        logger.info(" insert Data cost : " +  (System.currentTimeMillis() - startTimeInsert));
                        logger.info(" import Data cost : " +  (System.currentTimeMillis() - startTime));

                        if (!flag) {
                            //固件不存在
                            logger.info(" insert data failed.");
                            return reutrnMap(RespCodeEnum.RC_1.code(), "导入网关失败", false);
                        }
                        try {
                            if("ON".equals(lock)) { //同步开关打开时才上传文件
                                            if ("ON".equals(lock)) { //同步开关打开时才上传文件
                                //生成excel文件后上传到文件服务器
                                //生成excel文件后上传到文件服务器
                                String filePath = createExcel(listBooks);
                                //导入网关信息同步到杭研
                                synPlatform(filePath);
                                logger.info(" synchronization to platform ok.");
                                            } else {
                                logger.info(" synchronization switch is closed!");
                            }
                           }
                        } catch (FileNotFoundException e) {
                            logger.info(" synchronization to platform failed.");
                            logger.info(exceptionInfo(e));
                        } catch (Exception e) {
                            logger.info(" synchronization to platform failed.");
                            logger.info(exceptionInfo(e));
                        }
                        //日志调用方法
                                        recordOperationLog("导入网关信息,导入成功 " + listBooks.size() + " 条网关记录!", String.valueOf(parameter.get("userName")), String.valueOf(parameter.get("roleName")));
                        logger.info(listBooks.size() + " records import successed!");
                        return reutrnMap(RespCodeEnum.RC_0.code(), "导入网关成功", false);
                    }
                } else {
                    String msg = "上传失败,文件上传为空";
                    return reutrnMap(RespCodeEnum.RC_1.code(), msg, false);
                }
            }catch (Exception e){
                logger.info(exceptionInfo(e));
                logger.info(" 上传失败错误信息 " +e.toString());
                return reutrnMap(RespCodeEnum.RC_1.code(), "上传失败,系统内部错误", false);
            }
        }catch (Exception e){
            String msg = "上传失败,系统异常错误!" + e.toString();
            logger.info(msg);
            logger.info(exceptionInfo(e));
            return reutrnMap(RespCodeEnum.RC_ERROR.code(), msg, false);
        }
    }


    private String check(List<GateWayInfoExcelContent> listBooks)
    {
        int i= 0;
        ValidateResult result;
        for(GateWayInfoExcelContent content : listBooks)
        {
            result = validatorManagement.validate(++i, content);
            if(!result.isValid())
            {
                return result.getMessage();
            }
        }
        return "";
    }

    private void synPlatform(String url)
    {
        logger.info("synchronized to the first level platform...");
        Map reportMap = new HashMap();
        reportMap.put("RPCMethod", "Report");
        reportMap.put("ID", Integer.valueOf((int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));
        reportMap.put("CmdType", "REPORT_HG_BASIC_INFO");
        reportMap.put("SequenceId", InstructionUtil.generate8HexString());
        Map reqmap = new HashMap();
        reqmap.put("InfoFilePath", url);
        reportMap.put("Parameter", reqmap);
        this.syncInfoToFirstLevelPlatformService.report("reportHgBasicInfo", reportMap);
        logger.info("synchronized to the first level platform ok.");
    }

    private boolean insertGatewayInfo(List<GateWayInfoExcelContent> listBooks)
    {
        List<GatewayInfo> allDatas = new ArrayList();
        List<GatewayQueue> allGatewayQueueDatas = new ArrayList();
        boolean flag = true;
        logger.info(" create DataList start : " +  DateTools.getCurrentTimeMillis());

        List<Factory> factoryList = factoryMapper.queryFactoryCodeOui();

        for (int k = 0; k < listBooks.size(); k++) {
           GateWayInfoExcelContent gateWayExcelContent = (GateWayInfoExcelContent)listBooks.get(k);

               GatewayInfo gatewayInfo = new GatewayInfo();
                GatewayQueue gatewayQueue = new GatewayQueue();
                String gateWayuuid = UniqueUtil.uuid();
                gatewayInfo.setGatewayUuid(gateWayuuid);


                if(org.apache.commons.lang.StringUtils.isNotBlank(gateWayExcelContent.getDeviceInfoUuid())){
                    gatewayInfo.setGatewayDeviceUuid(gateWayExcelContent.getDeviceInfoUuid());
                }else{
                    logger.info(" line " + (k+1) + " data error!");
                    logger.info("deviceInfo is null! factoryCode " + gateWayExcelContent.getGatewayInfoFactoryCode() + " gateWayModel " + gateWayExcelContent.getGatewayInfoModel());
                    flag = false;
                    return flag;
                }

                gatewayInfo.setGatewayFirmwareUuid(gateWayExcelContent.getGateWayfrimwareUUID());
                if("".equals(gateWayExcelContent.getGatewayInfoType())||gateWayExcelContent.getGatewayInfoType()==null){
                    gatewayInfo.setGatewayType("");
                }else{
                    gatewayInfo.setGatewayType(gateWayExcelContent.getGatewayInfoType());
                }
                gatewayInfo.setGatewayModel(gateWayExcelContent.getGatewayInfoModel());
                gatewayInfo.setGatewayName("");
                if (!StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoVersion())) {
                    gatewayInfo.setGatewayVersion(gateWayExcelContent.getGatewayInfoVersion());
                }else{
                    gatewayInfo.setGatewayVersion("");
                }
                gatewayInfo.setGatewaySerialnumber(gateWayExcelContent.getGatewayInfoSerialnumber());
                gatewayInfo.setGatewayFactoryCode(gateWayExcelContent.getGatewayInfoFactoryCode());

                for (Factory factory : factoryList)
                {
                    if(factory.getOui().equals(gateWayExcelContent.getGatewayInfoFactoryCode()))
                    {
                        gatewayInfo.setNewFactoryCode(factory.getManufacturerId());
                        break;
                    }
                }

                gatewayInfo.setGatewayFactory("");
                gatewayInfo.setGatewayMemo("");
                if (!StringUtils.isEmpty(gateWayExcelContent.getGatewayInfoHardwareVersion())) {
                    gatewayInfo.setGatewayHardwareVersion(gateWayExcelContent.getGatewayInfoHardwareVersion());
                }else{
                    gatewayInfo.setGatewayHardwareVersion("");
                }
                gatewayInfo.setGatewayJoinTime(null);
                gatewayInfo.setGatewayLastConnTime(null);
                gatewayInfo.setGatewayAdslAccount("");
                gatewayInfo.setGatewayIpaddress(gateWayExcelContent.getGatewayInfoIpaddress());
                gatewayInfo.setGatewayMacaddress(gateWayExcelContent.getGatewayInfoMacaddress());
                gatewayInfo.setGatewayStatus("");
                gatewayInfo.setGatewayDigestAccount("");
                gatewayInfo.setGatewayDigestPassword("");
                gatewayInfo.setGatewayConnectionrequesturl("");
                gatewayInfo.setSsidInitLastnumber(gateWayExcelContent.getSsid());
                gatewayInfo.setSsidInitPwd(gateWayExcelContent.getSsidpwd());
                gatewayInfo.setUadminInitPwd(gateWayExcelContent.getUpwd());

                if (!StringUtils.isEmpty(gateWayExcelContent.getGatewayPassword())) {
                    gatewayInfo.setGatewayPassword(gateWayExcelContent.getGatewayPassword());
                }
                gatewayInfo.setOsgi(gateWayExcelContent.getOsgi());
                gatewayInfo.setJvm(gateWayExcelContent.getJvm());



                if(gateWayExcelContent.getIsIntelligentGateway().equals("Y")){
                    gatewayInfo.setGatewayType("iHGU");
                    gatewayQueue.setGatewayQueueId(UniqueUtil.uuid());
                    gatewayQueue.setGatewayUuid(gateWayuuid);
                    gatewayQueue.setSynStatu(0);
                    allGatewayQueueDatas.add(gatewayQueue);
                }
                else
                {
                    gatewayInfo.setGatewayType("HGU");
                }

                allDatas.add(gatewayInfo);

        }
        logger.info(" create DataList finish : " +  DateTools.getCurrentTimeMillis());
        if (flag)
        {
            try{
                logger.info("began insert data ... " +  allDatas.size());
                logger.info(" insert Data start : " +  DateTools.getCurrentTimeMillis());
                gatewayInfoService.addbatchGatewayHardwareQueue(allDatas,null,allGatewayQueueDatas);
                logger.info(" insert Data finish : " +  DateTools.getCurrentTimeMillis());
            }catch (Exception e){
                logger.info(exceptionInfo(e));
                flag = false;
                return flag;
            }
        }
        return flag;
    }

    public Map<String, Object> reutrnMap(String resultCode, String resultMsg, boolean data)
    {
        Map returnMap = new HashMap();
        returnMap.put("resultCode", resultCode);
        returnMap.put("resultMsg", resultMsg);
        returnMap.put("data", Boolean.valueOf(data));
        return returnMap;
    }

    public void recordOperationLog(String content, String userName, String roleName) {
        logger.info("excel import logs recording...");
        Map parameterLog = new HashMap();
        parameterLog.put("content", content);
        parameterLog.put("userName", userName);
        parameterLog.put("categoryMenu", CategoryEnum.GATEWAY_MANAGER_SERVICE.name());
        parameterLog.put("operation", "导入网关信息");
        parameterLog.put("roleName", roleName);
        parameterLog.put("categoryMenuName", CategoryEnum.GATEWAY_MANAGER_SERVICE.description());
        parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
        this.logManagerService.recordOperationLog(parameterLog);
        logger.info("excel import logs recording ok.");
    }

    public String createExcel(List<GateWayInfoExcelContent> listBooks) throws IOException
    {


        String fileName = "";
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFFont font = wb.createFont();
        font.setFontHeightInPoints((short)14);
        font.setFontName("宋体");
        font.setColor((short)8);
        font.setBoldweight((short)700);
        HSSFCellStyle style = wb.createCellStyle();
        style.setAlignment((short)2);
        style.setFillForegroundColor((short)41);
        style.setFillPattern((short)1);
        style.setBorderBottom((short)5);
        style.setFont(font);

        HSSFSheet sheet = wb.createSheet();

        HSSFRow rowHeader = sheet.createRow(0);

        HSSFCell cellMAC = rowHeader.createCell(0);
        cellMAC.setCellStyle(style);
        cellMAC.setCellValue("网关mac地址");

        HSSFCell cellSN = rowHeader.createCell(1);
        cellSN.setCellStyle(style);
        cellSN.setCellValue("网关产品序号");


        HSSFCell cellSSID = rowHeader.createCell(2);
        cellSSID.setCellStyle(style);
        cellSSID.setCellValue("初始SSID名称");

        HSSFCell cellSSIDPWD = rowHeader.createCell(3);
        cellSSIDPWD.setCellStyle(style);
        cellSSIDPWD.setCellValue("网关初始SSID密码");

        HSSFCell cellUserAdminPwd = rowHeader.createCell(4);
        cellUserAdminPwd.setCellStyle(style);
        cellUserAdminPwd.setCellValue("网关初始UserAdmin密码");

        HSSFCell cellModel = rowHeader.createCell(5);
        cellModel.setCellStyle(style);
        cellModel.setCellValue("网关产品型号");

        HSSFCell cellFactoryCode = rowHeader.createCell(6);
        cellFactoryCode.setCellStyle(style);
        cellFactoryCode.setCellValue("生产厂商名称或代码");

        HSSFCell cellHardWare = rowHeader.createCell(7);
        cellHardWare.setCellStyle(style);
        cellHardWare.setCellValue("网关硬件版本");

        HSSFCell cellVersion = rowHeader.createCell(8);
        cellVersion.setCellStyle(style);
        cellVersion.setCellValue("网关固件");

        HSSFCell cellOperSystemType = rowHeader.createCell(9);
        cellOperSystemType.setCellStyle(style);
        cellOperSystemType.setCellValue("网关操作系统类型");

        HSSFCell cellOperSystemVersion = rowHeader.createCell(10);
        cellOperSystemVersion.setCellStyle(style);
        cellOperSystemVersion.setCellValue("操作系统版本");

        HSSFCell cellOperDateOfManual = rowHeader.createCell(11);
        cellOperDateOfManual.setCellStyle(style);
        cellOperDateOfManual.setCellValue("生产日期");

        HSSFCell cellOperArrival = rowHeader.createCell(12);
        cellOperArrival.setCellStyle(style);
        cellOperArrival.setCellValue("到货日期");

        List<Factory> factoryList = factoryMapper.queryFactoryCodeOui();

        for (int i = 0; i < listBooks.size(); i++) {
            HSSFRow row = sheet.createRow(i + 1);

            HSSFCell cell = row.createCell((short)0);
            cell.setCellValue(((GateWayInfoExcelContent)listBooks.get(i)).getGatewayInfoMacaddress());

            HSSFCell cell01 = row.createCell((short)1);
            cell01.setCellValue(((GateWayInfoExcelContent)listBooks.get(i)).getGatewayInfoSerialnumber());

            HSSFCell cell02 = row.createCell((short)2);
            if(((GateWayInfoExcelContent)listBooks.get(i)).getSsid()!=null || !"".equals(((GateWayInfoExcelContent)listBooks.get(i)).getSsid())){
                cell02.setCellValue(((GateWayInfoExcelContent)listBooks.get(i)).getSsid());
            }else{
                cell02.setCellValue(((GateWayInfoExcelContent)listBooks.get(i)).getSsid());
            }

            HSSFCell cell03 = row.createCell((short)3);
            if(((GateWayInfoExcelContent)listBooks.get(i)).getSsidpwd()!=null || !"".equals(((GateWayInfoExcelContent)listBooks.get(i)).getSsidpwd())){
                cell03.setCellValue(((GateWayInfoExcelContent)listBooks.get(i)).getSsidpwd());
            }else{
                cell03.setCellValue("");
            }

            HSSFCell cell04 = row.createCell((short)4);
            if(((GateWayInfoExcelContent)listBooks.get(i)).getUpwd() != null || !"".equals(((GateWayInfoExcelContent)listBooks.get(i)).getUpwd())){
                cell04.setCellValue(((GateWayInfoExcelContent)listBooks.get(i)).getUpwd());
            }else{
                cell04.setCellValue("");
            }

            HSSFCell cell05 = row.createCell((short)5);
            cell05.setCellValue(((GateWayInfoExcelContent)listBooks.get(i)).getGatewayInfoModel());

            HSSFCell cell06 = row.createCell((short)6);
            for (Factory factory : factoryList)
            {
                if(factory.getOui().equals(((GateWayInfoExcelContent)listBooks.get(i)).getGatewayInfoFactoryCode()))
                {
                    cell06.setCellValue(factory.getFactoryCode());
                    break;
                }
            }

            HSSFCell cell07 = row.createCell((short)7);
            cell07.setCellValue(((GateWayInfoExcelContent)listBooks.get(i)).getGatewayInfoHardwareVersion());

            HSSFCell cell08 = row.createCell((short)8);
            cell08.setCellValue(((GateWayInfoExcelContent)listBooks.get(i)).getGatewayInfoVersion());

            HSSFCell cell09 = row.createCell((short)9);
            cell09.setCellValue("OSGI");

            HSSFCell cell10 = row.createCell((short)10);
            cell10.setCellValue("10");

            HSSFCell cell11 = row.createCell((short)11);
            cell11.setCellValue(((GateWayInfoExcelContent)listBooks.get(i)).getManufactureDate());

            HSSFCell cell12 = row.createCell((short)12);
            cell12.setCellValue(((GateWayInfoExcelContent)listBooks.get(i)).getArrivalDate());
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            wb.write(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] xls = os.toByteArray();

        fileName ="gateway" + DateTools.getCurrentTimeMillis() + ".xls";
        logger.info(" fileName " + fileName);
        File file = new File(fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            try {
                out.write(xls);
                out.flush();
                out.close();
            } catch (IOException e) {
                logger.info(exceptionInfo(e));
            }
        } catch (FileNotFoundException e) {
            logger.info(exceptionInfo(e));
        }

        //上传到文件服务器
        FtpBusiness ftpBusiness = new FtpBusiness();
        /*String url = buildFServerPath(ftpUrl);*/
        logger.info("upload to ftpServer: " + ftpUrl);
        String ftpserverName = ftpBusiness.sendTofileServer(ftpUrl,fileName);
        logger.info("upload excel to ftp server success...");
        logger.info("ftpserverName " + ftpserverName);
        ftpBusiness.deleteFiles(fileName);
        return ftpserverName;
    }


	@Override
	public Map<String, Object> syncGatewayOnlineStatus(Map<String, Object> parameter) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		String gatewayUuid = parameter.get("gatewayUuid").toString();
		String second = parameter.get("timeout").toString();
		Map<String, Object> amp = executeOne(gatewayUuid, "InternetGatewayDevice.WANDevice.", true, Integer.parseInt(second)*1000);
		retMap.put("resultCode", "0");
		if(amp == null){
			retMap.put("onLineState", "N");
		}else{
			retMap.put("onLineState", "Y");
		}
		return retMap;
	}
	
	public Map<String, Object> executeOne(String gatewayUuid, String path, boolean nextLevel, int timeOutMillis) {
        try {

            GatewayInfo cpe = gatewayInfoService.selectByUuid(gatewayUuid);

            // 判断是否有网关信息
            if (cpe == null || org.apache.commons.lang.StringUtils.isBlank(cpe.getGatewayConnectionrequesturl())) {
              
                return null;
            }

            InstructionsInfoWithBLOBs is = new InstructionsInfoWithBLOBs();
            
            Map<String, Object> map = new HashMap<>();
            map.put("gatewayId", gatewayUuid);
            map.put("methodName", "GetParameterNames");
            map.put("parameterPath", path);
            map.put("nextLevel", nextLevel);
            
            //生成指令ID作为请求的requestId
            String insId = UniqueUtil.uuid();
            is.setInstructionsId(insId);
            is.setCpeIdentity(gatewayUuid);
            map.put("requestId", insId);
            AbstractInstruction ins = (AbstractInstruction) Class.forName("com.cmiot.rms.services.instruction.impl." + map.get("methodName") + "Instruction").newInstance();

            AbstractMethod abstractMethod = ins.createIns(is, cpe, map);
            if(org.apache.commons.lang.StringUtils.isNotBlank(cpe.getGatewayConnectionrequestUsername())
                    && org.apache.commons.lang.StringUtils.isNotBlank(cpe.getGatewayConnectionrequestPassword()))
            {
                abstractMethod.setCpeUserName(cpe.getGatewayConnectionrequestUsername());
                abstractMethod.setCpePassword(cpe.getGatewayConnectionrequestPassword());
            }
            is.setInstructionsBeforeContent(JSON.toJSONString(abstractMethod));

            instructionsService.addInstructionsInfo(is);

            Map<String, Object> returmMap ;
            logger.info("发送json到acs 指令id:{} 网关id：{} json:{}", insId, gatewayUuid, JSON.toJSONString(abstractMethod) );

            List<AbstractMethod> abstractMethods = new ArrayList<>();
            abstractMethods.add(abstractMethod);
            returmMap = operationCpeFacade.doACSEMethods(abstractMethods);

            logger.info("连接ACS返回结果：" + returmMap);
            returmMap.put("requestId", insId);

            //为指令添加临时对象锁，等待指令异步返回
            TemporaryObject temporaryObject = new TemporaryObject(insId);
            RequestCache.set(insId, temporaryObject);

            logger.info("Start wait: {}", temporaryObject.getRequestId());
            synchronized (temporaryObject) {
                temporaryObject.wait(timeOutMillis);
            }
            RequestCache.delete(insId);

            logger.info("End wait: {}", temporaryObject.getRequestId());

            // 记录接口调用日志
            LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(abstractMethod), insId, cpe.getGatewayUuid(), map.get("methodName").toString(),LogTypeEnum.LOG_TYPE_SYSTEM.description());
            
            
            if(null == returmMap || returmMap.get("resultCode").toString().equals("1"))
            {
                return null;
            }


            String requestId = returmMap.get("requestId").toString();
            Map<String, String> insMap = instructionsService.getInstructionsInfo(requestId);
            int status = getStatus(insMap.get("status"));
            if (status == InstructionsStatusEnum.STATUS_1.code()) {
            	Map<String, Object> retMap  =  new HashMap<>();
                String jsonStr = insMap.get("json");
                if (jsonStr == null) {
                    return null;
                }
                JSONObject jsonObject = JSON.parseObject(jsonStr);

                JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(jsonObject.get("parameterList")));
                ParameterInfoStruct parameterInfoStruct;
                for(Object object : jsonArray)
                {
                    JSONObject jsonObj = (JSONObject) JSON.toJSON(object);
                    parameterInfoStruct = JSON.toJavaObject(jsonObj, ParameterInfoStruct.class);
                    retMap.put(parameterInfoStruct.getName(), parameterInfoStruct.isWritable());
                }
                return retMap;
            }
            
            
        } catch (Exception e) {
            logger.error("executeOne exception:{}", e);
        }
        return null;
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


    @Override
    public Map<String, Object> getBusinessCategory(Map<String, Object> parameter) {
        List<BusinessCode> mapList= businessCodeMapper.seletAll();
        List<Map<String, Object>> bclist = new ArrayList<>();
        if(null != mapList && mapList.size() >0){
        	 for(BusinessCode bb : mapList){
        		 Map<String, Object> map = new HashMap<>();
        		 map.put("businessCodeBoss", bb.getBusinessCodeBoss());
        		 map.put("businessNameBoss", bb.getBusinessNameBoss());
        		 bclist.add(map);
                 logger.info(" ~~bb~~ " +bb.getBusinessNameBoss());
             }
        }
        Map<String, Object> backMap = new HashMap<>();
        backMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
        backMap.put(Constant.MESSAGE, "业务类型");
        backMap.put(Constant.DATA, bclist);
        return backMap;
    }

    @Override
    public Map<String, Object> getDeviceInfoList(Map<String, Object> parameter) {
        Map<String, Object> backMap = new HashMap<>();
        if(parameter.get("factoryCode").toString()==null||"".equals(parameter.get("factoryCode").toString())){
            logger.info(" gatewayFactoryCode is null");
            backMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
            backMap.put(Constant.MESSAGE, "设备型号");
            backMap.put(Constant.DATA, null);
        }else {
            DeviceInfo deviceInfoToSearch = new DeviceInfo();
            deviceInfoToSearch.setDeviceFactory(parameter.get("factoryCode").toString());
            List<DeviceInfo> deviceInfoList = deviceInfoMapper.queryList(deviceInfoToSearch);
            backMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
            backMap.put(Constant.MESSAGE, "设备型号");
            backMap.put(Constant.DATA, deviceInfoList);
        }
        logger.info("  ~~getDeviceInfoList ok~~");
        return backMap;
    }

    @Override
    public Map<String, Object> queryGatewayUpAndDownSpeed(Map<String, Object> parameter) {
        logger.info("start invoke queryGatewayUpAndDownSpeed,parameter:{}",parameter);
        Map<String, Object> retMap = new HashMap<String, Object>();
        String gatewayId = parameter.get("id") == null ? null : parameter.get("id").toString();
        if (gatewayId == null) {
            logger.info("invoke queryGatewayUpAndDownSpeed ， 输入id为空");
            retMap.put(Constant.CODE, 10000);
            retMap.put(Constant.MESSAGE, "id为空");
            return retMap;
        }
        //数据库查询WAN口节点前缀
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewayUuid(gatewayId);
        gatewayInfo = gatewayInfoMapper.selectGatewayInfo(gatewayInfo);
        if (gatewayInfo == null) {
            logger.info("invoke queryGatewayUpAndDownSpeed，该id的网关不存在,id:{}", gatewayId);
            retMap.put(Constant.CODE, 10005);
            retMap.put(Constant.MESSAGE, "网关id不存在");
            return retMap;
        }

        queryGatewaySpeed(retMap, gatewayInfo);
        logger.info("end invoke queryGatewayUpAndDownSpeed,retMap:{}", retMap);
        return retMap;
    }

    /**
     * 查询网关上下行速率(通过网关mac查询)
     *
     * @param parameter@return
     */
    @Override
    public Map<String, Object> queryGatewaySpeedByMac(Map<String, Object> parameter) {
        logger.info("start invoke queryGatewaySpeedByMac,parameter:{}",parameter);
        Map<String, Object> retMap = new HashMap<String, Object>();
        String gatewayMac = parameter.get("mac") == null ? null : parameter.get("mac").toString();
        if (gatewayMac == null) {
            logger.info("invoke queryGatewaySpeedByMac ， 输入mac为空");
            retMap.put(Constant.CODE, 10000);
            retMap.put(Constant.MESSAGE, "id为空");
            return retMap;
        }
        //数据库查询WAN口节点前缀
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewayMacaddress(gatewayMac);
        gatewayInfo = gatewayInfoMapper.selectGatewayInfo(gatewayInfo);
        if (gatewayInfo == null) {
            logger.info("invoke queryGatewaySpeedByMac，该mac的网关不存在,mac:{}", gatewayMac);
            retMap.put(Constant.CODE, 10005);
            retMap.put(Constant.MESSAGE, "网关mac不存在");
            return retMap;
        }

        queryGatewaySpeed(retMap, gatewayInfo);
        logger.info("end invoke queryGatewaySpeedByMac,retMap:{}", retMap);
        return retMap;
    }



    private void queryGatewaySpeed(Map<String, Object> retMap, GatewayInfo gatewayInfo)
    {
        try {
            String gatewayMacAddress = gatewayInfo.getGatewayMacaddress();
            //计算网关的上下行速率
            String bytesSend = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.X_CMCC_Stats.BytesSen[d|t]";
            String bytesReceived = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.X_CMCC_Stats.BytesReceived";
            String lanDeviceMAC = "InternetGatewayDevice.LANDevice.[0-9]+.Hosts.Host.[0-9]+.MACAddress";
            GateWaySpeedReportBean gateWaySpeedReportBean = new GateWaySpeedReportBean();
            gateWaySpeedReportBean.setGateWayMac(gatewayMacAddress);
            gateWaySpeedReportBean.setTime(System.currentTimeMillis());
            Pattern pattrenSend = Pattern.compile(bytesSend);
            Pattern pattrenReceive = Pattern.compile(bytesReceived);

            List<String> wanNameList = new ArrayList<>();
            //查询网关下挂主机列表的所有节点名称
            Map<String, Object> nameMap = instructionMethodService.getParameterNames(gatewayMacAddress, "InternetGatewayDevice.LANDevice.", false);
            logger.debug(" queryGatewayUpAndDownSpeed getParameterNames nameMap:{}", nameMap);
            List<String> regWanNameList = new ArrayList<String>();
            regWanNameList.add(bytesSend);
            regWanNameList.add(bytesReceived);
            regWanNameList.add(lanDeviceMAC);
            for (Map.Entry<String, Object> entry : nameMap.entrySet()) {
                InstructionUtil.getName(wanNameList, regWanNameList, entry.getKey());
            }
            //根据具体的节点获取相应的value
            Map<String, Object> wanValueMap = instructionMethodService.getParameterValues(gatewayMacAddress, wanNameList);
            logger.info(" MonitorServiceImpl reportMonitor getParameterValues wanValueMap:{}", wanValueMap);
            //处理查询到的值，一种是下挂设备的MAC，一种是根据每个下挂设备的流量计算网关的流量
            Long sendBytes = 0l;
            Long receivedBytes = 0l;
            List<SubDeviceSpeedReportBean> lanDeviceList = new ArrayList<>();//用来存放当前网关所有的下挂设备信息
            for (Map.Entry<String, Object> entry : wanValueMap.entrySet()) {
                SubDeviceSpeedReportBean lanDevice = new SubDeviceSpeedReportBean();
                Matcher matcherSend = pattrenSend.matcher(entry.getKey());
                Matcher matcherReceived = pattrenReceive.matcher(entry.getKey());
                if (matcherSend.matches()) {
                    lanDevice.setSubDeviceUpBytes(entry.getValue().toString());
                    String lanDeviceMACname = entry.getKey().substring(0, entry.getKey().indexOf("X_CMCC_Stats")) + "MACAddress";

                    int indexBytesSend = entry.getKey().indexOf("BytesSend");
                    String lanDeviceReceivedname;
                    if(indexBytesSend != -1)
                    {
                        lanDeviceReceivedname = entry.getKey().substring(0, indexBytesSend) + "BytesReceived";
                    }
                    else
                    {
                        lanDeviceReceivedname = entry.getKey().substring(0, entry.getKey().indexOf("BytesSent")) + "BytesReceived";
                    }

                    lanDevice.setSubDeviceMac(wanValueMap.get(lanDeviceMACname).toString());
                    lanDevice.setSubDeviceDownBytes(wanValueMap.get(lanDeviceReceivedname).toString());
                    lanDeviceList.add(lanDevice);
                    sendBytes = sendBytes + Long.valueOf(entry.getValue().toString());
                } else if (matcherReceived.matches()) {
                    receivedBytes = receivedBytes + Long.valueOf(entry.getValue().toString());
                }
            }
            gateWaySpeedReportBean.setDownBytes(String.valueOf(receivedBytes));
            gateWaySpeedReportBean.setUpBytes(String.valueOf(sendBytes));
            gateWaySpeedReportBean.setGetHGByteTime(System.currentTimeMillis());
            gateWaySpeedReportBean.setLanDeviceList(lanDeviceList);
            //定义网关在redis上存储的键值
            String gateWaySpeedKey = gatewayMacAddress + "queryGateWaySpeedKey";
            //先获取，如果存在就用上次的做计算速率，并上报给一级家开平台，如果不存在，则保存到reids
            String redisStr = redisClientTemplate.get(gateWaySpeedKey);
            logger.info(" queryGatewayUpAndDownSpeed redisClientTemplate.get redisStr:{},gateWaySpeedKey:{}", redisStr,gateWaySpeedKey);
            DecimalFormat df2  = new DecimalFormat("0.00");
            Long HGDownByte = 0l;
            Long HGUpByte = 0l;
            Double DownSpeed = 0.0;
            Double UpSpeed = 0.0;
            if (org.apache.commons.lang.StringUtils.isEmpty(redisStr)) {
                redisClientTemplate.set(gateWaySpeedKey,JSON.toJSONString(gateWaySpeedReportBean));
                redisClientTemplate.expire(gateWaySpeedKey,gatewaySpeedLockTimeout);
            } else {
                //更新redis数据
                redisClientTemplate.set(gateWaySpeedKey,JSON.toJSONString(gateWaySpeedReportBean));
                redisClientTemplate.expire(gateWaySpeedKey, gatewaySpeedLockTimeout);
                GateWaySpeedReportBean gateWaySpeedReportBeanFind = JSON.parseObject(redisStr, GateWaySpeedReportBean.class);
                //网关速率，单位 字节/秒
                Long intervalTime2 = (Long.valueOf(gateWaySpeedReportBean.getGetHGByteTime()) - Long.valueOf(gateWaySpeedReportBeanFind.getGetHGByteTime())) / 1000;
                //计算网关速率，前一个时间点和当前时间点在线终端的速率的总和
                if (gateWaySpeedReportBean.getLanDeviceList() != null) {
                    for (int i = 0; i < gateWaySpeedReportBean.getLanDeviceList().size(); i++) {
                        SubDeviceSpeedReportBean subDeviceFind = gateWaySpeedReportBean.getLanDeviceList().get(i);
                        if (gateWaySpeedReportBeanFind.getLanDeviceList() != null) {
                            for (int j = 0; j < gateWaySpeedReportBeanFind.getLanDeviceList().size(); j++) {
                                if (subDeviceFind.getSubDeviceMac().equals(gateWaySpeedReportBeanFind.getLanDeviceList().get(j).getSubDeviceMac())) {
                                    HGDownByte = HGDownByte + (Long.valueOf(subDeviceFind.getSubDeviceDownBytes()) - Long.valueOf(gateWaySpeedReportBeanFind.getLanDeviceList().get(j).getSubDeviceDownBytes()));
                                    HGUpByte = HGUpByte + (Long.valueOf(subDeviceFind.getSubDeviceUpBytes()) - Long.valueOf(gateWaySpeedReportBeanFind.getLanDeviceList().get(j).getSubDeviceUpBytes()));
                                }
                            }
                        }
                    }
                }
                DownSpeed = HGDownByte /(double) intervalTime2;
                UpSpeed = HGUpByte / (double) intervalTime2;
            }
            retMap.put(Constant.CODE, 0);
            retMap.put(Constant.MESSAGE, "success");
            retMap.put("upSpeed", UpSpeed>=0?df2.format(UpSpeed)+"":"0");
            retMap.put("downSpeed", DownSpeed>=0?df2.format(DownSpeed)+"":"0");
        }catch (Exception e){
            logger.error("invoke queryGatewayUpAndDownSpeed,e:{}",e);
            retMap.put(Constant.CODE, 10005);
            retMap.put(Constant.MESSAGE, "fail");
        }
    }

    @Override
    public Map<String, Object> queryGatewayTypeByConf() {
        Map<String, Object> retMap = new HashMap<>();
        if("".equals(gateWayTypeConf)){
        retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
        retMap.put(Constant.MESSAGE, "查无数据");
        logger.info("End invoke queryBoxModel:{}", retMap);
        return retMap;
        }else{
            try {
                Map<String, String> gatewayTypeMap = new HashMap<>();
                String [] strArr =  gateWayTypeConf.split(";");
                if(strArr.length>0){
                    for(int t = 0;t<strArr.length;t++)
                    {
                        String str=new String(strArr[t].getBytes("ISO-8859-1"),"UTF-8");
                        gatewayTypeMap.put(str, str);
                    }
                    retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
                    retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
                    retMap.put(Constant.DATA, gatewayTypeMap);
                }else{
                    retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
                    retMap.put(Constant.MESSAGE, "查无数据");
                    logger.info("End invoke queryBoxModel:{}", retMap);
                }
            } catch (Exception e) {
                logger.error("queryBoxModel exception:{}", e);
                retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
            }
            logger.info("End invoke queryBoxModel:{}", retMap);
            return retMap;
        }
    }


    public String exceptionInfo (Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString().toLowerCase();
    }

    public static void createFolder(String filePath) {
        File f=new File(filePath);
        /*f.setWritable(true, false);*/
        logger.info(" filePath " + filePath);
        if(!f.exists()){
            boolean op = f.mkdirs();
            if(op){
                logger.info( " create filePath " + filePath +" success!");
            }else{
                logger.info( " mkdirs " + filePath +" failed!");
            }
        }else{
            logger.info( " filePath " + filePath +" is exists!");
        }
    }

    public static String buildFServerPath(String filePath) {
        Pattern pattern2 = Pattern
                .compile("(ftp|ftps):\\/\\/([\\w.]+\\/?)\\S*");
        /*.compile("(http|ftp|https):\\/\\/([\\w.]+\\/?)\\S*");*/
        Matcher matcher2 = pattern2.matcher(filePath);
        if(matcher2.find()){
            filePath = filePath + "?ftp=true";
            return filePath;
        }
        return filePath;
    }




    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Map<String, Object> unbindGateway(Map<String, Object> parameter) {

    	Map<String, Object> retMap = new HashMap<>();

    	if (org.apache.commons.lang.StringUtils.isEmpty((String)parameter.get("gatewayUuid"))) {
    		retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, "网关ID不能为空");
            return retMap;
		}

    	String gatewayUuid = (String)parameter.get("gatewayUuid");
    	GatewayInfo updateGatewayInfo = new GatewayInfo();
        updateGatewayInfo.setGatewayUuid(gatewayUuid);

        try {
			int result = gatewayInfoMapper.unBindPasswordAndAdslAccount(updateGatewayInfo);
			if (result == 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
	            retMap.put(Constant.MESSAGE, "数据不合法");
	            return retMap;
			}
			gatewayBusinessOpenDetailMapper.deleteByGatewayId(gatewayUuid);

			retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, "解绑成功");
    		//通知BMS
            GatewayInfo gw = gatewayInfoMapper.selectByPrimaryKey(gatewayUuid);
    		try {
    			Map<String,Object> resultbms = gatewayHandleService.factoryNotify(gw.getGatewayMacaddress(), "4", false);
    			logger.info("通知BMS恢复出厂设置并调用一级平台销户接口，请求MAC:"+ gw.getGatewayMacaddress() +",返回结果"+ resultbms.get("resultMsg"));
    			logger.info("通知BMS恢复出厂设置并调用一级平台销户接口，请求MAC:"+ gw.getGatewayMacaddress() +",由于异步处理，不等待BMS返回，所以返回结果为空");
    		} catch (Exception e) {
    			//logger.info("通知BMS恢复出厂设置并调用一级平台销户接口，请求MAC:"+ gw.getGatewayMacaddress() +",异常:"+ e.getMessage());
    		}
            
			//进行恢复出厂设置
			if ("1".equals((String)parameter.get("isResetFactory"))) {

				Map<String, Object> ResetMap = new HashMap<String, Object>();
                List<String> gatewayIdsList = new ArrayList<String>();
                gatewayIdsList.add(gatewayUuid);
                ResetMap.put("gatewayIds", gatewayIdsList);
				Map map = this.factoryReset(ResetMap);
				if (!RespCodeEnum.RC_0.code().equals(map.get(Constant.CODE).toString())) {
					retMap.put(Constant.CODE, ErrorCodeEnum.OPERATION_ERROR.getResultCode());
		            retMap.put(Constant.MESSAGE, "解绑成功,"+map.get(Constant.MESSAGE));
				}
			}
			OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.GATEWAY_MANAGER_SERVICE, "修改对象节点相关属性", JSON.toJSONString(parameter));
		} catch (Exception e) {
			logger.error("unbindGateway exception:{}", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.UPDATE_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, "解绑异常");
		}

    	return retMap;
	}

    /**
     * 根据网关mac查询网关信息
     * （bms使用）
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryGatewayByMac(Map<String, Object> parameter) {
        logger.info("Start invoke queryGatewayByMac:{}", parameter);
        List<String> macList = null != parameter.get("macList") ? (List)parameter.get("macList") : null;

        Map<String, Object> retMap = new HashMap<>();
        if(null == macList || macList.size() < 1)
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            logger.info("End invoke queryGatewayByMac:{}", retMap);
            return retMap;
        }

        try
        {
            List<Map<String, Object>> gatewayList = gatewayInfoMapper.selectByMac(macList);
            if(null == gatewayList || gatewayList.size() < 1)
            {
                retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
                retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
                retMap.put(Constant.DATA, new ArrayList<>());
            }

            String areaId;
            for(Map<String, Object> gateway : gatewayList)
            {
                gateway.put("areaName", "");
                areaId = null != gateway.get("areaId") ? gateway.get("areaId").toString() : "";
                if(org.apache.commons.lang.StringUtils.isNotBlank(areaId))
                {
                    Area area = areaService.findAreaById(Integer.valueOf(areaId));
                    if(null != area)
                    {
                        gateway.put("areaName", area.getName());
                    }
                }
            }

            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
            retMap.put(Constant.DATA, gatewayList);
        }
        catch (Exception e)
        {
            logger.error("queryGatewayByMac exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }

        logger.info("End invoke queryGatewayByMac:{}", parameter);
        return retMap;
    }

    @Override
	public Integer queryGatewayCountByAreaId(String areaId) {
		Integer count = 0;
		if(areaId == null || areaId.equals("")){
			return count;
		}
		count = gatewayInfoMapper.queryGatewayCountByAreaId(areaId);
		return count;
	}

	@Override
	public Integer queryGatewayCountByAreaList(List<String> areaIds) {
		Integer count = 0;
		if(areaIds == null || areaIds.size() == 0){
			return count;
		}
		count = gatewayInfoMapper.queryGatewayCountByAreaList(areaIds);
		return count;
	}


}
