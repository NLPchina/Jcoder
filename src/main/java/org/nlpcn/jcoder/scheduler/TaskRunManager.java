package org.nlpcn.jcoder.scheduler;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TaskRunManager {

	private static final Logger LOG = LoggerFactory.getLogger(TaskRunManager.class);

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

	public static synchronized boolean stop(String key) throws TaskException {
		if (THREAD_POOL.containsKey(key)) {

			TaskJob remove = THREAD_POOL.get(key);

			// 10次尝试将线程移除队列中
			for (int i = 0; i < 10; i++) {
				if (remove.isAlive() && !remove.isOver()) {
					try {
						remove.interrupt();
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error(e.getMessage(),e);
					}
				} else {
					THREAD_POOL.remove(key);
					remove.getTask().setMessage("thread has been stopd! by interrupt!");
					LOG.info("thread has been stopd!");
					return true;
				}
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LOG.error(e.getMessage(),e);
				}
			}

			if (THREAD_POOL.containsKey(key)) {
				remove.getTask().setMessage("线程尝试停止失败。被强制kill!");
				remove.stop();
			}

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
				LOG.error(e.getMessage(),e);
			}

			if (remove.isOver()) {
				THREAD_POOL.remove(key);
			} else {
				remove.getTask().setMessage("stop Failure");
				throw new TaskException(key + " stop Failure");
			}
		} else {
			LOG.info(key + " not find in Thread pool!");
		}
		return true;
	}

	/**
	 * 得到当前的任务队列
	 * 
	 * @return
	 */
	public static synchronized Set<Entry<String, TaskJob>> getTaskList() {
		return THREAD_POOL.entrySet();

	}

	/**
	 * 检查taskName是否存在.
	 * 
	 * @param key
	 * @return
	 */
	public static boolean checkTaskExists(String key) {
		key = key + "@";
		for (String threadName : THREAD_POOL.keySet()) {
			if (threadName.startsWith(key)) {
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

	public static void resetScheduler() throws TaskException {
		synchronized (THREAD_POOL) {
			Set<String> keys = THREAD_POOL.keySet();
			for (String key : keys) {
				stopAll(key);
			}
		}

	}

	/**
	 * stop all task in thread
	 * 
	 * @param taskName
	 */
	public static void stopAll(String taskName) {
		Set<String> all = new HashSet<>(THREAD_POOL.keySet());
		String pre = taskName + "@";
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
	 * remove key if it is over!
	 * 
	 * @param key
	 */
	public static void removeIfOver(String key) {
		TaskJob taskJob = THREAD_POOL.get(key);
		if (taskJob.isOver()) {
			THREAD_POOL.remove(key);
		}
	}

}
