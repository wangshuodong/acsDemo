package com.cmiot.acs.integration;

import com.cmiot.acs.control.ACSConfirmMethod;
import com.cmiot.acs.control.ACSProcessControl;
import com.cmiot.acs.control.ACSProcessControlManager;
import com.cmiot.acs.domain.cache.SpringRedisUtil;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.struct.Event;
import com.cmiot.acs.model.struct.EventStruct;

import java.util.List;
import java.util.Map;

/**
 * Created by zjial on 2016/6/15.
 */
public class AbIntegration {
    /**
     * @param laInform
     * @param abstractMethod
     */
    public static void reportInfo(Inform laInform, AbstractMethod abstractMethod) {
        if (abstractMethod != null) {
            abstractMethod.setGid(laInform.getGid());
            String cpeId = laInform.getDeviceId().getCpeId();
            ACSProcessControl acsPcl = ACSProcessControlManager.getInstance().getProcessControl(cpeId);
            if (acsPcl != null) {
                ACSConfirmMethod acsConfirmMethod = acsPcl.getConfirmMethodMap().get(abstractMethod.getRequestId());
                if (acsConfirmMethod != null) {
                    AbstractMethod requestMethod = acsConfirmMethod.getRequestMethod();
                    if (requestMethod != null) {
                        abstractMethod.setCallbackUrl(acsConfirmMethod.getRequestMethod().getCallbackUrl());
                    }
                }

                if (abstractMethod instanceof Inform) {
                    boolean boo = false;
                    Inform lbInform = (Inform) abstractMethod;
                    for (EventStruct struct : lbInform.getEvent().getEventCodes()) {
                        if (struct.getEvenCode().equalsIgnoreCase(Event.DIAGNOSTICS_COMPLETE)) {
                            boo = true;
                            break;
                        }
                    }

                    if (boo) {
                        String key = cpeId + "_diagnose";
                        String callbackUrl = SpringRedisUtil.get(key);
                        SpringRedisUtil.delete(key);
                        abstractMethod.setCallbackUrl(callbackUrl);
                    }
                }

            }
            if (laInform.getDeviceType() == 1) {
                AcsRmsIntegration.reportInfo(abstractMethod);
            } else {
                BoxRmsIntegration.reportInfo(abstractMethod);
            }
        }
    }

    /**
     * Get Digest Password
     *
     * @param inform
     * @return
     */
    public static Map<String, Object> getDigestPassword(Inform inform) {
        if (inform.getDeviceType() == 1) {
            return AcsRmsIntegration.getDigestPassword(inform);
        } else {
            return BoxRmsIntegration.getDigestPassword(inform);
        }
    }


    /**
     * 验证OuiSn
     *
     * @return
     */
    public static boolean checkOuiSn(Inform inform) {
        if (inform.getDeviceType() == 1) {
            return AcsRmsIntegration.checkOuiSn(inform);
        } else {
            return BoxRmsIntegration.checkOuiSn(inform);
        }
    }

    /**
     * 获取首次上报参数配置
     */
    public static List<AbstractMethod> getBootStrapInstructions(Inform inform) {
        if (inform.getDeviceType() == 1) {
            return AcsRmsIntegration.getBootStrapInstructions(inform);
        } else {
            return BoxRmsIntegration.getBootStrapInstructions(inform);
        }
    }

}
