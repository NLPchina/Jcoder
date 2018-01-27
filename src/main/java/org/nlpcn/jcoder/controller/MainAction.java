package org.nlpcn.jcoder.controller;

import com.google.common.collect.ImmutableMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.nlpcn.jcoder.constant.Api;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.Maps;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by Ansj on 14/12/2017.
 */

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@Ok("json")
public class MainAction {

	private static final Logger LOG = LoggerFactory.getLogger(TaskAction.class);

	@Inject
	private TaskService taskService;

	@Inject
	private GroupService groupService;

	@Inject
	private ProxyService proxyService;

	@At("/admin/main/left")
	public Restful left() throws Exception {

		JSONArray result = new JSONArray();

		User user = (User) Mvcs.getHttpSession().getAttribute("user");

		boolean isAdmin = user.getType() == 1;

		StaticValue.space().getHostGroupCache().toMap();

		List<String> strings = StaticValue.space().getZk().getChildren().forPath("/jcoder/group");

		Set<String> allGroups = new TreeSet<>(strings);

		//task 管理
		JSONArray submenus = new JSONArray();
		for (String groupName : allGroups) {
			submenus.add(ImmutableMap.of("name", groupName.toString(), "url", "task/list.html?name=" + groupName));
		}
		result.add(ImmutableMap.of("name", "Task管理", "submenus", submenus));


		//Resource管理
		submenus = new JSONArray();
		for (String groupName : allGroups) {
			submenus.add(ImmutableMap.of("name", groupName.toString(), "url", "resource/list.html?name=" + groupName));
		}
		result.add(ImmutableMap.of("name", "Resource管理", "submenus", submenus));

		//Thread管理
		submenus = new JSONArray();
		for (String groupName : allGroups) {
			submenus.add(ImmutableMap.of("name", groupName.toString(), "url", "thread/index.html?name=" + groupName));
		}
		result.add(ImmutableMap.of("name", "Thread管理", "submenus", submenus));


		//系统管理
		submenus = new JSONArray();
		submenus.add(ImmutableMap.of("name", "用户管理", "url", "user/list.html"));
		submenus.add(ImmutableMap.of("name", "Group管理", "url", "group/list.html"));
		submenus.add(ImmutableMap.of("name", "持续集成", "url", "group/group_git.html"));
		result.add(ImmutableMap.of("name", "系统管理", "submenus", submenus));


		//系统健康
		submenus = new JSONArray();

		Set<String> tempGroups = new HashSet<>(allGroups);


		//冲突的group
		List<String> groups = new ArrayList<>();
		StaticValue.space().getHostGroupCache().entrySet().forEach(e -> {
			int index = e.getKey().indexOf("_");
			String groupName = e.getKey().substring(index + 1);
			if (!e.getValue().isCurrent()) {
				groups.add(groupName);

			}
			tempGroups.remove(groupName);
		});
		for (String group : groups) {
			submenus.add(ImmutableMap.of("name", "冲突：" + group, "url", "group/group_host_list.html?name=" + group));
		}

		//同步主机
		for (String group : tempGroups) {
			submenus.add(ImmutableMap.of("name", "无同步：" + group, "url", "group/list.html#" + group));
		}

		final List<Object> errTask = new ArrayList<>();

		//编译失败的类
		//查询所有的group
		groupService.getAllGroupNames().forEach((String gn) -> {
			try {
				// 查询所有的组
				Set<String> hosts = groupService.getGroupHostList(gn).stream().map(gh -> gh.getHostPort()).collect(Collectors.toSet());
				// 查询所有组的机器
				Map<String, Restful> post = proxyService.post(hosts, Api.TASK_LIST.getPath(), Maps.hash("groupName", gn, "taskType", -1), 5000);
				post.entrySet().forEach(e -> {
					e.getValue().obj2JsonArray().stream().forEach(o -> {
						JSONObject job = (JSONObject) o;
						if (!job.getBooleanValue("compile") && job.getIntValue("status") == 1) {
							errTask.add(ImmutableMap.of("name", "编译：" + gn + "/" + job.getString("name"), "url", "task/list.html?name=" + gn + "&hostPort=" + e.getKey()));
						}
					});

				});

			} catch (Exception e) {
				e.printStackTrace();
			}

		});
		submenus.addAll(errTask);


		if (submenus.size() > 0) {
			result.add(ImmutableMap.of("name", "系统健康", "submenus", submenus));
		}


		return Restful.instance().obj(result);

	}
}