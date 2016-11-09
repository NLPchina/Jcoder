package cn.com.infcn.api.test;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nutz.ioc.loader.annotation.Inject;

public class TaskDemo {

	@Inject
	private Logger log;

	@DefaultExecute
	public void execute() {
		log.info("message on " + System.currentTimeMillis());
	}
}
