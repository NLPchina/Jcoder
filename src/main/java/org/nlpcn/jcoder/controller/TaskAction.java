package org.nlpcn.jcoder.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import org.h2.util.DateTimeUtils;
import org.nlpcn.jcoder.constant.*;
import org.nlpcn.jcoder.domain.HostGroup;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.domain.TaskStatistics;
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
import org.nutz.lang.Lang;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.adaptor.WhaleAdaptor;
import org.nutz.mvc.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	 * 获得每个任务对应的主机列表
	 *
	 * @param groupName 组名
	 * @param names     任务名数组
	 * @param taskType
	 * @return
	 * @throws Exception
	 */
	@At("/host/list")
	public Restful hostList(String groupName, @Param("names[]") String[] names, int taskType) throws Exception {
		if (StringUtil.isBlank(groupName)) {
			throw new IllegalArgumentException("empty groupName");
		}

		if (names == null || names.length < 1) {
			throw new IllegalArgumentException("empty names");
		}

		// 获取有这个组的所有主机
		Map<String, HostGroup> m = groupService.getGroupHostList(groupName).stream().collect(Collectors.toMap(HostGroup::getHostPort, Function.identity()));

		// 询问主机是否有这个任务
		Set<String> hosts = m.keySet();
		Map<String, Object[]> results = new HashMap<>(names.length);
		for (String name : names) {
			results.put(name, hosts.parallelStream().filter(h -> {
				String content;
				try {
					content = proxyService.post(h, Api.TASK_TASK.getPath(), ImmutableMap.of("groupName", groupName, "name", name), TIMEOUT).getContent();
				} catch (Exception e) {
					throw Lang.wrapThrow(e);
				}

				JSONObject t = JSON.parseObject(content).getJSONObject("obj");
				return t != null && Objects.equals(t.getInteger("type"), taskType);
			}).map(m::get).toArray());
		}

		return Restful.instance(results);
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
		if (hosts == null || hosts.length < 1) {
			throw new IllegalArgumentException("empty hosts");
		}

		if (task == null) {
			throw new IllegalArgumentException("task is null");
		}

		if (StringUtil.isBlank(task.getName()) || StringUtil.isBlank(task.getCode())) {
			throw new IllegalArgumentException("task name or code is empty");
		}

		// 是否更新主版本
		Set<String> hostPorts = new HashSet<>(Arrays.asList(hosts));
		boolean containsMaster = hostPorts.remove(Constants.HOST_MASTER);

		// 如果激活任务, 需要检查代码
		if (task.getStatus() == TaskStatus.ACTIVE.getValue()) {
			String errorMessage = proxyService.post(hostPorts, Api.TASK_CHECK.getPath(), ImmutableMap.of("task", JSON.toJSONString(task)), TIMEOUT, MERGE_FALSE_MESSAGE_CALLBACK);
			if (StringUtil.isNotBlank(errorMessage)) {
				return Restful.fail().code(ServerException).msg(errorMessage);
			}
		}

		// 保存
		User u = (User) Mvcs.getHttpSession().getAttribute(UserConstants.USER);
		Date now = new Date();
		if (task.getId() == null) {
			task.setCreateUser(u.getName());
			task.setCreateTime(now);
		}
		task.setUpdateUser(u.getName());
		task.setUpdateTime(now);

		// 集群的每台机器保存
		String errorMessage = proxyService.post(hostPorts, Api.TASK_SAVE.getPath(), ImmutableMap.of("task", JSON.toJSONString(task)), TIMEOUT, MERGE_FALSE_MESSAGE_CALLBACK);
		if (StringUtil.isNotBlank(errorMessage)) {
			return Restful.fail().code(ServerException).msg(errorMessage);
		}

		// 如果更新主版本
		if (containsMaster) {
			// 给任务设置ID值, 方便后续做编辑
			task.setId(Constants.MASTER_TASK_ID);

			// ZK保存
			StaticValue.space().addTask(task);
		}

		return Restful.ok();
	}

	@At
	public Restful __check__(@Param("::task") Task task) throws CodeException, IOException {
		if (task.getStatus() == TaskStatus.ACTIVE.getValue()) {
			new JavaRunner(task).check();
		}
		return Restful.ok();
	}

	@At
	public Restful __save__(@Param("::task") Task task) throws Exception {
		// 如果是编辑, 得替换成本地任务ID
		if (task.getId() != null) {
			Task t = taskService.findTask(task.getGroupName(), task.getName());
			if (t != null) {
				task.setId(t.getId());
			} else {
				if (task.getId() == Constants.MASTER_TASK_ID) {
					// 如果将主版本的任务保存到本地
					task.setId(null);
					task.setCreateUser(task.getUpdateUser());
					task.setCreateTime(task.getUpdateTime());
				} else {
					// 如果本地没有该任务, 则无法执行更新操作
					throw new IllegalStateException("can't update null task[" + task.getGroupName() + "-" + task.getName() + "]");
				}
			}
		}
		LOG.info("to save task[{}-{}]: {}", task.getGroupName(), task.getName(), taskService.saveOrUpdate(task));
		return Restful.ok();
	}

	/**
	 * 获取任务
	 *
	 * @param groupName  组名
	 * @param name       任务名
	 * @param sourceHost 来源主机, 即获取哪个主机的任务, 默认主版本
	 * @return
	 * @throws Exception
	 */
	@At
	@AdaptBy(type = WhaleAdaptor.class)
	public Restful task(String groupName, String name, @Param(value = "sourceHost", df = Constants.HOST_MASTER) String sourceHost) throws Exception {
		if (StringUtil.isBlank(groupName)) {
			throw new IllegalArgumentException("empty groupName");
		}

		if (StringUtil.isBlank(name)) {
			throw new IllegalArgumentException("empty name");
		}

		// 如果取主版本, 直接访问ZK
		if (Constants.HOST_MASTER.equals(sourceHost)) {
			Optional<Task> opt = taskService.getTasksByGroupNameFromCluster(groupName).stream().filter(t -> Objects.equals(t.getName(), name)).findAny();
			if (!opt.isPresent()) {
				LOG.warn("task[{}-{}] not found in zookeeper", groupName, name);
				return Restful.ok();
			}

			return Restful.ok().obj(opt.get());
		}

		// 如果取其他机器版本
		String content = proxyService.post(sourceHost, Api.TASK_TASK.getPath(), ImmutableMap.of("groupName", groupName, "name", name), TIMEOUT).getContent();
		JSONObject res = JSONObject.parseObject(content);
		return res.getBooleanValue("ok") ? Restful.ok().obj(res.get("obj")) : Restful.fail().code(ServerException).msg(res.getString("message"));
	}

	@At
	public Restful __task__(String groupName, String name) {
		// 从本地数据库中找到任务
		Task t = taskService.findTask(groupName, name);
		if (t == null) {
			LOG.warn("task[{}-{}] not found in host[{}]", groupName, name, StaticValue.getHostPort());
			return Restful.ok();
		}

		return Restful.ok().obj(t);
	}

	/**
	 * 删除任务, 支持删除单台机器和所有机器(包括ZK)
	 * 如果任务类型是垃圾, 就物理删除
	 * 如果任务类型不是垃圾, 就更改任务类型和状态
	 *
	 * @param host      主机, 如果有, 就删除指定机器, 否则删除所有
	 * @param groupName
	 * @param name
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@At
	public Restful delete(String host, String groupName, String name, int type) throws Exception {
		if (StringUtil.isBlank(groupName)) {
			throw new IllegalArgumentException("empty groupName");
		}

		if (StringUtil.isBlank(name)) {
			throw new IllegalArgumentException("empty name");
		}

		User u = (User) Mvcs.getHttpSession().getAttribute(UserConstants.USER);
		Date now = new Date();

		// 主机列表
		boolean isSingle = StringUtil.isNotBlank(host);
		String[] hosts = isSingle ? new String[]{host} : groupService.getGroupHostList(groupName).stream().map(HostGroup::getHostPort).toArray(String[]::new);

		if (type == TaskType.RECYCLE.getValue()) {
			// 如果是垃圾, 物理删除
			// 删除集群的每台机器
			String errorMessage = proxyService.post(hosts, Api.TASK_DELETE.getPath(), ImmutableMap.of("force", true, "groupName", groupName, "name", name, "user", u.getName(), "time", now.getTime()), TIMEOUT, MERGE_FALSE_MESSAGE_CALLBACK);
			if (StringUtil.isNotBlank(errorMessage)) {
				return Restful.fail().code(ServerException).msg(errorMessage);
			}

			if (!isSingle) {
				// 如果删除所有的, 删ZK
				taskService.deleteTaskFromCluster(groupName, name);
			}
		} else {
			// 更改类型为垃圾, 并停用
			// 更新集群的每台机器
			String errorMessage = proxyService.post(hosts, Api.TASK_DELETE.getPath(), ImmutableMap.of("groupName", groupName, "name", name, "user", u.getName(), "time", now.getTime()), TIMEOUT, MERGE_FALSE_MESSAGE_CALLBACK);
			if (StringUtil.isNotBlank(errorMessage)) {
				return Restful.fail().code(ServerException).msg(errorMessage);
			}

			if (!isSingle) {
				// 如果更新所有的, 更新ZK
				Task task = taskService.getTasksByGroupNameFromCluster(groupName).parallelStream().filter(t -> Objects.equals(t.getName(), name)).findAny().get();
				task.setUpdateUser(u.getName());
				task.setUpdateTime(now);
				task.setType(TaskType.RECYCLE.getValue());
				task.setStatus(TaskStatus.STOP.getValue());
				StaticValue.space().addTask(task);
			}
		}

		return Restful.ok();
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
		if (t == null) {
			LOG.warn("task[{}-{}] not found in host[{}]", groupName, name, StaticValue.getHostPort());
			return Restful.ok();
		}

		t.setUpdateUser(user);
		t.setUpdateTime(time);
		taskService.delete(t);

		// 如果强制删除, 删除数据库中任务
		if (force) {
			taskService.delByDB(t);
		}

		return Restful.ok();
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


	/**
	 * 传入多个主机获取主机们的成功失败数字
	 *
	 * @param hostPorts
	 * @return
	 */
	@At
	public Restful statistics(@Param("groupName") String groupName, @Param("name") String name, @Param(value = "first", df = "true") boolean first) throws Exception {
		if (!first) {
			long success = 0;
			long error = 0;
			Task taskByCache = TaskService.findTaskByCache(name);
			if (taskByCache != null) {
				success = taskByCache.success();
				error = taskByCache.error();
			}

			return Restful.ok().msg(success + "_" + error);
		} else {

			List<HostGroup> groupHostList = groupService.getGroupHostList(groupName);

			String[] hostPorts = groupHostList.stream().map(gh -> gh.getHostPort()).toArray(String[]::new);

			Map<String, String> map = proxyService.post(hostPorts, "/admin/task/statistics", ImmutableMap.of("groupName", groupName, "name", name, "first", false), 1000);

			List<TaskStatistics> result = new ArrayList<>();

			JSONObject jsonObject = null ;
			for (HostGroup hostGroup : groupHostList) {
				String content = map.get(hostGroup.getHostPort());
				try{
					jsonObject = JSONObject.parseObject(content);
					if(!jsonObject.getBoolean("ok")){
						continue;
					}
				}catch (Exception e){
					continue;
				}

				String[] split = jsonObject.getString("message").split("_");

				TaskStatistics ts = new TaskStatistics();
				ts.setHostPort(hostGroup.getHostPort());
				ts.setSuccess(Long.parseLong(split[0]));
				ts.setError(Long.parseLong(split[1]));
				ts.setWeight(hostGroup.getWeight());
				ts.setHostGroup(hostGroup);
				result.add(ts);
			}

			final long sum = result.stream().mapToLong(t -> t.getWeight()).sum();
			result.forEach(t -> t.setSumWeight(sum));

			TaskStatistics master = new TaskStatistics() ;
			master.setSumWeight(sum);
			master.setWeight(sum);
			master.setError(result.stream().mapToLong(t->t.getError()).sum());
			master.setSuccess(result.stream().mapToLong(t->t.getSuccess()).sum());
			master.setHostPort("master");

			result.add(0,master);

			return Restful.ok().obj(result);
		}
	}
}
