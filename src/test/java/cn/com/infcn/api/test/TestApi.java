package cn.com.infcn.api.test;

import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nutz.ioc.loader.annotation.Inject;
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

	@Inject
	private ExecutorService threadPool;

	@Inject
	private User user;

	@Execute
	public User test(@Param("_name") String name) {
		return user;
	}

}
