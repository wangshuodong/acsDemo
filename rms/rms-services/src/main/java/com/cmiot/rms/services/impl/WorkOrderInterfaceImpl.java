package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.ams.domain.*;
import com.cmiot.ams.domain.Area;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.enums.RebootEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.*;
import com.cmiot.rms.dao.model.*;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.services.GatewayManageService;
import com.cmiot.rms.services.WorkOrderInterface;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.workorder.ExcuteWorkOrderThread;
import com.cmiot.rms.services.workorder.SpringApplicationContextHolder;
import com.cmiot.rms.services.workorder.WorkerThreadPool;
import com.cmiot.rms.services.workorder.impl.BusiOperation;
import com.tydic.inter.app.service.GatewayHandleService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/6/9.
 */
@Service
public class WorkOrderInterfaceImpl  implements WorkOrderInterface {

    private Logger logger = LoggerFactory.getLogger(WorkOrderManagerServiceImpl.class);

    @Autowired
    private BusinessCategoryMapper businessCategoryMapper;

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;

    @Autowired
    private GatewayBusinessMapper gatewayBusinessMapper;

    @Autowired
    private GatewayPasswordMapper gatewayPasswordMapper;

    @Autowired
    BusiOperation busiOperation;

    @Autowired
    GatewayBusinessExecuteHistoryMapper gatewayBusinessExecuteHistoryMapper;

    @Autowired
    WorkOrderTemplateInfoMapper workOrderTemplateInfoMapper;

    @Autowired
    GatewayManageService gatewayManageService;

    @Autowired
    private RedisClientTemplate redisClientTemplate;

    @Autowired
    private GatewayBusinessOpenDetailMapper gatewayBusinessOpenDetailMapper;

    @Autowired
    private BusinessCodeMapper businessCodeMapper;

    @Autowired
    private AreaService areaService;

    @Autowired
    private SpringApplicationContextHolder springApplicationContextHolder;

    @Autowired
    private GatewayAdslAccountMapper gatewayAdslAccountMapper;

    @Value("${factoryResetTimeOut}")
    int factoryResetTimeOut;

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private GatewayHandleService gatewayHandleService;

    @Override
    public Map<String, Object> openService(Map<String, Object> parameter) {
//        logger.info("start invoke openService:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        try{
            Map<String,Object> retDataMap = new HashMap<>();
            String CmdType = (String) parameter.get("CmdType");
            retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_SUCCESS);
            retMap.put(Constant.PBOSS_CMDTYPE,CmdType);
            retMap.put(Constant.PBOSS_RESULTDATA,retDataMap);
            Map<String, Object> paraMap = (Map<String, Object>) parameter.get(Constant.PBOSS_PARAMETER);
            List<Map> installationList = (List) paraMap.get("NewInstallationArray");
            List<GatewayBusiness> listGb = new ArrayList<>();
            //解析参数
            for (Map item : installationList) {
                GatewayBusiness gatewayBusiness = new GatewayBusiness();
                Map<String, Object> parmMap = new HashMap<>();
                String orderNo = (String) item.get("OrderNo");
                if (StringUtils.isEmpty(orderNo)) {
                    logger.info("orderNo为空");
                    retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                    retMap.put(Constant.PBOSS_FAILREASON, "orderNo is null");
                    return retMap;
                }
                String redisKey = "W-O-" + orderNo;
                String redisResult = redisClientTemplate.get(redisKey);
                if(!StringUtils.isEmpty(redisResult)){
                    retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_VARIFICATION_FAIL);
                    retDataMap.put(Constant.PBOSS_FAILREASON,"OrderNo has existed");
                    return  retMap;
                }
                gatewayBusiness.setOrderNo(orderNo);
                String ServiceCode = (String) item.get("ServiceCode");
                if (StringUtils.isEmpty(ServiceCode)) {
                    logger.info("ServiceCode为空");
                    retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                    retMap.put(Constant.PBOSS_FAILREASON, "ServiceCode is null");
                    return retMap;
                }

                gatewayBusiness.setBusinessCode(ServiceCode);
                gatewayBusiness.setBusinessCodeBoss(ServiceCode);
                String loid = (String) item.get("LOID");
                if ("wband".equals(ServiceCode)) {
                    if (StringUtils.isEmpty(loid)) {
                        logger.info("ServiceCode为wabnd时loid为空");
                        retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                        retMap.put(Constant.PBOSS_FAILREASON, "ServiceCode is wband,LOID is null");
                        return retMap;
                    }
                }
                if (!StringUtils.isEmpty(loid)) {
                    gatewayBusiness.setGatewayPassword(loid);
                }
                String areCode = (String) item.get("AreaCode");
                //验证AreaCode是否合法

                if (!StringUtils.isEmpty(areCode)) {
                    gatewayBusiness.setAreacode(areCode);
                }
                String pppoeAccount = (String) item.get("PppoeAccount");
                if (!StringUtils.isEmpty(pppoeAccount)) {
                    gatewayBusiness.setAdslAccount(pppoeAccount);
                    parmMap.put("Username", pppoeAccount);
                }
                if ("wband".equals(ServiceCode)) {
                    if (StringUtils.isEmpty(pppoeAccount)) {
                        logger.info("ServiceCode为wabnd时PppoeAccount为空");
                        retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                        retMap.put(Constant.PBOSS_FAILREASON, "ServiceCode is wband,PppoeAccount is null");
                        return retMap;
                    }
                }
                String pppoePassword = (String) item.get("PppoePassword");
                if (!StringUtils.isEmpty(pppoePassword)) {
                    gatewayBusiness.setAdslPassword(pppoePassword);
                    parmMap.put("Password", pppoePassword);
                }
                if ("wband".equals(ServiceCode)) {
                    if (StringUtils.isEmpty(pppoePassword)) {
                        logger.info("ServiceCode为wabnd时PppoePassword为空");
                        retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                        retMap.put(Constant.PBOSS_FAILREASON, "ServiceCode is wband,PppoePassword is null");
                        return retMap;
                    }
                }

                String SIPUserName = (String) item.get("SIPUserName");
                if (!StringUtils.isEmpty(SIPUserName)) {
                    parmMap.put("SIP.AuthUserName", SIPUserName);
                }
                if ("voip".equals(ServiceCode)) {
                    if (StringUtils.isEmpty(SIPUserName)) {
                        logger.info("ServiceCode为voip时SIPUserName为空");
                        retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                        retMap.put(Constant.PBOSS_FAILREASON, "ServiceCode is voip,SIPUserName is null");
                        return retMap;
                    }
                }

                String SIPUserPWD = (String) item.get("SIPUserPWD");
                if (!StringUtils.isEmpty(SIPUserPWD)) {
                    parmMap.put("SIP.AuthPassword", SIPUserPWD);
                }
                if ("voip".equals(ServiceCode)) {
                    if (StringUtils.isEmpty(SIPUserPWD)) {
                        logger.info("ServiceCode为voip时SIPUserPWD为空");
                        retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                        retMap.put(Constant.PBOSS_FAILREASON, "ServiceCode is voip,SIPUserPWD is null");
                        return retMap;
                    }
                }

                String BANDWIDTH = (String) item.get("BANDWIDTH");
                if (!StringUtils.isEmpty(BANDWIDTH)) {
                    gatewayBusiness.setBandwidth(Integer.valueOf(BANDWIDTH));
                }
                gatewayBusiness.setBusinessStatu("0");

//                gatewayBusiness.setId(UniqueUtil.uuid());
                gatewayBusiness.setParameterList(JSON.toJSONString(parmMap));
//                gatewayBusiness.setCreateTime(DateTools.getCurrentSecondTime());
                gatewayBusiness.setBusinessType("1");
                listGb.add(gatewayBusiness);
            }

            //向redis写数据
            for(GatewayBusiness gb :listGb){
                String redisKey = "W-O-" + gb.getOrderNo();
                redisClientTemplate.set(redisKey, JSON.toJSONString(gb));
            }
            /*String serverCode = (String) parameter.get("serverCode");
            String gatewayPassword = (String) parameter.get("gatewayPassword");
            if(StringUtils.isEmpty(serverCode)){
                logger.info("openService serverCode 为空");
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
                retMap.put(Constant.MESSAGE,"服务编码不能为空");
                return retMap;
            }
            parameter.remove("serverCode");
            if(StringUtils.isEmpty(gatewayPassword)){
                logger.info("openService gatewayPassword 为空");
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
                retMap.put(Constant.MESSAGE,"网关密码不能为空");
                return retMap;
            }
            parameter.remove("gatewayPassword");
            parameter.remove("methodType");
            //根据业务代码查询业务
            BusinessCategory businessCategory = new BusinessCategory();
            businessCategory.setBusinessCode(serverCode);
            List<Map<String,Object>> list = businessCategoryMapper.queryList(businessCategory);
            if(list == null ||list.size()<=0){
                logger.info("openService serverCode 错误");
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
                retMap.put(Constant.MESSAGE,"serverCode 错误");
                return retMap;
            }
            //一个serverCode应该只有一条数据
            String serverName = (String) list.get(0).get("businessName");
            //根据gatewayPassword查询网关信息
            GatewayInfo paramInfo = new GatewayInfo();
            paramInfo.setGatewayPassword(gatewayPassword);
            GatewayInfo gatewayInfo = gatewayInfoMapper.selectGatewayInfo(paramInfo);
            if(gatewayInfo == null){
                logger.info("openService gatewayPassword 错误");
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
                retMap.put(Constant.MESSAGE,"gatewayPassword 错误");
                return retMap;
            }
            //先查询该网关是否存在非执行成功的业务
            String gateWayUuid = gatewayInfo.getGatewayUuid();
            GatewayBusiness gatewayBusiness = new GatewayBusiness();
            gatewayBusiness.setId(UniqueUtil.uuid());
            gatewayBusiness.setBusinessCode(serverCode);
            gatewayBusiness.setGatewayUuid(gateWayUuid);
            gatewayBusiness.setBusinessName(serverName);
            gatewayBusiness.setParameterList(JSON.toJSONString(parameter));
            gatewayBusiness.setBusinessStatu("0");
            gatewayBusiness.setBusinessType("1");
            int i = 0 ;
            i = gatewayBusinessMapper.insertSelective(gatewayBusiness);
            if(i>0){
                retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
                retMap.put(Constant.MESSAGE,"成功");
            }else{
                retMap.put(Constant.CODE,ErrorCodeEnum.INSERT_ERROR.getResultCode() );
                retMap.put(Constant.MESSAGE,"开通失败");
            }*/
        }catch (Exception e){
            logger.error("invoke  openService error:{}", e);
            retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_FAIL);
        }
        return retMap;
    }

    @Override
    public Map<String, Object> addNewInstallation(Map<String, Object> parameter) {
        logger.info("start invoke newInstallation,parameter:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        Map<String,Object> retDataMap = new HashMap<>();
        String CmdType = (String) parameter.get("CmdType");
        retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_SUCCESS);
        retMap.put(Constant.PBOSS_CMDTYPE,CmdType);
        retMap.put(Constant.PBOSS_RESULTDATA,retDataMap);
        try {
            Map<String, Object> paraMap = (Map<String, Object>) parameter.get(Constant.PBOSS_PARAMETER);
            List<Map> installationList = (List) paraMap.get("NewInstallationArray");
            GatewayInfo gatewayInfo = null;
            if(installationList.size() == 0){
                logger.info("NewInstallationArray为空");
                retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                retDataMap.put(Constant.PBOSS_FAILREASON,"NewInstallationArray不能为空");
                return retMap;
            }
            //解析参数
            Map<String, Object> returnMap = parseParam(installationList);
            if(!(Constant.PBOSS_SUCCESS == (int) returnMap.get(Constant.PBOSS_RESULT))){
                retMap.put(Constant.PBOSS_RESULT,returnMap.get(Constant.PBOSS_RESULT));
                retDataMap.put(Constant.PBOSS_FAILREASON,returnMap.get(Constant.PBOSS_FAILREASON));
                return  retMap;
            }
            String password = (String) returnMap.get("password");
            String orderNo = (String) returnMap.get("orderNo");
            List<GatewayBusiness> listGb = (List<GatewayBusiness>) returnMap.get("listGb");
            //新装宽带
            if("wband".equals(returnMap.get("serviceCode").toString())){
                //查询gateway_password表是否有该password，如果没有则写入
                GatewayPassword gatewayPassword = new GatewayPassword();
                gatewayPassword.setGatewayPassword(password);
                GatewayPassword gatewayPasswordResult = gatewayPasswordMapper.selectByPassword(gatewayPassword);
                if(gatewayPasswordResult == null){
                    gatewayPassword.setOrderNo(orderNo);  
                    gatewayPassword.setAdslAccount((String) returnMap.get("adslAccount"));
                    int i = gatewayPasswordMapper.insert(gatewayPassword);
                    logger.info("invoke newInstallation，写入password结果，i:{}",i);
                }else {
                	retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_UNMATCH);
                    retDataMap.put(Constant.PBOSS_FAILREASON,"网关已经开通宽带,不能重复开通");
                    return  retMap;
				}
                
            }
            
            //根据password查询网关信息
            GatewayInfo gatewayInfoParam = new GatewayInfo();
            gatewayInfoParam.setGatewayPassword(password);
            GatewayInfo gatewayInfoResult = gatewayInfoMapper.selectGatewayInfo(gatewayInfoParam);
            if(gatewayInfoResult != null){
                gatewayInfo = gatewayInfoResult;
            }else {
                logger.info("根据password没有查询到对应的网关信息,password:{}",password);
            }
            
            //如果有工单是成功状态，则需要写入工单开通明细表
            if(null!=gatewayInfo) {
                for (GatewayBusiness gatewayBus : listGb) {
                    if ("andmu".equals(gatewayBus.getBusinessCodeBoss())) {
                    	dealAndmu(gatewayBus, gatewayInfo);
                    }
                }
            }

            //手动提交事物
            DataSourceTransactionManager txManager = (DataSourceTransactionManager) ctx.getBean("txManager");
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus txStatus = txManager.getTransaction(def);// 获得事务状态
            //工单入库
            int addGatewayBusiness = gatewayBusinessMapper.batchInsert(listGb);
            txManager.commit(txStatus);
            logger.info("invoke newInstallation，写入工单结果,addGatewayBusiness:{}",addGatewayBusiness);

            try {
                //执行工单
                if (gatewayInfo != null&&"已绑定".equals(gatewayInfo.getGatewayStatus())) {
                    WorkerThreadPool.getInstance().submitTask(new ExcuteWorkOrderThread(gatewayInfo));
                }
            }catch (Exception e){
                logger.error("invoke newInstallation excuteWorkOrder fail,e:{}",e);
            }

        }catch (Exception e){
            logger.error("invoke newInstallation fail,e:{}",e);
            retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_FAIL);
            retDataMap.put(Constant.PBOSS_FAILREASON,"服务器内部错误");
        }
        logger.info("end invoke newInstallation,retMap:{}", retMap);
        return retMap;
    }

    
    /**
     * @param gatewayBus
     * @param gatewayInfo
     * andmu工单处理
     */
    private void dealAndmu(GatewayBusiness gatewayBus,GatewayInfo gatewayInfo){
    	gatewayBus.setBusinessStatu("1");
        //先查询，如果有就更新，如果没有就插入
        GatewayBusinessOpenDetail gatewayBusinessOpenDetailParam = new GatewayBusinessOpenDetail();
        gatewayBusinessOpenDetailParam.setBusinessCodeBoss(gatewayBus.getBusinessCodeBoss());
        gatewayBusinessOpenDetailParam.setGatewayUuid(gatewayInfo.getGatewayUuid());
        GatewayBusinessOpenDetail gatewayBusinessOpenDetailResult = gatewayBusinessOpenDetailMapper.selectByParam(gatewayBusinessOpenDetailParam);
        if (gatewayBusinessOpenDetailResult == null) {
            GatewayBusinessOpenDetail gatewayBusinessOpenDetail = new GatewayBusinessOpenDetail();
            gatewayBusinessOpenDetail.setId(UniqueUtil.uuid());
            gatewayBusinessOpenDetail.setBusinessCodeBoss(gatewayBus.getBusinessCodeBoss());
            gatewayBusinessOpenDetail.setOrderNo(gatewayBus.getOrderNo());
            gatewayBusinessOpenDetail.setGatewayUuid(gatewayInfo.getGatewayUuid());
            gatewayBusinessOpenDetail.setOpenStatus("1");
            int n = gatewayBusinessOpenDetailMapper.insert(gatewayBusinessOpenDetail);
            logger.info("invoke newInstallation，写入工单开通工单明细结果,n:{},gatewayBusinessOpenDetail:{}", n, gatewayBusinessOpenDetail);
        } else {
            if (!"1".equals(gatewayBusinessOpenDetailResult.getOpenStatus())) {
                gatewayBusinessOpenDetailResult.setOpenStatus("1");
                gatewayBusinessOpenDetailResult.setOrderNo(gatewayBus.getOrderNo());
                int n = gatewayBusinessOpenDetailMapper.updateByPrimaryKey(gatewayBusinessOpenDetailResult);
                logger.info("invoke newInstallation，更新工单开通工单明细结果,n:{},gatewayBusinessOpenDetailResult:{}", n, gatewayBusinessOpenDetailResult);
            }
        }
        String pppoeAccount =  gatewayBus.getAdslAccount();
        String AreaCode = gatewayBus.getAreacode();
        if ((!StringUtils.isEmpty(pppoeAccount))||(!StringUtils.isEmpty(AreaCode))) {
            GatewayInfo updateGatewayInfo = new GatewayInfo();
            updateGatewayInfo.setGatewayUuid(gatewayInfo.getGatewayUuid());
            if(!StringUtils.isEmpty(pppoeAccount)) {
                updateGatewayInfo.setGatewayAdslAccount(pppoeAccount);
            }
            if(!StringUtils.isEmpty(AreaCode)) {
                updateGatewayInfo.setGatewayAreaId(AreaCode);
            }
            gatewayInfoMapper.updateByPrimaryKeySelective(updateGatewayInfo);
        }
        //添加执行历史
        GatewayBusinessExecuteHistory gheh = new GatewayBusinessExecuteHistory();
        gheh.setOrderNo(gatewayBus.getOrderNo());
        gheh.setExecuteStatus(1);
        gheh.setExecuteTime(DateTools.getCurrentSecondTime());
        //写操作记录
        gatewayBusinessExecuteHistoryMapper.insert(gheh);
    }
    
    
    /**
     * 解析参数
     * @param installationList
     * @return
     */
    public Map<String,Object> parseParam( List<Map> installationList ) {
        Map<String, Object> returnMap = new HashMap<>();
        //先解析参数
        String password = null;
        String passwordOrderNo = null;
        String adslAccount = null;
        String code = null;
        List<GatewayBusiness> listGb = new ArrayList<>();
        List<String> listOrderNo = new ArrayList<>();
        //查询业务代码，用来获取业务名称
        List<BusinessCategory> listBusinessCategory = businessCategoryMapper.findAllGatewayBusiness();
        //实际上只有一条工单
        for (Map item : installationList) {
            GatewayBusiness gatewayBusiness = new GatewayBusiness();
            Map<String,Object> parmMap = new HashMap<>();
            String orderNo = (String) item.get("OrderNo");
            if (StringUtils.isEmpty(orderNo)) {
                logger.info("orderNo为空");
                returnMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                returnMap.put(Constant.PBOSS_FAILREASON, "工单号不能为空");
                return returnMap;
            }else {
            	//验证工单号是否存在
            	listOrderNo.add(orderNo);
                List<GatewayBusiness> gatewayBusinessesResult = gatewayBusinessMapper.selectByOrderNos(listOrderNo);
                if(gatewayBusinessesResult != null && gatewayBusinessesResult.size()>0){
                    returnMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_VARIFICATION_FAIL);
                    returnMap.put(Constant.PBOSS_FAILREASON,"工单号已经存在:" + orderNo);
                    return  returnMap;
                }
			}
            
            gatewayBusiness.setOrderNo(orderNo);
            String ServiceCode = (String) item.get("ServiceCode");
            code = ServiceCode;
            if (StringUtils.isEmpty(ServiceCode)) {
                logger.info("ServiceCode为空");
                returnMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                returnMap.put(Constant.PBOSS_FAILREASON, "业务代码不能为空");
                return returnMap;
            }

            for(BusinessCategory itemCategory:listBusinessCategory) {
                if (ServiceCode.equals(itemCategory.getBusinessCode())) {
                    gatewayBusiness.setBusinessName(itemCategory.getBusinessName());
                    break;
                }
            }
            //验证ServiceCode是否合法
            boolean isServiceCodeLegal = false;
            List<BusinessCode> businessCodes = businessCodeMapper.seletAll();
            for(BusinessCode businessCode:businessCodes){
                if(ServiceCode.equals(businessCode.getBusinessCodeBoss())){
                    isServiceCodeLegal = true;
                    break;
                }
            }
            if (!isServiceCodeLegal) {
                logger.info("ServiceCode不合法,ServiceCode:{}", ServiceCode);
                returnMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_VARIFICATION_FAIL);
                returnMap.put(Constant.PBOSS_FAILREASON, "业务代码不合法");
                return returnMap;
            }
            gatewayBusiness.setBusinessCode(ServiceCode);
            gatewayBusiness.setBusinessCodeBoss(ServiceCode);
            
            String loid = (String) item.get("LOID");
            if (StringUtils.isEmpty(loid)) {
                returnMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                returnMap.put(Constant.PBOSS_FAILREASON, "网关逻辑标识符不能为空");
                return returnMap;
            }else {
            	password = loid;
                passwordOrderNo = orderNo;
                gatewayBusiness.setGatewayPassword(loid);
			}
            
            String areCode = (String) item.get("AreaCode");
            if (StringUtils.isEmpty(areCode)) {
            	logger.info("areCode不能为空");
                returnMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_VARIFICATION_FAIL);
                returnMap.put(Constant.PBOSS_FAILREASON, "区域编码不能为空");
                return returnMap;
			}else{
				Integer area = areaService.findIdByCode(areCode);
                if(null == area ){
                    logger.info("areCode不合法");
                    returnMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_VARIFICATION_FAIL);
                    returnMap.put(Constant.PBOSS_FAILREASON, "区域编码不合法");
                    return returnMap;
                }
                gatewayBusiness.setAreacode(areCode);
			}
           
            String pppoeAccount = (String) item.get("PppoeAccount");
            if (!StringUtils.isEmpty(pppoeAccount)) {
                gatewayBusiness.setAdslAccount(pppoeAccount);
                parmMap.put("Username",pppoeAccount);
            }
            
            String pppoePassword = (String) item.get("PppoePassword");
            if (!StringUtils.isEmpty(pppoePassword)) {
                gatewayBusiness.setAdslPassword(pppoePassword);
                parmMap.put("Password",pppoePassword);
            }
            
            if ("wband".equals(ServiceCode)) {
                if (StringUtils.isEmpty(pppoeAccount)) {
                    logger.info("ServiceCode为wabnd时PppoeAccount为空");
                    returnMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                    returnMap.put(Constant.PBOSS_FAILREASON, "开网时，宽带账号不能为空");
                    return returnMap;
                }
                
                if (StringUtils.isEmpty(pppoePassword)) {
                    logger.info("ServiceCode为wabnd时PppoePassword为空");
                    returnMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                    returnMap.put(Constant.PBOSS_FAILREASON, "开网时，宽带密码不能为空");
                    return returnMap;
                }
                
                //判断宽带账号是否被其他loid的工单占用了 
                GatewayPassword gatewayPassword = new GatewayPassword();
                gatewayPassword.setAdslAccount(pppoeAccount);;
                GatewayPassword gatewayPasswordResult = gatewayPasswordMapper.selectBySelective(gatewayPassword);
                //没有被占用
                if (gatewayPasswordResult == null) {
                	adslAccount = pppoeAccount;
				}else {
					logger.info("PppoeAccount账号" + pppoeAccount + "已经被其他loid" + gatewayPasswordResult.getGatewayPassword()+"使用,当前loid是" + loid);
                    returnMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_VARIFICATION_FAIL);
                    returnMap.put(Constant.PBOSS_FAILREASON, "宽带账号重复");
                    return returnMap;
				}
            }
            
            String SIPUserName = (String) item.get("SIPUserName");
            if (!StringUtils.isEmpty(SIPUserName)) {
                parmMap.put("SIP.AuthUserName",SIPUserName);
            }
            
            String SIPUserPWD = (String) item.get("SIPUserPWD");
            if (!StringUtils.isEmpty(SIPUserPWD)) {
                parmMap.put("SIP.AuthPassword",SIPUserPWD);
            }
            
            if ("voip".equals(ServiceCode)) {
                if (StringUtils.isEmpty(SIPUserName)) {
                    logger.info("ServiceCode为voip时SIPUserName为空");
                    returnMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                    returnMap.put(Constant.PBOSS_FAILREASON, "开通语音业务时，SIPUserName不能为空");
                    return returnMap;
                }
                
                if (StringUtils.isEmpty(SIPUserPWD)) {
                    logger.info("ServiceCode为voip时SIPUserPWD为空");
                    returnMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                    returnMap.put(Constant.PBOSS_FAILREASON, "开通语音业务时，SIPUserPWD不能为空");
                    return returnMap;
                }
            }

            String BANDWIDTH = (String) item.get("BANDWIDTH");
            if (!StringUtils.isEmpty(BANDWIDTH)) {
                gatewayBusiness.setBandwidth(Integer.valueOf(BANDWIDTH));
            }
            
            //江苏有vlanId
            String vlanId = (String) item.get("vlanId");
            if (!StringUtils.isEmpty(vlanId)) {
            	parmMap.put("VLANID",vlanId);
            }
            //江苏otttv有vlanIdTv
            String vlanIdTv = (String) item.get("vlanId_tv");
            if (!StringUtils.isEmpty(vlanId)) {
            	parmMap.put("VLANIDTV",vlanIdTv);
            }
            
            gatewayBusiness.setBusinessStatu("0");
            gatewayBusiness.setGatewayPassword(loid);
            gatewayBusiness.setId(UniqueUtil.uuid());
            gatewayBusiness.setParameterList(JSON.toJSONString(parmMap));
            gatewayBusiness.setCreateTime(DateTools.getCurrentSecondTime());
            gatewayBusiness.setBusinessType("1");
            listGb.add(gatewayBusiness);
        }
        
        
        returnMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_SUCCESS);
        returnMap.put("orderNo",passwordOrderNo);
        returnMap.put("password", password);
        returnMap.put("serviceCode", code);
        returnMap.put("listGb", listGb);
        returnMap.put("adslAccount", adslAccount);
        
        return returnMap;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,readOnly=false)
    public Map<String, Object> broadBandUnsubcribe(Map<String, Object> parameter){
        logger.info("start invoke broadBandUnsubcribe,parameter:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        Map<String,Object> retDataMap = new HashMap<>();
        String CmdType = (String) parameter.get("CmdType");
        retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_SUCCESS);
        retMap.put(Constant.PBOSS_CMDTYPE,CmdType);
        retMap.put(Constant.PBOSS_RESULTDATA,retDataMap);
        try {
            Map<String, Object> paraMap = (Map<String, Object>) parameter.get(Constant.PBOSS_PARAMETER);
            //解析参数
            String PppoeAccount = (String) paraMap.get("PppoeAccount");
            String OrderNo = (String) paraMap.get("OrderNo");
            String ServiceCode = (String) paraMap.get("ServiceCode");
            if(StringUtils.isEmpty(PppoeAccount)){
                logger.info("PppoeAccount为空");
                retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                retDataMap.put(Constant.PBOSS_FAILREASON, "PppoeAccount is null");
                return retMap;
            }
            if(StringUtils.isEmpty(OrderNo)){
                logger.info("OrderNo为空");
                retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                retDataMap.put(Constant.PBOSS_FAILREASON, "OrderNo is null");
                return retMap;
            }
            if(StringUtils.isEmpty(ServiceCode)){
                logger.info("ServiceCode为空");
                retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
                retDataMap.put(Constant.PBOSS_FAILREASON, "ServiceCode is null");
                return retMap;
            }
            //验证ServiceCode是否合法
            boolean isServiceCodeLegal = false;
            List<BusinessCode> businessCodes = businessCodeMapper.seletAll();
            for(BusinessCode businessCode:businessCodes){
                if(ServiceCode.equals(businessCode.getBusinessCodeBoss())){
                    isServiceCodeLegal = true;
                    break;
                }
            }
            if (!isServiceCodeLegal) {
                logger.info("ServiceCode不合法,ServiceCode:{}", ServiceCode);
                retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_VARIFICATION_FAIL);
                retDataMap.put(Constant.PBOSS_FAILREASON, "ServiceCode is illegal");
                return retMap;
            }
            List<String> listOrderNo = new ArrayList<>();
            listOrderNo.add(OrderNo);
            //验证工单号是否存在
            List<GatewayBusiness> gatewayBusinessesResult = gatewayBusinessMapper.selectByOrderNos(listOrderNo);
            if(gatewayBusinessesResult != null && gatewayBusinessesResult.size()>0){
                retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_VARIFICATION_FAIL);
                retDataMap.put(Constant.PBOSS_FAILREASON,"OrderNo has existed");
                return  retMap;
            }

            //根据PppoeAccount查询网关信息
            GatewayPassword gatewayPassword = new GatewayPassword();
            gatewayPassword.setAdslAccount(PppoeAccount);;
            GatewayPassword gatewayPasswordResult = gatewayPasswordMapper.selectBySelective(gatewayPassword);
            if(gatewayPasswordResult == null){
                logger.info("根据PppoeAccount没有查询到对应的网关密码信息,PppoeAccount:{}",PppoeAccount);
                retMap.put(Constant.PBOSS_RESULT,0);
                retDataMap.put(Constant.PBOSS_FAILREASON,"");
                return  retMap;
            }
            //查询业务代码，用来验证工单所传业务代码是否合法
            List<BusinessCategory> listBusinessCategory = businessCategoryMapper.findAllGatewayBusiness();
            GatewayInfo gatewayInfoParam = new GatewayInfo();
            gatewayInfoParam.setGatewayAdslAccount(PppoeAccount);
            //新装工单执行成功了才有绑定的网关
            GatewayInfo gatewayInfoResult = gatewayInfoMapper.selectGatewayInfo(gatewayInfoParam);
            
            GatewayBusiness gatewayBusiness = new GatewayBusiness();
            gatewayBusiness.setId(UniqueUtil.uuid());
            if (gatewayInfoResult != null) {
            	gatewayBusiness.setGatewayUuid(gatewayInfoResult.getGatewayUuid());
			}
            gatewayBusiness.setBusinessCodeBoss(ServiceCode);
            gatewayBusiness.setOrderNo(OrderNo);
            gatewayBusiness.setAdslAccount(PppoeAccount);
            //必需设置 查询关联应用
            gatewayBusiness.setGatewayPassword(gatewayPasswordResult.getGatewayPassword());
            gatewayBusiness.setBusinessType("8");
            //需要执行模板
            gatewayBusiness.setBusinessStatu("0");
            
            
            GatewayBusiness installBusiness = new GatewayBusiness();
            installBusiness.setGatewayPassword(gatewayPasswordResult.getGatewayPassword());
            installBusiness.setBusinessType("1");
            installBusiness.setBusinessStatu("4");
            installBusiness.setBusinessCodeBoss(ServiceCode);
            List<GatewayBusiness> installBusinessList = gatewayBusinessMapper.selectExist(installBusiness);
            if (installBusinessList!=null&&!installBusinessList.isEmpty()) {
            	gatewayBusiness.setAreacode(installBusinessList.get(0).getAreacode());
			}
            
            if("wband".equals(ServiceCode)){
                gatewayBusiness.setBusinessCode("unwband");
                //不需要执行模板
                gatewayBusiness.setBusinessStatu("4");
			} else if ("otttv".equals(ServiceCode)) {
				gatewayBusiness.setBusinessCode("unotttv");
			} else if ("voip".equals(ServiceCode)) {
				gatewayBusiness.setBusinessCode("unvoip");
			} else if ("andmu".equals(ServiceCode)) {
				gatewayBusiness.setBusinessCode("unandmu");
			}

            for(BusinessCategory itemCategory:listBusinessCategory) {
                if (gatewayBusiness.getBusinessCode().equals(itemCategory.getBusinessCode())) {
                    gatewayBusiness.setBusinessName(itemCategory.getBusinessName());
                    break;
                }
            }
            gatewayBusiness.setCreateTime(DateTools.getCurrentSecondTime());
            
            //如果是拆wband,由于都恢复出厂设置了，所以所有业务都改为未开通
            if("wband".equals(ServiceCode)){
            	gatewayBusinessMapper.insertSelective(gatewayBusiness);
            	
            	//删除t_gateway_password的记录
                int m = gatewayPasswordMapper.deleteByPrimaryKey(gatewayPasswordResult.getGatewayPassword());
                logger.info(" invoke broadBandUnsubcribe删除t_gateway_password的记录,m:{},PppoeAccount:{}",m,PppoeAccount);
                
                //将该LOID对应的工单都置为 作废  根据password或宽带账号
                gatewayBusinessMapper.updateStatusByPassword(gatewayBusiness);
                
                //绑定了网关-->解绑网关
                if (gatewayInfoResult != null) {
                	
                	GatewayBusinessOpenDetail gatewayBusinessOpenDetail = new GatewayBusinessOpenDetail();
                    gatewayBusinessOpenDetail.setGatewayUuid(gatewayInfoResult.getGatewayUuid());
                    gatewayBusinessOpenDetail.setOpenStatus("0");
                    int n = gatewayBusinessOpenDetailMapper.updateByGatewayUuid(gatewayBusinessOpenDetail);
                    logger.info("invoke newInstallation，更新工单开通工单明细结果,n:{}", n);
                	
                	GatewayInfo updateGatewayInfo = new GatewayInfo();
                    updateGatewayInfo.setGatewayUuid(gatewayInfoResult.getGatewayUuid());
                    int k = gatewayInfoMapper.unBindPasswordAndAdslAccount(updateGatewayInfo);
                    logger.info(" invoke broadBandUnsubcribe,解除password和adsl与网关的绑定关系,k:{}",k);
                    
                    //通知BMS，调用一级平台销户接口
                    //1.用户恢复出厂设置   2.首次认证   3.固件升级    4.工单拆机恢复出厂设置 ，5销户
                   try {
					Map<String, Object> bmsResult =  gatewayHandleService.factoryNotify(gatewayInfoResult.getGatewayMacaddress(), "4", false);
					logger.info("拆机通知BMS结果,GatewayMacaddress:{},执行结果:{},结果描述:{}", gatewayInfoResult.getGatewayMacaddress(), bmsResult.get("resultCode"), bmsResult.get("resultMsg"));
					logger.info("拆机通知BMS结果,由于不需要等待BMS的处理，所以这里想当时异步处理，所以执行结果为空");
                   } catch (Exception e) {
						//logger.error("拆机通知BMS异常", e);
					}
                    
    			}
                
            }else {
            	 //手动提交事物
                DataSourceTransactionManager txManager = (DataSourceTransactionManager) ctx.getBean("txManager");
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                TransactionStatus txStatus = txManager.getTransaction(def);// 获得事务状态
                //工单入库
                gatewayBusinessMapper.insertSelective(gatewayBusiness);
                txManager.commit(txStatus);
            	
            	if (gatewayInfoResult != null&&"已绑定".equals(gatewayInfoResult.getGatewayStatus())) {
            		
            		WorkerThreadPool.getInstance().submitTask(new ExcuteWorkOrderThread(gatewayInfoResult));
				}else {
					//未绑定的 直接作废工单 根据password和业务账号  将该业务对应的工单改为 作废
					gatewayBusiness.setBusinessStatu("4");
			        gatewayBusinessMapper.updateStatusByPwdAndBusiness(gatewayBusiness);
				}
            }
            
            retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_SUCCESS);
        }catch (Exception e){
            logger.error("invoke broadBandUnsubcribe fail,e:{}",e);
            retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_FAIL);
        }
        
        return retMap;
    }

   
	@Override
	public Map<String, Object>  customerRequestStop(Map<String, Object> parameter) {
		logger.info("start invoke customerRequestStop:{}",parameter);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("CmdType", parameter.get("CmdType"));
		
		try {
			Map<String, Object> param = (Map<String, Object>) parameter.get("Parameter");
			if(param.get("OrderNo") == null || "".equals(param.get("OrderNo"))){
				returnMap.put("Result", -4);
				Map<String, Object> reason = new HashMap<String, Object>();
				reason.put("FailReason", "OrderNo为空");
				returnMap.put("ResultData", reason);
				return returnMap;
			}
			String orderNo = param.get("OrderNo").toString();
			String serviceCode = param.get("ServiceCode").toString();
			String PppoeAccount = param.get("PppoeAccount") == null ? "": param.get("PppoeAccount").toString();
			if("".equals(PppoeAccount)){
				returnMap.put("Result", -4);
				Map<String, Object> reason = new HashMap<String, Object>();
				reason.put("FailReason", "PppoeAccount为空");
				returnMap.put("ResultData", reason);
				return returnMap;
			}
			
			 //验证工单号是否存在
			List<String> orders = new ArrayList<String>();
			orders.add(orderNo);
            List<GatewayBusiness> gatewayBusinessesResult = gatewayBusinessMapper.selectByOrderNos(orders);
            if(gatewayBusinessesResult != null && gatewayBusinessesResult.size()>0){
            	returnMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_VARIFICATION_FAIL);
            	returnMap.put(Constant.PBOSS_FAILREASON,"OrderNo has existed");
                return  returnMap;
            }
			
            //根据宽带账号查询网关是否存在
    	    GatewayInfo gatewayInfoParam = new GatewayInfo();
            gatewayInfoParam.setGatewayAdslAccount(PppoeAccount);
            GatewayInfo gatewayInfoResult = gatewayInfoMapper.selectGatewayInfo(gatewayInfoParam);
            if(gatewayInfoResult == null){
                logger.info("根据PppoeAccount没有查询到对应的网关信息,PppoeAccount:{}",PppoeAccount);
                returnMap.put("Result", 0);
				Map<String, Object> reason = new HashMap<String, Object>();
				returnMap.put("ResultData", reason);
				return returnMap;
            }
            
			GatewayBusiness record = new GatewayBusiness();
			record.setId(UniqueUtil.uuid());
			record.setAdslAccount(PppoeAccount);
			record.setOrderNo(orderNo);
			record.setBusinessType("4");
			record.setBusinessCodeBoss(serviceCode);
			record.setBusinessCode(parameter.get("CmdType").toString().replace("_", ""));
			record.setBusinessStatu("1");
			BusinessCategory category = new BusinessCategory();
			category.setBusinessCode(parameter.get("CmdType").toString().replace("_", ""));
			List<Map<String, Object>> businessCategoryList = businessCategoryMapper.queryList(category);
			if(businessCategoryList != null && businessCategoryList.size() > 0){
				record.setBusinessName(businessCategoryList.get(0).get("businessName") == null ? "" :businessCategoryList.get(0).get("businessName").toString());
			}
			record.setCreateTime((int) (System.currentTimeMillis() /1000));
			record.setGatewayUuid(gatewayInfoResult.getGatewayUuid());
			record.setGatewayPassword(gatewayInfoResult.getGatewayPassword());
			record.setAreacode(gatewayInfoResult.getGatewayAreaId());
			
			//更新网关状态为 客户申请停机
			GatewayInfo gateway = new GatewayInfo();
			gateway.setGatewayAdslAccount(PppoeAccount);
			gateway.setBusinessStatus("4");
			
			gatewayInfoMapper.updateGatewayBusinessStatusByAdsl(gateway);
			gatewayBusinessMapper.insertSelective(record);
			
			returnMap.put("Result", 0);
			Map<String, Object> reason = new HashMap<String, Object>();
			returnMap.put("ResultData", reason);
			return returnMap;
			
		} catch (Exception e) {
			logger.info("start invoke customerRequestStop exception:{}",e);
			returnMap.put("Result", -1);
			Map<String, Object> reason = new HashMap<String, Object>();
			reason.put("FailReason", "服务器内部错误");
			returnMap.put("ResultData", reason);
			return returnMap;
		}
		
	}
	@Override
	public Map<String, Object>  customerRequestResume(Map<String, Object> parameter) {
		logger.info("start invoke customerRequestResume:{}",parameter);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("CmdType", parameter.get("CmdType"));
		
		try {
			Map<String, Object> param = (Map<String, Object>) parameter.get("Parameter");
			if(param.get("OrderNo") == null || "".equals(param.get("OrderNo"))){
				returnMap.put("Result", -4);
				Map<String, Object> reason = new HashMap<String, Object>();
				reason.put("FailReason", "OrderNo为空");
				returnMap.put("ResultData", reason);
				return returnMap;
			}
			String orderNo = param.get("OrderNo").toString();
			String serviceCode = param.get("ServiceCode").toString();
			String PppoeAccount = param.get("PppoeAccount") == null ? "": param.get("PppoeAccount").toString();
			if("".equals(PppoeAccount)){
				returnMap.put("Result", -4);
				Map<String, Object> reason = new HashMap<String, Object>();
				reason.put("FailReason", "PppoeAccount为空");
				returnMap.put("ResultData", reason);
				return returnMap;
			}
			
			 //验证工单号是否存在
			List<String> orders = new ArrayList<String>();
			orders.add(orderNo);
            List<GatewayBusiness> gatewayBusinessesResult = gatewayBusinessMapper.selectByOrderNos(orders);
            if(gatewayBusinessesResult != null && gatewayBusinessesResult.size()>0){
            	returnMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_VARIFICATION_FAIL);
            	returnMap.put(Constant.PBOSS_FAILREASON,"OrderNo has existed");
                return  returnMap;
            }
			
            //根据宽带账号查询网关是否存在
            GatewayInfo gatewayInfoParam = new GatewayInfo();
            gatewayInfoParam.setGatewayAdslAccount(PppoeAccount);
            GatewayInfo gatewayInfoResult = gatewayInfoMapper.selectGatewayInfo(gatewayInfoParam);
            if(gatewayInfoResult == null){
                logger.info("根据PppoeAccount没有查询到对应的网关信息,PppoeAccount:{}",PppoeAccount);
                returnMap.put("Result", 0);
				Map<String, Object> reason = new HashMap<String, Object>();
				returnMap.put("ResultData", reason);
				return returnMap;
            }
            
			GatewayBusiness record = new GatewayBusiness();
			record.setId(UniqueUtil.uuid());
			record.setAdslAccount(PppoeAccount);
			record.setOrderNo(orderNo);
			record.setBusinessType("5");
			record.setBusinessCodeBoss(serviceCode);
			record.setBusinessCode(parameter.get("CmdType").toString().replace("_", ""));
			record.setBusinessStatu("1");
			BusinessCategory category = new BusinessCategory();
			category.setBusinessCode(parameter.get("CmdType").toString().replace("_", ""));
			List<Map<String, Object>> businessCategoryList = businessCategoryMapper.queryList(category);
			if(businessCategoryList != null && businessCategoryList.size() > 0){
				record.setBusinessName(businessCategoryList.get(0).get("businessName") == null ? "" :businessCategoryList.get(0).get("businessName").toString());
			}
			record.setCreateTime((int) (System.currentTimeMillis() /1000));
			
			record.setGatewayUuid(gatewayInfoResult.getGatewayUuid());
			record.setGatewayPassword(gatewayInfoResult.getGatewayPassword());
			record.setAreacode(gatewayInfoResult.getGatewayAreaId());
			//更新网关状态为复机状态
			GatewayInfo gateway = new GatewayInfo();
			gateway.setGatewayAdslAccount(PppoeAccount);
			gateway.setBusinessStatus("5");
			
			gatewayInfoMapper.updateGatewayBusinessStatusByAdsl(gateway);
			gatewayBusinessMapper.insertSelective(record);
			
			returnMap.put("Result", 0);
			Map<String, Object> reason = new HashMap<String, Object>();
			returnMap.put("ResultData", reason);
			return returnMap;
			
		} catch (Exception e) {
			logger.info("start invoke customerRequestResume exception:{}",e);
			returnMap.put("Result", -1);
			Map<String, Object> reason = new HashMap<String, Object>();
			reason.put("FailReason", "服务器内部错误");
			returnMap.put("ResultData", reason);
			return returnMap;
		}
		
	}
	@Override
	public Map<String, Object>  changePPPoEPassword(Map<String, Object> parameter) {
		logger.info("start invoke changePPPoEPassword:{}",parameter);
		Map<String, Object> returnMap = new HashMap<String, Object>();
		returnMap.put("CmdType", parameter.get("CmdType"));
		
		try {
			Map<String, Object> param = (Map<String, Object>) parameter.get("Parameter");
			if(param.get("OrderNo") == null || "".equals(param.get("OrderNo"))){
				returnMap.put("Result", -4);
				Map<String, Object> reason = new HashMap<String, Object>();
				reason.put("FailReason", "OrderNo为空");
				returnMap.put("ResultData", reason);
				return returnMap;
			}
			String orderNo = param.get("OrderNo").toString();
			String PppoeAccount = param.get("PppoeAccount") == null ? "": param.get("PppoeAccount").toString();
			String PppoePassword = param.get("PppoePassword") == null ? "": param.get("PppoePassword").toString();
			if("".equals(PppoeAccount)){
				returnMap.put("Result", -4);
				Map<String, Object> reason = new HashMap<String, Object>();
				reason.put("FailReason", "PppoeAccount为空");
				returnMap.put("ResultData", reason);
				return returnMap;
			}
			if("".equals(PppoePassword)){
				returnMap.put("Result", -4);
				Map<String, Object> reason = new HashMap<String, Object>();
				reason.put("FailReason", "PppoePassword为空");
				returnMap.put("ResultData", reason);
				return returnMap;
			}
			
			 //验证工单号是否存在
			List<String> orders = new ArrayList<String>();
			orders.add(orderNo);
            List<GatewayBusiness> gatewayBusinessesResult = gatewayBusinessMapper.selectByOrderNos(orders);
            if(gatewayBusinessesResult != null && gatewayBusinessesResult.size()>0){
            	returnMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_VARIFICATION_FAIL);
            	returnMap.put(Constant.PBOSS_FAILREASON,"OrderNo has existed");
                return  returnMap;
            }
            
            
            GatewayPassword gp = new GatewayPassword();
            gp.setAdslAccount(PppoeAccount);
            GatewayPassword selectedGP = gatewayPasswordMapper.selectBySelective(gp);
            if(selectedGP == null){
            	
            	logger.info("根据PppoeAccount没有查询到对应的网关信息,PppoeAccount:{}",PppoeAccount);
                returnMap.put("Result", 0);
    			Map<String, Object> reason = new HashMap<String, Object>();
    			returnMap.put("ResultData", reason);
                return  returnMap;
            }
            
            GatewayBusiness wbandBusiness = new GatewayBusiness();
            wbandBusiness.setGatewayPassword(selectedGP.getGatewayPassword());
            wbandBusiness.setBusinessStatu("4");
            wbandBusiness.setBusinessType("1");
            wbandBusiness.setBusinessCodeBoss("wband");
            List<GatewayBusiness> wbandBusinessList = gatewayBusinessMapper.selectExist(wbandBusiness);
            
			GatewayBusiness record = new GatewayBusiness();
			String uuid = UniqueUtil.uuid();
			record.setId(uuid);
			record.setAdslAccount(PppoeAccount);
			record.setAdslPassword(PppoePassword);
			record.setOrderNo(orderNo);
			record.setBusinessType("6");
			record.setBusinessCode(parameter.get("CmdType").toString().replace("_", ""));
            record.setCreateTime((int) (System.currentTimeMillis() / 1000));
            if (wbandBusinessList!=null&&!wbandBusinessList.isEmpty()) {
            	record.setAreacode(wbandBusinessList.get(0).getAreacode());
			}
            
            
            BusinessCategory category = new BusinessCategory();
			category.setBusinessCode(parameter.get("CmdType").toString().replace("_", ""));
			List<Map<String, Object>> businessCategoryList = businessCategoryMapper.queryList(category);
			if(businessCategoryList != null && businessCategoryList.size() > 0){
				record.setBusinessName(businessCategoryList.get(0).get("businessName") == null ? "" :businessCategoryList.get(0).get("businessName").toString());
			}
            
			record.setGatewayPassword(selectedGP.getGatewayPassword());
			Map<String, Object> excuteParamMap = new HashMap<String, Object>();
            excuteParamMap.put("PppoePassword", PppoePassword);

            record.setParameterList(com.alibaba.fastjson.JSONObject.toJSONString(excuteParamMap));

            gatewayBusinessMapper.insertSelective(record);
			
            
            GatewayInfo gatewayInfoParam = new GatewayInfo();
            gatewayInfoParam.setGatewayPassword(selectedGP.getGatewayPassword());  
            GatewayInfo gatewayInfoResult = gatewayInfoMapper.selectGatewayInfo(gatewayInfoParam);
            
            if(gatewayInfoResult == null||!"已绑定".equals(gatewayInfoResult.getGatewayStatus())){
            	logger.info("根据PppoeAccount查询到对应的网关信息,但是网关状态为非绑定状态，不执行工单,PppoeAccount:{}",PppoeAccount);
                returnMap.put("Result", 0);
    			Map<String, Object> reason = new HashMap<String, Object>();
    			returnMap.put("ResultData", reason);
                return  returnMap;
            }
            
			//下发指令
			Map<String,Object> paMap = new HashMap<>();
            paMap.put("businessCode", parameter.get("CmdType").toString().replace("_", ""));
            paMap.put("gatewayMacaddress", gatewayInfoResult.getGatewayMacaddress());
            //先查询匹配固件，硬件，生产商，设备型号的模板，如果没有则查询默认模板
            Map selectResultMap = workOrderTemplateInfoMapper.selectByBusinessCode(paMap);
            if(null == selectResultMap){
                selectResultMap = workOrderTemplateInfoMapper.selectDefaultTemplate(paMap);
            }
            if (null != selectResultMap){
                String template_message = (String) selectResultMap.get("template_message");
                if (!StringUtils.isEmpty(template_message)) {
                    Map<String, String> resultMap = new HashMap<>();
                    GatewayBusiness gb = gatewayBusinessMapper.selectByPrimaryKey(uuid);
                    String parameterList = gb.getParameterList();
                    Map<String, Object> parameterMap = JSON.parseObject(parameterList, Map.class);
                    parameterMap.put("gateWayMac", gatewayInfoResult.getGatewayMacaddress());
                    //执行之前把工单状态改为执行中
                    gb.setBusinessStatu("3");
                    gatewayBusinessMapper.updateByPrimayKey(gb);

                    resultMap = busiOperation.excute(template_message, parameterMap);
//                        resultMap.put("MSG_CODE", "1");
                    int executeStatus = 2;
                    if(resultMap != null && !resultMap.isEmpty() &&  "0".equals(resultMap.get("MSG_CODE"))){
    					//下发成功
    					record.setBusinessStatu("1");
    					executeStatus =1;
    				}else{
    					record.setBusinessStatu("2");
    					record.setFailCount(1);	
    					executeStatus =2;
    				}
                    
                    //记录下发指令记录
                    GatewayBusinessExecuteHistory gheh = new GatewayBusinessExecuteHistory();
                    gheh.setOrderNo(gb.getOrderNo());
                    gheh.setExecuteTime(DateTools.getCurrentSecondTime());
                    gheh.setExecuteStatus(executeStatus);
                    //写操作记录
                    gatewayBusinessExecuteHistoryMapper.insert(gheh);

                } else {
                    logger.info("工单的模板类容为空,gateWayPassword:{},businessCode:{}", gatewayInfoResult.getGatewayPassword(), gatewayInfoResult.getBusinessCode());
                }
            } else {
                logger.info("工单的模板类容为空,gateWayPassword:{},businessCode:{}", gatewayInfoResult.getGatewayPassword(), gatewayInfoResult.getBusinessCode());
	        }
			
			
			returnMap.put("Result", 0);
			Map<String, Object> reason = new HashMap<String, Object>();
			returnMap.put("ResultData", reason);
			return returnMap;
			
		} catch (Exception e) {
			logger.info("start invoke changePPPoEPassword exception:{}",e);
			returnMap.put("Result", -1);
			Map<String, Object> reason = new HashMap<String, Object>();
			reason.put("FailReason", "服务器内部错误");
			returnMap.put("ResultData", reason);
			return returnMap;
		}
	}
	
	@Override
	public Map<String, Object> paymentBoot(Map<String, Object> parameter) {
		logger.info("start invoke paymentBoot:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        Map<String,Object> retData = new HashMap<>();
        try {
			retMap.putAll(parameter);
			Map<String,Object> orderMap = (Map<String, Object>) parameter.get(Constant.PBOSS_PARAMETER);
			if(null == orderMap.get("OrderNo") || null == orderMap.get("PppoeAccount") || null == orderMap.get("ServiceCode")){
				retData.put(Constant.PBOSS_FAILREASON, "字段验证不通过");
				retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_VARIFICATION_FAIL);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
			}
			String orderNo = orderMap.get("OrderNo").toString();
			String pppoeAccount = orderMap.get("PppoeAccount").toString();
			String serviceCode = orderMap.get("ServiceCode").toString();
			if(StringUtils.isEmpty(orderNo)){
				retData.put(Constant.PBOSS_FAILREASON, "参数OrderNo为空");
				retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
			}
			if(StringUtils.isEmpty(pppoeAccount)){
				retData.put(Constant.PBOSS_FAILREASON, "参数PppoeAccount为空");
				retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
			}
			if(StringUtils.isEmpty(serviceCode)){
				retData.put(Constant.PBOSS_FAILREASON, "参数ServiceCode为空");
				retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
			}
			 //验证工单号是否存在
			List<String> orders = new ArrayList<String>();
			orders.add(orderNo);
            List<GatewayBusiness> gatewayBusinessesResult = gatewayBusinessMapper.selectByOrderNos(orders);
            if(gatewayBusinessesResult != null && gatewayBusinessesResult.size()>0){
            	retData.put(Constant.PBOSS_FAILREASON, "OrderNo has existed");
            	retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_UNMATCH);
            	retMap.put(Constant.PBOSS_RESULTDATA,retData);
                return  retMap;
            }
            
            //根据宽带账号查询网关是否存在
    	    GatewayInfo gatewayInfoParam = new GatewayInfo();
            gatewayInfoParam.setGatewayAdslAccount(pppoeAccount);
            GatewayInfo gatewayInfoResult = gatewayInfoMapper.selectGatewayInfo(gatewayInfoParam);
            if(gatewayInfoResult == null){
                logger.info("根据PppoeAccount没有查询到对应的网关信息,PppoeAccount:{}",pppoeAccount);
                retMap.put(Constant.PBOSS_RESULT, 0);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
            }
            
			GatewayBusiness gb = new GatewayBusiness();
			String businessCode = "boot" + serviceCode;
			BusinessCategory category = new BusinessCategory();
			category.setBusinessCode(businessCode);
			List<Map<String, Object>> businessCategoryList = businessCategoryMapper.queryList(category);
			gb.setBusinessName((businessCategoryList == null || businessCategoryList.isEmpty()) ? "" :businessCategoryList.get(0).get("businessName").toString());
			gb.setId(UniqueUtil.uuid());
			gb.setOrderNo(orderNo);
			gb.setBusinessCode(businessCode);
			gb.setBusinessCodeBoss(serviceCode);
			gb.setAdslAccount(pppoeAccount);
			gb.setBusinessType("2");
			gb.setCreateTime((int)(System.currentTimeMillis() / 1000));
			gb.setBusinessStatu("1");
			gb.setGatewayUuid(gatewayInfoResult.getGatewayUuid());
			gb.setGatewayPassword(gatewayInfoResult.getGatewayPassword());
			gb.setAreacode(gatewayInfoResult.getGatewayAreaId());
			gatewayBusinessMapper.insertSelective(gb);
			
			//更新网关状态为 缴费开机
			GatewayInfo gateway = new GatewayInfo();
			gateway.setGatewayAdslAccount(pppoeAccount);
			gateway.setBusinessStatus("3");
			gatewayInfoMapper.updateGatewayBusinessStatusByAdsl(gateway);
			
			retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_SUCCESS);
			retMap.put(Constant.PBOSS_RESULTDATA, retData);
		} catch (Exception e) {
			logger.error("invoke  paymentBoot error:{}", e);
			retData.put(Constant.PBOSS_FAILREASON, "处理失败");
			retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_FAIL);
			retMap.put(Constant.PBOSS_RESULTDATA, retData);
		}
        retMap.remove(Constant.PBOSS_PARAMETER);
        logger.info("end invoke paymentBoot:{}",retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> arrearsStop(Map<String, Object> parameter) {
		logger.info("start invoke arrearsStop:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        Map<String,Object> retData = new HashMap<>();
        try {
			retMap.putAll(parameter);
			Map<String,Object> orderMap = (Map<String, Object>) parameter.get(Constant.PBOSS_PARAMETER);
			if(null == orderMap.get("OrderNo") || null == orderMap.get("PppoeAccount") || null == orderMap.get("ServiceCode")){
				retData.put(Constant.PBOSS_FAILREASON, "字段验证不通过");
				retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_VARIFICATION_FAIL);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
			}
			String orderNo = orderMap.get("OrderNo").toString();
			String pppoeAccount = orderMap.get("PppoeAccount").toString();
			String serviceCode = orderMap.get("ServiceCode").toString();
			if(StringUtils.isEmpty(orderNo)){
				retData.put(Constant.PBOSS_FAILREASON, "参数OrderNo为空");
				retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
			}
			if(StringUtils.isEmpty(pppoeAccount)){
				retData.put(Constant.PBOSS_FAILREASON, "参数PppoeAccount为空");
				retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
			}
			if(StringUtils.isEmpty(serviceCode)){
				retData.put(Constant.PBOSS_FAILREASON, "参数ServiceCode为空");
				retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
			}
			 //验证工单号是否存在
			List<String> orders = new ArrayList<String>();
			orders.add(orderNo);
            List<GatewayBusiness> gatewayBusinessesResult = gatewayBusinessMapper.selectByOrderNos(orders);
            if(gatewayBusinessesResult != null && gatewayBusinessesResult.size()>0){
            	retData.put(Constant.PBOSS_FAILREASON, "OrderNo has existed");
            	retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_UNMATCH);
            	retMap.put(Constant.PBOSS_RESULTDATA,retData);
                return  retMap;
            }
            
            //根据宽带账号查询网关是否存在
    	    GatewayInfo gatewayInfoParam = new GatewayInfo();
            gatewayInfoParam.setGatewayAdslAccount(pppoeAccount);
            GatewayInfo gatewayInfoResult = gatewayInfoMapper.selectGatewayInfo(gatewayInfoParam);
            if(gatewayInfoResult == null){
                logger.info("根据PppoeAccount没有查询到对应的网关信息,PppoeAccount:{}",pppoeAccount);
                retMap.put(Constant.PBOSS_RESULT, 0);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
            }
            
			GatewayBusiness gb = new GatewayBusiness();
			String businessCode = "stop" + serviceCode;
			BusinessCategory category = new BusinessCategory();
			category.setBusinessCode(businessCode);
			List<Map<String, Object>> businessCategoryList = businessCategoryMapper.queryList(category);
			gb.setBusinessName((businessCategoryList == null || businessCategoryList.isEmpty()) ? "" :businessCategoryList.get(0).get("businessName").toString());
			gb.setId(UniqueUtil.uuid());
			gb.setOrderNo(orderNo);
			gb.setBusinessCode(businessCode);
			gb.setBusinessCodeBoss(serviceCode);
			gb.setAdslAccount(pppoeAccount);
			gb.setBusinessType("3");
			gb.setCreateTime((int)(System.currentTimeMillis() / 1000));
			gb.setBusinessStatu("1");
			gb.setGatewayUuid(gatewayInfoResult.getGatewayUuid());
			gb.setGatewayPassword(gatewayInfoResult.getGatewayPassword());
			gb.setAreacode(gatewayInfoResult.getGatewayAreaId());
			gatewayBusinessMapper.insertSelective(gb);
			
			//更新网关状态为 欠费停机
			GatewayInfo gateway = new GatewayInfo();
			gateway.setGatewayAdslAccount(pppoeAccount);
			gateway.setBusinessStatus("2");
			gatewayInfoMapper.updateGatewayBusinessStatusByAdsl(gateway);
			
			retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_SUCCESS);
			retMap.put(Constant.PBOSS_RESULTDATA, retData);
		} catch (Exception e) {
			logger.error("invoke  arrearsStop error:{}", e);
			retData.put(Constant.PBOSS_FAILREASON, "处理失败");
			retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_FAIL);
			retMap.put(Constant.PBOSS_RESULTDATA, retData);
		}
        retMap.remove(Constant.PBOSS_PARAMETER);
        logger.info("end invoke arrearsStop:{}",retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> changeBandwidth(Map<String, Object> parameter) {
		logger.info("start invoke changeBandwidth:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        Map<String,Object> retData = new HashMap<>();
        try {
			retMap.putAll(parameter);
			Map<String,Object> orderMap = (Map<String, Object>) parameter.get(Constant.PBOSS_PARAMETER);
			if(null == orderMap.get("OrderNo") || null == orderMap.get("PppoeAccount") || null == orderMap.get("BANDWIDTH")){
				retData.put(Constant.PBOSS_FAILREASON, "字段验证不通过");
				retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_VARIFICATION_FAIL);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
			}
			String orderNo = orderMap.get("OrderNo").toString();
			String pppoeAccount = orderMap.get("PppoeAccount").toString();
			String bandwidth = orderMap.get("BANDWIDTH").toString();
			if(StringUtils.isEmpty(orderNo)){
				retData.put(Constant.PBOSS_FAILREASON, "参数OrderNo为空");
				retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
			}
			if(StringUtils.isEmpty(pppoeAccount)){
				retData.put(Constant.PBOSS_FAILREASON, "参数PppoeAccount为空");
				retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
			}
			if(StringUtils.isEmpty(bandwidth)){
				retData.put(Constant.PBOSS_FAILREASON, "参数BANDWIDTH为空");
				retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_PARAM_LOST);
				retMap.put(Constant.PBOSS_RESULTDATA, retData);
				return retMap;
			}
			 //验证工单号是否存在
			List<String> orders = new ArrayList<String>();
			orders.add(orderNo);
            List<GatewayBusiness> gatewayBusinessesResult = gatewayBusinessMapper.selectByOrderNos(orders);
            if(gatewayBusinessesResult != null && gatewayBusinessesResult.size()>0){
            	retData.put(Constant.PBOSS_FAILREASON, "OrderNo has existed");
            	retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_UNMATCH);
            	retMap.put(Constant.PBOSS_RESULTDATA,retData);
                return  retMap;
            }
            
            //根据宽带帐号查询网关uuid
            GatewayInfo gatewayInfoParam = new GatewayInfo();
            gatewayInfoParam.setGatewayAdslAccount(pppoeAccount);
            GatewayInfo gatewayInfoResult = gatewayInfoMapper.selectGatewayInfo(gatewayInfoParam);
            if(gatewayInfoResult == null){
                logger.info("根据PppoeAccount没有查询到对应的网关信息,PppoeAccount:{}",pppoeAccount);
                retMap.put(Constant.PBOSS_RESULT,0);
                retData.put(Constant.PBOSS_FAILREASON,"");
                retMap.put(Constant.PBOSS_RESULTDATA, retData);
                return  retMap;
            }
            
			GatewayBusiness gb = new GatewayBusiness();
			gb.setId(UniqueUtil.uuid());
			gb.setOrderNo(orderNo);
			gb.setAdslAccount(pppoeAccount);
			gb.setBusinessType("7");
			gb.setCreateTime((int)(System.currentTimeMillis() / 1000));
			gb.setBusinessStatu("1");
			gb.setBandwidth(Integer.valueOf(bandwidth));//宽带速率，传入参数单位为MB
			gb.setGatewayUuid(gatewayInfoResult.getGatewayUuid());
			gb.setGatewayPassword(gatewayInfoResult.getGatewayPassword());
			gb.setAreacode(gatewayInfoResult.getGatewayAreaId());
			gatewayBusinessMapper.insertSelective(gb);
			
			retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_SUCCESS);
			retMap.put(Constant.PBOSS_RESULTDATA, retData);
		} catch (Exception e) {
			logger.error("invoke changeBandwidth error:{}", e);
			retData.put(Constant.PBOSS_FAILREASON, "处理失败");
			retMap.put(Constant.PBOSS_RESULT, Constant.PBOSS_FAIL);
			retMap.put(Constant.PBOSS_RESULTDATA, retData);
		}
        retMap.remove(Constant.PBOSS_PARAMETER);
        logger.info("end invoke changeBandwidth:{}",retMap);
		return retMap;
	}
}
