package org.nlpcn.jcoder.scheduler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.CodeRunner;
import org.nlpcn.jcoder.util.DateUtils;
import org.nlpcn.jcoder.util.ExceptionUtil;

public class TaskJob extends Thread {

	private static final Logger LOG = Logger.getLogger(TaskJob.class);

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
		try {
			task.setMessage(task.getName() + " at　" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " begin runging");
			CodeRunner.run(task);
			task.setMessage("The last time at " + DateUtils.formatDate(new Date(), DateUtils.SDF_STANDARD) + " succesed");
			task.updateSuccess();
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e);
			task.updateError();
			task.setMessage("The last time at " + DateUtils.formatDate(new Date(), DateUtils.SDF_STANDARD) + " erred : " + ExceptionUtil.printStackTraceWithOutLine(e));
		} finally {
			over = true;
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
