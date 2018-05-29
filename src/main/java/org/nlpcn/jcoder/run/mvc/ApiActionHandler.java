package org.nlpcn.jcoder.run.mvc;

import org.nlpcn.jcoder.run.mvc.processor.ApiActionInvoker;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.NutConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApiActionHandler {

	private NutConfig config;


	public ApiActionHandler(NutConfig conf) {
		this.config = conf;
		config.setUrlMapping(null);
	}

	public boolean handle(HttpServletRequest req, HttpServletResponse resp) {

		ActionContext ac = new ActionContext();

		ac.setRequest(req).setResponse(resp).setServletContext(config.getServletContext());

		Mvcs.setActionContext(ac);

		ApiActionInvoker invoker = StaticValue.MAPPING.getOrCreate(config, ac);

		if (null == invoker) {
			return false;
		}
		return invoker.invoke(ac);

	}

}
