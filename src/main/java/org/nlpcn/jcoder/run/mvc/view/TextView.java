package org.nlpcn.jcoder.run.mvc.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;
import org.nutz.mvc.View;

import com.alibaba.fastjson.JSONObject;

/**
 * 直接返回正文
 * 
 * @author ansj
 * 
 */
public class TextView implements View {

	private int httpStatus = ApiException.OK;
	private Object result;

	public TextView(int httpStatus, Object result) {
		this.result = result;
		this.httpStatus = httpStatus;
	}

	public TextView(Object result) {
		this.result = result;
	}

	public TextView() {
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
		resp.setContentType("text/html");
		

		resp.getWriter().write(obj.toString());
		resp.flushBuffer();
	}

}