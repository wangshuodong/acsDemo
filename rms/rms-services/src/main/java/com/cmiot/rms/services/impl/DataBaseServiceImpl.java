/**
 * 
 */
package com.cmiot.rms.services.impl;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.dao.mapper.DatabaseMapper;
import com.cmiot.rms.dao.model.DatabaseRecord;
import com.cmiot.rms.services.DataBaseService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.util.OperationLogUtil;

/**
 * @author heping
 *
 */
public class DataBaseServiceImpl implements DataBaseService {
	private static final Logger logger = LoggerFactory.getLogger(DataBaseServiceImpl.class);

	/**
	 * redis存放的数据库备份策略id
	 */
	private static final String key = "backupStrategy";

	@Autowired
	DatabaseMapper databaseRecordMapper;

	@Autowired
	private RedisClientTemplate redisClientTemplate;

	@Value(value = "${ds.url}")
	private String url;

	@Value(value = "${ds.username}")
	private String username;

	@Value(value = "${ds.password}")
	private String password;
	
	@Value(value = "${db.backup.name}")
    private String dbname;
	
	
	@Override
	public Map<String, Object> restoreDatabase(Map<String, Object> params) {
		logger.info("开始还原数据库");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		String recordId = (String) params.get("recordId");
		String filePath = (String) params.get("filePath");
		File file = new File(filePath);
		if (!file.exists()) {
			resultMap.put(Constant.CODE, RespCodeEnum.RC_1004.code());
			resultMap.put(Constant.MESSAGE, "不存在该文件：" + filePath);
			return resultMap;
		}

		new Thread(new Runnable() {
			public void run() {
				logger.info("开始还原数据库");
				String cmd = generalRestoreCmd(filePath);
				logger.info("cmd:{}",cmd);
				try {
					Process process = Runtime.getRuntime().exec(new String[] {"sh", "-c", cmd});
					int resultCode = process.waitFor();
					if (resultCode == 0) {
						logger.info("还原数据库成功,fileName:{}", filePath);
					} else {
						logger.error("还原数据失败，fileName:{},resultCode:{}", filePath,resultCode);
					}
				} catch (Exception e) {
					logger.error("还原数据失败，fileName:{}", filePath);
				}
			}
		}).start();
		OperationLogUtil.getInstance().recordOperationLog(params, CategoryEnum.SYSTEMCONFIG_MANAGER_SERVICE, "数据库备份恢复", JSON.toJSONString(params));
		resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
		resultMap.put(Constant.MESSAGE, "下发备份数据库指令成功");
		return resultMap;
	}

	protected String generalRestoreCmd(String filePath) {
		// 获取ip地址
		String ip = url.substring(url.indexOf("//") + 2, url.lastIndexOf(":"));

		StringBuffer sb = new StringBuffer();
		sb.append("mysql -u").append(username).append(" -p").append(getPwd()).append(" -h").append(ip).append(" ").append(dbname)
				.append(" < ").append(filePath);
		return sb.toString();
	}

	@Override
	public Map<String, Object> queryList(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<>();

		try {
			DatabaseRecord databaseRecord = new DatabaseRecord();
			List<DatabaseRecord> recordList = databaseRecordMapper.query(databaseRecord);
			resultMap.put("total", recordList.size());
			resultMap.put(Constant.DATA, JSON.toJSON(recordList));
			resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
			resultMap.put(Constant.MESSAGE, "查询备份列表成功");
			return resultMap;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			resultMap.put(Constant.CODE, RespCodeEnum.RC_ERROR.code());
			resultMap.put(Constant.MESSAGE, "查询备份列表失败");
			return resultMap;
		}
	}

	@Override
	public Map<String, Object> addBackupTask(Map<String, Object> params) {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		String backupStrategy = (String) params.get(key);
		redisClientTemplate.set(key, backupStrategy);
		resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
		resultMap.put(Constant.MESSAGE, "备份策略设置成功");
		return resultMap;
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

}
