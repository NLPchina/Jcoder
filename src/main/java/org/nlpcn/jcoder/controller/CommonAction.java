package org.nlpcn.jcoder.controller;

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

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/common")
@Ok("json")
public class CommonAction {

    private static final Logger LOG = LoggerFactory.getLogger(CommonAction.class);

    @Inject
    private GroupService groupService;

    @At
    public Restful host() throws Exception {
        return Restful.instance(groupService.getAllHosts());
    }
}
