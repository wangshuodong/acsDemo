/**
 * 
 */
package com.cmiot.rms.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSON;
import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AdminService;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.dao.mapper.GatewayBusinessBeanMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessExecuteHistoryMapper;
import com.cmiot.rms.dao.model.GatewayBusinessExecuteHistory;
import com.cmiot.rms.dao.model.derivedclass.GatewayBusinessBean;
import com.cmiot.rms.services.GatewayBusinessService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

/**
 * @author heping
 *
 */
public class GatewayBusinessServiceImpl implements GatewayBusinessService {

	private static final Logger logger = LoggerFactory.getLogger(GatewayBusinessServiceImpl.class);

	@Autowired
	GatewayBusinessBeanMapper gatewayBusinessBeanMapper;
	
	@Autowired
	GatewayBusinessExecuteHistoryMapper gatewayBusinessExecuteHistoryMapper;

	@Autowired
    private AreaService amsAreaService;
	
	@Autowired
    AdminService adminService;

	@Autowired
    AreaService areaService;

	@Value("${work.order.fail.count}")
    int workOrderFailCount;

	@Override
	public Map<String, Object> queryList4Page(Map<String, Object> map) {
		logger.info("queryList4Page,paramters:{}", map.toString());
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			GatewayBusinessBean gatewayBusinessBean = new GatewayBusinessBean();
			generateBean(map, gatewayBusinessBean);
			gatewayBusinessBean.setFailCount(workOrderFailCount);
			
			//按照用户区域查询工单
            if (map.get("uid") != null && !"".equals(map.get("uid").toString().trim())) {
                List<com.cmiot.ams.domain.Area> userAreaList = areaService.findAreaByAdmin((String) map.get("uid"));
                if (userAreaList != null && !userAreaList.isEmpty()) {
                    //查询条件 区域编号 不为空
                    if (map.get("areaCode") != null
                            && !"".equals(map.get("areaCode").toString().trim())) {
                        List<com.cmiot.ams.domain.Area> areaList = areaService.findChildArea(Integer.parseInt((String) map.get("areaCode")));
                        if (areaList != null && areaList.size() > 0) {
                            StringBuffer sb = new StringBuffer();
                            sb.append("(");
                            for (int j = 0; j < userAreaList.size(); j++) {
                                for (int i = 0; i < areaList.size(); i++) {
                                    if(userAreaList.get(j).getId().equals(areaList.get(i).getId())){
                                        sb.append("," + areaList.get(i).getId());
                                    }
                                }
                            }
                            sb.append(")");
                            String par = sb.toString().replaceFirst(",", "");
                            gatewayBusinessBean.setAreacode(par);
                        }
                    }else{//使用用户的区域ID

                            StringBuffer sbNoArea = new StringBuffer();
                            sbNoArea.append("(");
                            for (int j = 0; j < userAreaList.size(); j++) {
                                sbNoArea.append("," + userAreaList.get(j).getId());
                            }
                            sbNoArea.append(")");
                            String par = sbNoArea.toString().replaceFirst(",", "");
                            gatewayBusinessBean.setAreacode(par);
                    }

                }else{
                	 gatewayBusinessBean.setAreacode("("+"'"+"'"+")");
                }
            }

			int page = (null != map.get("page")) ? Integer.valueOf(map.get("page").toString()) : 1;
			int pageSize = (null != map.get("pageSize")) ? Integer.valueOf(map.get("pageSize").toString()) : 10;
			PageHelper.startPage(page, pageSize);
			
			List<GatewayBusinessBean> list = gatewayBusinessBeanMapper.queryList4Page(gatewayBusinessBean);			
			for(GatewayBusinessBean info : list)
 	        {
 				String  gatewayAreaId = info.getAreacode();
 	            if(StringUtils.isNotBlank(gatewayAreaId))
 	            {
 	    			Area area = amsAreaService.findAreaById(Integer.valueOf(gatewayAreaId));
 	                if(null != area)
 	                {
 	                    info.setGatewayAreaName(area.getName());
 	                    info.setAreacode(area.getCode());
 	                }
 	            }
 	            //工单执行失败达到最大次数
 	            if ("2".equals(info.getBusiness_statu())&&info.getFailCount() != null) {
 	            	if (info.getFailCount()>= workOrderFailCount) {
 	            		//0不能重新执行 1可以重新执行
 	 	            	info.setReExecute(1);
					}
				}
 	            if("1".equals(info.getBusiness_statu()) || "4".equals(info.getBusiness_statu())){
 	            	//已完成
 	            	info.setBusiness_statu("1");
 	            }else{
 	            	//新装或修改或订户密码变更  返回未完成
 	            	if("1".equals(info.getBusiness_type()) || "6".equals(info.getBusiness_type()) || "9".equals(info.getBusiness_type())){
 	            		info.setBusiness_statu("0");
 	            	}else if("8".equals(info.getBusiness_type()) && info.getFailCount() != null && info.getFailCount() >= workOrderFailCount ){
 	            		//拆机， 执行次数大于最大执行次数，返回已完成
 	            		info.setBusiness_statu("1");
 	            	}else if("8".equals(info.getBusiness_type()) && (info.getFailCount() == null || info.getFailCount() < workOrderFailCount )){
 	            		//拆机， 执行次数小于最大执行次数，返回未完成
 	            		info.setBusiness_statu("0");
 	            	}else{
 	            		//其他不需要执行工单的，都返回已完成
 	            		info.setBusiness_statu("1");
 	            	}
 	            }
 	            

 	        }
			resultMap.put("page", page);
			resultMap.put("pageSize", pageSize);
			resultMap.put("total", ((Page) list).getTotal());
			resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			resultMap.put(Constant.MESSAGE, "工单管理查询");

			resultMap.put(Constant.DATA, JSON.toJSON(list));
		} catch (Exception e) {
			logger.info("工单管理查询失败.{}", e);
			resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			resultMap.put(Constant.MESSAGE, "工单管理查询失败");
			resultMap.put(Constant.DATA, null);

			return resultMap;
		}
		return resultMap;
	}

	private void generateBean(Map<String, Object> map, GatewayBusinessBean gatewayBusinessBean) {

		String mac = (String) map.get("gatewayMacaddress");
		String gatewaySerialnumber = (String) map.get("gatewaySerialnumber");
		String adsl_account = (String) map.get("adsl_account");
		String order_no = (String) map.get("order_no");
		String business_statu = (String) map.get("business_statu");

		if (StringUtils.isNotBlank(mac)) {
			gatewayBusinessBean.setGatewayMacaddress(mac);
		}

		if (StringUtils.isNotBlank(gatewaySerialnumber)) {
			gatewayBusinessBean.setGatewaySerialnumber(gatewaySerialnumber);
		}

		if (StringUtils.isNotBlank(adsl_account)) {
			gatewayBusinessBean.setAdsl_account(adsl_account);
		}

		if (StringUtils.isNotBlank(order_no)) {
			gatewayBusinessBean.setOrder_no(order_no);
		}

		if (StringUtils.isNotBlank(business_statu)) {
			gatewayBusinessBean.setBusiness_statu(business_statu);
		}

	}

	@Override
	public Map<String, Object> queryBusinessDetail(Map<String, Object> map) {
		logger.info("查询工单详情：参数：{}",map.toString());
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String orderNo = (String)map.get("orderNo");
		if(StringUtils.isBlank(orderNo))
		{
			logger.error("工单号不能为空");
			resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			resultMap.put(Constant.MESSAGE, "工单号不能为空");
			return resultMap;
		}
		
		try
		{
			GatewayBusinessBean bean = gatewayBusinessBeanMapper.queryBusinessDetail(orderNo);
			List<GatewayBusinessExecuteHistory> executeHistoryList = gatewayBusinessExecuteHistoryMapper.queryExecuteHisotry(orderNo);
			bean.setExecuteHistoryList(executeHistoryList);
			//将areaCode字段存储的areaId值替换为Code
			if(StringUtils.isNotEmpty(bean.getAreacode())){
				Area area = areaService.findAreaById(Integer.valueOf(bean.getAreacode()));
				bean.setAreacode(area == null ? "" : area.getCode());
			}
            //工单执行失败达到最大次数
            if ("2".equals(bean.getBusiness_statu())&&bean.getFailCount()> workOrderFailCount) {
            	//0不能重新执行 1可以重新执行
            	bean.setReExecute(1);
			}

            if("1".equals(bean.getBusiness_statu()) || "4".equals(bean.getBusiness_statu())){
	            	//已完成
            	bean.setBusiness_statu("1");
	            }else{
	            	//新装或修改或订户密码变更  返回未完成
	            	if("1".equals(bean.getBusiness_type()) || "6".equals(bean.getBusiness_type()) || "9".equals(bean.getBusiness_type())){
	            		bean.setBusiness_statu("0");
	            	}else if("8".equals(bean.getBusiness_type()) && bean.getFailCount() != null && bean.getFailCount() >= workOrderFailCount ){
	            		//拆机， 执行次数大于最大执行次数，返回已完成
	            		bean.setBusiness_statu("1");
	            	}else if("8".equals(bean.getBusiness_type()) && (bean.getFailCount() == null || bean.getFailCount() < workOrderFailCount )){
	            		//拆机， 执行次数小于最大执行次数，返回未完成
	            		bean.setBusiness_statu("0");
	            	}else{
	            		//其他不需要执行工单的，都返回已完成
	            		bean.setBusiness_statu("1");
	            	}
	            }
            
            
			resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			resultMap.put(Constant.MESSAGE, "工单详情查询");
			resultMap.put(Constant.DATA, JSON.toJSON(bean));
		}
		catch(Exception e)
		{
			logger.error("查询工单详情失败.{}", e.toString());
			resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			resultMap.put(Constant.MESSAGE, "查询工单详情失败");
			resultMap.put(Constant.DATA, null);
			return resultMap;
		}
		return resultMap;
	}

}
