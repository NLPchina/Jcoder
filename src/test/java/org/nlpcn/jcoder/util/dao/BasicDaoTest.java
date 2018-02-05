package org.nlpcn.jcoder.util.dao;

import org.junit.Test;

public class BasicDaoTest {

	@Test
	public void test() {
		BasicDao basicDao = new BasicDao("jdbc:mysql://192.168.10.103:3306/infcn_mss?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull", "root1", "98765432");
		System.out.println(basicDao.select("select * from etl_worker"));
	}

}
