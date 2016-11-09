package org.nlpcn.jcoder.filter;

import java.util.concurrent.ExecutionException;

import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.service.TokenService;
import org.nlpcn.jcoder.util.ApiException;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;

public class TokenFilter implements ActionFilter {

	@Override
	public View match(ActionContext actionContext) {
		String token = actionContext.getRequest().getHeader("authorization");

		if (StringUtil.isBlank(token)) {
			return new JsonView(ApiException.Unauthorized, "token 'authorization' not in header ");
		}

		try {
			if (TokenService.getToken(token) == Token.NULL) {
				return new JsonView(ApiException.Unauthorized, "token not access");
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
			return new JsonView(ApiException.ServerException, e);
		}

		return null;
	}

}
