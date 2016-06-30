package org.nlpcn.jcoder.run.mvc;

import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.run.mvc.view.JsonpView;
import org.nlpcn.jcoder.run.mvc.view.TextView;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.View;
import org.nutz.mvc.impl.processor.AbstractProcessor;

public class ApiViewProcessor extends AbstractProcessor {

	private View jsonView;
	private View textView;

	@Override
	public void init(NutConfig config, ActionInfo ai) throws Throwable {
		jsonView = new JsonView(null);
		textView = new TextView(null);

	}

	public void process(ActionContext ac) throws Throwable {
		Object re = ac.getMethodReturn();
		Throwable err = ac.getError();

		View view = jsonView;

		String temp = null;
		if ((temp = ac.getRequest().getParameter("_callback")) != null) {
			view = new JsonpView(temp);
		} else if (ac.getRequest().getParameter("_text") != null) {
			view = textView;
		}

		if (re != null) {
			view.render(ac.getRequest(), ac.getResponse(), re);
		} else if (err == null) {
			view.render(ac.getRequest(), ac.getResponse(), StaticValue.OK);
		}

		doNext(ac);
	}

}
