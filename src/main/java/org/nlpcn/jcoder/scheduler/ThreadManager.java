package org.nlpcn.jcoder.scheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.TaskInfo;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.DateUtils;
import org.nlpcn.jcoder.util.ExceptionUtil;
import org.nlpcn.jcoder.util.StaticValue;
import org.quartz.SchedulerException;

public class ThreadManager {

	private static final Logger LOG = Logger.getLogger(QuartzSchedulerManager.class);

	private static final AtomicLong JOB_ID = new AtomicLong();

	/**
	 * 增加一个task 只有master可以添加任务
	 * 
	 * @param task
	 * @throws SchedulerException
	 * @throws TaskException
	 */
	public synchronized static boolean add(Task task) throws TaskException {

		boolean flag = true;

		try {
			flag = QuartzSchedulerManager.addJob(task);
		} catch (SchedulerException e) {
			flag = false;
			LOG.error(e);
			throw new TaskException(e.getMessage());
		}

		return flag;
	}

	/**
	 * 运行一个task
	 * 
	 * @param task
	 * @throws TaskException
	 */
	public static void run(Task task) throws TaskException {
		String threadName = task.getName() + "@" + JOB_ID.getAndIncrement() + "@" + DateUtils.formatDate(new Date(), "yyyyMMddHHmmss");
		TaskRunManager.runTaskJob(new TaskJob(threadName, task));
	}

	/**
	 * 运行一个taskJob
	 * 
	 * @param task
	 * @throws TaskException
	 */
	public static void run(TaskJob taskJob) throws TaskException {
		TaskRunManager.runTaskJob(taskJob);
	}

	/**
	 * 停止一个task
	 * 
	 * @param task
	 * @throws TaskException
	 */
	private static synchronized void stopTaskAndRemove(String taskName) throws TaskException {
		try {
			// 从任务中移除
			try {
				TaskRunManager.stopAll(taskName);
			} catch (Exception e) {
				LOG.error(e);
				e.printStackTrace();
			}
			// 进行二次停止

			TaskRunManager.stopAll(taskName);

			// 从定时任务中移除
			QuartzSchedulerManager.stopTaskJob(taskName);

		} catch (Exception e) {
			LOG.error(e);
			throw new TaskException(e.getMessage());
		}
	}

	/**
	 * 刷新task 相当于从定时任务中移除，并且重新插入,非线程安全.如果调用记得在外层锁定对象
	 * 
	 * @param task
	 * @throws Exception
	 */
	public static void flush(Task oldTask, Task newTask) throws Exception {
		if (oldTask != null && StringUtil.isNotBlank(oldTask.getName())) {

			if (oldTask.getType() == 2) {
				LOG.info("to stop oldTask " + oldTask.getName() + " BEGIN! ");
				stopTaskAndRemove(oldTask.getName());
				LOG.info("to stop oldTask " + oldTask.getName() + " OK! ");
			} else if (oldTask.getType() == 1) {
				LOG.info("to remove Api oldTask " + oldTask.getName() + " BEGIN! ");
				stopActionAndRemove(oldTask.getName());
				LOG.info("to remove Api stop oldTask " + oldTask.getName() + " BEGIN! ");
			}
		}

		Thread.sleep(1000L);

		if (newTask != null && StringUtil.isNotBlank(newTask.getName()) && newTask.getStatus() == 1) {
			LOG.info("to start newTask " + newTask.getName() + " BEGIN! ");
			add(newTask);
			LOG.info("to start newTask " + newTask.getName() + " BEGIN! ");
		}
	}

	private static void stopActionAndRemove(String taskName) throws TaskException {
		StaticValue.MAPPING.remove(taskName); // remove url from api mapping
		try {
			// 从任务中移除
			try {
				ActionRunManager.stopAll(taskName);
			} catch (Exception e) {
				LOG.error(e);
				e.printStackTrace();
			}
			// 进行二次停止

			ActionRunManager.stopAll(taskName);

		} catch (Exception e) {
			LOG.error(e);
			throw new TaskException(e.getMessage());
		}

	}

	/**
	 * 判断一个定时任务是否存在
	 * 
	 * @param taskName
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean checkExists(String taskName) {
		try {
			return QuartzSchedulerManager.checkExists(taskName) || TaskRunManager.checkExists(taskName);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage());
		}
		return true;
	}

	/**
	 * 取得所有的在运行状态的线程
	 * 
	 * @throws SchedulerException
	 */
	public static List<TaskInfo> getAllThread() throws TaskException {
		Collection<Entry<String, TaskJob>> entrys = TaskRunManager.getTaskList();
		List<TaskInfo> threads = new ArrayList<>();
		for (Entry<String, TaskJob> entry : entrys) {
			Task task = entry.getValue().getTask();
			if (entry.getValue().isInterrupted()) {
				task.setRunStatus("Stoping");
			} else if (entry.getValue().isAlive()) {
				task.setRunStatus("Runging");
			} else if (entry.getValue().isOver()) {
				task.setRunStatus("Stoped");
			} else {
				task.setRunStatus("UnKnow");
			}
			threads.add(new TaskInfo(entry.getKey(), task, entry.getValue().getStartTime()));
		}
		return threads;
	}

	/**
	 * 获得所有的调度任务
	 * 
	 * @return
	 * @throws SchedulerException
	 */
	public static List<TaskInfo> getAllScheduler() throws SchedulerException {
		List<Task> taskList = QuartzSchedulerManager.getTaskList();
		List<TaskInfo> schedulers = new ArrayList<>();
		long time = System.currentTimeMillis();
		for (Task task : taskList) {
			schedulers.add(new TaskInfo(task.getName(), task, time));
		}
		return schedulers;
	}

	public static List<TaskInfo> getAllAction() {
		Set<String> actionList = ActionRunManager.getActionList();
		List<TaskInfo> actions = new ArrayList<>();
		for (String key : actionList) {
			TaskInfo taskInfo = null;
			try {
				String[] split = key.split("@");
				Task task = TaskService.findTaskByCache(split[0]);
				if (task == null) {
					task = new Task();
				}
				task.setRunStatus("Runging");
				taskInfo = new TaskInfo(key, task, DateUtils.getDate(split[3], "yyyyMMddHHmmss").getTime());
			} catch (Exception e) {
				taskInfo = new TaskInfo();
				taskInfo.setMessage(ExceptionUtil.printStackTrace(e));
				LOG.error(e);
			}
			taskInfo.setName(key);
			actions.add(taskInfo);
		}
		return actions;
	}

	/**
	 * 停止调度任务
	 */
	public static void stopScheduler() {
		try {
			QuartzSchedulerManager.stopScheduler();
		} catch (SchedulerException e) {
			e.printStackTrace();
			LOG.error(e);
		}
	}

	/**
	 * 开启调度任务
	 */
	public static void startScheduler() {
		try {
			QuartzSchedulerManager.startScheduler();
		} catch (SchedulerException e) {
			e.printStackTrace();
			LOG.error(e);
		}
	}

	/**
	 * 增加taskaction到运行队列中
	 * 
	 * @param key
	 * @param thread
	 */
	public static void add2ActionTask(String key, Thread thread) {
		ActionRunManager.add2ThreadPool(key, thread);
	}

	/**
	 * 强行停止一个 thread
	 * 
	 * @param key
	 * @param taskName
	 * @return
	 * @throws TaskException
	 */
	public static boolean stop(String key) throws TaskException {
		if (ActionRunManager.checkExists(key)) {
			return ActionRunManager.stop(key);
		}

		if (TaskRunManager.checkExists(key)) {
			return TaskRunManager.stop(key);
		}

		return true;

	}

	/**
	 * @param key
	 */
	public static void removeTaskIfOver(String key) {
		TaskRunManager.removeIfOver(key);
	}

	public static void removeActionIfOver(String key) {
		ActionRunManager.removeIfOver(key);
	}

	/**
	 * 判断一个任务是否存在
	 * 
	 * @param taskName
	 * @return
	 * @throws SchedulerException
	 */
	public static boolean checkActionExists(String taskName) {
		return ActionRunManager.checkExists(taskName);
	}

}
