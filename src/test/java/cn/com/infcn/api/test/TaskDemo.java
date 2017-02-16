package cn.com.infcn.api.test;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;

public class TaskDemo {

	@Inject
	private Logger log;

	@DefaultExecute
	public Object execute() {
		return StaticValue.getUserIoc().getNames();
	}
}
