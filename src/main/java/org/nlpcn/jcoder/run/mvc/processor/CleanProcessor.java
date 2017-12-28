package org.nlpcn.jcoder.run.mvc.processor;

import org.nlpcn.jcoder.constant.UserConstants;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.run.mvc.view.JsonpView;
import org.nlpcn.jcoder.run.mvc.view.TextView;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.View;
import org.nutz.mvc.impl.processor.ViewProcessor;

public class CleanProcessor extends ViewProcessor {



	@Override
	public void init(NutConfig config, ActionInfo ai) throws Throwable {
	}

	public void process(ActionContext ac) throws Throwable {
		String header = ac.getRequest().getHeader(UserConstants.CLUSTER_TOKEN_HEAD);
		if(StringUtil.isNotBlank(header)){
			//如果存在。说明是集群调用需要清除session
			ac.getRequest().getSession().invalidate();
		}
		doNext(ac);
	}

}
