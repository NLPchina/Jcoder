package org.nlpcn.jcoder.scheduler;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.StaticValue;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class QuartzJob implements Job {

	private String name;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		name = context.getJobDetail().getKey().getName();

		Task task = TaskService.findTaskByCache(name);

		try {
			StaticValue.space().add2TaskQueue(task.getGroupName(),task.getName(),task.getScheduleStr()) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
