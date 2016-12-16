package com.cmiot.rms.services.impl;

import com.alibaba.fastjson.JSON;
import com.cmiot.rms.common.constant.Constant;
import com.cmiot.rms.common.enums.CategoryEnum;
import com.cmiot.rms.common.enums.LogTypeEnum;
import com.cmiot.rms.common.enums.RespCodeEnum;
import com.cmiot.rms.common.utils.UniqueUtil;
import com.cmiot.rms.dao.mapper.DeviceInfoMapper;
import com.cmiot.rms.dao.mapper.FactoryMapper;
import com.cmiot.rms.dao.mapper.GatewayInfoMapper;
import com.cmiot.rms.dao.mapper.HardwareAblityMapper;
import com.cmiot.rms.dao.mapper.ManufacturerMapper;
import com.cmiot.rms.dao.model.DeviceInfo;
import com.cmiot.rms.dao.model.Factory;
import com.cmiot.rms.dao.model.GatewayInfo;
import com.cmiot.rms.dao.model.HardwareAblity;
import com.cmiot.rms.dao.model.Manufacturer;
import com.cmiot.rms.dao.model.derivedclass.DeviceBean;
import com.cmiot.rms.services.DeviceTypeManagerService;
import com.cmiot.rms.services.LogManagerService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备类型管理实现
 * Created by wangzhen on 2016/4/15.
 */
public class DeviceTypeManagerServiceImpl implements DeviceTypeManagerService {

    private static Logger logger = LoggerFactory.getLogger(LogManagerServiceImpl.class);

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Autowired
    private FactoryMapper factoryMapper;

    @Autowired
    private ManufacturerMapper manufacturerMapper;

    @Autowired
    private LogManagerService logManagerService;

    @Autowired
    private GatewayInfoMapper gatewayInfoMapper;

    @Autowired
    private HardwareAblityMapper hardwareAblityMapper;

    @Override
    public Map<String, Object> queryDeviceTypeInfo(Map<String, Object> parameter) {
        logger.info("设备类型查询方法");
        String deviceFactory = null != parameter.get("deviceFactory") ? String.valueOf(parameter.get("deviceFactory")):"";
        String deviceManufacturer = null != parameter.get("deviceManufacturer") ? String.valueOf(parameter.get("deviceManufacturer")) :"";

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setDeviceFactory(deviceFactory);
        deviceInfo.setDeviceManufacturer(deviceManufacturer);
        int page = (null != parameter.get("page")) ? Integer.valueOf(parameter.get("page").toString()) : 1;
        int pageSize = (null != parameter.get("pageSize")) ? Integer.valueOf(parameter.get("pageSize").toString()) : 10;

        PageHelper.startPage(page, pageSize);
        List<DeviceBean> result = deviceInfoMapper.queryDeviceInfoList(deviceInfo);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
        resultMap.put(Constant.MESSAGE, "");
        resultMap.put("total", ((Page) result).getTotal());
        resultMap.put("page", page);
        resultMap.put("pageSize", pageSize);
        resultMap.put(Constant.DATA, JSON.toJSON(result));
        return resultMap;
    }

    @Override
    public Map<String, Object> addDeviceTypeInfo(Map<String, Object> parameter) {
        logger.info("设备类型添加方法");
        Map<String, Object> resultMap = new HashMap<>();
        DeviceInfo deviceInfo = new DeviceInfo();

        Field[] fields = deviceInfo.getClass().getDeclaredFields();
        // Map转对象
        for (Field field : fields) {
            field.setAccessible(true); //设置些属性是可以访问的
            String name = field.getName();
            logger.info("DeviceInfo对象属性：" + name);
            if (parameter.containsKey(name)) {
                try {
                    logger.info("DeviceInfo对象属性值：" + parameter.get(field.getName()));
                    field.set(deviceInfo, parameter.get(field.getName()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                    resultMap.put(Constant.MESSAGE, "设备类型添加异常");
                    resultMap.put(Constant.DATA, false);
                }
            }
        }
        deviceInfo.setId(UniqueUtil.uuid());
        logger.info("添加传入数据" + JSON.toJSONString(deviceInfo));

        List<DeviceInfo> list= deviceInfoMapper.searchDeviceModel(deviceInfo);
        if(list !=null && list.size()>0){
            resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
            resultMap.put(Constant.MESSAGE, "对应型号已存在");
            resultMap.put(Constant.DATA, false);
            return resultMap;
        }else{
            int i = deviceInfoMapper.insertSelective(deviceInfo);
            if (i == 0) {
                resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                resultMap.put(Constant.MESSAGE, "新增设备失败");
                resultMap.put(Constant.DATA, false);
                return resultMap;
            }
            //记录硬件能力信息
            HardwareAblity ha = new HardwareAblity();
            ha.setHardwareAblityUuid(UniqueUtil.uuid());
            ha.setDeviceId(deviceInfo.getId());
            ha.setGatewayForm(parameter.get("gatewayForm")==null?"":parameter.get("gatewayForm").toString());
            ha.setHardwareAblityIpv4v6(parameter.get("ipV4V6")==null?"":parameter.get("ipV4V6").toString());
            ha.setHardwareAblityLanCount( (parameter.get("lanCount")==null || "".equals(parameter.get("lanCount").toString()))?0: Integer.parseInt(parameter.get("lanCount").toString()));
            boolean supportWifi = false;
            if(parameter.get("supportWifi") != null && "1".equals(parameter.get("supportWifi").toString())){
            	supportWifi = true;
            }
            ha.setHardwareAblitySupportWifi(supportWifi);

            ha.setHardwareAblitySupportWifi24ghz(parameter.get("supportWifi24GHz")==null?"":parameter.get("supportWifi24GHz").toString());
            ha.setHardwareAblitySupportWifi58ghz(parameter.get("supportWifi58GHz")==null?"":parameter.get("supportWifi58GHz").toString());
            ha.setHardwareAblityUsbCount( (parameter.get("usbCount")==null || "".equals(parameter.get("usbCount").toString()))?0: Integer.parseInt(parameter.get("usbCount").toString()));
            ha.setHardwareAblityWifiCount((parameter.get("wifiCount")==null || "".equals(parameter.get("wifiCount").toString()))?0: Integer.parseInt(parameter.get("wifiCount").toString()));
            ha.setHardwareAblityWifiLoc(parameter.get("wifiLoc")==null?"":parameter.get("wifiLoc").toString());
            ha.setHardwareAblityWifiSize(parameter.get("wifiSize")==null?"":parameter.get("wifiSize").toString());
            ha.setGatewayOSVersion(parameter.get("OSVersion")==null?"":parameter.get("OSVersion").toString());
            ha.setGatewayOSType(parameter.get("OSType")==null?"":parameter.get("OSType").toString());

            hardwareAblityMapper.insert(ha);


            Map<String, Object> parameterLog = new HashMap<>();
            // 操作的数据内容
            parameterLog.put("content", JSON.toJSONString(deviceInfo));
            // 登录用户名称
            parameterLog.put("userName", parameter.get("userName"));
            // 类目ID(菜单ID)
            parameterLog.put("categoryMenu", CategoryEnum.GATEWAY_MANAGER_SERVICE.name());
            // 具体的操作
            parameterLog.put("operation", "添加设备类型");
            // 角色名称
            parameterLog.put("roleName", parameter.get("roleName"));
            // 类目名称
            parameterLog.put("categoryMenuName", CategoryEnum.GATEWAY_MANAGER_SERVICE.description());
            // 日志类型
            parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
            logManagerService.recordOperationLog(parameterLog);
            resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
            resultMap.put(Constant.MESSAGE, "");
            resultMap.put(Constant.DATA, true);
            return resultMap;
        }
    }

    @Override
    public Map<String, Object> initUpdateDeviceTypeInfo(Map<String, Object> parameter) {
        Map<String, Object> resultMap = new HashMap<>();
        String deviceId = (String) parameter.get("deviceId");
        DeviceBean deviceBean = deviceInfoMapper.selectByDeviceId(deviceId);
        if (deviceBean == null) {
            logger.info("未查询到修改的数据；传入ID：" + deviceId);
            resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
            resultMap.put(Constant.MESSAGE, "未查询到修改的数据");
            resultMap.put(Constant.DATA, "");
            return resultMap;
        }

        // 获取全部制造商
        List<Manufacturer> manufacturerList = manufacturerMapper.queryAll();
        deviceBean.setManufacturerList(manufacturerList);
        // 根据当前的制造商获取生产商
        String manufacturerId = null != deviceBean.getDeviceType() ? deviceBean.getDeviceType() : null;
        List<Factory> factorylist = factoryMapper.queryForManufacturerId(manufacturerId);
        deviceBean.setFactoryList(factorylist);

        //查询硬件能力
        HardwareAblity ha =	hardwareAblityMapper.selectByDeviceUuid(deviceBean.getId());
        deviceBean.setHardwareAblity(ha);

        logger.info("修改初始化数据" + JSON.toJSONString(deviceBean));
        resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
        resultMap.put(Constant.MESSAGE, "");
        resultMap.put(Constant.DATA, JSON.toJSON(deviceBean));
        return resultMap;
    }

    @Override
    public Map<String, Object> updateDeviceTypeInfo(Map<String, Object> parameter) {
        Map<String, Object> resultMap = new HashMap<>();
        DeviceInfo deviceInfo = new DeviceInfo();

        Field[] fields = deviceInfo.getClass().getDeclaredFields();
        // Map转对象
        for (Field field : fields) {
            field.setAccessible(true); //设置些属性是可以访问的
            String name = field.getName();
            logger.info("修改DeviceInfo对象属性：" + name);
            if (parameter.containsKey(name)) {
                try {
                    logger.info("修改DeviceInfo对象属性值：" + parameter.get(field.getName()));
                    field.set(deviceInfo, parameter.get(field.getName()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                    resultMap.put(Constant.MESSAGE, "设备类型修改异常");
                    resultMap.put(Constant.DATA, false);
                }
            }
        }

        List<DeviceInfo> list = deviceInfoMapper.searchDeviceModel(deviceInfo);
        if (list != null && list.size() > 0) {
            for(DeviceInfo dInfo:list){
                if(!dInfo.getId().equals(deviceInfo.getId())){
                    resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                    resultMap.put(Constant.MESSAGE, "对应生产商的型号已存在,不能修改");
                    resultMap.put(Constant.DATA, false);
                    return resultMap;
                }
            }
        }
        int i = deviceInfoMapper.updateByPrimaryKeySelective(deviceInfo);
        if (i == 0) {
            logger.info("修改传入数据" + JSON.toJSONString(deviceInfo));
            resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
            resultMap.put(Constant.MESSAGE, "设备类型修改失败");
            resultMap.put(Constant.DATA, false);
            return resultMap;
        }
        //查询设备能力
        //记录硬件能力信息
        	HardwareAblity ha = hardwareAblityMapper.selectByDeviceUuid(deviceInfo.getId());
            if(ha != null){

            	ha.setGatewayForm(parameter.get("gatewayForm")==null?"":parameter.get("gatewayForm").toString());
            	ha.setHardwareAblityIpv4v6(parameter.get("ipV4V6")==null?"":parameter.get("ipV4V6").toString());
            	ha.setHardwareAblityLanCount( (parameter.get("lanCount")==null || "".equals(parameter.get("lanCount").toString()))?0: Integer.parseInt(parameter.get("lanCount").toString()));
            	boolean supportWifi = false;
            	if(parameter.get("supportWifi") != null && "1".equals(parameter.get("supportWifi").toString())){
            		supportWifi = true;
            	}
            	ha.setHardwareAblitySupportWifi(supportWifi);
            	ha.setHardwareAblitySupportWifi24ghz(parameter.get("supportWifi24GHz")==null?"":parameter.get("supportWifi24GHz").toString());
            	ha.setHardwareAblitySupportWifi58ghz(parameter.get("supportWifi58GHz")==null?"":parameter.get("supportWifi58GHz").toString());
            	ha.setHardwareAblityUsbCount( (parameter.get("usbCount")==null || "".equals(parameter.get("usbCount").toString()))?0: Integer.parseInt(parameter.get("usbCount").toString()));
            	ha.setHardwareAblityWifiCount((parameter.get("wifiCount")==null || "".equals(parameter.get("wifiCount").toString()))?0: Integer.parseInt(parameter.get("wifiCount").toString()));
            	ha.setHardwareAblityWifiLoc(parameter.get("wifiLoc")==null?"":parameter.get("wifiLoc").toString());
            	ha.setHardwareAblityWifiSize(parameter.get("wifiSize")==null?"":parameter.get("wifiSize").toString());
            	ha.setGatewayOSVersion(parameter.get("OSVersion")==null?"":parameter.get("OSVersion").toString());
                ha.setGatewayOSType(parameter.get("OSType")==null?"":parameter.get("OSType").toString());

            	hardwareAblityMapper.updateByPrimaryKey(ha);
            }else{
            	ha = new HardwareAblity();
            	ha.setHardwareAblityUuid(UniqueUtil.uuid());
            	ha.setDeviceId(deviceInfo.getId());
            	ha.setGatewayForm(parameter.get("gatewayForm")==null?"":parameter.get("gatewayForm").toString());
            	ha.setHardwareAblityIpv4v6(parameter.get("ipV4V6")==null?"":parameter.get("ipV4V6").toString());
            	ha.setHardwareAblityLanCount( (parameter.get("lanCount")==null || "".equals(parameter.get("lanCount").toString()))?0: Integer.parseInt(parameter.get("lanCount").toString()));
            	boolean supportWifi = false;
            	if(parameter.get("supportWifi") != null && "1".equals(parameter.get("supportWifi").toString())){
            		supportWifi = true;
            	}
            	ha.setHardwareAblitySupportWifi(supportWifi);

            	ha.setHardwareAblitySupportWifi24ghz(parameter.get("supportWifi24GHz")==null?"":parameter.get("supportWifi24GHz").toString());
            	ha.setHardwareAblitySupportWifi58ghz(parameter.get("supportWifi58GHz")==null?"":parameter.get("supportWifi58GHz").toString());
            	ha.setHardwareAblityUsbCount( (parameter.get("usbCount")==null || "".equals(parameter.get("usbCount").toString()))?0: Integer.parseInt(parameter.get("usbCount").toString()));
            	ha.setHardwareAblityWifiCount((parameter.get("wifiCount")==null || "".equals(parameter.get("wifiCount").toString()))?0: Integer.parseInt(parameter.get("wifiCount").toString()));
            	ha.setHardwareAblityWifiLoc(parameter.get("wifiLoc")==null?"":parameter.get("wifiLoc").toString());
            	ha.setHardwareAblityWifiSize(parameter.get("wifiSize")==null?"":parameter.get("wifiSize").toString());
            	ha.setGatewayOSVersion(parameter.get("OSVersion")==null?"":parameter.get("OSVersion").toString());
                ha.setGatewayOSType(parameter.get("OSType")==null?"":parameter.get("OSType").toString());

            	hardwareAblityMapper.insert(ha);
            }

        Map<String, Object> parameterLog = new HashMap<>();
        // 操作的数据内容
        parameterLog.put("content", JSON.toJSONString(deviceInfo));
        // 登录用户名称
        parameterLog.put("userName", parameter.get("userName"));
        // 类目ID(菜单ID)
        parameterLog.put("categoryMenu", CategoryEnum.GATEWAY_MANAGER_SERVICE.name());
        // 具体的操作
        parameterLog.put("operation", "修改设备类型");
        // 角色名称
        parameterLog.put("roleName", parameter.get("roleName"));
        // 类目名称
        parameterLog.put("categoryMenuName", CategoryEnum.GATEWAY_MANAGER_SERVICE.description());
        // 日志类型
        parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
        logManagerService.recordOperationLog(parameterLog);
        logger.info("修改传入数据" + JSON.toJSONString(deviceInfo));
        resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
        resultMap.put(Constant.MESSAGE, "设备类型修改成功");
        resultMap.put(Constant.DATA, true);
        return resultMap;
    }

    @Override
    public Map<String, Object> detailDeviceTypeInfo(Map<String, Object> parameter) {
        Map<String, Object> resultMap = new HashMap<>();
        String deviceId = (String) parameter.get("deviceId");
        DeviceBean deviceBean = deviceInfoMapper.selectByDeviceId(deviceId);
        if (deviceBean == null) {
            logger.info("没有查询到设备信息，设备类型ID" + deviceId);
            resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
            resultMap.put(Constant.MESSAGE, "没有查询到设备信息");
            resultMap.put(Constant.DATA, JSON.toJSON(deviceBean));
            return resultMap;
        }
        //查询硬件能力
        HardwareAblity ha = hardwareAblityMapper.selectByDeviceUuid(deviceId);
        deviceBean.setHardwareAblity(ha);
        logger.info("设备类型查看" + JSON.toJSONString(deviceBean));
        resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
        resultMap.put(Constant.MESSAGE, "");
        resultMap.put(Constant.DATA, JSON.toJSON(deviceBean));
        return resultMap;
    }

    @Override
    public Map<String, Object> deleteDeviceTypeInfo(Map<String, Object> parameter) {
        Map<String, Object> resultMap = new HashMap<>();
        DeviceInfo deviceInfo = new DeviceInfo();
        String deviceId = (String) parameter.get("deviceId");
        //查询设备型号对应的网关是否存在如果存在不能删除

        GatewayInfo gatewayInfo=new GatewayInfo();
        if (StringUtils.isNotEmpty(deviceId)) {
            gatewayInfo.setGatewayDeviceUuid(deviceId);

            List<GatewayInfo> list= gatewayInfoMapper.queryList(gatewayInfo);
            if(list !=null && list.size()>0){
                resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                resultMap.put(Constant.MESSAGE, "该设备型号的网关存在，不能删除!");
                resultMap.put(Constant.DATA, false);
                return resultMap;
            }else{
                int i = deviceInfoMapper.deleteByPrimaryKey(deviceId);
                if (i == 0) {
                    logger.info("设备删除" + JSON.toJSONString(deviceInfo));
                    resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
                    resultMap.put(Constant.MESSAGE, "设备类型删除失败");
                    resultMap.put(Constant.DATA, false);
                    return resultMap;
                }
                Map<String, Object> parameterLog = new HashMap<>();
                // 操作的数据内容
                parameterLog.put("content", JSON.toJSONString(deviceInfo));
                // 登录用户名称
                parameterLog.put("userName", parameter.get("userName"));
                // 类目ID(菜单ID)
                parameterLog.put("categoryMenu", CategoryEnum.GATEWAY_MANAGER_SERVICE.name());
                // 具体的操作
                parameterLog.put("operation", "删除设备类型");
                // 角色名称
                parameterLog.put("roleName", parameter.get("roleName"));
                // 类目名称
                parameterLog.put("categoryMenuName", CategoryEnum.GATEWAY_MANAGER_SERVICE.description());
                // 日志类型
                parameterLog.put("logType", LogTypeEnum.LOG_TYPE_OPERATION.code());
                logManagerService.recordOperationLog(parameterLog);
                logger.info("设备删除" + JSON.toJSONString(deviceInfo));
                resultMap.put(Constant.CODE, RespCodeEnum.RC_0.code());
                resultMap.put(Constant.MESSAGE, "设备类型删除测通");
                resultMap.put(Constant.DATA, true);
                return resultMap;
            }
        }
        resultMap.put(Constant.CODE, RespCodeEnum.RC_1.code());
        resultMap.put(Constant.MESSAGE, "设备ID不能为空");
        resultMap.put(Constant.DATA, false);
        return resultMap;
    }

}
