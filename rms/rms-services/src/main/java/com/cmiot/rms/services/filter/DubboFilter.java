package com.cmiot.rms.services.filter;

import com.alibaba.dubbo.rpc.*;

/**
 * Created by panmingguo on 2016/7/6.
 */
public class DubboFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext.getContext().setAttachment(com.alibaba.dubbo.common.Constants.ASYNC_KEY,"false");
        Result result = invoker.invoke(invocation);
        return result;
    }
}
