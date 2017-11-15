package org.nlpcn.jcoder.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.nlpcn.commons.lang.util.FileFinder;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.commons.lang.util.tuples.KeyValue;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nutz.http.Http;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.NutIoc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.json.JsonLoader;
import org.nutz.lang.Mirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * Test your task
 * 
 * @author Ansj
 *
 */
public class Testing {

	private static final Logger LOG = LoggerFactory.getLogger(Testing.class);

	private static final String IOC_PATH = "src/test/resources/ioc.js";

	/**
	 * instan task by ioc
	 * 
	 * @param c
	 * @return class c instance
	 * @throws Exception
	 */
	public static <T> T instance(Class<T> c, String iocPath, Class<?>... relation) throws Exception {
		Ioc ioc = new NutIoc(new JsonLoader(iocPath));

		StaticValue.setSystemIoc(ioc);
		StaticValue.setUserIoc(ioc);

		relation(ioc, relation);

		Mirror<?> mirror = Mirror.me(c);
		T obj = c.newInstance();

		for (Field field : mirror.getFields()) {
			Inject inject = field.getAnnotation(Inject.class);
			if (inject != null) {
				if (field.getType().equals(org.slf4j.Logger.class)) {
					mirror.setValue(obj, field, LoggerFactory.getLogger(c));
				} else {
					mirror.setValue(obj, field, ioc.get(field.getType(), StringUtil.isBlank(inject.value()) ? field.getName() : inject.value()));
				}
			}
		}

		return obj;
	}

	public static <T> T instance(Class<T> c, Class<?>... relation) throws Exception {

		File find = new File("src/test/resources/ioc.js");

		if (!find.exists()) {
			LOG.warn("ioc config not find in {} , will find it ", find.getAbsolutePath());
			find = FileFinder.find("ioc.js", 1);
			if (find != null)
				LOG.info("ioc config find in " + find.getAbsolutePath());
		}

		if (find != null && find.exists()) {
			return instance(c, find.getAbsolutePath(), relation);
		} else {
			throw new FileNotFoundException("ioc.js not found in your classpath ");
		}

	}

	private static void relation(Ioc ioc, Class<?>... clas) throws InstantiationException, IllegalAccessException {
		if (clas == null || clas.length == 0) {
			return;
		}

		for (Class<?> c : clas) {
			Mirror<?> mirror = Mirror.me(c);
			Object obj = c.newInstance();

			for (Field field : mirror.getFields()) {
				Inject inject = field.getAnnotation(Inject.class);
				if (inject != null) {
					if (field.getType().equals(org.slf4j.Logger.class)) {
						mirror.setValue(obj, field, LoggerFactory.getLogger(c));
					} else {
						mirror.setValue(obj, field, ioc.get(field.getType(), StringUtil.isBlank(inject.value()) ? field.getName() : inject.value()));
					}
				}
			}

			for (Method method : mirror.getMethods()) {
				if (method.getAnnotation(Execute.class) == null && method.getAnnotation(DefaultExecute.class) == null) {
					continue;
				}
				String key = c.getSimpleName() + "/" + method.getName();
				LOG.info("add relation " + key);
				TestingFilter.methods.put(key, KeyValue.with(method, obj));
			}
		}

	}

	/**
	 * 释放关联类
	 * 
	 * @param clas
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static void unRelation(Class<?>... clas) {
		for (Class<?> c : clas) {
			Mirror<?> mirror = Mirror.me(c);
			for (Method method : mirror.getMethods()) {
				if (method.getAnnotation(Execute.class) == null && method.getAnnotation(DefaultExecute.class) == null) {
					continue;
				}
				TestingFilter.methods.remove(c.getSimpleName() + "/" + method.getName());
			}
		}
	}

	/**
	 * 对比本地代码和线上代码的不同
	 * 
	 * @param apiPath 本地代码路径
	 * @param apiPath 线上api地址
	 * @return
	 * @throws IOException
	 */
	public static void diffCode(String apiPath, String ipPort) throws IOException {

		List<Path> lists = new ArrayList<>();

		Files.walkFileTree(new File(apiPath).toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
				if (file.toString().endsWith(".java")) {
					lists.add(file);
				}
				return FileVisitResult.CONTINUE;
			}
		});

		lists.forEach(p -> {
			Map<String, Object> params = new HashMap<>();
			File f = p.toFile();
			String fileName = f.getName();
			params.put("name", fileName.substring(0, fileName.length() - 5));
			params.put("code", IOUtil.getContent(f, "utf-8"));
			JSONObject parse = JSONObject.parseObject(Http.post("http://" + ipPort + "/api_diff", params, 30000));

			if (parse.getBooleanValue("ok")) {
				System.out.println(f.getAbsolutePath() + "\t same");
			} else if (parse.getDate("obj") != null) {
				System.err.println(f.getAbsolutePath() + "\t notsame,  online time: " + DateUtils.formatDate(parse.getDate("obj"), "yyyyMMddHHmmss") + " localfile time:"
						+ DateUtils.formatDate(f.lastModified(), "yyyyMMddHHmmss"));
			} else {
				System.err.println(f.getAbsolutePath() + " not found online");
			}

		});
	}

	/**
	 * test local api by server
	 * 
	 * @param port default 8080
	 * @param iocPath your ioc path
	 * @param jcoderHome if you need resource , you need it
	 * @throws Exception
	 */
	public static void startServer(int port, String iocPath, String jcoderHome, String[] packages) throws Exception {
		if (port <= 0) {
			port = 8080;
		}
		if (StringUtil.isBlank(iocPath)) {
			iocPath = IOC_PATH;
		}

		if (StringUtil.isBlank(jcoderHome)) {
			jcoderHome = new File(System.getProperty("user.home"), ".jcoder").getAbsolutePath();
		}

		System.setProperty(StaticValue.PREFIX + "home", jcoderHome);
		System.setProperty(StaticValue.PREFIX + "port", String.valueOf(port));

		Server server = new Server(port);

		Ioc ioc = new NutIoc(new JsonLoader(iocPath));
		StaticValue.setSystemIoc(ioc);
		StaticValue.setUserIoc(ioc);

		TestingFilter.init(packages);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.addFilter(TestingFilter.class, "/api/*", EnumSet.of(DispatcherType.REQUEST));
		server.setHandler(context);

		server.start();

		System.out.println("jcoder testing server start ok on http://127.0.0.1:" + port);

		server.join();

	}

	/**
	 * test local api by server
	 * 
	 * @param port default 8080
	 * @param iocPath your ioc path
	 * @param jcoderHome if you need resource , you need it
	 * @throws Exception
	 */
	public static void startServer(String iocPath, String[] packages) throws Exception {
		startServer(8080, iocPath, null, packages);
	}

}
