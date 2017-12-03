package org.nlpcn.jcoder.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.run.java.DynamicEngine;
import org.nlpcn.jcoder.scheduler.TaskException;
import org.nlpcn.jcoder.util.MD5Util;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.json.JsonLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class JarService {

	private static final Logger LOG = LoggerFactory.getLogger(JarService.class);

	public static final String JAR_PATH = StaticValue.HOME + "/lib";

	public static final String POM = JAR_PATH + "/pom.xml";

	private static final String CONFIG_PATH = JAR_PATH + "/config.properties";

	private static final String MAVEN_PATH = "maven";

	private static final String MD5 = "MD5";

	private static JSONObject config = readConfig();

	public static final Set<String> LIB_PATHS = new HashSet<>();

	/**
	 * 环境加载中
	 */
	public static void init() {
		config = readConfig();

		// 加载mavenpath
		if (StringUtil.isBlank(config.getString(MAVEN_PATH))) {
			setMavenPath(getMavenPath());
		}

		// 如果发生改变则刷新一次
		if (!check()) {
			try {
				flushMaven();
				writeVersion();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		flushClassLoader();
	}

	/**
	 * 统计并加载jar包
	 */
	public static void flushClassLoader() {
		LOG.info("to flush classloader");
		URL[] urls = null;
		try {
			List<File> findJars = findJars();
			urls = new URL[findJars.size()];
			LIB_PATHS.clear();
			for (int i = 0; i < findJars.size(); i++) {
				urls[i] = findJars.get(i).toURI().toURL();
				LOG.info("find JAR " + findJars.get(i));
				LIB_PATHS.add(findJars.get(i).getAbsolutePath());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		URLClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader().getParent()); //不在和系统的classload共享jar包，用来解决jar冲突
		try {
			DynamicEngine.flush(classLoader);
			flushIOC();
		} catch (TaskException e) {
			throw new RuntimeException(e);
		}
	}
	
	

	public static void flushIOC() {
		LOG.info("to flush ioc");
		Ioc ioc = new NutIoc(new JsonLoader(StaticValue.HOME + "/resource/ioc.js"));
		if(StaticValue.getUserIoc()!=null){
			StaticValue.getUserIoc().depose();	
		}
		StaticValue.setUserIoc(ioc);		
	}

	/**
	 * 得到maven路径
	 * 
	 * @return
	 */
	public static String getMavenPath() {
		String mavenPath = null;

		if (config != null) {
			mavenPath = config.getString(MAVEN_PATH);
		}

		if (StringUtil.isBlank(mavenPath)) {
			mavenPath = System.getProperty(StaticValue.PREFIX + "maven");
		}

		if (StringUtil.isBlank(mavenPath)) {
			String home = getPathByVar("MAVEN_HOME");
			if (StringUtil.isBlank(home)) {
				home = getPathByVar("M2_HOME");
			}
			if (StringUtil.isBlank(home)) {
				mavenPath = "mvn";
			} else {
				mavenPath = home + "/bin/mvn";
			}
		}

		return mavenPath;
	}

	/**
	 * 版本写入
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static JSONObject writeVersion() {

		JSONObject job = new JSONObject();

		job.put(MAVEN_PATH, getMavenPath());
		job.put(MD5, readMd5());

		IOUtil.Writer(CONFIG_PATH, IOUtil.UTF8, job.toJSONString());

		config = job;

		return config;
	}

	/**
	 * 读取pom文件的md5
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private static String readMd5() {
		File pom = new File(POM);
		if (pom.exists()) {
			try {
				return MD5Util.md5(getMavenPath() + IOUtil.getContent(pom, IOUtil.UTF8));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "null";
	}

	/**
	 * 检查maven的配置文件是否发生改变
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static boolean check() {
		return String.valueOf(config.get("MD5")).equals(readMd5());
	}

	/**
	 * 读取配置文件
	 * 
	 * @return
	 */
	private static JSONObject readConfig() {
		if (new File(CONFIG_PATH).exists()) {
			return JSONObject.parseObject(IOUtil.getContent(CONFIG_PATH, IOUtil.UTF8));
		} else {
			return new JSONObject();
		}
	}

	/**
	 * copyjar包到当前目录中
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String copy() throws IOException {
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			return execute("cmd", "/c", getMavenPath(), "-f", "pom.xml", "dependency:copy-dependencies");
		} else {
			return execute(getMavenPath(), "-f", "pom.xml", "dependency:copy-dependencies");
		}
	}

	/**
	 * 删除jiar包
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String clean() throws IOException {
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			return execute("cmd", "/c", getMavenPath(), "clean");
		} else {
			return execute(getMavenPath(), "clean");
		}
	}

	/**
	 * 刷新jar包
	 * 
	 * @return
	 * @throws IOException
	 */
	public synchronized static void flushMaven() throws IOException {
		clean();
		copy();
	}

	private static String execute(String... args) throws IOException {

		LOG.info("exec : " + Arrays.toString(args));

		StringBuilder sb = new StringBuilder();

		try {
			ProcessBuilder pb = new ProcessBuilder(args);
			pb.directory(new File(JAR_PATH));

			pb.redirectErrorStream(true);

			/* Start the process */
			Process proc = pb.start();

			LOG.info("Process started !");

			/* Read the process's output */
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = in.readLine()) != null) {
				sb.append(line).append("\n");
				LOG.info(line);
			}

			/* Clean-up */
			proc.destroy();
			LOG.info("Process ended !");
		} catch (Exception e) {
			LOG.warn("MAVEN_PATH ERR : " + e);
		}

		return sb.toString();
	}

	/**
	 * 查找所有的jar
	 * 
	 * @return
	 * @throws IOException
	 */
	public static List<File> findJars() throws IOException {
		List<File> findAllJar = new ArrayList<>();

		Files.walkFileTree(new File(JAR_PATH).toPath(), new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				File f = file.toFile();
				if (f.getName().toLowerCase().endsWith(".jar")) {
					findAllJar.add(f);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		return findAllJar;
	}

	private static String getPathByVar(String var) {
		String home = System.getProperty(var);

		if (StringUtil.isBlank(home)) {
			home = System.getenv("MAVEN_HOME");
		}
		return home;
	}

	/**
	 * 保存pom文件
	 * 
	 * @param code
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static String savePom(String code) throws IOException, NoSuchAlgorithmException {
		IOUtil.Writer(JarService.POM, IOUtil.UTF8, code);

		String message = "保存完毕，文件没有做任何更改！";

		if (!check()) {
			synchronized (DynamicEngine.getInstance()) {
				DynamicEngine.close();
				flushMaven();
				writeVersion();
				flushClassLoader();
			}
			message = "保存并更新jar包成功!";
		}

		return message;
	}

	/**
	 * 设置maven路径
	 * 
	 * @param mavenPath
	 */
	public static void setMavenPath(String mavenPath) {
		config.put(MAVEN_PATH, mavenPath);
	}

	/**
	 * 得到启动时候加载的路径
	 * 
	 * @return
	 */
	public static HashSet<String> getLibPathSet() {
		return new HashSet<>(LIB_PATHS);
	}

	/**
	 * 删除一个jar包.只能删除非maven得jar包
	 * 
	 * @param file
	 * @return
	 */
	public static boolean removeJar(File file) {
		if (file.getParentFile().getAbsolutePath().equals(new File(JAR_PATH).getAbsolutePath()) && file.getPath().toLowerCase().endsWith(".jar")) {
			try {
				synchronized (DynamicEngine.getInstance()) {
					DynamicEngine.close();
					for (int i = 0; i < 10 && file.exists(); i++) {
						LOG.info(i + " to delete file: " + file.getAbsolutePath());
						file.delete();
						Thread.sleep(300L);
					}
					flushClassLoader();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return true;
		} else {
			LOG.info(file.getAbsolutePath()+" is not manager by file JAR_PATH is :"+JAR_PATH);
			return false;
		}

	}

	/**
	 * 获得系统中的jar.就是WEB-INF/lib下面的.对于eclipse中.取得SystemLoad
	 * 
	 * @return
	 * @throws URISyntaxException
	 */
	public static List<File> findSystemJars() throws URISyntaxException {

		URLClassLoader classLoader = ((URLClassLoader) Thread.currentThread().getContextClassLoader());

		URL[] urls = classLoader.getURLs();

		if (urls.length == 0) {
			classLoader = (URLClassLoader) classLoader.getParent();
			urls = classLoader.getURLs();
		}

		List<File> systemFiles = new ArrayList<>();

		for (URL url : urls) {
			systemFiles.add(new File(url.toURI()));
		}

		return systemFiles;
	}


}
