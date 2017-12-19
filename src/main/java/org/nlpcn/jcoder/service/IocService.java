package org.nlpcn.jcoder.service;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.nlpcn.jcoder.util.IOUtil;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.domain.HostGroup;
import org.nutz.ioc.loader.annotation.IocBean;

@IocBean
public class IocService {
	private SharedSpaceService sharedSpaceService;

	public IocService() {
		this.sharedSpaceService = StaticValue.space();
	}

	public Map<String,HostGroup> getAllHosts(final String groupName) throws Exception {

		Map<String,HostGroup> result = new HashMap<>() ;

		Set<Map.Entry<String, HostGroup>> entries = sharedSpaceService.getHostGroupCache().entrySet();

		for (Map.Entry<String, HostGroup> entry : entries) {
			String[] split = entry.getKey().split("_");

			if(split[1].equals(groupName)){
				result.put(split[0],entry.getValue()) ;
			}
		}
		return result;
	}

	public String getIocInfo(String groupName) throws Exception {
		File ioc = new File(StaticValue.GROUP_FILE, groupName + "/resources");
		return IOUtil.getContent(new File(ioc, "ioc.js").getAbsolutePath(), "utf-8");
	}
}
