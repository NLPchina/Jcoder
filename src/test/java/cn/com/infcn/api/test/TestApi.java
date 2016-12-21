package cn.com.infcn.api.test;

import java.io.IOException;
import java.util.Date;

import org.nlpcn.jcoder.filter.TokenFilter;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.Testing;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.slf4j.Logger;

/**
 * 测试api
 * 
 * @author Ansj
 *
 */

public class TestApi {
	

	@Inject
	private Logger log;

	/**
	 * 测试创建api
	 * 
	 * @param name 姓名
	 * @param aaa 日期
	 * @return 拼接好的字符串
	 */
	@Execute
	@Filters(@By(type=TokenFilter.class) )
	public Restful test(String name, Date aaa) {
		return Restful.instance("ok");
	}

	public static void main(String[] args) throws IOException {
		Testing.diffCode("/Users/sunjian/Documents/workspace/jcoder/src/test/java", "localhost:8080");
	}
}
