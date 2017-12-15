package org.nlpcn.jcoder.service.impl;

import org.apache.curator.framework.CuratorFramework;
import org.nlpcn.jcoder.domain.Group;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.ioc.loader.annotation.IocBean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.nlpcn.jcoder.service.SharedSpaceService.*;

@IocBean(factory="org.nlpcn.jcoder.service.ServiceFactory#createGroupService")
public class ClusterGroupService implements GroupService {

	private SharedSpaceService sharedSpaceService;

	public ClusterGroupService(SharedSpaceService sharedSpaceService){
		this.sharedSpaceService = sharedSpaceService ;
	}

	@Override
	public List<Group> list() throws Exception {
		List<Group> result = new ArrayList<>() ;

		getAllGroupNames().forEach(gName -> {
			Group group = new Group();
			group.setName(gName);
			try {
				List<String> children = sharedSpaceService.getZk().getChildren().forPath(GROUP_PATH + "/" + gName);
				group.setTaskNum(children.size() - 1);

				Set<String> set = new HashSet<>() ;
				sharedSpaceService.walkAllDataNode(set,GROUP_PATH + "/" + gName + "/file") ;
				group.setFileNum(set.size());

				set = new HashSet<>() ;
				sharedSpaceService.walkAllDataNode(set,MAPPING_PATH+"/"+gName) ;

				Set<String> hosts = new HashSet<>() ;
				for (String p : set) {
					String[] split = p.split("/");
					hosts.add(split[split.length-1]) ;
				}
				group.setHosts(hosts.toArray(new String[hosts.size()]));

				result.add(group) ;

			} catch (Exception e) {
				e.printStackTrace();
			}
		});


		return result;
	}

	/**
	 * 获取所有的分组
	 */
	public List<String> getAllGroupNames() throws Exception {
		return sharedSpaceService.getZk().getChildren().forPath(GROUP_PATH);
	}

	@Override
	public void save(Group group) {

	}

	@Override
	public void delete(Group group) {

	}
}
