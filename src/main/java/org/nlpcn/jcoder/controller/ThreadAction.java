package org.nlpcn.jcoder.controller;

import java.util.List;

import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.TaskInfo;
import org.nlpcn.jcoder.domain.UserGroup;
import org.nlpcn.jcoder.scheduler.TaskException;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.dao.Cnd;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.filter.CheckSession;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

@IocBean
public class ThreadAction {

	private static final Logger LOG = LoggerFactory.getLogger(ThreadAction.class) ;

	private BasicDao basicDao = StaticValue.systemDao;

	@Filters(@By(type = CheckSession.class, args = { "user", "/login.html" }))
	@At("/thread/list/")
	@Ok("jsp:/thread_list.jsp")
	public JSONObject list() throws TaskException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, SchedulerException {
		return this.runInfo();
	}

	/**
	 * 通过api的方式获得线程
	 * 
	 * @return
	 * @throws SchedulerException
	 * @throws TaskException
	 */
	@At("/thread/info")
	@Ok("json")
	public JSONObject runInfo() throws SchedulerException, TaskException {
		// 线程任务
		List<TaskInfo> threads = ThreadManager.getAllThread();

		// 获得计划任务
		List<TaskInfo> schedulers = ThreadManager.getAllScheduler();

		// 获得执行中的action
		List<TaskInfo> actions = ThreadManager.getAllAction();

		JSONObject json = new JSONObject();

		json.put("threads", threads);

		json.put("schedulers", schedulers);

		json.put("actions", actions);

		return json;
	}

	@Filters(@By(type = CheckSession.class, args = { "user", "/login.html" }))
	@At("/thread/stop/task/?")
	@Ok("redirect:/thread/list/")
	public void stopTask(String name) throws Exception {
		try {
			authEditorValidate(name);
			try {
				ThreadManager.stop(name);
			} catch (TaskException e) {
				e.printStackTrace();
				LOG.error("stop task err!",e);
			}
		} catch (Exception e) {
			LOG.error("author err",e);
			throw e ;
		}
	}


	/**
	 * 停止一个运行的action
	 * 
	 * @param host
	 * @param name
	 * @throws Exception
	 */
	@Filters(@By(type = CheckSession.class, args = { "user", "/login.html" }))
	@At("/thread/stop/?")
	@Ok("redirect:/thread/list/")
	public void stop(String key) throws Exception {
		try {
			authEditorValidate(key.split("@")[0]);
			try {
				ThreadManager.stop(key);
			} catch (TaskException e) {
				e.printStackTrace();
				LOG.error("stop action err",e);
			}
		} catch (Exception e) {
			LOG.error("author err",e);
			throw e ;
		}
	}

	/**
	 * 编辑task权限验证
	 * 
	 * @throws Exception
	 */
	private void authEditorValidate(String name) throws Exception {
		if ((Integer) Mvcs.getHttpSession().getAttribute("userType") == 1) {
			return;
		}

		Task task = TaskService.findTaskByCache(name);

		UserGroup ug = basicDao.findByCondition(UserGroup.class, Cnd.where("groupId", "=", task.getGroupId()).and("userId", "=", Mvcs.getHttpSession().getAttribute("userId")));
		if (ug == null || ug.getAuth() != 2) {
			throw new Exception("not have editor auth in groupId:" + task.getGroupId());
		}
	}

}
