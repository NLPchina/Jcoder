package org.nlpcn.jcoder.util;

import com.google.common.collect.ImmutableMap;
import org.nlpcn.jcoder.constant.UserConstants;
import org.nlpcn.jcoder.domain.Token;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.service.TokenService;
import org.nutz.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public abstract class ProxyHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ProxyHandler.class);

	private static String myToken = null;
	protected String className;
	protected int timeout;
	private String[] hostPorts;

	public void setHostPorts(String... hostPorts) {
		this.hostPorts = hostPorts;
	}

	public void setPath(String path) {

	}


	/**
	 * 提交一个请求
	 */
	public Response post(String hostPort, String path, Map<String, Object> params, int timeout) throws Exception {
		return Sender.create(Request.create("http://" + hostPort + path, Request.METHOD.POST, params, Header.create(ImmutableMap.of(UserConstants.CLUSTER_TOKEN_HEAD, getOrCreateToken())))).setTimeout(timeout).setConnTimeout(timeout).send();
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
}
