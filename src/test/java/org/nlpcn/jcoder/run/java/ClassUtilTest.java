package org.nlpcn.jcoder.run.java;

import org.junit.Test;

import java.net.MalformedURLException;

public class ClassUtilTest {

	@Test
	public void test() throws MalformedURLException, ClassNotFoundException {
		Class<?> forName = Class.forName("org.nlpcn.jcoder.run.java.ClassUtil");

		System.out.println(forName);

		System.out.println(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/nlpcn/jcoder/run/java/ClassUtilTest.class"));
	}


}
