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
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/groupGit")
@Ok("json")
public class GroupGitAction {

	/**
	 * 组列表
	 *
	 * @return
	 */
	@At
	public Restful list() {
		TreeCache groupCache = StaticValue.space().getGroupCache();
		Map<String, ChildData> currentChildren = groupCache.getCurrentChildren(SharedSpaceService.GROUP_PATH);
		List<GroupGit> list = currentChildren.entrySet().stream().map(e -> {
			GroupGit gg = JSONObject.parseObject(e.getValue().getData(), GroupGit.class);
			if (gg == null) {
				gg = new GroupGit();
				gg.setGroupName(e.getKey());
				gg.setLastPullTime(new Date());
			}
			return gg;
		}).collect(Collectors.toList());
		return Restful.instance(list);
	}


	/**
	 * 获取远程分支
	 *
	 * @param groupGit
	 * @return
	 */
	@At
	public Restful branch(GroupGit groupGit) throws IOException, GitAPIException {
		try (Repository repository = new FileRepositoryBuilder().readEnvironment().findGitDir().build()) {
			try (Git git = new Git(repository)) {
				List<Ref> call = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
				for (Ref ref : call) {
					System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
				}
			}
		}

		return null ;
	}


}
