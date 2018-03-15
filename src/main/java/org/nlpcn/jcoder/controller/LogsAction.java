package org.nlpcn.jcoder.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import org.apache.curator.framework.CuratorFramework;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.job.StatisticalJob;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoField.*;

/**
 * Created by hanx on 06/2/2018.
 * 这个类提供日志收集
 */

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/logs")
@Ok("json")
public class LogsAction {

    private static final DateTimeFormatter LOCAL_DATE = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4)
            .appendValue(MONTH_OF_YEAR, 2)
            .appendValue(DAY_OF_MONTH, 2)
            .toFormatter();

    @Inject
    private GroupService groupService;

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
        Set<String> hosts = new HashSet<>();
        for (String date : Optional.ofNullable(StaticValue.space().getZk().getChildren().forPath(SharedSpaceService.LOG_STATS_PATH)).orElse(Collections.emptyList())) {
            hosts.addAll(Optional.ofNullable(StaticValue.space().getZk().getChildren().forPath(SharedSpaceService.LOG_STATS_PATH + "/" + date)).orElse(Collections.emptyList()));
        }
        return Restful.ok().obj(ImmutableMap.of("hosts", hosts, "groups", groupService.getAllGroupNames()));
    }

    /**
     * 查询API统计信息
     *
     * @param dates  日期。可以是日期范围，也可以是最近x天
     * @param hosts  主机列表
     * @param groups 分组列表
     * @return
     * @throws Exception
     */
    @At("/stat/list")
    public Restful statList(String[] dates, String[] hosts, String[] groups) throws Exception {
        LocalDate now = LocalDate.now();
        String start = "00000000", end = "99999999", today = LOCAL_DATE.format(now);
        if (dates != null) {
            if (dates.length < 1) {
                start = end;
            } else if (dates.length > 1) {
                // 如果是日期范围
                start = dates[0];
                end = dates[1];
            } else {
                // 如果是最近x天
                int n = Integer.parseInt(dates[0]);
                start = LOCAL_DATE.format(now.minus(n - 1, ChronoUnit.DAYS));
                end = today;
            }
        }

        Set<String> groupSet = null;
        if (groups != null) {
            groupSet = Arrays.stream(groups).collect(Collectors.toSet());
        }

        //
        byte[] bytes;
        String[] arr;
        String path, path2;
        Map<String, StatisticalJob.Stats> statsData;
        StatisticalJob.Stats stats;
        CuratorFramework zk = StaticValue.space().getZk();
        Map<String, StatisticalJob.Stats> map = new HashMap<>();
        for (String date : Optional.ofNullable(zk.getChildren().forPath(SharedSpaceService.LOG_STATS_PATH)).orElseGet(Collections::emptyList)) {
            if (date.compareTo(start) < 0 || date.compareTo(end) > 0) {
                continue;
            }

            path = SharedSpaceService.LOG_STATS_PATH + "/" + date;
            for (String h : Optional.ofNullable(hosts).orElse(Optional.ofNullable(zk.checkExists().forPath(path) != null ? zk.getChildren().forPath(path) : null).orElseGet(Collections::emptyList).toArray(new String[0]))) {
                path2 = path + "/" + h;
                bytes = StaticValue.space().getData2ZK(path2);
                statsData = bytes != null && 0 < bytes.length ? JSONObject.<JSONObject>parseObject(bytes, JSONObject.class).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, o -> JSON.toJavaObject((JSONObject) o.getValue(), StatisticalJob.Stats.class))) : null;

                // 对每个时间节点做合并
                if (statsData == null) {
                    statsData = new HashMap<>();
                    for (String time : Optional.ofNullable(zk.checkExists().forPath(path2) != null ? zk.getChildren().forPath(path2) : null).orElse(Collections.emptyList())) {
                        bytes = StaticValue.space().getData2ZK(path2 + "/" + time);
                        for (Map.Entry<String, Object> entry : bytes != null && 0 < bytes.length ? JSONObject.<JSONObject>parseObject(bytes, JSONObject.class).entrySet() : Collections.<Map.Entry<String, Object>>emptySet()) {
                            statsData.merge(entry.getKey(), JSON.toJavaObject((JSONObject) entry.getValue(), StatisticalJob.Stats.class), StatisticalJob.Stats::merge);
                        }
                    }

                    // 将今天以前的日志统计信息汇总后存入父节点
                    if (date.compareTo(today) < 0) {
                        StaticValue.space().setData2ZK(path2, JSON.toJSONBytes(statsData));
                    }
                }

                // 所有的 分组-类-方法 一致的统计合并
                for (Map.Entry<String, StatisticalJob.Stats> entry : statsData.entrySet()) {
                    arr = entry.getKey().split("/");
                    if (!(groups == null || groupSet.contains(arr[0]))) {
                        continue;
                    }

                    stats = entry.getValue();
                    stats.setGroupName(arr[0]);
                    stats.setClassName(arr[1]);
                    stats.setMethodName(arr[2]);
                    map.merge(entry.getKey(), stats, StatisticalJob.Stats::merge);
                }
            }
        }

        return Restful.ok().obj(map.values());
    }
}
