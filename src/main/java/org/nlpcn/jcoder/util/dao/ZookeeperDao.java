package org.nlpcn.jcoder.util.dao;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

/**
 * 单薄的zk客户端
 * Created by Ansj on 05/12/2017.
 */
public class ZookeeperDao {

	private CuratorFramework client = null;

	public ZookeeperDao(String connStr) {
		client = CuratorFrameworkFactory.newClient(
				connStr,
				new RetryNTimes(10, 2000)
		);
		client.start();

	}


	public void close() throws InterruptedException {
		client.close();
	}

	public CuratorFramework getZk() {
		return client;
	}
}
