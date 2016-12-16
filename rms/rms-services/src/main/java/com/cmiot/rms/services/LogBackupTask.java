package com.cmiot.rms.services;

import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.dao.mapper.LogAutoBakMapper;
import com.cmiot.rms.dao.model.LogAutoBak;
import com.cmiot.rms.dao.model.LogAutoBakExample;
import com.cmiot.rms.services.template.RedisClientTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author xukai
 * Date 2016/6/17
 */
public class LogBackupTask {


    private Logger logger = LoggerFactory.getLogger(LogBackupTask.class);


    @Autowired
    private LogAutoBakMapper logAutoBakMapper;

    @Autowired
    private LogManagerService logManagerService;

    @Autowired
    private RedisClientTemplate redisClientTemplate;

    public void work(){

        logger.info("日志自动备份任务启动");
        //1.检查自动备份是否开启
        LogAutoBakExample query = new LogAutoBakExample();
        List<LogAutoBak> list = logAutoBakMapper.selectByExample(query);
        if(list.size()!=1){
            logger.info("当前没有日志定时备份任务");
            return;
        }
        LogAutoBak logAutoBak = list.get(0);
        if(!logAutoBak.getEffective()){
            logger.info("当前日志定时备份任务配置为不生效");
            return;
        }

        //2.检查是否到达备份日期
        Map<String,Object> param = new HashMap<>();

        Integer intervalDay = logAutoBak.getIntervalDate(); //间隔天数
        Long nextWorkTime = logAutoBak.getLastWorkTime(); //下次备份的日期
        Date now = new Date();
        String endTime = DateTools.format(now,DateTools.YYYY_MM_DD_HH_MM_SS);//结束时间
        Long intervalMs =  intervalDay * 24 * 60 * 60 * 1000L; //设置的间隔毫秒
        Long start = now.getTime() - intervalMs; //开始毫秒
        String startTime = DateTools.format(new Date(start),DateTools.YYYY_MM_DD_HH_MM_SS);//开始时间
        boolean execute = false;//执行标示默认不执行
        if(nextWorkTime==null){
            //第一次备份
            execute = true;
        }else{
            //比较当前时间与下次备份时间
            if(now.getTime() - nextWorkTime >= 0){
                execute = true;
            }
        }

        //3.开始备份
        if(!execute){
            logger.info("尚未到达备份时间");
            return;
        }
        //加锁处理，防止数据过多导出耗时太长，没有及时更新下次备份时间，导致重复执行导出,默认10分钟
        String str = redisClientTemplate.set("log_auto_backup_lock_key", "1", "NX", "EX", 600);
        if(str == null){
            logger.info("日志正在备份");
        }else{
            logger.info("日志开始备份");
            //使用更新日期作为文件名
            String filePath = "";
            if(logAutoBak.getSavePath().endsWith("/")){
                filePath = logAutoBak.getSavePath() + DateTools.format(now,DateTools.YYYY_MM_DD_HHMM) + ".csv";
            }else{
                filePath = logAutoBak.getSavePath() + "/" + DateTools.format(now,DateTools.YYYY_MM_DD_HHMM) + ".csv";
            }
            param.put("startTime",startTime);
            param.put("endTime",endTime);
            param.put("filePath",filePath);
            logManagerService.exportLogFile(param);
            //更改最后备份日期

            LogAutoBak update = new LogAutoBak();
            update.setId(logAutoBak.getId());
            update.setLastWorkTime(now.getTime() + intervalMs);//根据间隔时间设置下次备份时间
            logAutoBakMapper.updateByPrimaryKeySelective(update);
            redisClientTemplate.del("log_auto_backup_lock_key");
            logger.info("日志备份结束");
        }
    }
}
