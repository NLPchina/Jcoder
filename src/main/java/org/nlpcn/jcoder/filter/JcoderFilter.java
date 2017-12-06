package org.nlpcn.jcoder.filter;

import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.run.java.DynamicEngine;
import org.nlpcn.jcoder.run.mvc.ApiActionHandler;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.NutFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JcoderFilter extends NutFilter {

	private ApiActionHandler apiHandler;

	private String host;

	private static final byte[] AUTH_ERR = "no right to call this server".getBytes();

	public void init(FilterConfig conf) throws ServletException {
		super.init(conf);
		apiHandler = new ApiActionHandler(conf);
		host = StaticValue.getConfigHost();
	}

	@Override
	public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();


		/**
		 * 先走代理服务
		 */

		if (!StaticValue.IS_LOCAL
				&& request.getHeader(ProxyService.PROXY_HEADER) == null
				&& StaticValue.getSystemIoc().get(ProxyService.class, "proxyService").service(request, response, "http://www.sina.com")) {
			response.getOutputStream().flush();
			response.getOutputStream().close();
			return;
		}

		if (path.startsWith("/api/")) {
			_doFilter(chain, request, response);
		} else {
			if (StringUtil.isBlank("host") || "*".equals(host) || host.equals(request.getServerName()) || request.getServletPath().startsWith("/apidoc")) {
				super.doFilter(request, response, chain);
			} else {
				_doAuthoErr(request, response);
			}
		}

	}

	private void _doAuthoErr(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			request.getSession().invalidate();
			response.setStatus(403);
			response.setHeader("Cache-Control", "no-cache");
			response.setContentType("text/html");
			response.setContentLength(AUTH_ERR.length);
			response.getOutputStream().write(AUTH_ERR);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			response.getOutputStream().flush();
			response.getOutputStream().close();
		}
	}

	private void _doFilter(final FilterChain chain, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Mvcs.setIoc(StaticValue.getUserIoc()); // reset ioc

		Mvcs.setServletContext(sc);
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(DynamicEngine.getInstance().getParentClassLoader());
			Mvcs.set(this.selfName, request, response);

			if (!apiHandler.handle(request, response)) {
				try {
					new JsonView().render(request, response, Restful.instance(false, "api not found ! may be it not actived!", null, ApiException.NotFound));
				} catch (Throwable e) {
					nextChain(request, response, chain);
				}
			}

		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
			Mvcs.set(null, null, null);
			Mvcs.ctx().removeReqCtx();
			Mvcs.setServletContext(null);
			if (request.getSession(false) != null && request.getSession(false).getAttribute("user") == null) { //if session is empty
				request.getSession().invalidate();
			}
		}
	}

	public ApiActionHandler getApiHandler() {
		return apiHandler;
	}

}
