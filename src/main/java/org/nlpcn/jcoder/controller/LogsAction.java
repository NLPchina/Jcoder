package org.nlpcn.jcoder.controller;

import com.google.common.collect.ImmutableMap;
import org.apache.curator.framework.CuratorFramework;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.job.StatisticalJob;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

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
        for (String h : Optional.ofNullable(StaticValue.space().getZk().getChildren().forPath(SharedSpaceService.LOG_STATS_PATH)).orElse(Collections.emptyList())) {
            hosts.add(h);
            groups.addAll(Optional.ofNullable(StaticValue.space().getZk().getChildren().forPath(SharedSpaceService.LOG_STATS_PATH + "/" + h)).orElse(Collections.emptyList()));
        }
        return Restful.ok().obj(ImmutableMap.of("hosts", hosts, "groups", groups));
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
    public Restful statList(@Param("dates[]") String[] dates, @Param("hosts[]") String[] hosts, @Param("groups[]") String[] groups) throws Exception {
        String start, end;
        if (dates == null) {
            start = "00000000";
            end = "99999999";
        } else if (dates.length > 1) {
            // 如果是日期范围
            start = dates[0];
            end = dates[1];
        } else {
            int n = Integer.parseInt(dates[0]);
            // 如果是最近x天
            LocalDate now = LocalDate.now();
            start = LOCAL_DATE.format(now.minus(n - 1, ChronoUnit.DAYS));
            end = LOCAL_DATE.format(now);
        }

        //
        CuratorFramework zk = StaticValue.space().getZk();
        Map<String, StatisticalJob.Stats> map = new HashMap<>();
        String path, path2, path3;
        StatisticalJob.Stats stats;
        int index;
        for (String h : Optional.ofNullable(hosts).orElse(Optional.ofNullable(zk.getChildren().forPath(SharedSpaceService.LOG_STATS_PATH)).orElseGet(Collections::emptyList).toArray(new String[0]))) {
            for (String g : Optional.ofNullable(groups).orElse(Optional.ofNullable(zk.getChildren().forPath(SharedSpaceService.LOG_STATS_PATH + "/" + h)).orElseGet(Collections::emptyList).toArray(new String[0]))) {
                // 获取所有节点: class-method
                path = SharedSpaceService.LOG_STATS_PATH + "/" + h + "/" + g;
                for (String item : Optional.ofNullable(zk.getChildren().forPath(path)).orElse(Collections.emptyList())) {
                    path2 = path + "/" + item;
                    for (String date : Optional.ofNullable(zk.getChildren().forPath(path2)).orElse(Collections.emptyList())) {
                        if (date.compareTo(start) < 0 || date.compareTo(end) > 0) {
                            continue;
                        }

                        // 合并这个节点下的所有统计信息
                        path3 = path2 + "/" + date;
                        stats = Optional.ofNullable(StaticValue.space().getData(path3, StatisticalJob.Stats.class)).orElse(new StatisticalJob.Stats());
                        for (String time : Optional.ofNullable(zk.getChildren().forPath(path3)).orElse(Collections.emptyList())) {
                            Optional.of(StaticValue.space().getData(path3 + "/" + time, StatisticalJob.Stats.class)).ifPresent(stats::merge);
                        }

                        // 所有的 分组-类-方法 一致的统计合并
                        index = item.indexOf('-');
                        stats.setGroupName(g);
                        stats.setClassName(item.substring(0, index));
                        stats.setMethodName(item.substring(index + 1));
                        map.merge(g + item, stats, StatisticalJob.Stats::merge);
                    }
                }
            }
        }

        return Restful.ok().obj(map.values());
    }
}
