package org.nlpcn.jcoder.run.mvc;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.nutz.filepool.FilePool;
import org.nutz.filepool.UU32FilePool;
import org.nutz.ioc.Ioc;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
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
import org.nutz.mvc.upload.FastUploading;
import org.nutz.mvc.upload.FieldMeta;
import org.nutz.mvc.upload.TempFile;
import org.nutz.mvc.upload.UploadException;
import org.nutz.mvc.upload.UploadingContext;
import org.nutz.mvc.upload.injector.FileInjector;
import org.nutz.mvc.upload.injector.FileMetaInjector;
import org.nutz.mvc.upload.injector.InputStreamInjector;
import org.nutz.mvc.upload.injector.MapSelfInjector;
import org.nutz.mvc.upload.injector.ReaderInjector;
import org.nutz.mvc.upload.injector.TempFileArrayInjector;
import org.nutz.mvc.upload.injector.TempFileInjector;

public class ApiPairAdaptor extends PairAdaptor {
	
	private static final Log log = Logs.get();
	
	protected UploadingContext uploadCtx;
	
	public ApiPairAdaptor() {
		this("");
	}

	public ApiPairAdaptor(String path) {
        String appRoot = Mvcs.getServletContext().getRealPath("/");
        if(appRoot==null){
        	appRoot = "" ;
        }
        if (path.isEmpty()) {
            path = "${app.root}/WEB-INF/tmp/nutzupload2";
        }
        if (path.contains("${app.root}")){
            path = path.replace("${app.root}", appRoot);
        }
        try{
        	uploadCtx = new UploadingContext(new UU32FilePool(path));
        }catch (Exception e) {
        	log.error(e.getMessage());
		}
    }

    public ApiPairAdaptor(FilePool pool) {
        this(new UploadingContext(pool));
    }

    public ApiPairAdaptor(UploadingContext up) {
        uploadCtx = up;
    }

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
	
	@SuppressWarnings("deprecation")
	protected ParamInjector evalInjectorBy(Type type, Param param) {
        // TODO 这里的实现感觉很丑, 感觉可以直接用type进行验证与传递
        // TODO 这里将Type的影响局限在了 github issue #30 中提到的局部范围
        Class<?> clazz = Lang.getTypeClass(type);
        if (clazz == null) {
            if (log.isWarnEnabled())
                log.warnf("!!Fail to get Type Class : type=%s , param=%s", type, param);
            return null;
        }

        // Map
        if (Map.class.isAssignableFrom(clazz))
            return new MapSelfInjector();

        if (null == param)
            return super.evalInjectorBy(type, null);

        String paramName = param.value();

        // File
        if (File.class.isAssignableFrom(clazz))
            return new FileInjector(paramName);
        // FileMeta
        if (FieldMeta.class.isAssignableFrom(clazz))
            return new FileMetaInjector(paramName);
        // TempFile
        if (TempFile.class.isAssignableFrom(clazz))
            return new TempFileInjector(paramName);
        // InputStream
        if (InputStream.class.isAssignableFrom(clazz))
            return new InputStreamInjector(paramName);
        // Reader
        if (Reader.class.isAssignableFrom(clazz))
            return new ReaderInjector(paramName);
        // List
        //if (List.class.isAssignableFrom(clazz)) {
        //    return new MapListInjector(paramName);
        //}
        if (TempFile[].class.isAssignableFrom(clazz)) {
            return new TempFileArrayInjector(paramName);
        }
        // Other
        return super.evalInjectorBy(type, param);
    }

	protected Object getReferObject(ServletContext sc, HttpServletRequest req, HttpServletResponse resp, String[] pathArgs) {
		String type = req.getHeader("Content-Type");
		if (!Strings.isBlank(type)) {
			if (type.contains("json")) { // JSON适配器
				try {
					return Json.fromJson(req.getReader());
				} catch (Exception e) {
					throw Lang.wrapThrow(e);
				}
			}
			if (type.contains("multipart/form-data")) { // 上传适配器
				FastUploading uploading = new FastUploading();
				try {
					return uploading.parse(req, uploadCtx);
				} catch (UploadException e) {
					throw Lang.wrapThrow(e);
				}
			}
		}
		return super.getReferObject(sc, req, resp, pathArgs);
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
