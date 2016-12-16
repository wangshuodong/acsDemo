package com.cmiot.acs.domain.kafka;

import com.cmiot.acs.ServerSetting;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * IKafkaProducer
 * Created by ZJL on 2016/11/13.
 */
public class IKafkaProducer {


    private static Producer<String, String> producer = null;

    public static void init() {
        if (producer == null) {
            Properties props = new Properties();
            props.put("bootstrap.servers", ServerSetting.kafkaAddress);
            props.put("acks", "all");
            props.put("retries", 0);
            props.put("batch.size", 16384);
            props.put("linger.ms", 1);
            props.put("buffer.memory", 33554432);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            producer = new KafkaProducer<>(props);
        }
    }

    public static void send(String topic, String key, String data) {
        ServerSetting.KAFKA_PRODUCER_THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                producer.send(new ProducerRecord<>(topic, key, data));
                producer.flush();
            }
        });

    }

}

