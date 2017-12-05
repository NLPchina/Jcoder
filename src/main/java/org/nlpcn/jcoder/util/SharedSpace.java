package org.nlpcn.jcoder.util;

import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * @author ansj
 */
public class SharedSpace {

	private static final Logger LOG = LoggerFactory.getLogger(SharedSpace.class);

	private static SharedSpaceService service;


	/**
	 * 发布一个taskqueue
	 *
	 * @param id
	 */
	public static void add2TaskQueue(String name) {
		LOG.info("publish " + name + " to task_quene !");
		service.add2TaskQueue(name);
	}


	/**
	 * 计数器，记录task成功失败个数
	 *
	 * @param id
	 * @param success
	 */
	public static void counter(Long id, boolean success) {
		service.counter(id, success);
	}


	public static long getSuccess(Long id) {
		return service.getSuccess(id);
	}

	public static long getError(Long id) {
		return service.getErr(id);
	}

	public static Token getToken(String key) throws ExecutionException {
		return service.getToken(key);
	}

	public static void regToken(Token token) {
		service.regToken(token);
	}

	public static Token removeToken(String key) {
		return service.removeToken(key);
	}

	public static void setService(SharedSpaceService service) {
		SharedSpace.service = service;
	}

	public static Long poll() throws InterruptedException {
		return service.poll() ;
	}
}
