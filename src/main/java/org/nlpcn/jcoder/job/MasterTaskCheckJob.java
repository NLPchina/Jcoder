package org.nlpcn.jcoder.job;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.nlpcn.jcoder.constant.Api;
import org.nlpcn.jcoder.constant.Constants;
import org.nlpcn.jcoder.domain.Handler;
import org.nlpcn.jcoder.domain.HostGroup;
import org.nlpcn.jcoder.domain.KeyValue;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.TaskInfo;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.MapCount;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.util.ZKMap;
import org.nutz.http.Response;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MasterTaskCheckJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(MasterTaskCheckJob.class);

	private static final BlockingQueue<Handler> HANDLER_QUEUE = new LinkedBlockingQueue<>();


	private static Thread thread = null;

	private List<TaskInfo> taskInfos = null;

	private ProxyService proxyService = null;

	private Cache<String, String> oneCache = CacheBuilder.newBuilder().maximumSize(1000).build();


	/**
	 * 当竞选为master时候调用此方法
	 */
	public synchronized static void startJob() {
		stopJob();
		ThreadManager.startScheduler();
		thread = new Thread(new MasterTaskCheckJob());
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
		HANDLER_QUEUE.clear();
		thread = null;
	}


	private MasterTaskCheckJob() {
	}

	@Override
	public void run() {

		proxyService = StaticValue.getSystemIoc().get(ProxyService.class, "proxyService");

		/**
		 *  监听任务变化
		 */
		while (StaticValue.isMaster()) {
			try {
				try {
					Handler handler = HANDLER_QUEUE.poll(60, TimeUnit.SECONDS);

					if (handler != null) {
						if (handler.getAction() == Type.NODE_REMOVED) {
							ThreadManager.removeTaskJob(handler.getGroupName(), handler.getTaskName());

						} else if (handler.getAction() == Type.NODE_ADDED || handler.getAction() == Type.NODE_UPDATED) {
							Task task = StaticValue.space().getDataInGroupCache(handler.getPath(), Task.class);
							if (task.getType() == 2 && task.getStatus() == 1) {
								ThreadManager.removeTaskJob(handler.getGroupName(), task.getName());
								ThreadManager.addJob(task.getGroupName(), task.getName(), task.getScheduleStr());
							} else {//其他情況都刪除一下反正沒什么坏处
								ThreadManager.removeTaskJob(handler.getGroupName(), handler.getTaskName());
							}
						}

					} else {
						//从 current 获取全部task，增加到定时任务,启动定时器
						List<Task> cTasks = new ArrayList<>();

						StaticValue.space().getGroupCache().getCurrentChildren(SharedSpaceService.GROUP_PATH).keySet().forEach(gp -> {
							StaticValue.space().getGroupCache().getCurrentChildren(SharedSpaceService.GROUP_PATH + "/" + gp).entrySet().forEach(e -> {
								if (!"file".equals(e.getKey())) {
									Task task = JSONObject.parseObject(e.getValue().getData(), Task.class);
									if (task.getType() == 2 && task.getStatus() == 1) {
										cTasks.add(task);
									}
								}
							});
						});


						Set<String> allScheduler = new HashSet<>();
						ThreadManager.getAllScheduler().forEach(s -> allScheduler.add(s.getGroupName() + Constants.GROUP_TASK_SPLIT + s.getTaskName()));

						//获取集群中所有的定时任务
						findAllRuningJob();

						/**
						 * 初始化所有定时任务
						 */
						cTasks.forEach(task -> {
							try {
								String scheduleStr = task.getScheduleStr();
								if ("while".equals(scheduleStr)) {
									checkWhileJob(task);
								} else if ("all".equals(scheduleStr)) {
									checkAllJob(task);
								} else if (StringUtil.isBlank(scheduleStr)) {
									checkOneceJob(task);
								} else {
									allScheduler.remove(task.getGroupName() + "@" + task.getName());
									if (!ThreadManager.checkExists(task.getGroupName(), task.getName())) {
										if (ThreadManager.addJob(task.getGroupName(), task.getName(), task.getScheduleStr())) {
											LOG.info("regedit ok ! cornStr : " + task.getScheduleStr());
										} else {
											LOG.error("regedit fail ! cornStr : " + task.getScheduleStr());
										}
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						});


						/**
						 * 对于规划外的定时任务移除
						 */
						if (allScheduler.size() > 0) {
							allScheduler.stream().forEach(groupTaskName -> {
								String[] split = groupTaskName.split(Constants.GROUP_TASK_SPLIT);
								try {
									ThreadManager.removeTaskJob(split[0], split[1]);
								} catch (SchedulerException e) {
									e.printStackTrace();
								}
							});
						}


						//查找所有机器的定时任务 发现没有在定时计划中的发送停止请求
						stopOutsideJob();
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

	private void stopOutsideJob() {
		//检查非同步状态的机器如果有定时任务就停止
		ZKMap<HostGroup> hostGroupCache = StaticValue.space().getHostGroupCache();

		/**
		 * 停止为同步状态机上的任务
		 */
		taskInfos.parallelStream().forEach(t -> {
			HostGroup hostGroup = hostGroupCache.get(t.getHostPort() + "_" + t.getGroupName());
			if (hostGroup != null && !hostGroup.isCurrent()) {
				stopTask(t.getHostPort(), t.getGroupName(), t.getTaskName());
			}

		});
	}

	/**
	 * 只执行一次的任务
	 */
	private void checkOneceJob(Task task) throws ExecutionException {
		if (oneCache.getIfPresent(task.getMd5()) == null) {
			MasterRunTaskJob.addQueue(KeyValue.with(task.getGroupName(), task.getName()));
			oneCache.put(task.getMd5(), task.getMd5());
		}
	}

	/**
	 * 检查all的task
	 */
	private void checkAllJob(Task task) {
		Set<String> currentHostPort = new HashSet<>(StaticValue.space().getCurrentHostPort(task.getGroupName())); //获得所有有这个group的同步机器

		MapCount<String> mc = new MapCount<>();
		for (TaskInfo taskInfo : taskInfos) {
			if (taskInfo.getGroupName().equals(task.getGroupName()) && taskInfo.getTaskName().equals(task.getName())) {
				mc.add(taskInfo.getHostPort());
			}
		}

		for (Map.Entry<String, Double> entry : mc.get().entrySet()) {
			if (entry.getValue() == 1) {
				currentHostPort.remove(entry.getKey());
			} else {
				stopTask(entry.getKey(), task.getGroupName(), task.getName());
			}
		}

		for (String hostPort : currentHostPort) {
			startTask(hostPort, task.getGroupName(), task.getName());
		}

	}

	/**
	 * 检查while的定时任务。必须有且只有一个存在
	 */
	private void checkWhileJob(Task task) {
		boolean have = false;

		for (TaskInfo t : taskInfos) {
			if (t.getTaskName().equals(task.getName()) && t.getGroupName().equals(task.getGroupName())) {
				if (have) {//已经有了stop掉
					stopTask(t.getHostPort(), t.getGroupName(), t.getTaskName());
				} else {
					have = true;
				}
			}
		}

		if (!have) {
			MasterRunTaskJob.addQueue(KeyValue.with(task.getGroupName(), task.getName()));
		}

	}

	/**
	 * 停止某个机器的定时任务
	 */
	private void stopTask(String hostPort, String groupName, String taskName) {
		LOG.info("to stop task {}/{} in host {}", groupName, taskName, hostPort);

		try {
			proxyService.post(hostPort, "/admin/thread/stopAllJob", ImmutableMap.of("groupName", groupName, "taskName", taskName), 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startTask(String hostPort, String groupName, String taskName) {
		LOG.info("to start task {}/{} in host {}", groupName, taskName, hostPort);
		try {
			Response post = proxyService.post(hostPort, Api.TASK_CRON.getPath(), ImmutableMap.of("groupName", groupName, "taskName", taskName), 1000);
			if (post.getStatus() == 200) {
				LOG.info("{}/{} publish ok result : " + post.getContent(), groupName, taskName);
			} else {
				LOG.error("{}/{} publish fail status : {} result : {}", groupName, taskName, post.getStatus(), post.getContent());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 获取所有机器运行中的任务
	 */
	private void findAllRuningJob() throws Exception {

		Response post = proxyService.post(StaticValue.getHostPort(), "/admin/thread/list", ImmutableMap.of("type", "threads"), 10000);

		JSONArray jar = JSONObject.parseObject(post.getContent()).getJSONObject("obj").getJSONArray("threads");

		taskInfos = jar.toJavaList(TaskInfo.class);
	}


	/**
	 * 发布一个监听任务
	 */
	public static void addQueue(Handler handler) {
		HANDLER_QUEUE.add(handler);
	}

}
