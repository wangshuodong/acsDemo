package com.cmiot.acs.domain.kafka;

import com.cmiot.acs.ServerSetting;
import com.cmiot.acs.control.ACSProcessControl;
import com.cmiot.acs.control.ACSProcessControlManager;
import com.cmiot.acs.domain.lock.ProcessLock;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;

/**
 * IKafkaConsumer
 *
 * Created by ZJL on 2016/9/21.
 */
public class IKafkaConsumer extends Thread {


    @Override
    public void run() {
        Properties props = new Properties();
        props.put("bootstrap.servers", ServerSetting.kafkaAddress);
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("acs-cpe", "system-parameter"));
        while (true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    ServerSetting.KAFKA_CONSUMER_THREAD_POOL.execute(new Runnable() {
                        @Override
                        public void run() {
                            if ("digest_switch".equals(record.key())) {
                                String value = record.value();
                                ServerSetting.digestSwitch = Boolean.parseBoolean(value);
                            } else {
                                String cpeId = record.value();
                                if (StringUtils.isNotBlank(cpeId)) {
                                    notifyByCpeId(cpeId);
                                }
                            }
                        }
                    });
                }
            } catch (Exception e) {

            }
        }
    }


    private void notifyByCpeId(String cpeId) {
        ACSProcessControl acsPcl = ACSProcessControlManager.getInstance().getProcessControl(cpeId);//获取流程控制器
        if (acsPcl != null) {
            String cpeIdLock = ProcessLock.get(cpeId);
            if (StringUtils.isNotBlank(cpeIdLock)) {
                synchronized (cpeIdLock) {
                    cpeIdLock.notifyAll();
                }
            }
        }
    }

}
