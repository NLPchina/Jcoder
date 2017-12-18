package org.nlpcn.jcoder.service;


import java.util.Set;
import java.util.stream.Collectors;

import org.nutz.ioc.loader.annotation.IocBean;

@IocBean
public class IocService {
	private SharedSpaceService sharedSpaceService;

	public IocService(SharedSpaceService sharedSpaceService) {
		this.sharedSpaceService = sharedSpaceService;
	}
	
	public Set<String> getAllHosts(final String groupName) throws Exception {
		return sharedSpaceService.getHostGroupCache().keySet().stream().filter(s -> {
			String[] split = s.split("_"); 
			return groupName.equals(split[2]) ;
		}).collect(Collectors.toSet());
	}
}
