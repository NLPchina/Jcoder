package org.nlpcn.jcoder.domain;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.nlpcn.jcoder.run.CodeException;
import org.nlpcn.jcoder.run.CodeRuntimeException;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.Ioc;

/**
 * the code init info
 * 
 * @author ansj
 *
 */
public class CodeInfo {

	private static final Logger LOG = Logger.getLogger(CodeInfo.class);

	private Class<?> classz;

	private Object JavaObject;

	private List<Method> executeMethods = new ArrayList<>();

	private Method defaultMethod;

	private Ioc ioc;

	private boolean single = true;

	public boolean iocChanged() {
		return this.ioc != StaticValue.getUserIoc();
	}

	public void setioc(Ioc ioc) {
		this.ioc = ioc;
	}

	public Class<?> getClassz() {
		return classz;
	}

	public void setClassz(Class<?> classz) {
		this.classz = classz;
	}

	public Object getJavaObject() {
		return JavaObject;
	}

	public void setJavaObject(Object javaObject) {
		JavaObject = javaObject;
	}

	public List<Method> getExecuteMethods() {
		return executeMethods;
	}

	public void setDefaultMethod(Method method) {
		if (defaultMethod == null) {
			this.defaultMethod = method;
		} else {
			LOG.warn(classz.getName() + " has being more than one @DefaultExecute annotation method : " + defaultMethod.getName() + " so skip method : " + method.getName());
		}
		addMethod(method);
	}

	public boolean isSingle() {
		return single;
	}

	public void setSingle(boolean single) {
		this.single = single;
	}

	public void addMethod(Method method) {
		this.executeMethods.add(method);
	}

	public Method getDefaultMethod() {
		if (defaultMethod == null) {
			if (executeMethods.size() == 0) {
				throw new CodeRuntimeException(classz.getName() + " not have any @Execute or @DefaultExecute annotation you must set one ");
			}
			defaultMethod = executeMethods.get(0);
			LOG.warn(defaultMethod.getDeclaringClass().getName() + " none @DefaultExecute annotation method : so set the firest method to DefaultExecute function:" + defaultMethod.getName());
		}
		return defaultMethod;
	}

}
