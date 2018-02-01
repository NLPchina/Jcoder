package org.nlpcn.jcoder.scheduler;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理运行中的action
 *
 * @author ansj
 */
class ActionRunManager {

	private static final Logger LOG = LoggerFactory.getLogger(ActionRunManager.class);

	private static final ConcurrentHashMap<String, Thread> THREAD_POOL = new ConcurrentHashMap<String, Thread>();

	public static void add2ThreadPool(String key, Thread thread) {
		THREAD_POOL.put(key, thread);
	}

	public static boolean stop(String key) throws TaskException {
		if (THREAD_POOL.containsKey(key)) {
			LOG.info(key + " BEGIN to stop!");
			Thread thread = THREAD_POOL.get(key);

			Task task = TaskService.findTaskByCache(key.split("@")[0], key.split("@")[1]);

			// 10次尝试将线程移除队列中
			for (int i = 0; i < 10; i++) {
				if (thread.isAlive()) {
					try {
						thread.interrupt();
					} catch (Exception e) {
						e.printStackTrace();
						LOG.error(e.getMessage(), e);
					}
				} else {
					THREAD_POOL.remove(key);
					LOG.info(key + " thread has been stopd!");
					return true;
				}
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					LOG.error(e.getMessage(), e);
				}
			}

			if (THREAD_POOL.containsKey(key)) {
				if (task != null) {
					LOG.info(task.getName() + " 线程尝试停止失败。被强制kill!");
				}
				try {
					thread.stop();
				} catch (Exception e) {
					LOG.info("Thread deah!" + e.getMessage());
					e.printStackTrace();
					LOG.error(e.getMessage(), e);
				}
			}

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
				LOG.error(e.getMessage(), e);
			}

			if (!thread.isAlive()) {
				THREAD_POOL.remove(key);
			} else {
				if (task != null) {
					LOG.info(task.getName() + " stop Failure");
				}
				throw new TaskException(key + " stop Failure");
			}
		} else {
			LOG.warn(key + " not in Thread_POOL ! it mabe stopd! ");
		}
		return true;
	}

	/**
	 * @param key
	 * @return
	 */
	public static boolean checkExists(String key) {
		return THREAD_POOL.containsKey(key);
	}

	/**
	 * 获得目前运行中的所有key
	 *
	 * @return
	 */
	public static Set<String> getActionList() {
		return THREAD_POOL.keySet();
	}

	public static void removeIfOver(String key) {
		THREAD_POOL.remove(key);
	}

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
}
