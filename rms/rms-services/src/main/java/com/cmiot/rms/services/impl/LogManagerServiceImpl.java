package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.ams.domain.Admin;
import com.cmiot.ams.service.AdminService;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.LogTypeEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.enums.SeparatorEnum;
import com.cmiot.rms.common.logback.LogBackRecord;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.LogAutoBakMapper;
import com.cmiot.rms.dao.mapper.LogBackupRecordMapper;
import com.cmiot.rms.dao.mapper.LoggingEventMapper;
import com.cmiot.rms.dao.model.*;
import com.cmiot.rms.services.LogManagerService;
import com.cmiot.rms.services.util.CsvUtil;
import com.cmiot.rms.services.util.OperationLogUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.*;

/**
 * Created by wangzhen on 2016/4/19.
 */
public class LogManagerServiceImpl implements LogManagerService {
    private static Logger logger = LoggerFactory.getLogger(LogManagerServiceImpl.class);

    @Autowired
    private LoggingEventMapper loggingEventMapper;

    @Autowired
    private LogAutoBakMapper logAutoBakMapper;

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private LogBackupRecordMapper logBackupRecordMapper;
    
    @Value(value = "${ds.url}")
    private String url;
    
    @Value(value="${ds.username}")
    private String username;
    
    @Value(value="${ds.password}")
    private String password;
    
    @Value(value="${log.backup.dir}")
    private  String logbackupdir;
    
    @Override
    public Map<String, Object> queryOperationLog(Map<String, Object> parameter) {
        logger.info("进入日志查询,参数：" + JSON.toJSONString(parameter));
        Map<String, Object> resultMap = new HashMap<>();

        int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()):1;
        int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()):10;

        PageHelper.startPage(page, pageSize);
        LoggingEvent loggingEvent = new LoggingEvent();

        String startTime = String.valueOf(parameter.get("startTime"));
        String endTime = String.valueOf(parameter.get("endTime"));
//        String categoryMenuName = String.valueOf(parameter.get("categoryMenuName"));
        String userName = String.valueOf(parameter.get("accountName"));
        String logTypeName = String.valueOf(parameter.get("logTypeName"));
        String uid = String.valueOf(parameter.get("uid"));
        String adminName = String.valueOf(parameter.get("userName"));
//        if (StringUtils.isNotEmpty(categoryMenuName)){
//            String categoryMenu = CategoryEnum.getDescription(categoryMenuName);
//            if (categoryMenu == null){
//                resultMap.put("total",0);
//                resultMap.put("page",page);
//                resultMap.put("pageSize",pageSize);
//                resultMap.put(Constant.DATA,"");
//                resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
//                resultMap.put(Constant.MESSAGE, "传入菜单类目不存在");
//                return resultMap;
//            }
//            loggingEvent.setArg1(categoryMenu);
//        }
        if(StringUtils.isEmpty(logTypeName) || null == LogTypeEnum.getCode(logTypeName)){
	        resultMap.put("total",0);
	        resultMap.put("page",page);
	        resultMap.put("pageSize",pageSize);
	        resultMap.put(Constant.DATA,"");
	        resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
	        resultMap.put(Constant.MESSAGE, "传入日志类型不存在");
	        return resultMap;
        }
    	String logType = LogTypeEnum.getCode(logTypeName);
    	loggingEvent.setArg0(logType);
    	//查询操作日志时  
    	if(logType.equals(LogTypeEnum.LOG_TYPE_OPERATION.code())){
    		//用户列表（用于控制查询权限,当前用户只能查询自己和下级的日志）
            List<String> userList = Lists.newArrayList(); 
    		//调用ams接口获取用户列表
            Map<String,Object> userMap = adminService.findChildAdmin(uid);
            if(userMap != null && String.valueOf(userMap.get("resultCode")).equals("0")){
            	String isAdmin = String.valueOf(userMap.get("super")); 
            	
            	List<Admin> list = (List<Admin>) userMap.get("child");
            	if(isAdmin.equals("true")){
            		userList.add("admin");
            	}else if(list.size() > 0){
            		for(Admin admin : list){
            			userList.add(admin.getAccount());
            		}
            	}else{
            		userList.add(adminName);//非超级管理员、安全管理员查询自己的日志
            	}
            }else{
            	logger.info("日志查询，获取当前登录用户查询权限列表失败,调用AMS的findChildAdmin接口，返回：" + JSON.toJSONString(userMap));
            	resultMap.put("total",0);
                resultMap.put("page",page);
                resultMap.put("pageSize",pageSize);
                resultMap.put(Constant.DATA,"");
                resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                resultMap.put(Constant.MESSAGE, "无查询权限");
                return resultMap;
               
            }
            //
            if(!StringUtils.isEmpty(userName)){
            	if(!userList.contains("admin")){
            		loggingEvent.setArg3(userName);
            		loggingEvent.setUserList(userList);
            	}else{
            		loggingEvent.setArg3(userName);
            	}
            }else{
            	if(!userList.contains("admin")){
            		loggingEvent.setUserList(userList);
            	}
            }
    	}else if(logType.equals(LogTypeEnum.LOG_TYPE_SAFE.code()) || logType.equals(LogTypeEnum.LOG_TYPE_SYSTEM.code())){//查询安全日志,系统日志，增加按用户名查询，不设权限
    		if(StringUtils.isNotEmpty(userName))loggingEvent.setArg3(userName);
    	}
        
        
        if (StringUtils.isNotEmpty(startTime)){
            Date date = DateTools.parse(startTime, DateTools.YYYY_MM_DD_HH_MM_SS);
            String st = String.valueOf(date.getTime());
            loggingEvent.setStartTime(Long.valueOf(st));
        }
        if (StringUtils.isNotEmpty(endTime)){
            Date date = DateTools.parse(endTime, DateTools.YYYY_MM_DD_HH_MM_SS);
            String et = String.valueOf(date.getTime());
            loggingEvent.setEndTime(Long.valueOf(et));
        }

        List<LoggingEvent> list = loggingEventMapper.queryList(loggingEvent);
        List<LoggingBean> resultList = new ArrayList<>();
        if(logType.equals(LogTypeEnum.LOG_TYPE_ALARM.code())){//告警日志，返回结果单独处理
        	resultList = eventToBeanLog(list, resultList);
        }else{
        	resultList = eventToBean(list, resultList);
        }
        resultMap.put("total",((Page)list).getTotal());
        resultMap.put("page",page);
        resultMap.put("pageSize",pageSize);
        resultMap.put(Constant.DATA,JSON.toJSON(resultList));
        resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
        resultMap.put(Constant.MESSAGE, "");

        return resultMap;
    }

    @Override
    public Map<String, Object> recordOperationLog(Map<String, Object> parameter) {
        Map<String, Object> resultMap = new HashMap<>();
        // 操作的数据内容
        String content = (String) parameter.get("content");
        // 登录用户名称
        String userName = (String) parameter.get("userName");
        // 类目ID
        String categoryMenu = (String) parameter.get("categoryMenu");
        // 具体的操作
        String operation = (String) parameter.get("operation");
        // 角色名称
        String roleName = (String) parameter.get("roleName");
        // 类目名称
        String categoryMenuName = (String) parameter.get("categoryMenuName");
        // 日志类别
        String logType = (String) parameter.get("logType");
        //
        String logTypeName = LogTypeEnum.getDescription(logType) == null ? "" : LogTypeEnum.getDescription(logType);
        LogBackRecord.logBackBean("", content, "",userName, categoryMenu, "", operation, "", roleName, categoryMenuName, logType, logTypeName);
        resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
        resultMap.put(Constant.MESSAGE, "");
        resultMap.put(Constant.DATA,true);
        return resultMap;
    }

    @Override
    public Map<String, Object> exportLogFile(Map<String, Object> parameter) {
        Map<String, Object> resultMap = new HashMap<>();
        // 1.查出所有日志记录
        try {
            LoggingEvent loggingEvent = new LoggingEvent();
            String startTime = (String) parameter.get("startTime");
            String endTime = (String) parameter.get("endTime");
            String filePath = (String) parameter.get("filePath");

            logger.info("导出文件的路径为 {}",filePath);

            try{
                File dir = new File(filePath);
                if(!dir.exists()){
                    dir.mkdirs();
                }
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }

            if (StringUtils.isNotEmpty(startTime)){
                Date date = DateTools.parse(startTime, DateTools.YYYY_MM_DD_HH_MM_SS);
                String st = String.valueOf(date.getTime());
                loggingEvent.setStartTime(Long.valueOf(st));
            }
            if (StringUtils.isNotEmpty(endTime)){
                Date date = DateTools.parse(endTime, DateTools.YYYY_MM_DD_HH_MM_SS);
                String et = String.valueOf(date.getTime());
                loggingEvent.setEndTime(Long.valueOf(et));
            }
            int pageNum = 1;//初始值
            int pageSize = 10000; //
            PageHelper.startPage(pageNum, pageSize); //1W条数据做一次
            List<LoggingEvent> list = loggingEventMapper.queryList(loggingEvent);
            int pages = ((Page)list).getPages(); //总页数
            write(list,makeName(filePath,pageNum));

            // 2.导出成csv格式的文件并存储
            while(pageNum < pages){
                pageNum++;
                PageHelper.startPage(pageNum, pageSize); //1W条数据做一次
                List<LoggingEvent> list2 = loggingEventMapper.queryList(loggingEvent);
                write(list2,makeName(filePath,pageNum));
            }
            OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.LOG_MANAGER_SERVICE, "日志手工导出", JSON.toJSONString(parameter));
            resultMap.put(Constant.CODE,RespCodeEnum.RC_0.code());
            logger.info("导出文件成功");
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            resultMap.put(Constant.CODE,RespCodeEnum.RC_ERROR.code());
        }

        return resultMap;
    }

    private String makeName(String filePath,Integer pageNum){
        int index = filePath.lastIndexOf(".");
        if(index == -1){
            return filePath + "_" + pageNum ;
        }else{
            String postfix = filePath.substring(index); //得到文件名后缀
            return filePath.substring(0,index)+ "_" + pageNum + postfix;
        }
    }


    private void write(List<LoggingEvent> list,String filePath) throws Exception{
        List<List<String>> source = Lists.newArrayList();
        List<String> title = Lists.newArrayList();
        title.add("timestmp");
        title.add("formattedMessage");
        title.add("arg0");
        title.add("arg1");
        title.add("arg2");
        title.add("arg3");
        title.add("callerFilename");
        title.add("callerClass");
        title.add("callerMethod");
        source.add(title);
        for(LoggingEvent log : list){
            List<String> tmpL = Lists.newArrayList();
            tmpL.add(log.getTimestmp()+"");
            tmpL.add(log.getFormattedMessage());
            tmpL.add(log.getArg0());
            tmpL.add(log.getArg1());
            tmpL.add(log.getArg2());
            tmpL.add(log.getArg3());
            tmpL.add(log.getCallerFilename());
            tmpL.add(log.getCallerClass());
            tmpL.add(log.getCallerMethod());
            source.add(tmpL);
        }
        if(source.size() >0 ){
            CsvUtil.writeCsv(source,filePath);
        }
    }

    @Override
    public Map<String, Object> updateLogAutoBak(Map<String, Object> parameter) {
        Map<String, Object> resultMap = new HashMap<>();
        Integer interval = (Integer)parameter.get("interval");
        Boolean effective = (Boolean)parameter.get("effective");
        String savePath = (String)parameter.get("savePath");
        String lastWorkTime= (String) parameter.get("lastWorkTime");
        if(interval==null){
            resultMap.put(Constant.CODE,RespCodeEnum.RC_1.code());
            resultMap.put(Constant.MESSAGE,"备份周期不能为空");
            return resultMap;
        }
        if(effective==null){
            resultMap.put(Constant.CODE,RespCodeEnum.RC_1.code());
            resultMap.put(Constant.MESSAGE,"自动备份是否开启不能为空");
            return resultMap;
        }
        if(StringUtils.isBlank(savePath)){
            resultMap.put(Constant.CODE,RespCodeEnum.RC_1.code());
            resultMap.put(Constant.MESSAGE,"自动备份路径不能为空");
            return resultMap;
        }
        if(lastWorkTime == null){
        	resultMap.put(Constant.CODE,RespCodeEnum.RC_1.code());
            resultMap.put(Constant.MESSAGE,"自动备份开始时间不能为空");
            return resultMap;
        }
        // 查找是否存在配置
        try {

        	long workTime = DateTools.parse(lastWorkTime, DateTools.YYYY_MM_DD_HH_MM_SS).getTime();
            LogAutoBakExample query = new LogAutoBakExample();
            List<LogAutoBak> list = logAutoBakMapper.selectByExample(query);
            if(list.size()==0){
                //新增配置
                LogAutoBak logAutoBak = new LogAutoBak();
                logAutoBak.setId(UniqueUtil.uuid());
                logAutoBak.setIntervalDate(interval);
                logAutoBak.setEffective(effective);
                logAutoBak.setSavePath(savePath);
                logAutoBak.setLastWorkTime(workTime);
                logAutoBakMapper.insertSelective(logAutoBak);
            }else{
                //修改配置
                LogAutoBak update = new LogAutoBak();
                update.setId(list.get(0).getId());
                update.setIntervalDate(interval);
                update.setEffective(effective);
                update.setSavePath(savePath);
                update.setLastWorkTime(workTime);
                logAutoBakMapper.updateByPrimaryKeySelective(update);
            }
            resultMap.put(Constant.CODE,RespCodeEnum.RC_0.code());
            OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.LOG_MANAGER_SERVICE, "设置日志定制备份", JSON.toJSONString(parameter));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            resultMap.put(Constant.CODE,RespCodeEnum.RC_ERROR.code());
            resultMap.put(Constant.MESSAGE,"更改日志自动备份配置失败");
        }
        return resultMap;
    }


    private List<LoggingBean>  eventToBean(List<LoggingEvent> loggingEventList, List<LoggingBean> loggingBeanList) {
        for (LoggingEvent loggingEvent : loggingEventList) {
            LoggingBean loggingBean = new LoggingBean();

            // 解析数据
            String formattedMessage = loggingEvent.getFormattedMessage();
            if (StringUtils.isNotEmpty(formattedMessage)) {
                String[] arr1 = formattedMessage.split(SeparatorEnum.SEPARATOR_ARR1.code());
                // 如果长度为2  则有参数
                if (arr1.length == 2) {
                    try {
                        // 创建一个存储器
                        Map<String, String> map = new HashMap<>();
                        String[] arr2 = arr1[1].split(SeparatorEnum.SEPARATOR_ARR2.code());
                        for (String s : arr2) {
                            String[] str = s.split("=");
                            
                            if (str.length > 1) {
                                map.put(str[0], StringUtils.isEmpty(str[1]) ? "" : str[1]);
                            } else {
                                map.put(str[0], "");
                            }
                        }

                        // 操作日志解析
                        loggingBean.setUserName(map.get("userName"));
                        loggingBean.setRoleName(map.get("roleName"));
                        loggingBean.setCategoryMenuName(map.get("categoryMenuName"));
                        loggingBean.setOperation(map.get("operation"));
                        loggingBean.setDatas(map.get("datas"));
                        loggingBean.setContent(map.get("content"));
                        loggingBean.setLogTypeName(map.get("logTypeName"));
                        String strDate = DateTools.format(loggingEvent.getTimestmp(), DateTools.YYYY_MM_DD_HH_MM);
                        loggingBean.setStrDate(strDate);
                        loggingBeanList.add(loggingBean);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return loggingBeanList;
    }
    /**
     * 处理告警日志返回结果
     * @param loggingEventList
     * @param loggingBeanList
     * @return
     */
    private List<LoggingBean>  eventToBeanLog(List<LoggingEvent> loggingEventList, List<LoggingBean> loggingBeanList){
    	for(LoggingEvent loggingEvent : loggingEventList){
    		String formattedMessage = loggingEvent.getFormattedMessage();
    		if (StringUtils.isNotEmpty(formattedMessage)) {
    			LoggingBean loggingBean = new LoggingBean();

                // 告警日志处理
                
                loggingBean.setAlarmAddress(loggingEvent.getArg1());
                loggingBean.setContent(formattedMessage);
                loggingBean.setLogTypeName(LogTypeEnum.getDescription(loggingEvent.getArg0()));
                String strDate = DateTools.format(loggingEvent.getTimestmp(), DateTools.YYYY_MM_DD_HH_MM_SS);
                loggingBean.setStrDate(strDate);
                loggingBeanList.add(loggingBean);
                    
                
            }
    	}
    	return loggingBeanList;
    }

	@Override
	public Map<String, Object> backupLog(Map<String, Object> parameter) {
		logger.info("备份日志，backupLog:" + JSON.toJSONString(parameter));

        Map<String, Object> resultMap = new HashMap<>();
		String logTypeName = String.valueOf(parameter.get("logTypeName"));
		
		if(StringUtils.isEmpty(logTypeName) || null == LogTypeEnum.getCode(logTypeName))
		{
	        resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
	        resultMap.put(Constant.MESSAGE, "传入日志类型不存在");
	        return resultMap;
        }		
    	
    	try
    	{
    		//创建备份目录
    		if(!createBackupDir())
    		{
    			logger.error("创建备份目录失败，logbackupdir：{}",logbackupdir);
        		resultMap.put(Constant.CODE,RespCodeEnum.RC_ERROR.code());
                resultMap.put(Constant.MESSAGE,"备份日志目录创建失败");
                return resultMap;
    		}
    		
    	}
    	catch(Exception e)
    	{
    		logger.error(e.getMessage(),e);
    		resultMap.put(Constant.CODE,RespCodeEnum.RC_ERROR.code());
            resultMap.put(Constant.MESSAGE,"备份日志失败");
            return resultMap;
    	}	

    		//生成执行命令
    		new Thread(new Runnable() {
				
				@Override
				public void run() {
										
					String recordId = UniqueUtil.uuid();
					String backupTime = DateTools.getTimeStamp();
		    		String logType = LogTypeEnum.getCode(logTypeName);
		    		String filePath = generateFileName(logType,recordId,backupTime);
		    		String cmd = generateCmd(logType,filePath);
		    		LoggingRecord record = new LoggingRecord();
		    		
		    		try 
		    		{			    		
			    		//备份记录入库
			    		record.setRecordId(recordId);
			    		record.setFilePath(filePath);
						record.setBackupTime(backupTime);
						record.setLogType(logType);
						record.setStatus("2");					
						logBackupRecordMapper.insert(record);
						
		    			logger.info("开始备份日志" + new Date());
						Process process = Runtime.getRuntime().exec(new String[] {"sh", "-c", cmd});
						//Process process = Runtime.getRuntime().exec(cmd);
						int resultCode = process.waitFor();
						if(resultCode==0)
						{
							//更新备份状态
							record.setStatus("0");
							logBackupRecordMapper.update(record);
							logger.info("备份日志成功" + new Date());
						}
						else
						{
							record.setStatus("1");
							logBackupRecordMapper.update(record);
							logger.info("备份日志失败" + new Date()+"resultCode:{}",resultCode);
						}	
					} 
		    		catch(Exception e)
		    		{
		    			//更新备份状态
		    			record.setStatus("1");
						logBackupRecordMapper.update(record);
		    			logger.error(e.getMessage(),e);
		    			logger.error("备份日志失败,filePath：{} " , filePath);
		    		}
				}
			}).start();
    	OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.LOG_MANAGER_SERVICE, "日志手工备份", JSON.toJSONString(parameter));
    	resultMap.put(Constant.CODE,RespCodeEnum.RC_0.code());
        resultMap.put(Constant.MESSAGE,"下发备份日志指令成功");
		return resultMap;
	}

	private String generateFileName(String logType, String recordId, String backupTime) 
	{
		StringBuffer sb = new StringBuffer();
		sb.append(logbackupdir)
		.append(File.separator)
		.append(logType)
		.append(backupTime)
		.append(recordId.substring(0, 4))
		.append(".sql");
		return sb.toString();
	}
	
	private String generateCmd(String logtypeName,String fileName) 
	{
		//获取ip地址
		String ip = url.substring(url.indexOf("//")+2, url.lastIndexOf(":"));	
		
		StringBuffer sb = new StringBuffer();
		sb.append("mysqldump -t -u")
		.append(username)
		.append(" -p")
		.append(getPwd())
		.append(" -h")
		.append(ip)
		.append(" rms logging_event --where=\"arg0='")
		.append(logtypeName)
		.append("'\" > ")
		.append(fileName);
			
		logger.info(sb.toString());
		return sb.toString();
	}

	private String getPwd() {
		
		if(password.contains("\\!"))
		{
			return password;
		}
		
		if(password.contains("!"))
		{
			password = password.replace("!", "\\!");
		}
		return password;
	}

	private boolean createBackupDir() 
	{
		try
		{
			File dir = new File(logbackupdir);
			if(!dir.exists())
			{
				dir.mkdirs();
			}
			return true;
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(),e);
			return false;
		}
	}

	@Override
	public Map<String, Object> restore(Map<String, Object> parameter) 
	{
		Map<String, Object> resultMap = new HashMap<>();
		String filePath = String.valueOf(parameter.get("filePath"));
		
		//还原日志
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				String cmd = generateRestoreCmd(filePath);
				logger.info("cmd:{}",cmd);
				try 
	    		{
	    			logger.info("开始还原日志" + new Date());
					Process process = Runtime.getRuntime().exec(new String[] {"sh", "-c", cmd});
					int resultCode = process.waitFor();
					if(resultCode==0)
					{
						logger.info("还原日志成功" + new Date());
					}	
				} 
	    		catch(Exception e)
	    		{
	    			logger.error(e.getMessage(),e);
	    			logger.error("还原日志失败： " + filePath);
	    		}
				
			}
		}).start();
		OperationLogUtil.getInstance().recordOperationLog(parameter, CategoryEnum.LOG_MANAGER_SERVICE, "日志手工恢复", JSON.toJSONString(parameter));
		resultMap.put(Constant.CODE,RespCodeEnum.RC_0.code());
        resultMap.put(Constant.MESSAGE,"下发还原日志指令成功");
		return resultMap;
	}

	@Override
	public Map<String, Object> queryBakList(Map<String, Object> parameter) {
		Map<String, Object> resultMap = new HashMap<>();
		
		try
		{
            //获取分页参数
            Integer begin = (Integer) parameter.get(Constant.PAGE);
            Integer limit = (Integer) parameter.get(Constant.PAGESIZE);
            if(begin>0 && limit>0){
                PageHelper.startPage(begin, limit);
            }

			List<LoggingRecord> recordList = logBackupRecordMapper.queryList();
            resultMap.put("page", ((Page)recordList).getPageNum());
            resultMap.put("pageSize", ((Page)recordList).getPageSize());
			resultMap.put("total",((Page)recordList).getTotal());
	        resultMap.put(Constant.DATA,JSON.toJSON(recordList));
	        resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
	        resultMap.put(Constant.MESSAGE, "查询备份列表成功");

			return resultMap;
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(),e);
			 resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
		        resultMap.put(Constant.MESSAGE, "查询备份列表失败");
			return resultMap;
		}
	}
	
	private String generateRestoreCmd(String backuptime)
	{
		//获取ip地址
		String ip = url.substring(url.indexOf("//")+2, url.lastIndexOf(":"));	
		
		StringBuffer sb = new StringBuffer();
		sb.append("mysql -u")
		.append(username)
		.append(" -p")
		.append(getPwd())
		.append(" -h")
		.append(ip)
		.append(" rms <")
		.append(backuptime);
		
		return sb.toString();
	}
}
