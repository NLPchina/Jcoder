package org.nlpcn.jcoder.job;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.nlpcn.jcoder.domain.GroupGit;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.ZKMap;
import org.nutz.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定期清除过期的token
 * Created by Ansj on 29/01/2018.
 */
public class MasterCleanTokenJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(MasterCleanTokenJob.class);

	private static Thread thread = null;

	/**
	 * 当竞选为master时候调用此方法
	 */
	public synchronized static void startJob() {
		stopJob();
		ThreadManager.startScheduler();
		thread = new Thread(new MasterCleanTokenJob());
		thread.start();
	}

	/**
	 * 当失去master时候调用此方法
	 */
	public synchronized static void stopJob() {
		ThreadManager.stopScheduler();
		if (thread != null) {
			try {
				thread.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		thread = null;
	}

	private MasterCleanTokenJob() {
	}

	@Override
	public void run() {

		LOG.info("I am master so to start master job clean token");

		/**
		 *  监听任务变化
		 */
		while (StaticValue.isMaster()) {
			try {
				if (StaticValue.space() == null) { //space 可能没准备好
					Thread.sleep(100L);
					continue;
				}
				ZKMap<Token> tokenCache = StaticValue.space().getTokenCache();

				Map<String, Token> stringTokenMap = tokenCache.toMap();

				for (Map.Entry<String, Token> entry : stringTokenMap.entrySet()) {
					if (entry.getValue().getExpirationTime().getTime() < System.currentTimeMillis() + 600000) { //过期10分钟的清除。防止服务器时间不太同步
						tokenCache.remove(entry.getKey());
					}
				}

				Thread.sleep(300000L); //5分钟执行一次

			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

	}


}
