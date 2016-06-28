package org.nlpcn.jcoder.util;

import org.nutz.ioc.Ioc;
import org.nutz.lang.Mirror;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.IocBy;

public class NutzUtil {
	public static void init(Class<?> c) {
		IocBy ib = c.getAnnotation(IocBy.class);
		Ioc ioc = Mirror.me(ib.type()).born().create(null, ib.args());
		Mvcs.setIoc(ioc);
	}

	public static void close() {
		Mvcs.close();
	}

}
