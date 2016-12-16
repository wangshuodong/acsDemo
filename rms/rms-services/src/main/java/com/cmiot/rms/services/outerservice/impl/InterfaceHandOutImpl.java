package com.cmiot.rms.services.outerservice.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.cache.TemporaryObject;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.services.message.KafkaProducer;
import com.cmiot.rms.services.outerservice.InterfaceHandOut;
import com.cmiot.rms.services.outerservice.RequestMgrService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangzhen on 2016/1/29.
 */
@Service("interfaceHandOut")
public class InterfaceHandOutImpl implements InterfaceHandOut {
    @Autowired
    KafkaProducer kafkaProducer;

    private static Logger logger = LoggerFactory.getLogger(InterfaceHandOutImpl.class);

    @Autowired
    private RequestMgrService requestMgrService;
    

    @Override
    public Object handOut(JSONObject jsonObject) {
        // 方法名称
        String methodName = (String) jsonObject.get("methodName");
        String servicemethod;
        // Inform 为特殊处理
        if (StringUtils.equals(methodName, "Inform") || StringUtils.equals(methodName, "Fault")) {
            servicemethod = methodName.toLowerCase();
        } else {
            String[] arr = methodName.split("Response");
            // 首字母转换为小写
            StringBuffer sb = new StringBuffer(arr[0]);
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            servicemethod = sb.toString();
        }
        Map<String, Object> result;
        Object value = "";
        // 对象json串
        String detail = JSON.toJSONString(jsonObject);
        try {
            // 数据分发
            Class cls = requestMgrService.getClass();
            Method method;
            method = cls.getDeclaredMethod(servicemethod, JSONObject.class);
            value = method.invoke(requestMgrService, jsonObject);

            String requestId  = null != jsonObject.get("requestId") ?jsonObject.get("requestId").toString():"";
            if(StringUtils.isNotEmpty(requestId) && (!StringUtils.equals(methodName, "Inform")))
            {
                TemporaryObject object = RequestCache.get(requestId);
                if(null != object)
                {
                    synchronized(object)
                    {
                        logger.info("handOut notify:{}", requestId);
                        object.notifyAll();
                    }
                }
                else
               {
                   logger.info("Gateway no cache :{}", requestId);
//                    logger.info("kafkaProducer sendMessage:{}", requestId);
//                    kafkaProducer.sendMessage("requestId", requestId);
                }
            }
        } catch (Exception e) {
            logger.error("invoke handOut error{}",e);
            result = new HashMap<>();
            result.put(Constant.CODE, RespCodeEnum.RC_1004.code());
            result.put(Constant.MESSAGE, "方法名不存在");
            logger.info(detail);
            logger.info(JSON.toJSONString(result));
            e.printStackTrace();
            return result;
        }
        return value;
    }
}
