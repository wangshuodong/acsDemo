package com.cmiot.acs.domain;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认线程池工厂
 * *@author: ZJL
 */
public class NameThreadFactory implements ThreadFactory {
    private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);
    private final AtomicInteger mThreadNum;
    private final String mPrefix;
    private final boolean mDemo;
    private final ThreadGroup mGroup;

    public NameThreadFactory() {
        this("pool-" + POOL_SEQ.getAndIncrement(), false);
    }

    public NameThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NameThreadFactory(String prefix, boolean demo) {
        this.mThreadNum = new AtomicInteger(1);
        this.mPrefix = prefix + "-thread-";
        this.mDemo = demo;
        SecurityManager s = System.getSecurityManager();
        this.mGroup = s == null ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    public Thread newThread(Runnable runnable) {
        String name = this.mPrefix + this.mThreadNum.getAndIncrement();
        Thread ret = new Thread(this.mGroup, runnable, name, 0L);
        ret.setDaemon(this.mDemo);
        return ret;
    }

    public ThreadGroup getThreadGroup() {
        return this.mGroup;
    }
}
