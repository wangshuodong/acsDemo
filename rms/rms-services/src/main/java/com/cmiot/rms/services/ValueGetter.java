/*
 * 文 件 名:  ValueGetter.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  Administrator
 * 修改时间:  2014年12月3日
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.cmiot.rms.services;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * 
 * @author  caojiagntao
 * @version  [版本号, 2014年12月3日]
 * @see  [相关类/方法]
 * @since  [产品/模块版本]
 */
public interface ValueGetter
{
    /**
     * <一句话功能简述>
     * <功能详细描述>
     * @param target 需要处理的对象,已经保证不会为NULL
     * @param fieldName 对应的属性名称,已经保证不会为空
     * @return [参数说明]
     * 
     * @return Object [返回类型说明]
     * @exception throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    Object getValue(Object target, String fieldName);
}
