package com.cmiot.acs.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by ZJL on 2016/11/12.
 */
public class FixedThreadPoolExecutor extends ThreadPoolExecutor {
    private static Logger logger = LoggerFactory.getLogger(FixedThreadPoolExecutor.class);
    private String poolName;
    private static final int QUEUE_BASE = 100;
    private static final int MAX_BLOCK_ERR = 1000;

    public FixedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveSecond, String poolName) {
        super(corePoolSize, maximumPoolSize, keepAliveSecond, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(QUEUE_BASE * corePoolSize), new NameThreadFactory(poolName));
        this.poolName = poolName;
        setRejectedExecutionHandler(new DiscardPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                StackTraceElement stes[] = Thread.currentThread().getStackTrace();
                for (StackTraceElement ste : stes) {
                    logger.warn(ste.toString());
                }
            }
        });
    }

    public FixedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveSecond) {
        super(corePoolSize, maximumPoolSize, keepAliveSecond, TimeUnit.SECONDS, new LinkedBlockingQueue<>(QUEUE_BASE * corePoolSize));
        setRejectedExecutionHandler(new DiscardPolicy() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                StackTraceElement stes[] = Thread.currentThread().getStackTrace();
                for (StackTraceElement ste : stes) {
                    logger.warn(ste.toString());
                }
            }

        });
    }

    @Override
    public void execute(Runnable command) {
        super.execute(command);
        if (super.getCorePoolSize() * QUEUE_BASE - this.getQueue().remainingCapacity() > MAX_BLOCK_ERR) {
            logger.error(poolName + " ThreadPool blocking Queue  size : " + (super.getCorePoolSize() * QUEUE_BASE - this.getQueue().remainingCapacity()));
        }
    }

}
