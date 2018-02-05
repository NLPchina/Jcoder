package org.nlpcn.jcoder.service;


import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.nlpcn.jcoder.constant.UserConstants;
import org.nlpcn.jcoder.domain.HostGroup;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.http.*;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.nlpcn.jcoder.constant.Constants.PROXY_HEADER;
import static org.nlpcn.jcoder.service.SharedSpaceService.HOST_GROUP_PATH;
import static org.nlpcn.jcoder.service.SharedSpaceService.MAPPING_PATH;

@IocBean
public class ProxyService {

	protected static final Set<String> HOP_HEADERS = Sets.newHashSet("Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
			"TE", "Trailers", "Transfer-Encoding", "Upgrade", "Content-Encoding");
	protected static final BitSet asciiQueryChars;
	private static final Logger LOG = LoggerFactory.getLogger(ProxyService.class);
	/**
	 * 合并所有的返回信息
	 */
	public static Function<Map<String, Restful>, Restful> MERGE_MESSAGE_CALLBACK = (Map<String, Restful> result) -> {
		List<String> messages = new ArrayList<>();

		boolean ok = true;
		int code = 200;
		for (Map.Entry<String, Restful> entry : result.entrySet()) {
			ok &= entry.getValue().isOk();
			if (entry.getValue().code() != 200) {
				code = entry.getValue().code();
			}
			messages.add(entry.getKey() + ":" + entry.getValue().getMessage());
		}
		return Restful.instance().ok(ok).code(code).msg(Joiner.on(" , ").join(messages));
	};

	/**
	 * 合并所有的okfalse的返回信息
	 */
	public static Function<Map<String, Restful>, Restful> MERGE_FALSE_MESSAGE_CALLBACK = (Map<String, Restful> result) -> {
		List<String> messages = new ArrayList<>();
		boolean ok = true;
		int code = 200;
		for (Map.Entry<String, Restful> entry : result.entrySet()) {
			try {
				boolean flag = entry.getValue().isOk();
				if (!flag) {
					ok = false;
					if (entry.getValue().code() != 200) {
						code = entry.getValue().code();
					}
					messages.add(entry.getKey() + ":" + entry.getValue().getMessage());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Restful.instance().ok(ok).code(code).msg(Joiner.on(" , ").join(messages));
	};

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

	private String myToken = null;

	/**
	 * Encodes characters in the query or fragment part of the URI. <p> <p>Unfortunately, an
	 * incoming URI sometimes has characters disallowed by the spec.  HttpClient insists that the
	 * outgoing proxied request has a valid URI because it uses Java's {@link }. To be more
	 * forgiving, we must escape the problematic characters.  See the URI class for the spec.
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

	/**
	 * 执行请求
	 *
	 * @return true 代表经过代理，false代表不需要代理
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

		Request request = Request.create(uri.toString(), Request.METHOD.valueOf(req.getMethod()), new HashMap<>(), makeHeader(req));

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
	 * 同时提交到多个主机上
	 */
	public <T> T post(String[] hostPorts, String path, Map<String, Object> params, int timeout, Function<Map<String, Restful>, T> fun) throws Exception {
		return post(Arrays.stream(hostPorts).collect(Collectors.toSet()), path, params, timeout, fun);
	}


	/**
	 * 同时提交到多个主机上
	 */
	public <T> T post(Set<String> hostPorts, String path, Map<String, Object> params, int timeout, Function<Map<String, Restful>, T> fun) throws Exception {
		Map<String, Restful> result = post(hostPorts, path, params, timeout);
		return fun.apply(result);
	}

	/**
	 * 同时向多个主机提交
	 */
	public Map<String, Restful> post(String[] hostPorts, String path, Map<String, Object> params, int timeout) throws Exception {
		return post(Arrays.stream(hostPorts).collect(Collectors.toSet()), path, params, timeout);
	}

	/**
	 * 同时向多个主机提交
	 */
	public Map<String, Restful> post(Set<String> hostPorts, String path, Map<String, Object> params, int timeout) throws Exception {

		if (hostPorts.size() == 0) {
			return new HashMap<>();
		}

		String token = getOrCreateToken();

		final String fToken = token;

		List<String> urlList = new ArrayList<>(hostPorts);

		Map<String, Restful> result = new LinkedHashMap<>();

		ExecutorService threadPool = null;
		try {
			threadPool = Executors.newFixedThreadPool(urlList.size());

			BlockingQueue<Future<Restful>> queue = new LinkedBlockingQueue<>(urlList.size());

			for (String hostPort : urlList) {
				Future<Restful> future = threadPool.submit(() -> {
					LOG.info("post url : http://" + hostPort + path);
					try {
						Response send = Sender.create(Request.create("http://" + hostPort + path, Request.METHOD.POST, params, Header.create(ImmutableMap.of(UserConstants.CLUSTER_TOKEN_HEAD, fToken)))).setTimeout(timeout).setConnTimeout(timeout).send();
						return Restful.instance(send);
					} catch (Exception e) {
						LOG.error("post to url : http://" + hostPort + path + " error ", e);
						return Restful.instance(false, "请求异常：" + e.getMessage());
					}
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


	/**
	 * 同时向多个主机提交
	 */
	public Map<String, String> upload(Set<String> hostPorts, String path, Map<String, Object> params, int timeout) throws Exception {

		String token = getOrCreateToken();

		final String fToken = token;

		List<String> urlList = new ArrayList<>(hostPorts);

		Map<String, String> result = new LinkedHashMap<>();

		ExecutorService threadPool = null;
		try {
			threadPool = Executors.newFixedThreadPool(urlList.size());

			BlockingQueue<Future<String>> queue = new LinkedBlockingQueue<Future<String>>(urlList.size());

			for (String hostPort : urlList) {
				Future<String> future = threadPool.submit(() -> {
					LOG.info("post url : http://" + hostPort + path);
					String content = null;
					try {
						Response send = Http.upload("http://" + hostPort + path, params, Header.create(ImmutableMap.of(UserConstants.CLUSTER_TOKEN_HEAD, fToken)), timeout);
						content = send.getContent();
					} catch (Exception e) {
						LOG.error("post to url : http://" + hostPort + path + " error ", e);

						content = Restful.instance(false, "请求异常：" + e.getMessage()).toJsonString();
					}
					return content;
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


	/**
	 * 获取一个token
	 */

	private synchronized String getOrCreateToken() throws Exception {
		Token token = StringUtil.isBlank(myToken) ? null : TokenService.getToken(myToken);
		if (token == null) {
			LOG.info("token timeout so create it ");
			myToken = TokenService.regToken(User.CLUSTER_USER);
		}
		return myToken;
	}


	/**
	 * 提交一个请求
	 */
	public Response post(String hostPort, String path, Map<String, Object> params, int timeout) throws Exception {
		return Sender.create(Request.create("http://" + hostPort + path, Request.METHOD.POST, params, Header.create(ImmutableMap.of(UserConstants.CLUSTER_TOKEN_HEAD, getOrCreateToken())))).setTimeout(timeout).setConnTimeout(timeout).send();
	}


	/**
	 * 传入路径，在路径中寻找合适运行此方法的主机
	 *
	 * @return 保护http。。。地址的
	 */
	public String host(String path) {
		String[] split = path.split("/");
		if (split.length < 5) {
			LOG.error(path + " not match any class it must /api/[groupName]/[className]/[methodName]");
			return null;
		}

		String groupName = split[2];
		String className = split[3];
		String methodName = split[4];

		return host(groupName, className, methodName);

	}


	/**
	 * 传入一个地址，给出路由到的地址，如果返回空则为本机，未找到或其他情况也保留于本机
	 */
	public String host(String groupName, String className, String mehtodName) {

		Map<String, ChildData> currentChildren = null;

		currentChildren = StaticValue.space().getMappingCache().getCurrentChildren(MAPPING_PATH + "/" + groupName + "/" + className + "/" + mehtodName);

		if (currentChildren == null || currentChildren.size() == 0) {
			return null;
		}

		if (StaticValue.TESTRING) { //如果测试模式本地优先
			if (currentChildren.containsKey(StaticValue.getHostPort())) {
				LOG.info("run by testing model , so return self hostPort");
				return null;
			}
		}

		List<HostGroup> hosts = new ArrayList<>();

		int sum = 0;
		for (Map.Entry<String, ChildData> entry : currentChildren.entrySet()) {

			String hostPort = entry.getKey();

			HostGroup hostGroup = StaticValue.space().getHostGroupCache().get(hostPort + "_" + groupName);

			if (hostGroup == null) {
				LOG.warn(HOST_GROUP_PATH + "/" + hostPort + "_" + groupName + " got null , so skip");
				continue;
			}

			Integer weight = hostGroup.getWeight();
			if (weight <= 0) {
				LOG.debug(HOST_GROUP_PATH + "/" + hostPort + "_" + groupName + " weight less than zero , so skip");
				continue;
			}
			sum += weight;
			hosts.add(hostGroup);
		}


		if (hosts.size() == 0) {
			return null;
		}

		int random = new Random().nextInt(sum);

		for (HostGroup hostGroup : hosts) {
			random -= hostGroup.getWeight();
			if (random < 0) {
				if (StaticValue.getHostPort().equals(hostGroup.getHostPort())) { //by self
					return null;
				}
				String toHost = (hostGroup.isSsl() ? "https://" : "http://") + hostGroup.getHostPort();
				LOG.info("{}/{}/{} proxy to {} ", groupName, className, mehtodName, toHost);
				return toHost;
			}
		}

		LOG.info("this log impossible print !");

		return null;

	}

}
