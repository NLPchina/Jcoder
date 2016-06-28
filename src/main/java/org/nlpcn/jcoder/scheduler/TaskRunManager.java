package org.nlpcn.jcoder.scheduler;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.Task;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

class TaskRunManager {

	private static final Logger LOG = Logger.getLogger(TaskRunManager.class);
	
	private static final ConcurrentHashMap<String, TaskJob> THREAD_POOL = new ConcurrentHashMap<String, TaskJob>();

	/**
	 * 增加一个ｔａｓｋ到线程池中,并且运行
	 * 
	 * @param taskJob
	 * @return
	 * @throws TaskException
	 */
	public static void runTaskJob(TaskJob taskJob) throws TaskException {

		try {
			taskJob.start();
			THREAD_POOL.put(taskJob.getName(), taskJob);
		} catch (Exception e) {
			throw new TaskException("the thread " + taskJob.getName() + e.toString());
		}
	}

	/**
	 * 从线程池中移除
	 * 
	 * @param taskJob
	 * @throws Exception
	 */
	public static synchronized void stopTaskJob(TaskJob taskJob) throws Exception {
		stopTaskJob(taskJob.getName());
	}

	public static synchronized boolean stopTaskJob(String taskName) throws TaskException {
		if (THREAD_POOL.containsKey(taskName)) {

			TaskJob remove = THREAD_POOL.get(taskName);

			// 10次尝试将线程移除队列中
			for (int i = 0; i < 10; i++) {
				if (remove.isAlive() && !remove.isOver()) {
					try {
						remove.interrupt();
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error(e);
					}
				} else {
					THREAD_POOL.remove(taskName);
					remove.getTask().setMessage("thread has been stopd! by interrupt!");
					LOG.info("thread has been stopd!");
					return true;
				}
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LOG.error(e);
				}
			}

			if (THREAD_POOL.containsKey(taskName)) {
				remove.getTask().setMessage("线程尝试停止失败。被强制kill!");
				remove.stop();
			}

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
				LOG.error(e);
			}

			if (remove.isOver()) {
				THREAD_POOL.remove(taskName);
			} else {
				remove.getTask().setMessage("stop Failure");
				throw new TaskException(taskName + " stop Failure");
			}
		} else {
			LOG.info(taskName + " not find in Thread pool!");
		}
		return true;
	}

	/**
	 * 得到当前的任务队列
	 * 
	 * @return
	 */
	public static synchronized Collection<TaskJob> getTaskList() {
		return THREAD_POOL.values();

	}

	public static boolean checkExists(String taskName) {
		return THREAD_POOL.containsKey(taskName);
	}

	public static void resetScheduler() throws TaskException {
		synchronized (THREAD_POOL) {
			Set<String> keys = THREAD_POOL.keySet();
			for (String key : keys) {
				stopTaskJob(key);
			}
		}

	}

	public static Task getTask(String taskName) {
		TaskJob taskJob = THREAD_POOL.get(taskName);
		if (taskJob == null) {
			return null;
		}
		return taskJob.getTask();
	}

	/**
	 * 删除一个已经结束的任务
	 * 
	 * @param name
	 */
	public static void removeTaskIfOver(String name) {
		synchronized (THREAD_POOL) {
			TaskJob taskJob = THREAD_POOL.get(name);
			if (taskJob != null && taskJob.isOver()) {
				THREAD_POOL.remove(name);
			}
		}
	}

}
