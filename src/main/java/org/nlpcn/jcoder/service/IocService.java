package org.nlpcn.jcoder.service;

import static org.nlpcn.jcoder.service.SharedSpaceService.HOST_GROUP_PATH;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean
public class IocService {
	private SharedSpaceService sharedSpaceService;

	public IocService(SharedSpaceService sharedSpaceService) {
		this.sharedSpaceService = sharedSpaceService;
	}
	
	public Set<String> getAllHosts() throws Exception {
		if(StaticValue.IS_LOCAL){
			Set<String> hosts = new HashSet<String>();
			hosts.add("127.0.0.1:9095");
			return hosts;
		}
		List<String> hostGroupPath = sharedSpaceService.getZk().getChildren().forPath(HOST_GROUP_PATH);
		return hostGroupPath.stream().filter(s -> s.split("_").length == 1).collect(Collectors.toSet());
		//return hostGroupPath.toString();
	}
}
