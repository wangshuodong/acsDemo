package com.cmiot.rms.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 基础数据正确性注解
 * Created by panmingguo on 2016/11/16.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CorrectnessData {

    /**
     * 字段名称，用于提示使用
     * @return
     */
    String fieldName();

    /**
     * 列名，与数据库表对应的实体对象的字段相同
     * @return
     */
    String columnName();
}
