package org.nlpcn.jcoder.run.mvc.processor;

import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.util.Restful;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.impl.processor.ViewProcessor;

public class ApiFailProcessor extends ViewProcessor {

	private static final Log log = Logs.get();

	@Override
	public void init(NutConfig config, ActionInfo ai) throws Throwable {
		view = new JsonView(500, null);
	}

	public void process(ActionContext ac) throws Throwable {
		if (log.isWarnEnabled()) {
			String uri = Mvcs.getRequestPath(ac.getRequest());
			log.warn(String.format("Error@%s :", uri), ac.getError());
		}
		if (ac.getRequest().getParameter("_debug") != null) {
			view.render(ac.getRequest(), ac.getResponse(), ac.getError());
		} else {

			Throwable cause = ac.getError();
			Throwable temp = cause;

			while ((temp = temp.getCause()) != null) {
				cause = temp;
			}

			cause.printStackTrace();

			String message = cause.getMessage();

			if (message == null) {
				message = "null is sex , sex is null";
			}

			view.render(ac.getRequest(), ac.getResponse(), Restful.instance(false, message).code(500));
		}

		doNext(ac);
	}
}