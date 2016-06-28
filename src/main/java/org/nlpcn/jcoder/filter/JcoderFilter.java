package org.nlpcn.jcoder.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nlpcn.jcoder.run.mvc.ApiActionHandler;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Context;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.NutFilter;

public class JcoderFilter extends NutFilter {

	private ApiActionHandler apiHandler;

	public void init(FilterConfig conf) throws ServletException {
		super.init(conf);
		apiHandler = new ApiActionHandler(conf);
	}

	@Override
	public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();

		if (path.startsWith("/api/")) {
			_doFilter(chain, request, response);
		} else {
			super.doFilter(request, response, chain);
		}
	}

	private void _doFilter(final FilterChain chain, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Mvcs.setIoc(StaticValue.getUserIoc()); // reset ioc

		ServletContext prCtx = Mvcs.getServletContext();

		Mvcs.setServletContext(sc);

		String matchUrl = request.getServletPath() + Strings.sBlank(request.getPathInfo());

		String markKey = "nutz_ctx_mark";
		Integer mark = (Integer) request.getAttribute(markKey);
		if (mark != null) {
			request.setAttribute(markKey, mark + 1);
		} else {
			request.setAttribute(markKey, 0);
		}

		String preName = Mvcs.getName();
		Context preContext = Mvcs.resetALL();
		try {
			if (sp != null)
				request = sp.filter(request, response, Mvcs.getServletContext());
			Mvcs.set(this.selfName, request, response);
			if (!isExclusion(matchUrl)) {
				if (apiHandler.handle(request, response))
					return;
			}
			nextChain(request, response, chain);
		} finally {
			// 仅当forward/incule时,才需要恢复之前设置
			if (mark != null) {
				Mvcs.ctx().reqCtx(preContext);
				Mvcs.setServletContext(prCtx);
				Mvcs.set(preName, request, response);
				if (mark == 0) {
					request.removeAttribute(markKey);
				} else {
					request.setAttribute(markKey, mark - 1);
				}
			} else {
				Mvcs.set(null, null, null);
				Mvcs.ctx().removeReqCtx();
				Mvcs.setServletContext(null);
			}
		}
	}

	public ApiActionHandler getApiHandler() {
		return apiHandler;
	}
	
}
