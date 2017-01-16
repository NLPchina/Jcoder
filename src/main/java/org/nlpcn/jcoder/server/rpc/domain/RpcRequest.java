package org.nlpcn.jcoder.server.rpc.domain;

import java.io.Serializable;

public class RpcRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String messageId;
	private String className;
	private String methodName;
	private Object[] arguments;

	public RpcRequest() {
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

}
