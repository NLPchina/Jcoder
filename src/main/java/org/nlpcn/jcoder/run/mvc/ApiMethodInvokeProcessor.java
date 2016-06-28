package org.nlpcn.jcoder.run.mvc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nutz.lang.Lang;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.impl.processor.AbstractProcessor;

public class ApiMethodInvokeProcessor extends AbstractProcessor {
	public void process(ActionContext ac) throws Throwable {
		Task module = (Task) ac.getModule();
		Method method = ac.getMethod();
		Object[] args = ac.getMethodArgs();
		try {
			ac.setMethodReturn(new JavaRunner(module).compile().instanceObjByIoc().execute(method, args));
			doNext(ac);
		} catch (IllegalAccessException e) {
			throw Lang.unwrapThrow(e);
		} catch (IllegalArgumentException e) {
			throw Lang.unwrapThrow(e);
		} catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}
}