/**
 * 
 */
package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.GatewayGroupMapper;
import com.cmiot.rms.dao.model.GatewayGroup;
import com.cmiot.rms.services.GatewayGroupManagementService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

/**
 * @author heping
 *
 */
public class GatewayGroupManagementServiceImpl implements GatewayGroupManagementService{
	
	private static final Logger logger = LoggerFactory.getLogger(GatewayGroupManagementServiceImpl.class);
	
	@Autowired
	GatewayGroupMapper gatewayGroupMapper;
	
	@Override
	public Map<String, Object> addGroup(Map<String, Object> parameter) {
		logger.info("开始添加分组:parameter{}",parameter.toString());
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String groupName = (String)parameter.get("groupName");
		String gatewayList = (String)parameter.get("gatewayList");
		String uuid = UniqueUtil.uuid();

		try
		{
		if(checkGroupName(groupName))
		{
			resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			resultMap.put(Constant.MESSAGE, "分组名称不合法");
			return resultMap;
		}	
		
		if(checkGatewayList(gatewayList))
		{
			resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			resultMap.put(Constant.MESSAGE, "选择的网关列表为空");
			return resultMap;
		}
		
			GatewayGroup gatewayGroup = new GatewayGroup();
			gatewayGroup.setGroupUuid(uuid);
			gatewayGroup.setGroupName(groupName);
			gatewayGroup.setGatewayUuid(gatewayList);
			gatewayGroupMapper.insert(gatewayGroup);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
			resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			resultMap.put(Constant.MESSAGE, "分组添加失败");
			return resultMap;
		}
		
		logger.info("添加分组结束:parameter{}",parameter.toString());
		resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
		resultMap.put(Constant.MESSAGE, "分组添加成功");
		return resultMap;
	}

	private boolean checkGatewayList(String gatewayList) {
		if(StringUtils.isBlank(gatewayList))
		{
			return true;
		}	
		
		return false;
	}

	private boolean checkGroupName(String groupName) {
		if(StringUtils.isBlank(groupName))
		{
			return true;
		}	
		
		GatewayGroup example = new GatewayGroup();
		example.setGroupName(groupName);
		
		GatewayGroup list= gatewayGroupMapper.selectByParam(example);
		if(list != null)
		{
			return true;
		}	
		
		return false;
	}

	@Override
	public Map<String, Object> updateGroup(Map<String, Object> parameter) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		if(validate(parameter))
		{
			resultMap.put(Constant.DATA, null);
			resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			resultMap.put(Constant.MESSAGE, "参数不合法");
			return resultMap;
		}
		
		String newids = generateNewGroup(parameter);
			
		GatewayGroup example = new GatewayGroup();
		example.setGatewayUuid(newids);
		example.setGroupUuid((String)parameter.get("groupId"));		
		gatewayGroupMapper.updateByExampleSelective(example);

			
		resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
		resultMap.put(Constant.MESSAGE, "更新组成功");
		return resultMap;
	}

	private String generateNewGroup(Map<String, Object> parameter) {
		GatewayGroup group = new GatewayGroup();
		
		String gatewayUuids = (String)parameter.get("gatewayList");
		String groupId = (String)parameter.get("groupId");
		group .setGroupUuid(groupId);
		GatewayGroup gatewayGroup = gatewayGroupMapper.selectByParam(group);
		
		Set<String> set = new HashSet<String>();
		String[] uuidArray = gatewayUuids.split(",");
		for(String uuid : uuidArray)
		{
			set.add(uuid);
		}
		
		String[] newArray = gatewayGroup.getGatewayUuid().split(",");
		for(String uuid : newArray)
		{
			set.add(uuid);
		}
		
		StringBuilder sb = new StringBuilder();
		for(Object id : set.toArray())
		{
			sb.append(id).append(",");
		}	
		
		return sb.toString().substring(0, sb.toString().length()-1);
	}

	private boolean validate(Map<String, Object> parameter) {
		String groupId = (String)parameter.get("groupId");
		String gatewayList = (String)parameter.get("gatewayList");
		
		if(StringUtils.isBlank(groupId))
		{
			return true;
		}	
		
		if(StringUtils.isBlank(gatewayList))
		{
			return true;
		}
		
		GatewayGroup group = new GatewayGroup();
		group.setGroupUuid(groupId);
		GatewayGroup gatewayGroup = gatewayGroupMapper.selectByParam(group);
		if(gatewayGroup == null)
		{
			return true;
		}	
		
		return false;
	}

	@Override
	public Map<String, Object> queryGroup(Map<String, Object> parameter) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<GatewayGroup> list = new ArrayList<GatewayGroup>();
		try
		{	GatewayGroup example = new GatewayGroup();

		int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
		int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;
		PageHelper.startPage(page, pageSize);

			if(parameter!=null){
				if(parameter.get("groupUuid") != null){
					example.setGroupUuid(String.valueOf(parameter.get("groupUuid")));
				}
				if(parameter.get("groupName") != null){
					example.setGroupName(String.valueOf(parameter.get("groupName")));
				}
			}
			list = gatewayGroupMapper.selectByExample(example);

			resultMap.put("page", page);
			resultMap.put("pageSize", pageSize);
			resultMap.put("total", ((Page) list).getTotal());
			resultMap.put(Constant.DATA, JSON.toJSON(list));
			resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			resultMap.put(Constant.MESSAGE, "查询列表成功");
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
			resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			resultMap.put(Constant.MESSAGE, "查询列表失败");
			return resultMap;
		}
		return resultMap;
	}
	@Override
	public Map<String, Object> queryAllGroup(Map<String, Object> parameter) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<GatewayGroup> list = new ArrayList<GatewayGroup>();
		try
		{	GatewayGroup example = new GatewayGroup();

		if(parameter!=null){
			if(parameter.get("groupUuid") != null){
				example.setGroupUuid(String.valueOf(parameter.get("groupUuid")));
			}
			if(parameter.get("groupName") != null){
				example.setGroupName(String.valueOf(parameter.get("groupName")));
			}
		}
		list = gatewayGroupMapper.selectByExample(example);

		resultMap.put(Constant.DATA, JSON.toJSON(list));
		resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
		resultMap.put(Constant.MESSAGE, "查询列表成功");
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
			resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			resultMap.put(Constant.MESSAGE, "查询列表失败");
			return resultMap;
		}
		return resultMap;
	}

	@Override
	public Map<String, Object> deleteGroup(Map<String, Object> parameter) {
		logger.info("开始删除分组:parameter{}",parameter.toString());

		Map<String, Object> resultMap = new HashMap<String, Object>();
		String groupId = (String)parameter.get("groupId");
		if(StringUtils.isBlank(groupId))
		{
			resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			resultMap.put(Constant.MESSAGE, "传入的分组id为空");
			return resultMap;
		}	
		try
		{
			GatewayGroup example = new GatewayGroup();
			example.setGroupUuid(groupId);
			gatewayGroupMapper.deleteByExample(example);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
			resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			resultMap.put(Constant.MESSAGE, "删除失败");
			return resultMap;
		}
		
		logger.info("删除分组结束}");
		resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
		resultMap.put(Constant.MESSAGE, "分组删除成功");
		return resultMap;
	}

}
