package cn.com.infcn.api.test;

import org.nlpcn.jcoder.filter.TokenFilter;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Param;
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

	@Execute
	public String test() {
		for (int i = 0; i < 100; i++) {
			log.info(""+i);
		}
		return "ok" ;
	}

}
