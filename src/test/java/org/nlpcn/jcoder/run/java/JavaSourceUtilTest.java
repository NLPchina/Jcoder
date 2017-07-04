package org.nlpcn.jcoder.run.java;

import static org.junit.Assert.*;

import org.junit.Test;


public class JavaSourceUtilTest {

	@Test
	public void test() {
		String findClassName = JavaSourceUtil.findClassName("public class AAA{}") ;
		assertEquals(findClassName, "AAA");
		
		findClassName = JavaSourceUtil.findClassName("public class AAA{\n}") ;
		assertEquals(findClassName, "AAA");
	}

}
