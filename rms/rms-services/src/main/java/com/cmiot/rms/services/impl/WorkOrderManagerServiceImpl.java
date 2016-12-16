package com.cmiot.rms.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BusinessCategoryMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessOpenDetailMapper;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.mapper.GatewayPasswordMapper;
import com.cmiot.rms.dao.mapper.WorkOrderTemplateInfoMapper;
import com.cmiot.rms.dao.model.BusinessCategory;
import com.cmiot.rms.dao.model.GatewayBusiness;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.GatewayPassword;
import com.cmiot.rms.dao.model.WorkOrderTemplateInfo;
import com.cmiot.rms.services.WorkOrderInterface;
import com.cmiot.rms.services.WorkOrderManagerService;
import com.cmiot.rms.services.util.OperationLogUtil;
import com.cmiot.rms.services.workorder.WorkOrderForTaskThread;
import com.cmiot.rms.services.workorder.WorkerThreadPool;
import com.cmiot.rms.services.workorder.impl.BusiOperation;

/**
 * Created by admin on 2016/6/8.
 */
@Service
public class WorkOrderManagerServiceImpl implements WorkOrderManagerService {

    private Logger logger = LoggerFactory.getLogger(WorkOrderManagerServiceImpl.class);

    @Autowired
    private WorkOrderTemplateInfoMapper workOrderTemplateInfoMapper;
    
    @Autowired
    private GatewayBusinessMapper gatewayBusinessMapper;

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;

    @Autowired
    private BusinessCategoryMapper businessCategoryMapper;
    
    @Autowired
    private BusiOperation busiOperation;
    
    @Autowired
    private WorkOrderInterface workOrderInterface;

    @Autowired
    private GatewayBusinessOpenDetailMapper gatewayBusinessOpenDetailMapper;
    
    @Autowired
    private GatewayPasswordMapper gatewayPasswordMapper;
    
    private static String NEW_INSTALLATION = "新装"; 
    private static String BROADBAND_UNSUBSCRIBE = "拆机"; 
    
    @Override
    public Map<String, Object> importWorkOrderTemplate(Map<String, Object> parameter) {
        logger.info("start invoke importWorkOrderTemplate:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        try{
            String templateMsg = (String) parameter.get("templateMessage");
            String templateName = (String) parameter.get("templateName");
            if(StringUtils.isEmpty(templateMsg)){
                logger.info("importWorkOrderTemplate templateMessage 为空");
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
                retMap.put(Constant.MESSAGE,"文件内容不能为空");
                return retMap;
            }
            if(StringUtils.isEmpty(templateName)){
                logger.info("importWorkOrderTemplate templateName 为空");
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
                retMap.put(Constant.MESSAGE,"模板名称不能为空");
                return retMap;
            }
            //先查询是否有相同名称的模板
            WorkOrderTemplateInfo workOrderTemplateInfoParam = new WorkOrderTemplateInfo();
            workOrderTemplateInfoParam.setTemplateName(templateName);
            List<WorkOrderTemplateInfo> workOrderTemplateInfoResult = workOrderTemplateInfoMapper.selectByParam(workOrderTemplateInfoParam);
            if(workOrderTemplateInfoResult!=null&&workOrderTemplateInfoResult.size()>0){
                logger.info("importWorkOrderTemplate 已经存在相同模板名称的模板，不能重复添加，templateName:{}",templateName);
                retMap.put(Constant.CODE, ErrorCodeEnum.INSERT_ERROR.getResultCode());
                retMap.put(Constant.MESSAGE,"模板名称不能重复");
                return retMap;
            }
            //TODO 验证XML文件格式
            //往数据库插入数据
            WorkOrderTemplateInfo workOrderTemplateInfo = new WorkOrderTemplateInfo();
            workOrderTemplateInfo.setId(UniqueUtil.uuid());
            workOrderTemplateInfo.setTemplateMessage(templateMsg);
            workOrderTemplateInfo.setTemplateName(templateName);
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat(DateTools.YYYY_MM_DD_HH_MM_SS);
            String createTime = df.format(cal.getTime());
            workOrderTemplateInfo.setCreateTime(createTime);
            int i = 0;
            i = workOrderTemplateInfoMapper.insertSelective(workOrderTemplateInfo);
            if(i>0){
            	OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "导入网关业务模板", JSON.toJSONString(parameter));
                retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
                retMap.put(Constant.MESSAGE,"成功");
            }else{
                retMap.put(Constant.CODE,ErrorCodeEnum.INSERT_ERROR.getResultCode() );
                retMap.put(Constant.MESSAGE,"新增失败");
            }
        }catch (Exception e){
            logger.error("导入工单模板错误!",e);
            retMap.put(Constant.CODE,ErrorCodeEnum.INSERT_ERROR.getResultCode() );
            retMap.put(Constant.MESSAGE,"新增失败");
        }
        logger.info("end invoke importWorkOrderTemplate:{}",retMap);
        return retMap;
    }

    @Override
    public Map<String, Object> queryWorkOrderList(Map<String, Object> parameter) {
        logger.info("start invoke queryWorkOrderList:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        try {
            String id = (String) parameter.get("id");
            WorkOrderTemplateInfo workOrderTemplateInfo = new WorkOrderTemplateInfo();
            if (!StringUtils.isEmpty(id)) {
                workOrderTemplateInfo.setId(id);
            }
            List<WorkOrderTemplateInfo> list =  workOrderTemplateInfoMapper.selectByParam(workOrderTemplateInfo);
            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE,"成功");
            retMap.put(Constant.DATA,list);
        }catch (Exception e){
            logger.error("查询模板错误!",e);
            retMap.put(Constant.CODE,ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE,"查询失败");
        }
        logger.info("end invoke queryWorkOrderList:{}",retMap);
        return retMap;
    }

    @Override
    public Map<String, Object> queryWorkOrderList4Page(Map<String, Object> parameter) {
        logger.info("start invoke queryWorkOrderList4Page:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        try {
            int page = parameter.get("page") == null ? 1 : Integer.valueOf(parameter.get("page") + "");
            int pageSize = parameter.get("pageSize") == null ? 10 : Integer.valueOf(parameter.get("pageSize") + "");
            Map<String, Object> parmMap = new HashMap<>();
            parmMap.put("start", (page - 1) * pageSize);
            parmMap.put("end", pageSize);
            String id = (String) parameter.get("id");
            if (!StringUtils.isEmpty(id)) {
                parmMap.put("id",id);
            }
            int count = workOrderTemplateInfoMapper.selectCountByParam(parmMap);
            List<WorkOrderTemplateInfo> list =  workOrderTemplateInfoMapper.selectByParamMap(parmMap);
            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE,"成功");
            retMap.put(Constant.DATA,list);
            retMap.put("page",page);
            retMap.put("pageSize",pageSize);
            retMap.put("total",count);
        }catch (Exception e){
            logger.error("查询模板错误!",e);
            retMap.put(Constant.CODE,ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE,"查询失败");
        }
        logger.info("end invoke queryWorkOrderList4Page:{}",retMap);
        return retMap;
    }

    @Override
    public Map<String, Object> queryGatewayOpenBusinessState(Map<String, Object> parameter) {
        logger.info("start invoke queryGatewayOpenBusinessState:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        try {
            String gatewayId = (String) parameter.get("gatewayId");
            if(StringUtils.isEmpty(gatewayId)){
                logger.info("queryGatewayOpenBusinessState gatewayId 为空");
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
                retMap.put(Constant.MESSAGE,"请选择网关");
                return retMap;
            }
            //根据网关ID查询网关信息
            GatewayInfo gatewayInfoQuery = gatewayInfoMapper.selectByPrimaryKey(gatewayId);
            if(gatewayInfoQuery == null){
                logger.info("queryGatewayOpenBusinessState gatewayId 错误");
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
                retMap.put(Constant.MESSAGE,"网关不存在");
                return retMap;
            }
            List<Map<String,Object>> list = gatewayBusinessOpenDetailMapper.selectByGatewayUuid(gatewayId);
            List<Map<String,Object>> data = new ArrayList<>();
            for(Map<String,Object> map :list){
                Map<String,Object> mapBusiness = new HashMap<>();
                String businessCode = (String) map.get("business_code_boss");
                String businessName = (String) map.get("business_name_boss");
                String businessStatu = map.get("open_status") == null?"0":(String) map.get("open_status");
                mapBusiness.put("businessCode",businessCode);
                mapBusiness.put("businessName",businessName);
                mapBusiness.put("businessStatu", businessStatu);
                data.add(mapBusiness);
            }
            retMap.put(Constant.CODE,ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, "成功");
            retMap.put(Constant.DATA, data);
        }catch (Exception e){
            logger.error("查询模板错误!",e);
            retMap.put(Constant.CODE,ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE,"查询失败");
        }
        logger.info("end invoke queryGatewayOpenBusinessState:{}",retMap);
        return retMap;
    }

    @Override
    public Map<String, Object> deleteWorkOrderTemplate(Map<String, Object> parameter) {
        logger.info("start invoke deleteWorkOrderTemplate:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        try{
            String id = (String) parameter.get("id");
            if(StringUtils.isEmpty(id)){
                logger.info("deleteWorkOrderTemplate 参数id为空");
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
                retMap.put(Constant.MESSAGE,"请选择模板");
                return retMap;
            }
            //判断该模板是否关联了业务CODE，如果关联了，则不能删除
//            BusinessCategory param = new BusinessCategory();
//            param.setBusinessTemplate(id);
            //List<BusinessCategory> result = businessCategoryMapper.findGatewayBusinessByTemp(param);
            //使用模板和业务代码的关联表进行查询
            Map<String,Object> params = new HashMap<>();
            params.put("businessTemplate", id);
            List<Map<String, Object>> result =  businessCategoryMapper.queryRelationByTemplate(params);
            if(result !=null && result.size()>0){
                logger.info("删除工单模板失败，该模板关联了业务代码，parameter:{}", parameter);
                retMap.put(Constant.CODE,ErrorCodeEnum.DELETE_ERROR.getResultCode() );
                retMap.put(Constant.MESSAGE,"删除失败,该模板关联了业务代码");
                return retMap;
            }
            int i = 0;
            i = workOrderTemplateInfoMapper.deleteByPrimaryKey(id);
            if(i>0){
            	OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "删除网关业务模板", JSON.toJSONString(parameter));
                retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
                retMap.put(Constant.MESSAGE,"成功");
            }else{
                retMap.put(Constant.CODE,ErrorCodeEnum.DELETE_ERROR.getResultCode() );
                retMap.put(Constant.MESSAGE,"删除失败");
            }
        }catch (Exception e){
            logger.error("删除工单模板错误!",e);
            retMap.put(Constant.CODE,ErrorCodeEnum.DELETE_ERROR.getResultCode() );
            retMap.put(Constant.MESSAGE,"删除失败");
        }
        logger.info("end invoke deleteWorkOrderTemplate:{}",retMap);
        return retMap;
    }

    @Override
    public Map<String, Object> updateWorkOrderTemplate(Map<String, Object> parameter) {
        logger.info("start invoke updateWorkOrderTemplate:{}",parameter);
        Map<String,Object> retMap = new HashMap<>();
        try{
            String id = (String) parameter.get("id");
            if(StringUtils.isEmpty(id)){
                logger.info("updateWorkOrderTemplate 参数id为空");
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
                retMap.put(Constant.MESSAGE,"请选择模板");
                return retMap;
            }
            WorkOrderTemplateInfo workOrderTemplateInfoQery = workOrderTemplateInfoMapper.selectByPrimaryKey(id);
            if(workOrderTemplateInfoQery == null){
                logger.info("updateWorkOrderTemplate 参数id错误，id:{}",id);
                retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
                retMap.put(Constant.MESSAGE,"模板不存在");
                return retMap;
            }
            String templateMsg = (String) parameter.get("templateMessage");
            String templateName = (String) parameter.get("templateName");
            WorkOrderTemplateInfo workOrderTemplateInfo = new WorkOrderTemplateInfo();
            if(!StringUtils.isEmpty(templateMsg)){
                workOrderTemplateInfo.setTemplateMessage(templateMsg);
            }
            if(!StringUtils.isEmpty(templateName)){
                //先查询是否有相同名称的其他模板
                WorkOrderTemplateInfo workOrderTemplateInfoParam = new WorkOrderTemplateInfo();
                workOrderTemplateInfoParam.setTemplateName(templateName);
                List<WorkOrderTemplateInfo> workOrderTemplateInfoResult = workOrderTemplateInfoMapper.selectByParam(workOrderTemplateInfoParam);
                if(workOrderTemplateInfoResult!=null&&workOrderTemplateInfoResult.size()>0){
                    boolean isExist = false;
                    for(WorkOrderTemplateInfo item: workOrderTemplateInfoResult){
                        if(!id.equals(item.getId())){
                            isExist = true;
                        }
                    }
                    if(isExist) {
                        logger.info("updateWorkOrderTemplate 已经存在相同模板名称的模板，不能重复添加，templateName:{}", templateName);
                        retMap.put(Constant.CODE, ErrorCodeEnum.UPDATE_ERROR.getResultCode());
                        retMap.put(Constant.MESSAGE, "模板名称不能重复");
                        return retMap;
                    }
                }
               workOrderTemplateInfo.setTemplateName(templateName);
            }
            workOrderTemplateInfo.setId(id);
            int i = 0;
            i = workOrderTemplateInfoMapper.updateByPrimaryKeySelective(workOrderTemplateInfo);
            if(i>0){
            	OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "修改网关业务模板", JSON.toJSONString(parameter));
                retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
                retMap.put(Constant.MESSAGE,"成功");
            }else{
                retMap.put(Constant.CODE,ErrorCodeEnum.UPDATE_ERROR.getResultCode() );
                retMap.put(Constant.MESSAGE,"更新失败");
            }
        }catch (Exception e){
            logger.error("更新工单模板错误!",e);
            retMap.put(Constant.CODE,ErrorCodeEnum.UPDATE_ERROR.getResultCode() );
            retMap.put(Constant.MESSAGE,"更新失败");
        }
        logger.info("end invoke updateWorkOrderTemplate:{}",retMap);
        return retMap;
    }
    private boolean isNotEmptyCell(Cell[] cells){
    	
    	if(cells != null && cells.length>0){
    		for(int i =0; i<cells.length; i++){
    			if(cells[i].getContents() != null && !"".equals(cells[i].getContents())){
    				return true;
    			}
    		}
    	}
    	return false;
    }
   
    
    @Override
    public Map<String, Object> importWorkOrder(Map<String, Object> parameter)
    {
        String filepath = (String)parameter.get("filePath");
        logger.info("Excel file Remote path " + filepath);
        try
        {
        	URL url = new URL(filepath.replaceAll(" ", "%20"));  
            HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
        	 Workbook book = Workbook.getWorkbook(urlCon.getInputStream());
        	 Sheet sheet = book.getSheet(0);
             int rows = sheet.getRows();
             if (rows > 0) {
            	 List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
            	 List<String> orderList = new ArrayList<String>();
            	 List<String> pppoeAccountList = new ArrayList<String>();
            	 List<String> serviceCodeLoidList = new ArrayList<String>();
            	 List<String> loidList = new ArrayList<String>();
                 for (int i = 1; i < rows; i++) {
                     Cell[] cells = sheet.getRow(i);
                     Map<String, Object> map = new HashMap<String, Object>();
                     if(cells.length < 10){
                    	 logger.info("遍历导入工单excel时，第"+ i +"行内容有误！");
                    	 return reutrnMap(RespCodeEnum.RC_1.code(), "文件第"+i+"行缺失内容");
                     }
                     if(cells[0] == null || "".equals(cells[0].getContents())){
                    	 
                    	 if(isNotEmptyCell(cells)){
                    		 logger.info("遍历导入工单excel时，第"+ i +"行第1列内容为空！");
                    		 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ i +"行第1列内容为空");
                    	 }else{
                    		 continue;
                    	 }
                     }else{
                    	 map.put("OrderNo", cells[0].getContents().toString());
                    	 orderList.add(cells[0].getContents().toString());
                     }
                     if(cells[1] == null || "".equals(cells[1].getContents())){
                    	 logger.info("遍历导入工单excel时，第"+ i +"行第2列内容为空！");
                    	 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ i +"行第2列内容为空");
                     }else{
                    	 map.put("ServiceCode", cells[1].getContents().toString());
                     }
                     if(NEW_INSTALLATION.equals(cells[9].getContents().toString()) && "wband".equals(cells[1].getContents().trim())){
	                     if(cells[2] == null || "".equals(cells[2].getContents())){
	                    	 logger.info("遍历导入工单excel时，第"+ i +"行第3列内容为空！");
	                    	 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ i +"行第3列内容为空");
	                     }else{
	                    		 
	                		 //验证业务编码和LOID组合是否重复
	                		 if(serviceCodeLoidList.contains(cells[1].getContents().toString()+"_"+cells[2].getContents().toString())){
	                			 //重复
	                			 logger.info("遍历导入工单excel时，第"+ i +"行内容LOID-ServiceCode");
	                			 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ i +"行内容网关逻辑标识，业务代码重复");
	                		 }else{
	                			 serviceCodeLoidList.add(cells[1].getContents().toString()+"_"+cells[2].getContents().toString());
	                		 }
	                		 loidList.add(cells[2].getContents().toString());
	                     }
                     }
                     map.put("LOID", cells[2].getContents());
                     
                     if(cells[3].getContents() != null && !"".equals(cells[3].getContents().toString())){
                		try {
							Integer.parseInt(cells[3].getContents().toString());
						} catch (Exception e) {
							 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ i +"行第4列区域编号只能是数字");
						}
                	 }
                     map.put("AreaCode", cells[3] == null ? "" : cells[3].getContents().toString());
                     
                     if((NEW_INSTALLATION.equals(cells[9].getContents().toString()) && "wband".equals(cells[1].getContents().trim()))
                    		|| (NEW_INSTALLATION.equals(cells[9].getContents().toString()) && "otttv".equals(cells[1].getContents().trim()))
                    		|| BROADBAND_UNSUBSCRIBE.equals(cells[9].getContents().toString()) ){
	                     if(cells[4] == null || "".equals(cells[4].getContents())){
	                    	 logger.info("遍历导入工单excel时，第"+ i +"行第5列内容为空！");
	                    	 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ i +"行第5列内容为空");
	                     }
	                     
	                     if(NEW_INSTALLATION.equals(cells[9].getContents().toString()) &&"wband".equals(cells[1].getContents().trim())){
	                    	 //新装 宽带验证宽带帐号是否被其他网关占用
	                    	 pppoeAccountList.add(cells[4].getContents().toString().trim());
	                     }
                     } 
                     map.put("PppoeAccount", cells[4].getContents());
                     
                     if(NEW_INSTALLATION.equals(cells[9].getContents().toString()) && "wband".equals(cells[1].getContents().trim())
                    		 || (NEW_INSTALLATION.equals(cells[9].getContents().toString()) && "otttv".equals(cells[1].getContents().trim()))){
                    	 
                    	 if(cells[5] == null || "".equals(cells[5].getContents())){
                    		 logger.info("遍历导入工单excel时，第"+ i +"行第6列内容为空！");
                    		 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ i +"行第6列内容为空");
                    	 }
                     }
                     map.put("PppoePassword", cells[5].getContents());
                     
                     if(NEW_INSTALLATION.equals(cells[9].getContents().toString()) && "voip".equals(cells[1].getContents().trim())){
                    	 
                    	 if(cells[6] == null || "".equals(cells[6].getContents())){
                    		 logger.info("遍历导入工单excel时，第"+ i +"行第7列内容为空！");
                    		 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ i +"行第7列内容为空");
                    	 }
                     }
                     map.put("SIPUserName", cells[6].getContents());
                     
                     if(NEW_INSTALLATION.equals(cells[9].getContents().toString()) && "voip".equals(cells[1].getContents().trim())){
                    	 
                    	 if(cells[7] == null || "".equals(cells[7].getContents())){
                    		 logger.info("遍历导入工单excel时，第"+ i +"行第8列内容为空！");
                    		 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ i +"行第8列内容为空");
                    	 }
                     }
                     map.put("SIPUserPWD", cells[7].getContents());
                	
                	 if(cells[8].getContents() != null && !"".equals(cells[8].getContents().toString())){
                		 try {
							Integer.parseInt(cells[8].getContents().toString());
						} catch (Exception e) {
							 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ i +"行第9列只能是数字");
						}
                	 }
                	 map.put("BANDWIDTH", cells[8].getContents());
                	 
                	 if(cells[9].getContents() == null || "".equals(cells[9].getContents().toString())){
                    	 
                		 logger.info("遍历导入工单excel时，第"+ i +"行第10列内容为空！");
                		 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ i +"行第10列内容为空");
                     }else{
                    	 if(!NEW_INSTALLATION.equals(cells[9].getContents().toString()) && !BROADBAND_UNSUBSCRIBE.equals(cells[9].getContents().toString())){
                    		 logger.info("遍历导入工单excel时，第"+ i +"行第9列内容错误！"+cells[9].getContents().toString());
                    		 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ i +"行第10列内容错误，只能是新装或拆机");
                    	 }else{
                    		 map.put("operType", cells[9].getContents());
                    	 }
                     }
                	 if(cells.length > 10){
                		 map.put("vlanId", cells[10].getContents());
                	 }
                	 if(cells.length > 11){
                		 map.put("vlanId_tv", cells[11].getContents());
                	 }
                     data.add(map);
                 }
                 //验证是否有重复的工单号 orderList
                 if(orderList.size() > 0){
                	 
                	 List<GatewayBusiness> result = gatewayBusinessMapper.selectByOrderNos(orderList);
                	 if(result != null && result.size()> 0){
                		 StringBuffer orders = new StringBuffer();
                		 for(GatewayBusiness gb : result){
                			 orders.append(gb.getOrderNo()+";");
                		 }
                		 logger.info("导入工单中有重复工单号："+orders.toString());
                		 return reutrnMap(RespCodeEnum.RC_1.code(), "工单号重复："+orders.toString());
                	 }
                 }else{
                	 return reutrnMap(RespCodeEnum.RC_1.code(), "文件内容为空");
                 }
                 //验证pppoeaccount是否被使用
                 if(pppoeAccountList.size()>0){
                	 List<GatewayInfo> result = gatewayInfoMapper.selectByPppoeAccounts(pppoeAccountList);
                	 if(result != null && result.size()> 0){
                		 StringBuffer pppoe = new StringBuffer();
                		 for(GatewayInfo gb : result){
                			 pppoe.append(gb.getGatewayAdslAccount()+";");
                		 }
                		 logger.info("导入工单中新装宽带帐号在网关表中已经被使用："+pppoe.toString());
                		 return reutrnMap(RespCodeEnum.RC_1.code(), "导入工单中操作类型（新装）业务代码（wband）宽带帐号在网关表中已经被使用："+pppoe.toString());
                	 }
                 }
                 //查询工单表中是否有未失效的loid
                 if(loidList.size()>0){
                	 List<GatewayBusiness> result = gatewayBusinessMapper.selectByLoids(loidList);
                	 if(result != null && result.size()> 0){
                		 StringBuffer orders = new StringBuffer();
                		 for(GatewayBusiness gb : result){
                			 orders.append(gb.getOrderNo()+";");
                		 }
                		 logger.info("导入工单中业务类型为wband的以下工单号网关逻辑标识已经存在："+orders.toString());
                		 return reutrnMap(RespCodeEnum.RC_1.code(), "导入业务类型为wband工单中的以下工单号网关逻辑标识已经存在："+orders.toString());
                	 }
                 }
                 
                 Map<String, Object> rParam = new HashMap<String, Object>();
                 
                 Map<String, Object> p = new HashMap<String, Object>();
                 for(int i=0; i < data.size(); i++){
                	 Map<String, Object> m = data.get(i);
                	 if("新装".equals(m.get("operType").toString())){
                		 rParam.put("CmdType", "NEW_INSTALLATION");
                		 List<Map<String, Object>> newInstalls = new ArrayList<Map<String,Object>>();
                		 newInstalls.add(m);
                		 
                		 p.put("NewInstallationArray", newInstalls);
                		 rParam.put("Parameter", p);
                		 
                		 Map<String, Object> resultMap = workOrderInterface.addNewInstallation(rParam);
                		 logger.info("调用新装接口请求参数："+rParam+"；返回结果："+resultMap);
                		 if(!"0".equals(resultMap.get("Result").toString())){
                			 Object ResultData = resultMap.get("ResultData");
                			 String reason="";
                			 if(ResultData != null){
                				 Map<String, Object> rd =  (Map<String, Object>)ResultData;
                				 reason = rd.get("FailReason") == null ? "" : rd.get("FailReason").toString();
                			 }
                			 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ (i+1) +"行数据错误，原因："+reason);
                		 }
                	 }else if("拆机".equals(m.get("operType").toString())){
                		 rParam.put("CmdType", "BROADBAND_UNSUBSCRIBE");
                		 Map<String, Object> pa = new HashMap<String,Object>();
                		 pa.put("OrderNo", m.get("OrderNo"));
                		 pa.put("ServiceCode", m.get("ServiceCode"));
                		 pa.put("PppoeAccount", m.get("PppoeAccount"));
                		 rParam.put("Parameter", pa);
                		 
                		 Map<String, Object> resultMap = workOrderInterface.broadBandUnsubcribe(rParam);
                		 logger.info("调用拆机接口请求参数："+rParam+"；返回结果："+resultMap);
                		 if(!"0".equals(resultMap.get("Result").toString())){
                			 Object ResultData = resultMap.get("ResultData");
                			 String reason="";
                			 if(ResultData != null){
                				 Map<String, Object> rd =  (Map<String, Object>)ResultData;
                				 reason = rd.get("FailReason") == null ? "" : rd.get("FailReason").toString();
                			 }
                			 return reutrnMap(RespCodeEnum.RC_1.code(), "第"+ (i+1) +"行数据错误，原因："+reason);
                		 }
                	 }
                	 
                 }
                 OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.WORKORDER_MANAGER_SERVICE, "网关工单导入", JSON.toJSONString(parameter));
                 return reutrnMap(RespCodeEnum.RC_0.code(), "导入成功");
             }else{
            	 logger.info("文件为空");
            	 return reutrnMap(RespCodeEnum.RC_1.code(), "文件内容为空");
             }
        	 
        }catch (Exception e){
            String msg = "上传失败,系统异常错误!" + e.toString();
            logger.info(msg);
            return reutrnMap(RespCodeEnum.RC_ERROR.code(), msg);
        }
		
    }
   
    public Map<String, Object> reutrnMap(String resultCode, String resultMsg)
    {
        Map returnMap = new HashMap();
        returnMap.put("resultCode", resultCode);
        returnMap.put("resultMsg", resultMsg);
        return returnMap;
    }
    @Override
    public Map<String, Object> updateWorkOrder(Map<String, Object> parameter)
    {
    	String order_no = parameter.get("order_no") == null ? "" : parameter.get("order_no").toString() ;
        try
        {
        	if("".equals(order_no)){
    			return reutrnMap(RespCodeEnum.RC_1002.code(), "order_no为空");
    		}
    		List<String> orderNos = new ArrayList<String>();
    		orderNos.add(order_no);
    		List<GatewayBusiness> gbs = gatewayBusinessMapper.selectByOrderNos(orderNos);
    		if(gbs == null || gbs.size() == 0){
    			return reutrnMap(RespCodeEnum.RC_1.code(), "没有对应工单");
    		}
    		GatewayBusiness gb = gbs.get(0);
    		if(!"0".equals(gb.getBusinessStatu())){
    			return reutrnMap(RespCodeEnum.RC_1.code(), "工单已经执行，不能编辑");
    		}
    		if(parameter.get("create_time") != null){
    			gb.setCreateTime(Integer.parseInt(parameter.get("create_time").toString()));
    		}
    		if(parameter.get("adsl_account") != null){
    			//修改后的宽带账号不能为已经存在的
    			String adsl = parameter.get("adsl_account").toString();
    			//与本条数据不同才去查询
    			if (!gbs.get(0).getAdslAccount().equals(adsl)) {
    				GatewayBusiness gatewayBusiness = new GatewayBusiness();
    				gatewayBusiness.setAdslAccount(adsl);
    				List<GatewayBusiness> list = gatewayBusinessMapper.selectByParam(gatewayBusiness);
    				if (list!=null&&!list.isEmpty()) {
    					return reutrnMap(RespCodeEnum.RC_1.code(), "宽带账号重复:"+adsl);
					}
				}
    			
    			gb.setAdslAccount(parameter.get("adsl_account").toString());
    		}
    		if(parameter.get("adsl_password") != null){
    			gb.setAdslPassword(parameter.get("adsl_password").toString());
    		}
    		if(parameter.get("business_code_boss") != null){
    			gb.setBusinessCodeBoss(parameter.get("business_code_boss").toString());
    		}
    		if(parameter.get("areacode") != null){
    			gb.setAreacode(parameter.get("areacode").toString());
    		}
    		if(parameter.get("bandwidth") != null){
    			String bandwidth = "0";
    			if("".equals(parameter.get("bandwidth"))){
    				bandwidth = "0";
    			}else{
    				bandwidth = parameter.get("bandwidth").toString();
    			}
    			gb.setBandwidth(Integer.parseInt(bandwidth));
    		}
    		
    		gatewayBusinessMapper.updateByPrimayKey(gb);
    		//宽带新装工单才修改
    		if (gb.getBusinessCode().startsWith("wband")&&parameter.get("adsl_account") != null
    				&&"1".equals(gb.getBusinessStatu())){
    			GatewayPassword record = new GatewayPassword();
        		record.setGatewayPassword(gb.getGatewayPassword());
        		record.setAdslAccount(gb.getAdslAccount());
        		gatewayPasswordMapper.updateByPrimaryKeySelective(record);
			}
    		OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.WORKORDER_MANAGER_SERVICE, "编辑网关工单", JSON.toJSONString(parameter));
    		return reutrnMap(RespCodeEnum.RC_0.code(), "更新成功"); 
    		
        }catch (Exception e){
            String msg = "系统异常错误!" + e.toString();
            logger.info(msg);
            return reutrnMap(RespCodeEnum.RC_ERROR.code(), msg);
        }
    }
    
    @Override
    public Map<String, Object> deleteWorkOrder(Map<String, Object> parameter)
    {
    	String order_no = parameter.get("order_no") == null ? "" : parameter.get("order_no").toString() ;
    	try
    	{
    		if("".equals(order_no)){
    			return reutrnMap(RespCodeEnum.RC_1002.code(), "order_no为空");
    		}
    		List<String> orderNos = new ArrayList<String>();
    		orderNos.add(order_no);
    		List<GatewayBusiness> gbs = gatewayBusinessMapper.selectByOrderNos(orderNos);
    		if(gbs == null || gbs.size() == 0){
    			return reutrnMap(RespCodeEnum.RC_1.code(), "没有对应工单");
    		}
    		if(!"0".equals(gbs.get(0).getBusinessStatu())){
    			return reutrnMap(RespCodeEnum.RC_1.code(), "工单已经执行，不能删除");
    		}
    		
    		//删掉工单记录
    		gatewayBusinessMapper.deleteByPrimaryKey(gbs.get(0).getId());
    		//删掉gatewayPassword记录  只能用orderNo 不能用password
    		gatewayPasswordMapper.deleteByOrderNo(gbs.get(0).getOrderNo());
    		OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.WORKORDER_MANAGER_SERVICE, "删除网关工单", JSON.toJSONString(parameter));
    		return reutrnMap(RespCodeEnum.RC_0.code(), "删除成功");
    	}catch (Exception e){
    		String msg = "系统异常错误!" + e.toString();
    		logger.info(msg);
    		return reutrnMap(RespCodeEnum.RC_ERROR.code(), msg);
    	}
    	
    }

	@Override
	@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Map<String, Object> reExecuteWorkOrder(Map<String, Object> parameter) {
		//默认失败
		String resultMsg = null;
		
		if (StringUtils.isEmpty((String)parameter.get("orderNo"))) {
			resultMsg = "工单号不能为空";
		}else if (StringUtils.isEmpty((String)parameter.get("gatewaySerialnumber"))) {
			resultMsg = "网关SN不能为空";
		}
		
		if (StringUtils.isNotEmpty(resultMsg)) {
			return reutrnMap(RespCodeEnum.RC_1002.code(), resultMsg);
		}
		
		//根据工单号查询工单  根据网关SN查询网关对象
		GatewayBusiness gatewayBusiness = new GatewayBusiness();
		gatewayBusiness.setBusinessStatu("2");
		gatewayBusiness.setOrderNo((String)parameter.get("orderNo"));
		List<GatewayBusiness> businessList =  gatewayBusinessMapper.selectByParam(gatewayBusiness);
		
		GatewayInfo gatewayInfo = new GatewayInfo();
		gatewayInfo.setGatewaySerialnumber((String)parameter.get("gatewaySerialnumber"));
		GatewayInfo gatewayInfo2 = gatewayInfoMapper.selectGatewayInfo(gatewayInfo);
		
		if (businessList==null||businessList.isEmpty()||gatewayInfo2==null) {
			return reutrnMap(RespCodeEnum.RC_1.code(), "参数非法");
		}
		
		//更新工单状态  工单号  网关pw
		gatewayBusiness.setBusinessStatu("3");
		int update = gatewayBusinessMapper.updateStatusByOrderno(gatewayBusiness);
		if (update==0) {
			resultMsg = "工单已经被执行,请刷新页面";
			return reutrnMap(RespCodeEnum.RC_1.code(), resultMsg);
		}
		
		//调用线程去执行开通该网关的业务开通,并提交线程池
		WorkerThreadPool.getInstance().submitTask(new WorkOrderForTaskThread(businessList,gatewayInfo2));
		
		return reutrnMap(RespCodeEnum.RC_0.code(), "重新执行指令发送成功");
	}
    
}
