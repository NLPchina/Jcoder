package org.nlpcn.jcoder.job;

import com.google.common.collect.ImmutableMap;

import com.alibaba.fastjson.JSONObject;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.nlpcn.jcoder.domain.GroupGit;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Ansj on 29/01/2018.
 */
public class MasterGitPullJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(MasterGitPullJob.class);

	private static Thread thread = null;

	/**
	 * 当竞选为master时候调用此方法
	 */
	public synchronized static void startJob() {
		stopJob();
		ThreadManager.startScheduler();
		thread = new Thread(new MasterGitPullJob());
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

	private MasterGitPullJob() {
	}

	@Override
	public void run() {

		ProxyService proxyService = StaticValue.getSystemIoc().get(ProxyService.class, "proxyService");


		LOG.info("I am master so to start master job");

		/**
		 *  监听任务变化
		 */
		while (StaticValue.isMaster()) {
			try {
				if (StaticValue.space() == null) { //space 可能没准备好
					Thread.sleep(100L);
					continue;
				}
				TreeCache groupCache = StaticValue.space().getGroupCache();
				Map<String, ChildData> currentChildren = groupCache.getCurrentChildren(SharedSpaceService.GROUP_PATH);
				List<GroupGit> list = currentChildren.entrySet().stream().map(e -> {
					GroupGit gg = JSONObject.parseObject(e.getValue().getData(), GroupGit.class);
					if (gg == null) {
						gg = new GroupGit();
					}
					return gg;
				}).filter(g -> g.getAutoPullMillis() > 0).collect(Collectors.toList());

				for (GroupGit groupGit : list) {
					if (groupGit.getLastPullTime().getTime() + groupGit.getAutoPullMillis() < System.currentTimeMillis()) {
						Response post = proxyService.post(StaticValue.getHostPort(), "/admin/groupGit/flush", ImmutableMap.of("groupName", groupGit.getGroupName()), 1200000);
						if (post.getStatus() == 200) {
							LOG.info(groupGit.getGroupName() + " syn git ok result : " + post.getContent());
						} else {
							LOG.error("{} publish fail status : {} result : {}", groupGit.getGroupName(), post.getStatus(), post.getContent());
						}
					}
				}
				Thread.sleep(30000L);

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
