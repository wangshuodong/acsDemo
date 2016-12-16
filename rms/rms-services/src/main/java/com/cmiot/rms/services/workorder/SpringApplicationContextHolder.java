package com.cmiot.rms.services.workorder;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by admin on 2016/8/30.
 */
public class SpringApplicationContextHolder implements ApplicationContextAware {

    public static ApplicationContext context;

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        SpringApplicationContextHolder.context = context;
    }


    public static Object getSpringBean(String beanName) {
        if(StringUtils.isEmpty(beanName)){
            return "beanName is required";
        }
        return context==null?null:context.getBean(beanName);
    }

    public static String[] getBeanDefinitionNames() {
        return context.getBeanDefinitionNames();
    }
}
