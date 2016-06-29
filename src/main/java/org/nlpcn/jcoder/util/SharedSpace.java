package org.nlpcn.jcoder.util;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 共享内存空间。不能在多线程中调用。因为终止线程可能会终止redis的连接池。做一个队列来分离线程
 *
 * @author ansj
 */
public class SharedSpace {

	private static final Logger LOG = Logger.getLogger(SharedSpace.class);

	// task_list job 队列
	private static LinkedBlockingQueue<String> taskQueue = new LinkedBlockingQueue<>();

	private static final Map<Long, String> TASK_MESSAGE = new HashMap<>();

	/**
	 * 发布一个taskqueue
	 *
	 * @param name
	 */
	public static void add2TaskQueue(String name) {
		LOG.info("publish " + name + " to task_quene !");
		taskQueue.add(name);
	}

	/**
	 * 获得任务队列
	 * 
	 * @return
	 */
	public static LinkedBlockingQueue<String> getTaskQueue() {
		return taskQueue;
	}

	/**
	 * add message for a task
	 * 
	 * @param id
	 * @param message
	 */
	public static void setTaskMessage(Long id, String message) {
		if (id != null)
			TASK_MESSAGE.put(id, message);
	}

	public static String getTaskMessage(Long id) {
		if (id == null) {
			return "";
		}
		return TASK_MESSAGE.get(id);
	}

	public static void removeTaskMessage(Long id) {
		if (id != null)
			TASK_MESSAGE.remove(id);
	}
}
