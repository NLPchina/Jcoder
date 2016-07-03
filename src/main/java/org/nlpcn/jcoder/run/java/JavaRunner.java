package org.nlpcn.jcoder.run.java;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.nlpcn.commons.lang.util.MapCount;
import org.nlpcn.commons.lang.util.StringUtil;
import org.nlpcn.jcoder.domain.CodeInfo;
import org.nlpcn.jcoder.domain.Task;
import org.nlpcn.jcoder.run.CodeException;
import org.nlpcn.jcoder.run.CodeRuntimeException;
import org.nlpcn.jcoder.run.annotation.DefaultExecute;
import org.nlpcn.jcoder.run.annotation.Execute;
import org.nlpcn.jcoder.run.annotation.Single;
import org.nlpcn.jcoder.util.StaticValue;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.lang.Mirror;

public class JavaRunner {

	private static final Logger LOG = Logger.getLogger(JavaRunner.class);

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

		if (codeInfo.getClassz() != null || codeInfo.classLoaderChanged()) {
			return this;
		}

		synchronized (codeInfo) {
			if (codeInfo.getClassz() == null) {
				try {

					String code = task.getCode();

					DynamicEngine de = DynamicEngine.getInstance();

					String pack = JavaSourceUtil.findPackage(code);

					String className = JavaSourceUtil.findClassName(code);

					if (className == null) {
						throw new CodeException("not find className");
					}

					codeInfo.setClassLoader(de.getParentClassLoader());

					Class<?> clz = (Class<?>) de.javaCodeToClass(pack + "." + className, code);

					Single single = clz.getAnnotation(Single.class);

					if (single != null) {
						codeInfo.setSingle(single.value());
					}

					MapCount<String> mc = new MapCount<>();
					// set execute method
					for (Method method : clz.getMethods()) {
						if (!Modifier.isPublic(method.getModifiers()) || method.isBridge() || method.getDeclaringClass() != clz) {
							continue;
						}

						if (Mirror.getAnnotationDeep(method, DefaultExecute.class) != null) {
							codeInfo.setDefaultMethod(method);
							mc.add(method.getName());
						} else if (Mirror.getAnnotationDeep(method, Execute.class) != null) { // 先default
							codeInfo.addMethod(method);
							mc.add(method.getName());
						}
					}

					if (mc.size() == 0) {
						throw new CodeRuntimeException("you must set a Execute or DefaultExecute annotation for execute");
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
					e.printStackTrace();
					throw new CodeRuntimeException(e);
				}
			}
		}

		return this;
	}

	/**
	 * instance objcet if object is created , it nothing to do !
	 * 
	 * @return
	 * @throws CodeException
	 */
	public JavaRunner instanceObj() {

		if (codeInfo.isSingle() && codeInfo.getJavaObject() != null) {
			objInstance = codeInfo.getJavaObject();
			return this;
		}

		if (codeInfo.isSingle()) {
			synchronized (codeInfo) {
				if (codeInfo.getJavaObject() == null) {
					instanceWithOutIoc();
				}
			}
		} else {
			instanceWithOutIoc();
		}

		return this;
	}

	private void instanceWithOutIoc() {
		try {
			this.objInstance = codeInfo.getClassz().newInstance();
			if (codeInfo.isSingle()) {
				codeInfo.setJavaObject(objInstance);
			}
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new CodeRuntimeException(e);
		}
	}

	/**
	 * instance and inject objcet if object is created , it nothing to do !
	 * 
	 * @return
	 */
	public JavaRunner instanceObjByIoc() {
		if (codeInfo.isSingle() && codeInfo.getJavaObject() != null && !codeInfo.iocChanged()) {
			this.objInstance = codeInfo.getJavaObject();
			return this;
		}

		if (codeInfo.isSingle()) {
			synchronized (codeInfo) {
				if (codeInfo.getJavaObject() == null) {
					instanceWithIoc();
				}
			}
		} else {
			instanceWithIoc();
		}

		return this;
	}

	private void instanceWithIoc() {
		try {

			objInstance = codeInfo.getClassz().newInstance();

			Ioc ioc = StaticValue.getUserIoc();

			codeInfo.setioc(ioc);

			Mirror<?> mirror = Mirror.me(codeInfo.getClassz());

			for (Field field : mirror.getFields()) {
				Inject inject = field.getAnnotation(Inject.class);
				if (inject != null) {
					if (field.getType().equals(Logger.class)) {
						mirror.setValue(objInstance, field, Logger.getLogger(codeInfo.getClassz()));
					} else {
						mirror.setValue(objInstance, field, ioc.get(field.getType(), StringUtil.isBlank(inject.value()) ? field.getName() : inject.value()));
					}
				}
			}
			if (codeInfo.isSingle()) {
				codeInfo.setJavaObject(objInstance);
			}
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			throw new CodeRuntimeException(e);
		}
	}

	/**
	 * @return
	 */
	public Task getTask() {
		return this.task;
	}

	private static final Object[] DEFAULT_ARG = new Object[0];

	/**
	 * execte task defaultExecute if not found , it execute excutemehtod ， if
	 * not found it throw Exception
	 * 
	 * @return
	 * 
	 * @throws CodeException
	 */
	public Object execute() {
		try {
			Object invoke = this.codeInfo.getDefaultMethod().invoke(this.codeInfo.getJavaObject(), DEFAULT_ARG);
			this.task.updateSuccess();
			return invoke;
		} catch (Exception e) {
			e.printStackTrace();
			this.task.updateError();
			throw new CodeRuntimeException(e);
		}
	}

	/**
	 * execte task defaultExecute if not found , it execute excutemehtod ， if
	 * not found it throw Exception
	 * 
	 * @return
	 * 
	 * @throws CodeException
	 */
	public Object execute(Method method, Object[] args) {
		try {
			Object invoke = method.invoke(this.codeInfo.getJavaObject(), args);
			this.task.updateSuccess();
			return invoke;
		} catch (Exception e) {
			e.printStackTrace();
			this.task.updateError();
			throw new CodeRuntimeException(e);
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

		DynamicEngine de = DynamicEngine.getInstance();

		String pack = JavaSourceUtil.findPackage(code);

		String className = JavaSourceUtil.findClassName(code);

		if (className == null) {
			throw new CodeException("not find className");
		}

		Class<?> clz = (Class<?>) de.javaCodeToClass(pack + "." + className, code);

		clz.getAnnotation(Single.class);

		MapCount<String> mc = new MapCount<>();
		// set execute method
		for (Method method : clz.getMethods()) {
			if (!Modifier.isPublic(method.getModifiers()) || method.isBridge() || method.getDeclaringClass() != clz) {
				continue;
			}

			if (Mirror.getAnnotationDeep(method, DefaultExecute.class) != null) {
				mc.add(method.getName());
			} else if (Mirror.getAnnotationDeep(method, Execute.class) != null) { // 先default
				mc.add(method.getName());
			}
		}

		if (mc.size() == 0) {
			throw new CodeRuntimeException("you must set a Execute or DefaultExecute annotation for execute");
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
