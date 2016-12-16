package com.cmiot.acs;

import com.cmiot.acs.common.ApplicationContextUtil;
import com.cmiot.acs.common.CommonUtil;
import com.cmiot.acs.common.LogConfigUtil;
import com.cmiot.acs.domain.kafka.IKafkaConsumer;
import com.cmiot.acs.domain.kafka.IKafkaProducer;
import com.cmiot.acs.netty.NettyServer;
import com.cmiot.acs.netty.ServerInitializer;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * 启动类
 * Created by zjial on 2016/4/27.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        //初始化日志配置
        System.setProperty("config.properties", CommonUtil.getFilePath("app.properties"));
        LogConfigUtil.initLogBack(CommonUtil.getFilePath("logback.xml"));
        run();
    }

    private static void run() throws Exception {
        //根据Spring获取配置信息，初始化Server参数
        ApplicationContextUtil.initSpringFactory(new FileSystemXmlApplicationContext(
                "file:" + CommonUtil.getFilePath("applicationcontext.xml"),
                "file:" + CommonUtil.getFilePath("dubbo-common.xml"),
                "file:" + CommonUtil.getFilePath("dubbo-provider.xml"),
                "file:" + CommonUtil.getFilePath("dubbo-consumer.xml")));

        IKafkaProducer.init();
        IKafkaConsumer kafkaConsumer = new IKafkaConsumer();
        kafkaConsumer.start();

        ServerInitializer initializer = ApplicationContextUtil.getBean("serverInitializer", ServerInitializer.class);
        NettyServer server = new NettyServer(ServerSetting.httpPort);
        server.setInitializer(initializer);
        server.run();
    }
}
