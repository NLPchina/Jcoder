package org.nlpcn.jcoder.run.mvc;

import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.Scope;
import org.nutz.mvc.ViewModel;
import org.nutz.mvc.adaptor.PairAdaptor;
import org.nutz.mvc.adaptor.ParamInjector;
import org.nutz.mvc.adaptor.injector.AllAttrInjector;
import org.nutz.mvc.adaptor.injector.AppAttrInjector;
import org.nutz.mvc.adaptor.injector.CookieInjector;
import org.nutz.mvc.adaptor.injector.HttpInputStreamInjector;
import org.nutz.mvc.adaptor.injector.HttpReaderInjector;
import org.nutz.mvc.adaptor.injector.IocInjector;
import org.nutz.mvc.adaptor.injector.IocObjInjector;
import org.nutz.mvc.adaptor.injector.ReqHeaderInjector;
import org.nutz.mvc.adaptor.injector.RequestAttrInjector;
import org.nutz.mvc.adaptor.injector.RequestInjector;
import org.nutz.mvc.adaptor.injector.ResponseInjector;
import org.nutz.mvc.adaptor.injector.ServletContextInjector;
import org.nutz.mvc.adaptor.injector.SessionAttrInjector;
import org.nutz.mvc.adaptor.injector.SessionInjector;
import org.nutz.mvc.adaptor.injector.ViewModelInjector;
import org.nutz.mvc.annotation.Attr;
import org.nutz.mvc.annotation.Cookie;
import org.nutz.mvc.annotation.IocObj;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.annotation.ReqHeader;

public class ApiPairAdaptor extends PairAdaptor {

	@Override
	public void init(Method method) {
		this.method = method;
		argTypes = method.getParameterTypes();
		injs = new ParamInjector[argTypes.length];
		defaultValues = new String[argTypes.length];
		Annotation[][] annss = method.getParameterAnnotations();
		Type[] types = method.getGenericParameterTypes();
		Parameter[] parameters = method.getParameters();

		for (int i = 0; i < annss.length; i++) {
			Annotation[] anns = annss[i];
			Param param = null;
			Attr attr = null;
			IocObj iocObj = null;
			ReqHeader reqHeader = null;
			Cookie cookie = null;

			// find @Param & @Attr & @IocObj in current annotations
			for (int x = 0; x < anns.length; x++) {
				if (anns[x] instanceof Param) {
					param = (Param) anns[x];
					break;
				} else if (anns[x] instanceof Attr) {
					attr = (Attr) anns[x];
					break;
				} else if (anns[x] instanceof IocObj) {
					iocObj = (IocObj) anns[x];
					break;
				} else if (anns[x] instanceof ReqHeader) {
					reqHeader = (ReqHeader) anns[x];
					break;
				} else if (anns[x] instanceof Cookie) {
					cookie = (Cookie) anns[x];
				}
			}
			// If has @Attr
			if (null != attr) {
				injs[i] = evalInjectorByAttrScope(attr);
				continue;
			}

			// If has @IocObj
			if (null != iocObj) {
				injs[i] = new IocObjInjector(method.getParameterTypes()[i], iocObj.value());
				continue;
			}

			if (null != reqHeader) {
				injs[i] = new ReqHeaderInjector(reqHeader.value(), argTypes[i]);
				continue;
			}
			if (null != cookie) {
				injs[i] = new CookieInjector(cookie.value(), argTypes[i]);
				continue;
			}

			// And eval as default suport types
			injs[i] = evalInjectorByParamType(argTypes[i]);
			if (null != injs[i]) {
				continue;
			}

			if (param == null) {

				
				final String paramName = parameters[i].getName();

				param = new Param() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return Param.class;
					}

					@Override
					public String value() {
						return paramName;
					}

					@Override
					public String dfmt() {
						return "";
					}

					@Override
					public String df() {
						return "//NOT EXIST IN//";
					}

					@Override
					public boolean array_auto_split() {
						return true;
					}
				};
			}

			// Eval by sub-classes
			injs[i] = evalInjector(types[i], param);
			// 子类也不能确定，如何适配这个参数，那么做一个标记，如果
			// 这个参数被 ParamInjector 适配到，就会抛错。
			// 这个设计是因为了 "路径参数"
			if (null == injs[i]) {
				injs[i] = paramNameInject(method, i);
			}
			if (param != null) {
				String tmp = param.df();
				if (tmp != null && !tmp.equals(ParamDefailtTag))
					defaultValues[i] = tmp;
			}
		}
	}

	private static ParamInjector evalInjectorByAttrScope(Attr attr) {
		if (attr.scope() == Scope.APP)
			return new AppAttrInjector(attr.value());
		if (attr.scope() == Scope.SESSION)
			return new SessionAttrInjector(attr.value());
		if (attr.scope() == Scope.REQUEST)
			return new RequestAttrInjector(attr.value());
		return new AllAttrInjector(attr.value());
	}

	private static ParamInjector evalInjectorByParamType(Class<?> type) {
		// Request
		if (ServletRequest.class.isAssignableFrom(type)) {
			return new RequestInjector();
		}
		// Response
		else if (ServletResponse.class.isAssignableFrom(type)) {
			return new ResponseInjector();
		}
		// Session
		else if (HttpSession.class.isAssignableFrom(type)) {
			return new SessionInjector();
		}
		// ServletContext
		else if (ServletContext.class.isAssignableFrom(type)) {
			return new ServletContextInjector();
		}
		// Ioc
		else if (Ioc.class.isAssignableFrom(type)) {
			return new IocInjector();
		}
		// InputStream
		else if (InputStream.class.isAssignableFrom(type)) {
			return new HttpInputStreamInjector();
		}
		// Reader
		else if (Reader.class.isAssignableFrom(type)) {
			return new HttpReaderInjector();
		}
		// ViewModel
		else if (ViewModel.class.isAssignableFrom(type))
			return new ViewModelInjector();
		return null;
	}
}
