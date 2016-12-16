package com.cmiot.rms.services.workorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 业务线程池 
 *
 * 目前线程池采用有界队列的线程池。
 * 当线程池的任务队列和线程都达到满值状态时。如果有新的任务提交到线程池，采用的处理程序策略为 ThreadPoolExecutor.CallerRunsPolicy
 * 即：让 调用submitTask(Runnable task)方法的线程，调用task.run()方法来运行此任务。
 * 此策略提供简单的反馈控制机制，能够减缓新任务的提交速度。
 *
 */
public class WorkerThreadPool
{
    private static Logger logger = LoggerFactory.getLogger(WorkerThreadPool.class);

    private static ThreadPoolExecutor worker = null;
    private static WorkerThreadPool instance = new WorkerThreadPool();

    private int taskQueueSize;
    private int coreSize;
    private int maxSize;
    private int keepAliveSeconds;

    private WorkerThreadPool()
    {
    }

    /**
     * 获取单例
     * <功能详细描述>
     * @return [参数说明]
     *
     * @return WorkerThreadPool [返回类型说明]
     * @exception throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public static WorkerThreadPool getInstance()
    {
        return instance;
    }

    /**
     * 初始化线程池
     *
     * @return void [返回类型说明]
     * @exception throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public void init(int taskQueueSize,int coreSize,int maxSize,int keepAliveSeconds)
    {
        this.taskQueueSize = taskQueueSize;
        this.coreSize = coreSize;
        this.maxSize = maxSize;
        this.keepAliveSeconds = keepAliveSeconds;
        if(worker == null) {
            BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(
                    taskQueueSize);
            worker = new ThreadPoolExecutor(coreSize,
                    maxSize,
                    keepAliveSeconds,
                    TimeUnit.SECONDS, queue,
                    new ThreadPoolExecutor.CallerRunsPolicy());
            logger.debug("工作线程池启动。");
        }
    }

    /**
     * 提交任务到线程池执行
     * 当线程池的任务队列和线程都达到满值状态时。如果有新的任务提交到线程池，
     * 采用的处理程序策略为 ThreadPoolExecutor.CallerRunsPolicy
     * 即：让 调用submitTask(Runnable task)方法的线程，调用task.run()方法来运行此任务。
     * @param task
     * @return [参数说明]
     *
     * @return Future [返回类型说明]
     * @exception throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    @SuppressWarnings("rawtypes")
    public Future submitTask(Runnable task)
    {
        if(worker==null)
        {
            init( taskQueueSize, coreSize, maxSize, keepAliveSeconds);
        }
        try
        {
            //满值后不再抛出 RejectedExecutionException
            Future future = worker.submit(task);
            return future;
        }
        catch (Exception e)
        {
            //用于处理定位任务的run方法中抛出的异常
            logger.error("提交线程池失败：",e);
            throw new RuntimeException();
        }
    }

    /**
     * 关闭线程池
     * <功能详细描述> [参数说明]
     *
     * @return void [返回类型说明]
     * @exception throws [违例类型] [违例说明]
     * @see [类、类#方法、类#成员]
     */
    public void shutdown()
    {
        if(worker!=null)
        {
            worker.shutdown();
            logger.debug("工作线程池停止。");
        }
    }
}
