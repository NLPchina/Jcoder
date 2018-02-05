package org.nlpcn.jcoder.run.java;

import org.nlpcn.jcoder.run.annotation.Execute;
import org.nutz.ioc.loader.annotation.Inject;

public class TestRun {


	@Execute
	public String defaultTest(String name) throws InterruptedException {
		return "Hello Jcoder " + name;
	}

}