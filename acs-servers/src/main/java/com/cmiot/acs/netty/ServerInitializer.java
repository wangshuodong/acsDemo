package com.cmiot.acs.netty;

import com.cmiot.acs.dispatcher.HandlerDispatcher;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.ReadTimeoutHandler;

/**
 * Created by ZJL on 2016/11/14.
 */
public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private int readTimeout;

    private HandlerDispatcher handlerDispatcher;

    public void init() {
        new Thread(this.handlerDispatcher).start();
    }

    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("HttpServerCodec", new HttpServerCodec());
        pipeline.addLast("HttpObjectAggregator", new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast("ReadTimeoutHandler", new ReadTimeoutHandler(this.readTimeout));
        pipeline.addLast("ServerAdapterHandler", new ServerAdapterHandler(this.handlerDispatcher));    //设置handler分发器
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public void setHandlerDispatcher(HandlerDispatcher handlerDispatcher) {
        this.handlerDispatcher = handlerDispatcher;
    }
}