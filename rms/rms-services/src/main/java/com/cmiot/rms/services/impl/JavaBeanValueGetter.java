/*
 * 文 件 名:  JavaBeanValueGetter.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年12月3日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmiot.rms.services.impl;

import com.cmiot.rms.services.ValueGetter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * 
 * @author  caojiangtao
 * @version  [版本号, 2014年12月3日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
@Component("valueGetter")
public class JavaBeanValueGetter implements ValueGetter
{
    
    /**
     * @param target
     * @param fieldName
     * @return
     */
    @Override
    public Object getValue(Object target, String fieldName)
    {
        Field field = getField(target.getClass(), fieldName);
        Object fieldValue = null;
        
        if (field != null)
        {
            field.setAccessible(true);
            try
            {
                fieldValue = field.get(target);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return fieldValue;
    }
    
    @SuppressWarnings("rawtypes")
    private Field getField(Class c, String fieldName)
    {
        Field field = null;
        
        try
        {
            field = c.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
            Class parentClass = c.getSuperclass();
            if (parentClass != null)
            {
                field = getField(parentClass, fieldName);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return field;
    }
}
