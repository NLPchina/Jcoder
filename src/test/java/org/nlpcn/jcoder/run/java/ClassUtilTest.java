package org.nlpcn.jcoder.run.java;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;

public class ClassUtilTest {

	@Test
	public void test() throws MalformedURLException, ClassNotFoundException {
		Class<?> forName = Class.forName("org.nlpcn.jcoder.run.java.ClassUtil") ;
		
		System.out.println(forName);
		
		System.out.println(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/nlpcn/jcoder/run/java/ClassUtilTest.class"));
	}
	

}
