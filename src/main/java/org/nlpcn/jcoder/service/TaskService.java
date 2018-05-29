package org.nlpcn.jcoder.service;

import com.alibaba.fastjson.JSONObject;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.apache.curator.framework.CuratorFramework;
import org.nlpcn.jcoder.constant.TaskStatus;
import org.nlpcn.jcoder.constant.TaskType;
import org.nlpcn.jcoder.domain.*;
import org.nlpcn.jcoder.domain.CodeInfo.ExecuteMethod;
import org.nlpcn.jcoder.filter.TestingFilter;
import org.nlpcn.jcoder.run.CodeException;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.scheduler.TaskRunManager;
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
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.nlpcn.jcoder.service.SharedSpaceService.GROUP_PATH;

@IocBean
public class TaskService {

	public static final String VERSION_SPLIT = "_";
	private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);
	private static final ConcurrentHashMap<Object, Task> TASK_MAP_CACHE = new ConcurrentHashMap<>();
	/**
	 * 记录task执行成功失败的计数器
	 */
	private static final Map<String, AtomicLong> taskSuccess = new ConcurrentHashMap<>();

	/**
	 * 记录task执行成功失败的计数器
	 */
	private static final Map<String, AtomicLong> taskErr = new ConcurrentHashMap<>();

	private BasicDao basicDao = StaticValue.systemDao;

	public static synchronized Task findTaskByCache(Long id) {
		return TASK_MAP_CACHE.get(id);
	}

	public static synchronized Task findTaskByCache(String groupName, String name) {
		return TASK_MAP_CACHE.get(makeKey(groupName, name));
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
	 * 查找出缓存中的所有task
	 *
	 * @return
	 */
	public static List<Task> findAllTasksByCache() {
		return TASK_MAP_CACHE.entrySet().stream().filter(e -> e.getKey() instanceof Long).map(e -> e.getValue()).collect(Collectors.toList());
	}

	/**
	 * 查找出缓存中的所有task
	 *
	 * @return
	 */
	public static List<Task> findAllTasksByCache(String groupName) {
		return findAllTasksByCache().stream().filter(t -> groupName.equals(t.getGroupName())).collect(Collectors.toList());
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
	 * 内部执行一个task 绕过请求，request为用户请求地址，只限于内部api使用
	 */
	public static <T> T executeTask(String className, String methodName, Map<String, Object> params) throws ExecutionException {
		return executeTask(StaticValue.getCurrentGroup(), className, methodName, params);
	}

	/**
	 * 内部执行一个task 绕过请求，request为用户请求地址，只限于内部api使用
	 */
	public static <T> T executeTask(String groupName, String className, String methodName, Map<String, Object> params) throws ExecutionException {
		Task task = findTaskByCache(groupName, className);
		if (task == null) {
			if (TestingFilter.methods != null) {
				LOG.info("use testing method to run ");
				return executeTaskByTest(groupName, className, methodName, params);
			}
			throw new ApiException(ApiException.NotFound, methodName + " not found");
		}
		task = new JavaRunner(task).compile().instance().getTask();

		ExecuteMethod method = task.codeInfo().getExecuteMethod(methodName);

		if (method == null) {
			throw new ApiException(ApiException.NotFound, methodName + "/" + methodName + " not found");
		}

		Object[] args = map2Args(params, method.getMethod());

		return (T) StaticValue.MAPPING.getOrCreateByUrl(groupName, className, methodName).getChain().getInvokeProcessor().executeByCache(task, method.getMethod(), args);
	}

	/**
	 * 通过test方式执行内部调用
	 */
	public static <T> T executeTaskByArgs(String groupName, String className, String methodName, Object... params) throws ExecutionException {
		Task task = findTaskByCache(groupName, className);
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

		return (T) StaticValue.MAPPING.getOrCreateByUrl(groupName, className, methodName).getChain().getInvokeProcessor().executeByCache(task, method.getMethod(), args);
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
	 */
	private static <T> T executeTaskByTest(String groupName, String className, String methodName, Map<String, Object> params) throws ApiException {
		KeyValue<Method, Object> kv = TestingFilter.methods.get(groupName + "/" + className + "/" + methodName);
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
	 */
	public static Object[] map2Args(Map<String, Object> params, Method method) {
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

	/**
	 * 计数器，记录task成功失败个数
	 */
	public static void counter(Task task, boolean success) {
		String groupTaskName = makeKey(task);
		if (success) {
			taskSuccess.compute(groupTaskName, (k, v) -> {
				if (v == null) {
					v = new AtomicLong();
				}
				v.incrementAndGet();
				return v;
			});
		} else {
			taskErr.compute(groupTaskName, (k, v) -> {
				if (v == null) {
					v = new AtomicLong();
				}
				v.incrementAndGet();
				return v;
			});
		}
	}

	/**
	 * 获得一个task成功次数
	 */
	public static long getSuccess(Task task) {
		AtomicLong atomicLong = taskSuccess.get(makeKey(task));
		if (atomicLong == null) {
			return 0L;
		} else {
			return atomicLong.get();
		}
	}

	/**
	 * 获得一个task失败次数
	 */
	public static long getError(Task task) {
		AtomicLong atomicLong = taskErr.get(makeKey(task));
		if (atomicLong == null) {
			return 0L;
		} else {
			return atomicLong.get();
		}
	}

	/**
	 * 清空计数器
	 *
	 * @param
	 */
	public static void clearSucessErr(Task task) {
		String groupTaskName = makeKey(task);
		taskErr.remove(groupTaskName);
		taskSuccess.remove(groupTaskName);
	}

	/**
	 * 构建task_cache 的key groupName_taskName
	 */
	private static String makeKey(Task task) {
		return task.getGroupName() + "/" + task.getName();
	}

	/**
	 * 构建task_cache 的key groupName_taskName
	 */
	private static String makeKey(String groupName, String taskName) {
		return groupName + "/" + taskName;
	}

	/**
	 * 根据分组名称获取所有Task
	 *
	 * @param groupName 组名
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
	 * 根据分组名称获取所有Task
	 *
	 * @param groupName 组名
	 */
	public Task getTaskFromCluster(String groupName, String taskName) {
		try {
			return StaticValue.space().getData(GROUP_PATH + "/" + groupName + "/" + taskName, Task.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 删除ZK集群里的Task
	 *
	 * @param groupName 组名
	 * @param taskName  任务名
	 */
	public void deleteTaskFromCluster(String groupName, String taskName) throws Exception {
		String path = GROUP_PATH + "/" + groupName + "/" + taskName;
		LOG.info("to delete task in zookeeper: {}", path);
		if (!existsInCluster(groupName, taskName)) {
			LOG.warn("task[{}] not found in zookeeper", path);
		} else {
			StaticValue.space().getZk().delete().forPath(path);
		}
	}

	public boolean existsInCluster(String groupName, String taskName) throws Exception {
		return StaticValue.space().getZk().checkExists().forPath(GROUP_PATH + "/" + groupName + "/" + taskName) != null;
	}

	/**
	 * 保存或者更新一个任务
	 */
	public boolean saveOrUpdate(Task task) throws Exception {
		// 历史库版本保存
		boolean isModify = checkTaskModify(task);

		String message = null;
		if ((validate(task)) != null) {
			throw new CodeException(message);
		}

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
	 * 验证一个taskcode是否正确
	 */
	public String validate(Task task) throws ParseException {

		String code = task.getCode();
		String name = null;
		String pk = null;

		CompilationUnit compile = JavaDocUtil.compile(code);

		pk = compile.getPackage().getPackageName();
		if (StringUtil.isBlank(pk)) {
			return ("package can not empty ");
		}

		List<TypeDeclaration> types = compile.getTypes();

		for (TypeDeclaration type : types) {
			if (type.getModifiers() == Modifier.PUBLIC) {
				if (name != null) {
					return "class not have more than one public class ";
				}
				name = type.getName();
			}
		}

		if (name == null) {
			return "not find className ";
		}

		return null;
	}

	/**
	 * 判断task代码是否修改过
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
				TASK_MAP_CACHE.remove(makeKey(oldTask));

				TASK_MAP_CACHE.put(newTask.getId(), newTask);
				TASK_MAP_CACHE.put(makeKey(newTask), newTask);

				clearSucessErr(oldTask);
				clearSucessErr(newTask);

				TaskRunManager.flush(oldTask, newTask);
			}
		}
	}

	/**
	 * 删除一个任务
	 */
	public void delete(Task task) throws Exception {
		task.setType(TaskType.RECYCLE.getValue());
		task.setStatus(TaskStatus.STOP.getValue());
		saveOrUpdate(task);
	}

	/**
	 * 彻底删除一个任务
	 */
	public void delByDB(Task task) {

		// 删除任务历史
		basicDao.delByCondition(TaskHistory.class, Cnd.where("taskId", "=", task.getId()));

		// 删除任务
		basicDao.delById(task.getId(), Task.class);

		//删除缓存中的
		TASK_MAP_CACHE.remove(task.getId());
		TASK_MAP_CACHE.remove(makeKey(task));
	}

	public Task findTask(String groupName, String name) {
		return basicDao.findByCondition(Task.class, Cnd.where("groupName", "=", groupName).and("name", "=", name));
	}

	public List<Task> findTasksByGroupName(String groupName) {
		LOG.info("find findTasksByGroupName from groupName: {}", groupName);
		return basicDao.search(Task.class, Cnd.where("groupName", "=", groupName));
	}

	/**
	 * 找到task根据groupName
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
	 */
	public synchronized void flushGroup(String groupName) {

		//查询出缓存中的所有task并移除
		List<Task> search = findAllTasksByCache(groupName);
		for (Task task : search) {
			try {
				task.codeInfo().getExecuteMethods().forEach(m -> {
					StaticValue.space().removeMapping(task.getGroupName(), task.getName(), m.getName());
				});
				StaticValue.MAPPING.remove(task.getGroupName(), task.getName());//删掉urlmapping重新加载
				TASK_MAP_CACHE.remove(task.getId());
				TASK_MAP_CACHE.remove(makeKey(task));
			} catch (Throwable e) {
				e.printStackTrace();
				LOG.error(e.getMessage(), e);
			}
		}

		search = findTasksByGroupName(groupName);

		for (Task task : search) {
			try {
				TASK_MAP_CACHE.put(task.getId(), task);
				TASK_MAP_CACHE.put(makeKey(task), task);
				try {
					new JavaRunner(task).compile();
					Collection<CodeInfo.ExecuteMethod> executeMethods = task.codeInfo().getExecuteMethods();
					executeMethods.forEach(e -> {
						StaticValue.space().addMapping(task.getGroupName(), task.getName(), e.getMethod().getName());
					});
				} catch (Exception e) {
					LOG.error("compile {}/{} err ", task.getGroupName(), task.getCode(), e);
				}
			} catch (Throwable e) {
				e.printStackTrace();
				LOG.error(e.getMessage(), e);
			}
		}
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


}
