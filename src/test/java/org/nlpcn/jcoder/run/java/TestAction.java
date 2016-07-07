package org.nlpcn.jcoder.run.java;

import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.GET;
import org.nutz.mvc.filter.CrossOriginFilter;

public class TestAction {

	@Filters(@By(type=CrossOriginFilter.class))
	public Object test(String name, int age) {
		return "a";
	}

}
