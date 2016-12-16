package com.cmiot.rms.common.cache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by panmingguo on 2016/5/5.
 */
public class RequestCache {
    public static ConcurrentHashMap<String, TemporaryObject> requestCache = new ConcurrentHashMap<>();

    public static void set(String key, TemporaryObject value)
    {
        requestCache.put(key, value);
    }

    public static TemporaryObject get(String key)
    {
        return requestCache.get(key);
    }

    public static void delete(String key)
    {
        requestCache.remove(key);
    }
}
