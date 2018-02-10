package org.nlpcn.jcoder.job;

import com.alibaba.fastjson.JSONObject;

import org.nlpcn.jcoder.domain.LogInfo;
import org.nlpcn.jcoder.util.StaticValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;

import static org.nlpcn.jcoder.constant.Constants.LOG_ROOM;

/**
 * 日志处理的定时任务。所有机器运行
 * Created by Ansj on 05/02/2018.
 */
public class LogJob implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(LogJob.class);

	private static final ArrayBlockingQueue<LogInfo> QUEUE = new ArrayBlockingQueue(100000);

	private static final int MAX = 100000;

	@Override
	public void run() {

		String message = null;

		while (true) {
			try {

				LogInfo logInfo = QUEUE.poll();

				if (logInfo == null) {
					Thread.sleep(10L);
					continue;
				}


				String logJson = JSONObject.toJSONString(logInfo);

				//发送到日志房间
				StaticValue.space().getRoomService().sendMessage(LOG_ROOM, logJson);

				if (logInfo.getGroupName() != null) {
					//发送到group日志房间
					StaticValue.space().getRoomService().sendMessage(LOG_ROOM + "_" + logInfo.getGroupName(), logJson);
				} else {
					StaticValue.space().getRoomService().sendMessage(LOG_ROOM + "_" + LOG_ROOM, logJson);
				}

				//进行日志统计分析
                StatisticalJob.add(logInfo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 增加一个元素到队列
	 */
	public static void add(LogInfo logInfo) {
		QUEUE.offer(logInfo);
	}
}
