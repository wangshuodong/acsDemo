package com.cmiot.rms.services.thread.upgradetask;

import com.cmiot.rms.common.enums.UpgradeTaskDetailStatusEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.dao.model.FirmwareInfo;
import com.cmiot.rms.dao.model.FirmwareUpgradeTaskDetail;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/7/14.
 */
public class UpgradeTaskDetailThread implements Runnable {


    public final Logger logger = LoggerFactory.getLogger(UpgradeTaskDetailThread.class);

    private UpgradeTaskParameter parameter;

    private int startPage;

    public  UpgradeTaskDetailThread(UpgradeTaskParameter parameter,  int startPage)
    {
        this.parameter = parameter;
        this.startPage = startPage;
    }

    @Override
    public void run() {
        Map<String, Object> map = new HashMap<>();
        map.put("upgradeTaskId", parameter.getFirmwareUpgradeTask().getId());
        map.put("status", UpgradeTaskDetailStatusEnum.WAIT.code());
        map.put("startRows", (startPage - 1) * parameter.getGatewayNumber());
        map.put("pageSize", parameter.getGatewayNumber());

        long startTime = System.currentTimeMillis();
        List<FirmwareUpgradeTaskDetail> firmwareUpgradeTaskDetailList = parameter.getDetailMapper().queryListByTaskId(map);

        logger.info("升级任务({})扫描到网关数:{}, 耗时:{} ", parameter.getFirmwareUpgradeTask().getTaskName(), firmwareUpgradeTaskDetailList.size(), (System.currentTimeMillis() - startTime));

        FirmwareInfo firmwareInfo = parameter.getFirmwareInfoMapper().selectByPrimaryKey(parameter.getFirmwareUpgradeTask().getFirmwareId());
        if (null == firmwareInfo || null == firmwareUpgradeTaskDetailList || firmwareUpgradeTaskDetailList.size() <= 0) {
            return;
        }

        List<String> gatewayIds = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (int j = 0; j < firmwareUpgradeTaskDetailList.size(); j++) {
            gatewayIds.add(firmwareUpgradeTaskDetailList.get(j).getGatewayId());
            ids.add(firmwareUpgradeTaskDetailList.get(j).getId());
        }

        logger.info("升级任务({})扫描到网关:{}", parameter.getFirmwareUpgradeTask().getTaskName(), gatewayIds);

        Map<String, Object> upgradeJob = new HashMap<>();

        upgradeJob.put("gatewayIds", gatewayIds);
        upgradeJob.put("commandKey", "");
        upgradeJob.put("methodName", "Download");
        upgradeJob.put("fileType", "1 Firmware Upgrade Image");

        upgradeJob.put("url", firmwareInfo.getFirmwarePath());
        upgradeJob.put("userName", parameter.getUserName());
        upgradeJob.put("passWord", parameter.getPassword());

        // 下载任务不需要指定文件名，这个参数的值可以为空
        upgradeJob.put("targetFileName", firmwareInfo.getFirmwareName());
        upgradeJob.put("successURL", "");
        upgradeJob.put("failureURL", "");
        upgradeJob.put("fileSize", firmwareInfo.getFirmwareSize());
        upgradeJob.put("delaySeconds", "0");

        //用于终端返回后的更新操作
        upgradeJob.put("taskId", parameter.getFirmwareUpgradeTask().getId());

        try {
            logger.info("notifyGate start invoke executeBatch:{}", upgradeJob);
            Map<String, Object> respMap = parameter.getInvokeInsService().executeBatch(upgradeJob);
            logger.info("notifyGate end invoke executeBatch:{}", respMap);

            //不为空且code为0代表下载指令发送成功
            if (null != respMap && (0 == (Integer) respMap.get("resultCode"))) {
                Map<String, Object> para = new HashMap<>();
                para.put("upgradeStartTime", DateTools.getCurrentSecondTime());
                para.put("ids", ids);
                parameter.getDetailMapper().batchUpdateProcessingStatusAndTime(para);

            } else {
                logger.error("Download instruct send failed!");
            }
        } catch (Exception e) {
            logger.error("Download instruct send exception:{}", e);
        }
    }
}
