package com.cmiot.rms.services;

import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.Inform;

import java.util.List;
import java.util.Map;

/**
 * 提供给acs调用接口
 * Created by panmingguo on 2016/5/10.
 */
public interface AcsInterfaceService {

    /**
     * ACS上报接口
     * @param abstractMethod
     * @return
     */
    Map<String, Object> reportInfo(AbstractMethod abstractMethod);

    /**
     * OUI-SN验证接口
     * @param parameter
     * @return
     */
    Map<String, Object> checkOuiSn(Map<String, Object> parameter);


    /**
     * password验证接口
     * @param parameter
     * @return
     */
    Map<String, Object> checkPassword(Map<String, Object> parameter);

    /**
     * 通过OUI-SN查询Digest账号
     * @param parameter
     * @return
     */
    Map<String, Object> queryDigestAccount(Map<String, Object> parameter);


    /**
     * 根据用户名查询digest密码
     * @param userName
     * @return
     */
    List<String> queryDigestPassword(String userName);


    /**
     * 网关首次上报ACS从RMS获取需要下发的指令
     * @param parameter
     * @return
     */
    List<AbstractMethod> getbootStrapInstructions(Map<String, Object> parameter);

    /**
     * 当rms为导入网关基础信息时，主动发现
     * @param inform
     * @return
     */
    Boolean createGatewayInfo(Inform inform);

    /**
     * 根据OUI—SN查询网关的Digest的账号和密码
     * @param parameter
     * @return
     */
    Map<String, Object> queryDigestAccAndPw(Map<String, Object> parameter);
}
