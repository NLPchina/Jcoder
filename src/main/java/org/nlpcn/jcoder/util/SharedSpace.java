//package org.nlpcn.jcoder.util;
//
//import org.nlpcn.jcoder.domain.Token;
//import org.nlpcn.jcoder.service.SharedSpaceService;
//import org.nlpcn.jcoder.util.dao.ZookeeperDao;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.concurrent.ExecutionException;
//
///**
// * @author ansj
// */
//public class SharedSpace {
//
//	private static final Logger LOG = LoggerFactory.getLogger(SharedSpace.class);
//
//	private static SharedSpaceService service;
//
//
//	/**
//	 * 发布一个taskqueue
//	 *
//	 * @param id
//	 */
//	public static void add2TaskQueue(String name) {
//		LOG.info("publish " + name + " to task_quene !");
//		service.add2TaskQueue(name);
//	}
//
//
//	/**
//	 * 计数器，记录task成功失败个数
//	 *
//	 * @param id
//	 * @param success
//	 */
//	public static void counter(Long id, boolean success) {
//		service.counter(id, success);
//	}
//
//
//	public static long getSuccess(Long id) {
//		return service.getSuccess(id);
//	}
//
//	public static long getError(Long id) {
//		return service.getErr(id);
//	}
//
//	public static Token getToken(String key) throws ExecutionException {
//		return service.getToken(key);
//	}
//
//	public static void regToken(Token token) {
//		service.regToken(token);
//	}
//
//	public static Token removeToken(String key) {
//		return service.removeToken(key);
//	}
//
//	public static void setService(SharedSpaceService service) {
//		SharedSpace.service = service;
//	}
//
//	public static Long poll() throws InterruptedException {
//		return null ;
//	}
//
//	/**
//	 * 删除一个地址映射
//	 * @param path
//	 */
//	public static void removeMapping(String path) {
//		service.removeMapping(path) ;
//	}
//
//	/**
//	 * 增加一个mapping到
//	 * @param path
//	 */
//	public static void addMapping(String path) {
//		service.addMapping(path) ;
//	}
//}
