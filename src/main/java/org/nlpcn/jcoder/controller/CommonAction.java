package org.nlpcn.jcoder.controller;

import org.nlpcn.jcoder.constant.Constants;
import org.nlpcn.jcoder.domain.HostGroup;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.util.Restful;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/common")
@Ok("json")
public class CommonAction {

	private static final Logger LOG = LoggerFactory.getLogger(CommonAction.class);

	@Inject
	private GroupService groupService;

	private HostGroup master;

	{
		master = new HostGroup();
		master.setCurrent(true);
		master.setHostPort(Constants.HOST_MASTER);
	}

	@At
	public Restful host(String groupName) throws Exception {
		List<HostGroup> hostList = groupService.getGroupHostList(groupName);
		hostList.add(0, master);
		return Restful.instance(hostList);
	}
}
