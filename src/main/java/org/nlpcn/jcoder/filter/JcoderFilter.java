package org.nlpcn.jcoder.filter;

import org.nlpcn.jcoder.run.mvc.ApiActionHandler;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.NutFilter;
import org.nutz.mvc.config.FilterNutConfig;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JcoderFilter extends NutFilter {

	private static final String JCODER_NAME = "jcoder";

	private ApiActionHandler apiHandler;

	private String host;

	private static final byte[] AUTH_ERR = "no right to call this server".getBytes();

	public void init(FilterConfig conf) throws ServletException {
		super.init(conf);
		FilterNutConfig config = new FilterNutConfig(conf);
		apiHandler = new ApiActionHandler(config);
		host = StaticValue.getConfigHost();
	}

	@Override
	public void doFilter(final ServletRequest req, final ServletResponse resp, final FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;
		String path = request.getServletPath();


		if (path.startsWith("/api/")) {
			_doFilter(chain, request, response);
		} else {
			if (StringUtil.isBlank(host) || "*".equals(host) || StaticValue.SELF_HOST.equals(request.getServerName()) || host.equals(request.getServerName()) || request.getServletPath().startsWith("/apidoc")) {
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
		Mvcs.setServletContext(sc);
		try {
			Mvcs.set(Thread.currentThread().getName(), request, response);
			Mvcs.setIoc(Mvcs.ctx().iocs.get(this.selfName));
			Mvcs.setAtMap(Mvcs.ctx().atMaps.get(this.selfName));
			if (!apiHandler.handle(request, response)) {
				try {
					new JsonView().render(request, response, Restful.instance(false, "api not found ! may be it not actived!", null, ApiException.NotFound));
				} catch (Throwable e) {
					nextChain(request, response, chain);
				}
			}
		} catch (Exception e) {
			try {
				new JsonView().render(request, response, Restful.instance(false, e.getMessage(), null, ApiException.ServerException));
			} catch (Throwable e1) {
				nextChain(request, response, chain);
			}
		} finally {
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
