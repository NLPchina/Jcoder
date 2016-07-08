package org.nlpcn.jcoder.run;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaRunner;

public class CodeRunner {
	
	public static Object run(Task task) throws Exception {
		switch (task.getCodeType()) {
		case "java":
			new JavaRunner(task).compile().instance().execute() ;
			return null ;
		default:
			break;
		}
		return null;
	}
	
	
}
