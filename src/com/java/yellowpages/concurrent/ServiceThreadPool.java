/**
 *
 */
package com.java.yellowpages.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Rahul Sahay (rahul.sahay@us.ing.com)
 * @Date Feb 29, 2012 9:19:57 PM
 * 
 * This class returns back Service Threadpool. In the INGSalesforceServices application
 * if anybod wants to perform asynchronous task irrespective of the [main] thread then they 
 * must use this pool instead of creating a new pool or thread. For Eg: This could be used in performing 
 * some background tasks like async logging, async auditing etc.
 * 
 * This pool runs irrespective of the [main] thread, means it could happen that main thread has 
 * performed its job and exited but the service thread is still running.
 * 
 * <p>
 * The simplest implementation of this class is just:
 * <pre>
 * ServiceThreadPool.getInstance().execute(new Runnable() {
 *	 public void run() {
 *		// Perform Task
 *	 }
 * };
 * </pre>
 * 
 * Note : If this ThreadPool is out of resources and the blocking queue is also full then the tasks are performed 
 * by [main] thread. 
 * 
 */
public class ServiceThreadPool {

	// ThreadPoolExecutor instance
	private static ThreadPoolExecutor tpEx;
	private static int corePoolSize = 15;
	private static int maxPoolSize = 300;
	private static int keepAliveTime = 10;
	private static int blockingQueueSize = 200;
	
	/**
	 * Create a static instance of worker thread
	 */
	public static synchronized void init() {
		if(tpEx == null) {
			try {
				tpEx = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(blockingQueueSize), new ThreadPoolExecutor.CallerRunsPolicy());
			}
			catch(Exception e) {
				System.out.println("Error in reading configuration parameters for service threadpool. " + e.getMessage());
			}
		}
	}
	
	/**
	 * Return ExecuterService
	 * @return
	 */
	public static synchronized ThreadPoolExecutor getInstance() throws Exception {
		if(tpEx == null)
			init();
		
		//Log Stats for future Improvements
		System.out.println("============= Service Thread Pool Stats =================");
		System.out.println("Pool Size : " + tpEx.getPoolSize());
		System.out.println("Core Pool Size : " + tpEx.getCorePoolSize());
		System.out.println("Max Pool Size : " + tpEx.getMaximumPoolSize());
		System.out.println("Active Thread Count : " + tpEx.getActiveCount());
		System.out.println("Task Count : " + tpEx.getTaskCount());
		System.out.println("Completed Task Count : " + tpEx.getCompletedTaskCount());
		System.out.println("Blocking Queue Size : " + tpEx.getQueue().size());
		System.out.println("=========================================================");
		
		return tpEx;
	}
}
