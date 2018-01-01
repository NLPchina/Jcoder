package org.nlpcn.jcoder.job;

import com.google.common.collect.ImmutableMap;
import org.nlpcn.jcoder.constant.Api;
import org.nlpcn.jcoder.domain.KeyValue;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class MasterJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(MasterJob.class);

	private static BlockingQueue<KeyValue<String, String>> TASK_QUEUE = new SynchronousQueue<>();

	private static Thread thread = null;

	/**
	 * 当竞选为master时候调用此方法
	 */
	public static void startJob() {
		stopJob();
		thread = new Thread(new MasterJob());
		thread.start();
	}

	/**
	 * 当失去master时候调用此方法
	 */
	public static void stopJob() {
		if (thread != null) {
			try {
				thread.interrupt();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		thread = null;
	}

	private MasterJob() {
	}

	@Override
	public void run() {
		LOG.info("I am master so to start master job");
		//从 current 获取全部task，增加到定时任务,启动定时器
		Set<String> paths = new HashSet<>();
		SharedSpaceService space = StaticValue.space();
		try {
			space.walkAllDataNode(paths, SharedSpaceService.GROUP_PATH);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("start master error ");
			space.resetMaster();
			return;
		}

		paths.stream().filter(p -> !p.contains("/file/")).forEach(p -> {
			try {
				Task task = space.getData(p, Task.class);
				if (task.getType() == 2) {
					if (ThreadManager.add(task.getGroupName(), task.getName(), task.getScheduleStr())) {
						LOG.info("regedit ok ! cornStr : " + task.getScheduleStr());
					} else {
						LOG.error("regedit fail ! cornStr : " + task.getScheduleStr());
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		ProxyService proxyService = StaticValue.getSystemIoc().get(ProxyService.class, "proxyService");

		while (StaticValue.isMaster()) {
			try {
				try {
					KeyValue<String, String> groupTask = TASK_QUEUE.poll(Integer.MAX_VALUE, TimeUnit.DAYS);
					LOG.info("publish " + groupTask);
					String hostPort = space.getRandomCurrentHostPort(groupTask.getKey());
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
