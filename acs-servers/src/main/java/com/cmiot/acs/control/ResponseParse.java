package com.cmiot.acs.control;

import com.cmiot.acs.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 分类解析器
 *
 * @author zjialin
 * @date 2016-2-16
 */
public final class ResponseParse {
    private static final Logger logger = LoggerFactory.getLogger(ResponseParse.class);
    private IResponseHandle responseHandle;

    public ResponseParse(IResponseHandle responseHandle) {
        if (responseHandle == null) {
            throw new NullPointerException();
        }
        this.responseHandle = responseHandle;
    }

    public IResponseHandle getResponseHandle() {
        return responseHandle;
    }

    /**
     * 解析CPEResquest，并做相应的自动回复
     *
     * @param method
     */
    public void parseCPEResquestMethod(AbstractMethod method) {
        AbstractMethod resMethod = null;
        try {
            ACSProcessControl acsPcl = responseHandle.getAcsProcessControl();
            responseHandle.beforeParse(method, null);           // 解析前的操作
            if (method instanceof Inform) {// 接收到Inform，进行解析
                responseHandle.parseInformMethod((Inform) method);
                resMethod = new InformResponse();
                if (acsPcl != null) { // 回复
                    acsPcl.setMaxEnvelopes(((Inform) method).getMaxEnvelopes());
                }
            } else if (method instanceof TransferComplete) {
                responseHandle.parseTranCmpMethod((TransferComplete) method);
                resMethod = new TransferCompleteResponse();
            } else if (method instanceof GetRPCMethods) {
                GetRPCMethodsResponse response = new GetRPCMethodsResponse();
                response.addMethodList("Inform");
                response.addMethodList("GetRPCMethods");
                response.addMethodList("RequestDownload");
                response.addMethodList("TransferComplete");
                resMethod = response;
            } else if (method instanceof RequestDownload) {
                resMethod = new RequestDownloadResponse();
            }
            if (acsPcl != null && resMethod != null) {  // 回复
                resMethod.setRequestId(method.getRequestId());
                acsPcl.addACSResponseMethod(resMethod);
            }
        } catch (Exception e) {
            logger.error("解析CPE响应方法错误：\n{},\n{}", resMethod, e);
        }
    }

    /**
     * 解析ACS请求,CPE回复方法
     *
     * @param confirmMethod
     */
    public void parseCPEResponseMethod(ACSConfirmMethod confirmMethod) {
        AbstractMethod msgModel = confirmMethod.getResponseMethod();
        Object attObj = confirmMethod.getAttachObj();
        responseHandle.beforeParse(msgModel, attObj);  // 解析前的操作
        try {
            if (msgModel instanceof GetParameterValuesResponse) {
                responseHandle.parseGetParamValuesResMethod((GetParameterValuesResponse) msgModel, attObj);
            } else if (msgModel instanceof SetParameterValuesResponse) {
                responseHandle.parseSetParamValuesResMethod((SetParameterValuesResponse) msgModel, attObj);
            } else if (msgModel instanceof GetParameterNamesResponse) {
                responseHandle.parseGetParamNamesResMethod((GetParameterNamesResponse) msgModel, attObj);
            } else if (msgModel instanceof SetParameterAttributesResponse) {
                responseHandle.parseSetParamAttrResMethod((SetParameterAttributesResponse) msgModel, attObj);
            } else if (msgModel instanceof GetParameterAttributesResponse) {
                responseHandle.parseGetParamAttrResMethod((GetParameterAttributesResponse) msgModel, attObj);
            } else if (msgModel instanceof AddObjectResponse) {
                responseHandle.parseAddObjectResMethod((AddObjectResponse) msgModel, attObj);
            } else if (msgModel instanceof DeleteObjectResponse) {
                responseHandle.parseDelObjectResMethod((DeleteObjectResponse) msgModel, attObj);
            } else if (msgModel instanceof RebootResponse) {
                responseHandle.parseRebootResMethod((RebootResponse) msgModel, attObj);
            } else if (msgModel instanceof DownloadResponse) {
                responseHandle.parseDownloadResMethod((DownloadResponse) msgModel, attObj);
            } else if (msgModel instanceof UploadResponse) {
                responseHandle.parseUploadResMethod((UploadResponse) msgModel, attObj);
            } else if (msgModel instanceof FactoryResetResponse) {
                responseHandle.parseFactoryRestResMethod((FactoryResetResponse) msgModel, attObj);
            } else if (msgModel instanceof GetRPCMethodsResponse) {
                responseHandle.parseGetRPCResMethod((GetRPCMethodsResponse) msgModel, attObj);
            } else {
                responseHandle.parseOtherMethod(msgModel, attObj);
            }
        } catch (Exception e) {
            logger.error("解析CPE响应方法错误：\n{},\n{}", msgModel, e);
        }
    }
}
