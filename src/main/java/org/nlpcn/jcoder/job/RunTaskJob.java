package org.nlpcn.jcoder.job;

import org.apache.log4j.Logger;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.SharedSpace;

import java.util.concurrent.LinkedBlockingQueue;

public class RunTaskJob implements Runnable {

	private static final Logger LOG = Logger.getLogger(RunTaskJob.class);

	@Override
	public void run() {
		LinkedBlockingQueue<String> taskQueue = SharedSpace.getTaskQueue() ;
		while (true) {
			try {
				String taskName = taskQueue.poll();
				if (StringUtil.isNotBlank(taskName)) {
					LOG.info("get " + taskName + " to task_quene ! wil be run!");
					Task task = TaskService.findTaskByCache(taskName);
					if (task == null) {
						LOG.error("task " + taskName + " is not found in task cache!");
					} else if (task.getStatus() == 0) {
						LOG.error("task " + taskName + " status is 0 so skip !");
					} else {
						ThreadManager.run(task);
					}
				}
				Thread.sleep(50L);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e);
			}
		}

	}

}
