package com.cmiot.acs.facadeimpl;

import com.cmiot.acs.control.ACSProcessControl;
import com.cmiot.acs.control.ACSProcessControlManager;
import com.cmiot.acs.domain.cache.SpringRedisUtil;
import com.cmiot.acs.domain.lock.ProcessLock;
import com.cmiot.acs.model.AbstractMethod;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * 异步执行收到的下发指令
 * Created by ZJL on 2016/11/4.
 */
public class AsynchronousFacadeImpl extends AcsCpeFacade implements Runnable {
    private String requestId;
    private String cpeId;
    private String cpeUrl;
    private String cpeUserName;
    private String cpeUserPassword;

    private List<AbstractMethod> abstractMethodList;

    public AsynchronousFacadeImpl(List<AbstractMethod> abstractMethodList) {
        this.abstractMethodList = abstractMethodList;
        AbstractMethod method = abstractMethodList.get(0);
        requestId = method.getRequestId();
        cpeId = method.getCpeId();
        cpeUrl = method.getCpeUrl();
        cpeUserName = method.getCpeUserName();
        cpeUserPassword = method.getCpePassword();
    }


    @Override
    public void run() {
        ACSProcessControl acsPcl = ACSProcessControlManager.getInstance().getProcessControl(cpeId);
        if (acsPcl != null) {
            acsPcl.addACSRequestMethod(abstractMethodList);
            String cpeIdLock = ProcessLock.get(cpeId);
            if (StringUtils.isNotBlank(cpeIdLock)) {
                synchronized (cpeIdLock) {
                    cpeIdLock.notifyAll();
                }
            }
        } else {
            SpringRedisUtil.leftPush(cpeId, abstractMethodList);
            abAcsToCpe(requestId, cpeId, cpeUrl, cpeUserName, cpeUserPassword);
        }
    }

}
