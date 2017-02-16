package org.nlpcn.jcoder.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.run.java.ClassUtil;
import org.nlpcn.jcoder.run.java.DynamicEngine;
import org.nlpcn.jcoder.run.mvc.processor.ApiAdaptorProcessor;
import org.nlpcn.jcoder.run.mvc.processor.ApiCrossOriginProcessor;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.lang.Mirror;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.NutFilter;
import org.nutz.resource.Scans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestingFilter extends NutFilter {

	private static final Logger LOG = LoggerFactory.getLogger(TestingFilter.class);
	
	private static Map<String,Method> methods = null ;

	public static void init(String... packages) throws IOException {
		Map<String,Method> tempMethods = new HashMap<>() ;
		for (String pk : packages) {
			List<Class<?>> list = Scans.me().scanPackage(pk);

			list.forEach(cla -> {
				for (Method method : cla.getMethods()) {
					String name = cla.getSimpleName() ;
					if (method.getAnnotationsByType(Execute.class) != null || method.getAnnotationsByType(DefaultExecute.class) != null) {
						
						tempMethods.put(cla.getName().substring(beginIndex, endIndex), value)
					}
				}
			});
		}
	}

	@Override
	public void init(FilterConfig conf) throws ServletException {
		sc = conf.getServletContext();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		Mvcs.setIoc(StaticValue.getUserIoc()); // reset ioc

		Mvcs.setServletContext(sc);
		ActionContext ac = new ActionContext();
		try {
			Mvcs.set("testing_filter", request, response);

			ac.setRequest(request).setResponse(response).setServletContext(request.getServletContext());
			Mvcs.setActionContext(ac);

			new ApiCrossOriginProcessor().process(ac);

			Class<?> clz = Class.forName("cn.com.infcn.mobile.api.LoginAction");

			ac.setModule(clz.newInstance());

			Map<String, Method> methodMap = new HashMap<>();
			for (Method method : clz.getMethods()) {
				if (!Modifier.isPublic(method.getModifiers()) || method.isBridge() || method.getDeclaringClass() != clz) {
					continue;
				}

				if (Mirror.getAnnotationDeep(method, DefaultExecute.class) != null || Mirror.getAnnotationDeep(method, Execute.class) != null) {

					if (methodMap.containsKey(method.getName())) {
						throw new RuntimeException("method name repeated: " + method.getName());
					}

					methodMap.put(method.getName(), method);
				}
			}

			Method method = methodMap.get("login");

			if (method == null) {
				throw new RuntimeException("not found method : ");
			}

			ac.setMethod(methodMap.get("login"));

			ActionInfo actionInfo = new ActionInfo();

			actionInfo.setMethod(ac.getMethod());
			actionInfo.setModuleType(clz);

			ApiAdaptorProcessor apiAdaptorProcessor = new ApiAdaptorProcessor();
			apiAdaptorProcessor.init(Mvcs.getNutConfig(), actionInfo);
			apiAdaptorProcessor.process(ac);

			Mirror<?> mirror = Mirror.me(clz);

			for (Field field : mirror.getFields()) {
				Inject inject = field.getAnnotation(Inject.class);
				if (inject != null) {
					field.setAccessible(true);
					if (field.getType().equals(org.apache.log4j.Logger.class)) {
						LOG.warn("org.apache.log4j.Logger Deprecated please use org.slf4j.Logger by LoggerFactory");
						mirror.setValue(ac.getModule(), field, org.apache.log4j.Logger.getLogger(ac.getModule().getClass()));
					} else if (field.getType().equals(org.slf4j.Logger.class)) {
						mirror.setValue(ac.getModule(), field, LoggerFactory.getLogger(ac.getModule().getClass()));
					} else {
						mirror.setValue(ac.getModule(), field,
								StaticValue.getUserIoc().get(field.getType(), StringUtil.isBlank(inject.value()) ? field.getName() : inject.value()));
					}
					field.setAccessible(false);
				}
			}

			Object invoke = ac.getMethod().invoke(ac.getModule(), ac.getMethodArgs());

			new JsonView().render(request, response, invoke);
		} catch (Throwable e) {
			e.printStackTrace();
			try {
				new JsonView().render(request, response, e);
			} catch (Throwable e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
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

	@Override
	public void destroy() {
		System.out.println("destroy");
	}

}
