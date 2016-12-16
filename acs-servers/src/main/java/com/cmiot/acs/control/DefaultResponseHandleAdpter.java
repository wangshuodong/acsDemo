package com.cmiot.acs.control;


import com.cmiot.acs.model.*;

/**
 * 默认解析代理实现
 *
 * @author zjialin
 * @date 2016-2-24
 */
public abstract class DefaultResponseHandleAdpter implements IResponseHandle {
    private ACSProcessControl acsProcessControl;

    @Override
    public ACSProcessControl getAcsProcessControl() {
        return acsProcessControl;
    }

    @Override
    public void setAcsProcessControl(ACSProcessControl acsProcessControl) {
        this.acsProcessControl = acsProcessControl;
    }

    /**
     * 在解析前，先执行该方法，个重写，以实现自己所需的方法
     *
     * @param method
     * @param attObj
     */
    @Override
    public void beforeParse(AbstractMethod method, Object attObj) {
        // TODO Auto-generated method stub
    }


    @Override
    public void parseAddObjectResMethod(AddObjectResponse addObjectRes, Object attObj) {
        // TODO Auto-generated method stub

    }


    @Override
    public void parseDelObjectResMethod(DeleteObjectResponse delObjectRes, Object attObj) {
        // TODO Auto-generated method stub

    }


    @Override
    public void parseFactoryRestResMethod(FactoryResetResponse fRestLoadRes, Object attObj) {
        // TODO Auto-generated method stub

    }


    @Override
    public void parseOtherMethod(AbstractMethod otherMethod, Object attObj) {
        // TODO Auto-generated method stub
    }


    @Override
    public void parseFault(Fault faultMethod, Object attObj) {
        // TODO Auto-generated method stub
    }


    @Override
    public void parseSetParamAttrResMethod(SetParameterAttributesResponse setParamAttrRes, Object attObj) {
        // TODO Auto-generated method stub
    }


    @Override
    public void parseUploadResMethod(UploadResponse upLoadRes, Object attObj) {
        // TODO Auto-generated method stub
    }


    @Override
    public void parseGetRPCResMethod(GetRPCMethodsResponse grpcMethods, Object attObj) {
        // TODO Auto-generated method stub
    }


    @Override
    public void parseTranCmpMethod(TransferComplete tranfCmp) {
        // TODO Auto-generated method stub
    }
}
