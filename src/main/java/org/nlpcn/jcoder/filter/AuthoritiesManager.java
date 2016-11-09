package org.nlpcn.jcoder.filter;

import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;

import javax.servlet.http.HttpSession;

public class AuthoritiesManager implements ActionFilter {

	private String name;
	private String value;
	private String path;

	public AuthoritiesManager(String name, String value, String path) {
		this.name = name;
		this.path = path;
		this.value = value;
	}

	@Override
	public View match(ActionContext actionContext) {
		HttpSession session = Mvcs.getHttpSession(false);

		if (session == null) {
			return new ServerRedirectView(path);
		}

		@SuppressWarnings("all")
		Object obj = session.getAttribute(name);
		if (value != null) {
			if (!value.equals(String.valueOf(obj))) {
				return new ServerRedirectView(path);
			}
		} else {
			if (obj != null) {
				return new ServerRedirectView(path);
			}
		}

		return null;
	}

}
