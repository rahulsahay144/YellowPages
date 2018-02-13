package com.java.yellowpages.concurrent;

/**
 * 
 * @author n689716
 *
 */
public class RunnableTask implements Runnable {

    private ITask task;
    private TaskExecutor taskEx;
    private long executionTime;
    
    public RunnableTask(ITask task, TaskExecutor taskEx) {
    	this.task = task;
    	this.taskEx = taskEx;
    }
    
    @Override
	public void run() {
    	long startTime = System.currentTimeMillis();
		System.out.println("Task " + task.getClass().getSimpleName() + " for task executor " + taskEx.getTaskExecutorName() + " started.");
		try {
			task.execute();
	    }catch(Exception e) {
			System.out.println("Exception while executing task " + task.getClass().getSimpleName() + " Due To : " + e);
			e.printStackTrace();
		}finally {
			executionTime = System.currentTimeMillis() - startTime;
			taskEx.setTaskDone(task);
			System.out.println("Task " + task.getClass().getSimpleName() + " for task executor " + taskEx.getTaskExecutorName() + " completed.");
		}
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public ITask getTask() {
		return task;
	}
}
