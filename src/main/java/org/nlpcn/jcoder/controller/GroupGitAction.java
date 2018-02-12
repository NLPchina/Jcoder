package org.nlpcn.jcoder.controller;

import com.alibaba.fastjson.JSONObject;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.nlpcn.jcoder.domain.GroupGit;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.GitSerivce;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.Maps;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.http.Response;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.nlpcn.jcoder.util.StaticValue.space;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/groupGit")
@Ok("json")
public class GroupGitAction {

	@Inject
	private GitSerivce gitSerivce;

	@Inject
	private GroupService groupService;

	@Inject
	private ProxyService proxyService;

	/**
	 * 组列表
	 */
	@At
	public Restful list() {
		TreeCache groupCache = space().getGroupCache();
		Map<String, ChildData> currentChildren = groupCache.getCurrentChildren(SharedSpaceService.GROUP_PATH);
		List<GroupGit> list = currentChildren.entrySet().stream().map(e -> {
			GroupGit gg = JSONObject.parseObject(e.getValue().getData(), GroupGit.class);
			if (gg == null) {
				gg = new GroupGit();
				gg.setGroupName(e.getKey());
			}
			return gg;
		}).collect(Collectors.toList());
		return Restful.instance(list);
	}


	/**
	 * 保存
	 */
	@At
	public Restful save(@Param("..") GroupGit groupGit) throws Exception {

		if (StringUtil.isBlank(groupGit.getGroupName())) {
			return Restful.fail().msg("沒有組名稱");
		}
		//找到旧的group
		GroupGit old = space().getData(SharedSpaceService.GROUP_PATH + "/" + groupGit.getGroupName(), GroupGit.class);

		if (old == null || !Objects.equals(old.getUri(), groupGit.getUri())) { //url 发生改变则设置token
			groupGit.setToken(UUID.randomUUID().toString());
		}
		space().setData2ZK(SharedSpaceService.GROUP_PATH + "/" + groupGit.getGroupName(), JSONObject.toJSONBytes(groupGit));
		return Restful.ok().msg("保存成功");
	}


	/**
	 * 從git同步
	 */
	@At
	public Restful flush(String groupName) throws Exception {
		GroupGit groupGit = space().getData(SharedSpaceService.GROUP_PATH + "/" + groupName, GroupGit.class);

		if (groupGit == null) {
			return Restful.fail().msg("未定义group");
		}

		//找一台同步机器进行更新，找最小的机器
		List<String> currentHostPort = groupService.getCurrentHostPort(groupName);

		if (currentHostPort.size() == 0) {
			return Restful.fail().msg("无同步主机");
		}

		String hostPort = currentHostPort.stream().min(String::compareTo).get();

		Response response = proxyService.post(hostPort, "/admin/groupGit/__flush__", Maps.hash("groupName", groupName), 1200000);

		return Restful.instance(response);

	}

	@At
	public Restful __flush__(String groupName) throws Exception {

		if (StaticValue.TESTRING) {
			return Restful.fail().msg("testing模式不能使用git模式");
		}

		GroupGit groupGit = space().getData(SharedSpaceService.GROUP_PATH + "/" + groupName, GroupGit.class);
		if (groupGit == null) {
			return Restful.fail().msg("未定义group");
		}
		String message = gitSerivce.flush(groupGit);
		return Restful.ok().msg(message);
	}


}
