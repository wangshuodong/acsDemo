package com.cmiot.rms.services.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.cmiot.ams.domain.Area;
import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BoxBusinessCategoryMapper;
import com.cmiot.rms.dao.mapper.BoxBusinessExecuteHistoryMapper;
import com.cmiot.rms.dao.mapper.BoxWorkOrderMapper;
import com.cmiot.rms.dao.mapper.BoxWorkOrderTemplateInfoMapper;
import com.cmiot.rms.dao.model.BoxBusinessExecuteHistory;
import com.cmiot.rms.dao.model.BoxWorkOrder;
import com.cmiot.rms.dao.model.BoxWorkOrderTemplateInfo;
import com.cmiot.rms.services.BoxWorkOrderManagerService;
import com.cmiot.rms.services.util.OperationLogUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

public class BoxWorkOrderManagerServiceImpl implements BoxWorkOrderManagerService {
	private Logger logger = LoggerFactory.getLogger(BoxWorkOrderManagerServiceImpl.class);
	@Autowired
	private BoxWorkOrderTemplateInfoMapper boxWorkOrderTemplateInfoMapper;
	@Autowired
	private BoxBusinessCategoryMapper boxBusinessCategoryMapper;

	@Autowired
	private BoxWorkOrderMapper boxWorkOrderMapper;

	@Autowired
	private AreaService amsAreaService;
	
	@Autowired
	private BoxBusinessExecuteHistoryMapper boxBusinessExecuteHistoryMapper;

	@Override
	public Map<String, Object> importBoxWorkOrderTemplate(Map<String, Object> parameter) {
		logger.info("start invoke importBoxWorkOrderTemplate:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String templateMsg = (String) parameter.get("templateMessage");
			String templateName = (String) parameter.get("templateName");
			if (StringUtils.isEmpty(templateMsg)) {
				logger.info("importBoxWorkOrderTemplate templateMessage 为空");
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, "文件内容不能为空");
				return retMap;
			}
			if (StringUtils.isEmpty(templateName)) {
				logger.info("importWorkOrderTemplate templateName 为空");
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, "模板名称不能为空");
				return retMap;
			}
			// 先查询是否有相同名称的模板
			BoxWorkOrderTemplateInfo workOrderTemplateInfoParam = new BoxWorkOrderTemplateInfo();
			workOrderTemplateInfoParam.setTemplateName(templateName);
			List<BoxWorkOrderTemplateInfo> workOrderTemplateInfoResult = boxWorkOrderTemplateInfoMapper
					.selectByParam(workOrderTemplateInfoParam);
			if (workOrderTemplateInfoResult != null && workOrderTemplateInfoResult.size() > 0) {
				logger.info("importBoxWorkOrderTemplate 已经存在相同模板名称的模板，不能重复添加，templateName:{}", templateName);
				retMap.put(Constant.CODE, ErrorCodeEnum.INSERT_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, "模板名称不能重复");
				return retMap;
			}
			// TODO 验证XML文件格式
			// 往数据库插入数据
			BoxWorkOrderTemplateInfo workOrderTemplateInfo = new BoxWorkOrderTemplateInfo();
			workOrderTemplateInfo.setId(UniqueUtil.uuid());
			workOrderTemplateInfo.setTemplateMessage(templateMsg);
			workOrderTemplateInfo.setTemplateName(templateName);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat df = new SimpleDateFormat(DateTools.YYYY_MM_DD_HH_MM_SS);
			String createTime = df.format(cal.getTime());
			workOrderTemplateInfo.setCreateTime(createTime);
			int i = 0;
			i = boxWorkOrderTemplateInfoMapper.insertSelective(workOrderTemplateInfo);
			if (i > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, "成功");
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "导入机顶盒业务模板", JSON.toJSONString(parameter));
			} else {
				retMap.put(Constant.CODE, ErrorCodeEnum.INSERT_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, "新增失败");
			}
		} catch (Exception e) {
			logger.error("导入机顶盒工单模板错误!", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.INSERT_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, "新增失败");
		}
		logger.info("end invoke importBoxWorkOrderTemplate:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> queryBoxWorkOrderList(Map<String, Object> parameter) {
		logger.info("start invoke queryBoxWorkOrderList:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String id = (String) parameter.get("id");
			BoxWorkOrderTemplateInfo workOrderTemplateInfo = new BoxWorkOrderTemplateInfo();
			if (!StringUtils.isEmpty(id)) {
				workOrderTemplateInfo.setId(id);
			}
			List<BoxWorkOrderTemplateInfo> list = boxWorkOrderTemplateInfoMapper.selectByParam(workOrderTemplateInfo);
			retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
			retMap.put(Constant.MESSAGE, "成功");
			retMap.put(Constant.DATA, list);
		} catch (Exception e) {
			logger.error("查询模板错误!", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, "查询失败");
		}
		logger.info("end invoke queryBoxWorkOrderList:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> queryBoxWorkOrderList4Page(Map<String, Object> parameter) {
		logger.info("start invoke queryBoxWorkOrderList4Page:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			int page = parameter.get("page") == null ? 1 : Integer.valueOf(parameter.get("page") + "");
			int pageSize = parameter.get("pageSize") == null ? 10 : Integer.valueOf(parameter.get("pageSize") + "");
			Map<String, Object> parmMap = new HashMap<>();
			parmMap.put("start", (page - 1) * pageSize);
			parmMap.put("end", page * pageSize);
			String id = (String) parameter.get("id");
			if (!StringUtils.isEmpty(id)) {
				parmMap.put("id", id);
			}
			int count = boxWorkOrderTemplateInfoMapper.selectCountByParam(parmMap);
			List<BoxWorkOrderTemplateInfo> list = boxWorkOrderTemplateInfoMapper.selectByParamMap(parmMap);
			retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
			retMap.put(Constant.MESSAGE, "成功");
			retMap.put(Constant.DATA, list);
			retMap.put("page", page);
			retMap.put("pageSize", pageSize);
			retMap.put("total", count);
		} catch (Exception e) {
			logger.error("查询机顶盒模板错误!", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, "查询失败");
		}
		logger.info("end invoke queryBoxWorkOrderList4Page:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> deleteBoxWorkOrderTemplate(Map<String, Object> parameter) {
		logger.info("start invoke deleteBoxWorkOrderTemplate:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String id = (String) parameter.get("id");
			if (StringUtils.isEmpty(id)) {
				logger.info("deleteWorkOrderTemplate 参数id为空");
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, "请选择模板");
				return retMap;
			}
			// 使用模板和业务编码的关联表进行查询
			Map<String, Object> params = new HashMap<>();
			params.put("businessTemplate", id);
			List<Map<String, Object>> result = boxBusinessCategoryMapper.queryBusinessForList(params);
			if (result != null && result.size() > 0) {
				logger.info("删除工单模板失败，该模板关联了业务代码，parameter:{}", parameter);
				retMap.put(Constant.CODE, ErrorCodeEnum.DELETE_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, "删除失败,该模板关联了业务编码");
				return retMap;
			}
			int i = 0;
			i = boxWorkOrderTemplateInfoMapper.deleteByPrimaryKey(id);
			if (i > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, "成功");
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "删除机顶盒业务模板", JSON.toJSONString(parameter));
			} else {
				retMap.put(Constant.CODE, ErrorCodeEnum.DELETE_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, "删除失败");
			}
		} catch (Exception e) {
			logger.error("删除盒子工单模板错误!", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.DELETE_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, "删除失败");
		}
		logger.info("end invoke deleteBoxWorkOrderTemplate:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> updateBoxWorkOrderTemplate(Map<String, Object> parameter) {
		logger.info("start invoke updateBoxWorkOrderTemplate:{}", parameter);
		Map<String, Object> retMap = new HashMap<>();
		try {
			String id = (String) parameter.get("id");
			if (StringUtils.isEmpty(id)) {
				logger.info("updateBoxWorkOrderTemplate 参数id为空");
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, "请选择模板");
				return retMap;
			}
			BoxWorkOrderTemplateInfo workOrderTemplateInfoQery = boxWorkOrderTemplateInfoMapper.selectByPrimaryKey(id);
			if (workOrderTemplateInfoQery == null) {
				logger.info("updateBoxWorkOrderTemplate 参数id错误，id:{}", id);
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, "模板不存在");
				return retMap;
			}
			String templateMsg = (String) parameter.get("templateMessage");
			String templateName = (String) parameter.get("templateName");
			BoxWorkOrderTemplateInfo workOrderTemplateInfo = new BoxWorkOrderTemplateInfo();
			if (!StringUtils.isEmpty(templateMsg)) {
				workOrderTemplateInfo.setTemplateMessage(templateMsg);
			}
			if (!StringUtils.isEmpty(templateName)) {
				// 先查询是否有相同名称的其他模板
				BoxWorkOrderTemplateInfo workOrderTemplateInfoParam = new BoxWorkOrderTemplateInfo();
				workOrderTemplateInfoParam.setTemplateName(templateName);
				List<BoxWorkOrderTemplateInfo> workOrderTemplateInfoResult = boxWorkOrderTemplateInfoMapper
						.selectByParam(workOrderTemplateInfoParam);
				if (workOrderTemplateInfoResult != null && workOrderTemplateInfoResult.size() > 0) {
					boolean isExist = false;
					for (BoxWorkOrderTemplateInfo item : workOrderTemplateInfoResult) {
						if (!id.equals(item.getId())) {
							isExist = true;
						}
					}
					if (isExist) {
						logger.info("updateBoxWorkOrderTemplate 已经存在相同模板名称的模板，不能重复添加，templateName:{}", templateName);
						retMap.put(Constant.CODE, ErrorCodeEnum.UPDATE_ERROR.getResultCode());
						retMap.put(Constant.MESSAGE, "模板名称不能重复");
						return retMap;
					}
				}
				workOrderTemplateInfo.setTemplateName(templateName);
			}
			workOrderTemplateInfo.setId(id);
			int i = 0;
			i = boxWorkOrderTemplateInfoMapper.updateByPrimaryKeySelective(workOrderTemplateInfo);
			if (i > 0) {
				retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
				retMap.put(Constant.MESSAGE, "成功");
				OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "修改机顶盒业务模板", JSON.toJSONString(parameter));
			} else {
				retMap.put(Constant.CODE, ErrorCodeEnum.UPDATE_ERROR.getResultCode());
				retMap.put(Constant.MESSAGE, "更新失败");
			}
		} catch (Exception e) {
			logger.error("更新机顶盒工单模板错误!", e);
			retMap.put(Constant.CODE, ErrorCodeEnum.UPDATE_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, "更新失败");
		}
		logger.info("end invoke updateBoxWorkOrderTemplate:{}", retMap);
		return retMap;
	}

	@Override
	public Map<String, Object> delBoxWorkOrder(Map<String, Object> parameter) {
		logger.info("start invoke delBoxWorkOrder:{}", parameter);

		Map<String, Object> retMap = new HashMap<>();
		try {
			String order_no = (String) parameter.get("order_no");

			if (StringUtils.isBlank(order_no)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, "order_no 为空");
				return retMap;
			}
			OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.WORKORDER_MANAGER_SERVICE, "删除机顶盒工单", JSON.toJSONString(parameter));
			boxWorkOrderMapper.deleteBoxWorkOrder(order_no);
		} catch (Exception e) {
			logger.error(e.toString());
			retMap.put(Constant.CODE, ErrorCodeEnum.DELETE_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, "删除错误");
			return retMap;
		}

		retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
		retMap.put(Constant.MESSAGE, "成功");
		return retMap;
	}

	@Override
	public Map<String, Object> updateBoxWorkOrder(Map<String, Object> parameter) {
		logger.info("start invoke updateBoxWorkOrder:{}", parameter);

		Map<String, Object> retMap = new HashMap<>();
		try {
			String order_no = (String) parameter.get("order_no");
			if (StringUtils.isBlank(order_no)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, "order_no 为空");
				return retMap;
			}

			BoxWorkOrder workOrder = generateWorkOrder(parameter);
			
			if(workOrder.getAreacode() != null && !"".equals(workOrder.getAreacode())){
    			Integer areaId = amsAreaService.findIdByCode(parameter.get("areacode").toString());
    			if(areaId == null){
    				//由于前端是不是由区域树选择，所以对这里的code进行验证，如果是错误的code，则返回错误信息
    				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_ERROR.getResultCode());
    				retMap.put(Constant.MESSAGE, "areacode不存在");
    				return retMap;
    			}
    		}
			
			
			boxWorkOrderMapper.updateBoxWorkOrder(workOrder);
			OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.WORKORDER_MANAGER_SERVICE, "编辑机顶盒工单", JSON.toJSONString(parameter));
		} catch (Exception e) {
			logger.error(e.toString());
			retMap.put(Constant.CODE, ErrorCodeEnum.DELETE_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, "删除错误：" + e.toString());
			return retMap;
		}

		retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
		retMap.put(Constant.MESSAGE, "成功");
		return retMap;
	}

	@Override
	public Map<String, Object> queryBoxWorkOrder(Map<String, Object> parameter) {
		logger.info("start invoke queryBoxWorkOrder:{}", parameter);

		Map<String, Object> retMap = new HashMap<>();
		try {
			int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
			int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString())
					: 10;
			PageHelper.startPage(page, pageSize);

			BoxWorkOrder boxWorkOrder = generateWorkOrder(parameter);
			List<BoxWorkOrder> boxWorkOrderlist = boxWorkOrderMapper.queryList4Page(boxWorkOrder);

			for (BoxWorkOrder workOrder : boxWorkOrderlist) {
				String boxAreacode = workOrder.getAreacode();
				if (StringUtils.isNotBlank(boxAreacode)) {
					Area area = amsAreaService.findAreaById(Integer.valueOf(boxAreacode));
					if (null != area) {
						workOrder.setAreaName(area.getName());
					}
				}
			}

			retMap.put("page", page);
			retMap.put("pageSize", pageSize);
			retMap.put("total", ((Page) boxWorkOrderlist).getTotal());
			retMap.put(Constant.DATA, JSON.toJSON(boxWorkOrderlist));
			retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			retMap.put(Constant.MESSAGE, "查询列表成功");
		} catch (Exception e) {
			logger.error(e.toString());
			retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, "查询列表错误");
			return retMap;
		}

		return retMap;
	}

	@Override
	public Map<String, Object> queryBoxWorkOrderDetail(Map<String, Object> parameter) {
		logger.info("start invoke queryBoxWorkOrderDetail:{}", parameter);

		Map<String, Object> retMap = new HashMap<>();
		try {
			String order_no = (String) parameter.get("order_no");
			if (StringUtils.isBlank(order_no)) {
				retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
				retMap.put(Constant.MESSAGE, "order_no 为空");
				return retMap;
			}

			BoxWorkOrder boxWorkOrder = new BoxWorkOrder();
			boxWorkOrder.setOrderNo(order_no);
			List<BoxWorkOrder> boxWorkOrderList = boxWorkOrderMapper.queryList4Page(boxWorkOrder);
			if (boxWorkOrderList == null || boxWorkOrderList.size() != 1) {
				retMap.put(Constant.DATA, JSON.toJSON(boxWorkOrder));
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "不存在该工单");
			}
			
			
			//查询工单执行历史记录
			List<BoxBusinessExecuteHistory> historyList = boxBusinessExecuteHistoryMapper.queryExecuteHisotry(order_no);
			boxWorkOrderList.get(0).setHistoryList(historyList);
			
			//查询归属地区
			String boxAreacode = boxWorkOrderList.get(0).getAreacode();
			if (StringUtils.isNotBlank(boxAreacode)) {
				Area area = amsAreaService.findAreaById(Integer.valueOf(boxAreacode));
				if (null != area) {
					boxWorkOrderList.get(0).setAreaName(area.getName());
				}
			}

			retMap.put(Constant.DATA, JSON.toJSON(boxWorkOrderList.get(0)));
			retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			retMap.put(Constant.MESSAGE, "查询成功");
		} catch (Exception e) {
			logger.error(e.toString());
			retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
			retMap.put(Constant.MESSAGE, "查询列表错误");
			return retMap;
		}

		return retMap;
	}

	private BoxWorkOrder generateWorkOrder(Map<String, Object> parameter) {

		BoxWorkOrder workorder = new BoxWorkOrder();
		String order_no = (String) parameter.get("order_no");
		if (StringUtils.isNotBlank(order_no)) {
			workorder.setOrderNo(order_no);
		}

		String business_statu = (String) parameter.get("business_statu");
		if (StringUtils.isNotBlank(business_statu)) {
			workorder.setBusinessStatu(business_statu);
		}

		String box_macaddress = (String) parameter.get("box_macaddress");
		if (StringUtils.isNotBlank(box_macaddress)) {
			workorder.setBoxMacaddress(box_macaddress);
		}

		String box_serialnumber = (String) parameter.get("box_serialnumber");
		if (StringUtils.isNotBlank(box_serialnumber)) {
			workorder.setBoxSerialnumber(box_serialnumber);
		}
		
		Integer createTime = (Integer) parameter.get("createTime");
		if (null != createTime) {
			workorder.setCreateTime(createTime);
		}
		
		String businessCodeBoss = (String) parameter.get("businessCodeBoss");
		if (StringUtils.isNotBlank(businessCodeBoss)) {
			workorder.setBusinessCode(businessCodeBoss);
			workorder.setBusinessCodeBoss(businessCodeBoss);
		}
		
		String areacode = (String) parameter.get("areacode");
		if (StringUtils.isNotBlank(areacode)) {
			workorder.setAreacode(areacode);
		}
		return workorder;
	}

}
