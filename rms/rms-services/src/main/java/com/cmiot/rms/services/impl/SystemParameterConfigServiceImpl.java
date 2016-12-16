package com.cmiot.rms.services.impl;

import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.dao.mapper.SystemParameterInfoMapper;
import com.cmiot.rms.dao.model.SystemParameterInfo;
import com.cmiot.rms.services.SystemParameterConfigService;
import com.cmiot.rms.services.message.KafkaProducer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by panmingguo on 2016/10/25.
 */
public class SystemParameterConfigServiceImpl implements SystemParameterConfigService {

    public final Logger logger = LoggerFactory.getLogger(SystemParameterConfigServiceImpl.class);

    @Autowired
    SystemParameterInfoMapper systemParameterInfoMapper;

    @Autowired
    KafkaProducer kafkaProducer;

    /**
     * 更新参数
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> updateParameter(Map<String, Object> parameter) {
        logger.info("Start invoke updateParameter:{}", parameter);
        String name = null != parameter.get("name") ? parameter.get("name").toString() : "";
        String value = null != parameter.get("value") ? parameter.get("value").toString() : "";
        Map<String, Object> retMap = new HashMap<>();
        if(StringUtils.isBlank(name) || StringUtils.isBlank(value))
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            return retMap;
        }
        try {
            SystemParameterInfo info = new SystemParameterInfo();
            info.setParameterName(name);
            info.setParameterValue(value);
            systemParameterInfoMapper.updateByName(info);

            //使用kafka广播, ACS收到消息后更新本地缓存
            kafkaProducer.sendMessageForTopic("system-parameter", name, value);
            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        }
        catch (Exception e)
        {
            logger.error("updateParameter exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.UPDATE_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.UPDATE_ERROR.getResultMsg());
        }

        logger.info("End invoke updateParameter:{}", retMap);
        return retMap;
    }

    /**
     * 查询参数
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> searchParameter(Map<String, Object> parameter) {
        logger.info("Start invoke searchParameter:{}", parameter);
        String name = null != parameter.get("name") ? parameter.get("name").toString() : "";
        Map<String, Object> retMap = new HashMap<>();
        if(StringUtils.isBlank(name))
        {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultMsg());
            return retMap;
        }
        try {
            SystemParameterInfo info = systemParameterInfoMapper.searchByName(name);
            if(null == info)
            {
                retMap.put(Constant.CODE, -1);
                retMap.put(Constant.MESSAGE, "未查询到相关参数!");
                return retMap;
            }

            retMap.put("name", name);

            //值为空查询默认值
            if(StringUtils.isBlank(info.getParameterValue()))
            {
                retMap.put("value", info.getParameterDefaultValue());
            }
            else
            {
                retMap.put("value", info.getParameterValue());
            }
            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
        }
        catch (Exception e)
        {
            logger.error("updateParameter exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }

        logger.info("End invoke searchParameter:{}", retMap);
        return retMap;
    }
}
