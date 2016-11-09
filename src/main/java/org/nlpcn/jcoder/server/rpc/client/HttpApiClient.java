package org.nlpcn.jcoder.server.rpc.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.nlpcn.commons.lang.util.IOUtil;
import org.nutz.http.Http;
import org.nutz.http.Response;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * http接口
 * 
 * @author Ansj
 *
 */
public class HttpApiClient {
	private String ip;
	private int port;

	public HttpApiClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}


	/**
	 * 通过get方式获取流
	 * 
	 * @param className
	 * @param methodName
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public Response get(String className, String methodName, int timeout) throws MalformedURLException, IOException {
		return get(className, methodName, timeout, Collections.emptyMap());
	}

	/**
	 * 通过get方式获取流
	 * 
	 * @param className
	 * @param methodName
	 * @param params
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public Response get(String className, String methodName, int timeout, Map<String, Object> params) throws MalformedURLException, IOException {
		StringBuilder url = new StringBuilder("http://" + ip + ":" + port + "/api/" + className + "/" + methodName + "?1=1");

		if (params != null && params.size() > 0) {
			for (Entry<String, Object> entry : params.entrySet()) {
				url.append("&");
				url.append(entry.getKey());
				url.append("=");
				url.append(URLEncoder.encode(String.valueOf(entry.getValue()), "utf-8"));
			}
		}

		return Http.get(url.toString(), timeout);
	}

	/**
	 * 通过get方式获取流
	 * 
	 * @param className
	 * @param methodName
	 * @param params 直接拼装的sql ,不需要带开始的?号
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public Response get(String className, String methodName, int timeout, String params) throws MalformedURLException, IOException {
		StringBuilder url = new StringBuilder("http://" + ip + ":" + port + "/api/" + className + "/" + methodName + "?" + params);
		return Http.get(url.toString(), timeout);
	}

	/**
	 * 通过post方式获取流
	 * 
	 * @param className
	 * @param methodName
	 * @param params
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public Response post(String className, String methodName, int timeout, Map<String, Object> params) throws MalformedURLException, IOException {
		return Http.post2("http://" + ip + ":" + port + "/api/" + className + "/" + methodName.toString(), params, timeout);
	}

}
