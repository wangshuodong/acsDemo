package com.cmiot.rms.services;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.DatabaseMapper;
import com.cmiot.rms.dao.model.DatabaseRecord;
import com.cmiot.rms.services.impl.DataBaseServiceImpl;
import com.cmiot.rms.services.template.RedisClientTemplate;

public class DatabaseBackupTask {
	private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupTask.class);

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

	@Value(value = "${db.backup.dir}")
	private String dbbackupPath;

	@Value(value = "${db.backup.name}")
	private String dbname;

	public void backupDatabase() {

		logger.info("备份数据库");

		if (!createBackupDir()) {
			logger.error("创建目录失败，dbbackupPath：{}", dbbackupPath);
			return;
		}

		try {
			String value = redisClientTemplate.get(key);
			if (StringUtils.isEmpty(value)) {
				logger.info("未设置备份策略，key={}", value);
				return;
			}

			// 到设定的分钟，才开始执行备份。
			String minute = value.substring(8, 12);
			if (!getMinute().equals(minute)) {
				return;
			}

			new Thread(new Runnable() {

				@Override
				public void run() {
					logger.info("开始备份数据库");
					DatabaseRecord databaseRecord = new DatabaseRecord();
					String recordId = UniqueUtil.uuid();
					String backupTime = DateTools.getTimeStamp();
					// 防止生成相同文件名
					StringBuilder sb1 = new StringBuilder();
					sb1.append(dbbackupPath).append(File.separator).append(backupTime).append(recordId.substring(0, 3))
							.append(".sql");
					String filePath = sb1.toString();
					databaseRecord.setStatus("2");
					databaseRecord.setFilePath(filePath);
					databaseRecord.setRecordId(recordId);
					databaseRecord.setBackupTime(backupTime);

					String cmd = generatCmd(filePath);
					try {
						logger.info("redisClientTemplate:{}", redisClientTemplate.get("backupDatabase_lock"));
						String str = redisClientTemplate.set("backupDatabase_lock", "yes", "NX", "EX", 30000);
						if (str == null) {// 存在锁
							logger.info("正在备份数据库");
							return;
						}

						databaseRecordMapper.insert(databaseRecord);
						Process process = Runtime.getRuntime().exec(new String[] { "sh", "-c", cmd });
						int result = process.waitFor();
						if (result == 0) {
							logger.info("备份数据库成功,recordid:{},backupTime:{}", recordId, backupTime);
							databaseRecord.setStatus("0");
							databaseRecordMapper.update(databaseRecord);
						} else {
							logger.error("备份数据库失败,recordid:{},backupTime:{}，result:{}", recordId, backupTime,result);
							databaseRecord.setStatus("1");
							databaseRecordMapper.update(databaseRecord);
						}

					} catch (Exception e) {
						logger.error("备份数据库失败,recordid:{},backupTime:{}", recordId, backupTime);
						logger.error(e.getMessage(), e);
						databaseRecord.setStatus("1");
						databaseRecordMapper.update(databaseRecord);
					} finally {
						redisClientTemplate.del("backupDatabase_lock");
					}
				}
			}).start();
		} catch (Exception e) {
			logger.error("备份数据库失败");
			logger.error(e.getMessage(), e);
		}

	}

	private String getMinute() {
		SimpleDateFormat df = new SimpleDateFormat("HHmm");
		return (df.format(new Date()));
	}

	private String generatCmd(String filePath) {
		// 获取ip地址
		String ip = url.substring(url.indexOf("//") + 2, url.lastIndexOf(":"));
		StringBuffer sb = new StringBuffer();
		sb.append("mysqldump -u").append(username).append(" -p").append(getPwd()).append(" -h").append(ip)
				.append(" ").append(dbname).append("> ").append(filePath);
		logger.info("cmd:{}",sb.toString());
		return sb.toString();
	}

	private boolean createBackupDir() {
		try {
			File dir = new File(dbbackupPath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	private String getPwd() {

		if (password.contains("\\!")) {
			return password;
		}

		if (password.contains("!")) {
			password = password.replace("!", "\\!");
		}
		return password;
	}
}
