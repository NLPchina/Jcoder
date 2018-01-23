package org.nlpcn.jcoder.job;

import com.google.common.collect.ImmutableMap;
import org.nlpcn.jcoder.constant.Api;
import org.nlpcn.jcoder.domain.KeyValue;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * master 发布job
 */
public class MasterRunTaskJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(MasterRunTaskJob.class);

	private static BlockingQueue<KeyValue<String, String>> TASK_QUEUE = new LinkedBlockingQueue<>();

	private static Thread thread = null;

	/**
	 * 当竞选为master时候调用此方法
	 */
	public synchronized static void startJob() {
		stopJob();
		ThreadManager.startScheduler();
		thread = new Thread(new MasterRunTaskJob());
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
		TASK_QUEUE.clear();
		thread = null;
	}

	private MasterRunTaskJob() {
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
				try {
					KeyValue<String, String> groupTask = TASK_QUEUE.poll(Integer.MAX_VALUE, TimeUnit.DAYS);
					LOG.info("publish " + groupTask);
					String hostPort = StaticValue.space().getRandomCurrentHostPort(groupTask.getKey());
					if (StringUtil.isBlank(hostPort)) {
						LOG.warn(groupTask + " not found any current hostport");
						continue;
					}
					Response post = proxyService.post(hostPort, Api.TASK_CRON.getPath(), ImmutableMap.of("groupName", groupTask.getKey(), "taskName", groupTask.getValue()), 1000);
					if (post.getStatus() == 200) {
						LOG.info(groupTask + " publish ok result : " + post.getContent());
					} else {
						LOG.error("{} publish fail status : {} result : {}", groupTask, post.getStatus(), post.getContent());
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
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


	/**
	 * 发布一个task到任务队列
	 *
	 * @param kv
	 */
	public static void addQueue(KeyValue kv) {
		TASK_QUEUE.add(kv);
	}

}