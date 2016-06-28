package org.nlpcn.jcoder.run.execute;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  execute function use it by classname/funname
 * 
 * @author ansj
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Execute {
	
	/**
     * 需要映射的HTTP方法,例如POST GET 等等
     */
    String[] methods() default {};
}
