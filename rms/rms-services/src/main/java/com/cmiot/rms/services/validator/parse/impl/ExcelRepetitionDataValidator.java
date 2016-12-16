package com.cmiot.rms.services.validator.parse.impl;

import com.cmiot.rms.common.annotation.ExcelRepetitionData;
import com.cmiot.rms.services.validator.parse.IValidator;
import com.cmiot.rms.services.validator.result.ValidateResult;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * excel中的数据重复校验
 * Created by panmingguo on 2016/11/15.
 */
public class ExcelRepetitionDataValidator implements IValidator {

    /**
     * 缓存满足条件的字段值和行号，用于判断重复性
     */
    private static Map<String, Integer> cache = new HashMap<>();

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
            if(f.isAnnotationPresent(ExcelRepetitionData.class))
            {
                ExcelRepetitionData excelRepetitionData = f.getAnnotation(ExcelRepetitionData.class);
                String key = f.getName() + "_" + value.toString();
                if(cache.containsKey(key))
                {
                    result.setMessage("导入失败,第"+ row +"条数据的" + excelRepetitionData.fieldName() +"与第" + cache.get(key) + "条重复，请修改后重新导入!");
                    return result;
                }
                else
                {
                    cache.put(key, row);
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
        cache.clear();
    }
}
