package org.nlpcn.jcoder.util.dao;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;
import org.nlpcn.jcoder.server.ZKServer;

import java.io.IOException;

public class ZKServiceTest {



	@Test
	public void test() throws Exception {
		CuratorFramework client = CuratorFrameworkFactory.newClient(
				"127.0.0.1:8082",
				new RetryNTimes(10, 5000)
		);


		CuratorFramework client2 = CuratorFrameworkFactory.newClient(
				"127.0.0.1:8082",
				new RetryNTimes(10, 5000)
		);

		client2.start();

		StringBuilder sb = new StringBuilder() ;

		for (int i = 0; i < 10; i++) {
			sb.append(i) ;
		}

		byte[] data = sb.toString().getBytes() ;

		String path = "/test2/test3/test3/test" ;



		client.start();

		if(client.checkExists().forPath("/test2/test3/test3/test")!=null){
			System.out.println("has");

			return ;
		}

		TreeCache treeCache = new TreeCache(client, path);

		try {
			client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, data);
		}catch (KeeperException.NodeExistsException e){
			client.setData().forPath(path,data) ;
		}

		for (int i = 0; i < 10; i++) {
			client.setData().forPath(path,("i"+i).getBytes()) ;
		}

		for (int i = 10; i < 20; i++) {
			client2.setData().forPath(path,("i"+i).getBytes()) ;

		}



		Thread.sleep(1000L);

		treeCache.start();

		long  start = System.currentTimeMillis() ;
		for(int i =0 ;i <10 ;i++) {

//			byte[] bytes = client.getData().forPath(path);
//
//			if(bytes==null){
//				System.out.println("data not set");
//				Thread.sleep(1000L);
//				continue;
//			}
//			System.out.println(bytes.length);





			System.out.println(treeCache.getCurrentData(path));

		}



		treeCache.close();

		client.close();


	}

}
