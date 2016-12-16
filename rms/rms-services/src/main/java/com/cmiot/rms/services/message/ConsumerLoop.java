package com.cmiot.rms.services.message;

import com.cmiot.rms.common.cache.RequestCache;
import com.cmiot.rms.common.cache.TemporaryObject;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @Description
 * @Author 黄川
 * @Date 16/5/29
 */
public class ConsumerLoop implements Runnable {

    private final org.apache.kafka.clients.consumer.KafkaConsumer<String, String> consumer;

    private static Logger logger = LoggerFactory.getLogger(ConsumerLoop.class);

    private final List<String> topics;

    public ConsumerLoop(String brokers,
                        List<String> topics,
                        String groupId) {
        this.topics = topics;
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", groupId);
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        this.consumer = new org.apache.kafka.clients.consumer.KafkaConsumer(props);
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(topics);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Long.MAX_VALUE);
                for (ConsumerRecord<String, String> record : records) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("partition", record.partition());
                    data.put("offset", record.offset());
                    data.put("requestId", record.value());
                    logger.info("KafkaConsumer revMessage requestId：{}", data);

                    TemporaryObject object = RequestCache.get(record.value());
                    if(null != object) {
                        synchronized(object) {
                            logger.info("KafkaConsumer notify：{}", object.getRequestId());
                            object.notifyAll();
                        }
                    }
                }
            }
        } catch (WakeupException e) {
            // ignore for shutdown
        } finally {
            consumer.close();
        }
    }

    public void shutdown() {
        consumer.wakeup();
    }
}
