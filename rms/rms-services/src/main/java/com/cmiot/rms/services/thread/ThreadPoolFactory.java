package com.cmiot.rms.services.thread;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by panmingguo on 2016/7/7.
 */
public class ThreadPoolFactory {

    private static int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;
    private static int maximumPoolSize = 50;
    private static long keepAliveTime = 10;
    private static int dequeMaxSize = 10000;

    private ConcurrentHashMap<Integer, ThreadPool> threadMap;

    private ThreadPoolFactory(){
        threadMap = new ConcurrentHashMap<>();
    }

    public static ThreadPoolFactory getInstance()
    {
        return LazyThreadPoolFactoryHolder.instance;
    }

    private static class LazyThreadPoolFactoryHolder {
        public static ThreadPoolFactory instance = new ThreadPoolFactory();
    }

    public ThreadPool getTheadPool(ThreadTypeEnum key)
    {
        if(null == threadMap.get(key.getType()))
        {
            synchronized (ThreadPoolFactory.class)
            {
                if(null == threadMap.get(key.getType()))
                {
                    threadMap.put(key.getType(), new ThreadPool(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, dequeMaxSize));
                }
            }

        }
        return threadMap.get(key.getType());
    }
}
