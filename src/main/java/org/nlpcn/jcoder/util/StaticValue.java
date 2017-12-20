package org.nlpcn.jcoder.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import org.nlpcn.jcoder.run.mvc.ApiUrlMappingImpl;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.IocException;
import org.nutz.mvc.Mvcs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

public class StaticValue {

	private static final Logger LOG = LoggerFactory.getLogger(StaticValue.class);

	public static final String PREFIX = "jcoder_";
	public static final String SELF_HOST = "127.0.0.1";

	public static final String ZK_ROOT = "/jcoder";

	public static final String HOME = getValueOrCreate("home", new File(System.getProperty("user.home"), ".jcoder").getAbsolutePath());
	private static final String HOST = getValueOrCreate("host", "*");
	public static final int PORT = TypeUtils.castToInt(getValueOrCreate("port", "8080"));
	public static final int RPCPORT = TypeUtils.castToInt(getValueOrCreate("rpcport", String.valueOf(PORT + 1)));
	//default token
	public static final String TOKEN = getValueOrCreate("token", null);
	public static final String LOG_PATH = getValueOrCreate("log", new File("log/jcoder.log").getAbsolutePath());
	public static final File HOME_FILE = new File(HOME);
	public static final File GROUP_FILE = new File(HOME_FILE, "group");
	public static final String VERSION = getResource("version");

	public static final String ZK = getValueOrCreate("zk", "127.0.0.1:" + (PORT + 2));

	//集群方式还是单机方式启动
	public static final boolean IS_LOCAL = ZK.equals("127.0.0.1:" + (PORT + 2));

	//是否是以SSL方式启动
	public static final boolean IS_SSL = StringUtil.isNotBlank(getValueOrCreate("ssl", null));


	private static boolean master = false;
	private static SharedSpaceService sharedSpace;

	static {
		LOG.info("env in system.propertie: jcoder_home : " + HOME_FILE.getAbsolutePath());
		LOG.info("env in system.propertie: jcoder_host : " + HOST);
		LOG.info("env in system.propertie: jcoder_port : " + PORT);
		LOG.info("env in system.propertie: jcoder_rpcport : " + RPCPORT);
		LOG.info("env in system.propertie: jcoder_log : " + LOG_PATH);
		LOG.info("env in system.propertie: jcoder_group : " + GROUP_FILE.getAbsolutePath());
		LOG.info("env in system.propertie: zookeeper : " + ZK);
		LOG.info("env in system.propertie: ssl : " + getValueOrCreate("ssl", null));
	}

	private static Ioc systemIoc;

	public static BasicDao systemDao; // 系统DAO

	public static final String SYSTEM_SPLIT = "SYSTEM_SPLIT_ANSJ";

	// api路径的映射
	public static final ApiUrlMappingImpl MAPPING = new ApiUrlMappingImpl();


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
	public static <T> T getBean(String groupName, Class<T> t, String name) {
		T object = null;
		try {
			object = JarService.getOrCreate(groupName).getIoc().get(t, name);
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
	public static String passwordEncoding(String password) throws NoSuchAlgorithmException {
		return MD5Util.md5(MD5Util.md5(password + "jcoder"));
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
	 *
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
	 *
	 * @return
	 */
	public static String getConfigHost() {
		return HOST;
	}

	public static void setMaster(boolean flag) {
		master = flag;
	}

	public static boolean isMaster() {
		return master;
	}

	public static void setSharedSpace(SharedSpaceService sharedSpace) {
		StaticValue.sharedSpace = sharedSpace;
	}

	/**
	 * 获得以zookeeper为内存的存储空间
	 *
	 * @return
	 */
	public static SharedSpaceService space() {
		return sharedSpace;
	}


	/**
	 * 获得主机和ip名称 case 127.0.0.1:9095
	 *
	 * @return
	 */
	public static String getHostPort() {
		return getHost() + ":" + PORT;
	}
}