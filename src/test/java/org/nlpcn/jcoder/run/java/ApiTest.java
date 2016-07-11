package org.nlpcn.jcoder.run.java;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.annotation.Cache;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.run.annotation.Single;
import org.nutz.ioc.loader.annotation.Inject;

/**
 * 测试文档搜索
 * @author Ansj
 *
 */
@Single(true)
public class ApiTest {
	

	@Inject
	private Logger log;
	Integer a = 0  ;

	/**
	 * 输入姓名返回ok字符串
	 * @param name 输入的名字
	 * @return 
	 * @return 结果信息
	 * @throws InterruptedException 线程停止时抛出
	 */
	@DefaultExecute
	@Cache
	public Integer defaultTest(HttpServletRequest req) throws InterruptedException {
		System.out.println(req);
		a++ ;
		return a ;
	}

}
