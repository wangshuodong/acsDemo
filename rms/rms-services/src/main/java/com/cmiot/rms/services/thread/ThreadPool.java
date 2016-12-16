package com.cmiot.rms.services.thread;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by panmingguo on 2016/7/7.
 */
public class ThreadPool {

    private ThreadPoolExecutor executor = null;

    public ThreadPool(int corePoolSize,
                      int maximumPoolSize,
                      long keepAliveTime,
                      TimeUnit unit,
                      int dequeMaxSize)
    {
        executor = new ThreadPoolExecutor(corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                new LinkedBlockingDeque<>(dequeMaxSize),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }


    /**
     * 执行任务
     * @param command
     */
    public void execute(Runnable command) {
        if(null != executor)
        {
            executor.execute(command);
        }
    }

    /**
     * 关闭线程池
     */
    public void shutdown()
    {
        if(null != executor)
        {
            executor.shutdown();
        }
    }


}
