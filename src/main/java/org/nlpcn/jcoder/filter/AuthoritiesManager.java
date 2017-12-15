package org.nlpcn.jcoder.filter;

import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;

import javax.servlet.http.HttpSession;

public class AuthoritiesManager implements ActionFilter {

	private String name;
	private String path;

	public AuthoritiesManager() {
		this.name = "user";
		this.path = "/login.html" ;
	}

	@Override
	public View match(ActionContext actionContext) {
		HttpSession session = Mvcs.getHttpSession(false);

		if (session == null) {
			return new ServerRedirectView(path);
		}

		@SuppressWarnings("all")
		Object obj = session.getAttribute(name);
		if (obj == null) {
			return new ServerRedirectView(path);
		}

		return null;
	}

}
