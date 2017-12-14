package org.nlpcn.jcoder.run.mvc.processor;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.service.TaskService;
import org.nutz.ioc.Ioc;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.impl.processor.AbstractProcessor;

import java.lang.reflect.Method;

/**
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 * @author ansj
 * 设置ioc 和classloader
 */
public class ApiModuleProcessor extends AbstractProcessor {

	private Method method;
	private Object moduleObj;
	private ClassLoader loader;
	private Ioc ioc;

	@Override
	public void init(NutConfig config, ActionInfo ai) throws Throwable {
		Task task = TaskService.findTaskByCache(ai.getModuleType().getSimpleName());
		JarService jarService = JarService.getOrCreate(task.getGroupName());
		ioc = jarService.getIoc();
		loader = jarService.getEngine().getClassLoader();
		method = ai.getMethod();
		moduleObj = new JavaRunner(task).compile().instance().getTask();
	}

	public void process(ActionContext ac) throws Throwable {
		Thread.currentThread().setContextClassLoader(loader);
		Mvcs.setIoc(ioc);
		ac.setModule(moduleObj);
		ac.setMethod(method);
		doNext(ac);
	}

}
