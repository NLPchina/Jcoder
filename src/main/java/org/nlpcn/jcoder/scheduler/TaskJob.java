package org.nlpcn.jcoder.scheduler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.CodeRunner;

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
	public TaskJob(String name , Task task) {
		super(task.getName());
		this.task = task;
	}

	public long getStartTime() {
		return startTime;
	}

	@Override
	public void run() {
		over = false;
		try {
			task.setMessage(task.getName() + " 开始运行　" + new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒").format(new Date()) + " 启动运行！");
			CodeRunner.run(task);
			task.setMessage("上一次运行时间　" + new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒").format(new Date()) + " 成功运行！");
			task.updateSuccess();
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e);
			task.updateError();
			task.setMessage("上一次运行时间　" + new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒").format(new Date()) + " 发生异常,异常信息如下　" + e.toString());
		} finally {
			over = true;
			ThreadManager.removeTaskJob(task.getName());
		}

	}

	public Task getTask() {
		return task;
	}

	public boolean isOver() {
		return over;
	}


}
