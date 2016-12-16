package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cmiot.rms.dao.mapper.BusinessTemplateRelationMapper;
import com.cmiot.rms.dao.model.BusinessTemplateRelation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BusinessCategoryMapper;
import com.cmiot.rms.dao.model.BusinessCategory;

import com.cmiot.rms.services.BusinessCategoryService;
import com.cmiot.rms.services.util.OperationLogUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

/**
 * zoujiang 业务管理类接口
 */
@Service("businessCategoryService")
public class BusinessCategoryServiceImpl implements BusinessCategoryService {

	private Logger logger = LoggerFactory.getLogger(BusinessCategoryServiceImpl.class);
	
    @Autowired
    private BusinessCategoryMapper businessCategoryMapper;

	@Autowired
	private BusinessTemplateRelationMapper businessTemplateRelationMapper;
    


	@Override
	public Map<String, Object> addBusinessCategory(Map<String, Object> params) {
		
		logger.info("新增业务类请求参数："+JSON.toJSONString(params));
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(params == null || params.get("businessName") == null || "".equals(params.get("businessName"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务类名称为空");
			return retMap;
		}
		if(params == null || params.get("businessCode") == null || "".equals(params.get("businessCode"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务代码空");
			return retMap;
		}
		if(params == null || params.get("deviceModel") == null || "".equals(params.get("deviceModel"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "设备类型为空");
			return retMap;
		}
		//编码为中文的验证
		if(validateChString(params.get("businessCode").toString())){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务代码不能为中文");
			return retMap;
		}
		/*if(params == null || params.get("businessTemplate") == null || "".equals(params.get("businessTemplate"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "适用业务模版为空");
			return retMap;
		}
		if(params.get("factoryCode") == null || "".equals(params.get("factoryCode"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "生成商编码为空");
			return retMap;
		}*/
		try {
			BusinessCategory category = new BusinessCategory();
			category.setBusinessCode(params.get("businessCode").toString());//进行业务代码的唯一验证

			List<Map<String,Object>> list = businessCategoryMapper.queryListAll(category);
			if(list.size() > 0){
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "该业务代码已存在");
				return retMap;
			}
			BusinessCategory _category = new BusinessCategory();//业务名称唯一验证
			_category.setBusinessName(params.get("businessName").toString());
			List<Map<String,Object>> _list = businessCategoryMapper.queryListAll(_category);
			if(_list.size() > 0){
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "该业务名称已存在");
				return retMap;
			}
			category.setBusinessName(params.get("businessName").toString());
			category.setDeviceModel(params.get("deviceModel").toString());
			String businessTemplate = (String)params.get("businessTemplate");
			if(!StringUtils.isEmpty(businessTemplate)) {
				category.setBusinessTemplate(businessTemplate);
			}
			category.setId(UniqueUtil.uuid());
			category.setCreateDate((int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			String factoryCode = (String) params.get("factoryCode");
			if(!StringUtils.isEmpty(factoryCode)) {
				category.setFactoryCode(factoryCode);
			}
			businessCategoryMapper.insert(category);
			retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			retMap.put(Constant.MESSAGE, "新增成功");
			return retMap;
		} catch (Exception e) {
			logger.error("新增业务类异常：", e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
		
	}

	@Override
	public Map<String, Object> updateBusinessCategory(Map<String, Object> params) {
		
		logger.info("更新业务类请求参数："+JSON.toJSONString(params));
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(params == null || params.get("businessId") == null || "".equals(params.get("businessId"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务类ID为空");
			return retMap;
		}
		try {
			String businessId = (String) params.get("businessId");
			BusinessCategory category = new BusinessCategory();
			category.setId(businessId);
			Map<String, Object> bcmap = businessCategoryMapper.selectByPrimaryKey(businessId);
			if(bcmap == null || bcmap.isEmpty()){
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "不存在该业务类");
				return retMap;
			}
			String businessCode = String.valueOf(bcmap.get("businessCode"));
			String businessName = String.valueOf(bcmap.get("businessName"));
			if(params.get("businessName") != null && !"".equals(params.get("businessName"))){
				category.setBusinessName(params.get("businessName").toString());
				if(!params.get("businessName").toString().equals(businessName)){
					BusinessCategory _category = new BusinessCategory();
					_category.setBusinessName(params.get("businessName").toString());//进行业务名称的唯一验证,存在提示不能修改

					List<Map<String,Object>> _list = businessCategoryMapper.queryListAll(_category);
					if(_list.size() > 0){
						retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
						retMap.put(Constant.MESSAGE, "不能修改为已存在的业务名称");
						return retMap;
					}
				}
			}
			if(params.get("businessCode") != null && !"".equals(params.get("businessCode"))){
				//业务代码为中文的验证
				if(validateChString(params.get("businessCode").toString())){
					retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
					retMap.put(Constant.MESSAGE, "业务代码不能为中文");
					return retMap;
				}
				category.setBusinessCode(params.get("businessCode").toString());
				if(!params.get("businessCode").toString().equals(businessCode)){
					BusinessCategory _category = new BusinessCategory();
					_category.setBusinessCode(params.get("businessCode").toString());//进行业务代码的唯一验证,存在提示不能修改

					List<Map<String,Object>> _list = businessCategoryMapper.queryListAll(_category);
					if(_list.size() > 0){
						retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
						retMap.put(Constant.MESSAGE, "不能修改为已存在的业务代码");
						return retMap;
					}
				}
				
			}
			if(params.get("deviceModel") != null && !"".equals(params.get("deviceModel"))){
				category.setDeviceModel(params.get("deviceModel").toString());
			}
			if(params.get("businessTemplate") != null && !"".equals(params.get("businessTemplate"))){
				category.setBusinessTemplate(params.get("businessTemplate").toString());
			}
			
			businessCategoryMapper.updateByPrimaryKey(category);
			retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			retMap.put(Constant.MESSAGE, "更新成功");
			return retMap;
		} catch (Exception e) {
			logger.error("更新业务类异常：", e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}

	@Override
	public Map<String, Object> deleteBusinessCategory(Map<String, Object> params) {

		logger.info("删除业务类请求参数："+JSON.toJSONString(params));
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(params == null || params.get("businessId") == null || "".equals(params.get("businessId"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务类ID为空");
			return retMap;
		}
		
		try {
			String id = (String) params.get("businessId");
			
			//先判断是否关联有工单，有就不能删除
			List<Map<String,Object>> list = businessCategoryMapper.findGatewayBusinessById(id);
			//
			if(list == null || list.size() == 0){
				businessCategoryMapper.deleteByPrimaryKey(id);
				retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
				retMap.put(Constant.MESSAGE, "删除成功");
			}else{
				retMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
				retMap.put(Constant.MESSAGE, "该业务类下有关联的工单,不能删除");
			}
			return retMap;
		} catch (Exception e) {
			logger.error("删除业务类异常：",e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}

	@Override
	public Map<String, Object> queryBusinessCategory(Map<String, Object> params) {
		
		logger.info("分页查询业务类请求参数："+JSON.toJSONString(params));
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		
		try {
			
			int page = (null != params.get("page")) ? Integer.valueOf(params.get("page").toString()) : 1;
		    int pageSize = (null != params.get("pageSize")) ? Integer.valueOf(params.get("pageSize").toString()) : 10;

		    PageHelper.startPage(page, pageSize);
			
			BusinessCategory category = new BusinessCategory();
			if(params != null && params.get("businessId") != null && !"".equals(params.get("businessId"))){
				
				category.setId(params.get("businessId").toString());
			}
			if(params != null && params.get("businessName") != null && !"".equals(params.get("businessName"))){
				
				category.setBusinessName(params.get("businessName").toString());
			}
			if(params != null && params.get("businessCode") != null && !"".equals(params.get("businessCode"))){
				
				category.setBusinessCode(params.get("businessCode").toString());
			}
			if(params != null && params.get("deviceModel") != null && !"".equals(params.get("deviceModel"))){
				
				category.setDeviceModel(params.get("deviceModel").toString());
			}
			
			List<Map<String, Object>> categoryList =  businessCategoryMapper.queryListLike(category);
			
			Map<String, Object> resultMap = new HashMap<>();
	        resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
	        resultMap.put(Constant.MESSAGE, "查询成功");
	        resultMap.put("total", ((Page)categoryList).getTotal());
	        resultMap.put("page", page);
	        resultMap.put("pageSize", pageSize);
	        resultMap.put(Constant.DATA, JSON.toJSON(categoryList));
	        return resultMap;
			
		} catch (Exception e) {
			logger.error("分页查询业务类异常：", e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}

	@Override
	public Map<String, Object> findBusinessCategory(Map<String, Object> params) {
		
		logger.info("查询业务类请求参数："+JSON.toJSONString(params));
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(params == null || params.get("businessId") == null || "".equals(params.get("businessId"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "businessId为空");
			return retMap;
		}
		
		try {
			Map<String, Object> data =	businessCategoryMapper.selectByPrimaryKey(params.get("businessId").toString());
			retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			retMap.put(Constant.DATA, data);
			retMap.put(Constant.MESSAGE, "查询成功");
			return retMap;
		} catch (Exception e) {
			logger.error("查询业务类异常：", e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}

	@Override
	public Map<String, Object> addAHBusinessCategory(Map<String, Object> params) {
		logger.info("新增业务类请求参数："+JSON.toJSONString(params));
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		if(params == null || params.get("businessName") == null || "".equals(params.get("businessName"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务类名称为空");
			return retMap;
		}
		if(params == null || params.get("businessCode") == null || "".equals(params.get("businessCode"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务代码空");
			return retMap;
		}
		if(params == null || params.get("deviceModel") == null || "".equals(params.get("deviceModel"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "设备类型为空");
			return retMap;
		}
		if(params == null || params.get("businessTemplate") == null || "".equals(params.get("businessTemplate"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "适用业务模版为空");
			return retMap;
		}
		if(params.get("factoryCode") == null || "".equals(params.get("factoryCode"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "生产商编码为空");
			return retMap;
		}
		try {
			BusinessCategory category = new BusinessCategory();
			category.setBusinessCode(params.get("businessCode").toString());//进行业务代码的唯一验证

			List<Map<String,Object>> list = businessCategoryMapper.queryList(category);
			if(list.size() > 0){
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "该业务代码已存在");
				return retMap;
			}
			BusinessCategory _category = new BusinessCategory();//业务名称唯一验证
			_category.setBusinessName(params.get("businessName").toString());
			List<Map<String,Object>> _list = businessCategoryMapper.queryListAll(_category);
			if(_list.size() > 0){
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "该业务名称已存在");
				return retMap;
			}
			category.setBusinessName(params.get("businessName").toString());
			category.setDeviceModel(params.get("deviceModel").toString());
			String businessTemplate = (String)params.get("businessTemplate");
			if(!StringUtils.isEmpty(businessTemplate)) {
				category.setBusinessTemplate(businessTemplate);
			}
			category.setId(UniqueUtil.uuid());
			category.setCreateDate((int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			String factoryCode = (String) params.get("factoryCode");
			if(!StringUtils.isEmpty(factoryCode)) {
				category.setFactoryCode(factoryCode);
			}
			businessCategoryMapper.insert(category);
			OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "新增网关业务代码", JSON.toJSONString(params));
			retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			retMap.put(Constant.MESSAGE, "新增成功");
			return retMap;
		} catch (Exception e) {
			logger.error("新增业务类异常：", e);
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
	public Map<String, Object> queryAHBusinessCategory(Map<String, Object> params) {
		logger.info("分页查询业务类请求参数："+JSON.toJSONString(params));
		
		Map<String, Object> retMap = new HashMap<String, Object>();
		
		try {
			
			int page = (null != params.get("page")) ? Integer.valueOf(params.get("page").toString()) : 1;
		    int pageSize = (null != params.get("pageSize")) ? Integer.valueOf(params.get("pageSize").toString()) : 10;

		    PageHelper.startPage(page, pageSize);
			
			BusinessCategory category = new BusinessCategory();
			if(params != null && params.get("businessId") != null && !"".equals(params.get("businessId"))){
				
				category.setId(params.get("businessId").toString());
			}
			if(params != null && params.get("businessName") != null && !"".equals(params.get("businessName"))){
				
				category.setBusinessName(params.get("businessName").toString());
			}
			if(params != null && params.get("businessCode") != null && !"".equals(params.get("businessCode"))){
				
				category.setBusinessCode(params.get("businessCode").toString());
			}
			if(params != null && params.get("deviceModel") != null && !"".equals(params.get("deviceModel"))){
				
				category.setDeviceModel(params.get("deviceModel").toString());
			}
			
			List<Map<String, Object>> categoryList =  businessCategoryMapper.queryBusinessForList(category);
			
			Map<String, Object> resultMap = new HashMap<>();
	        resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
	        resultMap.put(Constant.MESSAGE, "查询成功");
	        resultMap.put("total", ((Page)categoryList).getTotal());
	        resultMap.put("page", page);
	        resultMap.put("pageSize", pageSize);
	        resultMap.put(Constant.DATA, JSON.toJSON(categoryList));
	        return resultMap;
			
		} catch (Exception e) {
			logger.error("分页查询业务类异常：", e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}


	@Override
	public Map<String, Object> addBusinessCategoryNew(Map<String, Object> params) {
		logger.info("新增业务代码请求参数,addBusinessCategoryNew："+JSON.toJSONString(params));

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
			retMap.put(Constant.MESSAGE, "业务代码空");
			return retMap;
		}
		//编码为中文的验证
		if(validateChString(businessCode)){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务代码不能为中文");
			return retMap;
		}
		//解析relationList
		List<Map<String,Object>> relationList = (List<Map<String, Object>>) params.get("relationList");
		List<BusinessTemplateRelation> addList = new ArrayList<>();
		if(null != relationList && relationList.size()>0) {
			for (Map<String, Object> map : relationList) {
				BusinessTemplateRelation businessTemplateRelation = new BusinessTemplateRelation();
				businessTemplateRelation.setId(UniqueUtil.uuid());
				businessTemplateRelation.setBussinessCode(businessCode);
				String isIG = (String) map.get("IsIG");//是否智能网关，0表示是，1表示不是,为空默认1
				if(StringUtils.isEmpty(isIG)){
					isIG = "1";
				}
				boolean isIGboolean = false;
				if("0".equals(isIG)){
					isIGboolean = true;
				}
				businessTemplateRelation.setIsIG(isIG);
				String manufacturerID = (String) map.get("manufacturerID");
				if (StringUtils.isEmpty(manufacturerID)) {
					if(!isIGboolean){
						retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
						retMap.put(Constant.MESSAGE, "manufacturerID为空");
						return retMap;
					}
				}else {
					businessTemplateRelation.setManufacturerid(manufacturerID);
				}

				String manufacturerName = (String) map.get("manufacturerName");
				if (StringUtils.isEmpty(manufacturerName)) {
					if(!isIGboolean) {
						retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
						retMap.put(Constant.MESSAGE, "manufacturerName为空");
						return retMap;
					}
				}else {
					businessTemplateRelation.setManufacturerName(manufacturerName);
				}

				String factoryID = (String) map.get("factoryID");
				if (StringUtils.isEmpty(factoryID)) {
					if(!isIGboolean) {
						retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
						retMap.put(Constant.MESSAGE, "factoryID为空");
						return retMap;
					}
				}else {
					businessTemplateRelation.setFactoryid(factoryID);
				}


				String factoryName = (String) map.get("factoryName");
				if (StringUtils.isEmpty(factoryName)) {
					if(!isIGboolean) {
						retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
						retMap.put(Constant.MESSAGE, "factoryName为空");
						return retMap;
					}
				}else {

					businessTemplateRelation.setFactoryName(factoryName);
				}


				String gatewayModel = (String) map.get("gatewayModel");
				if (StringUtils.isEmpty(gatewayModel)) {
					if(!isIGboolean) {
						retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
						retMap.put(Constant.MESSAGE, "gatewayModel为空");
						return retMap;
					}
				}else {
					businessTemplateRelation.setGatewayModel(gatewayModel);
				}
				//由版本号改为存储ID
				String hdVersion = (String) map.get("hdVersion");
				if (StringUtils.isEmpty(hdVersion)) {
					if(!isIGboolean) {
						retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
						retMap.put(Constant.MESSAGE, "hdVersion为空");
						return retMap;
					}
				}else {
					businessTemplateRelation.setHdVersion(hdVersion);
				}
				//由版本号改为存储ID
				String firmwareVersion = (String) map.get("firmwareVersion");
				if (StringUtils.isEmpty(firmwareVersion)) {
					if(!isIGboolean) {
						retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
						retMap.put(Constant.MESSAGE, "firmwareVersion为空");
						return retMap;
					}
				}else {
					businessTemplateRelation.setFirmwareVersion(firmwareVersion);
				}
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
			BusinessCategory category = new BusinessCategory();
			category.setBusinessCode(businessCode);//进行业务代码的唯一验证

			List<Map<String,Object>> list = businessCategoryMapper.queryListAll(category);
			if(list.size() > 0){
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "该业务代码已存在");
				return retMap;
			}
			BusinessCategory _category = new BusinessCategory();//业务名称唯一验证
			_category.setBusinessName(businessName);
			List<Map<String,Object>> _list = businessCategoryMapper.queryListAll(_category);
			if(_list.size() > 0){
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "该业务名称已存在");
				return retMap;
			}
			category.setBusinessName(params.get("businessName").toString());
			category.setId(UniqueUtil.uuid());
			category.setCreateDate((int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			int add = businessCategoryMapper.insert(category);
			logger.info("invoke addBusinessCategoryNew 插入业务代码结果，add:{}",add);
			if(add >0){
				//添加关联信息
				if(addList.size()>0) {
					int addRelation = businessTemplateRelationMapper.batchInsert(addList);
					logger.info("invoke addBusinessCategoryNew 批量插入业务代码和业务模板关系结果，add:{}",addRelation);
				}
				OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "新增网关业务代码", JSON.toJSONString(params));
				retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
				retMap.put(Constant.MESSAGE, "新增成功");
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
	public Map<String, Object> updateBusinessCategoryNew(Map<String, Object> params) {
		logger.info("更新业务代码请求参数,updateBusinessCategoryNew："+JSON.toJSONString(params));

		Map<String, Object> retMap = new HashMap<String, Object>();
		String businessName = (String) params.get("businessName");
		String businessCode = (String) params.get("businessCode");
		String businessId = (String) params.get("businessId");
		BusinessCategory updateBusinessCategory = new BusinessCategory();
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
		List<BusinessTemplateRelation> addList = new ArrayList<>();
		if(null != relationList && relationList.size()>0) {
				for (Map<String, Object> map : relationList) {
					BusinessTemplateRelation businessTemplateRelation = new BusinessTemplateRelation();
					businessTemplateRelation.setId(UniqueUtil.uuid());
					businessTemplateRelation.setBussinessCode(businessCode);
					String isIG = (String) map.get("IsIG");//是否智能网关，0表示是，1表示不是,为空默认1
					if(StringUtils.isEmpty(isIG)){
						isIG = "1";
					}
					boolean isIGboolean = false;
					if("0".equals(isIG)){
						isIGboolean = true;
					}
					businessTemplateRelation.setIsIG(isIG);
					String manufacturerID = (String) map.get("manufacturerID");
					if (StringUtils.isEmpty(manufacturerID)) {
						if(!isIGboolean){
							retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
							retMap.put(Constant.MESSAGE, "manufacturerID为空");
							return retMap;
						}
					}else {
						businessTemplateRelation.setManufacturerid(manufacturerID);
					}

					String manufacturerName = (String) map.get("manufacturerName");
					if (StringUtils.isEmpty(manufacturerName)) {
						if(!isIGboolean) {
							retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
							retMap.put(Constant.MESSAGE, "manufacturerName为空");
							return retMap;
						}
					}else {
						businessTemplateRelation.setManufacturerName(manufacturerName);
					}

					String factoryID = (String) map.get("factoryID");
					if (StringUtils.isEmpty(factoryID)) {
						if(!isIGboolean) {
							retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
							retMap.put(Constant.MESSAGE, "factoryID为空");
							return retMap;
						}
					}else {
						businessTemplateRelation.setFactoryid(factoryID);
					}


					String factoryName = (String) map.get("factoryName");
					if (StringUtils.isEmpty(factoryName)) {
						if(!isIGboolean) {
							retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
							retMap.put(Constant.MESSAGE, "factoryName为空");
							return retMap;
						}
					}else {

						businessTemplateRelation.setFactoryName(factoryName);
					}


					String gatewayModel = (String) map.get("gatewayModel");
					if (StringUtils.isEmpty(gatewayModel)) {
						if(!isIGboolean) {
							retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
							retMap.put(Constant.MESSAGE, "gatewayModel为空");
							return retMap;
						}
					}else {
						businessTemplateRelation.setGatewayModel(gatewayModel);
					}

					String hdVersion = (String) map.get("hdVersion");
					if (StringUtils.isEmpty(hdVersion)) {
						if(!isIGboolean) {
							retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
							retMap.put(Constant.MESSAGE, "hdVersion为空");
							return retMap;
						}
					}else {
						businessTemplateRelation.setHdVersion(hdVersion);
					}

					String firmwareVersion = (String) map.get("firmwareVersion");
					if (StringUtils.isEmpty(firmwareVersion)) {
						if(!isIGboolean) {
							retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
							retMap.put(Constant.MESSAGE, "firmwareVersion为空");
							return retMap;
						}
					}else {
						businessTemplateRelation.setFirmwareVersion(firmwareVersion);
					}
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
			BusinessCategory category = new BusinessCategory();
			category.setId(businessId);//进行业务代码的唯一验证

			List<Map<String,Object>> list = businessCategoryMapper.queryListAll(category);
			if(null == list || list.size() < 1){
				logger.info("更新业务代码信息，业务代码信息不存在，businessId:{}",businessId);
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "业务信息不存在");
				return retMap;
			}
			//判断新业务代码的唯一性
			if(!StringUtils.isEmpty(businessCode)){
				if(!businessCode.equals((String)list.get(0).get("businessCode"))){
					BusinessCategory category_ = new BusinessCategory();
					category_.setBusinessCode(businessCode);//进行业务代码的唯一验证

					List<Map<String,Object>> list_ = businessCategoryMapper.queryListAll(category_);
					if(list_.size() > 0){
						retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
						retMap.put(Constant.MESSAGE, "该业务代码已存在");
						return retMap;
					}
				}
			}
			//业务名称唯一性判断
			if(!StringUtils.isEmpty(businessName)) {
				if(!businessName.equals(list.get(0).get("businessName"))) {
					BusinessCategory _category = new BusinessCategory();//业务名称唯一验证
					_category.setBusinessName(businessName);
					List<Map<String, Object>> _list = businessCategoryMapper.queryListAll(_category);
					if (_list.size() > 0) {
						retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
						retMap.put(Constant.MESSAGE, "该业务名称已存在");
						return retMap;
					}
				}
			}
			//首先更新业务代码信息
			int update = 0;
			if(isNeedUpdate){
				update = businessCategoryMapper.updateByPrimaryKey(updateBusinessCategory);
				logger.info("更新业务代码信息，更新结果，update:{}",update);
				if(update < 1){
					retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
					retMap.put(Constant.MESSAGE, "更新业务代码信息失败");
					return retMap;
				}
			}
			//更新关联信息
			if(addList.size()>0) {
				//先删除，后插入
				String oldBusinessCode = (String)list.get(0).get("businessCode");
				businessTemplateRelationMapper.deleteByBusinessCode(oldBusinessCode);
				if(StringUtils.isEmpty(businessCode)){
					for(BusinessTemplateRelation btr:addList){
						btr.setBussinessCode(oldBusinessCode);
					}
				}

				int addRelation = businessTemplateRelationMapper.batchInsert(addList);
				logger.info("invoke updateBusinessCategoryNew 批量插入业务代码和业务模板关系结果，add:{}", addRelation);
			}
			retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			retMap.put(Constant.MESSAGE, "新增成功");
			OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "修改网关业务代码", JSON.toJSONString(params));
			logger.info("end invoke updateBusinessCategoryNew，retMap:{}", retMap);
			return retMap;
		} catch (Exception e) {
			logger.error("更新业务类异常：", e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}

	@Override
	public Map<String, Object> queryBusinessCategoryNew(Map<String, Object> params) {
		logger.info("start invoke queryBusinessCategoryNew分页查询业务类请求参数："+JSON.toJSONString(params));

		Map<String, Object> retMap = new HashMap<String, Object>();

		try {

			int page = (null != params.get("page")) ? Integer.valueOf(params.get("page").toString()) : 1;
			int pageSize = (null != params.get("pageSize")) ? Integer.valueOf(params.get("pageSize").toString()) : 10;

			int total = businessCategoryMapper.queryCount(params);
			List<Map<String, Object>> categoryList = new ArrayList<>();
			if (total>0) {
				params.put("start",(page-1)*pageSize);
				params.put("end",pageSize);

				List<String> idList = businessCategoryMapper.queryBusinessIds(params);

				categoryList =  businessCategoryMapper.queryBusinessForListNew(idList);
			}


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
							rmap.put("manufacturerID",relationMap.get("manufacturerID"));
							rmap.put("manufacturerName",relationMap.get("manufacturerName"));
							rmap.put("factoryID",relationMap.get("factoryID"));
							rmap.put("factoryName",relationMap.get("factoryName"));
							rmap.put("gatewayModel",relationMap.get("gatewayModel"));
							rmap.put("hdVersion",relationMap.get("hdVersion"));
							rmap.put("firmwareVersion",relationMap.get("firmwareVersion"));
							rmap.put("businessTemplate",relationMap.get("businessTemplate"));
							rmap.put("businessTemplateName",relationMap.get("businessTemplateName"));
							rmap.put("IsIG",relationMap.get("isIG"));
							rmap.put("hdVersionName",relationMap.get("hdVersionName"));
							rmap.put("firmwareVersionName",relationMap.get("firmwareVersionName"));
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
			resultMap.put("total", total);
			resultMap.put("page", page);
			resultMap.put("pageSize", pageSize);
			resultMap.put(Constant.DATA, dataList);
			return resultMap;

		} catch (Exception e) {
			logger.error("分页查询业务类异常：", e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}

	@Override
	public Map<String, Object> deleteBusinessCategoryNew(Map<String, Object> params) {
		logger.info("删除业务类请求参数："+JSON.toJSONString(params));

		Map<String, Object> retMap = new HashMap<String, Object>();
		if(params == null || params.get("businessId") == null || "".equals(params.get("businessId"))){
			retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			retMap.put(Constant.MESSAGE, "业务类ID为空");
			return retMap;
		}

		try {
			String id = (String) params.get("businessId");

			//先查询业务代码是否存在
			BusinessCategory category = new BusinessCategory();
			category.setId(id);//进行业务代码的唯一验证

			List<Map<String,Object>> list = businessCategoryMapper.queryListAll(category);
			if(null == list || list.size() < 1){
				logger.info("删除业务代码信息，业务代码信息不存在，businessId:{}",id);
				retMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
				retMap.put(Constant.MESSAGE, "业务信息不存在");
				return retMap;
			}
			//先判断是否关联有工单，有就不能删除
			List<Map<String,Object>> listR = businessCategoryMapper.findGatewayBusinessById(id);
			//
			if(listR == null || listR.size() == 0){
				businessCategoryMapper.deleteByPrimaryKey(id);
				//同时删除关联信息
				businessTemplateRelationMapper.deleteByBusinessCode((String)list.get(0).get("businessCode"));
				retMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
				retMap.put(Constant.MESSAGE, "删除成功");
				OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "删除网关业务代码", JSON.toJSONString(params));
			}else{
				retMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
				retMap.put(Constant.MESSAGE, "该业务类下有关联的工单,不能删除");
			}
			return retMap;
		} catch (Exception e) {
			logger.error("删除业务类异常：",e);
			retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
			retMap.put(Constant.MESSAGE, "服务器内部错误");
			return retMap;
		}
	}
}
