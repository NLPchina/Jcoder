package org.nlpcn.jcoder.controller;

import com.google.common.collect.ImmutableMap;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * 获取日志统计的所有主机及分组
     */
    @At("/hostgroup/list")
    public Restful hostGroupList() throws Exception {
        Set<String> hosts = new HashSet<>(), groups = new HashSet<>();
        if (StaticValue.space().getZk().checkExists().forPath(SharedSpaceService.LOG_STATS_PATH) != null) {
            int index;
            for (String h : StaticValue.space().getZk().getChildren().forPath(SharedSpaceService.LOG_STATS_PATH)) {
                hosts.add(h);

                for (String item : StaticValue.space().getZk().getChildren().forPath(SharedSpaceService.LOG_STATS_PATH + "/" + h)) {
                    for (index = item.length() - 1; index >= 0; index--) {
                        if (item.charAt(index) == '-') {
                            break;
                        }
                    }
                    for (index--; index >= 0; index--) {
                        if (item.charAt(index) == '-') {
                            break;
                        }
                    }

                    groups.add(item.substring(0, index));
                }
            }
        }
        return Restful.ok().obj(ImmutableMap.of("hosts", hosts, "groups", groups));
    }
}
