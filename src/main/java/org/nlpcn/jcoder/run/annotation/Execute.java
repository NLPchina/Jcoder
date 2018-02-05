package org.nlpcn.jcoder.run.annotation;

import java.lang.annotation.*;

/**
 * execute function use it by classname/funname
 *
 * @author ansj
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Execute {

	/**
	 * 需要映射的HTTP方法,例如POST GET 等等
	 */
	String[] methods() default {};

	/**
	 * is publish as restful
	 *
	 * @return true
	 */
	boolean restful() default true;

	/**
	 * is publish as rpc
	 *
	 * @return true
	 */
	boolean rpc() default true;
}
