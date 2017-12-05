package org.nlpcn.jcoder.job;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.SharedSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunTaskJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(RunTaskJob.class);

	@Override
	public void run() {
		while (true) {
			try {
				Long id = SharedSpace.poll();
				if (id != null) {
					Task task = TaskService.findTaskByCache(id);
					LOG.info("get " + task.getName() + " to task_quene ! wil be run!");
					if (task == null) {
						LOG.error("task " + id + " is not found in task cache!");
					} else if (task.getStatus() == 0) {
						LOG.error("task " + task.getName() + " status is 0 so skip !");
					} else {
						ThreadManager.run(task);
					}
				}
				Thread.sleep(50L);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error("run task fail", e);
			}
		}

	}

}
