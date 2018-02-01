package org.nlpcn.jcoder.run.annotation;

import java.lang.annotation.*;

/**
 * is object Singleton is not set it is true
 *
 * @author ansj
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Single {
	boolean value() default true;
}
