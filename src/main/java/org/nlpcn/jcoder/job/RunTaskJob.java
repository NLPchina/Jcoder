package org.nlpcn.jcoder.job;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.SharedSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunTaskJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(RunTaskJob.class) ;

	@Override
	public void run() {
		LinkedBlockingQueue<String> taskQueue = SharedSpace.getTaskQueue() ;
		while (true) {
			try {
				String taskName = taskQueue.poll(Integer.MAX_VALUE,TimeUnit.DAYS);
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
				LOG.error("run task fail",e);
			}
		}

	}

}
