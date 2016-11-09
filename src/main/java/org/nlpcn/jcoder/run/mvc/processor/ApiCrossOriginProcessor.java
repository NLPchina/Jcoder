package org.nlpcn.jcoder.run.mvc.processor;

import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.impl.processor.AbstractProcessor;

public class ApiCrossOriginProcessor extends AbstractProcessor {
	private static final Log log = Logs.get();

	protected String origin = "*";
	protected String methods = "get, post, put, delete, options";
	protected String headers = "origin, content-type, accept";
	protected String credentials = "true";

	@Override
	public void init(NutConfig config, ActionInfo ai) throws Throwable {
	}

	public void process(ActionContext ac) throws Throwable {
		HttpServletResponse resp = ac.getResponse();
		if (!Strings.isBlank(origin))
			resp.addHeader("Access-Control-Allow-Origin", origin);
		if (!Strings.isBlank(methods))
			resp.addHeader("Access-Control-Allow-Methods", methods);
		if (!Strings.isBlank(headers))
			resp.addHeader("Access-Control-Allow-Headers", headers);
		if (!Strings.isBlank(credentials))
			resp.addHeader("Access-Control-Allow-Credentials", credentials);

		if ("OPTIONS".equals(ac.getRequest().getMethod())) {
			if (log.isDebugEnabled())
				log.debugf("Feedback -- [%s] [%s] [%s] [%s]", origin, methods, headers, credentials);
		} else {
			doNext(ac);
		}
	}

}
