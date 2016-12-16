package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.HdVersionInfoMapper;
import com.cmiot.rms.dao.model.HdVersionInfo;
import com.cmiot.rms.services.HdVersionService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/8/10.
 */
@Service
public class HdVersionServiceImpl implements HdVersionService {

    private Logger logger = LoggerFactory.getLogger(HdVersionServiceImpl.class);

    @Autowired
    private HdVersionInfoMapper hdVersionInfoMapper;

    @Override
    public Map<String, Object> searchHdVersion(Map<String, Object> parameter) {

        logger.info("start invoke HdVersionServiceImpl.searchHdVersion,parameter:{}",parameter);
        Map<String, Object> retMap = new HashMap<String, Object>();
        try{
            String deviceId = (String) parameter.get("deviceId");
            if(StringUtils.isEmpty(deviceId)){
                logger.error("invoke HdVersionServiceImpl.searchHdVersion，deviceId为空");
                retMap.put(Constant.CODE, "10000");
                retMap.put(Constant.MESSAGE, "deviceId为空");
                return retMap;
            }
            HdVersionInfo paramBean = new HdVersionInfo();
            paramBean.setDeviceId(deviceId);
            List<HdVersionInfo> list = hdVersionInfoMapper.queryByParam(paramBean);
            List<Map<String,Object>> dataList = new ArrayList<>();
            if(null!=list&&list.size()>0){
                for (HdVersionInfo hd:list){
                    Map<String,Object> map = new HashMap<>();
                    map.put(hd.getId(),hd.getHdVersion());
                    dataList.add(map);
                }
            }
            retMap.put(Constant.CODE, 0);
            retMap.put(Constant.MESSAGE, "成功");
            retMap.put("data",dataList);
            return retMap;
        } catch (Exception e) {
            logger.error("查询硬件信息异常：",e);
            retMap.put(Constant.CODE, 10005);
            retMap.put(Constant.MESSAGE, "服务器内部错误");
            return retMap;
        }
    }

    @Override
    public Map<String, Object> addHardVersion(Map<String, Object> parameter) {
        logger.info("start invoke HdVersionServiceImpl.addHardVesion,parameter:{}",parameter);
        Map<String, Object> retMap = new HashMap<String, Object>();
        try{
            String deviceId = (String) parameter.get("deviceId");
            if(StringUtils.isEmpty(deviceId)){
                logger.error("invoke HdVersionServiceImpl.addHardVesion，deviceId为空");
                retMap.put(Constant.CODE, 10000);
                retMap.put(Constant.MESSAGE, "deviceId为空");
                return retMap;
            }
            String hdVersion = (String) parameter.get("hdVersion");
            if(StringUtils.isEmpty(hdVersion)){
                logger.error("invoke HdVersionServiceImpl.addHardVesion，hdVersion为空");
                retMap.put(Constant.CODE, 10000);
                retMap.put(Constant.MESSAGE, "hdVersion为空");
                return retMap;
            }
            String deviceModel = (String) parameter.get("deviceModel");
            if(StringUtils.isEmpty(deviceModel)){
                logger.error("invoke HdVersionServiceImpl.addHardVesion，deviceModel为空");
                retMap.put(Constant.CODE, 10000);
                retMap.put(Constant.MESSAGE, "deviceModel为空");
                return retMap;
            }
            String description = (String) parameter.get("description");

            HdVersionInfo queryBean = new HdVersionInfo();
            queryBean.setDeviceId(deviceId);
            queryBean.setHdVersion(hdVersion);
            List<HdVersionInfo> list = hdVersionInfoMapper.queryByParam(queryBean);
            if(null!=list&&list.size()>0){
                logger.error("invoke HdVersionServiceImpl.addHardVesion，不能重复添加");
                retMap.put(Constant.CODE, 10002);
                retMap.put(Constant.MESSAGE, "同一设备型号不能重复添加相同的硬件版本");
                return retMap;
            }

            HdVersionInfo paramBean = new HdVersionInfo();
            paramBean.setDeviceId(deviceId);
            String hdVersionId = UniqueUtil.uuid();
            paramBean.setId(hdVersionId);
            paramBean.setCreateTime(System.currentTimeMillis()+"");
            paramBean.setDescription(description);
            paramBean.setDeviceModel(deviceModel);
            paramBean.setHdVersion(hdVersion);

            int n = hdVersionInfoMapper.insertSelective(paramBean);

            if(n>=1) {
                retMap.put(Constant.CODE, 0);
                retMap.put(Constant.MESSAGE, "成功");
                retMap.put("hdVersionId", hdVersionId);
            }else {
                retMap.put(Constant.CODE, 10002);
                retMap.put(Constant.MESSAGE, "添加失败");
            }
            return retMap;
        } catch (Exception e) {
            logger.error("新增硬件信息异常：",e);
            retMap.put(Constant.CODE, 10005);
            retMap.put(Constant.MESSAGE, "服务器内部错误");
            return retMap;
        }
    }

    @Override
    public Map<String, Object> updateHardVersion(Map<String, Object> parameter) {
        logger.info("start invoke HdVersionServiceImpl.updateHardVersion,parameter:{}",parameter);
        Map<String, Object> retMap = new HashMap<String, Object>();
        try{
            String deviceId = (String) parameter.get("deviceId");
            String hdVersion = (String) parameter.get("hdVersion");
            String deviceModel = (String) parameter.get("deviceModel");
            String description = (String) parameter.get("description");

            String hdVersionId = (String) parameter.get("hdVersionId");
            if(StringUtils.isEmpty(hdVersionId)){
                logger.error("invoke HdVersionServiceImpl.updateHardVersion，hdVersionId为空");
                retMap.put(Constant.CODE, 10000);
                retMap.put(Constant.MESSAGE, "hdVersionId为空");
                return retMap;
            }

            HdVersionInfo paramBean = new HdVersionInfo();

            HdVersionInfo paramBeanResult = hdVersionInfoMapper.selectByPrimaryKey(hdVersionId);
            if(null == paramBeanResult){
                logger.error("invoke HdVersionServiceImpl.updateHardVersion，hdVersionId查询不到硬件版本信息，hdVersionId：" + hdVersionId);
                retMap.put(Constant.CODE, 10000);
                retMap.put(Constant.MESSAGE, "hdVersionId错误");
                return retMap;
            }
            if((!StringUtils.isEmpty(deviceId)) || (!StringUtils.isEmpty(hdVersion))){
                    HdVersionInfo paramBean_ = new HdVersionInfo();
                    if(!StringUtils.isEmpty(deviceId)){
                        paramBean_.setDeviceId(deviceId);
                    }else{
                        paramBean_.setDeviceId(paramBeanResult.getDeviceId());
                    }

                    if(!StringUtils.isEmpty(hdVersion)){
                        paramBean_.setHdVersion(hdVersion);
                    }else{
                        paramBean_.setHdVersion(paramBeanResult.getHdVersion());
                    }
                    List<HdVersionInfo> list = hdVersionInfoMapper.queryByParam(paramBean_);
                    if(list != null && list.size() >0 && !list.get(0).getId().equals(hdVersionId)){
                        logger.error("invoke HdVersionServiceImpl.updateHardVersion，已有相同的硬件版本信息");
                        retMap.put(Constant.CODE, 10002);
                        retMap.put(Constant.MESSAGE, "已有相同的硬件版本信息");
                        return retMap;
                    }
                }


            paramBean.setId(hdVersionId);
            if(!StringUtils.isEmpty(deviceId)){
                paramBean.setDeviceId(deviceId);
            }

            if(!StringUtils.isEmpty(hdVersion)){
                paramBean.setHdVersion(hdVersion);
            }
            if(!StringUtils.isEmpty(deviceModel)){
                paramBean.setDeviceModel(deviceModel);
            }
            
            paramBean.setDescription(description);


            int n = hdVersionInfoMapper.updateByPrimaryKeySelective(paramBean);

            if(n>=1) {
                retMap.put(Constant.CODE, 0);
                retMap.put(Constant.MESSAGE, "成功");
                retMap.put("hdVersionId", hdVersionId);
            }else {
                retMap.put(Constant.CODE, 10003);
                retMap.put(Constant.MESSAGE, "更新失败");
            }
            return retMap;
        } catch (Exception e) {
            logger.error("更新硬件信息异常：",e);
            retMap.put(Constant.CODE, 10005);
            retMap.put(Constant.MESSAGE, "服务器内部错误");
            return retMap;
        }
    }

    @Override
    public Map<String, Object> deleteHardVersion(Map<String, Object> parameter) {
        logger.info("start invoke HdVersionServiceImpl.deleteHardVersion,parameter:{}",parameter);
        Map<String, Object> retMap = new HashMap<String, Object>();
        try{
            String id = (String) parameter.get("hdVersionId");
            if(StringUtils.isEmpty(id)){
                logger.error("invoke HdVersionServiceImpl.deleteHardVersion，id");
                retMap.put(Constant.CODE, 10000);
                retMap.put(Constant.MESSAGE, "id为空");
                return retMap;
            }

            int n = hdVersionInfoMapper.deleteByPrimaryKey(id);

            if(n>=1) {
                retMap.put(Constant.CODE, 0);
                retMap.put(Constant.MESSAGE, "成功");
            }else {
                retMap.put(Constant.CODE, 10004);
                retMap.put(Constant.MESSAGE, "删除失败");
            }
            return retMap;
        } catch (Exception e) {
            logger.error("更新硬件信息异常：",e);
            retMap.put(Constant.CODE, 10005);
            retMap.put(Constant.MESSAGE, "服务器内部错误");
            return retMap;
        }
    }

    @Override
    public Map<String, Object> queryHardVersion4Page(Map<String, Object> parameter) {
        logger.info("start invoke HdVersionServiceImpl.queryHardVersion4Page分页查询硬件信息，parameter:{}"+ JSON.toJSONString(parameter));

        Map<String, Object> retMap = new HashMap<String, Object>();

        try {

            int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
            int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;


            PageHelper.startPage(page, pageSize);

            List<Map<String, Object>> hdVersionList =  hdVersionInfoMapper.queryList4Page(parameter);

            List<Map<String,Object>> dataList = new ArrayList<>();
            for(Map<String,Object> map : hdVersionList){
                Map<String,Object> dataMap = new HashMap<>();
                dataMap.put("manufacturerID",map.get("manufacturerID"));
                dataMap.put("manufacturerName",map.get("manufacturerName"));
                dataMap.put("factoryID",map.get("factoryID"));
                dataMap.put("factoryName",map.get("factoryName"));
                dataMap.put("deviceModel",map.get("deviceModel"));
                dataMap.put("deviceId",map.get("deviceId"));
                dataMap.put("hdVersionId",map.get("hdVersionId"));
                dataMap.put("hdVersion",map.get("hdVersion"));
                dataMap.put("description",map.get("description"));
                dataList.add(dataMap);
            }

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
            resultMap.put(Constant.MESSAGE, "查询成功");
            resultMap.put("total", ((Page)hdVersionList).getTotal());
            resultMap.put("page", page);
            resultMap.put("pageSize", pageSize);
            resultMap.put(Constant.DATA, dataList);
            return resultMap;

        } catch (Exception e) {
            logger.error("分页查询业务类异常：", e);
            retMap.put(Constant.CODE, RespCodeEnum.RC_1001.code());
            retMap.put(Constant.MESSAGE, "服务器内部错误");
            return retMap;
        }
    }
}
