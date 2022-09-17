package test.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(TestRuns.class)
public @interface TestRun {
	
	/**
	 * @return the input arguments
	 */
	public String argument() default "";
	
	/**
	 * @return the expected toString result of the parsed argument array
	 */
	public String result() default "";
	
	/**
	 * @return whether or not the test is expected to succeed or not
	 */
	public boolean success();
	
}