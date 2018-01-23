package org.nlpcn.jcoder.filter;

import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.run.rpc.RpcFilter;
import org.nlpcn.jcoder.run.rpc.Rpcs;
import org.nlpcn.jcoder.run.rpc.domain.RpcRequest;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class HostFilter implements ActionFilter, RpcFilter {

	private static final Logger LOG = LoggerFactory.getLogger(IpErrorCountFilter.class);

	private static final String host = StaticValue.getConfigHost();

	@Override
	public View match(ActionContext actionContext) {

		if ("*".equals(host)) {
			return null;
		}

		HttpServletRequest request = actionContext.getRequest();

		if (!host.equals(request.getServerName())) {
			LOG.info(request.getServletPath() + " only vist by host! the server name is : " + request.getServerName());
			return new JsonView(Restful.instance(false, request.getServletPath() + " only vist by host! ", null, ApiException.Unauthorized));
		}

		return null;
	}

	@Override
	public Restful match(RpcRequest req) {

		if (StringUtil.isBlank(host) || "*".equals(host)) {
			return null;
		}

		if (!host.equals(Rpcs.ctx().localAddress())) {
			LOG.info(Rpcs.ctx().remoteAddress() + " only vist by host! the server name is : " + Rpcs.ctx().localAddress());
			return Restful.instance(false, req.getClassName() + "/" + req.getMethodName() + " only vist by host! ", null, ApiException.Unauthorized);
		}

		return null;
	}

}
