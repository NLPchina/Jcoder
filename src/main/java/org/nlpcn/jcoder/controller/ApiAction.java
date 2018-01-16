package org.nlpcn.jcoder.controller;

import java.io.*;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.nlpcn.jcoder.domain.ClassDoc;
import org.nlpcn.jcoder.domain.ClassDoc.MethodDoc;
import org.nlpcn.jcoder.domain.CodeInfo.ExecuteMethod;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.run.java.JavaSourceUtil;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.*;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

		List<ClassDoc> result = TaskService.findTaskList(type).stream().sorted((t1, t2) -> {
			/*int v = (int) (t1.getGroupId() - t2.getGroupId());
			if (v != 0) {
				return v;
			}*/
			return (int) (t1.getId() - t2.getId());
		}).map((t) -> {
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

	@At("/jar/org/nlpcn/jcoder/?/?/*")
	@Ok("void")
	public void maven(HttpServletRequest req, HttpServletResponse rep) throws ApiException, IOException {

		String path = req.getServletPath();

		if (!StaticValue.getJcoderJarFile().exists()) {
			throw new ApiException(404, path + " not found ");
		}

		String[] split = path.split("/");
		String libName = split[split.length - 1];
		if (StringUtil.isBlank(libName)) {
			throw new ApiException(404, "path error " + req.getServletPath());
		}

		String pom = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
				+ "		  <modelVersion>4.0.0</modelVersion>\n" + "		  <groupId>org.nlpcn.jcoder.jar</groupId>\n" + "		  <artifactId>jcoder.jar</artifactId>\n"
				+ "		  <version>1.0</version>\n" + "\n" + "\n" + "</project>";

		if (libName.endsWith(".pom")) {
			rep.getOutputStream().write(pom.getBytes("utf-8"));
		} else if (libName.endsWith(".pom.sha1")) {
			rep.getOutputStream().write(MD5Util.sha1(pom).getBytes());
		} else if (libName.endsWith(".jar")) {
			Mvcs.getResp().setContentType("application/octet-stream");
			try (FileInputStream is = new FileInputStream(StaticValue.getJcoderJarFile());
			     OutputStream os = rep.getOutputStream()) {
				IOUtil.writeAndClose(is, os);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ApiException(404, e.getMessage());
			}
		} else if (libName.endsWith(".jar.sha1")) {
			rep.getOutputStream().write(MD5Util.sha1(StaticValue.getJcoderJarFile()).getBytes());
		} else {
			throw new ApiException(404, "the end of " + libName + " err");
		}
	}



}
