package org.nlpcn.jcoder.run.mvc.processor;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.run.rpc.Rpcs;
import org.nlpcn.jcoder.service.TaskService;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.impl.processor.AbstractProcessor;

import java.lang.reflect.Method;

/**
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 * @author ansj 设置ioc 和classloader
 */
public class ApiModuleProcessor extends AbstractProcessor {

	private Method method;
	private Task moduleObj;
	private String groupName;

	@Override
	public void init(NutConfig config, ActionInfo ai) throws Throwable {
		String path = ai.getPaths()[0];
		String[] split = path.split("/");
		Task task = TaskService.findTaskByCache(split[2], split[3]);
		method = ai.getMethod();
		moduleObj = new JavaRunner(task).compile().instance().getTask();
		groupName = task.getGroupName();
	}

	public void process(ActionContext ac) throws Throwable {
		ac.setModule(moduleObj);
		ac.setMethod(method);
		Rpcs.ctx().setGroupName(groupName);
		Thread.currentThread().setContextClassLoader(moduleObj.codeInfo().getClassLoader());//设置classloader
		doNext(ac);
	}

}
