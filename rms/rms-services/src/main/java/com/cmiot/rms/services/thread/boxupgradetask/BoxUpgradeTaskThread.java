package com.cmiot.rms.services.thread.boxupgradetask;

import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.dao.model.BoxFirmwareInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by panmingguo on 2016/9/5.
 */
public class BoxUpgradeTaskThread implements Runnable {

    private BoxBackupTaskParameter parameter;

    private final Logger logger = LoggerFactory.getLogger(BoxUpgradeTaskThread.class);

    public BoxUpgradeTaskThread(BoxBackupTaskParameter parameter)
    {
        this.parameter = parameter;
    }

    @Override
    public void run() {
        if(null == parameter.getBoxIds() || parameter.getBoxIds().size() < 1 ||  null == parameter.getBoxFirmwareInfo())
        {
            return;
        }

        execute(parameter.getBoxFirmwareInfo());
    }

    private void execute(BoxFirmwareInfo bfi) {
        Map<String, Object> upgradeJob = new HashMap<>();
        upgradeJob.put("boxIds", parameter.getBoxIds());
        upgradeJob.put("commandKey", "");
        upgradeJob.put("methodName", "Download");
        upgradeJob.put("fileType", "1 Firmware Upgrade Image");
        upgradeJob.put("url", bfi.getFirmwarePath());
        upgradeJob.put("userName", parameter.getUserName());
        upgradeJob.put("passWord", parameter.getPassword());
        String targetFileName = bfi.getFirmwarePath();
        if (StringUtils.isNotBlank(targetFileName) && targetFileName.lastIndexOf("/") > 0) {
            targetFileName = targetFileName.substring(targetFileName.lastIndexOf("/") + 1);
            upgradeJob.put("targetFileName", targetFileName);
        } else {
            upgradeJob.put("targetFileName", "update.zip");
        }
        upgradeJob.put("successURL", "");
        upgradeJob.put("failureURL", "");
        upgradeJob.put("fileSize", bfi.getFirmwareSize());
        upgradeJob.put("delaySeconds", "0");
        upgradeJob.put("taskId", parameter.getTaskId());

        try {
            logger.info("Start invoke BoxUpgradeTaskThread executeBatch:{}", upgradeJob);
            Map<String, Object> respMap = parameter.getBoxInvokeInsService().executeBatch(upgradeJob);
            logger.info("End invoke BoxUpgradeTaskThread executeBatch:{}", respMap);
            if (respMap.size() > 0 && StringUtils.isNotBlank(String.valueOf(respMap.get("resultCode"))) && 0 == (Integer) respMap.get("resultCode")) {
                Map<String, Object> para = new HashMap<>();
                para.put("upgradeStartTime", DateTools.getCurrentSecondTime());
                para.put("ids", parameter.getDetailIds());
                parameter.getDetailMapper().batchUpdateProcessingStatusAndTime(para);
            }
            else {
                logger.error("Download instruct send failed!");
            }
        }catch (Exception e) {
            logger.error("Download instruct send exception:{}", e);
        }
    }
}
