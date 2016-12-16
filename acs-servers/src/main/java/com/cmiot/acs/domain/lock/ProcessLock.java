package com.cmiot.acs.domain.lock;

import org.apache.commons.lang.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 核心锁
 * Created by ZJL on 2016/9/23.
 */
public class ProcessLock {

    private static ConcurrentMap<String, String> processLockMap = new ConcurrentHashMap<>();

    public static String get(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        } else {
            return processLockMap.get(key);
        }
    }

    public static void put(String key, String value) {
        if (!processLockMap.containsKey(key)) {
            processLockMap.put(key, value);
        }
    }


    public static void remove(String key) {
        if (StringUtils.isNotBlank(key)) {
            processLockMap.remove(key);
        }
    }

}
