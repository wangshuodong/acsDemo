package com.cmiot.acs.common;

import org.springframework.context.ApplicationContext;

/**
 * Created by ZJL on 2016/11/21.
 */
public class ApplicationContextUtil {
    private static ApplicationContext springFactory;

    public static void initSpringFactory(ApplicationContext applicationContext) {
        if (springFactory == null) {
            springFactory = applicationContext;
        }
    }


    public static Object getBean(String name) {
        Object obj = springFactory.getBean(name);
        return obj;
    }


    public static <T> T getBean(String key, Class<T> elementType) {
        return springFactory.getBean(key, elementType);
    }

    public static ApplicationContext getSpringFactory() {
        return springFactory;
    }
}
