package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.curator.framework.CuratorFramework;
import org.nlpcn.jcoder.constant.TaskStatus;
import org.nlpcn.jcoder.constant.TaskType;
import org.nlpcn.jcoder.domain.CodeInfo.ExecuteMethod;
import org.nlpcn.jcoder.domain.*;
import org.nlpcn.jcoder.filter.TestingFilter;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.util.*;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.castor.Castors;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static org.nlpcn.jcoder.service.SharedSpaceService.GROUP_PATH;

@IocBean
public class TaskService {

	private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

	private static final ConcurrentHashMap<Object, Task> TASK_MAP_CACHE = new ConcurrentHashMap<>();
	public static final String VERSION_SPLIT = "_";

	private BasicDao basicDao = StaticValue.systemDao;

	/**
	 * 根据分组名称获取所有Task
	 *
	 * @param groupName 组名
	 * @return
	 * @throws Exception
	 */
    public List<Task> getTasksByGroupNameFromCluster(String groupName) throws Exception {
        CuratorFramework zk = StaticValue.space().getZk();
        String path = GROUP_PATH + "/" + groupName;
        List<String> taskNames = zk.getChildren().forPath(path);
        List<Task> tasks = new ArrayList<>(taskNames.size());
        Task t;
        for (String name : taskNames) {
			t = JSONObject.parseObject(zk.getData().forPath(path + "/" + name), Task.class);
			if (t != null && StringUtil.isNotBlank(t.getCode())) {
				tasks.add(t);
			}
		}
		return tasks;
	}

    /**
     * 删除ZK集群里的Task
     *
     * @param groupName 组名
     * @param taskName  任务名
     * @throws Exception
     */
    public void deleteTaskFromCluster(String groupName, String taskName) throws Exception {
        String path = GROUP_PATH + "/" + groupName + "/" + taskName;
        LOG.info("to delete task in zookeeper: {}", path);
        StaticValue.space().getZk().delete().forPath(path);
    }

	/**
	 * 保存或者更新一个任务
	 *
	 * @param task
	 * @throws Exception
	 */
	public boolean saveOrUpdate(Task task) throws Exception {
		// 历史库版本保存
		boolean isModify = checkTaskModify(task);

		if (isModify) {
			String version = generateVersion(task);
			task.setVersion(version);
		}

		if (task.getId() == null) {
			task = basicDao.save(task);
		} else {
			basicDao.update(task);
		}

		if (isModify) {
			basicDao.save(new TaskHistory(task));
		}

		flush(task.getId());

		return isModify;
	}

	/**
	 * 判断task代码是否修改过
	 *
	 * @param task
	 * @return
	 */
	private boolean checkTaskModify(Task task) {
		Long id = task.getId();
		if (id == null) {
			return true;
		}
		Task t = basicDao.find(id, Task.class);
		if (t == null) {
			return true;
		}
		if (!t.getCode().equals(task.getCode())) {
			return true;
		}
		return false;
	}

	/**
	 * 刷新某个task
	 *
	 * @throws Exception
	 */
	public void flush(Long id) throws Exception {

		Task oldTask = TASK_MAP_CACHE.get(id);

		// 查找处新的task
		Task newTask = this.basicDao.find(id, Task.class);

		Task temp = new Task();
		temp.setId(0L);
		temp.setName("");

		if (oldTask == null) {
			oldTask = temp;
		}
		if (newTask == null) {
			newTask = temp;
		}

		synchronized (oldTask) {
			synchronized (newTask) {
				TASK_MAP_CACHE.remove(oldTask.getId());
				TASK_MAP_CACHE.remove(oldTask.getName());

				TASK_MAP_CACHE.put(newTask.getId(), newTask);
				TASK_MAP_CACHE.put(newTask.getName(), newTask);

				ThreadManager.flush(oldTask, newTask);
			}
		}
	}

    /**
     * 删除一个任务
     *
     * @param task
     * @throws Exception
     */
    public void delete(Task task) throws Exception {
        task.setType(TaskType.RECYCLE.getValue());
        task.setStatus(TaskStatus.STOP.getValue());
        saveOrUpdate(task);
    }

    /**
     * 彻底删除一个任务
     *
     * @param task
     * @throws Exception
     */
    public void delByDB(Task task) {
        // 删除任务历史
        basicDao.delByCondition(TaskHistory.class, Cnd.where("taskId", "=", task.getId()));

        // 删除任务
        basicDao.delById(task.getId(), Task.class);
    }

    public Task findTask(String groupName, String name) {
        return basicDao.findByCondition(Task.class, Cnd.where("groupName", "=", groupName).and("name", "=", name));
    }

    public List<Task> findTasksByGroupName(String groupName) {
        if (groupName == null) {
            return null;
        }

        return basicDao.search(Task.class, Cnd.where("groupName", "=", groupName));
    }

	/**
	 * 找到task根据groupName
	 *
	 * @param groupName
	 * @return
	 */
	public LinkedHashSet<Task> findTaskByGroupNameCache(String groupName) {
		Collection<Task> values = TASK_MAP_CACHE.values();

		LinkedHashSet<Task> result = new LinkedHashSet<>();
		for (Task task : values) {
			if (groupName.equals(task.getGroupName())) {
				result.add(task);
			}
		}
		return result;
	}

    /**
     * 从数据库中init所有的task
     *
     * @param groupName
     */
    public void initTaskFromDB(String groupName) {
        List<Task> search = findTasksByGroupName(groupName);
        flushTaskMappingAndCache(search);
    }

	/**
	 * 刷新传入的tasks mapping and cache
	 *
	 * @param tasks
	 */
	private void flushTaskMappingAndCache(List<Task> tasks) {
		for (Task task : tasks) {
			try {
				TASK_MAP_CACHE.put(task.getId(), task);
				TASK_MAP_CACHE.put(task.getName(), task);
				StaticValue.MAPPING.remove(task.getName());//删掉urlmapping重新加载
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(e.getMessage(), e);
			}
		}
	}

	public static synchronized Task findTaskByCache(Long id) {
		return TASK_MAP_CACHE.get(id);
	}

	public static synchronized Task findTaskByCache(String name) {
		return TASK_MAP_CACHE.get(name);
	}


	public static synchronized Task findTaskByDB(Long id) {
		LOG.info("find task by db!");
		return StaticValue.systemDao.findByCondition(Task.class, Cnd.where("id", "=", id));
	}

	public static synchronized TaskHistory findTaskByDBHistory(Long taskId, String version) {
		LOG.info("find task by dbHistory!");
		return StaticValue.systemDao.findByCondition(TaskHistory.class, Cnd.where("version", "=", version).and("taskId", "=", taskId));
	}

	public static List<TaskHistory> findDBHistory() {
		return StaticValue.systemDao.search(TaskHistory.class, Cnd.NEW());
	}

	/**
	 * 根据类型查找task集合
	 *
	 * @param type
	 * @return
	 */
	public static synchronized Collection<Task> findTaskList(Integer type) {
		Collection<Task> values = TASK_MAP_CACHE.values();
		if (type == null) {
			return values;
		}

		LinkedHashSet<Task> result = new LinkedHashSet<>();
		for (Task task : values) {
			if (type.equals(task.getType())) {
				result.add(task);
			}
		}
		return result;
	}

	/**
	 * @param taskId
	 * @param size
	 * @return
	 */
	public List<String> versions(Long taskId, int size) {
		List<String> list = new ArrayList<>();
		Condition cnd = Cnd.where("taskId", "=", taskId).desc("id");
		List<TaskHistory> tasks = basicDao.search(TaskHistory.class, cnd);
		for (TaskHistory taskHistory : tasks) {
			list.add(taskHistory.getVersion());
			if (size-- == 0) {
				break;
			}
		}
		return list;
	}

	// 生成任务的版本号
	private static String generateVersion(Task t) {
		StringBuilder sb = new StringBuilder();
		sb.append(t.getUpdateUser());
		sb.append(VERSION_SPLIT);
		sb.append(DateUtils.formatDate(t.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
		return sb.toString();
	}

	/**
	 * 检查所有的task
	 *
	 * @throws Exception
	 */
	public void checkAllTask() throws Exception {
		// 获得当前运行的任务
		List<Task> search = StaticValue.systemDao.search(Task.class, "id");

		// 线程任务
		List<TaskInfo> threads = ThreadManager.getAllThread();

		MapCount<String> mc = new MapCount<>();

		threads.forEach(ti -> mc.add(ti.getTaskName()));

		for (Task task : search) {
			// 检查while的task是否活着
			if (task.getStatus() == 1 && "while".equalsIgnoreCase(task.getScheduleStr())) {
				Double num = mc.get().get(task.getName());
				if (num == null || num < 1) {
					LOG.warn(task.getName() + " is while task , not find in threads , now to start it! ");
					this.flush(task.getId());
				}
			}
			// stop的task是否活着
			if (task.getStatus() == 0) {
				// 如果不是1 那么不正常，全局刷新
				if (mc.get().containsKey(task.getName())) {
					LOG.warn(task.getName() + " is stop task , but it is runing, now sotp it ! ");
					this.flush(task.getId());
				}
			}
		}
	}

	/**
	 * 内部执行一个task 绕过请求，request为用户请求地址，只限于内部api使用
	 *
	 * @param className
	 * @param methodName
	 * @param params
	 * @return
	 * @throws ExecutionException
	 */
	public static <T> T executeTask(String className, String methodName, Map<String, Object> params) throws ExecutionException {
		Task task = findTaskByCache(className);
		if (task == null) {
			if (TestingFilter.methods != null) {
				LOG.info("use testing method to run ");
				return executeTaskByTest(className, methodName, params);
			}
			throw new ApiException(ApiException.NotFound, methodName + " not found");
		}
		task = new JavaRunner(task).compile().instance().getTask();

		ExecuteMethod method = task.codeInfo().getExecuteMethod(methodName);

		if (method == null) {
			throw new ApiException(ApiException.NotFound, methodName + "/" + methodName + " not found");
		}

		Object[] args = map2Args(params, method.getMethod());

		return (T) StaticValue.MAPPING.getOrCreateByUrl(className, methodName).getChain().getInvokeProcessor().executeByCache(task, method.getMethod(), args);
	}


	/**
	 * 通过test方式执行内部调用
	 *
	 * @param className
	 * @param methodName
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	public static <T> T executeTaskByArgs(String className, String methodName, Object... params) throws ExecutionException {
		Task task = findTaskByCache(className);
		if (task == null) {
			if (TestingFilter.methods != null) {
				LOG.info("use testing method to run ");
				return executeTaskByArgsByTest(className, methodName, params);
			}
			throw new ApiException(ApiException.NotFound, methodName + " not found");
		}
		task = new JavaRunner(task).compile().instance().getTask();

		ExecuteMethod method = task.codeInfo().getExecuteMethod(methodName);

		if (method == null) {
			throw new ApiException(ApiException.NotFound, methodName + "/" + methodName + " not found");
		}

		Object[] args = array2Args(method.getMethod(), params);

		return (T) StaticValue.MAPPING.getOrCreateByUrl(className, methodName).getChain().getInvokeProcessor().executeByCache(task, method.getMethod(), args);
	}

	private static <T> T executeTaskByArgsByTest(String className, String methodName, Object[] params) throws ApiException {
		KeyValue<Method, Object> kv = TestingFilter.methods.get(className + "/" + methodName);
		if (kv == null) {
			throw new ApiException(ApiException.NotFound, methodName + " not found");
		}
		Object[] args = array2Args(kv.getKey(), params);

		try {
			return (T) kv.getKey().invoke(kv.getValue(), args);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ApiException(500, e.getMessage());
		}
	}


	/**
	 * 通过test方式执行内部调用
	 *
	 * @param className
	 * @param methodName
	 * @param params
	 * @return
	 * @throws ApiException
	 */
	private static <T> T executeTaskByTest(String className, String methodName, Map<String, Object> params) throws ApiException {
		KeyValue<Method, Object> kv = TestingFilter.methods.get(className + "/" + methodName);
		if (kv == null) {
			throw new ApiException(ApiException.NotFound, methodName + " not found");
		}
		Object[] args = map2Args(params, kv.getKey());

		try {
			return (T) kv.getKey().invoke(kv.getValue(), args);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ApiException(500, e.getMessage());
		}
	}

	/**
	 * 将map转换为参数
	 *
	 * @param params
	 * @param method
	 * @return
	 */
	private static Object[] map2Args(Map<String, Object> params, Method method) {
		Parameter[] parameters = method.getParameters();

		Object[] args = new Object[parameters.length];

		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			String name = parameter.getName();

			if (!params.containsKey(name)) {
				Param annotation = parameter.getAnnotation(Param.class);
				if (annotation != null) {
					name = annotation.value();
				}
			}

			args[i] = Castors.me().castTo(params.get(name), parameter.getType());
		}
		return args;
	}

	/**
	 * 将对象数组转换为参数
	 *
	 * @param method
	 * @param params
	 * @return
	 */
	private static Object[] array2Args(Method method, Object... params) {
		Parameter[] parameters = method.getParameters();

		Object[] args = new Object[parameters.length];

		if (args.length != params.length) {
			throw new IllegalArgumentException("args.length " + args.length + " not equal params.length " + params.length);
		}
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			args[i] = Castors.me().castTo(params[i], parameter.getType());
		}
		return args;
	}

}
