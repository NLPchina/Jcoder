package org.nlpcn.jcoder.job;

import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.nlpcn.jcoder.domain.Handler;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.StaticValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class MasterGroupListenerJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(MasterGroupListenerJob.class);

	private static final BlockingQueue<Handler> HANDLER_QUEUE = new SynchronousQueue<>();


	private static Thread thread = null;

	/**
	 * 当竞选为master时候调用此方法
	 */
	public static void startJob() {
		stopJob();
		ThreadManager.startScheduler();
		thread = new Thread(new MasterGroupListenerJob());
		thread.start();
	}

	/**
	 * 当失去master时候调用此方法
	 */
	public static void stopJob() {
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


	private MasterGroupListenerJob() {
	}

	@Override
	public void run() {


		/**
		 *  监听任务变化
		 */
		while (StaticValue.isMaster()) {
			try {
				try {
					Handler handler = HANDLER_QUEUE.poll(60, TimeUnit.SECONDS);

					if(handler!=null){
						if(handler.getAction()== PathChildrenCacheEvent.Type.CHILD_REMOVED){
							ThreadManager.removeTaskJob(handler.getGroupName(),handler.getTaskName()) ;

						}else if(handler.getAction()== PathChildrenCacheEvent.Type.CHILD_ADDED || handler.getAction()== PathChildrenCacheEvent.Type.CHILD_UPDATED){
							Task task = StaticValue.space().getData(handler.getPath(), Task.class);
							if(task.getType()==2 && task.getStatus()==1) {
								ThreadManager.removeTaskJob(handler.getGroupName(),handler.getTaskName()) ;
								ThreadManager.addJob(task.getGroupName(), task.getName(), task.getScheduleStr());
							}
						}

					}else{
						//检查task是否和cluster同步
						//从 current 获取全部task，增加到定时任务,启动定时器
						Set<String> paths = new HashSet<>();
						try {
							StaticValue.space().walkAllDataNode(paths, SharedSpaceService.GROUP_PATH);
						} catch (Exception e) {
							e.printStackTrace();
							LOG.error("start master error ");
							StaticValue.space().resetMaster();
							return;
						}

						/**
						 * 初始化所有定时任务
						 */
						paths.stream().filter(p -> !p.contains("/file/")).forEach(p -> {
							try {
								Task task = StaticValue.space().getData(p, Task.class);
								if (task.getType() == 2) {
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
	 * 发布一个监听任务
	 *
	 * @param handler
	 */
	public static void addQueue(Handler handler) {
		HANDLER_QUEUE.add(handler);
	}

}
