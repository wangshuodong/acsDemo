package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BoxBusinessCategoryMapper;
import com.cmiot.rms.dao.mapper.BoxBusinessTemplateRelationMapper;
import com.cmiot.rms.dao.model.BoxBusinessCategory;
import com.cmiot.rms.dao.model.BoxBusinessTemplateRelation;
import com.cmiot.rms.services.BoxBusinessCategoryService;
import com.cmiot.rms.services.util.OperationLogUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

public class BoxBusinessCategoryServiceImpl implements BoxBusinessCategoryService {
	private Logger logger = LoggerFactory.getLogger(BoxBusinessCategoryServiceImpl.class);
	
	@Autowired
	private BoxBusinessCategoryMapper boxBusinessCategoryMapper;
	@Autowired
	private BoxBusinessTemplateRelationMapper boxBusinessTemplateRelationMapper;
	
	@Override
	public Map<String, Object> addBoxBusinessCategory(Map<String, Object> params) {
		logger.info("start invoke addBoxBusinessCategory：{}", params);

		Map<String, Object> retMap = new HashMap<String, Object>();
		String businessName = (String) params.get("businessName");
		String businessCode = (String) params.get("businessCode");
		if(StringUtils.isEmpty(businessName)){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务类名称为空");
			return retMap;
		}
		if(StringUtils.isEmpty(businessCode)){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务编码空");
			return retMap;
		}
		//编码为中文的验证
		if(validateChString(businessCode)){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务编码不能为中文");
			return retMap;
		}
		//解析relationList
		List<Map<String,Object>> relationList = (List<Map<String, Object>>) params.get("relationList");
		List<BoxBusinessTemplateRelation> addList = new ArrayList<>();
		if(null != relationList && relationList.size()>0) {
			for (Map<String, Object> map : relationList) {
				BoxBusinessTemplateRelation businessTemplateRelation = new BoxBusinessTemplateRelation();
				businessTemplateRelation.setId(UniqueUtil.uuid());
				businessTemplateRelation.setBussinessCode(businessCode);
				String businessTemplate = (String) map.get("businessTemplate");
				if (StringUtils.isEmpty(businessTemplate)) {
					retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
					retMap.put(Constant.MESSAGE, "businessTemplate为空");
					return retMap;
				}
				businessTemplateRelation.setBusinessTemplate(businessTemplate);

				addList.add(businessTemplateRelation);
			}
		}

		try {
			BoxBusinessCategory category = new BoxBusinessCategory();
			category.setBusinessCode(businessCode);//进行业务编码的唯一验证

			List<Map<String,Object>> list = boxBusinessCategoryMapper.queryListAll(category);
			if(list.size() > 0){
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "该业务编码已存在");
				return retMap;
			}
			BoxBusinessCategory _category = new BoxBusinessCategory();//业务名称唯一验证
			_category.setBusinessName(businessName);
			List<Map<String,Object>> _list = boxBusinessCategoryMapper.queryListAll(_category);
			if(_list.size() > 0){
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "该业务名称已存在");
				return retMap;
			}
			category.setBusinessName(params.get("businessName").toString());
			category.setId(UniqueUtil.uuid());
			category.setCreateDate((int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			int add = boxBusinessCategoryMapper.insert(category);
			logger.info("invoke addBusinessCategoryNew 插入业务编码结果，add:{}",add);
			if(add >0){
				//添加关联信息
				if(addList.size()>0) {
					int addRelation = boxBusinessTemplateRelationMapper.batchInsert(addList);
					logger.info("invoke addBusinessCategoryNew 批量插入业务编码和业务模板关系结果，add:{}",addRelation);
				}
				retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
				retMap.put(Constant.MESSAGE, "新增成功");
				OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "新增机顶盒业务代码", JSON.toJSONString(params));
			}else{
				retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
				retMap.put(Constant.MESSAGE, "新增失败");
			}
			logger.info("end invoke addBusinessCategoryNew，retMap:{}",retMap);
			return retMap;
		} catch (Exception e) {
			logger.error("新增业务类异常：", e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}

	@Override
	public Map<String, Object> updateBoxBusinessCategory(Map<String, Object> params) {
		logger.info("start invoke updateBoxBusinessCategory:{}", params);

		Map<String, Object> retMap = new HashMap<String, Object>();
		String businessName = (String) params.get("businessName");
		String businessCode = (String) params.get("businessCode");
		String businessId = (String) params.get("businessId");
		BoxBusinessCategory updateBusinessCategory = new BoxBusinessCategory();
		boolean isNeedUpdate = false;
		if(StringUtils.isEmpty(businessId)){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "businessId为空");
			return retMap;
		}
		updateBusinessCategory.setId(businessId);
		if(!StringUtils.isEmpty(businessName)){
			isNeedUpdate = true;
			updateBusinessCategory.setBusinessName(businessName);
		}
		if(!StringUtils.isEmpty(businessCode)){
			isNeedUpdate = true;
			updateBusinessCategory.setBusinessCode(businessCode);
		}
		//解析relationList
		List<Map<String,Object>> relationList = (List<Map<String, Object>>) params.get("relationList");
		List<BoxBusinessTemplateRelation> addList = new ArrayList<>();
		if(null != relationList && relationList.size()>0) {
				for (Map<String, Object> map : relationList) {
					BoxBusinessTemplateRelation businessTemplateRelation = new BoxBusinessTemplateRelation();
					businessTemplateRelation.setId(UniqueUtil.uuid());
					businessTemplateRelation.setBussinessCode(businessCode);
					//businessTemplate是必须的
					String businessTemplate = (String) map.get("businessTemplate");
					if (StringUtils.isEmpty(businessTemplate)) {
						retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
						retMap.put(Constant.MESSAGE, "businessTemplate为空");
						return retMap;
					}
					businessTemplateRelation.setBusinessTemplate(businessTemplate);

					addList.add(businessTemplateRelation);
				}
		}

		try {
			BoxBusinessCategory category = new BoxBusinessCategory();
			category.setId(businessId);//进行业务编码的唯一验证

			List<Map<String,Object>> list = boxBusinessCategoryMapper.queryListAll(category);
			if(null == list || list.size() < 1){
				logger.info("更新机顶盒业务编码信息，业务代码信息不存在，businessId:{}",businessId);
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "业务信息不存在");
				return retMap;
			}
			//首先更新业务编码信息
			int update = 0;
			if(isNeedUpdate){
				update = boxBusinessCategoryMapper.updateByPrimaryKey(updateBusinessCategory);
				logger.info("更新业务编码信息，更新结果，update:{}",update);
				if(update < 1){
					retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
					retMap.put(Constant.MESSAGE, "更新业务编码信息失败");
					return retMap;
				}
			}
			//更新关联信息
			if(addList.size()>0) {
				//先删除，后插入
				String oldBusinessCode = (String)list.get(0).get("businessCode");
				boxBusinessTemplateRelationMapper.deleteByBusinessCode(oldBusinessCode);
				if(StringUtils.isEmpty(businessCode)){
					for(BoxBusinessTemplateRelation btr:addList){
						btr.setBussinessCode(oldBusinessCode);
					}
				}

				int addRelation = boxBusinessTemplateRelationMapper.batchInsert(addList);
				logger.info("invoke updateBoxBusinessCategoryNew 批量插入业务编码和业务模板关系结果，add:{}", addRelation);
			}
			retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			retMap.put(Constant.MESSAGE, "新增成功");
			OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "修改机顶盒业务代码", JSON.toJSONString(params));
			logger.info("end invoke updateBoxBusinessCategoryNew，retMap:{}",retMap);
			return retMap;
		} catch (Exception e) {
			logger.error("更新机顶盒业务类异常：", e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}

	@Override
	public Map<String, Object> queryBoxBusinessCategory(Map<String, Object> params) {
		logger.info("start invoke queryBoxBusinessCategory分页查询机顶盒业务类请求参数："+JSON.toJSONString(params));

		Map<String, Object> retMap = new HashMap<String, Object>();

		try {

			int page = (null != params.get("page")) ? Integer.valueOf(params.get("page").toString()) : 1;
			int pageSize = (null != params.get("pageSize")) ? Integer.valueOf(params.get("pageSize").toString()) : 10;


			PageHelper.startPage(page, pageSize);

			List<Map<String, Object>> categoryList =  boxBusinessCategoryMapper.queryBusinessForList(params);

			List<Map<String,Object>> dataList = new ArrayList<>();
			Map<String,Object> parseMap = new HashMap<>();
			for(Map<String,Object> map : categoryList){
				Map<String,Object> dataMap = new HashMap<>();
				List<Map<String,Object>> relationList = new ArrayList<>();
				String businessId = (String) map.get("businessId");
				if(StringUtils.isEmpty((String) parseMap.get(businessId))){
					parseMap.put(businessId,"true");
					dataMap.put("businessId",map.get("businessId"));
					dataMap.put("businessName",map.get("businessName"));
					dataMap.put("businessCode",map.get("businessCode"));
					//再次遍历查询结果，生成relationList
					for(Map<String,Object> relationMap : categoryList){
						if(businessId.equals((String)relationMap.get("businessId"))){
							Map<String,Object> rmap = new HashMap<>();
//							rmap.put("manufacturerID",relationMap.get("manufacturerID"));
//							rmap.put("manufacturerName",relationMap.get("manufacturerName"));
//							rmap.put("factoryID",relationMap.get("factoryID"));
//							rmap.put("factoryName",relationMap.get("factoryName"));
//							rmap.put("boxModel",relationMap.get("boxModel"));
//							rmap.put("hdVersion",relationMap.get("hdVersion"));
//							rmap.put("firmwareVersion",relationMap.get("firmwareVersion"));
							rmap.put("businessTemplate",relationMap.get("businessTemplate"));
							rmap.put("businessTemplateName",relationMap.get("businessTemplateName"));
//							rmap.put("IsIG",relationMap.get("isIG"));
							relationList.add(rmap);
						}
					}
					dataMap.put("relationList",relationList);
					dataList.add(dataMap);
				}
			}

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			resultMap.put(Constant.MESSAGE, "查询成功");
			resultMap.put("total", ((Page)categoryList).getTotal());
			resultMap.put("page", page);
			resultMap.put("pageSize", pageSize);
			resultMap.put(Constant.DATA, dataList);
			return resultMap;

		} catch (Exception e) {
			logger.error("分页查询机顶盒业务类异常：", e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}

	@Override
	public Map<String, Object> deleteBoxBusinessCategory(Map<String, Object> params) {
		logger.info("start invoke deleteBoxBusinessCategory删除机顶盒业务类请求参数："+JSON.toJSONString(params));

		Map<String, Object> retMap = new HashMap<String, Object>();
		if(params == null || params.get("businessId") == null || "".equals(params.get("businessId"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务类ID为空");
			return retMap;
		}

		try {
			String id = (String) params.get("businessId");

			//先查询业务编码是否存在
			BoxBusinessCategory category = new BoxBusinessCategory();
			category.setId(id);//进行业务编码的唯一验证

			List<Map<String,Object>> list = boxBusinessCategoryMapper.queryListAll(category);
			if(null == list || list.size() < 1){
				logger.info("删除机顶盒业务编码信息，业务代码信息不存在，businessId:{}",id);
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "业务信息不存在");
				return retMap;
			}
			//先判断是否关联有工单，有就不能删除
			List<Map<String,Object>> listR = boxBusinessCategoryMapper.findBoxBusinessById(id);
			//
			if(listR == null || listR.size() == 0){
				boxBusinessCategoryMapper.deleteByPrimaryKey(id);
				//同时删除关联信息
				boxBusinessTemplateRelationMapper.deleteByBusinessCode((String)list.get(0).get("businessCode"));
				retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
				retMap.put(Constant.MESSAGE, "删除成功");
				OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "删除机顶盒业务代码", JSON.toJSONString(params));
			}else{
				retMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
				retMap.put(Constant.MESSAGE, "该业务类下有关联的工单,不能删除");
			}
			return retMap;
		} catch (Exception e) {
			logger.error("删除机顶盒业务类异常：",e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}

    /**
     * 判断字符串是否为中文
     * @param arg
     * @return
     */
	private Boolean validateChString(String arg){
		Boolean isCh = false;
		String regEx = "[\u4e00-\u9fa5]+";
		Pattern ptn = Pattern.compile(regEx);
		Matcher mcr = ptn.matcher(arg);
		if(mcr.find()){
			isCh = true;
		}
		return isCh;
	}

	@Override
	public Map<String, Object> findBoxBusinessCategory(Map<String, Object> params) {
		logger.info("start invoke findBoxBusinessCategory：{}", params);
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(params == null || params.get("businessId") == null || "".equals(params.get("businessId"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "businessId为空");
			return retMap;
		}
		
		try {
			Map<String, Object> data =	boxBusinessCategoryMapper.selectByPrimaryKey(params.get("businessId").toString());
			retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			retMap.put(Constant.DATA, data);
			retMap.put(Constant.MESSAGE, "查询成功");
			logger.info("end invoke findBoxBusinessCategory:{}", retMap);
			return retMap;
		} catch (Exception e) {
			logger.error("查询机顶盒业务类异常：", e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}

}
