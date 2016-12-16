package com.cmiot.rms.common.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {

	/**
	 * 创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程
	 * */
	public void cachedThreadPool(Runnable runnable){
		
		ExecutorService cachedThreadPool = Executors.newCachedThreadPool(); 
		cachedThreadPool.execute(runnable); 
		
	}
	/**
	 * 创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待
	 * */
	public void fixedThreadPool(int threadNum ,Runnable runnable){
		
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadNum);  
		 fixedThreadPool.execute(runnable);  
	}
	/**
	 * 定期执行
	 * @param  threadNum 线程数
	 * @parma  delay  延迟执行
	 * @param  rate   间隔多长时间执行一次
	 * */
	public void scheduledThreadPool(int threadNum, Runnable runnable, int delay, int rate){
		
		ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(threadNum);  
		  scheduledThreadPool.scheduleAtFixedRate(runnable , delay, rate, TimeUnit.SECONDS);  
	}
}
