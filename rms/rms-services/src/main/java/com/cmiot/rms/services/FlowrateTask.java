package com.cmiot.rms.services;

import com.cmiot.ams.service.AreaService;
import com.cmiot.rms.common.utils.DateTools;
import com.cmiot.rms.dao.mapper.FlowRateTaskMapper;
import com.cmiot.rms.dao.mapper.GatewayFlowrateTaskDetailMapper;
import com.cmiot.rms.services.instruction.InstructionMethodService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.thread.ThreadPoolFactory;
import com.cmiot.rms.services.thread.ThreadTypeEnum;
import com.cmiot.rms.services.thread.flowratetask.FlowrateTaskParameter;
import com.cmiot.rms.services.thread.flowratetask.FlowrateTaskThread;
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
 * Created by zoujiang  on 2016/6/17.
 */
public class FlowrateTask {

    private Logger logger = LoggerFactory.getLogger(FlowrateTask.class);

    @Autowired
    FlowRateTaskMapper flowRateTaskMapper;

    @Autowired
    private AreaService amsAreaService;

    @Autowired
    private GatewayInfoService gatewayInfoService;

    @Autowired
    private InstructionMethodService instructionMethodService;

    @Autowired
    RedisClientTemplate redisClientTemplate;
    
    @Autowired
    GatewayFlowrateTaskDetailMapper gatewayFlowrateTaskDetailMapper;


    @Value("${timing.task.search.number}")
    int number;

    @Value("${timing.task.lock.timeout}")
    int lockTimeout;

    public void process()
    {
        try {
            //1查询所有有效的网关业务数据
            String str = redisClientTemplate.set("flowrateTask_lock", "yes", "NX","EX", lockTimeout);
            if (str == null) {// 存在锁
                logger.info("流量任务正在执行查询..");
                return;
            }
            else
            {
                logger.info("开始查询流量任务：{}", str);
            }

            PageHelper.startPage(1, number);
            List<Map<String,Object>> taskList = flowRateTaskMapper.selectTimingTask(DateTools.getCurrentSecondTime());
            if(null == taskList || taskList.size() < 1)
            {
                return;
            }

            List<String> ids = new ArrayList<>();
            for(Map<String,Object> map : taskList)
            {
                String taskId = map.get("id").toString();
                ids.add(taskId);
            }

            Map<String, Object> para = new HashMap<>();
            para.put("status", 2);
            para.put("ids", ids);
            flowRateTaskMapper.batchUpdateStatus(para);

            for(Map<String,Object> map : taskList)
            {
                FlowrateTaskParameter parameter = new FlowrateTaskParameter();
                parameter.setAmsAreaService(amsAreaService);
                parameter.setFlowrateMap(map);
                parameter.setFlowRateTaskMapper(flowRateTaskMapper);
                parameter.setGatewayInfoService(gatewayInfoService);
                parameter.setInstructionMethodService(instructionMethodService);
                parameter.setGatewayFlowrateTaskDetailMapper(gatewayFlowrateTaskDetailMapper);
                ThreadPoolFactory.getInstance().getTheadPool(ThreadTypeEnum.EXECUTE_FLOWRATE_TASK).execute(new FlowrateTaskThread(parameter));
            }
        }
        finally {
            redisClientTemplate.del("flowrateTask_lock");
        }

    }

}
