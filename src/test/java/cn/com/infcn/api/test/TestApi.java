package cn.com.infcn.api.test;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.filter.TokenFilter;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Param;

/**
 * 测试api
 * 
 * @author Ansj
 *
 */
public class TestApi {

	@Inject
	private Logger log;

	@Execute
	@Filters(@By(type=TokenFilter.class))
	public String test(@Param("_name") String name) {
		return name;
	}

}
