package org.nlpcn.jcoder.filter;

import org.nlpcn.jcoder.constant.UserConstants;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.service.TokenService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.View;

import javax.servlet.http.HttpSession;

public class AuthoritiesManager implements ActionFilter {

	@Override
	public View match(ActionContext actionContext) {
		HttpSession session = Mvcs.getHttpSession();

		String tokenStr = actionContext.getRequest().getHeader(UserConstants.CLUSTER_TOKEN_HEAD);

		Object obj = session.getAttribute(UserConstants.USER);

		if (obj != null) {
			return null;
		}

		if (StringUtil.isNotBlank(tokenStr)) {
			try {
				Token token = TokenService.getToken(tokenStr);
				if (token != null) {
					actionContext.getRequest().getSession().setAttribute(UserConstants.USER, token.getUser());
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}


		return new JsonView(Restful.instance().code(ApiException.TokenAuthorNotFound).msg("未登录").ok(false));
	}

}
