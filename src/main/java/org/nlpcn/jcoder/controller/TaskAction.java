package org.nlpcn.jcoder.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import org.nlpcn.jcoder.constant.Api;
import org.nlpcn.jcoder.constant.TaskStatus;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.TaskHistory;
import org.nlpcn.jcoder.domain.User;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.run.CodeException;
import org.nlpcn.jcoder.run.java.JavaRunner;
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

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.nlpcn.jcoder.constant.Constants.TIMEOUT;
import static org.nlpcn.jcoder.constant.Constants.CURRENT_USER;
import static org.nlpcn.jcoder.service.ProxyService.MERGE_FALSE_MESSAGE_CALLBACK;
import static org.nlpcn.jcoder.util.ApiException.ServerException;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/task")
@Ok("json")
public class TaskAction {

	private static final Logger LOG = LoggerFactory.getLogger(TaskAction.class);

	@Inject
	private TaskService taskService;

    @Inject
    private ProxyService proxyService;

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
				.map(t -> ImmutableMap.of("name", t.getName(), "describe", t.getDescription(), "status", t.getStatus(), "createTime", t.getCreateTime(), "updateTime", t.getUpdateTime()))
				.toArray();
		return Restful.instance(tasks);
	}

	@At
    public Restful save(@Param("hosts[]") String[] hosts, @Param("::task") Task task) throws Exception {
        if (hosts == null) {
            throw new IllegalArgumentException("empty hosts");
        }

        if (task == null) {
            throw new IllegalArgumentException("task is null");
        }

        if (StringUtil.isBlank(task.getName()) || StringUtil.isBlank(task.getDescription()) || StringUtil.isBlank(task.getCode())) {
            throw new IllegalArgumentException("task name, description or code is empty");
        }

        // 如果激活任务, 需要检查代码
        if (task.getStatus() == TaskStatus.ACTIVE.getValue()) {
            String errorMessage = proxyService.post(hosts, Api.TASK_CHECK.getPath(), ImmutableMap.of("task", JSON.toJSONString(task)), TIMEOUT, MERGE_FALSE_MESSAGE_CALLBACK);
            if (StringUtil.isNotBlank(errorMessage)) {
                return Restful.ERR.code(ServerException).msg(errorMessage);
            }
        }

        // 保存, 先存入ZK，再分发到集群的每台机器保存
        User u = (User) Mvcs.getHttpSession().getAttribute(CURRENT_USER);
        Date now = new Date();
        if (task.getId() == null) {
            task.setCreateUser(u.getName());
            task.setCreateTime(now);
        }
        task.setUpdateUser(u.getName());
        task.setUpdateTime(now);
        StaticValue.space().addTask(task);
        String errorMessage = proxyService.post(hosts, Api.TASK_SAVE.getPath(), ImmutableMap.of("task", JSON.toJSONString(task)), TIMEOUT, MERGE_FALSE_MESSAGE_CALLBACK);
        if (StringUtil.isNotBlank(errorMessage)) {
            return Restful.ERR.code(ServerException).msg(errorMessage);
        }

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

        LOG.info("task[{}-{}] is modified: {}", task.getGroupName(), task.getName(), taskService.saveOrUpdate(task));

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

	@At("/task/_new/?")
	@Ok("jsp:/task/task_editor.jsp")
	public void create(Long groupId) {
		Mvcs.getReq().setAttribute("groupId", groupId);
	}

	@At("/task/delete/?")
	@Ok("raw")
	public boolean delete(String name) {
		Task task = TaskService.findTaskByCache(name);
		task = TaskService.findTaskByDB(task.getId()); // old task and new task must not same
		try {
			taskService.delete(task);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@At("/task/del/?")
	@Ok("raw")
	public boolean del(String name) {
		Task task = TaskService.findTaskByCache(name);
		try {
			taskService.delByDB(task);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 通过一个接口获取这个group下的所有task，包括未激活或者删除的task，镜像用
	 *
	 * @param groupName
	 */
	@At
	public Restful taskGroupList(@Param("groupName") String groupName) {
		return Restful.instance(taskService.getTasksByGroupName(groupName));

	}

	/**
	 * 查看权限验证
	 *
	 * @param groupId
	 */
	private void authValidateView(Long groupId) {
		HttpSession session = Mvcs.getHttpSession();

		if ((Integer) session.getAttribute("userType") == 1) {
			return;
		}

		@SuppressWarnings("unchecked")
		Map<Long, Integer> authMap = (Map<Long, Integer>) session.getAttribute("AUTH_MAP");

		if (authMap.containsKey(groupId)) {
			return;
		}

		throw new RuntimeException("auth error !");
	}

}
