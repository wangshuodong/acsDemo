package com.cmiot.rms.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.ams.service.AreaService;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.constant.ConstantDiagnose;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.InstructionsStatusEnum;
import com.cmiot.rms.common.enums.LogTypeEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.GatewayParam;
import com.cmiot.rms.services.GatewayInfoInterface;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.GatewayParamService;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.LogManagerService;
import com.cmiot.rms.services.SendInstructService;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.instruction.InvokeInsService;

import org.springframework.util.StringUtils;

public class GatewayInfoInterfaceImpl implements GatewayInfoInterface{
	
	private static Logger logger = LoggerFactory.getLogger(GatewayInfoInterfaceImpl.class);
    
	@Autowired
    private GatewayInfoMapper gatewayInfoMapper;
	
	@Autowired
    private InvokeInsService invokeInsService;
	
	@Autowired
    InstructionsService instructionsService;
	
	@Autowired
	GatewayParamService gatewayParamService;
	
    @Autowired
    private LogManagerService logManagerService;
	 
    @Autowired
	InstructionMethodService instructionMethodService;
    
    @Resource
    private GatewayInfoService gatewayInfoService;
    
    @Resource
    private SendInstructService sendInstructService;

	@Autowired
	AreaService areaService;

    /**
     * 功能:dubbo服务接口、网关批量配置-网关分页查询功能
     * @param map 请求参数
     * @return
     */
    public Map<String,Object> queryDubboList4Page(Map<String,Object> map) {
    	Map<String,Object> backMap=new HashMap<String,Object>();
    	try {
	    	/*int page=1;
	    	int pageSize=10;
	    	if(map!=null && !map.isEmpty()){
	    		if(map.get("page")!=null && !"".equalsIgnoreCase(map.get("page").toString())){
	    			//设置当前页
	    			page=Integer.valueOf(map.get("page").toString());
	    		}
	    		if(map.get("pageSize")!=null && !"".equalsIgnoreCase(map.get("pageSize").toString())){
	    			//设置每页条数
	    			pageSize=Integer.valueOf(map.get("pageSize").toString());
	    		}else{
	    			map.put("pageSize", pageSize);
	    		}
	    	}*/

			logger.info(" uid " + String.valueOf(map.get("uid")));
			if(!(map.get("makeFactory") == null || map.get("makeFactory").equals(""))){
				map.put("makeMerchantsId",map.get("makeFactory").toString());
			}
			if(!(map.get("gatewayInfoFactoryCode") == null || map.get("gatewayInfoFactoryCode").equals(""))){
				map.put("gatewayFactoryCode",map.get("gatewayInfoFactoryCode").toString());
			}
			logger.info(" map " + map.toString());
			if(!(map.get("gatewayInfoAreaId") == null || map.get("gatewayInfoAreaId").equals(""))){
				List<com.cmiot.ams.domain.Area> areaList = areaService.findChildArea(Integer.parseInt(String.valueOf(map.get("gatewayInfoAreaId"))));
				if(areaList != null && areaList.size() > 0){
					StringBuffer sb=new StringBuffer();
					sb.append("(");
					for(int i = 0;i<areaList.size();i++){
						sb.append(","+areaList.get(i).getId());
					}
					sb.append(")");
					String par =sb.toString().replaceFirst(",","");
					map.put("gatewayAreaId",par);
				}
			}else{
				if (map.get("uid") != null && !"".equals(map.get("uid").toString().trim())) {
	                List<com.cmiot.ams.domain.Area> userAreaList = areaService.findAreaByAdmin((String) map.get("uid"));
	                if (userAreaList != null && userAreaList.size() > 0) {
	                   
                        StringBuffer sbNoArea = new StringBuffer();
                        sbNoArea.append("(");
                        for (int j = 0; j < userAreaList.size(); j++) {
                            sbNoArea.append("," + userAreaList.get(j).getId());
                        }
                        sbNoArea.append(")");
                        String par = sbNoArea.toString().replaceFirst(",", "");
                        map.put("gatewayAreaId",par);
	                }
	            }else{
	            	//区域ID为空时， 用户ID必填
	            	logger.error("网关分页查询功能缺少参数,区域ID为空时， 用户ID必填");
	        		backMap.put(Constant.CODE, RespCodeEnum.RC_1002.code());
	    	    	backMap.put(Constant.MESSAGE, "网关分页查询设备区域为空时用户ID必传");
	    	    	backMap.put(Constant.DATA, null);
	        		return backMap;
	            }
			}
			logger.info("gatewayAreaId " + String.valueOf(map.get("gatewayAreaId")));
	    	/*long totalSize=gatewayInfoMapper.queryCount(map);*/
			int page = (null != map.get("page")) ? Integer.valueOf(map.get("page").toString()) : 1;
			int pageSize = (null != map.get("pageSize")) ? Integer.valueOf(map.get("pageSize").toString()) : 10;
			PageHelper.startPage(page, pageSize);
	    	/*long startLine=(page-1)*pageSize;
	    	map.put("startLine", startLine);*/
	    	List<GatewayInfo> list=gatewayInfoMapper.queryPage(map);
	    	backMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
	    	backMap.put(Constant.MESSAGE, "网关分页查询功能");
	    	backMap.put("page", page);
			backMap.put("pageSize", pageSize);
	    	/*backMap.put("total", totalSize);*/
			backMap.put("total", ((Page) list).getTotal());
	    	backMap.put(Constant.DATA, JSON.toJSON(list));
	    	return backMap;
    	} catch (Exception e) {
    		e.printStackTrace();
    		logger.error("网关分页查询功能出错", e);
    		backMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
	    	backMap.put(Constant.MESSAGE, "网关分页查询功能出错");
	    	backMap.put(Constant.DATA, null);
    		return backMap;
		}
    }

    /**
     * 功能:dubbo服务接口-获取网关属性
     */
    public Map<String, Object> getParameterNames(Map<String, Object> map) {

        Map<String, Object> backMap = new HashMap<String, Object>();
        map.put("methodName", "GetParameterNames");
        logger.info("======下发指令参数--->" + map);
        // 查发指令
        Map<String, Object> result = null;
        try {
            boolean nextLevel = true;
            if ("false".equalsIgnoreCase(map.get("nextLevel").toString())) {
                nextLevel = false;
            }
            result = (Map<String, Object>) getParameterNames(map.get("id").toString(), map.get("path").toString(), nextLevel);
            //invokeInsService.executeOne(map);
            if (result == null || result.get("requestId") == null
                    || "".equals(result.get("requestId").toString())) {
                logger.info("获取网关属性下发指令失败！");
                backMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                backMap.put(Constant.MESSAGE, "获取网关属性下发指令失败");
                backMap.put(Constant.DATA, null);

                return backMap;
            }
        } catch (Exception e) {
            logger.error("获取网关属性下发指令出错！", e);
            backMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
            backMap.put(Constant.MESSAGE, "获取网关属性下发指令出错");
            backMap.put(Constant.DATA, null);

            return backMap;
        }
        // 定时获取返回数据
        try {
            String requestId = (String) result.get("requestId");
            List list = getParameterNamePolling(requestId);
            backMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
            backMap.put(Constant.MESSAGE, "获取网关属性功能");
            Object obj = toZTreeMap(list);
            backMap.put(Constant.DATA, obj);

            return backMap;
        } catch (Exception e) {
        	e.printStackTrace();
            logger.error("获取网关属性出错", e);
            backMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
            backMap.put(Constant.MESSAGE, "获取网关属性出错");
            backMap.put(Constant.DATA, null);

            return backMap;
        }
    }
    
    /**
     * 功能:dubbo服务接口-获取网关属性参数
     */
	public Map<String,Object> getParameterValues(Map<String, Object> map) {
		
		Map<String, Object> backMap = new HashMap<String, Object>();
		logger.info("======下发指令参数--->" + map);
		// 查发指令
		Map<String, Object> result = null;
		try {
			String id=map.get("id").toString();
			String path=map.get("path").toString();
			List<String> list=new ArrayList<String>();
			String[] paths=path.split(",");
			for(String ph:paths){
				list.add(ph);
			}	
			result = (Map<String, Object>)getParameterValues(id, list);
					
			if (result == null || result.get("requestId")==null 
					|| "".equals(result.get("requestId").toString())) {
				logger.info("获取网关属性参数下发指令失败！");
				backMap.put(Constant.CODE, -1);
				backMap.put(Constant.MESSAGE, "获取网关属性参数下发指令失败");
				backMap.put(Constant.DATA, null);
				
				return backMap;
			}
		} catch (Exception e) {
			logger.info("获取网关属性参数下发指令出错！", e);
			backMap.put(Constant.CODE, 1);
			backMap.put(Constant.MESSAGE, "获取网关属性参数下发指令出错");
			backMap.put(Constant.DATA, null);
			
			return backMap;
		}
		// 定时获取返回数据
		try {
			String requestId = (String) result.get("requestId");
			List<JSONObject> list = getParameterValuePolling(requestId);
			backMap.put(Constant.CODE, 0);
			backMap.put(Constant.MESSAGE, "获取网关属性参数功能");
			//把返回结果传化为Map结构
			Object obj=toParameterValueList(list);
			backMap.put(Constant.DATA, obj);
			
			return backMap;
		} catch (Exception e) {
			logger.error("获取网关属性参数出错", e);
			backMap.put(Constant.CODE, 1);
			backMap.put(Constant.MESSAGE, "获取网关属性参数出错");
			backMap.put(Constant.DATA, null);
			
			return backMap;
		}
	}
	
    /**
     * 功能:dubbo服务接口-设置网关属性参数
     */
	public Map<String, Object> setParameterValues(Map<String, Object> map) {
		// TODO Auto-generated method stub
		Map<String, Object> backMap = new HashMap<String, Object>();
		logger.info("======下发指令参数--->" + map);
		// 查发指令
		Map<String, Object> result = null;
		try {
			String id=map.get("id").toString();
			String listS=map.get("listS").toString();
				
			result = (Map<String, Object>)SetParameterValues(id, listS);
					
			if (result == null || result.get("requestId")==null 
					|| "".equals(result.get("requestId").toString())) {
				logger.info("设置网关属性参数下发指令失败！");
				backMap.put(Constant.CODE, -1);
				backMap.put(Constant.MESSAGE, "设置网关属性参数下发指令失败");
				backMap.put(Constant.DATA, null);
				
				return backMap;
			}
		} catch (Exception e) {
			logger.info("设置网关属性参数下发指令出错！", e);
			backMap.put(Constant.CODE, 0);
			backMap.put(Constant.MESSAGE, "设置网关属性参数下发指令出错");
			backMap.put(Constant.DATA, null);
			
			return backMap;
		}
		// 定时获取返回数据
		try {
			String requestId = (String) result.get("requestId");
			Map<String, String> msg = getAcsReturnBackMessage(requestId);
			if(msg!=null && msg.get("status")!=null && "1".equalsIgnoreCase(msg.get("status"))){
				backMap.put(Constant.CODE, 0);
				backMap.put(Constant.MESSAGE, "网关属性参数设置成功");
			}else if(msg!=null && msg.get("status")!=null && "2".equalsIgnoreCase(msg.get("status"))){
				backMap.put(Constant.CODE, -1);
				backMap.put(Constant.MESSAGE, "网关属性参数设置失败");
			}else{
				backMap.put(Constant.CODE, 0);
				backMap.put(Constant.MESSAGE, "正在处理当中");
			}
			
			backMap.put(Constant.DATA, null);
			
			
			Map<String,Object> parameterLog = new HashMap<>();
	        // 操作的数据内容
	        parameterLog.put("content",JSON.toJSONString(map));
	        // 登录用户名称
	        parameterLog.put("userName",map.get("userName"));
	        // 类目ID(菜单ID)
	        parameterLog.put("categoryMenu", CategoryEnum.GATEWAY_MANAGER_SERVICE.name());
	        // 具体的操作
	        parameterLog.put("operation","网关参数设置");
	        // 角色名称
	        parameterLog.put("roleName",map.get("roleName"));
	        // 类目名称
	        parameterLog.put("categoryMenuName",CategoryEnum.GATEWAY_MANAGER_SERVICE.description());
	        // 日志类型
            parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
	        logManagerService.recordOperationLog(parameterLog);
	        
			return backMap;
		} catch (Exception e) {
			logger.error("设置网关属性参数出错", e);
			backMap.put(Constant.CODE, 1);
			backMap.put(Constant.MESSAGE, "设置网关属性参数出错");
			backMap.put(Constant.DATA, null);
			return backMap;
		}
      
	}
	
    /**
     * 获取参数值
     *
     * @param id
     * @param list
     * @return
     * @throws Exception
     */
    public Object getParameterValues(String id, List<String> list) throws Exception {
        logger.info("传入getParameterNames参数:" + id + " " + list);
        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", id);
        map.put("methodName", "GetParameterValues");
        map.put("parameterList", list);
        Map<String, Object> result = invokeInsService.executeOne(map);
        return result;
    }
	
    /**
     * 获取参数名称公共方法
     *
     * @param id
     * @param path
     * @param nextLevel
     * @return
     */
    public Object getParameterNames(String id, String path, boolean nextLevel) throws Exception {
        logger.info("传入getParameterNames参数" + id + " " + path + " " + nextLevel);
        Map<String, Object> map = new HashMap<>();
        map.put("gatewayId", id);
        map.put("methodName", "GetParameterNames");
        map.put("parameterPath", path);
        map.put("nextLevel", nextLevel);
        Map<String, Object> result = invokeInsService.executeOne(map);
        return result;
    }
    
	/**
     * 功能:根据requestId循环获取网关属性
     *
     * @param requestId
     * @return
     */
    public List<JSONObject> getParameterNamePolling(String requestId) {
		Map<String, String> map = instructionsService.getInstructionsInfo(requestId);
		if(null != map && (!map.isEmpty()) && "1".equalsIgnoreCase(map.get("status").toString()))
		{
			String json = map.get("json");
			JSONObject jsonObject = JSON.parseObject(json);
			List<JSONObject> list = (List<JSONObject>) jsonObject.get("parameterList");
			return list;
		}

        return null;
    }
    
    /**
     * 功能:根据requestId循环获取网关属性参数
     *
     * @param requestId
     * @return
     */
    public List<JSONObject> getParameterValuePolling(String requestId) {
		Map<String, String> map = instructionsService.getInstructionsInfo(requestId);
		if(null != map && (!map.isEmpty()) && "1".equalsIgnoreCase(map.get("status").toString()))
		{
			String json = map.get("json");
			JSONObject jsonObject = JSON.parseObject(json);
			JSONObject jsonParameterList=(JSONObject)jsonObject.get("parameterList");
			List<JSONObject> list = (List<JSONObject>) jsonParameterList.get("parameterValueStructs");
			return list;
		}
        return null;
    }
    
    /**
     * 功能:把返回的网关属性传化为ztree节点treeMap的形式
     */
    public List<Map<String, Object>> toZTreeMap(List<JSONObject> list) {
        List backList = new ArrayList();
        if(list!=null && list.size()>0){
	        for (JSONObject jsonObject : list) {
	            if (jsonObject.getString("name").endsWith(".")) {
	                Map map = new HashMap();
	                map.put("isParent", true);
	                String name = jsonObject.getString("name");
	                String path = name.substring(0, name.lastIndexOf("."));
	                String nodeName = path.substring(path.lastIndexOf(".")+1, path.length());
	                map.put("name", nodeName);
	                map.put("readWrite", jsonObject.get("writable"));
	                backList.add(map);
	            } else {
	                Map map = new HashMap();
	                map.put("isParent", false);
	
	                String path = jsonObject.getString("name");
	                String nodeName = path.substring(path.lastIndexOf(".")+1, path.length());
	                map.put("name", nodeName);
	                map.put("readWrite", jsonObject.get("writable"));
	                backList.add(map);
	            }
	        }
        }
        return backList;
    }
    
    /**
     * 功能:把返回的网关属性参数转化为list
     */
	public List<Map<String, Object>> toParameterValueList(List<JSONObject> list) {
		List backList = new ArrayList();
		if (list != null && list.size() > 0) {
			for (JSONObject jsonObject : list) {
				Map map = new HashMap();
				map.put("valueType", jsonObject.getString("valueType"));
				map.put("name", jsonObject.getString("name"));
				map.put("readWrite", jsonObject.get("writable"));
				map.put("value", jsonObject.get("value"));
				
				backList.add(map);
			}
		}
		return backList;
	}
	
    /**
     * 设置参数值
     *
     * @param id
     * @param listS
     * @return
     * @throws Exception
     */
	public Object SetBatchParameterValues(String id, String listS) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("gatewayIds", id);
		map.put("methodName", "SetParameterValues");
		List<ParameterValueStruct> list = new ArrayList<>();
		try {
			list = JSON.parseArray(listS, ParameterValueStruct.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.put("pvList", list);
		
		Map<String, Object> backResult=new HashMap<String, Object>();
 		List<String> ids = new ArrayList<>();
		ids=JSON.parseArray(id, String.class);
		for(int i=0;i<ids.size();i++){
			List<String> idS=new ArrayList<String>(); 
			idS.add(ids.get(i));
			map.put("gatewayIds", idS);
			Map<String, Object> result = invokeInsService.executeBatch(map);
			logger.info("SetBatchParameterValues invokeInsService.executeBatch 请求数据:{} 返回数据:{}", map, result);
			//BUG IG-3447 抛java.lang.IndexOutOfBoundsException: Index: 0, Size: 0异常,根据原有业务逻辑添加对result进行判断
			if(null != result && result.size()>0){
				if(backResult.get("requestIds")!=null){
					List<String> requestIds=new ArrayList<String>();
					requestIds=(List<String>)result.get("requestIds");
					
					if(null != requestIds && requestIds.size()>0){
						List<String> backRequestIds=new ArrayList<String>();
						backRequestIds=(List<String>)backResult.get("requestIds");
						backRequestIds.add(requestIds.get(0));
					
						backResult.put("requestIds", backRequestIds);
					}
				}else{
					List<String> requestIds=new ArrayList<String>();
					requestIds=(List<String>)result.get("requestIds");
					
					if(null != requestIds && requestIds.size()>0){
						backResult.put("requestIds", requestIds);
					}
				}
			}
		}
//		JSONArray jsonArray=new JSONArray().parseArray(listS);
//		//修改网关数据
//		gatewayParamService.updateObject(jsonArray,id);
		
		return backResult;

	}
	
    /**
     * 设置参数值
     *
     * @param id
     * @param listS
     * @return
     * @throws Exception
     */
	public Object SetParameterValues(String id, String listS) throws Exception {
		Map<String, Object> map = new HashMap<>();
		map.put("gatewayId", id);
		map.put("methodName", "SetParameterValues");
		List<ParameterValueStruct> list = new ArrayList<>();
		try {
			list = JSON.parseArray(listS, ParameterValueStruct.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.put("pvList", list);
		Map<String, Object> result = invokeInsService.executeOne(map);
		return result;

	}

    /**
     * 功能:根据requestId循环获取网关属性参数
     * @param requestId
     * @return
     */
    public Map<String, String> getAcsReturnBackMessage(String requestId) {
        Map<String, String> map = null;
        for (int i = 0; i < Constant.COUNT_CYCLE; i++) { 	
            map = instructionsService.getInstructionsInfo(requestId);
            String status = map.get("status");
            if ("1".equalsIgnoreCase(status) || "2".equalsIgnoreCase(status)) {
                break;//跳出当前for循环
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 功能:dubbo服务接口实现、批量配置获取网关属性
     * @param parameter
     * @return
     */
	public Map<String, Object> getBatchParameterNames(Map<String, Object> parameter) {
		Map<String, Object> backMap = new HashMap<String, Object>();
		try {
			GatewayParam gatewayParam =new GatewayParam();
			gatewayParam.setParmFactory(parameter.get("parmFactory").toString().trim());
			//查询设备属性
	        List<GatewayParam> list = gatewayParamService.selectObject(gatewayParam);
	        //解析属性为treeMap
	        Map<String, Object> treeMap = listToMap(list);
	
	        List treeList = new ArrayList();
	        assemblyTree(treeList, treeMap);
	        
	        backMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			backMap.put(Constant.MESSAGE, "批量配置获取网关属性功能");
			Object obj=JSON.toJSON(treeList);
	    	backMap.put(Constant.DATA, obj);
			return backMap;
		} catch (Exception e) {
			logger.info("批量配置获取网关属性出错", e);
			backMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			backMap.put(Constant.MESSAGE, "批量配置获取网关属性出错");
			backMap.put(Constant.DATA, null);
			return backMap;
		}
        
	}
	
    public static Map<String, Object> listToMap(List<GatewayParam> list) {
        Map<String, Object> map = new ConcurrentHashMap<String, Object>();
        if (list != null && list.size() > 0) {
            for (GatewayParam param : list) {
                String key = param.getParmName();
                String[] objKey = key.split("\\.");
                Object value = param;
                assemblyMap(map, objKey, 0, value);
            }
        }
        return map;
    }
    
    public static void assemblyTree(List tree, Map map) {
        if (map != null && !map.isEmpty() && tree != null) {
            Set<Entry<String, Object>> set = map.entrySet();
            Iterator<Entry<String, Object>> it = set.iterator();
            while (it.hasNext()) {
                Entry entry = it.next();
                String key = entry.getKey().toString();
                Object obj = entry.getValue();

                Map mp = new ConcurrentHashMap();
                if (obj instanceof java.util.Map) {
                    List tr = new ArrayList();
                    mp.put("name", key);
                    assemblyTree(tr, (Map) obj);
                    mp.put("children", tr);
                } else if (obj instanceof java.lang.String) {
                    mp.put("name", key);
                    mp.put("value", obj);
                } else {
                    mp.put("name", key);
                    mp.put("value", obj);
                }
                if (obj instanceof java.lang.String) {
                    mp.put("type", "String");
                } else if (obj instanceof java.lang.Integer) {
                    mp.put("type", "int");
                } else if (obj instanceof java.lang.Long) {
                    mp.put("type", "long");
                }
                tree.add(mp);
            }
        }
    }
    
    /**
     * 功能：json对象转换为Map对象时循环调用方法
     *
     * @param map   上一级对象
     * @param key   当key以.号分开时分割的数组
     * @param index 当前key的第几个，从0开始
     * @param value 当前key对应的值
     */
    public static void assemblyMap(Map map, String[] key, Integer index, Object value) {

        //当前节点不存在
        if (map.get(key[index]) == null || "".equals(map.get(key[index]))) {
            //当前节点为最后一个节点
            if (key != null && key.length > 0 && (key.length - 1) == index) {
                map.put(key[index], value);
                return;
            } else if (key != null && key.length > 0) {
                Map<String, Object> childMap = new ConcurrentHashMap<String, Object>();
                map.put(key[index], childMap);
                assemblyMap(childMap, key, index + 1, value);
            }
        } else {//当前节点存在
            //当前节点为最后一个节点
            if (key != null && key.length > 0 && (key.length - 1) == index) {
                map.put(key[index], value);
                return;
            } else if (key != null && key.length > 0) {
                Map childMap = (Map) map.get(key[index]);
                //childMap.put(key[index], value);
                assemblyMap(childMap, key, index + 1, value);
            }
        }
    }

    /**
     * 功能:dubbo服务接口实现、批量配置设置网关属性参数
     * @param map
     * @return
     */
	public Map<String, Object> setBatchParameterValues(Map<String, Object> map) {
		// TODO Auto-generated method stub
		Map<String, Object> backMap = new HashMap<String, Object>();
		logger.info("======下发指令参数--->" + map);
		// 查发指令
		Map<String, Object> result = null;
		List<String> gatewayIdList = null;
		try {
			String gatewayIds=map.get("gatewayIds").toString().trim();
			
			//对传入的参数做验证
			if(org.apache.commons.lang.StringUtils.isBlank(gatewayIds)){
				backMap.put(Constant.CODE, -1);
				backMap.put(Constant.MESSAGE, "获取网关信息失败,请检查传入参数");
				backMap.put(Constant.DATA, null);
				return backMap;
			}
			
			//获取传入网关 ID 集为了后面计算失败使用(临时处理方法后续业务优化)
			gatewayIdList =JSON.parseArray(gatewayIds, String.class);
			
			String listS=map.get("listS").toString().trim();
			
			//下面result返回的值是正常可用网关的requestIds集(比如当前传入3个的网关,1个未注册,那么result返回两个可用的requestIds)
			result = (Map<String, Object>)SetBatchParameterValues(gatewayIds, listS);
					
			if (result == null || result.get("requestIds")==null 
					|| "".equals(result.get("requestIds").toString())) {
				logger.info("批量配置设置网关属性参数下发指令失败！");
				backMap.put(Constant.CODE, -1);
				backMap.put(Constant.MESSAGE, "批量配置设置网关属性参数下发指令失败");
				backMap.put(Constant.DATA, null);
				
				return backMap;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("批量配置设置网关属性参数下发指令出错！", e);
			backMap.put(Constant.CODE, 1);
			backMap.put(Constant.MESSAGE, "批量配置设置网关属性参数下发指令出错");
			backMap.put(Constant.DATA, null);
			
			return backMap;
		}
		// 定时获取返回数据
		try {
			List<String> requestIds = (List<String>) result.get("requestIds");
			Integer successCount = 0;//成功条数
	        Integer failCount = 0; //失败条数
	        Integer prcessingCount = 0; //处理中条数
	        List<String> failCpeSns = new ArrayList<>();

//			for(String id:requestIds){
//				Map<String, String> msg = getAcsReturnBackMessage(id);
//				 Integer status = Integer.valueOf(msg.get("status"));
//	                if (status == InstructionsStatusEnum.STATUS_2.code()) {
//	                    failCpeSns.add(msg.get("cpeIdentity"));
//	                    failCount++;
//	                }
//	                if (status == InstructionsStatusEnum.STATUS_1.code()) {//成功条数
//	                    successCount++;
//	                }
//	                if (status == InstructionsStatusEnum.STATUS_0.code()) {
//	                    prcessingCount++;
//	                }
//			}
			
			boolean flag=false;
			for (int i = 0; i < Constant.COUNT_CYCLE; i++) { 	
				for(String id:requestIds){
					Map<String, String> msg = instructionsService.getInstructionsInfo(id);
					Integer status = Integer.valueOf(msg.get("status"));
	                if (status == InstructionsStatusEnum.STATUS_2.code()) {
	                    failCpeSns.add(msg.get("cpeIdentity"));
	                    failCount++;
	                    flag=true;
	                }
	                if (status == InstructionsStatusEnum.STATUS_1.code()) {//成功条数
	                    successCount++;
	                    flag=true;
	                }
	                if (status == InstructionsStatusEnum.STATUS_0.code()) {
	                   // prcessingCount++;
	                }
				}
				
				try {
	                Thread.sleep(2000);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
				if(flag){
					break;
				}
	        }
			
//			Map<String, String> msg = getAcsReturnBackMessage(requestId);
//			if(msg!=null && msg.get("status")!=null && "1".equalsIgnoreCase(msg.get("status"))){
//				backMap.put("resultCode", RespCodeEnum.RC_0.code());
//				backMap.put("resultMessage", "批量配置设置网关属性参数设置成功");
//			}else if(msg!=null && msg.get("status")!=null && "2".equalsIgnoreCase(msg.get("status"))){
//				backMap.put("resultCode", RespCodeEnum.RC_1.code());
//				backMap.put("resultMessage", "批量配置设置网关属性参数设置失败");
//			}else{
//				backMap.put("resultCode", RespCodeEnum.RC_1.code());
//				backMap.put("resultMessage", "正在处理当中");
//			}
			
			Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("successCount", successCount);
            //总网关数-成功网关数+成功网关执行失败数
			int fc = gatewayIdList.size() - requestIds.size() + failCount;
            returnMap.put("failCount", fc);
            returnMap.put("prcessingCount", requestIds.size()-successCount-failCount);
            returnMap.put("failList", failCpeSns);
            if (prcessingCount > 0) {//如果有正在处理中的数据，设置状态
                returnMap.put("status", InstructionsStatusEnum.STATUS_0.code());
            } else {
                returnMap.put("status", InstructionsStatusEnum.STATUS_1.code());
            }

            backMap.put(Constant.CODE, 0);
			backMap.put(Constant.MESSAGE, "批量设置网关属性参数功能");
			backMap.put(Constant.DATA, returnMap);
			
			Map<String,Object> parameterLog = new HashMap<>();
	        // 操作的数据内容
	        parameterLog.put("content",JSON.toJSONString(map));
	        // 登录用户名称
	        parameterLog.put("userName",map.get("userName"));
	        // 类目ID(菜单ID)
	        parameterLog.put("categoryMenu", CategoryEnum.GATEWAY_MANAGER_SERVICE.name());
	        // 具体的操作
	        parameterLog.put("operation","网关参数设置");
	        // 角色名称
	        parameterLog.put("roleName",map.get("roleName"));
	        // 类目名称
	        parameterLog.put("categoryMenuName",CategoryEnum.GATEWAY_MANAGER_SERVICE.description());
	        // 日志类型
            parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
	        logManagerService.recordOperationLog(parameterLog);
			
			return backMap;
		} catch (Exception e) {
			logger.error("设置网关属性参数出错", e);
			backMap.put(Constant.CODE, 1);
			backMap.put(Constant.MESSAGE, "设置网关属性参数出错");
			backMap.put(Constant.DATA, null);
			return backMap;
		}
      
	}

	@Override
	public Map<String, Object> getGatewayPhysicalInterfaceState(Map<String, Object> parameter) {
		
		//1.根据网关MAC查询网关ID
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = macMap.get(Constant.MAC) == null ? null :macMap.get(Constant.MAC).toString();
        parameter.remove(Constant.PARAMETER);
        parameter.remove(Constant.RPCMETHOD);
        if(gatewayMacAddress == null){
        	logger.info("请求获取网关物理接口状态时输入MAC地址为空");
        	parameter.put(Constant.RESULT, -102);
        	parameter.put(Constant.STATUS, -102);
            return parameter;
        }
        //2.下发指令并获取结果 
        List<String> namesList = new ArrayList<>();
        namesList.add(ConstantDiagnose.LANDEVICE);
        Map<String, Object> resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, namesList);
        if(resultMap == null ){
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求获取网关物理接口状态失败，原因：网关MAC地址不存在");
        	parameter.put(Constant.RESULT, -201);
        	parameter.put(Constant.STATUS, -201);
        	parameter.put(Constant.FAILREASON, "网关MAC地址不存在");
            return parameter;
        }
       // System.out.println(resultMap.keySet());
      //  List<Map<String, Object>> lanDeviceList = (List<Map<String, Object>>) resultMap.get(ConstantDiagnose.LANDEVICE);
        Map<String, Object> returnMap = new HashMap<String, Object>();
        List<Map<String, Object>> wlanReturnList = new ArrayList<Map<String,Object>>();
		if(resultMap != null && !resultMap.isEmpty()){

			//获取LAN结果
			for(int i=0;i<4;i++){
				boolean enable = (boolean) resultMap.get(ConstantDiagnose.LANETHERNETINTERFACECONFIG + (i+1) + ".Enable");
				returnMap.put("LAN"+(i+1)+"Status", enable ? "ON" : "OFF" );
			}
			//获取WLAN结果
			String regExService= ConstantDiagnose.WLANCONFIGURATION+"[1-9].SSID";
			Pattern patternService = Pattern.compile(regExService);
			namesList = new ArrayList<>();
			Set<String> keys = resultMap.keySet();
			for(String key : keys){

				Matcher matchService = patternService.matcher(key);
				if(matchService.matches()){
					namesList.add(key);
				}
			}
			for(int i=0;i<namesList.size();i++){
				String  ssid =  resultMap.get(namesList.get(i))+"";
				boolean  enable =   (boolean)resultMap.get(namesList.get(i).substring(0, namesList.get(i).lastIndexOf("."))+".Enable");
				Map<String, Object> wanlanReturnMap = new HashMap<String, Object>();
				wanlanReturnMap.put("SSIDIndex", ssid );
				wanlanReturnMap.put("Enable", enable ? "1" : "0" );
				wlanReturnList.add(wanlanReturnMap);
			}

			returnMap.put("WIFIStatus", wlanReturnList);
		}

        //获取PonStatus
        Map<String, Object> namesMap = instructionMethodService.getParameterNames(gatewayMacAddress, ConstantDiagnose.WANDEVICE, false);
		String regExService= "InternetGatewayDevice.WANDevice.[0-9].X_CMCC_GponInterfaceConfig.Status";
		Pattern patternService = Pattern.compile(regExService);
		Set<String> keys = namesMap.keySet();
        namesList = new ArrayList<>();
        for(String key : keys){
        	
        	Matcher matchService = patternService.matcher(key);
        	if(matchService.find()){
	        	namesList.add(key);
        	}
        }
        String statusStr = "";
        if(namesList.size()>0){
        	resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, namesList);
        	for(String name : namesList){
        		Object status = resultMap.get(name);
        		if(status != null && !"".equals(status)){
        			statusStr = status.toString();
        			break;
        		}
        	}
        }
        if("".equals(statusStr)){
        	//如果没有查询到状态，那么继续查询EPON的状态
        	regExService= "InternetGatewayDevice.WANDevice.[0-9].X_CMCC_EPONInterfaceConfig.Status";
            patternService = Pattern.compile(regExService);
            namesList = new ArrayList<>();
            for(String key : keys){
            	
            	Matcher matchService = patternService.matcher(key);
            	if(matchService.matches()){
    	        	namesList.add(key);
            	}
            }
            if(namesList.size()>0){
            	resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, namesList);
            	for(String name : namesList){
            		Object status = resultMap.get(name);
            		if(status != null && !"".equals(status)){
            			statusStr = status.toString();
            			break;
            		}
            	}
            }
        }
        if("".equals(statusStr)){
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求获取PON状态失败，原因：没有找到对应的PON信息");
        }
        
        returnMap.put("PonStatus", statusStr);
        
        
        parameter.put(Constant.RESULT, 0);
        parameter.put(Constant.STATUS, 0);
        parameter.put("ResultData", returnMap);
		return returnMap;
	}

	@Override
	public Map<String, Object> getVoIPStatus(Map<String, Object> parameter) {
		//1.根据网关MAC查询网关ID
        Map<String, Object> macMap = (Map<String, Object>)parameter.get(Constant.PARAMETER);
        String gatewayMacAddress = macMap.get(Constant.MAC) == null ? null :macMap.get(Constant.MAC).toString();
        parameter.remove(Constant.PARAMETER);
        parameter.remove(Constant.RPCMETHOD);
        if(gatewayMacAddress == null){
        	logger.info("请求获取VoIP状态时输入MAC地址为空");
        	parameter.put(Constant.RESULT, -102);
        	//parameter.put(Constant.FAILREASON, "MAC地址为空");
            return parameter;
        }
        //2.下发指令并获取结果 
        Map<String, Object> resultMap = instructionMethodService.getParameterNames(gatewayMacAddress, ConstantDiagnose.VoIp, false);
        if(resultMap == null ){
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求获取VoIP状态失败，原因：网关MAC地址不存在");
        	parameter.put(Constant.RESULT, -201);
        	//parameter.put(Constant.FAILREASON, "网关MAC地址不存在");
            return parameter;
        }
        //获取URI
        String regExService= "InternetGatewayDevice.Services.VoiceService.[0-9].VoiceProfile.[0-9].Line.[1-9].SIP.URI";
        Pattern patternService = Pattern.compile(regExService);
        Set<String> keys = resultMap.keySet();
        List<String> namesList = new ArrayList<>();
        for(String key : keys){
        	
        	Matcher matchService = patternService.matcher(key);
        	if(matchService.matches()){
	        	namesList.add(key);
        	}
        }
        //返回值
        String VoIPName = "";
        //查状态用
        String url = "";
        String lineStatus = "";
        if(namesList.size()>0){
        	resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, namesList);
        	for(String name : namesList){
        		Object uri = resultMap.get(name);
        		if(uri != null && !"".equals(uri)){
        			VoIPName = uri.toString();
        			url = name;
        			break;
        		}
        	}
        }else{
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求获取VoIP状态失败，原因：没有找到对应的VoIP信息");
        	parameter.put(Constant.RESULT, -205);
        	parameter.put(Constant.STATUS, -205);
        	parameter.put(Constant.FAILREASON, "VoIP信息不存在");
        	return parameter;
        }
        if("".equals(VoIPName)){
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求获取VoIP状态失败，原因：没有找到VoIP对应的电话号码信息");
        	parameter.put(Constant.RESULT, -400);
        	//parameter.put(Constant.FAILREASON, "没有找到VoIP对应的电话号码信息");
        	return parameter;
        }
        
        //获取状态
        String reqName= url.substring(0, url.indexOf(".SIP.URI")) + ".Status";
        namesList = new ArrayList<String>();
        namesList.add(reqName);
        resultMap = instructionMethodService.getParameterValues(gatewayMacAddress, namesList);
        if(resultMap == null){
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求获取VoIP状态失败，原因：没有找到对应VoIP信息");
        	parameter.put(Constant.RESULT, -205);
        	parameter.put(Constant.STATUS, -205);
        	parameter.put(Constant.FAILREASON, "没有找到对应VoIP信息");
        	return parameter;
        }else{
        	lineStatus = resultMap.get(reqName) == null ? "" : resultMap.get(reqName).toString();
        }
 /*       if("".equals(lineStatus)){
        	logger.info("网关MAC地址："+gatewayMacAddress+"请求获取VoIP状态失败，原因：没有找到VoIP对应lineStatus信息");
        	parameter.put(Constant.RESULT, -201);
        	parameter.put(Constant.STATUS, -201);
        	parameter.put(Constant.FAILREASON, "没有找到VoIP对应lineStatus信息");
        	return parameter;
        }*/
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("VoIPName", VoIPName);
        result.put("LineStatus", lineStatus);
        parameter.put("ResultData", result);
        parameter.put(Constant.RESULT, 0);
        parameter.put(Constant.STATUS, 0);
		return parameter;
	}
	
	
	
}
