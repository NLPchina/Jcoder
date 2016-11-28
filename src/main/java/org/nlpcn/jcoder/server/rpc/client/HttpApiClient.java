package org.nlpcn.jcoder.server.rpc.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.nlpcn.jcoder.util.ApiException;
import org.nutz.http.Header;
import org.nutz.http.Http;
import org.nutz.http.Request;
import org.nutz.http.Request.METHOD;
import org.nutz.http.Response;
import org.nutz.http.Sender;

import com.alibaba.fastjson.JSONObject;

/**
 * http接口
 * 
 * @author Ansj
 *
 */
@Deprecated
public class HttpApiClient {

	private static final String TOKEN_HEAD = "authorization";

	private String ip;
	private int port;
	private String name;
	private String password;

	private Header header;

	public HttpApiClient(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	/**
	 * 具有登录的restful api 接口
	 * 
	 * @param ip
	 * @param port
	 * @param name
	 * @param password
	 * @param autoCheck 是否自动检查token过期.检查频率为10分钟一次.保证token一直续约
	 * @throws Exception
	 */
	public HttpApiClient(String ip, int port, String name, String password, boolean validation) throws Exception {
		this.ip = ip;
		this.port = port;
		this.name = name;
		this.password = password;
		login();
	}

	/**
	 * 进行登录验证
	 * 
	 * @throws Exception
	 */
	public synchronized void login() throws Exception {

		if (this.header != null && validation()) { //如果已经持有token ,则不再登录
			return;
		}

		Map<String, Object> params = new HashMap<>();

		params.put("name", name);
		params.put("password", password);
		Response response = Http.post2("http://" + ip + ":" + port + "/login/api", params, 60000);

		JSONObject job = JSONObject.parseObject(response.getContent());

		if (response.getStatus() == 200) {
			String token = job.getString("obj");
			Map<String, String> headMap = new HashMap<>();
			headMap.put(TOKEN_HEAD, token);
			header = Header.create(headMap);
		} else {
			throw new ApiException(ApiException.Forbidden, job.toJSONString());
		}
	}

	private boolean validation() {
		Response response = Http.get("http://" + ip + ":" + port + "/validation/token");
		if (response.getStatus() != 200) {
			return true;
		} else {
			return false;
		}

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
		StringBuilder paramSb = new StringBuilder();

		if (params != null && params.size() > 0) {
			for (Entry<String, Object> entry : params.entrySet()) {
				if (paramSb.length() > 0) {
					paramSb.append("&");
				}
				paramSb.append(entry.getKey());
				paramSb.append("=");
				paramSb.append(URLEncoder.encode(String.valueOf(entry.getValue()), "utf-8"));
			}
		}

		return get(className, methodName, paramSb.toString(), timeout);
	}

	/**
	 * 通过get方式获取流
	 * 
	 * @param className
	 * @param methodName
	 * @param params 直接拼装的http参数 ,不需要带开始的?号
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public Response get(String className, String methodName, String params, int timeout) throws MalformedURLException, IOException {
		StringBuilder makeUrl = makeUrl(className, methodName);
		makeUrl.append("?").append(params);
		Request req = Request.get(makeUrl.toString());
		if (header != null) {
			req = req.setHeader(header);
		}
		return Sender.create(req).setTimeout(timeout).send();
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
		return Sender.create(Request.create(makeUrl(className, methodName).toString(), METHOD.POST, params, header)).setTimeout(timeout).send();
	}

	/**
	 * POST 一个字符串
	 * 
	 * @param className
	 * @param methodName
	 * @param content
	 * @param timeout
	 * @return
	 */
	public Response post(String className, String methodName, Object body, int timeout) {
		return Http.post3(makeUrl(className, methodName).toString(), body, header, timeout);
	}

	/**
	 * 上传一个文件
	 * 
	 * @param className
	 * @param methodName
	 * @param params
	 * @param timeout
	 * @return
	 */
	public Response upload(String className, String methodName, Map<String, Object> params, int timeout) {
		return Http.upload(makeUrl(className, methodName).toString(), params, header, timeout);
	}

	private StringBuilder makeUrl(String className, String methodName) {
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(ip);
		sb.append(":");
		sb.append(port);
		sb.append("/api/");
		sb.append(className);
		sb.append(methodName);
		return sb;
	}

}
