package org.nlpcn.jcoder.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import org.h2.util.DateTimeUtils;
import org.nlpcn.jcoder.constant.Api;
import org.nlpcn.jcoder.constant.TaskStatus;
import org.nlpcn.jcoder.constant.TaskType;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.TaskHistory;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.run.CodeException;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.service.GroupService;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.nlpcn.jcoder.constant.Constants.CURRENT_USER;
import static org.nlpcn.jcoder.constant.Constants.TIMEOUT;
import static org.nlpcn.jcoder.service.ProxyService.MERGE_FALSE_MESSAGE_CALLBACK;
import static org.nlpcn.jcoder.util.ApiException.ServerException;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/task")
@Ok("json")
public class TaskAction {

    private static final Logger LOG = LoggerFactory.getLogger(TaskAction.class);

    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Inject
    private TaskService taskService;

    @Inject
    private ProxyService proxyService;

    @Inject
    private GroupService groupService;

    /**
     * 获得task列表
     *
     * @param groupName 组名
     * @param taskType  task类型: 0、垃圾；1、独立；2、计划；3、调度
     * @return
     * @throws Exception
     */
    @At
    public Restful list(String groupName, @Param(value = "taskType", df = "-1") int taskType) throws Exception {
        Object[] tasks = taskService.getTasksByGroupNameFromCluster(groupName)
                .stream()
                .filter(t -> taskType == -1 || Objects.equals(t.getType(), taskType))
                .map(t -> ImmutableMap.of("name", t.getName(),
                        "description", t.getDescription(),
                        "status", t.getStatus(),
                        "createTime", DateTimeUtils.formatDateTime(t.getCreateTime(), DATETIME_FORMAT, null, null),
                        "updateTime", DateTimeUtils.formatDateTime(t.getUpdateTime(), DATETIME_FORMAT, null, null)))
                .toArray();
        return Restful.instance(tasks);
    }

    /**
     * 保存或更新任务
     *
     * @param hosts
     * @param task
     * @return
     * @throws Exception
     */
    @At
    public Restful save(@Param("hosts[]") String[] hosts, @Param("::task") Task task) throws Exception {
        if (hosts == null) {
            throw new IllegalArgumentException("empty hosts");
        }

        if (task == null) {
            throw new IllegalArgumentException("task is null");
        }

        if (StringUtil.isBlank(task.getName()) || StringUtil.isBlank(task.getCode())) {
            throw new IllegalArgumentException("task name or code is empty");
        }

        // 如果激活任务, 需要检查代码
        if (task.getStatus() == TaskStatus.ACTIVE.getValue()) {
            String errorMessage = proxyService.post(hosts, Api.TASK_CHECK.getPath(), ImmutableMap.of("task", JSON.toJSONString(task)), TIMEOUT, MERGE_FALSE_MESSAGE_CALLBACK);
            if (StringUtil.isNotBlank(errorMessage)) {
                return Restful.ERR.code(ServerException).msg(errorMessage);
            }
        }

        // 保存
        User u = (User) Mvcs.getHttpSession().getAttribute(CURRENT_USER);
        Date now = new Date();
        if (task.getId() == null) {
            task.setCreateUser(u.getName());
            task.setCreateTime(now);
        }
        task.setUpdateUser(u.getName());
        task.setUpdateTime(now);

        // 集群的每台机器保存
        String errorMessage = proxyService.post(hosts, Api.TASK_SAVE.getPath(), ImmutableMap.of("task", JSON.toJSONString(task)), TIMEOUT, MERGE_FALSE_MESSAGE_CALLBACK);
        if (StringUtil.isNotBlank(errorMessage)) {
            return Restful.ERR.code(ServerException).msg(errorMessage);
        }

        // ZK保存
        StaticValue.space().addTask(task);

        return Restful.OK;
    }

    @At
    public Restful __check__(@Param("::task") Task task) throws CodeException, IOException {
        if (task.getStatus() == TaskStatus.ACTIVE.getValue()) {
            new JavaRunner(task).check();
        }
        return Restful.OK;
    }

    @At
    public Restful __save__(@Param("::task") Task task) throws Exception {
        // 如果是编辑, 得替换成本地任务ID
        if (task.getId() != null) {
            Task t = taskService.findTask(task.getGroupName(), task.getName());
            task.setId(t.getId());
        }
        LOG.info("to save task[{}-{}]: {}", task.getGroupName(), task.getName(), taskService.saveOrUpdate(task));
        return Restful.OK;
    }

    @At("/task/editor/?/?")
    @Ok("jsp:/task/task_editor.jsp")
    public void find(Long groupId, Long taskId, @Param("version") String version) {
        Mvcs.getReq().setAttribute("groupId", groupId);
        if (!StringUtil.isBlank(version)) {
            TaskHistory task = TaskService.findTaskByDBHistory(taskId, version);
            Mvcs.getReq().setAttribute("task", task);
            taskId = task.getTaskId();
        } else {
            Task task = TaskService.findTaskByDB(taskId);
            Mvcs.getReq().setAttribute("task", new TaskHistory(task));
            version = task.getVersion();
            taskId = task.getId();
        }

        List<String> versions = taskService.versions(taskId, 100);
        Mvcs.getReq().setAttribute("versions", versions);
    }

    @At("/task/find/?/?")
    @Ok("json")
    public JSONObject findApi(Long groupId, Long taskId, @Param("version") String version) {
        JSONObject result = new JSONObject();
        Mvcs.getReq().setAttribute("groupId", groupId);
        if (!StringUtil.isBlank(version)) {
            TaskHistory task = TaskService.findTaskByDBHistory(taskId, version);
            result.put("task", task);
            taskId = task.getTaskId();
        } else {
            Task task = TaskService.findTaskByDB(taskId);
            result.put("task", new TaskHistory(task));
            version = task.getVersion();
            taskId = task.getId();
        }
        return result;
    }

    /**
     * 删除任务
     * 如果任务类型是垃圾, 就物理删除
     * 如果任务类型不是垃圾, 就更改任务类型和状态
     *
     * @param groupName
     * @param name
     * @return
     * @throws Exception
     */
    @At
    public Restful delete(@Param("hosts[]") String[] hosts, String groupName, String name, int type) throws Exception {
        if (hosts == null) {
            throw new IllegalArgumentException("empty hosts");
        }

        if (StringUtil.isBlank(groupName)) {
            throw new IllegalArgumentException("empty groupName");
        }

        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("empty name");
        }

        User u = (User) Mvcs.getHttpSession().getAttribute(CURRENT_USER);
        Date now = new Date();

        if (type == TaskType.RECYCLE.getValue()) {
            // 如果是垃圾, 物理删除
            // 删除集群的每台机器
            String errorMessage = proxyService.post(hosts, Api.TASK_DELETE.getPath(), ImmutableMap.of("force", true, "groupName", groupName, "name", name, "user", u.getName(), "time", now), TIMEOUT, MERGE_FALSE_MESSAGE_CALLBACK);
            if (StringUtil.isNotBlank(errorMessage)) {
                return Restful.ERR.code(ServerException).msg(errorMessage);
            }

            // 删ZK
            taskService.deleteTaskFromCluster(groupName, name);
        } else {
            // 更改类型为垃圾, 并停用
            // 更新集群的每台机器
            String errorMessage = proxyService.post(hosts, Api.TASK_SAVE.getPath(), ImmutableMap.of("groupName", groupName, "name", name, "user", u.getName(), "time", now), TIMEOUT, MERGE_FALSE_MESSAGE_CALLBACK);
            if (StringUtil.isNotBlank(errorMessage)) {
                return Restful.ERR.code(ServerException).msg(errorMessage);
            }

            // 更新ZK
            Task task = taskService.getTasksByGroupNameFromCluster(groupName).parallelStream().filter(t -> Objects.equals(t.getName(), name)).findAny().get();
            task.setUpdateUser(u.getName());
            task.setUpdateTime(now);
            task.setType(TaskType.RECYCLE.getValue());
            task.setStatus(TaskStatus.STOP.getValue());
            StaticValue.space().addTask(task);
        }

        return Restful.OK;
    }

    /**
     * 内部调用删除任务
     *
     * @param force     是否强制删除任务, 如果是就删除数据库的任务
     * @param groupName 组名
     * @param name      任务名
     * @param user      删除这个任务的用户
     * @param time      删除这个任务的时间
     * @return
     * @throws Exception
     */
    @At
    public Restful __delete__(@Param(value = "force", df = "false") boolean force, String groupName, String name, String user, Date time) throws Exception {

        LOG.info("to force[{}] delete task[{}-{}]", force, groupName, name);

        // 从本地数据库中找到任务
        Task t = taskService.findTask(groupName, name);
        t.setUpdateUser(user);
        t.setUpdateTime(time);
        taskService.delete(t);

        // 如果强制删除, 删除数据库中任务
        if (force) {
            taskService.delByDB(t);
        }

        return Restful.OK;
    }

    /**
     * 通过一个接口获取这个group下的所有task，包括未激活或者删除的task，镜像用
     *
     * @param groupName
     * @return
     */
    @At
    public Restful taskGroupList(@Param("groupName") String groupName) {
        return Restful.instance(taskService.findTasksByGroupName(groupName));
    }
}
