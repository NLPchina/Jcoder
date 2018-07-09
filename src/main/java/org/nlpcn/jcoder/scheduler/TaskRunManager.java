package org.nlpcn.jcoder.scheduler;

import org.nlpcn.jcoder.constant.Constants;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.TaskInfo;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.DateUtils;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TaskRunManager {

	private static final Logger LOG = LoggerFactory.getLogger(TaskRunManager.class);

	private static final ConcurrentHashMap<String, Thread> THREAD_POOL = new ConcurrentHashMap<>();

	private static final AtomicLong JOB_ID = new AtomicLong();

	public static synchronized void stop(String key) throws TaskException {
		if (THREAD_POOL.containsKey(key)) {

			Thread remove = THREAD_POOL.get(key);

			// 10次尝试将线程移除队列中
			for (int i = 0; i < 10; i++) {
				if (remove.isAlive()) {
					try {
						remove.interrupt();
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error(e.getMessage(), e);
					}
				} else {
					THREAD_POOL.remove(key);
					LOG.info("thread has been stopd!");
				}
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LOG.error(e.getMessage(), e);
				}
			}

			if (THREAD_POOL.containsKey(key)) {
				remove.stop();
			}

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
				LOG.error(e.getMessage(), e);
			}

			if (remove.isAlive()) {
				THREAD_POOL.remove(key);
			} else {
				throw new TaskException(key + " stop Failure");
			}
		} else {
			LOG.info(key + " not find in Thread pool!");
		}
	}

	/**
	 * 得到当前的任务队列
	 *
	 * @return
	 */
	public static synchronized Set<Entry<String, Thread>> getTaskList() {
		return THREAD_POOL.entrySet();
	}

	/**
	 * 检查task是否有实例运行
	 *
	 * @param
	 * @return
	 */
	public static boolean checkTaskExists(String groupName, String taskName) {
		for (String threadName : THREAD_POOL.keySet()) {
			if (threadName.startsWith(groupName + Constants.GROUP_TASK_SPLIT + taskName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查threadName是否存在.
	 *
	 * @param key
	 * @return
	 */
	public static boolean checkExists(String key) {
		return THREAD_POOL.containsKey(key);
	}

	/**
	 * stop all task in thread
	 *
	 * @param taskName
	 */
	public static void stopAll(String groupName, String taskName) {
		Set<String> all = new HashSet<>(THREAD_POOL.keySet());
		String pre = groupName + "@" + taskName + "@";
		for (String key : all) {
			try {
				if (key.startsWith(pre)) {
					stop(key);
				}
			} catch (TaskException e) {
				e.printStackTrace();
				LOG.error("stop " + key + " err !" + e.getMessage());
			}
		}
	}

	/**
	 * 从线程列表中删除这个task，发生在task执行完毕后
	 *
	 * @param key
	 */
	public static void remove(String key) {
		THREAD_POOL.remove(key);
	}


	/**
	 * 增加一个到
	 *
	 * @param key
	 * @param thread
	 */
	public static void put(String key, Thread thread) {
		THREAD_POOL.put(key, thread);
	}


	/**
	 * 刷新task 相当于从定时任务中移除，并且重新插入,非线程安全.如果调用记得在外层锁定对象
	 */
	public static void flush(Task oldTask, Task newTask) throws Exception {
		if (oldTask != null && StringUtil.isNotBlank(oldTask.getName())) {
			LOG.info("to stop oldTask " + oldTask.getName() + " BEGIN! ");
			StaticValue.MAPPING.remove(oldTask.getGroupName(), oldTask.getName()); // remove url from api mapping
			stopAll(oldTask.getGroupName(), oldTask.getName());
			LOG.info("to stop oldTask " + oldTask.getName() + " OK! ");
		}

		Thread.sleep(1000L);

		if (newTask == null || StringUtil.isBlank(newTask.getName()) || newTask.getStatus() == 0) {
			return;
		}

		if (newTask.getType() == 1 && newTask.getStatus() == 1) {
			addApi(oldTask, newTask);
		}else if(newTask.getType() == 2 && newTask.getStatus() == 1){
			new JavaRunner(newTask).compile(); //定時任務進行一次編譯
		}

	}


	/**
	 * 更新映射
	 */
	public static void addApi(Task oldTask, Task newTask) {

		oldTask.codeInfo().getExecuteMethods().forEach(m -> {
			StaticValue.space().removeMapping(oldTask.getGroupName(), oldTask.getName(), m.getName());

		});

		try {
			new JavaRunner(newTask).compile();
			/**
			 * 注册api到共享空间
			 */
			newTask.codeInfo().getExecuteMethods().forEach(m -> {
				StaticValue.space().addMapping(newTask.getGroupName(), newTask.getName(), m.getName());
			});

		} catch (Exception e) {
			LOG.error("compile {}/{} err ", newTask.getGroupName(), newTask.getName(), e);
		}
	}

	/**
	 * 获得所有的线程任务
	 *
	 * @return
	 */
	public static List<TaskInfo> getAllThread() {
		Collection<Entry<String, Thread>> entries = THREAD_POOL.entrySet().stream().filter(e -> e.getValue() instanceof TaskJob).collect(Collectors.toSet());
		List<TaskInfo> threads = new ArrayList<>();
		for (Entry<String, Thread> entry : entries) {
			Task task = ((TaskJob) entry.getValue()).getTask();
			if (entry.getValue().isInterrupted()) {
				task.setRunStatus("Stoping");
			} else if (entry.getValue().isAlive()) {
				task.setRunStatus("Runging");
			} else {
				task.setRunStatus("UnKnow");
			}
			threads.add(new TaskInfo(entry.getKey(), task, ((TaskJob) entry.getValue()).getStartTime()));
		}
		return threads;
	}

	/**
	 * 获得所有的api任务
	 *
	 * @return
	 */
	public static List<TaskInfo> getAllAction() {
		Collection<Entry<String, Thread>> entries = THREAD_POOL.entrySet().stream().filter(e -> !(e.getValue() instanceof TaskJob)).collect(Collectors.toSet());
		List<TaskInfo> actions = new ArrayList<>();
		for (Entry<String, Thread> entry : entries) {
			String key = entry.getKey();
			TaskInfo taskInfo = null;
			try {
				String[] split = key.split("@");
				Task task = TaskService.findTaskByCache(split[0], split[1]);
				if (task == null) {
					task = new Task();
				}
				task.setRunStatus("Runging");
				taskInfo = new TaskInfo(key, task, DateUtils.getDate(split[4], "yyyyMMddHHmmss").getTime());
			} catch (Exception e) {
				taskInfo = new TaskInfo();
				LOG.error(e.getMessage(), e);
			}
			taskInfo.setName(key);
			actions.add(taskInfo);
		}
		return actions;
	}

	public static void run(Task task) throws TaskException {
		//如果是while或者一次性任务将不再添加进来
		if (StringUtil.isBlank(task.getScheduleStr()) || "while".equals(task.getScheduleStr().toLowerCase())) {
			if (TaskRunManager.checkTaskExists(task.getGroupName(), task.getName())) {
				LOG.warn("task " + task.getName() + " has been in joblist so skip it ");
				return;
			}
		}

		TaskJob taskJob = new TaskJob(makeThreadKey(task), task);
		try {
			taskJob.start(); //裏面負責放縣城和清理縣城
		} catch (Exception e) {
			throw new TaskException("the thread " + taskJob.getName() + e.toString());
		}
	}

	/**
	 * 构建线程运行的key
	 *
	 * @param task
	 * @return
	 */
	public static String makeThreadKey(Task task) {
		return task.getGroupName() + "@" + task.getName() + "@" + JOB_ID.getAndIncrement() + "@" + DateUtils.formatDate(new Date(), "yyyyMMddHHmmss");
	}

}
