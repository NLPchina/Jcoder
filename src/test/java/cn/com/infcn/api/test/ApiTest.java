package cn.com.infcn.api.test;

import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.util.Restful;

/**
 * 测试文档搜索
 *
 * @author Ansj
 */

public class ApiTest {


	/**
	 * @return
	 * @throws Exception
	 */
	@Execute
	public Object test(String name) throws Exception {
		return Restful.ok().msg("hello " + name);
	}

}
