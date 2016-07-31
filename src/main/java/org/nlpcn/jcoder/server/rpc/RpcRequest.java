package org.nlpcn.jcoder.server.rpc;

import java.io.Serializable;
import java.lang.reflect.Method;

public class RpcRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private String messageId;
	private String className;
	private String methodName;
	private Object[] arguments;

	private boolean jsonStr = false;

	private long timeout = 10000;

	public RpcRequest(String messageId, Class<?> clz, Method method, Object[] arguments) {
		this.messageId = messageId;
		this.className = clz.getName();
		this.methodName = method.getName();
		this.arguments = arguments;

	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	public boolean isJsonStr() {
		return jsonStr;
	}
	
	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void setJsonStr(boolean jsonStr) {
		this.jsonStr = jsonStr;
	}

}
