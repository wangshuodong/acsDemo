package com.cmiot.acs.netty;

import com.cmiot.acs.ServerSetting;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

/**
 * Created by ZJL on 2016/11/14.
 */
public class NettyServer {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(ServerSetting.workerNum, Executors.newCachedThreadPool());
    private final int port;
    private ServerInitializer initializer;

    public NettyServer(int port) {
        this.port = port;
    }

    public void setInitializer(ServerInitializer initializer) {
        this.initializer = initializer;
    }

    public void run() throws Exception {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childHandler(this.initializer);
            logger.info("-----------------开始启动ACS-(HTTP)--------------------------");
            Channel channel = bootstrap.bind(port).sync().channel();
            logger.info("端口号：" + port + "的服务端已经启动成功");
            logger.info("-----------------启动结束ACS-(HTTP)--------------------------");
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
