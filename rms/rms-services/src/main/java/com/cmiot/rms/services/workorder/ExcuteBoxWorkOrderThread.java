package com.cmiot.rms.services.workorder;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.dao.mapper.*;
import com.cmiot.rms.dao.model.*;
import com.cmiot.rms.services.workorder.impl.BoxBusiOperation;
import com.cmiot.rms.services.workorder.impl.BusiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lcs on 2016/9/21.
 */
public class ExcuteBoxWorkOrderThread implements Runnable,Serializable {

    private static final long serialVersionUID = 1L;

    private BoxBusiness boxBusiness;

    private BoxBusiOperation boxBusiOperation;

    private BoxBusinessTemplateRelationMapper boxBusinessTemplateRelationMapper;
    
    private BoxBusinessMapper boxBusinessMapper;
    
    private BoxBusinessExecuteHistoryMapper boxBusinessExecuteHistoryMapper;


    private Logger logger = LoggerFactory.getLogger(ExcuteBoxWorkOrderThread.class);

    public ExcuteBoxWorkOrderThread(BoxBusiness boxBusiness){
    	
    	this.boxBusiness = boxBusiness;
    	
        this.boxBusiOperation = (BoxBusiOperation) SpringApplicationContextHolder.getSpringBean("boxBusiOperation");
        this.boxBusinessTemplateRelationMapper = (BoxBusinessTemplateRelationMapper)SpringApplicationContextHolder.getSpringBean("boxBusinessTemplateRelationMapper");
        this.boxBusinessMapper =  (BoxBusinessMapper) SpringApplicationContextHolder.getSpringBean("boxBusinessMapper");
        this.boxBusinessExecuteHistoryMapper = (BoxBusinessExecuteHistoryMapper) SpringApplicationContextHolder.getSpringBean("boxBusinessExecuteHistoryMapper");
    }
    @Override
    public void run() {
        try {
            logger.info("start invoke ExcuteBoxWorkOrder,北向接口触发");
            
            //根据工单业务编码查询模板来执行
            Map<String,String> temMap = boxBusinessTemplateRelationMapper.getBoxTemplate(boxBusiness.getBusinessCode());
            if (temMap==null||temMap.get("template_message")==null) {
            	
            	//把工单状态改为执行失败
            	boxBusiness.setBusinessStatu("2");
            	
                if (null == boxBusiness.getFailCount()) {
                	boxBusiness.setFailCount(1);
                } else {
                	boxBusiness.setFailCount(boxBusiness.getFailCount() + 1);
                }
                
                boxBusinessMapper.updateByPrimaryKeySelective(boxBusiness);
                
            	logger.info("工单的模板类容为空,boxBusiness id:{},businessCode:{}",boxBusiness.getId(),boxBusiness.getBusinessCode());
            	return;
			}
            
            Map<String, String> resultMap = new HashMap<>();
            String parameterList = boxBusiness.getParameterList();
            Map<String, Object> parameterMap = JSON.parseObject(parameterList, Map.class);
            parameterMap.put("boxMac", boxBusiness.getBoxMac());
            /*//执行之前把工单状态改为执行中*/
            boxBusiness.setBusinessStatu("3");
            boxBusinessMapper.updateByPrimaryKeySelective(boxBusiness);
            //执行工单
            resultMap = boxBusiOperation.excute(temMap.get("template_message"), parameterMap);
            
            //添加工单执行记录
            BoxBusinessExecuteHistory boxhistory = new BoxBusinessExecuteHistory();
            boxhistory.setOrderNo(boxBusiness.getOrderNo());
            
            if ("0".equals(resultMap.get("MSG_CODE"))) {

            	//执行成功
                boxBusiness.setBusinessStatu("1");
                boxhistory.setExecuteStatus(1);
                
            } else {
                //执行失败
            	boxBusiness.setBusinessStatu("2");
                if (null == boxBusiness.getFailCount()) {
                	boxBusiness.setFailCount(1);
                } else {
                	boxBusiness.setFailCount(boxBusiness.getFailCount() + 1);
                }
                
                boxhistory.setExecuteStatus(2);
            }
            
            boxhistory.setExecuteTime(DateTools.getCurrentSecondTime());
            //更新工单状态
            boxBusinessMapper.updateByPrimaryKeySelective(boxBusiness);
            //写操作记录
            boxBusinessExecuteHistoryMapper.insert(boxhistory);
            
        }catch (Exception e){
            logger.error("执行机顶盒工单报错,Exception:{}", e);
        }
    }
}
