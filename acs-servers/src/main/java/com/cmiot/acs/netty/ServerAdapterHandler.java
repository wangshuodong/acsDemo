package com.cmiot.acs.netty;


import com.cmiot.acs.common.CommonUtil;
import com.cmiot.acs.common.SoapMethodUtil;
import com.cmiot.acs.control.ACSProcessControlManager;
import com.cmiot.acs.dispatcher.HandlerDispatcher;
import com.cmiot.acs.domain.Message;
import com.cmiot.acs.domain.cache.SpringRedisUtil;
import com.cmiot.acs.domain.lock.ProcessLock;
import com.cmiot.acs.model.Inform;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpRequest 报文解析器
 * Created by ZJL on 2016/11/14.
 */
public class ServerAdapterHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private Logger logger = LoggerFactory.getLogger(ServerAdapterHandler.class);
    private String gid = CommonUtil.getGid();
    private Inform inform;
    private HandlerDispatcher handlerDispatcher;

    public ServerAdapterHandler() {

    }

    public ServerAdapterHandler(HandlerDispatcher handlerDispatcher) {
        this.handlerDispatcher = handlerDispatcher;
    }

    public void setHandlerDispatcher(HandlerDispatcher handlerDispatcher) {
        this.handlerDispatcher = handlerDispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        HttpRequest httpRequest = null;
        StringBuilder builder = new StringBuilder();

        if (msg instanceof HttpRequest) {
            httpRequest = msg;
        }

        if (msg instanceof LastHttpContent) {
            builder.append(msg.content().toString(CharsetUtil.UTF_8));
        }

        if (inform == null) {
            inform = (Inform) SoapMethodUtil.soapToMethod(builder);
            inform.setGid(gid);
        }
        this.handlerDispatcher.addMessage(ctx, new Message(httpRequest, inform, builder, gid));
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    //出现异常
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.handlerDispatcher.removeMessageQueue(ctx);
        ctx.close();
    }

    //断开链接
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.evenLoss();
        this.handlerDispatcher.removeMessageQueue(ctx);
        super.channelInactive(ctx);
    }


    private void evenLoss() {
        try {
            if (inform != null) {
                String cpeId = inform.getDeviceId().getCpeId();
                ACSProcessControlManager.getInstance().removeProcessControl(cpeId);
                ProcessLock.remove(cpeId);
                SpringRedisUtil.delete(cpeId + "_URL");
                SpringRedisUtil.deleteSt(cpeId);
                SpringRedisUtil.deleteList(cpeId);
                logger.info("【GID={}】CpeId={}掉线！", gid, cpeId);
            }
        } catch (Exception ignored) {

        }
    }

}
