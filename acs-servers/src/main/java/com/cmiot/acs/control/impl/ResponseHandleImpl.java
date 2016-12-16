package com.cmiot.acs.control.impl;


import com.cmiot.acs.control.DefaultResponseHandleAdpter;
import com.cmiot.acs.model.*;

/**
 * @author zjialin
 * @date 2016-2-24
 */
public class ResponseHandleImpl extends DefaultResponseHandleAdpter {

    @Override
    public void parseSetParamValuesResMethod(SetParameterValuesResponse setParamValuesRes, Object attObj) {
        // TODO Auto-generated method stub
    }

    @Override
    public void parseRebootResMethod(RebootResponse rebootRes, Object attObj) {
        // TODO Auto-generated method stub
    }

    @Override
    public void parseInformMethod(Inform inform) {
        // TODO Auto-generated method stub
    }

    @Override
    public void parseGetParamValuesResMethod(GetParameterValuesResponse getParamValuesRes, Object attObj) {
        // TODO Auto-generated method stub
    }

    @Override
    public void parseGetParamNamesResMethod(GetParameterNamesResponse getParamNamesRes, Object attObj) {
        // TODO Auto-generated method stub
    }

    @Override
    public void parseGetParamAttrResMethod(GetParameterAttributesResponse getParamAttrRes, Object attObj) {
        // TODO Auto-generated method stub
    }

    @Override
    public void parseDownloadResMethod(DownloadResponse downLoadRes, Object attObj) {
        // TODO Auto-generated method stub
    }

    @Override
    public void parseTranCmpMethod(TransferComplete tranfCmp) {
        // TODO Auto-generated method stub
        super.parseTranCmpMethod(tranfCmp);

    }

}
