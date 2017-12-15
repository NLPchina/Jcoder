package org.nlpcn.jcoder.run.mvc;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.nlpcn.jcoder.run.java.DynamicEngine;
import org.nlpcn.jcoder.run.mvc.processor.ApiMethodInvokeProcessor;
import org.nutz.lang.Lang;
import org.nutz.mvc.ActionChain;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.Processor;
import org.nutz.mvc.impl.NutActionChain;

public class JcoderActionChain implements ActionChain {

	private Processor head;

	private Processor errorProcessor;

	private Method method;

	private Integer lineNumber;

	/**
	 * 暴露出去执行动作链,为其他调用者使用其缓存
	 */
	private ApiMethodInvokeProcessor invokeProcessor;

	public JcoderActionChain(List<Processor> list, ApiMethodInvokeProcessor invokeProcessor, Processor errorProcessor, ActionInfo ai) {
		if (null != list) {
			Iterator<Processor> it = list.iterator();
			if (it.hasNext()) {
				head = it.next();
				Processor p = head;
				while (it.hasNext()) {
					Processor next = it.next();
					p.setNext(next);
					p = next;
				}
			}
		}
		this.errorProcessor = errorProcessor;
		this.method = ai.getMethod();
		this.lineNumber = ai.getLineNumber();
		this.invokeProcessor = invokeProcessor;
	}

	public void doChain(ActionContext ac) {
		if (null != head) {
			try {
				head.process(ac);
			} catch (Throwable e) {
				ac.setError(e);
				try {
					errorProcessor.process(ac);
				} catch (Throwable ee) {
					throw Lang.wrapThrow(ee);
				}
			}
		}
	}

	public ApiMethodInvokeProcessor getInvokeProcessor() {
		return invokeProcessor;
	}

	String methodStr;

	public String toString() {
		if (methodStr == null) {
			if (lineNumber != null) {
				String className = method.getDeclaringClass().getSimpleName();
				String methodName = method.getName();
				methodStr = String.format("%s.%s(%s.java:%d)", className, methodName, className, lineNumber);
			} else {
				methodStr = Lang.simpleMetodDesc(method);
			}
		}
		return methodStr;
	}
}
