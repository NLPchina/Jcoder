package org.nlpcn.jcoder.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.domain.ClassDoc;
import org.nlpcn.jcoder.domain.ClassDoc.MethodDoc;
import org.nlpcn.jcoder.domain.CodeInfo.ExecuteMethod;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.run.java.ClassUtil;
import org.nlpcn.jcoder.run.java.DynamicEngine;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.run.java.JavaSourceUtil;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.ExceptionUtil;
import org.nlpcn.jcoder.util.JavaDocUtil;
import org.nlpcn.jcoder.util.JavaSource2RpcUtil;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.filter.CrossOriginFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;

@IocBean
public class ApiAction {

	private static final Logger LOG = LoggerFactory.getLogger(ApiAction.class);

	private static final Set<String> DEFAULT_METHODS = Sets.newHashSet("GET", "POST", "PUT", "DELETE");

	@Inject
	private TaskService taskService;

	/**
	 * api
	 * 
	 * @return api info
	 */
	@At("/apidoc/info")
	@Ok("json")
	@Filters(@By(type = CrossOriginFilter.class))
	public Object api(@Param(value = "type", df = "1") int type) {

		List<ClassDoc> result = TaskService.findTaskList(type).stream().map((t) -> {
			boolean compile = false;
			ClassDoc cd = null;
			try {
				new JavaRunner(t).compile();
				compile = true;
			} catch (Exception e1) {
				LOG.error(e1.getMessage(), e1);
			}
			try {
				cd = JavaDocUtil.parse(new StringReader(t.getCode()));
				cd.getSub().stream().forEach(method -> {
					ExecuteMethod executeMethod = t.codeInfo().getExecuteMethod(method.getName());
					MethodDoc md = (MethodDoc) method;
					if (executeMethod.isRestful()) {
						if (executeMethod.getMethodTypeSet().size() == 0) {
							md.getMethods().addAll(DEFAULT_METHODS);
						} else {
							md.getMethods().addAll(executeMethod.getMethodTypeSet());
						}
					}
					if (executeMethod.isRpc()) {
						md.addMethod("RPC");
					}

				});

			} catch (Exception e) {
				e.printStackTrace();
				cd = new ClassDoc(t.getName());
			}
			cd.setStatus(compile);
			cd.setVersion(t.getVersion());
			cd.setDescription(t.getDescription());
			return cd;
		}).collect(Collectors.toList());

		return result;

	}

	@At("/api_maven/repository/org/nlpcn/jcoder/jar/*")
	public void fileMavenRepository(HttpServletRequest req, HttpServletResponse rep) throws UnsupportedEncodingException, IOException {
		String path = req.getServletPath();
		LOG.info("visit maven response : " + path);

		String[] split = path.split("/");

		String libName = split[split.length - 1];

		try {

			if (StringUtil.isBlank(libName)) {
				throw new FileNotFoundException("path error " + req.getServletPath());
			}

			if (libName.endsWith(".pom")) {
				libName = libName.substring(0, libName.length() - 4);
			} else if (libName.endsWith(".pom.sha1")) {
				libName = libName.substring(0, libName.length() - 9);
			} else if (libName.endsWith(".jar")) {
				libName = libName.substring(0, libName.length() - 4);
			} else if (libName.endsWith(".jar.sha1")) {
				libName = libName.substring(0, libName.length() - 9);
			} else {
				throw new FileNotFoundException("the end of " + libName + " err");
			}

			libName += ".jar";

			File file = new File(StaticValue.LIB_FILE, libName);

			if (!file.exists()) {
				file = new File(StaticValue.LIB_FILE, "target/dependency/" + libName);
			}

			if (!file.exists()) {
				throw new FileNotFoundException("path error " + req.getServletPath());
			}

			String pom = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
					+ "		  <modelVersion>4.0.0</modelVersion>\n" + "		  <groupId>org.nlpcn.jcoder.jar</groupId>\n" + "		  <artifactId>jcoder.jar</artifactId>\n"
					+ "		  <version>1.0</version>\n" + "\n" + "\n" + "</project>";

			ServletOutputStream outputStream = rep.getOutputStream();

			if (libName.endsWith(".pom")) {
				outputStream.write(pom.getBytes("utf-8"));
			}

			if (libName.endsWith(".pom.sha1")) {
				outputStream.write(toSHA1(pom.getBytes("utf-8")));
			}

			if (libName.endsWith(".jar")) {
				try (InputStream in = IOUtil.getInputStream(file.getAbsolutePath())) {
					int len = 0;
					byte[] bytes = new byte[1024];
					while ((len = in.read(bytes)) >= 0) {
						outputStream.write(bytes, 0, len);
					}
				}
			}

			if (libName.endsWith(".jar.sha1")) {
				try (InputStream in = IOUtil.getInputStream(file.getAbsolutePath())) {
					try (ByteArrayOutputStream bas = new ByteArrayOutputStream(1024 * 1024 * 50)) {
						int len = 0;
						byte[] bytes = new byte[1024];
						while ((len = in.read(bytes)) >= 0) {
							bas.write(bytes, 0, len);
						}
						outputStream.write(toSHA1(bas.toByteArray()));
					}
				}
			}

			outputStream.flush();

		} catch (Exception e1) {
			e1.printStackTrace();
			try {
				rep.setStatus(404);
				rep.setHeader("Cache-Control", "no-cache");
				rep.setContentType("text/html");
				rep.getOutputStream().write(e1.getMessage().getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				rep.getOutputStream().flush();
				rep.getOutputStream().close();
			}
		}

	}

	@At("/api_maven/repository/org/nlpcn/jcoder/package/*")
	public void packageMaven(HttpServletRequest req, HttpServletResponse rep) throws UnsupportedEncodingException, IOException {
		String path = req.getServletPath();
		LOG.info("visit maven response : " + path);

		try {
			String[] split = path.split("/");

			if (split.length <= 8) {
				throw new ClassNotFoundException("path error " + path);
			}

			String packageStr = split[7];

			LOG.info("find class in packages " + packageStr);

			Map<String, byte[]> classes = ClassUtil.getClasses(packageStr);

			String pom = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
					+ "		  <modelVersion>4.0.0</modelVersion>\n" + "		  <groupId>org.nlpcn.jcoder.jar</groupId>\n" + "		  <artifactId>jcoder.jar</artifactId>\n"
					+ "		  <version>1.0</version>\n" + "\n" + "\n" + "</project>";

			pom = pom.replace("<artifactId>jcoder.jar</artifactId>", " <artifactId>" + packageStr + "</artifactId>");

			byte[] bytes = makeRpcClientJar(classes);

			ServletOutputStream outputStream = rep.getOutputStream();

			if (classes.size() == 0) {
				throw new ClassNotFoundException("not foun any class in " + packageStr);
			}

			if (path.endsWith(".pom")) {
				outputStream.write(pom.getBytes("utf-8"));
			}

			if (path.endsWith(".pom.sha1")) {
				outputStream.write(toSHA1(pom.getBytes("utf-8")));
			}

			if (path.endsWith(".jar")) {
				outputStream.write(bytes);
			}

			if (path.endsWith(".jar.sha1")) {
				outputStream.write(toSHA1(bytes));
			}

			outputStream.flush();

		} catch (Exception e1) {
			e1.printStackTrace();
			try {
				rep.setStatus(404);
				rep.setHeader("Cache-Control", "no-cache");
				rep.setContentType("text/html");
				rep.getOutputStream().write(String.valueOf(e1.getMessage()).getBytes());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				rep.getOutputStream().flush();
				rep.getOutputStream().close();
			}
		}

	}

	@At("/api_maven/repository/org/nlpcn/jcoder/jcoder-rpc-sdk/*")
	public void jocderRpcSdkmaven(HttpServletRequest req, HttpServletResponse rep) throws UnsupportedEncodingException, IOException {

		String path = req.getServletPath();

		LOG.info("visit maven response : " + path);

		rep.setContentType("application/octet-stream");

		String pom = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "                  <modelVersion>4.0.0</modelVersion>\n" + "                  <groupId>org.nlpcn.jcoder</groupId>\n"
				+ "                  <artifactId>jcoder-rpc-client</artifactId>\n" + "                  <version>1.0</version>\n" + "\n" + "\n" + "        <dependencies>\n" + "\n"
				+ "                <dependency>\n" + "                        <groupId>io.netty</groupId>\n" + "                        <artifactId>netty-all</artifactId>\n"
				+ "                        <version>4.1.2.Final</version>\n" + "                        <scope>compile</scope>\n" + "                </dependency>\n" + "\n"
				+ "                <dependency>\n" + "                        <groupId>io.protostuff</groupId>\n"
				+ "                        <artifactId>protostuff-core</artifactId>\n" + "                        <version>1.4.4</version>\n"
				+ "                        <scope>compile</scope>\n" + "                </dependency>\n" + "\n" + "                <dependency>\n"
				+ "                        <groupId>io.protostuff</groupId>\n" + "                        <artifactId>protostuff-runtime</artifactId>\n"
				+ "                        <version>1.4.4</version>\n" + "                        <scope>compile</scope>\n" + "                </dependency>\n" + "\n"
				+ "                <dependency>\n" + "                        <groupId>org.objenesis</groupId>\n" + "                        <artifactId>objenesis</artifactId>\n"
				+ "                        <version>2.4</version>\n" + "                        <scope>compile</scope>\n" + "                </dependency>\n" + "\n"
				+ "                <dependency>\n" + "                        <groupId>org.jasypt</groupId>\n" + "                        <artifactId>jasypt</artifactId>\n"
				+ "                        <version>1.9.2</version>\n" + "                        <scope>compile</scope>\n" + "                </dependency>\n" + "\n"
				+ "        </dependencies>\n" + "</project>";

		ServletOutputStream outputStream = rep.getOutputStream();

		if (path.endsWith(".pom")) {
			outputStream.write(pom.getBytes("utf-8"));
		}

		if (path.endsWith(".pom.sha1")) {
			outputStream.write(toSHA1(pom.getBytes("utf-8")));
		}

		if (path.endsWith(".jar")) {
			byte[] bytes = makeRpcClientJar();
			outputStream.write(bytes);
		}

		if (path.endsWith(".jar.sha1")) {
			byte[] bytes = makeRpcClientJar();
			outputStream.write(toSHA1(bytes));
		}

		outputStream.flush();

	}

	/**
	 * 编译class类到jar中
	 * 
	 * @return
	 * @throws IOException
	 */
	private byte[] makeRpcClientJar() throws IOException {

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

			try (JarOutputStream jos = new JarOutputStream(bos)) {

				Collection<Task> findTaskList = TaskService.findTaskList(1);

				for (Task task : findTaskList) {
					String code = null;
					try {
						code = JavaSource2RpcUtil.makeRpcSource(task);

						if (StringUtil.isBlank(code)) {
							LOG.warn(task.getName() + ": not have any rpc api , so skip it ");
							continue;
						}

						String pack = JavaSourceUtil.findPackage(code);

						String className = JavaSourceUtil.findClassName(code);

						LOG.info("to compile " + pack + "." + className);

						byte[] bytes = DynamicEngine.getInstance().javaCode2Bytes(pack + "." + className, code);

						String path = pack.replace(".", "/") + "/" + className + ".class";

						jos.putNextEntry(new JarEntry(path));
						jos.write(bytes);
						// package source code
						jos.putNextEntry(new JarEntry(pack.replace(".", "/") + "/" + className + ".java"));
						jos.write(code.getBytes("utf-8"));

					} catch (Exception e) {
						e.printStackTrace();
						LOG.error(code);
					}
				}

				Map<String, byte[]> classes = ClassUtil.getClasses("org.nlpcn.jcoder.server.rpc.client");

				for (Entry<String, byte[]> entry : classes.entrySet()) {
					jos.putNextEntry(new JarEntry(entry.getKey()));
					jos.write(entry.getValue());
				}

			}
			return bos.toByteArray();
		}
	}

	/**
	 * 编译class类到jar中
	 * 
	 * @return
	 * @throws IOException
	 */
	private byte[] makeRpcClientJar(Map<String, byte[]> classes) throws IOException {

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			try (JarOutputStream jos = new JarOutputStream(bos)) {
				for (Entry<String, byte[]> entry : classes.entrySet()) {
					jos.putNextEntry(new JarEntry(entry.getKey()));
					jos.write(entry.getValue());
				}
			}
			return bos.toByteArray();
		}
	}

	private byte[] toSHA1(byte[] bytes) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return md.digest(bytes);
	}

	/**
	 * 执行测试用户的api
	 * 
	 * @param jsonTask
	 * @return
	 */
	@At("/run_api")
	@Ok("raw")
	@Filters(@By(type = AuthoritiesManager.class, args = { "userType", "1", "/login.jsp" }))
	public Object runApi(@Param("json") String jsonTask) {

		String taskName = null;
		try {

			JSONObject json = JSONObject.parseObject(jsonTask);

			String code = json.getString("task.code");
			String codeType = json.getString("task.codeType");

			Task task = new Task();
			task.setCode(code);
			task.setCodeType(codeType);
			task.setType(2);

			taskName = task.getName() + "@" + "0@" + Mvcs.getReq().getRemoteAddr();

			if (ThreadManager.checkActionExists(taskName)) {
				LOG.warn(taskName + " has beening run! pleast stop it first!");
			} else {
				LOG.info(taskName + " publish ok ! will be run !");
				ThreadManager.add2ActionTask(taskName, Thread.currentThread());
				LOG.info(taskName + " result : " + new JavaRunner(task).compile().instance().execute());
			}

		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(taskName + " " + ExceptionUtil.printStackTraceWithOutLine(e));

			return StaticValue.errMessage(e.getMessage());
		} finally {
			if (taskName != null)
				ThreadManager.removeActionIfOver(taskName);
		}

		return StaticValue.okMessage("code run over!");
	}

	/**
	 * 停止一个api任务
	 * 
	 * @param jsonTask
	 * @return
	 */
	@At("/stop_api")
	@Ok("raw")
	@Filters(@By(type = AuthoritiesManager.class, args = { "userType", "1", "/login.jsp" }))
	public Object stopApi(@Param("json") String jsonTask) {

		String taskName = null;
		try {
			JSONObject json = JSONObject.parseObject(jsonTask);
			taskName = JavaSourceUtil.findClassName(json.getString("task.code")) + "@" + "0@" + Mvcs.getReq().getRemoteAddr();
			LOG.info(taskName + " will be to stop ! ");
			ThreadManager.stop(taskName);
			LOG.info(taskName + " stoped ! ");
			return StaticValue.OK;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(taskName + " err " + ExceptionUtil.printStackTraceWithOutLine(e));
			return StaticValue.ERR;
		}
	}

	/**
	 * 停止一个api任务
	 * 
	 * @param jsonTask
	 * @return
	 */
	@At("/api_diff")
	@Ok("json")
	public Object diff(String name, String code) {

		Task task = TaskService.findTaskByCache(name);

		if (task == null) {
			return Restful.instance(false, "notFound");
		}

		if (code.trim().equals(task.getCode().trim())) {
			return Restful.instance(true, "same");
		}

		Date updateTime = task.getUpdateTime();

		return Restful.instance(false, "unSame", updateTime);

	}

}
