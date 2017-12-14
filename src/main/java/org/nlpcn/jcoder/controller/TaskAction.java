package org.nlpcn.jcoder.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.TaskHistory;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.*;
import org.nutz.mvc.filter.CheckSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.util.*;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/task")
@Ok("json")
public class TaskAction {

	private static final Logger LOG = LoggerFactory.getLogger(TaskAction.class);

	@Inject
	private TaskService taskService;

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
        Object[] tasks = StaticValue.space()
                .getTasksByGroupName(groupName)
                .stream()
                .filter(t -> taskType == -1 || t.getType() == taskType)
                .map(t -> ImmutableMap.of("name", t.getName(), "describe", t.getDescription(), "status", t.getStatus(), "createTime", t.getCreateTime(), "updateTime", t.getUpdateTime()))
                .toArray();
        return Restful.instance(tasks);
    }

	@At("/task/save/?")
	@Ok("raw")
	public String save(Long groupId, @Param("::task") Task task) {
		JSONObject job = new JSONObject();
		try {
			boolean save = taskService.saveOrUpdate(task, groupId);
			job.put("ok", true);
			job.put("save", save);
			job.put("message", "save ok！");
			job.put("id", task.getId());
			job.put("name", task.getName());
			return job.toJSONString();
		} catch (Exception e) {
			e.printStackTrace();
			job.put("ok", false);
			job.put("message", "save err!　message:" + e.getMessage());
			return job.toJSONString();
		}
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

	@At("/task/group")
	@Ok("jsp:/task/task_group_list.jsp")
	@Fail("jsp:/fail.jsp")
	public void taskGroupList(@Param("groupId") Long groupId) {
		authValidateView(groupId);
		Mvcs.getReq().setAttribute("groupId", groupId);
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
