package org.nlpcn.jcoder.run.java;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nutz.ioc.loader.annotation.Inject;

public class TestRun {

	@Inject
	private Logger laog;

	@Execute
	public Object defaultTest(String name) throws InterruptedException {
		User user = new User();
		user.setName("ansj");
		user.setPassword("fuck");
		user.setId(100L);
		return user;
	}

}
