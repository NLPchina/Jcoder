package org.nlpcn.jcoder.util;

import java.io.File;
import java.util.ResourceBundle;

import org.nlpcn.commons.lang.util.MD5;
import org.nlpcn.commons.lang.util.ObjConver;
import org.nlpcn.jcoder.run.mvc.ApiUrlMappingImpl;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.IocException;
import org.nutz.mvc.Mvcs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class StaticValue {

	private static final Logger LOG = LoggerFactory.getLogger(StaticValue.class);

	public static final String PREFIX = "jcoder_";
	public static final String SELF_HOST = "127.0.0.1";

	public static final String HOME = getValueOrCreate("home", new File(System.getProperty("user.home"), ".jcoder").getAbsolutePath());
	private static final String HOST = getValueOrCreate("host", "*");
	public static final int PORT = ObjConver.getIntValue(getValueOrCreate("port", "8080"));
	public static final int RPCPORT = ObjConver.getIntValue(getValueOrCreate("rpcport", String.valueOf(PORT + 1)));
	public static final String LOG_PATH = getValueOrCreate("log", new File("log/jcoder.log").getAbsolutePath());
	public static final File HOME_FILE = new File(HOME);
	public static final File RESOURCE_FILE = new File(HOME_FILE, "resource");
	public static final File LIB_FILE = new File(HOME_FILE, "lib");
	public static final File PLUGIN_FILE = new File(HOME_FILE, "plugins");
	public static final String VERSION = getResource("version");
	public static final File UPLOAD_DIR = new File(getValueOrCreate("upload", new File(HOME_FILE, "upload").getAbsolutePath()));

	static {
		LOG.info("env in system.propertie: jcoder_home : " + HOME_FILE.getAbsolutePath());
		LOG.info("env in system.propertie: jcoder_host : " + HOST);
		LOG.info("env in system.propertie: jcoder_port : " + PORT);
		LOG.info("env in system.propertie: jcoder_rpcport : " + RPCPORT);
		LOG.info("env in system.propertie: jcoder_log : " + LOG_PATH);
		LOG.info("env in system.propertie: jcoder_resource : " + RESOURCE_FILE.getAbsolutePath());
		LOG.info("env in system.propertie: jcoder_lib : " + LIB_FILE.getAbsolutePath());
		LOG.info("env in system.propertie: jcoder_plugins : " + PLUGIN_FILE.getAbsolutePath());
		LOG.info("env in system.propertie: jcoder_upload : " + UPLOAD_DIR.getAbsolutePath());
	}

	private static Ioc systemIoc;

	private static Ioc userIoc;

	public static BasicDao systemDao; // 系统DAO

	// 成功
	public static final String OK = JSONObject.toJSONString(new JsonResult(true));

	// 错误
	public static final String ERR = JSONObject.toJSONString(new JsonResult(false));

	// 成功
	public static final JSONObject OK_J = (JSONObject) JSONObject.toJSON(new JsonResult(true));

	// 错误
	public static final JSONObject ERR_J = (JSONObject) JSONObject.toJSON(new JsonResult(false));

	public static final String SYSTEM_SPLIT = "SYSTEM_SPLIT_ANSJ";

	// api路径的映射
	public static final ApiUrlMappingImpl MAPPING = new ApiUrlMappingImpl();

	/**
	 * 失败消息
	 * 
	 * @param message
	 * @return
	 */
	public static String errMessage(Exception e) {
		String error = JSON.toJSONString(errMessageJson(e));
		return error;
	}

	private static String getValueOrCreate(String key, String def) {
		String value = System.getProperty(PREFIX + key);
		if (LOG.isDebugEnabled()) {
			LOG.debug("get property " + key + ":" + value);
		}
		if (value == null) {
			if (def != null) {
				System.setProperty(PREFIX + key, def);
			}
			return def;
		} else {
			return value;
		}
	}

	/**
	 * 失败消息
	 * 
	 * @param message
	 * @return
	 */
	public static JsonResult errMessageJson(Exception e) {
		JsonResult jsonResult = new JsonResult(false);
		jsonResult.setException(e);
		return jsonResult;
	}

	/**
	 * 失败消息
	 * 
	 * @param message
	 * @return
	 */
	public static String errMessage(String message) {
		return JSON.toJSONString(errMessageJson(message));
	}

	public static JsonResult errMessageJson(String message) {
		JsonResult jsonResult = new JsonResult(false);
		jsonResult.setMessage(message);
		return jsonResult;
	}

	/**
	 * 成功消息
	 * 
	 * @param message
	 * @return
	 */
	public static String okMessage(String message) {
		return JSON.toJSONString(okMessageJson(message));
	}

	/**
	 * 成功消息
	 * 
	 * @param message
	 * @return
	 */
	public static JsonResult okMessageJson(String message) {
		JsonResult jsonResult = new JsonResult(true);
		jsonResult.setMessage(message);
		return jsonResult;
	}

	public static JSONObject makeReuslt(boolean ok, Object object) {
		JSONObject job = new JSONObject();
		job.put("result", object);
		job.put("ok", ok);
		return job;
	}

	public static Ioc getUserIoc() {
		return userIoc;
	}

	// default ioc is userIoc
	public static Ioc getIoc() {
		return getUserIoc();
	}

	public static Ioc getSystemIoc() {
		if (systemIoc == null) {
			systemIoc = Mvcs.getIoc();
		}
		return systemIoc;
	}

	/**
	 * 從ｉｏｃ容器中獲取ｂｅａｎ
	 * 
	 * @param name
	 * @return
	 */
	public static <T> T getBean(Class<T> t, String name) {
		T object = null;
		try {
			object = getUserIoc().get(t, name);
		} catch (IocException e) {
			LOG.error(e.getMessage());
		}
		if (object == null) {
			object = getSystemIoc().get(t, name);
		}
		return object;
	}

	public static void setSystemIoc(Ioc ioc) {
		StaticValue.systemIoc = ioc;
	}

	public static void setUserIoc(Ioc ioc) {
		StaticValue.userIoc = ioc;
	}

	/**
	 * 从配置文件查找
	 * 
	 * @param key
	 * @return
	 */
	public static String getResource(String key) {
		ResourceBundle bundle = ResourceBundle.getBundle("jcoder");
		if (bundle.containsKey(key)) {
			return bundle.getString(key);
		} else {
			return null;
		}
	}

	/**
	 * md5 code
	 * 
	 * @param password
	 * @return
	 */
	public static String passwordEncoding(String password) {
		return MD5.code(MD5.code(password + "jcoder"));
	}

	/**
	 * 获取用户ip
	 * 
	 * @param request
	 * @return
	 */
	public static String getRemoteHost(javax.servlet.http.HttpServletRequest request) {

		if (request == null) {
			return null;
		}

		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
	}

	/**
	 * 获得host如果host为* 则返回127.0.0.1
	 * @return
	 */
	public static String getHost() {
		if ("*".equals(HOST)) {
			return SELF_HOST;
		}
		return HOST;
	}

	/**
	 * 获得用户配置的host
	 * @return
	 */
	public static String getConfigHost() {
		return HOST;
	}
}