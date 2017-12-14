package org.nlpcn.jcoder.run.mvc.processor;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.*;
import org.nutz.mvc.impl.processor.ViewProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 代理类
 * Created by Ansj on 14/12/2017.
 */
public class ApiProxyProcessor extends ViewProcessor {

	private static final Log log = Logs.get();

	private ProxyService proxyService ;


	public void init(NutConfig config, ActionInfo ai) throws Throwable {
		proxyService = StaticValue.getSystemIoc().get(ProxyService.class, "proxyService") ;
	}

	public void process(ActionContext ac) throws Throwable {

		HttpServletRequest request = ac.getRequest();
		HttpServletResponse response = ac.getResponse() ;
		String path = request.getPathInfo() ;

		if(StaticValue.IS_LOCAL || request.getHeader(ProxyService.PROXY_HEADER)!=null){ //head中包含则条过
			doNext(ac);
			return ;
		}

		String proxyUrl = StaticValue.space().host(ac.getRequest().getHeader("jcoder_group"), request.getHeader("jcoder_group"));
		proxyService.service(request,response,proxyUrl) ;
		response.getOutputStream().flush();
		response.getOutputStream().close();

	}
}
