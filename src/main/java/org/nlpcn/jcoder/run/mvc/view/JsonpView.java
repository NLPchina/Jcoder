package org.nlpcn.jcoder.run.mvc.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;
import org.nutz.mvc.View;

import com.alibaba.fastjson.JSONObject;

public class JsonpView implements View {

	private static final String ORIGIN = "*";
	private static final String METHODS = "get, post, put, delete, options";
	private static final String HEADERS = "origin, content-type, accept";
	private static final String CREDENTIALS = "true";

	private int httpStatus = ApiException.OK;
	private Object result;
	private String methodName;

	public JsonpView(int httpStatus, String methodName, Object result) {
		this.httpStatus = httpStatus;
		this.methodName = methodName;
		this.result = result;
	}

	public JsonpView(String methodName, Object result) {
		this.methodName = methodName;
		this.result = result;
	}

	public JsonpView(String methodName) {
		this.methodName = methodName;
	}

	@Override
	public void render(HttpServletRequest req, HttpServletResponse resp, Object obj) throws Throwable {
		if (obj instanceof Restful) {
			resp.setStatus(((Restful) obj).code());
		} else {
			resp.setStatus(httpStatus);
		}
		
		if (obj == null) {
			obj = result;
		}
		
		if (obj == null) {
			return;
		}
		
		resp.setHeader("Cache-Control", "no-cache");
		resp.setContentType("text/javascript");
		// crossorigin
		resp.addHeader("Access-Control-Allow-Origin", ORIGIN);
		resp.addHeader("Access-Control-Allow-Methods", METHODS);
		resp.addHeader("Access-Control-Allow-Headers", HEADERS);
		resp.addHeader("Access-Control-Allow-Credentials", CREDENTIALS);


		StringBuilder sb = new StringBuilder();

		sb.append(methodName);
		sb.append("(");
		sb.append(toString(obj));
		sb.append(")");

		resp.getWriter().write(sb.toString());
		resp.flushBuffer();
	}

	public String toString(Object result) {
		if (result instanceof String) {
			return (String) result;
		}
		return JSONObject.toJSONString(result);
	}
}
