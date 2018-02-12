package org.nlpcn.jcoder.controller;

import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by hanx on 06/2/2018.
 * 这个类提供日志收集
 */

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/logs")
@Ok("json")
public class LogsAction {

    /**
     * 获取主机列表
     */
    @At
    public Restful getAllHosts() {
        List<String> allHosts = null;
        try {
            allHosts = StaticValue.space().getAllHosts();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Restful.ok().obj(allHosts);
    }

    /**
     * 获取group列表
     */
    @At
    public Restful getAllGroups() {
        List<String> allGroups = null;
        try {
            allGroups = StaticValue.space().getZk().getChildren().forPath("/jcoder/group");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Restful.ok().obj(allGroups);
    }

}
