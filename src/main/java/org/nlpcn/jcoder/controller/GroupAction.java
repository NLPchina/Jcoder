package org.nlpcn.jcoder.controller;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.nlpcn.jcoder.constant.Api;
import org.nlpcn.jcoder.constant.Constants;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.HostGroup;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.ApiException;
import org.nlpcn.jcoder.util.DateUtils;
import org.nlpcn.jcoder.util.GroupFileListener;
import org.nlpcn.jcoder.util.IOUtil;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.util.ZKMap;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.http.Response;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

	@Inject
	private TaskService taskService;

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
	public Restful changeWeight(@Param("groupName") String groupName, @Param("hostPort") String hostPort, @Param("weight") Integer weight) {
		if (weight == null) {
			return Restful.instance().ok(false).msg("权重必须为正整数");
		}

		ZKMap<HostGroup> hostGroupCache = StaticValue.space().getHostGroupCache();

		HostGroup hostGroup = hostGroupCache.get(hostPort + "_" + groupName);

		if (hostGroup == null) {
			return Restful.instance().ok(false).msg("没有找到此对象");
		}

		hostGroup.setWeight(weight);

		hostGroupCache.put(hostPort + "_" + groupName, hostGroup);

		return Restful.instance().ok(true).msg(hostPort + " 更改权重为：" + weight);

	}

	@At
	public Restful check(@Param("name") String name) {
		Condition con = Cnd.where("name", "=", name);
		int count = basicDao.searchCount(Group.class, con);
		if (count > 0) {
			return Restful.instance().ok(false).msg("组" + name + "已存在");
		}
		return Restful.instance().ok(true).msg("组" + name + "不存在");
	}

	@At
	public Restful add(@Param("hostPorts") String[] hostPorts, @Param("name") String name, @Param(value = "first", df = "true") boolean first) throws Exception {

		if (!name.matches("[a-z0-9A-Z_\\.]+")) {
			return Restful.fail().msg("Group名称只能是英文字母或数字和_");
		}

		if (!first) {

			Group groupByName = groupService.findGroupByName(name);

			if (groupByName != null) {
				return Restful.fail().msg(name + " 已存在");
			}

			File file = new File(StaticValue.GROUP_FILE, name);
			file.mkdirs();
			File ioc = new File(StaticValue.GROUP_FILE, name + "/resources");
			ioc.mkdir();
			File lib = new File(StaticValue.GROUP_FILE, name + "/lib");
			lib.mkdir();

			if (!ioc.exists()) {
				IOUtil.Writer(new File(ioc, "ioc.js").getAbsolutePath(), "utf-8", "var ioc = {\n\t\n};");
			}

			if (!new File(file, "pom.xml").exists()) {
				IOUtil.Writer(new File(file, "pom.xml").getAbsolutePath(), "utf-8",
						"<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
								"\txsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
								"\t<modelVersion>4.0.0</modelVersion>\n" +
								"\t<groupId>org.nlpcn.jcoder</groupId>\n" +
								"\t<artifactId>" + name + "</artifactId>\n" +
								"\t<version>0.1</version>\n" +
								"\n" +
								"\t<dependencies>\n" +
								"\t\t<dependency>\n" +
								"\t\t\t<groupId>org.nlpcn</groupId>\n" +
								"\t\t\t<artifactId>jcoder</artifactId>\n" +
								"\t\t\t<version>0.1</version>\n" +
								"\t\t\t<systemPath>" + StaticValue.getJcoderJarFile().getCanonicalPath() + "</systemPath>\n" +
								"\t\t\t<scope>system</scope>\n" +
								"\t\t</dependency>" +
								"\n" +
								"\t</dependencies>\n" +
								"\t<build>\n" +
								"\t\t<sourceDirectory>src/main</sourceDirectory>\n" +
								"\t\t<testSourceDirectory>src/api</testSourceDirectory>\n" +
								"\t\t<plugins>\n" +
								"\t\t\t<plugin>\n" +
								"\t\t\t\t<artifactId>maven-compiler-plugin</artifactId>\n" +
								"\t\t\t\t<version>3.3</version>\n" +
								"\t\t\t\t<configuration>\n" +
								"\t\t\t\t\t<source>1.8</source>\n" +
								"\t\t\t\t\t<target>1.8</target>\n" +
								"\t\t\t\t\t<encoding>UTF-8</encoding>\n" +
								"\t\t\t\t\t<compilerArguments>\n" +
								"\t\t\t\t\t\t<extdirs>lib</extdirs>\n" +
								"\t\t\t\t\t</compilerArguments>\n" +
								"\t\t\t\t</configuration>\n" +
								"\t\t\t</plugin>\n" +
								"\t\t</plugins>\n" +
								"\t</build>\n" +
								"</project>\n");
			}

			Group group = new Group();
			group.setName(name);

			basicDao.save(group);

			StaticValue.space().joinCluster(group, true);

			if (StaticValue.TESTRING) { //测试模式进行文件监听
				GroupFileListener.regediter(name);
			}

			return Restful.instance(true, "添加成功");
		} else {

			Set<String> hostPortsArr = new HashSet<>();

			Arrays.stream(hostPorts).forEach(s -> hostPortsArr.add((String) s));

			Restful restful = proxyService.post(hostPortsArr, "/admin/group/check", ImmutableMap.of("name", name, "first", false), 100000, ProxyService.MERGE_FALSE_MESSAGE_CALLBACK);

			if (!restful.isOk()) {
				return restful.code(500);
			}

			return proxyService.post(hostPortsArr, "/admin/group/add", ImmutableMap.of("name", name, "first", false), 100000, ProxyService.MERGE_MESSAGE_CALLBACK);
		}
	}

	/**
	 * 从集群中彻底删除一个group 要求group name必须没有任何一个机器使用中
	 */
	@At
	public Restful deleteByCluster(@Param("name") String name) {
		try {
			groupService.deleteByCluster(name);
			return Restful.instance().ok(true).msg("删除 group: " + name + " 成功");
		} catch (Exception e) {
			e.printStackTrace();
			return Restful.instance().ok(false).msg("删除 group: " + name + " 失败");
		}
	}


	/**
	 * 删除group
	 */
	@At
	public Restful delete(@Param("hostPorts") String[] hostPorts, @Param("name") String name, @Param(value = "first", df = "true") boolean first) throws Exception {

		if (!first) {
			boolean flag = groupService.deleteGroup(name);
			if (StaticValue.TESTRING) { //测试模式进行文件监听
				GroupFileListener.unRegediter(name);
			}
			if (flag) {
				return Restful.instance(flag, "删除成功");
			} else {
				return Restful.instance(flag, "删除文件失败");
			}
		} else {
			Set<String> hostPortsArr = new HashSet<>();
			Arrays.stream(hostPorts).forEach(s -> hostPortsArr.add((String) s));
			return proxyService.post(hostPortsArr, "/admin/group/delete", ImmutableMap.of("name", name, "first", false), 100000, ProxyService.MERGE_MESSAGE_CALLBACK);
		}
	}


	@At
	public Restful share(@Param("hostPorts") String[] hostPorts, @Param("formHostPort") String fromHostPort, @Param("groupName") String groupName, @Param("toGroupName") String toGroupName) throws Exception {
		if (StringUtil.isBlank(toGroupName)) {
			toGroupName = groupName;
		}

		if (hostPorts == null || hostPorts.length == 0 || StringUtil.isBlank(hostPorts[0])) {
			return Restful.fail().msg("必须选择一个目标主机");
		}

		return proxyService.post(hostPorts, "/admin/group/installGroup", ImmutableMap.of("fromHostPort", fromHostPort, "groupName", groupName, "toGroupName", toGroupName.trim()), 1200000, ProxyService.MERGE_MESSAGE_CALLBACK);
	}


	/**
	 * 克隆一个主机的group到当前主机上
	 */
	@At
	public synchronized Restful installGroup(@Param("fromHostPort") String fromHostPort, @Param("groupName") String groupName, @Param("toGroupName") String toGroupName) throws Exception {
		//判断当前group是否存在
		Group group = basicDao.findByCondition(Group.class, Cnd.where("name", "=", toGroupName));
		if (group != null) {
			return Restful.instance(false, toGroupName + " 已存在！");
		}

		//获取远程主机的所有files
		Response response = proxyService.post(fromHostPort, "/admin/fileInfo/listFiles", ImmutableMap.of("groupName", groupName), 120000);

		JSONArray jarry = JSONObject.parseObject(response.getContent()).getJSONArray("obj");

		File groupFile = new File(StaticValue.GROUP_FILE, toGroupName);

		for (Object o : jarry) {
			FileInfo fileInfo = JSONObject.toJavaObject((JSON) o, FileInfo.class);

			File file = new File(groupFile, fileInfo.getRelativePath());

			if (fileInfo.isDirectory()) {
				file.mkdirs();
			} else {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				long start = System.currentTimeMillis();
				LOG.info("to down " + fileInfo.getRelativePath());
				Response post = proxyService.post(fromHostPort, "/admin/fileInfo/downFile", ImmutableMap.of("groupName", groupName, "relativePath", fileInfo.getRelativePath()), 1200000);
				IOUtil.writeAndClose(post.getStream(), file);
				LOG.info("down ok : {} use time : {} ", fileInfo.getRelativePath(), System.currentTimeMillis() - start);
			}
		}

		//从远程主机获取所有的task
		response = proxyService.post(fromHostPort, "/admin/task/taskGroupList", ImmutableMap.of("groupName", groupName), 120000);

		System.out.println(response.getContent());

		//获取远程主机的所有tasks,本地创建group
		group = new Group();
		group.setName(toGroupName);
		group.setDescription("create at " + DateUtils.formatDate(new Date(), DateUtils.SDF_FORMAT) + " from " + fromHostPort);
		group.setCreateTime(new Date());
		basicDao.save(group);

		jarry = JSONObject.parseObject(response.getContent()).getJSONArray("obj");
		for (Object o : jarry) {
			Task task = JSONObject.toJavaObject((JSON) o, Task.class);
			task.setGroupName(group.getName());
			basicDao.save(task);
			taskService.flush(task.getId());
			LOG.info("install task {}", task.getName());
		}

		//刷新本地group,加入到集群中
		groupService.flush(group, true);

		if (StaticValue.TESTRING) { //测试模式进行文件监听
			GroupFileListener.regediter(toGroupName);
		}

		return Restful.instance().msg("克隆成功");
	}

	/**
	 * 刷新一个group到集群中
	 *
	 * @param hostPort 需要刷新的主机
	 * @return 不同
	 */
	@At
	public Restful flush(@Param("hostPort") String hostPort, @Param("groupName") String groupName, @Param("upMapping") boolean upMapping) throws Exception {
		if (StringUtil.isBlank(hostPort) || StaticValue.getHostPort().equals(hostPort)) {
			return Restful.instance(groupService.flush(groupName, upMapping));
		} else {
			Response post = proxyService.post(hostPort, "/admin/group/flush", ImmutableMap.of("hostPort", hostPort, "groupName", groupName, "upMapping", upMapping), 120000);
			return Restful.instance(post);
		}

	}

	/**
	 * 修复不同
	 */
	@At
	public Restful fixDiff(String fromHostPort, String toHostPort, String groupName, @Param("relativePath[]") String[] relativePaths) throws Exception {

		if (StringUtil.isBlank(fromHostPort)) {
			fromHostPort = Constants.HOST_MASTER;
		}

		if (StringUtil.isBlank(toHostPort)) {
			toHostPort = Constants.HOST_MASTER;
		}

		boolean toMaster = Constants.HOST_MASTER.equals(toHostPort);

		Set<String> toHostPorts = new HashSet<>();

		if (Constants.HOST_MASTER.equals(fromHostPort)) { //说明是主机
			fromHostPort = groupService.getRandomCurrentHostPort(groupName);
			if (fromHostPort == null) {
				return Restful.fail().msg("主版本中不存在任何实例，所以无法同步");
			}
		}

		if (toMaster) {
			List<String> currentHostPort = groupService.getCurrentHostPort(groupName);
			toHostPorts.addAll(currentHostPort);
		} else {
			toHostPorts.add(toHostPort);
		}

		List<String> message = new ArrayList<>();

		boolean flag = true;

		for (String relativePath : relativePaths) {
			if (relativePath.startsWith("/")) {//更新文件的
				if (toHostPorts.size() > 0) {
					Restful restful = proxyService.post(toHostPorts, "/admin/fileInfo/copyFile", ImmutableMap.of("fromHostPort", fromHostPort, "groupName", groupName, "relativePaths", relativePath), 120000, ProxyService.MERGE_FALSE_MESSAGE_CALLBACK);
					flag = flag && restful.isOk();
					if (!restful.isOk()) {
						message.add(restful.getMessage());
					}
				}

				if (toMaster) {
					Response post1 = proxyService.post(fromHostPort, "/admin/fileInfo/upCluster", ImmutableMap.of("groupName", groupName, "relativePaths", relativePath), 120000);
					message.add(post1.getContent());
					flag = flag && Restful.instance(post1).isOk();
				}

			} else {//更新task的

				if (toHostPorts.size() > 0) {
					Restful restful = proxyService.post(toHostPorts, Api.TASK_SYN.getPath(), ImmutableMap.of("fromHost", fromHostPort, "groupName", groupName, "taskName", relativePath), 10000, ProxyService.MERGE_FALSE_MESSAGE_CALLBACK);
					if (!restful.isOk()) {
						flag = false;
						message.add(restful.getMessage());
					}
				}

				if (toMaster) {
					Response post = proxyService.post(StaticValue.getHostPort(), "/admin/task/task", ImmutableMap.of("groupName", groupName, "name", relativePath, "sourceHost", fromHostPort), 100000);
					Restful restful = Restful.instance(post);

					if (restful.code() == ApiException.NotFound) {
						taskService.deleteTaskFromCluster(groupName, relativePath);
					} else if (restful.code() == ApiException.OK && restful.getObj() != null) {
						StaticValue.space().addTask(JSONObject.toJavaObject(restful.getObj(), Task.class));
					} else {
						message.add(restful.getMessage());
						flag = false;
					}

				}


			}
		}

		return Restful.instance(flag, Joiner.on(",").skipNulls().join(message));
	}

}
