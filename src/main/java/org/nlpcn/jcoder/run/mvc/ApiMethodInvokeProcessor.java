package org.nlpcn.jcoder.run.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.util.DateUtils;
import org.nutz.lang.Lang;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.impl.processor.AbstractProcessor;

public class ApiMethodInvokeProcessor extends AbstractProcessor {

	private AtomicLong al = new AtomicLong();

	public void process(ActionContext ac) throws Throwable {
		String threadName = null;
		Task module = (Task) ac.getModule();
		Method method = ac.getMethod();
		Object[] args = ac.getMethodArgs();
		try {
			threadName = module.getName() + "@" + ac.getRequest().getRemoteAddr() + "@" + DateUtils.formatDate(new Date(), "yyyyMMddHHmmss") + "@" + al.getAndIncrement();
			ThreadManager.add2ActionTask(threadName, Thread.currentThread());
			Object result = new JavaRunner(module).compile().instanceObjByIoc().execute(method, args);
			ac.setMethodReturn(result);
			doNext(ac);
		} catch (IllegalAccessException e) {
			throw Lang.unwrapThrow(e);
		} catch (IllegalArgumentException e) {
			throw Lang.unwrapThrow(e);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		} finally {
			ThreadManager.removeActionIfOver(threadName);
		}
	}
}