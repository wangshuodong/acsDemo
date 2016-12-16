package com.cmiot.rms.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.services.util.InstructionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.cmiot.rms.common.enums.UpgradeTaskDetailStatusEnum;
import com.cmiot.rms.common.enums.UpgradeTaskStatusEnum;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.dao.mapper.FirmwareInfoMapper;
import com.cmiot.rms.dao.mapper.FirmwareUpgradeTaskDetailMapper;
import com.cmiot.rms.dao.mapper.FirmwareUpgradeTaskMapper;
import com.cmiot.rms.dao.model.FirmwareUpgradeTask;
import com.cmiot.rms.dao.model.FirmwareUpgradeTaskDetail;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.instruction.InvokeInsService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.thread.ThreadPoolFactory;
import com.cmiot.rms.services.thread.ThreadTypeEnum;
import com.cmiot.rms.services.thread.upgradetask.UpgradeTaskParameter;
import com.cmiot.rms.services.thread.upgradetask.UpgradeTaskThread;
import com.github.pagehelper.PageHelper;

/**
 * Created by fuwanhong on 2016/2/20.
 */
public class UpgradeTask {
    protected List<FirmwareUpgradeTask> newTaskList;

    @Autowired
    FirmwareUpgradeTaskMapper firmwareUpgradeTaskMapper;

    @Autowired
    FirmwareUpgradeTaskDetailMapper firmwareUpgradeTaskDetailMapper;
    @Autowired
    GatewayInfoService gatewayInfoService;

    
    @Autowired
    InvokeInsService invokeInsService;
    

    @Autowired
    FirmwareInfoMapper firmwareInfoMapper;

    @Autowired
    RedisClientTemplate redisClientTemplate;

    @Autowired
    SyncInfoToFirstLevelPlatformService syncInfoToFirstLevelPlatformService;

    @Value("${file.server.userName}")
    String userName;

    @Value("${file.server.password}")
    String password;

    @Value("${upgrade.timeout}")
    int upgradeTimeout;

    @Value("${timing.task.search.number}")
    int number;

    @Value("${timing.task.lock.timeout}")
    int lockTimeout;

    @Value("${upgrade.gateway.number}")
    int gatewayNumber;


    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    public UpgradeTask() {
        super();
        this.newTaskList = new ArrayList<>();
    }

    public void process() {
        scanTask();
        notifyGate();
    }

    /**
     *发送指令给网关
     */
    public void notifyGate() {
        for (FirmwareUpgradeTask firmwareUpgradeTask : newTaskList) {
            UpgradeTaskParameter parameter = new UpgradeTaskParameter();
            parameter.setDetailMapper(firmwareUpgradeTaskDetailMapper);
            parameter.setFirmwareInfoMapper(firmwareInfoMapper);
            parameter.setFirmwareUpgradeTask(firmwareUpgradeTask);
            parameter.setUserName(userName);
            parameter.setPassword(password);
            parameter.setInvokeInsService(invokeInsService);
            parameter.setGatewayNumber(gatewayNumber);
            ThreadPoolFactory.getInstance().getTheadPool(ThreadTypeEnum.EXECUTE_UPGREAD_TASK).execute(new UpgradeTaskThread(parameter));
        }
    }

    /**
     * 扫描新加任务或者没有发送指令的升级任务
     */
    public void scanTask() {

        try {
            String str = redisClientTemplate.set("upgradeTask_lock", "yes", "NX","EX", lockTimeout);
            if (str == null) {// 存在锁
                logger.info("升级任务正在执行查询..");
                return;
            }
            else
            {
                logger.info("开始查询升级任务：{}", str);
            }

            long startTime = System.currentTimeMillis();

            long timeMillis = System.currentTimeMillis();
            long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
            int currentTime = (int) timeSeconds;

            FirmwareUpgradeTask firmwareUpgradeTaskToSearch = new FirmwareUpgradeTask();
            firmwareUpgradeTaskToSearch.setTaskStatus(UpgradeTaskStatusEnum.NEW.code());
            //只执行定时触发的任务
            firmwareUpgradeTaskToSearch.setTaskTriggerMode(1);
            firmwareUpgradeTaskToSearch.setCurrentTime(currentTime);

            PageHelper.startPage(1, number);
            newTaskList = firmwareUpgradeTaskMapper.queryList(firmwareUpgradeTaskToSearch);
            logger.info("扫描到达到升级时间要求的新任务包:{}, 耗时:{}", newTaskList.size(), (System.currentTimeMillis() -startTime));

            List<String> ids = new ArrayList<>();
            for(FirmwareUpgradeTask task : newTaskList)
            {
                ids.add(task.getId());
            }

            if(ids.size() > 0)
            {
                logger.info("扫描到达到升级时间要求的新任务包:{}", ids);

                Map<String, Object> para = new HashMap<>();
                para.put("taskStatus", UpgradeTaskStatusEnum.PROCESSING.code());
                para.put("ids", ids);
                firmwareUpgradeTaskMapper.batchUpdateStatus(para);
            }
        }
       finally {
            redisClientTemplate.del("upgradeTask_lock");
        }

    }

    /**
     * 更新升级任务状态
     */
    public void updateTaskStatus() {

        PageHelper.startPage(1, number);
        List<FirmwareUpgradeTask> processingTaskList = firmwareUpgradeTaskMapper.queryListByStatus(UpgradeTaskStatusEnum.PROCESSING.code());

        //检查任务包中的具体网关升级是否全部已经处理
        for (FirmwareUpgradeTask firmwareUpgradeTask : processingTaskList) {

            //1.先检查是否存在超时的正在处理中的任务,如果存在超时任务，将状态更新为失败
            Map<String, Object> para = new HashMap<>();
            para.put("upgradeTaskId", firmwareUpgradeTask.getId());
            para.put("status", UpgradeTaskDetailStatusEnum.PROCESSING.code());
            para.put("startRows", 0);
            para.put("pageSize", number);
            List<FirmwareUpgradeTaskDetail> processingDetailList = firmwareUpgradeTaskDetailMapper.queryListByTaskId(para);

            int currentTime;
            //是否有失败的任务
            boolean flag = false;
            List<String> gatewayIds = new ArrayList<String>();
            for(FirmwareUpgradeTaskDetail detail : processingDetailList)
            {
                currentTime = DateTools.getCurrentSecondTime();
                if(currentTime - detail.getUpgradeStartTime() > upgradeTimeout)
                {
                    detail.setUpgradeEndTime(currentTime);
                    detail.setStatus(UpgradeTaskDetailStatusEnum.FAILURE.code());
                    firmwareUpgradeTaskDetailMapper.updateByPrimaryKeySelective(detail);

                    flag = true;
                    gatewayIds.add(detail.getGatewayId());

                }
            }
            if(flag){
            	 //升级任务失败，调用删除升级同步到一级平台
                syncInfoToFirstLevelPlat(firmwareUpgradeTask.getId(),gatewayIds);
            }

            //2.查询升级任务中所有的网关是否已经处理完成，处理完成，将升级任务的状态更新为完成
            FirmwareUpgradeTaskDetail firmwareUpgradeTaskDetailToSearch = new FirmwareUpgradeTaskDetail();
            firmwareUpgradeTaskDetailToSearch.setUpgradeTaskId(firmwareUpgradeTask.getId());
            List<FirmwareUpgradeTaskDetail> waitProcessTaskDetailList = firmwareUpgradeTaskDetailMapper.queryListNoComplete(
                    firmwareUpgradeTaskDetailToSearch);

            //确认网关升级已经全部处理
            if (waitProcessTaskDetailList.size() < 1) {
                firmwareUpgradeTask.setTaskStatus(UpgradeTaskStatusEnum.PROCESSED.code());
                firmwareUpgradeTaskMapper.updateByPrimaryKeySelective(firmwareUpgradeTask);
            }

        }
    }


    /**
     *  升级任务失败，调用删除升级同步到一级平台
     * @param taskId
     */
    private void syncInfoToFirstLevelPlat(String taskId, List<String> gatewayIds)
    {
        //同步升级任务完成到杭研，调用删除升级任务
        Map<String, Object> reportMap = new HashMap<String, Object>();
        reportMap.put("RPCMethod", "Report");
        reportMap.put("CmdType", "REPORT_DELETE_UPGRADE_PLAN");
        reportMap.put("ID", (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
        reportMap.put(Constant.SEQUENCEID, InstructionUtil.generate8HexString());
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("PlanId", taskId);
    	List<String> macs = new ArrayList<String>();
    	List<GatewayInfo> gatewayList = gatewayInfoService.queryListByIds(gatewayIds);
    	for(GatewayInfo gw : gatewayList){

    		macs.add(gw.getGatewayMacaddress());
    	}
    	map1.put("GatewayList", macs);
        reportMap.put("Parameter", map1);
        syncInfoToFirstLevelPlatformService.report("reportDeleteUpgradePlan",reportMap);
    }
}
