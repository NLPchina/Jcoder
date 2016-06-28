package org.nlpcn.jcoder.run.mvc;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.config.FilterNutConfig;
import org.nutz.mvc.impl.ActionInvoker;

public class ApiActionHandler {

	private ApiUrlMappingImpl mapping;

	private NutConfig config;

	public ApiActionHandler(FilterConfig conf) {
		this.config = new FilterNutConfig(conf);
		this.mapping = StaticValue.MAPPING;
	}

	public boolean handle(HttpServletRequest req, HttpServletResponse resp) {

		ActionContext ac = new ActionContext();

		ac.setRequest(req).setResponse(resp).setServletContext(config.getServletContext());

		Mvcs.setActionContext(ac);

		ActionInvoker invoker = mapping.getOrCreate(config, ac);

		if (null == invoker) {
			return false;
		}
		return invoker.invoke(ac);

	}

}
