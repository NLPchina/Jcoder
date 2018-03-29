package org.nlpcn.jcoder.util;

import org.junit.Test;
import org.nlpcn.jcoder.domain.ClassDoc;

/**
 * Created by Ansj on 29/03/2018.
 */
public class JavaDocUtilTest {

	@Test
	public void testSchdule() throws Exception {
		String code = "package cn.com.infcn.irsp;\n" +
				"\n" +
				"import org.nlpcn.jcoder.run.annotation.Execute;\n" +
				"import org.nlpcn.jcoder.run.annotation.Schedule;\n" +
				"\n" +
				"\n" +
				"/**\n" +
				" * Created by Ansj on 29/03/2018.\n" +
				" */\n" +
				"@Schedule(\"0/3 * * * * ?\")\n" +
				"public class TestApi {\n" +
				"\n" +
				"\t@Execute\n" +
				"\tpublic void execute(){\n" +
				"\t\tSystem.out.println(\"100\");\n" +
				"\t}\n" +
				"}\n";

		ClassDoc parse = JavaDocUtil.parse(code);
		System.out.println(parse.getScheduleStr());

	}
}
