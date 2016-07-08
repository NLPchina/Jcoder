package org.nlpcn.jcoder.util;

public class Restful {

	private boolean ok = true;

	private String message;

	private Object obj;

	private int code = ApiException.OK;

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

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public Restful(boolean ok, String message, Object obj, int code) {
		this.ok = ok;
		this.message = message;
		this.obj = obj;
		this.code = code;
	}

	public Restful(boolean ok, String message, Object obj) {
		this.ok = ok;
		this.message = message;
		this.obj = obj;
	}

	public Restful(String message, Object obj) {
		this.message = message;
		this.obj = obj;
	}

	public Restful(Object obj) {
		this.obj = obj;
	}

	public static Restful instance(boolean ok, String message, Object obj, int code) {
		return new Restful(ok, message, obj, code);
	}

	public static Restful instance(boolean ok, String message, Object obj) {
		return new Restful(ok, message, obj);
	}

	public static Restful instance(String message, Object obj) {
		return new Restful(message, obj);
	}

	public static Restful instance(Object obj) {
		return new Restful(obj);
	}
}
