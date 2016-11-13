package org.nlpcn.jcoder.filter;

import java.util.concurrent.ExecutionException;

import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.service.TokenService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenFilter implements ActionFilter {
	
	private static final Logger LOG = LoggerFactory.getLogger(TokenFilter.class) ;

	@Override
	public View match(ActionContext actionContext) {
		String token = actionContext.getRequest().getHeader("authorization");

		if (StringUtil.isBlank(token)) {
			LOG.info(StaticValue.getRemoteHost(actionContext.getRequest()) + " token 'authorization' not in header");
			return new JsonView(Restful.instance(false, "token 'authorization' not in header ",null,ApiException.Unauthorized));
		}

		try {
			if (TokenService.getToken(token) == Token.NULL) {
				LOG.info(StaticValue.getRemoteHost(actionContext.getRequest()) + " token not access");
				return new JsonView(Restful.instance(false, "token not access",null,ApiException.Unauthorized));
			}
		} catch (ExecutionException e) {
			e.printStackTrace();
			LOG.error(e.getMessage(),e);
			return new JsonView(Restful.instance(false, e.getMessage(),e,ApiException.ServerException));
		}

		return null;
	}

}
