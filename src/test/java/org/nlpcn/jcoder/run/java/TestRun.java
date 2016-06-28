package org.nlpcn.jcoder.run.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.run.execute.DefaultExecute;
import org.nlpcn.jcoder.run.execute.Execute;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.mvc.annotation.Param;

public class TestRun {

	@Inject
	private Logger log;

	@Inject
	private User user;

	@Execute
	public String wangchao(String name) {
		log.info("jetty hello wangchao !" + name);

		return "jetty hello wangchao !" + name;
	}

	@Execute
	public String test(@Param("name") String name, String bbb) throws InterruptedException {
		log.info("execute----------------aaa----------");
		Thread.sleep(1000000L);
		return "execute : " + user.getName() + " " + name + "\t" + bbb;
	}

	@DefaultExecute
	public String defaultTest() throws InterruptedException {
		Thread.sleep(100000L);
		return "default : " + user.getName();
	}

	public static void main(String[] args) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		TestRun tr = new TestRun();

		Method method = tr.getClass().getDeclaredMethod("test", String.class);

		Object invoke = method.invoke(tr, new Object[] { null });

		System.out.println(invoke);
	}
}
