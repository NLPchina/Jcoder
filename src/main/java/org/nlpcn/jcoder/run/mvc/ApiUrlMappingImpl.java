package org.nlpcn.jcoder.run.mvc;

import org.nlpcn.jcoder.domain.CodeInfo;
import org.nlpcn.jcoder.domain.CodeInfo.ExecuteMethod;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.run.mvc.processor.ApiActionInvoker;
import org.nlpcn.jcoder.service.TaskService;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.http.Http;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.*;
import org.nutz.mvc.annotation.BlankAtException;
import org.nutz.mvc.impl.ActionInvoker;
import org.nutz.mvc.impl.Loadings;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ApiUrlMappingImpl implements UrlMapping {

	private static final Log log = Logs.get();

	protected Map<String, ApiActionInvoker> map;

	private Map<String, Lock> lockMap = new HashMap<>();

	public ApiUrlMappingImpl() {
		this.map = new ConcurrentHashMap<String, ApiActionInvoker>();
	}

	public void add(ActionChainMaker maker, ActionInfo ai, NutConfig config) {

		// 检查所有的path
		String[] paths = ai.getPaths();
		for (int i = 0; i < paths.length; i++) {
			String path = paths[i];
			if (Strings.isBlank(path))
				throw new BlankAtException(ai.getModuleType(), ai.getMethod());

			if (path.charAt(0) != '/')
				paths[i] = '/' + path;
		}

		ActionChain chain = maker.eval(config, ai);

		for (String path : ai.getPaths()) {

			// 尝试获取，看看有没有创建过这个 URL 调用者
			ApiActionInvoker invoker = map.get(path);

			// 如果没有增加过这个 URL 的调用者，为其创建备忘记录，并加入索引
			if (null == invoker) {
				invoker = new ApiActionInvoker();
				map.put(path, invoker);
				// 记录一下方法与 url 的映射
				config.getAtMap().addMethod(path, ai.getMethod());
			}

			// 将动作链，根据特殊的 HTTP 方法，保存到调用者内部
			invoker.setDefaultChain(chain);

		}

		printActionMapping(ai);

		// TODO 下面个IF要不要转换到NutLoading中去呢?
		// 记录一个 @At.key
		if (!Strings.isBlank(ai.getPathKey()))
			config.getAtMap().add(ai.getPathKey(), ai.getPaths()[0]);
	}

	@Override
	public ActionInvoker get(ActionContext ac) {
		throw new RuntimeException(this.getClass() + " not support this function ! ");
	}

	public ApiActionInvoker getOrCreate(NutConfig config, ActionContext ac) {
		RequestPath rp = Mvcs.getRequestPathObject(ac.getRequest());

		String path = rp.getPath();

		int len = path.length();

		if (path.charAt(len - 1) == '/') {
			path = path.substring(0, len - 1);
		}

		ac.setSuffix(rp.getSuffix());

		ApiActionInvoker invoker = getOrCreate(config, path);

		if (invoker != null) {
			setActionSomeArgs(ac, path);
			ActionChain chain = invoker.getActionChain(ac);
			if (chain != null) {
				if (log.isDebugEnabled()) {
					log.debugf("Found mapping for [%s] path=%s : %s", ac.getRequest().getMethod(), path, chain);
				}
				return invoker;
			}
		}
		if (log.isDebugEnabled())
			log.debugf("Search mapping for path=%s : NOT Action match", path);
		return null;
	}

	/**
	 * 获得一个ApiActionInvoker如果不存在则创建一个
	 *
	 * @param config
	 * @param path
	 * @return
	 */
	private ApiActionInvoker getOrCreate(NutConfig config, String path) {
		ApiActionInvoker invoker = map.get(path);

		if (invoker == null) {
			Lock lock = null;
			synchronized (this) {
				lock = lockMap.getOrDefault(path, new ReentrantLock());
			}
			lock.lock();
			try {
				if ((invoker = map.get(path)) == null) {
					invoker = createInvoker(config, path);
				}
			} finally {
				lock.unlock();
			}
		}
		return invoker;
	}

	private void setActionSomeArgs(ActionContext ac, String path) {
		ac.setPath(path);
		ac.setPathArgs(new LinkedList<String>());
	}

	private ApiActionInvoker createInvoker(NutConfig config, String path) {
		String[] split = path.split("/");
		Task task = TaskService.findTaskByCache(split[2]);

		if (task != null && task.getStatus() == 1 && task.getType() == 1) {

			CodeInfo codeInfo = new JavaRunner(task).compile().instance().getTask().codeInfo();

			ApiActionChainMaker aacm = null;

			if (config != null) {
				aacm = Loadings.evalObj(config, ApiActionChainMaker.class, new String[]{});
			} else {
				aacm = new ApiActionChainMaker();
			}

			Class<?> module = codeInfo.getClassz();

			ExecuteMethod dm = codeInfo.getDefaultMethod();

			for (ExecuteMethod method : codeInfo.getExecuteMethods()) {
				// 增加到映射中
				ActionInfo info = ApiLoadings.createInfo(method.getMethod());

				info.setModuleType(module);

				if (dm == method) {
					info.setPaths(new String[]{"/api/" + task.getName() + "/" + method.getName(), "/api" + "/" + task.getName()});
				} else {
					info.setPaths(new String[]{"/api/" + task.getName() + "/" + method.getName()});
				}

				this.add(aacm, info, config);
			}
		}

		return map.get(path);
	}

	/**
	 * 从映射表中删除一个api
	 */
	public void remove(String className) {
		Iterator<Entry<String, ApiActionInvoker>> iterator = map.entrySet().iterator();
		String path;
		Task task = TaskService.findTaskByCache(className) ;
		synchronized (map) {
			while (iterator.hasNext()) {
				if ((path = iterator.next().getKey()).startsWith("/api/" + task.getName() + "/") || path.equals("/api/" + task.getName())) {
					iterator.remove();
					log.info("remove api " + path);
				}
			}
		}
	}

	/**
	 * 通过一个地址获取ActionInvoker
	 *
	 * @param className
	 * @param methodName
	 * @return
	 */
	public ApiActionInvoker getOrCreateByUrl(String className, String methodName) {
		String path = "/api/" + className + "/" + methodName;
		ApiActionInvoker apiActionInvoker = map.get(path);

		if (apiActionInvoker == null) { // 调用http接口进行渲染填充map
			//TODO: 这里可能不需要一次http请求
			Http.get("http://127.0.0.1:" + System.getProperty(StaticValue.PREFIX + "port") + path + "?_rpc_init=true");
			apiActionInvoker = map.get(path);
		}

		return apiActionInvoker;
	}

	/**
	 * 删除全部访问地址,相当于全站reload
	 */
	public void removeAll() {
		map.clear();
	}

	@Override
	public void add(String path, ActionInvoker invoker) {
		throw new RuntimeException(this.getClass() + " not support this function ! ");
	}

	protected void printActionMapping(ActionInfo ai) {
		/*
		 * 打印基本调试信息
		 */
		if (log.isDebugEnabled()) {
			// 打印路径
			String[] paths = ai.getPaths();
			StringBuilder sb = new StringBuilder();
			if (null != paths && paths.length > 0) {
				sb.append("   '").append(paths[0]).append("'");
				for (int i = 1; i < paths.length; i++)
					sb.append(", '").append(paths[i]).append("'");
			} else {
				throw Lang.impossible();
			}
			// 打印方法名
			Method method = ai.getMethod();
			String str;
			if (null != method)
				str = String.format("%-30s : %-10s", Lang.simpleMetodDesc(method), method.getReturnType().getSimpleName());
			else
				throw Lang.impossible();
			log.debugf("%s >> %50s | @Ok(%-5s) @Fail(%-5s) | by %d Filters | (I:%s/O:%s)",
					Strings.alignLeft(sb, 30, ' '),
					str,
					ai.getOkView(),
					ai.getFailView(),
					(null == ai.getFilterInfos() ? 0
							: ai.getFilterInfos().length),
					ai.getInputEncoding(),
					ai.getOutputEncoding());
		}
	}
}
