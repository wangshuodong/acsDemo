package com.cmiot.acs.domain;

import com.cmiot.acs.ServerSetting;
import com.cmiot.acs.domain.kafka.IKafkaProducer;

/**
 * Created by ZJL on 2016/11/12.
 */
public class LogBackup {
    private static final String LOG_TOPIC = "acs-log";

    public static void backupLog(String cpeId, StringBuilder builder) {
        if (ServerSetting.LOG_BACKUP && builder.length() > 0) {
            IKafkaProducer.send(LOG_TOPIC, cpeId, builder.toString());
        }
    }

}
