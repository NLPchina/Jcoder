package org.nlpcn.jcoder.run.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * make cache by action, if you used it , it only use method args by cache key ,
 * so not use request or response !
 * 
 * @author ansj
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface Cache {

	int time() default 10; // SECONDS

	int size() default 1000;

	boolean block() default true;

}
