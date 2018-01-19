package org.nlpcn.jcoder.run.mvc;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nlpcn.jcoder.run.mvc.processor.ApiActionInvoker;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.Loading;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.UrlMapping;
import org.nutz.mvc.config.FilterNutConfig;

public class ApiActionHandler {

	private ApiUrlMappingImpl mapping;

	private NutConfig config;


	public ApiActionHandler(NutConfig conf) {
		this.config = conf;
		this.mapping = StaticValue.MAPPING;
		config.setUrlMapping(mapping);
	}

	public boolean handle(HttpServletRequest req, HttpServletResponse resp) {

		ActionContext ac = new ActionContext();

		ac.setRequest(req).setResponse(resp).setServletContext(config.getServletContext());

		Mvcs.setActionContext(ac);

		ApiActionInvoker invoker = mapping.getOrCreate(config, ac);

		if (null == invoker) {
			return false;
		}
		return invoker.invoke(ac);

	}

}
