package org.nlpcn.jcoder.filter;

import javax.servlet.http.HttpServletRequest;

import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.run.mvc.view.JsonView;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionFilter;
import org.nutz.mvc.View;

public class HostFilter implements ActionFilter {
	
	private  static final String host = StaticValue.HOST ;

	@Override
	public View match(ActionContext actionContext) {
		
		if(StringUtil.isBlank(host) || "*".equals(host)){
			return null ;
		}
		
		HttpServletRequest request = actionContext.getRequest() ;
		
		if(!host.equals(request.getServerName())){
			return new JsonView(ApiException.Unauthorized, request.getServletPath()+" only vist by host! ");
		}
		
		return null;
	}

}
