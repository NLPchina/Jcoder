package org.nlpcn.jcoder.run.java;

import com.google.common.collect.Sets;
import org.nlpcn.jcoder.domain.CodeInfo;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.CodeException;
import org.nlpcn.jcoder.run.CodeRuntimeException;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.run.annotation.Single;
import org.nlpcn.jcoder.run.rpc.Rpcs;
import org.nlpcn.jcoder.run.rpc.domain.RpcContext;
import org.nlpcn.jcoder.service.JarService;
import org.nlpcn.jcoder.util.ExceptionUtil;
import org.nlpcn.jcoder.util.MapCount;
import org.nlpcn.jcoder.util.StringUtil;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.lang.Mirror;
import org.nutz.mvc.Mvcs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map.Entry;

public class JavaRunner {

	private static final Logger LOG = LoggerFactory.getLogger(JavaRunner.class);
	private static final Object[] DEFAULT_ARG = new Object[0];
	private Task task = null;
	private CodeInfo codeInfo;
	private Object objInstance;

	public JavaRunner(Task task) {
		this.task = task;
		this.codeInfo = task.codeInfo();
	}

	/**
	 * compile to class if , class in task , it nothing to do!
	 *
	 * @return
	 * @throws CodeException
	 */
	public JavaRunner compile() {

		if (codeInfo.getClassz() != null) {
			return this;
		}

		synchronized (codeInfo) {
			if (codeInfo.getClassz() == null) try {

				String code = task.getCode();

				JarService jarService = JarService.getOrCreate(task.getGroupName());

				DynamicEngine de = jarService.getEngine();

				codeInfo.setioc(jarService.getIoc());

				JavaSourceUtil javaSourceUtil = new JavaSourceUtil(code);

				String pack = javaSourceUtil.getPackage();
				String className = javaSourceUtil.getClassName();

				LOG.info("to compile " + pack + "." + className);

				if (StringUtil.isBlank(pack)) {
					throw new CodeException("package 不能为空");
				}

				if (className == null) {
					throw new CodeException("not find className");
				}

				codeInfo.setClassLoader(de.getClassLoader());

				Class<?> clz = (Class<?>) de.javaCodeToClass(pack + "." + className, code);

				Single single = clz.getAnnotation(Single.class);

				codeInfo.setClassz(clz);

				if (single != null) {
					codeInfo.setSingle(single.value());
				}

				MapCount<String> mc = new MapCount<>();
				// set execute method
				for (Method method : clz.getMethods()) {
					if (!Modifier.isPublic(method.getModifiers()) || method.isBridge() || method.getDeclaringClass() != clz) {
						continue;
					}
					Execute execute = null;

					if ((execute = Mirror.getAnnotationDeep(method, Execute.class)) != null) { // 先default
						codeInfo.addMethod(method, Sets.newHashSet(Arrays.asList(execute.methods())), execute.rpc(), execute.restful());
						mc.add(method.getName());
					}
				}

				if (mc.size() == 0) {
					throw new CodeRuntimeException("you must set a @Execute annotation for execute");
				}

				StringBuilder sb = null;
				for (Entry<String, Double> entry : mc.get().entrySet()) {
					if (entry.getValue() > 1) {
						if (sb == null) {
							sb = new StringBuilder();
						}
						sb.append(entry.getKey() + " ");
					}
				}

				if (sb != null && sb.length() > 0) {
					sb.append(" Execute method name repetition");
					throw new CodeRuntimeException(sb.toString());
				}

				codeInfo.getDefaultMethod(); // 空取一下，如果报异常活该

				codeInfo.setClassz(clz);

			} catch (IOException | CodeException e) {
				LOG.debug("code compile err ", e);
				throw new CodeRuntimeException(e);
			}
		}

		return this;
	}

	/**
	 * instance and inject objcet if object is created , it nothing to do !
	 *
	 * @return
	 */
	public JavaRunner instance() {

		if (!codeInfo.isSingle()) {// if not single .it only instance by run
			_instance();
			return this;
		}

		if (codeInfo.getJavaObject() != null && !codeInfo.iocChanged(task.getGroupName())) {
			this.objInstance = codeInfo.getJavaObject();
			return this;
		}

		synchronized (codeInfo) {
			if (codeInfo.getJavaObject() == null || codeInfo.iocChanged(task.getGroupName())) {
				_instance();
			} else {
				this.objInstance = codeInfo.getJavaObject();
			}
		}

		return this;
	}

	private void _instance() {

		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

		Ioc contextIoc = Mvcs.getIoc();


		try {
			LOG.info("to instance with ioc className: " + codeInfo.getClassz().getName());

			/**
			 * 设置groupname
			 */
			RpcContext ctx = Rpcs.ctx();
			if (ctx.getGroupName() == null) {
				ctx.setGroupName(task.getGroupName());
			}

			Thread.currentThread().setContextClassLoader(codeInfo.getClassLoader());

			Mvcs.setIoc(codeInfo.getIoc());

			objInstance = codeInfo.getClassz().newInstance();

			Mirror<?> mirror = Mirror.me(codeInfo.getClassz());

			for (Field field : mirror.getFields()) {
				Inject inject = field.getAnnotation(Inject.class);
				if (inject != null) {
					field.setAccessible(true);
					if (field.getType().equals(org.slf4j.Logger.class)) {
						mirror.setValue(objInstance, field, LoggerFactory.getLogger(codeInfo.getClassz()));
					} else {
						mirror.setValue(objInstance, field, codeInfo.getIoc().get(field.getType(), StringUtil.isBlank(inject.value()) ? field.getName() : inject.value()));
					}
					field.setAccessible(false);
				}
			}

			if (codeInfo.isSingle()) {
				codeInfo.setJavaObject(objInstance);
			}
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new CodeRuntimeException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
			Mvcs.setIoc(contextIoc);
		}
	}

	/**
	 * @return
	 */
	public Task getTask() {
		return this.task;
	}

	/**
	 * execte task defaultExecute if not found , it execute excutemehtod ， if not found it throw Exception
	 *
	 * @return
	 * @throws CodeException
	 */
	public Object execute() {
		return execute(this.codeInfo.getDefaultMethod().getMethod(), DEFAULT_ARG);
	}

	/**
	 * execte task defaultExecute if not found , it execute excutemehtod ， if not found it throw Exception
	 *
	 * @return
	 * @throws CodeException
	 */
	public Object execute(Method method, Object[] args) {
		long start = System.currentTimeMillis();

		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

		Ioc contextIoc = Mvcs.getIoc();

		try {
			Thread.currentThread().setContextClassLoader(codeInfo.getClassLoader());
			Mvcs.setIoc(codeInfo.getIoc());
			Object invoke = method.invoke(objInstance, args);
			String endInfo = "Execute OK  " + task.getName() + "/" + method.getName() + " succesed ! use Time : " + (System.currentTimeMillis() - start);
			LOG.info(endInfo);
			this.task.updateSuccess();
			return invoke;
		} catch (Exception e) {
			this.task.updateError();
			LOG.error("Execute ERR  " + task.getName() + "/" + method.getName() + " useTime " + (System.currentTimeMillis() - start) + " erred : " + ExceptionUtil.printStackTraceWithOutLine(e));
			e.printStackTrace();
			throw new CodeRuntimeException(ExceptionUtil.realException(e));
		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
			Mvcs.setIoc(contextIoc);
		}
	}

	/**
	 * to compile it and validate some function
	 *
	 * @return it always return true
	 * @throws CodeException
	 * @throws IOException
	 */
	public boolean check() throws CodeException, IOException {
		String code = task.getCode();

		DynamicEngine de = JarService.getOrCreate(task.getGroupName()).getEngine();

		JavaSourceUtil javaSourceUtil = new JavaSourceUtil(code);

		String pack = javaSourceUtil.getPackage();

		String className = javaSourceUtil.getClassName();

		if (className == null) {
			throw new CodeException("not find className");
		}

		Class<?> clz = (Class<?>) de.javaCodeToClass(pack + "." + className, code);

		MapCount<String> mc = new MapCount<>();
		// set execute method
		for (Method method : clz.getMethods()) {
			if (!Modifier.isPublic(method.getModifiers()) || method.isBridge() || method.getDeclaringClass() != clz) {
				continue;
			}

			if (Mirror.getAnnotationDeep(method, Execute.class) != null) { // 先default
				mc.add(method.getName());
			}
		}

		if (mc.size() == 0) {
			throw new CodeRuntimeException("you must set a Execute annotation for execute");
		}

		StringBuilder sb = null;
		for (Entry<String, Double> entry : mc.get().entrySet()) {
			if (entry.getValue() > 1) {
				if (sb == null) {
					sb = new StringBuilder();
				}
				sb.append(entry.getKey() + " ");
			}
		}

		if (sb != null && sb.length() > 0) {
			sb.append(" Execute method name repetition");
			throw new CodeRuntimeException(sb.toString());
		}

		return true;

	}


}
