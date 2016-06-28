package org.nlpcn.jcoder.run.mvc;

import org.nlpcn.jcoder.util.JsonView;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.impl.processor.AbstractProcessor;

public class ApiViewProcessor extends AbstractProcessor {

	@Override
	public void init(NutConfig config, ActionInfo ai) throws Throwable {

	}

	public void process(ActionContext ac) throws Throwable {
		Object re = ac.getMethodReturn();
		Object err = ac.getError();

		new JsonView(null).render(ac.getRequest(), ac.getResponse(), null == re ? err : re);

		doNext(ac);
	}

}
