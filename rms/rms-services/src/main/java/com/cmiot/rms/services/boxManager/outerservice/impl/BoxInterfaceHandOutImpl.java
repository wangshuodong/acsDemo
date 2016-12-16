package com.cmiot.rms.services.boxManager.outerservice.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.model.Inform;
import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.cache.TemporaryObject;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.services.BoxInfoService;
import com.cmiot.rms.services.InstructionsService;
import com.cmiot.rms.services.boxManager.outerservice.BoxInterfaceHandOut;
import com.cmiot.rms.services.boxManager.outerservice.BoxRequestMgrService;
import com.cmiot.rms.services.message.KafkaProducer;
import com.cmiot.rms.services.template.RedisClientTemplate;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangzhen on 2016/1/29.
 */
@Service
public class BoxInterfaceHandOutImpl implements BoxInterfaceHandOut {
    @Autowired
    KafkaProducer kafkaProducer;

    private static Logger logger = LoggerFactory.getLogger(BoxInterfaceHandOutImpl.class);

    @Autowired
    private BoxRequestMgrService boxRequestMgrService;

    @Autowired
    private RedisClientTemplate redisClientTemplate;

    @Autowired
    private BoxInfoService boxInfoService;

    @Autowired
    private InstructionsService instructionsService;

    @Value("${box.online.timeout}")
    int onlineTimeout;

    @Override
    public Object handOut(JSONObject jsonObject) {
        // 方法名称
        String methodName = (String) jsonObject.get("methodName");
        String servicemethod;
        // Inform 为特殊处理
        if (StringUtils.equals(methodName, "Inform") || StringUtils.equals(methodName, "Fault")) {
            servicemethod = methodName.toLowerCase();
            //修改redis中机顶盒在线状态标识
            if (StringUtils.equals(methodName, "Inform")) {
                Inform inform = JSON.toJavaObject(jsonObject, Inform.class);
                if(null != inform)
                {
                    BoxInfo box = new BoxInfo();
                    box.setBoxSerialnumber(inform.getDeviceId().getSerialNubmer());
                    box.setBoxOnline(1);
                    long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                    // 最近连接时间
                    box.setBoxLastConnTime((int) timeSeconds);
                    // 设置机顶盒状态：在线
                    boxInfoService.updateBySnSelective(box);
                    //在线标识
                    redisClientTemplate.set(Constant.BOX_ONLINE + inform.getDeviceId().getSerialNubmer(), "1");
                    //设置超时时间
                    redisClientTemplate.expire(Constant.BOX_ONLINE + inform.getDeviceId().getSerialNubmer(), onlineTimeout);

                    logger.info("redisClientTemplate put " + Constant.BOX_ONLINE + inform.getDeviceId().getSerialNubmer() + ",超时时间:" + onlineTimeout);
                }
            }

        } else {
            String[] arr = methodName.split("Response");
            // 首字母转换为小写
            StringBuffer sb = new StringBuffer(arr[0]);
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
            servicemethod = sb.toString();

            String requestId;
            if("transferComplete".equals(servicemethod))
            {
                requestId = jsonObject.getString("commandKey");
            }
            else
            {
                requestId = jsonObject.getString("requestId");
            }

            //修改redis中机顶盒在线状态标识

            if (StringUtils.isNotEmpty(requestId)) {

                Map<String, String> map = instructionsService.getInstructionsInfo(requestId);
                if (null != map && map.size() > 0) {
                    String boxId = map.get("cpeIdentity");
                    if (StringUtils.isNotBlank(boxId)) {
                        BoxInfo box = boxInfoService.selectByPrimaryKey(boxId);
                        if (null != box) {
                            BoxInfo box2 = new BoxInfo();
                            box2.setBoxSerialnumber(box.getBoxSerialnumber());
                            box2.setBoxOnline(1);
                            long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
                            // 最近连接时间
                            box2.setBoxLastConnTime((int) timeSeconds);
                            // 设置机顶盒状态：在线
                            boxInfoService.updateBySnSelective(box2);

                            //在线标识
                            redisClientTemplate.set(Constant.BOX_ONLINE + box.getBoxSerialnumber(), "1");
                            //设置超时时间
                            redisClientTemplate.expire(Constant.BOX_ONLINE + box.getBoxSerialnumber(), onlineTimeout);

                            logger.info("redisClientTemplate put " + Constant.BOX_ONLINE + box.getBoxSerialnumber() + ",超时时间:" + onlineTimeout);
                        }
                    }
                }
            }

        }
        Map<String, Object> result;
        Object value = "";
        // 对象json串
        String detail = JSON.toJSONString(jsonObject);
        try {
            // 数据分发
            Class cls = boxRequestMgrService.getClass();
            Method method;
            method = cls.getDeclaredMethod(servicemethod, JSONObject.class);
            value = method.invoke(boxRequestMgrService, jsonObject);

            String requestId = null != jsonObject.get("requestId") ? jsonObject.get("requestId").toString() : "";
            if (StringUtils.isNotEmpty(requestId) && (!StringUtils.equals(methodName, "Inform"))) {
                TemporaryObject object = RequestCache.get(requestId);
                if (null != object) {
                    synchronized (object) {
                        logger.info("handOut notify:{}", requestId);
                        object.notifyAll();
                    }
                }
                else {
                    logger.info("Box no cache :{}", requestId);
//                    logger.info("kafkaProducer sendMessage:{}", requestId);
//                    kafkaProducer.sendMessage("requestId", requestId);
                }
            }
        } catch (Exception e) {
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
