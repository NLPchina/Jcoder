package org.nlpcn.jcoder.run.java;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.filter.HostFilter;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;

/**
 * 测试文档搜索
 * @author Ansj
 *
 */
public class ApiTest {

	@Inject
	private Logger log;

	/**
	 * 输入姓名返回ok字符串
	 * @param name 输入的名字
	 * @return 结果信息
	 * @throws InterruptedException 线程停止时抛出
	 */
	@DefaultExecute
	@Filters(@By(type = HostFilter.class))
	public String defaultTest(String name) throws InterruptedException {
    	Integer a = null ;
		return StaticValue.okMessage("hello " + name);
	}

}
