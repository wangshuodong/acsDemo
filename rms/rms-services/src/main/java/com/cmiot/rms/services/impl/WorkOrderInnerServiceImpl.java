package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.acs.model.Inform;
import com.cmiot.rms.dao.mapper.BusinessCategoryMapper;
import com.cmiot.rms.dao.mapper.GatewayBusinessMapper;
import com.cmiot.rms.dao.model.GatewayBusiness;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.WorkOrderInnerService;
import com.cmiot.rms.services.workorder.WorkOrderForTaskThread;
import com.cmiot.rms.services.workorder.WorkerThreadPool;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/9/18.
 */
@Service
public class WorkOrderInnerServiceImpl implements WorkOrderInnerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkOrderInnerServiceImpl.class);

    @Autowired
    private GatewayInfoService gatewayInfoService;

    @Value("${work.order.fail.count}")
    int workOrderFailCount;

    @Autowired
    GatewayBusinessMapper gatewayBusinessMapper;

    @Autowired
    private BusinessCategoryMapper businessCategoryMapper;



    @Override
    public void excuteFailWorkOrder(Inform inform) {
        LOGGER.info("start invoke WorkOrderInnerServiceImpl.excuteFailWorkOrder");
        GatewayInfo gatewayInfo = new GatewayInfo();
        gatewayInfo.setGatewaySerialnumber(inform.getDeviceId().getSerialNubmer());
        gatewayInfo.setGatewayFactoryCode(inform.getDeviceId().getOui());
        // 根据 SN 和 OUI查询网关信息
        GatewayInfo selectGatewayInfo = gatewayInfoService.selectGatewayInfo(gatewayInfo);
        if(selectGatewayInfo == null){
            LOGGER.info("invoke WorkOrderInnerServiceImpl.excuteFailWorkOrder，网关信息为空");
            return;
        }
        if("已绑定".equals(selectGatewayInfo.getGatewayStatus())){
            //查询是否有执行失败达到最大次数的工单
            GatewayBusiness gatewayBusinessParam = new GatewayBusiness();
            gatewayBusinessParam.setFailCount(workOrderFailCount);
            gatewayBusinessParam.setBusinessStatu("2");
            gatewayBusinessParam.setGatewayPassword(selectGatewayInfo.getGatewayPassword());
            List<GatewayBusiness> list = gatewayBusinessMapper.selectMaxFail(gatewayBusinessParam);

            List<String> ids = new ArrayList<>();

            if (list != null && list.size() > 0) {
                for (GatewayBusiness gatewayBusiness : list) {
                    String gatewayBusinessId = gatewayBusiness.getId();
                    ids.add(gatewayBusinessId);
                }
                if(ids.size() > 0)
                {
                    Map<String, Object> para = new HashMap<>();
                    para.put("businessStatu", "3");
                    para.put("ids", ids);
                    gatewayBusinessMapper.batchUpdateStatus(para);
                }
                //调用线程去执行开通该网关的业务开通,并提交线程池
                WorkerThreadPool.getInstance().submitTask(new WorkOrderForTaskThread(list,gatewayInfo));

            }else {
                LOGGER.info("invoke WorkOrderInnerServiceImpl.excuteFailWorkOrder，网关网关没有执行失败次数为" + workOrderFailCount + "次的工单");
            }
        }else{
            LOGGER.info("invoke WorkOrderInnerServiceImpl.excuteFailWorkOrder，网关未绑定");
        }
    }
}
