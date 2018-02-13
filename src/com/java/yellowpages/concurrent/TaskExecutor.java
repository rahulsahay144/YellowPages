package com.java.yellowpages.concurrent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author n689716
 *
 */
public class TaskExecutor {

    private static final long DEFAULT_WAIT_TIME				= 45*1000;//Default wait time.
    private static final String REQUEST_DATETIME_FORMAT		= "MMddyyyy_hhmmssaaa";

	private List<ITask> registeredTasks = new ArrayList<ITask>();
	private List<ITask> completedTasks = new ArrayList<ITask>();
	
	private long waitTime;
	private String taskExName;
	private boolean executionComplete;
	private boolean logExecutionSummary;
	
	public TaskExecutor(String name) {
		SimpleDateFormat sdf = new SimpleDateFormat(REQUEST_DATETIME_FORMAT);
		String strDate = sdf.format(new Date());
		taskExName = name + "_" + strDate;
		waitTime = DEFAULT_WAIT_TIME;// This has to be set by caller by calling setWaitTime method.
		logExecutionSummary = true;
	}
	
	/**
	 * Adds task
	 * @param name
	 */
	public synchronized void addTask(ITask task) {
		System.out.println("Task " + task.getClass().getSimpleName() + " is added in task executor " + taskExName);
		registeredTasks.add(task);
	}
	
	public void execute() {
		int taskCount = registeredTasks.size();
		if(taskCount == 0) {
			System.out.println("No task added to execute for " + taskExName);
			return;
		}
		List<RunnableTask> taskList = new ArrayList<RunnableTask>();
		
		for(int count=0; count < taskCount; count++) {
			RunnableTask newTask = new RunnableTask(registeredTasks.get(count), this);
			taskList.add(newTask);
			AppThreadPool.getInstance().submit(newTask);
		}
		// When last task runs in current thread, all tasks could be completed by the time this line is executed. 
		// Hence in that case wait is NOT needed.
		if(registeredTasks.size() != completedTasks.size()) {
			try {
				synchronized(this) {
					wait(waitTime);
				}
				executionComplete = true;
			}catch(InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		
		logExecutionSummary(taskList);
	}
	
	/**
	 * Set wait time to wait till submitted tasks are done.
	 * Otherwise the control is returned.
	 * @param time
	 */
	public synchronized void setWaitTime(long waitTime) {
		if(!executionComplete)
			this.waitTime = waitTime;
	}
	
	public synchronized void setTaskDone(ITask task) {
		if(!executionComplete) {
			completedTasks.add(task);
			// Notify caller thread when all the tasks are completed.
			if(registeredTasks.size() == completedTasks.size())
				synchronized (this) {
					executionComplete = true;
					notify();
				}
		}
	}
	
	public synchronized List<ITask> getCompletedTasks() {
		return completedTasks;
	}
	
	public String getTaskExecutorName() {
		return taskExName;
	}
	
	/**
	 * Log the task execution summary.
	 * @param taskList
	 */
	private void logExecutionSummary(List<RunnableTask> taskList) {
		if(logExecutionSummary) {
			int completedTaskCount = completedTasks.size();
			if(registeredTasks.size() == completedTaskCount) {
				//Log summary of task executions.
				StringBuilder taskExeSummary = new StringBuilder("\nSummary of task executions for request :- " + taskExName);
				long totalExecutionTime = 0;
				long executionTimeOfBiggestTask = 0;
				for(int count=0; count < taskList.size(); count++) {
					RunnableTask task = taskList.get(count);
					totalExecutionTime += task.getExecutionTime();
					if(task.getExecutionTime() > executionTimeOfBiggestTask)
						executionTimeOfBiggestTask = task.getExecutionTime();
					taskExeSummary.append("\nTask " + task.getTask().getClass().getSimpleName() + " completed in " + task.getExecutionTime() + " milliseconds.");
				}
				if(totalExecutionTime != 0)
					taskExeSummary.append("\nGain due to multitasking is " + 100*(totalExecutionTime-executionTimeOfBiggestTask)/totalExecutionTime + "%.");
				System.out.println(taskExeSummary.toString());
			}else {
				StringBuilder taskExeSummary = new StringBuilder();
				taskExeSummary.append("\nWait time (" + waitTime + " milliseconds) for Task executor(SMUser_Date_Time) " + taskExName + " is over.");
				if(completedTaskCount != 0) {
					taskExeSummary.append("\nCompleted tasks:- ");
					for (int count = 0; count < completedTaskCount; count++) {
						taskExeSummary.append((count != 0 ? ", " : "") + completedTasks.get(count).getClass().getSimpleName());
					}
				}	
				System.out.println(taskExeSummary.toString());
			}
		}
	}

	public synchronized boolean isLogExecutionSummary() {
		return logExecutionSummary;
	}

	public synchronized void setLogExecutionSummary(boolean logExecutionSummary) {
		this.logExecutionSummary = logExecutionSummary;
	}
}
