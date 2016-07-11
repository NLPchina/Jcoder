package org.nlpcn.jcoder.run.java;

import org.nlpcn.jcoder.run.annotation.Execute;

public class TestAction {

	@Execute
	public Object test(String name, int age) throws InterruptedException {
		Thread.sleep(100000);
		return "a";
	}

}
