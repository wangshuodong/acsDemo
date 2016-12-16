package com.cmiot.acs.facadeimpl;

import com.alibaba.fastjson.JSONObject;
import com.cmiot.acs.ServerSetting;
import com.cmiot.acs.common.AbHttpUtil;
import com.cmiot.acs.domain.cache.SpringRedisUtil;
import com.cmiot.acs.model.AbstractMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反向连接CPE
 * Created by ZJL on 2016/11/4.
 */
public class AcsCpeFacade {
    public static final Logger logger = LoggerFactory.getLogger(AcsCpeFacade.class);

    public static Map<String, Object> abAcsToCpe(String requestId, String cpeId, String cpeUrl, String cpeUserName, String cpeUserPassword) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("requestId", requestId);
        resultMap.put("cpeId", cpeId);
        boolean cpeStatus = false;
        if (cpeUrl.startsWith("http") || cpeUrl.startsWith("HTTP")) {
            try {
                CloseableHttpResponse response = AbHttpUtil.get(cpeUrl, cpeUserName, cpeUserPassword);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    cpeStatus = true;
                    resultMap.put("resultCode", 0);
                    resultMap.put("resultMessage", "ACS-CPE反向连接成功");
                } else {
                    resultMap.put("resultCode", 3);
                    resultMap.put("resultMessage", "ACS-CPE反向连接失败：" + statusCode);
                }
            } catch (Exception e) {
                resultMap.put("resultCode", 5);
                resultMap.put("resultMessage", "ACS-CPE反向连接异常：" + e.getMessage());
            }
        } else {
            String[] urls = cpeUrl.split(":");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ip", urls[0]);
            jsonObject.put("port", urls[1]);
            jsonObject.put("un", cpeUserName);
            jsonObject.put("key", cpeUserPassword);
            try {
                CloseableHttpResponse response = AbHttpUtil.sendJson(ServerSetting.stunAddress, jsonObject);
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    cpeStatus = true;
                    resultMap.put("resultCode", 0);
                    resultMap.put("resultMessage", "ACS-ITV反向连接成功");
                } else {
                    resultMap.put("resultCode", 3);
                    resultMap.put("resultMessage", "ACS-ITV反向连接失败" + statusCode);
                }
            } catch (Exception e) {
                resultMap.put("resultCode", 5);
                resultMap.put("resultMessage", "ACS-ITV反向连接异常" + e.getMessage());
            }
        }

        if (!cpeStatus) {
            logger.info("反向连接失败删除cpeId={},requestId={}的指令!", cpeId, requestId);
            SpringRedisUtil.deleteList(cpeId);
        }

        return resultMap;
    }


    public static Map<String, List<AbstractMethod>> abstractMethodListGroup(List<AbstractMethod> abstractMethodList) {
        Map<String, List<AbstractMethod>> groupMap = new HashMap<>();
        if (abstractMethodList != null && abstractMethodList.size() > 0) {
            for (AbstractMethod method : abstractMethodList) {
                String cpeId = method.getCpeId();
                if (StringUtils.isNotBlank(cpeId)) {
                    if (groupMap.containsKey(cpeId)) {
                        groupMap.get(cpeId).add(method);
                    } else {
                        List<AbstractMethod> methods = new ArrayList<>();
                        methods.add(method);
                        groupMap.put(cpeId, methods);
                    }
                }
            }
        }
        return groupMap;
    }

}
