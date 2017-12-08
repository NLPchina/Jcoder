package org.nlpcn.jcoder.filter;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.server.rpc.RpcFilter;
import org.nlpcn.jcoder.server.rpc.Rpcs;
import org.nlpcn.jcoder.server.rpc.domain.RpcRequest;
import org.nlpcn.jcoder.service.TokenService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenFilter implements ActionFilter, RpcFilter {

	private static final Logger LOG = LoggerFactory.getLogger(TokenFilter.class);

	private boolean def = true;

	public TokenFilter(boolean def) {
		this.def = def;
	}

	public TokenFilter() {
	}

	@Override
	public View match(ActionContext actionContext) {

		String token = actionContext.getRequest().getHeader("authorization");

		if (token == null) { //尝试从参数中获取
			token = actionContext.getRequest().getParameter("_authorization");
		}

		if (StringUtil.isBlank(token)) {
			LOG.info(StaticValue.getRemoteHost(actionContext.getRequest()) + " token 'authorization' not in header and '_authorization' not in parameters");
			return new JsonView(Restful.instance(false, " token 'authorization' not in header and '_authorization' not in parameters", null, ApiException.TokenAuthorNotFound));
		}

		if (def && StaticValue.TOKEN != null && token.equals(StaticValue.TOKEN)) {
			return null;
		}

		try {
			Token token2 = TokenService.getToken(token);
			if (token2 == null || token2 == Token.NULL) {
				String message = "token not access";
				if (token.equals(StaticValue.TOKEN)) {
					message += " this api can not visti by default token";
				}

				LOG.info(StaticValue.getRemoteHost(actionContext.getRequest()) + " token not access visit " + actionContext.getPath());
				return new JsonView(Restful.instance(false, message, null, ApiException.TokenAuthorNotFound));
			}

			String path = actionContext.getPath();

			String[] split = path.replace("/api/", "").split("/");
			
			
			if(split.length<2){
				split = new String[]{split[0],null} ;
			}

			if (!token2.authorize(split[0], split[1])) {
				return new JsonView(Restful.instance(false, "token not access visit for " + path, null, ApiException.TokenNoPermissions));
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
			return new JsonView(Restful.instance(false, e.getMessage(), e, ApiException.ServerException));
		}

		return null;
	}

	@Override
	public Restful match(RpcRequest req) {
		String token = req.getTokenStr();

		if (token == null) { //尝试从参数中获取
			token = (String) Rpcs.getContext().get("_authorization");
		}

		if (StringUtil.isBlank(token)) {
			LOG.info(req.getMessageId() + " " + Rpcs.getContext().remoteAddress() + " token 'authorization' not in header and '_authorization' not in parameters");
			return Restful.instance(false, req.getMessageId() + " token 'authorization' not in header and '_authorization' not in parameters", null,
					ApiException.TokenAuthorNotFound);
		}

		if (def && StaticValue.TOKEN != null && token.equals(StaticValue.TOKEN)) {
			return null;
		}

		try {
			Token token2 = TokenService.getToken(token);
			if (token2 == null || token2 == Token.NULL) {
				String message = "token not access";
				if (token.equals(StaticValue.TOKEN)) {
					message += " this api can not visti by default token";
				}

				LOG.info(req.getMessageId() + " " + Rpcs.getContext().remoteAddress() + " token not access visit " + req.getClassName() + "/" + req.getMethodName());
				return Restful.instance(false, message, null, ApiException.TokenAuthorNotFound);
			}

			if (!token2.authorize(req.getClassName(), req.getMethodName())) {
				return Restful.instance(false, req.getMessageId() + " token not access visit for " + req.getClassName() + "/" + req.getMethodName(), null,
						ApiException.TokenNoPermissions);
			}

		} catch (ExecutionException e) {
			e.printStackTrace();
			LOG.error(e.getMessage(), e);
			return Restful.instance(false, e.getMessage(), e, ApiException.ServerException);
		}

		return null;
	}

}
