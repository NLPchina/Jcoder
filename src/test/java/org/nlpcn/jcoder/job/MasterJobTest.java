package org.nlpcn.jcoder.job;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;
import org.nlpcn.jcoder.server.ZKServer;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.ZookeeperDao;

import java.io.IOException;

public class MasterJobTest {
	@Test
	public void test() throws IOException, KeeperException, InterruptedException {

//		new ZooKeeper("127.0.0.1:2181",1000,null).delete("/jcoder/master",-1);

		for (int i = 0; i < 1; i++) {
			new MasterJob() ;
		}
		System.in.read();
	}


}
