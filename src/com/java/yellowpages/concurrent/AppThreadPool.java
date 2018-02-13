package com.java.yellowpages.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author n689716
 *
 */
public class AppThreadPool {
	
	private static ThreadPoolExecutor tpEx;
	private static int corePoolSize = 100;
	private static int maxPoolSize = 200;
	private static int keepAliveTime = 10;
	private static int blockingQueueSize = 200;
	
	public static synchronized void init() {
		if(tpEx == null) {
			try {
				tpEx = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(blockingQueueSize), new ThreadPoolExecutor.CallerRunsPolicy());
			}catch(Exception e) {
				System.out.println("Error in reading configuration parameters for threadpool. " + e.getMessage());
			}
		}
	}
	
	public static synchronized ThreadPoolExecutor getInstance() {
		if(tpEx == null)
			init();
		
		//Log Stats for future Improvements
		System.out.println("============= CFSThreadPool Stats =================");
		System.out.println("Pool Size : " + tpEx.getPoolSize());
		System.out.println("Core Pool Size : " + tpEx.getCorePoolSize());
		System.out.println("Max Pool Size : " + tpEx.getMaximumPoolSize());
		System.out.println("Active Thread Count : " + tpEx.getActiveCount());
		System.out.println("Task Count : " + tpEx.getTaskCount());
		System.out.println("Completed Task Count : " + tpEx.getCompletedTaskCount());
		System.out.println("Blocking Queue Size : " + tpEx.getQueue().size());
		System.out.println("===================================================");
		
		return tpEx;
	}
}
