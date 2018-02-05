package org.nlpcn.jcoder.scheduler;

import org.nlpcn.jcoder.util.StringUtil;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 任务调度管理
 *
 * @author ansj
 */
class QuartzSchedulerManager {

	private static final Logger LOG = LoggerFactory.getLogger(QuartzSchedulerManager.class);

	private static Scheduler scheduler;

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
			LOG.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 增加一个ｊｏｂ
	 *
	 * @param groupTaskName
	 * @throws SchedulerException
	 */
	protected static synchronized boolean addJob(String groupTaskName, String scheduleStr) throws SchedulerException {
		LOG.info(groupTaskName + " add to the schedulejob! ");
		scheduler.scheduleJob(makeJobDetail(groupTaskName), makeTrigger(scheduleStr));
		return true;
	}

	protected static JobDetail makeJobDetail(String groupTaskName) {
		JobDetail job = JobBuilder.newJob(QuartzJob.class).withIdentity(groupTaskName).build();
		return job;
	}

	protected static Trigger makeTrigger(String scheduleStr) {
		if (StringUtil.isBlank(scheduleStr) || "while".equalsIgnoreCase(scheduleStr)) {
			return TriggerBuilder.newTrigger().build();
		} else {
			return TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(scheduleStr)).build();
		}
	}

	/**
	 * 删除一个job
	 *
	 * @param groupTaskName
	 * @throws SchedulerException
	 * @throws Exception
	 */
	protected static synchronized boolean stopTaskJob(String groupTaskName) throws SchedulerException {
		scheduler.deleteJob(JobKey.jobKey(groupTaskName));
		return true;
	}

	/**
	 * 判断一个task是否运行
	 *
	 * @return
	 * @throws SchedulerException
	 */
	public static synchronized boolean checkExists(String key) throws SchedulerException {
		return scheduler.checkExists(JobKey.jobKey(key));
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
	protected static List<String> getTaskList() throws SchedulerException {
		if (scheduler == null) {
			return Collections.emptyList();
		}
		List<String> list = new ArrayList<>();
		Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.anyJobGroup());

		for (JobKey jobKey : jobKeys) {
			list.add(jobKey.getName());
		}
		return list;
	}

}
