package org.nlpcn.jcoder.run.java;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;
import org.nlpcn.jcoder.run.CodeException;
import org.nlpcn.jcoder.scheduler.TaskException;

public class DynamicEngineTest {

	@Test
	public void test() throws IOException, CodeException {

//		URLClassLoader ucl = new URLClassLoader(new URL[] {
//				new File("D:/Program Files/gradle-2.14/caches/modules-2/files-2.1/org.nutz/nutz/1.r.56.r3/d0300412d4907542477e663d916bc32baae1d6e/nutz-1.r.56.r3-sources.jar").toURI().toURL() },
//				ClassLoader.getSystemClassLoader());
//
//		try {
//			DynamicEngine.flush(ucl);
//		} catch (TaskException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		DynamicEngine de = DynamicEngine.getInstance();
//
//		Class<?> javaCodeToClass = de.javaCodeToClass("\n" + "package org.nlpcn.jcoder.run.java;\n" + "\n" + "\n" + "import org.nlpcn.jcoder.util.StaticValue;\n"
//				+ "import org.nutz.mvc.annotation.At;\n" + "import org.nutz.mvc.annotation.Param;\n" + "\n" + "public class TestRun {\n" + "\n" + "	@At(\"ok\")\n"
//				+ "	public String test(@Param(\"aa\") String aa) {\n" + "		return StaticValue.OK;\n" + "	}\n" + "}");
//
//		for (Method method : javaCodeToClass.getMethods()) {
//			String name = method.getName();
//
//			if (!name.equals("test")) {
//				continue;
//			}
//			
//			Parameter[] parameters = method.getParameters() ;
//			
//			for (Parameter parameter : parameters) {
//				System.out.println(parameter.getName());
//			}
//		}
	}

}
