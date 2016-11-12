package org.nlpcn.jcoder.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 共享内存空间。不能在多线程中调用。因为终止线程可能会终止redis的连接池。做一个队列来分离线程
 *
 * @author ansj
 */
public class SharedSpace {

	private static final Logger LOG = LoggerFactory.getLogger(SharedSpace.class);

	// task_list job 队列
	private static LinkedBlockingQueue<String> taskQueue = new LinkedBlockingQueue<>();

	private static final Map<Long, String> TASK_MESSAGE = new HashMap<>();

	private static final Map<Long, AtomicLong> TASK_SUCCESS = new HashMap<>();

	private static final Map<Long, AtomicLong> TASK_ERR = new HashMap<>();
	
	private static final Cache<String, Token> TOKEN_CACHE = CacheBuilder.newBuilder().expireAfterAccess(20, TimeUnit.MINUTES).build();
	
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

	private static final AtomicLong COUNT_NULL = new AtomicLong();

	private static AtomicLong getSuccessOrCreate(Long id) {
		if (id == null) {
			return COUNT_NULL;
		}

		AtomicLong result = TASK_SUCCESS.get(id);

		if (result == null) {
			result = new AtomicLong();
			TASK_SUCCESS.put(id, result);
		}
		return result;
	}

	private static AtomicLong getErrOrCreate(Long id) {
		if (id == null) {
			return COUNT_NULL;
		}

		AtomicLong result = TASK_ERR.get(id);

		if (result == null) {
			result = new AtomicLong();
			TASK_ERR.put(id, result);
		}
		return result;
	}

	public static long getSuccess(Long id) {
		return getSuccessOrCreate(id).get();
	}

	public static long getError(Long id) {
		return getErrOrCreate(id).get();
	}

	public static void updateError(Long id) {
		getErrOrCreate(id).incrementAndGet();
	}

	public static void updateSuccess(Long id) {
		getSuccessOrCreate(id).incrementAndGet();
	}

	public static Token getToken(String key) throws ExecutionException {
		return TOKEN_CACHE.getIfPresent(key);
	}

	public static void regToken(Token token) {
		TOKEN_CACHE.put(token.getToken(), token);		
	}

	public static Token removeToken(String key) {
		return TOKEN_CACHE.asMap().remove(key) ;
	}
}
