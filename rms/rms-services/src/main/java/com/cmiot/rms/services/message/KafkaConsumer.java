package com.cmiot.rms.services.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by panmingguo on 2016/5/5.
 */
public class KafkaConsumer {

    private static Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    public KafkaConsumer(String brokers, final List<String> topics, String groupId) {
        final ExecutorService executor = Executors.newFixedThreadPool(1);
        final ConsumerLoop consumer = new ConsumerLoop(brokers, topics, groupId);
        executor.submit(consumer);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                logger.info("shutdown kafka client...");
                consumer.shutdown();
                executor.shutdown();
                try {
                    executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
