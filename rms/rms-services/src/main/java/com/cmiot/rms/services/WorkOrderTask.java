package com.cmiot.rms.services;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.dao.mapper.*;
import com.cmiot.rms.dao.model.BoxBusiness;
import com.cmiot.rms.dao.model.BoxBusinessExample;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.BoxBusinessExample.Criteria;
import com.cmiot.rms.dao.model.GatewayBusiness;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.template.RedisClientTemplate;
import com.cmiot.rms.services.workorder.ExcuteBoxWorkOrderThread;
import com.cmiot.rms.services.workorder.SpringApplicationContextHolder;
import com.cmiot.rms.services.workorder.WorkOrderForTaskThread;
import com.cmiot.rms.services.workorder.WorkerThreadPool;
import com.cmiot.rms.services.workorder.impl.BusiOperation;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/6/9.
 */
public class WorkOrderTask {

    private Logger logger = LoggerFactory.getLogger(WorkOrderTask.class);

    @Autowired
    private GatewayBusinessMapper gatewayBusinessMapper;

    //不能删除，初始化springApplicationContextHolder用的
    @Autowired
    private SpringApplicationContextHolder springApplicationContextHolder;

    @Autowired
    RedisClientTemplate redisClientTemplate;

    @Autowired
    GatewayBusinessExecuteHistoryMapper gatewayBusinessExecuteHistoryMapper;

    @Autowired
    private BoxBusinessMapper boxBusinessMapper;

    @Autowired
	private  BoxInfoMapper boxInfoMapper;
    @Autowired
    GatewayInfoMapper gatewayInfoMapper;


    @Value("${timing.task.search.number}")
    int number;

    @Value("${timing.task.lock.timeout}")
    int lockTimeout;

    @Value("${work.order.fail.count}")
    int workOrderFailCount;

    @Value("${workOrder.workerPool.taskQueue.size}")
    int taskQueueSize;

    @Value("${workOrder.workerPool.coreSize}")
    int coreSize;

    @Value("${workOrder.workerPool.maxSize}")
    int maxSize;

    @Value("${workOrder.workerPool.keepAlive.seconds}")
    int keepAliveSeconds;

    public void work(){
        logger.info("start invoke WorkOrderTask.work");
        //初始化线程池
        WorkerThreadPool.getInstance().init(taskQueueSize,coreSize,maxSize,keepAliveSeconds);
        try {
            //1查询所有有效的网关业务数据
            String str = redisClientTemplate.set("workOrderTask_lock", "yes", "NX","EX", lockTimeout);
            if (str == null) {// 存在锁
                logger.info("工单任务正在执行查询..");
                return;
            }
            else
            {
                logger.info("开始查询工单任务：{}", str);
            }

            PageHelper.startPage(1, number);
            GatewayBusiness gatewayBusinessParam = new GatewayBusiness();
            gatewayBusinessParam.setFailCount(workOrderFailCount + 1);
            //查询出所有符合执行工单的网关
            List<GatewayInfo> list = gatewayInfoMapper.selectGatewayInfoListByWorkOrder(gatewayBusinessParam);

            //循环更新工单状态，并启动线程去执行工单
            if (list != null && list.size() > 0) {
                for (GatewayInfo gatewayInfo : list) {
                    //先查询该网关所有符合条件的工单
                    GatewayBusiness selectBusi = new GatewayBusiness();
                    selectBusi.setGatewayPassword(gatewayInfo.getGatewayPassword());
                    selectBusi.setBusinessStatu("2");
                    if(!StringUtils.isEmpty(gatewayInfo.getGatewayAdslAccount())){
                        selectBusi.setAdslAccount(gatewayInfo.getGatewayAdslAccount());
                    }
                    selectBusi.setFailCount(workOrderFailCount + 1);
                    List<GatewayBusiness> businessList = gatewayBusinessMapper.selectByGatewayInfo(selectBusi);
                    List ids = new ArrayList();
                    if(businessList != null && businessList.size() > 0){
                        //更新工单状态
                        for (GatewayBusiness gatewayBusiness : businessList) {
                            String gatewayBusinessId = gatewayBusiness.getId();
                            ids.add(gatewayBusinessId);
                        }
                        if(ids.size() > 0)
                        {
                            Map<String, Object> para = new HashMap<>();
                            para.put("businessStatu", "3");
                            para.put("ids", ids);
                            int k = gatewayBusinessMapper.batchUpdateStatus(para);
                            logger.info("更新工单为执行中，更新数据条数：" + k);
                        }

                        //调用线程去执行开通该网关的业务开通,并提交线程池
                        WorkerThreadPool.getInstance().submitTask(new WorkOrderForTaskThread(businessList,gatewayInfo));
                    }else{
                        logger.info("网关SN为"+gatewayInfo.getGatewaySerialnumber()+"的网关没有可执行的工单");
                    }
                }
            } else {
                logger.info("未查询到有需要执行工单的网关信息");
            }



            /*-------------------------------查询机顶盒工单----------------------------------------*/
            BoxBusinessExample example = new BoxBusinessExample();
            Criteria criteria = example.createCriteria();
            criteria.andBusinessStatuEqualTo("2");
            criteria.andFailCountIsNotNull();
        	List<BoxBusiness> businessList = boxBusinessMapper.selectByExample(example);
        	for (BoxBusiness boxBusiness : businessList) {

        		BoxInfo boxInfo = new BoxInfo();
                boxInfo.setBoxMacaddress(boxBusiness.getBoxMac());
                List<BoxInfo> boxList = boxInfoMapper.selectBoxInfo(boxInfo);
				if (boxList != null && !boxList.isEmpty()) {
					// 注册且在线
					if ("2".equals(boxList.get(0).getBoxStatus()) && boxList.get(0).getBoxOnline() == 1) {
						// 在redis中确认是否真的在线
						if ("1".equals(redisClientTemplate.get(Constant.BOX_ONLINE + boxList.get(0).getBoxSerialnumber()))) {
							WorkerThreadPool.getInstance().submitTask(new ExcuteBoxWorkOrderThread(boxBusiness));
						}
					}
				}

			}

        }catch (Exception e){
            logger.error("定时执行工单任务报错 {}",e);
        }
        finally {
            redisClientTemplate.del("workOrderTask_lock");
        }
    }

}
