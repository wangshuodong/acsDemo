package com.cmiot.rms.services.validator.parse.impl;


import com.cmiot.rms.common.annotation.NotBlank;
import com.cmiot.rms.services.validator.parse.IValidator;
import com.cmiot.rms.services.validator.result.ValidateResult;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;

/**
 * 非空校验
 * Created by panmingguo on 2016/11/14.
 */
public class NotBlankValidator implements IValidator {
    /**
     * 校验前准备工作
     */
    @Override
    public void beforeValidate() {

    }

    /**
     * 按照规则校验对象，并且把校验结果保存在ValidateResult中返回
     * @param row 当前数据的行号
     * @param t 需要验证的实体对象
     * @return
     */
    @Override
    public <T> ValidateResult validate(int row, T t)
    {
        ValidateResult result = new ValidateResult();
        Field [] fields = t.getClass().getDeclaredFields();
        for(Field f : fields) {
            f.setAccessible(true);
            Object value = null;
            try {
                value = f.get(t);
            } catch (Exception e) {
                System.out.println("解析异常");
            }

            if(f.isAnnotationPresent(NotBlank.class))
            {
                NotBlank notBlank = f.getAnnotation(NotBlank.class);
                if(null == value || StringUtils.isBlank(value.toString()))
                {
                    result.setMessage("导入失败,第"+ row +"条数据错误,"+ notBlank.fieldName() +"不能为空，请按照下载的导入模版填入全部字段后再重新导入!");
                    return result;
                }
            }

        }
        return result;
    }

    /**
     * 校验后清除工作
     */
    @Override
    public void afterValidate() {

    }
}
