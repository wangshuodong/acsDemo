package com.cmiot.rms.services;

import com.cmiot.acs.model.AbstractMethod;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/6/14.
 */
public interface BoxInterfaceService {

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
     * 根据用户名查询digest密码
     * @param userName
     * @return
     */
    List<String> queryDigestPassword(String userName);


    /**
     * 机顶盒首次上报ACS从RMS获取需要下发的指令
     * @param parameter
     * @return
     */
    List<AbstractMethod> getbootStrapInstructions(Map<String, Object> parameter);


    /**
     * 根据OUI—SN查询机顶盒的Digest的账号和密码
     * @param parameter
     * @return
     */
    Map<String, Object> queryDigestAccAndPw(Map<String, Object> parameter);

}
