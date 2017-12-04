package org.nlpcn.jcoder.service;


import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.nutz.http.Http;
import org.nutz.http.ProxySwitcher;
import org.nutz.http.Request;
import org.nutz.http.Sender;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Enumeration;

public class ProxyService {

	/**
	 * 执行请求
	 *
	 * @param servletRequest
	 * @param req
	 * @throws rep
	 * @throws IOException
	 */
	public void service(HttpServletRequest req, HttpServletResponse rep, String targetUrl)
			throws ServletException, IOException {


		Sender.create(req) ;

	}

}
