package org.nlpcn.jcoder.service;

import org.nlpcn.jcoder.domain.CodeInfo.ExecuteMethod;
import org.nlpcn.jcoder.domain.*;
import org.nlpcn.jcoder.filter.TestingFilter;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.scheduler.TaskException;
import org.nlpcn.jcoder.scheduler.ThreadManager;
import org.nlpcn.jcoder.util.*;
import org.nlpcn.jcoder.util.dao.BasicDao;
import org.nutz.castor.Castors;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.annotation.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@IocBean
public class TaskService {

	private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

	private static final ConcurrentHashMap<Object, Task> TASK_MAP_CACHE = new ConcurrentHashMap<>();
	public static final String VERSION_SPLIT = "_";

	private BasicDao basicDao = StaticValue.systemDao;

	/**
	 * 保存或者更新一个任务
	 *
	 * @param task
	 * @throws Exception
	 */
	public boolean saveOrUpdate(Task task, Long groupId) throws Exception {

		// 进行权限认证
		authEditorValidate(groupId);
		authEditorValidate(task.getGroupId());

		if (task.getStatus() == 1) {// check code throw Exception
			new JavaRunner(task).check();
		}

		checkTask(task);

		HttpSession session = Mvcs.getHttpSession();
		String userName = session.getAttribute("user").toString();
		Date date = new Date();
		task.setUpdateTime(date);
		task.setUpdateUser(userName);

		// 历史库版本保存
		boolean isModify = checkTaskModify(task);

		if (isModify) {
			String version = generateVersion(task);
			task.setVersion(version);
		}

		if (task.getId() == null) {
			task.setCreateTime(date);
			task.setCreateUser(userName);
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

	public void checkTask(Task task) throws Exception {
		if (task == null) {
			throw new Exception("task is null!");
		} else if (StringUtil.isBlank(task.getName())) {
			throw new Exception("task is name null or empty!");
		} else if (StringUtil.isBlank(task.getDescription())) {
			throw new Exception("task is description null or empty!");
		} else if (StringUtil.isBlank(task.getCode())) {
			throw new Exception("task is code null or empty!");
		} else if (TASK_MAP_CACHE.contains(task.getName())) {
			if (!TASK_MAP_CACHE.get(task.getName()).getId().equals(task.getId())) {
				throw new Exception("task name is unique !");
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
		authEditorValidate(task.getGroupId());
		task.setType(0);
		task.setStatus(0);
		saveOrUpdate(task, task.getGroupId());
	}

	/**
	 * 删除一个历史任务
	 *
	 * @param task
	 * @throws Exception
	 */
	public void delete(TaskHistory task) throws Exception {
		authEditorValidate(task.getGroupId());
		basicDao.delById(task.getId(), TaskHistory.class);
	}

	/**
	 * 彻底删除一个任务
	 *
	 * @param task
	 * @throws Exception
	 */
	public void delByDB(Task task) throws Exception {
		authEditorValidate(task.getGroupId());
		basicDao.delByCondition(TaskHistory.class, Cnd.where("taskId", "=", task.getId()));//delete history
		basicDao.delById(task.getId(), Task.class); // 不需要通知队列了
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
	 * @throws TaskException
	 */
	public void initTaskFromDB(String groupName) throws TaskException {
		Group group = this.basicDao.findByCondition(Group.class, Cnd.where("name", "=", groupName));
		List<Task> search = this.basicDao.search(Task.class, Cnd.where("groupId", "=", group.getId()));
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
	 * 根据组id获得task集合
	 *
	 * @param groupId
	 * @return
	 */
	public List<Task> tasksList(Long groupId) {
		if (groupId == null) {
			return null;
		}
		return basicDao.search(Task.class, Cnd.where("groupId", "=", groupId));
	}

	/**
	 * 编辑task权限验证
	 *
	 * @param groupId
	 * @throws Exception
	 */
	private void authEditorValidate(Long groupId) throws Exception {
		if ((Integer) Mvcs.getHttpSession().getAttribute("userType") == 1) {
			return;
		}
		UserGroup ug = basicDao.findByCondition(UserGroup.class, Cnd.where("groupId", "=", groupId).and("userId", "=", Mvcs.getHttpSession().getAttribute("userId")));
		if (ug == null || ug.getAuth() != 2) {
			throw new Exception("not have editor auth in groupId:" + groupId);
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

	// 生成任务的版本号
	private static String generateVersion(Task t) {
		StringBuilder sb = new StringBuilder();
		sb.append(t.getUpdateUser());
		sb.append(VERSION_SPLIT);
		sb.append(DateUtils.formatDate(t.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
		return sb.toString();
	}

	public List<Task> getTasks(int... ids) {
		return basicDao.searchByIds(Task.class, ids, "name");
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
