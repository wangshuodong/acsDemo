package com.cmiot.acs.integration;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.cmiot.acs.common.ApplicationContextUtil;
import com.cmiot.acs.common.CommonUtil;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.ParameterList;
import com.cmiot.acs.model.struct.ParameterValueStruct;
import com.cmiot.rms.services.AcsInterfaceService;
import com.cmiot.rms.services.SystemParameterConfigService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zjial on 2016/6/14.
 */
public class AcsRmsIntegration {

    public static Logger logger = LoggerFactory.getLogger(AcsRmsIntegration.class);

    private static AcsInterfaceService acsInterfaceService;

    private static SystemParameterConfigService systemParameterConfigService;

    public void setSetAcsInterfaceService(AcsInterfaceService setAcsInterfaceService) {
        this.acsInterfaceService = setAcsInterfaceService;
    }

    public void setSetSystemParameterConfigService(SystemParameterConfigService setSystemParameterConfigService) {
        this.systemParameterConfigService = setSystemParameterConfigService;
    }

    /**
     * 上报信息到RMS
     *
     * @param abstractMethod
     * @return
     */
    public static void reportInfo(AbstractMethod abstractMethod) {
        if (abstractMethod == null) return;
        String gid = abstractMethod.getGid();
        try {
            logger.info("【GID={}】异步上报信息到RMS，入参:{}", gid, abstractMethod.getMethodName());
            if (StringUtils.isNotBlank(abstractMethod.getCallbackUrl())) {
                ReferenceBean<AcsInterfaceService> referenceBean = new ReferenceBean<>();
                referenceBean.setApplicationContext(ApplicationContextUtil.getSpringFactory());
                referenceBean.setInterface(AcsInterfaceService.class);
                referenceBean.setUrl(abstractMethod.getCallbackUrl());
                referenceBean.afterPropertiesSet();
                AcsInterfaceService laAcsInterfaceService = referenceBean.get();
                laAcsInterfaceService.reportInfo(abstractMethod);
            } else {
                acsInterfaceService.reportInfo(abstractMethod);
            }
            logger.info("【GID={}】异步上报信息到RMS，出参:{}", gid, "成功");
        } catch (Exception e) {
            logger.info("【GID={}】异步上报信息到RMS，异常:{}", gid, e);
        }
    }


    /**
     * 获取摘要认真密码
     *
     * @param inform
     * @return
     */
    public static Map<String, Object> getDigestPassword(Inform inform) {
        String gid = inform.getGid();
        Map<String, Object> objectMap = null;
        try {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("oui", inform.getDeviceId().getOui());
            parameters.put("sn", inform.getDeviceId().getSerialNubmer());
            parameters.put("isFirst", false);
            logger.info("【GID={}】获取摘要认证密码，入参：{}", gid, parameters);
            objectMap = acsInterfaceService.queryDigestAccAndPw(parameters);
            logger.info("【GID={}】获取摘要认证密码，出参：{}", gid, objectMap);
        } catch (Exception e) {
            logger.info("【GID={}】获取摘要认证密码，异常:{}", gid, e);
        }
        return objectMap;
    }


    /**
     * 验证OuiSn
     *
     * @return
     */
    public static boolean checkOuiSn(Inform inform) {
        boolean bool = false;
        String gid = inform.getGid();
        try {
            String oui = inform.getDeviceId().getOui();
            String sn = inform.getDeviceId().getSerialNubmer();
            Map<String, Object> parameter = new HashMap<>();
            parameter.put("sn", sn);
            parameter.put("oui", oui);
            logger.info("【GID={}】验证OuiSn，入参:{}", gid, parameter);
            Map<String, Object> checkOuiSnMap = acsInterfaceService.checkOuiSn(parameter);
            logger.info("【GID={}】验证OuiSn，出参:{}", gid, checkOuiSnMap);
            bool = CommonUtil.judgeResultCode(checkOuiSnMap);
        } catch (Exception e) {
            logger.info("【GID={}】验证OuiSn，异常：{}", gid, e);
        }
        return bool;

    }


    /**
     * 验证Password
     *
     * @return
     */

    public static final String Password = "InternetGatewayDevice.X_CMCC_UserInfo.Password";
    private static final String PASSWORD_B = "InternetGatewayDevice.X_CMCC_UserInfo.UserName";

    public static boolean checkPassword(Inform inform) {
        String gid = inform.getGid();
        boolean checkPassword = false;
        try {
            // 2)从Inform里遍历出Password
            List<ParameterValueStruct> valueStructList = inform.getParameterList().getParameterValueStructs();
            String oui = inform.getDeviceId().getOui();
            String sn = inform.getDeviceId().getSerialNubmer();
            String password = (String) getNodeValue(valueStructList, Password);
            if (StringUtils.isBlank(password)) password = (String) getNodeValue(valueStructList, PASSWORD_B);
            // 3)判断Password是否为空
            if (StringUtils.isBlank(password)) {
                logger.info("【GID={}】{} 不能为空", gid, ParameterList.Password);
            } else {
                // 4)调用服务查询Password
                Map<String, Object> parameter = new HashMap<>();
                parameter.put("sn", sn);
                parameter.put("oui", oui);
                parameter.put("gatewayPassword", password);
                logger.info("【GID={}】验证Password，入参：{}", gid, parameter);
                Map<String, Object> checkPasswordMap = acsInterfaceService.checkPassword(parameter);
                logger.info("【GID={}】验证Password，出参：{}", gid, checkPasswordMap);
                checkPassword = CommonUtil.judgeResultCode(checkPasswordMap);
            }
        } catch (Exception e) {
            logger.info("【GID={}】验证Password，异常：{}", gid, e);
        }
        return checkPassword;
    }


    /**
     * 获取首次上报参数配置
     *
     * @param inform
     * @return
     */


    public static List<AbstractMethod> getBootStrapInstructions(Inform inform) {
        List<AbstractMethod> abstractMethodList = null;
        String gid = inform.getGid();
        try {
            String oui = inform.getDeviceId().getOui();
            String sn = inform.getDeviceId().getSerialNubmer();
            List<ParameterValueStruct> valueStructList = inform.getParameterList().getParameterValueStructs();
            Map<String, Object> parameter = new HashMap<>();
            parameter.put("sn", sn);
            parameter.put("oui", oui);
            parameter.put("url", getNodeValue(valueStructList, ParameterList.ConnectionRequestURL));
            parameter.put("hardwareVersion", getNodeValue(valueStructList, ParameterList.HardwareVersion));
            parameter.put("softwareVersion", getNodeValue(valueStructList, ParameterList.SoftwareVesion));
            logger.info("【GID={}】获取首次上报参数配置，入参:{}", gid, parameter);
            abstractMethodList = acsInterfaceService.getbootStrapInstructions(parameter);
            logger.info("【GID={}】获取首次上报参数配置，出参:{}", gid, abstractMethodList);
        } catch (Exception e) {
            logger.info("【GID={}】获取首次上报参数配置，异常：{}", gid, e);

        }
        return abstractMethodList;
    }

    /**
     * 主动发现设备
     *
     * @param inform
     * @return
     */
    public static Boolean createGatewayInfo(Inform inform) {
        boolean bool = false;
        String gid = inform.getGid();
        try {
            logger.info("【GID={}】主动发现设备，入参:{}", gid, inform);
            bool = acsInterfaceService.createGatewayInfo(inform);
            logger.info("【GID={}】主动发现设备，出参:{}", gid, bool);
        } catch (Exception e) {
            logger.info("【GID={}】主动发现设备，异常：{}", gid, e);

        }
        return bool;
    }


    /**
     * 根据节点名字获取节点值
     *
     * @param parameterValueStructList
     * @param nodeName
     * @return
     */
    private static Object getNodeValue(List<ParameterValueStruct> parameterValueStructList, String nodeName) {
        Object nodeValue = null;
        if (parameterValueStructList != null && parameterValueStructList.size() > 0)
            for (ParameterValueStruct valueStruct : parameterValueStructList) {
                if (valueStruct.getName().equalsIgnoreCase(nodeName)) {
                    nodeValue = valueStruct.getValue();
                    break;
                }
            }
        return nodeValue;
    }


    /**
     * 查询digest_switch认证状态
     */
    public static boolean queryDigestSwitch() {
        try {
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("name", "digest_switch");
            logger.info("查询digest_switch认证状态，入参:{}", objectMap);
            Map<String, Object> searchParameterMap = systemParameterConfigService.searchParameter(objectMap);
            logger.info("查询digest_switch认证状态，出参:{}", searchParameterMap);
            return Boolean.parseBoolean(searchParameterMap.get("value").toString());
        } catch (Exception e) {
            logger.info("查询digest_switch认证状态，异常：{}", e);
            return false;
        }

    }


}
