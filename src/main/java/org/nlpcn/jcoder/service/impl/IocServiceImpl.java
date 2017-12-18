package org.nlpcn.jcoder.service.impl;

import static org.nlpcn.jcoder.service.SharedSpaceService.HOST_GROUP_PATH;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.nlpcn.jcoder.service.IocService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean(factory = "org.nlpcn.jcoder.service.ServiceFactory#createIocService")
public class IocServiceImpl implements IocService {
	private SharedSpaceService sharedSpaceService;

	public IocServiceImpl(SharedSpaceService sharedSpaceService) {
		this.sharedSpaceService = sharedSpaceService;
	}
	
	@Override
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
