package com.cmiot.rms.services.validator.parse;

import com.cmiot.rms.services.validator.result.ValidateResult;

import java.lang.reflect.Field;

/**
 * 校验器接口
 * Created by panmingguo on 2016/11/14.
 */
public interface IValidator {

    /**
     *校验前准备工作
     */
    void beforeValidate();

    /**
     * 按照规则校验对象，并且把校验结果保存在ValidateResult中返回
     * @param row 当前数据的行号
     * @param t 需要验证的实体对象
     * @return
     */
    <T> ValidateResult  validate(int row, T t);


    /**
     *校验后清除工作
     */
    void afterValidate();
}
