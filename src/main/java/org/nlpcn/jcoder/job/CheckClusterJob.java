package org.nlpcn.jcoder.job;

import org.nlpcn.jcoder.util.StaticValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 定期检查集群运行情况。检查本机是否在集群中，是否有group需要刷新
 *
 * @author ansj
 */
public class CheckClusterJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(CheckClusterJob.class);

	private static final ConcurrentHashMap<String, Long> GROUP_TIME = new ConcurrentHashMap<>();

	@Override
	public void run() {
		while(true) {
			try {
				List<String> allHosts = StaticValue.space().getAllHosts();
				System.out.println(allHosts);

			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(3000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 接到cluster 通知，将需要重新刷新的group 放到这里
	 * @param groupName
	 */
	public void changeGroup(String groupName) {
		GROUP_TIME.put(groupName, System.currentTimeMillis());
	}
}
