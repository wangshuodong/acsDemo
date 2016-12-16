package com.cmiot.rms.services;

import com.cmiot.rms.common.enums.UpgradeTaskDetailStatusEnum;
import com.cmiot.rms.common.enums.UpgradeTaskStatusEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.dao.mapper.BoxFirmwareUpgradeTaskDetailMapper;
import com.cmiot.rms.dao.mapper.BoxFirmwareUpgradeTaskMapper;
import com.cmiot.rms.dao.model.BoxFirmwareUpgradeTask;
import com.cmiot.rms.dao.model.BoxFirmwareUpgradeTaskDetail;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/9/5.
 */
public class BoxUpgradeTask {

    @Autowired
    BoxFirmwareUpgradeTaskMapper taskMapper;

    @Autowired
    BoxFirmwareUpgradeTaskDetailMapper detailMapper;

    @Value("${timing.task.search.number}")
    int number;

    @Value("${upgrade.timeout}")
    int upgradeTimeout;


    /**
     * 更新升级任务状态
     */
    public void updateTaskStatus() {

        PageHelper.startPage(1, number);
        List<BoxFirmwareUpgradeTask> processingTaskList = taskMapper.queryListByStatus(UpgradeTaskStatusEnum.PROCESSING.code());

        //检查任务包中的具体网关升级是否全部已经处理
        for (BoxFirmwareUpgradeTask task : processingTaskList) {

            //1.先检查是否存在超时的正在处理中的任务,如果存在超时任务，将状态更新为失败
            Map<String, Object> para = new HashMap<>();
            para.put("upgradeTaskId", task.getId());
            para.put("status", UpgradeTaskDetailStatusEnum.PROCESSING.code());

            PageHelper.startPage(1, number);
            List<BoxFirmwareUpgradeTaskDetail> processingDetailList = detailMapper.queryListByTaskId(para);

            int currentTime;
            for(BoxFirmwareUpgradeTaskDetail detail : processingDetailList)
            {
                currentTime = DateTools.getCurrentSecondTime();
                if(currentTime - detail.getUpgradeStartTime() > upgradeTimeout)
                {
                    detail.setUpgradeEndTime(currentTime);
                    detail.setStatus(UpgradeTaskDetailStatusEnum.FAILURE.code());
                    detailMapper.updateByPrimaryKeySelective(detail);
                }
            }

            //2.查询升级任务中所有的网关是否已经处理完成，处理完成，将升级任务的状态更新为完成
            BoxFirmwareUpgradeTaskDetail firmwareUpgradeTaskDetailToSearch = new BoxFirmwareUpgradeTaskDetail();
            firmwareUpgradeTaskDetailToSearch.setUpgradeTaskId(task.getId());
            int count = detailMapper.queryCountNoComplete(firmwareUpgradeTaskDetailToSearch);

            //确认网关升级已经全部处理
            if (count < 1) {
                task.setTaskStatus(UpgradeTaskStatusEnum.PROCESSED.code());
                taskMapper.updateByPrimaryKeySelective(task);
            }

        }
    }
}
