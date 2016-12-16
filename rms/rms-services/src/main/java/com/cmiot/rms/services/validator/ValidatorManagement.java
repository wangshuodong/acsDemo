package com.cmiot.rms.services.validator;



import com.cmiot.rms.services.validator.parse.IValidator;
import com.cmiot.rms.services.validator.result.ValidateResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 校验器管理类，通过spring注解方式注入校验器
 * Created by panmingguo on 2016/11/14.
 */
public class ValidatorManagement {

    //校验器列表，通过spring注入
    private List<IValidator> validatorList = new ArrayList<>();

    public ValidatorManagement()
    {
    }

    /**
     * 对传入的对象根据注册的校验依次进行校验
     * @param row
     * @param t
     * @param <T>
     * @return
     */
    public <T> ValidateResult validate(int row, T t){
        ValidateResult result = new ValidateResult();

        //校验前准备工作
        for(IValidator validator : validatorList)
        {
            validator.beforeValidate();
        }

        try {
            for(IValidator validator : validatorList)
            {
                result = validator.validate(row, t);
                if(!result.isValid())
                {
                    //失败后清除工作
                    afterValidate();
                    return result;
                }
            }
        }
        finally {
            //完成后清除工作
            afterValidate();
        }

        return result;
    }

    /**
     * 校验后清除工作
     */
    private void afterValidate()
    {
        for(IValidator validator : validatorList)
        {
            validator.afterValidate();
        }
    }

    public void setValidatorList(List<IValidator> validatorList) {
        this.validatorList = validatorList;
    }
}
