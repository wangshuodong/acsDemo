/**
 * 
 */
package com.cmiot.rms.services.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.alibaba.fastjson.JSON;
import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BoxBusinessCategoryMapper;
import com.cmiot.rms.dao.mapper.BoxBusinessMapper;
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.mapper.BusinessCategoryMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessExecuteHistoryMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessOpenDetailMapper;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.mapper.GatewayPasswordMapper;
import com.cmiot.rms.dao.model.BoxBusiness;
import com.cmiot.rms.dao.model.BoxBusinessCategory;
import com.cmiot.rms.dao.model.BoxBusinessExample;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.BusinessCategory;
import com.cmiot.rms.dao.model.GatewayBusiness;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.GatewayPassword;
import com.cmiot.rms.services.GatewayManageService;
import com.cmiot.rms.services.WorkOrderStandardInterface;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.workorder.ExcuteBoxWorkOrderThread;
import com.cmiot.rms.services.workorder.ExcuteWorkOrderThread;
import com.cmiot.rms.services.workorder.WorkerThreadPool;

/**
 * @author lcs
 *
 */
public class WorkOrderStandardInterfaceImpl implements WorkOrderStandardInterface {

	private Logger logger = LoggerFactory.getLogger(WorkOrderStandardInterfaceImpl.class);
	
	@Autowired
	private GatewayBusinessMapper gatewayBusinessMapper;
	
	@Autowired
	private BoxBusinessMapper boxBusinessMapper;
	
	@Autowired
	private  BoxInfoMapper boxInfoMapper;
	
	@Autowired
    private GatewayPasswordMapper gatewayPasswordMapper;
	
	@Autowired
    private GatewayInfoMapper gatewayInfoMapper;
	
	@Autowired
	GatewayBusinessExecuteHistoryMapper gatewayBusinessExecuteHistoryMapper;
	
    @Autowired
    private AreaService areaService;
    
    @Autowired
    private RedisClientTemplate redisClientTemplate;
    
    @Autowired
    GatewayManageService gatewayManageService;
    
    @Autowired
    private GatewayBusinessOpenDetailMapper gatewayBusinessOpenDetailMapper;
    
    @Autowired
	private BoxBusinessCategoryMapper boxBusinessCategoryMapper;
    
    @Autowired
	private BusinessCategoryMapper businessCategoryMapper;


	@Autowired
	private ApplicationContext ctx;
    
    @Value("${pboss.province.code}")
	String provinceCode;
    
    @Value("${factoryResetTimeOut}")
    int factoryResetTimeOut;
	
    public static final List<String> deviceTypeList = new ArrayList<String>();
    
    public static final List<String> serviceCodeList = new ArrayList<String>();
    
    public static final List<String> operationTypeList = new ArrayList<String>();
    
    static{
    	deviceTypeList.add("hgu");
    	deviceTypeList.add("ihgu");
    	deviceTypeList.add("ott");
    	deviceTypeList.add("iptv");
    	
    	serviceCodeList.add("wband");
    	serviceCodeList.add("stb");
    	serviceCodeList.add("voip");
    	serviceCodeList.add("ott");
    	serviceCodeList.add("iptv");
    	
    	operationTypeList.add("Z");
    	operationTypeList.add("C");
    	operationTypeList.add("X");
    	
    }
    
	@Override
	public Map<String, Object> addNewInstallation(Map<String, Object> parameter) {
		logger.info("start invoke standard newInstallation,parameter:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_SUCCESS);
        retMap.put(Constant.ERROR_PARAMS,"");
        try {
            //解析公共参数
        	String checkResult = checkCommonParam(parameter);
        	
            if(StringUtils.isNotBlank(checkResult)){
                retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_STANDARD_FAIL);
                retMap.put(Constant.ERROR_PARAMS,checkResult.substring(0, checkResult.length()-1));
                logger.info("WorkOrderStandardInterface.addNewInstallation checkCommonParam result:"+checkResult.substring(0, checkResult.length()-1));
                return  retMap;
            }
            
            String deviceType = (String)parameter.get("deviceType");
            //如果是网关 根据password查询网关信息
            if ("hgu".equals(deviceType)||"ihgu".equals(deviceType)) {
            	
            	 //解析公共参数
            	String checkArgueResult = checkGatewayParam(parameter);
            	
            	//工单唯一性校验
            	List<String> list = new ArrayList<String>();
    			list.add(parameter.get("orderNo").toString());
    			List<GatewayBusiness> gatewayBusinessesResult = gatewayBusinessMapper.selectByOrderNos(list);
    			if (gatewayBusinessesResult!=null&&!gatewayBusinessesResult.isEmpty()) {
    				checkArgueResult = checkArgueResult+"orderNo:已存在,";
    				logger.info("orderNo repeated:"+parameter.get("orderNo").toString());
    			}
    			
    			
    			BusinessCategory category = new BusinessCategory();
    			String operationType = (String) parameter.get("operationType");
    			category.setBusinessCode(parameter.get("serviceMode").toString()+"_"+operationType);
    			
    			List<Map<String,Object>> categoryList = businessCategoryMapper.queryListAll(category);
    			if (categoryList.isEmpty()) {
    				checkArgueResult = checkArgueResult+"serviceMode:不存在,";
    				logger.info("serviceMode is not exist:"+category.getBusinessCode());
				}
    			
    			//修改、拆机前需要新建
    			if ("X".equals(operationType)||"C".equals(operationType)) {
    				Map<String,String> map = new HashMap<String,String>();
    				map.put("gatewayPassword", parameter.get("bindCode").toString());
    				map.put("businessCode", parameter.get("serviceMode").toString()+"_Z");
    				String result = gatewayBusinessOpenDetailMapper.selectBusinessOpen(map);
    				if (!"1".equals(result)) {
    					checkArgueResult = checkArgueResult+"网关还没有开通该业务,不能进行此操作,";
					}
				}
    			
                if(StringUtils.isNotBlank(checkArgueResult)){
                    retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_STANDARD_FAIL);
                    retMap.put(Constant.ERROR_PARAMS,checkArgueResult.substring(0, checkArgueResult.length()-1));
                    logger.info("WorkOrderStandardInterface.addNewInstallation checkGatewayParam result:"+checkArgueResult.substring(0, checkArgueResult.length()-1));
                    return  retMap;
                }
                
                GatewayBusiness business = getGatewayBusiness(parameter);
                business.setOrderNo(parameter.get("orderNo").toString());
                business.setBusinessCode((String)categoryList.get(0).get("businessCode"));
                business.setBusinessCodeBoss((String)categoryList.get(0).get("businessCode"));
                business.setBusinessName((String)categoryList.get(0).get("businessName"));
                //查询gateway_password表是否有该password，如果没有则写入
                if("Z".equals(operationType) && "wband".equals(parameter.get("serviceCode").toString())){
                	
                	GatewayPassword gatewayPassword = new GatewayPassword();
                	gatewayPassword.setGatewayPassword(business.getGatewayPassword());
                	GatewayPassword gatewayPasswordResult = gatewayPasswordMapper.selectByPassword(gatewayPassword);
                	if(gatewayPasswordResult == null){
                		gatewayPassword.setOrderNo(business.getOrderNo());
                		Map<String, Object> vectorArgues = (Map<String, Object>) parameter.get("vectorArgues");
                		if (vectorArgues!=null&&!vectorArgues.isEmpty()) {
                			String pppoeAccount = (String) vectorArgues.get("Username");
                    		gatewayPassword.setAdslAccount(pppoeAccount);
						}
                		int i = gatewayPasswordMapper.insertSelective(gatewayPassword);
                		
                		logger.info("WorkOrderStandardInterface.newInstallation，写入password参数，gatewayPassword-AdslAccount:{}",gatewayPassword.getGatewayPassword()+"-"+ gatewayPassword.getAdslAccount());
                		logger.info("WorkOrderStandardInterface.newInstallation，写入password结果，i:{}",i);
                	}
                }

            	GatewayInfo gatewayInfoParam = new GatewayInfo();
                gatewayInfoParam.setGatewayPassword(business.getGatewayPassword());
                GatewayInfo gatewayInfo = gatewayInfoMapper.selectGatewayInfo(gatewayInfoParam);
                if(gatewayInfo == null){
                	logger.info("根据password没有查询到对应的网关信息,password:{}",business.getGatewayPassword());
                }else {
                	business.setGatewayUuid(gatewayInfo.getGatewayUuid());
				}
              
              try {

				  //手动提交事物
				  DataSourceTransactionManager txManager = (DataSourceTransactionManager) ctx.getBean("txManager");
				  DefaultTransactionDefinition def = new DefaultTransactionDefinition();
				  def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
				  TransactionStatus txStatus = txManager.getTransaction(def);// 获得事务状态
            	  //记录工单 
	              int addResult = gatewayBusinessMapper.insertSelective(business);
				  txManager.commit(txStatus);
	              logger.info("WorkOrderStandardInterface.newInstallation，写入工单结果,addGatewayBusiness:{}",addResult);
	              //提交工单
            	  if (gatewayInfo != null&&"已绑定".equals(gatewayInfo.getGatewayStatus())) {
            		  WorkerThreadPool.getInstance().submitTask(new ExcuteWorkOrderThread(gatewayInfo));
				  }
                  
              }catch (Exception e){
                  logger.error("WorkOrderStandardInterface.newInstallation Exception,e:{}",e);
              }
			}
            //机顶盒
            else if ("ott".equals(deviceType)||"iptv".equals(deviceType)) {
				
            	 //解析
            	String checkArgueResult = checkBoxParam(parameter);
            	//工单唯一性校验
            	BoxBusinessExample example = new BoxBusinessExample();
            	String orderNo = (String)parameter.get("orderNo");
            	example.createCriteria().andBoxMacEqualTo(orderNo);
            	List<BoxBusiness> businessList = boxBusinessMapper.selectByExample(example);
            	if (!businessList.isEmpty()) {
            		checkArgueResult = checkArgueResult+ "orderNo:已存在,";
				}
            	
            	//MAC号是否存在校验
            	//查询机顶盒对象
                BoxInfo boxInfo = new BoxInfo();
                boxInfo.setBoxMacaddress((String)parameter.get("bindCode"));
                List<BoxInfo> boxList = boxInfoMapper.selectBoxInfo(boxInfo);
                if (boxList.isEmpty()) {
                	checkArgueResult = checkArgueResult+ "bindCode:不存在,";
				}
            	
                //校验serviceMode是否存在
                BoxBusinessCategory category = new BoxBusinessCategory();
                
				String operationType = (String) parameter.get("operationType");
				category.setBusinessCode(parameter.get("serviceMode").toString() + "_"+operationType);
                
        		List<Map<String,Object>> boxBusinessCategory = boxBusinessCategoryMapper.queryListAll(category);
        		if(boxBusinessCategory.isEmpty()){
        			checkArgueResult = checkArgueResult+ "serviceMode:不存在,";
        		}
                
                if(StringUtils.isNotBlank(checkArgueResult)){
                    retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_STANDARD_FAIL);
                    retMap.put(Constant.ERROR_PARAMS,checkArgueResult.substring(0, checkArgueResult.length()-1));
                    logger.info("WorkOrderStandardInterface.addNewInstallation checkBoxParam result:"+checkArgueResult.substring(0, checkArgueResult.length()-1));
                    return  retMap;
                }
            	
               BoxBusiness boxBusiness = getBoxBusiness(parameter);
               BoxInfo box = boxList.get(0);
               
               boxBusiness.setBoxUuid(box.getBoxUuid());
               boxBusiness.setOrderNo(orderNo);
               boxBusiness.setBusinessCode((String)boxBusinessCategory.get(0).get("businessCode"));
               boxBusiness.setBusinessCodeBoss((String)boxBusinessCategory.get(0).get("businessCode"));
               boxBusiness.setBusinessName((String)boxBusinessCategory.get(0).get("businessName"));

				//手动提交事物
				DataSourceTransactionManager txManager = (DataSourceTransactionManager) ctx.getBean("txManager");
				DefaultTransactionDefinition def = new DefaultTransactionDefinition();
				def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
				TransactionStatus txStatus = txManager.getTransaction(def);// 获得事务状态
                boxBusinessMapper.insertSelective(boxBusiness);
				txManager.commit(txStatus);
               //如果机顶盒注册且在线 提交执行工单
               if ("2".equals(box.getBoxStatus())&&box.getBoxOnline()==1) {
            	   //在redis中确认是否真的在线
					if ("1".equals(redisClientTemplate.get(Constant.BOX_ONLINE + box.getBoxSerialnumber()))) {
						WorkerThreadPool.getInstance().submitTask(new ExcuteBoxWorkOrderThread(boxBusiness));
					}
               }
            	
			}
            
        }catch (Exception e){
            logger.error("WorkOrderStandardInterface.newInstallation Exception,e:{}",e);
            retMap.put(Constant.PBOSS_RESULT,Constant.PBOSS_STANDARD_FAIL);
        }
        logger.info("WorkOrderStandardInterface.newInstallation,retMap:{}", retMap);
        return retMap;
	}

	
    /**
     * 参数校验
     * @param parameter
     * @return
     */
    private String checkCommonParam(Map<String, Object> parameter) {
        //先解析参数
        StringBuffer errorParam = new StringBuffer();
        
        if (StringUtils.isBlank((String) parameter.get("orderNo"))) {
        	errorParam.append("orderNo:不能为空,");
		}else if (parameter.get("orderNo").toString().length()>128) {
			errorParam.append("orderNo:长度超过128个字符,");
		}
        
        if (!StringUtils.equals(provinceCode, (String) parameter.get("provCode"))) {
        	errorParam.append("provCode:编码不正确,");
		}
        
        if (StringUtils.isBlank((String) parameter.get("areaCode"))) {
        	errorParam.append("areaCode:不能为空,");
		}else {
			//校验区域编码的合法性
			Area area = areaService.findAreaById(Integer.valueOf(parameter.get("areaCode").toString()));
			if (area==null) {
				errorParam.append("areaCode:编码不存在,");
			}
		}
        
        if (StringUtils.isBlank((String) parameter.get("userId"))) {
        	errorParam.append("userId:不能为空,");
		}else if (parameter.get("userId").toString().length()>128) {
			errorParam.append("userId:长度超过128个字符,");
		}
        
        
        if (StringUtils.isBlank((String) parameter.get("orderTime"))) {
        	errorParam.append("orderTime:不能为空,");
		}
        
        if (!deviceTypeList.contains(parameter.get("deviceType"))) {
        	errorParam.append("deviceType:设备类型存在,");
		}
        
        if (!serviceCodeList.contains(parameter.get("serviceCode"))) {
        	errorParam.append("serviceCode:值不存在,");
		}
        
        if (!operationTypeList.contains(parameter.get("operationType"))) {
        	errorParam.append("operationType:值不存在,");
		}
        
        if (StringUtils.isBlank((String) parameter.get("serviceMode"))) {
        	errorParam.append("serviceMode:不能为空,");
		}else if (parameter.get("serviceMode").toString().length()>64) {
			errorParam.append("serviceMode:长度超过64个字符,");
		}
        
        if (StringUtils.isBlank((String) parameter.get("bindCode"))) {
        	errorParam.append("bindCode:不能为空,");
		}else if (parameter.get("bindCode").toString().length()>32) {
			errorParam.append("bindCode:长度超过32个字符,");
		}
        
        if (StringUtils.isNotBlank((String) parameter.get("userName"))&&
        		parameter.get("userName").toString().length()>64) {
        	errorParam.append("userName:超过64个字符,");
		}
        
        if (StringUtils.isNotBlank((String) parameter.get("userAddress"))&&
        		parameter.get("userAddress").toString().length()>128) {
        	errorParam.append("userAddress:超过128个字符,");
		}
        
        if (StringUtils.isNotBlank((String) parameter.get("contactPerson"))&&
        		parameter.get("contactPerson").toString().length()>128) {
        	errorParam.append("contactPerson:超过128个字符,");
		}
        
        if (StringUtils.isNotBlank((String) parameter.get("contactManner"))&&
        		parameter.get("contactManner").toString().length()>128) {
        	errorParam.append("contactManner:超过128个字符,");
		}
        
        String str = (String) parameter.get("vectorArgues");
        if (StringUtils.isNotBlank(str)) {
        	if (StringUtils.isNotBlank(str)&&str.toString().length()>2048) {
    			errorParam.append("vectorArgues:超过2048个字符");
    		}else if (!Pattern.compile("^[\\w.]+={1}[\\w.@]+(\\^{1}[\\w.]+={1}[\\w.@]+){0,}$").matcher(str).find()) {
    			errorParam.append("vectorArgues:格式不正确");
    		}
		}
        
        return errorParam.toString();
    }
	
  
    private String checkGatewayParam(Map<String, Object> parameter){
		
    	StringBuffer errorParam = new StringBuffer();
    	
    	String vectorArgues = (String) parameter.get("vectorArgues");
    	if (StringUtils.isBlank(vectorArgues)) {
			return null;
		}
    	
    	String[] array = vectorArgues.split("\\^");
		String[] mapArray;
		Map<String,String> argMap = new HashMap<String,String>();
		for (int i = 0; i < array.length; i++) {
			 mapArray = array[i].split("=");
			 argMap.put(mapArray[0], mapArray[1]);
		}
		
		parameter.put("vectorArgues", argMap);
		
        String serviceMode = (String)parameter.get("serviceMode");
        String operationType = parameter.get("operationType").toString(); 
        
        //宽带上网业务(路由模式PPPOE拨号模式)
		if ("wband_ppp".equals(serviceMode)) {
			
			if (Constant.OPERATIONTYPE_C.equals(operationType)) {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
			}
			else {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("LanInterface"))&&
						argMap.get("LanInterface").toString().length()>256) {
		        	errorParam.append("vectorArgues.LanInterface:超过256个字符,");
				}
				
				if (StringUtils.isBlank((String) argMap.get("Username"))) {
		        	errorParam.append("vectorArgues.Username:不能为空,");
				}else if (argMap.get("Username").toString().length()>64) {
					errorParam.append("vectorArgues.Username:超过64个字符,");
				}
				
				if (StringUtils.isBlank((String) argMap.get("Password"))) {
		        	errorParam.append("vectorArgues.Password:不能为空,");
				}else if (argMap.get("Password").toString().length()>64) {
					errorParam.append("vectorArgues.Password:超过64个字符,");
				}
				
			}
			
		}
		//宽带上网业务(DHCP模式)
		else if ("wband_dhcp".equals(serviceMode)) {
			if (Constant.OPERATIONTYPE_C.equals(operationType)) {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
			}
			else {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("LanInterface"))&&
						argMap.get("LanInterface").toString().length()>256) {
		        	errorParam.append("vectorArgues.LanInterface:超过256个字符,");
				}
				
			}
		}
        
		//宽带上网业务(静态IP模式)
		else if ("wband_static".equals(serviceMode)) {
			if (Constant.OPERATIONTYPE_C.equals(operationType)) {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
			}
			else {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("LanInterface"))&&
						argMap.get("LanInterface").toString().length()>256) {
		        	errorParam.append("vectorArgues.LanInterface:超过256个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("SubnetMask"))&&
						argMap.get("SubnetMask").toString().length()>32) {
		        	errorParam.append("vectorArgues.SubnetMask:超过32个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("DefaultGateway"))&&
						argMap.get("DefaultGateway").toString().length()>32) {
		        	errorParam.append("vectorArgues.DefaultGateway:超过32个字符,");
				}
				
				if (StringUtils.isBlank((String) argMap.get("ExternalIPAddress"))) {
		        	errorParam.append("vectorArgues.ExternalIPAddress:不能为空,");
				}else if (argMap.get("ExternalIPAddress").toString().length()>32) {
					errorParam.append("vectorArgues.ExternalIPAddress:超过32个字符,");
				}
			}
		}
		
		//宽带上网业务(桥接模式)
		else if ("wband_bridge".equals(serviceMode)) {
			if (Constant.OPERATIONTYPE_C.equals(operationType)) {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
			}
			else {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("LanInterface"))&&
						argMap.get("LanInterface").toString().length()>256) {
		        	errorParam.append("vectorArgues.LanInterface:超过256个字符,");
				}
				
			}
		}
		//语音业务（SIP模式） 
		else if ("voip_sip".equals(serviceMode)) {
			if (Constant.OPERATIONTYPE_C.equals(operationType)) {
				if (StringUtils.isBlank((String) argMap.get("Port"))) {
		        	errorParam.append("vectorArgues.Port:不能为空,");
				}else if (argMap.get("Port").toString().length()>12) {
					errorParam.append("vectorArgues.Port:超过12个字符,");
				}
			}
			else {
				if (StringUtils.isBlank((String) argMap.get("Port"))) {
		        	errorParam.append("vectorArgues.Port:不能为空,");
				}else if (argMap.get("Port").toString().length()>12) {
					errorParam.append("vectorArgues.Port:超过12个字符,");
				}
				
				if (StringUtils.isBlank((String) argMap.get("SIPUserName"))) {
		        	errorParam.append("vectorArgues.SIPUserName:不能为空,");
				}else if (argMap.get("SIPUserName").toString().length()>128) {
					errorParam.append("vectorArgues.SIPUserName:超过128个字符,");
				}
				
				if (StringUtils.isBlank((String) argMap.get("SIPPassword"))) {
		        	errorParam.append("vectorArgues.SIPPassword:不能为空,");
				}else if (argMap.get("SIPPassword").toString().length()>128) {
					errorParam.append("vectorArgues.SIPPassword:超过128个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("URI"))&&
						argMap.get("URI").toString().length()>128) {
		        	errorParam.append("vectorArgues.URI:超过128个字符,");
				}
				
			}
		}
		
		//语音业务（DHCP模式）
		else if ("voip_dhcp".equals(serviceMode)) {
			if (Constant.OPERATIONTYPE_C.equals(operationType)) {
				if (StringUtils.isBlank((String) argMap.get("Port"))) {
		        	errorParam.append("vectorArgues.Port:不能为空,");
				}else if (argMap.get("Port").toString().length()>12) {
					errorParam.append("vectorArgues.Port:超过12个字符,");
				}
			}
			else {
				if (StringUtils.isBlank((String) argMap.get("Port"))) {
		        	errorParam.append("vectorArgues.Port:不能为空,");
				}else if (argMap.get("Port").toString().length()>12) {
					errorParam.append("vectorArgues.Port:超过12个字符,");
				}
				
				if (StringUtils.isBlank((String) argMap.get("SIPUserName"))) {
		        	errorParam.append("vectorArgues.SIPUserName:不能为空,");
				}else if (argMap.get("SIPUserName").toString().length()>128) {
					errorParam.append("vectorArgues.SIPUserName:超过128个字符,");
				}
				
				if (StringUtils.isBlank((String) argMap.get("SIPPassword"))) {
		        	errorParam.append("vectorArgues.SIPPassword:不能为空,");
				}else if (argMap.get("SIPPassword").toString().length()>128) {
					errorParam.append("vectorArgues.SIPPassword:超过128个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("URI"))&&
						argMap.get("URI").toString().length()>128) {
		        	errorParam.append("vectorArgues.URI:超过128个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("LanInterface"))&&
						argMap.get("LanInterface").toString().length()>256) {
		        	errorParam.append("vectorArgues.LanInterface:超过256个字符,");
				}
			}
		}
				
		//语音业务（静态IP模式）
		else if ("voip_static".equals(serviceMode)) {
			if (Constant.OPERATIONTYPE_C.equals(operationType)) {
				if (StringUtils.isBlank((String) argMap.get("Port"))) {
		        	errorParam.append("vectorArgues.Port:不能为空,");
				}else if (argMap.get("Port").toString().length()>12) {
					errorParam.append("vectorArgues.Port:超过12个字符,");
				}
			}
			else {
				if (StringUtils.isBlank((String) argMap.get("Port"))) {
		        	errorParam.append("vectorArgues.Port:不能为空,");
				}else if (argMap.get("Port").toString().length()>12) {
					errorParam.append("vectorArgues.Port:超过12个字符,");
				}
				
				if (StringUtils.isBlank((String) argMap.get("SIPUserName"))) {
		        	errorParam.append("vectorArgues.SIPUserName:不能为空,");
				}else if (argMap.get("SIPUserName").toString().length()>128) {
					errorParam.append("vectorArgues.SIPUserName:超过128个字符,");
				}
				
				if (StringUtils.isBlank((String) argMap.get("SIPPassword"))) {
		        	errorParam.append("vectorArgues.SIPPassword:不能为空,");
				}else if (argMap.get("SIPPassword").toString().length()>128) {
					errorParam.append("vectorArgues.SIPPassword:超过128个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("URI"))&&
						argMap.get("URI").toString().length()>128) {
		        	errorParam.append("vectorArgues.URI:超过128个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("LanInterface"))&&
						argMap.get("LanInterface").toString().length()>256) {
		        	errorParam.append("vectorArgues.LanInterface:超过256个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("SubnetMask"))&&
						argMap.get("SubnetMask").toString().length()>32) {
		        	errorParam.append("vectorArgues.SubnetMask:超过32个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("DefaultGateway"))&&
						argMap.get("DefaultGateway").toString().length()>32) {
		        	errorParam.append("vectorArgues.DefaultGateway:超过32个字符,");
				}
				
				if (StringUtils.isBlank((String) argMap.get("ExternalIPAddress"))) {
		        	errorParam.append("vectorArgues.ExternalIPAddress:不能为空,");
				}else if (argMap.get("ExternalIPAddress").toString().length()>32) {
					errorParam.append("vectorArgues.ExternalIPAddress:超过32个字符,");
				}
			}
		}
		//OTT机顶盒通道（PPPOE拨号模式） 
		else if ("ott_ppp".equals(serviceMode)) {
			if (Constant.OPERATIONTYPE_C.equals(operationType)) {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
			}
			else {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("LanInterface"))&&
						argMap.get("LanInterface").toString().length()>256) {
		        	errorParam.append("vectorArgues.LanInterface:超过256个字符,");
				}
				
				if (StringUtils.isBlank((String) argMap.get("Username"))) {
		        	errorParam.append("vectorArgues.Username:不能为空,");
				}else if (argMap.get("Username").toString().length()>64) {
					errorParam.append("vectorArgues.Username:超过64个字符,");
				}
				
				if (StringUtils.isBlank((String) argMap.get("Password"))) {
		        	errorParam.append("vectorArgues.Password:不能为空,");
				}else if (argMap.get("Password").toString().length()>64) {
					errorParam.append("vectorArgues.Password:超过64个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("DefaultConnectionService"))&&
						argMap.get("DefaultConnectionService").toString().length()>256) {
		        	errorParam.append("vectorArgues.DefaultConnectionService:超过256个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("DestIPAddress"))&&
						argMap.get("DestIPAddress").toString().length()>32) {
		        	errorParam.append("vectorArgues.DestIPAddress:超过32个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("DestSubnetMask"))&&
						argMap.get("DestSubnetMask").toString().length()>32) {
		        	errorParam.append("vectorArgues.DestSubnetMask:超过32个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("Interface"))&&
						argMap.get("Interface").toString().length()>256) {
		        	errorParam.append("vectorArgues.Interface:超过256个字符,");
				}
				
			}
		}
		
		//OTT机顶盒通道（DHCP模式） 
		else if ("ott_dhcp".equals(serviceMode)) {
			if (Constant.OPERATIONTYPE_C.equals(operationType)) {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
			}
			else {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("LanInterface"))&&
						argMap.get("LanInterface").toString().length()>256) {
		        	errorParam.append("vectorArgues.LanInterface:超过256个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("DefaultConnectionService"))&&
						argMap.get("DefaultConnectionService").toString().length()>256) {
		        	errorParam.append("vectorArgues.DefaultConnectionService:超过256个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("DestIPAddress"))&&
						argMap.get("DestIPAddress").toString().length()>32) {
		        	errorParam.append("vectorArgues.DestIPAddress:超过32个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("DestSubnetMask"))&&
						argMap.get("DestSubnetMask").toString().length()>32) {
		        	errorParam.append("vectorArgues.DestSubnetMask:超过32个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("Interface"))&&
						argMap.get("Interface").toString().length()>256) {
		        	errorParam.append("vectorArgues.Interface:超过256个字符,");
				}
				
			}
		}
				
		
		//IPTV机顶盒通道（桥接模式）
		else if ("iptv_bridge".equals(serviceMode)) {
			if (Constant.OPERATIONTYPE_C.equals(operationType)) {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
			}
			else {
				if (StringUtils.isNotBlank((String) argMap.get("VLANID"))&&
						argMap.get("VLANID").toString().length()>12) {
		        	errorParam.append("vectorArgues.VLANID:超过12个字符,");
				}
				
				if (StringUtils.isNotBlank((String) argMap.get("LanInterface"))&&
						argMap.get("LanInterface").toString().length()>256) {
		        	errorParam.append("vectorArgues.LanInterface:超过256个字符,");
				}
				
			}
		}
		
    	return errorParam.toString();
    	
    }
 
    
    private  GatewayBusiness getGatewayBusiness(Map<String, Object> parameter){
		
    	//入库工单对象
        GatewayBusiness business = new GatewayBusiness();
        business.setProvcode((String)parameter.get("provCode"));
        business.setAreacode(parameter.get("areaCode").toString());
        business.setUseridBoss((String)parameter.get("userId"));
        business.setDevicetype((String)parameter.get("deviceType"));
        business.setBusinessStatu("0");
        business.setGatewayPassword(parameter.get("bindCode").toString());
		business.setUsernameBoss((String)parameter.get("userName"));
		business.setUseraddressBoss((String)parameter.get("userAddress"));
		business.setContactpersonBoss((String)parameter.get("contactPerson"));
		business.setContactmannerBoss((String)parameter.get("contactManner"));
        business.setCreateTime(DateTools.getCurrentSecondTime());
        
        business.setId(UniqueUtil.uuid());
        //统一将区域编码改为区域id存储
        if(StringUtils.isNotEmpty(business.getAreacode())){
        	Integer areaId = areaService.findIdByCode(business.getAreacode());
        	business.setAreacode(areaId == null ? "" : areaId.toString());
        }
        String str = "YYYY-MM-DD HH:mm:ss";
		 SimpleDateFormat df = new SimpleDateFormat(str);
		 try {
			Date date = df.parse((String)parameter.get("orderTime"));
			business.setOrdertimeBoss(date);
		} catch (ParseException e) {
			logger.error("工单时间转换异常:{}",parameter.get("orderTime"));
		}
        
		String operationType = (String) parameter.get("operationType");
        if ("Z".equals(operationType)) {
        	 business.setBusinessType("1");
		}else if ("C".equals(operationType)) {
			 business.setBusinessType("8");
		}else if ("X".equals(operationType)) {
			 business.setBusinessType("9");
		}
		 
        @SuppressWarnings("unchecked")
        Map<String,String> argMap =  (Map<String,String>) parameter.get("vectorArgues");
        if (argMap!=null&&!argMap.isEmpty()) {
        	if (StringUtils.isNotEmpty((String) argMap.get("Username"))) {
            	business.setAdslAccount((String) argMap.get("Username"));
    		}
            if (StringUtils.isNotEmpty((String) argMap.get("Password"))) {
            	business.setAdslPassword((String) argMap.get("Password"));
    		}
    			
    		//模板里面动态参数
    		Map<String,String> parmMap = new HashMap<String,String>();
    		if (StringUtils.isNotBlank(argMap.get("VLANID"))) {
    			  parmMap.put("VLANID", argMap.get("VLANID"));
    		}
    		if (StringUtils.isNotBlank(argMap.get("LanInterface"))) {
    			  parmMap.put("LanInterface", argMap.get("LanInterface"));
    		}
    		//宽带  PPPoE模式
    		if (StringUtils.isNotBlank(argMap.get("Username"))) {
    			parmMap.put("Username", argMap.get("Username"));
    	        parmMap.put("Password", argMap.get("Password"));
    		}
    		
    		String serviceMode = (String)parameter.get("serviceMode");
    		//宽带 静态IP模式
    		if ("wband_static".equals(serviceMode)||"voip_static".equals(serviceMode)) {
    			business.setSubnetmask((String) argMap.get("SubnetMask"));
    			business.setIpaddress((String) argMap.get("ExternalIPAddress"));
    			
    			parmMap.put("ExternalIPAddress", argMap.get("ExternalIPAddress"));
    			parmMap.put("SubnetMask", argMap.get("SubnetMask"));
    			
    		}else if ("ott_ppp".equals(serviceMode)||"ott_dhcp".equals(serviceMode)) {
    			business.setIpaddress((String) argMap.get("DestIPAddress"));
    			business.setSubnetmask((String) argMap.get("DestSubnetMask"));
    			
    			parmMap.put("DestIPAddress", argMap.get("DestIPAddress"));
    			parmMap.put("DestSubnetMask", argMap.get("DestSubnetMask"));
    		}
    		
    		if (StringUtils.isNotBlank((String) argMap.get("DefaultGateway"))) {
    			parmMap.put("DefaultGateway", argMap.get("DefaultGateway"));
    		}
    	   
    		if (StringUtils.isNotBlank((String) argMap.get("Port"))) {
    			parmMap.put("Port", argMap.get("Port"));
    		}
    		
    		if (StringUtils.isNotBlank((String) argMap.get("SIPUserName"))) {
    			parmMap.put("SIPUserName", argMap.get("SIPUserName"));
    		}
    		
    		if (StringUtils.isNotBlank((String) argMap.get("SIPPassword"))) {
    			parmMap.put("SIPPassword", argMap.get("SIPPassword"));
    		}
    		
    		if (StringUtils.isNotBlank((String) argMap.get("URI"))) {
    			parmMap.put("URI", argMap.get("URI"));
    		}
    		
    		if (StringUtils.isNotBlank((String) argMap.get("DefaultConnectionService"))) {
    			parmMap.put("DefaultConnectionService", argMap.get("DefaultConnectionService"));
    		}
    		
    	   if (!parmMap.isEmpty()) {
    		   business.setParameterList(JSON.toJSONString(parmMap));
    	   }
           business.setVlanid((String)argMap.get("VLANID"));
           business.setLaninterface((String)argMap.get("LanInterface"));
           business.setDefaultgateway((String)argMap.get("DefaultGateway"));
           business.setPortSip((String)argMap.get("Port"));
           business.setUriSip((String)argMap.get("URI"));
           business.setDefaultconnectionservice((String)argMap.get("DefaultConnectionService"));
           business.setWanInterface((String)argMap.get("Interface"));
		}
        
       
       return business;
    }
    
	/**
	 * 机顶盒业务参数校验
	 * @param parameter
	 * @return
	 */
	private String checkBoxParam(Map<String, Object> parameter) {

		StringBuffer errorParam = new StringBuffer();
		String serviceMode = (String) parameter.get("serviceMode");

		String vectorArgues = parameter.get("vectorArgues").toString();
    	String[] array = vectorArgues.split("\\^");
		String[] mapArray;
		Map<String,String> argMap = new HashMap<String,String>();
		for (int i = 0; i < array.length; i++) {
			 mapArray = array[i].split("=");
			 argMap.put(mapArray[0], mapArray[1]);
		}
		
		parameter.put("vectorArgues", argMap);
		
		
		String operationType = parameter.get("operationType").toString();

		// OTT机顶盒开通
		if ("ott_stb".equals(serviceMode)) {
			if (Constant.OPERATIONTYPE_Z.equals(operationType)) {
				if (StringUtils.isBlank((String) argMap.get("IPOEPassword"))) {
					errorParam.append("vectorArgues.IPOEPassword:不能为空,");
				}else if ( argMap.get("IPOEPassword").toString().length() > 32) {
					errorParam.append("vectorArgues.IPOEPassword:超过32个字符,");
				}

				if (StringUtils.isBlank((String) argMap.get("IPOEID"))) {
					errorParam.append("vectorArgues.IPOEID:不能为空,");
				}else if (argMap.get("IPOEID").toString().length() > 32) {
					errorParam.append("vectorArgues.IPOEID:超过32个字符,");
				}

				if (StringUtils.isBlank((String) argMap.get("UserID"))) {
					errorParam.append("vectorArgues.UserID:不能为空,");
				}else if (argMap.get("UserID").toString().length() > 32) {
					errorParam.append("vectorArgues.UserID:超过32个字符,");
				}

				if (StringUtils.isBlank((String) argMap.get("UserIDPassword"))) {
					errorParam.append("vectorArgues.UserIDPassword:不能为空,");
				}else if (argMap.get("UserIDPassword").toString().length() > 32) {
					errorParam.append("vectorArgues.UserIDPassword:超过32个字符,");
				}
			} else if (Constant.OPERATIONTYPE_X.equals(operationType)) {
				if (StringUtils.isNotBlank((String) argMap.get("IPOEPassword"))
						&& argMap.get("IPOEPassword").toString().length() > 32) {
					errorParam.append("vectorArgues.IPOEPassword:超过32个字符,");
				}

				if (StringUtils.isNotBlank((String) argMap.get("IPOEID"))
						&& argMap.get("IPOEID").toString().length() > 32) {
					errorParam.append("vectorArgues.IPOEID:超过32个字符,");
				}

				if (StringUtils.isNotBlank((String) argMap.get("UserID"))
						&& argMap.get("UserID").toString().length() > 32) {
					errorParam.append("vectorArgues.UserID:超过32个字符,");
				}

				if (StringUtils.isNotBlank((String) argMap.get("UserIDPassword"))
						&& argMap.get("UserIDPassword").toString().length() > 32) {
					errorParam.append("vectorArgues.UserIDPassword:超过32个字符,");
				}
			}

		}
		// IPTV机顶盒开通
		else if ("iptv_stb".equals(serviceMode)) {
			if (Constant.OPERATIONTYPE_Z.equals(operationType)) {
				if (StringUtils.isBlank((String) argMap.get("UserID"))) {
					errorParam.append("vectorArgues.UserID::不能为空,");
				}else if (argMap.get("UserID").toString().length() > 32) {
					errorParam.append("vectorArgues.UserID:超过32个字符,");
				}

				if (StringUtils.isBlank((String) argMap.get("UserIDPassword"))) {
					errorParam.append("vectorArgues.UserIDPassword:不能为空,");
				}else if (argMap.get("UserIDPassword").toString().length() > 32) {
					errorParam.append("vectorArgues.UserIDPassword,");
				}
			} else if (Constant.OPERATIONTYPE_X.equals(operationType)) {
				if (StringUtils.isNotBlank((String) argMap.get("UserID"))
						&& argMap.get("UserID").toString().length() > 32) {
					errorParam.append("vectorArgues.UserID:超过32个字符,");
				}

				if (StringUtils.isNotBlank((String) argMap.get("UserIDPassword"))
						&& argMap.get("UserIDPassword").toString().length() > 32) {
					errorParam.append("vectorArgues.UserIDPassword:超过32个字符,");
				}
			}
		}

		return errorParam.toString();

	}
   
	
	private BoxBusiness getBoxBusiness(Map<String, Object> parameter){
		
		BoxBusiness boxBusiness = new BoxBusiness();
		boxBusiness.setProvcode((String)parameter.get("provCode"));
        boxBusiness.setAreacode(parameter.get("areaCode").toString());
        boxBusiness.setUseridBoss((String)parameter.get("userId"));
        boxBusiness.setDevicetype((String)parameter.get("deviceType"));
        boxBusiness.setBusinessStatu("0");
        boxBusiness.setBoxMac(parameter.get("bindCode").toString());
		boxBusiness.setUsernameBoss((String)parameter.get("userName"));
		boxBusiness.setUseraddressBoss((String)parameter.get("userAddress"));
		boxBusiness.setContactpersonBoss((String)parameter.get("contactPerson"));
		boxBusiness.setContactmannerBoss((String)parameter.get("contactManner"));
        boxBusiness.setCreateTime(DateTools.getCurrentSecondTime());
        boxBusiness.setId(UniqueUtil.uuid());
        String operationType = (String) parameter.get("operationType");
        if ("Z".equals(operationType)) {
        	boxBusiness.setBusinessType("1");
		}else if ("X".equals(operationType)) {
			boxBusiness.setBusinessType("2");
		}
        //统一将存储区域编码的字段改为存储区域Id
        if(StringUtils.isNotEmpty(boxBusiness.getAreacode())){
     	   Integer areaId = areaService.findIdByCode(boxBusiness.getAreacode());
     	   boxBusiness.setAreacode(areaId == null ? "" : areaId.toString());
        }
        String str = "YYYY-MM-DD HH:mm:ss";
		 SimpleDateFormat df = new SimpleDateFormat(str);
		 try {
			Date date = df.parse((String)parameter.get("orderTime"));
			boxBusiness.setOrdertimeBoss(date);
		} catch (ParseException e) {
			logger.error("工单时间转换异常:{}",parameter.get("orderTime"));
		}
        
        @SuppressWarnings("unchecked")
        Map<String,String> argMap =  (Map<String,String>) parameter.get("vectorArgues");
        
        boxBusiness.setIpoeid((String)argMap.get("IPOEID"));
        boxBusiness.setIpoepassword((String)argMap.get("IPOEPassword"));
        boxBusiness.setUserid((String)argMap.get("UserID"));
        boxBusiness.setUseridpassword((String)argMap.get("UserIDPassword"));
        
        
        Map<String,String> parmMap = new HashMap<String,String>();

        if (StringUtils.isNotBlank((String) argMap.get("IPOEID"))) {
        	parmMap.put("IPOEID", argMap.get("IPOEID"));
		}
        
        if (StringUtils.isNotBlank((String) argMap.get("IPOEPassword"))) {
        	parmMap.put("IPOEPassword", argMap.get("IPOEPassword"));
		}
        
        if (StringUtils.isNotBlank((String) argMap.get("UserID"))) {
        	parmMap.put("UserID", argMap.get("UserID"));
		}
        
        if (StringUtils.isNotBlank((String) argMap.get("UserIDPassword"))) {
        	parmMap.put("UserIDPassword", argMap.get("UserIDPassword"));
		}
		
        if (!parmMap.isEmpty()) {
        	boxBusiness.setParameterList(JSON.toJSONString(parmMap));
		}
		
		return boxBusiness;
	}
    
}
