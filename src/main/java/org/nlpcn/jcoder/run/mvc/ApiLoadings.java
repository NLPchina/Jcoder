package org.nlpcn.jcoder.run.mvc;

import org.nlpcn.jcoder.run.annotation.Execute;
import org.nutz.ioc.annotation.InjectName;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.ClassMeta;
import org.nutz.lang.util.ClassMetaReader;
import org.nutz.lang.util.Context;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.*;
import org.nutz.mvc.annotation.*;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ApiLoadings {

	private static final Log log = Logs.get();

	public static ActionInfo createInfo(Class<?> type) {
		ActionInfo ai = new ActionInfo();
		evalEncoding(ai, Mirror.getAnnotationDeep(type, Encoding.class));
		evalHttpAdaptor(ai, Mirror.getAnnotationDeep(type, AdaptBy.class));
		evalActionFilters(ai, Mirror.getAnnotationDeep(type, Filters.class), null);
		evalPathMap(ai, Mirror.getAnnotationDeep(type, PathMap.class));
		evalOk(ai, Mirror.getAnnotationDeep(type, Ok.class));
		evalFail(ai, Mirror.getAnnotationDeep(type, Fail.class));
		evalAt(ai, Mirror.getAnnotationDeep(type, At.class), type.getSimpleName());
		evalActionChainMaker(ai, Mirror.getAnnotationDeep(type, Chain.class));
		evalModule(ai, type);
		if (Mvcs.DISPLAY_METHOD_LINENUMBER) {
			InputStream ins = type.getClassLoader().getResourceAsStream(type.getName().replace(".", "/") + ".class");
			if (ins != null) {
				try {
					ClassMeta meta = ClassMetaReader.build(ins);
					ai.setMeta(meta);
				} catch (Exception e) {
				}
			}
		}
		return ai;
	}

	public static ActionInfo createInfo(Method method) {
		ActionInfo ai = new ActionInfo();
		evalEncoding(ai, Mirror.getAnnotationDeep(method, Encoding.class));
		evalHttpAdaptor(ai, Mirror.getAnnotationDeep(method, AdaptBy.class));
		evalActionFilters(ai, Mirror.getAnnotationDeep(method.getDeclaringClass(), Filters.class), Mirror.getAnnotationDeep(method, Filters.class));
		evalActionChainMaker(ai, Mirror.getAnnotationDeep(method, Chain.class));
		evalHttpMethod(ai, method, Mirror.getAnnotationDeep(method, Execute.class));
		ai.setMethod(method);
		return ai;
	}

	public static void evalHttpMethod(ActionInfo ai, Method method, Execute execute) {
		if (Mirror.getAnnotationDeep(method, GET.class) != null)
			ai.getHttpMethods().add("GET");
		if (Mirror.getAnnotationDeep(method, POST.class) != null)
			ai.getHttpMethods().add("POST");
		if (Mirror.getAnnotationDeep(method, PUT.class) != null)
			ai.getHttpMethods().add("PUT");
		if (Mirror.getAnnotationDeep(method, DELETE.class) != null)
			ai.getHttpMethods().add("DELETE");

		if (execute != null) {
			for (String m : execute.methods()) {
				ai.getHttpMethods().add(m.toUpperCase());
			}
		}
	}

	public static void evalActionChainMaker(ActionInfo ai, Chain cb) {
		if (null != cb) {
			ai.setChainName(cb.value());
		}
	}

	public static void evalAt(ActionInfo ai, At at, String def) {
		if (null != at) {
			if (null == at.value() || at.value().length == 0) {
				ai.setPaths(Lang.array("/" + def.toLowerCase()));
			} else {
				ai.setPaths(at.value());
			}

			if (!Strings.isBlank(at.key()))
				ai.setPathKey(at.key());
			if (at.top())
				ai.setPathTop(true);
		}
	}

	@SuppressWarnings("unchecked")
	private static void evalPathMap(ActionInfo ai, PathMap pathMap) {
		if (pathMap != null) {
			ai.setPathMap(Json.fromJson(Map.class, pathMap.value()));
		}
	}

	public static void evalFail(ActionInfo ai, Fail fail) {
		if (null != fail) {
			ai.setFailView(fail.value());
		}
	}

	public static void evalOk(ActionInfo ai, Ok ok) {
		if (null != ok) {
			ai.setOkView(ok.value());
		}
	}

	public static void evalModule(ActionInfo ai, Class<?> type) {
		ai.setModuleType(type);
		String beanName = null;
		// 按照5.10.3章节的说明，优先使用IocBean.name的注解声明bean的名字 Modify By QinerG@gmai.com
		InjectName innm = Mirror.getAnnotationDeep(type, InjectName.class);
		IocBean iocBean = Mirror.getAnnotationDeep(type, IocBean.class);
		if (innm == null && iocBean == null) // TODO 再考虑考虑
			return;
		if (iocBean != null) {
			beanName = iocBean.name();
		}
		if (Strings.isBlank(beanName)) {
			if (innm != null && !Strings.isBlank(innm.value())) {
				beanName = innm.value();
			} else {
				beanName = Strings.lowerFirst(type.getSimpleName());
			}
		}
		ai.setInjectName(beanName);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void evalActionFilters(ActionInfo ai, Filters classFilters, Filters filters) {
		List<ObjectInfo<? extends ActionFilter>> list = new ArrayList<ObjectInfo<? extends ActionFilter>>();
		if (null != classFilters) {
			for (By by : classFilters.value()) {
				list.add(new ObjectInfo(by.type(), by.args()));
			}
		}
		if (null != filters) {
			for (By by : filters.value()) {
				list.add(new ObjectInfo(by.type(), by.args()));
			}
		}
		ai.setFilterInfos(list.toArray(new ObjectInfo[list.size()]));

	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void evalHttpAdaptor(ActionInfo ai, AdaptBy ab) {
		if (null != ab) {
			ai.setAdaptorInfo((ObjectInfo<? extends HttpAdaptor>) new ObjectInfo(ab.type(), ab.args()));
		}
	}

	public static void evalEncoding(ActionInfo ai, Encoding encoding) {
		if (null == encoding) {
			ai.setInputEncoding(org.nutz.lang.Encoding.UTF8);
			ai.setOutputEncoding(org.nutz.lang.Encoding.UTF8);
		} else {
			ai.setInputEncoding(Strings.sNull(encoding.input(), org.nutz.lang.Encoding.UTF8));
			ai.setOutputEncoding(Strings.sNull(encoding.output(), org.nutz.lang.Encoding.UTF8));
		}
	}

	public static <T> T evalObj(NutConfig config, Class<T> type, String[] args) {
		// 用上下文替换参数
		Context context = config.getLoadingContext();
		for (int i = 0; i < args.length; i++) {
			args[i] = Segments.replace(args[i], context);
		}
		// 判断是否是 Ioc 注入

		if (args.length == 1 && args[0].startsWith("ioc:")) {
			String name = Strings.trim(args[0].substring(4));
			return config.getIoc().get(type, name);
		}
		return Mirror.me(type).born((Object[]) args);
	}

	public static boolean isModule(Class<?> classZ) {
		int classModify = classZ.getModifiers();
		if (!Modifier.isPublic(classModify) || Modifier.isAbstract(classModify) || Modifier.isInterface(classModify))
			return false;
		for (Method method : classZ.getMethods())
			if (Mirror.getAnnotationDeep(method, At.class) != null)
				return true;
		return false;
	}
}
