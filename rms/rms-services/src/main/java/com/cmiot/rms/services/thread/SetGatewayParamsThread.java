package com.cmiot.rms.services.thread;

import com.cmiot.rms.dao.model.GatewayBusiness;
import com.cmiot.rms.services.workorder.impl.BusiOperation;

/**
 * Created by zoujiang on 2016/7/20.
 */
public class SetGatewayParamsThread implements Runnable {

    private GatewayBusiness gatewayBusiness;
    private String templateContent;
    private BusiOperation busiOperation;
    
    public SetGatewayParamsThread(GatewayBusiness gatewayBusiness, String xml,BusiOperation busiOperation )
    {
        this.gatewayBusiness = gatewayBusiness;
        this.templateContent = xml;
        this.busiOperation = busiOperation;
    }

    @Override
    public void run() {
       
    	//busiOperation.excute(templateContent, paramS);
    	
    	//把下发结果写回数据库，考虑重试机制，防止工单信息还没有写入到数据库
    }
}
