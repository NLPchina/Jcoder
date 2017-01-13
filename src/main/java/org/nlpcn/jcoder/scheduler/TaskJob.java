package org.nlpcn.jcoder.scheduler;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.DynamicEngine;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskJob extends Thread {

	private static final Logger LOG = LoggerFactory.getLogger(TaskJob.class);

	private Task task = null;

	private boolean over = true;

	private long startTime = System.currentTimeMillis();

	/**
	 * 运行一个任务
	 * 
	 * @param code
	 */
	public TaskJob(String name, Task task) {
		super(name);
		this.task = task;
	}

	public long getStartTime() {
		return startTime;
	}

	@Override
	public void run() {
		over = false;
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(DynamicEngine.getInstance().getParentClassLoader());
			new JavaRunner(task).compile().instance().execute() ;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(),e);
		} finally {
			over = true;
			Thread.currentThread().setContextClassLoader(contextClassLoader);
			ThreadManager.removeTaskIfOver(this.getName());
		}

	}

	public Task getTask() {
		return task;
	}

	public boolean isOver() {
		return over;
	}

}
