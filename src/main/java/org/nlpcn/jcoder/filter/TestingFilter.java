package org.nlpcn.jcoder.filter;

import org.nlpcn.jcoder.domain.KeyValue;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.run.annotation.Single;
import org.nlpcn.jcoder.run.mvc.processor.ApiAdaptorProcessor;
import org.nlpcn.jcoder.run.mvc.processor.ApiCrossOriginProcessor;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.lang.Mirror;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.NutFilter;
import org.nutz.resource.Scans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestingFilter extends NutFilter {

	private static final Logger LOG = LoggerFactory.getLogger(TestingFilter.class);

	public static Map<String, KeyValue<Method, Object>> methods = new HashMap<>();

	public static Ioc ioc = null;

	public static void init(Ioc ioc, String... packages) throws IOException {
		TestingFilter.ioc = ioc;
		Map<String, KeyValue<Method, Object>> tempMethods = new HashMap<>();
		for (String pk : packages) {
			List<Class<?>> list = Scans.me().scanPackage(pk);
			list.forEach(cla -> {
				Object obj = null;
				Single single = Mirror.getAnnotationDeep(cla, Single.class);

				for (Method method : cla.getMethods()) {
					if (!Modifier.isPublic(method.getModifiers()) || method.isBridge() || method.getDeclaringClass() != cla) {
						continue;
					}

					if (Mirror.getAnnotationDeep(method, Execute.class) != null) {
						if (obj == null && (single == null || single.value())) { //用懒加载方式
							try {
								obj = cla.newInstance();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						String path = cla.getSimpleName() + "/" + method.getName();
						LOG.info("find api : " + path);
						tempMethods.put(path, KeyValue.with(method, obj));
					}
				}

				obj = null;
			});
		}

		methods = tempMethods;
	}

	@Override
	public void init(FilterConfig conf) throws ServletException {
		sc = conf.getServletContext();
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		Mvcs.setIoc(ioc); // reset ioc

		Mvcs.setServletContext(sc);

		ActionContext ac = new ActionContext();

		String apiPath = request.getServletPath().substring(5);

		try {
			Mvcs.set("testing_filter", request, response);

			ac.setRequest(request).setResponse(response).setServletContext(request.getServletContext());
			Mvcs.setActionContext(ac);

			KeyValue<Method, Object> keyValue = methods.get(apiPath);

			if (keyValue == null) {
				throw new ApiException(404, "api not found " + apiPath);
			}

			// add encoding
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");

			new ApiCrossOriginProcessor().process(ac); // add cross origin

			Method method = keyValue.getKey();
			Class<?> clz = method.getDeclaringClass();

			ac.setModule(keyValue.getValue() == null ? clz.newInstance() : keyValue.getValue());
			ac.setMethod(method);

			ActionInfo actionInfo = new ActionInfo();
			actionInfo.setMethod(method);
			actionInfo.setModuleType(clz);

			//set url to args
			ApiAdaptorProcessor apiAdaptorProcessor = new ApiAdaptorProcessor();
			apiAdaptorProcessor.init(Mvcs.getNutConfig(), actionInfo);
			apiAdaptorProcessor.process(ac);

			//ioc set
			Mirror<?> mirror = Mirror.me(clz);
			for (Field field : mirror.getFields()) {
				Inject inject = field.getAnnotation(Inject.class);
				if (inject != null) {
					field.setAccessible(true);
					if (field.getType().equals(org.slf4j.Logger.class)) {
						mirror.setValue(ac.getModule(), field, LoggerFactory.getLogger(ac.getModule().getClass()));
					} else {
						mirror.setValue(ac.getModule(), field,
								ioc.get(field.getType(), StringUtil.isBlank(inject.value()) ? field.getName() : inject.value()));
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
