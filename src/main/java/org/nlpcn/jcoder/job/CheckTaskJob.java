package org.nlpcn.jcoder.job;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.TaskInfo;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.BasicDao;

import java.util.List;
import java.util.Set;

/**
 * 定期检查集群运行情况。只有master可以
 *
 * @author ansj
 */
public class CheckTaskJob implements Runnable {

    private static final Logger LOG = Logger.getLogger(CheckTaskJob.class);

    @Override
    public void run() {

        TaskService taskService = StaticValue.getBean(TaskService.class, "taskService");

        while (true) {
            try {
                Thread.sleep(60000L);

                LOG.info("begin checkTaskJob");
                // 获得当前运行的任务
                List<Task> search = StaticValue.systemDao.search(Task.class, "id");

                // 线程任务
                List<TaskInfo> threads = ThreadManager.getAllThread();


                Set<String> sets = Sets.newHashSet();

                threads.forEach(ti -> sets.add(ti.getName()));

                for (Task task : search) {
                    // 检查while的task是否活着
                    if (task.getStatus() == 1 && "while".equalsIgnoreCase(task.getScheduleStr())) {
                        if (!sets.contains(task.getName())) {
                            LOG.warn(task.getName() + " is while task , not find in threads , now to start it! ");
                            taskService.flush(task.getId());
                        }
                    }
                    // stop的task是否活着
                    if (task.getStatus() == 0) {
                        // 如果不是1 那么不正常，全局刷新
                        if (sets.contains(task.getName())) {
                            LOG.warn(task.getName() + " is stop task , but it is runing, now sotp it ! ");
                            taskService.flush(task.getId());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(e);
            }
        }
    }
}
