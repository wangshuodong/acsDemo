package com.cmiot.rms.common.page;

/**
 * @Author xukai
 * Date 2016/1/25
 */
public class PageBeanContext {
    /**
     * 存放分布容器的本地线程变量
     */
    private static ThreadLocal<PageBean> context = new ThreadLocal<PageBean>();

    /**
     * @param page
     */
    public static void setPageBean(PageBean page)
    {
        context.set(page);
    }

    /**
     * @return 返回 context
     */
    public static PageBean getPageBean()
    {
        return context.get();
    }

    /**
     * 将分页容器清空
     *
     * @return void [返回类型说明]
     * @exception throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static void clear()
    {
        context.remove();
    }
}
