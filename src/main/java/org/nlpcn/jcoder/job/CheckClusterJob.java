//package org.nlpcn.jcoder.job;
//
//import org.nlpcn.jcoder.domain.Group;
//import org.nlpcn.jcoder.service.GroupService;
//import org.nlpcn.jcoder.util.StaticValue;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
///**
// * 定期检查集群运行情况。检查本机是否在集群中，是否有group需要刷新
// *
// * @author ansj
// */
//public class CheckClusterJob implements Runnable {
//
//	private static final Logger LOG = LoggerFactory.getLogger(CheckClusterJob.class);
//
//	private static final ConcurrentHashMap<String, Long> GROUP_TIME = new ConcurrentHashMap<>();
//
//	private static final Lock LOCK = new ReentrantLock();
//
//	@Override
//	public void run() {
//		while (true) {
//			try {
//				LOCK.lock();
//
//				List<String> groupNames = new ArrayList<>();
//
//				Set<Map.Entry<String, Long>> entries = GROUP_TIME.entrySet();
//				for (Map.Entry<String, Long> entry : entries) {
//					if (System.currentTimeMillis() - entry.getValue() > 10000) { //这个group10s后稳定了,那么需要刷新
//						groupNames.add(entry.getKey());
//					}
//				}
//
//				if (groupNames.size() > 0) {
//					for (String groupName : groupNames) {
//						long start = System.currentTimeMillis();
//						Group group = StaticValue.getSystemIoc().get(GroupService.class, "groupService").findGroupByName(groupName);
//						if (group != null) {
//							StaticValue.getSystemIoc().get(GroupService.class, "groupService").flush(groupName, false);
//						}
//						GROUP_TIME.remove(groupName);
//						LOG.info("{} to flush ok use time: {}", groupName, (System.currentTimeMillis() - start));
//					}
//				}
//
//				Thread.sleep(3000L);
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				LOCK.unlock();
//			}
//		}
//
//	}
//
//	/**
//	 * 接到cluster 通知，将需要重新刷新的group 放到这里
//	 *
//	 * @param groupName
//	 */
//	public static void changeGroup(String groupName) {
//		try {
//			LOCK.lock();
//			GROUP_TIME.put(groupName, System.currentTimeMillis());
//		} finally {
//			LOCK.unlock();
//		}
//	}
//}
