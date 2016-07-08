package org.nlpcn.jcoder.util;

public class JsonResult {

	private boolean ok;

	private String message;

	private Exception exception;

	public JsonResult(boolean ok) {
		this.ok = ok;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

}