package org.nlpcn.jcoder.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.service.TaskService;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务调度管理
 * 
 * @author ansj
 * 
 */
class QuartzSchedulerManager {

	private static final Logger LOG = LoggerFactory.getLogger(QuartzSchedulerManager.class);

	private static Scheduler scheduler ;

	/**
	 * 初始化
	 * 
	 * @return
	 */
	private static Scheduler init() {
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			while (!scheduler.isStarted()) {
				LOG.info("wait for SCHEDULER started!");
				Thread.sleep(100L);
			}
			return scheduler;
		} catch (SchedulerException e) {
			e.printStackTrace();
			LOG.error(e.getMessage(),e);
		} catch (InterruptedException e) {
			e.printStackTrace();
			LOG.error(e.getMessage(),e);
		}
		return null;
	}

	/**
	 * 增加一个ｊｏｂ
	 * 
	 * @param jd
	 * @param jobKey
	 * @param scheduleStr
	 * @throws SchedulerException
	 */
	protected static synchronized boolean addJob(Task task) throws SchedulerException {
		
		// ｔａｓｋ已激活
		if (task.getStatus() == 0) {
			LOG.info(task.getName() + " status not active to skip!");
			return false;
		}

		// ｔａｓｋ已激活，并且ｔａｓｋ是计划任务才会加入到计划任务中
		if (task.getType() == 2) {
			LOG.info(task.getName() + " add to the schedulejob! ");
			scheduler.scheduleJob(makeJobDetail(task), makeTrigger(task));
			return true;
		} else {
			LOG.info(task.getName() + " type not to skip!");
			return false;
		}
		
		
	}

	protected static JobDetail makeJobDetail(Task task) {
		JobDetail job = JobBuilder.newJob(QuartzJob.class).withIdentity(task.getName()).build();
		return job;
	}

	protected static Trigger makeTrigger(Task task) {
		String scheduleStr = task.getScheduleStr();
		if (StringUtil.isBlank(task.getScheduleStr()) || "while".equalsIgnoreCase(scheduleStr)) {
			return TriggerBuilder.newTrigger().build();
		} else {
			return TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(scheduleStr)).build();
		}
	}

	/**
	 * 删除一个job
	 * 
	 * @param task
	 * @throws SchedulerException
	 * @throws Exception
	 */
	protected static synchronized boolean stopTaskJob(String taskName) throws SchedulerException {
		scheduler.deleteJob(JobKey.jobKey(taskName));
		return true;
	}

	/**
	 * 判断一个task是否运行
	 * 
	 * @param task
	 * @return
	 * @throws SchedulerException
	 */
	public static synchronized boolean checkExists(String taskName) throws SchedulerException {
		return scheduler.checkExists(JobKey.jobKey(taskName));
	}

	/**
	 * 重置任务队列
	 * 
	 * @throws SchedulerException
	 */
	protected static void startScheduler() throws SchedulerException {
		scheduler = init();
	}

	protected static void stopScheduler() throws SchedulerException {
		if (scheduler != null) {
			scheduler.clear();
			scheduler.shutdown();
			scheduler = null;
		}

	}

	/**
	 * 获得所有当前的task
	 * 
	 * @return
	 * @throws SchedulerException
	 */
	protected static List<Task> getTaskList() throws SchedulerException {
		if (scheduler == null) {
			return Collections.emptyList();
		}
		List<Task> list = new ArrayList<>();
		Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyJobGroup());
		for (JobKey jobKey : jobKeys) {
			Task task = TaskService.findTaskByCache(jobKey.getName());
			task.setRunStatus("Schedulerd");
			list.add(task);
		}
		return list;
	}

}
