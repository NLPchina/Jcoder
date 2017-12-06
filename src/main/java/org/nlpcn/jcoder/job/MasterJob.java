package org.nlpcn.jcoder.job;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.StaticValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 选举master的定时任务
 */
public class MasterJob implements Watcher {

	private static final Logger LOG = LoggerFactory.getLogger(MasterJob.class);


	private static final String MASTER_PATH = StaticValue.ZK_ROOT + "/master";

	private ZooKeeper zk;

	private String hostPort ;

	public MasterJob() throws IOException, KeeperException, InterruptedException {
		zk = new ZooKeeper(StaticValue.ZK, 3000, this);

		hostPort = StaticValue.getHost() + ":" + StaticValue.PORT;

		if (zk.exists(StaticValue.ZK_ROOT, false) == null) {
			LOG.info(StaticValue.ZK_ROOT + " path not exists so create it");
			try {
				zk.create(StaticValue.ZK_ROOT, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			} catch (KeeperException.NodeExistsException e) {
			}
		}


		try {//尝试连接，如果发生异常就停止启动了
			zk.create(MASTER_PATH, hostPort.getBytes("utf-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			LOG.info("I am master my host is " + hostPort);
		} catch (KeeperException.NodeExistsException e) {
			LOG.info("master is " + new String(zk.getData(MASTER_PATH, false, null)));
			zk.exists(MASTER_PATH, true);
		}
	}

	@Override
	public void process(WatchedEvent event) {
		try {

			if (event.getType() == Event.EventType.NodeDeleted) {
				try {
					zk.create(MASTER_PATH, hostPort.getBytes("utf-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
					LOG.info("I am master my host is " + hostPort );
				} catch (KeeperException.NodeExistsException e) {
					LOG.info("master is " + new String(zk.getData(MASTER_PATH, false, null)));
					zk.exists(MASTER_PATH, true);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


}
