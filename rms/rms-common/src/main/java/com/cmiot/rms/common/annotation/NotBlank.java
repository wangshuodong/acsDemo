package com.cmiot.rms.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段非空注解
 * Created by panmingguo on 2016/11/14.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlank {
    /**
     * 字段名称，用于提示使用
     * @return
     */
    String fieldName();
}
