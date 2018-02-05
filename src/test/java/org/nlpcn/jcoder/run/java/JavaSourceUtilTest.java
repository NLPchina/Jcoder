package org.nlpcn.jcoder.run.java;

import org.junit.Test;
import org.nlpcn.jcoder.run.CodeException;

import static org.junit.Assert.assertEquals;


public class JavaSourceUtilTest {

	@Test
	public void test() throws CodeException {
		String findClassName = new JavaSourceUtil("public class AAA{}").getClassName();
		assertEquals(findClassName, "AAA");

		findClassName = new JavaSourceUtil("public class AAA{\n}").getClassName();
		assertEquals(findClassName, "AAA");
	}

}
