package org.nlpcn.jcoder.job;

import com.google.common.collect.EvictingQueue;

import org.nlpcn.jcoder.util.StaticValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志处理的定时任务。所有机器运行
 * Created by Ansj on 05/02/2018.
 */
public class LogJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(LogJob.class);

	public static final EvictingQueue<String> QUEUE = EvictingQueue.create(10000);

	@Override
	public void run() {

		String message = null;

		while (true) {
			try {
				message = QUEUE.poll();
				if (message == null) {
					Thread.sleep(10L);
					continue;
				}

				//发送到日志房间
				StaticValue.space().getRoomService().sendMessage("jcoder_log", message);

				//进行日志统计分析


			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
