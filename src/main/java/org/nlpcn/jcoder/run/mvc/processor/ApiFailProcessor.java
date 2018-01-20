package org.nlpcn.jcoder.run.mvc.processor;

import org.nlpcn.jcoder.constant.Constants;
import org.nlpcn.jcoder.constant.UserConstants;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.ExceptionUtil;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StringUtil;
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
		view = new JsonView(ApiException.ServerException, null);
	}

	public void process(ActionContext ac) throws Throwable {
		if (log.isWarnEnabled()) {
			String uri = Mvcs.getRequestPath(ac.getRequest());
			log.warn(String.format("Error@%s :", uri), ac.getError());
		}

		int status = ApiException.ServerException ;

		Throwable error = ac.getError();

		if(error instanceof ApiException){
			status = ((ApiException) error).getStatus() ;
		}

		if (ac.getRequest().getHeader(Constants.DEBUG) != null) {
			view.render(ac.getRequest(), ac.getResponse(), Restful.fail().obj(ac.getError()).code(status));
		} else {

			Throwable cause = ExceptionUtil.realException(ac.getError());

			cause.printStackTrace();

			String message = cause.getMessage();
			if (message == null) {
				message = "null is sex , sex is null";
			}

			view.render(ac.getRequest(), ac.getResponse(), Restful.instance(false, message).code(status));
		}

		if (StringUtil.isNotBlank(ac.getRequest().getHeader(UserConstants.CLUSTER_TOKEN_HEAD))) {
			//如果存在。说明是集群调用需要清除session
			ac.getRequest().getSession().invalidate();
		}

		doNext(ac);
	}
}