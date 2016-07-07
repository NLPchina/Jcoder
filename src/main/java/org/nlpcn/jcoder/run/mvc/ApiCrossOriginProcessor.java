package org.nlpcn.jcoder.run.mvc;

import javax.servlet.http.HttpServletResponse;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.impl.processor.AbstractProcessor;

/**
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 * @author Ansj
 * 
 */
public class ApiCrossOriginProcessor extends AbstractProcessor {

	private static final Log log = Logs.get();

	@Override
	public void init(NutConfig config, ActionInfo ai) throws Throwable {
	}

	private static final String ORIGIN = "*";
	private static String METHODS = "get, post, put, delete, options";
	private static final String HEADERS = "origin, content-type, accept";
	private static final String CREDENTIALS = "true";

	public void process(ActionContext ac) throws Throwable {
		HttpServletResponse resp = ac.getResponse();
		resp.addHeader("Access-Control-Allow-Origin", ORIGIN);
		resp.addHeader("Access-Control-Allow-Methods", METHODS);
		resp.addHeader("Access-Control-Allow-Headers", HEADERS);
		resp.addHeader("Access-Control-Allow-Credentials", CREDENTIALS);

		if ("OPTIONS".equals(ac.getRequest().getMethod())) {
			if (log.isDebugEnabled())
				log.debugf("Feedback -- [%s] [%s] [%s] [%s]", ORIGIN, METHODS, HEADERS, CREDENTIALS);
		}
		doNext(ac);
	}

}
