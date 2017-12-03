package org.nlpcn.jcoder.cluster;

import org.apache.zookeeper.*;

import java.io.IOException;


public class ZKClient {
	public static void main(String[] args) throws KeeperException, InterruptedException, IOException {
		String connectString = "127.0.0.1:2181";
		//String connectString = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
		ZooKeeper zk = new ZooKeeper(connectString, 10000, new Watcher() {

			public void process(WatchedEvent event) {
				System.out.println("Zk event: [" + event.toString() + "]");
			}});

		System.out.println(zk.getState());
		System.out.println("Zk Status: " + zk.getState());

		zk.create("/nodes", "节点集合".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		zk.create("/nodes/persistent", "持久节点".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		zk.create("/nodes/persistent_sequential1", "持久顺序节点1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
		zk.create("/nodes/persistent_sequential2", "持久顺序节点2".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
		zk.create("/nodes/ephemeral", "临时节点".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
		zk.create("/nodes/ephemeral_sequential1", "临时顺序节点1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		zk.create("/nodes/ephemeral_sequential2", "临时顺序节点2".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

		zk.setData("/nodes/persistent", "改变持久节点".getBytes(), -1);
		zk.setData("/nodes/persistent", "改变持久节点1".getBytes(), 0);
		zk.setData("/nodes/persistent", "改变持久节点2".getBytes(), 1);
		zk.setData("/nodes/persistent", "改变持久节点3".getBytes(), 2);
		zk.setData("/nodes/persistent", "改变持久节点4".getBytes(), 3);

		for (String child : zk.getChildren("/nodes", true)) {
			zk.delete("/nodes/" + child, -1);
		}

		zk.delete("/nodes", -1);

		zk.close();
	}

}
