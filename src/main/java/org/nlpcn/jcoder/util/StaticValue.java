package org.nlpcn.jcoder.util;

import java.io.File;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.nlpcn.commons.lang.util.MD5;
import org.nlpcn.jcoder.run.mvc.ApiUrlMappingImpl;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.IocException;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.json.JsonLoader;
import org.nutz.mvc.Mvcs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class StaticValue {

	private static final Logger LOG = Logger.getLogger(StaticValue.class);

	public static final String PREFIX = "jcoder_";

	public static final String HOME = getValueOrCreate("home", new File(System.getProperty("user.home"), ".jcoder").getAbsolutePath());
	public static final String HOST = getValueOrCreate("host", null);
	public static final String LOG_PATH = getValueOrCreate("log", "log/jcoder.log");

	public static final File HOME_FILE = new File(HOME);
	public static final File RESOURCE_FILE = new File(HOME_FILE, "resource");
	public static final File LIB_FILE = new File(HOME_FILE, "lib");
	public static final File PLUGIN_FILE = new File(HOME_FILE, "plugins");

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
		if (userIoc == null) {
			userIoc = new NutIoc(new JsonLoader(StaticValue.HOME + "/resource/ioc.js"));
		}
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
	public static Object getBean(String name) {
		return getBean(null, name);
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
			LOG.info(e.getMessage());
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
		ResourceBundle bundle = ResourceBundle.getBundle("config");
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

}
