package org.nlpcn.jcoder.servlet;

import org.eclipse.jetty.proxy.AsyncProxyServlet;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

public class JcoderProxyServlet extends AsyncProxyServlet {
	@Override
	protected URI rewriteURI(HttpServletRequest request) {

		return URI.create("http://www.sina.com");
	}
}
