package org.nlpcn.jcoder.util;

import org.junit.Test;
import org.nutz.mvc.Mvcs;

public class TestingTest {

	@Test
	public void test() throws InterruptedException, Exception {
		Testing.startServer(null, new String[]{"cn.com.infcn.api.test"});
	}

}
