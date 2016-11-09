package org.nlpcn.jcoder.server.rpc.client;

import java.io.Serializable;
import java.lang.reflect.Method;

public class RpcRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private String messageId;
	private String className;
	private String methodName;
	private Object[] arguments;

	private boolean syn = true; // 是否同步
	private boolean jsonStr = false; // 是否jsonstr返回
	private long timeout = 10000; // 超时时间,<=0为不做限制
	
	public RpcRequest(){}
	
	public RpcRequest(String messageId, Class<?> clz, Method method, boolean syn, boolean jsonStr, long timeout, Object[] arguments) {
		this.messageId = messageId;
		this.className = clz.getSimpleName();
		this.methodName = method.getName();
		this.arguments = arguments;
		this.syn = syn;
		this.jsonStr = jsonStr;
		this.timeout = timeout;
	}
	
	public RpcRequest(String messageId, String className, String methodName, boolean syn, boolean jsonStr, long timeout, Object[] arguments) {
		this.messageId = messageId;
		this.className = className;
		this.methodName = methodName;
		this.arguments = arguments;
		this.syn = syn;
		this.jsonStr = jsonStr;
		this.timeout = timeout;
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

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void setJsonStr(boolean jsonStr) {
		this.jsonStr = jsonStr;
	}

	public void setSyn(boolean syn) {
		this.syn = syn;
	}

	public boolean isSyn() {
		return syn;
	}

	public long getTimeout() {
		return timeout;
	}

}
