package com.cmiot.acs.facadeimpl;

import com.cmiot.acs.control.ACSProcessControl;
import com.cmiot.acs.control.ACSProcessControlManager;
import com.cmiot.acs.domain.cache.SpringRedisUtil;
import com.cmiot.acs.domain.lock.ProcessLock;
import com.cmiot.acs.model.AbstractMethod;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 同步执行收到的下发指令
 * Created by ZJL on 2016/11/4.
 */
public class SynchronousFacadeImpl extends AcsCpeFacade {

    public static Map<String, Object> synchronous(AbstractMethod method) {
        String requestId = method.getRequestId();
        String cpeId = method.getCpeId();
        String cpeUrl = method.getCpeUrl();
        String cpeUserName = method.getCpeUserName();
        String cpeUserPassword = method.getCpePassword();
        ACSProcessControl acsPcl = ACSProcessControlManager.getInstance().getProcessControl(cpeId);
        if (acsPcl != null) {
            acsPcl.addACSRequestMethod(method);
            String cpeIdLock = ProcessLock.get(cpeId);
            if (StringUtils.isNotBlank(cpeIdLock)) {
                synchronized (cpeIdLock) {
                    cpeIdLock.notifyAll();
                }
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("requestId", requestId);
            resultMap.put("cpeId", cpeId);
            resultMap.put("resultCode", 0);
            resultMap.put("resultMessage", "ACS-CPE已经通道已经存在不需要反响连接");
            return resultMap;
        } else {
            SpringRedisUtil.leftPush(cpeId, method);
            return abAcsToCpe(requestId, cpeId, cpeUrl, cpeUserName, cpeUserPassword);
        }


    }


}
