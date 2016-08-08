package org.nlpcn.jcoder.server.rpc.client;

import java.io.Serializable;

public class RpcResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String messageId;
	private String error;
	private Object result;
	private boolean file ;
	
	public RpcResponse(){}

	public RpcResponse(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public boolean isFile() {
		return file;
	}

	public void setFile(boolean file) {
		this.file = file;
	}
	

}
