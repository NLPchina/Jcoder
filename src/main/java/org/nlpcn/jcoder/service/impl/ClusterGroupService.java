package org.nlpcn.jcoder.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.HostGroup;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.nlpcn.jcoder.service.SharedSpaceService.*;

@IocBean(factory = "org.nlpcn.jcoder.service.ServiceFactory#createGroupService")
public class ClusterGroupService implements GroupService {

	private SharedSpaceService sharedSpaceService;

	public ClusterGroupService(SharedSpaceService sharedSpaceService) {
		this.sharedSpaceService = sharedSpaceService;
	}

	@Override
	public List<Group> list() throws Exception {
		List<Group> result = new ArrayList<>();

		getAllGroupNames().forEach(gName -> {
			Group group = new Group();
			group.setName(gName);
			try {
				List<String> children = sharedSpaceService.getZk().getChildren().forPath(GROUP_PATH + "/" + gName);
				group.setTaskNum(children.size() - 1);

				Set<String> set = new HashSet<>();
				sharedSpaceService.walkAllDataNode(set, GROUP_PATH + "/" + gName + "/file");
				group.setFileNum(set.size()-1);

				FileInfo root = JSONObject.parseObject(sharedSpaceService.getData2ZK(GROUP_PATH + "/" + gName + "/file"), FileInfo.class);

				group.setFileLength(root.getLength());

				List<String> hostGroupPath = sharedSpaceService.getZk().getChildren().forPath(HOST_GROUP_PATH);

				List<HostGroup> hosts = new ArrayList<>();
				for (String p : hostGroupPath) {
					String[] split = p.split("_");
					if (split.length == 1) {
						continue;
					}
					if (gName.equals(split[1])){
						HostGroup hg = JSONObject.parseObject(sharedSpaceService.getData2ZK(HOST_GROUP_PATH+"/"+p), HostGroup.class);
						hg.setHostPort(split[0]);
						hosts.add(hg);
					}

				}
				group.setHosts(hosts);
				result.add(group);

			} catch (Exception e) {
				e.printStackTrace();
			}
		});


		return result;
	}

	/**
	 * 得到一个group下所有主机的信息
	 * @param groupName
	 * @throws Exception
	 */
	public List<HostGroup> getGroupHostList(String groupName) throws Exception {

		List<String> hostGroupPath = sharedSpaceService.getZk().getChildren().forPath(HOST_GROUP_PATH);

		List<HostGroup> hosts = new ArrayList<>();
		for (String p : hostGroupPath) {
			String[] split = p.split("_");
			if (split.length == 1) {
				continue;
			}
			if (groupName.equals(split[1])){
				HostGroup hg = JSONObject.parseObject(sharedSpaceService.getData2ZK(HOST_GROUP_PATH+"/"+p), HostGroup.class);
				hg.setHostPort(split[0]);
				hosts.add(hg);
			}
		}

		return hosts ;
	}

	/**
	 * 获取所有的分组
	 */
	public List<String> getAllGroupNames() throws Exception {
		return sharedSpaceService.getZk().getChildren().forPath(GROUP_PATH);
	}

	@Override
	public Set<String> getAllHosts() throws Exception {
		List<String> hostGroupPath = sharedSpaceService.getZk().getChildren().forPath(HOST_GROUP_PATH);
		return hostGroupPath.stream().filter(s -> s.split("_").length == 1).collect(Collectors.toSet());
	}

	@Override
	public void save(Group group) {

	}

	@Override
	public void delete(Group group) {

	}
}
