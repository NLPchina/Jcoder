package org.nlpcn.jcoder.server.rpc.domain;

import java.io.Serializable;

import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;

public class RpcResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private String messageId;
	private boolean ok = true;
	private Object obj;
	private String message;
	private int code = ApiException.OK;

	public RpcResponse() {
	}

	public RpcResponse(String messageId, Restful restful) {
		this.messageId = messageId;
		this.ok = restful.isOk();
		this.obj = restful.getObj();
		this.message = restful.getMessage();
		this.code = restful.code();
	}

	public RpcResponse(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public Object getObj() {
		return obj;
	}

	/**
	 * if obj instanceof Restful , it parse it field to self
	 * 
	 * @param obj
	 */
	public void setObj(Object obj) {
		if (obj instanceof Restful) {
			Restful restful = (Restful) obj;
			this.ok = restful.isOk();
			this.obj = restful.getObj();
			this.message = restful.getMessage();
			this.code = restful.code();
		} else {
			this.obj = obj;
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
