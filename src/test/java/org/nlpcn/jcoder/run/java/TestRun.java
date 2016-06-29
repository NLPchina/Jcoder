package org.nlpcn.jcoder.run.java;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nutz.ioc.loader.annotation.Inject;

public class TestRun {

	@Inject
	private Logger log;

	@DefaultExecute
	public String defaultTest(String name) throws InterruptedException {
		return "Hello Jcoder " + name;
	}

}
