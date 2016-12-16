package com.cmiot.acs.integration;

import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.cmiot.acs.common.ApplicationContextUtil;
import com.cmiot.acs.common.CommonUtil;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.Inform;
import com.cmiot.rms.services.BoxInterfaceService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zjial on 2016/6/14.
 */
public class BoxRmsIntegration {
    public static Logger logger = LoggerFactory.getLogger(BoxRmsIntegration.class);
    private static BoxInterfaceService boxInterfaceService;

    public void setSetSetBoxInterfaceService(BoxInterfaceService setSetBoxInterfaceService) {
        this.boxInterfaceService = setSetBoxInterfaceService;
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
            logger.info("【GID={}】异步BOX上报信息到RMS,入参:{}", gid, abstractMethod.getMethodName());
            if (StringUtils.isNotBlank(abstractMethod.getCallbackUrl())) {
                ReferenceBean<BoxInterfaceService> referenceBean = new ReferenceBean<>();
                referenceBean.setApplicationContext(ApplicationContextUtil.getSpringFactory());
                referenceBean.setInterface(BoxInterfaceService.class);
                referenceBean.setUrl(abstractMethod.getCallbackUrl());
                referenceBean.afterPropertiesSet();
                BoxInterfaceService laBoxInterfaceService = referenceBean.get();
                laBoxInterfaceService.reportInfo(abstractMethod);
            } else {
                boxInterfaceService.reportInfo(abstractMethod);
            }
            logger.info("【GID={}】异步BOX上报信息到RMS,出参:{}", gid, "成功");
        } catch (Exception e) {
            logger.info("【GID={}】异步BOX上报信息到RMS,异常:{}", gid, e);
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
            logger.info("【GID={}】BOX 获取摘要认真密码,入参：{}", gid, parameters);
            objectMap = boxInterfaceService.queryDigestAccAndPw(parameters);
            logger.info("【GID={}】BOX 获取摘要认真密码,出参：{}", gid, objectMap);
        } catch (Exception e) {
            logger.info("【GID={}】BOX 获取摘要认真密码,异常:{}", e);
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
            logger.info("【GID={}】BOX验证OuiSn,入参:{}", gid, parameter);
            Map<String, Object> checkOuiSnMap = boxInterfaceService.checkOuiSn(parameter);
            logger.info("【GID={}】BOX验证OuiSn,出参:{}", gid, checkOuiSnMap);
            bool = CommonUtil.judgeResultCode(checkOuiSnMap);
        } catch (Exception e) {
            logger.info("【GID={}】BOX验证OuiSn异常：{}", gid, e);
        }
        return bool;
    }


    /**
     * BOX获取首次上报参数配置
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
            Map<String, Object> parameter = new HashMap<>();
            parameter.put("sn", sn);
            parameter.put("oui", oui);
            logger.info("【GID={}】BOX获取首次上报参数配置，入参:{}", gid, parameter);
            abstractMethodList = boxInterfaceService.getbootStrapInstructions(parameter);
            logger.info("【GID={}】BOX获取首次上报参数配置，出参:{}", gid, abstractMethodList);
        } catch (Exception e) {
            logger.info("【GID={}】BOX获取首次上报参数配置，异常：{}", gid, e);
        }
        return abstractMethodList;
    }

}
