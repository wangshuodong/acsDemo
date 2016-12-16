package com.cmiot.rms.services;

import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.dao.mapper.GatewayBackupFileInfoMapper;
import com.cmiot.rms.dao.mapper.GatewayBackupFileTaskAreaMapper;
import com.cmiot.rms.dao.mapper.GatewayBackupFileTaskDetailMapper;
import com.cmiot.rms.dao.mapper.GatewayBackupFileTaskMapper;
import com.cmiot.rms.dao.model.GatewayBackupFileInfo;
import com.cmiot.rms.dao.model.GatewayBackupFileTask;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.thread.ThreadPoolFactory;
import com.cmiot.rms.services.thread.ThreadTypeEnum;
import com.cmiot.rms.services.thread.backuptask.BackupTaskParameter;
import com.cmiot.rms.services.thread.backuptask.BackupTaskThread;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/6/9.
 */
public class BackupTask {

    @Autowired
    GatewayBackupFileTaskMapper gatewayBackupFileTaskMapper;

    @Autowired
    GatewayBackupFileTaskInnerService gatewayBackupFileTaskInnerService;

    @Autowired
    GatewayBackupFileTaskDetailMapper gatewayBackupFileTaskDetailMapper;

    @Autowired
    GatewayBackupFileInfoMapper gatewayBackupFileInfoMapper;

    @Autowired
    GatewayBackupFileTaskAreaMapper gatewayBackupFileTaskAreaMapper;

    @Autowired
    RedisClientTemplate redisClientTemplate;

    @Value("${upgrade.timeout}")
    int upgradeTimeout;

    @Value("${timing.task.search.number}")
    int number;

    @Value("${timing.task.lock.timeout}")
    int lockTimeout;

    @Value("${upgrade.gateway.number}")
    int gatewayNumber;

    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void process()
    {
        try {
            String str = redisClientTemplate.set("backupTask_lock", "yes", "NX","EX", lockTimeout);
            if (str == null) {// 存在锁
                logger.info("备份任务正在执行查询..");
                return;
            }
            else
            {
                logger.info("开始查询备份任务：{}", str);
            }

            long startTime = System.currentTimeMillis();

            PageHelper.startPage(1, number);
            List<GatewayBackupFileTask> tasks = gatewayBackupFileTaskMapper.selectTimingTask(DateTools.getCurrentSecondTime());

            logger.info("查询备份任务:{}, 耗时：{}", tasks, (System.currentTimeMillis() - startTime));

            if(null == tasks || tasks.size() < 1)
            {
                return;
            }

            List<String> ids = new ArrayList<>();

            for(GatewayBackupFileTask task : tasks)
            {
                ids.add(task.getId());
                BackupTaskParameter parameter = new BackupTaskParameter();
                parameter.setGatewayBackupFileTaskDetailMapper(gatewayBackupFileTaskDetailMapper);
                parameter.setGatewayBackupFileTaskInnerService(gatewayBackupFileTaskInnerService);
                parameter.setGatewayBackupFileTaskMapper(gatewayBackupFileTaskMapper);
                parameter.setGatewayBackupFileTaskAreaMapper(gatewayBackupFileTaskAreaMapper);
                parameter.setTask(task);
                parameter.setGatewayNumber(gatewayNumber);

                ThreadPoolFactory.getInstance().getTheadPool(ThreadTypeEnum.EXECUTE_BACKUP_TASK).execute(new BackupTaskThread(parameter));
            }

            if(ids.size() > 0)
            {
                logger.info("扫描到达到备份时间要求的任务包:{}", ids);

                Map<String, Object> para = new HashMap<>();
                para.put("status", 2);
                para.put("ids", ids);
                gatewayBackupFileTaskMapper.batchUpdateTaskStatus(para);
            }
        }
        finally {
            redisClientTemplate.del("backupTask_lock");
        }

    }

    /**
     * 更新备份任务状态
     */
    public void updateTaskStatus() {

        //更新备份文件状态
        PageHelper.startPage(1, number);
        List<GatewayBackupFileInfo> infoList = gatewayBackupFileInfoMapper.selectProcessingList();

        long currentTime = System.currentTimeMillis();
        if(null != infoList && infoList.size() > 0)
        {
            for(GatewayBackupFileInfo info : infoList)
            {
                if(currentTime - Long.valueOf(info.getStartTime()) > upgradeTimeout * 1000)
                {
                    //超时更新为失败
                    info.setStatus(3);
                    info.setEndTime(String.valueOf(currentTime));
                    gatewayBackupFileInfoMapper.updateByPrimaryKeySelective(info);
                }
            }
        }

        //更新定时任务的状态为完成
        PageHelper.startPage(1, number);
        List<GatewayBackupFileTask> taskList = gatewayBackupFileTaskMapper.selectProcessingTask(DateTools.getCurrentSecondTime());
        if(null != taskList && taskList.size() > 0)
        {
            for(GatewayBackupFileTask task : taskList)
            {
                task.setStatus(3);
                gatewayBackupFileTaskMapper.updateByPrimaryKeySelective(task);
            }
        }

    }

}
