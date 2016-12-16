package com.cmiot.rms.common.page;


import java.io.Serializable;
import java.util.List;

public class PageBean<T> implements Serializable
{
    private static final int DEFAULT_CURRENTPAGE = 1;
    
    private static final int DEFAULT_PAGESIZE = 10;

    /**
     * 每页显示10个分页按钮
     */
    public static final int DEFAULT_NUM_OF_PAGEBAR = 10;
    /**
     * 注释内容
     */
    private static final long serialVersionUID = -2577728775221010283L;
    
    /**
     * 当前页、跳转目标页
     */
    private long currentPage = DEFAULT_CURRENTPAGE;
    
    /**
     * 一页显示多少 
     */
    private long pageSize = DEFAULT_PAGESIZE;// 一页显示多少 
    
    /**
     * 数据总条数
     */
    private long totalSize;// 总条
    
    /**
     * 数据总页数(需计算)
     */
    private long totalPage = DEFAULT_CURRENTPAGE;// 总页
    
    /**
     * 当前已显示条数
     */
    private long currentResult;

    /**
     * 分页按钮开始值
     */
    private long pageBarStart = DEFAULT_CURRENTPAGE;
    /**
     * 分页按钮结束值
     */
    private long pageBarEnd = DEFAULT_CURRENTPAGE;
    
    /**
     * 查询Bean
     */
    private T bean;
    
    /**
     * 返回结果
     */
    private List<T> result;
    
    /**
     * <默认构造函数>
     */
    public PageBean()
    {
        
    }
    
    /**
     * 传入总条数构造页面对象
     */
    public PageBean(long totalSize)
    {
        this(DEFAULT_CURRENTPAGE, totalSize);
    }
    
    /**
     * 传入总条数及当前页面编号
     */
    public PageBean(long totalSize, long pageSize)
    {
        this.setPageSize(pageSize);
        this.setTotalSize(totalSize);
    }
    
    /**
     * 传入总条数及当前页面编号
     */
    public PageBean(long currentPage, long totalSize, long pageSize)
    {
        this.setPageSize(pageSize);
        this.setCurrentPage(currentPage);
        this.setTotalSize(totalSize);
    }
    
    /**
     * @return 返回 currentPage
     */
    public long getCurrentPage()
    {
        return currentPage;
    }
    
    /**
     * @param currentPage
     */
    public void setCurrentPage(long currentPage)
    {
        if (currentPage < 1)
        {
            this.currentPage = DEFAULT_CURRENTPAGE;
        }
        else
        {
            this.currentPage = currentPage;
        }
        
    }
    
    /**
     * @return 返回 pageSize
     */
    public long getPageSize()
    {
        return pageSize;
    }
    
    /**
     * @param pageSize
     */
    public void setPageSize(long pageSize)
    {
        if (pageSize < 1)
        {
            this.pageSize = DEFAULT_PAGESIZE;
        }
        else
        {
            this.pageSize = pageSize;
        }
    }
    
    /**
     * @return 返回 totalSize
     */
    public long getTotalSize()
    {
        return totalSize;
    }
    
    /**
     * @param totalSize2
     */
    public void setTotalSize(long totalSize2)
    {
        if (totalSize2 < 0)
        {
            this.totalSize = 0L;
        }
        else
        {
            this.totalSize = totalSize2;
        }

        // 求总页数
        this.totalPage = (long) Math.ceil((float) ((totalSize2 + this.pageSize - 1) / this.pageSize));

        //修改已显示条数
        long curr = this.currentPage <= 0 ? 0 : this.currentPage - 1;
        this.currentResult = curr * this.pageSize;

        // 总页数小于显示分页按钮页数
        if (totalPage <= this.pageSize) {
            this.pageBarStart = 1;
            if (this.totalPage == 0) this.totalPage = 1;
            this.pageBarEnd = this.totalPage;
            return;
        }

        // 当前页小于分页按钮数一半(按钮值不移动)
        if (this.currentPage <= this.pageSize / 2) {
            this.pageBarStart = 1;
            this.pageBarEnd = this.pageSize;
            return;
        }

        // 当前页为最后几页(剩余页数不足按钮数一半,按钮值不移动)
        if (this.currentPage >= (this.totalPage - this.pageSize / 2)) {
            this.pageBarStart = this.totalPage - this.pageSize;
            this.pageBarEnd = this.totalPage;
            return;
        }
        
        // 处理手动设置条数pageSize为1的情况
        if(this.currentPage == this.pageSize){
        	this.pageBarStart = 1;
            this.pageBarEnd = this.totalPage;
            return;
        }

        // 根据当前页计算按钮值开始和结束值
        this.pageBarStart = this.currentPage - this.pageSize / 2 + 1;
        this.pageBarEnd = this.currentPage + (this.pageSize + 1) / 2;
    }
    
    /**
     * @return 返回 totalPage
     */
    public long getTotalPage()
    {
        return totalPage;
    }
    
    /**
     * @return 返回 currentResult
     */
    public long getCurrentResult()
    {
        return currentResult;
    }
    
    /**
     * @param currentResult
     */
    public void setCurrentResult(long currentResult)
    {
        this.currentResult = currentResult;
    }
    
    /**
     * @return 返回 bean
     */
    public T getBean()
    {
        return bean;
    }
    
    /**
     * @param bean
     */
    public void setBean(T bean)
    {
        this.bean = bean;
    }
    
    /**
     * @return 返回 result
     */
    public List<T> getResult()
    {
        return result;
    }
    
    /**
     * @param result
     */
    public void setResult(List<T> result)
    {
        this.result = result;
    }
    
    public void setTotalPage(long totalPage)
    {
        this.totalPage = totalPage;
    }

    public long getPageBarStart() {
        return pageBarStart;
    }

    public void setPageBarStart(long pageBarStart) {
        this.pageBarStart = pageBarStart;
    }

    public long getPageBarEnd() {
        return pageBarEnd;
    }

    public void setPageBarEnd(long pageBarEnd) {
        this.pageBarEnd = pageBarEnd;
    }
}
