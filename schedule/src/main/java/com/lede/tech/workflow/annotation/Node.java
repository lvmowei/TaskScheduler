package com.lede.tech.workflow.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(
{ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Node
{
	String name();

	String next() default "";

	String previous() default "";

	long fixedDelay() default 0L;

	int retryTimes() default 0;

	long retryDelay() default 0L;

	/**不能含有()[]
	 * @return
	 */
	String preCondition() default "";

	/**不能含有()[]
	 * @return
	 */
	String postCondition() default "";

	String delay() default "";
}
