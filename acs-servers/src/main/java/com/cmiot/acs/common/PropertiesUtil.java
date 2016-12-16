package com.cmiot.acs.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by zjial on 2016/5/25.
 */
public class PropertiesUtil extends CommonUtil {
    public static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    public static Properties myProperties = new Properties();


    public static Properties getMyProperties() {
        synchronized (myProperties) {
            if (myProperties.isEmpty()) {
                initPros();
            }
            return myProperties;
        }
    }

    /**
     * 初始化PROPERTIES_CONFIGS
     *
     * @throws Exception
     */
    public static void initPros() {
        try {
            String path = getFilePath("app.properties");
            FileInputStream fileInputStream = new FileInputStream(path);
            myProperties.load(fileInputStream);
            fileInputStream.close();
        } catch (Exception e) {
            logger.info("初始化PROPERTIES_CONFIGS异常：{}", e);
        }
    }

    /**
     * 根据KEY获取Properties文件的值
     *
     * @param key
     * @return
     */
    public static String getPropertiesValue(String key) {
        try {
            return getMyProperties().getProperty(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据KEY获取Properties文件的INT值
     *
     * @param key
     * @return
     */
    public static Integer getPropertiesValueInt(String key) {
        try {
            return Integer.valueOf(getMyProperties().getProperty(key));
        } catch (Exception e) {
            return null;
        }

    }


    /**
     * 根据KEY获取Properties文件的Long值
     *
     * @param key
     * @return
     */
    public static Long getPropertiesValueLong(String key) {
        try {
            return Long.valueOf(getMyProperties().getProperty(key));
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 根据KEY获取Properties文件的Boolean值
     *
     * @param key
     * @return
     */
    public static Boolean getPropertiesValueBoolean(String key) {
        try {
            return Boolean.valueOf(getMyProperties().getProperty(key));
        } catch (Exception e) {
            return null;
        }

    }

}
