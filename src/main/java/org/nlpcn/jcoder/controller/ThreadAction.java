package org.nlpcn.jcoder.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableMap;
import org.nlpcn.jcoder.domain.TaskInfo;
import org.nlpcn.jcoder.filter.AuthoritiesManager;
import org.nlpcn.jcoder.scheduler.TaskException;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.service.ProxyService;
import org.nlpcn.jcoder.util.Restful;
import org.nlpcn.jcoder.util.StaticValue;
import org.nlpcn.jcoder.util.StringUtil;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

@IocBean
@Filters(@By(type = AuthoritiesManager.class))
@At("/admin/thread")
@Ok("json")
public class ThreadAction {

	private static final Logger LOG = LoggerFactory.getLogger(ThreadAction.class) ;

	private BasicDao basicDao = StaticValue.systemDao;

	@Inject
	private ProxyService proxyService ;

	/**
	 * 通过api的方式获得线程
	 *
	 * @return
	 * @throws TaskException
	 */
	@At
	public Restful list(@Param("hostPorts[]") String[] hostPorts ,@Param("groupName") String groupName ,@Param(value = "first" , df = "true") boolean first) throws Exception {

		if(first) {
			if (hostPorts == null || hostPorts.length == 0 || StringUtil.isBlank(hostPorts[0])) {
				hostPorts = StaticValue.space().getAllHosts().toArray(new String[0]);
			}


			Map<String, String> post = proxyService.post(hostPorts, "/admin/thread/list", ImmutableMap.of("groupName", groupName, "first", false), 10000);

			for (Map.Entry<String, String> entry : post.entrySet()) {

				JSONObject jsonObject = JSONObject.parseObject(entry.getValue());

			}


		}else {

			// 线程任务
			List<TaskInfo> threads = ThreadManager.getAllThread();

			// 获得计划任务
			List<TaskInfo> schedulers = ThreadManager.getAllScheduler();

			// 获得执行中的action
			List<TaskInfo> actions = ThreadManager.getAllAction();

			if (StringUtil.isNotBlank(groupName)) {
				threads = threads.stream().filter(t -> t.getGourName().equals(groupName)).collect(Collectors.toList());
				schedulers = schedulers.stream().filter(t -> t.getGourName().equals(groupName)).collect(Collectors.toList());
				actions = actions.stream().filter(t -> t.getGourName().equals(groupName)).collect(Collectors.toList());
			}

			JSONObject json = new JSONObject();

			json.put("threads", threads);
			json.put("schedulers", schedulers);
			json.put("actions", actions);

			return Restful.ok().obj(json);
		}
		return null ;
	}

	/**
	 * 停止一个运行的action
	 *
	 * 停止一个运行的action，或者task
	 * 
	 * @throws Exception
	 */
	@At
	public void stop(String key) throws Exception {
		try {
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
}
