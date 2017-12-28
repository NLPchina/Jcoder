package org.nlpcn.jcoder.util.dao;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.io.Closeable;

/**
 * 单薄的zk客户端
 * Created by Ansj on 05/12/2017.
 */
public class ZookeeperDao implements Closeable {

	private CuratorFramework client = null;

	public ZookeeperDao(String connStr) {
		client = CuratorFrameworkFactory.newClient(
				connStr,
				new RetryNTimes(10, 2000)
		);
	}

	public ZookeeperDao start(){
		client.start();
		return this ;
	}

	@Override
	public void close() {
		client.close();
	}

	public CuratorFramework getZk() {
		return client;
	}
}
