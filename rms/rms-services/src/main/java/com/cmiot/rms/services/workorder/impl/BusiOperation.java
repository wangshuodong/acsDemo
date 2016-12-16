package com.cmiot.rms.services.workorder.impl;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.cmiot.rms.services.util.InstructionUtil;
import com.cmiot.rms.services.workorder.bean.*;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.acs.model.struct.ParameterValueStructStr;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.workorder.IBusiOperation;
import com.cmiot.rms.services.workorder.exception.BusiException;
import com.cmiot.rms.services.workorder.util.TemplateDecoder;

/**
 * 业务的执行
 * 
 * @author lili
 *
 */
@Service("busiOperation")
public class BusiOperation implements IBusiOperation {

	private final static Logger logger = LoggerFactory.getLogger(BusiOperation.class);

	@Resource
	private InstructionMethodService instructionMethodService;

	/**
	 * 是否需要先查询实例
	 */
	private final static String SEARCH_FIRST_YES = "0";

	@SuppressWarnings("unused")
	private final static String CACHED_YES = "1";

	/**
	 * 执行模板业务流程
	 * 
	 * @param xmlTemplate
	 *            业务模板
	 * @param paramS
	 *            流程执行参数
	 * @return
	 */
	public Map<String, String> excute(String xmlTemplate, Map<String, Object> paramS) {
		Map<String, String> retMap = new HashMap<String, String>();
		logger.info("excute : {},{}", xmlTemplate, paramS);
		OperationFlow operationFlow = null;
		String gatewayMacAddress = paramS.get("gateWayMac") + "";
		// 首先进行解析xml
		try {
			operationFlow = TemplateDecoder.xmlDecoder(xmlTemplate);
		} catch (FileNotFoundException | UnsupportedEncodingException | DocumentException e) {
			e.printStackTrace();
			logger.error("xml decoder excute failure : {}", e.getMessage());
			retMap.put("MSG_CODE", "1");
			retMap.put("MSG", "解析模板失败!");
			return retMap;
		}
		
		boolean isSuccess = true;
		//执行之前需要设置一个值
		isSuccess = this.excuteSetRet(gatewayMacAddress, operationFlow, "0");
		if(!isSuccess){
			retMap.put("MSG_CODE", "1");
			retMap.put("MSG", "设置参数["+operationFlow.getOperationReturn()+"=0]失败!");
			return retMap;
		}
		
		//真正的业务执行
		retMap = this.busiExcute(operationFlow,paramS);
		
		//执行 之后根据执行情况再设置 成功为1 失败为2
		String result = "0".equals(retMap.get("MSG_CODE")) ? "1" : "2";
		
		isSuccess = this.excuteSetRet(gatewayMacAddress, operationFlow, result);
		if(!isSuccess || !"0".equals(retMap.get("MSG_CODE"))){
			retMap.put("MSG_CODE", "1");
			retMap.put("MSG", "设置参数[" + operationFlow.getOperationReturn() + "=" + result + "]失败!");
			return retMap;
		}
		
		return retMap;
		
	}
	
	@SuppressWarnings("rawtypes")
	private boolean excuteSetRet(String gatewayMacAddress,OperationFlow operationFlow,String value){
		boolean isSuccess = true;
		if(operationFlow.getOperationReturn() != null && !"".equals(operationFlow.getOperationReturn())){
			//准备设置参数
			List<ParameterValueStruct> list = new ArrayList<ParameterValueStruct>();
			ParameterValueStructStr pvs = new ParameterValueStructStr(operationFlow.getOperationReturn(), value + "");
			list.add(pvs);
			logger.debug("设置参数：{}", operationFlow.getOperationReturn() + " = " + value);
			//下发ServiceName
			if ("0".equals(value)) {
				String command = operationFlow.getServiceName();
				if (StringUtils.isNotBlank(command)&&command.indexOf(":")>-1) {
					String serviceNamecommand = command.substring(0, command.lastIndexOf(":"));
					String serviceName = command.substring(command.lastIndexOf(":")+1);
					list.add(new ParameterValueStructStr(serviceNamecommand, serviceName));
				}
			}
			
			isSuccess = instructionMethodService.setParameterValue(gatewayMacAddress, list);
		}
		return isSuccess;
	}
	
	/**
	 * 真正的业务执行
	 * @param operationFlow
	 * @param paramS
	 * @return
	 */
	public Map<String,String> busiExcute(OperationFlow operationFlow,Map<String, Object> paramS){
		Map<String, String> retMap = new HashMap<String, String>();
		String gatewayMacAddress = paramS.get("gateWayMac") + "";
		
		// 开始执行
		List<Flow> flows = operationFlow.getFlows();
		for (Flow flow : flows) {
			// 子流程操作
			if (flow instanceof FlowBean) {
				FlowBean flowBean = (FlowBean) flow;
				List<Flow> childFlow = flowBean.getFlows();
				// 流程的具体步骤 ADD SET操作
				for (Flow child : childFlow) {
					if (child instanceof FlowBeanAdd) { // 执行SET操作
						FlowBeanAdd addOP = (FlowBeanAdd) child;
						String objectName = null;
						try {
							//执行添加实例
							objectName = excuteAdd(addOP, paramS, gatewayMacAddress);
						} catch (Exception e) {
							retMap.put("MSG_CODE", "1");
							retMap.put("MSG", e.getMessage());
							logger.error("excuteAdd error:{}",e);
							return retMap;
						}

						// 将创建的实例放到MAP里面方便下面的操作使用
						paramS.put(addOP.getId(), objectName);
						if(!StringUtils.isEmpty(flow.getId()) && !StringUtils.isEmpty(objectName)) {
							paramS.put(flow.getId(), objectName);
						}
					} else if (child instanceof FlowBeanSet) { // 执行SET操作
						FlowBeanSet setOP = (FlowBeanSet) child;

						boolean isSuccess = false;
						try {
							//执行参数设置
							isSuccess = excuteSet(setOP, paramS, gatewayMacAddress);
						} catch (Exception e) {
							retMap.put("MSG_CODE", "1");
							retMap.put("MSG", e.getMessage());
							logger.error("excuteSet error:{}",e);
							return retMap;
						}

						if (!isSuccess) {
							retMap.put("MSG_CODE", "1");
							retMap.put("MSG", "设置参数失败!");
							logger.error("SET操作失败 : ", setOP.getId());
							return retMap;
						}
					}else if (child instanceof FlowBeanSearch) { // 执行SET操作
						FlowBeanSearch searchOP = (FlowBeanSearch) child;
						String objectName = null;
						try {
							//执行添加实例
							objectName = excuteSearch(searchOP, paramS, gatewayMacAddress);
						} catch (Exception e) {
							retMap.put("MSG_CODE", "1");
							retMap.put("MSG", e.getMessage());
							logger.error("excuteSearch e:{}",e);
							return retMap;
						}
						// 将创建的实例放到MAP里面方便下面的操作使用
						paramS.put(searchOP.getId(), objectName);
						if(!StringUtils.isEmpty(flow.getId()) && !StringUtils.isEmpty(objectName)) {
							paramS.put(flow.getId(), objectName);
						}
					}else if (child instanceof FlowBeanDelete) { // 执行delete操作
						FlowBeanDelete deleteOP = (FlowBeanDelete) child;
						try {
							//执行删除实例
							excuteDelete(deleteOP, paramS, gatewayMacAddress);
						} catch (Exception e) {
							retMap.put("MSG_CODE", "1");
							retMap.put("MSG", e.getMessage());
							logger.error("excuteSearch e:{}",e);
							return retMap;
						}
						
					}
				}
			}
		}
		retMap.put("MSG_CODE", "0");
		retMap.put("MSG", "操作成功!");
		logger.info("业务执行成功");
		paramS = null;
		return retMap;
	}

	/**
	 * 执行添加实例
	 * 
	 * @param addOP
	 * @param paramS
	 * @param gatewayMacAddress
	 * @return
	 */
	private String excuteAdd(FlowBeanAdd addOP, Map<String, Object> paramS, String gatewayMacAddress)
			throws BusiException {

		String objectName = null;
		int objNum = -1;
		//首先判断所依赖的查询实例是否有返回结果，如果有，则不需要做本步骤
		if(!StringUtils.isEmpty(addOP.getRefSearch())){
			String searchStr = (String) paramS.get(addOP.getRefSearch());
			if(!StringUtils.isEmpty(searchStr)){
				return objectName;
			}
		}
		// 首先判断是否依赖之前的实例
		if (addOP.getRef() != null && !"".equals(addOP.getRef())) {
			objectName = paramS.get(addOP.getRef()) + addOP.getCommand();
		} else {
			objectName = addOP.getCommand();
		}

		// 先判断节点是否需要先查询实例
		if (SEARCH_FIRST_YES.equals(addOP.getSearchFirst())) {

			logger.debug("执行查询：{}", objectName);
			Map<String, Object> m = instructionMethodService.getParameterNamesResult(gatewayMacAddress, objectName, true);
			if (Integer.valueOf(m.get("result").toString())==-1) {
				logger.debug("查询节点结果为空：{}", objectName);
				throw new BusiException("查询节点名称失败:" + objectName);
			}
			else if (m.size()==1) {// 没有实例就新增一个实例
				objNum = instructionMethodService.AddObject(gatewayMacAddress, objectName,
						System.currentTimeMillis() + "");
				logger.debug("创建实例：{}", objectName + objNum);
			} else {
				m.remove("result");
				// 有就进行遍历
				// 将参数存起来
				Iterator<String> k = m.keySet().iterator();
				int i = 1;
				Object paramName = null;
				while (k.hasNext()) {
					paramName = m.get(objectName + i + ".");
					if (paramName != null) {
						// 取中一个实例
						paramS.put(addOP.getId(), objectName + i + ".");
						return objectName + i + ".";
					}
					i++;
				}
				//最后如果paramName为空,还是需要创建实例
				if(paramName == null){
					objNum = instructionMethodService.AddObject(gatewayMacAddress, objectName,
							System.currentTimeMillis() + "");
					logger.debug("创建实例：{}", objectName + objNum);
				}
			}
		} else {
			// 新增实例
			objNum = instructionMethodService.AddObject(gatewayMacAddress, objectName, System.currentTimeMillis() + "");
			logger.debug("创建实例：{}", objectName + objNum);
		}
		if (objNum <= 0) {
			logger.error("AddObject 操作失败 : {}", objectName);
			throw new BusiException("创建实例失败:" + objectName);
		}
		return objectName + objNum + ".";
	}

	/**
	 * 执行查询
	 *
	 * @param searchOP
	 * @param paramS
	 * @param gatewayMacAddress
	 * @return
	 */
	private String excuteSearch(FlowBeanSearch searchOP, Map<String, Object> paramS, String gatewayMacAddress)
			throws BusiException {
		String returnPath = null;
		String objectName = null;
		String splitPath = searchOP.getSplitPath();
		// 首先判断是否依赖之前的实例
		if (searchOP.getRef() != null && !"".equals(searchOP.getRef())) {
			objectName = paramS.get(searchOP.getRef()) + searchOP.getCommand();
		} else {
			objectName = searchOP.getCommand();
		}
		logger.debug("执行查询：{}", objectName);
		Map<String, Object> m = instructionMethodService.getParameterNamesResult(gatewayMacAddress, objectName, false);
		//查询返回异常
		if (Integer.valueOf(m.get("result").toString())==-1) {
			logger.debug("查询节点结果为空：{}", objectName);
			throw new BusiException("查询节点名称失败:" + objectName);
		} else {
			m.remove("result");
			List<String> nameList = new ArrayList<>();
			List<FlowBeanMatch> matchList = searchOP.getList();
			for(FlowBeanMatch item:matchList) {
				Pattern pattern = Pattern.compile(item.getPattern());
				for (Map.Entry<String, Object> entry : m.entrySet()) {
					Matcher matcher = pattern.matcher(entry.getKey());
					if (matcher.find()) {
						nameList.add(entry.getKey());
					}
				}
			}
			boolean isMatch = false;
			if(nameList.size()>0) {
				//根据匹配到的路径获取值
				//4.根据具体的节点获取相应的value
				Map<String, Object> valueMap = instructionMethodService.getParameterValuesResult(gatewayMacAddress, nameList);
				if (Integer.valueOf(valueMap.get("result").toString())==-1) {
					throw new BusiException("查询节点值失败");
				}

				valueMap.remove("result");
				for(FlowBeanMatch item:matchList) {
					for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
						String value = entry.getValue() + "";
						if (!StringUtils.isEmpty(item.getMatchValue()) && !StringUtils.isEmpty(value)) {
							if (item.getMatchValue().equals(value)) {
								isMatch = true;
								String matchedPath = entry.getKey().substring(0, entry.getKey().indexOf(item.getLeaf()));
								for(FlowBeanMatch item1:matchList){
									String item1Path = matchedPath + item1.getLeaf();
									if(!item1.getMatchValue().equals(valueMap.get(item1Path) + "")){
										isMatch = false;
										break;
									}
									if(!StringUtils.isEmpty(item1.getId()) && item1.getId().equals(splitPath)){
										if (!StringUtils.isEmpty(searchOP.getSplitStr())) {
											returnPath = entry.getKey().substring(0, entry.getKey().indexOf(searchOP.getSplitStr()));
										}else{
											returnPath = entry.getKey();
										}
									}
								}

								if(isMatch){
									break;
								}
							}
						}
					}
					if(isMatch){
						break;
					}
				}
			}
			//如果没有全部匹配上直接返回空
			if(!isMatch){
				return null;
			}
		}
		return returnPath;
	}

	/**
	 * 执行添加实例
	 * 
	 * @param setOP
	 * @param paramS
	 * @param gatewayMacAddress
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean excuteSet(FlowBeanSet setOP, Map<String, Object> paramS, String gatewayMacAddress)
			throws BusiException {

		String objectName = null;
		//根据某个FLOW的ID查询FLOW的执行结果，如果执行结果不为空，则不需要执行
		if(!StringUtils.isEmpty(setOP.getIsNeedExcute())){
			if(!StringUtils.isEmpty((String) paramS.get(setOP.getIsNeedExcute()))){
				return true;
			}
		}
		// 执行操作
		//首先判断所依赖的查询实例是否有返回结果，如果有，则不去去依赖的ref
		if(!StringUtils.isEmpty(setOP.getRefSearch())){
			String searchStr = (String) paramS.get(setOP.getRefSearch());
			if(!StringUtils.isEmpty(searchStr)){
				objectName = paramS.get(setOP.getRefSearch()) + "";
			}
		}
		//如果refSearch没有查询到结果，则取依赖的ref
		if(StringUtils.isEmpty(objectName)) {
			// 首先判断是否依赖之前的实例
			if (setOP.getRef() != null && !"".equals(setOP.getRef())) {
				objectName = paramS.get(setOP.getRef()) + "";
			} else {
				objectName = "";
			}
		}
		//准备设置参数
		List<ParameterValueStruct> list = new ArrayList<ParameterValueStruct>();
		for (Param p : setOP.getParams()) {
			Object value = null;
			if (p.getParamKey() != null) {
				value = paramS.get(p.getParamKey());
				/*if (value == null) {
					throw new BusiException("SET操作失败 : 传入参数中,未取到[" + p.getParamKey() + "]值 " + value);
				}*/
				//添加支持DefaultKey
				if(value == null){
					value = paramS.get(p.getDefaultKey());
				}
				//添加拼接字符串
				if(p.getStitch() != null){
					value = value + p.getStitch();
				}
				//拆分字符串
				if(p.getSplit() != null){
					String ss = value + "";
					if(p.getBeforeOrAfter()!=null && "after".equals(p.getBeforeOrAfter())){
						value = ss.substring(ss.indexOf(p.getSplit()) + p.getSplit().length(),ss.length());
					}else{
						value = ss.substring(0,ss.indexOf(p.getSplit()));
					}
				}
			}
			if(value == null){
				if (p.getDefaultV() != null) {
					value = p.getDefaultV();
				}
			}
			if (value == null) {
				throw new BusiException("SET操作失败 : 未取到[" + objectName + p.getCommand() + "]值 " + value);
			}
			logger.debug("设置参数：{}", objectName + p.getCommand() + " = " + value);
			//为了兼容老模板，当没有valueType时，默认为string
			String valueType = p.getValueType() != null?p.getValueType():"string";
			if (value.toString().endsWith(".")) {
				value = value.toString().substring(0, value.toString().length()-1);
			}
			ParameterValueStruct pvs = new ParameterValueStruct(objectName + p.getCommand(), value ,valueType);
			list.add(pvs);
		}

		boolean isSuccess = instructionMethodService.setParameterValue(gatewayMacAddress, list);

		return isSuccess;
	}
	
	
	/**
	 * 执行删除实例
	 * 
	 * @param setOP
	 * @param paramS
	 * @param gatewayMacAddress
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean excuteDelete(FlowBeanDelete setOP, Map<String, Object> paramS, String gatewayMacAddress)
			throws BusiException {
		boolean isSuccess = true;
		String objectName = null;
		
		// 执行操作
		//首先判断所依赖的查询实例是否有返回结果，如果有，则不去去依赖的ref
		if(StringUtils.isNotEmpty(setOP.getRefSearch())){
			String searchStr = (String) paramS.get(setOP.getRefSearch());
			if(!StringUtils.isEmpty(searchStr)){
				objectName = paramS.get(setOP.getRefSearch()) + "";
			}
		}
		//如果refSearch没有查询到结果，则取依赖的ref
		if(StringUtils.isEmpty(objectName)) {
			// 首先判断是否依赖之前的实例
			if (StringUtils.isNotEmpty(setOP.getRef())) {
				objectName = paramS.get(setOP.getRef()) + "";
			} else {
				objectName = "";
			}
		}
		
		if (StringUtils.isNotEmpty(objectName)) {
			int  deleteNum = instructionMethodService.DeleteObject(gatewayMacAddress, objectName, System.currentTimeMillis() + "");
			if (deleteNum<1) {
				throw new BusiException("删除节点失败："+objectName);
			}
		}

		return isSuccess;
	}
}
