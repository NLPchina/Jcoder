package org.nlpcn.jcoder.run.mvc.processor;

import org.nlpcn.jcoder.run.mvc.ApiPairAdaptor;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.HttpAdaptor;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.impl.processor.AbstractProcessor;

/**
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 * @author Ansj
 */
public class ApiAdaptorProcessor extends AbstractProcessor {

	// api not support path args so it all ways empty
	private static final String[] PATH_ARGS = new String[0];
	private HttpAdaptor adaptor;

	protected static HttpAdaptor evalHttpAdaptor(NutConfig config, ActionInfo ai) {
		HttpAdaptor re = evalObj(config, ai.getAdaptorInfo());
		if (null == re)
			re = new ApiPairAdaptor();
		re.init(ai.getMethod());
		return re;
	}

	@Override
	public void init(NutConfig config, ActionInfo ai) throws Throwable {
		adaptor = evalHttpAdaptor(config, ai);
	}

	public void process(ActionContext ac) throws Throwable {
		Object[] args = adaptor.adapt(ac.getServletContext(), ac.getRequest(), ac.getResponse(), PATH_ARGS);
		ac.setMethodArgs(args);
		doNext(ac);
	}
}
