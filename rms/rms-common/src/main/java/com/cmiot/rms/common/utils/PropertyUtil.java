package com.cmiot.rms.common.utils;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author xukai
 * Date 2016/2/29
 */
public class PropertyUtil {

    private static ConcurrentHashMap<String,String> cache = new ConcurrentHashMap<>();

    public static String readProp(String filePath ,String key){
        if(!cache.containsKey(key)){
            synchronized (PropertyUtil.class){
                if(!cache.containsKey(key)){
                    Properties props = new Properties();
                    try {
                        FileInputStream fileInputStream = new FileInputStream(filePath);
                        props.load(fileInputStream);
                        fileInputStream.close();
                        for(Object obj:props.keySet()){
                            cache.put(obj+"",props.get(obj)+"");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return cache.get(key);
    }


    public static String getValue(String key)
    {
        String filePath =  System.getProperty("rms.config.path");
        if(StringUtils.isNotEmpty(filePath))
        {
            return readProp(filePath, key);
        }

        return null;
    }
}
