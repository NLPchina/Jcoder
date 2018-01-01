package org.nlpcn.jcoder.scheduler;

import org.nlpcn.jcoder.constant.Constants;
import org.nlpcn.jcoder.domain.KeyValue;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.job.MasterJob;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.StaticValue;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.security.Key;

public class QuartzJob implements Job {

	private String name;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		name = context.getJobDetail().getKey().getName();
		String[] split = name.split(Constants.GROUP_TASK_SPLIT);
		MasterJob.addQueue(KeyValue.with(split[0], split[1]));
	}

}
