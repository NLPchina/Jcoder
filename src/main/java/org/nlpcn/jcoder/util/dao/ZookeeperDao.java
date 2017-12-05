package org.nlpcn.jcoder.util.dao;

import org.apache.zookeeper.*;

import java.io.IOException;

/**
 * 单薄的zk客户端
 * Created by Ansj on 05/12/2017.
 */
public class ZookeeperDao {

	private ZooKeeper zk = null;

	public ZookeeperDao(ZooKeeper zk){
		this.zk = zk ;
	}

	public ZookeeperDao(String connStr, Watcher watcher) throws IOException {
		zk = new ZooKeeper(connStr, 10000, watcher);
	}

	public void close() throws InterruptedException {
		zk.close();
	}

	public ZooKeeper getZk(){
		return zk ;
	}
}
