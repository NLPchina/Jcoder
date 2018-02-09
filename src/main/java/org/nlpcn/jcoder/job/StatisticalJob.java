package org.nlpcn.jcoder.job;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import org.nlpcn.jcoder.domain.LogInfo;
import org.nlpcn.jcoder.service.SharedSpaceService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * 统计任务，定时生成。报告
 * Created by Ansj on 05/02/2018.
 */
public class StatisticalJob implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticalJob.class);

    private static final Map<String, Stats> STATS_CACHE = new ConcurrentHashMap<>();

    @Override
    public void run() {
        while (true) {
            try {
                Stream.of(STATS_CACHE.keySet().toArray(new String[0])).forEach(key -> STATS_CACHE.compute(key, (k, stats) -> {
                    appendStats2ZK(k, stats);

                    // 移除数据
                    return null;
                }));

                TimeUnit.MINUTES.sleep(1);
            } catch (Exception e) {
                LOG.error("run stats job error", e);
            }
        }
    }

    /**
     * 将日志统计信息放入ZK, 存储结构: jcoder <- log_stats <- 主机 <- group_class_method <- 年月日 <- (时分 ------- 日志统计信息)
     *
     * @param key   格式: group_class_method|年月日时分
     * @param stats 日志统计信息
     */
    private void appendStats2ZK(String key, Stats stats) {
        // 追加数据
        int index = key.lastIndexOf('|'), len = key.length();
        String path = String.format("%s/%s/%s/%s/%s",
                SharedSpaceService.LOG_STATS_PATH,
                StaticValue.getHostPort(),
                key.substring(0, index),
                key.substring(index + 1, len - 4),
                key.substring(len - 4));
        try {
            if (StaticValue.space().getZk().checkExists().forPath(path) != null) {
                // 同一分钟的日志进行合并
                Optional<Stats> opt = Optional.of(StaticValue.space().getData(path, Stats.class));
                if (opt.isPresent()) {
                    stats = opt.get().add(stats);
                }
            }
            StaticValue.space().setData2ZK(path, JSONObject.toJSONBytes(stats));
        } catch (Exception e) {
            throw Lang.wrapThrow(e, "append stats[%s-%s] to zookeeper error", key, JSONObject.toJSONString(stats));
        }
    }

    /**
     * 对日志信息做统计
     *
     * @param log 日志信息
     */
    public static void add(LogInfo log) {
        if (StringUtil.isBlank(log.getGroupName()) || StringUtil.isBlank(log.getClassName()) || StringUtil.isBlank(log.getMethodName())) {
            if (LOG.isDebugEnabled()) {
                LOG.warn("groupName or className or methodName not found");
            }
            return;
        }
        String message = log.getMessage();
        if (StringUtil.isBlank(message)) {
            LOG.warn("empty log message");
            return;
        }

        //
        Stats stats;
        int duration;
        if (message.startsWith("Execute OK")) {
            // Execute OK  ApiTest/test succesed ! use Time : 0
            (stats = new Stats()).successCount.incrementAndGet();
            duration = Integer.parseInt(StringUtils.subString(message, " succesed ! use Time : ", null).trim());
        } else if (message.startsWith("Execute ERR")) {
            // Execute ERR  ApiTest/test useTime 0 erred : java.lang.reflect.InvocationTargetException...
            (stats = new Stats()).errorCount.incrementAndGet();
            duration = Integer.parseInt(StringUtils.subString(message, " useTime ", " erred : ").trim());
        } else {
            // 忽略其他日志信息
            return;
        }

        // 最小耗时
        if (duration < stats.minDuration.get()) {
            stats.minDuration.updateAndGet(operand -> Math.min(duration, operand));
        }

        // 最大耗时
        if (duration > stats.maxDuration.get()) {
            stats.maxDuration.updateAndGet(operand -> Math.max(duration, operand));
        }

        // 总耗时
        stats.totalDuration.addAndGet(duration);

        // 格式: group_class_method年月日时分
        ZonedDateTime time = Instant.ofEpochMilli(log.getTime()).atZone(ZoneId.systemDefault());
        String key = String.format("%s_%s_%s|%s%s%s%s%s",
                log.getGroupName(),
                log.getClassName(),
                log.getMethodName(),
                time.getYear(),
                Strings.padStart(String.valueOf(time.getMonthValue()), 2, '0'),
                Strings.padStart(String.valueOf(time.getDayOfMonth()), 2, '0'),
                Strings.padStart(String.valueOf(time.getHour()), 2, '0'),
                Strings.padStart(String.valueOf(time.getMinute()), 2, '0'));
        STATS_CACHE.merge(key, stats, Stats::add);
    }

    public static class Stats {
        private final AtomicInteger successCount = new AtomicInteger();
        private final AtomicInteger errorCount = new AtomicInteger();
        private final AtomicInteger minDuration = new AtomicInteger();
        private final AtomicInteger maxDuration = new AtomicInteger();
        private final AtomicInteger totalDuration = new AtomicInteger();

        private Stats add(Stats stats) {
            successCount.addAndGet(stats.successCount.get());
            errorCount.addAndGet(stats.errorCount.get());
            if (stats.minDuration.get() < minDuration.get()) {
                minDuration.updateAndGet(operand -> Math.min(operand, stats.minDuration.get()));
            }
            if (stats.maxDuration.get() > maxDuration.get()) {
                maxDuration.updateAndGet(operand -> Math.max(operand, stats.maxDuration.get()));
            }
            totalDuration.addAndGet(stats.totalDuration.get());
            return this;
        }

        public int getSuccessCount() {
            return successCount.get();
        }

        public void setSuccessCount(int successCount) {
            this.successCount.set(successCount);
        }

        public int getErrorCount() {
            return errorCount.get();
        }

        public void setErrorCount(int errorCount) {
            this.errorCount.set(errorCount);
        }

        public int getMinDuration() {
            return minDuration.get();
        }

        public void setMinDuration(int minDuration) {
            this.minDuration.set(minDuration);
        }

        public int getMaxDuration() {
            return maxDuration.get();
        }

        public void setMaxDuration(int maxDuration) {
            this.maxDuration.set(maxDuration);
        }

        public int getTotalDuration() {
            return totalDuration.get();
        }

        public void setTotalDuration(int totalDuration) {
            this.totalDuration.set(totalDuration);
        }
    }
}
