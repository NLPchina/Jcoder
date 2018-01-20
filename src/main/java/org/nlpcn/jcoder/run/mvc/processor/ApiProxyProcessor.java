package org.nlpcn.jcoder.run.mvc.processor;

import org.nlpcn.jcoder.constant.Constants;
import org.nlpcn.jcoder.server.rpc.Rpcs;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.impl.processor.ViewProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 代理类
 * Created by Ansj on 14/12/2017.
 */
public class ApiProxyProcessor extends ViewProcessor {

	private static final Log log = Logs.get();

	private ProxyService proxyService;


	public void init(NutConfig config, ActionInfo ai) throws Throwable {
		proxyService = StaticValue.getSystemIoc().get(ProxyService.class, "proxyService");
	}

	public void process(ActionContext ac) throws Throwable {
		Rpcs.getContext().setTook(System.currentTimeMillis()); //设置请求时间

		HttpServletRequest request = ac.getRequest();
		HttpServletResponse response = ac.getResponse();
		if (StaticValue.IS_LOCAL || StringUtil.isNotBlank(request.getParameter(Constants.PROXY_HEADER)) || request.getHeader(Constants.PROXY_HEADER) != null) { //head中包含则条过
			doNext(ac);
			return;
		}
		String hostPort = proxyService.host(ac.getPath());

		if (StringUtil.isNotBlank(hostPort)) {
			if (StaticValue.getHostPort().equals(hostPort)) {
				doNext(ac);
			} else {
				proxyService.service(request, response, hostPort);
				response.getOutputStream().flush();
				response.getOutputStream().close();
			}
		} else {
			log.debug("not found any host in proxy so do next by self");
			doNext(ac);
		}

	}
}
