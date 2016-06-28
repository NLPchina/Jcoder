package org.nlpcn.jcoder.util;

import org.nutz.mvc.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 直接返回正文
 * 
 * @author ansj
 * 
 */
public class TextView implements View {

	private int httpStatus = ApiException.OK;

	public TextView(Object data) {
		this.data = data;
	}

	public TextView(int httpStatus, Object data) {
		this.httpStatus = httpStatus;
		this.data = data;
	}

	private Object data;

	public void setData(Object data) {
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	@Override
	public void render(HttpServletRequest req, HttpServletResponse resp, Object obj) throws Throwable {
		resp.setStatus(httpStatus);
		resp.setHeader("Cache-Control", "no-cache");
		resp.setContentType("text/html");
		resp.getWriter().write(data.toString());
		resp.flushBuffer();
	}

}