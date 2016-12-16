package com.cmiot.rms.services.thread.backuptask;

import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.model.GatewayBackupFileTaskDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/7/14.
 */
public class BackupTaskDetailThread implements  Runnable{

    public final Logger logger = LoggerFactory.getLogger(BackupTaskDetailThread.class);

    private BackupTaskParameter backupTaskParameter;

    private int startPage;

    public BackupTaskDetailThread(BackupTaskParameter backupTaskParameter, int startPage)
    {
        this.backupTaskParameter = backupTaskParameter;
        this.startPage = startPage;
    }

    @Override
    public void run() {

        long startTime = System.currentTimeMillis();

        Map<String, Object> para = new HashMap<>();
        para.put("areaIds", backupTaskParameter.getAreas());
        para.put("startRows", (startPage - 1) * backupTaskParameter.getGatewayNumber());
        para.put("pageSize", backupTaskParameter.getGatewayNumber());
        List<Map<String,Object>> gatewayIds = backupTaskParameter.getGatewayBackupFileTaskMapper().selectGatewayIdsByArea(para);

        logger.info("查询网关:{}, 耗时:{}", gatewayIds,  (System.currentTimeMillis() - startTime));
        if(null == gatewayIds || gatewayIds.size() < 1)
        {
            return;
        }

        for(Map<String, Object> gatewayMap : gatewayIds)
        {
            String gatewayId = String.valueOf(gatewayMap.get("gatewayId"));
            startTime = System.currentTimeMillis();
            GatewayBackupFileTaskDetail seachDetail = new GatewayBackupFileTaskDetail();
            seachDetail.setGatewayId(gatewayId);
            seachDetail.setTaskId(backupTaskParameter.getTask().getId());
            int count = backupTaskParameter.getGatewayBackupFileTaskDetailMapper().selectCountById(seachDetail);

            logger.info("查询网关({},{})是否已经备份:{}, 耗时:{}", gatewayId, backupTaskParameter.getTask().getId(), count, (System.currentTimeMillis() - startTime));

            if(count > 0)
            {
                return;
            }

            startTime = System.currentTimeMillis();
            Map<String, String> retMap = backupTaskParameter.getGatewayBackupFileTaskInnerService().backup(gatewayId);

            logger.info("网关({})备份耗时:{}", gatewayId, (System.currentTimeMillis() - startTime));

            if(retMap.get("result").equals("0"))
            {
                GatewayBackupFileTaskDetail detail = buildDetail(backupTaskParameter.getTask().getId(), gatewayId, retMap.get("fileId"));
                backupTaskParameter.getGatewayBackupFileTaskDetailMapper().insert(detail);
            }
        }
    }

    /**
     * 构造任务详情对象
     * @return
     */
    private GatewayBackupFileTaskDetail buildDetail(String taskId, String gatewayId, String fileId)
    {
        GatewayBackupFileTaskDetail detail = new GatewayBackupFileTaskDetail();
        detail.setId(UniqueUtil.uuid());
        detail.setGatewayId(gatewayId);
        detail.setTaskId(taskId);
        detail.setBackupFileId(fileId);
        detail.setStatus(1);
        return detail;
    }
}
