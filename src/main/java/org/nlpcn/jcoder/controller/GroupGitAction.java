package org.nlpcn.jcoder.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.nlpcn.jcoder.domain.GroupGit;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.GitSerivce;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.nlpcn.jcoder.util.StaticValue.*;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/groupGit")
@Ok("json")
public class GroupGitAction {

	@Inject
	private GitSerivce gitSerivce;

	/**
	 * 组列表
	 *
	 * @return
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
	 *
	 * @param groupGit
	 * @return
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
	 *
	 * @param
	 * @return
	 */
	@At
	public Restful flush(String groupName) throws Exception {
		GroupGit groupGit = space().getData(SharedSpaceService.GROUP_PATH + "/" + groupName, GroupGit.class);
		if (groupGit == null) {
			return Restful.fail().msg("未定义group");
		}

		String message = gitSerivce.flush(groupGit);
		return Restful.ok().msg(message);
	}


}
