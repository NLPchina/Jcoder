package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.nlpcn.jcoder.domain.GroupCache;
import org.nlpcn.jcoder.run.java.DynamicEngine;
import org.nlpcn.jcoder.scheduler.TaskException;
import org.nlpcn.jcoder.util.IOUtil;
import org.nlpcn.jcoder.util.MD5Util;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.ioc.loader.json.JsonLoader;
import org.nutz.lang.Lang;
import org.nutz.lang.reflect.FastClassFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@IocBean
public class JarService {

	private static final Logger LOG = LoggerFactory.getLogger(JarService.class);
	private static final LoadingCache<String, JarService> CACHE = CacheBuilder.newBuilder()
			.removalListener((RemovalListener<String, JarService>) notification -> {

				try {
					notification.getValue().getIoc().depose();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if (notification.getValue().engine != null) {
						notification.getValue().engine.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}).build(new CacheLoader<String, JarService>() {
				@Override
				public JarService load(String key) throws Exception {
					return new JarService(key);
				}
			});
	private static final ConcurrentHashMap<String, Lock> LOCK_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();
	private static final String MAVEN_PATH = "maven";

	public Set<String> libPaths = new HashSet<>();
	@Inject
	private BasicDao basicDao;
	private String groupName;
	private String jarPath = null;
	private String pomPath = null;
	private String iocPath = null;
	private DynamicEngine engine;
	private Ioc ioc;

	private JarService(String groupName) throws IOException {
		this.groupName = groupName;

		jarPath = new File(StaticValue.GROUP_FILE, groupName + "/lib").getCanonicalPath();
		if (!new File(jarPath).exists()) {
			new File(jarPath).mkdirs();
			LOG.warn("lib path not exists so create it");
		}
		pomPath = new File(StaticValue.GROUP_FILE, groupName + "/pom.xml").getCanonicalPath();
		iocPath = new File(StaticValue.GROUP_FILE, groupName + "/resources/ioc.js").getCanonicalPath();
		engine = new DynamicEngine(groupName);
		init();
	}

	public static JarService getOrCreate(String groupName) {

		JarService jarService = CACHE.getIfPresent(groupName);


		if (jarService == null) {
			long start = System.currentTimeMillis();
			LOG.info("to init JarService by group {}", groupName);
			try {
				lock(groupName);
				jarService = CACHE.get(groupName);
				StaticValue.getSystemIoc().get(TaskService.class, "taskService").flushGroup(groupName);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				unLock(groupName);
			}
			LOG.info("init JarService by group {} ok use time : {}", groupName, System.currentTimeMillis() - start);
		}

		return jarService;
	}

	/**
	 * 锁一个group
	 */
	public synchronized static void lock(String groupName) {
		LOCK_CONCURRENT_HASH_MAP.computeIfAbsent(groupName, (k) -> new ReentrantLock()).lock();
	}

	/**
	 * 解锁一个group
	 */
	public synchronized static void unLock(String groupName) {
		Lock lock = LOCK_CONCURRENT_HASH_MAP.get(groupName);
		if (lock != null) {
			lock.unlock();
			LOCK_CONCURRENT_HASH_MAP.remove(groupName);
		}
	}

	public static void remove(String groupName) {
		CACHE.invalidate(groupName);
	}

	/**
	 * 环境加载中
	 */
	private void init() {
		// 如果发生改变则刷新一次
		try {
			flushMaven();
			flushClassLoader();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * 统计并加载jar包
	 */
	private void flushClassLoader() {
		LOG.info("to flush classloader");
		URL[] urls = null;
		try {
			List<File> findJars = findJars();
			urls = new URL[findJars.size()];
			libPaths.clear();
			for (int i = 0; i < findJars.size(); i++) {
				urls[i] = findJars.get(i).toURI().toURL();
				LOG.debug("find JAR " + findJars.get(i));
				libPaths.add(findJars.get(i).getAbsolutePath());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		URLClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

		try {
			engine.flush(classLoader);
			flushIOC();
		} catch (TaskException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * 保存ioc文件
	 */
	public void saveIoc(String groupName, String code) throws IOException, NoSuchAlgorithmException {
		File ioc = new File(StaticValue.GROUP_FILE, groupName + "/resources");
		IOUtil.Writer(new File(ioc, "ioc.js").getAbsolutePath(), "utf-8", code);
		this.release();
	}

	private synchronized void flushIOC() {
		LOG.info("to flush ioc");

		JsonLoader loader = null;

		if (!new File(iocPath).exists()) {
			LOG.warn("iocPath: {} not exists so create an empty ioc!!!!!!!");
			loader = new JsonLoader();
		} else {
			loader = new JsonLoader(iocPath);
		}
		ioc = new NutIoc(loader);

		// 实例化lazy为false的bean
		loader.getMap().entrySet().stream()
				.filter(entry -> entry.getValue().containsKey("type") && Objects.equals(false, entry.getValue().get("lazy")))
				.forEach(entry -> {
					// 移除自定义配置项lazy
					entry.getValue().remove("lazy");

					LOG.info("to init bean[{}{}]", entry.getKey(), entry.getValue());

					//TODO: nutz的bug 错误缓存
					FastClassFactory.clearCache();
					//
					ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
					try {
						Thread.currentThread().setContextClassLoader(engine.getClassLoader());
						ioc.get(Lang.loadClass(entry.getValue().get("type").toString()), entry.getKey());
					} catch (Throwable e) {
						e.printStackTrace();
						LOG.error("ioc lazy err for classLoader: {}", entry);
					} finally {
						Thread.currentThread().setContextClassLoader(contextClassLoader);
					}
				});
	}

	/**
	 * 得到maven路径
	 */
	private String getMavenPath() {
		String mavenPath = null;

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
	 * copyjar包到当前目录中
	 */
	private String copy() throws IOException {
		String tempPom = pomContent();
		File tempFile = new File(jarPath + "/pom.xml");
		try (FileOutputStream fos = new FileOutputStream(new File(jarPath + "/pom.xml"))) {
			fos.write(tempPom.getBytes("utf-8"));
			fos.flush();
		}
		try {

			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				return execute(new File(jarPath), "cmd", "/c", getMavenPath(), "-f", "pom.xml", "dependency:copy-dependencies", "-DexcludeScope=system", "-DoutputDirectory=target/dependency");
			} else {
				return execute(new File(jarPath), getMavenPath(), "-f", "pom.xml", "dependency:copy-dependencies", "-DexcludeScope=system", "-DoutputDirectory=target/dependency");
			}
		} finally {
			org.nutz.lang.Files.deleteFile(tempFile); //执行完毕后删除
		}
	}

	/**
	 * 删除jiar包
	 */
	private void clean() throws IOException {
		String tempPom = pomContent();

		File tempFile = new File(jarPath + "/pom.xml");

		try (FileOutputStream fos = new FileOutputStream(new File(jarPath + "/pom.xml"))) {
			fos.write(tempPom.getBytes("utf-8"));
			fos.flush();
		}

		try {

			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				execute(new File(jarPath), "cmd", "/c", getMavenPath(), "clean");
			} else {
				execute(new File(jarPath), getMavenPath(), "clean");
			}
		} finally {
			org.nutz.lang.Files.deleteFile(tempFile); //执行完毕后删除
		}
	}

	/**
	 * 刷新jar包
	 */
	private synchronized void flushMaven() throws IOException {
		//判断文件是否发生改变

		File pomFile = new File(pomPath);

		if (!pomFile.exists()) {
			LOG.warn(pomFile.getCanonicalPath() + " not found in local");
			return;
		}

		GroupCache groupCache = null;

		try {
			File cacheFile = new File(StaticValue.GROUP_FILE, groupName + ".cache");
			if (cacheFile.exists()) {
				String content = IOUtil.getContent(cacheFile, "utf-8");
				if (StringUtil.isNotBlank(content)) {
					groupCache = JSONObject.parseObject(content, GroupCache.class);
				}
			} else {
				groupCache = new GroupCache();
			}
		} catch (Exception e) {
			groupCache = new GroupCache();
			LOG.warn(groupName + " cache read err so create new ");
		}

		String pomMD5 = getPomMd5();

		if (pomMD5 != null && !pomMD5.equals(groupCache.getPomMD5())) {
			groupCache.setPomMD5(pomMD5);
			IOUtil.Writer(new File(StaticValue.GROUP_FILE, groupName + ".cache").getAbsolutePath(), "utf-8", JSONObject.toJSONString(groupCache));
			clean();
			copy();
		}
	}

	/**
	 * 获取pom文件的内容并移除掉jcoder元素
	 */
	private String pomContent() {
		File file = new File(pomPath);
		if (!file.exists()) {
			return null;
		}
		String content = IOUtil.getContent(file, "utf-8");

		String[] split = content.split("\n");

		int mid = 0;

		for (int i = 0; i < split.length; i++) {
			if ("<scope>system</scope>".equals(split[i].replace("\\s+", "").trim())) {
				mid = i;
				break;
			}
		}

		int min = mid;

		while (!"<dependency>".equals(split[min].replace("\\s+", "").trim())) {
			min--;
		}

		int max = mid;

		while (!"</dependency>".equals(split[max].replace("\\s+", "").trim())) {
			max++;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < min; i++) {
			sb.append(split[i]);
			sb.append("\n");
		}

		for (int i = max + 1; i < split.length; i++) {
			sb.append(split[i]);
			sb.append("\n");
		}

		return sb.toString();

	}


	/**
	 * 获得当前group pom的md5
	 */
	public String getPomMd5() {
		File file = new File(pomPath);
		if (!file.exists()) {
			return null;
		}
		String localMd5 = MD5Util.md5(file);
		return localMd5;
	}

	private String execute(File baseDir, String... args) throws IOException {

		LOG.info("exec : " + Arrays.toString(args));

		StringBuilder sb = new StringBuilder();

		try {
			ProcessBuilder pb = new ProcessBuilder(args);
			pb.directory(baseDir);

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
	 */
	private List<File> findJars() throws IOException {
		List<File> findAllJar = new ArrayList<>();

		if (!new File(jarPath).exists()) {
			return findAllJar;
		}

		Files.walkFileTree(new File(jarPath).toPath(), new SimpleFileVisitor<Path>() {

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

	private String getPathByVar(String var) {
		String home = System.getProperty(var);

		if (StringUtil.isBlank(home)) {
			home = System.getenv("MAVEN_HOME");
		}
		return home;
	}

	/**
	 * 保存pom文件
	 */
	public void savePom(String groupName, String content) throws IOException, NoSuchAlgorithmException {
		File pom = new File(StaticValue.GROUP_FILE, groupName);
		IOUtil.Writer(new File(pom, "pom.xml").getAbsolutePath(), "utf-8", content);
		this.release();
	}

	/**
	 * 删除一个jar包.只能删除非maven得jar包
	 */
	public boolean removeJar(File file) {
		if (file.getParentFile().getAbsolutePath().equals(new File(jarPath).getAbsolutePath()) && file.getPath().toLowerCase().endsWith(".jar")) {
			try {
				synchronized (this) {
					for (int i = 0; i < 10 && file.exists(); i++) {
						this.release();
						System.gc();
						Thread.sleep(300L);
						LOG.info(i + " to delete file: " + file.getAbsolutePath());
						file.delete();
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.release();
			return true;
		} else {
			LOG.info(file.getAbsolutePath() + " is not manager by file JAR_PATH is :" + jarPath);
			return false;
		}

	}


	public Ioc getIoc() {
		return ioc;
	}

	public DynamicEngine getEngine() {
		return engine;
	}

	/**
	 * 释放和关闭当前jarservice。在操作。ioc和jar 之后。都需要调用此方式使之生效
	 */
	private synchronized void release() {
		CACHE.invalidate(groupName);
	}

	/**
	 * 刷新一个
	 * @param groupName
	 */
	public static void flush(String groupName) {
		try {
			lock(groupName);
			remove(groupName);
			getOrCreate(groupName);
		} finally {
			unLock(groupName);
		}
	}
}
