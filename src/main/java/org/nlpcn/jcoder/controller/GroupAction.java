package org.nlpcn.jcoder.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.util.IOUtil;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.http.Response;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.function.Function;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/group")
@Ok("json")
public class GroupAction {

	private static final Logger LOG = LoggerFactory.getLogger(GroupAction.class);

	@Inject
	private GroupService groupService;

	@Inject
	private ProxyService proxyService;

	private BasicDao basicDao = StaticValue.systemDao;


	@At
	public Restful list() throws Exception {
		return Restful.instance(groupService.list());
	}

	@At
	public Restful hostList() throws Exception {
		return Restful.instance(groupService.getAllHosts());
	}

	@At
	public Restful groupHostList(@Param("name") String name) throws Exception {
		return Restful.instance(groupService.getGroupHostList(name));
	}


	@At
	public Restful delete(@Param("name") String name) throws Exception {
		return null;
	}


	@At
	public Restful diff(@Param("name") String name) {
		Condition con = Cnd.where("name", "=", name);
		int count = basicDao.searchCount(Group.class, con);
		if (count > 0) {
			return Restful.instance().ok(false).msg("组" + name + "已存在");
		}
		return Restful.instance().ok(true).msg("组" + name + "不存在");
	}

	@At
	public Restful add(@Param("hostPorts") String[] hostPorts, @Param("name") String name, @Param(value = "first", df = "true") boolean first) throws Exception {

		if (!first) {
			File file = new File(StaticValue.GROUP_FILE, name);
			file.mkdirs();
			File ioc = new File(StaticValue.GROUP_FILE, name + "/resoureces");
			ioc.mkdir();
			File lib = new File(StaticValue.GROUP_FILE, name + "/lib");
			lib.mkdir();

			IOUtil.Writer(new File(ioc, "ioc.js").getAbsolutePath(), "utf-8", "var ioc = {\n\t\n};");

			IOUtil.Writer(new File(lib, "pom.xml").getAbsolutePath(), "utf-8",
					"<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
							+ "	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n"
							+ "	<modelVersion>4.0.0</modelVersion>\n" + "	<groupId>org.nlpcn</groupId>\n" + "	<artifactId>jcoder</artifactId>\n" + "	<version>0.1</version>\n"
							+ "	\n" + "	<dependencies>\n" + "	</dependencies>\n" + "\n" + "	<build>\n" + "		<sourceDirectory>src/main/java</sourceDirectory>\n"
							+ "		<testSourceDirectory>src/test/java</testSourceDirectory>\n" + "		\n" + "		<plugins>\n" + "			<plugin>\n"
							+ "				<artifactId>maven-compiler-plugin</artifactId>\n" + "				<version>3.3</version>\n" + "				<configuration>\n"
							+ "					<source>1.8</source>\n" + "					<target>1.8</target>\n" + "					<encoding>UTF-8</encoding>\n"
							+ "					<compilerArguments>\n" + "						<extdirs>lib</extdirs>\n" + "					</compilerArguments>\n"
							+ "				</configuration>\n" + "			</plugin>\n" + "		</plugins>\n" + "	</build>\n" + "</project>\n" + "");

			Group group = new Group();
			group.setName(name);

			basicDao.save(group);

			StaticValue.space().joinCluster();

			return Restful.OK.msg("添加成功");
		} else {

			Set<String> hostPortsArr = new HashSet<>();

			Arrays.stream(hostPorts).forEach(s -> hostPortsArr.add((String) s));

			String message = proxyService.post(hostPortsArr, "/admin/group/diff", ImmutableMap.of("name", name, "first", false), 100000, ProxyService.MERGE_FALSE_MESSAGE_CALLBACK);

			if (StringUtil.isNotBlank(message)) {
				return Restful.instance().ok(false).code(500).msg(message);
			}

			message = proxyService.post(hostPortsArr, "/admin/group/add", ImmutableMap.of("name", name, "first", false), 100000, ProxyService.MERGE_MESSAGE_CALLBACK);

			return Restful.instance().msg(message);
		}
	}


	@At
	public Restful delete(@Param("..") Group group) {
		groupService.delete(group);
		return Restful.OK.msg("删除成功！");
	}


}
