package com.cmiot.rms.services.boxManager.instruction.impl;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.facade.OperationCpeFacade;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.cache.TemporaryObject;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.LogTypeEnum;
import com.cmiot.rms.common.logback.LogBackRecord;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.dao.model.InstructionsInfoWithBLOBs;
import com.cmiot.rms.services.BoxInfoService;
import com.cmiot.rms.services.BoxInterfaceService;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.boxManager.instruction.BoxAbstractInstruction;
import com.cmiot.rms.services.boxManager.instruction.BoxInvokeInsService;

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
 * @Author admin
 * Date 2016/6/14
 */
@Service
public class BoxInvokeInsServiceImpl implements BoxInvokeInsService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private InstructionsService instructionsService;

    @Autowired
    private  OperationCpeFacade operationCpeFacade;

    @Autowired
    BoxInfoMapper boxInfoMapper;
    
    @Autowired
    private BoxInfoService boxInfoService;


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
            JSONArray array = new JSONArray();
            List<String> boxIds = (List<String>) map.get("boxIds");
            String methodName = (String) map.get("methodName");
            List<BoxInfo> cpes = boxInfoService.queryListByIds(boxIds);
            List<String> requestIds = new ArrayList<>();

            List<AbstractMethod> abstractMethods = new ArrayList<>();

            //对每个CPE生成指令加入到数组中
            for (BoxInfo cpe : cpes) {
                /*if(StringUtils.isBlank(cpe.getBoxConnectionrequesturl()))
                {
                    logger.info("机顶盒== {}== 未注册！", cpe.getGatewayUuid());
                    continue;
                }*/
                InstructionsInfoWithBLOBs is = new InstructionsInfoWithBLOBs();
                is.setInstructionsId(UniqueUtil.uuid());
                is.setCpeIdentity(cpe.getBoxUuid());//设置机顶盒唯一ID
                requestIds.add(is.getInstructionsId());//把指令的ID返回出去
                if (methodName.equals("Download")) {
                    map.put("commandKey", is.getInstructionsId()); //如果是下载指令，commandKey为指令ID
                }
                BoxAbstractInstruction ins = (BoxAbstractInstruction) Class.forName("com.cmiot.rms.services.boxManager.instruction.impl.Box" + methodName + "Instruction").newInstance();

                AbstractMethod abstractMethod = ins.createIns(is, cpe, map);
                abstractMethods.add(abstractMethod);

                if (methodName.equals("Download") && null != map.get("taskId")) {
                    JSONObject beforeContent = JSON.parseObject(JSON.toJSONString(abstractMethod));
                    beforeContent.put("taskId", map.get("taskId").toString());
                    beforeContent.put("loguuid", map.get("loguuid"));
                    is.setInstructionsBeforeContent(beforeContent.toJSONString());
                    logger.info("LogId:{} down命令执行is.setInstructionsBeforeConten()设置值为:{}", map.get("loguuid"), beforeContent.toJSONString());
                } else {
                    is.setInstructionsBeforeContent(JSON.toJSONString(abstractMethod));
                }

                instructionsService.addInstructionsInfo(is);

                logger.info("发送json到acs,指令id:{} json:{}", is.getInstructionsId(), array.toJSONString());
                // 记录接口调用日志
                LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(abstractMethod), is.getInstructionsId(), cpe.getBoxUuid(), methodName, LogTypeEnum.LOG_TYPE_SYSTEM.description());

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
        logger.info("start invoke BoxInvokeInsServiceImpl.executeOne,parameter:{}" ,map);
        try {

            String boxUuid = (String) map.get("boxUuid");
            String methodName = (String) map.get("methodName");
            BoxInfo boxInfo = boxInfoMapper.selectByPrimaryKey(boxUuid);

            // 判断是否有网关信息
            if (boxInfo == null) {
                Map<String, Object> returmMap = new HashMap<>();
                returmMap.put("requestId", "");
                returmMap.put("resultCode", 1);
                return returmMap;
            }

          /*  if(StringUtils.isBlank(boxInfo.getBoxConnectionrequesturl()))
            {
                logger.info("机顶盒== {}== 未注册！", boxInfo.getGatewayUuid());
                Map<String, Object> returmMap = new HashMap<>();
                returmMap.put("requestId", "");
                returmMap.put("resultCode", 1);
                return returmMap;
            }
*/
            InstructionsInfoWithBLOBs is = new InstructionsInfoWithBLOBs();
            //生成指令ID作为请求的requestId
            String insId = UniqueUtil.uuid();
            is.setInstructionsId(insId);
            is.setCpeIdentity(boxUuid);
            map.put("requestId", insId);
            if (methodName.equals("Download")) {
                map.put("commandKey", insId); //如果是下载指令，commandKey为指令ID
            }
            BoxAbstractInstruction ins = (BoxAbstractInstruction) Class.forName("com.cmiot.rms.services.boxManager.instruction.impl.Box" + methodName + "Instruction").newInstance();

            AbstractMethod abstractMethod = ins.createIns(is, boxInfo, map);

			// down命令包含任务ID时，需要储存任务ID，供返回时更新状态使用
			if (methodName.equals("Download") && null != map.get("taskId")) {
				JSONObject beforeContent = JSON.parseObject(JSON.toJSONString(abstractMethod));
				beforeContent.put("taskId", map.get("taskId").toString());
				beforeContent.put("loguuid", map.get("loguuid"));
				is.setInstructionsBeforeContent(beforeContent.toJSONString());
				logger.info("LogId:{} down命令执行is.setInstructionsBeforeConten()设置值为:{}", map.get("loguuid"), beforeContent.toJSONString());
			} else {
				is.setInstructionsBeforeContent(JSON.toJSONString(abstractMethod));
			}

            String providerUrl = "dubbo://" + RpcContext.getContext().getLocalHost() + ":" + providerPort + "/" + BoxInterfaceService.class.getName();
            abstractMethod.setCallbackUrl(providerUrl);
			
            instructionsService.addInstructionsInfo(is);

            Map<String, Object> returnMap ;
            logger.info("发送json到acs 指令id:{} 机顶盒id：{} json:{}", insId, boxUuid, JSON.toJSONString(abstractMethod) );

            List<AbstractMethod> abstractMethods = new ArrayList<>();
            abstractMethods.add(abstractMethod);

            returnMap = invokeAcs(boxInfo, abstractMethods);

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

            returnMap.put("requestId", insId);

            //为指令添加临时对象锁，等待指令异步返回
            TemporaryObject temporaryObject = new TemporaryObject(insId);
            RequestCache.set(insId, temporaryObject);

            logger.info("Start wait: {}", temporaryObject.getRequestId());
            synchronized (temporaryObject) {
                temporaryObject.wait(instructionSendTimeOut);
            }
            RequestCache.delete(insId);

            logger.info("End wait: {}", temporaryObject.getRequestId());

            // 记录接口调用日志
            LogBackRecord.getInstance().recordInvokingLog(LogTypeEnum.LOG_TYPE_SYSTEM.code(), JSON.toJSONString(abstractMethod), insId, boxUuid, methodName, LogTypeEnum.LOG_TYPE_SYSTEM.description());

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
    private Map<String, Object> invokeAcs(BoxInfo boxInfo, List<AbstractMethod> abstractMethods) {
        Map<String, Object> retMap = null;

        //从redis中获取网关连接的ACS地址
        String url = redisClientTemplate.get(boxInfo.getBoxFactoryCode() + Constant.SEPARATOR + boxInfo.getBoxSerialnumber() + Constant.SEPARATOR + "URL");

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
            logger.error("Box invokeAcs exception:{}", e);
        }

        return retMap;
    }


}
