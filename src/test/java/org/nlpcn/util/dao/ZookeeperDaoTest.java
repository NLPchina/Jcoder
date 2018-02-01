package org.nlpcn.util.dao;

import org.junit.Test;
import org.nlpcn.jcoder.util.dao.ZookeeperDao;

import java.util.List;

/**
 * Created by Ansj on 09/01/2018.
 */
public class ZookeeperDaoTest {

	@Test
	public void test() throws Exception {
		ZookeeperDao zd = new ZookeeperDao("192.168.3.137:2181");

		zd.start();

		List<String> strings = zd.getZk().getChildren().forPath("/jcoder/group/自然语言处理");

		System.out.println(strings);

		zd.close();
	}
}
