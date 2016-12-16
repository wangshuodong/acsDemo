package com.cmiot.rms.services.thread.backuptask;

import com.cmiot.rms.dao.model.GatewayBackupFileTask;
import com.cmiot.rms.services.thread.ThreadPoolFactory;
import com.cmiot.rms.services.thread.ThreadTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/7/7.
 */
public class BackupTaskThread implements Runnable {

    public final Logger logger = LoggerFactory.getLogger(BackupTaskThread.class);

    private BackupTaskParameter backupTaskParameter;

    public BackupTaskThread(BackupTaskParameter backupTaskParameter)
    {
        this.backupTaskParameter = backupTaskParameter;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        GatewayBackupFileTask task = backupTaskParameter.getTask();
        List<Map<String, Object>> areasMap = backupTaskParameter.getGatewayBackupFileTaskAreaMapper().selectAreasById(task.getId());
        logger.info("查询备份任务:{}, 区域:{}, 耗时:{}", task.getId(), areasMap, (System.currentTimeMillis() - startTime));
        if(null == areasMap || areasMap.size() < 1)
        {
            return;
        }
        List<Integer> areas = new ArrayList<>();
        for(Map<String, Object> areaMap : areasMap)
        {
            areas.add(Integer.valueOf(areaMap.get("areaId").toString()));
        }

        backupTaskParameter.setAreas(areas);

        int count = backupTaskParameter.getGatewayBackupFileTaskMapper().selectGatewayCountByArea(areas);

        logger.info("备份任务查询网关总数:{}", count);

        int times = count % backupTaskParameter.getGatewayNumber() == 0 ? (count / backupTaskParameter.getGatewayNumber()) : (count / backupTaskParameter.getGatewayNumber() + 1);

        logger.info("备份任务执行总次数:{}", times);

        for(int i =0; i < times; i++)
        {
            ThreadPoolFactory.getInstance().getTheadPool(ThreadTypeEnum.EXECUTE_BACKUP_DETAIL_TASK).execute(new BackupTaskDetailThread(backupTaskParameter, i + 1));
        }

    }
}
