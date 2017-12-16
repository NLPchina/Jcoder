package org.nlpcn.jcoder.service;


import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.http.*;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Streams;
import org.nutz.mvc.Mvcs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

@IocBean
public class ProxyService {

	private static final Logger LOG = LoggerFactory.getLogger(ProxyService.class);

	public static final String PROXY_HEADER = "PROXY_HEADER";

	protected static final Set<String> HOP_HEADERS = Sets.newHashSet("Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
			"TE", "Trailers", "Transfer-Encoding", "Upgrade", "Content-Encoding");


	/**
	 * 合并所有的返回信息
	 */
	public static Function<Map<String, Response>, String> MERGE_MESSAGE_CALLBACK = (Map<String, Response> result) -> {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Response> entry : result.entrySet()) {
			sb.append(entry.getKey() + ": " + JSONObject.parseObject(entry.getValue().getContent()).getString("message") + " , ");
		}
		return sb.toString();
	};

	/**
	 * 合并所有的okfalse的返回信息
	 */
	public static Function<Map<String, Response>, String> MERGE_FALSE_MESSAGE_CALLBACK = (Map<String, Response> result) -> {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Response> entry : result.entrySet()) {
			boolean flag = JSONObject.parseObject(entry.getValue().getContent()).getBoolean("ok");
			if (!flag) {
				sb.append(entry.getKey() + ": " + JSONObject.parseObject(entry.getValue().getContent()).getString("message") + ", ");
			}
		}
		return sb.toString();
	} ;

	/**
	 * 执行请求
	 *
	 * @param req
	 * @param req
	 * @return true 代表经过代理，false代表不需要代理
	 * @throws IOException
	 */
	public boolean service(HttpServletRequest req, HttpServletResponse rep, String targetUrl)
			throws ServletException, IOException {

		if (req.getHeader(PROXY_HEADER) != null) {
			LOG.warn("SKIP " + targetUrl + " because it header has " + PROXY_HEADER);//这个错误不会发生
			return false;
		}

		StringBuilder uri = new StringBuilder();

		uri.append(targetUrl);

		String pathInfo = req.getServletPath();
		if (pathInfo != null) {//ex: /my/path.html
			uri.append(encodeUriQuery(pathInfo, true));
		}

		String queryString = req.getQueryString();//ex:(following '?'): name=value&foo=bar#fragment

		if (queryString != null && queryString.length() > 0) {
			uri.append('?');
			uri.append(encodeUriQuery(queryString, false));
		}

		Request request = Request.create(uri.toString(), Request.METHOD.valueOf(req.getMethod()), new HashMap<>(req.getParameterMap()), makeHeader(req));

		if (req.getInputStream() != null) {
			request.setInputStream(req.getInputStream());
		}

		Response response = Sender.create(request, -1).send();


		Header header = response.getHeader();
		rep.setStatus(response.getStatus());
		Set<Map.Entry<String, String>> all = header.getAll();

		for (Map.Entry<String, String> e : all) {
			if (e.getKey() != null && !HOP_HEADERS.contains(e.getKey())) {
				rep.setHeader(e.getKey(), e.getValue());
			}
		}
		Streams.write(rep.getOutputStream(), response.getStream());
		return true;
	}

	/**
	 * 构建请求头
	 *
	 * @param req
	 * @return
	 */
	private Header makeHeader(HttpServletRequest req) {
		Enumeration<String> headerNames = req.getHeaderNames();
		Header header = Header.create();
		while (headerNames.hasMoreElements()) {
			String key = headerNames.nextElement();
			header.set(key, req.getHeader(key));
		}
		header.set(PROXY_HEADER, "true");
		return header;
	}

	/**
	 * Encodes characters in the query or fragment part of the URI.
	 * <p>
	 * <p>Unfortunately, an incoming URI sometimes has characters disallowed by the spec.  HttpClient
	 * insists that the outgoing proxied request has a valid URI because it uses Java's {@link URI}.
	 * To be more forgiving, we must escape the problematic characters.  See the URI class for the
	 * spec.
	 *
	 * @param in            example: name=value&amp;foo=bar#fragment
	 * @param encodePercent determine whether percent characters need to be encoded
	 */
	protected static CharSequence encodeUriQuery(CharSequence in, boolean encodePercent) {
		//Note that I can't simply use URI.java to encode because it will escape pre-existing escaped things.
		StringBuilder outBuf = null;
		Formatter formatter = null;
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			boolean escape = true;
			if (c < 128) {
				if (asciiQueryChars.get((int) c) && !(encodePercent && c == '%')) {
					escape = false;
				}
			} else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) {//not-ascii
				escape = false;
			}
			if (!escape) {
				if (outBuf != null)
					outBuf.append(c);
			} else {
				//escape
				if (outBuf == null) {
					outBuf = new StringBuilder(in.length() + 5 * 3);
					outBuf.append(in, 0, i);
					formatter = new Formatter(outBuf);
				}
				//leading %, 0 padded, width 2, capital hex
				formatter.format("%%%02X", (int) c);//TODO
			}
		}
		return outBuf != null ? outBuf : in;
	}

	protected static final BitSet asciiQueryChars;

	static {
		char[] c_unreserved = "_-!.~'()*".toCharArray();//plus alphanum
		char[] c_punct = ",;:$&+=".toCharArray();
		char[] c_reserved = "?/[]@".toCharArray();//plus punct

		asciiQueryChars = new BitSet(128);
		for (char c = 'a'; c <= 'z'; c++) asciiQueryChars.set((int) c);
		for (char c = 'A'; c <= 'Z'; c++) asciiQueryChars.set((int) c);
		for (char c = '0'; c <= '9'; c++) asciiQueryChars.set((int) c);
		for (char c : c_unreserved) asciiQueryChars.set((int) c);
		for (char c : c_punct) asciiQueryChars.set((int) c);
		for (char c : c_reserved) asciiQueryChars.set((int) c);

		asciiQueryChars.set((int) '%');//leave existing percent escapes in place
	}


	/**
	 * 同时提交到多个主机上
	 *
	 * @param ipPorts
	 * @param params
	 * @param timeout
	 * @param fun
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	public <T> T post(Set<String> ipPorts, String path, Map<String, Object> params, int timeout, Function<Map<String, Response>, T> fun) throws Exception {
		Map<String, Response> result = post(ipPorts, path, params, timeout);
		return fun.apply(result);
	}

	/**
	 * 同时向多个主机提交
	 *
	 * @param ipPorts
	 * @param params
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	public Map<String, Response> post(Set<String> ipPorts, String path, Map<String, Object> params, int timeout) throws Exception {

		HttpSession session = Mvcs.getReq().getSession();
		String token = (String) session.getAttribute("userToken");

		if (token == null || StaticValue.space().getToken(token) == null) {
			LOG.info("token timeout so create it ");
			User user = (User) session.getAttribute("user");
			token = TokenService.regToken(user);
			session.setAttribute("userToken", token);

		}
		final String fToken = token;

		List<String> urlList = new ArrayList<>(ipPorts);

		Map<String, Response> result = new LinkedHashMap<>();

		ExecutorService threadPool = null;
		try {
			threadPool = Executors.newFixedThreadPool(urlList.size());

			BlockingQueue<Future<Response>> queue = new LinkedBlockingQueue<Future<Response>>(urlList.size());

			for (String ipPort : urlList) {
				Future<Response> future = threadPool.submit(() -> {
					LOG.info("post url : http://" + ipPort + path);
					return Sender.create(Request.create("http://" + ipPort + path, Request.METHOD.POST, params, Header.create(ImmutableMap.of("authorization", fToken)))).setTimeout(timeout).setConnTimeout(timeout).send();
				});
				queue.add(future);
			}


			for (int i = 0; i < urlList.size(); i++) {
				result.put(urlList.get(i), queue.take().get());
			}
		} finally {
			if (threadPool != null) {
				threadPool.shutdown();
			}
		}

		return result;

	}


}
