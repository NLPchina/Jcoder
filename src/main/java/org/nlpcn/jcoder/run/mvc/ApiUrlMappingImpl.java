package org.nlpcn.jcoder.run.mvc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.nlpcn.jcoder.domain.CodeInfo;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.java.JavaRunner;
import org.nlpcn.jcoder.service.TaskService;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.ActionChain;
import org.nutz.mvc.ActionChainMaker;
import org.nutz.mvc.ActionContext;
import org.nutz.mvc.ActionInfo;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.RequestPath;
import org.nutz.mvc.UrlMapping;
import org.nutz.mvc.annotation.BlankAtException;
import org.nutz.mvc.impl.ActionInvoker;
import org.nutz.mvc.impl.Loadings;

public class ApiUrlMappingImpl implements UrlMapping {

	private static final Log log = Logs.get();

	protected Map<String, ActionInvoker> map;

	public ApiUrlMappingImpl() {
		this.map = new HashMap<String, ActionInvoker>();
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
			ActionInvoker invoker = map.get(path);

			// 如果没有增加过这个 URL 的调用者，为其创建备忘记录，并加入索引
			if (null == invoker) {
				invoker = new ActionInvoker();
				map.put(path, invoker);
				// 记录一下方法与 url 的映射
				config.getAtMap().addMethod(path, ai.getMethod());
			}

			// 将动作链，根据特殊的 HTTP 方法，保存到调用者内部
			if (ai.isForSpecialHttpMethod()) {
				for (String httpMethod : ai.getHttpMethods())
					invoker.addChain(httpMethod, chain);
			}
			// 否则，将其设置为默认动作链
			else {
				invoker.setDefaultChain(chain);
			}
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

	public ActionInvoker getOrCreate(NutConfig config, ActionContext ac) {
		RequestPath rp = Mvcs.getRequestPathObject(ac.getRequest());

		String path = rp.getPath();

		int len = path.length();

		if (path.charAt(len - 1) == '/') {
			path = path.substring(0, len - 1);
		}

		ac.setSuffix(rp.getSuffix());

		ActionInvoker invoker = map.get(path);

		if (invoker == null) {
			invoker = createInvoker(config, path);
		}

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

	private void setActionSomeArgs(ActionContext ac, String path) {
		ac.setPath(path);
		ac.setPathArgs(new LinkedList<String>());
	}

	private ActionInvoker createInvoker(NutConfig config, String path) {
		String[] split = path.split("/");
		Task task = TaskService.findTaskByCache(split[2]);

		if (task != null && task.getStatus() == 1) {

			CodeInfo codeInfo = new JavaRunner(task).compile().instance().getTask().codeInfo();

			ApiActionChainMaker aacm = Loadings.evalObj(config, ApiActionChainMaker.class, new String[] {});

			Class<?> module = codeInfo.getClassz();

			Method dm = codeInfo.getDefaultMethod();

			for (Method method : codeInfo.getExecuteMethods()) {
				// 增加到映射中
				ActionInfo info = ApiLoadings.createInfo(method);

				info.setModuleType(module);

				if (dm == method) {
					info.setPaths(new String[] { "/api/" + task.getName() + "/" + method.getName(), "/api" + "/" + task.getName() });
				} else {
					info.setPaths(new String[] { "/api/" + task.getName() + "/" + method.getName() });
				}

				this.add(aacm, info, config);
			}
		}

		return map.get(path);
	}

	/**
	 * 从映射表中删除一个api
	 */
	public void remove(String name) {
		Iterator<Entry<String, ActionInvoker>> iterator = map.entrySet().iterator();
		String path = null;
		synchronized (map) {
			while (iterator.hasNext()) {
				if ((path = iterator.next().getKey()).startsWith("/api/" + name + "/") || path.equals("/api/" + name)) {
					iterator.remove();
					log.info("remove api " + path);
				}
			}
		}

	}

	@Override
	public void add(String path, ActionInvoker invoker) {
		map.put(path, invoker);
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
			log.debugf("%s >> %s | @Ok(%-5s) @Fail(%-5s) | by %d Filters | (I:%s/O:%s)", Strings.alignLeft(sb, 30, ' '), str, ai.getOkView(), ai.getFailView(),
					(null == ai.getFilterInfos() ? 0 : ai.getFilterInfos().length), ai.getInputEncoding(), ai.getOutputEncoding());
		}
	}
}
