package org.nlpcn.jcoder.scheduler;

import org.nlpcn.jcoder.constant.Constants;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.TaskInfo;
import org.nlpcn.jcoder.service.TaskService;
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
public class QuartzSchedulerManager {

	private static final Logger LOG = LoggerFactory.getLogger(QuartzSchedulerManager.class);

	private static Scheduler scheduler;

	/**
	 * 初始化
	 *
	 * @return
	 */
	private synchronized static Scheduler init() {
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
	 * @param groupName
	 * @param taskName
	 * @param scheduleStr
	 * @throws SchedulerException
	 */
	public static synchronized boolean addJob(String groupName, String taskName, String scheduleStr) throws SchedulerException {
		String groupTaskName = groupName + Constants.GROUP_TASK_SPLIT + taskName;
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
	 * @param groupName
	 * @param taskName
	 * @throws SchedulerException
	 * @throws Exception
	 */
	public static synchronized boolean removeJob(String groupName, String taskName) throws SchedulerException {
		scheduler.deleteJob(JobKey.jobKey(groupName + Constants.GROUP_TASK_SPLIT + taskName));
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
	public static void startScheduler() throws SchedulerException {
		scheduler = init();
	}

	public static void stopScheduler() throws SchedulerException {
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
	public static Set<JobKey> jobList() throws SchedulerException {
		if (scheduler == null) {
			return Collections.emptySet();
		}
		return scheduler.getJobKeys(GroupMatcher.anyJobGroup());
	}


	/**
	 * 获得所有的定时计划任务
	 *
	 * @return
	 */
	public static List<TaskInfo> getAllScheduler() throws SchedulerException {
		List<TaskInfo> allScheduler = new ArrayList<>();
		for (JobKey jobKey : jobList()) {
			String[] split = jobKey.getName().split(Constants.GROUP_TASK_SPLIT);
			Task taskByCache = TaskService.findTaskByCache(split[0], split[1]);
			if (taskByCache == null) {
				LOG.warn("not found task in cache {}/{}", split[0], split[1]);
			} else {
				TaskInfo taskInfo = new TaskInfo(taskByCache.getScheduleStr(), split[1], split[0]);
				taskInfo.setDescription(taskByCache.getDescription());
				allScheduler.add(taskInfo);
			}
		}
		return allScheduler;
	}
}
