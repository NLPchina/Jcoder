package org.nlpcn.jcoder.run.rpc.domain;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;

public class RpcRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String messageId;
	private String groupName;
	private String className;
	private String methodName;
	private String tokenStr;
	private boolean debug;
	private JSON arguments;

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

	public JSON getArguments() {
		return arguments;
	}

	public void setArguments(JSON arguments) {
		this.arguments = arguments;
	}

	public String getTokenStr() {
		return tokenStr;
	}

	public void setTokenStr(String tokenStr) {
		this.tokenStr = tokenStr;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
