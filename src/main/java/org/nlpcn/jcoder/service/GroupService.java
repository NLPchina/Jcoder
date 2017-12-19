package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.nlpcn.jcoder.domain.FileInfo;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.domain.HostGroup;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.*;

import static org.nlpcn.jcoder.service.SharedSpaceService.GROUP_PATH;

/**
 * Created by Ansj on 05/12/2017.
 */
@IocBean
public class GroupService {
	private SharedSpaceService sharedSpaceService;

	public GroupService() {
		this.sharedSpaceService = StaticValue.space();
	}

	public List<Group> list() throws Exception {
		List<Group> result = new ArrayList<>();

		getAllGroupNames().forEach((String gName) -> {
			Group group = new Group();
			group.setName(gName);
			try {
				List<String> children = sharedSpaceService.getZk().getChildren().forPath(GROUP_PATH + "/" + gName);
				group.setTaskNum(children.size() - 1);

				Set<String> set = new HashSet<>();
				sharedSpaceService.walkAllDataNode(set, GROUP_PATH + "/" + gName + "/file");
				group.setFileNum(set.size());

				FileInfo root = JSONObject.parseObject(sharedSpaceService.getData2ZK(GROUP_PATH + "/" + gName + "/file"), FileInfo.class);

				group.setFileLength(root.getLength());

				Set<Map.Entry<String, HostGroup>> entries = sharedSpaceService.getHostGroupCache().entrySet();

				List<HostGroup> hosts = new ArrayList<>();

				for (Map.Entry<String, HostGroup> entry : entries) {
					String[] split = entry.getKey().split("_");
					if (split.length == 1) {
						continue;
					}
					if (gName.equals(split[1])) {
						HostGroup hg = entry.getValue();
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

		result.sort(Comparator.comparingInt(g -> -g.getHosts().size()));

		return result;
	}

	/**
	 * 得到一个group下所有主机的信息
	 *
	 * @param groupName
	 * @throws Exception
	 */
	public List<HostGroup> getGroupHostList(String groupName) throws Exception {

		Set<Map.Entry<String, HostGroup>> entries = sharedSpaceService.getHostGroupCache().entrySet();

		List<HostGroup> hosts = new ArrayList<>();
		for (Map.Entry<String, HostGroup> entry : entries) {
			String[] split = entry.getKey().split("_");
			if (split.length == 1) {
				continue;
			}
			if (groupName.equals(split[1])) {
				HostGroup hg = entry.getValue();
				hg.setHostPort(split[0]);
				hosts.add(hg);
			}
		}

		return hosts;
	}

	/**
	 * 获取所有的分组
	 */
	public List<String> getAllGroupNames() throws Exception {
		return sharedSpaceService.getZk().getChildren().forPath(GROUP_PATH);
	}

	public List<String> getAllHosts() throws Exception {
		return sharedSpaceService.getAllHosts();
	}


	/**
	 * 从集群把一个组彻底删除掉
	 *
	 * @param groupName
	 */
	public void deleteByCluster(String groupName) throws Exception {

		InterProcessMutex interProcessMutex = sharedSpaceService.lockGroup(groupName);

		try {
			interProcessMutex.acquire();
			//判断当前是否有机器在使用此group
			for (String k : sharedSpaceService.getHostGroupCache().keySet()) {
				String gName = k.split("_")[1];
				if (gName.equals(groupName)) {
					throw new Exception("组: " + groupName + "存在主机持有。清删除后重试: " + k);
				}
			}
			sharedSpaceService.getZk().delete().deletingChildrenIfNeeded().forPath(SharedSpaceService.GROUP_PATH + "/" + groupName);
		} finally {
			sharedSpaceService.unLockAndDelete(interProcessMutex);
		}
	}
}
