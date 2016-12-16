package com.cmiot.acs.domain.code;

import com.cmiot.acs.ServerSetting;
import com.cmiot.acs.common.SignDigestUtil;
import com.cmiot.acs.control.ACSProcessControl;
import com.cmiot.acs.control.ACSProcessControlManager;
import com.cmiot.acs.integration.AbIntegration;
import com.cmiot.acs.integration.AcsRmsIntegration;
import com.cmiot.acs.model.AbstractMethod;
import com.cmiot.acs.model.DownloadResponse;
import com.cmiot.acs.model.Inform;
import com.cmiot.acs.model.UploadResponse;
import com.cmiot.acs.model.struct.Event;
import com.cmiot.acs.model.struct.EventStruct;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

import java.util.List;
import java.util.Map;

/**
 * VerifyMethod
 * Created by ZJL on 2016/11/11.
 */
public class VerifyMethod {
    /**
     * @param laInform
     * @return
     */
    public boolean verifyInform(Inform laInform) {
        boolean status = true;
        if (ifXcmccbind(laInform)) {
            if (ServerSetting.checkPassword) {
                status = AcsRmsIntegration.checkPassword(laInform);
            }
        } else {
            if (ServerSetting.checkOuiSn) {
                status = AcsRmsIntegration.checkOuiSn(laInform);
            }
        }
        if (!status && ServerSetting.initiativeFound && laInform.getDeviceType() == 1) {
            status = AcsRmsIntegration.createGatewayInfo(laInform);
        }
        if (status && (ifXcmccbind(laInform) || ifBootstrap(laInform))) {
            String cpeId = laInform.getDeviceId().getCpeId();
            ACSProcessControl acsPcl = ACSProcessControlManager.getInstance().getProcessControl(cpeId);
            List<AbstractMethod> abstractMethodList = AbIntegration.getBootStrapInstructions(laInform);
            if (abstractMethodList != null && abstractMethodList.size() > 0 && acsPcl != null) {
                for (AbstractMethod abstractMethod : abstractMethodList) {
                    acsPcl.addACSRequestMethod(abstractMethod);
                }
            }
        }
        return status;
    }


    /**
     * Digest摘要认证签名
     *
     * @return
     */
    public boolean verifyDigest(Inform laInform, HttpRequest request) {
        String authorization = request.headers().get(HttpHeaderNames.AUTHORIZATION);
        Map<String, String> laAutMap = SignDigestUtil.authenticateToMap(authorization);
        laAutMap.put("method", request.method().name());
        if (ifBootstrap(laInform) || ifXcmccbind(laInform)) {
            String username = laAutMap.get("username");
            if ("cpe".equals(username)) {
                laAutMap.put("password", "cpe");
            }
        } else {
            Map<String, Object> getDigestPasswordMap = AbIntegration.getDigestPassword(laInform);
            if (getDigestPasswordMap != null && Integer.parseInt(String.valueOf(getDigestPasswordMap.get("resultCode"))) == 0) {
                laAutMap.put("password", (String) getDigestPasswordMap.get("password"));
            } else {
                return false;
            }
        }
        return SignDigestUtil.digestSign(laAutMap);
    }


    /**
     * 判断CPEs上报的method是否为（UploadResponse，DownloadResponse）
     *
     * @param method
     * @return
     */
    public boolean verifyNoSpecialResponse(AbstractMethod method) {
        if (method instanceof UploadResponse) {
            UploadResponse uploadResponse = (UploadResponse) method;
            return uploadResponse.getStatus() != 1;
        } else if (method instanceof DownloadResponse) {
            DownloadResponse downloadResponse = (DownloadResponse) method;
            return downloadResponse.getStatus() != 1;
        } else {
            return true;
        }
    }


    public boolean queryDigestSwitch() {
        return AcsRmsIntegration.queryDigestSwitch();
    }


    /**
     * 判断是否存在（0 BOOTSTRAP）事件
     *
     * @param inform
     * @return
     */

    private boolean ifBootstrap(Inform inform) {
        boolean bootstrap = false;
        for (EventStruct eventStruct : inform.getEvent().getEventCodes()) {
            if (eventStruct.getEvenCode().equals(Event.BOOTSTRAP)) {
                bootstrap = true;
                break;
            }
        }
        return bootstrap;
    }


    /**
     * 判断是否存在（X CMCC BIND）事件
     *
     * @param inform
     * @return
     */
    private boolean ifXcmccbind(Inform inform) {
        boolean xcmccbind = false;
        for (EventStruct eventStruct : inform.getEvent().getEventCodes()) {
            if (eventStruct.getEvenCode().equals(Event.XCMCCBIND)) {
                xcmccbind = true;
                break;
            }
        }
        return xcmccbind;
    }


}
