package com.cmiot.acs;

import com.alibaba.dubbo.rpc.RpcContext;
import com.cmiot.acs.common.PropertiesUtil;
import com.cmiot.acs.domain.NameThreadFactory;
import com.cmiot.acs.facade.OperationCpeFacade;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 系统设置
 * Created by zjial on 2016/6/14.
 */
public class ServerSetting {

    public static ExecutorService KAFKA_CONSUMER_THREAD_POOL = new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS,
            new LinkedTransferQueue<>(), new NameThreadFactory("KAFKA-CONSUMER"));


    public static ExecutorService KAFKA_PRODUCER_THREAD_POOL = new ThreadPoolExecutor(10, 10, 10, TimeUnit.SECONDS,
            new LinkedTransferQueue<>(), new NameThreadFactory("KAFKA-PRODUCER"));


    public static ExecutorService OPERATION_CPE_THREAD_POOL = new ThreadPoolExecutor(10, 10, 10, TimeUnit.SECONDS,
            new LinkedTransferQueue<>(), new NameThreadFactory("OPERATION_CPE"));


    public static final Integer bossNum = PropertiesUtil.getPropertiesValueInt("boss.num");
    public static final Integer workerNum = PropertiesUtil.getPropertiesValueInt("worker.num");

    public static final Integer dubboPort = PropertiesUtil.getPropertiesValueInt("dubbo.port");
    public static final String zkAddress = PropertiesUtil.getPropertiesValue("zookeeper.address");


    public static final Integer httpPort = PropertiesUtil.getPropertiesValueInt("http.port");

    public static final String kafkaAddress = PropertiesUtil.getPropertiesValue("kafka.address");

    public static final boolean checkOuiSn = PropertiesUtil.getPropertiesValueBoolean("check.oui.sn");

    public static final boolean checkPassword = PropertiesUtil.getPropertiesValueBoolean("check.password");

    public static final boolean initiativeFound = PropertiesUtil.getPropertiesValueBoolean("initiative.found");

    public static final Integer waitTimeout = PropertiesUtil.getPropertiesValueInt("wait.timeout");

    public static String stunAddress = PropertiesUtil.getPropertiesValue("stun.address");

    public static Boolean LOG_BACKUP = PropertiesUtil.getPropertiesValueBoolean("cpe.log.backup");

    public static Boolean digestSwitch = null;

    public static final String dubboUrl = new StringBuilder("dubbo://")
            .append(RpcContext.getContext().getLocalHost()).append(":").append(dubboPort).append("/")
            .append(OperationCpeFacade.class.getName()).toString();


}
