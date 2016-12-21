package org.nlpcn.jcoder.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import org.nlpcn.commons.lang.util.FileFinder;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.ObjConver;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.server.rpc.client.RpcClient;
import org.nlpcn.jcoder.server.rpc.client.RpcRequest;
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

	public static final String CODE_RUN = "123__CODE__RUN";

	/**
	 * instan task by ioc
	 * 
	 * @param c
	 * @return class c instance
	 * @throws Exception
	 */
	public static <T> T instance(Class<T> c, String iocPath) throws Exception {
		Ioc ioc = new NutIoc(new JsonLoader(iocPath));

		Mirror<?> mirror = Mirror.me(c);
		T obj = c.newInstance();

		for (Field field : mirror.getFields()) {
			Inject inject = field.getAnnotation(Inject.class);
			if (inject != null) {
				if (field.getType().equals(org.apache.log4j.Logger.class)) {
					LOG.warn("org.apache.log4j.Logger Deprecated please use org.slf4j.Logger by LoggerFactory");
					mirror.setValue(obj, field, org.apache.log4j.Logger.getLogger(c));
				} else if (field.getType().equals(org.slf4j.Logger.class)) {
					mirror.setValue(obj, field, LoggerFactory.getLogger(c));
				} else {
					mirror.setValue(obj, field, ioc.get(field.getType(), StringUtil.isBlank(inject.value()) ? field.getName() : inject.value()));
				}
			}
		}

		return obj;
	}

	public static <T> T instance(Class<T> c) throws Exception {

		File find = new File("src/test/resources/ioc.js");

		if (!find.exists()) {
			LOG.warn("ioc config not find in {} , will find it ", find.getAbsolutePath());
			find = FileFinder.find("ioc.js", 1);
			if (find != null)
				LOG.info("ioc config find in " + find.getAbsolutePath());
		}

		if (find != null && find.exists()) {
			return instance(c, find.getAbsolutePath());
		} else {
			throw new FileNotFoundException("ioc.js not found in your classpath ");
		}

	}

	/**
	 * 提交本地代码到远程运行,
	 * 
	 * @param timeout 超时时间,毫秒
	 * @param c 本地类文件
	 * @param methodName 运行的方法名称
	 * @param params 方法传入的参数,
	 * @return object 返回结果
	 * @throws Throwable
	 */
	public static Object remote(int timeout, Class<?> c, String methodName, Object... params) throws Throwable {
		// 根据class 获得代码

		File codeFile = FileFinder.find(c.getName().replace(".", System.getProperty("file.separator")) + ".java");

		String code = IOUtil.getContent(codeFile, "utf-8");

		ResourceBundle rb = ResourceBundle.getBundle("remote_code_config");

		String host = rb.getString("host");

		int port = ObjConver.getIntValue(rb.getString("port"));

		HashMap<String, String> map = new HashMap<>();
		map.put("code", code);
		map.put("name", rb.getString("name"));
		map.put("password", StaticValue.passwordEncoding(rb.getString("password")));

		try {
			RpcClient.connect(host, port);
			RpcRequest req = new RpcRequest();
			req.setClassName(CODE_RUN);
			req.setMessageId(UUID.randomUUID().toString());
			req.setMethodName(methodName);
			params = Arrays.copyOf(params, params.length + 1);
			params[params.length - 1] = map;
			req.setArguments(params);
			return RpcClient.getInstance().proxy(req);
		} finally {
			RpcClient.shutdown();
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

}
