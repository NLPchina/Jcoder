package org.nlpcn.jcoder.run.mvc.processor;

import java.lang.reflect.Method;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.service.TaskService;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.impl.processor.AbstractProcessor;

/**
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 * @author ansj
 * 
 */
public class ApiModuleProcessor extends AbstractProcessor {

	private Method method;
	private Object moduleObj;

	@Override
	public void init(NutConfig config, ActionInfo ai) throws Throwable {
		Task task = TaskService.findTaskByCache(ai.getModuleType().getSimpleName());
		method = ai.getMethod();
		moduleObj = new JavaRunner(task).compile().instance().getTask();
	}

	public void process(ActionContext ac) throws Throwable {
		ac.setModule(moduleObj);
		ac.setMethod(method);
		doNext(ac);
	}

}
