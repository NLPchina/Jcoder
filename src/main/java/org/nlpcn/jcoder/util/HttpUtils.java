package org.nlpcn.jcoder.util;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 提供了网页下载的工具包
 * 
 * @author tjx
 * 
 */
public class HttpUtils {

	private static final Logger LOG = Logger.getLogger(HttpUtils.class);
	private static final String userAgent = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)";

	/**
	 * 下载一个网页的内容去除html
	 * 
	 * @param url
	 * @return
	 */
	public static String getText(String url) {
		Document document = getDocument(url);
		if (document != null) {
			return document.text();
		}
		return null;
	}

	/**
	 * 返回jsonp的文档
	 * 
	 * @param url
	 * @return
	 */
	public static Document getDocument(String url) {
		Document document = null;
		try {
			document = Jsoup.connect(url).userAgent("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)").referrer("www.test.com").get();
			return document;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(e);
		}
		return null;
	}

	/**
	 * 下载一个网页的内容包含html
	 * 
	 * @param url
	 * @return
	 */
	public static String getHtml(String url) {
		Document document = getDocument(url);
		if (document != null) {
			return document.html();
		}
		return null;
	}

	/**
	 * 下载一个网页的内容包含html
	 * 
	 * @param url
	 * @return
	 */
	public static Document postDocument(String url, Map<String, String> data) {
		Document document = null;
		try {
			document = Jsoup.connect(url).userAgent(userAgent).referrer("www.test.com").data(data).post();
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error(e);
		}
		return document;
	}

	/**
	 * 下载一个网页的内容包含html
	 * 
	 * @param url
	 * @return
	 */
	public static String postHtml(String url, Map<String, String> data) {
		Document document = postDocument(url, data);
		if (document != null) {
			return document.html();
		}
		return null;
	}

	/**
	 * 通过get方式获取json数据
	 * 
	 * @param url
	 * @return
	 */
	public static String getJSONStr(String url) {
		try {
			Response response = Jsoup.connect(url).timeout(600000).execute();
			return response.body();
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error(e);
		}
		return null;

	}

	/**
	 * 通过get方式获取json数据
	 * 
	 * @param url
	 * @return
	 */
	public static String getJSONStr(String url, Map<String, String> heads) {
		try {

			Connection connect = Jsoup.connect(url);

			for (Entry<String, String> entry : heads.entrySet()) {
				connect.header(entry.getKey(), entry.getValue());

			}
			Response response = connect.timeout(600000).execute();
			return response.body();
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error(e);
		}
		return null;

	}

	/**
	 * 通过get方式获取json数据
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String getJSONStr(String url, int timeout) throws IOException {
		Response response = Jsoup.connect(url).timeout(timeout).execute();
		return response.body();

	}

	/**
	 * 通过post方式获取json数据
	 * 
	 * @param url
	 * @return
	 */
	public static String postJSONStr(String url, Map<String, String> data) {
		try {
			Connection conn = Jsoup.connect(url).timeout(600000).method(Method.POST);
			if (data != null && !data.isEmpty()) {
				conn.data(data);
			}
			conn.userAgent(userAgent);
			Response response = conn.execute();
			return response.body();
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error(e);
		}
		return null;

	}

	/**
	 * 通过post方式获取json数据
	 * 
	 * @param url
	 * @return
	 */
	public static String postJSONStr(String url, Map<String, String> data, Map<String, Object> cookies) {
		try {
			Connection conn = Jsoup.connect(url).timeout(600000).method(Method.POST);
			if (data != null && !data.isEmpty()) {
				conn.data(data);
			}
			for (Entry<String, Object> entry : cookies.entrySet()) {
				conn.cookie(entry.getKey(), String.valueOf(entry.getValue()));
			}
			conn.userAgent(userAgent);
			Response response = conn.execute();
			return response.body();
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error(e);
		}
		return null;

	}

	/**
	 * 通过post方式获取
	 * 
	 * @param url
	 * @return
	 */
	public static Response postResponse(String url, Map<String, String> data, Map<String, Object> cookies) {
		try {
			Connection conn = Jsoup.connect(url).timeout(600000).method(Method.POST);
			if (data != null && !data.isEmpty()) {
				conn.data(data);
			}
			if (cookies != null && !cookies.isEmpty()) {
				for (Entry<String, Object> entry : cookies.entrySet()) {
					conn.cookie(entry.getKey(), String.valueOf(entry.getValue()));
				}
			}
			conn.userAgent(userAgent);
			return conn.execute();
		} catch (IOException e) {
			e.printStackTrace();
			LOG.error(e);
		}
		return null;
	}
}
