package com.cmiot.rms.services.instruction.impl;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.facade.OperationCpeFacade;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.cache.TemporaryObject;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.LogTypeEnum;
import com.cmiot.rms.common.logback.LogBackRecord;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.InstructionsInfoWithBLOBs;
import com.cmiot.rms.services.AcsInterfaceService;
import com.cmiot.rms.services.GatewayInfoService;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.instruction.AbstractInstruction;
import com.cmiot.rms.services.instruction.InvokeInsService;
import com.cmiot.rms.services.template.RedisClientTemplate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author xukai
 * Date 2016/2/17
 */
@Service("invokeInsService")
public class InvokeInsServiceImpl implements InvokeInsService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private InstructionsService instructionsService;

    @Autowired
    private GatewayInfoService gatewayInfoService;

    @Autowired
    private  OperationCpeFacade operationCpeFacade;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedisClientTemplate redisClientTemplate;

    @Value("${instructionSendTimeOut}")
    int instructionSendTimeOut;

    @Value("${dubbo.provider.port}")
    int providerPort;

    @Override
    public Map<String, Object> executeBatch(Map<String, Object> map) {
        logger.info("======执行批量指令--->" + map.get("methodName"));
        try {
            List<String> gatewayIds = (List<String>) map.get("gatewayIds");
            String methodName = (String) map.get("methodName");
            List<GatewayInfo> cpes = gatewayInfoService.queryListByIds(gatewayIds);
            List<String> requestIds = new ArrayList<>();

            List<AbstractMethod> abstractMethods = new ArrayList<>();

            //对每个CPE生成指令加入到数组中
            for (GatewayInfo cpe : cpes) {

                if(StringUtils.isBlank(cpe.getGatewayConnectionrequesturl()))
                {
                    logger.info("网关== {}== 未注册！", cpe.getGatewayUuid());
                    continue;
                }

                InstructionsInfoWithBLOBs is = new InstructionsInfoWithBLOBs();
                is.setInstructionsId(UniqueUtil.uuid());
                is.setCpeIdentity(cpe.getGatewayUuid());//设置网关唯一ID
                requestIds.add(is.getInstructionsId());//把指令的ID返回出去
                if (methodName.equals("Download")) {
                    map.put("commandKey", is.getInstructionsId()); //如果是下载指令，commandKey为指令ID
                }
                AbstractInstruction ins = (AbstractInstruction) Class.forName("com.cmiot.rms.services.instruction.impl." + methodName + "Instruction").newInstance();

                AbstractMethod abstractMethod = ins.createIns(is, cpe, map);

                if(StringUtils.isNotBlank(cpe.getGatewayConnectionrequestUsername())
                        && StringUtils.isNotBlank(cpe.getGatewayConnectionrequestPassword()))
                {
                    abstractMethod.setCpeUserName(cpe.getGatewayConnectionrequestUsername());
                    abstractMethod.setCpePassword(cpe.getGatewayConnectionrequestPassword());
                }

                //down命令包含任务ID时，需要储存任务ID，供返回时更新状态使用
                if(null != map.get("taskId") && methodName.equals("Download"))
                {
                    JSONObject beforeContent = JSON.parseObject(JSON.toJSONString(abstractMethod));
                    beforeContent.put("taskId", map.get("taskId").toString());
                    is.setInstructionsBeforeContent(beforeContent.toJSONString());
                }
                else
                {
                    is.setInstructionsBeforeContent(JSON.toJSONString(abstractMethod));
                }

                instructionsService.addInstructionsInfo(is);

                abstractMethods.add(abstractMethod);

                logger.info("发送json到acs,指令id:{} json:{}", is.getInstructionsId(), JSON.toJSONString(abstractMethod));
                // 记录接口调用日志
                LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(abstractMethod), is.getInstructionsId(), cpe.getGatewayUuid(), methodName,LogTypeEnum.LOG_TYPE_SYSTEM.description());

            }
            Map<String, Object> returmMap = operationCpeFacade.doACSEMethods(abstractMethods);
            logger.info("连接ACS返回结果：" + returmMap);
            returmMap.put("requestIds", requestIds);

            return returmMap;
        } catch (Exception e) {
            logger.error("executeBatch exception:{}", e);
        }

        return null;
    }

    @Override
    public Map<String, Object> executeOne(Map<String, Object> map) {
        logger.info("======执行单条指令--->" + map.get("methodName"));
        try {

            String gatewayId = (String) map.get("gatewayId");
            String methodName = (String) map.get("methodName");
            GatewayInfo cpe = gatewayInfoService.selectByUuid(gatewayId);

            // 判断是否有网关信息
            if (cpe == null) {
                Map<String, Object> returmMap = new HashMap<>();
                returmMap.put("requestId", "");
                returmMap.put("resultCode", 1);
                return returmMap;
            }

            if(StringUtils.isBlank(cpe.getGatewayConnectionrequesturl()))
            {
                logger.info("网关== {}== 未注册！", cpe.getGatewayUuid());
                Map<String, Object> returmMap = new HashMap<>();
                returmMap.put("requestId", "");
                returmMap.put("resultCode", 1);
                return returmMap;
            }

            InstructionsInfoWithBLOBs is = new InstructionsInfoWithBLOBs();
            //生成指令ID作为请求的requestId
            String insId = UniqueUtil.uuid();
            is.setInstructionsId(insId);
            is.setCpeIdentity(gatewayId);
            map.put("requestId", insId);
            if (methodName.equals("Download")) {
                map.put("commandKey", insId); //如果是下载指令，commandKey为指令ID
            }
            AbstractInstruction ins = (AbstractInstruction) Class.forName("com.cmiot.rms.services.instruction.impl." + methodName + "Instruction").newInstance();

            AbstractMethod abstractMethod = ins.createIns(is, cpe, map);
            if(StringUtils.isNotBlank(cpe.getGatewayConnectionrequestUsername())
                    && StringUtils.isNotBlank(cpe.getGatewayConnectionrequestPassword()))
            {
                abstractMethod.setCpeUserName(cpe.getGatewayConnectionrequestUsername());
                abstractMethod.setCpePassword(cpe.getGatewayConnectionrequestPassword());
            }

            //down命令包含任务ID时，需要储存任务ID，供返回时更新状态使用
            if(methodName.equals("Download") && null != map.get("taskId"))
            {
                JSONObject beforeContent = JSON.parseObject(JSON.toJSONString(abstractMethod));
                beforeContent.put("taskId", map.get("taskId").toString());
                is.setInstructionsBeforeContent(beforeContent.toJSONString());
            }
            else
            {
                is.setInstructionsBeforeContent(JSON.toJSONString(abstractMethod));
            }

            String providerUrl = "dubbo://" + RpcContext.getContext().getLocalHost() + ":" + providerPort + "/" + AcsInterfaceService.class.getName();
            abstractMethod.setCallbackUrl(providerUrl);

            instructionsService.addInstructionsInfo(is);

            Map<String, Object> returnMap ;
            logger.info("发送json到acs 指令id:{} 网关id：{} json:{}", insId, gatewayId, JSON.toJSONString(abstractMethod) );

            
            List<AbstractMethod> abstractMethods = new ArrayList<>();
            abstractMethods.add(abstractMethod);

            returnMap = invokeAcs(cpe, abstractMethods);
            logger.info("连接ACS返回结果：" + returnMap);

            //acs返回失败，表示acs与网关不能建立连接，直接返回
            if(null == returnMap)
            {
                returnMap.put("requestId", "");
                returnMap.put("resultCode", 1);
                return returnMap;
            }
            if(null != returnMap && (!String.valueOf(returnMap.get("resultCode")).equals("0")))
            {
                return returnMap;
            }

            //为指令添加临时对象锁，等待指令异步返回
            TemporaryObject temporaryObject = new TemporaryObject(insId);
            RequestCache.set(insId, temporaryObject);

            logger.info("Start wait: {}", temporaryObject.getRequestId());
            synchronized (temporaryObject) {
                temporaryObject.wait(instructionSendTimeOut);
            }
            RequestCache.delete(insId);

            returnMap.put("requestId", insId);

            logger.info("End wait: {}", temporaryObject.getRequestId());

            // 记录接口调用日志
            LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(abstractMethod), insId, cpe.getGatewayUuid(), methodName,LogTypeEnum.LOG_TYPE_SYSTEM.description());
            return returnMap;
        } catch (Exception e) {
            logger.error("executeOne exception:{}", e);
        }
        return null;
    }

    /**
     * 发送指令到ACS
     * 先从redis中查询ACS地址（网关与ACS保持连接），如果存在连接，直接通过直连方式发送指令给ACS
     * 如果不存在连接，则采用传统的dubbo方式调用，dubbo自己负载均衡
     *
     * @param abstractMethods
     */
    private Map<String, Object> invokeAcs(GatewayInfo cpe, List<AbstractMethod> abstractMethods) {
        Map<String, Object> retMap = null;

        //从redis中获取网关连接的ACS地址
        String url = redisClientTemplate.get(cpe.getGatewayFactoryCode() + Constant.SEPARATOR + cpe.getGatewaySerialnumber() + Constant.SEPARATOR + "URL");

        //地址为空，说明目前网关未与ACS连接，采用传统的dubbo方式调用，dubbo自己负载均衡
        if (StringUtils.isBlank(url)) {
            return operationCpeFacade.doACSEMethods(abstractMethods);
        }

        //地址不为空，说明存在连接，直接通过直连方式发送指令给ACS
        ReferenceBean<OperationCpeFacade> referenceBean = new ReferenceBean<>();
        referenceBean.setApplicationContext(applicationContext);
        referenceBean.setInterface(com.cmiot.acs.facade.OperationCpeFacade.class);
        referenceBean.setUrl(url);

        try {
            referenceBean.afterPropertiesSet();
            OperationCpeFacade cpeFacade = referenceBean.get();
            retMap = cpeFacade.doACSEMethods(abstractMethods);

            //如果直连失败，重新调用
            if (null == retMap || (!String.valueOf(retMap.get("resultCode")).equals("0"))) {
                retMap = operationCpeFacade.doACSEMethods(abstractMethods);
            }
        } catch (Exception e) {
            logger.error("invokeAcs exception:{}", e);
        }

        return retMap;
    }


}
