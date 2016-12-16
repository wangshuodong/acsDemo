package com.cmiot.rms.services.thread.upgradetask;

import com.cmiot.rms.common.enums.UpgradeTaskDetailStatusEnum;
import com.cmiot.rms.services.thread.ThreadPoolFactory;
import com.cmiot.rms.services.thread.ThreadTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by panmingguo on 2016/7/7.
 */
public class UpgradeTaskThread implements Runnable {

    public final Logger logger = LoggerFactory.getLogger(UpgradeTaskThread.class);

    private UpgradeTaskParameter parameter;

    public UpgradeTaskThread(UpgradeTaskParameter parameter)
    {
        this.parameter = parameter;
    }

    @Override
    public void run() {
        Map<String, Object> map = new HashMap<>();
        map.put("upgradeTaskId", parameter.getFirmwareUpgradeTask().getId());
        map.put("status", UpgradeTaskDetailStatusEnum.WAIT.code());

        int count =  parameter.getDetailMapper().queryCount(map);
        int times = count % parameter.getGatewayNumber() == 0 ? (count / parameter.getGatewayNumber()) : (count / parameter.getGatewayNumber() + 1);
        for(int i =0; i < times; i++)
        {
            ThreadPoolFactory.getInstance().getTheadPool(ThreadTypeEnum.EXECUTE_UPGREAD_DETAIL_TASK).execute(new UpgradeTaskDetailThread(parameter, i + 1));
        }
    }
}
