package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.ErrorCodeEnum;
import com.cmiot.rms.common.utils.StringLocalUtils;
import com.cmiot.rms.dao.mapper.BoxDeviceInfoMapper;
import com.cmiot.rms.dao.mapper.BoxFactoryInfoMapper;
import com.cmiot.rms.dao.mapper.BoxInfoMapper;
import com.cmiot.rms.dao.model.BoxDeviceInfo;
import com.cmiot.rms.dao.model.BoxFactoryInfo;
import com.cmiot.rms.dao.model.BoxInfo;
import com.cmiot.rms.services.BoxCommonSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by panmingguo on 2016/6/15.
 */
public class BoxCommonSearchServiceImpl implements BoxCommonSearchService {

    private static Logger logger = LoggerFactory.getLogger(BoxCommonSearchServiceImpl.class);

    @Autowired
    BoxFactoryInfoMapper boxFactoryInfoMapper;

    @Autowired
    BoxInfoMapper boxInfoMapper;

    @Autowired
    BoxDeviceInfoMapper boxDeviceInfoMapper;
    /**
     * 查询生产商信息
     *
     * @return
     */
    @Override
    public Map<String, Object> queryFactoryInfo(Map<String, Object> parameter) {
        logger.info("Start invoke queryFactoryInfo！");
        Map<String, Object> retMap = new HashMap<>();
        try {
            String factoryCode = StringLocalUtils.ObjectToNull(parameter.get("factoryCode"));
            String factoryName = StringLocalUtils.ObjectToNull(parameter.get("factoryName"));
            String id = StringLocalUtils.ObjectToNull(parameter.get("id"));
            BoxFactoryInfo info = new BoxFactoryInfo();
            info.setFactoryCode(factoryCode);
            info.setFactoryName(factoryName);
            info.setId(id);

            List<BoxFactoryInfo> list = boxFactoryInfoMapper.queryList(info);
            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
            retMap.put(Constant.DATA, JSON.toJSON(list));
        } catch (Exception e) {
            logger.error("queryFactoryInfo exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }
        logger.info("End invoke queryFactoryInfo:{}", retMap);
        return retMap;
    }

    /**
     * 根据生产商编码查询设备型号
     *
     * @param parameter
     * @return
     */
    @Override
    public Map<String, Object> queryBoxModel(Map<String, Object> parameter) {
        logger.info("Start invoke queryBoxModel！");
        Map<String, Object> retMap = new HashMap<>();
        String factoryCode = StringLocalUtils.ObjectToNull(parameter.get("factoryCode"));
        String deviceId = StringLocalUtils.ObjectToNull(parameter.get("deviceId"));
        if (null == parameter || (null == factoryCode && null == deviceId)) {
            retMap.put(Constant.CODE, ErrorCodeEnum.PATAMETER_IS_NULL.getResultCode());
            retMap.put(Constant.MESSAGE, "请选择生产商");
            logger.info("End invoke queryBoxModel:{}", retMap);
            return retMap;
        }
        try {
            BoxDeviceInfo deviceInfo = new BoxDeviceInfo();
            deviceInfo.setFactoryCode(factoryCode);
            deviceInfo.setId(deviceId);
            List<Map<String, Object>> listMap = boxDeviceInfoMapper.selectBoxDeviceInfo(deviceInfo);
            Map<String, String> boxModelMap = new HashMap<>();
            for(int i = 0 ; i < listMap.size() ; i++)
            {
                boxModelMap.put((String) listMap.get(i).get("id"),(String) listMap.get(i).get("boxModel"));
            }
            retMap.put(Constant.CODE, ErrorCodeEnum.SUCCESS.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SUCCESS.getResultMsg());
            retMap.put(Constant.DATA, boxModelMap);
        } catch (Exception e) {
            logger.error("queryBoxModel exception:{}", e);
            retMap.put(Constant.CODE, ErrorCodeEnum.SEARCH_ERROR.getResultCode());
            retMap.put(Constant.MESSAGE, ErrorCodeEnum.SEARCH_ERROR.getResultMsg());
        }
        logger.info("End invoke queryBoxModel:{}", retMap);
        return retMap;
    }
}
